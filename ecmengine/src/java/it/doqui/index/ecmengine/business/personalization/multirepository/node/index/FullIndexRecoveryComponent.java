/* Index ECM Engine - A system for managing the capture (when created 
 * or received), classification (cataloguing), storage, retrieval, 
 * revision, sharing, reuse and disposition of documents.
 *
 * Copyright (C) 2008 Regione Piemonte
 * Copyright (C) 2008 Provincia di Torino
 * Copyright (C) 2008 Comune di Torino
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
 
package it.doqui.index.ecmengine.business.personalization.multirepository.node.index;

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.MultiTTenantAdminService;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FullIndexRecoveryComponent extends AbstractReindexComponent {
	
    private static final String ERR_INDEX_OUT_OF_DATE = "index.recovery.out_of_date";
    private static final String MSG_RECOVERY_STARTING = "index.recovery.starting";
    private static final String MSG_RECOVERY_COMPLETE = "index.recovery.complete";
    private static final String MSG_RECOVERY_PROGRESS = "index.recovery.progress";
    private static final String MSG_RECOVERY_TERMINATED = "index.recovery.terminated";
    private static final String MSG_RECOVERY_ERROR = "index.recovery.error";
    
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);
    
    public static enum RecoveryMode {
        /** Do nothing - not even a check. */
        NONE,
        /**
         * Perform a quick check on the state of the indexes only.  This only checks that the
         * first N transactions are present in the index and doesn't guarantee that the indexes
         * are wholely consistent.  Normally, the indexes are consistent up to a certain time.
         * The system does a precautionary index top-up by default, so the last transactions are
         * not validated.
         */
        VALIDATE,
        /**
         * Performs a validation and starts a recovery if necessary.  In this mode, if start
         * transactions are missing then FULL mode is enabled.  If end transactions are missing
         * then the indexes will be "topped up" to bring them up to date.
         */
        AUTO,
        /**
         * Performs a full pass-through of all recorded transactions to ensure that the indexes
         * are up to date.
         */
        FULL;
    }
    
    private boolean lockServer;
    private IndexTransactionTracker indexTracker;
    private boolean stopOnError;
    
    /**
     * <ul>
     *   <li><b>recoveryMode: </b>VALIDATE</li>
     *   <li><b>stopOnError:</b> true</li>
     * </ul>
     *
     */
    public FullIndexRecoveryComponent() {}

	public void setTenantAdminService(MultiTTenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}
    
    public void setRecoveryMode(String recoveryMode) {}

    /**
     * Set this on to put the server into READ-ONLY mode for the duration of the index recovery.
     * The default is <tt>true</tt>, i.e. the server will be locked against further updates.
     * 
     * @param lockServer true to force the server to be read-only
     */
    public void setLockServer(boolean lockServer) {
        this.lockServer = lockServer;
    }

    /**
     * Set the tracker that will be used for AUTO mode.
     * 
     * @param indexTracker      an index tracker component
     */
    public void setIndexTracker(IndexTransactionTracker indexTracker)
    {
        this.indexTracker = indexTracker;
    }

    /**
     * Set whether a full rebuild should stop in the event of encoutering an error.  The default is
     * to stop reindexing, and this will lead to the server startup failing when index recovery mode
     * is <b>FULL</b>.  Sometimes, it is necessary to start the server up regardless of any errors
     * with particular nodes.
     * 
     * @param stopOnError       <tt>true</tt> to stop reindexing when an error is encountered.
     */
    public void setStopOnError(boolean stopOnError)
    {
        this.stopOnError = stopOnError;
    }

    @Override
    protected void reindexImpl() {
    	logger.debug("[FullIndexRecoveryComponent::reindexImpl] BEGIN");

		try {
			final String currentRepository = RepositoryManager.getCurrentRepository();
			String recoveryModeString = RepositoryManager.getInstance().getRepository(currentRepository).getIndexRecoveryMode();
			if (recoveryModeString == null) { // Default: validate
				recoveryModeString = "VALIDATE";
			}
			RecoveryMode recoveryMode = RecoveryMode
					.valueOf(recoveryModeString);
			if (logger.isDebugEnabled()) {
				logger.debug("[FullIndexRecoveryComponent::reindexImpl] " +
						"Repository '" + currentRepository + "' -- " +
						"Performing index recovery for type: " + recoveryMode);
			}
	        // Ignore when NONE
			if (recoveryMode == RecoveryMode.NONE) {
				return;
			}

			// put the server into read-only mode for the duration
			boolean allowWrite = !transactionService.isReadOnly();
			try {
				if (lockServer) {
					// set the server into read-only mode
					transactionService.setAllowWrite(false);
				}

				// do we need to perform a full recovery
	            // Check that the first and last meaningful transactions are indexed 
	            List<Transaction> startTxns = nodeDaoService.getTxnsByCommitTimeAscending(
	                    Long.MIN_VALUE, Long.MAX_VALUE, 10, null);
	            boolean startAllPresent = areTxnsInIndex(startTxns);
	            List<Transaction> endTxns = nodeDaoService.getTxnsByCommitTimeDescending(
	                    Long.MIN_VALUE, Long.MAX_VALUE, 10, null);
	            boolean endAllPresent = areTxnsInIndex(endTxns);
	            
	            // check the level of cover required
	            switch (recoveryMode)
	            {
	            case AUTO:
	                if (!startAllPresent)
	                {
	                    // Initial transactions are missing - rebuild
	                    performFullRecovery();
	                }
	                else if (!endAllPresent)
	                {
	                    // Trigger the tracker, which will top up the indexes
	                    indexTracker.reindex();
	                }
	                break;
	            case VALIDATE:
	                // Check
	                if (!startAllPresent || !endAllPresent)
	                {
	                    // Index is out of date
	                    logger.warn(I18NUtil.getMessage(ERR_INDEX_OUT_OF_DATE));
	                }
	                break;
	            case FULL:
	                performFullRecovery();
	                break;
				}
			} finally {
				// restore read-only state
				transactionService.setAllowWrite(allowWrite);
			}
		} finally {
			logger.debug("[FullIndexRecoveryComponent::reindexImpl] END");
		}  
    }
    
    private static final int MAX_TRANSACTIONS_PER_ITERATION = 1000;
    private void performFullRecovery() {
        logger.debug("[FullIndexRecoveryComponent::performFullRecovery] BEGIN");
		try {
			int txnCount = nodeDaoService.getTransactionCount();
			// starting
			String msgStart = I18NUtil.getMessage(MSG_RECOVERY_STARTING,
					txnCount);
			logger.info(msgStart);
			// count the transactions
			int processedCount = 0;
	        long fromTimeInclusive = Long.MIN_VALUE;
	        long toTimeExclusive = Long.MAX_VALUE;
	        List<Long> lastTxnIds = Collections.<Long>emptyList();
			while (true) {
	            List<Transaction> nextTxns = nodeDaoService.getTxnsByCommitTimeAscending(
	                    fromTimeInclusive,
	                    toTimeExclusive,
	                    MAX_TRANSACTIONS_PER_ITERATION,
	                    lastTxnIds);

	            lastTxnIds = new ArrayList<Long>(nextTxns.size());

				// reindex each transaction
				for (Transaction txn : nextTxns) {
					Long txnId = txn.getId();
	                // Keep it to ensure we exclude it from the next iteration
	                lastTxnIds.add(txnId);
					// check if we have to terminate
					if (isShuttingDown()) {
						String msgTerminated = I18NUtil
								.getMessage(MSG_RECOVERY_TERMINATED);
						logger.warn(msgTerminated);
						return;
					}

	                // Allow exception to bubble out or not
	                if (stopOnError)
	                {
	                    reindexTransaction(txnId);
	                }
	                else
	                {
	                    try
	                    {
	                        reindexTransaction(txnId);
	                    }
	                    catch (Throwable e)
	                    {
	                        String msgError = I18NUtil.getMessage(MSG_RECOVERY_ERROR, txnId, e.getMessage());
	                        logger.info(msgError, e);
	                    }
	                }
	                // Although we use the same time as this transaction for the next iteration, we also
	                // make use of the exclusion list to ensure that it doesn't get pulled back again.
	                fromTimeInclusive = txn.getCommitTimeMs();

					// dump a progress report every 10% of the way
					double before = (double) processedCount / (double) txnCount
							* 10.0; // 0 - 10 
					processedCount++;
					double after = (double) processedCount / (double) txnCount
							* 10.0; // 0 - 10
					if (Math.floor(before) < Math.floor(after)) // crossed a 0 - 10 integer boundary
					{
						int complete = ((int) Math.floor(after)) * 10;
						String msgProgress = I18NUtil.getMessage(
								MSG_RECOVERY_PROGRESS, complete);
						logger.info(msgProgress);
					}
				}

				// have we finished?
				if (nextTxns.size() == 0) {
					// there are no more
					break;
				}

			}
			// done
			String msgDone = I18NUtil.getMessage(MSG_RECOVERY_COMPLETE);
			logger.info(msgDone);
		} finally {
			logger.debug("[FullIndexRecoveryComponent::performFullRecovery] END");
		}
    }
    
    /**
     * Perform a full reindexing of the given transaction in the context of a completely
     * new transaction.
     * 
     * @param txnId the transaction identifier
     */
    public void reindexTransaction(final long txnId) {
        logger.debug("[FullIndexRecoveryComponent::reindexTransaction] BEGIN");

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("[FullIndexRecoveryComponent::reindexTransaction] " +
						"Repository '" + RepositoryManager.getCurrentRepository() + 
						"' -- Reindexing transaction: " + txnId);
			}
			RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>() {
				public Object execute() throws Exception {
					// get the node references pertinent to the transaction
					List<NodeRef> nodeRefs = nodeDaoService
							.getTxnChanges(txnId);
					// reindex each node
					for (NodeRef nodeRef : nodeRefs) {
						Status nodeStatus = nodeService.getNodeStatus(nodeRef);
						if (nodeStatus == null) {
							// it's not there any more
							continue;
						}
						if (nodeStatus.isDeleted()) { // node deleted
							// only the child node ref is relevant
							ChildAssociationRef assocRef = new ChildAssociationRef(
									ContentModel.ASSOC_CHILDREN, null, null,
									nodeRef);
							indexer.deleteNode(assocRef);
						} else { // node created
							// reindex
							indexer.updateNode(nodeRef);
						}
					}
					// done
					return null;
				}
			};
	        transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true, true);
			// done
		} finally {
			logger.debug("[FullIndexRecoveryComponent::reindexTransaction] END");
		}
    }
}
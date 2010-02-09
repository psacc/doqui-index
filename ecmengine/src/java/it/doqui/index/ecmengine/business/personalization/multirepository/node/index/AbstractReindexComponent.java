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

import it.doqui.index.ecmengine.business.foundation.repository.TenantAdminSvc;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;
import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.MultiTTenantAdminService;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts.RepositoryAwareFullTextSearchIndexer;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;
import it.doqui.index.ecmengine.exception.repository.TenantRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import net.sf.acegisecurity.Authentication;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.node.index.IndexRecovery;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
//import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractReindexComponent implements IndexRecovery
{
	private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);

	/** kept to notify the thread that it should quit */
	private static VmShutdownListener vmShutdownListener = new VmShutdownListener("IndexRecovery");

	private AuthenticationComponent authenticationComponent;

	/** provides transactions to atomically index each missed transaction */
	protected TransactionServiceImpl transactionService;

	/** the component to index the node hierarchy */
	protected Indexer indexer;

	/** the FTS indexer that we will prompt to pick up on any un-indexed text */
	protected RepositoryAwareFullTextSearchIndexer ftsIndexer;

	/** the component providing searches of the indexed nodes */
	protected SearchService searcher;

	/** the component giving direct access to <b>store</b> instances */
	protected NodeService nodeService;

	/** the component giving direct access to <b>transaction</b> instances */
	protected NodeDaoService nodeDaoService;

	protected MultiTTenantAdminService tenantAdminService;

	private volatile boolean shutdown;
	private final Map<String, WriteLock> indexerWriteLockMap;

	public AbstractReindexComponent()
	{
		shutdown = false;
		indexerWriteLockMap = new HashMap<String, WriteLock>(RepositoryManager.getInstance().getRepositories().size());

		for (Repository repository : RepositoryManager.getInstance().getRepositories()) {
			ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

			indexerWriteLockMap.put(repository.getId(), readWriteLock.writeLock());
		}
	}

	/**
	 * Convenience method to get a common write lock.  This can be used to avoid
	 * concurrent access to the work methods.
	 */
	protected WriteLock getIndexerWriteLock() {
		// Restitusco il lock relativo al repository corrente
		WriteLock lock = indexerWriteLockMap.get(RepositoryManager.getCurrentRepository());

		return lock;
	}

	/**
	 * Programmatically notify a reindex thread to terminate
	 *
	 * @param shutdown true to shutdown, false to reset
	 */
	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	/**
	 *
	 * @return Returns true if the VM shutdown hook has been triggered, or the instance
	 *      was programmatically {@link #shutdown shut down}
	 */
	protected boolean isShuttingDown() {
		return shutdown || vmShutdownListener.isVmShuttingDown();
	}

	/**
	 * @param authenticationComponent ensures that reindexing operates as system user
	 */
	public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
		this.authenticationComponent = authenticationComponent;
	}

	/**
	 * Set the low-level transaction component to use
	 *
	 * @param transactionService provide transactions to index each missed transaction
	 */
	public void setTransactionService(TransactionServiceImpl transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * @param indexer the indexer that will be index
	 */
	public void setIndexer(Indexer indexer)
	{
		this.indexer = indexer;
	}

	/**
	 * @param ftsIndexer the FTS background indexer
	 */
	public void setFtsIndexer(RepositoryAwareFullTextSearchIndexer ftsIndexer)
	{
		this.ftsIndexer = ftsIndexer;
	}

	/**
	 * @param searcher component providing index searches
	 */
	public void setSearcher(SearchService searcher)
	{
		this.searcher = searcher;
	}

	/**
	 * @param nodeService provides information about nodes for indexing
	 */
	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	/**
	 * @param nodeDaoService provides access to transaction-related queries
	 */
	public void setNodeDaoService(NodeDaoService nodeDaoService)
	{
		this.nodeDaoService = nodeDaoService;
	}

	/**
	 * Perform the actual work.  This method will be called as the system user
	 * and within an existing transaction.  This thread will only ever be accessed
	 * by a single thread per instance.
	 *
	 */
	protected abstract void reindexImpl();

	/**
	 * If this object is currently busy, then it just nothing
	 */
	public final void reindex() {

		PropertyCheck.mandatory(this, "authenticationComponent", this.authenticationComponent);
		PropertyCheck.mandatory(this, "ftsIndexer", this.ftsIndexer);
		PropertyCheck.mandatory(this, "indexer", this.indexer);
		PropertyCheck.mandatory(this, "searcher", this.searcher);
		PropertyCheck.mandatory(this, "nodeService", this.nodeService);
		PropertyCheck.mandatory(this, "nodeDaoService", this.nodeDaoService);
		PropertyCheck.mandatory(this, "transactionComponent", this.transactionService);

		//MB: rimosso il loop sui repository
		//
		//for (Repository repository : RepositoryManager.getInstance().getRepositories()) {
		//RepositoryManager.setCurrentRepository(repository.getId());
        if (logger.isDebugEnabled()) {
	    	logger.debug("[AbstractReindexComponent::reindex] Reindexing repository: " +RepositoryManager.getCurrentRepository());
        }

		if (indexerWriteLockMap.get(RepositoryManager.getCurrentRepository()).tryLock()) {
			Authentication auth = null;

			try {
				auth = AuthenticationUtil.getCurrentAuthentication();

				// authenticate as the system user
				authenticationComponent.setSystemUserAsCurrentUser();
				RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>() {
					public Object execute() throws Exception {

						reindexImpl();
						return null;
					}
				};

				transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true);
			} finally {
				try {
					indexerWriteLockMap.get(RepositoryManager.getCurrentRepository()).unlock();
				} catch (Throwable e) {
					// noop
				}

				if (auth != null) {
					authenticationComponent.setCurrentAuthentication(auth);
				}
			}
			// done
			if (logger.isDebugEnabled()) {
				logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
						"' -- Reindex work completed: " + this);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
						"' -- Bypassed reindex work - already busy: " + this);
			}
		} // End if trylock()
		//} // End for loop
	}

	protected enum InIndex {
		YES, NO, INDETERMINATE;
	}

	/**
	 * Determines if a given transaction is definitely in the index or not.
	 *
	 * @param txnId     a specific transaction
	 * @return          Returns <tt>true</tt> if the transaction is definitely in the index
	 */
	protected InIndex isTxnIdPresentInIndex(long txnId) {
		if (logger.isDebugEnabled()) {
			logger.debug("[AbstractReindexComponent::isTxnIdPresentInIndex] " +
					"Repository '" + RepositoryManager.getCurrentRepository() + "' -- " +
					"Checking for transaction in index: " + txnId);
		}

		Transaction txn = nodeDaoService.getTxnById(txnId);
		if (txn == null) {
			return InIndex.YES;
		}

		// count the changes in the transaction
		int updateCount = nodeDaoService.getTxnUpdateCount(txnId);
		int deleteCount = nodeDaoService.getTxnDeleteCount(txnId);
		if (logger.isDebugEnabled()) {
			logger.debug("[AbstractReindexComponent::isTxnIdPresentInIndex] " +
					"Repository '" + RepositoryManager.getCurrentRepository() + "' -- " +
					"Transaction " + txnId + " has " + updateCount + " updates and " + deleteCount + " deletes.");
		}


		InIndex result = InIndex.NO;
		if (updateCount == 0 && deleteCount == 0) {
			// If there are no update or deletes, then it is impossible to know if the transaction was removed
			// from the index or was never there in the first place.
			result = InIndex.INDETERMINATE;
		} else {
			// get the stores
			List<StoreRef> storeRefs = nodeService.getStores();
			if(tenantAdminService==null){
				logger.debug("[AbstractReindexComponent::isTxnIdPresentInIndex] tenantAdminService not setted: skipping tenant detection.");
			} else {
				/*
				 * AF: Nel caso in cui stiamo facendo un controllo in fase di startup per l'integrità degli indici, verifichiamo la coerenza delle
				 * transazioni su ogni tenant di ogni store.
				 */
			    List<Tenant> tenants=tenantAdminService.getAllTenantsDoqui();
				if(tenants!=null && tenants.size()!=0){
					List<StoreRef> newStoreRefs=nodeService.getStores();
					for(int i=0;i<tenants.size();i++){
						Tenant ttenant=tenants.get(i);
						if(ttenant.isEnabled()){
							for(int j=0;j<storeRefs.size();j++){
								StoreRef tstoreRef=storeRefs.get(j);
								StoreRef newStoreRef=new StoreRef(tstoreRef.getProtocol(),"@"+ttenant.getTenantDomain()+"@"+tstoreRef.getIdentifier());
								newStoreRefs.add(newStoreRef);
							}
						}
					}
					storeRefs=newStoreRefs;
				}
			}
			for (StoreRef storeRef : storeRefs) {
				boolean inStore = isTxnIdPresentInIndex(storeRef, txn, updateCount, deleteCount);
				if (inStore) {
					// found in a particular store
					result = InIndex.YES;
					break;
				}
			}
		}
		// done
		if (logger.isDebugEnabled()) {
			logger.debug("[AbstractReindexComponent::isTxnIdPresentInIndex] " +
					"Repository '" + RepositoryManager.getCurrentRepository() + "' -- " +
					"Transaction " + txnId + " present in indexes: " + result);
		}
		return result;
	}

	/**
	 * @param updateCount       the number of node updates in the transaction
	 * @param deleteCount       the number of node deletions in the transaction
	 * @return                  Returns true if the given transaction is indexed,
	 *                          or if there are no updates or deletes
	 */
	private boolean isTxnIdPresentInIndex(StoreRef storeRef, Transaction txn, int updateCount, int deleteCount) {

		final long txnId = txn.getId();
		final String changeTxnId = txn.getChangeTxnId();

		// do the most update check, which is most common
		if (updateCount > 0) {

			ResultSet results = null;
			try {
				SearchParameters sp = new SearchParameters();
				sp.addStore(storeRef);

				// search for it in the index, sorting with youngest first, fetching only 1
				sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				sp.setQuery("TX:" + LuceneQueryParser.escape(changeTxnId));
				sp.setLimit(1);

				results = searcher.query(sp);

				if (results.length() > 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
								"' -- Index has results for txn " + txnId + " for store " + storeRef);
					}
					return true;        // there were updates/creates and results for the txn were found
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
								"' -- Transaction " + txnId + " not in index for store " + storeRef + ".  Possibly out of date.");
					}
					return false;
				}
			} finally {
				if (results != null) {
					results.close();
				}
			}
		} else if (deleteCount > 0) {
			// there have been deletes, so we have to ensure that none of the nodes deleted are present in the index
			// get all node refs for the transaction
			List<NodeRef> nodeRefs = nodeDaoService.getTxnChangesForStore(storeRef, txnId);

			for (NodeRef nodeRef : nodeRefs) {
				if (logger.isDebugEnabled()) {
					logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
							"' -- Searching for node in index: N: " + nodeRef + " T: " + txnId);
				}

				// we know that these are all deletions
				ResultSet results = null;
				try {
					SearchParameters sp = new SearchParameters();
					sp.addStore(storeRef);

					// search for it in the index, sorting with youngest first, fetching only 1
					sp.setLanguage(SearchService.LANGUAGE_LUCENE);
					sp.setQuery("ID:" + LuceneQueryParser.escape(nodeRef.toString()));
					sp.setLimit(1);

					results = searcher.query(sp);

					if (results.length() == 0) {

						// no results, as expected
						if (logger.isDebugEnabled()) {
							logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
							"' -- Node not found (OK)");
						}
						continue;
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
							"' -- Node found (Index out of date)");
						}
						return false;
					}
				} finally {
					if (results != null) { results.close(); }
				}
			}
		}
		// else  -> The fallthrough case where there are no updates or deletes

		// all tests passed
		if (logger.isDebugEnabled()) {
			logger.debug("[AbstractReindexComponent::reindex] Repository '" + RepositoryManager.getCurrentRepository() +
					"' -- Index is in synch with transaction: " + txnId);
		}
		return true;
	}

	/**
	 * @return          Returns <tt>false</tt> if any one of the transactions aren't in the index.
	 */
	protected boolean areTxnsInIndex(List<Transaction> txns)
	{
		for (Transaction txn : txns)
		{
			long txnId = txn.getId().longValue();
			if (isTxnIdPresentInIndex(txnId) == InIndex.NO)
			{
				// Missing txn
				return false;
			}
		}
		return true;
	}

	/**
	 * Perform a full reindexing of the given transaction in the context of a completely
	 * new transaction.
	 *
	 * @param txnId the transaction identifier
	 */
	protected void reindexTransaction(final long txnId) {

		if (logger.isDebugEnabled()) {
			logger.debug("[AbstractReindexComponent::reindexTransaction] Repository '" + RepositoryManager.getCurrentRepository() +
					"' -- Reindexing transaction: " + txnId);
		}

		RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>() {

			public Object execute() throws Exception {

				final long start = System.currentTimeMillis();

				// get the node references pertinent to the transaction
				List<NodeRef> nodeRefs = nodeDaoService.getTxnChanges(txnId);
				if (logger.isDebugEnabled()) {
					logger.debug("[AbstractReindexComponent::reindexTransaction] " +
							"Repository '" + RepositoryManager.getCurrentRepository() + "' -- Reindex transaction: " + txnId +
							" [Changes: " + nodeRefs.size() + "]");
				}

				// reindex each node
				for (NodeRef nodeRef : nodeRefs) {

					Status nodeStatus = nodeService.getNodeStatus(nodeRef);

					if (nodeStatus == null) {
						// it's not there any more
		                if (logger.isDebugEnabled()) {
        				    logger.debug("[AbstractReindexComponent::reindexTransaction] T: " + txnId + " nodeRef (" +nodeRef +") nodeStatus NULL");
                        }
						continue;
					} else if (nodeStatus.isDeleted()) { 	// node deleted
						// only the child node ref is relevant
						ChildAssociationRef assocRef = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, null, null, nodeRef);
						indexer.deleteNode(assocRef);

		                if (logger.isDebugEnabled()) {
        				    logger.debug("[AbstractReindexComponent::reindexTransaction] T: " + txnId + " nodeRef (" +nodeRef +") nodeStatus IS DELETED");
                        }
					} else { 	// node created
						// reindex
						indexer.updateNode(nodeRef);

		                if (logger.isDebugEnabled()) {
        				    logger.debug("[AbstractReindexComponent::reindexTransaction] T: " + txnId + " nodeRef (" +nodeRef +") nodeStatus REINDEX");
                        }
					}
				}
                if (logger.isDebugEnabled()) {
				    final long stop = System.currentTimeMillis();
				    logger.debug("[AbstractReindexComponent::reindexTransaction] T: " + txnId + " Elapsed: " + (stop - start) + " ms");
				}

				return null;
			}
		};

		transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true);

		if (logger.isDebugEnabled()) {
			logger.debug("[AbstractReindexComponent::reindexTransaction] Repository '" + RepositoryManager.getCurrentRepository() +
					"' -- Transaction reindexed: " + txnId);
		}
	}
}

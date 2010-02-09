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

import it.doqui.index.ecmengine.business.job.JobBusinessInterface;
import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.MultiTTenantAdminService;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.
 *
 * <p>
 * Questo componente non aggiunge nulla all'implementazione originale. L'unica
 * differenza &egrave; nella superclasse, che in questo caso &egrave; stata personalizzata.
 * </p>
 *
 * @author Doqui
 */
public class IndexTransactionTracker extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);

    private JobBusinessInterface jobManager;

    private long maxTxnDurationMs;
    private long reindexLagMs;
    private int maxRecordSetSize;

//    private boolean started;
//    private List<Long> previousTxnIds;
    private Map<String, List<Long>> previousTxnIdsMap;
    private long lastMaxTxnId;
    private Map<String, Long> lastMaxTxnIdMap;
//    private long fromTimeInclusive;
//    private Map<Long, TxnRecord> voids;
    private Map<String, Long> fromTimeInclusiveMap;
    private Map<String, Map<Long, TxnRecord>> voidsMap;


    /**
     * Set the defaults.
     * <ul>
     *   <li><b>Maximum transaction duration:</b> 1 hour</li>
     *   <li><b>Reindex lag:</b> 1 second</li>
     *   <li><b>Maximum recordset size:</b> 1000</li>
     * </ul>
     */
    public IndexTransactionTracker()
    {
        maxTxnDurationMs = 3600L * 1000L;
        reindexLagMs = 1000L;
        maxRecordSetSize = 1000;
//        previousTxnIds = Collections.<Long>emptyList();
        previousTxnIdsMap = new HashMap<String, List<Long>>();
        lastMaxTxnId = Long.MAX_VALUE;
        lastMaxTxnIdMap = new HashMap<String, Long>();
//        fromTimeInclusive = -1L;
        fromTimeInclusiveMap = new HashMap<String, Long>();
//        voids = new TreeMap<Long, TxnRecord>();
        voidsMap = new HashMap<String, Map<Long,TxnRecord>>();
    }

    /**
     * Set the expected maximum duration of transaction supported.  This value is used to adjust the
     * look-back used to detect transactions that committed.  Values must be greater than zero.
     *
     * @param maxTxnDurationMinutes     the maximum length of time a transaction will take in minutes
     *
     * @since 1.4.5, 2.0.5, 2.1.1
     */
    public void setMaxTxnDurationMinutes(long maxTxnDurationMinutes)
    {
        if (maxTxnDurationMinutes < 1)
        {
            throw new AlfrescoRuntimeException("Maximum transaction duration must be at least one minute.");
        }
        this.maxTxnDurationMs = maxTxnDurationMinutes * 60L * 1000L;
    }

    /**
     * Transaction tracking should lag by the average commit time for a transaction.  This will minimize
     * the number of holes in the transaction sequence.  Values must be greater than zero.
     *
     * @param reindexLagMs              the minimum age of a transaction to be considered by
     *                                  the index transaction tracking
     *
     * @since 1.4.5, 2.0.5, 2.1.1
     */
    public void setReindexLagMs(long reindexLagMs)
    {
        if (reindexLagMs < 1)
        {
            throw new AlfrescoRuntimeException("Reindex lag must be at least 1 millisecond.");
        }
        this.reindexLagMs = reindexLagMs;
    }

    /**
     * Set the number of transactions to request per query.
     */
    public void setMaxRecordSetSize(int maxRecordSetSize)
    {
        this.maxRecordSetSize = maxRecordSetSize;
    }

    @Override
    protected void reindexImpl()
    {
//        if (!started)
//        {
//            // Make sure that we start clean
//            voids.clear();
//            previousTxnIds = new ArrayList<Long>(maxRecordSetSize);
//            lastMaxTxnId = Long.MAX_VALUE;                              // So that it is ignored at first
//            fromTimeInclusive = getStartingTxnCommitTime();
//            started = true;
//        }

    	// TODO: il tracking potrebbe essere sospeso solo sul repository sul quale e` in creazione il nuovo tenant.

        // Fermo l'IndexTransactionTracker se e' attivo il job di creazione dei tenant
        boolean isTenantBootstrapRunning = false;
        try {
        	isTenantBootstrapRunning = jobManager.isExecuting(EcmEngineConstants.ECMENGINE_TENANT_ADMIN_JOB_REF);
        } catch (Exception e) {
        	// Riportiamo semplicemente un warning
        	logger.warn("[IndexTransactionTracker::reindexImpl] Exception accessing Job DAO on repository (" +RepositoryManager.getCurrentRepository() +")", e);
        }
        if (isTenantBootstrapRunning) {
        	// ECM Engine sta eseguendo il bootstrap di un nuovo tenant. Sospendiamo momentaneamente l'index tracking.
        	logger.info("[IndexTransactionTracker::reindexImpl] Tenant bootstrap running on repository (" +RepositoryManager.getCurrentRepository() +") skipping index tracking.");
        	return;
        }

        // Fermo l'IndexTransactionTracker se e' attivo il job di delete dei tenant
        boolean isTenantDeleteRunning = false;
        try {
        	isTenantDeleteRunning = jobManager.isExecuting(EcmEngineConstants.ECMENGINE_TENANT_DELETE_JOB_REF);
        } catch (Exception e) {
        	// Riportiamo semplicemente un warning
        	logger.warn("[IndexTransactionTracker::reindexImpl] Exception accessing Job DAO on repository (" +RepositoryManager.getCurrentRepository() +")", e);
        }
        if (isTenantDeleteRunning) {
        	// ECM Engine sta eseguendo il bootstrap di un nuovo tenant. Sospendiamo momentaneamente l'index tracking.
        	logger.info("[IndexTransactionTracker::reindexImpl] Tenant delete running on repository (" +RepositoryManager.getCurrentRepository() +") skipping index tracking.");
        	return;
        }


        final String currentRepositoryId = RepositoryManager.getCurrentRepository();

        if (lastMaxTxnIdMap.get(currentRepositoryId) != null) {
        	lastMaxTxnId = lastMaxTxnIdMap.get(currentRepositoryId).longValue();
        } else {
        	// Need to initialize for current repo

        	voidsMap.put(currentRepositoryId, new TreeMap<Long, TxnRecord>());
        	voidsMap.get(currentRepositoryId).clear();
        	previousTxnIdsMap.put(currentRepositoryId, new ArrayList<Long>(maxRecordSetSize));
        	lastMaxTxnId = Long.MAX_VALUE;	// So that it is ignored at first
        	fromTimeInclusiveMap.put(currentRepositoryId, new Long(getStartingTxnCommitTime()));
        }

        List<Long> curPreviousTxnIds = previousTxnIdsMap.get(currentRepositoryId);	// List is mutable

        while (true)
        {
            long toTimeExclusive = System.currentTimeMillis() - reindexLagMs;

            // Check that the voids haven't been filled
//            fromTimeInclusive = checkVoids(fromTimeInclusive);
            long curFromTimeInclusive = fromTimeInclusiveMap.get(currentRepositoryId).longValue();
            curFromTimeInclusive = checkVoids(curFromTimeInclusive);
            fromTimeInclusiveMap.put(currentRepositoryId, new Long(curFromTimeInclusive));

            // get next transactions to index
            List<Transaction> txns = getNextTransactions(curFromTimeInclusive, toTimeExclusive, curPreviousTxnIds);

            if (logger.isDebugEnabled())
            {
            	//AF: Modificato per stampare anche le transazioni usate per la ricerca.
                /*String msg = String.format(
                        "Reindexing %d transactions from %s (%s) to %s",
                        txns.size(),
                        (new Date(curFromTimeInclusive)).toString(),
                        txns.isEmpty() ? "---" : txns.get(0).getId().toString(),
                        (new Date(toTimeExclusive)).toString());*/
            	String msg = String.format(
            	        "Reindexing %d transactions from %s (%s) to %s",
            	        txns.size(),
            	        (new Date(curFromTimeInclusive)).toString() +" - " +curFromTimeInclusive,
            	        txns.isEmpty() ? "---" : txns.get(0).getId().toString(),
            	        (new Date(toTimeExclusive)).toString() +" - " +toTimeExclusive);
                logger.debug(msg);
            }

            // Reindex the transactions.  Voids between the last set of transactions and this
            // set will be detected as well.  Additionally, the last max transaction will be
            // updated by this method.
            reindexTransactions(txns);

            // Move the time on.
            // Note the subtraction here.  Yes, it's odd.  But the results of the getNextTransactions
            // may be limited by recordset size and it is possible to have multiple transactions share
            // the same commit time.  If these txns get split up and we exclude the time period, then
            // they won't be requeried.  The list of previously used transaction IDs is passed back to
            // be excluded from the next query.
            curFromTimeInclusive = toTimeExclusive - 1L;
            fromTimeInclusiveMap.put(currentRepositoryId, new Long(curFromTimeInclusive));

            curPreviousTxnIds.clear();
            for (Transaction txn : txns)
            {
            	curPreviousTxnIds.add(txn.getId());
            }

            // Break out if there were no transactions processed
            if (curPreviousTxnIds.isEmpty())
            {
                break;
            }

            // break out if the VM is shutting down
            if (isShuttingDown())
            {
                break;
            }
        }
        lastMaxTxnIdMap.put(currentRepositoryId, new Long(lastMaxTxnId));
    }

    /**
     * Find a transaction time to start indexing from (inclusive).  The last recorded transaction by ID
     * is taken and the max transaction duration substracted from its commit time.  A transaction is
     * retrieved for this time and checked for indexing.  If it is present, then that value is chosen.
     * If not, a step back in time is taken again.  This goes on until there are no more transactions
     * or a transaction is found in the index.
     */
    protected long getStartingTxnCommitTime()
    {
       	logger.info("[IndexTransactionTracker::getStartingTxnCommitTime] BEGIN on repository (" +RepositoryManager.getCurrentRepository() +")");

        // Look back in time by the maximum transaction duration
        long toTimeExclusive = System.currentTimeMillis() - maxTxnDurationMs;
        long fromTimeInclusive = 0L;
        double stepFactor = 1.0D;
found:
        while (true)
        {
            // Get the most recent transaction before the given look-back
            List<Transaction> nextTransactions = nodeDaoService.getTxnsByCommitTimeDescending(
                    0L,
                    toTimeExclusive,
                    1,
                    null);
            // There are no transactions in that time range
            if (nextTransactions.size() == 0)
            {
                break found;
            }
            // We found a transaction
            Transaction txn = nextTransactions.get(0);
            Long txnId = txn.getId();
            long txnCommitTime = txn.getCommitTimeMs();
            // Check that it is in the index
            InIndex txnInIndex = isTxnIdPresentInIndex(txnId);
            switch (txnInIndex)
            {
                case YES:
                    fromTimeInclusive = txnCommitTime;
                    break found;
                default:
                    // Look further back in time.  Step back by the maximum transaction duration and
                    // increase this step back by a factor of 10% each iteration.
                    toTimeExclusive = txnCommitTime - (long)(maxTxnDurationMs * stepFactor);
                    stepFactor *= 1.1D;
                    continue;
            }
        }

       	logger.info("[IndexTransactionTracker::getStartingTxnCommitTime] END on repository (" +RepositoryManager.getCurrentRepository() +")");

        // We have a starting value
        return fromTimeInclusive;
    }

    /**
     * Voids - otherwise known as 'holes' - in the transaction sequence are timestamped when they are
     * discovered.  This method discards voids that were timestamped before the given date.  It checks
     * all remaining voids, passing back the transaction time for the newly-filled void.  Otherwise
     * the value passed in is passed back.
     *
     * @param fromTimeInclusive     the oldest void to consider
     * @return                      Returns an adjused start position based on any voids being filled
     */
    private long checkVoids(long fromTimeInclusive)
    {
        long maxHistoricalTime = (fromTimeInclusive - maxTxnDurationMs);
        long fromTimeAdjusted = fromTimeInclusive;

        Map<Long, TxnRecord> curVoids = voidsMap.get(RepositoryManager.getCurrentRepository()); // Map is mutable

        List<Long> toExpireTxnIds = new ArrayList<Long>(1);
        // The voids are stored in a sorted map, sorted by the txn ID
        for (Long voidTxnId : curVoids.keySet())
        {
            TxnRecord voidTxnRecord = curVoids.get(voidTxnId);
            // Is the transaction around, yet?
            Transaction voidTxn = nodeDaoService.getTxnById(voidTxnId);
            if (voidTxn == null)
            {
                // It's still just a void.  Shall we expire it?
                if (voidTxnRecord.txnCommitTime < maxHistoricalTime)
                {
                    // It's too late for this void
                    toExpireTxnIds.add(voidTxnId);
                }
                continue;
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Void has become live: " + voidTxn);
                }
                //MB: 11:00:50 lunedi' 19 ottobre 2009
                // a volte va in errore a questa riga, quando il commit time della tabella
                // alf_transaction e' uguale a NULL. Occorre analizzare meglio
                // quando questo problema accade, per evitare succeda
                // Con ALfresco 3.x e' stata introdotta questa if ;)
                //if (voidTxn.getCommitTimeMs() == null)          // Just coping with Hibernate mysteries
                //{
                    //continue;
                //}
                //MB: 11:00:50 lunedi' 19 ottobre 2009

                // We found one that has become a real transaction.
                // We don't throw the other voids away.
                fromTimeAdjusted = voidTxn.getCommitTimeMs();
                // Break out as sequential rebuilding is required
                break;
            }
        }
        // Throw away all the expired ones
        for (Long toExpireTxnId : toExpireTxnIds)
        {
        	curVoids.remove(toExpireTxnId);
            if (logger.isDebugEnabled())
            {
                logger.debug("Void has expired: " + toExpireTxnId);
            }
        }
        // Done
        return fromTimeAdjusted;
    }

    private List<Transaction> getNextTransactions(long fromTimeInclusive, long toTimeExclusive, List<Long> previousTxnIds)
    {
        List<Transaction> txns = nodeDaoService.getTxnsByCommitTimeAscending(
                fromTimeInclusive,
                toTimeExclusive,
                maxRecordSetSize,
                previousTxnIds);
        if (logger.isDebugEnabled()) {
        	logger.debug(String.format("Got transactions from \"%s\" to \"%s\":",
        			(new Date(fromTimeInclusive)).toString(),
        			(new Date(toTimeExclusive)).toString()));
        	for (Transaction txn : txns) {
        		logger.debug(String.format("\t\tID: %s - Server: %s - Commit time: %s",
        				txn.getId(),
        				txn.getServer().getIpAddress(),
        				(new Date(txn.getCommitTimeMs())).toString()));
        	}
        }
        // done
        return txns;
    }

    /**
     * Checks that each of the transactions is present in the index.  As soon as one is found that
     * isn't, all the following transactions will be reindexed.  After the reindexing, the sequence
     * of transaction IDs will be examined for any voids.  These will be recorded.
     *
     * @param txns      transactions ordered by time ascending
     * @return          returns the
     */
    private void reindexTransactions(List<Transaction> txns)
    {
        if (txns.isEmpty())
        {
            return;
        }

        Set<Long> processedTxnIds = new HashSet<Long>(13);

        Map<Long, TxnRecord> curVoids = voidsMap.get(RepositoryManager.getCurrentRepository()); // Map is mutable

        boolean forceReindex = false;
        long minNewTxnId = Long.MAX_VALUE;
        long maxNewTxnId = Long.MIN_VALUE;
        long maxNewTxnCommitTime = System.currentTimeMillis();
        for (Transaction txn : txns)
        {
            Long txnId = txn.getId();
            long txnIdLong = txnId.longValue();
            if (txnIdLong < minNewTxnId)
            {
                minNewTxnId = txnIdLong;
            }
            if (txnIdLong > maxNewTxnId)
            {
                maxNewTxnId = txnIdLong;
                maxNewTxnCommitTime = txn.getCommitTimeMs();
            }
            // Keep track of it for void checking
            processedTxnIds.add(txnId);
            // Remove this entry from the void list - it is not void
            curVoids.remove(txnId);

            // Reindex the transaction if we are forcing it or if it isn't in the index already
            if (forceReindex || isTxnIdPresentInIndex(txnId) == InIndex.NO)
            {
                // Any indexing means that all the next transactions have to be indexed
                forceReindex = true;
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Reindexing transaction: " + txn);
                    }
                    // We try the reindex, but for the sake of continuity, have to let it run on
                    reindexTransaction(txnId);
                }
                catch (Throwable e)
                {
                    logger.warn("\n" +
                            "Reindex of transaction failed: \n" +
                            "   Transaction ID: " + txnId + "\n" +
                            "   Error: " + e.getMessage(),
                            e);
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Reindex skipping transaction: " + txn);
                }
            }
        }
        // We have to search for voids now.  Don't start at the min transaction,
        // but start at the least of the lastMaxTxnId and minNewTxnId
        long voidCheckStartTxnId = (lastMaxTxnId < minNewTxnId ? lastMaxTxnId : minNewTxnId) + 1;
        long voidCheckEndTxnId = maxNewTxnId;
        // Check for voids in new transactions
        for (long i = voidCheckStartTxnId; i <= voidCheckEndTxnId; i++)
        {
            Long txnId = Long.valueOf(i);
            if (processedTxnIds.contains(txnId))
            {
                // It is there
                continue;
            }

            // First make sure that it is a real void.  Sometimes, transactions are in the table but don't
            // fall within the commit time window that we queried.  If they're in the DB AND in the index,
            // then they're not really voids and don't need further checks.  If they're missing from either,
            // then they're voids and must be processed.
            Transaction voidTxn = nodeDaoService.getTxnById(txnId);
            if (voidTxn != null && isTxnIdPresentInIndex(txnId) != InIndex.NO)
            {
                // It is a real transaction (not a void) and is already in the index, so just ignore it.
                continue;
            }

            // Calculate an age for the void.  We can't use the current time as that will mean we keep all
            // discovered voids, even if they are very old.  Rather, we use the commit time of the last transaction
            // in the set as it represents the query time for this iteration.
            TxnRecord voidRecord = new TxnRecord();
            voidRecord.txnCommitTime = maxNewTxnCommitTime;
            curVoids.put(txnId, voidRecord);
            if (logger.isDebugEnabled())
            {
                logger.debug("Void detected: " + txnId);
            }
        }
        // Having searched for the nodes, we've recorded all the voids.  So move the lastMaxTxnId up.
        lastMaxTxnId = voidCheckEndTxnId;
    }

    private class TxnRecord
    {
        private long txnCommitTime;
    }

	public JobBusinessInterface getJobManager() {
		return jobManager;
	}

	public void setJobManager(JobBusinessInterface jobManager) {
		this.jobManager = jobManager;
	}

	public void setTenantAdminService(MultiTTenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}
}

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

package it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene;

import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareIndexerAndSearcher;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;
import org.alfresco.repo.search.transaction.SimpleTransaction;
import org.alfresco.repo.search.transaction.SimpleTransactionManager;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Lock;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class RepositoryAwareAbstractLuceneIndexerAndSearcherFactory
	implements RepositoryAwareIndexerAndSearcher, LuceneIndexerAndSearcher, XAResource {

	/** Logger. */
    private static Log logger = LogFactory.getLog(
    		EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

    private int queryMaxClauses;
    private int indexerBatchSize;

    /**
     * A map of active global transactions. It contains all the indexers a transaction has used,
     * with at most one indexer for each "repository:store" pair within a transaction
     */
    private Map<Xid, Map<String, LuceneIndexer>> activeIndexersInGlobalTx =
    	new HashMap<Xid, Map<String, LuceneIndexer>>();

    /** Suspended global transactions. */
    private Map<Xid, Map<String, LuceneIndexer>> suspendedIndexersInGlobalTx =
    	new HashMap<Xid, Map<String, LuceneIndexer>>();

    /** Thread local indexers - used outside of a global transaction */
    private ThreadLocal<Map<String, LuceneIndexer>> threadLocalIndexers =
    	new ThreadLocal<Map<String, LuceneIndexer>>();

    /** The default timeout for transactions TODO: Respect this */
    private int timeout = DEFAULT_TIMEOUT;

    /** Default time out value set to 10 minutes. */
    private static final int DEFAULT_TIMEOUT = 600000;

    private RepositoryManager repositoryManager;

    private QueryRegisterComponent queryRegister;

    /** The maximum transformation time to allow atomically, defaulting to 20ms */
    private long maxAtomicTransformationTime = 20;

    private int indexerMaxFieldLength;

    private long writeLockTimeout;

    private long commitLockTimeout;

    private String lockDirectory;

    protected TenantService tenantService;

    private String indexRootLocation;

    private MLAnalysisMode defaultMLIndexAnalysisMode = MLAnalysisMode.EXACT_LANGUAGE_AND_ALL;

    private MLAnalysisMode defaultMLSearchAnalysisMode = MLAnalysisMode.EXACT_LANGUAGE_AND_ALL;

    private ThreadPoolExecutor threadPoolExecutor;

    public RepositoryAwareAbstractLuceneIndexerAndSearcherFactory() {
        super();
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public RepositoryManager getRepositoryManager() {
    	return this.repositoryManager;
    }

	/**
     * Set the query register.
     *
     * @param queryRegister
     */
    public void setQueryRegister(QueryRegisterComponent queryRegister) {
        this.queryRegister = queryRegister;
    }

    /**
     * Get the query register.
     *
     * @return - the query register.
     */
    public QueryRegisterComponent getQueryRegister() {
        return queryRegister;
    }

    /**
     * Set the maximum average transformation time allowed to a transformer in order to have the transformation
     * performed in the current transaction. The default is 20ms.
     *
     * @param maxAtomicTransformationTime
     *            the maximum average time that a text transformation may take in order to be performed atomically.
     */
    public void setMaxAtomicTransformationTime(long maxAtomicTransformationTime) {
        this.maxAtomicTransformationTime = maxAtomicTransformationTime;
    }

    /**
     * Get the max time for an atomic transform
     *
     * @return - milliseconds as a long
     */
    public long getMaxTransformationTime() {
        return maxAtomicTransformationTime;
    }

    /**
     * Set the tenant service
     *
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Check if we are in a global transaction according to the transaction manager.
     *
     * @return {@code true} if we are in a global transaction
     */
    private boolean inGlobalTransaction() {
        try {
            return SimpleTransactionManager.getInstance().getTransaction() != null;
        } catch (SystemException e) {
            return false;
        }
    }

    /**
     * Get the local transaction (may be {@code null} if we are outside a transaction).
     *
     * @return The transaction
     * @throws IndexerException
     */
    private SimpleTransaction getTransaction() throws IndexerException {
        try {
            return SimpleTransactionManager.getInstance().getTransaction();
        } catch (SystemException e) {
            throw new IndexerException("Failed to get transaction", e);
        }
    }

	public Indexer getIndexer(StoreRef storeRef) throws IndexerException {
		logger.error("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getIndexer] Unsupported: getIndexer() without a repository ID!");
		throw new UnsupportedOperationException("Repository ID needed!");
	}

	public SearchService getSearcher(StoreRef storeRef, boolean arg1)
			throws SearcherException {
		logger.error("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getSearcher] Unsupported: getSearcher() without a repository ID!");
		throw new UnsupportedOperationException("Repository ID needed!");
	}

    /**
     * Get an indexer for the store to use in the current transaction for this thread of control.
     *
     * @param storeRef -
     *            the id of the store
     */
    public LuceneIndexer getIndexer(StoreRef storeRef, String repository) throws IndexerException {
        storeRef = tenantService.getName(storeRef);

        // register to receive txn callbacks
        // TODO: make this conditional on whether the XA stuff is being used
        // directly on not
        AlfrescoTransactionSupport.bindLucene(this);	// Serve veramente? - FF

        final String cacheKey = repository + ":" + storeRef;

        if (logger.isDebugEnabled()) {
            logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getIndexer] " +
        		"Repository '" + repository + "' -- Retrieving indexer for: " + storeRef);
        }

        if (inGlobalTransaction()) {
            SimpleTransaction tx = getTransaction();

            // Only find indexers in the active list
            Map<String, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(tx);
            if (indexers == null) {
                if (suspendedIndexersInGlobalTx.containsKey(tx)) {
                    throw new IndexerException("Trying to obtain an index for a suspended transaction.");
                }

                indexers = new HashMap<String, LuceneIndexer>();
                activeIndexersInGlobalTx.put(tx, indexers);

                try {
                    tx.enlistResource(this);
                } catch (IllegalStateException e) { // TODO: what to do in each case?
                    throw new IndexerException("", e);
                } catch (RollbackException e) {
                    throw new IndexerException("", e);
                } catch (SystemException e) {
                    throw new IndexerException("", e);
                }
            }
            LuceneIndexer indexer = indexers.get(cacheKey);
            if (indexer == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getIndexer] " +
                		"Repository '" + repository + "' -- Indexer not found... creating new for store: " + storeRef);
                }

                indexer = createIndexer(storeRef, repository, getTransactionId(tx, storeRef, repository));
                indexers.put(cacheKey, indexer);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getIndexer] " +
                		"Repository '" + repository + "' -- Indexer found for store: " + storeRef + " [Indexer: " + indexer + "]");
                }
            }
            return indexer;
        } else {
        	// A thread local transaction
        	return getThreadLocalIndexer(storeRef, repository);
        }
    }

    private LuceneIndexer getThreadLocalIndexer(StoreRef storeRef, String repository) {
        Map<String, LuceneIndexer> indexers = threadLocalIndexers.get();
        final String cacheKey = repository + ":" + storeRef;

        if (indexers == null) {
            indexers = new HashMap<String, LuceneIndexer>();
            threadLocalIndexers.set(indexers);
        }

        LuceneIndexer indexer = indexers.get(cacheKey);

        if (indexer == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getThreadLocalIndexer] " +
            		"Repository '" + repository + "' -- ThreadLocal Indexer not found... creating new for store: " + storeRef);
            }

            indexer = createIndexer(storeRef, repository, GUID.generate());
            indexers.put(cacheKey, indexer);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getThreadLocalIndexer] " +
            		"Repository '" + repository + "' -- ThreadLocal Indexer found for store: " + storeRef + " [Indexer: " + indexer + "]");
            }
        }
        return indexer;
    }

    /**
     * Get the transaction identifier used to store it in the transaction map.
     *
     * @param tx
     * @return - the transaction id
     */
    private String getTransactionId(Transaction tx, StoreRef storeRef, String repository) {
    	final String cacheKey = repository + ":" + storeRef;

        if (tx instanceof SimpleTransaction) {
            SimpleTransaction simpleTx = (SimpleTransaction) tx;
            return simpleTx.getGUID();
        } else {
            Map<String, LuceneIndexer> indexers = threadLocalIndexers.get();

            if (indexers != null) {
                LuceneIndexer indexer = indexers.get(cacheKey);
                if (indexer != null) {
                    return indexer.getDeltaId();
                }
            }
            return null;
        }
    }

    /**
     * Encapsulate creating an indexer
     *
     * @param storeRef
     * @param deltaId
     * @return - the indexer made by the concrete implementation
     */
    protected abstract LuceneIndexer createIndexer(StoreRef storeRef, String repository, String deltaId);

    /**
     * Encapsulate creating a searcher over the main index
     */
    public LuceneSearcher getSearcher(StoreRef storeRef, String repository,
    		boolean searchDelta) throws SearcherException {

        storeRef = tenantService.getName(storeRef);

        if (logger.isDebugEnabled()) {
            logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getSearcher] " +
        		"Repository '" + repository + "' -- Retrieving searcher for: " + storeRef + " [Delta: " + searchDelta + "]");
        }

        String deltaId = null;
        LuceneIndexer indexer = null;

        if (searchDelta) {
            deltaId = getTransactionId(getTransaction(), storeRef, repository);

            if (deltaId != null) {
                indexer = getIndexer(storeRef, repository);
            }
        }
        LuceneSearcher searcher = getSearcher(storeRef, indexer, repository);

        return searcher;
    }

    /**
     * Get a searcher over the index and the current delta
     *
     * @param storeRef
     * @param indexer
     * @param repository
     * @return - the searcher made by the concrete implementation.
     * @throws SearcherException
     */
    protected abstract LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer, String repository)
    throws SearcherException;

    /*
     * XAResource implementation
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {

    	try {
            // TODO: Should be remembering overall state
            // TODO: Keep track of prepare responses
            Map<String, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);

            if (indexers == null) {
                if (suspendedIndexersInGlobalTx.containsKey(xid)) {
                    throw new XAException("Trying to commit indexes for a suspended transaction.");
                } else {
                    // nothing to do
                    return;
                }
            }

            if (onePhase) {
                if (indexers.isEmpty()) {
                    return;
                } else if (indexers.size() == 1) {
                    for (LuceneIndexer indexer : indexers.values()) {
                        indexer.commit();
                    }
                    return;
                } else {
                    throw new XAException("Trying to do one phase commit on more than one index");
                }
            } else { // two phase
                for (LuceneIndexer indexer : indexers.values()) {
                    indexer.commit();
                }
                return;
            }
        } finally {
            activeIndexersInGlobalTx.remove(xid);
        }
    }

    public void end(Xid xid, int flag) throws XAException {
        Map<String, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);

        if (indexers == null) {
            if (suspendedIndexersInGlobalTx.containsKey(xid)) {
                throw new XAException("Trying to commit indexes for a suspended transaction.");
            } else {
                // nothing to do
                return;
            }
        }

        if (flag == XAResource.TMSUSPEND) {
            activeIndexersInGlobalTx.remove(xid);
            suspendedIndexersInGlobalTx.put(xid, indexers);
        } else if (flag == TMFAIL) {
            activeIndexersInGlobalTx.remove(xid);
            suspendedIndexersInGlobalTx.remove(xid);
        } else if (flag == TMSUCCESS) {
            activeIndexersInGlobalTx.remove(xid);
        }
    }

    public void forget(Xid xid) throws XAException {
        activeIndexersInGlobalTx.remove(xid);
        suspendedIndexersInGlobalTx.remove(xid);
    }

    public int getTransactionTimeout() throws XAException {
        return timeout;
    }

    public boolean isSameRM(XAResource xar) throws XAException {
        return (xar instanceof RepositoryAwareAbstractLuceneIndexerAndSearcherFactory);
    }

    public int prepare(Xid xid) throws XAException {
        // TODO: Track state OK, ReadOnly, Exception (=> rolled back?)
        Map<String, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);

        if (indexers == null) {
            if (suspendedIndexersInGlobalTx.containsKey(xid)) {
                throw new XAException("Trying to commit indexes for a suspended transaction.");
            } else {
                // nothing to do
                return XAResource.XA_OK;
            }
        }

        boolean isPrepared = true;
        boolean isModified = false;

        for (LuceneIndexer indexer : indexers.values()) {
            try {
                isModified |= indexer.isModified();
                indexer.prepare();
            } catch (IndexerException e) {
                isPrepared = false;
            }
        }

        if (isPrepared) {
        	return (isModified) ? XAResource.XA_OK : XAResource.XA_RDONLY;
        } else {
            throw new XAException("Failed to prepare: requires rollback");
        }
    }

    public Xid[] recover(int arg0) throws XAException {
        // We can not rely on being able to recover at the moment
        // Avoiding for performance benefits at the moment
        // Assume roll back and no recovery - in the worst case we get an unused
        // delta
        // This should be there to avoid recovery of partial commits.
        // It is difficult to see how we can mandate the same conditions.
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        // TODO: What to do if all do not roll back?
        try {
            Map<String, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);

            if (indexers == null) {
                if (suspendedIndexersInGlobalTx.containsKey(xid)) {
                    throw new XAException("Trying to commit indexes for a suspended transaction.");
                } else {
                    // nothing to do
                    return;
                }
            }

            for (LuceneIndexer indexer : indexers.values()) {
                indexer.rollback();
            }
        } finally {
            activeIndexersInGlobalTx.remove(xid);
        }
    }

    public boolean setTransactionTimeout(int timeout) throws XAException {
        this.timeout = timeout;
        return true;
    }

    public void start(Xid xid, int flag) throws XAException {

        Map<String, LuceneIndexer> active = activeIndexersInGlobalTx.get(xid);
        Map<String, LuceneIndexer> suspended = suspendedIndexersInGlobalTx.get(xid);

        if (flag == XAResource.TMJOIN) {
            // must be active
            if ((active != null) && (suspended == null)) {
                return;
            } else {
                throw new XAException("Trying to rejoin transaction in an invalid state");
            }

        } else if (flag == XAResource.TMRESUME) {
            // must be suspended
            if ((active == null) && (suspended != null)) {
                suspendedIndexersInGlobalTx.remove(xid);
                activeIndexersInGlobalTx.put(xid, suspended);
                return;
            } else {
                throw new XAException("Trying to rejoin transaction in an invalid state");
            }

        } else if (flag == XAResource.TMNOFLAGS) {
            if ((active == null) && (suspended == null)) {
                return;
            } else {
                throw new XAException("Trying to start an existing or suspended transaction");
            }
        } else {
            throw new XAException("Unkown flags for start " + flag);
        }
    }

    /*
     * Thread local support for transactions
     */

    /**
     * Commit the transaction
     */

    public void commit() throws IndexerException {
        try {
            Map<String, LuceneIndexer> indexers = threadLocalIndexers.get();
            if (indexers != null) {
                for (LuceneIndexer indexer : indexers.values()) {
                    try {
                        indexer.commit();
                    } catch (IndexerException e) {
                        rollback();
                        throw e;
                    }
                }
            }
        } finally {
            if (threadLocalIndexers.get() != null) {
                threadLocalIndexers.get().clear();
                threadLocalIndexers.set(null);
            }
        }
    }

    /**
     * Prepare the transaction TODO: Store prepare results
     *
     * @return - the tx code
     */
    public int prepare() throws IndexerException
    {
        boolean isPrepared = true;
        boolean isModified = false;
        Map<String, LuceneIndexer> indexers = threadLocalIndexers.get();
        if (indexers != null) {
            for (LuceneIndexer indexer : indexers.values()) {
                try {
                    isModified |= indexer.isModified();
                    indexer.prepare();
                } catch (IndexerException e) {
                    isPrepared = false;
                    throw new IndexerException("Failed to prepare: requires rollback", e);
                }
            }
        }
        if (isPrepared) {
            if (isModified) {
                return XAResource.XA_OK;
            } else {
                return XAResource.XA_RDONLY;
            }
        } else {
            throw new IndexerException("Failed to prepare: requires rollback");
        }
    }

    /**
     * Roll back the transaction
     */
    public void rollback() {
        Map<String, LuceneIndexer> indexers = threadLocalIndexers.get();

        if (indexers != null) {
            for (LuceneIndexer indexer : indexers.values()) {

            	try {
                    indexer.rollback();
                } catch (IndexerException e) {
                	// noop
                }
            }
        }

        if (threadLocalIndexers.get() != null) {
            threadLocalIndexers.get().clear();
            threadLocalIndexers.set(null);
        }
    }

    public void flush() {
        // TODO: Needs fixing if we expose the indexer in JTA
        Map<String, LuceneIndexer> indexers = threadLocalIndexers.get();

        if (indexers != null) {
            for (LuceneIndexer indexer : indexers.values()) {
                indexer.flushPending();
            }
        }
    }

//    public String getIndexRootLocation() {
//
//    	String indexRootLocation = repositoryManager.getRepository(
//   				RepositoryManager.getCurrentRepository()).getIndexRootLocation();
//
//    	logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::getIndexRootLocation] " +
//    			"Using location: " + indexRootLocation);
//
//    	return indexRootLocation;
//    }

    public void setIndexRootLocation(String location) {
    	this.indexRootLocation = location;
    }

    public String getIndexRootLocation() {
    	return this.indexRootLocation;
    }

    public int getIndexerBatchSize() {
        return indexerBatchSize;
    }

    /**
     * Set the batch six to use for background indexing
     *
     * @param indexerBatchSize
     */
    public void setIndexerBatchSize(int indexerBatchSize) {
        this.indexerBatchSize = indexerBatchSize;
    }

    /**
     * Get the directory where any lock files are written (by default there are none)
     *
     * @return - the path to the directory
     */
    public String getLockDirectory() {
        return lockDirectory;
    }

    public void setLockDirectory(String lockDirectory) {
        this.lockDirectory = lockDirectory;

        // Set the Lucene lock file via System property
        // org.apache.lucene.lockDir
        System.setProperty("org.apache.lucene.lockDir", lockDirectory);

        // Make sure the lock directory exists
        File lockDir = new File(lockDirectory);
        if (!lockDir.exists()) {
            lockDir.mkdirs();
        }

        // clean out any existing locks when we start up
        File[] children = lockDir.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (child.isFile()) {
                    if (child.exists() && !child.delete() && child.exists()) {
                        throw new IllegalStateException("Failed to delete " + child);
                    }
                }
            }
        }
    }

    public int getQueryMaxClauses() {
        return queryMaxClauses;
    }

    /**
     * Set the max number of queries in a llucen boolean query
     *
     * @param queryMaxClauses
     */
    public void setQueryMaxClauses(int queryMaxClauses) {
        this.queryMaxClauses = queryMaxClauses;
        BooleanQuery.setMaxClauseCount(this.queryMaxClauses);
    }

    /**
     * Set the lucene write lock timeout
     * @param timeout
     */
    public void setWriteLockTimeout(long timeout) {
        this.writeLockTimeout = timeout;
    }

    /**
     * Set the lucene commit lock timeout (no longer used with lucene 2.1)
     * @param timeout
     */
    public void setCommitLockTimeout(long timeout) {
        this.commitLockTimeout = timeout;
    }

    /**
     * Get the commit lock timout.
     * @return - the timeout
     */
    public long getCommitLockTimeout() {
        return commitLockTimeout;
    }

    /**
     * Get the write lock timeout
     * @return - the timeout in ms
     */
    public long getWriteLockTimeout() {
        return writeLockTimeout;
    }

    /**
     * Set the lock poll interval in ms
     *
     * @param time
     */
    public void setLockPollInterval(long time) {
        Lock.LOCK_POLL_INTERVAL = time;
    }

    /**
     * Get the max number of tokens in the field
     * @return - the max tokens considered.
     */
    public int getIndexerMaxFieldLength() {
        return indexerMaxFieldLength;
    }

    /**
     * Set the max field length.
     * @param indexerMaxFieldLength
     */
    public void setIndexerMaxFieldLength(int indexerMaxFieldLength) {
        this.indexerMaxFieldLength = indexerMaxFieldLength;
    }

    public ThreadPoolExecutor getThreadPoolExecutor()
    {
        return this.threadPoolExecutor;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * This component is able to <i>safely</i> perform backups of the Lucene indexes
     * while the server is running.
     *
     * <p>It can be run directly by calling the {@link #backup() } method, but the
     * convenience {@link LuceneIndexBackupJob} can be used to call it as well.</p>
     */
    public static class LuceneIndexBackupComponent {

        private TransactionService transactionService;

        private Set<LuceneIndexerAndSearcher> factories;

        @SuppressWarnings("unused")
        private NodeService nodeService;

        private RepositoryManager repositoryManager;

        /** Default constructor. */
        public LuceneIndexBackupComponent() {}

        /**
         * Provides transactions in which to perform the work.
         *
         * @param transactionService The transaction service.
         */
        public void setTransactionService(TransactionService transactionService) {
            this.transactionService = transactionService;
        }

        /**
         * Set the Lucene index factory that will be used to control the index locks.
         *
         * @param factories The index factories.
         */
        public void setFactories(Set<LuceneIndexerAndSearcher> factories) {
            this.factories = factories;
        }

        /**
         * Used to retrieve the stores.
         *
         * @param nodeService The node service.
         */
        public void setNodeService(NodeService nodeService) {
            this.nodeService = nodeService;
        }

        public void setRepositoryManager(RepositoryManager repositoryManager) {
            this.repositoryManager = repositoryManager;
        }

        /** Backup Lucene indexes. */
        public void backup() {
            RetryingTransactionCallback<Object> backupWork = new RetryingTransactionCallback<Object>() {
                public Object execute() throws Exception {
                    backupImpl();
                    return null;
                }
            };

            for (Repository repository : repositoryManager.getRepositories()) {
            	RepositoryManager.setCurrentRepository(repository.getId());
                if (logger.isDebugEnabled()) {
                	logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::backup] " +
            			"Repository '" + RepositoryManager.getCurrentRepository() +
            			"' -- Doing backup of lucene indexes.");
                }
            	transactionService.getRetryingTransactionHelper().doInTransaction(backupWork);
            }
        }

        private void backupImpl() {
        	final String currentRepositoryId = RepositoryManager.getCurrentRepository();
        	final String targetLocation = repositoryManager.getRepository(currentRepositoryId).getIndexBackupLocation();

            // create the location to copy to
            File targetDir = new File(targetLocation);

            if (targetDir.exists() && !targetDir.isDirectory()) {
                throw new AlfrescoRuntimeException("Target location is a file and not a directory: " + targetDir);
            }

            File targetParentDir = targetDir.getParentFile();

            if (targetParentDir == null) {
                throw new AlfrescoRuntimeException("Target location may not be a root directory: " + targetDir);
            }

            File tempDir = new File(targetParentDir, "indexbackup_temp");

            for (LuceneIndexerAndSearcher factory : factories) {
                WithAllWriteLocksWork<Object> backupWork = new BackUpWithAllWriteLocksWork(factory, tempDir, targetDir);
                factory.doWithAllWriteLocks(backupWork);

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::backupImpl] " +
                    		"Repository '" + RepositoryManager.getCurrentRepository() +
                    		"' -- Backed up Lucene indexes to target directory: " + targetDir);
                }
            }
        }

        /**
         * Static internal class which implements a Lucene index backup work.
         */
        static class BackUpWithAllWriteLocksWork implements WithAllWriteLocksWork<Object> {

            LuceneIndexerAndSearcher factory;
            File tempDir;
            File targetDir;

            BackUpWithAllWriteLocksWork(LuceneIndexerAndSearcher factory, File tempDir, File targetDir) {
                this.factory = factory;
                this.tempDir = tempDir;
                this.targetDir = targetDir;
            }

            public Object doWork() {
                try {
                    File indexRootDir = new File(factory.getIndexRootLocation());

                    // perform the copy
                    backupDirectory(indexRootDir, tempDir, targetDir);
                    return null;
                } catch (Throwable e) {
                    throw new AlfrescoRuntimeException("Failed to copy Lucene index root: \n"
                            + "   Index root: " + factory.getIndexRootLocation() +
                            "\n   Target: " + targetDir, e);
                }
            }

            /**
             * Makes a backup of the source directory via a temporary folder
             */
            private static void backupDirectory(File sourceDir, File tempDir, File targetDir) throws Exception {
                if (!sourceDir.exists()) {
                    // there is nothing to copy
                    return;
                }

                // delete the files from the temp directory
                if (tempDir.exists()) {
                    FileUtils.deleteDirectory(tempDir);
                    if (tempDir.exists()) {
                        throw new AlfrescoRuntimeException("Temp directory exists and cannot be deleted: " + tempDir);
                    }
                }

                // copy to the temp directory
                FileUtils.copyDirectory(sourceDir, tempDir, true);

                // check that the temp directory was created
                if (!tempDir.exists()) {
                    throw new AlfrescoRuntimeException("Copy to temp location failed");
                }

                // delete the target directory
                FileUtils.deleteDirectory(targetDir);

                if (targetDir.exists()) {
                    throw new AlfrescoRuntimeException("Failed to delete older files from target location");
                }

                // rename the temp to be the target
                tempDir.renameTo(targetDir);

                // make sure the rename worked
                if (!targetDir.exists()) {
                    throw new AlfrescoRuntimeException(
                            "Failed to rename temporary directory to target backup directory");
                }
            }
        }
    }

    /**
     * Job that lock uses the {@link LuceneIndexBackupComponent} to perform safe backups of the Lucene indexes.
     */
    public static class LuceneIndexBackupJob implements Job {

        /** Bean name for the {@code LuceneIndexBackupComponent}. */
        public static final String KEY_LUCENE_INDEX_BACKUP_COMPONENT = "luceneIndexBackupComponent";

        /** Locks the Lucene indexes and copies them to a backup location. */
        public void execute(JobExecutionContext context) throws JobExecutionException {

            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            LuceneIndexBackupComponent backupComponent = (LuceneIndexBackupComponent) jobData
                    .get(KEY_LUCENE_INDEX_BACKUP_COMPONENT);
            if (backupComponent == null) {
                throw new JobExecutionException("Missing job data: " + KEY_LUCENE_INDEX_BACKUP_COMPONENT);
            }

            // perform the backup
            backupComponent.backup();
        }
    }

    public MLAnalysisMode getDefaultMLIndexAnalysisMode()
    {
        return defaultMLIndexAnalysisMode;
    }

    /**
     * Set the ML analysis mode at index time.
     *
     * @param mode
     */
    public void setDefaultMLIndexAnalysisMode(MLAnalysisMode mode)
    {
        // defaultMLIndexAnalysisMode = MLAnalysisMode.getMLAnalysisMode(mode);
        defaultMLIndexAnalysisMode = mode;
    }

    public MLAnalysisMode getDefaultMLSearchAnalysisMode()
    {
        return defaultMLSearchAnalysisMode;
    }

    /**
     * Set the ML analysis mode at search time
     * @param mode
     */
    public void setDefaultMLSearchAnalysisMode(MLAnalysisMode mode)
    {
        // defaultMLSearchAnalysisMode = MLAnalysisMode.getMLAnalysisMode(mode);
        defaultMLSearchAnalysisMode = mode;
    }

    protected abstract List<StoreRef> getAllStores();

    public <R> R doWithAllWriteLocks(WithAllWriteLocksWork<R> lockWork) {

        // get all the available stores
        List<StoreRef> storeRefs = getAllStores();

        // get all the available repositories
        List<Repository> repos = repositoryManager.getRepositories();

        IndexInfo.LockWork<R> currentLockWork = null;

        for (Repository repo : repos) {
            if (logger.isDebugEnabled()) {
            	logger.debug("[RepositoryAwareAbstractLuceneIndexerAndSearcherFactory::doWithAllWriteLocks] " +
        			"Initializing works for repository: " + repo.getId());
            }
        	for (int i = storeRefs.size() - 1; i >= 0; i--) {
        		if (currentLockWork == null) {
        			currentLockWork = new CoreLockWork<R>(getIndexer(storeRefs.get(i), repo.getId()), lockWork);
        		} else {
        			currentLockWork = new NestingLockWork<R>(getIndexer(storeRefs.get(i), repo.getId()), currentLockWork);
        		}
        	}
        }

        if (currentLockWork != null) {
            try {
                return currentLockWork.doWork();
            } catch (Throwable exception) {

                // Re-throw the exception
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                } else {
                    throw new RuntimeException("Error during run with lock.", exception);
                }
            }

        } else {
            return null;
        }
    }

    private static class NestingLockWork<R> implements IndexInfo.LockWork<R> {
        IndexInfo.LockWork<R> lockWork;
        LuceneIndexer indexer;

        NestingLockWork(LuceneIndexer indexer, IndexInfo.LockWork<R> lockWork) {
            this.indexer = indexer;
            this.lockWork = lockWork;
        }

        public R doWork() throws Exception {
            return indexer.doWithWriteLock(lockWork);
        }
    }

    private static class CoreLockWork<R> implements IndexInfo.LockWork<R> {
        WithAllWriteLocksWork<R> lockWork;
        LuceneIndexer indexer;

        CoreLockWork(LuceneIndexer indexer, WithAllWriteLocksWork<R> lockWork) {
            this.indexer = indexer;
            this.lockWork = lockWork;
        }

        public R doWork() throws Exception {
            return indexer.doWithWriteLock(new IndexInfo.LockWork<R>() {
                public R doWork() {
                    try {
                        return lockWork.doWork();
                    } catch (Throwable exception) {

                        // Re-throw the exception
                        if (exception instanceof RuntimeException) {
                            throw (RuntimeException) exception;
                        } else {
                            throw new RuntimeException("Error during run with lock.", exception);
                        }
                    }
                }
            });
        }
    }
}

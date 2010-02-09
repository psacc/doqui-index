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

import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.LuceneAnalyser;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.alfresco.repo.search.impl.lucene.LuceneIndexException;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;
import org.alfresco.repo.search.impl.lucene.index.TransactionStatus;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo.LockWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

public abstract class RepositoryAwareLuceneBase {

	private static Logger s_logger = Logger.getLogger(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

	private IndexInfo indexInfo;

	/** The identifier for the store. */
	protected StoreRef store;

	/** The identifier for the repository. */
	protected String repoId;

	/** The identifier for the delta. */
	protected String deltaId;

	/** The Lucene configuration options. */
	private LuceneConfig config;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The status of the transaction. */
	private TransactionStatus status = TransactionStatus.UNKNOWN;

	protected IndexInfoProxyServiceInterface service;

	protected IndexInfoProxyServiceInterface serviceTC;
	protected IndexInfoProxyServiceInterface serviceNoTC;


	/**
	 * Initialize the configuration elements of the Lucene store indexers and searchers.
	 *
	 * @param store
	 * @param deltaId
	 * @throws IOException
	 */
	protected void initialise(StoreRef store, String deltaId, String repository) throws LuceneIndexException {
		this.store = store;
		this.deltaId = deltaId;

		// XXX: tengo traccia del repository associato all'indexer
		this.repoId = repository;

		final String basePath = getBasePath();

        if(s_logger.isDebugEnabled()){
		    s_logger.debug("[RepositoryAwareLuceneBase::initialise] " + "Repository '" + repository + "' -- Initializing Lucene with root: " + basePath);
        }

		final File baseDir = new File(basePath);

		indexInfo = IndexInfo.getIndexInfo(baseDir, config);

        if(s_logger.isDebugEnabled()){
		    s_logger.debug("[RepositoryAwareLuceneBase::initialise] " + "Repository '" + repository + "' -- Got IndexInfo: " + indexInfo + " [Cleaner: " + indexInfo.isEnableCleanerThread() +" - Merger: " +indexInfo.isEnableMerger() + "]");
        }

        // MB: Inizializza le IndexInfoProxy sia TC che NON TC
        // Ci serve per evitare che gli oggetti IndexInfoProxyService, terracottati, vengano usati con degli oggetti a null
        initIndexInfoProxyService();

		try {
			if (deltaId != null) {
				//				indexInfo.setStatus(deltaId, TransactionStatus.ACTIVE, null, null);
                //MB: modificato in modo da usare il setStatus che memorizza lo stato del delta
				//service.setStatus(deltaId, getRepoStorePath(), TransactionStatus.ACTIVE);
				setStatus(TransactionStatus.ACTIVE);
			}
		} catch (IOException e) {
			throw new IndexerException("Failed to set delta as active.");
		}

	}

	/**
	 * Metodo per creare il Proxy per Index Info
	 */
	protected void initIndexInfoProxyService() {
		s_logger.debug("[RepositoryAwareLuceneBase::initIndexInfoProxyService] creating indexInfoProxyService with deltaId :" + deltaId);
		if (this.service == null) {
   			s_logger.debug("[RepositoryAwareLuceneBase::initIndexInfoProxyService] creating indexInfoProxyService");
    		this.serviceTC = new IndexInfoProxyService();
   			s_logger.debug("[RepositoryAwareLuceneBase::initIndexInfoProxyService] creating indexInfoProxyServiceNoTC");
	    	this.serviceNoTC = new IndexInfoProxyServiceNoTC();
		}

		serviceTC.init(dictionaryService, config, getRepoStorePath());
		serviceNoTC.init(dictionaryService, config, getRepoStorePath());

		if( isCaller() ){
			service=serviceTC;
		} else {
			service=serviceNoTC;
		}
	}

    private boolean isCaller(){
       Throwable ex = new Throwable();
       boolean useTC=true;
       // Get the list of StackTraceElements from the throwable.
       StackTraceElement[] stackElements = ex.getStackTrace();
       String buffer="";
       for (int lcv=0;lcv<stackElements.length;lcv++) {
           String className  = stackElements[lcv].getClassName();
           if( className.indexOf("IndexRecoveryJob"         )!=-1 || // Processo di recover degli indici
               className.indexOf("EcmEngineStartup"         )!=-1 || // Processo di partenza di check dell'ambiente
               className.indexOf("TenantAdminJob"           )!=-1 || // Processo di creazione dei tenant. Index verra' allineato in modo asincrono
               className.indexOf("CustomModelActivationJob" )!=-1 ){ // Processo di allineamento dei custom model. Index verra' allineato in modo asincrono
        	   useTC=false;
           } else if(className.indexOf("doqui"   )!=-1 || // Loggo le classi doqui
                     className.indexOf("alfresco")!=-1 ){ // Loggo le classi alfresco
        	   if(s_logger.isDebugEnabled()){
                   String method     = stackElements[lcv].getMethodName();
                   String linenumber = ""+stackElements[lcv].getLineNumber();
        		   buffer+=( "ST_REPOAWARE["+lcv+"]: "+className+"."+method+"():"+linenumber+"\n");
        	   }
           }
       }
       if( useTC ){
           if(s_logger.isDebugEnabled()){
    	       s_logger.debug("\n"+buffer+"\n");
           }
       }
       return useTC;
    }

    /**
	 * Utility method to find the path to the base index.
	 *
	 * @return The base path.
	 */
	private String getBasePath() {
		if (config.getIndexRootLocation() == null) {
			throw new IndexerException("No configuration for index location");
		}

		String basePath = config.getIndexRootLocation() + File.separator + getRepoStorePath();

		return basePath;
	}

	protected String getRepoStorePath() {
		return repoId + File.separator + store.getProtocol() + File.separator + store.getIdentifier() + File.separator;
	}

	/**
	 * Get the repository ID.
	 *
	 * @return The repository ID.
	 */
	public String getRepository() {
		return this.repoId;
	}

	/**
	 * Get a searcher for the main index. TODO: Split out support for the main index. We really only need this if we want to search over the changing
	 * index before it is committed.
	 *
	 * @return The searcher.
	 * @throws IOException
	 */
	protected IndexSearcher getSearcher() throws LuceneIndexException {
		initIndexInfoProxyService();
		try {
            if(s_logger.isDebugEnabled()){
	    		s_logger.debug("[RepositoryAwareLuceneBase::getSearcher] " + "Repository '" + getRepository() + "' -- " + "Creating IndexSearcher from main IndexReader.");
            }
			// return new RepositoryAwareClosingIndexSearcher(indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader());
			return new RepositoryAwareClosingIndexSearcher(service.getReader(getRepoStorePath()));
		} catch (IOException e) {
			s_logger.error("[RepositoryAwareLuceneBase::getSearcher] " + "Repository '" + getRepository() + "' -- Input/Output error!", e);
			throw new LuceneIndexException("Failed to open IndexSearcher for path: " + getBasePath(), e);
		}
	}

	protected RepositoryAwareClosingIndexSearcher getSearcher(LuceneIndexer luceneIndexer) throws LuceneIndexException {
		initIndexInfoProxyService();
		// If we know the delta id we should do better
		try {
			if (luceneIndexer == null) {
                if(s_logger.isDebugEnabled()){
	    			s_logger.debug("[RepositoryAwareLuceneBase::getSearcher] Repository '" + getRepository() + "' -- " + "LuceneIndexer null. Creating IndexSearcher from main IndexReader.");
                }
 				// return new RepositoryAwareClosingIndexSearcher(indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader());
				return new RepositoryAwareClosingIndexSearcher(service.getReader(getRepoStorePath()));
			} else {
				// TODO: Create appropriate reader that lies about deletions
				// from the first
				luceneIndexer.flushPending();

                if(s_logger.isDebugEnabled()){
    				s_logger.debug("[RepositoryAwareLuceneBase::getSearcher] Repository '" + getRepository() + "' -- " + "LuceneIndexer flushed. Creating IndexSearcher from main IndexReader.");
                }

				//				return new RepositoryAwareClosingIndexSearcher(indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader(deltaId, luceneIndexer.getDeletions(), luceneIndexer.getDeleteOnlyNodes()));
				Set<String> deletions = new HashSet<String>();
				deletions.addAll(luceneIndexer.getDeletions());
				return new RepositoryAwareClosingIndexSearcher(service.getMainIndexReferenceCountingReadOnlyIndexReader(deltaId, getRepoStorePath(), deletions, luceneIndexer.getDeleteOnlyNodes()));
			}
		} catch (IOException e) {
			s_logger.error("[RepositoryAwareLuceneBase::getSearcher] " + "Repository '" + getRepository() + "' -- Input/Output error!", e);
			throw new LuceneIndexException("Failed to open RepositoryAwareClosingIndexSearcher for path: " + getBasePath(), e);
		}
	}

	/**
	 * Get a reader for the on file portion of the delta.
	 *
	 * @return The index reader.
	 * @throws IOException
	 */
	//	protected IndexReader getDeltaReader() throws LuceneIndexException, IOException {
	//		return indexInfo.getDeltaIndexReader(deltaId);
	//	}
	/**
	 * Close the on file reader for the delta if it is open.
	 *
	 * @throws IOException
	 */
	//	protected void closeDeltaReader() throws LuceneIndexException, IOException {
	//		indexInfo.closeDeltaIndexReader(deltaId);
	//	}
	/**
	 * Get the on file writer for the delta.
	 *
	 * @return The writer for the delta.
	 * @throws IOException
	 */
	//	protected IndexWriter getDeltaWriter() throws LuceneIndexException, IOException {
	//		return indexInfo.getDeltaIndexWriter(deltaId, new LuceneAnalyser(dictionaryService, config.getDefaultMLIndexAnalysisMode()));
	//	}
	/**
	 * Close the on disk delta writer.
	 *
	 * @throws IOException
	 */
	//	protected void closeDeltaWriter() throws LuceneIndexException, IOException {
	//		indexInfo.closeDeltaIndexWriter(deltaId);
	//	}
	//
	/**
	 * Save the in memory delta to the disk, make sure there is nothing held in memory.
	 *
	 * @throws IOException
	 */
	protected void saveDelta() throws LuceneIndexException, IOException {
		// Only one should exist so we do not need error trapping to execute the
		// other
		service.saveDelta(deltaId, getRepoStorePath());
	}

	protected void setInfo(long docs, Set<String> deletions, boolean deleteNodesOnly) throws IOException {
		service.setPreparedState(deltaId,getRepoStorePath(), deletions, docs, deleteNodesOnly);
		//		indexInfo.setPreparedState(deltaId, deletions, docs, deleteNodesOnly);
	}

	protected void setStatus(TransactionStatus status) throws IOException {
		//		indexInfo.setStatus(deltaId, status, null, null);
		service.setStatus(deltaId, getRepoStorePath(), status);
		this.status = status;
	}

	protected TransactionStatus getStatus() {
		return status;
	}

	//	protected IndexReader getReader() throws LuceneIndexException, IOException {
	//		return indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader();
	//	}

	/**
	 * Set the dictionary service
	 *
	 * @param dictionaryService
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		if (serviceTC != null) {
			this.serviceTC.setDictionaryService(dictionaryService);
		}
		if (serviceNoTC != null) {
			this.serviceNoTC.setDictionaryService(dictionaryService);
		}
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Get the dictionary service.
	 *
	 * @return The dictionary service.
	 */
	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	/**
	 * Set the Lucene configuration options.
	 *
	 * @param config The Lucene configuration options.
	 */
	public void setLuceneConfig(LuceneConfig config) {
		this.config = config;
	}

	/**
	 * Get the Lucene configuration options.
	 *
	 * @return The configuration options object.
	 */
	public LuceneConfig getLuceneConfig() {
		return config;
	}

	/**
	 * Get the ID for the delta we are working with.
	 *
	 * @return The ID.
	 */
	public String getDeltaId() {
		return deltaId;
	}

	/**
	 * Execute actions while holding the write lock oin the index
	 *
	 * @param <R>
	 * @param lockWork The work to do with write lock.
	 * @return The result returned by the action.
	 */
	public <R> R doWithWriteLock(LockWork<R> lockWork) {
		return indexInfo.doWithWriteLock(lockWork);
	}
}

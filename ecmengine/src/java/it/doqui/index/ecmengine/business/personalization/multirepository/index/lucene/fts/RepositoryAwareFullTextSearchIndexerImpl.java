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

package it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts;

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareIndexerAndSearcher;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareSupportsBackgroundIndexing;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryBackgroundIndexerAware;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.fts.FTSIndexerException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class RepositoryAwareFullTextSearchIndexerImpl implements RepositoryFTSIndexerAware, RepositoryAwareFullTextSearchIndexer {

	private static Set<Pair<String, StoreRef>> requiresIndex = new LinkedHashSet<Pair<String, StoreRef>>();
	private static Set<Pair<String, StoreRef>> indexing = new HashSet<Pair<String, StoreRef>>();

    private RepositoryAwareIndexerAndSearcher indexerAndSearcherFactory;

    private int pauseCount = 0;

    private boolean paused = false;

    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

    private RetryingTransactionHelper retryingTransactionHelper;

    public RepositoryAwareFullTextSearchIndexerImpl() {
        super();
    }

    public synchronized void requiresIndex(StoreRef storeRef, String repository) {
    	logger.debug("[RepositoryAwareFullTextSearchIndexerImpl::requiresIndex] Requested index of: " + storeRef + " - " + repository);
        requiresIndex.add(new Pair<String, StoreRef>(repository, storeRef));
    }

    public synchronized void indexCompleted(StoreRef storeRef, String repository, int remaining, Exception e) {
        try {
        	Pair<String, StoreRef> pair = new Pair<String, StoreRef>(repository, storeRef);

            indexing.remove(pair);
            if ((remaining > 0) || (e != null)) {
                requiresIndex(storeRef, repository);
            }

            if (e != null) {
                throw new FTSIndexerException(e);
            }
        } finally {
            this.notifyAll();
        }
    }

    public synchronized void pause() throws InterruptedException {
        pauseCount++;
        while ((!indexing.isEmpty())) {
            this.wait();
        }
        pauseCount--;
        if (pauseCount == 0) {
            paused = true;
            this.notifyAll(); // only resumers
        }
    }

    public synchronized void resume() throws InterruptedException {
        if (pauseCount == 0) {
            paused = false;
        } else {
            while (pauseCount > 0) {
                this.wait();
            }
            paused = false;
        }
    }

    @SuppressWarnings("unused")
    private synchronized boolean isPaused() throws InterruptedException {
        if (pauseCount == 0) {
            return paused;
        } else {
            while (pauseCount > 0) {
                this.wait();
            }
            return paused;
        }
    }

    public void index() {
    	// Use the calling thread to index
    	// Parallel indexing via multiple Quartz thread initiating indexing
    	final RepositoryFTSIndexerAware ftsIndexer = this;

    	int done = 0;
    	while (done == 0) {
    		Pair<String, StoreRef> toIndex = getNextPair();
    		if (toIndex != null) {

    			logger.debug("[RepositoryAwareFullTextSearchIndexer::getNextPair] " +
    					"Retrieving Indexer for Pair: " + toIndex.getFirst() + " - " + toIndex.getSecond());

    			final String repository = toIndex.getFirst();
    			final StoreRef storeRef = toIndex.getSecond();

    			final String origRepo = RepositoryManager.getCurrentRepository();

    			RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>() {
    				public Long execute() {
    					long myDone = 0;
    	    			final Indexer indexer = indexerAndSearcherFactory.getIndexer(storeRef, repository);

    	    			if (indexer instanceof RepositoryBackgroundIndexerAware) {
    	    				RepositoryBackgroundIndexerAware backgroundIndexerAware = (RepositoryBackgroundIndexerAware)indexer;
    	    				backgroundIndexerAware.registerCallBack(ftsIndexer);
    	    				myDone = backgroundIndexerAware.updateFullTextSearch(1000);
    	    			}
    					return myDone;
    				}
    			};

    			RepositoryManager.setCurrentRepository(repository);
    			try {
    				done += retryingTransactionHelper.doInTransaction(callback, false, true);
    			} finally {
    				RepositoryManager.setCurrentRepository(origRepo);
    			}
    		} else {
    			break;
    		}
    	}
    }

    private synchronized Pair<String, StoreRef> getNextPair() {
        if (paused || (pauseCount > 0)) {
            return null;
        }

        Pair<String, StoreRef> nextPair = null;

        logger.debug("[RepositoryAwareFullTextSearchIndexer::getNextPair] Looking for next pair in: " + requiresIndex);

        for (Pair<String, StoreRef> pair : requiresIndex) {
            if (!indexing.contains(pair)) {
                nextPair = pair;

                // FIFO
                break;
            } else {
            	logger.debug("[RepositoryAwareFullTextSearchIndexer::getNextPair] Already indexing: " + pair.getFirst() + " - " + pair.getSecond());
            }
        }

        if (nextPair != null) {
        	logger.debug("[RepositoryAwareFullTextSearchIndexer::getNextPair] Next to index: " + nextPair.getFirst() + " - " + nextPair.getSecond());
            requiresIndex.remove(nextPair);
            indexing.add(nextPair);
        }

        return nextPair;
    }

    /**
     * @param indexerAndSearcherFactory
     */
    public void setIndexerAndSearcherFactory(RepositoryAwareIndexerAndSearcher indexerAndSearcherFactory) {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }

    /**
     * @param beanFactory
     * @throws InterruptedException
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Find bean implementing RepositoryAwareSupportsBackgroundIndexing and register
        for (Object bgindexable : beanFactory.getBeansOfType(RepositoryAwareSupportsBackgroundIndexing.class).values()) {
            if (bgindexable instanceof RepositoryAwareSupportsBackgroundIndexing) {
                ((RepositoryAwareSupportsBackgroundIndexing)bgindexable).setFullTextSearchIndexer(this);
            }
        }
    }

	public RetryingTransactionHelper getRetryingTransactionHelper() {
		return retryingTransactionHelper;
	}

	public void setRetryingTransactionHelper(
			RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}
}

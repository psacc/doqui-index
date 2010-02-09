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

import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareSupportsBackgroundIndexing;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts.RepositoryAwareFullTextSearchIndexer;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepositoryAwareAVMLuceneIndexerAndSearcherFactory 
extends RepositoryAwareAbstractLuceneIndexerAndSearcherFactory 
implements RepositoryAwareSupportsBackgroundIndexing {
	
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);
    
    private DictionaryService dictionaryService;

    private NamespaceService nameSpaceService;

    private ContentService contentService;

    private AVMService avmService;

    private AVMSyncService avmSyncService;

    private NodeService nodeService;

    private ContentStore contentStore;

    private RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer;

    public RepositoryAwareAVMLuceneIndexerAndSearcherFactory() { }
    
    /**
     * Set the dictionary service
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the name space service
     * @param nameSpaceService
     */
    public void setNameSpaceService(NamespaceService nameSpaceService) {
        this.nameSpaceService = nameSpaceService;
    }

    /**
     * Set the content service
     * @param contentService
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * Set the AVM service
     * @param avmService
     */
    public void setAvmService(AVMService avmService) {
        this.avmService = avmService;
    }

    /**
     * Set the AVM sync service
     * @param avmSyncService
     */
    public void setAvmSyncService(AVMSyncService avmSyncService) {
        this.avmSyncService = avmSyncService;
    }

    /**
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
 
    /**
     * Set the content service
     * @param contentStore
     */
    public void setContentStore(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    @Override
    protected LuceneIndexer createIndexer(StoreRef storeRef, String repository, String deltaId) {
        logger.debug("[RepositoryAwareAVMLuceneIndexerAndSearcherFactory::createIndexer] BEGIN");

		try {
			RepositoryAwareAVMLuceneIndexerImpl indexer = 
				RepositoryAwareAVMLuceneIndexerImpl.getUpdateIndexer(storeRef, deltaId, this, repository);
			indexer.setDictionaryService(dictionaryService);
			indexer.setContentService(contentService);
			indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
			indexer.setAvmService(avmService);
			indexer.setAvmSyncService(avmSyncService);
			indexer.setContentStore(contentStore);
			indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
			return indexer;
		} finally {
			logger.debug("[RepositoryAwareAVMLuceneIndexerAndSearcherFactory::createIndexer] END");
		}
    }

    @Override
    protected List<StoreRef> getAllStores() {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(stores.size());
        
        for(AVMStoreDescriptor storeDesc : stores) {
            StoreRef storeRef = AVMNodeConverter.ToStoreRef(storeDesc.getName());
            storeRefs.add(storeRef);
        }
        
        return storeRefs;
    }

    @Override
    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer, String repository) throws SearcherException {
        logger.debug("[RepositoryAwareAVMLuceneIndexerAndSearcherFactory::getSearcher] BEGIN");

		try {
			//TODO: Store overlays
			RepositoryAwareADMLuceneSearcherImpl searcher = 
				RepositoryAwareADMLuceneSearcherImpl.getSearcher(storeRef, indexer, repository, this);
			searcher.setNamespacePrefixResolver(nameSpaceService);
			searcher.setNodeService(nodeService);
	        searcher.setTenantService(tenantService);
			searcher.setDictionaryService(dictionaryService);
			searcher.setQueryRegister(getQueryRegister());
			return searcher;
		} finally {
			logger.debug("[RepositoryAwareAVMLuceneIndexerAndSearcherFactory::getSearcher] END");
		}
    }

    /** 
     * Register the full text searcher (done by the searcher bean to break cyclic bean definitions) .
     */
    public void setFullTextSearchIndexer(RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer) {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }
}

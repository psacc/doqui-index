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
 
package it.doqui.index.ecmengine.business.personalization.splitting.index.lucene;

import it.doqui.index.ecmengine.business.personalization.splitting.SplittingNodeService;

import java.util.List;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.SupportsBackgroundIndexing;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneIndexerAndSearcherFactory;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;

public class SplittingADMLuceneFactory extends AbstractLuceneIndexerAndSearcherFactory 
	implements SupportsBackgroundIndexing {
	
    private DictionaryService dictionaryService;

    private NamespaceService nameSpaceService;

    private SplittingNodeService nodeService;
    
    private FullTextSearchIndexer fullTextSearchIndexer;

    private ContentService contentService;

    /**
     * Set the dictionary service.
     * 
     * @param dictionaryService The dictionary service.
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the namespace service.
     * 
     * @param nameSpaceService The namespace service.
     */
    public void setNameSpaceService(NamespaceService nameSpaceService) {
        this.nameSpaceService = nameSpaceService;
    }

    /**
     * Set the node service with splitting support.
     * 
     * @param nodeService The node service with splitting support.
     */
    public void setSplittingNodeService(SplittingNodeService nodeService) {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the full-text indexer with multirepository support.
     * 
     * @param fullTextSearchIndexer The full-text indexer with multirepository support.
     */
    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer) {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

    /**
     * Set the content service.
     * 
     * @param contentService The content service.
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    protected LuceneIndexer createIndexer(StoreRef storeRef, String deltaId) {
        storeRef = tenantService.getName(storeRef);

        SplittingADMIndexerImpl indexer = 
        SplittingADMIndexerImpl.getUpdateIndexer(storeRef, deltaId, this);

        indexer.setSplittingNodeService(nodeService);
        indexer.setTenantService(tenantService);
        indexer.setDictionaryService(dictionaryService);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        indexer.setContentService(contentService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        
        return indexer;
    }

    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer) throws SearcherException {
        storeRef = tenantService.getName(storeRef);

        ADMLuceneSearcherImpl searcher = 
        	ADMLuceneSearcherImpl.getSearcher(storeRef, indexer, this);
        
        searcher.setNamespacePrefixResolver(nameSpaceService);
        searcher.setNodeService(nodeService);
        searcher.setTenantService(tenantService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryRegister(getQueryRegister());
        
        return searcher;
    }
   
    protected List<StoreRef> getAllStores() {
        return nodeService.getStores();
    }
}

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

import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareSupportsBackgroundIndexing;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.RepositoryAwareADMLuceneSearcherImpl;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.RepositoryAwareAbstractLuceneIndexerAndSearcherFactory;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts.RepositoryAwareFullTextSearchIndexer;
//import it.doqui.index.ecmengine.business.personalization.security.ReadersService;
import it.doqui.index.ecmengine.business.personalization.splitting.SplittingNodeService;
import it.doqui.index.fileformat.business.service.FileFormatServiceImpl;

import java.util.List;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;

public class MultiRepositorySplittingADMLuceneFactory extends RepositoryAwareAbstractLuceneIndexerAndSearcherFactory 
	implements RepositoryAwareSupportsBackgroundIndexing {
	
    private DictionaryService dictionaryService;

    private NamespaceService nameSpaceService;

    private SplittingNodeService nodeService;
    
//	private ReadersService readersService;
    
    private RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer;

    private ContentService contentService;

    private FileFormatServiceImpl fileformatService;
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
    public void setFullTextSearchIndexer(RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer) {
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

    public void setFileformatService(FileFormatServiceImpl fileformatService) {
		this.fileformatService = fileformatService;
	}    
    
    protected LuceneIndexer createIndexer(StoreRef storeRef, String repository, String deltaId) {
        storeRef = tenantService.getName(storeRef);
        MultiRepositorySplittingADMIndexerImpl indexer = 
        	MultiRepositorySplittingADMIndexerImpl.getUpdateIndexer(storeRef, deltaId, repository, this);

        indexer.setSplittingNodeService(nodeService);
        indexer.setTenantService(tenantService);
        indexer.setDictionaryService(dictionaryService);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        indexer.setContentService(contentService);
//		indexer.setReadersService(readersService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        indexer.setFileformatService(fileformatService);
        
        return indexer;
    }

    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer, String repository) throws SearcherException {
        storeRef = tenantService.getName(storeRef);
        RepositoryAwareADMLuceneSearcherImpl searcher = 
        	RepositoryAwareADMLuceneSearcherImpl.getSearcher(storeRef, indexer, repository, this);
        
        searcher.setNamespacePrefixResolver(nameSpaceService);
        searcher.setNodeService(nodeService);
        searcher.setTenantService(tenantService);
        searcher.setDictionaryService(dictionaryService);
//		searcher.setReadersService(readersService);
        searcher.setQueryRegister(getQueryRegister());
        
        return searcher;
    }
   
    protected List<StoreRef> getAllStores() {
        return nodeService.getStores();
    }
//
//	public void setReadersService(ReadersService readersService) {
//		this.readersService = readersService;
//	}
}

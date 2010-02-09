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
//import it.doqui.index.ecmengine.business.personalization.security.ReadersService;

import java.util.List;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepositoryAwareADMLuceneIndexerAndSearcherFactory
	extends RepositoryAwareAbstractLuceneIndexerAndSearcherFactory implements RepositoryAwareSupportsBackgroundIndexing {

	/** The dictionary service. */
    private DictionaryService dictionaryService;

    /** The namespace service. */
    private NamespaceService nameSpaceService;

    /** The node service. */
    private NodeService nodeService;
//
//	/** Readers service to get list of users with Read permission. */
//	private ReadersService readersService;

    /** The indexer for full-text search. */
    private RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer;

    /** The content service. */
    private ContentService contentService;

    private Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

    /**
     * Set the dictionary service.
     *
     * @param dictionaryService The dictionary service.
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the name space service.
     *
     * @param nameSpaceService The namespace service.
     */
    public void setNameSpaceService(NamespaceService nameSpaceService) {
        this.nameSpaceService = nameSpaceService;
    }

    /**
     * Set the node service.
     *
     * @param nodeService The node service.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set the full-text search indexer.
     *
     * @param fullTextSearchIndexer The full-text indexer.
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

    protected LuceneIndexer createIndexer(StoreRef storeRef, String repository, String deltaId) {
        storeRef = tenantService.getName(storeRef);

        RepositoryAwareADMLuceneIndexerImpl indexer =
        	RepositoryAwareADMLuceneIndexerImpl.getUpdateIndexer(storeRef, deltaId, repository, this);

        indexer.setNodeService(nodeService);
        indexer.setDictionaryService(dictionaryService);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        indexer.setContentService(contentService);
//		indexer.setReadersService(readersService);
        indexer.setTenantService(tenantService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());

        return indexer;
    }

    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer, String repository)
    throws SearcherException {
		if (logger.isDebugEnabled()) {
            logger.debug("[RepositoryAwareADMLuceneIndexerAndSearcherFactory::getSearcher] " +
        		"Repository '" + repository + "' -- " +
        		"Getting Searcher for store: " + storeRef + " [Indexer: " + indexer + "]");
        }

        storeRef = tenantService.getName(storeRef);

        RepositoryAwareADMLuceneSearcherImpl searcher =
        	RepositoryAwareADMLuceneSearcherImpl.getSearcher(storeRef, indexer, repository, this);

        searcher.setNamespacePrefixResolver(nameSpaceService);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
//		searcher.setReadersService(readersService);
        searcher.setTenantService(tenantService);
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

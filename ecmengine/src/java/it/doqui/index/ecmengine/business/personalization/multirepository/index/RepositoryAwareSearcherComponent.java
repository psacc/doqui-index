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

package it.doqui.index.ecmengine.business.personalization.multirepository.index;

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.search.AbstractSearcherComponent;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepositoryAwareSearcherComponent extends AbstractSearcherComponent {
    private RepositoryAwareIndexerAndSearcher indexerAndSearcherFactory;

    private Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

    public void setIndexerAndSearcherFactory(RepositoryAwareIndexerAndSearcher indexerAndSearcherFactory) {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }

    public ResultSet query(StoreRef store,
            String language,
            String query,
            Path[] queryOptions,
            QueryParameterDefinition[] queryParameterDefinitions) {
    	final String repository = RepositoryManager.getCurrentRepository();

        SearchService searcher = indexerAndSearcherFactory.getSearcher(store, repository, true);

		if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareSearcherComponent::query] Repository '" + repository + "' -- Got searcher: " + searcher);
        }

        return searcher.query(store, language, query, queryOptions, queryParameterDefinitions);
    }

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters) {
        throw new UnsupportedOperationException();
    }

    public ResultSet query(SearchParameters searchParameters) {
        if(searchParameters.getStores().size() != 1) {
            throw new IllegalStateException("Only one store can be searched at present");
        }
        final String repository = RepositoryManager.getCurrentRepository();

        StoreRef storeRef = searchParameters.getStores().get(0);
        SearchService searcher = indexerAndSearcherFactory.getSearcher(storeRef, repository, !searchParameters.excludeDataInTheCurrentTransaction());

		if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareSearcherComponent::query] Repository '" + repository + "' -- Got searcher: " + searcher);
        }

        return searcher.query(searchParameters);
    }

    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern) throws InvalidNodeRefException {
        return contains(nodeRef, propertyQName, googleLikePattern, SearchParameters.Operator.OR);
    }

    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern,
    		SearchParameters.Operator defaultOperator) throws InvalidNodeRefException {
    	final String repository = RepositoryManager.getCurrentRepository();

        SearchService searcher = indexerAndSearcherFactory.getSearcher(nodeRef.getStoreRef(), repository, true);

		if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareSearcherComponent::query] Repository '" + repository + "' -- Got searcher: " + searcher);
        }

        return searcher.contains(nodeRef, propertyQName, googleLikePattern);
    }

    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS) throws InvalidNodeRefException {
    	final String repository = RepositoryManager.getCurrentRepository();

        SearchService searcher = indexerAndSearcherFactory.getSearcher(nodeRef.getStoreRef(), repository, true);

		if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareSearcherComponent::like] Repository '" + repository + "' -- Got searcher: " + searcher);
        }

        return searcher.like(nodeRef, propertyQName, sqlLikePattern, includeFTS);
    }

    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
    		NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
    		throws InvalidNodeRefException, XPathException {
    	final String repository = RepositoryManager.getCurrentRepository();

        SearchService searcher = indexerAndSearcherFactory.getSearcher(contextNodeRef.getStoreRef(), repository, true);

		if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareSearcherComponent::selectNodes] Repository '" + repository + "' -- Got searcher: " + searcher);
        }

        return searcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
    		NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
    		throws InvalidNodeRefException, XPathException {
    	final String repository = RepositoryManager.getCurrentRepository();

        SearchService searcher = indexerAndSearcherFactory.getSearcher(contextNodeRef.getStoreRef(), repository, true);

		if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareSearcherComponent::selectProperties] Repository '" + repository + "' -- Got searcher: " + searcher);
        }

        return searcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }
}

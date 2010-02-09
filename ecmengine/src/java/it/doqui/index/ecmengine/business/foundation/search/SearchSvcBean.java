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
 
package it.doqui.index.ecmengine.business.foundation.search;

import java.util.List;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.search.SearchRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;


public class SearchSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = -3914083526417881916L;

	public List<NodeRef> selectNodes(NodeRef nodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks) throws SearchRuntimeException {
		logger.debug("[SearchSvcBean::selectNodes] BEGIN");
		List<NodeRef> result = null;
		try {
			SearchService searchService = serviceRegistry.getSearchService();
			result = searchService.selectNodes(nodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks);
		} catch (RuntimeException e) {
			logger.warn("[SearchSvcBean::selectNodes] Error executing query: " + e.getMessage());
			handleSearchServiceException("selectNodes", e);
		} finally {
			logger.debug("[SearchSvcBean::selectNodes] END");
		}
		return result;
	}

	public List<NodeRef> selectNodes(NodeRef nodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language) throws SearchRuntimeException {
		logger.debug("[SearchSvcBean::selectNodes] BEGIN");
		List<NodeRef> result = null;
		try {
			SearchService searchService = serviceRegistry.getSearchService();
			result = searchService.selectNodes(nodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
		} catch (RuntimeException e) {
			logger.warn("[SearchSvcBean::selectNodes] Error executing query: " + e.getMessage());
			handleSearchServiceException("selectNodes", e);
		} finally {
			logger.debug("[SearchSvcBean::selectNodes] END");
		}
		return result;
	}

	public ResultSet query(StoreRef store, String language, String query) throws SearchRuntimeException {
		logger.debug("[SearchSvcBean::query] BEGIN");
		ResultSet result = null;
		try {
			SearchService searchService = serviceRegistry.getSearchService();
			result = searchService.query(store, language, query);
		} catch (RuntimeException e) {
			logger.warn("[SearchSvcBean::query] Error executing query: " + e.getMessage());
			handleSearchServiceException("query", e);
		} finally {
			logger.debug("[SearchSvcBean::query] END");
		}
		return result;
	}


	public ResultSet query(SearchParameters searchParameters) throws SearchRuntimeException {
		logger.debug("[SearchSvcBean::query] BEGIN");
		ResultSet result = null;
		try {
			SearchService searchService = serviceRegistry.getSearchService();
			result = searchService.query(searchParameters);
		} catch (RuntimeException e) {
			logger.warn("[SearchSvcBean::query] Error executing query: " + e.getMessage());
			handleSearchServiceException("query", e);
		} finally {
			logger.debug("[SearchSvcBean::query] END");
		}
		return result;
	}


	public boolean contains(NodeRef nodeRef, String googleLikePattern) throws SearchRuntimeException {
		logger.debug("[SearchSvcBean::contains] BEGIN");
		boolean result = false;
		try {
			SearchService searchService = serviceRegistry.getSearchService();
			result = searchService.contains(nodeRef, ContentModel.PROP_TITLE, googleLikePattern);
		} catch (RuntimeException e) {
			handleSearchServiceException("contains", e);
		} finally {
			logger.debug("[SearchSvcBean::contains] END");
		}

		return result;
	}
	
	private void handleSearchServiceException(String methodName, Throwable e) throws SearchRuntimeException {
		logger.warn("[SearchSvcBean::handleSearchServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		throw new SearchRuntimeException(FoundationErrorCodes.GENERIC_SEARCH_SERVICE_ERROR);
	}
}

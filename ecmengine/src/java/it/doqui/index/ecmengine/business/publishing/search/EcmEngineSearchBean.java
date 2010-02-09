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

package it.doqui.index.ecmengine.business.publishing.search;

import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;

import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvc;
import it.doqui.index.ecmengine.business.foundation.search.SearchSvc;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.foundation.util.QueryBuilder;
import it.doqui.index.ecmengine.business.foundation.util.QueryBuilderParams;
import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.publishing.EcmEngineFeatureBean;
import it.doqui.index.ecmengine.business.publishing.util.MergeSort;
import it.doqui.index.ecmengine.business.publishing.util.Sort;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.Path;
import it.doqui.index.ecmengine.dto.engine.NodeArchiveParams;
import it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.dto.engine.management.Category;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.Namespace;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultProperty;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.dto.engine.search.SortField;
import it.doqui.index.ecmengine.dto.engine.search.TopCategory;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.TooManyResultsException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.exception.repository.NodeRuntimeException;
import it.doqui.index.ecmengine.exception.search.SearchRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class EcmEngineSearchBean extends EcmEngineFeatureBean {

	private static final long serialVersionUID = -2926269188489773170L;

	private static final String QUERY_ARCHIVE_ALL = "+PARENT:\"%s\" +ASPECT:\"%s\"";
	private static final String QUERY_ARCHIVE_BY_TYPE = "+PARENT:\"%s\" +ASPECT:\"%s\" +%s:\"%s\"";

//	private static int MAX_CHILDREN = 40;



	public String nodeExists(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, NoDataExtractedException,
	RemoteException, InvalidCredentialsException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::nodeExists] BEGIN");

		validate(ValidationType.XPATH, "xpath", xpath);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "XPATH: " + xpath.getXPathQuery() + " U: " + context.getUsername();
		String result = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "nodeExists", logCtx, "Autenticazione completata.");

			final String escapedQuery;
			try {
				escapedQuery = QueryBuilder.escapeXPathQuery(xpath.getXPathQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + xpath.getXPathQuery();

				logger.warn("[EcmEngineSearchBean::nodeExists] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::nodeExists] Query - Q: " + escapedQuery);

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy(LimitBy.FINAL_SIZE);
			searchParams.setLimit(1);
			searchParams.setLanguage(SearchSvc.LANGUAGE_XPATH);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);

			List<NodeRef> nodeRefs = null;

			if (resultSet != null && resultSet.length() > 0) {
				nodeRefs = resultSet.getNodeRefs();

				if (nodeRefs.isEmpty()) {
					logger.debug("[EcmEngineSearchBean::nodeExists] Nodo non trovato.");
					throw new NoDataExtractedException(xpath.getXPathQuery(), context.getRepository());
				}

				int size = nodeRefs.size();

				logger.debug("[EcmEngineSearchBean::nodeExists] Trovati " + size + " nodo/i.");
				if (nodeRefs.get(0) != null) {
					result = nodeRefs.get(0).getId();
				}
				dumpElapsed("EcmEngineSearchBean", "nodeExists", logCtx, "Ricerca completata - " +size + " risultati.");
				logger.debug("[EcmEngineSearchBean::nodeExists] Ricerca completata - " + size + " risultati.");
			} else {
				logger.debug("[EcmEngineSearchBean::nodeExists] Nodo non trovato.");
				throw new NoDataExtractedException(xpath.getXPathQuery(), context.getRepository());
			}
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "nodeExists", context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::nodeExists] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::nodeExists] END");
		}
		return result;
	}

	public NodeResponse luceneSearchNoMetadata(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::luceneSearchNoMetadata] BEGIN");

		validate(ValidationType.LUCENE, "lucene", lucene);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "LUCENE: " + lucene.getLuceneQuery() +
		" U: " + context.getUsername();
		Node [] results = null;

		NodeResponse response = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "luceneSearchNoMetadata", logCtx, "Autenticazione completata.");

			String escapedQuery = null;

			try {
				escapedQuery = QueryBuilder.escapeLuceneQuery(lucene.getLuceneQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + lucene.getLuceneQuery();

				logger.warn("[EcmEngineSearchBean::luceneSearchNoMetadata] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::luceneSearchNoMetadata] Query - Q: " + escapedQuery);
			final int limit = lucene.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);
			dumpElapsed("EcmEngineSearchBean", "luceneSearchNoMetadata", logCtx, "Ricerca completata - " +resultSet.length() +" risultati trovati. "
                                                            +"(Page size: " + lucene.getPageSize()
                                                              +" - index: " + lucene.getPageIndex()
                                                              +" - limit: " + lucene.getLimit()     +")");

			results = translateResultSetToNodeArray(resultSet, lucene.getPageSize(), lucene.getPageIndex(), lucene.getSortFields());
			dumpElapsed("EcmEngineSearchBean", "luceneSearchNoMetadata", logCtx, "Risultati processati.");

			response = new NodeResponse();
			response.setNodeArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(lucene.getPageSize());
			response.setPageIndex(lucene.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "luceneSearchNoMetadata", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "luceneSearchNoMetadata", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::luceneSearchNoMetadata] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::luceneSearchNoMetadata] END");
		}
		return response;
	}

	public SearchResponse luceneSearch(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::luceneSearch] BEGIN");

		validate(ValidationType.LUCENE, "lucene", lucene);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "LUCENE: " + lucene.getLuceneQuery() +
		" U: " + context.getUsername();
		ResultContent [] results = null;

		SearchResponse response = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "luceneSearch", logCtx, "Autenticazione completata.");

			String escapedQuery = null;

			try {
				escapedQuery = QueryBuilder.escapeLuceneQuery(lucene.getLuceneQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + lucene.getLuceneQuery();

				logger.warn("[EcmEngineSearchBean::luceneSearch] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::luceneSearch] Query - Q: " + escapedQuery);
			final int limit = lucene.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(escapedQuery);

			// MB: gestione del retry
			// Essendo le indicizzazioni asincrone rispetto all'inserimento .. in caso di riferimento
			// a un nodo non valido, mi permetto di fare un retry
			// La cosa aiuta anche le installazioni in cluster, in quanto l'allineamento indici
			// in fase di clusler e' ancora piu' lento.
			int nMaxRetry = 5;
			for( int nRetry=0; nRetry<nMaxRetry; nRetry++ ){
				try {
					logger.debug("[EcmEngineSearchBean::luceneSearch] Prima di QUERY");

					resultSet = searchService.query(searchParams);
                    dumpElapsed("EcmEngineSearchBean", "luceneSearch", logCtx, "Ricerca completata - " +resultSet.length() +" risultati trovati. "
                                                                    +"(Page size: " + lucene.getPageSize()
                                                                      +" - index: " + lucene.getPageIndex()
                                                                      +" - limit: " + lucene.getLimit()     +")");

					results = translateResultSet(resultSet, lucene.getPageSize(), lucene.getPageIndex(), lucene.getSortFields(), lucene.isFullProperty(),lucene.getProperties());
					dumpElapsed("EcmEngineSearchBean", "luceneSearch", logCtx, "Risultati processati.");

					break;
				} catch (EcmEngineFoundationException e) {
					logger.error("[EcmEngineSearchBean::luceneSearch] ERRORE: (" +nRetry +")" +e.getCode());
					// Verifico se si tratta di un "INVALID_NODE_REF_ERROR" e fino al penultimo faccio sleep
					if( e.getCode()==FoundationErrorCodes.INVALID_NODE_REF_ERROR && (nRetry+1)<nMaxRetry){
                        if (resultSet != null) {
                            resultSet.close();
                            resultSet = null;
                        }
						// Log
						logger.debug("[EcmEngineSearchBean::luceneSearch] INVALID_NODE_REF_ERROR: retry " +nRetry);

						// Sleep di 500 millis
						try {
							Thread.sleep(500);
						} catch (Throwable xx) {}

						// Wake up
						logger.debug("[EcmEngineSearchBean::luceneSearch] INVALID_NODE_REF_ERROR: wake up");
					} else {
						// All'ultimo giro o in caso di errore NON INVALID_NODE_REF_ERROR, rimbalzo l'eccezione
						throw e;
					}
				}
			}
			// -------------

			// Codice senza retry
			// resultSet = searchService.query(searchParams);
			// dumpElapsed("EcmEngineSearchBean", "luceneSearch", logCtx, "Ricerca completata - " +resultSet.length() + " risultati.");
			//
			// results = translateResultSet(resultSet, lucene.getPageSize(), lucene.getPageIndex(), lucene.getSortFields(), lucene.isFullProperty());
			// dumpElapsed("EcmEngineSearchBean", "luceneSearch", logCtx, "Risultati processati.");
			// -------------

			response = new SearchResponse();
			response.setResultContentArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(lucene.getPageSize());
			response.setPageIndex(lucene.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "luceneSearch", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "luceneSearch", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::luceneSearch] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::luceneSearch] END");
		}
		return response;
	}

	public int getTotalResultsLucene(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, SearchException, InvalidCredentialsException, RemoteException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::getTotalResultsLucene] BEGIN");

		validate(ValidationType.LUCENE, "lucene", lucene);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "LUCENE: " + lucene.getLuceneQuery() +
		" U: " + context.getUsername();


		int totalResults = 0;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getTotalResultsLucene", logCtx, "Autenticazione completata.");

			String escapedQuery = null;

			try {
				escapedQuery = QueryBuilder.escapeLuceneQuery(lucene.getLuceneQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + lucene.getLuceneQuery();

				logger.warn("[EcmEngineSearchBean::getTotalResultsLucene] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::getTotalResultsLucene Query - Q: " + escapedQuery);


			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy(LimitBy.UNLIMITED);
			searchParams.setLimit(0);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);

			totalResults = (resultSet != null) ? resultSet.length() :  0;

			dumpElapsed("EcmEngineSearchBean", "getTotalResultsLucene", logCtx, "Ricerca completata - " +
					totalResults + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getTotalResultsLucene", context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getTotalResultsLucene] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::getTotalResultsLucene] END");
		}
        return totalResults;
	}

	public SearchResponse xpathSearch(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::xpathSearch] BEGIN");

		validate(ValidationType.XPATH, "xpath", xpath);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		SearchResponse response = null;

		ResultSet resultSet = null;

		final String logCtx = "XPATH: " + xpath.getXPathQuery() + " U: " + context.getUsername();
		ResultContent [] results = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "xpathSearch", logCtx, "Autenticazione completata.");

			final String escapedQuery;
			try {
				escapedQuery = QueryBuilder.escapeXPathQuery(xpath.getXPathQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + xpath.getXPathQuery();

				logger.warn("[EcmEngineSearchBean::xpathSearch] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

            if(logger.isDebugEnabled()) {
			    logger.debug("[EcmEngineSearchBean::xpathSearch] Query - Q: " + escapedQuery);
			}

			final int limit = xpath.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_XPATH);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);
			dumpElapsed("EcmEngineSearchBean", "xpathSearch", logCtx, "Ricerca completata - " +resultSet.length() +" risultati trovati. "
                                                            +"(Page size: " + xpath.getPageSize()
                                                              +" - index: " + xpath.getPageIndex()
                                                              +" - limit: " + xpath.getLimit()     +")");

			results = translateResultSet(resultSet, xpath.getPageSize(), xpath.getPageIndex(), xpath.getSortFields(), xpath.isFullProperty(),xpath.getProperties());
			dumpElapsed("EcmEngineSearchBean", "xpathSearch", logCtx, "Risultati processati.");

			response = new SearchResponse();
			response.setResultContentArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(xpath.getPageSize());
			response.setPageIndex(xpath.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "xpathSearch", context.getUsername(), null);
			logger.error("[EcmEngineSearchBean::xpathSearch] Foundation services error: " + e.getCode());

			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::xpathSearch] END");
		}
		return response;
	}

	public NodeResponse selectNodes(Node node, SearchParamsAggregate parameterAggregate, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineSearchBean::selectNodes] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.XPATH_AGGREGATE, "xpath", parameterAggregate);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		Node [] results = null;

		NodeResponse response = null;

		final String logCtx = "Uid: " + node.getUid() +" - XPATH: " +parameterAggregate.getXPathQuery() +" - U: " + context.getUsername();

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "selectNodes", logCtx, "Autenticazione completata.");

			final NodeRef nodeRef = checkNodeExists(node, null);

			final String escapedQuery;
			try {
				escapedQuery = QueryBuilder.escapeXPathQuery(parameterAggregate.getXPathQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + parameterAggregate.getXPathQuery();

				logger.warn("[EcmEngineSearchBean::selectNodes] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::selectNodes] Query - Q: " + escapedQuery);

			//final int limit = parameterAggregate.getLimit();
			//final SearchParameters searchParams = new SearchParameters();
			//searchParams.addStore(DictionarySvc.SPACES_STORE);
			//searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			//searchParams.setLimit(limit);
			//searchParams.setLanguage(SearchSvc.LANGUAGE_XPATH);
			//searchParams.setQuery(escapedQuery);

			DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);

			/*
	        namespacePrefixResolver.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
	        namespacePrefixResolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
	        namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
	        namespacePrefixResolver.registerNamespace("macta-agg", "http://www.doqui.it/microacta/model/aggregazioni/1.0");
	        namespacePrefixResolver.registerNamespace("macta-doc", "http://www.doqui.it/microacta/model/documenti/1.0");
	        namespacePrefixResolver.registerNamespace("macta-arc", "http://www.doqui.it/microacta/model/archivio/1.0");
			 */

			/* Lascio libera la possibilita' di impostare i namespace su cui effettuare la ricerca
			 * se ho valorizzato l'attributo namespace sull'oggetto SearchParamsAggregate  allora registro i namespace impostati
			 * altrimenti mi faccio restituire dal NamespaceService tutti i namespace registrati e li registro tutti
			 */


			Namespace[] elencoNamespace = parameterAggregate.getNamespace();
			logger.debug("[EcmEngineSearchBean::selectNodes] elencoNamespace vale :" + elencoNamespace);
			if(elencoNamespace != null && elencoNamespace.length > 0) {
				logger.debug("[EcmEngineSearchBean::selectNodes] entro in elencoNamespace diverso da null");
				for (Namespace namespace : elencoNamespace) {
					logger.debug("[EcmEngineSearchBean::selectNodes] namespace prefix: [" + namespace.getPrefix() +"] URI: [" + namespace.getUri()+"]");
					namespacePrefixResolver.registerNamespace(namespace.getPrefix(), namespace.getUri());
				}
			} else {
				logger.debug("[EcmEngineSearchBean::selectNodes] entro in elencoNamespace uguale a null");
				Collection<String> prefixesList = namespaceService.getPrefixes();
				logger.debug("[EcmEngineSearchBean::selectNodes] prefixesList vale: "+prefixesList);

				for (String inPrefix : prefixesList) {
					String namespace = namespaceService.getNamespaceURI(inPrefix);
					logger.debug("[EcmEngineSearchBean::selectNodes] Namespace prefix: [" + inPrefix + "] URI: [" + namespace+"]");
					namespacePrefixResolver.registerNamespace(inPrefix, namespace);
				}

			}

			List<NodeRef> nodes = searchService.selectNodes(nodeRef, escapedQuery, null, namespacePrefixResolver, parameterAggregate.isFollowAllParentLinks(), SearchSvc.LANGUAGE_XPATH);

			/* Se non ha restituito dati eccezione */
			if(nodes == null) {
				//throw new InvalidParameterException("No data extracted");
				throw new NoSuchNodeException("No data extracted");
			}

			dumpElapsed("EcmEngineSearchBean", "selectNodes", logCtx, "Ricerca completata - " +nodes.size() +" risultati trovati. "
                                                            +"(Page size: " + parameterAggregate.getPageSize()
                                                              +" - index: " + parameterAggregate.getPageIndex()
                                                              +" - limit: " + parameterAggregate.getLimit()     +")");

			//Stefano LB(add ordinamento)
			/*
			response = new NodeResponse();
			int numberOfNodes = nodes.size();
			logger.debug("[EcmEngineSearchBean::selectNodes] numberOfNodes vale :"+numberOfNodes);
			results = new Node[numberOfNodes];

			dumpElapsed("EcmEngineSearchBean", "selectNodes", logCtx, "Prima formazione risultati");
			for(int indice = 0; indice < nodes.size(); indice ++) {
				Node nodeResult = new Node();
				nodeResult.setUid((nodes.get(indice)).getId());
				results[indice] = nodeResult;
			}
			dumpElapsed("EcmEngineSearchBean", "selectNodes", logCtx, "Dopo formazione risultati");
			response.setNodeArray(results);
			*/

			results = translateNodeListToNodeArray(nodes, parameterAggregate.getPageSize(), parameterAggregate.getPageIndex(), parameterAggregate.getSortFields());
			dumpElapsed("EcmEngineSearchBean", "selectNodes", logCtx, "Risultati processati.");

			response = new NodeResponse();
			response.setNodeArray(results);
			response.setTotalResults(nodes.size());
			response.setPageSize(parameterAggregate.getPageSize());
			response.setPageIndex(parameterAggregate.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "selectNodes", context.getUsername(), null);
			logger.error("[EcmEngineSearchBean::xpathSearch] Foundation services error: " + e.getCode());

			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineSearchBean::selectNodes] END");
		}
		return response;
	}



	public NodeResponse listDeletedNodesNoMetadata(NodeArchiveParams params, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException,SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::listDeletedNodesNoMetadata] BEGIN");

//		validate(ValidationType.LUCENE, "lucene", lucene);
		validate(ValidationType.NOT_NULL, "params", params);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "LIST DELETED: " + context.getRepository() +
		" U: " + context.getUsername();
		Node [] results = null;

		NodeResponse response = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "listDeletedNodesNoMetadata", logCtx, "Autenticazione completata.");

			String escapedQuery = null;

			/*
			 * Codice derivato da quello presente in NodeArchiveServiceImpl.getArchivedNodes().
			 *
			 * Non viene utilizzato direttamente tale metodo perche` vogliamo un controllo maggiore per consentire:
			 * 		1) Filtraggio per tipo
			 * 		2) Limitazione e paginazione del result set
			 *
			 * NB: nell'archivio tutti i nodi sono figli diretti della root dello store.
			 */
			final NodeRef archiveRootRef = nodeArchiveService.getStoreArchiveNode(DictionarySvc.SPACES_STORE);

			final String query = (params.getTypePrefixedName() == null)
			? String.format(QUERY_ARCHIVE_ALL,
					archiveRootRef.toString(),
					dictionaryService.resolveQNameToPrefixName(ContentModel.ASPECT_ARCHIVED))
					: String.format(QUERY_ARCHIVE_BY_TYPE,
							archiveRootRef.toString(),
							dictionaryService.resolveQNameToPrefixName(ContentModel.ASPECT_ARCHIVED),
							(params.isTypeAsAspect()) ? "ASPECT" : "TYPE",
									params.getTypePrefixedName());

			try {
				escapedQuery = QueryBuilder.escapeLuceneQuery(query);
			} catch (IllegalArgumentException e) {
				String errorMessage = "Parametro non valido: " + params.getTypePrefixedName();

				logger.warn("[EcmEngineSearchBean::listDeletedNodesNoMetadata] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::listDeletedNodesNoMetadata] Query - Q: " + escapedQuery);
			final int limit = params.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.ARCHIVE_SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(escapedQuery);

			try{
				resultSet = searchService.query(searchParams);
			}catch(SearchRuntimeException e){
				throw new NoSuchNodeException("Nessun nodo trovato per la query fornita.");
			}
			dumpElapsed("EcmEngineSearchBean", "listDeletedNodesNoMetadata", logCtx, "Ricerca completata - " +
					resultSet.length() + " risultati.");

			results = translateResultSetToNodeArray(resultSet, params.getPageSize(), params.getPageIndex(), null);
			dumpElapsed("EcmEngineSearchBean", "listDeletedNodesNoMetadata", logCtx, "Risultati processati.");

			response = new NodeResponse();
			response.setNodeArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(params.getPageSize());
			response.setPageIndex(params.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "listDeletedNodesNoMetadata", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "listDeletedNodesNoMetadata", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::listDeletedNodesNoMetadata] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::listDeletedNodesNoMetadata] END");
		}
		return response;
	}

	public SearchResponse listDeletedNodes(NodeArchiveParams params, OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::listDeletedNodes] BEGIN");



//		validate(ValidationType.LUCENE, "lucene", lucene);
		validate(ValidationType.NOT_NULL, "params", params);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "LIST DELETED: " + context.getRepository() +
		" U: " + context.getUsername();
		ResultContent [] results = null;

		SearchResponse response = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "listDeletedNodes", logCtx, "Autenticazione completata.");

			String escapedQuery = null;

			/*
			 * Codice derivato da quello presente in NodeArchiveServiceImpl.getArchivedNodes().
			 *
			 * Non viene utilizzato direttamente tale metodo perche` vogliamo un controllo maggiore per consentire:
			 * 		1) Filtraggio per tipo
			 * 		2) Limitazione e paginazione del result set
			 *
			 * NB: nell'archivio tutti i nodi sono figli diretti della root dello store.
			 */
			final NodeRef archiveRootRef = nodeArchiveService.getStoreArchiveNode(DictionarySvc.SPACES_STORE);

			final String query = (params.getTypePrefixedName() == null)
			? String.format(QUERY_ARCHIVE_ALL,
					archiveRootRef.toString(),
					dictionaryService.resolveQNameToPrefixName(ContentModel.ASPECT_ARCHIVED))
					: String.format(QUERY_ARCHIVE_BY_TYPE,
							archiveRootRef.toString(),
							dictionaryService.resolveQNameToPrefixName(ContentModel.ASPECT_ARCHIVED),
							(params.isTypeAsAspect()) ? "ASPECT" : "TYPE",
									params.getTypePrefixedName());

			try {
				escapedQuery = QueryBuilder.escapeLuceneQuery(query);
			} catch (IllegalArgumentException e) {
				String errorMessage = "Parametro non valido: " + params.getTypePrefixedName();

				logger.warn("[EcmEngineSearchBean::listDeletedNodes] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::listDeletedNodes] Query - Q: " + escapedQuery);
			final int limit = params.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.ARCHIVE_SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(escapedQuery);

			try{
				resultSet = searchService.query(searchParams);
			}catch(SearchRuntimeException  e){
				throw new NoSuchNodeException("Nessun nodo trovato per la query fornita.");
			}
			dumpElapsed("EcmEngineSearchBean", "listDeletedNodes", logCtx, "Ricerca completata - " +
					resultSet.length() + " risultati.");

			results = translateResultSet(resultSet, params.getPageSize(), params.getPageIndex(), null, false,null);
			dumpElapsed("EcmEngineSearchBean", "listDeletedNodes", logCtx, "Risultati processati.");

			response = new SearchResponse();
			response.setResultContentArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(params.getPageSize());
			response.setPageIndex(params.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "listDeletedNodes", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "listDeletedNodes", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::listDeletedNodes] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::listDeletedNodes] END");
		}
		return response;
	}

	public NodeResponse genericGlobalSearchNoMetadata(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::genericGlobalSearchNoMetadata] BEGIN");

		validate(ValidationType.NOT_NULL, "params", params);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "SEARCH: " + params + " U: " + context.getUsername();
		Node [] results = null;

		NodeResponse response = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "genericGlobalSearchNoMetadata", logCtx, "Autenticazione completata.");

			final String query = QueryBuilder.buildQuery(
					convertToQueryBuilderParams(params, dictionaryService));

			logger.debug("[EcmEngineSearchBean::genericGlobalSearchNoMetadata] Query - Q: " + query);
			final int limit = params.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(query);

			resultSet = searchService.query(searchParams);
			dumpElapsed("EcmEngineSearchBean", "genericGlobalSearchNoMetadata", logCtx, "Ricerca completata - " +resultSet.length() +" risultati trovati. "
                                                            +"(Page size: " + params.getPageSize()
                                                              +" - index: " + params.getPageIndex()
                                                              +" - limit: " + params.getLimit()     +")");

			results = translateResultSetToNodeArray(resultSet, params.getPageSize(), params.getPageIndex(), params.getSortFields());

			dumpElapsed("EcmEngineSearchBean", "genericGlobalSearchNoMetadata", logCtx, "Risultati processati.");

			response = new NodeResponse();
			response.setNodeArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(params.getPageSize());
			response.setPageIndex(params.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "genericGlobalSearchNoMetadata", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "genericGlobalSearchNoMetadata", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::genericGlobalSearchNoMetadata] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::genericGlobalSearchNoMetadata] END");
		}
		return response;
	}

	public SearchResponse genericGlobalSearch(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::genericGlobalSearch] BEGIN");

		validate(ValidationType.NOT_NULL, "params", params);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "SEARCH: " + params + " U: " + context.getUsername();
		ResultContent [] results = null;

		SearchResponse response = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "genericGlobalSearch", logCtx, "Autenticazione completata.");

			final String query = QueryBuilder.buildQuery(
					convertToQueryBuilderParams(params, dictionaryService));

			logger.debug("[EcmEngineSearchBean::genericGlobalSearch] Query - Q: " + query);
			final int limit = params.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_LUCENE);
			searchParams.setQuery(query);

			resultSet = searchService.query(searchParams);
			dumpElapsed("EcmEngineSearchBean", "genericGlobalSearch", logCtx, "Ricerca completata - " +resultSet.length() +" risultati trovati. "
                                                            +"(Page size: " + params.getPageSize()
                                                              +" - index: " + params.getPageIndex()
                                                              +" - limit: " + params.getLimit()     +")");

			results = translateResultSet(resultSet, params.getPageSize(), params.getPageIndex(), params.getSortFields(), params.isFullProperty(),params.getProperties());
			dumpElapsed("EcmEngineSearchBean", "genericGlobalSearch", logCtx, "Risultati processati.");

			response = new SearchResponse();
			response.setResultContentArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(params.getPageSize());
			response.setPageIndex(params.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "genericGlobalSearch", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "genericGlobalSearch", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::genericGlobalSearch] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::genericGlobalSearch] END");
		}
		return response;
	}

	public ResultAssociation[] getCategories(Category category, String depth,OperationContext context) throws InvalidParameterException,
	NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException
	{
		logger.debug("[EcmEngineSearchBean::getCategories] BEGIN");

		//validate(ValidationType.NODE             , "parent" , parent);
		validate(ValidationType.CATEGORY_ASPECT  , "category", category);
		validate(ValidationType.DEPTH            , "depth", depth);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

    	final String logCtx = "USER: " + context.getUsername() + " - CA: " + category.getAspectPrefixedName();

		ResultAssociation [] associations = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getCategories", logCtx, "Autenticazione completata.");

			//final NodeRef nodeRef = checkNodeExists(parent, null);

			final QName aspectName = dictionaryService.resolvePrefixNameToQName(category.getAspectPrefixedName());

			/* Lo SpacesStore STANDARD */
			StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			//Collection<ChildAssociationRef> categories = categoryService.getCategories(nodeRef.getStoreRef(),aspectName,Depth.valueOf(depth));

			Collection<ChildAssociationRef> categories = categoryService.getCategories(SPACES_STORE,
					aspectName, Depth.valueOf(depth));

			associations = translateCategoryChildAssoc(categories);

			dumpElapsed("EcmEngineSearchBean", "getCategories", logCtx, "Ricerca Categories completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

			logger.debug("[EcmEngineSearchBean::getCategories] Ricerca Categories completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getCategories", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getCategories", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getCategories] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineSearchBean::getCategories] END");
		}
		return associations;
	}

	public ResultAssociation[] getCategoryChildren(Node categoryNode, String mode, String depth,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException
	{
		logger.debug("[EcmEngineSearchBean::getCategoryChildren] BEGIN");

		validate(ValidationType.NODE             , "categoryNode" , categoryNode);
		validate(ValidationType.MODE             , "mode", mode);
		validate(ValidationType.DEPTH            , "depth", depth);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

    	final String logCtx = "CAN: " + categoryNode.getUid() + " - U: " + context.getUsername();

		ResultAssociation [] associations = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getCategoryChildren", logCtx, "Autenticazione completata.");

			final NodeRef categoryRef = checkNodeExists(categoryNode, null);

			Collection<ChildAssociationRef> categoryChildren = categoryService.getChildren(categoryRef,
					Mode.valueOf(mode), Depth.valueOf(depth));

			associations = translateCategoryChildAssoc(categoryChildren);

			dumpElapsed("EcmEngineSearchBean", "getCategoryChildren", logCtx, "Ricerca children categories completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

			logger.debug("[EcmEngineSearchBean::getCategoryChildren] Ricerca children categories completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getCategoryChildren", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getCategoryChildren", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getCategoryChildren] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineSearchBean::getCategoryChildren] END");
		}
		return associations;
	}



	public ResultAspect[] getClassificationAspects(OperationContext context) throws InvalidParameterException,
	SearchException, RemoteException, InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException
	{
		logger.debug("[EcmEngineSearchBean::getClassificationAspects] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "ClassificationAspects "+ " - U: " + context.getUsername();

		ResultAspect [] resultAspects = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getCategoryChildren", logCtx, "Autenticazione completata.");

			Collection<QName> aspects = categoryService.getClassificationAspects();

			final int aspectsSize = aspects.size();

			if (aspectsSize > 0) {
				resultAspects = new ResultAspect[aspectsSize];

				int j = 0;

				for (QName aspect : aspects) {
					resultAspects[j] = new ResultAspect();
					resultAspects[j].setPrefixedName(
							dictionaryService.resolveQNameToPrefixName(aspect));
					j++;
				}
			}

			dumpElapsed("EcmEngineSearchBean", "getClassificationAspects", logCtx, "Ricerca ClassificationAspects completata -" +
					" " + (resultAspects != null ? resultAspects.length : 0) + " risultati.");

			logger.debug("[EcmEngineSearchBean::getClassificationAspects] Ricerca ClassificationAspects completata -" +
					" " + (resultAspects != null ? resultAspects.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getClassificationAspects", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getClassificationAspects", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getClassificationAspects] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineSearchBean::getClassificationAspects] END");
		}
		return resultAspects;
	}

	public ResultAssociation[] getClassifications(OperationContext context) throws InvalidParameterException,
	NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException{

		logger.debug("[EcmEngineSearchBean::getClassifications] BEGIN");

		//validate(ValidationType.NODE             , "parent" , parent);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

    	final String logCtx = " U: " + context.getUsername();

		ResultAssociation [] associations = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getClassifications", logCtx, "Autenticazione completata.");

			//final NodeRef parentRef = checkNodeExists(parent, null);

			/* Lo SpacesStore STANDARD */
			StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			//Collection<ChildAssociationRef> classifications = categoryService.getClassifications(parentRef.getStoreRef());

			Collection<ChildAssociationRef> classifications = categoryService.getClassifications(SPACES_STORE);

			associations = translateCategoryChildAssoc(classifications);

			dumpElapsed("EcmEngineSearchBean", "getClassifications", logCtx, "Ricerca classifications completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

			logger.debug("[EcmEngineSearchBean::getClassifications] Ricerca classifications completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getClassifications", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getClassifications", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getClassifications] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineSearchBean::getClassifications] END");
		}
		return associations;
	}


	public ResultAssociation[] getRootCategories(Category category,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException,
	InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException
	{
		logger.debug("[EcmEngineSearchBean::getRootCategories] BEGIN");

		//validate(ValidationType.NODE             , "parent" , parent);
		validate(ValidationType.CATEGORY_ASPECT  , "category", category);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

    	final String logCtx = "A: " + category.getAspectPrefixedName() + " - USER: " + context.getUsername();

		ResultAssociation [] associations = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getRootCategories", logCtx, "Autenticazione completata.");

			//final NodeRef nodeRef = checkNodeExists(parent, null);

			/* Lo SpacesStore STANDARD */
			StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			final QName aspectName = dictionaryService.resolvePrefixNameToQName(category.getAspectPrefixedName());

			//Collection<ChildAssociationRef> categories = categoryService.getRootCategories(nodeRef.getStoreRef(),aspectName);

			Collection<ChildAssociationRef> categories = categoryService.getRootCategories(SPACES_STORE,
					aspectName);

			associations = translateCategoryChildAssoc(categories);

			dumpElapsed("EcmEngineSearchBean", "getRootCategories", logCtx, "Ricerca Root Categories completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

			logger.debug("[EcmEngineSearchBean::getRootCategories] Ricerca Root Categories completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getRootCategories", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getRootCategories", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getRootCategories] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineSearchBean::getRootCategories] END");
		}
		return associations;
	}


	public TopCategory[] getTopCategories(Category category,int count,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException,
	InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException
	{
		logger.debug("[EcmEngineSearchBean::getTopCategories] BEGIN");

		//validate(ValidationType.NODE             , "parent" , parent);
		validate(ValidationType.CATEGORY_ASPECT  , "category", category);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

    	final String logCtx = "A: " + category.getAspectPrefixedName() + " - USER: " + context.getUsername();

    	TopCategory[] topCategory = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getTopCategories", logCtx, "Autenticazione completata.");

			//final NodeRef nodeRef = checkNodeExists(parent, null);

			/* Lo SpacesStore STANDARD */
			StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			final QName aspectName = dictionaryService.resolvePrefixNameToQName(category.getAspectPrefixedName());

			//List<Pair<NodeRef,Integer>> topCategories = categoryService.getTopCategories(nodeRef.getStoreRef(),aspectName, count);

			List<Pair<NodeRef,Integer>> topCategories = categoryService.getTopCategories(SPACES_STORE,
					aspectName, count);

			if(!topCategories.isEmpty()){

				topCategory = new TopCategory[topCategories.size()];

				int j = 0;

				Node tempNode = null;

				for (Pair<NodeRef, Integer> pair : topCategories) {

					topCategory[j] = new TopCategory();
					if(pair.getFirst()!=null){
						tempNode = new Node();
						tempNode.setUid(pair.getFirst().getId());
					}
					topCategory[j].setNode(tempNode);
					topCategory[j].setCount(
							pair.getSecond()!=null ? pair.getSecond().intValue() : 0);

					j++;
				}
			}

			dumpElapsed("EcmEngineSearchBean", "getTopCategories", logCtx, "Ricerca Top Categories completata -" +
					" " + (topCategory != null ? topCategory.length : 0) + " risultati.");

			logger.debug("[EcmEngineSearchBean::getTopCategories] Ricerca Top Categories completata -" +
					" " + (topCategory != null ? topCategory.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getTopCategories", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getTopCategories", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getTopCategories] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineSearchBean::getTopCategories] END");
		}
		return topCategory;
	}

	public Node getUid(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, SearchException,
	InvalidCredentialsException, PermissionDeniedException, RemoteException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::getUid] BEGIN");

		validate(ValidationType.XPATH, "xpath", xpath);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultSet resultSet = null;
		final String logCtx = "XPATH: " + xpath.getXPathQuery() + " U: " + context.getUsername();
		Node result = null;

		start();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineSearchBean", "getUid", logCtx, "Autenticazione completata.");

			final String escapedQuery;
			try {
				escapedQuery = QueryBuilder.escapeXPathQuery(xpath.getXPathQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + xpath.getXPathQuery();

				logger.warn("[EcmEngineSearchBean::getUid] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			logger.debug("[EcmEngineSearchBean::getUid] Query - Q: " + escapedQuery);

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy(LimitBy.FINAL_SIZE);
			searchParams.setLimit(1);
			searchParams.setLanguage(SearchSvc.LANGUAGE_XPATH);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);

			List<NodeRef> nodeRefs = null;

			if (resultSet != null && resultSet.length() > 0) {
				nodeRefs = resultSet.getNodeRefs();
				int size = nodeRefs.size();
				logger.debug("[EcmEngineSearchBean::getUid] Trovati " + size + " nodo/i.");
				if (nodeRefs != null && nodeRefs.get(0) != null) {
					result = new Node(nodeRefs.get(0).getId());
				}
				dumpElapsed("EcmEngineSearchBean", "getUid", logCtx, "Ricerca completata - " + size + " risultati.");
				logger.debug("[EcmEngineSearchBean::getUid] Ricerca completata - " + size + " risultati.");
			} else{
				logger.debug("[EcmEngineSearchBean::getUid] Nodo non trovato.");
				throw new NoDataExtractedException(xpath.getXPathQuery(), context.getRepository());
			}
		} catch (EcmEngineFoundationException e) {
			logger.error("[EcmEngineSearchBean::getUid] Foundation services error: " + e.getCode());
			checkCredentialsException(e, "EcmEngineSearchBean", "getUid", context.getUsername(), null);
			checkAccessException(e, "EcmEngineSearchBean", "getUid", "User: " + context.getUsername(), null);
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally{
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::getUid] END");
		}
		return result;
	}

	public int getTotalResults(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, InvalidCredentialsException, RemoteException, EcmEngineTransactionException {

		logger.debug("[EcmEngineSearchBean::getTotalResults] BEGIN");

		validate(ValidationType.XPATH, "xpath", xpath);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		int totalResults = 0;

		ResultSet resultSet = null;

		final String logCtx = "XPATH: " + xpath.getXPathQuery() + " U: " + context.getUsername();


		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineSearchBean", "getTotalResults", logCtx, "Autenticazione completata.");

			String query = xpath.getXPathQuery();

			logger.debug("[EcmEngineSearchBean::getTotalResults] Query prima del controllo - " +
					" Q: " + query);

			//TODO:
			// Ho inserito questo controllo per effettuare la ricerca di tutti i nodi
			// figli del nodo dato in input.
			// ad esempio la query di input /app:company_home/cm:Test viene trasformata
			// in /app:company_home/cm:Test/* ; in questo modo la ricerca xpath ritorna
			// tutti i nodi figli del folder cm:Test

			if (!query.endsWith("/*")) {
				query = (query.endsWith("/")) ? query.concat("*") : query.concat("/*");
			}

			logger.debug("[EcmEngineSearchBean::getTotalResults] Query dopo il controllo - " +
					" Q: " + query);

			final String escapedQuery;
			try {
				escapedQuery = QueryBuilder.escapeXPathQuery(xpath.getXPathQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + xpath.getXPathQuery();

				logger.warn("[EcmEngineSearchBean::getTotalResults] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

            if(logger.isDebugEnabled()) {
		    	logger.debug("[EcmEngineSearchBean::getTotalResults] Query dopo l' escape - Q: " + escapedQuery);
            }



			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy(LimitBy.UNLIMITED);
			searchParams.setLimit(0);
			searchParams.setLanguage(SearchSvc.LANGUAGE_XPATH);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);

			totalResults = (resultSet != null) ? resultSet.length() :  0;

			dumpElapsed("EcmEngineSearchBean", "getTotalResults", logCtx, "Ricerca completata - " +
					totalResults + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "getTotalResults", context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::getTotalResults] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineSearchBean::getTotalResults] END");
		}
		return totalResults;
	}

	public OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException,
	PermissionDeniedException, SearchException, NoDataExtractedException,
	EcmEngineTransactionException, RemoteException {
		logger.debug("[EcmEngineSearchBean::ricercaAudit] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "R: " + context.getRepository() + " - U: " + context.getUsername();

		// avvia stopwatch
		start();

		OperazioneAudit[] listaAudit = null;
		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineSearchBean", "ricercaAudit", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono ricercare audit
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineSearchBean::ricercaAudit] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

			// Effettua la ricerca
			listaAudit = auditService.ricercaAudit(parametriRicerca);
			logger.debug("trovati "+(listaAudit != null ? listaAudit.length : "null" ));

			if (listaAudit != null && listaAudit.length > 0) {
				dumpElapsed("EcmEngineSearchBean", "ricercaAudit", logCtx, "Ricerca audit completata -" +
						" " + listaAudit.length + " risultati.");

				logger.debug("[EcmEngineSearchBean::ricercaAudit] Ricerca audit completata -" +
						" " + listaAudit.length + " risultati.");
			} else {
				logger.debug("[EcmEngineSearchBean::ricercaAudit] Ricerca audit completata - nessun risultato");
				throw new NoDataExtractedException("no audit records found");
			}

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineSearchBean", "ricercaAudit", context.getUsername(), null);

			logger.error("[EcmEngineSearchBean::ricercaAudit] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			// arresta stopwatch
			stop();
			// log della misurazione effettuata
			dumpElapsed("EcmEngineSearchBean", "ricercaAudit", logCtx, "Ricerca audit terminata");
			logger.debug("[EcmEngineSearchBean::ricercaAudit] END");
		}

		return listaAudit;
	}

	private QueryBuilderParams convertToQueryBuilderParams(SearchParams inParams, DictionarySvc dictionarySvc)
	throws DictionaryRuntimeException {
		logger.debug("[EcmEngineSearchBean::convertToQueryBuilderParams] BEGIN");
		QueryBuilderParams outParams = new QueryBuilderParams();

		try {
			final QName typeQName = (inParams.getTypePrefixedName() != null)
			? dictionarySvc.resolvePrefixNameToQName(inParams.getTypePrefixedName())
					: null;
			final Property [] properties = inParams.getProperties();

			outParams.setContentType((typeQName != null) ? typeQName.toString() : null);
			outParams.setMimeType(inParams.getMimeType());
			outParams.setFullTextAllWords(inParams.isFullTextAllWords());
			outParams.setFullTextQuery(inParams.getFullTextQuery());

			for (int i = 0; properties != null && i < properties.length; i++) {

				// TODO: vedere come gestire ricerca su property a valori multipli
				// e property di tipo diverso da stringhe (es: mltext, datetime, int, long)...

				outParams.addAttribute(properties[i].getPrefixedName(), properties[i].getValue());
			}

		} finally {
			logger.debug("[EcmEngineSearchBean::convertToQueryBuilderParams] END");
		}

		return outParams;
	}

	private ResultAssociation[] translateCategoryChildAssoc(Collection<ChildAssociationRef> categories) throws DictionaryRuntimeException{

		logger.debug("[EcmEngineSearchBean::translateCategoryChildAssoc] BEGIN");
		ResultAssociation[] associations = null;
		try{
			if (categories.isEmpty()) {

				logger.debug("[EcmEngineSearchBean::translateCategoryChildAssoc] Trovati 0 risultati.");
				// Non ci sono categorie
				return null;
			}

			associations = new ResultAssociation[categories.size()];

			int j = 0;

			for (ChildAssociationRef childAssocRef : categories) {

				associations[j] = new ResultAssociation();
				associations[j].setChildAssociation(true);
				associations[j].setPrefixedName(
						dictionaryService.resolveQNameToPrefixName(childAssocRef.getQName()));
				associations[j].setTypePrefixedName(
						dictionaryService.resolveQNameToPrefixName(childAssocRef.getTypeQName()));
				associations[j].setTargetUid(childAssocRef.getChildRef().getId());
				j++;
			}
		}finally{
			logger.debug("[EcmEngineSearchBean::translateCategoryChildAssoc] END");
		}
		return associations;
	}


	private ResultContent [] translateResultSet(ResultSet resultSet, int pageSize, int pageIndex, SortField[] sortFields, boolean fullProperty,Property[] properties)
	throws DictionaryRuntimeException, NodeRuntimeException, InvalidParameterException {
		final Vector<ResultContent> results = new Vector<ResultContent>();

		logger.debug("[EcmEngineSearchBean::translateResultSet] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
			    logger.debug("[EcmEngineSearchBean::translateResultSet] Translating " +resultSet.length() + " results.");
			}

			// La paginazione viene implementata estraendo il sottinsieme
			// di dati interessati alla ricerca.
			int startIndex = 0;
			int endIndex = resultSet.length();
			if (pageSize > 0 && pageIndex >= 0) {
				startIndex = pageSize * pageIndex;
				int upperLimit = pageSize * (pageIndex+1);
				if (upperLimit < resultSet.length()) {
					endIndex = upperLimit;
				}
			}

			// Ordinamento risultati
			List<ResultSetRow> rows = new Vector<ResultSetRow>();
			if (sortFields != null && sortFields.length > 0) {
    			List<ResultSetRow> rows2 = new Vector<ResultSetRow>();
                for (int i = 0; i < resultSet.length(); i++) {
                    rows2.add(resultSet.getRow(i));
                }
				logger.info("[EcmEngineSearchBean::translateResultSet] Sorting results.");

				// Viene utilizzato l'algoritmo di in-place mergesort ( stabile, tempo: O(n*log(n)), memoria: O(1) )
				for (int i = sortFields.length - 1; i >= 0; i--) {
					if (sortFields[i] != null) {
						logger.info("[EcmEngineSearchBean::translateResultSet] Sorting by " +
								sortFields[i].getFieldName() + (sortFields[i].isAscending() ? " ASC" : " DESC"));
						if (sortFields[i].getFieldName() == null || sortFields[i].getFieldName().length() == 0) {
							throw new InvalidParameterException("Invalid sort field: "+sortFields[i].getFieldName());
						}
						QName sortFieldQName = dictionaryService.resolvePrefixNameToQName(sortFields[i].getFieldName());
						PropertyDefinition propertyDef = dictionaryService.getProperty(sortFieldQName);
						if (propertyDef == null) {
							throw new InvalidParameterException("Invalid sort field: " +sortFields[i].getFieldName());
						}
						Sort sorter = new MergeSort(rows2, sortFieldQName,
								sortFields[i].isAscending(), Sort.SORT_CASEINSENSITIVE, nodeService);
						sorter.sort();
						logger.info("[EcmEngineSearchBean::translateResultSet] Sorted by " + sortFields[i].getFieldName());
					} else {
						logger.info("[EcmEngineSearchBean::translateResultSet] Found null sort field: ignoring...");
					}
				}
                for (int i = startIndex; i < endIndex; i++) {
                    rows.add(rows2.get(i));
                }
			} else {
                for (int i = startIndex; i < endIndex; i++) {
                    rows.add(resultSet.getRow(i));
                }
            }

			for (int i = 0; i < rows.size(); i++) {
				final ResultContent result = new ResultContent();
				final NodeRef node = rows.get(i).getNodeRef();
				final QName typeQName = nodeService.getType(node);
				ChildAssociationRef parentRef = null;

				try {
					parentRef = nodeService.getPrimaryParent(node);

					result.setPrefixedName((parentRef.getQName() != null)
							? dictionaryService.resolveQNameToPrefixName(parentRef.getQName())
									: null);

					final TypeDefinition typeDef = dictionaryService.getType(typeQName);
					final QName modelQName = typeDef.getModel().getName();
					result.setModelPrefixedName(dictionaryService.resolveQNameToPrefixName(modelQName));

				} catch (EcmEngineFoundationException e) {
					if (e.getCode().equals(FoundationErrorCodes.ACCESS_DENIED_ERROR)) {
						logger.debug("[EcmEngineSearchBean::translateResultSet] " +
								"Access denied reading parent of node: " + node);
					} else {
						logger.warn("[EcmEngineSearchBean::translateResultSet] " +
								"Unexpected error reading parent of node: " + node + " [" + e.getCode() + "]");
					}

					// XXX: nome fittizio!
					result.setPrefixedName("sys:unreadable-parent");
				}

				final Map<QName, Serializable> props = nodeService.getProperties(node);
				result.setUid((String) props.get(ContentModel.PROP_NODE_UUID));

				final Set<QName> aspects = nodeService.getAspects(node);

				// Process encryption information if needed
                // MB: 12:13:47 venerdi' 18 settembre 2009
                // Usando hasAspect viene rifatta la getAspects. Usando inceve il Set precaricato, possiamo
                //      accelerare le funzioni di lettura
                // final boolean encrypted = nodeService.hasAspect(node, EcmEngineModelConstants.ASPECT_ENCRYPTED);
				final boolean encrypted = aspects.contains(EcmEngineModelConstants.ASPECT_ENCRYPTED);
				if (encrypted) {
					EncryptionInfo encInfo = new EncryptionInfo();

					encInfo.setKeyId((String) props.get(EcmEngineModelConstants.PROP_ENCRYPTION_KEY_ID));
					encInfo.setSourceIV((String) props.get(EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));
					encInfo.setSourceEncrypted(((Boolean) props.get(EcmEngineModelConstants.PROP_ENCRYPTED_FROM_SOURCE)).booleanValue());

					final String rawTransformationString = (String) props.get(EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

					try {
						CryptoTransformationSpec transform = CryptoTransformationSpec.buildTransformationSpec(
								rawTransformationString);

						encInfo.setAlgorithm(transform.getAlgorithm());
						encInfo.setMode(transform.getMode());
						encInfo.setPadding(transform.getPadding());

						encInfo.setCorruptedEncryptionInfo(false);

					} catch (IllegalArgumentException e) {

						// Fallback per cercare di recuperare almeno informazioni parziali
						String [] parts = rawTransformationString.split("/");
						if (parts != null && parts.length >= 1) {
							encInfo.setAlgorithm(parts[0]);

							if (parts.length >= 2) {
								encInfo.setMode(parts[1]);

								if (parts.length == 3) {
									encInfo.setPadding(parts[2]);
								}
							}
						}

						encInfo.setCorruptedEncryptionInfo(true);
					}

					result.setEncryptionInfo(encInfo);
				}


				for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
					if (entry.getValue() instanceof ContentData) {
						final ContentData data = (ContentData) entry.getValue();
						result.setContentPropertyPrefixedName(dictionaryService.resolveQNameToPrefixName(entry.getKey()));
						result.setEncoding(data.getEncoding());
						result.setMimeType(data.getMimetype());
						result.setSize(data.getSize());
					}
				}

				result.setTypePrefixedName(dictionaryService.resolveQNameToPrefixName(typeQName));
				result.setParentAssocTypePrefixedName(
						(parentRef != null) ?
								dictionaryService.resolveQNameToPrefixName(parentRef.getTypeQName()) :
									null
				);

                // Di default sono restituiti solo le properties del content model di default
				final ResultProperty [] resultProps;
                if( fullProperty ) {
				    resultProps = translatePropertyMap(props,properties);
                } else {
				    resultProps = translateReducedPropertyMap(props);
                }
				result.setProperties(resultProps);

//				// TODO: gestire numero massimo da parametri di ricerca.
//				ResultAssociation[] associations = translateAssociations(node, ECMENGINE_ASSOC_TYPE_CHILD, MAX_CHILDREN);
//				result.setAssociations(associations);

				// Gestione aspect associati al risultato
				if (!aspects.isEmpty()) {
					ResultAspect [] resultAspects = new ResultAspect[aspects.size()];

				    int j = 0;
					for (QName aspect : aspects) {
						resultAspects[j] = new ResultAspect();
						resultAspects[j].setPrefixedName(dictionaryService.resolveQNameToPrefixName(aspect));
						j++;
					}
					result.setAspects(resultAspects);
				}
				results.add(result);
			}
		} finally {
			logger.debug("[EcmEngineSearchBean::translateResultSet] END");
		}
		return results.toArray(new ResultContent [] {});
	}

	public Path getAbsolutePath(Node node, OperationContext context)throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException,	RemoteException{
		Path result=null;
		try{
            // MB: TODO: test sui tenant + test su un content con lo storageID diverso

			result=new Path();
			NodeRef nodeRef=checkNodeExists(node, null);
			String path=nodeService.getProperty(nodeRef, dictionaryService.resolvePrefixNameToQName("cm:content")).toString();
			path=path.split("\\|")[0];
			path=path.substring(path.indexOf("//")+1);

            // MB: Root DIR
            Repository repository = RepositoryManager.getInstance().getRepository( RepositoryManager.getCurrentRepository() );
            String rootDir 	  	  = repository.getContentRootLocation();

            // MB: prende il tenant corrente
            String tenantName = "";
            if (context.getUsername().indexOf("@")>0) {
                tenantName = context.getUsername().substring(context.getUsername().indexOf("@")+1);
            }

            // MB: estraggo la lista dei contentStore
            List<ContentStoreDefinition> contentStores = null;
            // MB: se sono su un tenant, prendo da li la lista dei contestStores, poi sovrascrivo la rootDir
            if( tenantName.length()>0 ) {
                Tenant tenant = tenantAdminService.getTenant( tenantName );
                if( tenant!=null && tenant.getContentStores()!=null ){
                    contentStores = tenant.getContentStores();
                }
                if (tenant != null){
                    if (tenant.getRootContentStoreDir() != null && !tenant.getRootContentStoreDir().equals("")){
                       rootDir = tenant.getRootContentStoreDir();
                    }
                }
            } else {
                // MB: Se tenant vuoto, prendo la lista dal repository
                if( repository!=null && repository.getContentStores()!=null ){
                    contentStores = repository.getContentStores();
                }
            }

            // MB: ora ho la lista dei contentStore e so il rootDir
            // Vedo se devo usare rootDir, o passare sui contentStore, perche' ho un ASPECT_STORAGE

            // Vedo se c'e' l'aspect Storage
            final boolean storage = this.nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_STORAGE);
            if( storage ){
       		   Map<QName, Serializable> props = this.nodeService.getProperties(nodeRef);
        	   String storageId = (String) props.get(EcmEngineModelConstants.PROP_STORAGE_ID);
               if( storageId!=null && storageId.length()>0 ){
                   ContentStoreDefinition csd = null;
                   // MB: Verifica se il protocol e' gestito
                   if( contentStores!=null ) {
                       for( ContentStoreDefinition tcs : contentStores ) {
                           // Dal protocol assegnato al Tenant, accedo al bean del repository che gestisce quel protocol
                           if( tcs.getProtocol().equals( storageId ) ){
                               csd = tcs;
                           }
                       }
                   }

                   // Se ho un contentStore associato, ne prendo il resource
                   if( csd!=null ){
                      rootDir = csd.getResource();
                   }
               }
			}

			//path="D:/ecmengine/content/contentstore-primary/"+path;
            path = rootDir +path;
			//System.out.println(path);
			result.setPath(path);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("getAbsolutePath Exception: "+e);
		}

		return result;
	}
}

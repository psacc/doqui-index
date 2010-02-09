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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.CannedQueryDef;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.repo.search.impl.lucene.LuceneAnalyser;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.alfresco.repo.search.impl.lucene.LuceneIndexException;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.LuceneResultSet;
import org.alfresco.repo.search.impl.lucene.LuceneXPathHandler;
import org.alfresco.repo.search.impl.lucene.ParseException;
import org.alfresco.repo.search.impl.lucene.QueryParameterisationException;
import org.alfresco.repo.search.impl.lucene.QueryParser.Operator;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.saxpath.SAXPathException;

import com.werken.saxpath.XPathReader;

public class RepositoryAwareADMLuceneSearcherImpl extends RepositoryAwareLuceneBase implements RepositoryAwareLuceneSearcher {

	private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

    /** Default field name. */
    private static final String DEFAULT_FIELD = "TEXT";

    /** The namespace service. */
    private NamespacePrefixResolver namespacePrefixResolver;

    /** The node service. */
    private NodeService nodeService;

    private TenantService tenantService;

    private QueryRegisterComponent queryRegister;

    /** The associated indexer. */
    private LuceneIndexer indexer;

    /**
     * Get an initialized searcher for the store and transaction.
     *
     * Normally we do not search against a a store and delta. Currently only gets the searcher against the main index.
     *
     * @param storeRef
     * @param indexer
     * @param config
     * @return - the searcher implementation
     */
    public static RepositoryAwareADMLuceneSearcherImpl getSearcher(StoreRef storeRef, LuceneIndexer indexer,
    		String repository, LuceneConfig config) {

    	logger.debug("[RepositoryAwareADMLuceneSearcherImpl::getSearcher] " +
    			"Repository '" + repository + "' -- " +
    			"Building new searcher for store: " + storeRef + " [Indexer: " + indexer + "]");

        RepositoryAwareADMLuceneSearcherImpl searcher = new RepositoryAwareADMLuceneSearcherImpl();
        searcher.setLuceneConfig(config);

        try {
            searcher.initialise(storeRef,
            		(indexer == null) ? null : indexer.getDeltaId(),
            		repository);
            searcher.indexer = indexer;
        } catch (LuceneIndexException e) {
            throw new SearcherException(e);
        }
        return searcher;
    }

    /**
     * Get an initialized searcher for the store. No transactional amendments are searched.
     *
     * @param storeRef
     * @param config
     * @return the searcher
     */
    public static RepositoryAwareADMLuceneSearcherImpl getSearcher(StoreRef storeRef, String repository, LuceneConfig config) {
    	logger.debug("[RepositoryAwareADMLuceneSearcherImpl::getSearcher] " +
    			"Repository '" + repository + "' -- " +
    			"Getting new searcher for store: " + storeRef + " [Indexer: null]");

        return getSearcher(storeRef, null, repository, config);
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public boolean indexExists() {
        // return mainIndexExists();
        return true;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set the query register
     * @param queryRegister
     */
    public void setQueryRegister(QueryRegisterComponent queryRegister) {
        this.queryRegister = queryRegister;
    }

    public ResultSet query(StoreRef store, String language, String queryString, Path[] queryOptions,
            QueryParameterDefinition[] queryParameterDefinitions) throws SearcherException {
        store = tenantService.getName(store);

        SearchParameters sp = new SearchParameters();
        sp.addStore(store);
        sp.setLanguage(language);
        sp.setQuery(queryString);

        if (queryOptions != null) {
            for (Path path : queryOptions) {
                sp.addAttrbutePath(path);
            }
        }

        if (queryParameterDefinitions != null) {
            for (QueryParameterDefinition qpd : queryParameterDefinitions) {
                sp.addQueryParameterDefinition(qpd);
            }
        }
        sp.excludeDataInTheCurrentTransaction(true);

        return query(sp);
    }

    public ResultSet query(SearchParameters searchParameters) {

//    	final Query filter;

        if (searchParameters.getStores().size() != 1) {
            throw new IllegalStateException("Only one store can be searched at present");
        }

        ArrayList<StoreRef> stores = searchParameters.getStores();
        stores.set(0, tenantService.getName(searchParameters.getStores().get(0)));

        final StoreRef queryStore = stores.get(0);

        if(logger.isDebugEnabled()){
            logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- Executing query on store: " +queryStore);
        }

        String parameterisedQueryString;
        if (!searchParameters.getQueryParameterDefinitions().isEmpty()) {
            Map<QName, QueryParameterDefinition> map = new HashMap<QName, QueryParameterDefinition>();

            for (QueryParameterDefinition qpd : searchParameters.getQueryParameterDefinitions()) {
                map.put(qpd.getQName(), qpd);
            }

            parameterisedQueryString = parameterise(searchParameters.getQuery(), map, null, namespacePrefixResolver);
        } else {
            parameterisedQueryString = searchParameters.getQuery();
        }

        if (searchParameters.getLanguage().equalsIgnoreCase(SearchService.LANGUAGE_LUCENE)) {
            try {

                final Operator defaultOperator = (searchParameters.getDefaultOperator() == SearchParameters.AND)
                		? LuceneQueryParser.AND_OPERATOR
                		: LuceneQueryParser.OR_OPERATOR;

//                Query userQuery = LuceneQueryParser.parse(parameterisedQueryString, DEFAULT_FIELD,
//                		new LuceneAnalyser(getDictionaryService(),
//                				(searchParameters.getMlAnalaysisMode() == null)
//                        				? getLuceneConfig().getDefaultMLSearchAnalysisMode()
//                        				: searchParameters.getMlAnalaysisMode()),
//                        namespacePrefixResolver,
//                        getDictionaryService(),
//                        defaultOperator,
//                        searchParameters,
//                        getLuceneConfig());

//                // Aggiunta filtraggio per permessi di lettura
//                BooleanQuery query = new BooleanQuery();
//                query.add(userQuery, BooleanClause.Occur.MUST);

//				if (filter != null) {
//					query.add(filter, BooleanClause.Occur.MUST);
//				}

                if(logger.isDebugEnabled()){
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- Retrieving searcher for Indexer: " +indexer);
                }

                RepositoryAwareClosingIndexSearcher searcher = getSearcher(indexer);

                Query query = LuceneQueryParser.parse(parameterisedQueryString, DEFAULT_FIELD, new LuceneAnalyser(getDictionaryService(),
                        searchParameters.getMlAnalaysisMode() == null ? getLuceneConfig().getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode()),
                        namespacePrefixResolver, getDictionaryService(), tenantService, defaultOperator, searchParameters, getLuceneConfig(), searcher.getIndexReader());

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Query is '" + query.toString() + "'");
                }

                if (searcher == null) {
                    // no index return an empty result set
                    return new EmptyResultSet();
                }

                Hits hits;

                if (!searchParameters.getSortDefinitions().isEmpty()) {
                    int index = 0;
                    SortField[] fields = new SortField[searchParameters.getSortDefinitions().size()];
                    for (SearchParameters.SortDefinition sd : searchParameters.getSortDefinitions()) {
                        switch (sd.getSortType()) {
                        case FIELD:
                            String field = sd.getField();
                            if (field.startsWith("@"))
                            {
                                field = expandAttributeFieldName(field);
                                PropertyDefinition propertyDef = getDictionaryService().getProperty(QName.createQName(field.substring(1)));

                                if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
                                {
                                    DataTypeDefinition dataType = propertyDef.getDataType();
                                    String analyserClassName = dataType.getAnalyserClassName();
                                    if (analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName()))
                                    {
                                        field = field + ".sort";
                                    }
                                }

                            }
                            if (fieldHasTerm(searcher.getReader(), field)) {
                                fields[index++] = new SortField(field, !sd.isAscending());
                            } else {
                                fields[index++] = new SortField(null, SortField.DOC, !sd.isAscending());
                            }
                            break;
                        case DOCUMENT:
                            fields[index++] = new SortField(null, SortField.DOC, !sd.isAscending());
                            break;
                        case SCORE:
                            fields[index++] = new SortField(null, SortField.SCORE, !sd.isAscending());
                            break;
                        }
                    }
                    hits = searcher.search(query, new Sort(fields));
                } else {
                    hits = searcher.search(query);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- Lucene query executed: " +query.toString());
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- Lucene Results count: " +hits.length());
                }

                Path[] paths = searchParameters.getAttributePaths().toArray(new Path[0]);
                LuceneResultSet rs = new LuceneResultSet(hits, searcher, nodeService, tenantService, paths, searchParameters);

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- Lucene Results created");
                }

                return rs;

            } catch (ParseException e) {
                throw new SearcherException("Failed to parse query: " + parameterisedQueryString, e);
            } catch (IOException e) {
                throw new SearcherException("IO exception during search", e);
            }
        } else if (searchParameters.getLanguage().equalsIgnoreCase(SearchService.LANGUAGE_XPATH)) {
            try {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(getDictionaryService());
                // TODO: Handler should have the query parameters to use in
                // building its lucene query
                // At the moment xpath style parameters in the PATH
                // expression are not supported.
                reader.setXPathHandler(handler);
                reader.parse(parameterisedQueryString);
                Query userQuery = handler.getQuery();

                // Aggiunta filtraggio per permessi di lettura
                BooleanQuery query = new BooleanQuery();
                query.add(userQuery, BooleanClause.Occur.MUST);

//				if (filter != null) {
//					query.add(filter, BooleanClause.Occur.MUST);
//				}

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- Retrieving searcher for Indexer: " +indexer);
                }

//          	Searcher searcher = getSearcher(null);
            	Searcher searcher = getSearcher(indexer);

                if (searcher == null) {
                    // no index return an empty result set
                    return new EmptyResultSet();
                }

                Hits hits = searcher.search(query);

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- XPath query executed: " +query.toString());
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- XPath Results count: " +hits.length());
                }

                Path[] paths = searchParameters.getAttributePaths().toArray(new Path[0]);
                LuceneResultSet rs = new LuceneResultSet(hits, searcher, nodeService, tenantService, paths, searchParameters);

                if (logger.isDebugEnabled()) {
                    logger.debug("[RepositoryAwareADMLuceneSearcherImpl::query] Repository '" +getRepository() +"' -- XPath Results created");
                }

                return rs;
                //return new LuceneResultSet(hits, searcher, nodeService, tenantService, searchParameters.getAttributePaths().toArray(new Path[0]), searchParameters);
            } catch (SAXPathException e) {
                throw new SearcherException("Failed to parse query: " + searchParameters.getQuery(), e);
            } catch (IOException e) {
                throw new SearcherException("IO exception during search", e);
            }
        } else {
            throw new SearcherException("Unknown query language: " + searchParameters.getLanguage());
        }
    }

    private static boolean fieldHasTerm(IndexReader indexReader, String field)
    {
        try
        {
            TermEnum termEnum = indexReader.terms(new Term(field, ""));
            try
            {
                if (termEnum.next())
                {
                    Term first = termEnum.term();
                    return first.field().equals(field);
                }
                else
                {
                    return false;
                }
            }
            finally
            {
                termEnum.close();
            }
        }
        catch (IOException e)
        {
            throw new SearcherException("Could not find terms for sort field ", e);
        }

    }

    public ResultSet query(StoreRef store, String language, String query)
    {
        return query(store, language, query, null, null);
    }

    public ResultSet query(StoreRef store, String language, String query,
            QueryParameterDefinition[] queryParameterDefintions)
    {
        return query(store, language, query, null, queryParameterDefintions);
    }

    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths)
    {
        return query(store, language, query, attributePaths, null);
    }

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        CannedQueryDef definition = queryRegister.getQueryDefinition(queryId);

        // Do parameter replacement
        // As lucene phrases are tokensied it is correct to just do straight
        // string replacement.
        // The string will be formatted by the tokeniser.
        //
        // For non phrase queries this is incorrect but string replacement is
        // probably the best we can do.
        // As numbers and text are indexed specially, direct term queries only
        // make sense against textual data

        checkParameters(definition, queryParameters);

        String queryString = parameterise(definition.getQuery(), definition.getQueryParameterMap(), queryParameters,
                definition.getNamespacePrefixResolver());

        return query(store, definition.getLanguage(), queryString, null, null);
    }

    /**
     * The definitions must provide a default value, or of not there must be a parameter to provide the value
     *
     * @param definition
     * @param queryParameters
     * @throws QueryParameterisationException
     */
    private void checkParameters(CannedQueryDef definition, QueryParameter[] queryParameters)
            throws QueryParameterisationException
    {
        List<QName> missing = new ArrayList<QName>();

        Set<QName> parameterQNameSet = new HashSet<QName>();
        if (queryParameters != null)
        {
            for (QueryParameter parameter : queryParameters)
            {
                parameterQNameSet.add(parameter.getQName());
            }
        }

        for (QueryParameterDefinition parameterDefinition : definition.getQueryParameterDefs())
        {
            if (!parameterDefinition.hasDefaultValue())
            {
                if (!parameterQNameSet.contains(parameterDefinition.getQName()))
                {
                    missing.add(parameterDefinition.getQName());
                }
            }
        }

        if (missing.size() > 0)
        {
            StringBuilder buffer = new StringBuilder(128);
            buffer.append("The query is missing values for the following parameters: ");
            for (QName qName : missing)
            {
                buffer.append(qName);
                buffer.append(", ");
            }
            buffer.delete(buffer.length() - 1, buffer.length() - 1);
            buffer.delete(buffer.length() - 1, buffer.length() - 1);
            throw new QueryParameterisationException(buffer.toString());
        }
    }

    /*
     * Parameterise the query string - not sure if it is required to escape lucence spacials chars The parameters could be used to build the query - the contents of parameters
     * should alread have been escaped if required. ... mush better to provide the parameters and work out what to do TODO: conditional query escapement - may be we should have a
     * parameter type that is not escaped
     */
    private String parameterise(String unparameterised, Map<QName, QueryParameterDefinition> map,
            QueryParameter[] queryParameters, NamespacePrefixResolver nspr) throws QueryParameterisationException
    {

        Map<QName, List<Serializable>> valueMap = new HashMap<QName, List<Serializable>>();

        if (queryParameters != null)
        {
            for (QueryParameter parameter : queryParameters)
            {
                List<Serializable> list = valueMap.get(parameter.getQName());
                if (list == null)
                {
                    list = new ArrayList<Serializable>();
                    valueMap.put(parameter.getQName(), list);
                }
                list.add(parameter.getValue());
            }
        }

        Map<QName, ListIterator<Serializable>> iteratorMap = new HashMap<QName, ListIterator<Serializable>>();

        List<QName> missing = new ArrayList<QName>(1);
        StringBuilder buffer = new StringBuilder(unparameterised);
        int index = 0;
        while ((index = buffer.indexOf("${", index)) != -1)
        {
            int endIndex = buffer.indexOf("}", index);
            String qNameString = buffer.substring(index + 2, endIndex);
            QName key = QName.createQName(qNameString, nspr);
            QueryParameterDefinition parameterDefinition = map.get(key);
            if (parameterDefinition == null)
            {
                missing.add(key);
                buffer.replace(index, endIndex + 1, "");
            }
            else
            {
                ListIterator<Serializable> it = iteratorMap.get(key);
                if ((it == null) || (!it.hasNext()))
                {
                    List<Serializable> list = valueMap.get(key);
                    if ((list != null) && (list.size() > 0))
                    {
                        it = list.listIterator();
                    }
                    if (it != null)
                    {
                        iteratorMap.put(key, it);
                    }
                }
                String value;
                if (it == null)
                {
                    value = parameterDefinition.getDefault();
                }
                else
                {
                    value = DefaultTypeConverter.INSTANCE.convert(String.class, it.next());
                }
                buffer.replace(index, endIndex + 1, value);
            }
        }
        if (missing.size() > 0)
        {
            StringBuilder error = new StringBuilder();
            error.append("The query uses the following parameters which are not defined: ");
            for (QName qName : missing)
            {
                error.append(qName);
                error.append(", ");
            }
            error.delete(error.length() - 1, error.length() - 1);
            error.delete(error.length() - 1, error.length() - 1);
            throw new QueryParameterisationException(error.toString());
        }
        return buffer.toString();
    }

    /**
     * @see org.alfresco.repo.search.impl.NodeSearcher
     */
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, getDictionaryService(), this);

        contextNodeRef = tenantService.getName(contextNodeRef);

        return nodeSearcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    /**
     * @see org.alfresco.repo.search.impl.NodeSearcher
     */
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, getDictionaryService(), this);
        return nodeSearcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    /**
     * @return Returns true if the pattern is present, otherwise false.
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern)
    {
        return contains(nodeRef, propertyQName, googleLikePattern, SearchParameters.Operator.OR);
    }

    /**
     * @return Returns true if the pattern is present, otherwise false.
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern,
            SearchParameters.Operator defaultOperator)
    {
        ResultSet resultSet = null;
        try
        {
            // build Lucene search string specific to the node
            StringBuilder sb = new StringBuilder();
            sb.append("+ID:\"").append(nodeRef.toString()).append("\" +(TEXT:(")
                    .append(googleLikePattern.toLowerCase()).append(") ");
            if (propertyQName != null)
            {
                sb.append(" OR @").append(
                        LuceneQueryParser.escape(QName.createQName(propertyQName.getNamespaceURI(),
                                ISO9075.encode(propertyQName.getLocalName())).toString()));
                sb.append(":(").append(googleLikePattern.toLowerCase()).append(")");
            }
            else
            {
                for (QName key : nodeService.getProperties(nodeRef).keySet())
                {
                    sb.append(" OR @").append(
                            LuceneQueryParser.escape(QName.createQName(key.getNamespaceURI(),
                                    ISO9075.encode(key.getLocalName())).toString()));
                    sb.append(":(").append(googleLikePattern.toLowerCase()).append(")");
                }
            }
            sb.append(")");

            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(sb.toString());
            sp.setDefaultOperator(defaultOperator);
            sp.addStore(nodeRef.getStoreRef());

            resultSet = this.query(sp);
            boolean answer = resultSet.length() > 0;
            return answer;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    /**
     * @return Returns true if the pattern is present, otherwise false.
     */
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS)
    {
        if (propertyQName == null)
        {
            throw new IllegalArgumentException("Property QName is mandatory for the like expression");
        }

        StringBuilder sb = new StringBuilder(sqlLikePattern.length() * 3);

        if (includeFTS)
        {
            // convert the SQL-like pattern into a Lucene-compatible string
            String pattern = SearchLanguageConversion.convertXPathLikeToLucene(sqlLikePattern.toLowerCase());

            // build Lucene search string specific to the node
            sb = new StringBuilder();
            sb.append("+ID:\"").append(nodeRef.toString()).append("\" +(");
            // FTS or attribute matches
            if (includeFTS)
            {
                sb.append("TEXT:(").append(pattern).append(") ");
            }
            if (propertyQName != null)
            {
                sb.append(" @").append(
                        LuceneQueryParser.escape(QName.createQName(propertyQName.getNamespaceURI(),
                                ISO9075.encode(propertyQName.getLocalName())).toString())).append(":(").append(pattern)
                        .append(")");
            }
            sb.append(")");

            ResultSet resultSet = null;
            try
            {
                resultSet = this.query(nodeRef.getStoreRef(), "lucene", sb.toString());
                boolean answer = resultSet.length() > 0;
                return answer;
            }
            finally
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            }
        }
        else
        {
            // convert the SQL-like pattern into a Lucene-compatible string
            String pattern = SearchLanguageConversion.convertXPathLikeToRegex(sqlLikePattern.toLowerCase());

            Serializable property = nodeService.getProperty(nodeRef, propertyQName);
            if (property == null)
            {
                return false;
            }
            else
            {
                String propertyString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(
                        nodeRef, propertyQName));
                return propertyString.toLowerCase().matches(pattern);
            }
        }
    }

    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    private String expandAttributeFieldName(String field)
    {
        String fieldName = field;
        // Check for any prefixes and expand to the full uri
        if (field.charAt(1) != '{')
        {
            int colonPosition = field.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                fieldName = "@{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
            }
            else
            {
                // find the prefix
                fieldName = "@{" + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}" + field.substring(colonPosition + 1);
            }
        }
        return fieldName;
    }

    public List<Pair<String, Integer>> getTopTerms(String field, int count)
    {
        RepositoryAwareClosingIndexSearcher searcher = null;
        try
        {
            LinkedList<Pair<String, Integer>> answer = new LinkedList<Pair<String, Integer>>();
            searcher = getSearcher(indexer);
            IndexReader reader = searcher.getIndexReader();
            TermEnum terms = reader.terms(new Term(field, ""));
            do
            {
                Term term = terms.term();
                if (term != null)
                {
                    if(!term.field().equals(field))
                    {
                        break;
                    }
                    int freq = terms.docFreq();
                    Pair<String, Integer> pair = new Pair<String, Integer>(term.text(), Integer.valueOf(freq));
                    if (answer.size() < count)
                    {
                       if (answer.size() == 0)
                       {
                           answer.add(pair);
                       }
                       else if (answer.get(answer.size() - 1).getSecond().compareTo(pair.getSecond()) >= 0)
                       {
                           answer.add(pair);
                       }
                       else
                       {
                           for (ListIterator<Pair<String, Integer>> it = answer.listIterator(); it.hasNext(); /**/)
                           {
                               Pair<String, Integer> test = it.next();
                               if (test.getSecond().compareTo(pair.getSecond()) < 0)
                               {
                                   it.previous();
                                   it.add(pair);
                                   break;
                               }
                           }
                        }
                    }
                    else if (answer.get(count - 1).getSecond().compareTo(pair.getSecond()) < 0)
                    {
                        for (ListIterator<Pair<String, Integer>> it = answer.listIterator(); it.hasNext(); /**/)
                        {
                            Pair<String, Integer> test = it.next();
                            if (test.getSecond().compareTo(pair.getSecond()) < 0)
                            {
                                it.previous();
                                it.add(pair);
                                break;
                            }
                        }
                        answer.removeLast();
                    }
                    else
                    {
                        // off the end
                    }
                }
            } while (terms.next());
            terms.close();
            return answer;

        }
        catch (IOException e)
        {
            throw new SearcherException(e);
        }
        finally
        {
            if (searcher != null)
            {
                try
                {
                    searcher.close();
                }
                catch (IOException e)
                {
                    throw new SearcherException(e);
                }
            }
        }

    }


}

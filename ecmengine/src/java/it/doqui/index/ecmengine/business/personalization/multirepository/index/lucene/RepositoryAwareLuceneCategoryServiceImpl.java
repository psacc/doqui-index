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

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareIndexerAndSearcher;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepositoryAwareLuceneCategoryServiceImpl implements CategoryService {

	/** Logger. */
    private static Log logger = LogFactory.getLog(
    		EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LUCENE_LOG_CATEGORY);

    private NodeService nodeService;

    private NodeService publicNodeService;

    private TenantService tenantService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

    private RepositoryAwareIndexerAndSearcher indexerAndSearcher;

    public RepositoryAwareLuceneCategoryServiceImpl()
    {
        super();
    }

    // Inversion of control support

    /**
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the public node service
     *
     * @param publicNodeService
     */
    public void setPublicNodeService(NodeService publicNodeService)
    {
        this.publicNodeService = publicNodeService;
    }

    /**
     * Set the tenant service
     *
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the service to map prefixes to uris
     * @param namespacePrefixResolver
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * Set the dictionary service
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the indexer and searcher
     * @param indexerAndSearcher
     */
    public void setIndexerAndSearcher(RepositoryAwareIndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth)
    {
        if (categoryRef == null)
        {
            return Collections.<ChildAssociationRef> emptyList();
        }

        categoryRef = tenantService.getName(categoryRef);

        ResultSet resultSet = null;
        try
        {
            StringBuilder luceneQuery = new StringBuilder(64);

            switch(mode)
            {
            case ALL:
                luceneQuery.append("PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("*").append("\" ");
                break;
            case MEMBERS:
                luceneQuery.append("PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("member").append("\" ");
                break;
            case SUB_CATEGORIES:
                luceneQuery.append("+PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("*").append("\" ");
                luceneQuery.append("+TYPE:\"" + ContentModel.TYPE_CATEGORY.toString() + "\"");
                break;
            }

            final String repository = RepositoryManager.getCurrentRepository();
            final SearchService searcher = indexerAndSearcher.getSearcher(categoryRef.getStoreRef(), repository, false);

            resultSet = searcher.query(categoryRef.getStoreRef(), "lucene", luceneQuery.toString(), null, null);

            return resultSetToChildAssocCollection(resultSet);
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    private String buildXPath(Path path)
    {
        StringBuilder pathBuffer = new StringBuilder(64);
        for (Iterator<Path.Element> elit = path.iterator(); elit.hasNext(); /**/)
        {
            Path.Element element = elit.next();
            if (!(element instanceof Path.ChildAssocElement))
            {
                throw new IndexerException("Confused path: " + path);
            }
            Path.ChildAssocElement cae = (Path.ChildAssocElement) element;
            if (cae.getRef().getParentRef() != null)
            {
                pathBuffer.append("/");
                pathBuffer.append(getPrefix(cae.getRef().getQName().getNamespaceURI()));
                pathBuffer.append(ISO9075.encode(cae.getRef().getQName().getLocalName()));
            }
        }
        return pathBuffer.toString();
    }

    HashMap<String, String> prefixLookup = new HashMap<String, String>();

    private String getPrefix(String uri)
    {
        String prefix = prefixLookup.get(uri);
        if (prefix == null)
        {
            Collection<String> prefixes = namespacePrefixResolver.getPrefixes(uri);
            for (String first : prefixes)
            {
                prefix = first;
                break;
            }

            prefixLookup.put(uri, prefix);
        }
        if (prefix == null)
        {
            return "";
        }
        else
        {
            return prefix + ":";
        }

    }

    private Collection<ChildAssociationRef> resultSetToChildAssocCollection(ResultSet resultSet)
    {
        List<ChildAssociationRef> collection = new ArrayList<ChildAssociationRef>();
        if (resultSet != null)
        {
            for (ResultSetRow row : resultSet)
            {
                ChildAssociationRef car = nodeService.getPrimaryParent(row.getNodeRef());
                collection.add(car);
            }
        }
        return collection;
        // The caller closes the result set
    }

    public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth)
    {
        Collection<ChildAssociationRef> assocs = new ArrayList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectQName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, depth));
        }
        return assocs;
    }

    private Set<NodeRef> getClassificationNodes(StoreRef storeRef, QName qname)
    {
        storeRef = tenantService.getName(storeRef);

        ResultSet resultSet = null;
        try
        {
            final String repository = RepositoryManager.getCurrentRepository();
            final SearchService searcher = indexerAndSearcher.getSearcher(storeRef, repository, false);

            resultSet = searcher.query(storeRef, "lucene",
            		"PATH:\"/" + getPrefix(qname.getNamespaceURI()) + ISO9075.encode(qname.getLocalName()) + "\"",
                    null, null);

            Set<NodeRef> nodeRefs = new HashSet<NodeRef>(resultSet.length());
            for (ResultSetRow row : resultSet)
            {
                nodeRefs.add(row.getNodeRef());
            }

            return nodeRefs;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef)
    {
        storeRef = tenantService.getName(storeRef);

        ResultSet resultSet = null;
        try
        {
            final String repository = RepositoryManager.getCurrentRepository();
            final SearchService searcher = indexerAndSearcher.getSearcher(storeRef, repository, false);

            resultSet = searcher.query(storeRef, "lucene", "PATH:\"//cm:categoryRoot/*\"", null, null);
            return resultSetToChildAssocCollection(resultSet);
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    public Collection<QName> getClassificationAspects()
    {
        return dictionaryService.getSubAspects(ContentModel.ASPECT_CLASSIFIABLE, true);
    }

    public NodeRef createClassifiction(StoreRef storeRef, QName typeName, String attributeName)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName)
    {
        Collection<ChildAssociationRef> assocs = new ArrayList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE));
        }
        return assocs;
    }

    public NodeRef createCategory(NodeRef parent, String name)
    {
        if(!nodeService.exists(parent))
        {
            throw new AlfrescoRuntimeException("Missing category?");
        }
        String uri = nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();
        String validLocalName = QName.createValidLocalName(name);

        if(publicNodeService==null){
        	logger.error("[RepositoryAwareLuceneCategoryServiceImpl::createCategory] ATTENZIONE : publicNodeService NULL");
        }

        ChildAssociationRef childAssocRef = publicNodeService.createNode(parent, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(uri, validLocalName), ContentModel.TYPE_CATEGORY);

        if(childAssocRef==null){
        	logger.error("[RepositoryAwareLuceneCategoryServiceImpl::createCategory] ATTENZIONE : childAssocRef NULL");
        }

        NodeRef newCategory = childAssocRef.getChildRef();
        publicNodeService.setProperty(newCategory, ContentModel.PROP_NAME, name);
        return newCategory;
    }

    public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name)
    {
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        if(nodeRefs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Missing classification: "+aspectName);
        }
        NodeRef parent = nodeRefs.iterator().next();
        return createCategory(parent, name);
    }

    public void deleteCategory(NodeRef nodeRef)
    {
        publicNodeService.deleteNode(nodeRef);
    }

    public void deleteClassification(StoreRef storeRef, QName aspectName)
    {
        throw new UnsupportedOperationException();
    }

    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        if (indexerAndSearcher instanceof RepositoryAwareIndexerAndSearcher)
        {
            AspectDefinition definition = dictionaryService.getAspect(aspectName);
            if(definition == null)
            {
                throw new IllegalStateException("Unknown aspect");
            }
            QName catProperty = null;
            Map<QName, PropertyDefinition> properties = definition.getProperties();
            for(QName pName : properties.keySet())
            {
                if(pName.getNamespaceURI().equals(aspectName.getNamespaceURI()))
                {
                    if(pName.getLocalName().equalsIgnoreCase(aspectName.getLocalName()))
                    {
                        PropertyDefinition def = properties.get(pName);
                        if(def.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                        {
                            catProperty = pName;
                        }
                    }
                }
            }
            if(catProperty == null)
            {
                throw new IllegalStateException("Aspect does not have category property mirroring the aspect name");
            }


            RepositoryAwareIndexerAndSearcher lias = (RepositoryAwareIndexerAndSearcher) indexerAndSearcher;
            String field = "@" + catProperty;
            final String repository = RepositoryManager.getCurrentRepository();
            final SearchService searchService = lias.getSearcher(storeRef, repository, false);

            if (searchService instanceof LuceneSearcher)
            {
                LuceneSearcher luceneSearcher = (LuceneSearcher)searchService;
                List<Pair<String, Integer>> topTerms = luceneSearcher.getTopTerms(field, count);
                List<Pair<NodeRef, Integer>> answer = new ArrayList<Pair<NodeRef, Integer>>();
                for (Pair<String, Integer> term : topTerms)
                {
                    Pair<NodeRef, Integer> toAdd;

                    //LOBIANCO 9 marzo 2009
                    //workspace://SpacesStore/
                    if (logger.isDebugEnabled()) {
                        logger.debug("[RepositoryAwareLuceneCategoryServiceImpl::getTopCategories] First: " +term.getFirst());
                    }
                    NodeRef nodeRef = null;
                    //new NodeRef("workspace://SpacesStore/"

                    //Exception in method 'getTopCategories':
                    //Invalid store ref: Does not contain ://   spacesstore

                    if (logger.isDebugEnabled()) {
                        logger.debug("[RepositoryAwareLuceneCategoryServiceImpl::getTopCategories] STORE_REF Protocol: "+storeRef.getProtocol());
                        logger.debug("[RepositoryAwareLuceneCategoryServiceImpl::getTopCategories] STORE_REF Identifier: "+storeRef.getIdentifier());
                    }

                    if(!term.getFirst().contains("://")){
                    	nodeRef = new NodeRef(storeRef.getProtocol()+"://"+term.getFirst());
                    	//nodeRef = new NodeRef("workspace://"+term.getFirst());
                    }else{
                    	nodeRef = new NodeRef(term.getFirst());
                    }
                    //LOBIANCO

                    //NodeRef nodeRef = new NodeRef(term.getFirst());
                    if (nodeService.exists(nodeRef))
                    {
                        toAdd = new Pair<NodeRef, Integer>(nodeRef, term.getSecond());
                    }
                    else
                    {
                        toAdd = new Pair<NodeRef, Integer>(null, term.getSecond());
                    }
                    answer.add(toAdd);
                }
                return answer;
            }
            else
            {
                throw new UnsupportedOperationException("getPolularCategories is only supported for lucene indexes");
            }
        }
        else
        {
            throw new UnsupportedOperationException("getPolularCategories is only supported for lucene indexes");
        }
    }

}

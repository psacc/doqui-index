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

package it.doqui.index.ecmengine.business.personalization.splitting;

import it.doqui.index.ecmengine.business.personalization.splitting.util.SplittingNodeServiceConstants;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.node.AbstractNodeServiceImpl;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.InvalidChildAssociationRefException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;

public class SplittingDbNodeServiceImpl extends AbstractNodeServiceImpl implements SplittingNodeService {
    private static Log logger = LogFactory.getLog(SplittingNodeServiceConstants.ECMENGINE_SPLITTING_LOG_CATEGORY);
    private static Log loggerPaths = LogFactory.getLog(SplittingNodeServiceConstants.ECMENGINE_SPLITTING_LOG_CATEGORY + ".paths");

    private NodeDaoService nodeDaoService;
    private StoreArchiveMap storeArchiveMap;
    private NodeService avmNodeService;
    private TenantService tenantService;
    private int partsCount;

	public void setPartsCount(int count) {
    	this.partsCount = count;
    }

    public SplittingDbNodeServiceImpl() {
    	logger.debug("[SplittingDbNodeServiceImpl::constructor] BEGIN");
		this.storeArchiveMap = new StoreArchiveMap(); // in case it is not set
		logger.debug("[SplittingDbNodeServiceImpl::constructor] END");
    }

    public void setNodeDaoService(NodeDaoService nodeDaoService) {
        this.nodeDaoService = nodeDaoService;
    }

    public void setStoreArchiveMap(StoreArchiveMap storeArchiveMap) {
        this.storeArchiveMap = storeArchiveMap;
    }

    public void setAvmNodeService(NodeService avmNodeService) {
        this.avmNodeService = avmNodeService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    //private Indexer indexer;
    //public void setIndexer(Indexer indexer) {
        //this.indexer = indexer;
    //}

    //private NodeIndexer nodeIndexer;
    /**
     * @param nodeIndexer       the indexer that will be notified of node additions,
     *                          modifications and deletions
     */
    //public void setNodeIndexer(NodeIndexer nodeIndexer)
    //{
        //this.nodeIndexer = nodeIndexer;
    //}

    /**
     * Performs a null-safe get of the node
     *
     * @param nodeRef the node to retrieve
     * @return Returns the node entity (never null)
     * @throws InvalidNodeRefException if the referenced node could not be found
     */
    private Node getNodeNotNull(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getNodeNotNull] BEGIN");
    	Node unchecked = null;

    	try {
    		ParameterCheck.mandatory("nodeRef", nodeRef);

            nodeRef = tenantService.getName(nodeRef);
            unchecked = nodeDaoService.getNode(nodeRef);

    		if (unchecked == null) {
    			throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getNodeNotNull] END");
    	}
        return unchecked;
    }

    /**
     * Gets the node status for a live node.
     * @param nodeRef the node reference
     * @return Returns the node status, which will not be <tt>null</tt> and will have a live node attached.
     * @throws InvalidNodeRefException if the node is deleted or never existed
     */
    public NodeStatus getNodeStatusNotNull(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getNodeStatusNotNull] BEGIN");
    	NodeStatus nodeStatus = null;

    	try {
    		ParameterCheck.mandatory("nodeRef", nodeRef);

            nodeRef = tenantService.getName(nodeRef);
            nodeStatus = nodeDaoService.getNodeStatus(nodeRef, false);
    		if (nodeStatus == null || nodeStatus.getNode() == null) {
    			throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getNodeStatusNotNull] END");
    	}
    	return nodeStatus;
    }

    public boolean exists(StoreRef storeRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::exists] BEGIN");
    	try {
            storeRef = tenantService.getName(storeRef);
    		Store store = nodeDaoService.getStore(storeRef.getProtocol(), storeRef.getIdentifier());

    		return (store != null);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::exists] END");
    	}
    }

    public boolean exists(NodeRef nodeRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::exists] BEGIN");
    	try {
    		ParameterCheck.mandatory("nodeRef", nodeRef);

            nodeRef = tenantService.getName(nodeRef);
    		Node node = nodeDaoService.getNode(nodeRef);
    		boolean result = (node != null);

    		if (logger.isDebugEnabled()) {
    			logger.debug("[SplittingDbNodeServiceImpl::exists] Node \"" + nodeRef + "\" exists: " + result);
    		}
    		return result;
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::exists] END");
    	}
    }

    public Status getNodeStatus(NodeRef nodeRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::getNodeStatus] BEGIN");

    	try {
    		ParameterCheck.mandatory("nodeRef", nodeRef);

            nodeRef = tenantService.getName(nodeRef);
    		NodeStatus nodeStatus = nodeDaoService.getNodeStatus(nodeRef, false);
    		if (nodeStatus == null) {     // node never existed
    			if (logger.isDebugEnabled()) {
    				logger.debug("[SplittingDbNodeServiceImpl::getNodeStatus] Node without status: " + nodeRef);
    			}
    			return null;
    		} else {
    			return new NodeRef.Status(nodeStatus.getTransaction().getChangeTxnId(), nodeStatus.isDeleted());
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getNodeStatus] END");
    	}
    }

    /**
     * @see NodeDaoService#getStores()
     */
    public List<StoreRef> getStores() {
    	logger.debug("[SplittingDbNodeServiceImpl::getStores] BEGIN");
        final List<Store> stores = nodeDaoService.getStores();
        final List<StoreRef> avmStores = avmNodeService.getStores();
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(stores.size() + avmStores.size());

        for (Store store : stores) {
            StoreRef storeRef = store.getStoreRef();
            try {
            	if (tenantService.isEnabled()) {
                    tenantService.checkDomain(storeRef.getIdentifier());
                    storeRef = tenantService.getBaseName(storeRef);
                }
                storeRefs.add(storeRef);
            } catch (RuntimeException re) {
                // deliberately ignore - stores in different domain will not be listed
            }
        }
        storeRefs.addAll(avmStores);

        logger.debug("[SplittingDbNodeServiceImpl::getStores] END");
        return storeRefs;
    }

    /**
     * Defers to the typed service
     * @see StoreDaoService#createWorkspace(String)
     */
    public StoreRef createStore(String protocol, String identifier) {
    	logger.debug("[SplittingDbNodeServiceImpl::createStore] BEGIN");
        StoreRef storeRef = tenantService.getName(new StoreRef(protocol, identifier));
        identifier = storeRef.getIdentifier();

    	try {
    		// check that the store does not already exist
    		Store store = nodeDaoService.getStore(protocol, identifier);
    		if (store != null) {
    			throw new StoreExistsException("Unable to create a store that already exists: " + storeRef, storeRef);
    		}

    		// invoke policies
    		invokeBeforeCreateStore(ContentModel.TYPE_STOREROOT, storeRef);

    		// create a new one
    		store = nodeDaoService.createStore(protocol, identifier);

    		// get the root node
    		Node rootNode = store.getRootNode();

    		// assign the root aspect - this is expected of all roots, even store roots
    		addAspect(rootNode.getNodeRef(),
    				ContentModel.ASPECT_ROOT,
    				Collections.<QName, Serializable>emptyMap());

    		// invoke policies
    		invokeOnCreateStore(rootNode.getNodeRef());

    		// done
    		if (!store.getStoreRef().equals(storeRef)) {
    			throw new RuntimeException("Incorrect store reference");
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::createStore] END");
    	}
        storeRef = tenantService.getBaseName(storeRef);
    	return storeRef;
    }

    /**
     * @see NodeDaoService#deleteStore(String, String)
     */
    public void deleteStore(StoreRef storeRef)
    {
        storeRef = tenantService.getName(storeRef);

        String protocol = storeRef.getProtocol();
        String identifier = storeRef.getIdentifier();

        // check that the store does exist
        Store store = nodeDaoService.getStore(protocol, identifier);
        if (store == null)
        {
            throw new InvalidStoreRefException("Unable to delete a store that does not exist: " + storeRef, storeRef);
        }

        Node rootNode = store.getRootNode();

		QName nodeTypeQName = rootNode.getTypeQName();
		Set<QName> nodeAspectQNames = rootNode.getAspects();
        ChildAssociationRef assocRef = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNode.getNodeRef(), null, rootNode.getNodeRef());
        invokeOnDeleteNode(assocRef, nodeTypeQName, nodeAspectQNames, false);
        nodeDaoService.deleteStore(protocol, identifier);
        return;
    }

    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getRootNode] BEGIN");
    	Node node = null;
    	NodeRef nodeRef = null;

    	try {
            storeRef = tenantService.getName(storeRef);
    		Store store = nodeDaoService.getStore(storeRef.getProtocol(), storeRef.getIdentifier());
    		if (store == null) {
    			throw new InvalidStoreRefException("Store does not exist: " + storeRef, storeRef);
    		}

    		// get the root
    		node = store.getRootNode();
    		if (node == null) {
    			throw new InvalidStoreRefException("Store does not have a root node: " + storeRef, storeRef);
    		}
            nodeRef = tenantService.getBaseName(node.getNodeRef());
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getRootNode] END");
    	}
    	return nodeRef;
    }

    /**
     * @see #createNode(NodeRef, QName, QName, QName, Map)
     */
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName) {
        return createNode(parentRef, assocTypeQName, assocQName, nodeTypeQName, null);
    }

    /**
     * @see org.alfresco.service.cmr.repository.NodeService#createNode(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName,
    		QName nodeTypeQName, Map<QName, Serializable> properties) {
    	logger.debug("[SplittingDbNodeServiceImpl::createNode] BEGIN");

    	final long start = System.currentTimeMillis();
    	ChildAssociationRef childAssocRef = null;

    	Assert.notNull(parentRef);
    	Assert.notNull(assocTypeQName);
    	Assert.notNull(assocQName);

    	try {
            parentRef = tenantService.getName(parentRef);
    		// Se il metodo e` richiamato su un nodo parte -> bug
    		Node parentNode = getNodeNotNull(parentRef);
    		Assert.isTrue(!isPart(parentNode), "BUG: createNode() with part as parent");

    		// get the store that the parent belongs to
    		StoreRef storeRef = parentRef.getStoreRef();
    		Store store = nodeDaoService.getStore(storeRef.getProtocol(), storeRef.getIdentifier());
    		if (store == null) {
    			throw new RuntimeException("No store found for parent node: " + parentRef);
    		}

    		// null property map is allowed
    		properties = (properties == null)
    				? new HashMap<QName, Serializable>()
    				: new HashMap<QName, Serializable>(properties);

    		// Invoke policy behaviour
    		invokeBeforeCreateNode(parentRef, assocTypeQName, assocQName, nodeTypeQName);

    		// check the node type
    		TypeDefinition nodeTypeDef = dictionaryService.getType(nodeTypeQName);
    		if (nodeTypeDef == null) {
    			throw new InvalidTypeException(nodeTypeQName);
    		}

    		// BEGIN GESTIONE SPLITTING
    		Node destinationNode = null;

    		if (isSplitted(parentNode)) {

    			// Il nodo e` splittato in piu` parti
    			if (logger.isDebugEnabled()) {
    				logger.debug("[SplittingDbNodeServiceImpl::createNode] Detected splitted node: " +parentRef);
    			}
    			destinationNode = getPartByChildAssocName(parentNode, assocQName.getLocalName());

    			if (destinationNode == null) {

    				// La parte non e` stata trovata e deve essere creata
    				destinationNode = createMissingPart(parentNode, assocQName.getLocalName());
    			}
    		} else {
    			destinationNode = parentNode;
    		}

    		Assert.notNull(destinationNode, "BUG: destinationNode is null in createNode()!");
    		// END GESTIONE SPLITTING

    		// get/generate an ID for the node
    		final String newId = generateGuid(properties);

    		// create the node instance
    		Node childNode = nodeDaoService.newNode(store, newId, nodeTypeQName);
    		NodeRef childNodeRef = childNode.getNodeRef();

    		// We now have enough to declare the child association creation
    		invokeBeforeCreateChildAssociation(parentRef, childNodeRef, assocTypeQName,
    				assocQName, true);

    		// Create the association
    		ChildAssoc childAssoc = nodeDaoService.newChildAssoc(
    				destinationNode, childNode, true, assocTypeQName, assocQName);
    		ChildAssociationRef internalChildAssocRef = childAssoc.getChildAssocRef();
    		if (logger.isDebugEnabled()) {
    			logger.debug("[SplittingDbNodeServiceImpl::createNode] Internal ChildAssociationRef: " + internalChildAssocRef);
    		}

    		// Set the default property values
    		addDefaultPropertyValues(nodeTypeDef, properties);

    		// Add the default aspects to the node
    		addDefaultAspects(nodeTypeDef, childNode, properties);

    		// set the properties - it is a new node so only set properties if there are any
    		Map<QName, Serializable> propertiesBefore = getPropertiesImpl(childNode);
    		Map<QName, Serializable> propertiesAfter = null;
    		if (!properties.isEmpty()) {
    			propertiesAfter = setPropertiesImpl(childNode, properties);
    		}

    		setChildUniqueName(childNode);	// ensure uniqueness
    		childAssocRef = new ChildAssociationRef(assocTypeQName, parentRef, assocQName, childNodeRef,
    				true, 	// Nuovo nodo -> associazione primaria
    				childAssoc.getIndex()
    		);
    		if (logger.isDebugEnabled()) {
    			logger.debug("[SplittingDbNodeServiceImpl::createNode] External ChildAssociationRef: " + childAssocRef);
    		}

        	// update the node status
        	nodeDaoService.recordChangeId(childNodeRef);

    		// Invoke policy behavior
    		invokeOnCreateNode(childAssocRef);
    		invokeOnCreateChildAssociation(childAssocRef, true);
    		if (propertiesAfter != null) {
    			invokeOnUpdateProperties(childNodeRef, propertiesBefore, propertiesAfter);
    		}
    	} finally {
    		final long stop = System.currentTimeMillis();
    		logger.debug("[SplittingDbNodeServiceImpl::createNode] Elapsed: " + (stop - start) + " ms");
    		logger.debug("[SplittingDbNodeServiceImpl::createNode] END");
    	}
    	return childAssocRef;
    }

    /**
     * Add the default aspects to a given node
     *
     * @param nodeTypeDef
     */
    private void addDefaultAspects(ClassDefinition classDefinition, Node node,
    		Map<QName, Serializable> properties) {
    	logger.debug("[SplittingDbNodeServiceImpl::addDefaultAspects] BEGIN");
    	// Se il nodo e` una parte -> bug
    	Assert.isTrue(!isPart(node), "BUG: addDefaultAspects() on part");

    	try {
    		final boolean splitted = isSplitted(node);
    		NodeRef nodeRef = node.getNodeRef();

    		// get the mandatory aspects for the node type
    		List<AspectDefinition> defaultAspectDefs = classDefinition.getDefaultAspects();

    		// add all the aspects to the node
    		Set<QName> nodeAspects = node.getAspects();
    		for (AspectDefinition defaultAspectDef : defaultAspectDefs) {
                QName aspectTypeQName = defaultAspectDef.getName();
                invokeBeforeAddAspect(nodeRef, aspectTypeQName);
                nodeAspects.add(aspectTypeQName);
                addDefaultPropertyValues(defaultAspectDef, properties);
                invokeOnAddAspect(nodeRef, aspectTypeQName);

    			if (defaultAspectDef.getName().equals(EcmEngineModelConstants.ASPECT_SPLITTED)
    					&& !splitted) {
    				splitNode(node);	// per gestire lo splitting via mandatory aspect
    			}

    			invokeOnAddAspect(nodeRef, defaultAspectDef.getName());

    			// Now add any default aspects for this aspect
    			addDefaultAspects(defaultAspectDef, node, properties);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::addDefaultAspects] END");
    	}
    }

    /**
     * Drops the old primary association and creates a new one
     */
    public ChildAssociationRef moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef,
            QName assocTypeQName, QName assocQName) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::moveNode] BEGIN");
        Assert.notNull(nodeToMoveRef);
        Assert.notNull(newParentRef);
        Assert.notNull(assocTypeQName);
        Assert.notNull(assocQName);

        ChildAssociationRef newAssocRef = null;

        try {
        	// check the node references
        	Node nodeToMove = getNodeNotNull(nodeToMoveRef);
        	Node newParentNode = getNodeNotNull(newParentRef);

        	Assert.isTrue(!isPart(nodeToMove) && !isPart(newParentNode),
        			"BUG: moveNode() on one or more part node!");

        	// get the primary parent association
        	ChildAssoc oldAssoc = nodeDaoService.getPrimaryParentAssoc(nodeToMove);

        	final NodeRef oldParentRef = getPartsContainerNode(oldAssoc.getParent()).getNodeRef();

        	ChildAssociationRef oldAssocRef = new ChildAssociationRef(oldAssoc.getTypeQName(), oldParentRef,
        			oldAssoc.getQname(), nodeToMoveRef, oldAssoc.getIsPrimary(), oldAssoc.getIndex());


        	Node destinationNode = null;

        	if (isSplitted(newParentNode)) {
        		// BEGIN GESTIONE SPLITTING

        		Store store = nodeDaoService.getStore(newParentRef.getStoreRef().getProtocol(),
        				newParentRef.getStoreRef().getIdentifier());
        		if (store == null) {
        			throw new RuntimeException("No store found for parent node: " + newParentRef);
        		}

        		destinationNode = getPartByChildAssocName(newParentNode, assocQName.getLocalName());

        		if (destinationNode == null) {
        			destinationNode = createMissingPart(newParentNode, assocQName.getLocalName());
        		}

        		// END GESTIONE SPLITTING
        	} else {
        		destinationNode = newParentNode;
        	}

        	Assert.notNull(destinationNode, "BUG: destinationNode null in moveNode()!");

        	boolean movingStore = !nodeToMoveRef.getStoreRef().equals(newParentRef.getStoreRef());

        	// data needed for policy invocation
        	QName nodeToMoveTypeQName = nodeToMove.getTypeQName();
        	Set<QName> nodeToMoveAspects = nodeToMove.getAspects();

        	// Invoke policy behavior
        	if (movingStore) {
        		invokeBeforeDeleteNode(nodeToMoveRef);
        		invokeBeforeCreateNode(newParentRef, assocTypeQName, assocQName, nodeToMoveTypeQName);
        	} else {
        		invokeBeforeDeleteChildAssociation(oldAssocRef);
        		invokeBeforeCreateChildAssociation(newParentRef, nodeToMoveRef, assocTypeQName, assocQName, false);
        	}

        	// remove the child assoc from the old parent
        	// don't cascade as we will still need the node afterwards
        	nodeDaoService.deleteChildAssoc(oldAssoc, false);

        	// create a new assoc
        	ChildAssoc newAssoc = nodeDaoService.newChildAssoc(destinationNode,	// Nodo o parte
        			nodeToMove, true, assocTypeQName, assocQName);

        	setChildUniqueName(nodeToMove);	// ensure uniqueness

        	newAssocRef = new ChildAssociationRef(newAssoc.getTypeQName(), newParentRef,
        			newAssoc.getQname(), nodeToMoveRef, newAssoc.getIsPrimary(), newAssoc.getIndex());

        	// If the node is moving stores, then drag the node hierarchy with it
        	if (movingStore) {

        		// do the move
        		Store newStore = newParentNode.getStore();
        		moveNodeToStore(nodeToMove, newStore);

        		// the node reference will have changed too
        		nodeToMoveRef = nodeToMove.getNodeRef();
        	}

        	// check that no cyclic relationships have been created
        	getPaths(nodeToMoveRef, false);

        	// invoke policy behavior
        	if (movingStore) {
        		/*
        		 * TODO for now indicate that the node has been archived to prevent the version
        		 * history from being removed in the future a onMove policy could be added and
        		 * remove the need for onDelete and onCreate to be fired here
        		 */
        		invokeOnDeleteNode(oldAssocRef, nodeToMoveTypeQName, nodeToMoveAspects, true);
        		invokeOnCreateNode(newAssocRef);
        	} else {
        		invokeOnCreateChildAssociation(newAssocRef, false);
        		invokeOnDeleteChildAssociation(oldAssocRef);
        	}
        	invokeOnMoveNode(oldAssocRef, newAssocRef);

        	// update the node status
        	nodeDaoService.recordChangeId(nodeToMoveRef);
        } finally {
        	logger.debug("[SplittingDbNodeServiceImpl::moveNode] END");
        }

        // done
        return newAssocRef;
    }

    public void setChildAssociationIndex(ChildAssociationRef childAssocRef,
    		int index) {
    	logger.debug("[SplittingDbNodeServiceImpl::setChildAssociationIndex] BEGIN");

    	try {
    		// Se uno dei due nodi e` una parte -> bug
    		Node parentNode = getNodeNotNull(childAssocRef.getParentRef());
    		Node childNode = getNodeNotNull(childAssocRef.getChildRef());

    		Assert.isTrue(!isPart(parentNode) && !isPart(childNode),
    				"BUG: ChildAssociationRef not correctly translated!");

    		Node partNode = null;

    		if (isSplitted(parentNode)) {
    			final QName partQName = QName.createQName(
    					EcmEngineModelConstants.ECMENGINE_SYS_MODEL_URI,
    					getPartName(childAssocRef.getQName().getLocalName()));

    			Collection<ChildAssociationRef> partAssocs = nodeDaoService.getChildAssocRefs(
    					parentNode, partQName);

    			for (ChildAssociationRef partAssoc : partAssocs) {
    				partNode = getNodeNotNull(partAssoc.getChildRef());
    				break; // Consideriamo un solo risultato - le parti devono avere nomi distinti
    			}

    			if (partNode == null) {
    				throw new InvalidChildAssociationRefException("Unable to find part for association " +
    						"while setting index: " +
    						"\n   assoc: " + childAssocRef +
    						"\n   index: " + index, childAssocRef);
    			}
    		}

    		ChildAssoc assoc = nodeDaoService.getChildAssoc(
    				(partNode == null) ? parentNode : partNode,
    						childNode,
    						childAssocRef.getTypeQName(),
    						childAssocRef.getQName());
    		if (assoc == null) {
    			throw new InvalidChildAssociationRefException("Unable to set child association index: " +
    					"\n   assoc: " + childAssocRef +
    					"\n   index: " + index,
    					childAssocRef);
    		}

    		// set the index
    		assoc.setIndex(index);

    		// flush
    		nodeDaoService.flush();
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setChildAssociationIndex] END");
    	}
    }

    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getType] BEGIN");
    	QName typeQName = null;
    	try {
    		Node node = getNodeNotNull(nodeRef);

    		Assert.isTrue(!isPart(node), "BUG: getType() on part node!");
    		typeQName = node.getTypeQName();
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getType] END");
    	}
    	return typeQName;
    }

    /**
     * @see org.alfresco.service.cmr.repository.NodeService#setType(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::setType] BEGIN");

    	try {
    		// check the node type
    		TypeDefinition nodeTypeDef = dictionaryService.getType(typeQName);
    		if (nodeTypeDef == null) {
    			throw new InvalidTypeException(typeQName);
    		}

    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(isPart(node), "BUG: setType() su nodo parte");

    		// Invoke policies
    		invokeBeforeUpdateNode(nodeRef);

    		// Get the node and set the new type
    		node.setTypeQName(typeQName);

    		// Add the default aspects to the node (update the properties with any new default values)
    		Map<QName, Serializable> properties = this.getPropertiesImpl(node);
    		addDefaultAspects(nodeTypeDef, node, properties);
    		this.setProperties(nodeRef, properties);

    		// Invoke policies
    		invokeOnUpdateNode(nodeRef);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setType] END");
    	}
    }

    /**
     * @see Node#getAspects()
     */
    public void addAspect(NodeRef nodeRef, QName aspectTypeQName,
    		Map<QName, Serializable> aspectProperties) throws InvalidNodeRefException,
    		InvalidAspectException {
    	logger.debug("[SplittingDbNodeServiceImpl::addAspect] BEGIN");

    	try {
            nodeRef = tenantService.getName(nodeRef);
    		// check that the aspect is legal
    		AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
    		if (aspectDef == null) {
    			throw new InvalidAspectException("The aspect is invalid: " + aspectTypeQName, aspectTypeQName);
    		}

    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: addAspect() on part node");

    		final boolean splitted = isSplitted(node);

    		// Invoke policy behaviors
    		invokeBeforeUpdateNode(nodeRef);
    		invokeBeforeAddAspect(nodeRef, aspectTypeQName);

    		// attach the properties to the current node properties
    		Map<QName, Serializable> nodeProperties = getPropertiesImpl(node);

    		if (aspectProperties != null) {
    			nodeProperties.putAll(aspectProperties);
    		}

    		// Set any default property values that appear on the aspect
    		addDefaultPropertyValues(aspectDef, nodeProperties);

    		// Add any dependent aspect
    		addDefaultAspects(aspectDef, node, nodeProperties);

    		// Set the property values back on the node
    		setProperties(nodeRef, nodeProperties);

    		// Gestione splitting
    		if (aspectTypeQName.equals(EcmEngineModelConstants.ASPECT_SPLITTED)
    				&& !splitted) {
    			splitNode(node);
    		}

    		// physically attach the aspect to the node
    		if (node.getAspects().add(aspectTypeQName) == true) {
    			// Invoke policy behaviors
    			invokeOnUpdateNode(nodeRef);
    			invokeOnAddAspect(nodeRef, aspectTypeQName);

    			// update the node status
    			nodeDaoService.recordChangeId(nodeRef);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::addAspect] END");
    	}
    }

    /**
     * @see Node#getAspects()
     */
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException {
    	logger.debug("[SplittingDbNodeServiceImpl::removeAspect] BEGIN");

    	try {
    		// get the aspect
    		AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
    		if (aspectDef == null) {
    			throw new InvalidAspectException(aspectTypeQName);
    		}

    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: removeAspect() on part node");

    		// Impedire la rimozione dell'aspect "ecm-sys:splitted"
    		if (aspectTypeQName.equals(EcmEngineModelConstants.ASPECT_SPLITTED)) {
    			logger.debug("[SplittingDbNodeServiceImpl::removeAspect] Ignored request to remove ecm-sys:splitted aspect");
    			return;
    		}

    		Set<QName> nodeAspects = node.getAspects();

    		if (!nodeAspects.contains(aspectTypeQName)) {

    			// The aspect isn't present so just leave it
    			return;
    		}

    		// Invoke policy behaviors
    		invokeBeforeUpdateNode(nodeRef);
    		invokeBeforeRemoveAspect(nodeRef, aspectTypeQName);

    		// remove the aspect, if present
    		node.getAspects().remove(aspectTypeQName);

    		Map<QName, PropertyValue> nodeProperties = node.getProperties();
    		Map<QName, PropertyDefinition> propertyDefs = aspectDef.getProperties();
    		for (QName propertyName : propertyDefs.keySet()) {
    			nodeProperties.remove(propertyName);
    		}

    		// Remove child associations
    		Map<QName, ChildAssociationDefinition> childAssocDefs = aspectDef.getChildAssociations();

    		List<Node> parts = null;

    		if (!childAssocDefs.isEmpty()) {

    			if (isSplitted(node)) {
    				parts = getPartNodes(node);
    			} else {
    				parts = new ArrayList<Node>(1);
    				parts.add(node);
    			}
    		} else {
    			parts = Collections.<Node>emptyList();
    		}

    		for (Node part : parts) {
    			Collection<ChildAssoc> childAssocs = nodeDaoService.getChildAssocs(part);
    			for (ChildAssoc childAssoc : childAssocs) {

    				// Ignore if the association type is not defined by the aspect
    				QName childAssocQName = childAssoc.getTypeQName();
    				if (!childAssocDefs.containsKey(childAssocQName)) {
    					continue;
    				}

    				// The association is of a type that should be removed
    				nodeDaoService.deleteChildAssoc(childAssoc, true);
    			}
    		}

    		// Remove regular associations
    		Map<QName, AssociationDefinition> assocDefs = aspectDef.getAssociations();

    		if (!assocDefs.isEmpty()) {
    			List<NodeAssoc> nodeAssocs = nodeDaoService.getTargetNodeAssocs(node);
    			for (NodeAssoc nodeAssoc : nodeAssocs) {

    				// Ignore if the association type is not defined by the aspect
    				QName nodeAssocQName = nodeAssoc.getTypeQName();
    				if (!assocDefs.containsKey(nodeAssocQName)) {
    					continue;
    				}

    				// Delete the association
    				nodeDaoService.deleteNodeAssoc(nodeAssoc);
    			}
    		}

    		// Invoke policy behaviors
    		invokeOnUpdateNode(nodeRef);
    		invokeOnRemoveAspect(nodeRef, aspectTypeQName);

    		// update the node status
    		nodeDaoService.recordChangeId(nodeRef);

    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::removeAspect] END");
    	}
    }

    /**
     * Performs a check on the set of node aspects
     *
     * @see Node#getAspects()
     */
    public boolean hasAspect(NodeRef nodeRef, QName aspectRef)
    throws InvalidNodeRefException, InvalidAspectException {
    	logger.debug("[SplittingDbNodeServiceImpl::hasAspect] BEGIN");
    	try {
    		// Se e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: hasAspect() on part node!");

    		Set<QName> aspectQNames = node.getAspects();

    		return aspectQNames.contains(aspectRef);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::hasAspect] END");
    	}
    }

    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getAspects] BEGIN");
    	Set<QName> ret = null;

    	try {
    		// Se e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getAspects() on part node!");

    		Set<QName> aspectQNames = node.getAspects();

    		// copy the set to ensure initialization
    		ret = new HashSet<QName>(aspectQNames.size());
    		ret.addAll(aspectQNames);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getAspects] END");
    	}
    	return ret;
    }

    public void deleteNode(NodeRef nodeRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::deleteNode] BEGIN");

    	try {
            nodeRef = tenantService.getName(nodeRef);
    		// First get the node to ensure that it exists
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: deleteNode() on part node!");

    		boolean requiresDelete = false;

    		// Invoke policy behaviors
    		invokeBeforeDeleteNode(nodeRef);

    		// get the primary parent-child relationship before it is gone
    		ChildAssociationRef childAssocRef = getPrimaryParent(nodeRef);

    		// get type and aspect QNames as they will be unavailable after the delete
    		final QName nodeTypeQName = node.getTypeQName();
    		final Set<QName> nodeAspectQNames = node.getAspects();

    		// check if we need to archive the node
    		StoreRef archiveStoreRef = null;
            if (nodeAspectQNames.contains(ContentModel.ASPECT_TEMPORARY) ||
                    nodeAspectQNames.contains(ContentModel.ASPECT_WORKING_COPY)) {
    			// the node has the temporary aspect meaning
    			// it can not be archived
    			requiresDelete = true;
    		} else {
   	            StoreRef storeRef = nodeRef.getStoreRef();

   	            // remove tenant domain - to retrieve archive store from map
   	            final StoreRef baseStoreRef = tenantService.getBaseName(storeRef);

    			archiveStoreRef = storeArchiveMap.getArchiveMap().get(baseStoreRef);

    			// get the type and check if we need archiving
    			final TypeDefinition typeDef = dictionaryService.getType(node.getTypeQName());

    			requiresDelete = (typeDef == null || !typeDef.isArchive() || archiveStoreRef == null);
                if(logger.isDebugEnabled()) {
    			    logger.debug("[SplittingDbNodeServiceImpl::deleteNode] typeDef = " + typeDef);
    		    	if (typeDef != null) {
    	    			logger.debug("[SplittingDbNodeServiceImpl::deleteNode] typeDef.isArchive() = " + typeDef.isArchive());
        			}
        			logger.debug("[SplittingDbNodeServiceImpl::deleteNode] archiveStoreRef = " + archiveStoreRef);
    			    logger.debug("[SplittingDbNodeServiceImpl::deleteNode] requiresDelete = " + requiresDelete);
    			}
    		}

    		if (requiresDelete) {

    			// perform a normal deletion
    			nodeDaoService.deleteNode(node, true);

    			// Invoke policy behaviors
    			invokeOnDeleteNode(childAssocRef, nodeTypeQName, nodeAspectQNames, false);
    		} else {
                archiveStoreRef = tenantService.getName(archiveStoreRef);

    			// archive it
    			archiveNode(nodeRef, archiveStoreRef);
    			// The archive performs a move, which will fire the appropriate OnDeleteNode
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::deleteNode] END");
    	}
    }

    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName,
    		QName assocQName) {
    	logger.debug("[SplittingDbNodeServiceImpl::addChild] BEGIN");
    	ChildAssociationRef assocRef = null;

    	try {
    		Node parentNode = getNodeNotNull(parentRef);
    		Node childNode = getNodeNotNull(childRef);

    		Assert.isTrue(!isPart(parentNode) && !isPart(childNode),
    				"BUG: addChild() on one or more part nodes!");

    		// Invoke policy behaviors
    		invokeBeforeCreateChildAssociation(parentRef, childRef, assocTypeQName, assocQName, false);

    		Node destinationNode = null;

    		if (isSplitted(parentNode)) {
    			destinationNode = getPartByChildAssocName(parentNode, assocQName.getLocalName());

    			if (destinationNode == null) {
    				destinationNode = createMissingPart(parentNode, assocQName.getLocalName());
    			}
    		} else {
    			destinationNode = parentNode;
    		}

    		Assert.notNull(destinationNode, "BUG: null destination node in addChild()!");

    		// make the association
    		ChildAssoc assoc = nodeDaoService.newChildAssoc(destinationNode,	// Nodo o parte
    				childNode, false, assocTypeQName, assocQName);
    		// ensure name uniqueness
    		setChildUniqueName(childNode);

    		assocRef = new ChildAssociationRef(assoc.getTypeQName(), parentRef,
    				assoc.getQname(), childRef, assoc.getIsPrimary(), assoc.getIndex());

    		// check that the child addition of the child has not created a cyclic relationship
    		// this functionality is provided for free in getPath
    		getPaths(childRef, false);

    		// update the node status (backported for CSI from 20071004 HEAD)
    		nodeDaoService.recordChangeId(childRef);

    		// Invoke policy behaviors
    		invokeOnCreateChildAssociation(assocRef, false);

    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::addChild] END");
    	}
    	return assocRef;
    }

    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::removeChild] BEGIN");

    	try {
    		Node parentNode = getNodeNotNull(parentRef);
    		Node childNode = getNodeNotNull(childRef);

    		Assert.isTrue(!isPart(parentNode) && !isPart(childNode),
    				"BUG: removeChild() on one or more part nodes!");

    		Long childNodeId = childNode.getId();

    		List<Node> parts = null;

    		if (isSplitted(parentNode)) {
    			parts = getPartNodes(parentNode);
    		} else {
    			parts = new ArrayList<Node>(1);
    			parts.add(parentNode);
    		}

    		ChildAssociationRef primaryAssocRef = null;

    		for (Node part : parts) {
    			// get all the child associations
    			Collection<ChildAssoc> assocs = nodeDaoService.getChildAssocs(part);

    			assocs = new HashSet<ChildAssoc>(assocs);	// copy set as we will be modifying it
    			for (ChildAssoc assoc : assocs) {

    				if (!assoc.getChild().getId().equals(childNodeId)) {
    					continue;	// not a matching association
    				}

    				ChildAssociationRef assocRef = new ChildAssociationRef(assoc.getTypeQName(),
    						parentRef, assoc.getQname(), childRef, assoc.getIsPrimary(), assoc.getIndex());

    				// Is this a primary association?
    				if (assoc.getIsPrimary()) {

    					// keep the primary association for last
    					primaryAssocRef = assocRef;
    				} else {

    					// delete the association instance - it is not primary
    					invokeBeforeDeleteChildAssociation(assocRef);
    					nodeDaoService.deleteChildAssoc(assoc, true);   // cascade
    					invokeOnDeleteChildAssociation(assocRef);
    				}
    			}
    		}

    		// remove the child if the primary association was a match
    		if (primaryAssocRef != null) {
    			deleteNode(primaryAssocRef.getChildRef());
    		} else {
    			/*
    			 * The cascade delete will update the node status, but just a plain
    			 * association deletion will not.
    			 * Update the node status (backported for CSI from 20071004 HEAD)
    			 */
    			nodeDaoService.recordChangeId(childRef);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::removeChild] END");
    	}
    }

    public boolean removeChildAssociation(ChildAssociationRef childAssocRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::removeChildAssociation] BEGIN");
    	boolean deleted = false;

    	try {
    		Node parentNode = getNodeNotNull(childAssocRef.getParentRef());
    		Node childNode = getNodeNotNull(childAssocRef.getChildRef());

    		Assert.isTrue(!isPart(parentNode) && !isPart(childNode),
    				"BUG: removeChildAssociation() on one or more part nodes!");

    		final QName typeQName = childAssocRef.getTypeQName();
    		final QName qname = childAssocRef.getQName();

    		Node realParentNode = (isSplitted(parentNode))
    				? getPartByChildAssocName(parentNode, qname.getLocalName())
    				: parentNode;

    		Assert.notNull(realParentNode, "BUG: part of child to remove not found!");

    		// Delete the association
    		invokeBeforeDeleteChildAssociation(childAssocRef);
    		deleted = nodeDaoService.deleteChildAssoc(realParentNode, childNode, typeQName, qname);

    		if (deleted) {

    			invokeOnDeleteChildAssociation(childAssocRef);

    			// Update the node status (backported for CSI from 20071004 HEAD)
    			nodeDaoService.recordChangeId(childNode.getNodeRef());
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::removeChildAssociation] END");
    	}
    	return deleted;
    }

    public boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::removeSeconaryChildAssociation] BEGIN");

    	try {
    		Node parentNode = getNodeNotNull(childAssocRef.getParentRef());
    		Node childNode = getNodeNotNull(childAssocRef.getChildRef());

    		Assert.isTrue(!isPart(parentNode) && !isPart(childNode),
    				"BUG: removeSeconaryChildAssociation() on one or more part nodes!");

    		final QName typeQName = childAssocRef.getTypeQName();
    		final QName qname = childAssocRef.getQName();

    		Node realParentNode = (isSplitted(parentNode))
    				? getPartByChildAssocName(parentNode, qname.getLocalName())
    				: parentNode;

    		Assert.notNull(realParentNode, "BUG: part of child to remove not found!");

    		ChildAssoc assoc = nodeDaoService.getChildAssoc(realParentNode, childNode, typeQName, qname);
    		if (assoc == null) {

    			// No association exists
    			return false;
    		}

    		if (assoc.getIsPrimary()) {
    			throw new IllegalArgumentException(
    					"removeSeconaryChildAssociation can not be applied to a primary association: \n" +
    					"   Child Assoc: " + assoc);
    		}

    		// Delete the secondary association
    		nodeDaoService.deleteChildAssoc(assoc, false);
    		invokeOnDeleteChildAssociation(childAssocRef);

    		// Update the node status (backported for CSI from 20071004 HEAD)
    		nodeDaoService.recordChangeId(childNode.getNodeRef());
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::removeSeconaryChildAssociation] END");
    	}
    	return true;
    }

    /**
     * Remove properties that should not be persisted as general properties.  Where necessary, the
     * properties are set on the node.
     *
     * @param node the node to set properties on
     * @param properties properties to change
     */
    private void extractIntrinsicProperties(Node node, Map<QName, Serializable> properties) {
    	logger.debug("[SplittingDbNodeServiceImpl::extractIntrinsicProperties] BEGIN");
        properties.remove(ContentModel.PROP_STORE_PROTOCOL);
        properties.remove(ContentModel.PROP_STORE_IDENTIFIER);
        properties.remove(ContentModel.PROP_NODE_UUID);
        properties.remove(ContentModel.PROP_NODE_DBID);
        logger.debug("[SplittingDbNodeServiceImpl::extractIntrinsicProperties] END");
    }

    /**
     * Adds all properties used by the
     * {@link ContentModel#ASPECT_REFERENCEABLE referencable aspect}.
     * <p>
     * This method can be used to ensure that the values used by the aspect
     * are present as node properties.
     * <p>
     * This method also ensures that the {@link ContentModel#PROP_NAME name property}
     * is always present as a property on a node.
     *
     * @param node the node with the values
     * @param nodeRef the node reference containing the values required
     * @param properties the node properties
     */
    private void addIntrinsicProperties(Node node, Map<QName, Serializable> properties) {
    	logger.debug("[SplittingDbNodeServiceImpl::addIntrinsicProperties] BEGIN");

    	final NodeRef nodeRef = tenantService.getBaseName(node.getNodeRef());

        properties.put(ContentModel.PROP_STORE_PROTOCOL, nodeRef.getStoreRef().getProtocol());
        properties.put(ContentModel.PROP_STORE_IDENTIFIER, nodeRef.getStoreRef().getIdentifier());
        properties.put(ContentModel.PROP_NODE_UUID, nodeRef.getId());
        properties.put(ContentModel.PROP_NODE_DBID, node.getId());

        // add the ID as the name, if required
        if (properties.get(ContentModel.PROP_NAME) == null) {
            properties.put(ContentModel.PROP_NAME, nodeRef.getId());
        }
        logger.debug("[SplittingDbNodeServiceImpl::addIntrinsicProperties] END");
    }

    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getProperties] BEGIN");

    	try {
            nodeRef = tenantService.getName(nodeRef);
    		Node node = getNodeNotNull(nodeRef);

    		return getPropertiesImpl(node);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getProperties] END");
    	}
    }

    private Map<QName, Serializable> getPropertiesImpl(Node node) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getPropertiesImpl] BEGIN");
    	Map<QName, Serializable> ret = null;

    	try {
    		// Se il nodo e` una parte -> bug
    		Assert.isTrue(!isPart(node), "BUG: getPropertiesImpl() on part node!");

        	Map<QName,PropertyDefinition> propDefs = dictionaryService.getPropertyDefs(node.getTypeQName());

        	Map<QName, PropertyValue> nodeProperties = node.getProperties();
    		ret = new HashMap<QName, Serializable>(nodeProperties.size());

    		// copy values
    		for (Map.Entry<QName, PropertyValue> entry: nodeProperties.entrySet()) {
    			QName propertyQName = entry.getKey();
    			PropertyValue propertyValue = entry.getValue();

    			if (propDefs != null) {
	    			// get the property definition
	                PropertyDefinition propertyDef = propDefs.get(propertyQName);

	                if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) &&
	                    (propertyValue != null) && (propertyValue.getStringValue() != null))
	                {
	                	propertyValue.setStringValue(tenantService.getBaseName(new NodeRef(propertyValue.getStringValue())).toString());
	                }

	    			// convert to the correct type
	    			Serializable value = makeSerializableValue(propertyDef, propertyValue);

	    			// copy across
	    			ret.put(propertyQName, value);
    			}
    		}

    		// spoof referenceable properties
    		addIntrinsicProperties(node, ret);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getPropertiesImpl] END");
    	}
    	return ret;
    }

    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getProperty] BEGIN");

    	try {
    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getProperty() on part node!");

    		// spoof referencable properties
    		if (qname.equals(ContentModel.PROP_STORE_PROTOCOL)) {
    			return nodeRef.getStoreRef().getProtocol();
    		} else if (qname.equals(ContentModel.PROP_STORE_IDENTIFIER)) {
    			return nodeRef.getStoreRef().getIdentifier();
    		} else if (qname.equals(ContentModel.PROP_NODE_UUID)) {
    			return nodeRef.getId();
    		} else if (qname.equals(ContentModel.PROP_NODE_DBID)) {
    			return node.getId();
    		}

    		Map<QName, PropertyValue> properties = node.getProperties();
    		PropertyValue propertyValue = properties.get(qname);

    		// check if we need to provide a spoofed name
    		if (propertyValue == null && qname.equals(ContentModel.PROP_NAME)) {
    			return nodeRef.getId();
    		}

    		// get the property definition
    		PropertyDefinition propertyDef = dictionaryService.getProperty(qname);

            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) &&
                (propertyValue != null) && (propertyValue.getStringValue() != null))
            {
            	propertyValue.setStringValue(tenantService.getBaseName(new NodeRef(propertyValue.getStringValue())).toString());
            }

    		// convert to the correct type and return
    		return makeSerializableValue(propertyDef, propertyValue);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getProperty] END");
    	}
    }

    /**
     * Ensures that all required properties are present on the node and copies the
     * property values to the <code>Node</code>.
     * <p>
     * To remove a property, <b>remove it from the map</b> before calling this method.
     * Null-valued properties are allowed.
     * <p>
     * If any of the values are null, a marker object is put in to mimic nulls.  They will be turned back into
     * a real nulls when the properties are requested again.
     *
     * @see Node#getProperties()
     */
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties)
    throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::setProperties] BEGIN");

    	try {
    		Node node = getNodeNotNull(nodeRef);

    		// Invoke policy behaviours
    		invokeBeforeUpdateNode(nodeRef);

    		// Do the set properties
    		Map<QName, Serializable> propertiesBefore = getPropertiesImpl(node);
    		Map<QName, Serializable> propertiesAfter = setPropertiesImpl(node, properties);

    		setChildUniqueName(node);	// ensure uniqueness

    		// Invoke policy behaviours
    		invokeOnUpdateNode(nodeRef);
    		invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setProperties] END");
    	}
    }

    /**
     * Does the work of setting the property values.  Returns a map containing the state of the properties after the set
     * operation is complete.
     *
     * @param node              the node
     * @param properties        the map of property values
     * @return                  the map of property values after the set operation is complete
     * @throws InvalidNodeRefException
     */
    private Map<QName, Serializable> setPropertiesImpl(Node node, Map<QName, Serializable> properties)
    throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::setPropertiesImpl] BEGIN");

    	try {
    		ParameterCheck.mandatory("properties", properties);

    		// Se il nodo e` una parte -> bug
    		Assert.isTrue(!isPart(node), "BUG: setPropertiesImpl() on part node!");

    		// remove referencable properties
    		extractIntrinsicProperties(node, properties);

    		// copy properties onto node
    		Map<QName, PropertyValue> nodeProperties = node.getProperties();
    		nodeProperties.clear();

    		// check the property type and copy the values across
    		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {
    			PropertyDefinition propertyDef = dictionaryService.getProperty(property.getKey());

    			// get a persistable value
    			PropertyValue propertyValue = makePropertyValue(propertyDef, property.getValue());
    			nodeProperties.put(property.getKey(), propertyValue);
    		}

    		// update the node status
    		NodeRef nodeRef = node.getNodeRef();
    		nodeDaoService.recordChangeId(nodeRef);

    		// Return the properties after
    		return Collections.unmodifiableMap(properties);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setPropertiesImpl] END");
    	}
    }

    /**
     * Gets the properties map, sets the value (null is allowed) and checks that the new set
     * of properties is valid.
     *
     * @see DbNodeServiceImpl.NullPropertyValue
     */
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value)
    		throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::setProperty] BEGIN");

    	try {
    		Assert.notNull(qname);

            nodeRef = tenantService.getName(nodeRef);
    		Node node = getNodeNotNull(nodeRef);

    		// Invoke policy behaviors
    		invokeBeforeUpdateNode(nodeRef);

    		// Do the set operation
    		Map<QName, Serializable> propertiesBefore = getPropertiesImpl(node);
    		Map<QName, Serializable> propertiesAfter = setPropertyImpl(node, qname, value);

    		if (qname.equals(ContentModel.PROP_NAME)) {
    			setChildUniqueName(node);	// ensure uniqueness
    		}

    		// Invoke policy behaviors
    		invokeOnUpdateNode(nodeRef);
    		invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setProperty] END");
    	}
    }

    /**
     * Does the work of setting a property value.  Returns the values of the properties after the set operation is
     * complete.
     *
     * @param node          the node
     * @param qname         the qname of the property
     * @param value         the value of the property
     * @return              the values of the properties after the set operation is complete
     * @throws InvalidNodeRefException
     */
    private Map<QName, Serializable> setPropertyImpl(Node node, QName qname, Serializable value)
    throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::setPropertyImpl] BEGIN");

    	try {
    		// Se il nodo e` una parte -> bug
    		Assert.isTrue(!isPart(node), "BUG: setPropertyImpl() on part node!");

    		NodeRef nodeRef = node.getNodeRef();

    		Map<QName, PropertyValue> properties = node.getProperties();
    		PropertyDefinition propertyDef = dictionaryService.getProperty(qname);

    		// get a persistable value
    		PropertyValue propertyValue = makePropertyValue(propertyDef, value);
    		properties.put(qname, propertyValue);

    		// update the node status
    		nodeDaoService.recordChangeId(nodeRef);

    		return getPropertiesImpl(node);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setPropertyImpl] END");
    	}
    }

    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::removeProperty] BEGIN");

    	try {
    		if (qname.equals(ContentModel.PROP_NAME)) {
    			throw new UnsupportedOperationException("The property " + qname +
    			" may not be removed individually");
    		}

            nodeRef = tenantService.getName(nodeRef);
    		Node node = getNodeNotNull(nodeRef);

    		// Invoke policy behaviors
    		invokeBeforeUpdateNode(nodeRef);

    		// Get the values before
    		Map<QName, Serializable> propertiesBefore = getPropertiesImpl(node);

    		// Remove the property
    		Map<QName, PropertyValue> properties = node.getProperties();
    		properties.remove(qname);

    		// Get the values afterwards
    		Map<QName, Serializable> propertiesAfter = getPropertiesImpl(node);

    		// Invoke policy behaviours
    		invokeOnUpdateNode(nodeRef);
    		invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::removeProperty] END");
    	}
    }

    /**
     * Transforms {@link Node#getParentAssocs()} to a new collection
     */
    public Collection<NodeRef> getParents(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getParents] BEGIN");
    	Collection<NodeRef> results = null;

    	try {
    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getParents() on part node!");

    		// get the assocs pointing to it
    		Collection<ChildAssoc> parentAssocs = nodeDaoService.getParentAssocs(node);

    		// list of results
    		results = new ArrayList<NodeRef>(parentAssocs.size());
    		for (ChildAssoc assoc : parentAssocs) {

    			// get the parent
                results.add(tenantService.getBaseName(assoc.getParent().getNodeRef()));
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getParents] END");
    	}
    	return results;
    }

    /**
     * Restituisce le associazioni ai padri del nodo dato filtrandole per tipo e nome completo.
     *
     * @param nodeRef Il riferimento al nodo di cui richiedere le associazioni.
     * @param typeQNamePattern Il pattern dei nomi completi di tipo di associazione da accettare.
     * @param qnamePattern Il pattern dei nomi completi di associazione da accettare.
     *
     * @return Una lista di riferimenti alle associazioni che soddisfano i filtri.
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
    		QNamePattern qnamePattern) {
    	logger.debug("[SplittingDbNodeServiceImpl::getParentAssocs] BEGIN");
    	List<ChildAssociationRef> results = null;

    	try {
    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getParentAssocs() on part node!");

    		// get the associations pointing to it
    		Collection<ChildAssoc> parentAssocs = nodeDaoService.getParentAssocs(node);

    		// shortcut if there are no associations
    		if (parentAssocs.size() == 0) {
    			return Collections.<ChildAssociationRef>emptyList();
    		}

    		// list of results
    		results = new ArrayList<ChildAssociationRef>(parentAssocs.size());
    		for (ChildAssoc assoc : parentAssocs) {

    			// does the qname match the pattern?
    			if (!qnamePattern.isMatch(assoc.getQname())
    					|| !typeQNamePattern.isMatch(assoc.getTypeQName())) {

    				// no match - ignore
    				continue;
    			}

    			if (isPart(assoc.getParent())) {

    				// Il parent e` una parte, e` necessario risalire al contenitore...
    				Node realParent = getPartsContainerNode(assoc.getParent());

    				// Aggiunta dell'associazione tradotta ai risultati
    				results.add(new ChildAssociationRef(assoc.getTypeQName(), tenantService.getBaseName(realParent.getNodeRef()),
    						assoc.getQname(), tenantService.getBaseName(nodeRef), assoc.getIsPrimary(), assoc.getIndex()));
    			} else {
    	            ChildAssociationRef childAssocRef = new ChildAssociationRef(
    	                    assoc.getChildAssocRef().getTypeQName(),
    	                    tenantService.getBaseName(assoc.getChildAssocRef().getParentRef()),
    	                    assoc.getChildAssocRef().getQName(),
    	                    tenantService.getBaseName(assoc.getChildAssocRef().getChildRef()),
    	                    assoc.getChildAssocRef().isPrimary(),
    	                    assoc.getChildAssocRef().getNthSibling());
    	            results.add(childAssocRef);
    			}
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getParentAssocs] END");
    	}
    	return results;
    }

    /**
     * Restituisce le associazioni ai figli del nodo dato filtrandole per tipo e nome completo.
     *
     * @param nodeRef Il riferimento al nodo di cui richiedere le associazioni.
     * @param typeQNamePattern Il pattern dei nomi completi di tipo di associazione da accettare.
     * @param qnamePattern Il pattern dei nomi completi di associazione da accettare.
     *
     * @return Una lista di riferimenti alle associazioni che soddisfano i filtri.
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
    		QNamePattern qnamePattern) {
    	logger.debug("[SplittingDbNodeServiceImpl::getChildAssocs] BEGIN");

    	try {
    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getChildAssocs() on part node!");

    		Collection<ChildAssociationRef> childAssocRefs = null;

    		// if the type is the wildcard type, and the qname is not a search, then use a shortcut query
    		if (typeQNamePattern.equals(RegexQNamePattern.MATCH_ALL)
    				&& (qnamePattern instanceof QName)) {

    			// Ricerca per un figlio di un nome preciso -> ottimizzazione sulla parte associata
    			final QName childQName = (QName) qnamePattern;
    			Node part = null;
    			if (isSplitted(node)) {
    				part = getPartByChildAssocName(node, childQName.getLocalName());

    				if (part == null) { // Non c'e` la parte che dovrebbe contenere il figlio cercato
    					return Collections.<ChildAssociationRef>emptyList();
    				}
    			} else {
    				part = node;
    			}

    			if (logger.isDebugEnabled()) {
    				logger.debug("[SplittingDbNodeServiceImpl::getChildAssocs] " +
    						"Ricerca limitata alla parte " + part.getNodeRef());
    			}

    			// get all child associations with the specific qualified name
    			childAssocRefs = nodeDaoService.getChildAssocRefs(part, childQName);
    		} else {

    			List<Node> parts;

    			if (isSplitted(node)) {
    				parts = getPartNodes(node);
    			} else {
    				parts = new ArrayList<Node>(1);
    				parts.add(node);
    			}

    			childAssocRefs = new ArrayList<ChildAssociationRef>();

    			for (Node part : parts) {
    				// get all child associations
    				Collection<ChildAssociationRef> partialResult = nodeDaoService.getChildAssocRefs(part);
    				if (logger.isDebugEnabled()) {
    					logger.debug("[SplittingDbNodeServiceImpl::getChildAssocs] " +
    							"Found " + partialResult.size() + " children in part: " + part.getNodeRef());
    				}

    				// remove non-matching associations
    				Iterator<ChildAssociationRef> iter = partialResult.iterator();
    				while (iter.hasNext()) {

    					ChildAssociationRef child = iter.next();

    					// does the qname match the pattern?
    					if (!qnamePattern.isMatch(child.getQName())
    							|| !typeQNamePattern.isMatch(child.getTypeQName())) {

    						// no match - remove
    						iter.remove();
    					}
    				}

    				// add to results
    				childAssocRefs.addAll(partialResult);
    			}
    		}

    		// sort the results and return
    		if (logger.isDebugEnabled()) {
    			logger.debug("[SplittingDbNodeServiceImpl::getChildAssocs] Found " + childAssocRefs.size() + " children.");
    		}
    		return reorderChildAssocs(childAssocRefs);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getChildAssocs] END");
    	}
    }

    private List<ChildAssociationRef> reorderChildAssocs(Collection<ChildAssociationRef> childAssocRefs) {
    	logger.debug("[SplittingDbNodeServiceImpl::reorderChildAssocs] BEGIN");
    	ArrayList<ChildAssociationRef> orderedList = null;
    	try {
    		// shortcut if there are no associations
    		if (childAssocRefs.isEmpty()){
    			return Collections.<ChildAssociationRef>emptyList();
    		}

    		// sort results
    		orderedList = new ArrayList<ChildAssociationRef>(childAssocRefs);
    		Collections.sort(orderedList);

    		// list of results
    		int nthSibling = 0;
    		for (ChildAssociationRef child : orderedList) {
    			child.setNthSibling(nthSibling);
    			nthSibling++;
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::reorderChildAssocs] END");
    	}

    	return orderedList;
    }

    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName) {
    	logger.debug("[SplittingDbNodeServiceImpl::getChildByName] BEGIN");
    	NodeRef result = null;

    	try {
    		// Se il nodo e` una parte -> bug
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getChildByName() on part node!");

    		List<Node> parts = null;

    		// Nome dell'associazione e childName possono essere diversi, quindi cerchiamo su
    		// tutte le parti
    		if (isSplitted(node)) {
    			parts = getPartNodes(node);
    		} else {
    			parts = new ArrayList<Node>(1);
    			parts.add(node);
    		}

    		for (Node part : parts) {
    			ChildAssoc childAssoc = nodeDaoService.getChildAssoc(part, assocTypeQName, childName);

    			if (childAssoc != null) {
    				result = tenantService.getBaseName(childAssoc.getChild().getNodeRef());
    			}
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getChildByName] END");
    	}
    	return result;
    }

    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getPrimaryParent] BEGIN");
    	ChildAssociationRef assocRef = null;

    	try {
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getPrimaryParent() on part node!");

    		// get the primary parent association
    		ChildAssoc assoc = nodeDaoService.getPrimaryParentAssoc(node);

    		// done - the association may be null for a root node
    		if (assoc == null) {
    			assocRef = new ChildAssociationRef(null, null, null, nodeRef);
    		} else {

    			// Il padre trovato potrebbe essere un nodo parte - necessario risalire al
    			// nodo contenitore
    			Node parentNode = assoc.getParent();

    			assocRef = new ChildAssociationRef(assoc.getTypeQName(),
    					tenantService.getBaseName(getPartsContainerNode(parentNode).getNodeRef()), assoc.getQname(),
    					tenantService.getBaseName(nodeRef),
    					assoc.getIsPrimary(), assoc.getIndex());
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getPrimaryParent] END");
    	}
    	return assocRef;
    }

    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
    throws InvalidNodeRefException, AssociationExistsException {
    	logger.debug("[SplittingDbNodeServiceImpl::createAssociation] BEGIN");
    	AssociationRef assocRef = null;

    	try {
    		Node sourceNode = getNodeNotNull(sourceRef);
    		Node targetNode = getNodeNotNull(targetRef);

    		Assert.isTrue(!isPart(sourceNode) && !isPart(targetNode),
    				"BUG: createAssociation() on one or more part nodes!");

    		// see if it exists
    		NodeAssoc assoc = nodeDaoService.getNodeAssoc(sourceNode, targetNode, assocTypeQName);
    		if (assoc != null) {
    			throw new AssociationExistsException(sourceRef, targetRef, assocTypeQName);
    		}

    		// we are sure that the association doesn't exist - make it
    		assoc = nodeDaoService.newNodeAssoc(sourceNode, targetNode, assocTypeQName);
    		assocRef = assoc.getNodeAssocRef();

    		// Invoke policy behavious
    		invokeOnCreateAssociation(assocRef);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::createAssociation] END");
    	}
    	return assocRef;
    }

    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
    throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::removeAssociation] BEGIN");

    	try {
    		Node sourceNode = getNodeNotNull(sourceRef);
    		Node targetNode = getNodeNotNull(targetRef);

    		Assert.isTrue(!isPart(sourceNode) && !isPart(targetNode),
    				"BUG: removeAssociation() on one or more part nodes!");

    		// get the association
    		NodeAssoc assoc = nodeDaoService.getNodeAssoc(sourceNode, targetNode, assocTypeQName);
    		if (assoc == null) {
    			// nothing to remove
    			return;
    		}
    		AssociationRef assocRef = assoc.getNodeAssocRef();

    		// delete it
    		nodeDaoService.deleteNodeAssoc(assoc);

    		// Invoke policy behaviors
    		invokeOnDeleteAssociation(assocRef);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::removeAssociation] END");
    	}
    }

    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern) {
    	logger.debug("[SplittingDbNodeServiceImpl::getTargetAssocs] BEGIN");
    	List<AssociationRef> nodeAssocRefs = null;

    	try {
    		Node sourceNode = getNodeNotNull(sourceRef);

    		Assert.isTrue(!isPart(sourceNode), "BUG: getTargetAssocs() on part node!");

    		// get all associations to target
    		Collection<NodeAssoc> assocs = nodeDaoService.getTargetNodeAssocs(sourceNode);
    		nodeAssocRefs = new ArrayList<AssociationRef>(assocs.size());
    		for (NodeAssoc assoc : assocs) {

    			// check qname pattern
    			if (!qnamePattern.isMatch(assoc.getTypeQName())) {
    				continue;	// the association name doesn't match the pattern given
    			}
    			nodeAssocRefs.add(assoc.getNodeAssocRef());
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getTargetAssocs] END");
    	}
    	return nodeAssocRefs;
    }

    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern) {
    	logger.debug("[SplittingDbNodeServiceImpl::getSourceAssocs] BEGIN");
    	List<AssociationRef> nodeAssocRefs = null;

    	try {
    		Node targetNode = getNodeNotNull(targetRef);

    		Assert.isTrue(!isPart(targetNode), "BUG: getSourceAssocs() on part node!");

    		// get all associations to source
    		Collection<NodeAssoc> assocs = nodeDaoService.getSourceNodeAssocs(targetNode);
    		nodeAssocRefs = new ArrayList<AssociationRef>(assocs.size());
    		for (NodeAssoc assoc : assocs) {

    			// check qname pattern
    			if (!qnamePattern.isMatch(assoc.getTypeQName())) {
    				continue;	// the association name doesn't match the pattern given
    			}
    			nodeAssocRefs.add(assoc.getNodeAssocRef());
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getSourceAssocs] END");
    	}
    	return nodeAssocRefs;
    }

    /**
     * Recursive method used to build up paths from a given node to the root.
     * <p>
     * Whilst walking up the hierarchy to the root, some nodes may have a <b>root</b> aspect.
     * Everytime one of these is encountered, a new path is farmed off, but the method
     * continues to walk up the hierarchy.
     *
     * @param currentNode the node to start from, i.e. the child node to work upwards from
     * @param currentPath the path from the current node to the descendent that we started from
     * @param completedPaths paths that have reached the root are added to this collection
     * @param assocStack the parent-child relationships traversed whilst building the path.
     *      Used to detected cyclic relationships.
     * @param primaryOnly true if only the primary parent association must be traversed.
     *      If this is true, then the only root is the top level node having no parents.
     * @throws CyclicChildRelationshipException
     */
    private void prependPaths(final Node currentNode, final Path currentPath, Collection<Path> completedPaths,
    		Stack<ChildAssoc> assocStack, boolean primaryOnly) throws CyclicChildRelationshipException {
    	logger.debug("[SplittingDbNodeServiceImpl::prependPaths] BEGIN");

    	try {
    		Assert.isTrue(!isPart(currentNode), "BUG: prependPaths() on part node!");
    		NodeRef currentNodeRef = currentNode.getNodeRef();

    		// get the parent associations of the given node
    		Collection<ChildAssoc> parentAssocs = nodeDaoService.getParentAssocs(currentNode);

    		// does the node have parents
    		boolean hasParents = !parentAssocs.isEmpty();

    		// does the current node have a root aspect?
    		boolean isRoot = currentNode.getAspects().contains(ContentModel.ASPECT_ROOT);
    		boolean isStoreRoot = currentNode.getTypeQName().equals(ContentModel.TYPE_STOREROOT);

    		// look for a root.  If we only want the primary root, then ignore all but the top-level root.
    		if (isRoot && !(primaryOnly && hasParents)) { // exclude primary search with parents present

    			// create a one-sided association reference for the root node and prepend to the stack
    			// this effectively spoofs the fact that the current node is not below the root
    			// - we put this association in as the first association in the path must be a one-sided
    			//   reference pointing to the root node
    			ChildAssociationRef assocRef = new ChildAssociationRef(
    					null, null, null, // Type, parent and QName
    					getRootNode(currentNode.getNodeRef().getStoreRef()));

    			// create a path to save and add the 'root' association
    			Path pathToSave = new Path();
    			Path.ChildAssocElement first = null;

    			for (Path.Element element : currentPath) {
    				if (first == null) {
    					first = (Path.ChildAssocElement) element;
    				} else {
    					pathToSave.append(element);
    				}
    			}

    			if (first != null) {

    				// mimic an association that would appear if the current node was below
    				// the root node
    				// or if first beneath the root node it will make the real thing
    				ChildAssociationRef updateAssocRef = new ChildAssociationRef(
    						isStoreRoot ? ContentModel.ASSOC_CHILDREN : first.getRef().getTypeQName(),
    								getRootNode(currentNode.getNodeRef().getStoreRef()),
    								first.getRef().getQName(),
    								first.getRef().getChildRef());
    				Path.Element newFirst =  new Path.ChildAssocElement(updateAssocRef);
    				pathToSave.prepend(newFirst);
    			}

    			Path.Element element = new Path.ChildAssocElement(assocRef);
    			pathToSave.prepend(element);

    			// store the path just built
    			completedPaths.add(pathToSave);
    		}

    		if (!hasParents && !isRoot) {
    			throw new RuntimeException("Node without parents does not have root aspect: " +
    					currentNodeRef);
    		}

    		// walk up each parent association
    		for (ChildAssoc assoc : parentAssocs) {

    			// does the association already exist in the stack
    			if (assocStack.contains(assoc)) {
    				// the association was present already
    				throw new CyclicChildRelationshipException(
    						"Cyclic parent-child relationship detected: \n" +
    						"   current node: " + currentNode + "\n" +
    						"   current path: " + currentPath + "\n" +
    						"   next assoc: " + assoc,
    						assoc);
    			}

    			// do we consider only primary associations?
    			if (primaryOnly && !assoc.getIsPrimary()) {
    				continue;
    			}

    			Node realParent = getPartsContainerNode(assoc.getParent());

    			// build a real association reference
    			ChildAssociationRef assocRef = new ChildAssociationRef(assoc.getTypeQName(), tenantService.getBaseName(realParent.getNodeRef()),
    					assoc.getQname(), tenantService.getBaseName(assoc.getChild().getNodeRef()), assoc.getIsPrimary(), -1);

    			// Ordering is not important here: We are building distinct paths upwards
    			Path.Element element = new Path.ChildAssocElement(assocRef);

    			// create a new path that builds on the current path
    			Path path = new Path();
    			path.append(currentPath);

    			// prepend element
    			path.prepend(element);

    			// push the associations stack, recurse and pop
    			assocStack.push(assoc);
    			prependPaths(realParent, path, completedPaths, assocStack, primaryOnly);
    			assocStack.pop();
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::prependPaths] END");
    	}
    }

    /**
     * @see #getPaths(NodeRef, boolean)
     * @see #prependPaths(Node, Path, Collection, Stack, boolean)
     */
    public Path getPath(NodeRef nodeRef) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getPath] BEGIN");

    	try {
    		List<Path> paths = getPaths(nodeRef, true);   // checks primary path count
    		Assert.isTrue(paths.size() == 1, "BUG: Primary path count not checked!");

    		return paths.get(0);   // we know there is only one
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getPath] END");
    	}
    }

    /**
     * When searching for <code>primaryOnly == true</code>, checks that there is exactly
     * one path.
     * @see #prependPaths(Node, Path, Collection, Stack, boolean)
     */
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws InvalidNodeRefException {
    	logger.debug("[SplittingDbNodeServiceImpl::getPaths] BEGIN");
    	final long start = System.currentTimeMillis();
    	List<Path> paths = null;

    	try {
    		// get the starting node
    		Node node = getNodeNotNull(nodeRef);
    		Assert.isTrue(!isPart(node), "BUG: getPaths() on a part node!");

    		// create storage for the paths - only need 1 bucket if we are looking for the primary path
    		paths = new ArrayList<Path>(primaryOnly ? 1 : 10);

    		// create an empty current path to start from
    		Path currentPath = new Path();

    		// create storage for touched associations
    		Stack<ChildAssoc> assocStack = new Stack<ChildAssoc>();

    		// call recursive method to sort it out
    		prependPaths(node, currentPath, paths, assocStack, primaryOnly);

    		// check that for the primary only case we have exactly one path
    		if (primaryOnly && paths.size() != 1) {
    			throw new RuntimeException("Node has " + paths.size() + " primary paths: " + nodeRef);
    		}

    		if (loggerPaths.isDebugEnabled()) {
    			StringBuilder sb = new StringBuilder(256);

    			sb.append((primaryOnly) ? "Primary path for node " : "Paths for node ").append(nodeRef);

    			for (Path path : paths) {
    				sb.append("\n   ").append(path);
    			}

    			loggerPaths.debug("[SplittingDbNodeServiceImpl::getPaths] " + sb);
    		}
    	} finally {
    		final long stop = System.currentTimeMillis();
    		logger.debug("[SplittingDbNodeServiceImpl::getPaths] Elapsed: " + (stop - start) + " ms");
    		logger.debug("[SplittingDbNodeServiceImpl::getPaths] END");
    	}
    	return paths;
    }

    private void archiveNode(NodeRef nodeRef, StoreRef archiveStoreRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::archiveNode] BEGIN");

    	try {
    		NodeStatus nodeStatus = nodeDaoService.getNodeStatus(nodeRef, false);
    		Node node = nodeStatus.getNode();
    		Assert.isTrue(!isPart(node), "BUG: archiveNode() on part node!");

    		ChildAssoc primaryParentAssoc = nodeDaoService.getPrimaryParentAssoc(node);

    		// add the aspect
    		Set<QName> aspects = node.getAspects();
    		aspects.add(ContentModel.ASPECT_ARCHIVED);

    		Map<QName, PropertyValue> properties = node.getProperties();

    		PropertyValue archivedByProperty = makePropertyValue(
    				dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_BY),
    				AuthenticationUtil.getCurrentUserName());
    		properties.put(ContentModel.PROP_ARCHIVED_BY, archivedByProperty);

    		PropertyValue archivedDateProperty = makePropertyValue(
    				dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_DATE),
    				new Date());
    		properties.put(ContentModel.PROP_ARCHIVED_DATE, archivedDateProperty);

    		ChildAssociationRef origParentAssocRef = new ChildAssociationRef(primaryParentAssoc.getTypeQName(),
    				getPartsContainerNode(primaryParentAssoc.getParent()).getNodeRef(),
    				primaryParentAssoc.getQname(), nodeRef, true, primaryParentAssoc.getIndex());
    		PropertyValue archivedPrimaryParentNodeRefProperty = makePropertyValue(dictionaryService.getProperty(
    				ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC), origParentAssocRef);
    		properties.put(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC, archivedPrimaryParentNodeRefProperty);

    		PropertyValue originalOwnerProperty = properties.get(ContentModel.PROP_OWNER);
    		PropertyValue originalCreatorProperty = properties.get(ContentModel.PROP_CREATOR);

    		if (originalOwnerProperty != null || originalCreatorProperty != null) {
    			properties.put(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER,
    					originalOwnerProperty != null ? originalOwnerProperty : originalCreatorProperty);
    		}

    		// change the node ownership
    		aspects.add(ContentModel.ASPECT_OWNABLE);
    		PropertyValue newOwnerProperty = makePropertyValue(
    				dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER),
    				AuthenticationUtil.getCurrentUserName());
    		properties.put(ContentModel.PROP_OWNER, newOwnerProperty);

    		// move the node
    		NodeRef archiveStoreRootNodeRef = getRootNode(archiveStoreRef);
    		moveNode(nodeRef, archiveStoreRootNodeRef, ContentModel.ASSOC_CHILDREN,
    				QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedItem"));

    		// the node reference has changed due to the store move
    		nodeRef = node.getNodeRef();

    		// as has the node status
    		nodeStatus = nodeDaoService.getNodeStatus(nodeRef, true);

    		// get the IDs of all the node's primary children, including its own
    		Map<Long, NodeStatus> nodeStatusesById = getNodeHierarchy(nodeStatus, null);

    		// Archive all the associations between the archived nodes and non-archived nodes
    		for (NodeStatus nodeStatusToArchive : nodeStatusesById.values()) {
    			Node nodeToArchive = nodeStatusToArchive.getNode();
    			if (nodeToArchive == null) {
    				continue;
    			}

    			archiveAssocs(nodeToArchive, nodeStatusesById);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::archiveNode] END");
    	}
    }

    /**
     * Performs all the necessary housekeeping involved in changing a node's store.
     * This method cascades down through all the primary children of the node as
     * well.
     *
     * @param node the node whose store is changing
     * @param store the new store for the node
     */
    private void moveNodeToStore(Node node, Store store) {
    	logger.debug("[SplittingDbNodeServiceImpl::moveNodeToStore] BEGIN");

    	try {
    		NodeRef nodeRef = node.getNodeRef();
    		NodeStatus nodeStatus = nodeDaoService.getNodeStatus(nodeRef, true);

    		// get the IDs of all the node's primary children, including its own
    		Map<Long, NodeStatus> nodeStatusesById = getNodeHierarchy(nodeStatus, null);

    		// move each node into the archive store
    		for (NodeStatus oldNodeStatus : nodeStatusesById.values()) {

    			// Backported for CSI from 20071004 HEAD
    			// Check if the target node (node in the store) is already there
    			NodeRef targetStoreNodeRef = new NodeRef(store.getStoreRef(),
    					oldNodeStatus.getKey().getGuid());

    			if (exists(targetStoreNodeRef)) {

    				// It is there already. It must be an archive of an earlier version, so just wipe it out
    				Node archivedNode = getNodeNotNull(targetStoreNodeRef);
    				nodeDaoService.deleteNode(archivedNode, true);

    				// We need to flush here as the node deletion may not take effect before the node creation
    				// is done. As this will only occur during a clash, it is not going to add extra overhead
    				// to the general system performance.
    				nodeDaoService.flush();
    			}

    			Node nodeToMove = oldNodeStatus.getNode();

    			if (isSplitted(nodeToMove)) {
    				for (Node partNode : getPartNodes(nodeToMove)) {
    					partNode.setStore(store);
    				}
    			}

    			NodeRef oldNodeRef = nodeToMove.getNodeRef();
    			nodeToMove.setStore(store);
    			NodeRef newNodeRef = nodeToMove.getNodeRef();

    			// update old status
    			oldNodeStatus.setNode(null);

    			// create the new status
    			NodeStatus newNodeStatus = nodeDaoService.getNodeStatus(newNodeRef, true);
    			newNodeStatus.setNode(nodeToMove);

    			// Record change IDs
    			nodeDaoService.recordChangeId(oldNodeRef);
    			nodeDaoService.recordChangeId(newNodeRef);

    			invokeOnUpdateNode(newNodeRef);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::moveNodeToStore] END");
    	}
    }

    /**
     * Fill the map of all primary children below the given node.
     * The given node will be added to the map and the method is recursive
     * to all primary children.
     *
     * @param nodeStatus the status of the node at the top of the hierarchy
     * @param nodeStatusesById a map of node statuses that will be reused as the return value
     * @return Returns a map of nodes in the hierarchy keyed by their IDs
     */
    private Map<Long, NodeStatus> getNodeHierarchy(NodeStatus nodeStatus,
    		Map<Long, NodeStatus> nodeStatusesById) {
    	logger.debug("[SplittingDbNodeServiceImpl::getNodeHierarchy] BEGIN");

    	try {
    		if (nodeStatusesById == null) {
    			nodeStatusesById = new LinkedHashMap<Long, NodeStatus>(23);

    			// this is the entry into the hierarchy - flush to ensure we are not stale
    			nodeDaoService.flush();
    		}

    		Node node = nodeStatus.getNode();
    		if (node == null) {

    			// the node has already been deleted
    			return nodeStatusesById;
    		}

    		Long nodeId = node.getId();
    		if (nodeStatusesById.containsKey(nodeId)) {

    			// this ID was already added - circular reference
    			if (logger.isWarnEnabled()) {
    				logger.warn("[SplittingDbNodeServiceImpl::getNodeHierarchy] Circular hierarchy found including node: " + nodeId);
    			}
    			return nodeStatusesById;
    		}

    		// add the node to the map
    		nodeStatusesById.put(nodeId, nodeStatus);

    		// recurse into the primary children
    		Collection<NodeStatus> primaryChildNodeStatuses = null;

    		if (isSplitted(node)) {
    			primaryChildNodeStatuses = new ArrayList<NodeStatus>();
    			for (Node part : getPartNodes(node)) {
    				primaryChildNodeStatuses.addAll(nodeDaoService.getPrimaryChildNodeStatuses(part));
    			}
    		} else {
    			primaryChildNodeStatuses = nodeDaoService.getPrimaryChildNodeStatuses(node);
    		}

    		for (NodeStatus primaryChildNodeStatus : primaryChildNodeStatuses) {

    			// cascade into primary associations
    			nodeStatusesById = getNodeHierarchy(primaryChildNodeStatus, nodeStatusesById);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getNodeHierarchy] END");
    	}
    	return nodeStatusesById;
    }

    /**
     * Archive all associations to and from the given node, with the
     * exception of associations to or from nodes in the given map.
     * <p>
     * Primary parent associations are also ignored.
     *
     * @param node the node whose associations must be archived
     * @param nodesById a map of nodes partaking in the archival process
     */
    private void archiveAssocs(Node node, Map<Long, NodeStatus> nodeStatusesById) {
    	logger.debug("[SplittingDbNodeServiceImpl::archiveAssocs] BEGIN");

    	try {
    		Assert.isTrue(!isPart(node), "BUG: archiveAssocs() on part node!");

    		List<ChildAssoc> childAssocsToDelete = new ArrayList<ChildAssoc>(5);

    		// child associations
    		ArrayList<ChildAssociationRef> archivedChildAssocRefs =
    			new ArrayList<ChildAssociationRef>(25);

    		if (isSplitted(node)) {
        		for (ChildAssoc part : getPartAssocs(node)) {
        			for (ChildAssoc assoc : nodeDaoService.getChildAssocs(part.getChild())) {

        				Long relatedNodeId = assoc.getChild().getId();

        				if (nodeStatusesById.containsKey(relatedNodeId)) {
        					// a sibling in the archive process
        					continue;
        				}

        				childAssocsToDelete.add(assoc);

        				// Salvo l'associazione tradotta, il ripristino dello splitting avverra` nella fase di restore
        				ChildAssociationRef assocRef = new ChildAssociationRef(assoc.getTypeQName(), node.getNodeRef(),
        						assoc.getQname(), assoc.getChild().getNodeRef(), assoc.getIsPrimary(), assoc.getIndex());

        				archivedChildAssocRefs.add(assocRef);
        			}
        		}
    		} else {
    			for (ChildAssoc assoc : nodeDaoService.getChildAssocs(node)) {

    				Long relatedNodeId = assoc.getChild().getId();

    				if (nodeStatusesById.containsKey(relatedNodeId)) {

    					// a sibling in the archive process
    					continue;
    				}

    				childAssocsToDelete.add(assoc);
    				archivedChildAssocRefs.add(assoc.getChildAssocRef());
    			}
    		}

    		// parent associations
    		ArrayList<ChildAssociationRef> archivedParentAssocRefs = new ArrayList<ChildAssociationRef>(5);
    		for (ChildAssoc assoc : nodeDaoService.getParentAssocs(node)) {

    			Long relatedNodeId = assoc.getParent().getId();

    			if (nodeStatusesById.containsKey(relatedNodeId)) {

    				// a sibling in the archive process
    				continue;
    			} else if (assoc.getIsPrimary()) {

    				// ignore the primary parent as this is handled more specifically
    				continue;
    			}
    			childAssocsToDelete.add(assoc);

    			// Salvo l'associazione tradotta, il ripristino dello splitting avverra` nella
    			// fase di restore
    			Node realParent = getPartsContainerNode(assoc.getParent());
    			ChildAssociationRef assocRef = new ChildAssociationRef(assoc.getTypeQName(),
    					realParent.getNodeRef(), assoc.getQname(), node.getNodeRef(),
    					assoc.getIsPrimary(), assoc.getIndex());

    			archivedParentAssocRefs.add(assocRef);
    		}
    		List<NodeAssoc> nodeAssocsToDelete = new ArrayList<NodeAssoc>(5);

    		// source associations
    		ArrayList<AssociationRef> archivedSourceAssocRefs = new ArrayList<AssociationRef>(5);
    		for (NodeAssoc assoc : nodeDaoService.getSourceNodeAssocs(node)) {

    			Long relatedNodeId = assoc.getSource().getId();

    			if (nodeStatusesById.containsKey(relatedNodeId)) {
    				continue;	// a sibling in the archive process
    			}
    			nodeAssocsToDelete.add(assoc);
    			archivedSourceAssocRefs.add(assoc.getNodeAssocRef());
    		}

    		// target associations
    		ArrayList<AssociationRef> archivedTargetAssocRefs = new ArrayList<AssociationRef>(5);
    		for (NodeAssoc assoc : nodeDaoService.getTargetNodeAssocs(node)) {

    			Long relatedNodeId = assoc.getTarget().getId();

    			if (nodeStatusesById.containsKey(relatedNodeId)) {
    				continue; 	// a sibling in the archive process
    			}
    			nodeAssocsToDelete.add(assoc);
    			archivedTargetAssocRefs.add(assoc.getNodeAssocRef());
    		}

    		// delete child associations
    		for (ChildAssoc assoc : childAssocsToDelete) {
    			nodeDaoService.deleteChildAssoc(assoc, false);
    		}

    		// delete node associations
    		for (NodeAssoc assoc : nodeAssocsToDelete) {
    			nodeDaoService.deleteNodeAssoc(assoc);
    		}

    		// add archived aspect
    		node.getAspects().add(ContentModel.ASPECT_ARCHIVED_ASSOCS);

    		// set properties
    		Map<QName, PropertyValue> properties = node.getProperties();

    		if (!archivedParentAssocRefs.isEmpty()) {
    			PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_PARENT_ASSOCS);
    			PropertyValue propertyValue = makePropertyValue(propertyDef, archivedParentAssocRefs);
    			properties.put(ContentModel.PROP_ARCHIVED_PARENT_ASSOCS, propertyValue);
    		}

    		if (!archivedChildAssocRefs.isEmpty()) {
    			PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_CHILD_ASSOCS);
    			PropertyValue propertyValue = makePropertyValue(propertyDef, archivedChildAssocRefs);
    			properties.put(ContentModel.PROP_ARCHIVED_CHILD_ASSOCS, propertyValue);
    		}

    		if (!archivedSourceAssocRefs.isEmpty()) {
    			PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);
    			PropertyValue propertyValue = makePropertyValue(propertyDef, archivedSourceAssocRefs);
    			properties.put(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS, propertyValue);
    		}

    		if (!archivedTargetAssocRefs.isEmpty()) {
    			PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);
    			PropertyValue propertyValue = makePropertyValue(propertyDef, archivedTargetAssocRefs);
    			properties.put(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS, propertyValue);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::archiveAssocs] END");
    	}
    }

    public NodeRef getStoreArchiveNode(StoreRef storeRef) {
    	logger.debug("[SplittingDbNodeServiceImpl::getStoreArchiveNode] BEGIN");

    	try {
            storeRef = tenantService.getBaseName(storeRef);
    		final StoreRef archiveStoreRef = storeArchiveMap.getArchiveMap().get(storeRef);

    		return (archiveStoreRef == null) ? null : getRootNode(archiveStoreRef);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::getStoreArchiveNode] END");
    	}
    }

    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName, QName assocQName) {
    	logger.debug("[SplittingDbNodeServiceImpl::restoreNode] BEGIN");
    	NodeRef restoredNodeRef = null;

    	try {
    		NodeStatus archivedNodeStatus = getNodeStatusNotNull(archivedNodeRef);
    		Node archivedNode = archivedNodeStatus.getNode();

    		Assert.isTrue(!isPart(archivedNode), "BUG: restoreNode() on a part node!");

    		Set<QName> aspects = archivedNode.getAspects();
    		Map<QName, PropertyValue> properties = archivedNode.getProperties();

    		// the node must be a top-level archive node
    		if (!aspects.contains(ContentModel.ASPECT_ARCHIVED)) {
    			throw new AlfrescoRuntimeException("The node to archive is not an archive node");
    		}

    		ChildAssociationRef originalPrimaryParentAssocRef = (ChildAssociationRef) makeSerializableValue(
    				dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC),
    				properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC));
    		PropertyValue originalOwnerProperty = properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);

    		// remove the aspect archived aspect

            // remove the archived aspect
            removeAspect(archivedNodeRef, ContentModel.ASPECT_ARCHIVED); // allow policy to fire, e.g. for DictionaryModelType

    		properties.remove(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
    		properties.remove(ContentModel.PROP_ARCHIVED_BY);
    		properties.remove(ContentModel.PROP_ARCHIVED_DATE);
    		properties.remove(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);

    		// restore the original ownership
    		if (originalOwnerProperty != null) {
    			aspects.add(ContentModel.ASPECT_OWNABLE);
    			properties.put(ContentModel.PROP_OWNER, originalOwnerProperty);
    		}

    		if (destinationParentNodeRef == null) {

    			// we must restore to the original location
    			destinationParentNodeRef = originalPrimaryParentAssocRef.getParentRef();
    		}

    		// check the associations
    		if (assocTypeQName == null) {
    			assocTypeQName = originalPrimaryParentAssocRef.getTypeQName();
    		}

    		if (assocQName == null) {
    			assocQName = originalPrimaryParentAssocRef.getQName();
    		}

    		// La gestione dello splitting e` a carico della moveNode()
    		ChildAssociationRef newChildAssocRef = moveNode(archivedNodeRef,
    				destinationParentNodeRef, assocTypeQName, assocQName);

    		archivedNodeRef = newChildAssocRef.getChildRef();
    		archivedNodeStatus = nodeDaoService.getNodeStatus(archivedNodeRef, false);

    		// get the IDs of all the node's primary children, including its own
    		Map<Long, NodeStatus> restoreNodeStatusesById = getNodeHierarchy(archivedNodeStatus, null);

    		// Restore the archived associations, if required
    		for (NodeStatus restoreNodeStatus : restoreNodeStatusesById.values()) {
    			Node restoreNode = restoreNodeStatus.getNode();
    			restoreAssocs(restoreNode);
    		}

    		// the node reference has changed due to the store move
    		restoredNodeRef = archivedNode.getNodeRef();

    		if (logger.isDebugEnabled()) {
    			logger.debug("Restored node: " +
    					"\n   original noderef: " + archivedNodeRef +
    					"\n   restored noderef: " + restoredNodeRef +
    					"\n   new parent: " + destinationParentNodeRef);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::restoreNode] END");
    	}
    	return restoredNodeRef;
    }

    @SuppressWarnings("unchecked")
	private void restoreAssocs(Node node) {
    	logger.debug("[SplittingDbNodeServiceImpl::restoreAssocs] BEGIN");

    	try {
    		NodeRef nodeRef = node.getNodeRef();

    		// set properties
    		Map<QName, PropertyValue> properties = node.getProperties();

    		// restore parent associations
    		Collection<ChildAssociationRef> parentAssocRefs = (Collection<ChildAssociationRef>) getProperty(
    				nodeRef, ContentModel.PROP_ARCHIVED_PARENT_ASSOCS);

    		if (parentAssocRefs != null) {

    			for (ChildAssociationRef assocRef : parentAssocRefs) {

    				NodeRef parentNodeRef = assocRef.getParentRef();

    				if (!exists(parentNodeRef)) {
    					continue;	// Il nodo padre non esiste piu`
    				}
    				Node parentNode = getNodeNotNull(parentNodeRef);
    				QName assocQName = assocRef.getQName();

    				Node destinationNode = null;

    				destinationNode = (isSplitted(parentNode))
    						? getPartByChildAssocName(parentNode, assocQName.getLocalName())
    						: parentNode;

    				if (destinationNode == null) {	// Il nodo e` splittato e la parte di destinazione non e` stata trovata
    					destinationNode = createMissingPart(parentNode, assocQName.getLocalName());
    				}

    				// get the name to use for the unique child check
    				QName assocTypeQName = assocRef.getTypeQName();
    				nodeDaoService.newChildAssoc(destinationNode, node, assocRef.isPrimary(), assocTypeQName, assocQName);
    			}
    			properties.remove(ContentModel.PROP_ARCHIVED_PARENT_ASSOCS);
    		}

    		// make sure that the node name uniqueness is enforced
    		setChildUniqueName(node);

    		// restore child associations
    		Collection<ChildAssociationRef> childAssocRefs = (Collection<ChildAssociationRef>) getProperty(
    				nodeRef, ContentModel.PROP_ARCHIVED_CHILD_ASSOCS);

    		if (childAssocRefs != null) {

    			for (ChildAssociationRef assocRef : childAssocRefs) {

    				NodeRef childNodeRef = assocRef.getChildRef();

    				if (!exists(childNodeRef)) {
    					continue;	// Il nodo figlio non esiste piu`
    				}
    				Node childNode = getNodeNotNull(childNodeRef);

    				// get the name to use for the unique child check
    				QName assocTypeQName = assocRef.getTypeQName();
    				QName assocQName = assocRef.getQName();

    				Node destinationNode = null;

    				destinationNode = (isSplitted(node))
    						? getPartByChildAssocName(node, assocQName.getLocalName())
    						: node;

    				if (destinationNode == null) {	// Il nodo e` splittato e la parte di destinazione non e` stata trovata
    					destinationNode = createMissingPart(node, assocQName.getLocalName());
    				}

    				nodeDaoService.newChildAssoc(destinationNode, childNode, assocRef.isPrimary(), assocTypeQName, assocQName);

    				// ensure that the name uniqueness is enforced for the child node
    				setChildUniqueName(childNode);
    			}
    			properties.remove(ContentModel.PROP_ARCHIVED_CHILD_ASSOCS);
    		}

    		// restore source associations
    		Collection<AssociationRef> sourceAssocRefs = (Collection<AssociationRef>) getProperty(
    				nodeRef, ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);

    		if (sourceAssocRefs != null) {

    			for (AssociationRef assocRef : sourceAssocRefs) {

    				NodeRef sourceNodeRef = assocRef.getSourceRef();

    				if (!exists(sourceNodeRef)) {
    					continue;
    				}
    				Node sourceNode = getNodeNotNull(sourceNodeRef);
    				nodeDaoService.newNodeAssoc(sourceNode, node, assocRef.getTypeQName());
    			}
    			properties.remove(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);
    		}

    		// restore target associations
    		Collection<AssociationRef> targetAssocRefs = (Collection<AssociationRef>) getProperty(
    				nodeRef, ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);

    		if (targetAssocRefs != null) {

    			for (AssociationRef assocRef : targetAssocRefs) {

    				NodeRef targetNodeRef = assocRef.getTargetRef();

    				if (!exists(targetNodeRef)) {
    					continue;
    				}
    				Node targetNode = getNodeNotNull(targetNodeRef);
    				nodeDaoService.newNodeAssoc(node, targetNode, assocRef.getTypeQName());
    			}
    			properties.remove(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);
    		}

    		// remove the aspect
    		node.getAspects().remove(ContentModel.ASPECT_ARCHIVED_ASSOCS);
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::restoreAssocs] END");
    	}
    }

    /**
     * Checks the dictionary's definition of the association to assign a unique name to the child node.
     *
     * @param assocTypeQName the type of the child association
     * @param childNode the child node being added.  The name will be extracted from it, if necessary.
     * @return Returns the value to be put on the child association for uniqueness, or null if
     */
    private void setChildUniqueName(Node childNode) {
    	logger.debug("[SplittingDbNodeServiceImpl::setChildUniqueName] BEGIN");

    	try {
    		Map<QName, PropertyValue> properties = childNode.getProperties();
    		PropertyValue nameValue = properties.get(ContentModel.PROP_NAME);
    		String useName = null;

    		useName = (nameValue == null)
    				? childNode.getUuid()
    				: (String) nameValue.getValue(DataTypeDefinition.TEXT);

    		// get all the parent assocs
    		Collection<ChildAssoc> parentAssocs = nodeDaoService.getParentAssocs(childNode);
    		for (ChildAssoc assoc : parentAssocs) {
    			/*
    			 * TODO: se il nodo padre e` splittato il controllo dovrebbe essere propagato anche alle
    			 * altre parti per evitare che nel complesso ci possano essere figli duplicati.
    			 *
    			 * Questa modifica potrebbe richiedere una modifica alla API del nodeServiceDao.
    			 */
    			QName assocTypeQName = assoc.getTypeQName();
    			AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
    			if (!assocDef.isChild()) {
    				throw new DataIntegrityViolationException("Child association has non-child type: " +
    						assoc.getId());
    			}

    			ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;

    			nodeDaoService.setChildNameUnique(assoc,
    					(childAssocDef.getDuplicateChildNamesAllowed()) ? null : useName);
    		}

    		if (logger.isDebugEnabled()) {
    			logger.debug("[SplittingDbNodeServiceImpl::setChildUniqueName] Unique name set for all " + parentAssocs.size() +
    					" parent associations: \n" +
    					"   name: " + useName);
    		}
    	} finally {
    		logger.debug("[SplittingDbNodeServiceImpl::setChildUniqueName] END");
    	}
    }

	private Node getPartsContainerNode(Node node) {
		long start = System.currentTimeMillis();

		Node realNode = (isPart(node))
				? nodeDaoService.getPrimaryParentAssoc(node).getParent()
				: node;

		if (logger.isDebugEnabled()) {
    		long end = System.currentTimeMillis();
			logger.debug("[SplittingDbNodeServiceImpl::getPartsContainerNode] Get container:" +
					"\n\t\tI: " + node + "\n\t\tO: " + realNode);
		    logger.debug("[SplittingDbNodeServiceImpl::getPartsContainerNode] Elapsed: " + (end - start) + " ms");
		}
		return realNode;
	}

	private boolean isSplitted(Node node) {
		logger.debug("[SplittingDbNodeServiceImpl::isSplitted] BEGIN");

		try {
			return node.getAspects().contains(EcmEngineModelConstants.ASPECT_SPLITTED);
		} finally {
			logger.debug("[SplittingDbNodeServiceImpl::isSplitted] END");
		}
	}

	private boolean isPart(Node node) {
		logger.debug("[SplittingDbNodeServiceImpl::isPart] BEGIN");

		try {
			return node.getAspects().contains(EcmEngineModelConstants.ASPECT_PART);
		} finally {
			logger.debug("[SplittingDbNodeServiceImpl::isPart] END");
		}
	}

    private String getPartName(String contentName) {

    	long start = System.currentTimeMillis();
    	char [] chars = contentName.toCharArray();
    	int sum = 0;

    	for (int i = 0; i < (chars.length / 2); i++) {
    		sum += chars[i];
    	}
    	sum = sum << 8;
    	for (int j = (chars.length / 2); j < chars.length; j++) {
    		sum += (j % 2 == 0) ? (chars[j] / 2) : chars[j];
    	}

    	final String partName = "part" + (sum % partsCount);
    	long end = System.currentTimeMillis();

    	if (logger.isDebugEnabled()) {
    		logger.debug("[SplittingDbNodeServiceImpl::getPartName] " +
    				"I: " + contentName + " -> O: " + partName);
    	    logger.debug("[SplittingDbNodeServiceImpl::getPartName] Elapsed: " + (end - start) + " ms");
    	}

    	return partName;
    }

    private List<Node> getPartNodes(Node container) {
    	// Questo metodo non deve essere richiamato su nodi che non siano splittati
    	Assert.isTrue(isSplitted(container), "BUG: getPartNodes() on a non-splitted node!");

    	final long start = System.currentTimeMillis();
    	final Collection<ChildAssoc> parts = nodeDaoService.getChildAssocs(container);
    	List<Node> results = new ArrayList<Node>(parts.size());

    	// Tutti i figli di un nodo splittato sono "parti"
    	for (ChildAssoc part : parts) {
    		results.add(part.getChild());
    	}
    	final long stop = System.currentTimeMillis();

    	if (logger.isDebugEnabled()) {
    		logger.debug("[SplittingDbNodeServiceImpl::getPartNodes] N: " +
    				container.getNodeRef() + " Parts: " + results.size());
    	    logger.debug("[SplittingDbNodeServiceImpl::getPartNodes] Elapsed: " + (stop - start) + " ms");
    	}

    	return results;
    }

    private List<ChildAssoc> getPartAssocs(Node container) {
    	// Questo metodo non deve essere richiamato su nodi che non siano splittati
    	Assert.isTrue(isSplitted(container), "BUG: getPartAssocs() on a non-splitted node!");

    	final long start = System.currentTimeMillis();
    	final Collection<ChildAssoc> parts = nodeDaoService.getChildAssocs(container);
    	List<ChildAssoc> results = new ArrayList<ChildAssoc>(parts.size());

    	// Tutti i figli di un nodo splittato sono "parti"
    	results.addAll(parts);

    	final long stop = System.currentTimeMillis();

    	if (logger.isDebugEnabled()) {
    		logger.debug("[SplittingDbNodeServiceImpl::getPartAssocs] Container: " +
    				container.getNodeRef() + " Parts: " + results.size());
    	    logger.debug("[SplittingDbNodeServiceImpl::getPartAssocs] Elapsed: " + (stop - start) + " ms");
    	}

    	return results;
    }

    private Node getPartByChildAssocName(Node container, String localName) {
    	logger.debug("[SplittingDbNodeServiceImpl::getPartByChildName] BEGIN");
    	final long start = System.currentTimeMillis();
    	final QName partQName = QName.createQName(EcmEngineModelConstants.ECMENGINE_SYS_MODEL_URI,
    			getPartName(localName));
    	Node part = null;

    	try {
    		Collection<ChildAssociationRef> partAssocs = nodeDaoService.getChildAssocRefs(
    				container, partQName);

    		for (ChildAssociationRef partAssoc : partAssocs) {
    			part = getNodeNotNull(partAssoc.getChildRef());
    			break;	// Consideriamo un solo risultato - le parti devono avere nomi distinti
    		}
    	} catch (InvalidNodeRefException e) {
    		// Ignore
    	} finally {
    		if (logger.isDebugEnabled()) {
        		final long stop = System.currentTimeMillis();
    			logger.debug("[SplittingDbNodeServiceImpl::getPartByChildName] Name: " + localName +
    					" Ref: " + ((part != null) ? part.getNodeRef() : part));
        		logger.debug("[SplittingDbNodeServiceImpl::getPartByChildName] Elapsed: " +	(stop - start) + " ms");
        		logger.debug("[SplittingDbNodeServiceImpl::getPartByChildName] END");
    		}
    	}
    	return part; // Se la parte non e` stata trovata e` null
    }

    private Node createMissingPart(Node parent, String childLocalName) {
		logger.debug("[SplittingDbNodeServiceImpl::createMissingPart] BEGIN");
    	final long start = System.currentTimeMillis();
    	final QName partQName = QName.createQName(EcmEngineModelConstants.ECMENGINE_SYS_MODEL_URI,
    			getPartName(childLocalName));
		ChildAssoc part = null;
    	Node child = null;
    	Store store = null;

    	try {
    		store = nodeDaoService.getStore(parent.getNodeRef().getStoreRef().getProtocol(),
        			parent.getNodeRef().getStoreRef().getIdentifier());

    		HashMap<QName, Serializable> props = new HashMap<QName, Serializable>(1);

    		PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_NAME);
            PropertyValue nameValue = makePropertyValue(propertyDef, partQName.getLocalName());

            if (logger.isDebugEnabled()) {
            	logger.debug("[SplittingDbNodeServiceImpl::createMissingPart] Creating missing part " + partQName);
            }

            String newId = generateGuid(props);

            child = nodeDaoService.newNode(store, newId, parent.getTypeQName());

            child.getAspects().add(EcmEngineModelConstants.ASPECT_PART);
            child.getProperties().put(ContentModel.PROP_NAME, nameValue);

            // Create the association
            part = nodeDaoService.newChildAssoc(parent, child, true,
            		EcmEngineModelConstants.ASSOC_PARTS, partQName);

    	} finally {
    		final long stop = System.currentTimeMillis();
    		logger.debug("[SplittingDbNodeServiceImpl::createMissingPart] Elapsed: " + (stop - start) + " ms");
    		logger.debug("[SplittingDbNodeServiceImpl::createMissingPart] END");
    	}

    	return part.getChild();
    }

    private void splitNode(Node node) {
    	logger.debug("[SplittingDbNodeServiceImpl::splitNode] BEGIN");
    	Assert.isTrue(!isPart(node), "BUG: splitNode() on part node!");

    	final long start = System.currentTimeMillis();
    	int moved = 0;
    	int parts = 0;

    	try {
    		Collection<ChildAssoc> curChildren = nodeDaoService.getChildAssocs(node);

    		for (ChildAssoc child : curChildren) {

    			ChildAssociationRef oldAssocRef = child.getChildAssocRef();
    			QName assocTypeQName = child.getTypeQName();
    			QName assocQName = child.getQname();
    			NodeRef childRef = child.getChild().getNodeRef();
    			Node childNode = child.getChild();
    			Node part = getPartByChildAssocName(node, assocQName.getLocalName());

    			if (part == null) {
    				part = createMissingPart(node, assocQName.getLocalName());
    				parts++;
    			}

    			if (logger.isDebugEnabled()) {
    				logger.debug("[SplittingDbNodeServiceImpl::splitNode] Moving child " +
    						childRef + " to part " + part.getNodeRef());
    			}

    			// BEGIN SPOSTAMENTO

    			invokeBeforeDeleteChildAssociation(oldAssocRef);
    			invokeBeforeCreateChildAssociation(node.getNodeRef(), childRef, assocTypeQName,
    					assocQName, false);

    			// remove the child association from the old parent
    			// don't cascade as we will still need the node afterwards
    			nodeDaoService.deleteChildAssoc(child, false);

    			// create a new association
    			ChildAssoc newAssoc = nodeDaoService.newChildAssoc(part,	// Nodo o parte
    					childNode, true, assocTypeQName, assocQName);
    			setChildUniqueName(childNode);	// ensure uniqueness

    			ChildAssociationRef newAssocRef = new ChildAssociationRef(newAssoc.getTypeQName(),
    					node.getNodeRef(), assocQName, childRef, newAssoc.getIsPrimary(),
    					newAssoc.getIndex());

    			// check that no cyclic relationships have been created
    			getPaths(childRef, false);

    			// invoke policy behavior
    			invokeOnCreateChildAssociation(newAssocRef, false);
    			invokeOnDeleteChildAssociation(oldAssocRef);

    			invokeOnMoveNode(oldAssocRef, newAssocRef);

    			// update the node status
    			nodeDaoService.recordChangeId(childRef);

    			// END SPOSTAMENTO

    			moved++;

    			if (logger.isDebugEnabled()) {
    				logger.debug("[SplittingDbNodeServiceImpl::splitNode] Move completed.");
    			}
    		}
    		if (logger.isDebugEnabled()) {
    			logger.debug("[SplittingDbNodeServiceImpl::splitNode] Moved " + moved + " nodes to " +
    					parts + " parts.");
    		}
    	} finally {
    		final long stop = System.currentTimeMillis();
    		logger.debug("[SplittingDbNodeServiceImpl::splitNode] Elapsed: " +
    				(stop - start) + " ms");
    		logger.debug("[SplittingDbNodeServiceImpl::splitNode] END");
    	}
    }

	public boolean isPartRef(NodeRef nodeRef) {
		logger.debug("[SplittingDbNodeServiceImpl::isPartRef] BEGIN");

		try {
			final Node node = getNodeNotNull(nodeRef);
			return isPart(node);
		} finally {
			logger.debug("[SplittingDbNodeServiceImpl::isPartRef] END");
		}
	}

	public boolean isSplittedRef(NodeRef nodeRef) {
		logger.debug("[SplittingDbNodeServiceImpl::isSplittedRef] BEGIN");

		try {
			final Node node = getNodeNotNull(nodeRef);
			return isSplitted(node);
		} finally {
			logger.debug("[SplittingDbNodeServiceImpl::isSplittedRef] END");
		}
	}

	public ChildAssociationRef translateChildAssociationRef(
			ChildAssociationRef internalChildAssoc) {
		logger.debug("[SplittingDbNodeServiceImpl::translateChildAssociationRef] BEGIN");
		ChildAssociationRef externalChildAssoc = null;
		NodeRef translatedChild = null;

		final long start = System.currentTimeMillis();
		try {
			try {
				translatedChild = translateNodeRef(internalChildAssoc.getChildRef());
			} catch (InvalidNodeRefException e) {
				/*
				 * Se questo succede probabilmente stiamo gestendo una delete. A questo punto
				 * quindi il nodo figlio non esiste piu` ma possiamo assumere che il nodeRef originale
				 * sia utilizzabile.
				 */
				translatedChild = internalChildAssoc.getChildRef();
                /*
                 * MB: Se sono su un tenant, occorre tradurre il child.
                 *     La modifica e' stata introdotta, dopo la verifica che, la DELETE su
                 *     tenant, cadeva in questo ramo, creando, di fatto, un NodeRef puntato al
                 *     repository principale e non al tenant. Questo e' sbagliato e provocava
                 *     un'aggiornamento degli indici Lucene con un percorso sbagliato
                 *     ex
                 *       workspace://SpacesStore/1381fb1b-081e-11de-84be-134d9b875fad
                 *     al posto di
                 *       workspace://@mactaicindex@SpacesStore/1381fb1b-081e-11de-84be-134d9b875fad
                 */
                translatedChild = tenantService.getName(translatedChild);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("[SplittingDbNodeServiceImpl::translateChildAssociationRef] " +
						"Input: " + internalChildAssoc);
			}
			externalChildAssoc = new ChildAssociationRef(
					internalChildAssoc.getTypeQName(),
					translateNodeRef(internalChildAssoc.getParentRef()),
					internalChildAssoc.getQName(),
					translatedChild,
					internalChildAssoc.isPrimary(),
					internalChildAssoc.getNthSibling());

			if (logger.isDebugEnabled()) {
				logger.debug("[SplittingDbNodeServiceImpl::translateChildAssociationRef] " +
						"Output: " + externalChildAssoc);
			}
		} finally {
			final long stop = System.currentTimeMillis();
			logger.debug("[SplittingDbNodeServiceImpl::translateChildAssociationRef] END" +
					 " [" + (stop - start) + " ms]");
		}

		return externalChildAssoc;
	}

	public NodeRef translateNodeRef(NodeRef internalNode) {
		logger.debug("[SplittingDbNodeServiceImpl::translateNodeRef] BEGIN");

		try {
			final Node node = getNodeNotNull(internalNode);
			return getPartsContainerNode(node).getNodeRef();
		} finally {
			logger.debug("[SplittingDbNodeServiceImpl::translateNodeRef] END");
		}
	}
}

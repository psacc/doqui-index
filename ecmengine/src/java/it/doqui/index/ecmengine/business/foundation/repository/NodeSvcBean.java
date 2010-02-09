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

package it.doqui.index.ecmengine.business.foundation.repository;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.NodeRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

public class NodeSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 2664522005131086983L;

    /* TODO: servizi non ancora esportati e che RICHIEDONO la gestione della AccessDeniedException
     *
     *	org.alfresco.service.cmr.repository.NodeService.getStores=AFTER_ACL_NODE.sys:base.ReadProperties
     *	org.alfresco.service.cmr.repository.NodeService.createStore=ACL_METHOD.ROLE_ADMINISTRATOR
     *	org.alfresco.service.cmr.repository.NodeService.getNodeStatus=ACL_NODE.0.sys:base.ReadProperties
     *	org.alfresco.service.cmr.repository.NodeService.getRootNode=ACL_NODE.0.sys:base.ReadProperties
     *	org.alfresco.service.cmr.repository.NodeService.setChildAssociationIndex=ACL_PARENT.0.sys:base.WriteProperties
     *	org.alfresco.service.cmr.repository.NodeService.setType=ACL_NODE.0.sys:base.WriteProperties
     *	org.alfresco.service.cmr.repository.NodeService.deleteNode=ACL_NODE.0.sys:base.DeleteNode
     *	org.alfresco.service.cmr.repository.NodeService.setProperty=ACL_NODE.0.sys:base.WriteProperties
     *	org.alfresco.service.cmr.repository.NodeService.removeProperty=ACL_NODE.0.sys:base.WriteProperties
     *	org.alfresco.service.cmr.repository.NodeService.getChildByName=ACL_NODE.0.sys:base.ReadChildren,AFTER_ACL_NODE.sys:base.ReadProperties
     *	org.alfresco.service.cmr.repository.NodeService.getStoreArchiveNode=ACL_NODE.0.sys:base.Read
     *	org.alfresco.service.cmr.repository.NodeService.restoreNode=ACL_NODE.0.sys:base.DeleteNode,ACL_NODE.1.sys:base.CreateChildren
     */

	public ChildAssociationRef createNode(NodeRef nodeRef, QName assocTypeQName,
			QName assocQName, QName typeQName, Map<QName, Serializable> props)
			throws NodeRuntimeException {
		ChildAssociationRef assocRef = null;
		logger.debug("[NodeSvcBean::createNode] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::createNode] Creating new node with parent: " + nodeRef.getId());
		    }

			assocRef = serviceRegistry.getNodeService().createNode(nodeRef, assocTypeQName, assocQName, typeQName, props);
    	} catch (Exception e) {
			handleNodeServiceException("createNode", e);
		} finally {
			logger.debug("[NodeSvcBean::createNode] END");
		}
		return assocRef;
	}

	public void deleteNode(NodeRef nodeRef) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::deleteNode] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::deleteNode] Deleting node with uid: " + nodeRef.getId());
		    }

			serviceRegistry.getNodeService().deleteNode(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("deleteNode", e);
		} finally {
			logger.debug("[NodeSvcBean::deleteNode] END");
		}
	}

	public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> props)
	throws NodeRuntimeException {

		logger.debug("[NodeSvcBean::addAspect] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::addAspect] Adding aspect \"" + aspectTypeQName + "\" to node: " + nodeRef);
		    }

			serviceRegistry.getNodeService().addAspect(nodeRef, aspectTypeQName, props);
    	} catch (Exception e) {
			handleNodeServiceException("addAspect", e);
		} finally {
			logger.debug("[NodeSvcBean::addAspect] END");
		}
	}

    public Set<QName> getAspects(NodeRef nodeRef) throws NodeRuntimeException {
    	Set<QName> aspects = null;
    	logger.debug("[NodeSvcBean::getAspects] BEGIN");

    	try {
    	  	if (logger.isDebugEnabled()) {
        		logger.debug("[NodeSvcBean::getAspects] Retrieving aspects for node: " + nodeRef.getId());
		    }

    		aspects = serviceRegistry.getNodeService().getAspects(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("getAspects", e);
    	} finally {
    		logger.debug("[NodeSvcBean::getAspects] END");
    	}
    	return aspects;
    }

    public boolean hasAspect(NodeRef nodeRef, QName aspectRef) throws NodeRuntimeException {
    	boolean result = false;
    	logger.debug("[NodeSvcBean::hasAspect] BEGIN");

    	try {
    	  	if (logger.isDebugEnabled()) {
        		logger.debug("[NodeSvcBean::hasAspect] Checking if aspect \"" + aspectRef +
    				"\" is set on node: " + nodeRef.getId());
		    }

    		result = serviceRegistry.getNodeService().hasAspect(nodeRef, aspectRef);
    	} catch (Exception e) {
			handleNodeServiceException("hasAspect", e);
    	} finally {
    		logger.debug("[NodeSvcBean::hasAspect] END");
    	}
    	return result;
    }

    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
    	throws NodeRuntimeException {
    	logger.debug("[NodeSvcBean::removeAspect] BEGIN");

    	try {
    	  	if (logger.isDebugEnabled()) {
        		logger.debug("[NodeSvcBean::removeAspect] Removing aspect \"" + aspectTypeQName +
    				"\" from node: " + nodeRef.getId());
		    }

    		serviceRegistry.getNodeService().removeAspect(nodeRef, aspectTypeQName);
    	} catch (Exception e) {
			handleNodeServiceException("removeAspect", e);
    	} finally {
    		logger.debug("[NodeSvcBean::removeAspect] END");
    	}
    }

	public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws NodeRuntimeException {
		Map<QName,Serializable> props = null;
		logger.debug("[NodeSvcBean::getProperties] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getProperties] Retrieving properties for node: " + nodeRef.getId());
		    }

			props = serviceRegistry.getNodeService().getProperties(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("getProperties", e);
		} finally {
			logger.debug("[NodeSvcBean::getProperties] END");
		}

		return (props != null) ? props : Collections.<QName, Serializable>emptyMap();
	}

    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws NodeRuntimeException {
    	logger.debug("[NodeSvcBean::setProperties] BEGIN");
		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::setProperties] Updating properties for node: " + nodeRef.getId());
		    }

			serviceRegistry.getNodeService().setProperties(nodeRef, properties);
    	} catch (Exception e) {
			handleNodeServiceException("setProperties", e);
		} finally {
			logger.debug("[NodeSvcBean::setProperties] END");
		}
    }

	public ChildAssociationRef moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName, QName assocQName)
	throws NodeRuntimeException {
		ChildAssociationRef assocRef = null;
		logger.debug("[NodeSvcBean::moveNode] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::moveNode] Moving node \"" + nodeToMoveRef.getId() +
					"\" to new parent: " + newParentRef.getId());
		    }

			assocRef = serviceRegistry.getNodeService().moveNode(nodeToMoveRef, newParentRef, assocTypeQName, assocQName);
    	} catch (Exception e) {
			handleNodeServiceException("moveNode", e);
		} finally {
			logger.debug("[NodeSvcBean::moveNode] END");
		}
		return assocRef;
	}

	public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName assocQName)
	throws NodeRuntimeException {
		ChildAssociationRef assocRef = null;
		logger.debug("[NodeSvcBean::addChild] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::addChild] Adding child \"" + childRef.getId() +
					"\" to parent: " + parentRef.getId());
		    }

			assocRef = serviceRegistry.getNodeService().addChild(parentRef, childRef, assocTypeQName, assocQName);
    	} catch (Exception e) {
			handleNodeServiceException("addChild", e);
		} finally {
			logger.debug("[NodeSvcBean::addChild] END");
		}
		return assocRef;
	}

	public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
			throws NodeRuntimeException {
		AssociationRef assocRef = null;
		logger.debug("[NodeSvcBean::createAssociation] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::createAssociation] Create association between source \"" + sourceRef.getId() +
					"\" and target: " + targetRef.getId());
		    }

			assocRef = serviceRegistry.getNodeService().createAssociation(sourceRef, targetRef, assocTypeQName);
    	} catch (Exception e) {
			handleNodeServiceException("createAssociation", e);
		} finally {
			logger.debug("[NodeSvcBean::createAssociation] END");
		}
		return assocRef;
	}

	public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws NodeRuntimeException {
		ChildAssociationRef assocRef = null;
		logger.debug("[NodeSvcBean::getPrimaryParent] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getPrimaryParent] " +
					"Retrieving primary parent for node: " + nodeRef.getId());
		    }

			assocRef = serviceRegistry.getNodeService().getPrimaryParent(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("getPrimaryParent", e);
		} finally {
			logger.debug("[NodeSvcBean::getPrimaryParent] END");
		}
		return assocRef;
	}

    public boolean removeChildAssociation(ChildAssociationRef childAssocRef)
    throws NodeRuntimeException {
    	logger.debug("[NodeSvcBean::removeChildAssociation] BEGIN");
    	boolean assocExist=false;
    	try {
    		assocExist=serviceRegistry.getNodeService().removeChildAssociation(childAssocRef);

    	  	if (logger.isDebugEnabled()) {
        		logger.debug("[NodeSvcBean::removeChildAssociation] " + "Removing child association \""
    				+ childAssocRef.getQName()+"\"");
		    }
    	} catch (Exception e) {
			handleNodeServiceException("removeChildAssociation", e);
    	} finally {
    		logger.debug("[NodeSvcBean::removeChildAssociation] END");
    	}
    	return assocExist;
    }

    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef)
    	throws NodeRuntimeException {
    	logger.debug("[NodeSvcBean::removeSecondaryChildAssociation] BEGIN");
    	boolean assocExist=false;
    	try {
    		assocExist=serviceRegistry.getNodeService().removeSeconaryChildAssociation(childAssocRef);

    	  	if (logger.isDebugEnabled()) {
        		logger.debug("[NodeSvcBean::removeSecondaryChildAssociation] Removing secondary child association \""
    				+ childAssocRef.getQName() + "\"");
		    }
    	} catch (Exception e) {
			handleNodeServiceException("removeSecondaryChildAssociation", e);
    	} finally {
    		logger.debug("[NodeSvcBean::removeSecondaryChildAssociation] END");
    	}
    	return assocExist;
    }

	public void removeChild(NodeRef parentRef, NodeRef childRef) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::removeChild] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::removeChild] Removing child \"" + childRef.getId() + "\" from parent: " + parentRef.getId());
		    }

			serviceRegistry.getNodeService().removeChild(parentRef, childRef);
    	} catch (Exception e) {
			handleNodeServiceException("removeChild", e);
		} finally {
			logger.debug("[NodeSvcBean::removeChild] END");
		}
	}

	public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
	throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::removeAssociation] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::removeAssociation] Removing association between source \"" + sourceRef.getId() +
					"\" and target: " + targetRef.getId());
		    }

			serviceRegistry.getNodeService().removeAssociation(sourceRef, targetRef, assocTypeQName);
    	} catch (Exception e) {
			handleNodeServiceException("removeAssociation", e);
		} finally {
			logger.debug("[NodeSvcBean::removeAssociation] END");
		}
	}

	public QName getType(NodeRef nodeRef) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::getType] BEGIN");
		QName type = null;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getType] Retrieving type for node: " + nodeRef.getId());
		    }

			type = serviceRegistry.getNodeService().getType(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("getType", e);
		} finally {
			logger.debug("[NodeSvcBean::getType] END");
		}

		return type;
	}

	public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef)
	throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::getChildAssocs] BEGIN");
		List<ChildAssociationRef> childAssociations = null;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getChildAssocs] Retrieving child assocs for node: " + nodeRef.getId());
		    }

			childAssociations = serviceRegistry.getNodeService().getChildAssocs(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("getChildAssocs", e);
		} finally {
			logger.debug("[NodeSvcBean::getChildAssocs] END");
		}
		return childAssociations;
	}

	public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef)
	throws NodeRuntimeException  {
		logger.debug("[NodeSvcBean::getParentAssocs] BEGIN");
		List<ChildAssociationRef> parentAssociations = null;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getParentAssocs] Retrieving parent assocs for node: " + nodeRef.getId());
		    }

			parentAssociations = serviceRegistry.getNodeService().getParentAssocs(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("getParentAssocs", e);
		} finally {
			logger.debug("[NodeSvcBean::getParentAssocs] END");
		}
		return parentAssociations;
	}

	public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
		throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::getTargetAssocs] BEGIN");
		List<AssociationRef> assocs = null;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getTargetAssocs] Retrieving target assocs for node: " +
					sourceRef.getId() + " [Pattern: " + qnamePattern + "]");
		    }

			assocs = serviceRegistry.getNodeService().getTargetAssocs(sourceRef, qnamePattern);
    	} catch (Exception e) {
			handleNodeServiceException("getTargetAssocs", e);
		} finally {
			logger.debug("[NodeSvcBean::getTargetAssocs] END");
		}
		return assocs;
	}

	public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
		throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::getSourceAssocs] BEGIN");
		List<AssociationRef> assocs = null;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getSourceAssocs] Retrieving source assocs for node: " +
					targetRef.getId() + " [Pattern: " + qnamePattern + "]");
		    }

			assocs = serviceRegistry.getNodeService().getSourceAssocs(targetRef, qnamePattern);
    	} catch (Exception e) {
			handleNodeServiceException("getSourceAssocs", e);
		} finally {
			logger.debug("[NodeSvcBean::getSourceAssocs] END");
		}
		return assocs;
	}

	public boolean exists(NodeRef nodeRef) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::exists] BEGIN");
		boolean exists = false;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::exists] Exists the node: " + nodeRef.getId());
		    }

			exists = serviceRegistry.getNodeService().exists(nodeRef);
    	} catch (Exception e) {
			handleNodeServiceException("exists", e);
		} finally {
			logger.debug("[NodeSvcBean::exists] END");
		}
		return exists;
	}

	public boolean exists(StoreRef storeRef) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::exists] BEGIN");
		boolean exists = false;

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::exists] Store: " + storeRef.getIdentifier());
		    }

			exists = serviceRegistry.getNodeService().exists(storeRef);
    	} catch (Exception e) {
			handleNodeServiceException("exists", e);
		} finally {
			logger.debug("[NodeSvcBean::exists] END");
		}
		return exists;
	}

	public Serializable getProperty(NodeRef nodeRef, QName propertyQName) throws NodeRuntimeException {
		Serializable value = null;
		logger.debug("[NodeSvcBean::getProperty] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::getProperty] Retrieving property '" + propertyQName + "' for node: " + nodeRef.getId());
		    }

			value = serviceRegistry.getNodeService().getProperty(nodeRef, propertyQName);

    	  	if (logger.isDebugEnabled()) {
			    logger.debug("[NodeSvcBean::getProperty] Value: " + value);
		    }
    	} catch (Exception e) {
			handleNodeServiceException("getProperty", e);
		} finally {
			logger.debug("[NodeSvcBean::getProperty] END");
		}
		return value;
	}

	public void setProperty(NodeRef nodeRef, QName propertyQName, Serializable value) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::setProperty] BEGIN");

		try {
    	  	if (logger.isDebugEnabled()) {
    			logger.debug("[NodeSvcBean::setProperty] Retrieving property '" + propertyQName + "' for node: " + nodeRef.getId());
		    }

			serviceRegistry.getNodeService().setProperty(nodeRef, propertyQName, value);
			logger.debug("[NodeSvcBean::setProperty] Value: " + value);
    	} catch (Exception e) {
			handleNodeServiceException("setProperty", e);
		} finally {
			logger.debug("[NodeSvcBean::setProperty] END");
		}
	}

	public Path getPath(NodeRef nodeRef) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::getPath] BEGIN");
		Path result = null;

		try {
			result = serviceRegistry.getNodeService().getPath(nodeRef);
		} catch (Exception e) {
			handleNodeServiceException("getPath", e);
		} finally {
			logger.debug("[NodeSvcBean::getPath] END");
		}
		return result;
	}

	public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws NodeRuntimeException {
		logger.debug("[NodeSvcBean::getPaths] BEGIN");
		List<Path> results = null;

		try {
			results = serviceRegistry.getNodeService().getPaths(nodeRef, primaryOnly);
		} catch (Exception e) {
			handleNodeServiceException("getPaths", e);
		} finally {
			logger.debug("[NodeSvcBean::getPaths] END");
		}
		return results;
	}

	private void handleNodeServiceException(String methodName, Throwable e) throws NodeRuntimeException {
		logger.warn("[NodeSvcBean::handleNodeServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new NodeRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new NodeRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new NodeRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof InvalidTypeException) {
			throw new NodeRuntimeException(FoundationErrorCodes.INVALID_TYPE_ERROR);
		} else if (e instanceof InvalidAspectException) {
			throw new NodeRuntimeException(FoundationErrorCodes.INVALID_ASPECT_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new NodeRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new NodeRuntimeException(FoundationErrorCodes.GENERIC_NODE_SERVICE_ERROR);
		}
	}
}

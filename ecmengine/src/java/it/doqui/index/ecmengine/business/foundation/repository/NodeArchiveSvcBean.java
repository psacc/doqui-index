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
import it.doqui.index.ecmengine.exception.repository.NodeArchiveRuntimeException;

import java.util.List;

import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * @author DoQui
 *
 */
public class NodeArchiveSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -298045035497976955L;

	private static final String NODE_ARCHIVE_SERVICE_BEAN = "nodeArchiveService";
	private static final QName NODE_ARCHIVE_SERVICE_QNAME = QName.createQName("", NODE_ARCHIVE_SERVICE_BEAN);
	
	private NodeArchiveService getService() {
		return (NodeArchiveService) serviceRegistry.getService(NODE_ARCHIVE_SERVICE_QNAME);
	}
	
	public NodeRef getStoreArchiveNode(StoreRef storeRef) throws NodeArchiveRuntimeException {
		NodeRef nodeRef = null;
		logger.debug("[NodeArchiveSvcBean::getStoreArchiveNode] BEGIN");			
		
		try {
			logger.debug("[NodeArchiveSvcBean::getStoreArchiveNode] " +
					"Getting store archive node for store: " + storeRef.toString());
			nodeRef = getService().getStoreArchiveNode(storeRef);
    	} catch (Exception e) {
			handleNodeArchiveServiceException("getStoreArchiveNode", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::getStoreArchiveNode] END");
		}
		return nodeRef;
	}
	
	public NodeRef getArchivedNode(NodeRef originalNodeRef) throws NodeArchiveRuntimeException {
		NodeRef nodeRef = null;
		logger.debug("[NodeArchiveSvcBean::getArchivedNode] BEGIN");			
		
		try {
			logger.debug("[NodeArchiveSvcBean::getArchivedNode] " +
					"Getting archived copy for node: " + originalNodeRef.toString());
			nodeRef = getService().getArchivedNode(originalNodeRef);
    	} catch (Exception e) {
			handleNodeArchiveServiceException("getArchivedNode", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::getArchivedNode] END");
		}
		return nodeRef;
	}
	
	public RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef) throws NodeArchiveRuntimeException {
		RestoreNodeReport report = null;
		logger.debug("[NodeArchiveSvcBean::restoreArchivedNode] BEGIN");			
		
		try {
			logger.debug("[NodeArchiveSvcBean::restoreArchivedNode] " +
					"Restoring archived node: " + archivedNodeRef.toString());
			report = getService().restoreArchivedNode(archivedNodeRef);
    	} catch (Exception e) {
			handleNodeArchiveServiceException("restoreArchivedNode", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::restoreArchivedNode] END");
		}
		return report;
	}
	
	public RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef, NodeRef destinationNodeRef,
			QName assocTypeQName, QName assocQName) throws NodeArchiveRuntimeException {
		RestoreNodeReport report = null;
		logger.debug("[NodeArchiveSvcBean::restoreArchivedNode] BEGIN");			

		try {
			logger.debug("[NodeArchiveSvcBean::restoreArchivedNode] " +
					"Restoring archived node: " + archivedNodeRef.toString() + " [To destination: " + destinationNodeRef + "]");
			report = getService().restoreArchivedNode(archivedNodeRef, destinationNodeRef, assocTypeQName, assocQName);
		} catch (Exception e) {
			handleNodeArchiveServiceException("restoreArchivedNode", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::restoreArchivedNode] END");
		}
		return report;
	}

	public List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs) throws NodeArchiveRuntimeException {
		List<RestoreNodeReport> reports = null;
		logger.debug("[NodeArchiveSvcBean::restoreArchivedNodes] BEGIN");			
		
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("[NodeArchiveSvcBean::restoreArchivedNodes] " +
						"Restoring archived nodes: " + archivedNodeRefs.toString());
			}
			reports = getService().restoreArchivedNodes(archivedNodeRefs);
    	} catch (Exception e) {
			handleNodeArchiveServiceException("restoreArchivedNodes", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::restoreArchivedNodes] END");
		}
		return reports;
	}
	
	public List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs,
			NodeRef destinationNodeRef, QName assocTypeQName, QName assocQName) throws NodeArchiveRuntimeException {
		List<RestoreNodeReport> reports = null;
		logger.debug("[NodeArchiveSvcBean::restoreArchivedNodes] BEGIN");			

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("[NodeArchiveSvcBean::restoreArchivedNodes] " +
						"Restoring archived nodes: " + archivedNodeRefs.toString() 
						+ " [To destination: " + destinationNodeRef + "]");
			}
			reports = getService().restoreArchivedNodes(archivedNodeRefs, destinationNodeRef, assocTypeQName, assocQName);
		} catch (Exception e) {
			handleNodeArchiveServiceException("restoreArchivedNodes", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::restoreArchivedNodes] END");
		}
		return reports;
	}
	 
	public List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef) throws NodeArchiveRuntimeException {
		List<RestoreNodeReport> reports = null;
		logger.debug("[NodeArchiveSvcBean::restoreAllArchivedNodes] BEGIN");			

		try {
			logger.debug("[NodeArchiveSvcBean::restoreAllArchivedNodes] " +
					"Restoring all archived nodes to store: " + originalStoreRef.toString());
			reports = getService().restoreAllArchivedNodes(originalStoreRef);
		} catch (Exception e) {
			handleNodeArchiveServiceException("restoreAllArchivedNodes", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::restoreAllArchivedNodes] END");
		}
		return reports;
	}
	 
	public List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef,
			NodeRef destinationNodeRef, QName assocTypeQName, QName assocQName) throws NodeArchiveRuntimeException {
		List<RestoreNodeReport> reports = null;
		logger.debug("[NodeArchiveSvcBean::restoreAllArchivedNodes] BEGIN");			

		try {
			logger.debug("[NodeArchiveSvcBean::restoreAllArchivedNodes] " +
					"Restoring all archived nodes for store: " + originalStoreRef.toString()
					+ " [To destination: " + destinationNodeRef + "]");
			reports = getService().restoreAllArchivedNodes(originalStoreRef, destinationNodeRef, assocTypeQName, assocQName);
		} catch (Exception e) {
			handleNodeArchiveServiceException("restoreAllArchivedNodes", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::restoreAllArchivedNodes] END");
		}
		return reports;
	}
	 
	public void purgeArchivedNode(NodeRef archivedNodeRef) throws NodeArchiveRuntimeException {
		logger.debug("[NodeArchiveSvcBean::purgeArchivedNode] BEGIN");			

		try {
			logger.debug("[NodeArchiveSvcBean::purgeArchivedNode] " +
					"Purging archived node: " + archivedNodeRef.toString());
			getService().purgeArchivedNode(archivedNodeRef);
		} catch (Exception e) {
			handleNodeArchiveServiceException("purgeArchivedNode", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::purgeArchivedNode] END");
		}
	}
	 
	public void purgeArchivedNodes(List<NodeRef> archivedNodes) throws NodeArchiveRuntimeException {
		logger.debug("[NodeArchiveSvcBean::purgeArchivedNodes] BEGIN");			

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("[NodeArchiveSvcBean::purgeArchivedNodes] " +
						"Purging archived node: " + archivedNodes.toString());
			}
			getService().purgeArchivedNodes(archivedNodes);
		} catch (Exception e) {
			handleNodeArchiveServiceException("purgeArchivedNodes", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::purgeArchivedNodes] END");
		}
	}
	 
	public void purgeAllArchivedNodes(StoreRef originalStoreRef) throws NodeArchiveRuntimeException {
		logger.debug("[NodeArchiveSvcBean::purgeAllArchivedNodes] BEGIN");			

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("[NodeArchiveSvcBean::purgeAllArchivedNodes] " +
						"Purging all archived nodes for store: " + originalStoreRef.toString());
			}
			getService().purgeAllArchivedNodes(originalStoreRef);
		} catch (Exception e) {
			handleNodeArchiveServiceException("purgeAllArchivedNodes", e);
		} finally {
			logger.debug("[NodeArchiveSvcBean::purgeAllArchivedNodes] END");
		}
	}

	private void handleNodeArchiveServiceException(String methodName, Throwable e) throws NodeArchiveRuntimeException {
		logger.warn("[NodeArchiveSvcBean::handleNodeArchiveServiceException] " +
				"Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof InvalidTypeException) {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.INVALID_TYPE_ERROR);
		} else if (e instanceof InvalidAspectException) {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.INVALID_ASPECT_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new NodeArchiveRuntimeException(FoundationErrorCodes.GENERIC_NODE_SERVICE_ERROR);
		}
	}
}

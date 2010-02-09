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
 
package it.doqui.index.ecmengine.business.foundation.contentmanagement;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.contentmanagement.VersionRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;

public class VersionSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 337994163281601450L;

	public NodeRef restore(NodeRef nodeRef, NodeRef parentNodeRef, QName assocTypeQName, QName assocQName) 
	throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::restore] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[VersionSvcBean::restore] restore for node: " + nodeRef + 
					" parentNodeRef " + parentNodeRef + 
					" assocTypeQName " + assocTypeQName + 
					" assocQName " + assocQName);
			outNodeRef = serviceRegistry.getVersionService().restore(nodeRef, parentNodeRef, assocTypeQName, assocQName);
		} catch (Exception e) {
			handleVersionServiceException("restore", e);
		} finally {
			logger.debug("[VersionSvcBean::restore] END");
		}
		return outNodeRef;
	}
	
	public NodeRef restore(NodeRef nodeRef, NodeRef parentNodeRef, QName assocTypeQName, QName assocQName, boolean deep) 
	throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::restore] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[VersionSvcBean::restore] restore for node: " + nodeRef + 
					" parentNodeRef " + parentNodeRef + 
					" assocTypeQName " + assocTypeQName + 
					" assocQName " + assocQName + 
					" deep " + deep);
			outNodeRef = serviceRegistry.getVersionService().restore(nodeRef, parentNodeRef, assocTypeQName, assocQName, deep);
		} catch (Exception e) {
			handleVersionServiceException("restore", e);
		} finally {
			logger.debug("[VersionSvcBean::restore] END");
		}
		return outNodeRef;
	}
	
	public Collection<Version> createVersion(Collection<NodeRef> nodeRefs, Map<String, Serializable> versionProperties) 
	throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::createVersion] BEGIN");
		Collection<Version> collectionOfVersion = null;
		
		try {
			logger.debug("[VersionSvcBean::createVersion] createVersion for nodeRefs: " + nodeRefs + 
					" versionProperties " + versionProperties);
			collectionOfVersion = serviceRegistry.getVersionService().createVersion(nodeRefs, versionProperties);
		} catch (Exception e) {
			handleVersionServiceException("createVersion", e);
		} finally {
			logger.debug("[VersionSvcBean::createVersion] END");
		}
		return collectionOfVersion;
	}
	
	public Collection<Version> createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties, boolean versionChildren) 
	throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::createVersion] BEGIN");
		Collection<Version> collectionOfVersion = null;
		
		try {
			logger.debug("[VersionSvcBean::createVersion] createVersion for nodeRef: " + nodeRef + 
					" versionProperties " + versionProperties + 
					" versionChildren " + versionChildren);
			collectionOfVersion = serviceRegistry.getVersionService().createVersion(nodeRef, versionProperties, versionChildren);
		} catch (Exception e) {
			handleVersionServiceException("createVersion", e);
		} finally {
			logger.debug("[VersionSvcBean::createVersion] END");
		}
		return collectionOfVersion;
	}
	
	public Version createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties) 
	throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::createVersion] BEGIN");
		Version version = null;
		
		try {
			logger.debug("[VersionSvcBean::createVersion] createVersion for nodeRef: " + nodeRef + " versionProperties " + versionProperties);
			version = serviceRegistry.getVersionService().createVersion(nodeRef, versionProperties);
		} catch (Exception e) {
			handleVersionServiceException("createVersion", e);
		} finally {
			logger.debug("[VersionSvcBean::createVersion] END");
		}
		return version;
	}

	public Version getCurrentVersion(NodeRef nodeRef) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::getCurrentVersion] BEGIN");
		Version version = null;
		
		try {
			logger.debug("[VersionSvcBean::getCurrentVersion] getCurrentVersion for nodeRef: " + nodeRef);
			version = serviceRegistry.getVersionService().getCurrentVersion(nodeRef);
		} catch (Exception e) {
			handleVersionServiceException("createVersion", e);
		} finally {
			logger.debug("[VersionSvcBean::getCurrentVersion] END");
		}
		return version;
	}

	public void deleteVersionHistory(NodeRef nodeRef) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::deleteVersionHistory] BEGIN");
		
		try {
			logger.debug("[VersionSvcBean::deleteVersionHistory] deleteVersionHistory for nodeRef: " + nodeRef);
			serviceRegistry.getVersionService().deleteVersionHistory(nodeRef);
		} catch (Exception e) {
			handleVersionServiceException("deleteVersionHistory", e);
		} finally {
			logger.debug("[VersionSvcBean::deleteVersionHistory] END");
		}
	}
	
	public StoreRef getVersionStoreReference() throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::getVersionStoreReference] BEGIN");
		StoreRef storeRef = null;
		try {
			logger.debug("[VersionSvcBean::getVersionStoreReference] getVersionHistory ");
			storeRef = serviceRegistry.getVersionService().getVersionStoreReference();
		} catch (Exception e) {
			handleVersionServiceException("getVersionStoreReference", e);
		} finally {
			logger.debug("[VersionSvcBean::getVersionStoreReference] END");
		}
		return storeRef;
	}
	
	public void revert(NodeRef nodeRef) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::revert] BEGIN");
		try {
			logger.debug("[VersionSvcBean::revert] revert for nodeRef: " + nodeRef);
			serviceRegistry.getVersionService().revert(nodeRef);
		} catch (Exception e) {
			handleVersionServiceException("revert", e);
		} finally {
			logger.debug("[VersionSvcBean::revert] END");
		}
	}
	
	public void revert(NodeRef nodeRef, boolean deep) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::revert] BEGIN");
		try {
			logger.debug("[VersionSvcBean::revert] revert for nodeRef: " + nodeRef + " deep " + deep);
			serviceRegistry.getVersionService().revert(nodeRef, deep);
		} catch (Exception e) {
			handleVersionServiceException("revert", e);
		} finally {
			logger.debug("[VersionSvcBean::revert] END");
		}
	}
	
	public void revert(NodeRef nodeRef, Version version) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::revert] BEGIN");
		try {
			logger.debug("[VersionSvcBean::revert] revert for nodeRef: " + nodeRef + " version " + version);
			serviceRegistry.getVersionService().revert(nodeRef, version);
		} catch (Exception e) {
			handleVersionServiceException("revert", e);
		} finally {
			logger.debug("[VersionSvcBean::revert] END");
		}
	}

	public void revert(NodeRef nodeRef, Version version, boolean deep) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::revert] BEGIN");
		try {
			logger.debug("[VersionSvcBean::revert] revert for nodeRef: " + nodeRef + " version " + version + " deep " + deep);
			serviceRegistry.getVersionService().revert(nodeRef, version, deep);
		} catch (Exception e) {
			handleVersionServiceException("revert", e);
		} finally {
			logger.debug("[VersionSvcBean::revert] END");
		}
	}			

	public VersionHistory getVersionHistory(NodeRef nodeRef) throws VersionRuntimeException {
		logger.debug("[VersionSvcBean::getVersionHistory] BEGIN");
		VersionHistory versionHistory = null;
		try {
			logger.debug("[VersionSvcBean::getVersionHistory] getVersionHistory for nodeRef: "+nodeRef);
			versionHistory = serviceRegistry.getVersionService().getVersionHistory(nodeRef);
		} catch (Exception e) {
			handleVersionServiceException("getVersionHistory", e);
		} finally {
			logger.debug("[VersionSvcBean::getVersionHistory] END");
		}
		return versionHistory;
	}
	
	private void handleVersionServiceException(String methodName, Throwable e) throws VersionRuntimeException {
		logger.warn("[VersionSvcBean::handleVersionServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new VersionRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new VersionRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new VersionRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new VersionRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new VersionRuntimeException(FoundationErrorCodes.GENERIC_VERSION_SERVICE_ERROR);
		}
	}
}

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
 
package it.doqui.index.ecmengine.business.foundation.security;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.personalization.security.permissions.MultipleInheritancePermissionServiceImpl;
import it.doqui.index.ecmengine.exception.security.PermissionRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Set;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;


public class PermissionSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 6669971217052316457L;

	private static final String PERMISSION_SERVICE_IMPL_BEAN = "permissionServiceImpl";
	
	public void clearPermission(NodeRef nodeRef, String authority) throws PermissionRuntimeException {
		logger.debug("[PermissionSvcBean::clearPermission] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::clearPermission] " +
					"Cleaning permissions on node: " + nodeRef + " (authority: " + authority + ")");
			serviceRegistry.getPermissionService().clearPermission(nodeRef, authority);
		} catch (Exception e) {
			handlePermissionServiceException("clearPermission", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::clearPermission] END");
		}
	}

	public void deletePermission(NodeRef nodeRef, String authority, String permission) 
			throws PermissionRuntimeException {
		logger.debug("[PermissionSvcBean::deletePermission] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::deletePermission] " +
					"Deleting permission on node: " + nodeRef + " " +
					"(authority: " + authority + " permission: " + permission + ")");
			serviceRegistry.getPermissionService().deletePermission(nodeRef, authority, permission);
		} catch (Exception e) {
			handlePermissionServiceException("deletePermission", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::deletePermission] END");
		}
	}

	public void deletePermissions(NodeRef nodeRef) throws PermissionRuntimeException {
		logger.debug("[PermissionSvcBean::deletePermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::deletePermissions] " +
					"Deleting all ACL entries of node: " + nodeRef);
			serviceRegistry.getPermissionService().deletePermissions(nodeRef);
		} catch (Exception e) {
			handlePermissionServiceException("deletePermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::deletePermissions] END");
		}
	}

	public String getAllAuthorities() throws PermissionRuntimeException {
		String allAuthorities = null;

		logger.debug("[PermissionSvcBean::getAllAuthorities] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getAllAuthorities] " +
					"Retrieving the ALL_AUTHORITIES authority name.");
			allAuthorities = serviceRegistry.getPermissionService().getAllAuthorities();
		} catch (Exception e) {
			handlePermissionServiceException("getAllAuthorities", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getAllAuthorities] END");
		}
		
		return allAuthorities;
	}

	public String getAllPermission() throws PermissionRuntimeException {
		String allPermissions = null;

		logger.debug("[PermissionSvcBean::getAllPermission] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getAllPermission] " +
					"Retrieving the ALL_PERMISSIONS permission name.");
			allPermissions = serviceRegistry.getPermissionService().getAllPermission();
		} catch (Exception e) {
			handlePermissionServiceException("getAllPermission", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getAllPermission] END");
		}
		
		return allPermissions;
	}

	public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef) 
			throws PermissionRuntimeException {
		Set<AccessPermission> setPermissions = null;

		logger.debug("[PermissionSvcBean::getAllSetPermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getAllSetPermissions] " +
					"Retrieving all the permissions set on node: " + nodeRef);
			setPermissions = serviceRegistry.getPermissionService().getAllSetPermissions(nodeRef);
		} catch (Exception e) {
			handlePermissionServiceException("getAllSetPermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getAllSetPermissions] END");
		}
		
		return setPermissions;
	}

	public boolean getInheritParentPermissions(NodeRef nodeRef) throws PermissionRuntimeException {
		boolean inherit = false;

		logger.debug("[PermissionSvcBean::getInheritParentPermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getInheritParentPermissions] " +
					"Checking if inheritParentPermissions is set on node: " + nodeRef);
			inherit = serviceRegistry.getPermissionService().getInheritParentPermissions(nodeRef);
		} catch (Exception e) {
			handlePermissionServiceException("getInheritParentPermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getInheritParentPermissions] END");
		}
		
		return inherit;
	}

	public String getOwnerAuthority() throws PermissionRuntimeException {
		String ownerAuthority = null;

		logger.debug("[PermissionSvcBean::getOwnerAuthority] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getOwnerAuthority] " +
					"Retrieving the owner authority name.");
			ownerAuthority = serviceRegistry.getPermissionService().getOwnerAuthority();
		} catch (Exception e) {
			handlePermissionServiceException("getOwnerAuthority", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getOwnerAuthority] END");
		}

		return ownerAuthority;
	}

	public Set<AccessPermission> getPermissions(NodeRef nodeRef) 
			throws PermissionRuntimeException {
		Set<AccessPermission> setPermissions = null;

		logger.debug("[PermissionSvcBean::getPermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getPermissions] " +
					"Retrieving all the permissions set for the current user on node: " + nodeRef);
			setPermissions = serviceRegistry.getPermissionService().getPermissions(nodeRef);
		} catch (Exception e) {
			handlePermissionServiceException("getPermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getPermissions] END");
		}
		
		return setPermissions;
	}

	public Set<String> getSettablePermissions(NodeRef nodeRef) 
			throws PermissionRuntimeException {
		Set<String> settablePermissions = null;

		logger.debug("[PermissionSvcBean::getSettablePermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getSettablePermissions] " +
					"Retrieving all settable permissions for node: " + nodeRef);
			settablePermissions = serviceRegistry.getPermissionService().getSettablePermissions(nodeRef);
		} catch (Exception e) {
			handlePermissionServiceException("getSettablePermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getSettablePermissions] END");
		}
		
		return settablePermissions;
	}

	public Set<String> getSettablePermissions(QName type) throws PermissionRuntimeException {
		Set<String> settablePermissions = null;

		logger.debug("[PermissionSvcBean::getSettablePermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::getSettablePermissions] " +
					"Retrieving all settable permissions for type: " + type);
			settablePermissions = serviceRegistry.getPermissionService().getSettablePermissions(type);
		} catch (Exception e) {
			handlePermissionServiceException("getSettablePermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::getSettablePermissions] END");
		}
		
		return settablePermissions;
	}

	public AccessStatus hasPermission(NodeRef nodeRef, String permission) throws PermissionRuntimeException {
		AccessStatus status = null;

		logger.debug("[PermissionSvcBean::hasPermission] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::hasPermission] " +
					"Checking status of permission on node: " + nodeRef + " (permission: " + permission + ")");
			status = serviceRegistry.getPermissionService().hasPermission(nodeRef, permission);
		} catch (Exception e) {
			handlePermissionServiceException("hasPermission", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::hasPermission] END");
		}
		
		return status;
	}

	public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermission) 
			throws PermissionRuntimeException {
		logger.debug("[PermissionSvcBean::setInheritParentPermissions] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::setInheritParentPermissions] " +
					"Setting inheritParentPermissions on node: " + nodeRef + " " +
					"(value: " + inheritParentPermission + ")");
			serviceRegistry.getPermissionService().setInheritParentPermissions(nodeRef, inheritParentPermission);
		} catch (Exception e) {
			handlePermissionServiceException("setInheritParentPermissions", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::setInheritParentPermissions] END");
		}
	}

	public void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow) 
			throws PermissionRuntimeException {
		logger.debug("[PermissionSvcBean::setPermission] BEGIN");
		
		try {
			logger.debug("[PermissionSvcBean::setPermission] " +
					"Setting permission on node: " + nodeRef + " " +
					"(authority: " + authority + " permission: " + permission + " allow: " + allow + ")");
			serviceRegistry.getPermissionService().setPermission(nodeRef, authority, permission, allow);
		} catch (Exception e) {
			handlePermissionServiceException("setPermission", e);
		} finally {
			
			logger.debug("[PermissionSvcBean::setPermission] END");
		}
	}
	
	public boolean supportsMultipleInheritance() {
		logger.debug("[PermissionSvcBean::supportsMultipleInheritance] BEGIN");
		
		try {
			return (getBeanFactory().getBean(PERMISSION_SERVICE_IMPL_BEAN) instanceof MultipleInheritancePermissionServiceImpl);
		} finally {
			logger.debug("[PermissionSvcBean::supportsMultipleInheritance] END");
		}
	}
	
	private void handlePermissionServiceException(String methodName, Throwable e) throws PermissionRuntimeException {
		logger.warn("[PermissionSvcBean::handlePermissionServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new PermissionRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new PermissionRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else {
			throw new PermissionRuntimeException(FoundationErrorCodes.GENERIC_PERMISSION_SERVICE_ERROR);
		}
	}
	
//	public void deletePermission(PermissionEntry permissionEntry) {
//	// TODO Auto-generated method stub
//	
//}

//public void deletePermissions(NodePermissionEntry nodePermissionEntry) {
//	// TODO Auto-generated method stub
//	
//}

//	public void deletePermissions(String recipient) {
//		// TODO Auto-generated method stub
//		
//	}

//	public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public PermissionReference getAllPermissionReference() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public String getPermission(PermissionReference permissionReference) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public PermissionReference getPermissionReference(QName qname, String permissionName) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public PermissionReference getPermissionReference(String permissionName) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public NodePermissionEntry getSetPermissions(NodeRef nodeRef) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public Set<PermissionReference> getSettablePermissionReferences(QName type) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public void setPermission(NodePermissionEntry nodePermissionEntry) {
//		// TODO Auto-generated method stub
//		
//	}

//	public void setPermission(PermissionEntry permissionEntry) {
//		// TODO Auto-generated method stub
//		
//	}
}

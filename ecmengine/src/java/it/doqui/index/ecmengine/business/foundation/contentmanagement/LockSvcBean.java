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
import it.doqui.index.ecmengine.exception.contentmanagement.LockRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

public class LockSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 337994163281601450L;

	public void checkForLock(NodeRef node) throws LockRuntimeException {
		logger.debug("[LockSvcBean::checkForLock] BEGIN");
		
		try {
			logger.debug("[LockSvcBean::checkForLock] Checking for locks: " + node);
			serviceRegistry.getLockService().checkForLock(node);
		} catch (Exception e) {
			handleLockServiceException("checkForLock", e);
		} finally {
			logger.debug("[LockSvcBean::checkForLock] END");
		}
	}
	
	public List<NodeRef> getLocks(StoreRef inStore) throws LockRuntimeException {
		logger.debug("[LockSvcBean::getLocks] BEGIN");
		List<NodeRef> locks = null;

		try {
			logger.debug("[LockSvcBean::getLocks] Get locks for StoreRef: " + inStore);
			locks = serviceRegistry.getLockService().getLocks(inStore);
		} catch (Exception e) {
			handleLockServiceException("getLocks", e);
		} finally {
			logger.debug("[LockSvcBean::getLocks] END");
		}
		return (locks != null) ? locks: Collections.<NodeRef>emptyList();
	}
	
	public List<NodeRef> getLocks(StoreRef inStore, LockType inLockType) throws LockRuntimeException {
		logger.debug("[LockSvcBean::getLocks] BEGIN");
		List<NodeRef> locks = null;

		try {
			logger.debug("[LockSvcBean::getLocks] Get locks for StoreRef: " + inStore + " and LockType " + inLockType);
			locks = serviceRegistry.getLockService().getLocks(inStore, inLockType);
		} catch (Exception e) {
			handleLockServiceException("getLocks", e);
		} finally {
			logger.debug("[LockSvcBean::getLocks] END");
		}
		return (locks != null) ? locks: Collections.<NodeRef>emptyList();
	}
	
	public LockStatus getLockStatus(NodeRef inNodeRef) throws LockRuntimeException {
		logger.debug("[LockSvcBean::getLockStatus] BEGIN");
		LockStatus lockStatus = null;

		try {
			logger.debug("[LockSvcBean::getLockStatus] Get lock status for NodeRef: " + inNodeRef);
			lockStatus = serviceRegistry.getLockService().getLockStatus(inNodeRef);
		} catch (Exception e) {
			handleLockServiceException("getLockStatus", e);
		} finally {
			logger.debug("[LockSvcBean::getLockStatus] END");
		}
		return lockStatus;
	}


	public LockType getLockType(NodeRef inNodeRef) throws LockRuntimeException {
		logger.debug("[LockSvcBean::getLockType] BEGIN");
		LockType lockType = null;

		try {
			logger.debug("[LockSvcBean::getLockType] Get lock type for NodeRef: " + inNodeRef);
			lockType = serviceRegistry.getLockService().getLockType(inNodeRef);
		} catch (Exception e) {
			handleLockServiceException("getLockType", e);
		} finally {
			logger.debug("[LockSvcBean::getLockType] END");
		}
		return lockType;
	}
	
	
	public void lock(NodeRef inNodeRef, LockType inLockType) throws LockRuntimeException {
		logger.debug("[LockSvcBean::lock] BEGIN");

		try {
			logger.debug("[LockSvcBean::lock] Lock NodeRef: " + inNodeRef + 
					" lockType " + inLockType);
			serviceRegistry.getLockService().lock(inNodeRef, inLockType);
		} catch (Exception e) {
			handleLockServiceException("lock", e);
		} finally {
			logger.debug("[LockSvcBean::lock] END");
		}
	}	
	
	public void lock(NodeRef inNodeRef, LockType inLockType, int inTimeToExpire) throws LockRuntimeException {
		logger.debug("[LockSvcBean::lock] BEGIN");

		try {
			
			logger.debug("[LockSvcBean::lock] Lock NodeRef: " + inNodeRef + 
					" lockType " + inLockType + 
					" timeToExpire " + inTimeToExpire);
			serviceRegistry.getLockService().lock(inNodeRef, inLockType, inTimeToExpire);
		} catch (Exception e) {
			handleLockServiceException("lock", e);
		} finally {
			logger.debug("[LockSvcBean::lock] END");
		}
	}	
	
	public void lock(NodeRef inNodeRef, LockType inLockType, int inTimeToExpire, boolean inLockChildren) throws LockRuntimeException {
		logger.debug("[LockSvcBean::lock] BEGIN");

		try {
			
			logger.debug("[LockSvcBean::lock] Lock NodeRef: " + inNodeRef + 
					" lockType " + inLockType + 
					" timeToExpire " + inTimeToExpire + 
					" lockChildren " + inLockChildren);
			serviceRegistry.getLockService().lock(inNodeRef, inLockType, inTimeToExpire, inLockChildren);
		} catch (Exception e) {
			handleLockServiceException("lock", e);
		} finally {
			logger.debug("[LockSvcBean::lock] END");
		}
	}	

	public void lock(Collection<NodeRef> inCollectionNodeRef, LockType inLockType, int inTimeToExpire) throws LockRuntimeException {
		logger.debug("[LockSvcBean::lock] BEGIN");

		try {
			
			logger.debug("[LockSvcBean::lock] Lock Collection: " + inCollectionNodeRef + 
					" lockType " + inLockType + 
					" timeToExpire " + inTimeToExpire);
			serviceRegistry.getLockService().lock(inCollectionNodeRef, inLockType, inTimeToExpire);
		} catch (Exception e) {
			handleLockServiceException("lock", e);
		} finally {
			logger.debug("[LockSvcBean::lock] END");
		}
	}	
	
	public void unlock(Collection<NodeRef> inCollectionNodeRef) throws LockRuntimeException {
		logger.debug("[LockSvcBean::unlock] BEGIN");

		try {
			logger.debug("[LockSvcBean::unlock] Unlock Collection: " + inCollectionNodeRef);
			serviceRegistry.getLockService().unlock(inCollectionNodeRef);
		} catch (Exception e) {
			handleLockServiceException("unlock", e);
		} finally {
			logger.debug("[LockSvcBean::unlock] END");
		}
	}	
	
	
	public void unlock(NodeRef inNodeRef) throws LockRuntimeException {
		logger.debug("[LockSvcBean::unlock] BEGIN");

		try {
			logger.debug("[LockSvcBean::unlock] Unlock NodeRef: " + inNodeRef);
			serviceRegistry.getLockService().unlock(inNodeRef);
		} catch (Exception e) {
			handleLockServiceException("unlock", e);
		} finally {
			logger.debug("[LockSvcBean::unlock] END");
		}
	}	
	
	public void unlock(NodeRef inNodeRef, boolean inUnlockChildren) throws LockRuntimeException {
		logger.debug("[LockSvcBean::unlock] BEGIN");

		try {
			logger.debug("[LockSvcBean::unlock] Unlock NodeRef: " + inNodeRef + " unlockChildren " + inUnlockChildren);
			serviceRegistry.getLockService().unlock(inNodeRef, inUnlockChildren);
		} catch (Exception e) {
			handleLockServiceException("unlock", e);
		} finally {
			logger.debug("[LockSvcBean::unlock] END");
		}
	}	
	
	private void handleLockServiceException(String methodName, Throwable e) throws LockRuntimeException {
		logger.warn("[LockSvcBean::handleLockServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new LockRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new LockRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new LockRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else {
			throw new LockRuntimeException(FoundationErrorCodes.GENERIC_LOCK_SERVICE_ERROR);
		}
	}
}

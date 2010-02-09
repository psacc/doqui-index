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
import it.doqui.index.ecmengine.exception.security.OwnableRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;


public class OwnableSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 2372054907482103612L;

	public String getOwner(NodeRef nodeRef) throws OwnableRuntimeException {
		String owner = null;

		logger.debug("[OwnableSvcBean::getOwner] BEGIN");

		try {
			logger.debug("[OwnableSvcBean::getOwner] Retrieving owner for node: " + nodeRef);
			owner = serviceRegistry.getOwnableService().getOwner(nodeRef);
		} catch (Exception e) {
			handleOwnableServiceException("getOwner", e);
		} finally {
			logger.debug("[OwnableSvcBean::getOwner] END");
		}
		
		return owner;
	}

	public boolean hasOwner(NodeRef nodeRef) throws OwnableRuntimeException {
		boolean hasOwner = false;

		logger.debug("[OwnableSvcBean::hasOwner] BEGIN");

		try {
			logger.debug("[OwnableSvcBean::hasOwner] Checking if owner exists for node: " + nodeRef);
			hasOwner = serviceRegistry.getOwnableService().hasOwner(nodeRef);
		} catch (Exception e) {
			handleOwnableServiceException("hasOwner", e);
		} finally {
			logger.debug("[OwnableSvcBean::hasOwner] END");
		}
		
		return hasOwner;
	}

	public void setOwner(NodeRef nodeRef, String userName) throws OwnableRuntimeException {
		logger.debug("[OwnableSvcBean::setOwner] BEGIN");

		try {
			logger.debug("[OwnableSvcBean::setOwner] Setting owner for node: " + nodeRef + " (user: " + userName + ")");
			serviceRegistry.getOwnableService().setOwner(nodeRef, userName);
		} catch (Exception e) {
			handleOwnableServiceException("setOwner", e);
		} finally {
			logger.debug("[OwnableSvcBean::setOwner] END");
		}
	}

	public void takeOwnership(NodeRef nodeRef) throws OwnableRuntimeException {
		logger.debug("[OwnableSvcBean::takeOwnership] BEGIN");

		try {
			logger.debug("[OwnableSvcBean::takeOwnership] Taking ownership for node: " + nodeRef);
			serviceRegistry.getOwnableService().takeOwnership(nodeRef);
		} catch (Exception e) {
			handleOwnableServiceException("takeOwnership", e);
		} finally {
			logger.debug("[OwnableSvcBean::takeOwnership] END");
		}
	}
	
	private void handleOwnableServiceException(String methodName, Throwable e) throws OwnableRuntimeException {
		logger.warn("[OwnableSvcBean::handleOwnableServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof InvalidNodeRefException) {
			throw new OwnableRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof AccessDeniedException) {
			throw new OwnableRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new OwnableRuntimeException(FoundationErrorCodes.GENERIC_OWNABLE_SERVICE_ERROR);
		}
	}
}

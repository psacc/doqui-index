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
import it.doqui.index.ecmengine.exception.contentmanagement.CopyRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.List;

import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class CopySvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 337994163281601450L;

	public NodeRef copy(NodeRef sourceNodeRef, NodeRef destinationParent, 
			QName destinationAssocTypeQName, QName destinationQName) throws CopyRuntimeException {
		logger.debug("[CopySvcBean::copy] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CopySvcBean::copy] Copy node : " + sourceNodeRef + 
					" to destinationParent " + destinationParent + 
					" destinationAssocTypeQName " + destinationAssocTypeQName + 
					" destinationQName " + destinationQName);
			outNodeRef = serviceRegistry.getCopyService().copy(sourceNodeRef, destinationParent, destinationAssocTypeQName, destinationQName);
		} catch (Exception e) {
			handleCopyServiceException("copy", e);
		} finally {
			logger.debug("[CopySvcBean::copy] END");
		}
		return outNodeRef;
	}
	
	public NodeRef copy(NodeRef sourceNodeRef, NodeRef destinationParent, 
			QName destinationAssocTypeQName, QName destinationQName, boolean copyChildren) 
			throws CopyRuntimeException {
		logger.debug("[CopySvcBean::copy] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CopySvcBean::copy] Copy node : " + sourceNodeRef + 
					" to destinationParent " + destinationParent + 
					" destinationAssocTypeQName " + destinationAssocTypeQName + 
					" destinationQName " + destinationQName + 
					" copyChildren " + copyChildren);
			outNodeRef = serviceRegistry.getCopyService().copy(sourceNodeRef, destinationParent, destinationAssocTypeQName, destinationQName, copyChildren);
		} catch (Exception e) {
			handleCopyServiceException("copy", e);
		} finally {
			logger.debug("[CopySvcBean::copy] END");
		}
		return outNodeRef;
	}

	public NodeRef copyAndRename(NodeRef sourceNodeRef, NodeRef destinationParent, 
			QName destinationAssocTypeQName, QName destinationQName, boolean copyChildren) 
			throws CopyRuntimeException {
		logger.debug("[CopySvcBean::copyAndRename] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CopySvcBean::copyAndRename] Copy and rename node : " + sourceNodeRef + 
					" to destinationParent " + destinationParent + 
					" destinationAssocTypeQName " + destinationAssocTypeQName + 
					" destinationQName " + destinationQName + 
					" copyChildren " + copyChildren);
			outNodeRef = serviceRegistry.getCopyService().copyAndRename(sourceNodeRef, destinationParent, destinationAssocTypeQName, destinationQName, copyChildren);
		} catch (Exception e) {
			handleCopyServiceException("copyAndRename", e);
		} finally {
			logger.debug("[CopySvcBean::copyAndRename] END");
		}
		return outNodeRef;
	}
	
	public void copy(NodeRef sourceNodeRef, NodeRef destinationParent) throws CopyRuntimeException {
		logger.debug("[CopySvcBean::copy] BEGIN");
		
		try {
			logger.debug("[CopySvcBean::copy] Copy node : " + sourceNodeRef + 
					" to destinationParent " + destinationParent);
			serviceRegistry.getCopyService().copy(sourceNodeRef, destinationParent);
		} catch (Exception e) {
			handleCopyServiceException("copy", e);
		} finally {
			logger.debug("[CopySvcBean::copy] END");
		}
	}
	
	public List<NodeRef> getCopies(NodeRef nodeRef) throws CopyRuntimeException {
		logger.debug("[CopySvcBean::getCopies] BEGIN");
		List<NodeRef> listNodeRef = null;
		try {
			logger.debug("[CopySvcBean::getCopies] Get copies for node : " + nodeRef);
			listNodeRef = serviceRegistry.getCopyService().getCopies(nodeRef);
		} catch (Exception e) {
			handleCopyServiceException("getCopies", e);
		} finally {
			logger.debug("[CopySvcBean::getCopies] END");
		}
		return listNodeRef;
	}

	private void handleCopyServiceException(String methodName, Throwable e) throws CopyRuntimeException {
		logger.warn("[CopySvcBean::handleCopyServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new CopyRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new CopyRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new CopyRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new CopyRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new CopyRuntimeException(FoundationErrorCodes.GENERIC_COPY_SERVICE_ERROR);
		}
	}
}

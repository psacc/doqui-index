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
import it.doqui.index.ecmengine.exception.contentmanagement.FileFolderRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

public class FileFolderSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 6761448899133632769L;

	public NodeRef searchSimple(NodeRef inNodeRef, String inName) throws FileFolderRuntimeException {
		logger.debug("[FileFolderSvcBean::searchSimple] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[FileFolderSvcBean::searchSimple] searchSimple node : " + inNodeRef + 
					" name " + inName);
			outNodeRef = serviceRegistry.getFileFolderService().searchSimple(inNodeRef, inName);
		} catch (Exception e) {
			handleFileFolderServiceException("searchSimple", e);
		} finally {
			logger.debug("[FileFolderSvcBean::searchSimple] END");
		}
		return outNodeRef;
	}
	
	public FileInfo copy(NodeRef inSourceNodeRef, NodeRef inDestinationNodeRef, String inName) throws FileFolderRuntimeException {
		logger.debug("[FileFolderSvcBean::copy] BEGIN");
		FileInfo fileInfo = null;
		
		try {
			logger.debug("[FileFolderSvcBean::searchSimple] copy nodeSource : " + inSourceNodeRef +
					" nodeDestination " + inDestinationNodeRef +
					" name " + inName);
			fileInfo = serviceRegistry.getFileFolderService().copy(inSourceNodeRef, inDestinationNodeRef, inName);
		} catch (Exception e) {
			handleFileFolderServiceException("copy", e);
		} finally {
			logger.debug("[FileFolderSvcBean::copy] END");
		}
		return fileInfo;
	}
	
	
	private void handleFileFolderServiceException(String methodName, Throwable e) throws FileFolderRuntimeException {
		logger.warn("[CopySvcBean::handleCopyServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new FileFolderRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new FileFolderRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new FileFolderRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new FileFolderRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new FileFolderRuntimeException(FoundationErrorCodes.GENERIC_FILEFOLDER_SERVICE_ERROR);
		}
	}
}

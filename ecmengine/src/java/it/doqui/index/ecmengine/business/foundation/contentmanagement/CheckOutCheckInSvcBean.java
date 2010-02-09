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
import it.doqui.index.ecmengine.exception.contentmanagement.CheckOutCheckInRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class CheckOutCheckInSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final String STR_ALREADY_CHECKED_OUT = "already checked out";
	
	private static final long serialVersionUID = 337994163281601450L;

	public NodeRef cancelCheckout(NodeRef node) throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::cancelCheckout] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::cancelCheckout] cancelCheckout for node: " + node);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().cancelCheckout(node);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("cancelCheckout", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::cancelCheckout] END");
		}
		return outNodeRef;
	}
	

	public NodeRef checkin(NodeRef node, Map<String, Serializable> versionProperties)
	throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::checkin] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::checkin] checkin for node: " + node+" versionProperties "+versionProperties);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().checkin(node, versionProperties);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("checkin", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::checkin] END");
		}
		return outNodeRef;
	}
	
	public NodeRef checkin(NodeRef node, Map<String, Serializable> versionProperties, String contentUrl) 
	throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::checkin] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::checkin] checkin for node: " + node + 
					" versionProperties " + versionProperties + 
					" contentUrl " + contentUrl);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().checkin(node, versionProperties, contentUrl);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("checkin", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::checkin] END");
		}
		return outNodeRef;
	}
	
	public NodeRef checkin(NodeRef node, Map<String, Serializable> versionProperties, String contentUrl, boolean keepCheckedOut)
	throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::checkin] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::checkin] checkin for node: " + node + 
					" versionProperties " + versionProperties + 
					" contentUrl " + contentUrl + 
					" keepCheckedOut " + keepCheckedOut);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().checkin(node, versionProperties, contentUrl, keepCheckedOut);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("checkin", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::checkin] END");
		}
		return outNodeRef;
	}

	public NodeRef checkout(NodeRef node) throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::checkout] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::checkout] checkout for node: " + node);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().checkout(node);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("checkout", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::checkout] END");
		}
		return outNodeRef;
	}
	
	public NodeRef checkout(NodeRef node, NodeRef destinationParentNodeRef, QName destinationAssocTypeQName, QName destinationAssocQName)
	throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::checkout] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::checkout] checkout for node: " + node +
					" destinationParentNodeRef " + destinationParentNodeRef + 
					" destinationAssocTypeQName " + destinationAssocTypeQName + 
					" destinationAssocQName " + destinationAssocQName);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().checkout(node, destinationParentNodeRef, destinationAssocTypeQName, destinationAssocQName);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("checkout", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::checkout] END");
		}
		return outNodeRef;
	}


	public NodeRef getWorkingCopy(NodeRef node) throws CheckOutCheckInRuntimeException {
		logger.debug("[CheckOutCheckInSvcBean::getWorkingCopy] BEGIN");
		NodeRef outNodeRef = null;
		
		try {
			logger.debug("[CheckOutCheckInSvcBean::getWorkingCopy] getWorkingCopy for node: " + node);
			outNodeRef = serviceRegistry.getCheckOutCheckInService().getWorkingCopy(node);
		} catch (Exception e) {
			handleCheckOutCheckInServiceException("getWorkingCopy", e);
		} finally {
			logger.debug("[CheckOutCheckInSvcBean::getWorkingCopy] END");
		}
		return outNodeRef;
	}
	
	private void handleCheckOutCheckInServiceException(String methodName, Throwable e) throws CheckOutCheckInRuntimeException {
		logger.warn("[CheckOutCheckInSvcBean::handleCheckOutCheckInServiceException] " +
				"Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else if (e instanceof CheckOutCheckInServiceException) {
			if (e.getMessage() == null) {
				throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.GENERIC_CHECKOUT_CHECKIN_SERVICE_ERROR);
			} else if (e.getMessage().contains(STR_ALREADY_CHECKED_OUT)) {
				throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.ALREADY_CHECKED_OUT);
			}
		} else {
			throw new CheckOutCheckInRuntimeException(FoundationErrorCodes.GENERIC_CHECKOUT_CHECKIN_SERVICE_ERROR);
		}
	}
}

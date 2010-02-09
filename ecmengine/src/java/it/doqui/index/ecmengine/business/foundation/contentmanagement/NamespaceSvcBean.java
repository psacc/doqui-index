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
import it.doqui.index.ecmengine.exception.contentmanagement.NamespaceRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Collection;

import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

public class NamespaceSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {


	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public void unregisterNamespace(String inNamespaceUri) throws NamespaceRuntimeException {
		logger.debug("[NamespaceSvcBean::copy] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[NamespaceSvcBean::unregisterNamespace] unregisterNamespace inNamespaceUri : " + inNamespaceUri);
    		}
			serviceRegistry.getNamespaceService().unregisterNamespace(inNamespaceUri);
		} catch (Exception e) {
			handleNamespaceServiceException("unregisterNamespace", e);
		} finally {
			logger.debug("[NamespaceSvcBean::unregisterNamespace] END");
		}
	}

	public void registerNamespace(String inPrefix, String inNamespaceUri) throws NamespaceRuntimeException {
		logger.debug("[NamespaceSvcBean::registerNamespace] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[NamespaceSvcBean::copy] registerNamespace inPrefix : " + inPrefix +" inNamespaceUri : " + inNamespaceUri);
    		}
			serviceRegistry.getNamespaceService().registerNamespace(inPrefix, inNamespaceUri);
		} catch (Exception e) {
			handleNamespaceServiceException("registerNamespace", e);
		} finally {
			logger.debug("[NamespaceSvcBean::registerNamespace] END");
		}
	}

	public Collection<String> getUriList() throws NamespaceRuntimeException {
		logger.debug("[CopySvcBean::getUriList] BEGIN");
		Collection<String> uriList = null;

		try {
			uriList = serviceRegistry.getNamespaceService().getURIs();
		} catch (Exception e) {
			handleNamespaceServiceException("getUriList", e);
		} finally {
			logger.debug("[NamespaceSvcBean::getUriList] END");
		}
		return uriList;
	}

	public Collection<String> getPrefixes(String inNamespace) throws NamespaceRuntimeException {
		logger.debug("[NamespaceSvcBean::copy] BEGIN");
		Collection<String> prefixesList = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[NamespaceSvcBean::getPrefixes] getPrefixes : " + inNamespace);
    		}
			prefixesList = serviceRegistry.getNamespaceService().getPrefixes(inNamespace);
		} catch (Exception e) {
			handleNamespaceServiceException("getPrefixes", e);
		} finally {
			logger.debug("[NamespaceSvcBean::getPrefixes] END");
		}
		return prefixesList;

	}

	public Collection<String> getPrefixes() throws NamespaceRuntimeException {
		logger.debug("[NamespaceSvcBean::getPrefixes] BEGIN");
		Collection<String> prefixesList = null;

		try {
			prefixesList = serviceRegistry.getNamespaceService().getPrefixes();
		} catch (Exception e) {
			handleNamespaceServiceException("getPrefixes", e);
		} finally {
			logger.debug("[NamespaceSvcBean::getPrefixes] END");
		}
		return prefixesList;
	}

	public String getNamespaceURI(String inPrefix) throws NamespaceRuntimeException {
		logger.debug("[NamespaceSvcBean::getNamespaceURI] BEGIN");
		String namespaceUri = null;

		try {
            if(logger.isDebugEnabled()) {
	    		logger.debug("[NamespaceSvcBean::getPrefixes] prefix vale : " + inPrefix);
            }
			namespaceUri = serviceRegistry.getNamespaceService().getNamespaceURI(inPrefix);
		} catch (Exception e) {
			handleNamespaceServiceException("getNamespaceURI", e);
		} finally {
			logger.debug("[NamespaceSvcBean::getNamespaceURI] END");
		}
		return namespaceUri;
	}


	private void handleNamespaceServiceException(String methodName, Throwable e) throws NamespaceRuntimeException {
		logger.warn("[CopySvcBean::handleNamespaceServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new NamespaceRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new NamespaceRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new NamespaceRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new NamespaceRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new NamespaceRuntimeException(FoundationErrorCodes.GENERIC_NAMESPACE_SERVICE_ERROR);
		}
	}
}

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

package it.doqui.index.ecmengine.business.foundation.integrity;

import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.integrity.IntegrityBusinessInterface;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.exception.repository.EcmEngineIntegrityException;

public class IntegritySvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = 686260652933942333L;

	public Map<String,Node> getAllNodes() throws EcmEngineIntegrityException {
		logger.debug("[IntegritySvcBean::getAllNodes] BEGIN");
		Map<String,Node> node = null;
		try {
			node = getIntegrityService().getAllNodes();
		} catch (EcmEngineIntegrityException e) {
			//handleMimetypeServiceException("getMimetype", e);
			//TODO: Handle
			throw e;
		} finally {
			logger.debug("[IntegritySvcBean::getAllNodes] END");
		}
        return node;
	}
	
	public Long getDBID(Node node) throws EcmEngineIntegrityException {
		logger.debug("[IntegritySvcBean::getDBID] BEGIN");
		Long response = null;
		try {
			response = getIntegrityService().getDBID(node);
		} catch (EcmEngineIntegrityException e) {
			//handleMimetypeServiceException("getMimetype", e);
			//TODO: Handle
			throw e;
		} finally {
			logger.debug("[IntegritySvcBean::getDBID] END");
		}
        return response;
	}
	
	public Map<Long,Set<Long>> getAllAssociations() throws EcmEngineIntegrityException {
		logger.debug("[IntegritySvcBean::getAllAssociations] BEGIN");
		Map<Long,Set<Long>> result = null;
		try {
			result = getIntegrityService().getAllAssociations();
		} catch (EcmEngineIntegrityException e) {
			//handleMimetypeServiceException("getMimetype", e);
			//TODO: Handle
			throw e;
		} finally {
			logger.debug("[IntegritySvcBean::getAllAssociations] END");
		}
        return result;
	}
	
	public Map<Long,String> getAllDBIDUID() throws EcmEngineIntegrityException {
		//System.out.println("Arrivo al svcbean");
		logger.debug("[IntegritySvcBean::getAllDBIDUID] BEGIN");
		Map<Long,String> result = null;
		try {
			result = getIntegrityService().getAllDBIDUID();
		} catch (EcmEngineIntegrityException e) {
			//handleMimetypeServiceException("getMimetype", e);
			//TODO: Handle
			throw e;
		} finally {
			logger.debug("[IntegritySvcBean::getAllDBIDUID] END");
			//System.out.println("Esco dal svcbean");
		}
        return result;
	}

	private IntegrityBusinessInterface getIntegrityService() {
		return (IntegrityBusinessInterface)serviceRegistry.getService(QName.createQName(ECMENGINE_INTEGRITY_SERVICE_BEAN));
	}

	/*private void handleMimetypeServiceException(String methodName, Throwable e) throws MimetypeRuntimeException {
		logger.warn("[MimetypeSvcBean::handleMimetypeServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new MimetypeRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new MimetypeRuntimeException(FoundationErrorCodes.GENERIC_MIMETYPE_SERVICE_ERROR);
		}
	}*/

}

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

package it.doqui.index.ecmengine.business.foundation.audit;

import it.doqui.index.ecmengine.business.audit.AuditTrailBusinessInterface;
import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.exception.repository.AuditRuntimeException;

import org.alfresco.service.namespace.QName;

public class AuditTrailSvcBean extends EcmEngineWrapperBean implements AuditTrailBusinessInterface {

	private static final long serialVersionUID = -5411858690421701718L;

	public void logTrail(AuditInfo auditInfo) throws AuditRuntimeException {
		logger.debug("[AuditTrailSvcBean::logTrail] BEGIN");
		try {
			getAuditTrailManager().logTrail(auditInfo);
		} catch (Exception e) {
			handleAuditTrailServiceException("logTrail", e);
		} finally {
			logger.debug("[AuditTrailSvcBean::logTrail] END");
		}
	}

	public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca)
			throws AuditRuntimeException {
		logger.debug("[AuditTrailSvcBean::ricercaAuditTrail] BEGIN");
		AuditInfo[] result = null;
		try {
			result = getAuditTrailManager().ricercaAuditTrail(parametriRicerca);
		} catch (Exception e) {
			handleAuditTrailServiceException("ricercaAuditTrail", e);
		} finally {
			logger.debug("[AuditTrailSvcBean::ricercaAuditTrail] END");
		}
		return result;
	}

	private AuditTrailBusinessInterface getAuditTrailManager() {
		return (AuditTrailBusinessInterface)serviceRegistry.getService(QName.createQName(ECMENGINE_AUDIT_TRAIL_MANAGER_BEAN));
	}

	private void handleAuditTrailServiceException(String methodName, Throwable e) throws AuditRuntimeException {
		logger.warn("[AuditTrailSvcBean::handleAuditServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new AuditRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new AuditRuntimeException(FoundationErrorCodes.GENERIC_AUDIT_TRAIL_SERVICE_ERROR);
		}
	}

}

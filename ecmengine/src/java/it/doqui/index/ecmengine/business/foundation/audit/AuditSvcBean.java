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

import org.alfresco.service.namespace.QName;

import it.doqui.index.ecmengine.business.audit.AuditBusinessInterface;
import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.exception.repository.AuditRuntimeException;

public class AuditSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -7087768272736721314L;

	public void insertAudit(OperazioneAudit operazioneDto) throws AuditRuntimeException {
		logger.debug("[AuditSvcBean::insertAudit] BEGIN");
		try {
			getAuditManager().insertAudit(operazioneDto);
		} catch (Exception e) {
			handleAuditServiceException("insertAudit", e);
		} finally {
			logger.debug("[AuditSvcBean::insertAudit] END");
		}
	}

	public OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca) throws AuditRuntimeException {
		logger.debug("[AuditSvcBean::ricercaAudit] BEGIN");
		OperazioneAudit[] result = null;
		try {
			result = getAuditManager().ricercaAudit(parametriRicerca);
		} catch (Exception e) {
			handleAuditServiceException("ricercaAudit", e);
		} finally {
			logger.debug("[AuditSvcBean::ricercaAudit] END");
		}
		return result;
	}

	private AuditBusinessInterface getAuditManager() {
		return (AuditBusinessInterface)serviceRegistry.getService(QName.createQName(ECMENGINE_AUDIT_MANAGER_BEAN));
	}

	private void handleAuditServiceException(String methodName, Throwable e) throws AuditRuntimeException {
		logger.warn("[AuditSvcBean::handleAuditServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new AuditRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new AuditRuntimeException(FoundationErrorCodes.GENERIC_AUDIT_SERVICE_ERROR);
		}
	}

}

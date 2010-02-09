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

package it.doqui.index.ecmengine.business.audit;

import it.doqui.index.ecmengine.business.audit.util.AuditDtoHelper;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.integration.audittrail.dao.AuditTrailDAO;
import it.doqui.index.ecmengine.integration.audittrail.vo.AuditTrailVO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che implementa il gestore dei dati di audit trail.
 *
 * @author DoQui
 */
public final class AuditTrailManager implements AuditTrailBusinessInterface, EcmEngineConstants {

	private static Log logger = LogFactory.getLog(ECMENGINE_AUDIT_TRAIL_LOG_CATEGORY);

	private AuditTrailDAO auditTrailDAO;

	/** Costruttore predefinito. */
	public AuditTrailManager() {}

	public void setAuditTrailDAO(AuditTrailDAO auditTrailDAO) {
		this.auditTrailDAO = auditTrailDAO;
	}

	public void logTrail(AuditInfo auditInfo) throws Exception {
		logger.debug("[AuditTrailManager::logTrail] BEGIN");
		try {
			AuditTrailVO auditTrailVO = AuditDtoHelper.getAuditTrailVO(auditInfo);
			// Forzare la property "data" con la data e ora corrente.
			auditTrailVO.setData(new Date());

			this.auditTrailDAO.logTrail(auditTrailVO);
		} catch (Exception e) {
			logger.error("[AuditTrailManager::logTrail] ERROR", e);
			throw e;
		} finally {
			logger.debug("[AuditTrailManager::logTrail] END");
		}
	}

	public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca)
			throws Exception {
		logger.debug("[AuditTrailManager::ricercaAuditTrail] BEGIN");
		AuditInfo[] result = null;
		try {
			AuditTrailVO[] queryResult = auditTrailDAO.ricercaAuditTrail(
					parametriRicerca.getUtente(),
					parametriRicerca.getIdOggetto(),
					parametriRicerca.getNomeOperazione(),
					null,
					parametriRicerca.getInizioIntervallo(),
					parametriRicerca.getFineIntervallo()
					);
			final int sizeLista = (queryResult == null) ? 0 : queryResult.length;
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuditTrailManager::ricercaAuditTrail] trovati " + sizeLista + " record");
			}

			// Ricostruzione risultato
			result = new AuditInfo[sizeLista];
			for (int i=0; i<sizeLista; i++) {
				result[i] = AuditDtoHelper.getAuditInfo(queryResult[i]);
			}
		} catch (Exception e) {
			logger.error("[AuditTrailManager::ricercaAuditTrail] ERROR", e);
			throw e;
		} finally {
			logger.debug("[AuditTrailManager::ricercaAuditTrail] END");
		}
		return result;
	}

}

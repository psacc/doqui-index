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
import it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.integration.audit.dao.AuditDAO;
import it.doqui.index.ecmengine.integration.audit.vo.AuditVO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che implementa il gestore dei dati di audit.
 *
 * @author DoQui
 */
public final class AuditManager implements AuditBusinessInterface, EcmEngineConstants {

	private static Log logger = LogFactory.getLog(ECMENGINE_AUDIT_LOG_CATEGORY);

	private AuditDAO auditDAO;

	/** Costruttore predefinito. */
	public AuditManager(){}

	public void setAuditDAO(AuditDAO auditDAO) {
		this.auditDAO = auditDAO;
	}

	public void insertAudit(OperazioneAudit operazioneDto) throws Exception {
		logger.debug("[AuditManager::insertAudit] BEGIN");

		try {
			this.auditDAO.insert(AuditDtoHelper.getAuditVO(operazioneDto));
		} catch(Exception e) {
			logger.error("[AuditManager::insertAudit] ERROR");
			throw e;
		} finally {
			logger.debug("[AuditManager::insertAudit] END");
		}
	}

	public OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca) throws Exception {
		logger.debug("[AuditManager::ricercaAudit] BEGIN");

        OperazioneAudit[] listaOperazioni = null;
        try {

        	AuditVO[] result = this.auditDAO.ricercaAudit(
        			parametriRicerca.getUtente(),
        			parametriRicerca.getFruitore(),
        			parametriRicerca.getServizio(),
        			parametriRicerca.getIdOggetto(),
        			parametriRicerca.getTipoOggetto(),
        			parametriRicerca.getNomeOperazione(),
        			parametriRicerca.getInizioIntervallo(),
        			parametriRicerca.getFineIntervallo()
        			);
			final int sizeLista = (result == null) ? 0 : result.length;
            if(logger.isDebugEnabled()) {
		    	logger.debug("[AuditManager::ricercaAudit] trovati " + sizeLista + " record");
    		}

			// Ricostruzione risultato
			listaOperazioni = new OperazioneAudit[sizeLista];
			for (int i=0; i<sizeLista; i++) {
				listaOperazioni[i] = AuditDtoHelper.getOperazioneAudit(result[i]);
			}

        } catch (Exception e) {
			logger.error("[AuditManager::ricercaAudit] ERROR", e);
			throw e;
		} finally {
			logger.debug("[AuditManager::ricercaAudit] END");
		}

        if(logger.isDebugEnabled()) {
		    logger.debug("listaOperazioni: ["+listaOperazioni+"]");
		}
		return listaOperazioni;
	}

}

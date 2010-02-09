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

import it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.integration.audit.dao.AuditDAO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che implementa il gestore dei dati di audit.
 *
 * @author DoQui
 */
public final class AuditManagerNull implements AuditBusinessInterface, EcmEngineConstants {

    private static Log logger = LogFactory.getLog(ECMENGINE_AUDIT_LOG_CATEGORY);

    /** Costruttore predefinito. */
    public AuditManagerNull(){}

    public void setAuditDAO(AuditDAO auditDAO) {}

    public void insertAudit(OperazioneAudit operazioneDto) throws Exception {
        logger.debug("[AuditManagerNull::insertAudit] BEGIN");
        logger.debug("[AuditManagerNull::insertAudit] END");
    }

    public OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca) throws Exception {
        logger.debug("[AuditManagerNull::ricercaAudit] BEGIN");
        logger.debug("[AuditManagerNull::ricercaAudit] END");
        return new OperazioneAudit[0];
    }

}

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

import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.integration.audittrail.dao.AuditTrailDAO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che implementa il gestore dei dati di audit trail.
 *
 * @author DoQui
 */
public final class AuditTrailManagerNull implements AuditTrailBusinessInterface, EcmEngineConstants {

    private static Log logger = LogFactory.getLog(ECMENGINE_AUDIT_TRAIL_LOG_CATEGORY);

    /** Costruttore predefinito. */
    public AuditTrailManagerNull() {}

    public void setAuditTrailDAO(AuditTrailDAO auditTrailDAO) {}

    public void logTrail(AuditInfo auditInfo) throws Exception {
        logger.debug("[AuditTrailManagerNull::logTrail] BEGIN");
        logger.debug("[AuditTrailManagerNull::logTrail] END");
    }

    public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca) throws Exception {
        logger.debug("[AuditTrailManagerNull::ricercaAuditTrail] BEGIN");
        logger.debug("[AuditTrailManagerNull::ricercaAuditTrail] END");
        return new AuditInfo[0];
    }

}

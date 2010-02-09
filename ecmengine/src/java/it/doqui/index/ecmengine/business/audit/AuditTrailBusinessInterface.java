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

/**
 * <p>Interfaccia di business del gestore dei dati di audit trail.</p>
 * 
 * @author DoQui
 */
public interface AuditTrailBusinessInterface {

	/**
	 * Inserisce una riga di audit trail nella corrispondente tabella
	 * dell'ECMENGINE.
	 * 
	 * @param auditInfo
	 *            il DTO contente le info dell'audit trail da tracciare su db.
	 * 
	 * @throws Exception
	 *             Se si verifica un errore in fase di inserimento nella tabella
	 *             dell'audit trail del ECMENGINE.
	 */

	public void logTrail(AuditInfo auditInfo) throws Exception;

	/**
	 * Restituisce tutte le entry nella tabella di audit trail che soddisfano i
	 * parametri specificati.
	 * 
	 * @param parametriRicerca
	 *            parametri in base a cui viene effettuata la ricerca nella
	 *            tabella di audit trail.
	 * 
	 * @return una array di {@link AuditInfo}.
	 * 
	 * @throws Exception
	 *             Se si verifica un errore in fase di ricerca nella tabella
	 *             dell'audit trail.
	 */
	public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca)
			throws Exception;

}
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

import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.exception.repository.AuditRuntimeException;

import javax.ejb.EJBLocalObject;

/**
 * Interfaccia pubblica del servizio di gestione degli audit trail
 * esportata come componente EJB 2.1.
 * 
 * <p>L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link AuditTrailSvcBean}.</p>
 * 
 * <p>Tutti i metodi esportati dal bean di gestione degli audit rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link it.doqui.index.ecmengine.exception.repository.AuditRuntimeException}.
 * </p>
 * 
 * @author DoQui
 * 
 * @see AuditTrailSvcBean
 * @see it.doqui.index.ecmengine.exception.repository.AuditRuntimeException
 */
public interface AuditTrailSvc extends EJBLocalObject {

	/**
	 * Inserisce una riga di audit trail nella corrispondente tabella
	 * dell'ECMENGINE.
	 * 
	 * @param audiInfo
	 *            il DTO contente le info dell'audit trail da tracciare su db.
	 * 
	 * @throws AuditRuntimeException
	 *             Se si verifica un errore in fase di inserimento o ricerca
	 *             nella tabella dell'audit trail del ECMENGINE.
	 */
	public void logTrail(AuditInfo auditInfo) throws AuditRuntimeException;

	/**
	 * Restituisce tutte le entry nella tabella di audit trail che soddisfano i
	 * parametri specificati e contenute nell'intervallo di tempo specificato.
	 * 
	 * @param parametriRicerca
	 *            parametri in base a cui viene effettuata la ricerca nella
	 *            tabella di audit trail.
	 * 
	 * @return una array di {@link AuditInfo}.
	 * 
	 * @throws AuditRuntimeException
	 *             Se si verifica un errore in fase di inserimento o ricerca
	 *             nella tabella dell'audit trail dell'ECMENGINE.
	 */
	public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca)
			throws AuditRuntimeException;

}

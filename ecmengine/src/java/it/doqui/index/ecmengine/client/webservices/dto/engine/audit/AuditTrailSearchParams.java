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

package it.doqui.index.ecmengine.client.webservices.dto.engine.audit;

import java.util.Date;

public class AuditTrailSearchParams {

	private String utente;
	private String nomeOperazione;
	private String idOggetto;
	private Date inizioIntervallo;
	private Date fineIntervallo;

	public AuditTrailSearchParams() {}

	public String getUtente() {
		return utente;
	}

	public void setUtente(String utente) {
		this.utente = utente;
	}

	public String getNomeOperazione() {
		return nomeOperazione;
	}

	public void setNomeOperazione(String nomeOperazione) {
		this.nomeOperazione = nomeOperazione;
	}

	public String getIdOggetto() {
		return idOggetto;
	}

	public void setIdOggetto(String idOggetto) {
		this.idOggetto = idOggetto;
	}

	public Date getInizioIntervallo() {
		return inizioIntervallo;
	}

	public void setInizioIntervallo(Date startDate) {
		this.inizioIntervallo = startDate;
	}

	public Date getFineIntervallo() {
		return fineIntervallo;
	}

	public void setFineIntervallo(Date endDate) {
		this.fineIntervallo = endDate;
	}

}

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

/**
 * Data Transfer Object che rappresenta le informazioni dell'audit trail
 * da tracciare sulla base dati dell'ECMENGINE. Questo DTO viene utilizzato anche 
 * come filtro per effettuare le ricerche nell'audit trail.
 * 
 * @author Doqui
 *
 */
public class AuditInfo {

	private Long id;
	private String utente;
	private String operazione;
	private String idOggetto;
	private String metaDati;
	private Date data;

	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getIdOggetto() {
		return idOggetto;
	}
	public void setIdOggetto(String idOggetto) {
		this.idOggetto = idOggetto;
	}
	public String getMetaDati() {
		return metaDati;
	}
	public void setMetaDati(String metaDati) {
		this.metaDati = metaDati;
	}
	public String getOperazione() {
		return operazione;
	}
	public void setOperazione(String operazione) {
		this.operazione = operazione;
	}
	public String getUtente() {
		return utente;
	}
	public void setUtente(String utente) {
		this.utente = utente;
	}

}

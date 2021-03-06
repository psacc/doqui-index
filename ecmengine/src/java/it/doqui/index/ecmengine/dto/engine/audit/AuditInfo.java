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

package it.doqui.index.ecmengine.dto.engine.audit;

import it.doqui.index.ecmengine.dto.EcmEngineDto;

import java.util.Date;

/**
 * Data Transfer Object che rappresenta le informazioni dell'audit trail
 * da tracciare sulla base dati dell'ECMENGINE. Questo DTO viene utilizzato anche
 * come filtro per effettuare le ricerche nell'audit trail.
 *
 * @author Doqui
 *
 */
public class AuditInfo extends EcmEngineDto {

	private static final long serialVersionUID = 7165140519146634149L;

	private Long id;
	private String utente;
	private String operazione;
	private String idOggetto;
	private String metaDati;
	private Date data;

	/**
	 * Costruttore di default.
	 *
	 */
	public AuditInfo() {
	}

	/**
	 * Restituisce la data in cui l'operazione e' stata richiamata.
	 * @return data dell'operazione.
	 */
	public Date getData() {
		return data;
	}

	/**
	 * Imposta la data in cui l'operazione e' stata richiamata.
	 * @param data data ed ora in cui l'operazione e' stata richiamata.
	 */
	public void setData(Date data) {
		this.data = data;
	}

	/**
	 * Restituisce l'identificativo del record di Audit Trail
	 * @return id dell'operazione.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Imposta l'identificativo del record di Audit Trail
	 * @param id dell'operazione.
	 */
	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * Restituisce l' identificativo del contenuto su cui viene richiesta l'operazione.
	 * @return id del contenuto su cui viene richiesta l'operazione.
	 */
	public String getIdOggetto() {
		return idOggetto;
	}

	/**
	 * Imposta l' identificativo del contenuto su cui viene richiesta l'operazione.
	 * @param idOggetto id del contenuto su cui viene richiesta l'operazione.
	 */
	public void setIdOggetto(String idOggetto) {
		this.idOggetto = idOggetto;
	}

	/**
	 * Restituisce il nome dell'operazione eseguita (ad esempio createContent).
	 * @return nome operazione eseguita (ad esempio createContent).
	 */
	public String getOperazione() {
		return operazione;
	}

	/**
	 * Imposta il nome dell'operazione eseguita (ad esempio createContent).
	 * @param operazione nome operazione eseguita (ad esempio createContent).
	 */
	public void setOperazione(String operazione) {
		this.operazione = operazione;
	}

	/**
	 * Restituisce il nome dell'utente fisico(reale) che ha effettuato
	 * l'operazione.
	 *
	 * @return Il nome dell'utente fisico.
	 */
	public String getUtente() {
		return utente;
	}

	/**
	 * Imposta il nome dell'utente fisico(reale) che ha effettuato l'operazione.
	 * @param utente Il nome dell'utente fisico.
	 */
	public void setUtente(String utente) {
		this.utente = utente;
	}

	/**
	 * Ritorna i metadati associati al contenuto oggetto dell' audit trail.
	 * @return metadati associati al contenuto.
	 */
	public String getMetaDati() {
		return metaDati;
	}

	/**
	 * Imposta i metadati associati al contenuto.
	 * @param metaDati metadati associati al contenuto.
	 */
	public void setMetaDati(String metaDati) {
		this.metaDati = metaDati;
	}

}

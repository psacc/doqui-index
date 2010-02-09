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
 
package it.doqui.index.ecmengine.integration.job.vo;

import java.util.Date;

/**
 * Value object (VO) per lo scambio dei dati della tabella dei job sul database.
 * 
 * @author DoQui
 * 
 */
public class JobVO {

	private int id;
	private String ref;
	private Date timestampCreazione;
	private String status;
	private String message;
	private Date lastUpdate;

	/**
	 * Costruttore predefinito.
	 */
	public JobVO() {
		super();
	}

	/**
	 * Restituisce l'id del job.
	 * 
	 * @return L'id del job.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Imposta l'id del job.
	 * 
	 * @param jobId L'id del job.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Restituisce l'id dell'esecutore del job.
	 * 
	 * @return L'id dell'esecutore del job.
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * Imposta l'id dell'esecutore del job.
	 * 
	 * @param ref L'id dell'esecutore del job
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}

	/**
	 * Restituisce il timestamp di ultimo aggiornamento del job.
	 * 
	 * @return Il timestamp di ultimo aggiornamento del job.
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Imposta il timestamp di ultimo aggiornamento del job.
	 * 
	 * @param lastUpdate Il timestamp di ultimo aggiornamento del job.
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Restituisce l'eventuale messaggio di errore del job ({@code null} se non &egrave; stato impostato).
	 * 
	 * @return L'eventuale messaggio di errore del job ({@code null} se non &egrave; stato impostato).
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Imposta l'eventuale messaggio di errore del job.
	 * 
	 * @param message L'eventuale messaggio di errore del job.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Restituisce lo stato del job.
	 * 
	 * @return Lo stato del job.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Imposta lo stato del job.
	 * 
	 * @param status Lo stato del job.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Restituisce il timestamp di creazione del job.
	 * 
	 * @return Il timestamp di creazione del job.
	 */
	public Date getTimestampCreazione() {
		return timestampCreazione;
	}

	/**
	 * Imposta il timestamp di creazione del job.
	 * 
	 * @param lastUpdate Il timestamp di creazione del job.
	 */
	public void setTimestampCreazione(Date timestampCreazione) {
		this.timestampCreazione = timestampCreazione;
	}

}

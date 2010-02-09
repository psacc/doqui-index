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

import java.util.Date;

import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * Data Transfer Object che rappresenta i parametri di ricerca dell'audit.
 * Tutti i campi sono opzionali e la presenza di pi&ugrave; campi impostati
 * viene gestita con l'operazione di AND logico.
 * 
 * @author Doqui
 * 
 */
public class AuditSearchParams extends EcmEngineDto {

	private static final long serialVersionUID = -1915069594006938340L;

	private String utente;
	private String nomeOperazione;
	private String fruitore;
	private String servizio;
	private String tipoOggetto;
	private String idOggetto;
	private Date inizioIntervallo;
	private Date fineIntervallo;

	public AuditSearchParams() {}

	/**
	 * Restituisce l'utente da ricercare.
	 * 
	 * @return l'utente da ricercare.
	 */
	public String getUtente() {
		return utente;
	}

	/**
	 * Imposta l'utente da ricercare.
	 * 
	 * @param utente l'utente da ricercare.
	 */
	public void setUtente(String utente) {
		this.utente = utente;
	}

	/**
	 * Restituisce il nome dell'operazione da ricercare.
	 * 
	 * @return il nome dell'operazione da ricercare.
	 */
	public String getNomeOperazione() {
		return nomeOperazione;
	}

	/**
	 * Imposta il nome dell'operazione da ricercare.
	 * 
	 * @param nomeOperazione il nome dell'operazione da ricercare.
	 */
	public void setNomeOperazione(String nomeOperazione) {
		this.nomeOperazione = nomeOperazione;
	}

	/**
	 * Restituisce il fruitore da ricercare.
	 * 
	 * @return il fruitore da ricercare.
	 */
	public String getFruitore() {
		return fruitore;
	}

	/**
	 * Imposta il fruitore da ricercare.
	 * 
	 * @param fruitore il fruitore da ricercare.
	 */
	public void setFruitore(String fruitore) {
		this.fruitore = fruitore;
	}

	/**
	 * Restituisce il servizio da ricercare.
	 * 
	 * @return il servizio da ricercare.
	 */
	public String getServizio() {
		return servizio;
	}

	/**
	 * Imposta il servizio da ricercare.
	 * 
	 * @param servizio il servizio da ricercare.
	 */
	public void setServizio(String servizio) {
		this.servizio = servizio;
	}

	/**
	 * Restituisce il tipo di oggetto da ricercare.
	 * 
	 * @return il tipo di oggetto da ricercare.
	 */
	public String getTipoOggetto() {
		return tipoOggetto;
	}

	/**
	 * Imposta il tipo di oggetto da ricercare.
	 * 
	 * @param tipoOggetto il tipo di oggetto da ricercare.
	 */
	public void setTipoOggetto(String tipoOggetto) {
		this.tipoOggetto = tipoOggetto;
	}

	/**
	 * Restituisce l'id dell'oggetto da ricercare.
	 * 
	 * @return l'id dell'oggetto da ricercare.
	 */
	public String getIdOggetto() {
		return idOggetto;
	}

	/**
	 * Imposta l'id dell'oggetto da ricercare.
	 * 
	 * @param idOggetto l'id dell'oggetto da ricercare.
	 */
	public void setIdOggetto(String idOggetto) {
		this.idOggetto = idOggetto;
	}

	/**
	 * Restituisce l'inizio dell'intervallo temporale nel quale effettuare la
	 * ricerca.
	 * 
	 * @return
	 *            l'inizio dell'intervallo temporale nel quale effettuare la
	 *            ricerca.
	 */
	public Date getInizioIntervallo() {
		return inizioIntervallo;
	}

	/**
	 * Imposta l'inizio dell'intervallo temporale nel quale effettuare la
	 * ricerca.
	 * 
	 * @param startDate
	 *            l'inizio dell'intervallo temporale nel quale effettuare la
	 *            ricerca.
	 */
	public void setInizioIntervallo(Date startDate) {
		this.inizioIntervallo = startDate;
	}

	/**
	 * Restituisce la fine dell'intervallo temporale nel quale effettuare la
	 * ricerca.
	 * 
	 * @return
	 *            la fine dell'intervallo temporale nel quale effettuare la
	 *            ricerca.
	 */
	public Date getFineIntervallo() {
		return fineIntervallo;
	}

	/**
	 * Imposta la fine dell'intervallo temporale nel quale effettuare la
	 * ricerca.
	 * 
	 * @param endDate
	 *            la fine dell'intervallo temporale nel quale effettuare la
	 *            ricerca.
	 */
	public void setFineIntervallo(Date endDate) {
		this.fineIntervallo = endDate;
	}

}

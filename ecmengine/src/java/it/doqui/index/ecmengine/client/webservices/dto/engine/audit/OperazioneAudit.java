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
 * Data Transfer Object che rappresenta un operazione eseguita sul repository
 * da tracciare sulla base dati dell'ECMENGINE.
 *
 * @author Doqui
 */
public class OperazioneAudit {



	private Long id;
	private String fruitore;
	private String utente;
	private String servizio;
	private String nomeOperazione;
	private java.util.Date dataOra;
	private String idOggetto;
	private String tipoOggetto;

	/**
	 * Costruttore di default.
	 *
	 */
	public OperazioneAudit() {
	}

	/**
	 * Restituisce l'identificativo dell'operazione soggetta
	 * ad audit.
	 *
	 * @return id dell'operazione
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Imposta l'identificativo univoco dell'operazione
	 * soggetta ad audit
	 *
	 * @param id identificativo univoco
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Restituisce la data e l'ora in cui l'operazione
	 * e' stata richiamata
	 * @return data dell'operazione
	 */
	public Date getDataOra() {
		return dataOra;
	}

	/**
	 * Imposta la data e l'ora in cui l'operazione
	 * e' stata richiamata
	 * @param dataOra data ed ora in cui l'operazione  e' stata richiamata
	 */
	public void setDataOra(Date dataOra) {
		this.dataOra = dataOra;
	}

	/**
	 * Restituisce l' identificativo del contenuto
	 * (fascicolo,dossier,serie,ecc..) su cui viene richiesta l'operazione
	 * @return id del contenuto(fascicolo,dossier,serie,ecc..) su cui viene richiesta l'operazione
	 */
	public String getIdOggetto() {
		return idOggetto;
	}

	/**
	 * Imposta l'identificativo del contenuto(fascicolo,dossier,serie,ecc..)
	 * su cui viene richiesta l'operazione
	 * @param idOggetto id del contenuto(fascicolo,dossier,serie,ecc..) su cui viene richiesta l'operazione
	 */
	public void setIdOggetto(String idOggetto) {
		this.idOggetto = idOggetto;
	}

	/**
	 * Restituisce il tipo dell'oggetto , cioe' del contenuto su
	 * cui viene richiesta l'operazione
	 * @return il tipo dell'oggetto (contenuto su cui viene richiesta l'operazione)
	 */
	public String getTipoOggetto() {
		return tipoOggetto;
	}

	/**
	 * Imposta il tipo dell' oggetto, cio&egrave; del contenuto
	 * su cui viene richiesta l'operazione
	 * @param tipoOggetto il tipo dell' oggetto
	 */
	public void setTipoOggetto(String tipoOggetto) {
		this.tipoOggetto = tipoOggetto;
	}

	/**
	 * Restituisce il nome dell'operazione eseguita (ad esempio creaFascicolo)
	 * @return nome operazione eseguita (ad esempio creaFascicolo)
	 */
	public String getNomeOperazione() {
		return nomeOperazione;
	}

	/**
	 * Imposta il nome dell'operazione eseguita (ad esempio creaFascicolo)
	 * @param nomeOperazione  nome operazione eseguita (ad esempio creaFascicolo)
	 */
	public void setNomeOperazione(String nomeOperazione) {
		this.nomeOperazione = nomeOperazione;
	}

	/**
	 * Restituisce il servizio chiamato
	 * (ad esempio, Management per i servizi di gestione dei contenuti, Search per i servizi di ricerca).
	 * @return servizio chiamato
	 */
	public String getServizio() {
		return servizio;
	}

	/**
	 * Imposta il servizio chiamato
	 * (ad esempio, Management per i servizi di gestione dei contenuti, Search per i servizi di ricerca).
	 * @param servizio servizio chiamato
	 */
	public void setServizio(String servizio) {
		this.servizio = servizio;
	}

	/**
	 * Restituisce il nome dell'utente fisico(reale) che ha effettuato
	 * l'operazione sui servizi dell'ECMENGINE.
	 *
	 * @return Il nome dell'utente fisico.
	 */
	public String getUtente() {
		return utente;
	}

	/**
	 * Imposta il nome dell'utente fisico(reale) che ha effettuato l'operazione
	 * sui servizi dell'ECMENGINE.
	 *
	 * @param utente Il nome dell'utente fisico.
	 */
	public void setUtente(String utente) {
		this.utente = utente;
	}

	/**
	 * Restituisce il nome dell'applicativo fruitore dell'ECMENGINE,
	 * che ha richiesto l'operazione soggetta ad audit (ad esempio SIDE, CEDOLINI)
	 *
	 * @return Il nome del fruitore.
	 */
	public String getFruitore() {
		return fruitore;
	}

	/**
	 * Imposta il nome dell'applicativo fruitore dell'ECMENGINE,
	 * che ha richiesto l'operazione soggetta ad audit (ad esempio SIDE, CEDOLINI)
	 *
	 * @param fruitore Il nome dell'applicativo fruitore.
	 */
	public void setFruitore(String fruitore) {
		this.fruitore = fruitore;
	}

}

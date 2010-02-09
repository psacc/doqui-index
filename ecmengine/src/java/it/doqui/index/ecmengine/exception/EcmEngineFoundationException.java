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
 
package it.doqui.index.ecmengine.exception;

import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;

/**
 * Generica eccezione che indica una condizione di
 * errore verificatasi in uno dei servizi applicativi dell'ECMENGINE.
 * 
 * @author Doqui
 */
public class EcmEngineFoundationException extends Exception {

	private static final long serialVersionUID = -9217086628026949818L;
	
	/**
	 * Codice di errore associato all'istanza dell'eccezione.
	 * 
	 * <p>Questo campo &egrave; disponibile per l'utilizzo nelle sottoclassi
	 * di {@code EcmEngineFoundationException}.</p>
	 */
	protected FoundationErrorCodes code;

	/**
	 * Costruttore di default utilizzabile nelle sottoclassi.
	 *
	 */
	protected EcmEngineFoundationException() {
		super();
		this.code = FoundationErrorCodes.UNKNOWN_ERROR;
	}
	
	/**
	 * Costruttore protected utilizzabile nelle sottoclassi che permette
	 * di specificare un codice di errore.
	 * 
	 * @param errorCode Il codice di errore.
	 */
	protected EcmEngineFoundationException(FoundationErrorCodes errorCode) {
		super();
		this.code = errorCode;
	}
	
	/**
	 * Costruttore che permette di associare un messaggio di errore alla
	 * nuova istanza di {@code EcmEngineFoundationException}.
	 * 
	 * @param msg Il messaggio di errore.
	 */
	public EcmEngineFoundationException(String msg) {
		super(msg);
	}

	/**
	 * Costruttore che permette di associare un messaggio di errore e una
	 * causa alla nuova istanza di {@code EcmEngineFoundationException}.
	 * 
	 * @param msg Il messaggio di errore.
	 * @param cause La causa dell'errore.
	 */
	public EcmEngineFoundationException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * Restituisce il codice di errore associato a questa eccezione.
	 * 
	 * @return Il codice di errore associato a questa eccezione.
	 */
	public final FoundationErrorCodes getCode() {
		return this.code;
	}
}

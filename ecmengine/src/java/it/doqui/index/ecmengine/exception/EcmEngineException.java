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

import it.csi.csi.wrapper.UserException;

/**
 * Generica eccezione (checked) che indica una condizione di
 * errore fatale verificatasi in uno dei servizi dell'ECMENGINE. 
 * 
 * Un'eccezione
 * di questo tipo indica che l'applicativo ha cessato di funzionare
 * correttamente e necessita di manutenzione.
 * 
 * @author Doqui
 */
public class EcmEngineException extends UserException {

	private static final long serialVersionUID = 605940437080936786L;
	
	private String msg;

	/**
	 * Costruttore che permette di associare un messaggio di errore alla
	 * nuova istanza di {@code EcmEngineException}.
	 * 
	 * @param msg Il messaggio di errore.
	 */
	public EcmEngineException(String msg) {
		super(msg);
		this.msg = msg;
	}

	/**
	 * Costruttore che permette di associare un messaggio di errore e una
	 * causa alla nuova istanza di {@code EcmEngineException}.
	 * 
	 * @param msg Il messaggio di errore.
	 * @param cause La causa dell'errore.
	 */
	public EcmEngineException(String msg, Throwable cause) {
		super(msg, cause);
		this.msg = msg;
	}

	/**
	 * Override necessario a preservare il messaggio nell'utilizzo via
	 * web-service con {@code UserException} impostata come stop-class.
	 * 
	 * @return Il messaggio di errore.
	 */
	public String getMessage() {
		if (this.msg != null) {
			return this.msg;
		} else {
			return super.getMessage();
		}
	}
	
	/**
	 * Imposta il messaggio di errore.
	 * 
	 * @param message Il messaggio di errore.
	 */
	public void setMessage(String message) {
		this.msg = message;
	}
}

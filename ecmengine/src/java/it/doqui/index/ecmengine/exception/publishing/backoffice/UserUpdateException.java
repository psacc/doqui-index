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
 
package it.doqui.index.ecmengine.exception.publishing.backoffice;

import it.doqui.index.ecmengine.exception.EcmEngineException;

/**
 * Eccezione che rappresenta un errore in fase di modifica di un utente
 * applicativo.
 * 
 * <p>Questa eccezione pu&ograve; essere sollevata dal servizio di modifica degli
 * utenti del backoffice qualora si verifichi un errore che impedisce la modifica
 * dell'utente esistente.</p>
 * 
 * @author Doqui
 * 
 */
public class UserUpdateException extends EcmEngineException {

	private static final long serialVersionUID = -2995949671401372493L;

	/**
	 * Costruttore che crea una nuova istanza di {@code UserUpdateException}
	 * inizializzandola con un messaggio che specifica il nome dell'utente la cui modifica ha
	 * sollevato l'errore.
	 * 
	 * @param userName Il nome dell'utente la cui modifica ha sollevato l'errore.
	 */
	public UserUpdateException(String userName) {
		super("Impossibile modificare l'utente: " + userName);
	}
	
	/**
	 * Costruttore che crea una nuova istanza di {@code UserUpdateException}
	 * inizializzandola con un messaggio che specifica il nome dell'utente la cui modifica ha
	 * sollevato l'errore e con la causa dell'errore.
	 * 
	 * @param userName Il nome dell'utente la cui modifica ha sollevato l'errore.
	 * @param cause La causa dell'errore.
	 */
	public UserUpdateException(String userName, Throwable cause) {
		super("Impossibile modificare l'utente: " + userName);
		initCause(cause);
	}

}

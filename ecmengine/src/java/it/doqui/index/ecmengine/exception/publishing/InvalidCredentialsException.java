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
 
package it.doqui.index.ecmengine.exception.publishing;

import it.doqui.index.ecmengine.exception.EcmEngineException;

/**
 * Eccezione che rappresenta un errore di tipo &quot;credenziali di autenticazione non valide&quot;.
 * 
 * @author Doqui.
 */
public class InvalidCredentialsException extends EcmEngineException {

	private static final long serialVersionUID = -7502886465327961680L;
		
	/**
	 * Costruisce una nuova istanza di {@code InvalidCredentialsException}
	 * con un messaggio di errore predefinito.
	 */
	public InvalidCredentialsException() {
		super("Invalid username or password!");
	}
	
	/**
	 * Costruisce una nuova istanza di {@code InvalidCredentialsException}
	 * contenente il messaggio di errore specificato.
	 * 
	 * @param msg Il messaggio di errore.
	 */
	public InvalidCredentialsException(String msg) {
		super(msg);
	}
}

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
 * Eccezione che rappresenta un errore in fase di creazione di un gruppo
 * applicativo.
 * 
 * <p>Questa eccezione pu&ograve; essere sollevata dal servizio di creazione dei
 * gruppi del backoffice qualora si verifichi un errore che impedisce la creazione
 * del nuovo gruppo (solo se tale gruppo non esiste gi&agrave;).</p>
 * 
 * @author Doqui
 */
public class GroupCreateException extends EcmEngineException {

	private static final long serialVersionUID = -2176066905705611811L;

	/**
	 * Costruttore che crea una nuova istanza di {@code GroupCreateException}
	 * inizializzandola con un messaggio che specifica il nome dell'utente la cui creazione ha
	 * sollevato l'errore.
	 * 
	 * @param groupName Il nome del gruppo la cui creazione ha sollevato l'errore.
	 */
	public GroupCreateException(String groupName) {
		super("Impossibile creare il gruppo: " + groupName);
	}
	
	/**
	 * Costruttore che crea una nuova istanza di {@code GroupCreateException}
	 * inizializzandola con un messaggio che specifica il nome del gruppo la cui creazione ha
	 * sollevato l'errore e con la causa dell'errore.
	 * 
	 * @param groupName Il nome del gruppo la cui creazione ha sollevato l'errore.
	 * @param cause La causa dell'errore.
	 */
	public GroupCreateException(String groupName, Throwable cause) {
		super("Impossibile creare il gruppo: " + groupName);
		initCause(cause);
	}
}

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
 * <p>Eccezione che rappresenta una condizione di &quot;
 * gruppo non esistente&quot;.</p>
 * 
 * <p>Questa eccezione pu&ograve; essere sollevata dal componente di backoffice qualora 
 * venga rilevato che il gruppo su cui deve essere eseguita un'operazione
 * non sia esistente sul repository.
 * 
 * @author Doqui
 */
public class NoSuchGroupException extends EcmEngineException {

	private static final long serialVersionUID = 2806845714294156239L;
	
	/**
	 * Costruttore che crea una nuova istanza di {@code NoSuchGroupException}
	 * inizializzandola con un messaggio che specifica il nome del gruppo mancante.
	 * 
	 * @param groupName Il nome del gruppo che &egrave; stato rilevato come &quot;mancante&quot;.
	 */
	public NoSuchGroupException(String groupName) {
		super("Gruppo mancante: " + groupName);
	}
}

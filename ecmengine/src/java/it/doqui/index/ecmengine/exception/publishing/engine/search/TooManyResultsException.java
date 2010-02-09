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
 
package it.doqui.index.ecmengine.exception.publishing.engine.search;

import it.doqui.index.ecmengine.exception.EcmEngineException;

/**
 * Eccezione checked che rappresenta una condizione di &quot;result set troppo grande&quot;.
 * 
 * <p>Questa eccezione viene lanciata da servizio di ricerca nel caso il result set restituito
 * da una query fosse pi&ugrave; grande di una dimensione prefissata. Se si verifica una tale
 * condizione il fruitore del servizio di ricerca <i>dovrebbe</i> eseguire nuovamente la query con
 * parametri pi&ugrave; specifici.</p>
 * 
 * @author Doqui
 */
public class TooManyResultsException extends EcmEngineException {
	
	private static final long serialVersionUID = 9150084223479315128L;

	/**
	 * Costruttore che crea una nuova istanza di {@code TooManyResultsException}
	 * inizializzandola con un messaggio di errore.
	 * 
	 * @param msg Il messaggio di errore.
	 */
	public TooManyResultsException(String msg) {
		super(msg);
	}
}

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
 * Eccezione che rappresenta una condizione di nessun dato trovato
 * a fronte di una ricerca sul repository per path.
 * 
 * @author Doqui
 */
public class NoDataExtractedException  extends EcmEngineException{

	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore che crea una nuova istanza di {@code NoDataExtractedException}
	 * inizializzandola con un messaggio che specifica la chiave della risorsa cercata.
	 * 
	 * @param chiave La chiave di ricerca della risorsa che si intendeva cercare.
	 */
	public NoDataExtractedException(String chiave) {
		super("Nessun dato trovato per la chiave '" + chiave + "'");
	}

	/**
	 * Costruttore che crea una nuova istanza di {@code NoDataExtractedException}
	 * inizializzandola con un messaggio che specifica la chiave e il repository della risorsa cercata.
	 * 
	 * @param chiave La chiave della risorsa che si intendeva cercare.
	 * @param repository L'identificativo del repository su cui si &egrave; eseguita la ricerca.
	 */
	public NoDataExtractedException(String chiave, String repository) {
		super("Nessun dato trovato per la chiave '" + chiave 
				+ "' (nel repository " + (repository == null ? "default)" : repository + ")"));
	}
}

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
 
package it.doqui.index.ecmengine.exception.publishing.engine;

import it.doqui.index.ecmengine.exception.EcmEngineException;

/**
 * Eccezione checked che rappresenta un errore di tipo &quot;store non trovato&quot;.
 * 
 * @author Doqui.
 */
public class StoreNotFoundException extends EcmEngineException {

	private static final long serialVersionUID = -7502886465327961680L;
	private String idStore;
	
	/**
	 * Costruisce una nuova istanza di {@code StoreNotFoundException}.
	 * 
	 * @param inIdStore L'ID univoco dello Store non trovato.
	 */
	public StoreNotFoundException(String inIdStore) {
		super("Store non trovato: " + inIdStore);
		this.idStore = inIdStore;
	}
	
	/**
	 * Restituisce l'identificatore univoco dello store non trovato.
	 * 
	 * @return L'ID univoco dello store non trovato.
	 */
	public final String getIdStore() {
		return this.idStore;
	}
	
}

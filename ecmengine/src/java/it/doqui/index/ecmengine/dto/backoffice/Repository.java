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

package it.doqui.index.ecmengine.dto.backoffice;

import it.doqui.index.ecmengine.dto.EcmEngineDto;


/**
 * DTO che rappresenta un repository, per repository si intende quello che e' stato
 * presentato con il nome di repository fisico (corrente e deposito),
 * L'oggetto che viene passato alle operazioni identifica, nel caso in cui si stiano
 * gestendo piu' repository, su quale repository si vuole svolgere l'operazione
 *
 * @author doqui
 *
 */

public class Repository extends EcmEngineDto {

	private static final long serialVersionUID = -4852447349533448056L;

	/**
	 * Identificativo del repository
	 */
	private String id;

    // MB: per gestione MultiContentStore
    private ContentStoreDefinition[] contentStore;

	public Repository() {}

	public Repository(String id) {
		this.id = id;
	}

	/**
	 * Restituisce l'identificativo del repository
	 *
	 * @return identificativo del repository
	 */
	public String getId() {
		return id;
	}

	/**
	 * Imposta l'identificativo del repository
	 *
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Restituisce l'elenco di ContentStoreDefinition attivi su questo repository
	 *
	 * @return Un vettore di ContentStoreDefinition
	 */
	public ContentStoreDefinition[] getContentStores() {
		return contentStore;
	}

	/**
	 * Imposta l'elenco di ContentStoreDefinition attivi su questo repository
	 *
	 * @param contentStore L'elenco di contentStoreDefinition da usare su questo repository
	 */
	public void setContentStores(ContentStoreDefinition[] contentStore) {
		this.contentStore = contentStore;
	}

}

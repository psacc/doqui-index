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

package it.doqui.index.ecmengine.dto.engine.management;
/**
 * DTO che rappresenta un file non contenuto in ECMEngine di cui si vuol eseguire un
 * riconoscimento di tipo con FileFormat.
 * @author Doqui
 */
import it.doqui.index.ecmengine.dto.EcmEngineDto;

public class FileInfo extends EcmEngineDto {

	private static final long serialVersionUID = 5184891831925731705L;
	private String name;
	private byte[] contents;
	private boolean store=false;

	public FileInfo() {
		super();
	}

	/**
	 * Verifica se il contenuto deve essere memorizzato in un tenant temporaneo dopo il
	 * riconoscimento.
	 * @return true se il contenuto deve essere memorizzato, false altrimenti.
	 */
	public boolean isStore() {
		return store;
	}

	/**
	 * Imposta se il contenuto deve essere memorizzato in un tenant temporaneo dopo il
	 * riconoscimento.
	 * @param store
	 */
	public void setStore(boolean store) {
		this.store = store;
	}

	/**
	 * Restituisce il contenuto del file.
	 * @return Il contenuto del file.
	 */
	public byte[] getContents() {
		return contents;
	}

	/**
	 * Imposta il contenuto del file.
	 * @param contents Il contenuto del file.
	 */
	public void setContents(byte[] contents) {
		this.contents = contents;
	}
	
	/**
	 * Restituisce il nome del file.
	 * @return Il nome del file.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Imposta il nome del file.
	 * @param name Il nome del file.
	 */
	public void setName(String name) {
		this.name = name;
	}

}

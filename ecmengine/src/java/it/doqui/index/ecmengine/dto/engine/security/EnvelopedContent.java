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

package it.doqui.index.ecmengine.dto.engine.security;

/**
 * DTO che rappresenta un documento imbustato.
 * @author Doqui
 */
import it.doqui.index.ecmengine.dto.EcmEngineDto;

public class EnvelopedContent extends EcmEngineDto {

	private static final long serialVersionUID = 2268478121775392155L;
	private byte[]data;
	private boolean store;
	
	/**
	 * Restituisce il contenuto del documento imbustato.
	 * @return Il contenuto del documento imbustato.
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Imposta il contenuto del documento imbustato.
	 * @param data Il contenuto del documento imbustato.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Verfica se il contenuto dovr&agrave essere salvato su ECMEngine dopo l'operazione richiesta.
	 * @return true, se il documento dovr&agrave essere salvato, false altrimenti.
	 */
	public boolean isStore() {
		return store;
	}
	
	/**
	 * Imposta se il contenuto dovr&agrave essere salvato su ECMEngine dopo l'operazione richiesta.
	 * @param store
	 */
	public void setStore(boolean store) {
		this.store = store;
	}
}

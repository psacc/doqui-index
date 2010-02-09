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
 * DTO che rappresenta un documento.
 * @author Doqui
 */
import it.doqui.index.ecmengine.dto.EcmEngineDto;

public class Document extends EcmEngineDto{

	private static final long serialVersionUID = 1015929360387594667L;
	private byte[] buffer;
	private String uid;

	/**
	 * Restituisce il contenuto del documento.
	 * @return Il contenuto del documento.
	 */
	public byte[] getBuffer() {
		return buffer;
	}

	/**
	 * Imposta il contenuto del documento.
	 * @param buffer Il contenuto del documento.
	 */
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	/**
	 * Restituisce l'eventuale uid del documento.
	 * @return L'eventuale uid del documento.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Imposta l'eventuale uid del documento.
	 * @param uid L'uid del documento.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

}

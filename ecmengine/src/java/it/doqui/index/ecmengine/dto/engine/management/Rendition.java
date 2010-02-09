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
 * Classe DTO che rappresenta una rendition di un documento di ECMENGINE.
 * 
 * @author Doqui
 */
public class Rendition{

	private static final long serialVersionUID = -18041131739547456L;
	private String mimeType;
	private String encoding;
	private byte [] content;
	private EncryptionInfo encryptionInfo;

	/**
	 * Costruttore predefinito.
	 */
	public Rendition() {
		this.mimeType = null;
		this.encoding = null;
		this.content = null;
	}

	/**
	 * Restituisce il MIME-Type associato al contenuto fisico.
	 * 
	 * @return Il MIME-Type.
	 */
	public String getMimeType() {
		return this.mimeType;
	}

	/**
	 * Imposta il MIME-Type associato al contenuto fisico.
	 * 
	 * @param mimeType Il MIME-Type.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Restituisce l'encoding associato al contenuto fisico.
	 * 
	 * @return L'encoding.
	 */
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * Imposta l'encoding associato al contenuto fisico.
	 * 
	 * @param encoding L'encoding.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Restituisce il contenuto fisico come array di {@code byte}.
	 * 
	 * @return Il contenuto fisico.
	 */
	public byte[] getContent() {
		return this.content;
	}

	/**
	 * Imposta il contenuto fisico a partire da un array di byte.
	 * 
	 * @param content Il contenuto fisico.
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * Restituisce le informazioni di crittazione (se presenti).
	 * 
	 * @return Un oggetto {@link EncryptionInfo}.
	 */
	public EncryptionInfo getEncryptionInfo() {
		return encryptionInfo;
	}

	/**
	 * Imposta le informazioni di crittazione.
	 * 
	 * @param encryptionInfo Un'istanza di {@link EncryptionInfo}.
	 */
	public void setEncryptionInfo(EncryptionInfo encryptionInfo) {
		this.encryptionInfo = encryptionInfo;
	}
}

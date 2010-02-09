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

package it.doqui.index.ecmengine.client.webservices.dto.engine.management;

/**
 * Classe DTO che rappresenta la chiave utilizzata per cifrare/decifrare un generico
 * content nel repository dell'ECMENGINE.
 *  
 * @author Doqui
 */
public class EncryptionInfo {

	private String key;
	private String algorithm;
	private String padding;
	private String mode;
	private String keyId;
	private String sourceIV;
	private boolean sourceEncrypted;
	private boolean corruptedEncryptionInfo;

	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public boolean isCorruptedEncryptionInfo() {
		return corruptedEncryptionInfo;
	}
	public void setCorruptedEncryptionInfo(boolean corruptedEncryptionInfo) {
		this.corruptedEncryptionInfo = corruptedEncryptionInfo;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getPadding() {
		return padding;
	}
	public void setPadding(String padding) {
		this.padding = padding;
	}
	public boolean isSourceEncrypted() {
		return sourceEncrypted;
	}
	public void setSourceEncrypted(boolean sourceEncrypted) {
		this.sourceEncrypted = sourceEncrypted;
	}
	public String getSourceIV() {
		return sourceIV;
	}
	public void setSourceIV(String sourceIV) {
		this.sourceIV = sourceIV;
	}

}

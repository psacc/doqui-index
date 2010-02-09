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

import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.management.ItemsContainer;
import it.doqui.index.ecmengine.dto.engine.management.PropertiesContainer;

/**
 * Classe DTO che rappresenta un <i>contenuto</i> da caricare sull'ECMENGINE.
 * 
 * <p>
 * Un contenuto &egrave; sempre caratterizzato da un nome (completo di prefisso)
 * che lo identifica nella relazione con il suo contenuto padre, dal nome del
 * tipo e dal nome del modello in cui &egrave; contenuta la definizione del
 * tipo. Esso inoltre pu&ograve; contenere una serie di {@link Aspect} e di
 * {@link Property}.
 * </p>
 * 
 * <p>
 * <strong>NB:</strong> dal punto di vista dei servizi di gestione dei
 * contenuti il nome completo ottenuto richiamando il metodo
 * {@link #getPrefixedName()} deve essere usato come nome della relazione
 * &quot;padre-figlio&quot; che collega il padre specificato al contenuto
 * aggiunto.
 * </p>
 * 
 * @see ItemsContainer
 * @see PropertiesContainer
 * 
 * @author Doqui
 */
public class Content {

	private String prefixedName;
	private String typePrefixedName;
	private String modelPrefixedName;
	private String parentAssocTypePrefixedName;
	private String contentPropertyPrefixedName;
	private Aspect[] aspects;
	private String mimeType;
	private String encoding;
	private byte[] content;
	private EncryptionInfo encryptionInfo;
	private Property[] properties;

	public Aspect[] getAspects() {
		return aspects;
	}

	public void setAspects(Aspect[] aspects) {
		this.aspects = aspects;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getContentPropertyPrefixedName() {
		return contentPropertyPrefixedName;
	}

	public void setContentPropertyPrefixedName(
			String contentPropertyPrefixedName) {
		this.contentPropertyPrefixedName = contentPropertyPrefixedName;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public EncryptionInfo getEncryptionInfo() {
		return encryptionInfo;
	}

	public void setEncryptionInfo(EncryptionInfo encryptionInfo) {
		this.encryptionInfo = encryptionInfo;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getModelPrefixedName() {
		return modelPrefixedName;
	}

	public void setModelPrefixedName(String modelPrefixedName) {
		this.modelPrefixedName = modelPrefixedName;
	}

	public String getParentAssocTypePrefixedName() {
		return parentAssocTypePrefixedName;
	}

	public void setParentAssocTypePrefixedName(
			String parentAssocTypePrefixedName) {
		this.parentAssocTypePrefixedName = parentAssocTypePrefixedName;
	}

	public String getPrefixedName() {
		return prefixedName;
	}

	public void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}

	public Property[] getProperties() {
		return properties;
	}

	public void setProperties(Property[] properties) {
		this.properties = properties;
	}

	public String getTypePrefixedName() {
		return typePrefixedName;
	}

	public void setTypePrefixedName(String typePrefixedName) {
		this.typePrefixedName = typePrefixedName;
	}

}

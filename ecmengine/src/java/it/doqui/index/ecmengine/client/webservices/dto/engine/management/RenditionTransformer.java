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
 * Classe DTO che rappresenta un XSL usato come trasformatore per ottenenere una <i>rendition</i> a partire da un XML.
 *
 * <p>Un <i>Rendition Transformer</i> &egrave; caratterizzato da un nodeId, che identifica il nodo in cui di trova, da una descrizione
 * e dal MIME Type del {@link RenditionDocument} generato.
 * Viene associato ad un XML tramite l'Aspect <code>ecm-sys:renditionable</code>.</p>
 *
 *
 * @author Doqui
 */
public class RenditionTransformer {



	private String nodeId;
	private String description;
	private String genMymeType;

	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getGenMymeType() {
		return genMymeType;
	}
	public void setGenMymeType(String genMymeType) {
		this.genMymeType = genMymeType;
	}

// -- CONTENT --
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
// -- CONTENT --

}

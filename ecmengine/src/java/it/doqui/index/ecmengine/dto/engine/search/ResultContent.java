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

package it.doqui.index.ecmengine.dto.engine.search;

import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;

/**
 * @author Doqui
 *
 */
public class ResultContent extends ResultItemContainer {

	private static final long serialVersionUID = -1972630340544858139L;

	private String uid;
	private String contentPropertyPrefixedName;
	private String typePrefixedName;
	private String mimeType;
	private String encoding;
	private long size;
	private ResultAspect[] aspects;
	private EncryptionInfo encryptionInfo;
	private String modelPrefixedName;
	private String parentAssocTypePrefixedName;

	/**
	 * Costruttore predefinito.
	 */
	public ResultContent() {
		super();

		this.uid = null;
		this.contentPropertyPrefixedName = null;
		this.typePrefixedName = null;
		this.mimeType = null;
		this.encoding = null;
		this.aspects = null;
	}

	/**
	 * Restituisce l'insieme degli aspect associati all'oggetto corrente.
	 *
	 * @return L'array degli {@link ResultAspect} assegnati al contenuto.
	 */
	public ResultAspect[] getAspects() {
		return aspects;
	}

	/**
	 * Imposta l'insieme degli aspect associati all'oggetto corrente.
	 *
	 * @param aspects L'array degli {@link ResultAspect} assegnati al contenuto.
	 */
	public void setAspects(ResultAspect [] aspects) {
		this.aspects = aspects;
	}

	/**
	 * Restituisce l'encoding corrispondente al contenuto fisico.
	 *
	 * @return L'encoding del contenuto.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Imposta l'encoding corrispondente al contenuto fisico.
	 *
	 * @param encoding L'encoding del contenuto.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Restituisce il MIME-Type corrispondente al contenuto fisico.
	 *
	 * @return Il MIME-Type del contenuto.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Imposta il MIME-Type corrispondente al contenuto fisico.
	 *
	 * @param mimeType Il MIME-Type del contenuto.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Restituisce il nome, completo di prefisso, del tipo del contenuto corrente.
	 *
	 * @return Il nome del tipo del contenuto.
	 */
	public String getTypePrefixedName() {
		return typePrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, del tipo del contenuto corrente.
	 *
	 * @param prefixedName Il nome del tipo del contenuto.
	 */
	public void setTypePrefixedName(String prefixedName) {
		this.typePrefixedName = prefixedName;
	}

	/**
	 * Restituisce l'identificativo univoco (UID) del contenuto corrente.
	 *
	 * @return L'identificatore del contenuto.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Imposta l'identificativo univoco (UID) del contenuto corrente.
	 *
	 * @param uid L'identificativo del contenuto.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * Restituisce il nome, completo di prefisso, della property che contiene i
	 * riferimenti al contenuto fisico.
	 *
	 * @return Il nome della property che contiene i riferimenti al contenuto fisico.
	 */
	public String getContentPropertyPrefixedName() {
		return contentPropertyPrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, della property che contiene i
	 * riferimenti al contenuto fisico.
	 *
	 * @param prefixedName Il nome della property che contiene i riferimenti
	 * al contenuto fisico.
	 */
	public void setContentPropertyPrefixedName(String prefixedName) {
		this.contentPropertyPrefixedName = prefixedName;
	}

	/**
	 * Restituisce la dimensione (in byte) del contenuto fisico associato al contenuto
	 * corrente.
	 *
	 * @return La dimensione in byte.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Imposta la dimensione (in byte) del contenuto fisico associato al contenuto
	 * corrente.
	 *
	 * @param size La dimensione in byte.
	 */
	public void setSize(long size) {
		this.size = size;
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

	/**
	 * Restituisce il nome, completo di prefisso, del modello in cui &egrave; definito il tipo
	 * del contenuto corrente.
	 *
	 * @return Il nome del modello in cui &egrave; definito il tipo.
	 */
	public String getModelPrefixedName() {
		return modelPrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, del modello in cui &egrave; definito il tipo
	 * del contenuto corrente.
	 *
	 * @param modelPrefixedName Il nome del modello in cui &egrave; definito il tipo.
	 */
	public void setModelPrefixedName(String modelPrefixedName) {
		this.modelPrefixedName = modelPrefixedName;
	}

	/**
	 * Restituisce il nome, completo di prefisso, del tipo di associazione
	 * &quot;padre-figlio&quot; con cui questo contenuto deve essere legato
	 * al proprio padre.
	 *
	 * @return Il nome completo di prefisso del tipo di associazione.
	 */
	public String getParentAssocTypePrefixedName() {
		return parentAssocTypePrefixedName;
	}

	/**
	 * Imposta il nome completo del tipo di associazione &quot;padre-figlio&quot; con cui
	 * questo contenuto deve essere legato al proprio padre.
	 *
	 * @param prefixedName Il nome completo di prefisso del tipo di associazione.
	 */
	public void setParentAssocTypePrefixedName(String prefixedName) {
		this.parentAssocTypePrefixedName = prefixedName;
	}

}

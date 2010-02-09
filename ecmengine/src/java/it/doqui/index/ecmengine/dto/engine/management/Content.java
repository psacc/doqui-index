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

import java.util.Vector;

/**
 * Classe DTO che rappresenta un <i>contenuto</i> da caricare sull'ECMENGINE.
 * 
 * <p>Un contenuto &egrave; sempre caratterizzato da un nome (completo di prefisso)
 * che lo identifica nella relazione con il suo contenuto padre, dal nome del tipo e
 * dal nome del modello in cui &egrave; contenuta la definizione del tipo. Esso inoltre
 * pu&ograve; contenere una serie di {@link Aspect} e di {@link Property}.</p>
 * 
 * <p><strong>NB:</strong> dal punto di vista dei servizi di gestione dei contenuti il
 * nome completo ottenuto richiamando il metodo {@link #getPrefixedName()} deve essere
 * usato come nome della relazione &quot;padre-figlio&quot; che collega il padre
 * specificato al contenuto aggiunto.</p>
 * 
 * @see ItemsContainer
 * @see PropertiesContainer
 * 
 * @author Doqui
 */
public class Content extends ItemsContainer {

	private static final long serialVersionUID = -18041131739547612L;

	private String typePrefixedName;
	private String modelPrefixedName;
	private String parentAssocTypePrefixedName;
	private String contentPropertyPrefixedName;
	private Aspect [] aspects;
	private String mimeType;
	private String encoding;
	private byte [] content;
	private EncryptionInfo encryptionInfo;

	/**
	 * Costruttore predefinito.
	 */
	public Content() {
		super();
		
		this.typePrefixedName = null;
		this.modelPrefixedName = null;
		this.aspects = null;
		this.mimeType = null;
		this.encoding = null;
		this.content = null;
		this.contentPropertyPrefixedName = null;
	}

	/**
	 * Restituisce il nome, completo di prefisso, del tipo di contenuto.
	 * 
	 * @return Il nome completo di prefisso del tipo.
	 */
	public final String getTypePrefixedName() {
		return this.typePrefixedName;
	}

	/**
	 * Imposta il nome completo del tipo di contenuto.
	 * 
	 * @param prefixedName Il nome completo di prefisso del tipo.
	 */
	public final void setTypePrefixedName(String prefixedName) {	
		this.typePrefixedName = prefixedName;
	}
	
	/**
	 * Restituisce il nome, completo di prefisso, del modello in cui &egrave; contenuta
	 * la definizione del tipo di questo contenuto.
	 * 
	 * @return Il nome completo di prefisso del modello.
	 */
	public final String getModelPrefixedName() {
		return this.modelPrefixedName;
	}

	/**
	 * Imposta il nome completo del modello in cui il tipo di questo contenuto 
	 * &egrave; definito.
	 * 
	 * @param prefixedName Il nome completo di prefisso del modello.
	 */
	public final void setModelPrefixedName(String prefixedName) {
		this.modelPrefixedName = prefixedName;
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
	
	/**
	 * Restituisce il nome, completo di prefisso, della propriet&agrave; 
	 * relativa al contenuto.
	 * 
	 * @return Il nome completo di prefisso della propriet&agrave; 
	 * relativa al contenuto.
	 * 	
     */
	public String getContentPropertyPrefixedName() {
		return contentPropertyPrefixedName;
	}
	
	/**
	 * Imposta il nome completo della propriet&agrave; relativa al contenuto.
	 * 
	 * @param contentPropertyPrefixedName Il nome completo della propriet&agrave;
	 * relativa al contenuto.
	 */

	public void setContentPropertyPrefixedName(String contentPropertyPrefixedName) {
		this.contentPropertyPrefixedName = contentPropertyPrefixedName;
	}

	/**
	 * Restituisce tutti gli aspect associati a questo contenuto.
	 * 
	 * @return Un array di {@link Aspect}.
	 */
	public final Aspect [] getAspects() {
		return this.aspects;
	}

	/**
	 * Imposta gli aspect associati a questo contenuto.
	 * 
	 * <p><strong>NB:</strong> gli aspect gi&agrave; presenti vengono
	 * <strong>sovrascritti</strong>.</p>
	 * 
	 * @param values L'array di oggetti {@link Aspect} da impostare.
	 */
	public final void setAspects(Aspect [] values) {
		this.aspects = values;
	}
	
	/**
	 * Associa l'aspect specificato al contenuto.
	 * 
	 * <p>Se &egrave; gi&agrave; presente un aspect con lo stesso
	 * nome il vecchio aspect viene <strong>sovrascritto</strong>.</p>
	 * 
	 * @param aspect L'{@link Aspect} da aggiungere.
	 * 
	 * @deprecated
	 */
	public final void addAspect(Aspect aspect) {
		Vector aspects = new Vector();
		
		for (int i = 0; this.aspects != null && i < this.aspects.length; i++) {
			if (this.aspects[i].getPrefixedName().equals(aspect.getPrefixedName())) {
				continue;
			}
			
			aspects.add(this.aspects[i]);
		}
		aspects.add(aspect);
		
		this.aspects = (Aspect []) aspects.toArray(new Aspect[] {});
	}

	/**
	 * Ricerca l'aspect specificato.
	 * 
	 * @param prefixedName Il nome completo di prefisso dell'aspect da cercare.
	 * 
	 * @return L'{@link Aspect} identificato dal nome specificato (o {@code null}
	 * se non esiste).
	 */
	public final Aspect getAspect(String prefixedName) {
		for (int i = 0; this.aspects != null && i < this.aspects.length; i++) {
			if (this.aspects[i].getPrefixedName().equals(prefixedName)) {
				return this.aspects[i];
			}
		}
		
		return null;
	}

	/**
	 * Rimuove l'associazione con l'aspect specificato.
	 * 
	 * @param prefixedName Il nome completo di prefisso dell'aspect da eliminare.
	 * 
	 * @deprecated
	 */
	public final void deleteAspect(String prefixedName) {
		Vector keep = new Vector();
		
		for (int i = 0; this.aspects != null && i < this.aspects.length; i++) {
			if (this.aspects[i].getPrefixedName().equals(prefixedName)) {
				continue;
			}
			
			keep.add(this.aspects[i]);
		}
		
		this.aspects = (Aspect []) keep.toArray(new Aspect[] {});
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

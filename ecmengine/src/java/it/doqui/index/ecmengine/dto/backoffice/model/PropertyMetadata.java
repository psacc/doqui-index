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
 
package it.doqui.index.ecmengine.dto.backoffice.model;

/**
 * DTO che rappresenta la vista logica di Property presente sul Modello
 * del content model del repository dell'ECMENGINE
 * 
 * @author doqui
 *
 */
public class PropertyMetadata extends ModelComponentDTO {

	private static final long serialVersionUID = -5713729690333632884L;

	private String dataType;

	private boolean mandatory;

	private boolean multiValued;

	private boolean modifiable;

	/**
	 * Restituisce il tipo di dato rappresentato dal PropertyMetadata (es. : d:int per
	 * identificare un tipo int).
	 * 
	 * @return Il tipo di dato.
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * Imposta il tipo di dato del PropertyMetadata
	 * @param dataType
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * Il tipo di dato e' obbligatorio in inserimento ?
	 * 
	 * @return boolean
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Imposta il valore di mandatory sull'oggetto PropertyMetadata
	 * @param mandatory
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * L'oggetto supporta valori multipli ?
	 * @return boolean
	 */
	public boolean isMultiValued() {
		return multiValued;
	}

	/**
	 * Imposta il valore multiValued su PropertyMetadata
	 * @param multiValued
	 */
	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	/**
	 * Il valore e' modificabile ?
	 * 
	 * @return boolean
	 */
	public boolean isModifiable() {
		return modifiable;
	}

	/**
	 * Imposta il valore modifiable su PropertyMetadata
	 * @param modifiable
	 */
	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}

}

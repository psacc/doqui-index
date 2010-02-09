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

package it.doqui.index.ecmengine.client.webservices.dto.backoffice.model;

/**
 * DTO che rappresenta la vista logica di Property presente sul Modello
 * del content model del repository dell'ECMENGINE
 * 
 * @author doqui
 *
 */
public class PropertyMetadata {

	private String prefixedName;
	private String title;
	private String description;
	private ModelDescriptor modelDescriptor;
	private String dataType;
	private boolean mandatory;
	private boolean multiValued;
	private boolean modifiable;

	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public ModelDescriptor getModelDescriptor() {
		return modelDescriptor;
	}
	public void setModelDescriptor(ModelDescriptor modelDescriptor) {
		this.modelDescriptor = modelDescriptor;
	}
	public boolean isModifiable() {
		return modifiable;
	}
	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}
	public boolean isMultiValued() {
		return multiValued;
	}
	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPrefixedName() {
		return prefixedName;
	}
	public void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

}

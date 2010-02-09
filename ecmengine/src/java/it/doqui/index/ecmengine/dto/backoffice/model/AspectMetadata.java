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
 * DTO che rappresenta un aspect del Content Model su cui si sta lavorando
 *
 * @author DoQui
 */
public class AspectMetadata extends ModelComponentDTO {

	private static final long serialVersionUID = -1682199422060172006L;

	private PropertyMetadata [] properties;
	private AssociationMetadata [] associations;
	private ChildAssociationMetadata [] childAssociations;
	private String parentPrefixedName;

	/**
	 * Restituisce i metadati delle associazioni semplici definite in questo aspect.
	 *
	 * @return Un array di {@link AssociationMetadata}.
	 */
	public AssociationMetadata[] getAssociations() {
		return associations;
	}

	/**
	 * Imposta i metadati delle associazioni semplici definite in questo aspect.
	 *
	 * @param associations Un array di {@link AssociationMetadata}.
	 */
	public void setAssociations(AssociationMetadata[] associations) {
		this.associations = associations;
	}

	/**
	 * Restituisce i metadati delle associazioni padre-figlio definite in questo aspect.
	 *
	 * @return Un array di {@link ChildAssociationMetadata}.
	 */
	public ChildAssociationMetadata[] getChildAssociations() {
		return childAssociations;
	}

	/**
	 * Imposta i metadati delle associazioni padre-figlio definite in questo aspect.
	 *
	 * @param childAssociations Un array di {@link ChildAssociationMetadata}.
	 */
	public void setChildAssociations(ChildAssociationMetadata[] childAssociations) {
		this.childAssociations = childAssociations;
	}

	/**
	 * Restituisce i metadati delle property definite in questo aspect.
	 *
	 * @return Un array di {@link PropertyMetadata}.
	 */
	public PropertyMetadata[] getProperties() {
		return properties;
	}

	/**
	 * Imposta i metadati delle property definite in questo aspect.
	 *
	 * @param properties Un array di {@link PropertyMetadata}.
	 */
	public void setProperties(PropertyMetadata[] properties) {
		this.properties = properties;
	}

	/**
	 * Restituisce il nome completo di prefisso dell'aspect padre di questo aspect.
	 *
	 * @return Il nome completo di prefisso, oppure {@code null} se questo aspect non
	 * ha un aspect padre.
	 */
	public String getParentPrefixedName() {
		return parentPrefixedName;
	}

	/**
	 * Imposta il nome completo di prefisso dell'aspect padre di questo aspect.
	 *
	 * @param parentPrefixedName Il nome completo di prefisso.
	 */
	public void setParentPrefixedName(String parentPrefixedName) {
		this.parentPrefixedName = parentPrefixedName;
	}
}

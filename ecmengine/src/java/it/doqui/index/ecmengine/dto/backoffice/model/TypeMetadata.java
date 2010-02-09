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
 * DTO che rappresenta l'insieme dei metadati di un tipo
 * definito nel content model di ECMENGINE.
 *
 * @author DoQui
 */
public class TypeMetadata extends ModelComponentDTO {

	private static final long serialVersionUID = 6468637726206918501L;

	private String parentPrefixedName;
	private AspectMetadata [] aspects;
	private PropertyMetadata [] properties;
	private AssociationMetadata [] associations;
	private ChildAssociationMetadata [] childAssociations;

	/**
	 * Restituisce i metadati delle associazioni semplici definite in questo tipo.
	 *
	 * @return Un array di {@link AssociationMetadata}.
	 */
	public AssociationMetadata[] getAssociations() {
		return associations;
	}

	/**
	 * Imposta i metadati delle associazioni semplici definite in questo tipo.
	 *
	 * @param associations Un array di {@link AssociationMetadata}.
	 */
	public void setAssociations(AssociationMetadata[] associations) {
		this.associations = associations;
	}

	/**
	 * Restituisce i metadati delle associazioni padre-figlio definite in questo tipo.
	 *
	 * @return Un array di {@link ChildAssociationMetadata}.
	 */
	public ChildAssociationMetadata[] getChildAssociations() {
		return childAssociations;
	}

	/**
	 * Imposta i metadati delle associazioni padre-figlio definite in questo tipo.
	 *
	 * @param childAssociations Un array di {@link ChildAssociationMetadata}.
	 */
	public void setChildAssociations(ChildAssociationMetadata[] childAssociations) {
		this.childAssociations = childAssociations;
	}

	/**
	 * Restituisce i metadati degli aspect definiti in questo tipo.
	 *
	 * @return Un array di {@link AspectMetadata}.
	 */
	public AspectMetadata[] getAspects() {
		return aspects;
	}

	/**
	 * Imposta i metadati degli aspect definiti in questo tipo.
	 *
	 * @param aspects Un array di {@link AspectMetadata}.
	 */
	public void setAspects(AspectMetadata[] aspects) {
		this.aspects = aspects;
	}

	/**
	 * Restituisce i metadati delle property definite in questo tipo.
	 *
	 * @return Un array di {@link PropertyMetadata}.
	 */
	public PropertyMetadata[] getProperties() {
		return properties;
	}

	/**
	 * Imposta i metadati delle property definite in questo tipo.
	 *
	 * @param properties Un array di {@link PropertyMetadata}.
	 */
	public void setProperties(PropertyMetadata[] properties) {
		this.properties = properties;
	}

	/**
	 * Restituisce il nome completo di prefisso del tipo padre di questo tipo.
	 *
	 * @return Il nome completo di prefisso, oppure {@code null} se questo tipo non
	 * ha un tipo padre.
	 */
	public String getParentPrefixedName() {
		return parentPrefixedName;
	}

	/**
	 * Imposta il nome completo di prefisso del tipo padre di questo tipo.
	 *
	 * @param parentPrefixedName Il nome completo di prefisso.
	 */
	public void setParentPrefixedName(String parentPrefixedName) {
		this.parentPrefixedName = parentPrefixedName;
	}
}

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

import it.doqui.index.ecmengine.dto.EcmEngineDto;


/**
 * DTO che rappresenta le caratteristiche di un model del repository dell'ECMENGINE
 * 
 * @author doqui
 *
 */
public class ModelMetadata extends EcmEngineDto {

	private static final long serialVersionUID = -10207716751631231L;

	private String prefixedName;

	private String description;

	private TypeMetadata[] types;

	private AspectMetadata[] aspects;

	/**
	 * Restituisce il nome comprensivo di prefisso del model.
	 * 
	 * @return Nome, completo di prefisso, del modello.
	 */
	public String getPrefixedName() {
		return prefixedName;
	}

	/**
	 * Imposta il nome comprensivo di prefisso del model.
	 * 
	 * @param prefixedName Il nome del modello.
	 */
	public void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}

	/**
	 * Restituisce tutti i tipi di dato (array di TypeMetadata) presenti su un modello
	 * @return arry di TypeMetadata
	 */
	public TypeMetadata[] getTypes() {
		return types;
	}

	/**
	 * Imposta i tipi di dato su un model
	 * @param types
	 */
	public void setTypes(TypeMetadata[] types) {
		this.types = types;
	}

	/**
	 * Restituisce la descrizione del modello 
	 * @return descrizione del modello
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Imposta la descrizione del model
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Restituisce la collezione di AspectMetadata presenti sul modello (
	 * descrizione logica degli aspect presenti sul Model)
	 * 
	 * @return array di AspectMetadata
	 */
	public AspectMetadata[] getAspects() {
		return aspects;
	}

	/**
	 * Imposta la collezione di AspectMetadata sul Model
	 * @param aspects
	 */
	public void setAspects(AspectMetadata[] aspects) {
		this.aspects = aspects;
	}

}

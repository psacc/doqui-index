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
 * DTO astratto che definisce gli attributi comuni a tutti gli
 * elementi del modello di ECMENGINE.
 * 
 * @author DoQui
 */
public abstract class ModelDto extends EcmEngineDto {

	private String prefixedName;
	private String title;
	private String description;

	/** Costruttore vuoto. */
	public ModelDto() {}

	/**
	 * Restituisce il nome completo di prefisso di questo elemento.
	 * 
	 * @return Il nome completo di prefisso.
	 */
	public String getPrefixedName() {
		return prefixedName;
	}

	/**
	 * Imposta il nome completo di prefisso di questo elemento.
	 * 
	 * @param prefixedName Il nome completo di prefisso.
	 */
	public void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}

	/**
	 * Restituisce il titolo associato a questo elemento.
	 * 
	 * @return Il titolo.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Imposta il titolo associato a questo elemento.
	 * 
	 * @param title Il titolo.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Restituisce la descrizione associata a questo elemento.
	 * 
	 * @return La descrizione.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Imposta la descrizione associata a questo elemento.
	 * 
	 * @param description La descrizione.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}

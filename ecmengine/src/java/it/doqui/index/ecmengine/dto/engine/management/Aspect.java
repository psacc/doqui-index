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

/**
 * Classe DTO che rappresenta un <i>aspect</i> del content model
 * dell'ECMENGINE.
 * 
 * <p>Un aspect &egrave; sempre caratterizzato da un nome che lo identifica e
 * dal nome del modello in cui &egrave; contenuta la sua definizione. Esso inoltre
 * pu&ograve; contenere una serie di property.</p>
 * 
 * @see ItemsContainer
 * @see PropertiesContainer
 * 
 * @author Doqui
 */
public class Aspect extends ItemsContainer {

	private static final long serialVersionUID = 7183654847216855755L;

	private String modelPrefixedName;
	
	/**
	 * Costruttore predefinito.
	 */
	public Aspect() {
		super();
		
		this.modelPrefixedName = null;
	}
	
	/**
	 * Restituisce il nome, completo di prefisso, del modello in cui &egrave; contenuta
	 * la definizione di questo aspect.
	 * 
	 * @return Il nome completo di prefisso del modello.
	 */
	public final String getModelPrefixedName() {
		return this.modelPrefixedName;
	}
	
	/**
	 * Imposta il nome completo del modello in cui questo aspect &egrave; definito.
	 * 
	 * @param prefixedName Il nome completo di prefisso del modello.
	 */
	public final void setModelPrefixedName(String prefixedName) {
		this.modelPrefixedName = prefixedName;
	}
}

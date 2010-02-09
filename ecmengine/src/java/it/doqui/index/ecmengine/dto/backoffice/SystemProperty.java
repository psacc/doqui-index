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
 
package it.doqui.index.ecmengine.dto.backoffice;

import it.doqui.index.ecmengine.dto.EcmEngineDto;


/**
 * DTO che rappresenta una caratteristica di sistema.
 * 
 * <p>
 * Questo DTO fornisce una tupla chiave e valore per identificare una caratteristica di sistema.
 * </p>
 *   
 * @author doqui
 *
 */
public class SystemProperty extends EcmEngineDto {

	private static final long serialVersionUID = -6772578722889063005L;

	private String key;
	private String value;

	/**
	 * Costruttore predefinito
	 *
	 */
	public SystemProperty() {}

	/**
	 * Costruttore che prende in input la chiave ed il valore
	 * @param key
	 * @param value
	 */
	public SystemProperty(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Restituisce la chiave della property di sistema
	 * 
	 * @return chiave della property
 	 */
	public String getKey() {
		return key;
	}

	/**
	 * Imposta  il valore della key
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Restituisce il valore della property
	 * 
	 * @return il valore della property
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Imposta il valore della property
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

}

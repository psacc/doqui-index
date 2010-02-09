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

import it.doqui.index.ecmengine.dto.ContentItem;

/**
 * Classe che rappresenta un attributo dei metadati di un contenuto.
 * 
 * @author Doqui
 * 
 */
public class ResultProperty extends ContentItem {

	private static final long serialVersionUID = -888553312210988664L;

	private boolean multivalue;
	private String [] values;

	/**
	 * Costruttore predefinito.
	 */
	public ResultProperty() {
		super();
		
		this.multivalue = false;
		this.values = null;
	}

	/**
	 * Indica se la propriet&agrave; &egrave; multivalore.
	 * 
	 * @return {@code true} se la propriet&agrave; &egrave; multivalore,
	 *         {@code false} altrimenti
	 */
	public boolean isMultivalue() {
		return multivalue;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	/**
	 * Restituisce l'array di valori assegnati alla propriet&agrave;. Se la
	 * propriet&agrave; non &egrave; multivalore ed il valore assegnato &egrave;
	 * diverso da {@code null} viene restituito un array con un solo elemento.
	 * 
	 * @return l'array di valori assegnati alla propriet&agrave; o {@code null}
	 *         se non &egrave; stato assegnato nessun valore
	 */
	public String[] getValues() {
		return values;
	}

	public void setValues(String [] values) {
		this.values = values;
	}
}

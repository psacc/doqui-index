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

package it.doqui.index.ecmengine.client.webservices.dto.engine.management;

/**
 * Classe DTO che rappresenta una <i>property</i> del content model
 * dell'ECMENGINE.
 * 
 * <p>Una property &egrave; sempre caratterizzata da un nome, dal tipo di valore che
 * pu&ograve; contenere e da un flag che indica se la property pu&ograve; avere valori
 * multipli oppure no.</p>
 * 
 * @author Doqui
 */
public class Property {

	private String prefixedName;
	private String dataType;
	private boolean multivalue;
	private String [] values;

	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public boolean isMultivalue() {
		return multivalue;
	}
	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}
	public String getPrefixedName() {
		return prefixedName;
	}
	public void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}
	public String[] getValues() {
		return values;
	}
	public void setValues(String[] values) {
		this.values = values;
	}

}

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

package it.doqui.index.ecmengine.client.webservices.dto.engine.search;

/**
 * Classe che rappresenta un aspect definito in un model.
 * 
 * @author Doqui
 * 
 */
public class ResultAspect {

	private String prefixedName;
	private ResultProperty[] properties;
	private ResultAssociation[] associations;

	public ResultAssociation[] getAssociations() {
		return associations;
	}
	public void setAssociations(ResultAssociation[] associations) {
		this.associations = associations;
	}
	public String getPrefixedName() {
		return prefixedName;
	}
	public void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}
	public ResultProperty[] getProperties() {
		return properties;
	}
	public void setProperties(ResultProperty[] properties) {
		this.properties = properties;
	}

}

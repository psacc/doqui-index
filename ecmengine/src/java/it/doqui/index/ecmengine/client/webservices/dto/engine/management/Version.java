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

import it.doqui.index.ecmengine.client.webservices.dto.Node;

import java.util.Date;

/**
 * Classe DTO che rappresenta una <i>versione</i> di un contenuto 
 * La versione e' un identificativo legato al nodo; quando il nodo 
 * viene creato, viene creata una versione iniziale che poi evolve 
 * in base alle modifiche richieste sui metadati e sul contenuto.
 * 
 * @author Doqui
 */
public class Version {

	private String creator;
	private Date createdDate;
	private String description;
	private String versionLabel;
	private Node versionedNode;
	private Property[] versionProperties;

	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Node getVersionedNode() {
		return versionedNode;
	}
	public void setVersionedNode(Node versionedNode) {
		this.versionedNode = versionedNode;
	}
	public String getVersionLabel() {
		return versionLabel;
	}
	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}
	public Property[] getVersionProperties() {
		return versionProperties;
	}
	public void setVersionProperties(Property[] versionProperties) {
		this.versionProperties = versionProperties;
	}

}

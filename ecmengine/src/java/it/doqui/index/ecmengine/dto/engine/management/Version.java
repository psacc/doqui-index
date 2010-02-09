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

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

//import org.alfresco.service.cmr.version.VersionType;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * Classe DTO che rappresenta una <i>versione</i> di un contenuto 
 * La versione e' un identificativo legato al nodo; quando il nodo 
 * viene creato, viene creata una versione iniziale che poi evolve 
 * in base alle modifiche richieste sui metadati e sul contenuto.
 * 
 * @author Doqui
 */
public class Version extends EcmEngineDto {

	private static final long serialVersionUID = -4417937069784991790L;

	private String creator;
	private Date createdDate;
	private String description;
	private String versionLabel;
	private Node versionedNode;
	private Vector versionProperties;

	/**
	 * Costruttore predefinito.
	 */
	public Version() {
		super();
		this.versionProperties = new Vector();
	}

	public Date getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreator() {
		return this.creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
/*
	public Node getFrozenStateNode() {
		return this.frozenStateNode;
	}

	public getFrozenStateNode(Node frozenStateNode) {
		this.frozenStateNode = frozenStateNode;
	}
*/
	public String getVersionLabel() {
		return this.versionLabel;
	}

	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}

	public final void setVersionProperties(Property [] values) {
		this.versionProperties.clear();
		
		for (int i = 0; i < values.length; i++) {
			this.versionProperties.add(values[i]);
		}
	}
	
	public final Property[] getVersionProperties() {
		return ((this.versionProperties != null) && (this.versionProperties.size() > 0))
		? (Property []) this.versionProperties.toArray(new Property [] {})
		: null;
	}

	public final Property getVersionProperty(String prefixedName) {
		Iterator iterator = versionProperties.iterator();
		
		while (iterator.hasNext()) {
			final Property prop = (Property) iterator.next();
			
			if (prop.getPrefixedName().equals(prefixedName)) {
				return prop;
			}
		}
		
		return null;
	}

	public final String getVersionPropertyValue(String prefixedName) {
		Property prop = getVersionProperty(prefixedName);
		
		return (prop != null) ? prop.getValue() : null;
	}
/*
	public VersionType getVersionType() {
		// TODO Auto-generated method stub
		return null;
	}
*/
	public void setVersionedNode(Node node) {
		this.versionedNode = node;
	}

	public Node getVersionedNode() {
		return this.versionedNode;
	}

}

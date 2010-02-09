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

import it.doqui.index.ecmengine.dto.ContentItem;

import java.util.Vector;

/**
 * Classe astratta che definisce l'implementazione di un generico contenitore di
 * oggetti del modello.
 * 
 * <p>Questa classe supporta operazioni di base quali l'aggiunta e la rimozione di
 * {@link Property} e la loro ricerca per nome completo. Ogni contenitore di oggetti 
 * del modello &egrave; esso stesso un oggetto del modello, quindi estende la classe 
 * astratta {@link ContentItem}.</p>
 * 
 * @see PropertiesContainer
 * @see Property
 * @see ContentItem
 * 
 * @author Doqui
 */
public abstract class ItemsContainer extends ContentItem implements PropertiesContainer {

	private Property [] properties;
	
	/**
	 * Costruttore predefinito.
	 */
	public ItemsContainer() {
		super();
	}

	/**
	 * @deprecated
	 */
	public final void addProperty(Property property) {
		Vector props = new Vector();
		
		for (int i = 0; this.properties != null && i < this.properties.length; i++) {
			if (this.properties[i].getPrefixedName().equals(property.getPrefixedName())) {
				continue;
			}
			props.add(this.properties[i]);
		}
		props.add(property);
		
		this.properties = (Property []) props.toArray(new Property[] {});
	}

	/**
	 * @deprecated
	 */
	public final void deleteProperty(String prefixedName) {
		Vector keep = new Vector();
		
		for (int i = 0; this.properties != null && i < this.properties.length; i++) {
			if (this.properties[i].getPrefixedName().equals(prefixedName)) {
				continue;
			}
			keep.add(this.properties[i]);
		}
		
		this.properties = (Property []) keep.toArray(new Property[] {});
	}

	public final Property[] getProperties() {
		return this.properties;
	}
	
	public final void setProperties(Property [] values) {
		this.properties = values;
	}
	
	public final Property getProperty(String prefixedName) {

		for (int i = 0; this.properties != null && i < this.properties.length; i++) {
			
			if (this.properties[i].getPrefixedName().equals(prefixedName)) {
				return this.properties[i];
			}
		}
		return null;
	}

	public final String getPropertyValue(String prefixedName) {
		Property prop = getProperty(prefixedName);
		
		return (prop != null) ? prop.getValue() : null;
	}
}

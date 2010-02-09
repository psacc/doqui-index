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
 * Interfaccia implementata dal generico contenitore di property.
 * 
 * @author Doqui
 */
public interface PropertiesContainer {
	/**
	 * Restituisce tutte le property contenute.
	 * 
	 * @return Un array di {@link Property}.
	 */
	Property [] getProperties();
	
	/**
	 * Imposta le property contenute.
	 * 
	 * <p><strong>NB:</strong> le property gi&agrave; presenti vengono
	 * <strong>sovrascritte</strong>.</p>
	 * 
	 * @param values L'array di oggetti {@link Property} da impostare.
	 */
	void setProperties(Property [] values);
	
	/**
	 * Aggiunge la property specificata al contenitore.
	 * 
	 * <p>Se &egrave; gi&agrave; presente una property con lo stesso
	 * nome la vecchia property viene <strong>sovrascritta</strong>.</p>
	 * 
	 * @param property La {@link Property} da aggiungere.
	 * 
	 * @deprecated
	 */
	void addProperty(Property property);
	
	/**
	 * Elimina dal contenitore la property specificata.
	 * 
	 * @param prefixedName Il nome completo di prefisso della property
	 * da eliminare.
	 * 
	 * @deprecated
	 */
	void deleteProperty(String prefixedName);
	
	/**
	 * Restituisce la property specificata.
	 * 
	 * @param prefixedName Il nome completo di prefisso della property
	 * da restituire.
	 * 
	 * @return La {@link Property} identificata dal nome specificato (o {@code null}
	 * se non esiste).
	 */
	Property getProperty(String prefixedName);
	
	/**
	 * Restituisce il valore della property specificata.
	 * 
	 * @param prefixedName Il nome completo di prefisso della property.
	 * 
	 * @return Il primo valore della property identificata dal nome specificato.
	 */
	String getPropertyValue(String prefixedName);
}

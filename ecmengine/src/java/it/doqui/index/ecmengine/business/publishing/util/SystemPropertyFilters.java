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
 
package it.doqui.index.ecmengine.business.publishing.util;

import it.doqui.index.ecmengine.exception.EcmEngineException;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static it.doqui.index.ecmengine.util.EcmEngineConstants.*;

/**
 * Classe istanziata come singleton che contiene le property che devono essere
 * filtrate nel reperimento delle system property.
 * 
 * Le property sono contenute nel file di property definito da
 * {@code EcmEngineConstants.ECMENGINE_SYSTEM_PROPERTY_FILTERS_PROPERTY_FILE}.
 * 
 * @author Doqui
 * 
 */
public class SystemPropertyFilters {

	private static Set<String> filteredSystemProperties = new HashSet<String>();

	private static SystemPropertyFilters theInstance;

	protected static Log logger = LogFactory.getLog(ECMENGINE_ROOT_LOG_CATEGORY);

	/**
	 * Costruttore privato.
	 * 
	 * @throws EcmEngineException
	 *             Se si verifica un errore durante il caricamento del file di
	 *             property.
	 */
	private SystemPropertyFilters() throws EcmEngineException {
		try {
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream(ECMENGINE_SYSTEM_PROPERTY_FILTERS_PROPERTY_FILE));
			for (Object key : properties.keySet()) {
				filteredSystemProperties.add(""+key);
			}
		} catch(Exception e) {
			throw new EcmEngineException("Errore nella lettura dei filtri delle system property", e);
		}
	}

	/**
	 * Restituisce l'istanza statica di questa classe.
	 * 
	 * @return L'istanza statica di questa classe.
	 * 
	 * @throws EcmEngineException
	 *             Se si verifica un errore durante il caricamento del file di
	 *             property.
	 */
	public static synchronized SystemPropertyFilters getInstance() throws EcmEngineException {
		if (theInstance == null) {
			theInstance = new SystemPropertyFilters();
		}
		return theInstance;
	}

	/**
	 * Verifica che la property specificata &egrave; tra quelle da filtrare.
	 * 
	 * @param propertyName Il nome della property da verificare.
	 * 
	 * @return {@code true} se la property &egrave; tra quelle da filtrare.
	 */
	public boolean contains(String propertyName) {
		return filteredSystemProperties.contains(propertyName);
	}

	/**
	 * Restituisce l'insieme delle property da filtrare.
	 * 
	 * @return Un array di {@code String} contenente le property da filtrare.
	 */
	public String[] getFilteredProperties() {
		return filteredSystemProperties.toArray(new String[]{});
	}

}

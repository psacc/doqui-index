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
 
package it.doqui.index.ecmengine.client.engine;


import it.doqui.index.ecmengine.client.engine.exception.EcmEngineDelegateInstantiationException;
import it.doqui.index.ecmengine.client.engine.util.EcmEngineDelegateConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe factory utilizzabile per caricare una nuova istanza
 * del client delegate dei servizi di gestione e ricerca di contenuti.
 * 
 * <p>Questa classe non &egrave; istanziabile, ma fornisce un metodo
 * {@code static} che restituisce la nuova istanza. Alcuni dei parametri
 * di funzionamento della factory sono definiti nell'interfaccia
 * {@link EcmEngineDelegateConstants}, altri vengono caricati da
 * un file di propriet&agrave; il cui nome predefinito &egrave;
 * {@code ecmengine-engine-delegate.properties} 
 * (vedere anche {@link EcmEngineDelegateConstants#ECMENGINE_PROPERTIES_FILE}).
 * 
 * @author Doqui
 *
 * @see EcmEngineDelegateConstants#ECMENGINE_PROPERTIES_FILE
 */
public class EcmEngineDelegateFactory implements EcmEngineDelegateConstants {

    /**
     * Costruttore protected definito per nascondere il costruttore
     * pubblico di default poich&eacute; la classe non deve essere
     * istanziabile.
     */
    protected EcmEngineDelegateFactory() {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * Metodo factory che crea una nuova istanza del client delegate.
     * 
     * <p>La factory cerca di istanziare la classe di implementazione
     * specificata dall'utente nel file di propriet&agrave;
     * {@code ecmengine-engine-delegate.properties}. Se tale operazione fallisce
     * la factory cerca di istanziare la classe di default definita
     * in {@link it.doqui.index.ecmengine.client.engine.util.EcmEngineDelegateConstants#ECMENGINE_DELEGATE_CLASS_NAME_DEFAULT}.
     * </p>
     * 
     * @return Una nuova istanza del client delegate.
     * 
     * @throws EcmEngineDelegateInstantiationException Se si verifica un
     * errore nell'istanziazione del client delegate.
     */
    public static EcmEngineDelegate getEcmEngineDelegate() throws EcmEngineDelegateInstantiationException {
        final ResourceBundle resources = ResourceBundle.getBundle(ECMENGINE_PROPERTIES_FILE);
        final String caller = resources.getString(ECMENGINE_DELEGATE_CALLER);
    	final Log logger = LogFactory.getLog(caller + ECMENGINE_DELEGATE_LOG_CATEGORY);
    	final String implClass = resources.getString(ECMENGINE_DELEGATE_CLASS);
    	
    	
        EcmEngineDelegate ecmEngineDelegateImpl = null;
        logger.debug("[EcmEngineDelegateFactory::getEcmEngineDelegate] BEGIN");

        logger.debug("[EcmEngineDelegateFactory::getEcmEngineDelegate] " +
        		"Classe delegate: " + implClass);
        try {
        	ecmEngineDelegateImpl = getClassInstance(implClass, logger);
        } catch (EcmEngineDelegateInstantiationException e) {
        	logger.warn("[EcmEngineDelegateFactory::getEcmEngineDelegate] " +
        			"Impossibile caricare la classe \"" + implClass + 
        			"\": " + e.getMessage());

        	logger.debug("[EcmEngineDelegateFactory::getEcmEngineDelegate] " +
        			"Classe delegate di default: " + ECMENGINE_DELEGATE_CLASS_NAME_DEFAULT);
        	try {
        		ecmEngineDelegateImpl = getClassInstance(ECMENGINE_DELEGATE_CLASS_NAME_DEFAULT, logger);
        	} catch (EcmEngineDelegateInstantiationException ex) {
        		logger.error("[EcmEngineDelegateFactory::getEcmEngineDelegate] " +
        				"Impossibile caricare la classe di default \"" + ECMENGINE_DELEGATE_CLASS_NAME_DEFAULT + 
        				"\": " + ex.getMessage());

        		throw ex; // Rilancia l'eccezione al chiamante
        	}

        } finally {
        	logger.debug("[EcmEngineDelegateFactory::getEcmEngineDelegate] END");      	
        }
        
        return ecmEngineDelegateImpl;
    }

    private static EcmEngineDelegate getClassInstance(
    					String ecmEngineDelegateImplName, Log logger) 
    					throws EcmEngineDelegateInstantiationException {
    	EcmEngineDelegate classInstance = null;
    	
    	logger.debug("[EcmEngineDelegateFactory::getClassInstance] BEGIN");
    	try {
    		logger.debug("[EcmEngineDelegateFactory::getClassInstance] " +
    				"Caricamento classe: " + ecmEngineDelegateImplName);
    		final Class delegateClass = Class.forName(ecmEngineDelegateImplName);
    		final Constructor constructor = delegateClass.getConstructor(new Class[]{ Log.class });
    		
    		classInstance = (EcmEngineDelegate)constructor.newInstance(new Object[]{ logger });
    	} catch (ClassNotFoundException e) {
    		logger.error("[EcmEngineDelegateFactory::getClassInstance] " +
    				"FATAL: classe non trovata.");
    		throw new EcmEngineDelegateInstantiationException("Classe non trovata.", e);
    	} catch (NoSuchMethodException e) {
    		logger.error("[EcmEngineDelegateFactory::getClassInstance] " + 
    				"FATAL: nessun costruttore compatibile.");
    		throw new EcmEngineDelegateInstantiationException("Nessun costruttore compatibile.", e);
    	} catch (InstantiationException e) {
    		logger.error("[EcmEngineDelegateFactory::getClassInstance] " + 
    				"FATAL: errore di istanziazione.");
    		throw new EcmEngineDelegateInstantiationException("Errore di istanziazione.", e);
    	} catch (IllegalAccessException e) {
    		logger.error("[EcmEngineDelegateFactory::getClassInstance] " + 
					"FATAL: errore di accesso.");
    		throw new EcmEngineDelegateInstantiationException("Errore di accesso.", e);
    	} catch (InvocationTargetException e) {
    		logger.error("[EcmEngineDelegateFactory::getClassInstance] " + 
					"FATAL: eccezione nel target invocato.");
    		throw new EcmEngineDelegateInstantiationException("Eccezione nel target invocato.", e);
    	} finally {
    		logger.debug("[EcmEngineDelegateFactory::getClassInstance] END");
    	}
        return classInstance;
    }
}
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
 
package it.doqui.index.ecmengine.client.backoffice;

import it.doqui.index.ecmengine.client.backoffice.exception.EcmEngineBackofficeDelegateInstantiationException;
import it.doqui.index.ecmengine.client.backoffice.util.EcmEngineBackofficeDelegateConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe factory utilizzabile per caricare una nuova istanza
 * del client delegate dei servizi di backoffice.
 * 
 * <p>Questa classe non &egrave; istanziabile, ma fornisce un metodo
 * {@code static} che restituisce la nuova istanza. Alcuni dei parametri
 * di funzionamento della factory sono definiti nell'interfaccia
 * {@link EcmEngineBackofficeDelegateConstants}, altri vengono caricati da
 * un file di propriet&agrave; il cui nome predefinito &egrave;
 * {@code ecmengine-backoffice-delegate.properties} 
 * (vedere anche {@link EcmEngineBackofficeDelegateConstants#ECMENGINE_BKO_PROPERTIES_FILE}).
 * 
 * @author Doqui
 *
 * @see EcmEngineBackofficeDelegateConstants#ECMENGINE_BKO_PROPERTIES_FILE
 */
public class EcmEngineBackofficeDelegateFactory implements EcmEngineBackofficeDelegateConstants {
	
    /**
     * Costruttore protected definito per nascondere il costruttore
     * pubblico di default poich&eacute; la classe non deve essere
     * istanziabile.
     */
	protected EcmEngineBackofficeDelegateFactory() {
		throw new UnsupportedOperationException();
	}
	
    /**
     * Metodo factory che crea una nuova istanza del client delegate.
     * 
     * <p>La factory cerca di istanziare la classe di implementazione
     * specificata dall'utente nel file di propriet&agrave;
     * {@code ecmengine-backoffice-delegate.properties}. Se tale operazione fallisce
     * la factory cerca di istanziare la classe di default definita
     * in {@link EcmEngineBackofficeDelegateConstants#ECMENGINE_BKO_DELEGATE_CLASS_NAME_DEFAULT}.
     * </p>
     * 
     * @return Una nuova istanza del client delegate.
     * 
     * @throws EcmEngineBackofficeDelegateInstantiationException Se si verifica un
     * errore nell'istanziazione del client delegate.
     */
	public static EcmEngineBackofficeDelegate getEcmEngineBackofficeDelegate() 
			throws EcmEngineBackofficeDelegateInstantiationException {
        final ResourceBundle resources = ResourceBundle.getBundle(ECMENGINE_BKO_PROPERTIES_FILE);
        final String caller = resources.getString(ECMENGINE_BKO_DELEGATE_CALLER);
    	final Log logger = LogFactory.getLog(caller + ECMENGINE_BKO_DELEGATE_LOG_CATEGORY);
    	final String implClass = resources.getString(ECMENGINE_BKO_DELEGATE_CLASS);
		
    	logger.debug("[EcmEngineBackofficeDelegateFactory::getEcmEngineBackofficeDelegate] BEGIN");
    	
    	EcmEngineBackofficeDelegate backofficeInstance = null;
    	
    	logger.debug("[EcmEngineBackofficeDelegateFactory::getEcmEngineBackofficeDelegate] " +
    			"Classe delegate: " + implClass);
    	try {
    		backofficeInstance = getClassInstance(implClass, logger);
    	} catch (EcmEngineBackofficeDelegateInstantiationException e) {
        	logger.warn("[EcmEngineBackofficeDelegateFactory::getEcmEngineBackofficeDelegate] " +
        			"Impossibile caricare la classe \"" + implClass + 
        			"\": " + e.getMessage());
        	
        	logger.debug("[EcmEngineBackofficeDelegateFactory::getEcmEngineBackofficeDelegate] " +
        			"Classe delegate di default: " + ECMENGINE_BKO_DELEGATE_CLASS_NAME_DEFAULT);
        	try {
        		backofficeInstance = getClassInstance(ECMENGINE_BKO_DELEGATE_CLASS_NAME_DEFAULT, logger);
        	} catch (EcmEngineBackofficeDelegateInstantiationException ex) {
        		logger.error("[EcmEngineBackofficeDelegateFactory::getEcmEngineBackofficeDelegate] " +
        				"Impossibile caricare la classe di default \"" + 
        				ECMENGINE_BKO_DELEGATE_CLASS_NAME_DEFAULT + "\": " + ex.getMessage());

        		throw ex; // Rilancia l'eccezione al chiamante
        	}

    	} finally {
    		logger.debug("[EcmEngineBackofficeDelegateFactory::getEcmEngineBackofficeDelegate] END");
    	}

		return backofficeInstance;
	}
	
	private static EcmEngineBackofficeDelegate getClassInstance(String ecmEngineBkoDelegateImplName, Log logger)
	throws EcmEngineBackofficeDelegateInstantiationException {
		EcmEngineBackofficeDelegate classInstance = null;

		logger.debug("[EcmEngineBackofficeDelegateFactory::getClassInstance] BEGIN");
		try {
			logger.debug("[EcmEngineBackofficeDelegateFactory::getClassInstance] " +
					"Caricamento classe: " + ecmEngineBkoDelegateImplName);
			final Class delegateClass = Class.forName(ecmEngineBkoDelegateImplName);
			final Constructor constructor = delegateClass.getConstructor(new Class[]{ Log.class });
			classInstance = (EcmEngineBackofficeDelegate)constructor.newInstance(new Object[]{ logger });
		} catch (ClassNotFoundException e) {
			logger.error("[EcmEngineBackofficeDelegateFactory::getClassInstance] " +
			"FATAL: classe non trovata.");
			throw new EcmEngineBackofficeDelegateInstantiationException("Classe non trovata.", e);
		} catch (NoSuchMethodException e) {
			logger.error("[EcmEngineBackofficeDelegateFactory::getClassInstance] " + 
			"FATAL: nessun costruttore compatibile.");
			throw new EcmEngineBackofficeDelegateInstantiationException("Nessun costruttore compatibile.", e);
		} catch (InstantiationException e) {
			logger.error("[EcmEngineBackofficeDelegateFactory::getClassInstance] " + 
			"FATAL: errore di istanziazione.");
			throw new EcmEngineBackofficeDelegateInstantiationException("Errore di istanziazione.", e);
		} catch (IllegalAccessException e) {
			logger.error("[EcmEngineBackofficeDelegateFactory::getClassInstance] " + 
			"FATAL: errore di accesso.");
			throw new EcmEngineBackofficeDelegateInstantiationException("Errore di accesso.", e);
		} catch (InvocationTargetException e) {
			logger.error("[EcmEngineBackofficeDelegateFactory::getClassInstance] " + 
			"FATAL: eccezione nel target invocato.");
			throw new EcmEngineBackofficeDelegateInstantiationException("Eccezione nel target invocato.", e);
		} finally {
			logger.debug("[EcmEngineBackofficeDelegateFactory::getClassInstance] END");
		}
		return classInstance;
	}
}

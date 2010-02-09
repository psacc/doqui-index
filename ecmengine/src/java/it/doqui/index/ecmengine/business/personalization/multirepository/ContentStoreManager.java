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

package it.doqui.index.ecmengine.business.personalization.multirepository;

import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton per la gestione della lista dei ContentStoreDynamic conosciuti dall'ECMENGINE.
 *
 * @author Doqui
 */
public class ContentStoreManager {

    /**
     * L'istanza del manager
     */
	private static ContentStoreManager theInstance;

    /**
     * Il logger
     */
	private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);

    /**
     * La lista di tutti gli oggetti istanziati e inizializzati: type+prot+resource
     */
	private static Map<String, ContentStoreDynamic> contentStoreList;

    /**
     * La mappa di TYPE + BEAN non inizializzati
     */
	private Map<String, ContentStoreDynamic> contentStoreBeanList;

	/**
	 * Restituisce l'unica istanza di {@code ContentStoreManager}.
	 *
	 * @return L'unica istanza di {@code ContentStoreManager}.
	 */
	public static ContentStoreManager getInstance() {
		if (theInstance == null) {
			theInstance = new ContentStoreManager();
		}
		return theInstance;
	}

	/**
	 * Costruisce una nuova istanza di ContentStoreManager.
	 */
	private ContentStoreManager() {
		ContentStoreManager.theInstance = this;
		contentStoreBeanList = new LinkedHashMap<String, ContentStoreDynamic>();
		contentStoreList     = new LinkedHashMap<String, ContentStoreDynamic>();
	}

	/**
	 * Imposta la lista dei ContentStoreDynamic conosciuti.
	 *
	 * @param contentstores La lista dei ContentStoreDynamic da impostare.
	 */
	public void setContentStores(Map<String, ContentStoreDynamic> contentstores) {
		contentStoreBeanList = new LinkedHashMap<String, ContentStoreDynamic>( contentstores );
	}

	public ContentStoreDynamic getContentStore( ContentStoreDefinition csd ){
        // Cerco la tripletta in cache
        String cKey = getKey( csd );
        ContentStoreDynamic cs = contentStoreList.get( cKey );

        // Se cs==null, creo il cs, altrimenti, lo restituisco
        if( cs==null ){
            if (logger.isDebugEnabled()){
                logger.debug("ContentStoreManager::getContentStores CSD non trovato " +csd);
	        }
            // Prendo il bean vuoto
            ContentStoreDynamic csEmpty = contentStoreBeanList.get( csd.getType() );

            // Se null .. errore
            if( csEmpty==null ){
                logger.error("ContentStoreManager::getContentStores CSD (" +csd.getType() +") non definito " +csd);
                // TODO: eccezione
        		throw new AlfrescoRuntimeException("ContentStoreDynamic non trovato "+csd.getType());
            }

            try {
            	// Se esiste ne faccio una nuova istanza e imposto protocollo e resource
            	ContentStoreDynamic csNew = (ContentStoreDynamic)csEmpty.getClass().newInstance();
            	           			csNew.setProtocol( csd.getProtocol() );
            				        csNew.setResource( csd.getResource() );

                // lo metto in cache
            	contentStoreList.put( cKey, csNew );

                // Lo assegno al ritorno
	     	    cs = csNew;
        	} catch( java.lang.InstantiationException e ){
        		throw new AlfrescoRuntimeException("Errore nell'istanziazione di "+csEmpty, e);
        	} catch( java.lang.IllegalAccessException ee ){
        		throw new AlfrescoRuntimeException("Errore nell'accesso di "+csEmpty, ee);
        	}
            if (logger.isDebugEnabled()){
                logger.debug("ContentStoreManager::getContentStores Creato "  +cs );
	        }
	    } else {
            if (logger.isDebugEnabled()){
                logger.debug("ContentStoreManager::getContentStores Riusato " +cs );
	        }
	    }
        return cs;
	}

    /**
     * Crea una K da un ContentStoreDefinition
     */
    private String getKey( ContentStoreDefinition csd ){
        return (csd.getType() +"-" +csd.getProtocol() +"-" +csd.getResource());
	}

}

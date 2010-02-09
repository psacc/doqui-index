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

import it.doqui.index.ecmengine.business.publishing.management.EcmEngineManagementBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.management.EcmEngineManagementHome;
import it.doqui.index.ecmengine.business.publishing.massive.EcmEngineMassiveBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.massive.EcmEngineMassiveHome;
import it.doqui.index.ecmengine.business.publishing.search.EcmEngineSearchBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.search.EcmEngineSearchHome;
import it.doqui.index.ecmengine.business.publishing.security.EcmEngineSecurityBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.security.EcmEngineSecurityHome;
import it.doqui.index.ecmengine.client.engine.util.EcmEngineDelegateConstants;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;

public class EcmEngineDirectDelegateImpl extends AbstractEcmEngineDelegateImpl implements EcmEngineDelegateConstants {

    private static ResourceBundle rb = null;

    /**
     * Costruttore pubblico del delegate client.
     *
     * @param inLog Il logger da utilizzare per scrivere il log.
     */
    public EcmEngineDirectDelegateImpl(Log inLog) {
    	super(inLog);
    }

	/*
     * Chiamata al servizio EcmEngineManagement attraverso attraverso il binding diretto all'oggetto di business
     */
    protected EcmEngineManagementBusinessInterface createManagementService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] BEGIN ");
        Properties properties = new Properties();

		/* Caricamento del file contenenti le properties su cui fare il binding */
        rb = ResourceBundle.getBundle(ECMENGINE_PROPERTIES_FILE);

        /*
    	 * Caricamento delle proprieta' su cui fare il binding all'oggetto di business delle funzionalita'
    	 * implementate per il management.
    	 */
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] P-Delegata di backoffice.");

    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] context factory vale : "+rb.getString(ECMENGINE_CONTEXT_FACTORY));
            properties.put(Context.INITIAL_CONTEXT_FACTORY, rb.getString(ECMENGINE_CONTEXT_FACTORY));
    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] url to connect vale : "+rb.getString(ECMENGINE_URL_TO_CONNECT));
            properties.put(Context.PROVIDER_URL, rb.getString(ECMENGINE_URL_TO_CONNECT));

            /* Controllo che la property cluster partition sia valorizzata per capire se
             * sto lavorando in una configurazione in cluster oppure no */
            String clusterPartition = rb.getString(ECMENGINE_CLUSTER_PARTITION);
    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] clusterPartition vale : "+clusterPartition);
            if(clusterPartition != null && clusterPartition.length() > 0) {
                properties.put("jnp.partitionName", clusterPartition);
        		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] disable discovery vale : "+rb.getString(ECMENGINE_DISABLE_DISCOVERY));
                properties.put("jnp.disableDiscovery", rb.getString(ECMENGINE_DISABLE_DISCOVERY));
            }

            // Get an initial context
            InitialContext jndiContext = new InitialContext(properties);
            log.debug("["+getClass().getSimpleName()+"::createManagementService] context istanziato");

            // Get a reference to the Bean
            Object ref  = jndiContext.lookup(ECMENGINE_MANAGEMENT_JNDI_NAME);

            // Get a reference from this to the Bean's Home interface
            EcmEngineManagementHome home = (EcmEngineManagementHome)PortableRemoteObject.narrow (ref, EcmEngineManagementHome.class);

            // Create an Adder object from the Home interface
            return home.create();

    	} catch (Throwable e) {
    		this.log.error("["+getClass().getSimpleName()+"::createManagementService] " +
    				"Impossibile istanziare la P-Delegata di management: " + e.getMessage());
    		throw e;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] END ");
    	}
    }

	protected EcmEngineSearchBusinessInterface createSearchService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] BEGIN ");

        Properties properties = new Properties();

		/* Caricamento del file contenenti le properties su cui fare il binding */
        rb = ResourceBundle.getBundle(ECMENGINE_PROPERTIES_FILE);

        /*
    	 * Caricamento delle proprieta' su cui fare il binding all'oggetto di business delle funzionalita'
    	 * implementate per la ricerca.
    	 */
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] P-Delegata di search.");

    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] context factory vale : "+rb.getString(ECMENGINE_CONTEXT_FACTORY));
            properties.put(Context.INITIAL_CONTEXT_FACTORY, rb.getString(ECMENGINE_CONTEXT_FACTORY));
    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] url to connect vale : "+rb.getString(ECMENGINE_URL_TO_CONNECT));
            properties.put(Context.PROVIDER_URL, rb.getString(ECMENGINE_URL_TO_CONNECT));

            /* Controllo che la property cluster partition sia valorizzata per capire se
             * sto lavorando in una configurazione in cluster oppure no */
            String clusterPartition = rb.getString(ECMENGINE_CLUSTER_PARTITION);
    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] clusterPartition vale : "+clusterPartition);
            if(clusterPartition != null && clusterPartition.length() > 0) {
                properties.put("jnp.partitionName", clusterPartition);
        		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] disable discovery vale : "+rb.getString(ECMENGINE_DISABLE_DISCOVERY));
                properties.put("jnp.disableDiscovery", rb.getString(ECMENGINE_DISABLE_DISCOVERY));
            }

            // Get an initial context
            InitialContext jndiContext = new InitialContext(properties);
            log.debug("["+getClass().getSimpleName()+"::createSearchService] context istanziato");

            // Get a reference to the Bean
            Object ref  = jndiContext.lookup(ECMENGINE_SEARCH_JNDI_NAME);

            // Get a reference from this to the Bean's Home interface
            EcmEngineSearchHome home = (EcmEngineSearchHome)PortableRemoteObject.narrow (ref, EcmEngineSearchHome.class);

            // Create an Adder object from the Home interface
            return home.create();

    	} catch (Throwable e) {
    		this.log.error("["+getClass().getSimpleName()+"::createSearchService] " +
    				"Impossibile l'EJB di management: " + e.getMessage());
    		throw e;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] END ");
    	}
	}

	protected EcmEngineSecurityBusinessInterface createSecurityService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] BEGIN ");

        Properties properties = new Properties();

		// Caricamento del file contenenti le properties su cui fare il binding
        rb = ResourceBundle.getBundle(ECMENGINE_PROPERTIES_FILE);

    	// Caricamento delle proprieta' su cui fare il binding all'oggetto di business delle funzionalita'
    	// implementate per la ricerca.
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] P-Delegata di backoffice.");

    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] context factory vale : "+rb.getString(ECMENGINE_CONTEXT_FACTORY));
            properties.put(Context.INITIAL_CONTEXT_FACTORY, rb.getString(ECMENGINE_CONTEXT_FACTORY));
    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] url to connect vale : "+rb.getString(ECMENGINE_URL_TO_CONNECT));
            properties.put(Context.PROVIDER_URL, rb.getString(ECMENGINE_URL_TO_CONNECT));

            // Controllo che la property cluster partition sia valorizzata per capire se
            // sto lavorando in una configurazione in cluster oppure no
            String clusterPartition = rb.getString(ECMENGINE_CLUSTER_PARTITION);
    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] clusterPartition vale : "+clusterPartition);
            if(clusterPartition != null && clusterPartition.length() > 0) {
                properties.put("jnp.partitionName", clusterPartition);
        		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] disable discovery vale : "+rb.getString(ECMENGINE_DISABLE_DISCOVERY));
                properties.put("jnp.disableDiscovery", rb.getString(ECMENGINE_DISABLE_DISCOVERY));
            }

            // Get an initial context
            InitialContext jndiContext = new InitialContext(properties);
            log.debug("["+getClass().getSimpleName()+"::createSecurityService] context istanziato");

            // Get a reference to the Bean
            Object ref  = jndiContext.lookup(ECMENGINE_SECURITY_JNDI_NAME);

            // Get a reference from this to the Bean's Home interface
            EcmEngineSecurityHome home = (EcmEngineSecurityHome)PortableRemoteObject.narrow (ref, EcmEngineSecurityHome.class);

            // Create an Adder object from the Home interface
            return home.create();

    	} catch (Throwable e) {
    		this.log.error("["+getClass().getSimpleName()+"::createSecurityService] " +
    				"Impossibile l'EJB di security: " + e.getMessage());
    		throw e;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] END ");
    	}
	}

	protected EcmEngineMassiveBusinessInterface createMassiveService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] BEGIN ");

        Properties properties = new Properties();

		// Caricamento del file contenenti le properties su cui fare il binding
        rb = ResourceBundle.getBundle(ECMENGINE_PROPERTIES_FILE);

    	// Caricamento delle proprieta' su cui fare il binding all'oggetto di business delle funzionalita'
    	// implementate per la ricerca.
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] P-Delegata di massive.");

    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] context factory vale : "+rb.getString(ECMENGINE_CONTEXT_FACTORY));
            properties.put(Context.INITIAL_CONTEXT_FACTORY, rb.getString(ECMENGINE_CONTEXT_FACTORY));
    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] url to connect vale : "+rb.getString(ECMENGINE_URL_TO_CONNECT));
            properties.put(Context.PROVIDER_URL, rb.getString(ECMENGINE_URL_TO_CONNECT));

            // Controllo che la property cluster partition sia valorizzata per capire se
            // sto lavorando in una configurazione in cluster oppure no
            String clusterPartition = rb.getString(ECMENGINE_CLUSTER_PARTITION);
    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] clusterPartition vale : "+clusterPartition);
            if(clusterPartition != null && clusterPartition.length() > 0) {
                properties.put("jnp.partitionName", clusterPartition);
        		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] disable discovery vale : "+rb.getString(ECMENGINE_DISABLE_DISCOVERY));
                properties.put("jnp.disableDiscovery", rb.getString(ECMENGINE_DISABLE_DISCOVERY));
            }

            // Get an initial context
            InitialContext jndiContext = new InitialContext(properties);
            log.debug("["+getClass().getSimpleName()+"::createMassiveService] context istanziato");

            // Get a reference to the Bean
            Object ref  = jndiContext.lookup(ECMENGINE_MASSIVE_JNDI_NAME);

            // Get a reference from this to the Bean's Home interface
            EcmEngineMassiveHome home = (EcmEngineMassiveHome)PortableRemoteObject.narrow (ref, EcmEngineMassiveHome.class);

            // Create an Adder object from the Home interface
            return home.create();

    	} catch (Throwable e) {
    		this.log.error("["+getClass().getSimpleName()+"::createMassiveService] " +
    				"Impossibile l'EJB di security: " + e.getMessage());
    		throw e;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] END ");
    	}
	}
}

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

import it.doqui.index.ecmengine.business.publishing.backoffice.EcmEngineBackofficeBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.backoffice.EcmEngineBackofficeHome;
import it.doqui.index.ecmengine.client.backoffice.util.EcmEngineBackofficeDelegateConstants;

import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;

public class EcmEngineBackofficeDirectDelegateImpl extends AbstractEcmEngineBackofficeDelegateImpl implements EcmEngineBackofficeDelegateConstants {

    private static ResourceBundle rb = null;

    /**
     * Costruttore pubblico del delegate client per le iniziative che non possono/vogliono utilizzare
     * come strumento di comunicazione il framework CSI.
     *
     * @param inLog Il logger da utilizzare per scrivere il log.
     */
    public EcmEngineBackofficeDirectDelegateImpl(Log inLog) {
    	super(inLog);
    }

	@Override
	protected EcmEngineBackofficeBusinessInterface createBackofficeService() throws Throwable {
        Properties properties = new Properties();
		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] BEGIN ");

        rb = ResourceBundle.getBundle(ECMENGINE_BKO_PROPERTIES_FILE);
    	/*
    	 * Caricamento della porta delegata del backoffice.
    	 */
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] P-Delegata di backoffice.");

    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] context factory vale : "+rb.getString(ECMENGINE_BKO_CONTEXT_FACTORY));
            properties.put(Context.INITIAL_CONTEXT_FACTORY, rb.getString(ECMENGINE_BKO_CONTEXT_FACTORY));
    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] url to connect vale : "+rb.getString(ECMENGINE_BKO_URL_TO_CONNECT));
            properties.put(Context.PROVIDER_URL, rb.getString(ECMENGINE_BKO_URL_TO_CONNECT));
            
            /* Controllo che la property cluster partition sia valorizzata per capire se 
             * sto lavorando in una configurazione in cluster oppure no */
            String clusterPartition = rb.getString(ECMENGINE_BKO_CLUSTER_PARTITION);
    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] clusterPartition vale : "+clusterPartition);
            if(clusterPartition != null && clusterPartition.length() > 0) {
                properties.put("jnp.partitionName", clusterPartition);
        		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] disable discovery vale : "+rb.getString(ECMENGINE_BKO_DISABLE_DISCOVERY));
                properties.put("jnp.disableDiscovery", rb.getString(ECMENGINE_BKO_DISABLE_DISCOVERY));           	
            }
            	
            // Get an initial context
            InitialContext jndiContext = new InitialContext(properties);
            log.debug("["+getClass().getSimpleName()+"::createBackofficeService] context istanziato");

            // Get a reference to the Bean
            Object ref  = jndiContext.lookup(ECMENGINE_BKO_JNDI_NAME);

            // Get a reference from this to the Bean's Home interface
            EcmEngineBackofficeHome home = (EcmEngineBackofficeHome)PortableRemoteObject.narrow (ref, EcmEngineBackofficeHome.class);

            // Create an Adder object from the Home interface
            return home.create();

    	} catch (Throwable e) {
    		this.log.error("["+getClass().getSimpleName()+"::createBackofficeService] " +
    				"Impossibile istanziare la P-Delegata di backoffice: " + e.getMessage());
    		throw e;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] END ");
    	}
	}

}

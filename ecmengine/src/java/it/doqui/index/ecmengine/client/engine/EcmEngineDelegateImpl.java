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

import it.csi.csi.porte.InfoPortaDelegata;
import it.csi.csi.porte.proxy.PDProxy;
import it.csi.csi.util.xml.PDConfigReader;
import it.doqui.index.ecmengine.client.engine.util.EcmEngineDelegateConstants;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.interfacecsi.management.EcmEngineManagementInterface;
import it.doqui.index.ecmengine.interfacecsi.massive.EcmEngineMassiveInterface;
import it.doqui.index.ecmengine.interfacecsi.search.EcmEngineSearchInterface;
import it.doqui.index.ecmengine.interfacecsi.security.EcmEngineSecurityInterface;

import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;

public class EcmEngineDelegateImpl extends AbstractEcmEngineDelegateImpl implements EcmEngineDelegateConstants {

    /**
     * Costruttore pubblico del delegate client.
     *
     * @param inLog Il logger da utilizzare per scrivere il log.
     */
    public EcmEngineDelegateImpl(Log inLog) {
    	super(inLog);
    }

	/*
     * Chiamata al servizio attraverso lo strumento di cooperazione applicativa
     * per informazioni alla cooperazione applicativa fa fede i documenti
     *
     * documento di specifica INFR-COOP-Vision-V01.doc
     * manuale di utilizzo
     * linee guida
     */
    protected EcmEngineManagementInterface createManagementService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] BEGIN");
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] PD per le funzionalita' di management");

    		final InputStream configManagement = this.getClass().getResourceAsStream(ECMENGINE_MANAGEMENT_CONFIG_FILE);
    		final InfoPortaDelegata info = PDConfigReader.read(configManagement);

    		return (EcmEngineManagementInterface) PDProxy.newInstance(info);
    	} catch (Throwable ex) {
    		this.log.error("["+getClass().getSimpleName()+"::createManagementService] Can't load Interface EcmEngineManagement", ex);
    		throw ex;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createManagementService] END");
    	}
    }

	protected EcmEngineSearchInterface createSearchService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] BEGIN");
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] PD per le funzionalita' di search");

    		final InputStream configSearch = this.getClass().getResourceAsStream(ECMENGINE_SEARCH_CONFIG_FILE);
    		final InfoPortaDelegata info = PDConfigReader.read(configSearch);

    		return (EcmEngineSearchInterface) PDProxy.newInstance(info);
    	} catch (Throwable ex) {
    		this.log.error("["+getClass().getSimpleName()+"::createSearchService] Can't load Interface EcmEngineSearch", ex);
    		throw ex;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createSearchService] END");
    	}
	}

	protected EcmEngineSecurityInterface createSecurityService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] BEGIN");
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] PD per le funzionalita' di security");

    		final InputStream configSecurity = this.getClass().getResourceAsStream(ECMENGINE_SECURITY_CONFIG_FILE);
    		final InfoPortaDelegata info = PDConfigReader.read(configSecurity);

    		return (EcmEngineSecurityInterface) PDProxy.newInstance(info);
    	} catch (Throwable ex) {
    		this.log.error("["+getClass().getSimpleName()+"::createSecurityService] Can't load Interface EcmEngineSecurity", ex);
    		throw ex;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createSecurityService] END");
    	}
	}

	protected EcmEngineMassiveInterface createMassiveService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] BEGIN");
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] PD per le funzionalita' di massive");

    		final InputStream configMassive = this.getClass().getResourceAsStream(ECMENGINE_MASSIVE_CONFIG_FILE);
    		final InfoPortaDelegata info = PDConfigReader.read(configMassive);

    		return (EcmEngineMassiveInterface) PDProxy.newInstance(info);
    	} catch (Throwable ex) {
    		this.log.error("["+getClass().getSimpleName()+"::createMassiveService] Can't load Interface EcmEngineMassive", ex);
    		throw ex;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createMassiveService] END");
    	}
	}
}

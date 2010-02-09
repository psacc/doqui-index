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

import it.csi.csi.porte.InfoPortaDelegata;
import it.csi.csi.porte.proxy.PDProxy;
import it.csi.csi.util.xml.PDConfigReader;
import it.doqui.index.ecmengine.client.backoffice.util.EcmEngineBackofficeDelegateConstants;
import it.doqui.index.ecmengine.interfacecsi.backoffice.EcmEngineBackofficeInterface;

import java.io.InputStream;

import org.apache.commons.logging.Log;

public class EcmEngineBackofficeDelegateImpl extends AbstractEcmEngineBackofficeDelegateImpl implements EcmEngineBackofficeDelegateConstants {

    /**
     * Costruttore pubblico del delegate client.
     *
     * @param inLog Il logger da utilizzare per scrivere il log.
     */
    public EcmEngineBackofficeDelegateImpl(Log inLog) {
    	super(inLog);
    }

	@Override
	protected EcmEngineBackofficeInterface createBackofficeService() throws Throwable {
		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] BEGIN");
    	try {
    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] PD per le funzionalita' di gestione ");

    		final InputStream configManagement = this.getClass().getResourceAsStream(ECMENGINE_BKO_PD_CONFIG_FILE);
    		final InfoPortaDelegata info = PDConfigReader.read(configManagement);

    		return (EcmEngineBackofficeInterface) PDProxy.newInstance(info);
    	} catch (Throwable ex) {
    		this.log.error("["+getClass().getSimpleName()+"::createBackofficeService] " +
    				"Impossibile istanziare la P-Delegata di backoffice: " + ex.getMessage());
    		throw ex;
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::createBackofficeService] END");
    	}
	}
	
}

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
 
package it.doqui.index.ecmengine.test.webservices.util;

import it.doqui.index.ecmengine.test.util.EcmEngineTestConstants;

/**
 * Interfaccia che raccoglie le costanti utilizzate
 * per i test unitari dei web service pubblicati da ECMENGINE.
 * 
 * @author Doqui
 *
 */
public interface EcmEngineWebservicesTestConstants extends EcmEngineTestConstants {

    /**
     * Nome del file di propriet&agrave; contenente i nomi dei folder
     * da creare sul repository per i test unitari.
     *  
     * <p>Questo file viene incluso nello zip che viene creato con il 
     * target package-unit-test; deve essere presente nella cartella 
     * da cui si esegue il runJunit.</p>
     */
    String ECMENGINE_WEBSERVICES_TEST_PROPERTIES_FILE = "ecmengine-webservices.properties";
    
    /**
     * Propriet&agrave; contenuta nel file di properties 
     * che indica l'URL del web service di management.
     */
    String ECMENGINE_WEBSERVICES_MANAGEMENT_URL = "managementUrl"; 

    /**
     * Propriet&agrave; contenuta nel file di properties 
     * che indica l'URL del web service di backoffice.
     */
    String ECMENGINE_WEBSERVICES_BACKOFFICE_URL = "backofficeUrl"; 

}

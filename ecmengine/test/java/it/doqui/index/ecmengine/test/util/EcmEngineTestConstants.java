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

package it.doqui.index.ecmengine.test.util;

/**
 * Interfaccia che raccoglie le costanti utilizzate
 * per i test unitari dei servizi pubblicati da ECMENGINE.
 *
 * @author Doqui
 *
 */
public interface EcmEngineTestConstants {

	/**
	 * Category per il log dei test unitari dei servizi ECMENGINE utilizzando il richiamo
	 * attraverso CSI Framework.
	 */
    String ECMENGINE_TEST_LOG_CATEGORY = "ecmengine.test";

    /**
	 * Category per il log dello Stopwatch.
	 */
    String ECMENGINE_TEST_STOPWATCH_LOG_CATEGORY = "ecmengine.test";

    /**
     * Nome del file di propriet&agrave; contenente i nomi dei folder
     * da creare sul repository per i test unitari.
     *
     * <p>Questo file viene incluso nello zip che viene creato con il
     * target package-unit-test; deve essere presente nella cartella
     * da cui si esegue il runJunit.</p>
     */
    String ECMENGINE_TEST_PROPERTIES_FILE = "junit.properties";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties per i test unitari indicante
     * uno dei folder sul quale verr&agrave; creato il legame da testare.
     */

    String ECMENGINE_TEST_FOLDER_PARENT = "folderParent";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties per i test unitari indicante l'altro folder
     * sul quale verr&agrave; creato il legame da testare.
     */
    String ECMENGINE_TEST_FOLDER = "folder";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante l'uid della company home
     * del repository da utilizzare per i test unitari.
     */

    String  ECMENGINE_TEST_UID_COMPANYHOME = "uidCompanyHome";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante un contenuto generico
     * da caricare nel repository per i test unitari.
     */

    String ECMENGINE_TEST_CONTENT = "content";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante un contenuto generico
     * da caricare nel repository per i test unitari.
     */

    String ECMENGINE_TEST_SECURITY_CONTENT = "securityContent";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties per i test unitari di backoffice
     * indicante un folder.
     */

    String ECMENGINE_TEST_FOLDER_BKO = "folderBko";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties per i test unitari di backoffice
     * indicante un utente.
     */

    String ECMENGINE_TEST_USER_BKO = "user";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante un archivio di test
     * da caricare nel repository per i test unitari.
     */

    String ECMENGINE_TEST_ZIP_ARCHIVE = "zipArchive";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante il content model
     * da caricare nel repository per i test unitari.
     */

    String ECMENGINE_TEST_CONTENT_MODEL = "contentModel";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante lo username da utilizzare
     * per l'operation context.
     */

    String ECMENGINE_TEST_USERNAME = "context.username";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante la password da utilizzare
     * per l'operation context.
     */

    String ECMENGINE_TEST_PASSWORD = "context.password";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante il tenant da utilizzare
     * per tutte le operazioni.
     */

    String ECMENGINE_TEST_TENANT = "context.tenant";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante il repository da utilizzare
     * per tutte le operazioni.
     */

    String ECMENGINE_TEST_REPOSITORY = "repository";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante lo sleep time
     * per tutte le operazioni.
     */

    String ECMENGINE_TEST_SLEEP_TIME = "sleep.time";

    /**
     * Propriet&agrave; contenuta nel file di properties
     * junit.properties indicante il target di test,
     * inteso come online o batch
     */

    String ECMENGINE_TEST_TARGET = "testTarget";

}

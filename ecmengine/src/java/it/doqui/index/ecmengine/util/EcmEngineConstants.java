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

package it.doqui.index.ecmengine.util;

/**
 * Interfaccia che raccoglie le costanti utilizzate
 * all'interno della componente ECMENGINE.
 *
 * @author Doqui
 */
public interface EcmEngineConstants {

	/** Category root per il log della componente ECMENGINE. */
	String ECMENGINE_ROOT_LOG_CATEGORY = new EcmEngineConstantsReader().getRootLogCategory();

	/** Category per il log dei servizi di audit. */
    String ECMENGINE_AUDIT_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.audit";

    /** Category per il log dei servizi di audit trail. */
    String ECMENGINE_AUDIT_TRAIL_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.audittrail";

    /** Category per il log del servizio di lettura info dalla tabella alf_server. */
    String ECMENGINE_SERVERINFO_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.serverinfo";

    /** Category per il log del job quartz per lo spostamento strutture aggregative. */
    String ECMENGINE_JOB_MOVE_AGGREG_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.job.move";

    /** Category per il log del job quartz per il backup metadati. */
    String ECMENGINE_JOB_BACKUP_METADATI_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.job.backup";

    /** Category per il log del job quartz per la creazione dei tenant. */
    String ECMENGINE_JOB_TENANT_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.job.tenant";

    /** Category per il log del job quartz per l'attivazione dei content model. */
    String ECMENGINE_JOB_MODEL_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.job.model";

    /** Category per il log del DAO di accesso alla base dati per le funzioni di audit. */
    String ECMENGINE_DAO_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".integration.dao";

	/**
	 * Category per il log dei <i>Foundation EJB</i>
	 * (o &quot;servizi applicativi&quot;, EJB 2.1 che
	 * incapsulano le chiamate agli Alfresco Foundation Services).
	 */
    String ECMENGINE_FOUNDATION_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.foundation";

	/**
	 * Category per il log degli <i>ECMENGINE EJB</i>
	 * (o &quot;servizi orchestrati&quot;, EJB 2.1 che
	 * implementano la logica di business dei servizi
	 * esportati dall'ECMENGINE).
	 */
    String ECMENGINE_BUSINESS_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".business.publishing";

    /**
	 * Category per il log dello Stopwatch.
	 *
	 * <p>Lo Stopwatch viene utilizzato per tenere traccia dei tempi
	 * di esecuzione dei servizi orchestrati. Per il rilascio base
	 * dell'ECMENGINE viene utilizzato il prodotto <i>util_perf</i>
	 * versione 1.0.0.</p>
	 */
    String ECMENGINE_STOPWATCH_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY;

    /**
     * File xml contentente la configurazione per il caricamento del context
     * per il reperimento del Service Registry
     * Il caricamento attraverso la classe
     * org.springframework.context.support.ClassPathXmlApplicationContext
     */
    String ECMENGINE_CONFIGURATION_APPLICATION_CONTEXT = "beanRefContext.xml";

    /**
     * Chiave identificativa del bean factory memorizzata da
     * org.springframework.context.access.ContextSingletonBeanFactoryLocator
     */
    String ECMENGINE_BUSINESS_BEAN_KEY = "businessBeanFactory";

    /** Valorizzazione del workspace standard. */
    String ECMENGINE_STANDARD_WORKSPACE = "workspace";

    /** Valorizzazione dello store standard. */
    String ECMENGINE_STANDARD_SPACESTORE = "SpacesStore";


    /**
     * Enumerazione utilizzata nella ricerca delle associazioni
     * tra nodi
     */
    String ECMENGINE_ASSOC_TYPE_PARENT = "PARENT";
    String ECMENGINE_ASSOC_TYPE_CHILD = "CHILD";
    String ECMENGINE_ASSOC_TYPE_TARGET = "TARGET";
    String ECMENGINE_ASSOC_TYPE_SOURCE = "SOURCE";

    /*
     * enum Mode {MEMBERS, SUB_CATEGORIES, ALL};
     *
     * MEMBERS - get only category members (the things that have been classified in a category, not the sub categories)
     * SUB_CATEGORIES - get sub categories only, not the things that hyave been classified.
     * ALL - get both of the above
     *
     */
    String ECMENGINE_MODE_MEMBERS = "MEMBERS";
    String ECMENGINE_MODE_SUB_CATEGORY = "SUB_CATEGORIES";
    String ECMENGINE_MODE_ALL = "ALL";


    /*
     * enum Depth {IMMEDIATE, ANY};
     *
     * Depth from which to get nodes.
     *
     * IMMEDIATE - only immediate sub categories or members
     * ANY - find subcategories or members at any level
     */

    String ECMENGINE_DEPTH_IMMEDIATE= "IMMEDIATE";
    String ECMENGINE_DEPTH_ANY = "ANY";


    String AUDIT_ENABLEDISABLE_LOG_CATEGORY = ECMENGINE_ROOT_LOG_CATEGORY + ".audit.abilitato";

    String LOG4JEFFECTIVELEVEL = "OFF";

    /*
     * Bean utilizzati dal Job per lo spostamento dei nodi(strutture aggregative)
     */

    String ECMENGINE_AUTHENTICATION_BEAN = "authenticationComponent";

    String ECMENGINE_NODE_SERVICE_BEAN = "nodeService";

    String ECMENGINE_TRANSACTION_SERVICE_BEAN = "transactionService";

    String ECMENGINE_AUTHENTICATION_SERVICE_BEAN = "authenticationService";

    String ECMENGINE_SEARCH_SERVICE_BEAN = "searchService";

    String ECMENGINE_COPY_SERVICE_BEAN = "copyService";

    String ECMENGINE_NAMESPACE_SERVICE_BEAN = "namespaceService";

    String ECMENGINE_PERMISSION_SERVICE_BEAN = "permissionService";

    String ECMENGINE_DICTIONARY_SERVICE_BEAN = "dictionaryService";

	String ECMENGINE_TENANT_ADMIN_SERVICE_BEAN = "tenantAdminService";

	String ECMENGINE_REPO_ADMIN_SERVICE_BEAN = "repoAdminService";

	String ECMENGINE_JOB_MANAGER_BEAN = "ecmengineJobManager";

    String ECMENGINE_CONTENT_DIRECTORY = "contentDirectory";

	String ECMENGINE_CUSTOM_MODEL_LOCATION_BEAN = "customModelsRepositoryLocation";

	String ECMENGINE_DICTIONARY_MODEL_TYPE_BEAN = "dictionaryModelType";

    String ECMENGINE_CONTENT_SERVICE_BEAN = "contentService";

    String ECMENGINE_IMPORT_DIRECTORY = "importDirectory";

	String ECMENGINE_AUDIT_MANAGER_BEAN = "ecmengineAuditManager";

	String ECMENGINE_AUDIT_TRAIL_MANAGER_BEAN = "ecmengineAuditTrailManager";

	String ECMENGINE_MIMETYPE_SERVICE_BEAN = "mimetypeService";

	String ECMENGINE_INTEGRITY_SERVICE_BEAN = "integrityService";

	String ECMENGINE_FILE_FORMAT_SERVICE_BEAN = "fileformatService";

	/**
     * Nome della action per l'avvio di workflow semplici.
     */
	String ECMENGINE_SIMPLE_WORKFLOW_ACTION = "simple-workflow";

	String ECMENGINE_SYSTEM_PROPERTY_FILTERS_PROPERTY_FILE = "/filtered-system-props.properties";

	String ECMENGINE_RULE_TYPE_INBOUND  = "inbound";
	String ECMENGINE_RULE_TYPE_OUTBOUND = "outbound";
	String ECMENGINE_RULE_TYPE_UPDATE   = "update";

	String ECMENGINE_ARCHIVE_FORMAT_TAR     = "tar";
	String ECMENGINE_ARCHIVE_FORMAT_TAR_GZ  = "tar.gz";
	String ECMENGINE_ARCHIVE_FORMAT_ZIP     = "zip";

	/** Identificatori dei job **/
	String ECMENGINE_TENANT_ADMIN_JOB_REF       = "TENANT_ADMIN";
	String ECMENGINE_ARCHIVE_IMPORTER_JOB_REF   = "ARCHIVE_IMPORTER";
	String ECMENGINE_TENANT_DELETE_JOB_REF      = "TENANT_DELETE";

    /** Parametri del tenant temporaneo **/
	String ECMENGINE_TEMPORANEY_TENANT_NAME = "temp";
	String ECMENGINE_TEMPORANEY_USERNAME    = "admin";
	String ECMENGINE_TEMPORANEY_PASSWORD    = "admin";
	String ECMENGINE_TEMPORANEY_HOME        = "/app:company_home";
	String ECMENGINE_TEMPORANEY_XPATH       = "/cm:temp";

}

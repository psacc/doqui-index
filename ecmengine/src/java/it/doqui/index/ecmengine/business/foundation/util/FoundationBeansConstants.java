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

package it.doqui.index.ecmengine.business.foundation.util;

/**
 * Interfaccia che raccoglie le definizioni delle costanti
 * simboliche utilizzate per semplificare l'accesso agli
 * EJB dei servizi applicativi dai servizi di livello superiore
 * (servizi di business o servizi orchestrati).
 *
 * @author Doqui
 *
 */
public interface FoundationBeansConstants {


	/**
	 * Il nome dell'EJB che recupera il Service Registry.
	 */
	String SERVICE_REGISTRY_NAME = "ServiceRegistryBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che recupera il Service Registry.
	 */
	String SERVICE_REGISTRY_NAME_LOCAL = "ecmengine/ejb/ServiceRegistryBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di autenticazione.
	 */
	String AUTHENTICATION_SERVICE_NAME = "AuthenticationSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di autenticazione.
	 */
	String AUTHENTICATION_SERVICE_NAME_LOCAL = "ecmengine/ejb/AuthenticationSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione delle authority.
	 */
	String AUTHORITY_SERVICE_NAME = "AuthoritySvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione delle authority.
	 */
	String AUTHORITY_SERVICE_NAME_LOCAL = "ecmengine/ejb/AuthoritySvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei contenuti.
	 */
	String CONTENT_SERVICE_NAME	= "ContentSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei contenuti.
	 */
	String CONTENT_SERVICE_NAME_LOCAL = "ecmengine/ejb/ContentSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei nodi.
	 */
	String NODE_SERVICE_NAME = "NodeSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei nodi.
	 */
	String NODE_SERVICE_NAME_LOCAL = "ecmengine/ejb/NodeSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei nodi eliminati.
	 */
	String NODE_ARCHIVE_SERVICE_NAME = "NodeArchiveSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei nodi eliminati.
	 */
	String NODE_ARCHIVE_SERVICE_NAME_LOCAL = "ecmengine/ejb/NodeArchiveSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei nodi.
	 */
	String DICTIONARY_SERVICE_NAME = "DictionarySvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei nodi.
	 */
	String DICTIONARY_SERVICE_NAME_LOCAL = "ecmengine/ejb/DictionarySvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei proprietari.
	 */
	String OWNABLE_SERVICE_NAME = "OwnableSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei proprietari.
	 */
	String OWNABLE_SERVICE_NAME_LOCAL = "ecmengine/ejb/OwnableSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei permessi
	 * (o autorizzazioni).
	 */
	String PERMISSION_SERVICE_NAME = "PermissionSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei permessi (o autorizzazioni).
	 */
	String PERMISSION_SERVICE_NAME_LOCAL = "ecmengine/ejb/PermissionSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione delle persone.
	 */
	String PERSON_SERVICE_NAME = "PersonSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione delle persone.
	 */
	String PERSON_SERVICE_NAME_LOCAL = "ecmengine/ejb/PersonSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di ricerca.
	 */
	String SEARCH_SERVICE_NAME = "SearchSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di ricerca.
	 */
	String SEARCH_SERVICE_NAME_LOCAL = "ecmengine/ejb/SearchSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione delle
	 * transazioni.
	 */
	String TRANSACTION_SERVICE_NAME	= "TransactionSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione delle transazioni.
	 */
	String TRANSACTION_SERVICE_NAME_LOCAL = "ecmengine/ejb/TransactionSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di lock.
	 */
	String LOCK_SERVICE_NAME_LOCAL = "ecmengine/ejb/LockSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di checkout/checkin.
	 */
	String CHECKOUT_CHECKIN_NAME_LOCAL = "ecmengine/ejb/CheckOutCheckInSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di copia.
	 */
	String COPY_NAME_LOCAL = "ecmengine/ejb/CopySvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di versioning.
	 */
	String VERSION_NAME_LOCAL = "ecmengine/ejb/VersionSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione delle
	 * action.
	 */
	String ACTION_SERVICE_NAME	= "ActionSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione delle action.
	 */
	String ACTION_SERVICE_NAME_LOCAL = "ecmengine/ejb/ActionSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione delle
	 * rule.
	 */
	String RULE_SERVICE_NAME	= "RuleSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione delle rule.
	 */
	String RULE_SERVICE_NAME_LOCAL = "ecmengine/ejb/RuleSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei
	 * tenant.
	 */
	String TENANT_ADMIN_SERVICE_NAME	= "TenantAdminSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei tenant.
	 */
	String TENANT_ADMIN_SERVICE_NAME_LOCAL = "ecmengine/ejb/TenantAdminSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei
	 * content model dinamici.
	 */
	String REPO_ADMIN_SERVICE_NAME	= "RepoAdminSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei content model dinamici.
	 */
	String REPO_ADMIN_SERVICE_NAME_LOCAL = "ecmengine/ejb/RepoAdminSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei job.
	 */
	String JOB_SERVICE_NAME_LOCAL = "ecmengine/ejb/JobSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione degli audit.
	 */
	String AUDIT_SERVICE_NAME_LOCAL = "ecmengine/ejb/AuditSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione degli audit trail.
	 */
	String AUDIT_TRAIL_SERVICE_NAME_LOCAL = "ecmengine/ejb/AuditTrailSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei
	 * namespace.
	 */
	String NAMESPACE_SERVICE_NAME	= "NamespaceSvcBean";

	
	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei namespace.
	 */
	String NAMESPACE_SERVICE_NAME_LOCAL = "ecmengine/ejb/NamespaceSvcBean";

	/**
	 * Il nome dell'EJB che implementa il servizio di gestione dei
	 * fileFolder.
	 */
	String FILEFOLDER_SERVICE_NAME	= "FileFolderSvcBean";

	
	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione dei fileFolder .
	 */
	String FILEFOLDER_SERVICE_NAME_LOCAL = "ecmengine/ejb/FileFolderSvcBean";	
	
	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di mimetype
	 */
	String MIMETYPE_SERVICE_NAME_LOCAL = "ecmengine/ejb/MimetypeSvcBean";

	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di verifica dell'integrit&agrave
	 */	
	String INTEGRITY_SERVICE_NAME_LOCAL = "ecmengine/ejb/IntegritySvcBean";
	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di mimetype
	 */
	String FILE_FORMAT_SERVICE_NAME_LOCAL = "ecmengine/ejb/FileFormatSvcBean";
	
	/**
	 * Il nome JNDI dell'interfaccia locale dell'EJB che implementa
	 * il servizio di gestione delle category
	 */
	String CATEGORY_SERVICE_NAME_LOCAL = "ecmengine/ejb/CategorySvcBean";	
	
	String EXPORTER_SERVICE_NAME_LOCAL = "ecmengine/ejb/ExportSvcBean";
	
	String IMPORTER_SERVICE_NAME_LOCAL = "ecmengine/ejb/ImportSvcBean";
}
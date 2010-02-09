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

package it.doqui.index.ecmengine.business.publishing;

import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.business.foundation.TransactionSvc;
import it.doqui.index.ecmengine.business.foundation.TransactionSvcHome;
import it.doqui.index.ecmengine.business.foundation.audit.AuditSvc;
import it.doqui.index.ecmengine.business.foundation.audit.AuditSvcHome;
import it.doqui.index.ecmengine.business.foundation.audit.AuditTrailSvc;
import it.doqui.index.ecmengine.business.foundation.audit.AuditTrailSvcHome;
import it.doqui.index.ecmengine.business.foundation.category.CategorySvc;
import it.doqui.index.ecmengine.business.foundation.category.CategorySvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.ActionSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.ActionSvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.CheckOutCheckInSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.CheckOutCheckInSvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.CopySvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.CopySvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.FileFolderSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.FileFolderSvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.LockSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.LockSvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.NamespaceSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.NamespaceSvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.RuleSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.RuleSvcHome;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.VersionSvc;
import it.doqui.index.ecmengine.business.foundation.contentmanagement.VersionSvcHome;
import it.doqui.index.ecmengine.business.foundation.fileformat.FileFormatSvc;
import it.doqui.index.ecmengine.business.foundation.fileformat.FileFormatSvcHome;
import it.doqui.index.ecmengine.business.foundation.integrity.IntegritySvc;
import it.doqui.index.ecmengine.business.foundation.integrity.IntegritySvcHome;
import it.doqui.index.ecmengine.business.foundation.job.JobSvc;
import it.doqui.index.ecmengine.business.foundation.job.JobSvcHome;
import it.doqui.index.ecmengine.business.foundation.mimetype.MimetypeSvc;
import it.doqui.index.ecmengine.business.foundation.mimetype.MimetypeSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.ContentSvc;
import it.doqui.index.ecmengine.business.foundation.repository.ContentSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvc;
import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.ExportSvc;
import it.doqui.index.ecmengine.business.foundation.repository.ExportSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.ImportSvc;
import it.doqui.index.ecmengine.business.foundation.repository.ImportSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.NodeArchiveSvc;
import it.doqui.index.ecmengine.business.foundation.repository.NodeArchiveSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.NodeSvc;
import it.doqui.index.ecmengine.business.foundation.repository.NodeSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.RepoAdminSvc;
import it.doqui.index.ecmengine.business.foundation.repository.RepoAdminSvcHome;
import it.doqui.index.ecmengine.business.foundation.repository.TenantAdminSvc;
import it.doqui.index.ecmengine.business.foundation.repository.TenantAdminSvcHome;
import it.doqui.index.ecmengine.business.foundation.search.SearchSvc;
import it.doqui.index.ecmengine.business.foundation.search.SearchSvcHome;
import it.doqui.index.ecmengine.business.foundation.security.AuthenticationSvc;
import it.doqui.index.ecmengine.business.foundation.security.AuthenticationSvcHome;
import it.doqui.index.ecmengine.business.foundation.security.AuthoritySvc;
import it.doqui.index.ecmengine.business.foundation.security.AuthoritySvcHome;
import it.doqui.index.ecmengine.business.foundation.security.OwnableSvc;
import it.doqui.index.ecmengine.business.foundation.security.OwnableSvcHome;
import it.doqui.index.ecmengine.business.foundation.security.PermissionSvc;
import it.doqui.index.ecmengine.business.foundation.security.PermissionSvcHome;
import it.doqui.index.ecmengine.business.foundation.security.PersonSvc;
import it.doqui.index.ecmengine.business.foundation.security.PersonSvcHome;
import it.doqui.index.ecmengine.business.foundation.util.FoundationBeansConstants;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.publishing.util.IntegrityViolationHandler;
import it.doqui.index.ecmengine.dto.AclRecord;
import it.doqui.index.ecmengine.dto.ContentItem;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.dto.backoffice.Group;
import it.doqui.index.ecmengine.dto.backoffice.Tenant;
import it.doqui.index.ecmengine.dto.backoffice.User;
import it.doqui.index.ecmengine.dto.backoffice.model.CustomModel;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Association;
import it.doqui.index.ecmengine.dto.engine.management.Category;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.search.ResultProperty;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.exception.repository.NodeRuntimeException;
import it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.ecmengine.util.ISO8601DateFormat;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe astratta che implementa un generico EJB 2.x di pubblicazione dei servizi.
 *
 * <p>Questa classe si occupa di raccogliere in un unico punto le operazioni di inizializzazione
 * del logger e dello stopwatch utilizzati dai servizi pubblicati, oltre che di reperire i riferimenti
 * a tutti gli EJB wrapper dei servizi applicativi. Ogni EJB publisher deve ereditare da questa classe
 * in modo da avere direttamente disponibili i riferimenti a tutti i servizi applicativi.</p>
 *
 * @author Doqui
 */
public abstract class EcmEnginePublisherBean implements SessionBean, EcmEngineConstants {

	/** Il log utilizzato da tutti i servizi pubblicati. */
	protected static Log logger;

	/** Regex che definisce il formato degli identificativi di nodo validi. */
	protected static final String NODE_CHECK_REGEX =
		"[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}";

	protected static final long MASSIVE_MAX_CREATE_NUMBER=50;
	protected static final long MASSIVE_MAX_CREATE_SIZE=9437184; //9 MB, in Byte.
	protected static final long MASSIVE_MAX_RETRIEVE_NUMBER=50;
	protected static final long MASSIVE_MAX_RETRIEVE_SIZE=9437184; //9 MB, in Byte.
	protected static final long MASSIVE_MAX_GET_METADATA_SIZE=50;
	protected static final long MASSIVE_MAX_DELETE_NUMBER=50;
	protected static final long USERNAME_MAX_LENGTH = 70;

	/**
	 * Restituisce il repository sul quale verranno eseguite le operazioni successive.
	 *
	 * @return L'identificativo del repository corrente.
	 */
	protected static String getCurrentRepository() {
		logger.debug("[EcmEnginePublisherBean::getCurrentRepository] BEGIN");

		final String repository = RepositoryManager.getCurrentRepository();
        if( logger.isDebugEnabled() ) {
		    logger.debug("[EcmEnginePublisherBean::getCurrentRepository] Current repository: " + repository);
    	}

		logger.debug("[EcmEnginePublisherBean::getCurrentRepository] END");
		return repository;
	}

	/**
	 * Restituisce il repository predefinito.
	 *
	 * @return L'identificativo del repository predefinito.
	 */
	protected static String getDefaultRepository() {
		logger.debug("[EcmEnginePublisherBean::getDefaultRepository] BEGIN");

		final String repository = RepositoryManager.getInstance().getDefaultRepository().getId();
        if( logger.isDebugEnabled() ) {
		    logger.debug("[EcmEnginePublisherBean::getDefaultRepository] Default repository: " + repository);
    	}

		logger.debug("[EcmEnginePublisherBean::getDefaultRepository] END");
		return repository;
	}
	/**
	 * Restituisce gli identificativi dei repository conosciuti.
	 *
	 * @return Una lista di identificativi dei repository conosciuti.
	 */
	protected static List<Repository> getKnownRepositories() {
		logger.debug("[EcmEnginePublisherBean::getKnownRepositories] BEGIN");
		List<Repository> repositories = new ArrayList<Repository>(RepositoryManager.getInstance().getRepositories().size());

		for (Repository repo : RepositoryManager.getInstance().getRepositories()) {
            Repository newRep = new Repository();
            		   newRep.setDataSource(		 repo.getDataSource()           );
            		   newRep.setId(				 repo.getId()            		);
            		   newRep.setContentRootLocation(repo.getContentRootLocation()  );
            		   newRep.setIndexRootLocation(  repo.getIndexRootLocation()    );
            		   newRep.setIndexBackupLocation(repo.getIndexBackupLocation()  );
            		   newRep.setCacheConfigLocation(repo.getCacheConfigLocation()	);
            		   newRep.setIndexRecoveryMode(  repo.getIndexRecoveryMode()	);
 		   			   newRep.setContentStores( 	 repo.getContentStores() 		);
			repositories.add( newRep );
		}
		logger.debug("[EcmEnginePublisherBean::getKnownRepositories] END");
		return repositories;
	}

	/**
	 * Seleziona il repository richiesto per l'esecuzione delle operazioni successive.
	 *
	 * <p><strong>NB:</strong> se viene specificato un valore {@code null} viene impostato il
	 * repository predefinito. Se il repository viene specificato ma non esiste viene sollevata
	 * un'eccezione e viene eseguito il rollback della transazione eventualmente specificata.</p>
	 *
	 * @param repository L'identificativo del repository da selezionare.
	 * @param transaction La transazione di cui eseguire il rollback, oppure {@code null}.
	 *
	 * @throws InvalidParameterException Se il repository specificato non esiste.
	 */
	protected static void selectRepository(String repository, UserTransaction transaction) throws InvalidParameterException {
		logger.debug("[EcmEnginePublisherBean::selectRepository] BEGIN");

		try {
			RepositoryManager.setCurrentRepository(repository);
    	    if (logger.isDebugEnabled()) {
    			logger.debug("[EcmEnginePublisherBean::selectRepository] Repository selected: " + repository);
		    }
		} catch (IllegalArgumentException e) {
			logger.warn("[EcmEnginePublisherBean::selectRepository] Non-existent repository requested: " + repository);

			if (transaction != null) {
				rollbackQuietely(transaction);
			}
			throw new InvalidParameterException("Repository '" + repository + "' doesn't exist!");
		} finally {
			logger.debug("[EcmEnginePublisherBean::selectRepository] END");
		}
	}

	private SessionContext sessionContext;

	private transient InitialContext initialContext;

	private transient AuthenticationSvcHome authenticationServiceHome;

	private transient AuthoritySvcHome authorityServiceHome;

	private transient ContentSvcHome contentServiceHome;

	private transient DictionarySvcHome dictionaryServiceHome;

	private transient FileFolderSvcHome fileFolderServiceHome;

	private transient NodeSvcHome nodeServiceHome;

	private transient NodeArchiveSvcHome nodeArchiveServiceHome;

	private transient OwnableSvcHome ownableServiceHome;

	private transient PermissionSvcHome permissionServiceHome;

	private transient PersonSvcHome personServiceHome;

	private transient SearchSvcHome searchServiceHome;

	private transient TransactionSvcHome transactionServiceHome;

	/* Content management services */
	private transient LockSvcHome lockServiceHome;

	private transient CheckOutCheckInSvcHome checkOutCheckInHome;

	private transient CopySvcHome copyHome;

	private transient VersionSvcHome versionHome;

	private transient ActionSvcHome actionHome;

	private transient RuleSvcHome ruleHome;

	private transient TenantAdminSvcHome tenantAdminHome;

	private transient RepoAdminSvcHome repoAdminHome;

	private transient JobSvcHome jobHome;

	protected transient AuditSvcHome auditHome;

	protected transient AuditTrailSvcHome auditTrailHome;

	private transient MimetypeSvcHome mimetypeHome;

	private transient IntegritySvcHome integrityHome;

	private transient NamespaceSvcHome namespaceServiceHome;

	private transient FileFormatSvcHome fileFormatHome;

	private transient CategorySvcHome categoryHome;

	private transient ExportSvcHome exporterHome;

	private transient ImportSvcHome importerHome;

	/** Lo stopwatch da utilizzare per la registrazione dei tempi. */
	protected transient StopWatch stopwatch;

	/** Il servizio di autenticazione. */
	protected transient AuthenticationSvc authenticationService;

	/** Il servizio di gestione delle authority. */
	protected transient AuthoritySvc authorityService;

	/** Il servizio di gestione dei contenuti. */

	protected transient ContentSvc contentService;
	/** Il servizio di gestione del dizionario del content model. */
	protected transient DictionarySvc dictionaryService;

	/** Il servizio di gestione del dizionario del content model. */
	protected transient FileFolderSvc fileFolderService;

	/** Il servizio di gestione dei nodi. */
	protected transient NodeSvc nodeService;

	/** Il servizio di gestione dei nodi eliminati. */
	protected transient NodeArchiveSvc nodeArchiveService;

	/** Il servizio di gestione delle ownership. */
	protected transient OwnableSvc ownableService;

	/** Il servizio di gestione dei permessi. */
	protected transient PermissionSvc permissionService;

	/** Il servizio di gestione delle persone. */
	protected transient PersonSvc personService;

	/** Il servizio di ricerca. */
	protected transient SearchSvc searchService;

	/** Il servizio di gestione delle transazioni. */
	protected transient TransactionSvc transactionService;

	/** Il servizio di gestione del lock. */
	protected transient LockSvc lockService;

	/** Il servizio di gestione del checkout/checkin. */
	protected transient CheckOutCheckInSvc checkOutCheckInService;

	/** Il servizio di copia dei contenuti. */
	protected transient CopySvc copyService;

	/** Il servizio di gestione dl versioning. */
	protected transient VersionSvc versionService;

	/** Il servizio di gestione delle action. */
	protected transient ActionSvc actionService;

	/** Il servizio di gestione delle rule. */
	protected transient RuleSvc ruleService;

	/** Il servizio di gestione dei tenant. */
	protected transient TenantAdminSvc tenantAdminService;

	/** Il servizio di gestione dei content model dinamici. */
	protected transient RepoAdminSvc repoAdminService;

	/** Il servizio di gestione dei job. */
	protected transient JobSvc jobService;

	/** Il servizio di gestione degli audit. */
	protected transient AuditSvc auditService;

	/** Il servizio di gestione degli audit trail. */
	protected transient AuditTrailSvc auditTrailService;

	/** Il servizio di gestione dei mimetype */
	protected transient MimetypeSvc mimetypeService;

	/** Il servizio di verifica di integrit&agrave degli indici */
	protected transient IntegritySvc integrityService;

	/** Il servizio di gestione dei namespace */
	protected transient NamespaceSvc namespaceService;

	/** Il servizio di riconoscimento dei formati dei file */
	protected transient FileFormatSvc fileFormatService;

	/** Il servizio di gestione delle categorie */
	protected transient CategorySvc categoryService;

	protected transient ExportSvc exporterService;

	protected transient ImportSvc importerService;


	/**
	 * Seleziona il repository richiesto ed esegue l'autenticazione utilizzando
	 * le informazioni specificate.
	 *
	 * <p>Le operazioni successive saranno eseguite sul repository che &egrave; stato selezionato
	 * utilizzando questo metodo.</p>
	 *
	 * <p><strong>NB:</strong> se viene specificato un valore {@code null} viene impostato il
	 * repository predefinito. Se si verifica un errore qualsiasi durante l'esecuzione viene eseguito il
	 * rollback della transazione eventualmente specificata in input.</p>
	 *
	 * @param ctx L'oggetto {@link OperationContext} contenente le informazioni di autenticazione.
	 * @param transaction La transazione di cui eseguire il rollback, oppure {@code null}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri specificati non &egrave; valido.
	 * @throws WrongCredentialsException Se le informazioni di autenticazione specificate sono errate.
	 * @throws AuthenticationRuntimeException Se si verifica un errore generico nel servizio di autenticazione.
	 */
	public void authenticateOnRepository(OperationContext ctx, UserTransaction transaction)
	throws InvalidParameterException, AuthenticationRuntimeException {
		logger.debug("[EcmEnginePublisherBean::authenticateOnRepository] BEGIN");

		try {
			// Set current repository.
			final String currentRepository = ctx.getRepository();
			selectRepository(currentRepository, transaction);

			authenticationService.authenticate(ctx.getUsername(), ctx.getPassword().toCharArray());

    	    if (logger.isDebugEnabled()) {
			    logger.debug("[EcmEnginePublisherBean::authenticateOnRepository] Authenticated user \"" + ctx.getUsername() + "\" on repository \"" + currentRepository + "\"");
            }
		} catch (AuthenticationRuntimeException e) {
			logger.debug("[EcmEnginePublisherBean::authenticateOnRepository] " +
					"Got AuthenticationRuntimeException: " + e.getMessage());

			if (transaction != null) {
				rollbackQuietely(transaction);
			}
			throw e;
		} finally {
			logger.debug("[EcmEnginePublisherBean::authenticateOnRepository] END");
		}
	}

	/**
	 * Verifica che il nodo specificato in input esista nel workspace sul repository dell'ECMENGINE.
	 *
	 * <p>Se il nodo non esiste ed &egrave; viene eseguito il <i>rollback</i> della transazione
	 * eventualmente specificata.</p>
	 *
	 * <p><strong>NB:</strong> il controllo viene eseguito sul repository settato come corrente nel momento
	 * in cui il metodo viene invocato. L'implementazione di questo metodo non prende in considerazione l'ID di
	 * repository impostato nel DTO ricevuto in input.</p>
	 *
	 * @param node Il {@link Node} contente l'UID del nodo di cui verificare l'esistenza.
	 * @param transaction La transazione corrente.
	 *
	 * @return Il riferimento al nodo, se esso esiste.
	 *
	 * @throws NoSuchNodeException Se il nodo non esiste o se si verifica un errore durante il controllo.
	 */
	protected NodeRef checkNodeExists(Node node, UserTransaction transaction) throws NoSuchNodeException {
		logger.debug("[EcmEnginePublisherBean::checkNodeExists] BEGIN");
		final NodeRef nodeRef = new NodeRef(DictionarySvc.SPACES_STORE, node.getUid());

		try {
			if (!nodeService.exists(nodeRef)) {
				logger.warn("[EcmEnginePublisherBean::checkNodeExists] Node not found: " + node.getUid());

				if (transaction != null) {
					rollbackQuietely(transaction);
				}
				throw new NoSuchNodeException(node.getUid());
			}

			return nodeRef;
		} catch (NodeRuntimeException e) {
			logger.error("[EcmEnginePublisherBean::checkNodeExists] Error while checking existance of node: " + nodeRef, e);
			if (transaction != null) {
				rollbackQuietely(transaction);
			}
			throw new NoSuchNodeException(node.getUid());
		} finally {
			logger.debug("[EcmEnginePublisherBean::checkNodeExists] END");
		}
	}

	/**
	 * Verifica che il nodo specificato in input esista nell'archivio dei nodi eliminati sul
	 * repository dell'ECMENGINE.
	 *
	 * <p>Se il nodo non esiste ed &egrave; viene eseguito il <i>rollback</i> della transazione
	 * eventualmente specificata.</p>
	 *
	 * <p><strong>NB:</strong> il controllo viene eseguito sul repository settato come corrente nel momento
	 * in cui il metodo viene invocato. L'implementazione di questo metodo non prende in considerazione l'ID di
	 * repository impostato nel DTO ricevuto in input.</p>
	 *
	 * @param node Il {@link Node} contente l'UID del nodo di cui verificare l'esistenza.
	 * @param transaction La transazione corrente.
	 *
	 * @return Il riferimento al nodo, se esso esiste.
	 *
	 * @throws NoSuchNodeException Se il nodo non esiste o se si verifica un errore durante il controllo.
	 */
	protected NodeRef checkNodeArchived(Node node, UserTransaction transaction) throws NoSuchNodeException {
		logger.debug("[EcmEnginePublisherBean::checkNodeArchived] BEGIN");
		final NodeRef nodeRef = new NodeRef(DictionarySvc.ARCHIVE_SPACES_STORE, node.getUid());

		try {
			if (!nodeService.exists(nodeRef)) {
				logger.warn("[EcmEnginePublisherBean::checkNodeArchived] " +
						"Node not found: " + node.getUid());

				if (transaction != null) {
					rollbackQuietely(transaction);
				}
				throw new NoSuchNodeException(node.getUid());
			}

			return nodeRef;
		} catch (NodeRuntimeException e) {
			logger.error("[EcmEnginePublisherBean::checkNodeArchived] Error while checking archived state of node: " + nodeRef, e);
			if (transaction != null) {
				rollbackQuietely(transaction);
			}
			throw new NoSuchNodeException(node.getUid());
		} finally {
			logger.debug("[EcmEnginePublisherBean::checkNodeArchived] END");
		}
	}

	protected String convertPropertyValueToString(Object value) {
		logger.debug("[EcmEnginePublisherBean::convertPropertyValueToString] BEGIN");
		try {

			if (value == null) {
				return "-- null --";
			} else if (value instanceof String
					|| value instanceof Long
					|| value instanceof Float
					|| value instanceof Double
					|| value instanceof Integer
					|| value instanceof MLText
					|| value instanceof ContentData
					|| value instanceof Boolean
					|| value instanceof NodeRef) {

				return value.toString();
			} else if (value instanceof Date) {
				return ISO8601DateFormat.format((Date) value);
			} else {
				return "-- unknown type: " + value.getClass() + " --";
			}
		} finally {
			logger.debug("[EcmEnginePublisherBean::convertPropertyValueToString] END");
		}
	}

	/**
	 * Registra sul logger dello stowpatch il tempo misurato al momento della chiamata.
	 *
	 * @param className Il nome della classe chiamante.
	 * @param methodName Il nome del metodo chiamante.
	 * @param ctx Il contesto in cui il metodo &egrave; stato chiamato.
	 * @param message Un messaggio da registrare nel log assieme al tempo.
	 */
	protected void dumpElapsed(String className, String methodName, String ctx, String message) {
		this.stopwatch.dumpElapsed(className, methodName, ctx, message);
	}

	/**
	 * Metodo richiamato all'attivazione di un'istanza del session bean. Nell'implementazione
	 * corrente &egrave; vuoto.
	 *
	 * @throws EJBException Se si verificano errori durante l'esecuzione.
	 * @throws RemoteException Se si verificano errori durante la comunicazione remota.
	 */
	public void ejbActivate() throws EJBException, RemoteException { }

	/**
	 * Metodo chiamato dall'invocazione del metodo {@code create()} sulla
	 * Home Interface dell'EJB di pubblicazione dei servizi. Esso si occupa del reperimento
	 * degli EJB wrapper dei servizi applicativi, dell'inizializzazione del logger e dello
	 * stopwatch.
	 *
	 * @throws EJBException Se si verificano errori durante l'inizializzazione dell'EJB.
	 * @throws RemoteException Se si verificano errori durante la comunicazione remota.
	 */
	public void ejbCreate() throws EJBException, RemoteException {
		logger = LogFactory.getLog(ECMENGINE_BUSINESS_LOG_CATEGORY);
		logger.debug("[EcmEnginePublisherBean::ejbCreate] BEGIN");

		try {
			this.initialContext = new InitialContext();

			logger.debug("[EcmEnginePublisherBean::ejbCreate] Lookup dei servizi applicativi.");
			this.authenticationServiceHome = (AuthenticationSvcHome)lookup(
					FoundationBeansConstants.AUTHENTICATION_SERVICE_NAME_LOCAL);
			this.authorityServiceHome = (AuthoritySvcHome)lookup(
					FoundationBeansConstants.AUTHORITY_SERVICE_NAME_LOCAL);
			this.contentServiceHome = (ContentSvcHome)lookup(
					FoundationBeansConstants.CONTENT_SERVICE_NAME_LOCAL);
			this.dictionaryServiceHome = (DictionarySvcHome)lookup(
					FoundationBeansConstants.DICTIONARY_SERVICE_NAME_LOCAL);
			this.fileFolderServiceHome = (FileFolderSvcHome)lookup(
					FoundationBeansConstants.FILEFOLDER_SERVICE_NAME_LOCAL);
			this.namespaceServiceHome = (NamespaceSvcHome)lookup(
					FoundationBeansConstants.NAMESPACE_SERVICE_NAME_LOCAL);
			this.nodeServiceHome = (NodeSvcHome)lookup(
					FoundationBeansConstants.NODE_SERVICE_NAME_LOCAL);
			this.nodeArchiveServiceHome = (NodeArchiveSvcHome)lookup(
					FoundationBeansConstants.NODE_ARCHIVE_SERVICE_NAME_LOCAL);
			this.ownableServiceHome = (OwnableSvcHome)lookup(
					FoundationBeansConstants.OWNABLE_SERVICE_NAME_LOCAL);
			this.permissionServiceHome = (PermissionSvcHome)lookup(
					FoundationBeansConstants.PERMISSION_SERVICE_NAME_LOCAL);
			this.personServiceHome = (PersonSvcHome)lookup(
					FoundationBeansConstants.PERSON_SERVICE_NAME_LOCAL);
			this.searchServiceHome = (SearchSvcHome)lookup(
					FoundationBeansConstants.SEARCH_SERVICE_NAME_LOCAL);
			this.transactionServiceHome = (TransactionSvcHome)lookup(
					FoundationBeansConstants.TRANSACTION_SERVICE_NAME_LOCAL);
			this.lockServiceHome = (LockSvcHome)lookup(
					FoundationBeansConstants.LOCK_SERVICE_NAME_LOCAL);
			this.checkOutCheckInHome = (CheckOutCheckInSvcHome)lookup(
					FoundationBeansConstants.CHECKOUT_CHECKIN_NAME_LOCAL);
			this.copyHome = (CopySvcHome)lookup(
					FoundationBeansConstants.COPY_NAME_LOCAL);
			this.versionHome = (VersionSvcHome)lookup(
					FoundationBeansConstants.VERSION_NAME_LOCAL);
			this.actionHome = (ActionSvcHome)lookup(
					FoundationBeansConstants.ACTION_SERVICE_NAME_LOCAL);
			this.ruleHome = (RuleSvcHome)lookup(
					FoundationBeansConstants.RULE_SERVICE_NAME_LOCAL);
			this.tenantAdminHome = (TenantAdminSvcHome)lookup(
					FoundationBeansConstants.TENANT_ADMIN_SERVICE_NAME_LOCAL);
			this.repoAdminHome = (RepoAdminSvcHome)lookup(
					FoundationBeansConstants.REPO_ADMIN_SERVICE_NAME_LOCAL);
			this.jobHome = (JobSvcHome)lookup(
					FoundationBeansConstants.JOB_SERVICE_NAME_LOCAL);
			this.auditHome = (AuditSvcHome)lookup(
					FoundationBeansConstants.AUDIT_SERVICE_NAME_LOCAL);
			this.auditTrailHome = (AuditTrailSvcHome)lookup(
					FoundationBeansConstants.AUDIT_TRAIL_SERVICE_NAME_LOCAL);
			this.mimetypeHome = (MimetypeSvcHome)lookup(
					FoundationBeansConstants.MIMETYPE_SERVICE_NAME_LOCAL);
			this.integrityHome = (IntegritySvcHome)lookup(
					FoundationBeansConstants.INTEGRITY_SERVICE_NAME_LOCAL);
			this.fileFormatHome = (FileFormatSvcHome)lookup(
					FoundationBeansConstants.FILE_FORMAT_SERVICE_NAME_LOCAL);
			this.categoryHome = (CategorySvcHome)lookup(
					FoundationBeansConstants.CATEGORY_SERVICE_NAME_LOCAL);
			this.exporterHome = (ExportSvcHome)lookup(
					FoundationBeansConstants.EXPORTER_SERVICE_NAME_LOCAL);
			this.importerHome = (ImportSvcHome)lookup(
				FoundationBeansConstants.IMPORTER_SERVICE_NAME_LOCAL);

			logger.debug("[EcmEnginePublisherBean::ejbCreate] Creazione dei servizi applicativi.");
			this.authenticationService = this.authenticationServiceHome.create();
			this.authorityService = this.authorityServiceHome.create();
			this.contentService = this.contentServiceHome.create();
			this.dictionaryService = this.dictionaryServiceHome.create();
			this.nodeService = this.nodeServiceHome.create();
			this.nodeArchiveService = this.nodeArchiveServiceHome.create();
			this.ownableService = this.ownableServiceHome.create();
			this.permissionService = this.permissionServiceHome.create();
			this.personService = this.personServiceHome.create();
			this.searchService = this.searchServiceHome.create();
			this.transactionService = this.transactionServiceHome.create();
			this.lockService = this.lockServiceHome.create();
			this.checkOutCheckInService = this.checkOutCheckInHome.create();
			this.copyService = this.copyHome.create();
			this.versionService = this.versionHome.create();
			this.actionService = this.actionHome.create();
			this.ruleService = this.ruleHome.create();
			this.tenantAdminService = this.tenantAdminHome.create();
			this.repoAdminService = this.repoAdminHome.create();
			this.jobService = this.jobHome.create();
			this.auditService = this.auditHome.create();
			this.auditTrailService = this.auditTrailHome.create();
			this.mimetypeService = this.mimetypeHome.create();
			this.integrityService = this.integrityHome.create();
			this.namespaceService = this.namespaceServiceHome.create();
			this.fileFolderService = this.fileFolderServiceHome.create();
			this.fileFormatService = this.fileFormatHome.create();
			this.categoryService = this.categoryHome.create();
			this.exporterService = this.exporterHome.create();
			this.importerService = this.importerHome.create();

		} catch (NamingException e) {
			logger.error("[EcmEnginePublisherBean::ejbCreate] Errore nel lookup dei bean dei " +
					"servizi applicativi: " + e.getMessage());
			throw new EJBException("Errore nel lookup dei bean dei servizi applicativi.");
		} catch (CreateException e) {
			logger.error("[EcmEnginePublisherBean::ejbCreate] Errore nella creazione dei bean dei" +
					"servizi applicativi: " + e.getMessage());
			throw new EJBException("Errore nella creazione dei bean dei servizi applicativi.");
		} finally {
			logger.debug("[EcmEnginePublisherBean::ejbCreate] END");
		}
	}

	/**
	 * Metodo richiamato alla passivazione di un'istanza del session bean. Nell'implementazione
	 * corrente &egrave; vuoto.
	 *
	 * @throws EJBException Se si verificano errori durante l'esecuzione.
	 * @throws RemoteException Se si verificano errori durante la comunicazione remota.
	 */
	public void ejbPassivate() throws EJBException, RemoteException { }

	/**
	 * Metodo richiamato alla distruzione di un'istanza del session bean. Nell'implementazione
	 * corrente &egrave; vuoto.
	 *
	 * @throws EJBException Se si verificano errori durante l'esecuzione.
	 * @throws RemoteException Se si verificano errori durante la comunicazione remota.
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * Restituisce il context di esecuzione del session bean impostato dall'EJB container.
	 *
	 * @return Il context di esecuzione.
	 */
	protected SessionContext getSessionContext() {
		return this.sessionContext;
	}

	/**
	 * Registra un record sul sistema di audit dell'ECMENGINE.
	 *
	 * @param className Il nome della classe chiamante.
	 * @param methodName Il nome del metodo chiamante.
	 * @param logContext Il contesto in cui il metodo &egrave; stato chiamato.
	 * @param context L'oggetto {@link OperationContext} utilizzato per la chiamata al servizio.
	 * @param idOggetto L'ID dell'oggetto modificato, creato o rimosso.
	 * @param descrizioneOggetto La descrizione dell'oggetto modificato, creato o rimosso.
	 */
	protected void insertAudit(String className, String methodName, String logContext,
			OperationContext context, String idOggetto, String descrizioneOggetto) {
		try{
			OperazioneAudit operazioneAudit = new OperazioneAudit();
			operazioneAudit.setUtente(context.getNomeFisico());
			operazioneAudit.setNomeOperazione(methodName);
			operazioneAudit.setServizio(className);
			operazioneAudit.setFruitore(context.getFruitore());
			operazioneAudit.setDataOra(new Date());
			operazioneAudit.setIdOggetto(idOggetto);
			operazioneAudit.setTipoOggetto(descrizioneOggetto);
			auditService.insertAudit(operazioneAudit);

			dumpElapsed(className, methodName, logContext, "Audit inserito.");
		} catch (Exception e) {
			logger.error("[" + className + "::" + methodName + "] Errore nell'inserimento Audit: " + e.getMessage());
		}
	}

	/**
	 * Esegue il lookup di un EJB arbitrario usando l'{@code InitialContext} unico
	 * definito all'inizializzazione di un'istanza di {@code EcmEnginePublisherBean}.
	 *
	 * <p><strong>NB:</strong> il risultato va castato opportunamente.</p>
	 *
	 * @param ejbName Il nome della Home Interface da localizzare.
	 *
	 * @return L'istanza della Home Interface da utilizzare per creare una nuova istanza dell'EJB.
	 *
	 * @throws NamingException Se si verifica un errore durante il lookup.
	 */
	protected Object lookup(String ejbName) throws NamingException {
		Object home;

		if (this.initialContext == null) {
			return null;
		}

		home = this.initialContext.lookup(ejbName);

        if( logger.isDebugEnabled() ) {
	    	logger.debug("[EcmEnginePublisherBean::lookup] Looked up EJB \"" + ejbName + "\": " + home);
        }

		return home;
	}

	/**
	 * Metodo richiamato dall'EJB container per impostare il context di esecuzione del bean stesso.
	 *
	 * @param context Il contesto di esecuzione.
	 *
	 * @throws EJBException Se si verificano errori durante l'esecuzione.
	 * @throws RemoteException Se si verificano errori durante la comunicazione remota.
	 */
	public void setSessionContext(SessionContext context) throws EJBException, RemoteException {
		this.sessionContext = context;
	}
	/**
	 * Azzera e avvia la misurazione dei tempi da parte dello stopwatch.
	 */
	protected void start() {
		this.stopwatch = new StopWatch(ECMENGINE_STOPWATCH_LOG_CATEGORY);
		this.stopwatch.start();
	}

	/**
	 * Arresta la misurazione dei tempi da parte dello stopwatch.
	 */
	protected void stop() {
		this.stopwatch.stop();
	}

	/**
	 * Controlla se almeno uno degli argomenti in input nell'inserimento o ricerca
	 * dell'audit trail sia valorizzato.
	 *
	 * @param user utente che ha eseguito l'operazione su un contenuto del repository
	 * @param operazione l'operazione eseguita su un contenuto del repository
	 * @param idOggetto identificativo del contenuto
	 * @return {@code true} se almento un attributo &egrave; valorizzato,{@code false} altrimenti.
	 */
	protected static boolean isValidLogTrailArgument(String user, String operazione, String idOggetto) {
		if(user!=null || operazione !=null || idOggetto!=null)
			return true;
		return false;
	}

	/**
	 * Traduce una mappa di metadati ricevuta in input nel corispondente array di oggetti
	 * {@link ResultProperty}.
	 *
	 * @param props La {@code Map} da tradurre.
	 *
	 * @return Un array di {@link ResultProperty}.
	 */
	protected ResultProperty [] translatePropertyMap(Map<QName, Serializable> props,Property[] properties) {
		logger.debug("[EcmEnginePublisherBean::translatePropertyMap] BEGIN");
		ResultProperty current = null;
		ArrayList<ResultProperty> propArray = null;

		try {
			if (props == null) {
				return null;
			}

			propArray = new ArrayList<ResultProperty>();

			logger.debug("[EcmEnginePublisherBean::translatePropertyMap] Creazione array metadati.");
			//AF: se non sono richieste proprieta' particolari, vengono restituite tutte le propieta'
			if(properties==null||properties.length==0){
				// Ciclo sulle proprieta`
				int index = 0;
				for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
					ResultProperty rp = new ResultProperty();
					final QName propName = entry.getKey();
					final Serializable v = entry.getValue();

					rp.setPrefixedName(dictionaryService.resolveQNameToPrefixName(propName));

					if (v instanceof Collection<?>) {
						Collection<?> valuesCollection = (Collection<?>) v;
						String values [] = new String[valuesCollection.size()];

						int j = 0;
						for (Object o : valuesCollection) {
							values[j] = convertPropertyValueToString(o);
							j++;
						}

						rp.setMultivalue(true);
						rp.setValues(values);
					} else {
						rp.setMultivalue(false);
						rp.setValues(new String [] { convertPropertyValueToString(v) });
					}

            	    if( logger.isDebugEnabled() ) {
                       StringBuffer aOut = new StringBuffer();
                       for( String p : rp.getValues() ){
                           aOut.append( p +" " );
                       }
                       logger.debug("[EcmEnginePublisherBean::translatePropertyMap] " +rp.getPrefixedName() +" = " +aOut.toString());
					}
					propArray.add(rp);
					index++;
				}
			}
			//AF: se sono richieste proprieta' particolari, viene risolto il nome con il prefisso (prefixedName, ad esempio cm:content) convertendo il prefisso nel percorso dello schema di riferimento, e viene inserita solo quella proprieta'
			else{
				for(Property prop:properties){
					ResultProperty rp = new ResultProperty();
					QName propName = dictionaryService.resolvePrefixNameToQName(prop.getPrefixedName());
					Serializable v = props.get(propName);
					if(propName!=null&&v!=null){
						rp.setPrefixedName(dictionaryService.resolveQNameToPrefixName(propName));

						if (v instanceof Collection<?>) {
							Collection<?> valuesCollection = (Collection<?>) v;
							String values [] = new String[valuesCollection.size()];

							int j = 0;
							for (Object o : valuesCollection) {
								values[j] = convertPropertyValueToString(o);
								j++;
							}

							rp.setMultivalue(true);
							rp.setValues(values);
						} else {
							rp.setMultivalue(false);
							rp.setValues(new String [] { convertPropertyValueToString(v) });
						}

                        if( logger.isDebugEnabled() ) {
                           StringBuffer aOut = new StringBuffer();
                           for( String p : rp.getValues() ){
                               aOut.append( p +" " );
                           }
                           logger.debug("[EcmEnginePublisherBean::translatePropertyMap] " +rp.getPrefixedName() +" = " +aOut.toString());
                        }
						propArray.add(rp);
					}
				}
			}

		} catch (DictionaryRuntimeException e) {
			logger.warn("[EcmEnginePublisherBean::translatePropertyMap] " +
					"Errore nella traduzione della property " + current.getPrefixedName() +
					": property sconosciuta!");
		} finally {
			logger.debug("[EcmEnginePublisherBean::translatePropertyMap] END");
		}
		return propArray.toArray(new ResultProperty[propArray.size()]);
	}

	/**
	 * Estrae un insieme ridotto dei metadati di un contenuto da utilizzare nel result set
	 * di una ricerca.
	 *
	 * <p>I metadati estratti da questo metodo sono i seguenti:</p>
	 * <ul>
	 *  <li>{@code cm:name}: nome del contenuto.</li>
	 *  <li>{@code cm:created}: data di creazione.</li>
	 *  <li>{@code cm:creator}: creatore.</li>
	 *  <li>{@code cm:modified}: data di ultima modifica.</li>
	 *  <li>{@code cm:modifier}: autore dell'ultima modifica.</li>
	 *  <li>{@code sys:store-identifier}: identificativo dello store.</li>
	 *  <li>{@code sys:store-protocol}: protocollo di accesso allo store.</li>
	 * </ul>
	 *
	 * @param props La {@code Map} completa contenente i metadati.
	 *
	 * @return Un array contenente una parte dei metadati del contenuto.
	 */
	protected ResultProperty [] translateReducedPropertyMap(Map<QName, Serializable> props) {
		logger.debug("[EcmEnginePublisherBean::translateReducedPropertyMap] BEGIN");

		try {
			Vector<ResultProperty> propVector = new Vector<ResultProperty>(7);

			logger.debug("[EcmEnginePublisherBean::translateReducedPropertyMap] Creazione array metadati ridotto: 7");

			// Ciclo sulle proprieta`

			// cm:name
			ResultProperty propName = new ResultProperty();
			propName.setPrefixedName("cm:name");
			propName.setValues(new String [] { (String) props.get(ContentModel.PROP_NAME) });

			propVector.add(propName);
			propName = null;

			// cm:created
			Date rawCreationDate = (Date) props.get(ContentModel.PROP_CREATED);

			if (rawCreationDate != null) {
				ResultProperty propCreationDate = new ResultProperty();
				propCreationDate.setPrefixedName("cm:created");
				propCreationDate.setValues(new String [] { ISO8601DateFormat.format(rawCreationDate) });

				propVector.add(propCreationDate);
				propCreationDate = null;
				rawCreationDate = null;
			}

			// cm:creator
			String rawCreator = (String) props.get(ContentModel.PROP_CREATOR);

			if (rawCreator != null) {
				ResultProperty propCreator = new ResultProperty();
				propCreator.setPrefixedName("cm:creator");
				propCreator.setValues(new String [] { rawCreator });

				propVector.add(propCreator);
				propCreator = null;
				rawCreator = null;
			}

			// cm:modified
			Date rawModifiedDate = (Date) props.get(ContentModel.PROP_MODIFIED);

			if (rawModifiedDate != null) {
				ResultProperty propModifiedDate = new ResultProperty();
				propModifiedDate.setPrefixedName("cm:modified");
				propModifiedDate.setValues(new String [] { ISO8601DateFormat.format(rawModifiedDate) });

				propVector.add(propModifiedDate);
				propModifiedDate = null;
				rawCreationDate = null;
			}

			// cm:modifier
			String rawModifier = (String) props.get(ContentModel.PROP_MODIFIER);

			if (rawModifier != null) {
				ResultProperty propModifier = new ResultProperty();
				propModifier.setPrefixedName("cm:modifier");
				propModifier.setValues(new String [] { rawCreator });

				propVector.add(propModifier);
				propModifier = null;
				rawModifier = null;
			}

			// sys:store-identifier
			ResultProperty propStoreId = new ResultProperty();
			propStoreId.setPrefixedName("sys:store-identifier");
			propStoreId.setValues(new String [] { (String) props.get(ContentModel.PROP_STORE_IDENTIFIER) });

			propVector.add(propStoreId);
			propStoreId = null;

			// sys:store-protocol
			ResultProperty propStoreProtocol = new ResultProperty();
			propStoreProtocol.setPrefixedName("sys:store-protocol");
			propStoreProtocol.setValues(new String [] { (String) props.get(ContentModel.PROP_STORE_PROTOCOL) });

			propVector.add(propStoreProtocol);
			propStoreProtocol = null;

			return propVector.toArray(new ResultProperty[] {});
		} finally {
			logger.debug("[EcmEnginePublisherBean::translateReducedPropertyMap] END");
		}
	}

	protected void handleTransactionException(Throwable ex, String msg) throws EcmEngineTransactionException {
		logger.warn("[EcmEnginePublisherBean::handleTransactionException] Error in transaction: " + msg, ex);
		throw new EcmEngineTransactionException(msg);
	}

	protected enum ValidationType {
		NOT_NULL,
		NODE,
		OPERATION_CONTEXT,
		SEARCH_PARAMETER,
		AUDIT_INFO,
		PREFIXED_NAME,
		PREFIX,
		NAME,
		NOT_GUEST,
		PASSWORD,
		CONTENT_ITEM,
		ASPECT,
		PROPERTY,
		CONTENT_READ,
		CONTENT_WRITE_CONTENT,
		CONTENT_WRITE_METADATA,
		CONTENT_WRITE_NEW,
		ASSOCIATION,
		ACL_RECORD,
		USER,
		GROUP,
		GROUP_NOT_EMAIL_CONTRIBUTORS,
		ASSOC_TYPE,
		ENCRYPTION_INFO_ENCRYPT,
		ENCRYPTION_INFO_DECRYPT,
		RULE_TYPE,
		DATA_ARCHIVE,
		ARCHIVE_FORMAT,
		XPATH,
		XPATH_AGGREGATE,
		LUCENE,
		TENANT,
		CUSTOM_MODEL,
		BYTE_ARRAY,
		CATEGORY,
		CATEGORY_ROOT,
		CATEGORY_ASPECT,
		DEPTH,
		MODE,
		MASSIVE_CREATE_CONTENT,
		IS_ZERO,
		NOT_ZERO,
		MASSIVE_UPDATE_METADATA,
		MASSIVE_RETRIEVE,
		MASSIVE_RETRIEVE_SIZE,
		MASSIVE_GET_METADATA,
		MASSIVE_DELETE_CONTENT
	}

	/**
	 * Esegue la validazione del parametro specificato.
	 *
	 * <p><strong>NB:</strong> questo metodo &egrave; pensato per essere utilizzato <strong>fuori</strong> da una
	 * transazione. <i>Nel caso venisse utilizzato dentro una transazione il chiamante deve gestire la condizione di errore
	 * prevedendo il rollback della transazione stessa.</i></p>
	 *
	 * @see ValidationType
	 *
	 * @param type Il tipo di validazione da eseguire.
	 * @param paramName Il nome del parametro da validare.
	 * @param paramValue Il valore del parametro da validare.
	 *
	 * @throws InvalidParameterException Se la validazione fallisce.
	 */
	protected void validate(ValidationType type, String paramName, Object paramValue) throws InvalidParameterException {
		logger.debug("[EcmEnginePublisherBean::validate] BEGIN");

		try {
			if (paramValue == null) {
				final String errorMessage = "Parameter validation failed for type '"
						+ type.toString() + "' -- null parameter: " + paramName;

				logger.warn("[EcmEnginePublisherBean::validate] "
						+ errorMessage);
				throw new InvalidParameterException(errorMessage);
			}
			boolean isValid = false;
			String objDescription = null;

			switch (type) {
			case NOT_NULL:
				isValid = true;	// Se l'esecuzione arriva a questo punto l'oggetto non puo` essere null.
				break;
			case NODE:
				isValid = isValidNode((Node) paramValue);
				objDescription = "U: " + ((Node) paramValue).getUid();
				break;
			case OPERATION_CONTEXT:
				isValid = isValidOperationContext((OperationContext) paramValue);
				objDescription = "U: "
						+ ((OperationContext) paramValue).getUsername() + " - F: "
						+ ((OperationContext) paramValue).getFruitore();
				break;
			case SEARCH_PARAMETER:
				isValid = isValidSearchParameter((AuditInfo) paramValue);
				objDescription = "U: " + ((AuditInfo) paramValue).getUtente()
						+ " - OP: " + ((AuditInfo) paramValue).getOperazione()
						+ " - ID: " + ((AuditInfo) paramValue).getIdOggetto();
				break;
			case AUDIT_INFO:
				isValid = isValidAuditInfo((AuditInfo) paramValue);
				objDescription = "U: " + ((AuditInfo) paramValue).getUtente()
						+ " - OP: " + ((AuditInfo) paramValue).getOperazione()
						+ " - ID: " + ((AuditInfo) paramValue).getIdOggetto();
				break;
			case PREFIXED_NAME:
				isValid = isValidPrefixedName((String) paramValue);
				objDescription = "PN: " + (String) paramValue;
				break;
			case PREFIX:
				isValid = isValidPrefix((String) paramValue);
				objDescription = "P: " + (String) paramValue;
				break;
			case NAME:
				isValid = isValidName((String) paramValue);
				objDescription = "N: " + (String) paramValue;
				break;
			case NOT_GUEST:
				isValid = isValidNotGuest((String) paramValue);
				objDescription = "N: " + (String) paramValue;
				break;
			case PASSWORD:
				isValid = isValidPassword((char[]) paramValue);
				objDescription = "P: " + new String((char[]) paramValue);
				break;
			case CONTENT_ITEM:
				isValid = isValidContentItem((ContentItem) paramValue);
				objDescription = "PN: "
						+ ((ContentItem) paramValue).getPrefixedName();
				break;
			case ASPECT:
				isValid = isValidAspect((Aspect) paramValue);
				objDescription = "PN: " + ((Aspect) paramValue).getPrefixedName();
				break;
			case PROPERTY:
				isValid = isValidProperty((Property) paramValue);
				objDescription = "PN: " + ((Property) paramValue).getPrefixedName()
						+ " - T: " + ((Property) paramValue).getDataType();
				break;
			case CONTENT_READ:
				isValid = isValidContent((Content) paramValue, ValidationType.CONTENT_READ);
				objDescription = "PN: " + ((Content) paramValue).getPrefixedName()
						+ " - CPPN: " + ((Content) paramValue).getContentPropertyPrefixedName();
				break;
			case CONTENT_WRITE_NEW:
				isValid = isValidContent((Content) paramValue, ValidationType.CONTENT_WRITE_NEW);
				objDescription = "PN: " + ((Content) paramValue).getPrefixedName()
						+ " - PAT: " + ((Content) paramValue).getParentAssocTypePrefixedName()
						+ " - T: " + ((Content) paramValue).getTypePrefixedName()
						+ " - CPPN: " + ((Content) paramValue).getContentPropertyPrefixedName();
				break;
			case CONTENT_WRITE_METADATA:
				isValid = isValidContent((Content) paramValue, ValidationType.CONTENT_WRITE_METADATA);
				objDescription = "CPPN: " + ((Content) paramValue).getContentPropertyPrefixedName()
						+ " - A: " + (((Content) paramValue).getAspects() != null ? ((Content) paramValue).getAspects().length : "null");
				break;
			case CONTENT_WRITE_CONTENT:
				isValid = isValidContent((Content) paramValue, ValidationType.CONTENT_WRITE_CONTENT);
				objDescription = "CPPN: " + ((Content) paramValue).getContentPropertyPrefixedName()
						+ " - MT: " + ((Content) paramValue).getMimeType()
						+ " - ENC: " + ((Content) paramValue).getEncoding();
				break;
			case ASSOCIATION:
				isValid = isValidAssociation((Association) paramValue);
				objDescription = "PN: "
						+ ((Association) paramValue).getPrefixedName() + " - T: "
						+ ((Association) paramValue).getTypePrefixedName();
				break;
			case ACL_RECORD:
				isValid = isValidAclRecord((AclRecord) paramValue);
				objDescription = "A: " + ((AclRecord) paramValue).getAuthority()
						+ " - P: " + ((AclRecord) paramValue).getPermission();
				break;
			case USER:
				isValid = isValidUser((User) paramValue);
                if( paramValue==null ){
   				   objDescription  = "U: null";
                } else {
                   String cUserName = ((User) paramValue).getUsername();
                   objDescription  = "U: " + cUserName;
                   objDescription += " N: " + ((User) paramValue).getName();
                   objDescription += " S: " + ((User) paramValue).getSurname();
                   if( cUserName!=null ){
                      objDescription += " U: LEN " + cUserName.length() + " MAXLEN " +(USERNAME_MAX_LENGTH-1);
                   }
                }
				break;
			case GROUP:
				isValid = isValidGroup((Group) paramValue);
				objDescription = "G: " + ((Group) paramValue).getName();
				break;
			case GROUP_NOT_EMAIL_CONTRIBUTORS:
				isValid = isValidGroupNotEmailContributors((Group) paramValue);
				objDescription = "G: " + ((Group) paramValue).getName();
				break;
			case ASSOC_TYPE:
				isValid = isValidAssocType((String) paramValue);
				objDescription = "A: " + ((String) paramValue);
				break;
			case ENCRYPTION_INFO_ENCRYPT:
				isValid = isValidEncryptionInfo((EncryptionInfo) paramValue, false);
				objDescription = "A: "
						+ ((EncryptionInfo) paramValue).getAlgorithm() + " - KID: "
						+ ((EncryptionInfo) paramValue).getKeyId();
				break;
			case ENCRYPTION_INFO_DECRYPT:
				isValid = isValidEncryptionInfo((EncryptionInfo) paramValue, true);
				objDescription = "A: "
						+ ((EncryptionInfo) paramValue).getAlgorithm() + " - K: "
						+ ((EncryptionInfo) paramValue).getKey();
				break;
			case RULE_TYPE:
				isValid = isValidRuleType((String) paramValue);
				objDescription = "RT: " + (String) paramValue;
				break;
			case DATA_ARCHIVE:
				isValid = isValidDataArchive((DataArchive) paramValue);
				objDescription = "F: "
						+ ((DataArchive) paramValue).getFormat()
						+ " - CPN: "
						+ ((DataArchive) paramValue)
								.getMappedContainerNamePropertyPrefixedName()
						+ " - CT: "
						+ ((DataArchive) paramValue)
								.getMappedContainerTypePrefixedName();
				break;
			case ARCHIVE_FORMAT:
				isValid = isValidArchiveFormat((String) paramValue);
				objDescription = "F: " + (String) paramValue;
				break;
			case XPATH:
				isValid = isValidXpath((SearchParams) paramValue);
				objDescription = "XQ: " + ((SearchParams) paramValue).getXPathQuery();
				break;
			case XPATH_AGGREGATE:
				isValid = isValidXpathAggregate((SearchParamsAggregate) paramValue);
				objDescription = "XQ: " + ((SearchParamsAggregate) paramValue).getXPathQuery();
				break;
			case LUCENE:
				isValid = isValidLucene((SearchParams) paramValue);
				objDescription = "LQ: " + ((SearchParams) paramValue).getLuceneQuery();
				break;
			case TENANT:
				isValid = isValidTenant((Tenant) paramValue);
				objDescription = "T: " + ((Tenant) paramValue);
				break;
			case CUSTOM_MODEL:
				isValid = isValidCustomModel((CustomModel) paramValue);
				objDescription = "CM: " + ((CustomModel) paramValue);
				break;
			case BYTE_ARRAY:
				isValid = isValidByteArray((byte[]) paramValue);
				objDescription = "BA: " + ((byte[]) paramValue);
				break;
			case CATEGORY:
				isValid = isValidCategory((Category) paramValue);
				objDescription = "CATEGORY: " + ((Category) paramValue);
				break;
			case CATEGORY_ROOT:
				isValid = isValidRootCategory((Category) paramValue);
				objDescription = "CATEGORY ROOT: " + ((Category) paramValue);
				break;
			case DEPTH:
				isValid = isValidCategoryDepth((String) paramValue);
				objDescription = "DEPTH CATEGORY : " + ((String) paramValue);
				break;
			case MODE:
				isValid = isValidCategoryMode((String) paramValue);
				objDescription = "MODE CATEGORY : " + ((String) paramValue);
				break;
			case CATEGORY_ASPECT:
				isValid = isValidCategoryAspect((Category) paramValue);
				objDescription = "CATEGORY ASPECT: " + ((Category) paramValue);
				break;
			case IS_ZERO:
				isValid=isValidZero((Long)paramValue);
				break;
			case NOT_ZERO:
				isValid=isValidNotZero((Long)paramValue);
				break;
			case MASSIVE_CREATE_CONTENT:
				isValid = isValidMassiveNumberOfContents((Content[])paramValue)&&isValidMassiveSizeOfContents((Content[])paramValue);
				objDescription = "CONTENTS MAX NUMBER: "+MASSIVE_MAX_CREATE_NUMBER+"\nCONTENTS MAX SIZE: "+MASSIVE_MAX_CREATE_SIZE;
				break;
			case MASSIVE_UPDATE_METADATA:
				isValid = isValidMassiveNumberOfContents((Content[])paramValue);
				objDescription = "CONTENTS MAX NUMBER: "+MASSIVE_MAX_CREATE_NUMBER;
				break;
			case MASSIVE_RETRIEVE:
				isValid = isValidMassiveNumberOfRetrieve((Node[])paramValue);
				objDescription = "NODES MAX NUMBER: "+MASSIVE_MAX_RETRIEVE_NUMBER;
				break;
			case MASSIVE_RETRIEVE_SIZE:
				isValid = isValidMassiveNumberOfRetrieveSize((Long)paramValue);
				objDescription = "RETRIEVE MAX SIZE: "+MASSIVE_MAX_RETRIEVE_SIZE;
				break;
			case MASSIVE_GET_METADATA:
				isValid = isValidMassiveNumberOfGetMetadata((Long)paramValue);
				objDescription = "GET MAX SIZE: "+MASSIVE_MAX_GET_METADATA_SIZE;
				break;
			case MASSIVE_DELETE_CONTENT:
				isValid = isValidMassiveNumberOfDelete((Node[])paramValue);
				objDescription = "NODES MAX NUMBER: "+MASSIVE_MAX_DELETE_NUMBER+"\nCONTENTS MAX SIZE: "+MASSIVE_MAX_CREATE_SIZE;
				break;
			}


			if (!isValid) {
				String errorMessage = "Parameter validation failed for type '"
						+ type.toString() + "' -- invalid parameter: "
						+ paramName + " [" + objDescription + "]";

				logger.warn("[EcmEnginePublisherBean::validate] "
						+ errorMessage);
				throw new InvalidParameterException(errorMessage);
			}
		} finally {
			logger.debug("[EcmEnginePublisherBean::validate] END");
		}
	}

	/**
	 * Controlla se un'operazione fra i repository specificati &egrave; <i>cross-repository</i>.
	 *
	 * <p>Un'operazione &egrave; considerata <i>cross-repository</i> se coinvolge un nodo
	 * sorgente e un nodo destinazione che appartengono a due repository diversi.</p>
	 *
	 * @param repositoryA Il primo repository coinvolto nell'operazione.
	 * @param repositoryB Il secondo repository coinvolto nell'operazione.
	 *
	 * @return {@code true} se i repository specificati sono diversi, {@code false}
	 * se sono uguali o nessuno dei due &egrave; definito.
	 */
	protected static boolean isCrossRepository(String repositoryA, String repositoryB) {
		final boolean aSpecified = isValidName(repositoryA);
		final boolean bSpecified = isValidName(repositoryB);

		if (!aSpecified && !bSpecified) {
			return false;
		}
		if ((aSpecified && bSpecified)
				&& repositoryA.equals(repositoryB)) {
			return false;
		}
		return true;
	}

	/**
	 * Controlla se una generica istanza di {@link AclRecord} &egrave; valida.
	 *
	 * <p>Un {@link AclRecord} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>la sua {@code authority} deve essere un nome con prefisso valido
	 *  secondo quanto definito in {@link #isValidPrefixedName(String)};</li>
	 *  <li>la sua {@code permission} deve essere un nome con prefisso valido
	 *  secondo quanto definito in {@link #isValidPrefixedName(String)}.</li>
	 * </ul>
	 *
	 * @param acl Il {@link AclRecord} da controllare.
	 *
	 * @return {@code true} se &egrave; una {@link AclRecord} valida,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidAclRecord(AclRecord acl) {
		return (acl != null && acl.getAuthority()!=null && acl.getAuthority().length()<101)
		? isValidName(acl.getAuthority())
				&& isValidName(acl.getPermission())
				: false;
	}

	/**
	 * Controlla se la stringa specificata &egrave; un formato di archivio valido.
	 *
	 * <p>Il formato deve assumere uno dei seguienti valori:</p>
	 * <ul>
	 *  <li>{@code EcmEngineConstants.ECMENGINE_ARCHIVE_FORMAT_TAR}
	 *  <li>{@code EcmEngineConstants.ECMENGINE_ARCHIVE_FORMAT_TAR_GZ}
	 *  <li>{@code EcmEngineConstants.ECMENGINE_ARCHIVE_FORMAT_ZIP}
	 * </ul>
	 *
	 * @param format Il formato dell'archivio.
	 *
	 * @return {@code true} se &egrave; un formato di archivio valido,
	 * {@code false} altrimenti.
	 *
	 * @see EcmEngineConstants
	 */
	protected static boolean isValidArchiveFormat(String format) {
		return (EcmEngineConstants.ECMENGINE_ARCHIVE_FORMAT_TAR.equals(format)
				|| EcmEngineConstants.ECMENGINE_ARCHIVE_FORMAT_TAR_GZ.equals(format)
				|| EcmEngineConstants.ECMENGINE_ARCHIVE_FORMAT_ZIP.equals(format));
	}

	/**
	 * Controlla se una generica istanza di {@link Aspect} &egrave; valida.
	 *
	 * <p>Un {@link Aspect} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>deve essere un {@link ContentItem} valido secondo quanto definito
	 *  in {@link #isValidContentItem(ContentItem)}.</li>
	 * </ul>
	 *
	 * @param aspect L'{@link Aspect} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link Aspect} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidAspect(Aspect aspect) {
		return (aspect != null)
		? isValidContentItem(aspect)
				: false;
	}

	/**
	 * Controlla se una generica istanza di {@link Association} &egrave; valida.
	 *
	 * <p>Una {@link Association} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code prefixedName}, solo se l'associazione &egrave; di tipo padre-figlio,
	 *  deve essere un nome con prefisso valido secondo quanto definito
	 *  in {@link #isValidPrefixedName(String)};</li>
	 *  <li>il suo {@code typePrefixedName} deve essere un nome con prefisso valido
	 *  secondo quanto definito in {@link #isValidPrefixedName(String)};</li>
	 * </ul>
	 *
	 * @param association La {@link Association} da controllare.
	 *
	 * @return {@code true} se &egrave; una {@link Association} valida,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidAssociation(Association association) {
		if (association != null){
			if (association.isChildAssociation()) {
				if (isValidPrefixedName(association.getPrefixedName()) &&
						isValidPrefixedName(association.getTypePrefixedName()))
					return true;
			} else {
				if (isValidPrefixedName(association.getTypePrefixedName()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Controlla se il tipo delle associazioni da ricercare &egrave; valido.
	 *
	 * <p>Il tipo di un'associazione pu&ograve; essere:</p>
	 * <ul>
	 *  <li>PARENT</li>
	 *  <li>CHILD</li>
	 *  <li>TARGET</li>
	 *  <li>SOURCE</li>
	 * </ul>
	 *
	 * @param assocType il tipo di associazione da controllare.
	 *
	 * @return {@code true} se &egrave; un tipo valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidAssocType(String assocType) {
		if (assocType != null) {
			if (assocType.equals(ECMENGINE_ASSOC_TYPE_PARENT) ||
					(assocType.equals(ECMENGINE_ASSOC_TYPE_CHILD)) ||
					(assocType.equals(ECMENGINE_ASSOC_TYPE_SOURCE)) ||
					(assocType.equals(ECMENGINE_ASSOC_TYPE_TARGET))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Controlla se la profondita di ricerca delle categorie &egrave; valida.
	 *
	 * <p>La profondita di ricerca delle categorie pu&ograve; essere:</p>
	 * <ul>
	 *  <li>IMMEDIATE</li>
	 *  <li>ANY</li>
	 * </ul>
	 *
	 * @param depth il depth di ricerca categorie da controllare.
	 *
	 * @return {@code true} se &egrave; un depth valido,
	 * {@code false} altrimenti.
	 */

	protected static boolean isValidCategoryDepth(String depth) {
		if (depth != null) {
			if (depth.equals(ECMENGINE_DEPTH_IMMEDIATE) ||
					depth.equals(ECMENGINE_DEPTH_ANY)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Controlla se il tipo di elemento da ricercare nelle categorie &egrave; valido.
	 *
	 * <p>Il tipo di elemento da ricercare nelle categorie pu&ograve; essere:</p>
	 * <ul>
	 *  <li>MEMBERS</li>
	 *  <li>SUB_CATEGORIES</li>
	 *  <li>ALL</li>
	 * </ul>
	 *
	 * @param mode il tipo di elemento da ricercare nelle categorie da controllare.
	 *
	 * @return {@code true} se &egrave; un mode valido,
	 * {@code false} altrimenti.
	 */


	protected static boolean isValidCategoryMode(String mode) {
		if (mode != null) {
			if (mode.equals(ECMENGINE_MODE_ALL) ||
					mode.equals(ECMENGINE_MODE_MEMBERS) ||
						mode.equals(ECMENGINE_MODE_SUB_CATEGORY)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Controlla se una generica istanza di {@link AuditInfo} &egrave; valida per
	 * l'inserimento nella tavola di audit trail.
	 *
	 * <p>Uno dei seguenti attributi deve essere valorizzato affinch&egrave; possa
	 * essere utilizzato per l'inserimento:</p>
	 * <ul>
	 *  <li>utente</li>
	 *  <li>operazione</li>
	 *  <li>idOggetto</li>
	 * </ul>
	 *
	 * @param auditTrail Il {@link AuditInfo} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link AuditInfo} valido,{@code false} altrimenti.
	 */
	protected static boolean isValidAuditInfo(AuditInfo auditTrail){
		if (auditTrail!=null){
			if (auditTrail.getUtente() != null
					|| auditTrail.getOperazione() != null
					|| auditTrail.getIdOggetto() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Controlla se una generica istanza di {@link Content} &egrave; valida.
	 *
	 * <p>Un {@link Content} deve rispettare le seguenti condizioni quando viene usato
	 * in operazioni di scrittura di un nuovo contenuto:</p>
	 * <ul>
	 *  <li>deve essere un {@link ContentItem} valido secondo quanto definito
	 *  in {@link #isValidContentItem(ContentItem)};</li>
	 *  <li>il suo {@code parentAssocTypePrefixedName} deve essere un nome con prefisso valido
	 *  secondo quanto definito in {@link #isValidPrefixedName(String)};</li>
	 *  <li>il suo {@code typePrefixedName} deve essere un nome con prefisso valido
	 *  secondo quanto definito in {@link #isValidPrefixedName(String)};</li>
	 *  <li>la sua {@code contentPropertyPrefixedName}, se &egrave; settata, deve essere un nome con
	 *  prefisso valido secondo quanto definito in {@link #isValidPrefixedName(String)};</li>
	 *  <li>se &egrave; settata una {@code contentPropertyPrefixedName} valida devono essere specificati anche
	 *  MIME-Type, encoding e contenuto non nulli.</li>
	 * </ul>
	 *
	 * <p>Un {@link Content} deve rispettare le seguenti condizioni quando viene usato
	 * in operazioni di aggiornamento di un contenuto:</p>
	 * <ul>
	 *  <li>la sua {@code contentPropertyPrefixedName}, se &egrave; settata, deve essere un nome con
	 *  prefisso valido secondo quanto definito in {@link #isValidPrefixedName(String)};</li>
	 *  <li>se &egrave; settata una {@code contentPropertyPrefixedName} valida devono essere specificati anche
	 *  MIME-Type, encoding e contenuto non nulli.</li>
	 * </ul>
	 *
	 * <p>Un {@link Content} deve rispettare le seguenti condizioni quando viene usato
	 * in operazioni di aggiornamento dei metadadi di un contenuto:</p>
	 * <ul>
	 *  <li>la sua {@code contentPropertyPrefixedName}, il MIME-Type, l'encoding e il content non devono essere
	 *  settati;</li>
	 *  <li>l'array di {@link Aspect} deve essere settato (tipicamente conterr&agrave; almeno i due aspect
	 *  {@code sys:referenceable} e {@code cm:auditable}).</li>
	 * </ul>
	 *
	 * <p>Un {@link Content} deve rispettare le seguenti condizioni quando viene usato
	 * in operazioni di lettura:</p>
	 * <ul>
	 *  <li>la sua {@code contentPropertyPrefixedName}, se &egrave; settata, deve essere un nome con
	 *  prefisso valido secondo quanto definito in {@link #isValidPrefixedName(String)}.</li>
	 * </ul>
	 *
	 * @param content Il {@link Content} da controllare.
	 * @param type Il {@link ValidationType} da utilizzare. Sono validi solo i tipi {@code CONTENT_READ}
	 * e {@code CONTENT_WRITE_*}.
	 *
	 * @return {@code true} se &egrave; un {@link Content} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidContent(Content content, ValidationType type) {

		final boolean contentPropertyValidForWrite = (content.getContentPropertyPrefixedName() != null)
				? content.getMimeType() != null && content.getContent() != null && content.getEncoding() != null
				: content.getMimeType() == null && content.getContent() == null && content.getEncoding() == null;

		switch (type) {

		case CONTENT_READ:
			return isValidPrefixedName(content.getContentPropertyPrefixedName());
		case CONTENT_WRITE_NEW:
			return isValidContentItem(content)
				&& contentPropertyValidForWrite
				&& isValidPrefixedName(content.getTypePrefixedName())
				&& isValidPrefixedName(content.getParentAssocTypePrefixedName());
		case CONTENT_WRITE_CONTENT:
			return contentPropertyValidForWrite;
		case CONTENT_WRITE_METADATA:
			/*
			 * 21-05-2008
			 *
			 * La validazione approfondita e` stata rimossa per ripristinare la compatibilita`
			 * con l'applicativo CRECEDO. Vengono validati solo i parametri effettivamente
			 * richiesti dalle operazioni di scrittura metadati.
			 */
//			return content.getContentPropertyPrefixedName() == null
//				&& content.getMimeType() == null
//				&& content.getEncoding() == null
//				&& content.getContent() == null
//				&& content.getAspects() != null;
			return content.getAspects() != null;
		default:
			// Tipo di validazione errato
			return false;
		}
	}
	/**
	 * Controlla se una generica istanza di {@link ContentItem} &egrave; valida.
	 *
	 * <p>Un {@link ContentItem} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code prefixedName} deve essere un nome con prefisso valido
	 *  secondo quanto definito in {@link #isValidPrefixedName(String)}.</li>
	 * </ul>
	 *
	 * @param item Il {@link ContentItem} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link ContentItem} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidContentItem(ContentItem item) {
		return (item != null)
		? isValidPrefixedName(item.getPrefixedName())
				: false;
	}
	/**
	 * Controlla se il DTO {@link DataArchive} specificato &egrave; valido per operazioni
	 * di importazione di contenuti.
	 *
	 * <p>Un {@link DataArchive} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>deve essere diverso da {@code null};</li>
	 *  <li>il suo campo {@code format} deve essere una stringa valida secondo quanto
	 *  definito in {@link #isValidName(String)};</li>
	 *  <li>il suo campo {@code content} deve essere una array di byte diverso da null
	 *  e di lunghezza maggiore di zero.</li>
	 * </ul>
	 *
	 * @param dataArchive L'{@link EncryptionInfo} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link DataArchive} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidDataArchive(DataArchive dataArchive) {
		return (dataArchive != null)
		? isValidName(dataArchive.getFormat())
				&& dataArchive.getContent() != null
				&& dataArchive.getContent().length > 0
				: false;
	}
	/**
	 * Controlla se il DTO {@link EncryptionInfo} specificato &egrave; valido per operazioni
	 * di criptazione o decriptazione.
	 *
	 * <p>Un {@link EncryptionInfo} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>deve essere diverso da {@code null};</li>
	 *  <li>il suo campo {@code key} deve essere una stringa valida secondo quanto
	 *  definito in {@link #isValidName(String)}, <i>a meno che il contenuto non sia criptato alla fonte
	 *  e l'operazione sia una criptazione</i>;</li>
	 *  <li>se il contenuto &ergave; criptato alla fonte l'IV sorgente deve essere specificato
	 *  (<i>solo nel caso di operazioni di criptazione</i>);</li>
	 *  <li>il suo campo {@code algorithm} deve essere una stringa valida secondo quanto
	 *  definito in {@link #isValidName(String)};</li>
	 *  <li>i campi {@code mode} e {@code padding} devono essere entrambi specificati oppure nessuno dei due deve essere
	 *  specificato;</li>
	 *  <li>il suo campo {@code sourceIv} deve essere valorizzato con una stringa BASE64 se viene caricato
	 *  un contenuto gi&agrave; criptato con modalit&agrave; di criptazione non specificata oppure diversa da ECB;</li>
	 *  <li>il suo campo {@code keyId} deve essere una stringa valida secondo quanto
	 *  definito in {@link #isValidName(String)} (<i>solo nel caso di operazioni di criptazione</i>).</li>
	 * </ul>
	 *
	 * @param encInfo L'{@link EncryptionInfo} da controllare.
	 * @param forDecrypt {@code true} se la verifica deve essere effettuata per un'operazione di decriptazione.
	 *
	 * @return {@code true} se &egrave; un {@link EncryptionInfo} valido, {@code false} altrimenti.
	 */
	protected static boolean isValidEncryptionInfo(EncryptionInfo encInfo, boolean forDecrypt) {

		// Decrypting
		if (forDecrypt) {
			return (encInfo != null
					&& isValidName(encInfo.getKey())
					&& isValidName(encInfo.getAlgorithm()));
		}

		// Encrypting

		/*
		 * L'IV e` richiesto solo se viene inviato un contenuto gia` criptato e il mode e`
		 * specificato ed e` diverso da ECB.
		 */
		final boolean needsIv = encInfo != null
				&& encInfo.isSourceEncrypted()
				&& (encInfo.getMode() != null && !encInfo.getMode().equalsIgnoreCase("ECB"));

		/*
		 * La chiave serve quando non viene caricato un contenuto gia` criptato alla fonte.
		 */
		final boolean needsKey = !(encInfo != null && encInfo.isSourceEncrypted());

		/*
		 * Mode e padding devono essere entrambi specificati e validi (etrambi non nulli e non stringhe vuote)
		 * oppure nessuno dei due deve essere specificato (entrambi null).
		 */
		final boolean bothOrNoneModeAndPadding = encInfo != null
				&& (isValidName(encInfo.getMode()) && isValidName(encInfo.getPadding())
						|| (encInfo.getMode() == null && encInfo.getPadding() == null));

		/*
		 * Algoritmo e keyId sono sempre necessari, gli altri casi dipendono da quanto valutato sopra.
		 */
		return (encInfo != null
				&& (!needsKey
						|| (needsKey && isValidName(encInfo.getKey())))
				&& (!needsIv
						|| (needsIv
								&& encInfo.getSourceIV() != null
								&& Base64.isArrayByteBase64(encInfo.getSourceIV().getBytes())))
				&& isValidName(encInfo.getAlgorithm())
				&& bothOrNoneModeAndPadding
				&& isValidName(encInfo.getKeyId()));
	}

	/**
	 * Controlla se una generica istanza di {@link Group} &egrave; valida.
	 *
	 * <p>Un {@link Group} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code name} deve essere un nome valido secondo quanto
	 *  definito in {@link #isValidName(String)}.</li>
	 * </ul>
	 *
	 * @param group Il {@link Group} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link Group} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidGroup(Group group) {
		if(group==null || group.getName()==null || group.getName().contains("?") || group.getName().contains("$")||group.getName().length()>200){
			return false;
		}
		else{
			return isValidName(group.getName());
		}
		/*return (group != null)
		? isValidName(group.getName())
				: false;*/
	}


	protected static boolean isValidGroupNotEmailContributors(Group group) {
		if(group==null || group.getName()==null || group.getName().equalsIgnoreCase("EMAIL_CONTRIBUTORS")){
			return false;
		}
		else{
			return isValidName(group.getName());
		}
		/*return (group != null)
		? isValidName(group.getName())
				: false;*/
	}

	/**
	 * Controlla se il nome specificato in input &egrave; un nome valido.
	 *
	 * <p>Un nome deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>non pu&ograve; essere {@code null};</li>
	 *  <li>deve essere lungo almeno un carattere.</li>
	 * </ul>
	 *
	 * @param name Il nome da controllare.
	 *
	 * @return {@code true} se &egrave; un nome valido, {@code false} altrimenti.
	 */
	protected static boolean isValidName(String name) {
		return (name != null)
		? (name.length() > 0)
				: false;
	}

	protected static boolean isValidNotGuest(String name) {
		if(name==null || name.length()==0 || name.equalsIgnoreCase("guest")){
			return false;
		}
		else{
			return isValidName(name);
		}
	}

	/**
	 * Controlla se il nodo specificato &egrave; valido.
	 *
	 * <p>Un nodo valido deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>l'Uid associato al nodo deve rispettare la regex definita in
	 *  {@link #NODE_CHECK_REGEX}.</li>
	 * </ul>
	 *
	 * @param node Il nodo da controllare.
	 *
	 * @return {@code true} se &egrave; un nodo valido, {@code false} altrimenti.
	 */
	protected static boolean isValidNode(Node node) {
		if (node == null
				|| node.getUid() == null
				|| !node.getUid().matches(NODE_CHECK_REGEX)) {
			return false;
		}
		return true;
	}

	/**
	 * Controlla se una generica istanza di {@link OperationContext} &egrave; valida.
	 *
	 * <p>Un {@link OperationContext} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code username} deve essere un nome valido secondo quanto definito
	 *  in {@link #isValidName(String)};</li>
	 *  <li>la sua {@code password} deve essere una password valida secondo quanto definito
	 *  in {@link #isValidPassword(char[])};</li>
	 *  <li>il suo {@code fruitore} deve essere un nome valido secondo quanto definito
	 *  in {@link #isValidName(String)}.</li>
	 * </ul>
	 *
	 * @param ctx Il {@link OperationContext} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link OperationContext} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidOperationContext(OperationContext ctx) {
		return (ctx != null)
		? isValidName(ctx.getUsername())
				&& isValidName(ctx.getFruitore())
				&& isValidPassword(ctx.getPassword().toCharArray())
				: false;
	}

	/**
	 * Controlla se la password specificata in input &egrave; valida.
	 *
	 * <p>Una password deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>non pu&ograve; essere {@code null};</li>
	 *  <li>deve essere lunga almeno un carattere;</li>
	 *  <li>il primo carattere deve essere diverso da '0'.</li>
	 * </ul>
	 *
	 * @param password La password da controllare.
	 *
	 * @return {@code true} se &egrave; una password valida, {@code false} altrimenti.
	 */
	protected static boolean isValidPassword(char [] password) {
		return (password != null)
		? (password.length > 0)
				&& (password[0] != 0)
				: false;
	}

	/**
	 * Controlla se il prefisso specificato in input &egrave; un prefisso valido.
	 *
	 * <p>Un prefisso valido deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>non pu&ograve; essere {@code null};</li>
	 *  <li>deve essere lungo almeno un carattere;</li>
	 *  <li>non pu&ograve; contenere il separatore di prefisso definito in
	 *   {@link ContentItem#PREFIXED_NAME_SEPARATOR}.</li>
	 * </ul>
	 *
	 * @param prefix Il prefisso da controllare
	 *
	 * @return {@code true} se &egrave; un prefisso valido, {@code false} altrimenti.
	 */
	protected static boolean isValidPrefix(String prefix) {
		return (prefix != null)
		? (prefix.length() > 0)
				&& (prefix.indexOf(ContentItem.PREFIXED_NAME_SEPARATOR) < 0)
				: false;
	}

	/**
	 * Controlla se il nome completo di prefisso specificato in input &egrave; veramente
	 * un nome completo valido.
	 *
	 * <p>Un nome completo valido deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>deve contenere il separatore di prefisso definito in
	 *   {@link ContentItem#PREFIXED_NAME_SEPARATOR};</li>
	 *  <li>la parte precedente al separatore (prefisso) deve essere lunga almeno un carattere;</li>
	 *  <li>la parte successiva al separatore (nome) deve essere lunga almeno un carattere.</li>
	 * </ul>
	 *
	 * @param prefixedName Il nome da controllare.
	 *
	 * @return {@code true} se &egrave; un nome completo valido, {@code false} altrimenti.
	 */
	protected static boolean isValidPrefixedName(String prefixedName) {
		return (prefixedName != null)
		? (prefixedName.length() >= 3)
				&& (prefixedName.indexOf(ContentItem.PREFIXED_NAME_SEPARATOR) > 0)
				&& (prefixedName.indexOf(ContentItem.PREFIXED_NAME_SEPARATOR) < (prefixedName.length() - 1))
				: false;
	}

	/**
	 * Controlla se una generica istanza di {@link Property} &egrave; valida.
	 *
	 * <p>Una {@link Property} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>deve essere un {@link ContentItem} valido secondo quanto definito
	 *  in {@link #isValidContentItem(ContentItem)};</li>
	 *  <li>il suo {@code dataType} deve essere un nome valido secondo quanto definito
	 *  in {@link #isValidName(String)};</li>
	 * </ul>
	 *
	 * @param property La {@link Property} da controllare.
	 *
	 * @return {@code true} se &egrave; una {@link Property} valida,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidProperty(Property property) {
		return (property != null)
		? isValidContentItem(property)
				&& isValidName(property.getDataType())
				: false;
	}

	/**
	 * Controlla se la stringa specificata &egrave; un rule type valido.
	 *
	 * <p>Un rule type deve assumere uno dei seguienti valori:</p>
	 * <ul>
	 *  <li>{@code EcmEngineConstants.ECMENGINE_RULE_TYPE_INBOUND}
	 *  <li>{@code EcmEngineConstants.ECMENGINE_RULE_TYPE_OUTBOUND}
	 *  <li>{@code EcmEngineConstants.ECMENGINE_RULE_TYPE_UPDATE}
	 * </ul>
	 *
	 * @param ruleType Il rule type da verificare.
	 *
	 * @return {@code true} se &egrave; un rule type valido,
	 * {@code false} altrimenti.
	 *
	 * @see EcmEngineConstants
	 */
	protected static boolean isValidRuleType(String ruleType) {
		return (EcmEngineConstants.ECMENGINE_RULE_TYPE_INBOUND.equals(ruleType)
				|| EcmEngineConstants.ECMENGINE_RULE_TYPE_OUTBOUND.equals(ruleType)
				|| EcmEngineConstants.ECMENGINE_RULE_TYPE_UPDATE.equals(ruleType));
	}

	/**
	 * Controlla se una generica istanza di {@link AuditInfo} &egrave; valida come filtro
	 * per la ricerca nella tavola di audit trail.
	 *
	 * <p>Almeno uno dei seguenti attributi deve essere valorizzato affinch&egrave; possa
	 * essere utilizzato come filtro di ricerca:</p>
	 * <ul>
	 *  <li>utente</li>
	 *  <li>operazione</li>
	 *  <li>idOggetto</li>
	 * </ul>
	 *
	 * @param parametriRicerca Il {@link AuditInfo} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link AuditInfo} valido, {@code false} altrimenti.
	 */
	protected static boolean isValidSearchParameter(AuditInfo parametriRicerca){
		if (parametriRicerca != null) {
			if (parametriRicerca.getUtente() != null
					|| parametriRicerca.getOperazione() != null
					|| parametriRicerca.getIdOggetto() != null)
				return true;
		}
		return false;
	}

	/**
	 * Controlla se una generica istanza di {@link User} &egrave; valida.
	 *
	 * <p>Un {@link User} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code name} deve essere un nome valido secondo quanto
	 *  definito in {@link #isValidName(String)};</li>
	 *  <li>il suo {@code surname} deve essere un nome valido secondo quanto
	 *  definito in {@link #isValidName(String)};</li>
	 *  <li>il suo {@code username} deve essere un nome valido secondo quanto
	 *  definito in {@link #isValidName(String)}.</li>
	 * </ul>
	 *
	 * @param user Il {@link User} da controllare.
	 *
	 * @return {@code true} se &egrave; un {@link User} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidUser(User user) {
		return (user != null && user.getUsername()!=null && user.getUsername().length()<USERNAME_MAX_LENGTH)
		? isValidName(user.getUsername())
				&& isValidName(user.getName())
				&& isValidName(user.getSurname())
				: false;
	}

	/**
	 * Controlla se un'istanza di {@link SearchParams} &egrave; valida
	 * per una ricerca con chiave XPath.
	 *
	 * <p>Per essere valido un {@link SearchParams} deve rispettare le
	 * seguenti condizioni:</p>
	 * <ul>
	 *  <li>la sua {@code xpathQuery} deve essere diversa da {@code null}.</li>
	 * </ul>
	 *
	 * @param xpath L'oggetto {@code SearchParams} da validare.
	 *
	 * @return {@code true} se &egrave; un {@code SearchParams} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidXpath(SearchParams xpath) {
		return (xpath != null
				&& xpath.getXPathQuery() != null);
	}


	protected static boolean isValidXpathAggregate(SearchParamsAggregate xpath) {
		return (xpath != null
				&& xpath.getXPathQuery() != null);
	}


	/**
	 * Controlla se un'istanza di {@link SearchParams} &egrave; valida
	 * per una ricerca con chiave Lucene.
	 *
	 * <p>Per essere valido un {@link SearchParams} deve rispettare le
	 * seguenti condizioni:</p>
	 * <ul>
	 *  <li>la sua {@code luceneQuery} deve essere diversa da {@code null}.</li>
	 * </ul>
	 *
	 * @param lucene L'oggetto {@code SearchParams} da validare.
	 *
	 * @return {@code true} se &egrave; un {@code SearchParams} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidLucene(SearchParams lucene) {
		return (lucene != null
				&& lucene.getLuceneQuery() != null);
	}

	/**
	 * Controlla se un'istanza di {@link Tenant} &egrave; valida.
	 *
	 * <p>Per essere valido un {@link Tenant} deve rispettare le
	 * seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code domain} deve essere diversa da {@code null}
	 *  e da string avuota.</li>
	 * </ul>
	 *
	 * @param tenant L'oggetto {@code Tenant} da validare.
	 *
	 * @return {@code true} se &egrave; un {@code Tenant} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidTenant(Tenant tenant) {
		return (tenant != null
				&& tenant.getDomain().length()<30 && isValidName(tenant.getDomain()));
	}

	/**
	 * Controlla se un'istanza di {@link CustomModel} &egrave; valida.
	 *
	 * <p>Per essere valido un {@link CustomModel} deve rispettare le
	 * seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code domain} deve essere diversa da {@code null}
	 *  e da string avuota.</li>
	 * </ul>
	 *
	 * @param customModel L'oggetto {@code CustomModel} da validare.
	 *
	 * @return {@code true} se &egrave; un {@code CustomModel} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidCustomModel(CustomModel customModel) {
		return (customModel != null
				&& isValidName(customModel.getFilename()));
	}


	/**
	 * Controlla se una generica istanza di {@link Category} &egrave; valida.
	 *
	 * <p>Una {@link Category} deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code name} deve essere valorizzato</li>
	 * </ul>
	 *
	 * @param category La {@link Category} da controllare.
	 *
	 * @return {@code true} se &egrave; una {@link Category} valida,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidCategory(Category category) {
		return (category != null)
		? isValidName(category.getName()): false;
	}

	/**
	 * Controlla se una istanza di root {@link Category} &egrave; valida.
	 *
	 * <p>Una {@link Category} root deve rispettare le seguenti condizioni:</p>
	 * <ul>
	 *  <li>il suo {@code name} deve essere valorizzato</li>
	 *  <li>il suo {@code aspectPrefixedName} deve essere valorizzato correttamente</li>
	 * </ul>
	 *
	 * @param category La {@link Category} root da controllare.
	 *
	 * @return {@code true} se &egrave; una {@link Category} Root valida,
	 * {@code false} altrimenti.
	 */

	protected static boolean isValidRootCategory(Category category) {
		return (category != null)
		? isValidName(category.getName())
				&& isValidPrefixedName(category.getAspectPrefixedName()) : false;
	}

	/**
	 * Controlla se una generica istanza di {@link Category} utilizzata per le ricerche
	 *  &egrave; valida.
	 *
	 * <p>Una {@link Category} ai fini delle ricerche deve rispettare la seguente condizione:</p>
	 * <ul>
	 *  <li>il suo {@code aspectPrefixedName} deve essere valorizzato correttamente</li>
	 * </ul>
	 *
	 * @param category La {@link Category} da controllare.
	 *
	 * @return {@code true} se &egrave; una {@link Category} valida per le ricerche,
	 * {@code false} altrimenti.
	 */


	protected static boolean isValidCategoryAspect(Category category) {
		return (category != null)
		? isValidPrefixedName(category.getAspectPrefixedName()) : false;
	}


	/**
	 * Controlla se un'istanza di {@code byte[]} &egrave; valida.
	 *
	 * <p>Per essere valido un {@code byte[]} deve rispettare le
	 * seguenti condizioni:</p>
	 * <ul>
	 *  <li>deve essere diversa da {@code null}</li>
	 *  <li>deve avere length &gt; 0</li>
	 * </ul>
	 *
	 * @param byteArray L'oggetto {@code byte[]} da validare.
	 *
	 * @return {@code true} se &egrave; un {@code byte[]} valido,
	 * {@code false} altrimenti.
	 */
	protected static boolean isValidByteArray(byte[] byteArray) {
		return (byteArray != null
				&& byteArray.length > 0);
	}

	protected static boolean isValidMassiveNumberOfContents(Content[] contents){
		if(contents.length>MASSIVE_MAX_CREATE_NUMBER){
			return false;
		}
		else{
			return true;
		}
	}

	protected static boolean isValidMassiveNumberOfRetrieve(Node[] nodes){
		if(nodes.length>MASSIVE_MAX_RETRIEVE_NUMBER){
			return false;
		}
		else{
			return true;
		}
	}

	protected static boolean isValidMassiveNumberOfDelete(Node[] nodes){
		if(nodes.length>MASSIVE_MAX_DELETE_NUMBER){
			return false;
		}
		else{
			return true;
		}
	}

	protected static boolean isValidMassiveNumberOfRetrieveSize(Long size){
		if(size>MASSIVE_MAX_RETRIEVE_SIZE){
			return false;
		}
		else{
			return true;
		}
	}

	protected static boolean isValidMassiveNumberOfGetMetadata(Long size){
		if(size>MASSIVE_MAX_GET_METADATA_SIZE){
			return false;
		}
		else{
			return true;
		}
	}

	protected static boolean isValidMassiveSizeOfContents(Content[] contents){
		long somma=0;
		for(int i=0;i<contents.length;i++){
			somma=somma+contents[i].getContent().length;
		}
		if(somma>MASSIVE_MAX_CREATE_SIZE){
			return false;
		}
		else{
			return true;
		}
	}

	protected static boolean isValidZero(Long value){
		return value==0;
	}

	protected static boolean isValidNotZero(Long value){
		return value!=0;
	}

	/**
	 * Esegue il rollback della transazione specificata in input ignorando
	 * le eventuali eccezioni.
	 *
	 * @param transaction La transazione di cui eseguire il rollback.
	 */
	protected static void rollbackQuietely(UserTransaction transaction) {
		try {
			transaction.rollback();
		} catch (Throwable t) {
			logger.warn("[EcmEnginePublisherBean::rollbackQuietely] Ignored exception during rollback: " + t.getMessage());
		}
	}

	/**
	 * Verifica se l'eccezione dei servizi di foundation ricevuta in input indica un problema nelle
	 * credenziali di autenticazione.
	 *
	 * <p>&Egrave; possibile specificare una transazione come ultimo parametro per fare in modo che il metodo ne esegua il
	 * rollback &quot;silenzioso&quot; nel caso l'eccezione in input corrisponda.</p>
	 *
	 * @param e L'eccezione dei servizi di foundation da analizzare.
	 * @param serviceName Il nome del servizio da cui questo metodo &egrave; richiamato.
	 * @param methodName Il nome del metodo da cui questo metodo &egrave; richiamato.
	 * @param message Un messaggio di errore da inserire nell'eccezione lanciata.
	 * @param transaction Una transazione di cui eseguire il rollback (facoltativo).
	 *
	 * @throws InvalidCredentialsException Se l'eccezione ricevuta in input indica un problema nelle
	 * credenziali di autenticazione.
	 */
	protected static void checkCredentialsException(EcmEngineFoundationException e, String serviceName,
	String methodName, String message, UserTransaction transaction)
	throws InvalidCredentialsException {
		if (e.getCode().equals(FoundationErrorCodes.BAD_CREDENTIALS_ERROR)) {
			logger.info("[" + serviceName + "::" + methodName + "] Bad credentials: " + message);
			if (transaction != null) {
				rollbackQuietely(transaction);
			}
			throw new InvalidCredentialsException("Bad credentials: " + message);
		}
	}

	/**
	 * Verifica se l'eccezione dei servizi di foundation ricevuta in input indica un problema nelle
	 * di insufficienti permessi di accesso.
	 *
	 * <p>&Egrave; possibile specificare una transazione come ultimo parametro per fare in modo che il metodo ne esegua il
	 * rollback &quot;silenzioso&quot; nel caso l'eccezione in input corrisponda.</p>
	 *
	 * @param e L'eccezione dei servizi di foundation da analizzare.
	 * @param serviceName Il nome del servizio da cui questo metodo &egrave; richiamato.
	 * @param methodName Il nome del metodo da cui questo metodo &egrave; richiamato.
	 * @param message Un messaggio di errore da inserire nell'eccezione lanciata.
	 * @param transaction Una transazione di cui eseguire il rollback (facoltativo).
	 *
	 * @throws InvalidCredentialsException Se l'eccezione ricevuta in input indica un problema nelle
	 * di insufficienti permessi di accesso.
	 */
	protected static void checkAccessException(EcmEngineFoundationException e, String serviceName,
			String methodName, String message, UserTransaction transaction)
	throws PermissionDeniedException {
		if (e.getCode().equals(FoundationErrorCodes.ACCESS_DENIED_ERROR)) {
			logger.info("[" + serviceName + "::" + methodName + "] Access denied: " + message);
			if (transaction != null) {
				rollbackQuietely(transaction);
			}
			throw new PermissionDeniedException("Access denied: " + message);
		}
	}
}

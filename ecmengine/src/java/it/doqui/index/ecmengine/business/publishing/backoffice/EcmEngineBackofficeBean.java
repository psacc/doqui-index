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

package it.doqui.index.ecmengine.business.publishing.backoffice;

import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvc;
import it.doqui.index.ecmengine.business.foundation.search.SearchSvc;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.job.tenant.TenantAdminJob;
import it.doqui.index.ecmengine.business.job.tenant.TenantDeleteJob;
import it.doqui.index.ecmengine.business.personalization.importer.ArchiveImporter;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDynamic;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.publishing.EcmEngineFeatureBean;
import it.doqui.index.ecmengine.business.publishing.util.SystemPropertyFilters;
import it.doqui.index.ecmengine.dto.AclRecord;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.backoffice.AclListParams;
import it.doqui.index.ecmengine.dto.backoffice.ContentStoreDefinition;
import it.doqui.index.ecmengine.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.dto.backoffice.ExportedContent;
import it.doqui.index.ecmengine.dto.backoffice.Group;
import it.doqui.index.ecmengine.dto.backoffice.IntegrityMessage;
import it.doqui.index.ecmengine.dto.backoffice.IntegrityReport;
import it.doqui.index.ecmengine.dto.backoffice.Repository;
import it.doqui.index.ecmengine.dto.backoffice.SystemProperty;
import it.doqui.index.ecmengine.dto.backoffice.Tenant;
import it.doqui.index.ecmengine.dto.backoffice.User;
import it.doqui.index.ecmengine.dto.backoffice.model.AspectMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.AssociationMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.ChildAssociationMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.CustomModel;
import it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor;
import it.doqui.index.ecmengine.dto.backoffice.model.ModelMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.PropertyMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.TypeMetadata;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.UnsupportedMethodException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.AclEditException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupAlreadyExistsException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupCreateException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupDeleteException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupEditException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchUserException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.TooManyNodesException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserAlreadyExistsException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserCreateException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserDeleteException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserUpdateException;
import it.doqui.index.ecmengine.exception.repository.JobRuntimeException;
import it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException;
import it.doqui.index.ecmengine.exception.security.AuthorityRuntimeException;
import it.doqui.index.ecmengine.integration.integrity.dao.IntegrityDAO;
import it.doqui.index.ecmengine.integration.integrity.vo.IntegrityNodeVO;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

@SuppressWarnings("unchecked")
public class EcmEngineBackofficeBean extends EcmEngineFeatureBean {

	private static final long serialVersionUID = -4039599746698342282L;

	private static final String DEFAULT_USER_HOMES_PATH = "/app:company_home/app:user_homes";

	private static final String EXACT_USERNAME_QUERY = "@cm\\:userName:\"{0}\"";
	private static final String FILTER_USERNAME_QUERY = "@cm\\:userName:\"{0}\"";
	private static final String TYPE_PERSON_QUERY = "TYPE:\"cm:person\"";

	private static final String MOVED_HOME_FOLDER_NAME =
		"{0}_{1,number,0000}{2,number,00}{3,number,00}T{4,number,00}{5,number,00}";

	public String createUser(User nuovoUtente, OperationContext context)
	throws UserCreateException, RemoteException, UserAlreadyExistsException, InvalidParameterException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::createUser] BEGIN");

		validate(ValidationType.USER, "nuovoUtente", nuovoUtente);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nomeUtente = nuovoUtente.getUsername();
		final String logCtx = "Nuovo utente: " + nomeUtente;
		String result = null;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			// TODO: possibilita` di specificare il folder

			final String repository = context.getRepository();
			authenticateOnRepository(context, null);	// Autenticazione separata per repository

			transaction.begin();

			dumpElapsed("EcmEngineBackofficeBean", "createUser", logCtx,
					"Autenticazione completata sul repository: " + repository);

			// Validazione tenant
			String username = getTenantUsername(nomeUtente, context);

			if (personService.personExists(username)) {
				logger.warn("[EcmEngineBackofficeBean::createUser] Utente gia` presente: " + nomeUtente +
						" [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new UserAlreadyExistsException(nomeUtente);
			}

			final Map<QName, Serializable> userProps = new HashMap<QName, Serializable>();
			userProps.put(ContentModel.PROP_USERNAME, username);
			userProps.put(ContentModel.PROP_FIRSTNAME, nuovoUtente.getName());
			userProps.put(ContentModel.PROP_LASTNAME, nuovoUtente.getSurname());

			if (nuovoUtente.getOrganizationId() != null) {
				userProps.put(ContentModel.PROP_ORGID, nuovoUtente.getOrganizationId());
			}

			// Gestione home folder
			String homeFolderPath = nuovoUtente.getHomeFolderPath();
			if (homeFolderPath == null || homeFolderPath.length() == 0) {
				homeFolderPath = DEFAULT_USER_HOMES_PATH;
			} else {
				// Filtrare l'eventuale username specificato
				final String prefixMatch = "/sys:";
				int lastPathElement = homeFolderPath.lastIndexOf(prefixMatch);
				if (lastPathElement > 0 && homeFolderPath.substring(lastPathElement+prefixMatch.length()).equals(username)) {
					homeFolderPath = homeFolderPath.substring(0, lastPathElement);
				}
			}

			logger.debug("[EcmEngineBackofficeBean::createUser] Ricerca del contenitore degli home folder nel repository: " +
					repository);
			// MB: Su query XPATH non serve la close
			ResultSet rs = searchService.query(DictionarySvc.SPACES_STORE, SearchSvc.LANGUAGE_XPATH, homeFolderPath);
			if (rs.length() == 0) {
				logger.error("[EcmEngineBackofficeBean::createUser] Nessun contenitore trovato nel repository: " +
						repository);
				rollbackQuietely(transaction);
				throw new UserCreateException(nomeUtente);
			}

			final NodeRef homeFolderContainer = rs.getNodeRef(0);

			logger.debug("[EcmEngineBackofficeBean::createUser] Creazione home folder per l'utente: " + nomeUtente + " [Repository: " + repository + "]");
			final Map<QName, Serializable> homeFolderProps = new HashMap<QName, Serializable>();
			homeFolderProps.put(ContentModel.PROP_NAME, username);

			final QName homeFolderAssocName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, username);

			final ChildAssociationRef homeFolderAssocRef = nodeService.createNode(
					homeFolderContainer, ContentModel.ASSOC_CONTAINS,
					homeFolderAssocName, ContentModel.TYPE_FOLDER, homeFolderProps);
			final NodeRef homeFolder = homeFolderAssocRef.getChildRef();

			dumpElapsed("EcmEngineBackofficeBean", "createUser", logCtx, "Home folder creato - UID: " + homeFolder + " - R: " + repository);

			// Setto il valore del nuovo home folder
			userProps.put(ContentModel.PROP_HOMEFOLDER, homeFolder);

			logger.debug("[EcmEngineBackofficeBean::createUser] Creazione dell'utente: " + nomeUtente +" [Repository: " + repository + "]");

			final NodeRef person = personService.createPerson(userProps);
			dumpElapsed("EcmEngineBackofficeBean", "createUser", logCtx, "Utente creato - UID: " + person.getId() + " - R: " + repository);

			permissionService.setPermission(person, username, permissionService.getAllPermission(), true);
			dumpElapsed("EcmEngineBackofficeBean", "createUser", logCtx, "Impostate ACL sull'oggetto \"cm:person\" - R: " + repository);

			if (nuovoUtente.getPassword() != null) {
				logger.debug("[EcmEngineBackofficeBean::createUser] Creazione delle informazioni di autenticazione per l'utente: " + nomeUtente +" [Repository: " + repository + "]");
				authenticationService.createAuthentication(username, nuovoUtente.getPassword().toCharArray());
				dumpElapsed("EcmEngineBackofficeBean", "createUser", logCtx, "Informazioni di autenticazione create - R: " + repository);
			}

			result = nuovoUtente.getUsername();

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "createUser", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "createUser", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::createUser] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new UserCreateException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::createUser] END");
		}

		return result;
	}

	public void updateUserMetadata(User utente, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException,
	RemoteException, EcmEngineTransactionException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::updateUserMetadata] BEGIN");

		validate(ValidationType.USER, "utente", utente);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nomeUtente = utente.getUsername();
		final String logCtx = "Modifica metadati utente: " + nomeUtente;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();

			authenticateOnRepository(context, null);

			transaction.begin();
			if(utente.getHomeFolderPath()!=null){
				rollbackQuietely(transaction);
				throw new UserUpdateException("Non è possibile aggiornare l'home folder dell'utente.");
			}
			// Validazione tenant
			String username = getTenantUsername(nomeUtente, context);

			if (!personService.personExists(username)) {
				logger.warn("[EcmEngineBackofficeBean::updateUserMetadata] " +
						"Utente non presente: " + nomeUtente + " [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new NoSuchUserException(nomeUtente);
			}

			final NodeRef personRef = personService.getPerson(username);

			// Manteniamo le property presenti che non vengono specificate nel DTO
			final Map<QName, Serializable> userProps = nodeService.getProperties(personRef);

			// Sovrascriviamo con i nuovi valori
			userProps.put(ContentModel.PROP_USERNAME, username);
			userProps.put(ContentModel.PROP_FIRSTNAME, utente.getName());
			userProps.put(ContentModel.PROP_LASTNAME, utente.getSurname());
			userProps.put(ContentModel.PROP_ORGID, utente.getOrganizationId());

			logger.debug("[EcmEngineBackofficeBean::updateUserMetadata] " +
					"Modifica dei metadati dell'utente: " + nomeUtente + " [Repository: " + repository + "]");

			/*
			 * Chiamata diretta a nodeService per consentire ad utenti non amministratori di modificare
			 * il proprio profilo (setPersonProperties() e` normalmente limitato ai soli amministratori).
			 */
			nodeService.setProperties(personRef, userProps);

			dumpElapsed("EcmEngineBackofficeBean", "updateUserMetadata", logCtx,
					"Metadati modificati - U: " + nomeUtente + " - R: " + repository);

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "updateUserMetadata", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "updateUserMetadata", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::updateUserMetadata] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new UserUpdateException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::updateUserMetadata] END");
		}
	}

	public String createGroup(Group nuovoGruppo, Group gruppoPadre, OperationContext context)
	throws GroupCreateException, RemoteException, GroupAlreadyExistsException, InvalidParameterException,
	NoSuchGroupException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::createGroup] BEGIN");

		validate(ValidationType.GROUP, "nuovoGruppo", nuovoGruppo);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Nuovo gruppo: " + nuovoGruppo.getName()
		+ " [Repository: " + context.getRepository() + "]";
		String result = null;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();

			authenticateOnRepository(context, null);

			transaction.begin();

			final String fullGroupName = authorityService.getName(
					AuthorityType.GROUP, nuovoGruppo.getName());

			dumpElapsed("EcmEngineBackofficeBean", "createGroup", logCtx,
					"Autenticazione completata sul repository: " + repository);

			if (authorityService.authorityExists(fullGroupName)) {
				logger.warn("[EcmEngineBackofficeBean::createGroup] " +
						"Gruppo gia` presente: " + nuovoGruppo.getName() +
						" [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new GroupAlreadyExistsException(nuovoGruppo.getName());
			}

			logger.debug("[EcmEngineBackofficeBean::createGroup] Creazione del gruppo: " +
					nuovoGruppo.getName() + " [Repository: " + repository + "]");

			final String fullParentName = (gruppoPadre != null)
			? authorityService.getName(AuthorityType.GROUP, gruppoPadre.getName())
					: null;

			try {
				result = authorityService.createAuthority(AuthorityType.GROUP, fullParentName, nuovoGruppo.getName());
			} catch (AuthorityRuntimeException e) {
				if (e.getCode().equals(FoundationErrorCodes.UNKNOWN_AUTHORITY_ERROR)) {
					logger.info("[EcmEngineBackofficeBean::createGroup] Gruppo padre inesistente: " +
							fullParentName + " [Repository: " + repository + "]");
				} else {
					logger.warn("[EcmEngineBackofficeBean::createGroup] Authority service error: " + e.getCode());
				}
				rollbackQuietely(transaction);
				throw new NoSuchGroupException(gruppoPadre.getName());
			}
			dumpElapsed("EcmEngineBackofficeBean", "createGroup", logCtx, "Utente creato - R: " + repository);

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "createGroup", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "createGroup", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::createGroup] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new GroupCreateException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::createGroup] END");
		}

		return result;
	}

	public void addUserToGroup(User utente, Group gruppo, OperationContext context)
	throws GroupEditException, RemoteException, NoSuchUserException, NoSuchGroupException, InvalidParameterException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::addUserToGroup] BEGIN");

		validate(ValidationType.NOT_NULL, "utente", utente);
		validate(ValidationType.NAME, "utente.username", utente.getUsername());
		validate(ValidationType.GROUP, "gruppo", gruppo);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.NOT_GUEST, "utente.username", utente.getUsername());
		validate(ValidationType.GROUP_NOT_EMAIL_CONTRIBUTORS, "gruppo", gruppo);

		final String logCtx = "Aggiungi utente \"" + utente.getUsername() +
		"\" a gruppo: " + gruppo.getName();
		logger.debug("[EcmEngineBackofficeBean::addUserToGroup] Parametri -" +
				" U: " + utente.getUsername() +
				" G: " + gruppo.getName() +
				" POC: " + context.getUsername());

		// Verifica non piu' necessaria
//		if (isCrossRepository(utente.getRepository(), gruppo.getRepository())) {
//		throw new InvalidParameterException("I repository specificati non coincidono -" +
//		" RU: " + ((utente.getRepository() != null) ? utente.getRepository() : "null") +
//		" RG: " + ((gruppo.getRepository() != null) ? gruppo.getRepository() : "null"));
//		}

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();

			authenticateOnRepository(context, null);

			transaction.begin();

			// Validazione tenant
			String username = getTenantUsername(utente.getUsername(), context);

			final String fullGroupName = authorityService.getName(
					AuthorityType.GROUP, gruppo.getName());

			if (!authorityService.authorityExists(fullGroupName)) {
				logger.warn("[EcmEngineBackofficeBean::addUserToGroup] " +
						"Gruppo mancante: " + gruppo.getName() +
						" [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new NoSuchGroupException(gruppo.getName());
			}

			if (!personService.personExists(username)) {
				logger.warn("[EcmEngineBackofficeBean::addUserToGroup] " +
						"Utente mancante: " + utente.getUsername());
				rollbackQuietely(transaction);
				throw new NoSuchUserException(utente.getUsername());
			}

			logger.debug("[EcmEngineBackofficeBean::addUserToGroup] " +
					"Associazione dell'utente \"" + utente.getUsername() +
					"\" al gruppo: " + gruppo.getName() +
					" [Repository: " + repository + "]");

			// Il metodo addAuthority() prende come parametro il nome
			// dell'utente SENZA tenant
			authorityService.addAuthority(fullGroupName, username);
//			authorityService.addAuthority(fullGroupName, utente.getUsername());

			dumpElapsed("EcmEngineBackofficeBean", "addUserToGroup", logCtx, "Utente associato.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "addUserToGroup", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "addUserToGroup", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::addUserToGroup] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new GroupEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::addUserToGroup] END");
		}
	}

	public void removeUserFromGroup(User utente, Group gruppo, OperationContext context)
	throws GroupEditException, RemoteException, NoSuchUserException, NoSuchGroupException, InvalidParameterException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::removeUserFromGroup] BEGIN");

		validate(ValidationType.NOT_NULL, "utente", utente);
		validate(ValidationType.NAME, "utente.username", utente.getUsername());
		validate(ValidationType.GROUP, "gruppo", gruppo);
		validate(ValidationType.GROUP_NOT_EMAIL_CONTRIBUTORS, "gruppo", gruppo);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nomeUtente = utente.getUsername();
		final String logCtx = "Rimuovi utente \"" + nomeUtente +
		"\" da gruppo: " + gruppo.getName();
		logger.debug("[EcmEngineBackofficeBean::removeUserFromGroup] Parametri -" +
				" U: " + nomeUtente + " G: " + gruppo + " POC: " + context.getUsername());

		// Verifica non piu' necessaria
//		if (isCrossRepository(utente.getRepository(), gruppo.getRepository())) {
//		throw new InvalidParameterException("I repository specificati non coincidono -" +
//		" RU: " + ((utente.getRepository() != null) ? utente.getRepository() : "null") +
//		" RG: " + ((gruppo.getRepository() != null) ? gruppo.getRepository() : "null"));
//		}

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();

			authenticateOnRepository(context, null);

			transaction.begin();

			final String fullGroupName = authorityService.getName(
					AuthorityType.GROUP, gruppo.getName());

			if (!authorityService.authorityExists(fullGroupName)) {
				logger.warn("[EcmEngineBackofficeBean::removeUserFromGroup] " +
						"Gruppo mancante: " + gruppo.getName() +
						" [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new NoSuchGroupException(gruppo.getName());
			}

			// Validazione tenant
			String username = getTenantUsername(nomeUtente, context);

			if (!personService.personExists(username)) {
				logger.warn("[EcmEngineBackofficeBean::removeUserFromGroup] " +
						"Utente mancante: " + nomeUtente +
						" [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new NoSuchUserException(nomeUtente);
			}

			logger.debug("[EcmEngineBackofficeBean::removeUserFromGroup] " +
					"Rimozione dell'utente \"" + nomeUtente + "\" dal gruppo: " + gruppo.getName()+
					" [Repository: " + repository + "]");

			authorityService.removeAuthority(fullGroupName, username);
//			authorityService.removeAuthority(fullGroupName, nomeUtente);

			dumpElapsed("EcmEngineBackofficeBean", "removeUserFromGroup", logCtx, "Utente rimosso.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "removeUserFromGroup", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "removeUseFromGroup", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::removeUserFromGroup] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new GroupEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::removeUserFromGroup] END");
		}
	}

	public User [] listUsers(Group gruppo, OperationContext context)
	throws RemoteException, EcmEngineTransactionException, EcmEngineException, InvalidParameterException, InvalidCredentialsException {
		logger.debug("[EcmEngineBackofficeBean::listUsers] BEGIN");

		validate(ValidationType.GROUP, "gruppo", gruppo);
		validate(ValidationType.GROUP_NOT_EMAIL_CONTRIBUTORS, "gruppo", gruppo);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nomeGruppo = gruppo.getName();
		final String logCtx = "Lista utenti del gruppo: " + nomeGruppo;
		logger.debug("[EcmEngineBackofficeBean::listUsers] Parametri -" +
				" G: " + nomeGruppo + " POC: " + context.getUsername());

		Vector<User> results = new Vector<User>();

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "listUsers", logCtx, "Autenticazione completata.");

			transaction.begin();

			final String fullGroupName = authorityService.getName(AuthorityType.GROUP, nomeGruppo);
			if (!authorityService.authorityExists(fullGroupName)) {
				logger.warn("[EcmEngineBackofficeBean::listUsers] " +
						"Gruppo mancante: " + fullGroupName);
				rollbackQuietely(transaction);
				throw new NoSuchGroupException(fullGroupName);
			}

			logger.debug("[EcmEngineBackofficeBean::listUsers] " +
					"Lettura lista utenti del gruppo: " + nomeGruppo);

			final Set<String> userNames = authorityService.getContainedAuthorities(
					AuthorityType.USER, fullGroupName, true);
			dumpElapsed("EcmEngineBackofficeBean", "listUsers", logCtx,
					"Lettura completata: " + userNames.size() + " risultati.");

			logger.debug("[EcmEngineBackofficeBean::listUsers] " +
					"Generazione DTO risultato per il gruppo: " + nomeGruppo);
			for (String userName: userNames) {
				final NodeRef person = personService.getPerson(userName);
				final Map<QName, Serializable> personProps = nodeService.getProperties(person);
				final String nome = (String)personProps.get(ContentModel.PROP_FIRSTNAME);
				final String cognome = (String)personProps.get(ContentModel.PROP_LASTNAME);
				final String organizationId = (String)personProps.get(ContentModel.PROP_ORGID);

				User user = new User(nome, cognome, userName);
				user.setOrganizationId(organizationId);
				final NodeRef homeFolderNodeRef = (NodeRef)personProps.get(ContentModel.PROP_HOMEFOLDER);
				user.setHomeFolderPath(dictionaryService.resolvePathToPrefixNameString(nodeService.getPath(homeFolderNodeRef)));

				results.add(user);
			}
			dumpElapsed("EcmEngineBackofficeBean", "listUsers", logCtx, "Creazione DTO completata.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "listUsers", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::listUsers] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::listUsers] END");
		}

		return results.toArray(new User [] {});
	}

	public User retrieveUserMetadata(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, PermissionDeniedException, NoDataExtractedException,
	EcmEngineException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::retrieveUserMetadata] BEGIN");

		// Deve essere specificato solo lo username perche` il DTO viene utilizzato come filtro
		validate(ValidationType.NOT_NULL, "filter", filter);
		validate(ValidationType.NAME, "filter.username", filter.getUsername());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String username = getTenantUsername(filter.getUsername(), context);
		//final String repository = context.getRepository();
		final String logCtx = "Lettura metadati utente: " + username;
		User result = null;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "retrieveUserMetadata", logCtx, "Autenticazione completata.");

			transaction.begin();

			if (!personService.personExists(username)) {
				logger.error("[EcmEngineBackofficeBean::retrieveUserMetadata] ERROR: L'utente specificato non esiste: " + username);
				rollbackQuietely(transaction);
				throw new NoSuchUserException(username);
			}

			final NodeRef userNodeRef = personService.getPerson(username);
			Map<QName,Serializable> userProperties = nodeService.getProperties(userNodeRef);
			final String firstName = (String)userProperties.get(ContentModel.PROP_FIRSTNAME);
			final String lastName = (String)userProperties.get(ContentModel.PROP_LASTNAME);
			final String organization = (String)userProperties.get(ContentModel.PROP_ORGID);

			result = new User();
			result.setUsername(username);
			result.setName(firstName);
			result.setSurname(lastName);
			result.setOrganizationId(organization);
			final NodeRef homeFolderNodeRef = (NodeRef)userProperties.get(ContentModel.PROP_HOMEFOLDER);
			result.setHomeFolderPath(dictionaryService.resolvePathToPrefixNameString(nodeService.getPath(homeFolderNodeRef)));

			dumpElapsed("EcmEngineBackofficeBean", "retrieveUserMetadata", logCtx, "Lettura completata.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "retrieveUserMetadata", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "retrieveUserMetadata", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::retrieveUserMetadata] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::retrieveUserMetadata] END");
		}
		return result;
	}

	public User [] listAllUsers(User filter, OperationContext context)
	throws RemoteException, InvalidCredentialsException, NoDataExtractedException, InvalidParameterException,
	EcmEngineTransactionException, EcmEngineException {
		logger.debug("[EcmEngineBackofficeBean::listAllUsers] BEGIN");

		// Deve essere specificato solo lo username perche` il DTO viene utilizzato come filtro
		validate(ValidationType.NOT_NULL, "filter", filter);
		validate(ValidationType.NAME, "filter.username", filter.getUsername());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String userFilter = getTenantUsername(filter.getUsername(), context);
		final String logCtx = "Lista tutti utenti per filtro: " + userFilter;
		logger.debug("[EcmEngineBackofficeBean::listAllUsers] Parametri -" +
				" UF: " + userFilter + " POC: " + context.getUsername());

		Map<String, User> results = new HashMap<String, User>(800, 0.75f);

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		ResultSet users = null;

		try {
			final String repository = context.getRepository();
			authenticateOnRepository(context, null);

			transaction.begin();

			logger.debug("[EcmEngineBackofficeBean::listAllUsers] " +
					"Lettura lista utenti con filtro: " + userFilter + " [Repository: " + repository + "]");

			final String query = (userFilter == null)
			? TYPE_PERSON_QUERY
					: ((userFilter.contains("*")) ? MessageFormat.format(FILTER_USERNAME_QUERY, userFilter)
							: MessageFormat.format(EXACT_USERNAME_QUERY, userFilter));

			logger.debug("[EcmEngineBackofficeBean::listAllUsers] Q: " + query + " [Repository: " + repository + "]");

			// 17:46:35 martedi' 03 febraio 2009
			// Vecchia implementazione
			//ResultSet users = searchService.query(DictionarySvc.SPACES_STORE, SearchSvc.LANGUAGE_LUCENE, query);

			// MB: gestione del retry
			// Se avviene un inserimento e successivamente una search, il dato non viene subito trovato
			// In questo caso faccio 1 retry
			int nMaxRetry = 5;
			for( int nRetry=0; nRetry<nMaxRetry; nRetry++ ){
				users = searchService.query(DictionarySvc.SPACES_STORE, SearchSvc.LANGUAGE_LUCENE, query);
				if (users.length()<1 && (nRetry+1)<nMaxRetry ){
					if( users!=null ) {
						users.close();
					}

					logger.error("[EcmEngineBackofficeBean::listAllUsers] NoDataExtracted: retry " +nRetry);

					// Sleep di 500 millis
					try {
						Thread.sleep(500);
					} catch (Throwable xx) {}

					// Wake up
					logger.error("[EcmEngineBackofficeBean::listAllUsers] NoDataExtracted: wake up");
				} else {
					break;
				}
			}

			dumpElapsed("EcmEngineBackofficeBean", "listAllUsers", logCtx,
					"Lettura completata: " + users.length() + " - R: " + repository + "]");
			transaction.commit();
			if (users.length() > 0) {

				logger.debug("[EcmEngineBackofficeBean::listAllUsers] " +
						"Generazione DTO risultato per il filtro: " + userFilter + " [Repository: " + repository + "]");
				for (ResultSetRow user : users) {

					final Map<QName, Serializable> personProps = nodeService.getProperties(user.getNodeRef());
					final String userName = (String)personProps.get(ContentModel.PROP_USERNAME);
					final String nome = (String)personProps.get(ContentModel.PROP_FIRSTNAME);
					final String cognome = (String)personProps.get(ContentModel.PROP_LASTNAME);
					final String organizationId = (String)personProps.get(ContentModel.PROP_ORGID);

					if (!results.containsKey(userName)) {
                        logger.debug("[EcmEngineBackofficeBean::listAllUsers] Generazione DTO per utente: " +userName + " [Repository: " + repository + "]");

						final User newUser = new User();

						newUser.setName(nome);
						newUser.setSurname(cognome);
						newUser.setUsername(userName);
						newUser.setOrganizationId(organizationId);
						final NodeRef homeFolderNodeRef = (NodeRef)personProps.get(ContentModel.PROP_HOMEFOLDER);

                        try {
            			    org.alfresco.service.cmr.repository.Path userPath = nodeService.getPath(homeFolderNodeRef);
						    newUser.setHomeFolderPath(dictionaryService.resolvePathToPrefixNameString(userPath));
    					} catch (Exception xx) { //TODO:Da definire meglio l'eccezione.
                            logger.warn("[EcmEngineBackofficeBean::listAllUsers] HomeFolder not found for user "+userName+" - Set to null");
                            newUser.setHomeFolderPath(null);
                        }
						results.put(userName, newUser);
					} else {
						final User oldUser = results.get(userName);

						results.put(userName, oldUser);
					}
				}

				// Chiusa dalla finally
				//users.close();
			} else {
				throw new NoDataExtractedException(userFilter, context.getRepository());
			}

			dumpElapsed("EcmEngineBackofficeBean", "listAllUsers", logCtx, "Creazione DTO completata.");

			//AF:transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "listAllUsers", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::listAllUsers] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			if( users!=null ) {
				users.close();
			}
			stop();
			logger.debug("[EcmEngineBackofficeBean::listAllUsers] END");
		}

		return results.values().toArray(new User [] {});
	}


	public void updateUserPassword(User utente, OperationContext context)
	throws InvalidParameterException, UserUpdateException, NoSuchUserException, RemoteException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::updateUserPassword] BEGIN");

		validate(ValidationType.NOT_NULL, "utente", utente);
		validate(ValidationType.NAME, "utente.username", utente.getUsername());
		validate(ValidationType.PASSWORD, "utente.password", utente.getPassword().toCharArray());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nomeUtente = utente.getUsername();
		final String logCtx = "Modifica password utente: " + nomeUtente;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();
			authenticateOnRepository(context, null);

			transaction.begin();

			// Validazione tenant
			String username = getTenantUsername(nomeUtente, context);

			if (!personService.personExists(username)) {
				logger.warn("[EcmEngineBackofficeBean::updateUserPassword] " +
						"Utente non presente: " + nomeUtente + " [Repository: " + repository + "]");
				rollbackQuietely(transaction);
				throw new NoSuchUserException(nomeUtente);
			}

			logger.debug("[EcmEngineBackofficeBean::updateUserPassword] " +
					"Modifica password dell'utente: " + nomeUtente + " [Repository: " + repository + "]");

			authenticationService.setAuthentication(username, utente.getPassword().toCharArray());

			dumpElapsed("EcmEngineBackofficeBean", "updateUserPassword", logCtx,
					"Password modificata - U: " + nomeUtente + " - R: " + repository);

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "updateUserPassword", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "updateUserPassword", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::updateUserPassword] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new UserUpdateException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::updateUserPassword] END");
		}
	}

	public void deleteUser(User utente, OperationContext context)
	throws InvalidParameterException, UserDeleteException, NoSuchUserException, RemoteException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::deleteUser] BEGIN");

		validate(ValidationType.NOT_NULL, "utente", utente);
		validate(ValidationType.NAME, "utente.username", utente.getUsername());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nomeUtente = utente.getUsername();
		final String logCtx = "Eliminazione utente: " + nomeUtente;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();
			authenticateOnRepository(context, null);

			transaction.begin();

			NodeRef folderToMove = null;
			final GregorianCalendar curDate = new GregorianCalendar();

			// Validazione tenant
			String username = getTenantUsername(nomeUtente, context);

			if (personService.personExists(username)) {

				final NodeRef personRef = personService.getPerson(username);

				folderToMove = DefaultTypeConverter.INSTANCE.convert(NodeRef.class,
						nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER));

				if (folderToMove != null) {
					logger.debug("[EcmEngineBackofficeBean::deleteUser] Folder da spostare: " + folderToMove);

					final ChildAssociationRef oldAssocRef = nodeService.getPrimaryParent(folderToMove);
					Map<QName, Serializable> folderProps = nodeService.getProperties(folderToMove);

					final String oldFolderName = (String) folderProps.get(ContentModel.PROP_NAME);
					final String newFolderName = MessageFormat.format(MOVED_HOME_FOLDER_NAME,
							oldFolderName,
							curDate.get(GregorianCalendar.YEAR),
							curDate.get(GregorianCalendar.MONTH) + 1,
							curDate.get(GregorianCalendar.DAY_OF_MONTH),
							curDate.get(GregorianCalendar.HOUR_OF_DAY),
							curDate.get(GregorianCalendar.MINUTE));

					folderProps.put(ContentModel.PROP_NAME, newFolderName);

					final QName newAssocName = QName.createQName(oldAssocRef.getQName().getNamespaceURI(),
							newFolderName);

					logger.debug("[EcmEngineBackofficeBean::deleteUser] Nome nuova associazione: " + newAssocName);

					final ChildAssociationRef newAssocRef = nodeService.moveNode(folderToMove, oldAssocRef.getParentRef(),
							oldAssocRef.getTypeQName(), newAssocName);
					nodeService.setProperties(newAssocRef.getChildRef(), folderProps);

					dumpElapsed("EcmEngineBackofficeBean", "deleteUser", logCtx,
							"Home dell'utente '" + nomeUtente + "' rinominata in '" + newFolderName + "' - R: " + repository);
				}

				personService.deletePerson(username);
				dumpElapsed("EcmEngineBackofficeBean", "deleteUser", logCtx,
						"Utente '" + nomeUtente + "' eliminato - R: " + repository);
			} else {
				logger.error("[EcmEngineBackofficeBean::deleteUser] ERROR: L'utente specificato non esiste: " + nomeUtente);
				rollbackQuietely(transaction);
				throw new NoSuchUserException(nomeUtente);
			}

			if (authenticationService.authenticationExists(username)) {
				authenticationService.deleteAuthentication(username);
				dumpElapsed("EcmEngineBackofficeBean", "deleteUser", logCtx,
						"Info di autenticazione per '" + nomeUtente + "' eliminate - R: " + repository);
			} else {
				// Se le informazioni non ci sono segnaliamo solo la situazione e procediamo normalmente
				logger.debug("[EcmEngineBackofficeBean::deleteUser] " +
						"Le informazioni di autenticazione per l'utente specificato non esistono: " + nomeUtente);
			}

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "deleteUser", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "deleteUser", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::deleteUser] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new UserDeleteException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::deleteUser] END");
		}
	}

	public void deleteGroup(Group gruppo, OperationContext context)
	throws InvalidParameterException, GroupDeleteException, NoSuchGroupException, RemoteException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::deleteGroup] BEGIN");

		validate(ValidationType.GROUP, "gruppo", gruppo);
		validate(ValidationType.GROUP_NOT_EMAIL_CONTRIBUTORS, "gruppo", gruppo);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Eliminazione gruppo: " + gruppo.getName()
		+ " [Repository: " + context.getRepository() + "]";

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			final String repository = context.getRepository();
			authenticateOnRepository(context, null);

			transaction.begin();

			final String fullGroupName = authorityService.getName(
					AuthorityType.GROUP, gruppo.getName());

			logger.debug("[EcmEngineBackofficeBean::deleteGroup] Eliminazione del gruppo: " +
					gruppo.getName() + " [Repository: " + repository + "]");

			if (authorityService.authorityExists(fullGroupName)) {
				authorityService.deleteAuthority(fullGroupName);
				dumpElapsed("EcmEngineBackofficeBean", "deleteGroup", logCtx,
						"Gruppo '" + gruppo.getName() + "' eliminato - R: " + repository);
			} else {
				logger.error("[EcmEngineBackofficeBean::deleteGroup] ERROR: Il gruppo specificato non esiste: " + gruppo.getName());
				rollbackQuietely(transaction);
				throw new NoSuchGroupException(gruppo.getName());
			}

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "deleteGroup", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "deleteGroup", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::deleteGroup] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new GroupDeleteException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::deleteGroup] END");
		}
	}

	// NUOVI SERVIZI CON ARRAY

	public void addAcl(Node node, AclRecord[] acls, OperationContext context)
	throws AclEditException, NoSuchNodeException, RemoteException, InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::addAcl] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NOT_NULL, "acls", acls);
		for(AclRecord aclRecord:acls){
			validate(ValidationType.ACL_RECORD,"acl",aclRecord);
		}
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "ADD ACL [" + acls.length + " record(s)] a nodo: " + node.getUid();
		logger.debug("[EcmEngineBackofficeBean::addAcl] Parametri -" +
				" ACLs: [ " + acls + " - " + acls.length + " record(s)] N: " + node.getUid() +
				" U: " + context.getUsername());

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "addAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineBackofficeBean::addAcl] " +
					"Associazione dei record di ACL al nodo: " + node.getUid());

			for (int i = 0; i < acls.length; i++) {
				permissionService.setPermission(nodeRef, acls[i].getAuthority(),
						acls[i].getPermission(), acls[i].isAccessAllowed());
			}
			dumpElapsed("EcmEngineBackofficeBean", "addAcl", logCtx,
					"Record ACL aggiunti: " + acls.length + " record(s).");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "addAcl", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "addAcl", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::addAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::addAcl] END");
		}
	}

	public boolean isInheritsAcl(Node node, OperationContext context)
	throws NoSuchNodeException, RemoteException, AclEditException, InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException {
		logger.debug("[EcmEngineBackofficeBean::isInheritsAcl] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineBackofficeBean::isInheritsAcl] Parametri -" +
				" N: " + node.getUid() + " U: " + context.getUsername());

		final String logCtx = "Check inherits ACL su nodo: " + node.getUid();
		boolean result = false;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "isInheritsAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineBackofficeBean::isInheritsAcl] Nodo esistente.");

			result = permissionService.getInheritParentPermissions(nodeRef);

			dumpElapsed("EcmEngineBackofficeBean", "isInheritsAcl", logCtx,
					"Check completato: ACL " + ((result) ? "" : "non") + " ereditate.");
			logger.debug("[EcmEngineBackofficeBean::isInheritsAcl] " +
					"Check completato: ACL " + ((result) ? "" : "non") + " ereditate.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "isInheritsAcl", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::isInheritsAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::isInheritsAcl] END");
		}
		return result;
	}

	public AclRecord[] listAcl(Node node, AclListParams params, OperationContext context)
	throws NoSuchNodeException, RemoteException, AclEditException, InvalidParameterException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::listAcl] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NOT_NULL, "params", params);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String nodeUuid = node.getUid();
		final String logCtx = "Lista ACL nodo: " + nodeUuid;
		final Set<AclRecord> results = new HashSet<AclRecord>();

		logger.debug("[EcmEngineBackofficeBean::listAcl] Parametri -" +
				" N: " + nodeUuid + " UA: " + context.getUsername());

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "listAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineBackofficeBean::listAcl] Nodo esistente. Lettura della ACL.");

			final Set<AccessPermission> nodeAcl = permissionService.getAllSetPermissions(nodeRef);
			dumpElapsed("EcmEngineBackofficeBean", "listAcl", logCtx,
					"Lettura ACL completata: " + nodeAcl.size() + " risultati.");

			logger.debug("[EcmEngineBackofficeBean::listAcl] " +
					"Lettura della ACL completata: " + nodeAcl.size() + " risultati.");
			for (AccessPermission aclEntry: nodeAcl) {
				final AclRecord record = new AclRecord();

				record.setAuthority(aclEntry.getAuthority());
				record.setPermission(aclEntry.getPermission());
				record.setAccessAllowed(aclEntry.getAccessStatus() == AccessStatus.ALLOWED);

				results.add(record);
			}

			if (params.isShowInherited() && permissionService.getInheritParentPermissions(nodeRef)) {
				/* Se il nodo eredita i permessi dal padre e` necessario risalire l'albero per ottenere
				 * una lista completa dei permessi.
				 */
				List<ChildAssociationRef> parentAssocList = null;

				if (permissionService.supportsMultipleInheritance()) {

					// Ereditarieta` multipla: leggi le ACL di tutti i padri
					logger.debug("[EcmEngineBackofficeBean::listAcl] Ricorsione su tutti i padri...");
					parentAssocList = nodeService.getParentAssocs(nodeRef);
				} else {

					// Ereditarieta` singola: leggi le ACL del padre primario
					logger.debug("[EcmEngineBackofficeBean::listAcl] Ricorsione sul padre primario...");
					parentAssocList = new ArrayList<ChildAssociationRef>(1);
					parentAssocList.add(nodeService.getPrimaryParent(nodeRef));
				}

				HashSet<NodeRef> parentSet = new HashSet<NodeRef>(parentAssocList.size() * 5);

				while (!parentAssocList.isEmpty()) {
					HashSet<NodeRef> nextSet = new HashSet<NodeRef>(parentAssocList.size() * 2);

					for (ChildAssociationRef ref : parentAssocList) {
						final NodeRef parent = ref.getParentRef();

						if (parent == null) {
							continue;
						}

						if (!parentSet.contains(parent)) {
							parentSet.add(ref.getParentRef());
						}
						nextSet.add(parent);
					}

					parentAssocList.clear();

					for (NodeRef ref : nextSet) {
						if (permissionService.getInheritParentPermissions(ref)) {
							parentAssocList.addAll(nodeService.getParentAssocs(ref));
						}
					}
				}

				logger.debug("[EcmEngineBackofficeBean::listAcl] Nodi da cui vengono ereditate le ACL: " + parentSet);

				for (NodeRef parentRef : parentSet) {
					logger.debug("[EcmEngineBackofficeBean::listAcl] Lettura della ACL del padre: " + parentRef);

					final Set<AccessPermission> parentAcl = permissionService.getAllSetPermissions(parentRef);

					for (AccessPermission aclEntry: parentAcl) {
						//final Node parent = new Node(parentRef.getId());

						final AclRecord record = new AclRecord();

						record.setAuthority(aclEntry.getAuthority());
						record.setPermission(aclEntry.getPermission());
						record.setAccessAllowed(aclEntry.getAccessStatus() == AccessStatus.ALLOWED);

						results.add(record);
					}
				}

//				while (permissionService.getInheritParentPermissions(nodeRef)) {
//				logger.debug("[EcmEngineBackofficeBean::listAcl] Lettura della ACL del padre.");
//				final ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
//				final NodeRef parentRef = parentAssocRef.getParentRef();

//				if (parentRef == null) {
//				break;
//				}

//				final Set<AccessPermission> parentAcl = permissionService.getAllSetPermissions(parentRef);

//				for (AccessPermission aclEntry: parentAcl) {
//				final Node parent = new Node(parentRef.getId());

//				final AclRecord record = new AclRecord();

//				record.setAuthority(aclEntry.getAuthority());
//				record.setPermission(aclEntry.getPermission());
//				record.setAccessAllowed(aclEntry.getAccessStatus() == AccessStatus.ALLOWED);
//				record.setNodeUid(parent.getUid());

//				results.add(record);
//				}
//				nodeRef = parentRef; // Risali lungo l'albero
//				}

				dumpElapsed("EcmEngineBackofficeBean", "listAcl", logCtx,
						"Lettura ACL dei parent completata: " + results.size() + " risultati totali.");
				logger.debug("[EcmEngineBackofficeBean::listAcl] " +
						"Lettura della ACL dei parent completata: " + results.size() + " risultati totali.");
			}

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "listAcl", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "listAcl", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::listAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::listAcl] END");
		}

		return results.toArray(new AclRecord [] {});
	}

	public void removeAcl(Node node, AclRecord[] acls, OperationContext context)
	throws AclEditException, NoSuchNodeException, RemoteException, InvalidParameterException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::removeAcl] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NOT_NULL, "acls", acls);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "DEL ACL [" + acls.length + " record(s)] da nodo: " + node.getUid();
		logger.debug("[EcmEngineBackofficeBean::addAcl] Parametri -" +
				" ACLs: [ " + acls + " - " + acls.length + " record(s)] N: " + node.getUid() +
				" U: " + context.getUsername());
		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "removeAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineBackofficeBean::removeAcl] Rimozione dei record di ACL dal nodo: " + node.getUid());

			for (int i = 0; i < acls.length; i++) {
				permissionService.deletePermission(nodeRef, acls[i].getAuthority(),
						acls[i].getPermission());
			}
			dumpElapsed("EcmEngineBackofficeBean", "removeAcl", logCtx, "Record ACL rimossi: " + acls.length + " record(s).");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "removeAcl", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "removeAcl", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::removeAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::removeAcl] END");
		}
	}

	public void setInheritsAcl(Node node, boolean inherits, OperationContext context)
	throws NoSuchNodeException, RemoteException, AclEditException, InvalidParameterException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::setInheritsAcl] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Set inherits ACL su nodo: " + node.getUid();
		logger.debug("[EcmEngineBackofficeBean::setInheritsAcl] Parametri -" +
				" N: " + node.getUid() +
				" I: " + inherits +
				" U: " + context.getUsername());

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "setInheritsAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			dumpElapsed("EcmEngineBackofficeBean", "setInheritsAcl", logCtx, "Begin transazione.");
			logger.debug("[EcmEngineBackofficeBean::setInheritsAcl] Impostazione del flag per il nodo: " + node.getUid());

			permissionService.setInheritParentPermissions(nodeRef, inherits);

			dumpElapsed("EcmEngineBackofficeBean", "setInheritsAcl", logCtx, "Flag settato: " + inherits);

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "setInheritsAcl", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "setInheritsAcl", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::setInheritsAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::setInheritsAcl] END");
		}
	}

	public void updateAcl(Node node, AclRecord[] acls, OperationContext context)
	throws AclEditException, NoSuchNodeException, RemoteException, InvalidParameterException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		logger.debug("[EcmEngineBackofficeBean::updateAcl] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NOT_NULL, "acls", acls);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "ADD ACL [" + acls.length + " record(s)] a nodo: " + node.getUid();
		logger.debug("[EcmEngineBackofficeBean::updateAcl] Parametri -" +
				" ACLs: [ " + acls + " - " + acls.length + " record(s)] N: " + node.getUid() +
				" U: " + context.getUsername());

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "updateAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineBackofficeBean::updateAcl] " +
					"Rimozione vecchi record di ACL dal nodo: " + node.getUid());
			permissionService.deletePermissions(nodeRef);

			logger.debug("[EcmEngineBackofficeBean::updateAcl] " +
					"Associazione dei record di ACL al nodo: " + node.getUid());

			for (int i = 0; i < acls.length; i++) {
				permissionService.setPermission(nodeRef, acls[i].getAuthority(),
						acls[i].getPermission(), acls[i].isAccessAllowed());
			}
			dumpElapsed("EcmEngineBackofficeBean", "updateAcl", logCtx,
					"Record ACL aggiunti: " + acls.length + " record(s).");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "updateAcl", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "updateAcl", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::updateAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::updateAcl] END");
		}
	}

	public void resetAcl(Node node, AclRecord filter, OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {

		logger.debug("[EcmEngineBackofficeBean::resetAcl] BEGIN");

		// filter puo` essere null, in modo da eliminare
		// tutte le ACL associate al nodo specificato.
		validate(ValidationType.NODE, "node", node);
		if (filter != null && filter.getPermission() != null) {
			// se e` specificata la permission allora deve essere specificata anche l'authority
			validate(ValidationType.NAME, "filter.authority", filter.getAuthority());
			validate(ValidationType.NAME, "filter.permission", filter.getPermission());
		}
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Reset ACL";

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "resetAcl", logCtx, "Autenticazione completata.");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			if (filter != null) {
				// Workaround (LB - 18/01/2008)
				// Secondo il javadoc di Alfresco 2.1 il metodo
				// PermissionService.deletePermission(NodeRef nodeRef,
				//                 String authority, String permission)
				// accetta null per i parametri authority e permission.
				// A tutti gli effetti vengono invece generati dei
				// NullPointerException.
				// Si e` deciso quindi di differenziare le chiamate in
				// base al valore dei parametri.
				if (isValidName(filter.getAuthority()) && !isValidName(filter.getPermission())) {
					// Specificando solo il campo authority
					permissionService.clearPermission(nodeRef, filter.getAuthority());
				} else if (isValidName(filter.getAuthority()) && isValidName(filter.getPermission())) {
					// Specificando entrambi i campi
					permissionService.deletePermission(nodeRef, filter.getAuthority(), filter.getPermission());
				} else {
					// Non specificando alcun campo
					permissionService.deletePermissions(nodeRef);
				}
			} else {
				permissionService.deletePermissions(nodeRef);
			}
			dumpElapsed("EcmEngineBackofficeBean", "resetAcl", logCtx, "Reset ACL completato");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "resetAcl", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "resetAcl", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::resetAcl] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AclEditException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::resetAcl] END");
		}
	}


	public IntegrityReport[] checkRepositoryIntegrity(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, TooManyNodesException, InvalidCredentialsException,EcmEngineTransactionException, PermissionDeniedException,EcmEngineException,RemoteException {
		logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] BEGIN");
		logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Work on repository: " + context.getRepository()+" user:"+context.getUsername());
		List<IntegrityReport> risposta=new ArrayList<IntegrityReport>();
		Map<String,Node> allNodes=null;
		start(); // Avvia stopwatch
		//UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();
		try {
		//UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

			/* 4C BEGIN */
			SearchParams xpathSearchParams=new SearchParams();
			xpathSearchParams.setXPathQuery(getPaths(node, context)[0].getPath()+"//*");
			logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Query: "+xpathSearchParams.getXPathQuery());
			Node[] nodeResponse=xpathSearchNoMetadata(xpathSearchParams, context).getNodeArray();
			if(nodeResponse==null|| nodeResponse.length==0){
				//rollbackQuietely(transaction);
				throw new NoSuchNodeException("Nessun figlio per il nodo indicato.");
			}
			//System.out.println("Trovati "+nodeResponse.length+" nodi:");
			allNodes = integrityService.getAllNodes();
			boolean reindex=false;
			for(int i=0;i<nodeResponse.length;i++){
				//System.out.println(nodeResponse[i].getUid());
				if(allNodes.get(nodeResponse[i].getUid())==null){
					logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Nodo non piu' esistente "+nodeResponse[i].getUid());
					reindex=true;
					IntegrityReport report=new IntegrityReport();
					report.setMessage(IntegrityMessage.DELETED_NODE);
					report.setData(new String[]{nodeResponse[i].getUid()});
					risposta.add(report);
				}
			}
			if(reindex==true){
				//TODO: Correzione dal nodo passato come parametro. Pensare a deventuali altre alternative.
			}
			/* 4C END */


			/* 4E BEGIN */
			/*Long dbidnodo=integrityService.getDBID(node);
			System.out.println("*****DBID: "+dbidnodo);*/
			logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Creo la mappa delle associazioni dal db.");
			Map<Long,Set<Long>>associazioni=integrityService.getAllAssociations();
			Map<String,Set<String>>associazioniNodi=new HashMap<String,Set<String>>();
			Map<Long,String>dbiduid=integrityService.getAllDBIDUID();
			for(Long set:associazioni.keySet()){
				//System.out.println(set+":\n");
				//TODO:Verificare se bisogna contorllare il diverso da null e se si, se bisogna gestirlo come incongruenza.
				if(dbiduid.get(set)!=null){
					associazioniNodi.put(dbiduid.get(set), new HashSet<String>());
					for(Long set2:associazioni.get(set)){
						//System.out.print(set2+"");
						if(dbiduid.get(set2)!=null){
							associazioniNodi.get(dbiduid.get(set)).add(dbiduid.get(set2));
						}
					}
					//System.out.print("\n\n");
				}
			}
			logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Mappa delle associazioni dal db creata.");
			xpathSearchParams.setXPathQuery(getPaths(node, context)[0].getPath()+"//*");
			logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Cerco tutte le associazioni:\n"+xpathSearchParams.getXPathQuery());
			nodeResponse=xpathSearchNoMetadata(xpathSearchParams, context).getNodeArray();
			for(int i=-1;i<nodeResponse.length;i++){
				Node n=null;
				if(i==-1){
					n=node;
				}else{
					n=nodeResponse[i];
				}
				try{
					String tempPath=getPaths(n, context)[0].getPath();
					SearchParams tempSearchParam=new SearchParams();
					tempSearchParam.setXPathQuery(tempPath+"/*");
					Node[] tempResult=xpathSearchNoMetadata(tempSearchParam, context).getNodeArray();
					Set<String> tempSet=associazioniNodi.get(n.getUid());
					for(int j=0;j<tempResult.length;j++){
						if(!tempSet.contains(tempResult[j].getUid())){
							logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Errore nell'associazione xpath:\n"+n.getUid()+" -> "+tempResult[j].getUid());
							IntegrityReport report=new IntegrityReport();
							report.setMessage(IntegrityMessage.DELETED_ASSOC);
							report.setData(new String[]{n.getUid(),tempResult[j].getUid()});
							risposta.add(report);
						}
					}
				}catch(NoSuchNodeException e){
					logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] Nodo non trovato: "+n.getUid());
				}finally{
					logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity]"+n.getUid()+" controllato.\t["+(i+2)+"/"+(nodeResponse.length+1)+"]");
				}
			}
			/*for(String set:associazioniNodi.keySet()){
				//System.out.println(set+":\n");
				Node tempNode=new Node();
				tempNode.setUid(set);
				System.out.println("Cerco path per il nodo: "+set);
				String tempPath=getPaths(tempNode, context)[0].getPath();
				SearchParams tempSearchParam=new SearchParams();
				tempSearchParam.setXPathQuery(tempPath+"/*");
				System.out.println("Path di ricerca: "+tempSearchParam.getXPathQuery());
				Node[] tempResult=xpathSearchNoMetadata(tempSearchParam, context).getNodeArray();
				for(String set2:associazioniNodi.get(set)){
					boolean trovato=false;
					for(int i=0;i<tempResult.length&&trovato==false;i++){
						if(tempResult[i].getUid().equals(set2)){
							trovato=true;
						}
					}
					if(trovato==false){
						System.out.println("Trovata incongruenza nella ricerca xpath tra:\n"+set+" -> "+set2);
					}
				}
				System.out.print(set+" OK");
			}*/

			/* 4E END */
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "importDataArchive", context.getUsername(), null);
			checkAccessException(e, "EcmEngineBackofficeBean", "importDataArchive", "User: " + context.getUsername(), null);
			logger.error("[EcmEngineBackofficeBean::importDataArchive] Foundation services error: " + e.getCode());
			//rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		/*} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());*/
		/*} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");*/
		}finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineBackofficeBean::checkRepositoryIntegrity] END");
		}
		return risposta.toArray(new IntegrityReport[risposta.size()]);
	}

    public void importDataArchive(DataArchive data, Node parent, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException,
    EcmEngineTransactionException, EcmEngineException, PermissionDeniedException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::importDataArchive] BEGIN");

		validate(ValidationType.NODE                , "parent"      , parent);
		validate(ValidationType.DATA_ARCHIVE        , "data"        , data);
		validate(ValidationType.ARCHIVE_FORMAT      , "data.format" , data.getFormat());
		validate(ValidationType.OPERATION_CONTEXT   , "context"     , context);
        // Occorre creare un finto nodo per validare le proprieta' del data archive
        Content content = new Content();
        content.setPrefixedName(                "cm:foobar"                                     );
        content.setParentAssocTypePrefixedName( data.getParentContainerAssocTypePrefixedName()  );
        content.setModelPrefixedName(           data.getMappedContainerAssocTypePrefixedName()  );
        content.setTypePrefixedName(            data.getMappedContentTypePrefixedName()         );
        content.setContentPropertyPrefixedName( data.getMappedContentNamePropertyPrefixedName() );
        content.setMimeType(                    "application/octet-stream"                      );
        content.setEncoding(                    "UTF-8"                                         );
        content.setContent(                     new String("foobar").getBytes()                 );
		validate(ValidationType.CONTENT_WRITE_NEW   , "content"     , content);
		// TODO: Add max file size validation

		final String logCtx = "Get system properties";
		logger.debug("[EcmEngineBackofficeBean::importDataArchive] Parametri -" +
				" N: " + parent.getUid() +
				" U: " + context.getUsername());

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "importDataArchive", logCtx, "Autenticazione completata.");

			transaction.begin();

			ArchiveImporter importer = new ArchiveImporter( jobService );
			NodeRef nodeRef = checkNodeExists(parent, transaction);

			try {
				importer.importArchive( data, nodeRef, context );
			} catch (ContentIOException e) {
				logger.error("[EcmEngineBackofficeBean::importDataArchive] Errore durante l'import: " + e.getMessage(), e);
				rollbackQuietely(transaction);
				throw new EcmEngineException("Errore durante l'import: " + e.getMessage());
			}

			dumpElapsed("EcmEngineBackofficeBean", "importDataArchive", logCtx, "Importazione completata.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "importDataArchive", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineBackofficeBean", "importDataArchive", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::importDataArchive] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::importDataArchive] END");
		}
	}

	public SystemProperty[] getSystemProperties(OperationContext context)
	throws InvalidParameterException, EcmEngineException, InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::getSystemProperties] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Get system properties";
		logger.debug("[EcmEngineBackofficeBean::getSystemProperties] Parametri -" +
				" U: " + context.getUsername());

		Set<SystemProperty> results = new HashSet<SystemProperty>();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::getSystemProperties] Autenticazione - U: " + context.getUsername());

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "getSystemProperties", logCtx, "Autenticazione completata.");

			Properties systemProperties = System.getProperties();
			SystemPropertyFilters filteredSystemProperties = SystemPropertyFilters.getInstance();
			// Add all properties that are not contained in filteredSystemProperties.
			for (Object property : systemProperties.keySet()) {
				if (!filteredSystemProperties.contains(""+property)) {
					results.add(new SystemProperty((String)property, (String)systemProperties.get(property)));
				}
			}
			dumpElapsed("EcmEngineBackofficeBean", "getSystemProperties", logCtx, "Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getSystemProperties", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getSystemProperties] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getSystemProperties] END");
		}
		return results.toArray(new SystemProperty[]{});
	}

	public Repository[] getRepositories(OperationContext context)
	throws InvalidParameterException, EcmEngineException, InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::getRepositories] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Elenco repository";
		logger.debug("[EcmEngineBackofficeBean::getRepositories] Parametri -" +
				" U: " + context.getUsername());

		List<Repository> results = new Vector<Repository>();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::getRepositories] Autenticazione - U: " + context.getUsername());

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "getRepositories", logCtx, "Autenticazione completata.");

			List<it.doqui.index.ecmengine.business.personalization.multirepository.Repository> repositories = getKnownRepositories();
			for (it.doqui.index.ecmengine.business.personalization.multirepository.Repository repo : repositories) {
				Repository newRep = new Repository( repo.getId() );

				// MB: prendo da Alfresco TCS a Doqui TCS DTO
				ContentStoreDefinition []vatcs = null;
				List<it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition> vtcs = repo.getContentStores();
				if( vtcs!=null ){
					vatcs = new ContentStoreDefinition[vtcs.size()];
					int n=0;
					for (it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition atcs: vtcs) {
						vatcs[n] = new ContentStoreDefinition();
						vatcs[n].setType(     atcs.getType()     );
						vatcs[n].setProtocol( atcs.getProtocol() );
						vatcs[n].setResource( atcs.getResource() );
						n++;
					}
				}
				newRep.setContentStores( vatcs );
				results.add( newRep );
			}
			dumpElapsed("EcmEngineBackofficeBean", "getRepositories", logCtx, "Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getRepositories", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getRepositories] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getRepositories] END");
		}
		return results.toArray(new Repository[]{});
	}

	public ModelDescriptor[] getAllModelDescriptors(OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, EcmEngineException, InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::getAllModelDescriptors] BEGIN");

		final String logCtx = "Lista metadati data model";

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineBackofficeBean::getAllModelDescriptors] Parametri -" +
				" U: " + context.getUsername());

		ModelDescriptor[] result = null;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			logger.debug("[EcmEngineBackofficeBean::getAllModelDescriptors] Autenticazione - U: " + context.getUsername());

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "getAllModelDescriptors", logCtx, "Autenticazione completata.");

			transaction.begin();

			QName[] modelQNames = dictionaryService.getAllModels();
			result = new ModelDescriptor[modelQNames.length];
			for (int i=0; i<modelQNames.length; i++) {
				result[i] = new ModelDescriptor();
				result[i].setPrefixedName(dictionaryService.resolveQNameToPrefixName(modelQNames[i]));
				ModelDefinition modelDefinition = dictionaryService.getModelByName(modelQNames[i]);
				result[i].setDescription(modelDefinition.getDescription());
			}

			dumpElapsed("EcmEngineBackofficeBean", "getAllModelDescriptors", logCtx, "Operazione completata");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getAllModelDescriptors", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::getAllModelDescriptors] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getAllModelDescriptors] END");
		}
		return result;
	}

	public TypeMetadata getTypeDefinition(ModelDescriptor typeDescriptor, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException,
	InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::getTypeDefinition] BEGIN");

		final String logCtx = "METADATI TIPO: " + typeDescriptor.getPrefixedName();

		validate(ValidationType.NOT_NULL, "typeDescriptor", typeDescriptor);
		validate(ValidationType.PREFIXED_NAME, "typeDescriptor.prefixedName", typeDescriptor.getPrefixedName());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineBackofficeBean::getTypeDefinition] Parametri -" +
				" TD: " + typeDescriptor.getPrefixedName() +
				" U: " + context.getUsername());

		TypeMetadata result = null;

		start();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "getTypeDefinition", logCtx, "Autenticazione completata.");

			final QName typeQName = dictionaryService.resolvePrefixNameToQName(typeDescriptor.getPrefixedName());
			if (typeQName == null) {
				throw new NoDataExtractedException("Type definition not found for type: " + typeDescriptor.getPrefixedName());
			}

			TypeDefinition typeDefinition = dictionaryService.getType(typeQName);
			if (typeDefinition == null) {
				throw new NoDataExtractedException("Type metadata not found for type: " + typeDescriptor.getPrefixedName());
			}

			result = translateTypeDefinition(typeDefinition);

			dumpElapsed("EcmEngineBackofficeBean", "getTypeDefinition", logCtx, "Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getTypeDefinition", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getTypeDefinition] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getTypeDefinition] END");
		}
		return result;
	}

	public ModelMetadata getModelDefinition(ModelDescriptor modelDescriptor, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException,
	InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::getModelDefinition] BEGIN");

		final String logCtx = "METADATI MODEL: " + modelDescriptor.getPrefixedName();

		validate(ValidationType.NOT_NULL, "modelDescriptor", modelDescriptor);
		validate(ValidationType.PREFIXED_NAME, "modelDescriptor.prefixedName", modelDescriptor.getPrefixedName());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineBackofficeBean::getModelDefinition] Parametri -" +
				" MD: " + modelDescriptor.getPrefixedName() +
				" U: " + context.getUsername());

		ModelMetadata result = null;

		start();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "getModelDefinition", logCtx, "Autenticazione completata.");

			QName modelQName = dictionaryService.resolvePrefixNameToQName(modelDescriptor.getPrefixedName());
			ModelDefinition modelDefinition = dictionaryService.getModelByName(modelQName);
			if (modelDefinition == null) {
				throw new NoDataExtractedException("Model metadata not found for model: " + modelDescriptor.getPrefixedName());
			}

			result = new ModelMetadata();
			result.setPrefixedName(modelDescriptor.getPrefixedName());
			result.setDescription(modelDefinition.getDescription());

			TypeDefinition[] types = dictionaryService.getTypesByModelName(modelQName);
			TypeMetadata[] resultTypes = new TypeMetadata[types.length];
			for (int i = 0; i < resultTypes.length; i++) {
				resultTypes[i] = translateTypeDefinition(types[i]);
			}
			result.setTypes(resultTypes);

			AspectDefinition[] aspects = dictionaryService.getAspectsByModelName(modelQName);
			AspectMetadata[] resultAspects = new AspectMetadata[aspects.length];
			for (int i = 0; i < aspects.length; i++) {
				resultAspects[i] = translateAspectDefinition(aspects[i]);
			}
			result.setAspects(resultAspects);

			dumpElapsed("EcmEngineBackofficeBean", "getModelDefinition", logCtx, "Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getModelDefinition", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getModelDefinition] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getModelDefinition] END");
		}
		return result;
	}

	public Group[] listGroups(Group parentGroup, OperationContext context)
	throws InvalidParameterException, NoSuchGroupException, EcmEngineTransactionException, InvalidCredentialsException,
	EcmEngineException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::listGroups] BEGIN");

		validate(ValidationType.GROUP, "parentGroup", parentGroup);
		validate(ValidationType.GROUP_NOT_EMAIL_CONTRIBUTORS, "parentGroup", parentGroup);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineBackofficeBean::listGroups] Parametri -" +
				" G: " + (parentGroup == null ? "--null--" : parentGroup.getName()) +
				" U: " + context.getUsername());

		final String logCtx = "Elenco gruppi";

		Group[] result = null;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "listGroups", logCtx, "Autenticazione completata.");

			transaction.begin();

			if (isValidName(parentGroup.getName())) {
				// Get groups contained in parentGroup
				final String fullGroupName = authorityService.getName(
						AuthorityType.GROUP, parentGroup.getName());
				if (!authorityService.authorityExists(fullGroupName)) {
					logger.error("[EcmEngineBackofficeBean::listGroups] ERROR: Il gruppo specificato non esiste: " + parentGroup.getName());
					rollbackQuietely(transaction);
					throw new NoSuchGroupException(parentGroup.getName());
				}
				Set<String> groupSet = authorityService.getContainedAuthorities(AuthorityType.GROUP, fullGroupName, true);
				//AF: Rimuovo dai risultati il gruppo di Alfresco EMAIL_CONTRIBUTORS, in quanto generava problemi perchè condiviso dai tenant con il repository logico.
				if(groupSet.contains("GROUP_EMAIL_CONTRIBUTORS")){
					groupSet.remove("GROUP_EMAIL_CONTRIBUTORS");
				}
				result = new Group[groupSet.size()];
				String[] groups = groupSet.toArray(new String[]{});
				for (int i=0; i<groups.length; i++) {
					result[i] = new Group();
					result[i].setName(authorityService.getShortName(groups[i]));
				}
			} else {
				// Get root groups
				Set<String> groupSet = authorityService.getAllRootAuthorities(AuthorityType.GROUP);
				//AF: Rimuovo dai risultati il gruppo di Alfresco EMAIL_CONTRIBUTORS, in quanto generava problemi perchè condiviso dai tenant con il repository logico.
				if(groupSet.contains("GROUP_EMAIL_CONTRIBUTORS")){
					groupSet.remove("GROUP_EMAIL_CONTRIBUTORS");
				}
				result = new Group[groupSet.size()];
				String[] groups = groupSet.toArray(new String[]{});
				for (int i=0; i<groups.length; i++) {
					result[i] = new Group();
					result[i].setName(authorityService.getShortName(groups[i]));
				}
			}

			dumpElapsed("EcmEngineBackofficeBean", "listGroups", logCtx, "Letti gruppi");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "listGroups", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::listGroups] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::listGroups] END");
		}
		return result;
	}

	public Group[] listAllGroups(Group filter, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, EcmEngineException,
	NoDataExtractedException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::listAllGroups] BEGIN");

		validate(ValidationType.GROUP, "filter", filter);
		validate(ValidationType.GROUP_NOT_EMAIL_CONTRIBUTORS, "filter", filter);
		validate(ValidationType.NAME, "filter.name", filter.getName());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineBackofficeBean::listAllGroups] Parametri - U: " + context.getUsername());

		final String logCtx = "Elenco completo gruppi";

		Group[] result = null;

		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			//final String repository = context.getRepository();
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineBackofficeBean", "listAllGroups", logCtx, "Autenticazione completata.");

			transaction.begin();

			boolean isFiltered = !filter.getName().equalsIgnoreCase("*");
			String filterRegex = null;

			if (isFiltered) {
				filterRegex = filter.getName().replaceAll("\\*", ".*");
				logger.debug("[EcmEngineBackofficeBean::listAllGroups] Filter regex: " + filterRegex);
			}

			Set<String> groupSet = authorityService.getAllAuthorities(AuthorityType.GROUP);
			//AF: Rimuovo dai risultati il gruppo di Alfresco EMAIL_CONTRIBUTORS, in quanto generava problemi perchè condiviso dai tenant con il repository logico.
			if(groupSet.contains("GROUP_EMAIL_CONTRIBUTORS")){
				groupSet.remove("GROUP_EMAIL_CONTRIBUTORS");
			}

			List<Group> matchingGroups = new ArrayList<Group>(groupSet.size()); // Preallochiamo alla dimensione massima

			for (String groupName : groupSet) {
				final String shortName = authorityService.getShortName(groupName);

				if (!isFiltered || shortName.matches(filterRegex)) {
					Group group = new Group();
					group.setName(shortName);

					matchingGroups.add(group);
				}
			}

			logger.debug("[EcmEngineBackofficeBean::listAllGroups] Matching groups found: " + matchingGroups.size() +
					" [Total: " + groupSet.size() + "]");

			if (matchingGroups.isEmpty()) {
				transaction.commit();	// Nessun risultato ma la ricerca e` comunque andata a buon fine
				throw new NoDataExtractedException(filter.getName(), context.getRepository());
			}

			result = matchingGroups.toArray(new Group [] {});

			dumpElapsed("EcmEngineBackofficeBean", "listAllGroups", logCtx, "Operazione completata");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "listAllGroups", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::listAllGroups] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::listAllGroups] END");
		}
		return result;
	}

	public void createTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::createTenant] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context"             , context);
		validate(ValidationType.TENANT           , "tenant"              , tenant);
		validate(ValidationType.NAME             , "tenant.adminPassword", tenant.getAdminPassword());

        if( !tenant.isEnabled() ) {
            throw new InvalidParameterException("Non e' possibile creare tenant in stato DISABLE" );
    	}

        if( tenant.getContentStores()!=null )
        {
            for( ContentStoreDefinition tcs : tenant.getContentStores() )
            {
               if( tcs.getType()==null     || tcs.getType().length()==0 ){
      			   throw new InvalidParameterException("La ContentStoreDefinition non contiene un type "     +tcs.getType()     );
               }
               if( tcs.getProtocol()==null || tcs.getProtocol().length()==0 ){
      			   throw new InvalidParameterException("La ContentStoreDefinition non contiene un protocol " +tcs.getProtocol() );
               }
               if( tcs.getResource()==null || tcs.getResource().length()==0 ){
      			   throw new InvalidParameterException("La ContentStoreDefinition non contiene un resource " +tcs.getResource() );
               }

				// Istanzia il nuovo content store passando dal manager, se il CS e' null, evito di creare il tenant che
				// Altrimenti avrebbe una inconsistenza
				it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition csd = null;
				csd = new it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition();
				csd.setProtocol( tcs.getProtocol() );
				csd.setResource( tcs.getResource() );
				csd.setType(     tcs.getType() 	  );
				try {
					ContentStoreDynamic cs = ContentStoreManager.getInstance().getContentStore( csd );
				} catch (Exception e) {
					// Qualsiasi errore genera la creazione del contentStoreDynamic, genero una eccezione di invalid parameter
					throw new InvalidParameterException("ContentStoreDefinition non associabile a un ContentStore " +csd );
				}
			}
		}

        //MB: ora prendo il percorso impostato per questo tenant
        //    e verifico che non sia utilizzato all'interno di un repository diverso quello nel quale e' stato
        //    chiesto di creare il tenant: il senso e' quello di non usare lo stesso path su reposytory diversi.
        //
        //TODO: verificare la consistenza anche relativamente alle contentStoreDefinition
        //      occorre verificare che tutte le csd del tenant da creare, non vadano a sovrapporsi con le csd
        //      di un qualsiasi altro repository/tenant
        // MASTER: EcmEngineBackofficeBean.java
        String cRepID = RepositoryManager.getCurrentRepository();
		try {
            // Creo il path nel quale andranno i dati
            String cPath = tenant.getRootContentStoreDir();

            // Se e' presente un percorso, provo a vedere se esiste un tenant, in un altro
            // repository, con lo stesso path
            if( cPath!=null && cPath.length()>0 ) {
               List<it.doqui.index.ecmengine.business.personalization.multirepository.Repository> repositories = RepositoryManager.getInstance().getRepositories();
               for (it.doqui.index.ecmengine.business.personalization.multirepository.Repository repository : repositories) {
                   logger.debug("[EcmEngineBackofficeBean::createTenant] check tenants on repository '"+repository.getId()+"'");

                   // Salto il repository corrente
                   if( !context.getRepository().equals(repository.getId()) ){
                       // Imposto il nuovo repository
                       RepositoryManager.setCurrentRepository(repository.getId());

                       // Mi loggo come utente di sistema
			       	   AuthenticationUtil.setSystemUserAsCurrentUser();

                       // Prendo i tenant
                       List<it.doqui.index.ecmengine.business.personalization.multirepository.Tenant> tenantList = tenantAdminService.getAllTenants();
                       for (it.doqui.index.ecmengine.business.personalization.multirepository.Tenant tenantTarget : tenantList) {
                           // Prendo il path nel quale andranno i dati
                           String cPathTarget = tenantTarget.getRootContentStoreDir();
                           if( cPathTarget!=null && cPathTarget.length()>0 ) {
                              if( cPath.equals(cPathTarget) ){
                                 throw new InvalidParameterException("Utilizzo illegale del path (" +cPath +") in quanto gia' utilizzato dal tenant (" +tenantTarget.getTenantDomain() +") del repository (" +repository.getId() +")");
                              }
                           }
                       }
                   }
               }
            }
		} catch (InvalidParameterException ipe) {
            // Rimbalzo l'eccezione
            throw ipe;
        } catch (Exception e) {
            // Qualsiasi errore genera la creazione di un invalidParameter
            throw new InvalidParameterException("Impossibile determinare l'univocita' del path del tenant");
        } finally {
            RepositoryManager.setCurrentRepository(cRepID);
        }
        //--------------------------------------------------------------------------------------------------

		final String logCtx = "Creazione tenant";
		logger.debug("[EcmEngineBackofficeBean::createTenant] Parametri -" +
				" U: " + context.getUsername() +
				" T: " + tenant.getDomain());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::createTenant] Autenticazione -" +
					" U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "createTenant", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::createTenant] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

            // Se il tenant e' attivo
			if( tenantAdminService.isEnabledTenant( tenant.getDomain() ) ){
				throw new EcmEngineException("Tenant (" +tenant.getDomain() +") gia' attivo su repository (" +RepositoryManager.getCurrentRepository() +")");
			}

			transaction.begin();

			try {
				// TODO Rivedere gestione eccezioni!!!
				jobService.createJob(TenantAdminJob.createBatchJob(tenant));
			} catch(Exception ee) {
				// Non serve il rollback, perche' si passa da EcmEngineFoundationException che fa rollback
				if (ee instanceof JobRuntimeException) {
					throw (JobRuntimeException)ee;
				} else {
					throw new JobRuntimeException(FoundationErrorCodes.GENERIC_JOB_SERVICE_ERROR);
				}
			}
			dumpElapsed("EcmEngineBackofficeBean", "createTenant", logCtx, "Operazione completata");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "createTenant", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::createTenant] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::createTenant] END");
		}

	}

	public void enableTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::enableTenant] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.TENANT, "tenant", tenant);

		final String logCtx = "Abilitazione tenant";
		logger.debug("[EcmEngineBackofficeBean::enableTenant] Parametri -" +
				" U: " + context.getUsername() +
				" T: " + tenant.getDomain());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::enableTenant] Autenticazione -" +
					" U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "enableTenant", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::enableTenant] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

            // Se il tenant e' attivo
			if( tenantAdminService.isEnabledTenant( tenant.getDomain() ) ){
				throw new EcmEngineException("Tenant (" +tenant.getDomain() +") gia' attivo su repository (" +RepositoryManager.getCurrentRepository() +")");
			}

			transaction.begin();

			tenantAdminService.enableTenant(tenant.getDomain());
			dumpElapsed("EcmEngineBackofficeBean", "enableTenant", logCtx, "Operazione completata");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "enableTenant", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::enableTenant] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::enableTenant] END");
		}

	}

	public void disableTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::disableTenant] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.TENANT, "tenant", tenant);

		final String logCtx = "Disabilitazione tenant";
		logger.debug("[EcmEngineBackofficeBean::disableTenant] Parametri -" +
				" U: " + context.getUsername() +
				" T: " + tenant.getDomain());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::disableTenant] Autenticazione -" +
					" U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "disableTenant", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::disableTenant] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

            // Se il tenant non e' attivo
			if( !tenantAdminService.isEnabledTenant( tenant.getDomain() ) ){
				throw new EcmEngineException("Tenant (" +tenant.getDomain() +") gia' disattivo su repository (" +RepositoryManager.getCurrentRepository() +")");
			}

			transaction.begin();

			tenantAdminService.disableTenant(tenant.getDomain());
			dumpElapsed("EcmEngineBackofficeBean", "disableTenant", logCtx, "Operazione completata");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "disableTenant", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::disableTenant] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::disableTenant] END");
		}

	}

	public Tenant[] getAllTenants(OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::getAllTenants] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Lista tenant";
		logger.debug("[EcmEngineBackofficeBean::getAllTenants] Parametri -" +
				" U: " + context.getUsername() +
				" R: " + context.getRepository() );

		Tenant[] result = null;

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::getAllTenants] Autenticazione -" +
					" U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "getAllTenants", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::getAllTenants] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}


			final List<it.doqui.index.ecmengine.business.personalization.multirepository.Tenant> tenantList = tenantAdminService.getAllTenants();
			if (tenantList != null && tenantList.size() > 0) {
				result = new Tenant[tenantList.size()];
				for (int i=0; i<tenantList.size(); i++) {
					//dumpElapsed("EcmEngineBackofficeBean", "getAllTenants", logCtx, "Tenant: "+tenantList.get(i).getTenantDomain());
					result[i] = createDoquiTenant( tenantList.get(i) );
				}
			} else {
				throw new NoDataExtractedException("No tenants found.");
			}
			dumpElapsed("EcmEngineBackofficeBean", "getAllTenants", logCtx, "Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getAllTenants", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getAllTenants] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getAllTenants] END");
		}
		return result;

	}

	public Tenant getTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException,
	EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::getTenant] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.TENANT, "tenant", tenant);

		final String logCtx = "Lettura tenant";
		logger.debug("[EcmEngineBackofficeBean::getTenant] Parametri -"
				+ " U: " + context.getUsername());

		Tenant result = null;

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::getTenant] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "getTenant", logCtx,
			"Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::getTenant] Permission denied for user "
						+ context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}


			it.doqui.index.ecmengine.business.personalization.multirepository.Tenant resultTenant = tenantAdminService.getTenant(tenant.getDomain());
			if (resultTenant != null) {
				result = createDoquiTenant( resultTenant );
			} else {
				throw new NoDataExtractedException("No tenant found.");
			}
			dumpElapsed("EcmEngineBackofficeBean", "getTenant", logCtx,
			"Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "getTenant", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getTenant] Foundation services error: "
					+ e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: "
					+ e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getTenant] END");
		}
		return result;

	}

	public boolean tenantExists(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::tenantExists] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.TENANT, "tenant", tenant);

		final String logCtx = "Lettura tenant";
		logger.debug("[EcmEngineBackofficeBean::tenantExists] Parametri -"
				+ " U: " + context.getUsername());

		boolean result = false;

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::tenantExists] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "tenantExists", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::tenantExists] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

			transaction.begin();
			result = tenantAdminService.existsTenant(tenant.getDomain());
			dumpElapsed("EcmEngineBackofficeBean", "tenantExists", logCtx,	"Operazione completata");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "tenantExists", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::tenantExists] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::tenantExists] END");
		}
		return result;

	}

	public void tenantDelete(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {
		logger.debug("[EcmEngineBackofficeBean::tenantDelete] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.TENANT           , "tenant" , tenant );

		final String logCtx = "Lettura tenant";
		logger.debug("[EcmEngineBackofficeBean::tenantDelete] Parametri - U: " + context.getUsername());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::tenantDelete] Autenticazione - U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "tenantDelete", logCtx, "Autenticazione completata.");

			// Solo gli utenti ADMIN possono gestire i tenant
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineBackofficeBean::tenantDelete] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

			if(!tenantAdminService.existsTenant(tenant.getDomain())){
				logger.warn("[EcmEngineBackofficeBean::tenantDelete] Permission denied for user " + context.getUsername());
				throw new InvalidParameterException("Tenant "+tenant.getDomain()+" does not exist.");
			}

			transaction.begin();

			try {
				// TODO Rivedere gestione eccezioni!!!
				jobService.createJob(TenantDeleteJob.createBatchJob(tenant));
			} catch(Exception ee) {
				// Non serve il rollback, perche' si passa da EcmEngineFoundationException che fa rollback
				if (ee instanceof JobRuntimeException) {
					throw (JobRuntimeException)ee;
				} else {
					throw new JobRuntimeException(FoundationErrorCodes.GENERIC_JOB_SERVICE_ERROR);
				}
			}
			dumpElapsed("EcmEngineBackofficeBean", "tenantDelete", logCtx, "Operazione completata");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean",	"tenantDelete", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::tenantDelete] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::tenantDelete] END");
		}
	}

	public CustomModel[] getAllCustomModels(OperationContext context) throws InvalidParameterException,
	EcmEngineTransactionException, NoDataExtractedException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::getAllCustomModels] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Elenco custom model";
		logger.debug("[EcmEngineBackofficeBean::getAllCustomModels] Parametri -"
				+ " U: " + context.getUsername()
				+ " R: " + context.getRepository());

		CustomModel[] result = null;

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::getAllCustomModels] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "getAllCustomModels", logCtx, "Autenticazione completata.");

			List<RepoModelDefinition> models = repoAdminService.getModels();
			RepoModelDefinition model = null;
			if (models != null && models.size() > 0) {
				result = new CustomModel[models.size()];
				for (int i=0; i<models.size(); i++) {
					model = models.get(i);
					result[i] = new CustomModel();
					result[i].setFilename(model.getRepoName());
					if (model.getModel() != null) {
						result[i].setPrefixedName(dictionaryService.resolveQNameToPrefixName(model.getModel().getName()));
						result[i].setDescription(model.getModel().getDescription());
						result[i].setTitle(model.getModel().getDescription());
						result[i].setActive(true);
					} else {
						result[i].setActive(false);
					}
				}
			} else {
				throw new NoDataExtractedException("No custom models found.");
			}

			dumpElapsed("EcmEngineBackofficeBean", "getAllCustomModels", logCtx,
			"Operazione completata");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean",
					"getAllCustomModels", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::getAllCustomModels] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::getAllCustomModels] END");
		}
		return result;

	}

	public void deployCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::deployCustomModel] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.CUSTOM_MODEL, "model", model);
		validate(ValidationType.BYTE_ARRAY, "model.data", model.getData());

		final String logCtx = "Deploy custom model";
		logger.debug("[EcmEngineBackofficeBean::deployCustomModel] Parametri -"
				+ " U: " + context.getUsername()
				+ " CM: " + model.getFilename());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::deployCustomModel] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "deployCustomModel", logCtx, "Autenticazione completata.");

			transaction.begin();

			ByteArrayInputStream modelStream = new ByteArrayInputStream(model.getData());
			repoAdminService.deployModel(modelStream, model.getFilename());
            //modelStream.close(); // Non serve e' un bytestream

			dumpElapsed("EcmEngineBackofficeBean", "deployCustomModel", logCtx,	"Operazione completata");

			//transaction.commit();

            // Se viene chiesto il disable del content model, lo disabilito subito
            // Occorre fare l'operazione in una seconda transazione, dato che, se il model non e' ancora scritto
            // sul repository, non va a buon fine il deactivatemodel
            if( !model.isActive() ){
                //transaction = transactionService.getService().getNonPropagatingUserTransaction();
                //transaction.begin();

    			repoAdminService.deactivateModel(model.getFilename());

    			dumpElapsed("EcmEngineBackofficeBean", "deployCustomModel", logCtx,	"Disattivazione completata");

            }

    	    transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkAccessException(e, "EcmEngineBackofficeBean", "deployCustomModel", "Non si dispone dei privilegi per effettuare il deploy di un custom model.", transaction);
			checkCredentialsException(e, "EcmEngineBackofficeBean",
					"deployCustomModel", context.getUsername(), null);

			logger.error("[EcmEngineBackofficeBean::deployCustomModel] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::deployCustomModel] END");
		}

	}

	public void undeployCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, NoDataExtractedException,EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::undeployCustomModel] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.CUSTOM_MODEL, "model", model);

		final String logCtx = "Deploy custom model";
		logger.debug("[EcmEngineBackofficeBean::undeployCustomModel] Parametri -"
				+ " U: " + context.getUsername()
				+ " CM: " + model.getFilename());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::undeployCustomModel] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);
			boolean checkCustomModelExist=false;
			List<RepoModelDefinition> models = repoAdminService.getModels();
			if(models!=null && models.size()>0){
				for(RepoModelDefinition temp:models){
					logger.debug("[EcmEngineBackofficeBean::undeployCustomModel] Trovato custom model: "+temp.getRepoName());
					if(temp.getRepoName().equals(model.getFilename())){
						checkCustomModelExist=true;
					}
				}
			}
			if(checkCustomModelExist==false){
				throw new NoDataExtractedException(model.getFilename());
			}
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineSearchBean::undeployCustomModel] Permission denied for user " + context.getUsername());
				rollbackQuietely(transaction);
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}
			dumpElapsed("EcmEngineBackofficeBean", "undeployCustomModel", logCtx, "Autenticazione completata.");

			transaction.begin();
			repoAdminService.undeployModel(model.getFilename());
			dumpElapsed("EcmEngineBackofficeBean", "undeployCustomModel", logCtx, "Operazione completata");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean",	"undeployCustomModel", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::undeployCustomModel] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
//			Non serve, la NoDataExtractedException e' fatta fuori dalla transazione
//			}catch(NoDataExtractedException e){
//			rollbackQuietely(transaction);
//			throw e;
		}finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::undeployCustomModel] END");
		}

	}

	public void activateCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::activateCustomModel] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.CUSTOM_MODEL, "model", model);

		final String logCtx = "Attivazione custom model";
		logger.debug("[EcmEngineBackofficeBean::activateCustomModel] Parametri -"
				+ " U: " + context.getUsername()
				+ " CM: " + model.getFilename());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::activateCustomModel] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "activateCustomModel", logCtx, "Autenticazione completata.");

			transaction.begin();
			repoAdminService.activateModel(model.getFilename());
			dumpElapsed("EcmEngineBackofficeBean", "activateCustomModel", logCtx, "Operazione completata");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "activateCustomModel", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::activateCustomModel] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::activateCustomModel] END");
		}

	}

	public void deactivateCustomModel(CustomModel model,
			OperationContext context) throws InvalidParameterException,
			EcmEngineTransactionException, PermissionDeniedException,
			InvalidCredentialsException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineBackofficeBean::deactivateCustomModel] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.CUSTOM_MODEL, "model", model);

		final String logCtx = "Disattivazione custom model";
		logger.debug("[EcmEngineBackofficeBean::deactivateCustomModel] Parametri -"
				+ " U: " + context.getUsername()
				+ " CM: " + model.getFilename());

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		start();

		try {
			logger.debug("[EcmEngineBackofficeBean::deactivateCustomModel] Autenticazione -"
					+ " U: " + context.getUsername());

			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineBackofficeBean", "deactivateCustomModel", logCtx, "Autenticazione completata.");

			transaction.begin();
			repoAdminService.deactivateModel(model.getFilename());
			dumpElapsed("EcmEngineBackofficeBean", "deactivateCustomModel", logCtx,	"Operazione completata");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "deactivateCustomModel", context.getUsername(), transaction);

			logger.error("[EcmEngineBackofficeBean::deactivateCustomModel] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::deactivateCustomModel] END");
		}

	}

	public ExportedContent exportTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException{
		logger.debug("[EcmEngineBackofficeBean::exportTenant] BEGIN");
		ExportedContent result=new ExportedContent();
		validate(ValidationType.OPERATION_CONTEXT, "context"             , context);
		validate(ValidationType.TENANT           , "tenant"              , tenant);
		validate(ValidationType.NAME             , "tenant.adminPassword", tenant.getAdminPassword());
		try{
			authenticateOnRepository(context, null);
			start();
			result.setContent(exporterService.export(tenant.getDomain()));
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "exportTenant", context.getUsername(), null);
			logger.error("[EcmEngineBackofficeBean::deactivateCustomModel] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::exportTenant] END");
		}
		return result;
	}

	public void importTenant(ExportedContent content,Tenant dest, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException{
		logger.debug("[EcmEngineBackofficeBean::importTenant] BEGIN");
		validate(ValidationType.OPERATION_CONTEXT, "context"             , context);
		validate(ValidationType.TENANT           , "tenant"              , dest);
		validate(ValidationType.NAME             , "tenant.adminPassword", dest.getAdminPassword());
		try{
			authenticateOnRepository(context, null);
			start();
			importerService.importTenant(content.getContent(), dest.getDomain());
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineBackofficeBean", "exportTenant", context.getUsername(), null);
			logger.error("[EcmEngineBackofficeBean::deactivateCustomModel] Foundation services error: " + e.getCode());
			throw new EcmEngineException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineBackofficeBean::importTenant] END");
		}
	}

	private PropertyMetadata translatePropertyDefinition(PropertyDefinition propertyDefinition) throws EcmEngineFoundationException {
		logger.debug("[EcmEngineBackofficeBean::translatePropertyDefinition] BEGIN");
		PropertyMetadata result = null;
		try {
			result = new PropertyMetadata();
			result.setPrefixedName(dictionaryService.resolveQNameToPrefixName(propertyDefinition.getName()));
			result.setDataType(dictionaryService.resolveQNameToPrefixName(propertyDefinition.getDataType().getName()));
			result.setTitle(propertyDefinition.getTitle());
			result.setMandatory(propertyDefinition.isMandatory());
			result.setMultiValued(propertyDefinition.isMultiValued());
			result.setModifiable(!propertyDefinition.isProtected());

			result.setModelDescriptor(buildModelDescriptor(propertyDefinition.getModel()));
		} finally {
			logger.debug("[EcmEngineBackofficeBean::translatePropertyDefinition] END");
		}
		return result;
	}

	private ModelDescriptor buildModelDescriptor(ModelDefinition modelDef) throws EcmEngineFoundationException {
		logger.debug("[EcmEngineBackofficeBean::buildModelDescriptor] BEGIN");

		try {
			ModelDescriptor desc = new ModelDescriptor();
			desc.setPrefixedName(dictionaryService.resolveQNameToPrefixName(modelDef.getName()));
			desc.setDescription(modelDef.getDescription());

			return desc;
		} finally {
			logger.debug("[EcmEngineBackofficeBean::buildModelDescriptor] END");
		}
	}

	private TypeMetadata translateTypeDefinition(TypeDefinition typeDefinition) throws EcmEngineFoundationException {
		logger.debug("[EcmEngineBackofficeBean::translateTypeDefinition] BEGIN");
		TypeMetadata result = new TypeMetadata();

		Vector<PropertyMetadata> propertyMetadataVector = new Vector<PropertyMetadata>();
		Vector<AspectMetadata> aspectMetadataVector = new Vector<AspectMetadata>();
		Vector<AssociationMetadata> associationMetadataVector = new Vector<AssociationMetadata>();
		Vector<ChildAssociationMetadata> childAssociationMetadataVector = new Vector<ChildAssociationMetadata>();

		try {
			QName typeQName = typeDefinition.getName();
			QName parentQName = typeDefinition.getParentName();
			result.setPrefixedName(dictionaryService.resolveQNameToPrefixName(typeQName));
			result.setTitle(typeDefinition.getTitle());
			result.setDescription(typeDefinition.getDescription());
			result.setModelDescriptor(buildModelDescriptor(typeDefinition.getModel()));

			if (parentQName != null) {
				result.setParentPrefixedName(dictionaryService.resolveQNameToPrefixName(parentQName));
			}

			// Set type properties
			Collection<PropertyDefinition> props = typeDefinition.getProperties().values();

			for (PropertyDefinition prop : props) {
				propertyMetadataVector.add(translatePropertyDefinition(prop));
			}

			// Set type aspects
			Collection<AspectDefinition> aspects = typeDefinition.getDefaultAspects();

			for (AspectDefinition aspect : aspects) {
				aspectMetadataVector.add(translateAspectDefinition(aspect));
			}

			// Set type associations
			Collection<AssociationDefinition> assocs = typeDefinition.getAssociations().values();

			for (AssociationDefinition assoc : assocs) {
				associationMetadataVector.add(translateAssociationDefinition(assoc));
			}

			// Set type child associations
			Collection<ChildAssociationDefinition> childAssocs = typeDefinition.getChildAssociations().values();

			for (ChildAssociationDefinition childAssoc : childAssocs) {
				childAssociationMetadataVector.add(translateChildAssociationDefinition(childAssoc));
			}

			result.setProperties(propertyMetadataVector.isEmpty() ? null : propertyMetadataVector.toArray(
					new PropertyMetadata[] {}));
			result.setAspects(aspectMetadataVector.isEmpty() ? null : aspectMetadataVector.toArray(
					new AspectMetadata[] {}));
			result.setAssociations(associationMetadataVector.isEmpty() ? null : associationMetadataVector.toArray(
					new AssociationMetadata[] {}));
			result.setChildAssociations(childAssociationMetadataVector.isEmpty() ? null : childAssociationMetadataVector.toArray(
					new ChildAssociationMetadata[] {}));
		} finally {
			logger.debug("[EcmEngineBackofficeBean::translateTypeDefinition] END");
		}
		return result;
	}

	private AspectMetadata translateAspectDefinition(AspectDefinition aspectDefinition) throws EcmEngineFoundationException {
		logger.debug("[EcmEngineBackofficeBean::translateAspectDefinition] BEGIN");
		AspectMetadata aspectMetadata = new AspectMetadata();

		Vector<PropertyMetadata> propertyMetadataVector = new Vector<PropertyMetadata>();
		Vector<AssociationMetadata> associationMetadataVector = new Vector<AssociationMetadata>();
		Vector<ChildAssociationMetadata> childAssociationMetadataVector = new Vector<ChildAssociationMetadata>();

		try {
			QName aspectQName = aspectDefinition.getName();
			QName parentQName = aspectDefinition.getParentName();
			aspectMetadata.setPrefixedName(dictionaryService.resolveQNameToPrefixName(aspectQName));
			aspectMetadata.setTitle(aspectDefinition.getTitle());
			aspectMetadata.setDescription(aspectDefinition.getDescription());
			aspectMetadata.setModelDescriptor(buildModelDescriptor(aspectDefinition.getModel()));

			if (parentQName != null) {
				aspectMetadata.setParentPrefixedName(dictionaryService.resolveQNameToPrefixName(parentQName));
			}

			// Set aspect properties
			Collection<PropertyDefinition> props = aspectDefinition.getProperties().values();

			for (PropertyDefinition prop : props) {
				propertyMetadataVector.add(translatePropertyDefinition(prop));
			}

			// Set aspect associations
			Collection<AssociationDefinition> assocs = aspectDefinition.getAssociations().values();

			for (AssociationDefinition assoc : assocs) {
				associationMetadataVector.add(translateAssociationDefinition(assoc));
			}

			// Set aspect child associations
			Collection<ChildAssociationDefinition> childAssocs = aspectDefinition.getChildAssociations().values();

			for (ChildAssociationDefinition childAssoc : childAssocs) {
				childAssociationMetadataVector.add(translateChildAssociationDefinition(childAssoc));
			}

			aspectMetadata.setProperties(propertyMetadataVector.isEmpty() ? null : propertyMetadataVector.toArray(
					new PropertyMetadata[] {}));
			aspectMetadata.setAssociations(associationMetadataVector.isEmpty() ? null : associationMetadataVector.toArray(
					new AssociationMetadata[] {}));
			aspectMetadata.setChildAssociations(childAssociationMetadataVector.isEmpty() ? null : childAssociationMetadataVector.toArray(
					new ChildAssociationMetadata[] {}));
		} finally {
			logger.debug("[EcmEngineBackofficeBean::translateAspectDefinition] END");
		}
		return aspectMetadata;
	}

	private String getTenantUsername(String username, OperationContext context) throws InvalidParameterException {
		String result = username;

		String contextTenant = context.getUsername().indexOf("@") > 0 ?
				context.getUsername().substring(context.getUsername().indexOf("@")) : "";

				if (username.indexOf("@") < 0 && contextTenant.length() > 0) {
					result = username + contextTenant;
				}
				String userTenant = result.indexOf("@") > 0 ?
						result.substring(result.indexOf("@")) : "";

						if (!contextTenant.equals(userTenant)) {
							throw new InvalidParameterException("Cross-tenant operation or invalid use of '@' char detected [U: '"+username+"', OC:'"+context.getUsername()+"']");
						}

						return result;
	}

	private AssociationMetadata translateAssociationDefinition(AssociationDefinition assocDefinition) throws EcmEngineFoundationException {
		logger.debug("[EcmEngineBackofficeBean::translateAssociationDefinition] BEGIN");
		AssociationMetadata assocMetadata = new AssociationMetadata();

		try {
			QName assocQName = assocDefinition.getName();

			assocMetadata.setPrefixedName(dictionaryService.resolveQNameToPrefixName(assocQName));
			assocMetadata.setTitle(assocDefinition.getTitle());
			assocMetadata.setDescription(assocDefinition.getDescription());

		} finally {
			logger.debug("[EcmEngineBackofficeBean::translateAssociationDefinition] END");
		}
		return assocMetadata;
	}

	private ChildAssociationMetadata translateChildAssociationDefinition(ChildAssociationDefinition assocDefinition) throws EcmEngineFoundationException {
		logger.debug("[EcmEngineBackofficeBean::translateAssociationDefinition] BEGIN");
		ChildAssociationMetadata assocMetadata = new ChildAssociationMetadata();

		try {
			QName assocQName = assocDefinition.getName();

			assocMetadata.setPrefixedName(dictionaryService.resolveQNameToPrefixName(assocQName));
			assocMetadata.setTitle(assocDefinition.getTitle());
			assocMetadata.setDescription(assocDefinition.getDescription());

		} finally {
			logger.debug("[EcmEngineBackofficeBean::translateAssociationDefinition] END");
		}
		return assocMetadata;
	}


	private Tenant createDoquiTenant( it.doqui.index.ecmengine.business.personalization.multirepository.Tenant at ) throws EcmEngineFoundationException {
		Tenant t = new Tenant();
		// MB: la password non viene copiata, per evitare di esportarla all'esterno della piattaforma tramite il DTO
		t.setDomain(              at.getTenantDomain()           );
		t.setRootContentStoreDir( at.getRootContentStoreDir()    );
		t.setEnabled(             tenantAdminService.isEnabledTenant( at.getTenantDomain() ));

		// MB: prendo da Alfresco TCS a Doqui TCS DTO
		ContentStoreDefinition []vatcs = null;

		List<it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition> vtcs = at.getContentStores();
		if( vtcs!=null ){
			vatcs = new ContentStoreDefinition[vtcs.size()];
			int n=0;

			for (it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition atcs: vtcs) {
				vatcs[n] = new ContentStoreDefinition();
				vatcs[n].setType(     atcs.getType()     );
				vatcs[n].setProtocol( atcs.getProtocol() );
				vatcs[n].setResource( atcs.getResource() );
				n++;
			}
		}
		t.setContentStores( vatcs );
		return t;
	}
}


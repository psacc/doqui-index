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

package it.doqui.index.ecmengine.business.publishing.management;

import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvc;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.CustomSecretKey;
import it.doqui.index.ecmengine.business.personalization.encryption.exception.EncryptionRuntimeException;
import it.doqui.index.ecmengine.business.personalization.workflow.SimpleWorkflowApproveActionExecuter;
import it.doqui.index.ecmengine.business.personalization.workflow.SimpleWorkflowRejectActionExecuter;
import it.doqui.index.ecmengine.business.publishing.EcmEngineFeatureBean;
import it.doqui.index.ecmengine.business.publishing.util.FileFormatInputStream;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Association;
import it.doqui.index.ecmengine.dto.engine.management.Category;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatVersion;
import it.doqui.index.ecmengine.dto.engine.management.FileInfo;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.management.RenditionDocument;
import it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer;
import it.doqui.index.ecmengine.dto.engine.management.Rule;
import it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow;
import it.doqui.index.ecmengine.dto.engine.management.Version;
import it.doqui.index.ecmengine.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultProperty;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CheckInCheckOutException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CopyException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.MoveException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.TransformException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UnsupportedTransformationException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.WorkflowException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.TooManyResultsException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.crypto.NoSuchPaddingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Base64;



public class EcmEngineManagementBean extends EcmEngineFeatureBean {

	private static final long serialVersionUID = 3749862753175561854L;

	public void deleteContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException {

		logger.debug("[EcmEngineManagementBean::deleteContent] BEGIN");
		final String logCtx = "P: " + node.getUid();
		start(); // Avvia stopwatch
		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();
		try {
			authenticateOnRepository(context, transaction);
			dumpElapsed("EcmEngineManagementBean", "deleteContent", logCtx, "Autenticazione completata");
			transaction.begin();
			deleteContentNoTransaction(node, context);
			transaction.commit();
		} catch (InvalidParameterException e) {
			logger.error("[EcmEngineManagementBean::deleteContent] InvalidParameterException: "+e.getMessage());
			rollbackQuietely(transaction);
			throw e;
		} catch (NoSuchNodeException e) {
			logger.error("[EcmEngineManagementBean::deleteContent] NoSuchNodeException: "+e.getMessage());
			rollbackQuietely(transaction);
			throw e;			
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "deleteContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "deleteContent", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::deleteContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new DeleteException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(
					e, "EcmEngineManagementBean", "deleteContent", DeleteException.class);
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
			stop();// Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::deleteContent] END");
		}
	}

	public void purgeContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException {

		logger.debug("[EcmEngineManagementBean::purgeContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "PURGE NODE: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "purgeContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef archivedNodeRef = checkNodeArchived(node, transaction);

			nodeArchiveService.purgeArchivedNode(archivedNodeRef);

			dumpElapsed("EcmEngineManagementBean", "purgeContent", logCtx, "Commit transazione.");
			logger.debug("[EcmEngineManagementBean::purgeContent] Nodo eliminato definitivamente.");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "purgeContent", logCtx, context, node.getUid(),
					node.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "purgeContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "purgeContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::purgeContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new DeleteException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(e, "EcmEngineManagementBean", "purgeContent", DeleteException.class);

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
			stop();// Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::purgeContent] END");
		}
	}

	public void purgeAllContents(OperationContext context)
	throws InvalidParameterException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException {

		logger.debug("[EcmEngineManagementBean::purgeAllContents] BEGIN");

		validate(ValidationType.NAME, "repository", context.getRepository());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "PURGE ALL: " + context.getRepository();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "purgeAllContents", logCtx, "Autenticazione completata");

			transaction.begin();

			nodeArchiveService.purgeAllArchivedNodes(DictionarySvc.SPACES_STORE);

			dumpElapsed("EcmEngineManagementBean", "purgeAllContents", logCtx, "Commit transazione.");
			logger.debug("[EcmEngineManagementBean::purgeAllContents] Nodo eliminato definitivamente.");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "purgeAllContents", logCtx, context, "0000000-0000-0000-0000-000000000000",
			"Tutti i nodi archiviati.");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "purgeAllContents", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "purgeAllContents", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::purgeAllContents] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new DeleteException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(e, "EcmEngineManagementBean", "purgeAllContents", DeleteException.class);

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
			stop();// Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::purgeAllContents] END");
		}
	}

	public Node restoreContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException, EcmEngineException {

		logger.debug("[EcmEngineManagementBean::restoreContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "RESTORE NODE: " + node.getUid();

		start(); // Avvia stopwatch

		Node result = null;

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineSearchBean::restoreContent] Permission denied for user " + context.getUsername());
				rollbackQuietely(transaction);
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}
			dumpElapsed("EcmEngineManagementBean", "restoreContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef archivedNodeRef = checkNodeArchived(node, transaction);

			RestoreNodeReport restoreNodeReport = nodeArchiveService.restoreArchivedNode(archivedNodeRef);
			if(restoreNodeReport.getRestoredNodeRef()==null){
				rollbackQuietely(transaction);
				throw new PermissionDeniedException("Errore durante il restore: "+restoreNodeReport.getStatus());
			}
			result = new Node();
			result.setUid(restoreNodeReport.getRestoredNodeRef().getId());


			dumpElapsed("EcmEngineManagementBean", "restoreContent", logCtx, "Commit transazione.");
			logger.debug("[EcmEngineManagementBean::restoreContent] Nodo ripristinato.");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "restoreContent", logCtx, context, node.getUid(),
					node.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "restoreContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "restoreContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::restoreContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(e, "EcmEngineManagementBean", "restoreContent", DeleteException.class);

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
			stop();// Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::restoreContent] END");
		}
		return result;
	}

	public Node[] restoreAllContents(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException, EcmEngineException {

		logger.debug("[EcmEngineManagementBean::restoreAllContents] BEGIN");

		validate(ValidationType.NAME, "repository", context.getRepository());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "PURGE ALL: " + context.getRepository();

		start(); // Avvia stopwatch

		Node[] result = null;

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "restoreAllContents", logCtx, "Autenticazione completata");

			transaction.begin();

			List<RestoreNodeReport> restoreNodeReportList = nodeArchiveService.restoreAllArchivedNodes(DictionarySvc.SPACES_STORE);
			int resultSize = (restoreNodeReportList != null ? restoreNodeReportList.size() : 0);
			result = new Node[resultSize];
			RestoreNodeReport restoreNodeReport = null;
			for (int i=0; i<resultSize; i++) {
				restoreNodeReport = restoreNodeReportList.get(i);
				if (restoreNodeReport.getStatus().isSuccess()) {
					result[i] = new Node();
					result[i].setUid(restoreNodeReport.getRestoredNodeRef().getId());
				} else {
					logger.error("[EcmEngineManagementBean::restoreAllContents] restore operation failed for node '"+restoreNodeReport.getArchivedNodeRef()+"' ["+restoreNodeReport.getCause().getMessage()+"]");
					rollbackQuietely(transaction);
					throw new EcmEngineException("restore operation failed for node '"+restoreNodeReport.getArchivedNodeRef()+"' ["+restoreNodeReport.getCause().getMessage()+"]");
				}
			}

			dumpElapsed("EcmEngineManagementBean", "restoreAllContents", logCtx, "Commit transazione.");
			logger.debug("[EcmEngineManagementBean::restoreAllContents] Nodi ripristinati.");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "restoreAllContents", logCtx, context, "0000000-0000-0000-0000-000000000000",
			"Tutti i nodi archiviati.");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "restoreAllContents", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "restoreAllContents", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::restoreAllContents] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(e, "EcmEngineManagementBean", "restoreAllContents", DeleteException.class);

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
			stop();// Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::restoreAllContents] END");
		}
		return result;
	}

	public EncryptionInfo checkEncryption(Node node, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, EcmEngineException, NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, RemoteException {

		logger.debug("[EcmEngineManagementBean::checkEncryption] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();
		EncryptionInfo result = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "checkEncryption", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);
			final Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
			final boolean encrypted = nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

			if (encrypted) {
				result = new EncryptionInfo();

				result.setKeyId((String) props.get(EcmEngineModelConstants.PROP_ENCRYPTION_KEY_ID));
				result.setSourceIV((String) props.get(EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));
				result.setSourceEncrypted(((Boolean) props.get(EcmEngineModelConstants.PROP_ENCRYPTED_FROM_SOURCE)).booleanValue());

				final String rawTransformationString = (String) props.get(EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

				try {
					CryptoTransformationSpec transform = CryptoTransformationSpec.buildTransformationSpec(
							rawTransformationString);

					result.setAlgorithm(transform.getAlgorithm());
					result.setMode(transform.getMode());
					result.setPadding(transform.getPadding());

					result.setCorruptedEncryptionInfo(false);

				} catch (IllegalArgumentException e) {

					// Fallback per cercare di recuperare almeno informazioni parziali
					String [] parts = rawTransformationString.split("/");
					if (parts != null && parts.length >= 1) {
						result.setAlgorithm(parts[0]);

						if (parts.length >= 2) {
							result.setMode(parts[1]);

							if (parts.length == 3) {
								result.setPadding(parts[2]);
							}
						}
					}

					result.setCorruptedEncryptionInfo(true);
				}
			}

			dumpElapsed("EcmEngineManagementBean", "checkEncryption", logCtx, "Verificata cifratura.");
			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "checkEncryption", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "checkEncryption", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::checkEncryption] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::checkEncryption] END");
		}
		return result;
	}

	public Node checkOutContent(Node node, OperationContext context)
	throws InvalidParameterException, CheckInCheckOutException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, EcmEngineTransactionException, PermissionDeniedException {

		logger.debug("[EcmEngineManagementBean::checkOutContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();
		Node workingCopy = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "checkInContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineManagementBean::checkOutContent] " +
					"Esecuzione check-out sul nodo: " + node.getUid());
			final NodeRef wcRef = checkOutCheckInService.checkout(nodeRef);

			dumpElapsed("EcmEngineManagementBean", "checkOutContent", logCtx, "Checkout eseguito.");
			workingCopy = new Node();
			workingCopy.setUid(wcRef.getId());

			dumpElapsed("EcmEngineManagementBean", "checkOutContent", logCtx, "Commit transazione.");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "checkOutContent", logCtx, context, node.getUid(),
					"N: " + node.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "checkOutContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "checkOutContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::checkOutContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new CheckInCheckOutException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<CheckInCheckOutException>checkIntegrityException(
					e, "EcmEngineManagementBean", "checkOutContent", CheckInCheckOutException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::checkOutContent] END");
		}
		return workingCopy;
	}

	public Node checkInContent(Node workingCopy, OperationContext context)
	throws InvalidParameterException, CheckInCheckOutException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineManagementBean::checkInContent] BEGIN");

		validate(ValidationType.NODE, "workingCopy", workingCopy);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "WC: " + workingCopy.getUid();
		Node resultNode = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "checkInContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(workingCopy, transaction);

			// Il nodo deve essere una working copy.
			if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
				final String errorMsg = "Node is not a working copy: " + nodeRef.getId();
				logger.error("[EcmEngineManagementBean::checkInContent] ERROR: " + errorMsg);
				rollbackQuietely(transaction);
				throw new InvalidParameterException(errorMsg);
			}

			logger.debug("[EcmEngineManagementBean::checkInContent] " +
					"Esecuzione check-in sul nodo working copy: " + workingCopy.getUid());

			// TODO: gestire le proprieta` di versione (ora passiamo null).
			final NodeRef resultRef = checkOutCheckInService.checkin(nodeRef, null);

			dumpElapsed("EcmEngineManagementBean", "checkInContent", logCtx, "Checkin eseguito.");
			resultNode = new Node();
			resultNode.setUid(resultRef.getId());

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "checkInContent", logCtx, context, workingCopy.getUid(),
					"WC: " + workingCopy.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "checkInContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "checkInContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::checkInContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new CheckInCheckOutException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<CheckInCheckOutException>checkIntegrityException(
					e, "EcmEngineManagementBean", "checkInContent", CheckInCheckOutException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::checkInContent] END");
		}
		return resultNode;
	}

	public void moveAggregation(Node source, Node destinationParent, OperationContext context)
	throws InvalidParameterException, MoveException, NoSuchNodeException, RemoteException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineManagementBean::moveAggregation] BEGIN");

		validate(ValidationType.NODE, "source", source);
		validate(ValidationType.NODE, "destinationParent", destinationParent);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		logger.debug("[EcmEngineManagementBean::moveAggregation] Spostamento nodo " + source.getUid() +
				" sotto il nodo " + destinationParent.getUid());

		String logCtx = "S: " + source.getUid() +
		" - D: " + destinationParent.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			//			XXX: check disabilitato per evitare problemi legati a transazioni cross-repository
			//			authenticateOnRepository(context, destinationRepository);
			//			final NodeRef destinationParentRef = checkNodeExists(destinationParent);

			// Le operazioni cross repository non sono supportate.
			final NodeRef destinationParentRef = new NodeRef("workspace://SpacesStore/" + destinationParent.getUid());

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "moveAggregation", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef sourceRef = checkNodeExists(source, transaction);

			//TODO:
			//chiamata al job di quartz per lo spostamento schedulato
			//in realta quello che devo fare e` settare l'aspect "state"
			//insieme all'aspect "destination" (meglio un solo aspect "moveable")
			// al nodo sorgente. Il job parte in automatico ogni sera.

			//BEGIN DISABLE AGGREGATION

			logger.debug("[EcmEngineManagementBean::moveAggregation] " +
					"Impostazione dei metadati dell'aspect: " + EcmEngineModelConstants.ASPECT_STATE.toString());

			//<property name="ecm-sys:stato"> proprieta dell'aspect
			Map<QName, Serializable> stateAspectProps = new HashMap<QName, Serializable>();

			//la ricerca dei nodi da spostare nel job di quartz verra` eseguita
			//in base al valore "spostabile"; quindi non Modificare la variabile seguente
			String valoreStatoNodo = "spostabile";
			stateAspectProps.put(EcmEngineModelConstants.PROP_STATO, valoreStatoNodo);

			//add aspect ecm-sys:state con proprieta ecm-sys:stato di valore "spostabile" al nodo source
			nodeService.addAspect(sourceRef, EcmEngineModelConstants.ASPECT_STATE, stateAspectProps);
			dumpElapsed("EcmEngineManagementBean", "moveAggregation", logCtx, "Add Aspect state al Nodo.");

			logger.debug("[EcmEngineManagementBean::moveAggregation] Add Aspect " + EcmEngineModelConstants.ASPECT_STATE.toString() +
					" al nodo: " + sourceRef.getId());

			//property dell'aspect destination
			//idNodeDestination - repoDestination - idNodeSource - repoSource
			Map<QName, Serializable> destinationAspectProps = new HashMap<QName, Serializable>();
			String idNodeDest = destinationParent.getUid();
			destinationAspectProps.put(EcmEngineModelConstants.PROP_ID_NODE_DEST, idNodeDest);

			String repoDest = context.getRepository();
			destinationAspectProps.put(EcmEngineModelConstants.PROP_REPO_DEST, repoDest);

			String idNodeSource = source.getUid();
			destinationAspectProps.put(EcmEngineModelConstants.PROP_ID_NODE_SOURCE, idNodeSource);

			String repoSource = context.getRepository();
			destinationAspectProps.put(EcmEngineModelConstants.PROP_REPO_SOURCE, repoSource);

			nodeService.addAspect(sourceRef, EcmEngineModelConstants.ASPECT_DESTINATION, destinationAspectProps);
			dumpElapsed("EcmEngineManagementBean", "moveAggregation", logCtx, "Add Aspect destination al Nodo.");

			logger.debug("[EcmEngineManagementBean::moveAggregation] Add Aspect " +
					EcmEngineModelConstants.ASPECT_DESTINATION.toString() + " al nodo : " + sourceRef.getId());

			//TODO:
			// prima di fare una modifica bisogna controllare se il nodo padre possiede l'aspect state
			// con valore spostabile; se e` cosi non e` possibile effettuare nessuna modifica al nodo
			// bisogna modificare i servizi fin qui fatti inserendo prima di tutto questo controllo

			//END DISABLE AGGREGATION

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "moveAggregation", logCtx, context,source.getUid(),
					"Source: " + sourceRef.getId() + " -- Dest Parent: " + destinationParentRef.getId());
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "moveAggregation", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "moveAggregation", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::moveAggregation] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new MoveException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<MoveException>checkIntegrityException(
					e, "EcmEngineManagementBean", "moveAggregation", MoveException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::moveAggregation] END");
		}
	}


	public byte [] retrieveContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, RemoteException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineManagementBean::retrieveContentData] BEGIN");
		start();
		UserTransaction transaction=null;
		byte result[]=null;
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			result=retrieveContentDataNoTransaction(node, content, context);
			transaction.commit();
		} catch (EncryptionRuntimeException e) {	// FIXME: questa eccezione non dovrebbe arrivare fino a qui!
			rollbackQuietely(transaction);
			FoundationErrorCodes code = null;
			if (e.getCause() == null) {
				// Causa sconosciuta -> errore generico
				code = FoundationErrorCodes.ENCRYPTION_GENERIC_ERROR;
			} else if (e.getCause() instanceof NoSuchAlgorithmException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_ALGORITHM_ERROR;
			} else if (e.getCause() instanceof InvalidKeyException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_KEY_ERROR;
			} else if (e.getCause() instanceof NoSuchPaddingException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_PADDING_ERROR;
			} else if (e.getCause() instanceof InvalidAlgorithmParameterException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_PARAM_ERROR;
			} else {
				code = FoundationErrorCodes.ENCRYPTION_GENERIC_ERROR;
			}

			logger.error("[EcmEngineManagementBean::retrieveContentData] Foundation services error: " + code);
			throw new ReadException("Backend services error: " + code);
		} catch (ReadException e) {
			logger.error("[EcmEngineManagementBean::retrieveContentData] Foundation services error: " + e);
			rollbackQuietely(transaction);
			throw new ReadException("Backend services error: " + e);
		} catch (InvalidParameterException ipe) {
			logger.error("[EcmEngineManagementBean::retrieveContentData] Invalid parameter error: " + ipe.getMessage());
			rollbackQuietely(transaction);
			throw ipe;
		} catch (PermissionDeniedException pde) {
			logger.error("[EcmEngineManagementBean::retrieveContentData] Permission denied error: " + pde.getMessage());
			rollbackQuietely(transaction);
			throw pde;
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::retrieveContentData] END");
		}
		return result;
	}

	public void updateContentData(Node node, Content content,
			OperationContext context) throws InvalidParameterException,
			UpdateException, NoSuchNodeException, RemoteException, EcmEngineTransactionException,
			InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::updateContentData] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.CONTENT_WRITE_CONTENT, "content", content);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final boolean encryptionRequired = (content.getEncryptionInfo() != null);
		final boolean encryptionSupported = contentService.supportsCryptography();
		logger.debug("[EcmEngineManagementBean::updateContentData] Cryptography: " +
				((encryptionSupported) ? "" : "NOT ") + "supported");

		final String logCtx = "N: " + node.getUid() + " - CN: "
		+ content.getPrefixedName();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "updateContentData", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			final boolean isEncrypted = nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

			CustomSecretKey encryptionKey = null;
			CryptoTransformationSpec encryptionSpec = null;
			String encryptionTransformation = null;
			byte [] iv = null;

			if ((isEncrypted || encryptionRequired) && encryptionSupported) {
				try {
					validate(ValidationType.ENCRYPTION_INFO_ENCRYPT, "content.encryptionInfo", content.getEncryptionInfo());
				} catch (InvalidParameterException ipe) {

					// Rollback if needed and rethrow
					rollbackQuietely(transaction);
					throw ipe;
				}

				final String encryptionMode = content.getEncryptionInfo().getMode();

				// Build encrypted aspect
				if (!content.getEncryptionInfo().isSourceEncrypted()) {
					encryptionKey = new CustomSecretKey(content.getEncryptionInfo().getAlgorithm(),
							content.getEncryptionInfo().getKey().getBytes());
				}

				encryptionSpec = new CryptoTransformationSpec(
						content.getEncryptionInfo().getAlgorithm(),
						content.getEncryptionInfo().getMode(),
						content.getEncryptionInfo().getPadding());

				String encodedIv = null;

				encryptionTransformation = CryptoTransformationSpec.buildTransformationString(encryptionSpec);

				HashMap<QName, Serializable> encryptionProps = new HashMap<QName, Serializable>(6);

				encryptionProps.put(EcmEngineModelConstants.PROP_ENCRYPTION_KEY_ID,
						content.getEncryptionInfo().getKeyId());
				encryptionProps.put(EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION,
						encryptionTransformation);
				encryptionProps.put(EcmEngineModelConstants.PROP_ENCRYPTED_FROM_SOURCE,
						content.getEncryptionInfo().isSourceEncrypted());

				if (encryptionMode != null && !encryptionMode.equalsIgnoreCase("ECB")) {
					if (!content.getEncryptionInfo().isSourceEncrypted()) {
						iv = CryptoTransformationSpec.generateIv(encryptionSpec, encryptionKey);
						encryptionSpec.setIv(iv);
						encodedIv = Base64.encodeBytes(iv);
					} else {
						encodedIv = content.getEncryptionInfo().getSourceIV();
					}

					encryptionProps.put(EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR, encodedIv);
				}

				if (!isEncrypted) {
					nodeService.addAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED, encryptionProps);
					logger.debug("[EcmEngineManagementBean::updateContentData] Aspect \"encrypted\" impostato sul nodo: " + nodeRef);
				} else {
					nodeService.setProperties(nodeRef, encryptionProps);
					logger.debug("[EcmEngineManagementBean::updateContentData] Encryption property impostate sul nodo: " + nodeRef);
				}
			}

			QName contentPropertyName = null;
			if (content.getContentPropertyPrefixedName() != null) {
				contentPropertyName = dictionaryService.resolvePrefixNameToQName(
						content.getContentPropertyPrefixedName());
			}

			final byte [] data = content.getContent();

			if (data != null && contentPropertyName != null) {
				logger.debug("[EcmEngineManagementBean::updateContentData] Scrittura contenuto fisico.");

				final ContentWriter writer = (encryptionRequired && !content.getEncryptionInfo().isSourceEncrypted())
				? contentService.getEncryptingWriter(nodeRef, contentPropertyName, true, encryptionKey, encryptionSpec)
						: contentService.getWriter(nodeRef, contentPropertyName, true);

				writer.setMimetype(content.getMimeType());
				writer.setEncoding(content.getEncoding());

				try {
					writer.putContent(new ByteArrayInputStream(data));
				} catch (ContentQuotaException e) {
					logger.warn("[EcmEngineManagementBean::updateContentData] Content quota exceeded: " + e.getMessage(), e);
					rollbackQuietely(transaction);
					throw new UpdateException("Content quota exceeded: " + e.getMessage());
				} catch (ContentIOException e) {
					logger.warn("[EcmEngineManagementBean::updateContentData] Unable to write content: " + e.getMessage(), e);
					rollbackQuietely(transaction);
					throw new UpdateException("Unable to write content: " + e.getMessage());
				}

				dumpElapsed("EcmEngineManagementBean", "updateContentData", logCtx,
						"Scrittura contenuto completata: " + data.length + " byte.");
			}

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "updateContentData", logCtx,
					context, node.getUid(), content.getTypePrefixedName()
					+ " [Name: " + content.getPrefixedName() + "]");

			transaction.commit();

		} catch (EncryptionRuntimeException e) {	// FIXME: questa eccezione non dovrebbe arrivare fino a qui!
			rollbackQuietely(transaction);

			FoundationErrorCodes code = null;
			if (e.getCause() == null) {
				// Causa sconosciuta -> errore generico
				code = FoundationErrorCodes.ENCRYPTION_GENERIC_ERROR;
			} else if (e.getCause() instanceof NoSuchAlgorithmException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_ALGORITHM_ERROR;
			} else if (e.getCause() instanceof InvalidKeyException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_KEY_ERROR;
			} else if (e.getCause() instanceof NoSuchPaddingException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_PADDING_ERROR;
			} else if (e.getCause() instanceof InvalidAlgorithmParameterException) {
				code = FoundationErrorCodes.ENCRYPTION_INVALID_PARAM_ERROR;
			} else {
				code = FoundationErrorCodes.ENCRYPTION_GENERIC_ERROR;
			}

			logger.error("[EcmEngineManagementBean::updateContentData] Foundation services error: " + code);
			throw new UpdateException("Backend services error: " + code);
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "updateContentData", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "updateContentData", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::updateContentData] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new UpdateException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<UpdateException>checkIntegrityException(
					e, "EcmEngineManagementBean", "updateContentData", UpdateException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::updateContentData] END");
		}
	}

	public void unLinkContent(Node source, Node destination, Association association,
			OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
			RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineManagementBean::unLinkContent] BEGIN");

		validate(ValidationType.NODE, "source", source);
		validate(ValidationType.NODE, "destination", destination);
		validate(ValidationType.ASSOCIATION, "association", association);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "P: " + source.getUid() + " - C: " + destination.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "unLinkContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef sourceRef = checkNodeExists(source, transaction);
			final NodeRef targetRef = checkNodeExists(destination, transaction);

			QName assocTypeQName = dictionaryService.resolvePrefixNameToQName(association.getTypePrefixedName());

			if (association.isChildAssociation()) {
				//associazione padre-figlio
				List<ChildAssociationRef> childAssociations = nodeService.getChildAssocs(sourceRef);
				ChildAssociationRef childAssocRef = null;

				for (ChildAssociationRef assoc: childAssociations) {
					if (assoc != null
							&& assoc.getChildRef() != null
							&& assoc.getChildRef().getId().equals(targetRef.getId())) {
						childAssocRef = assoc;
						break;
					}
				}

				if (childAssocRef != null){
					//associazione padre-figlio
					nodeService.removeChildAssociation(childAssocRef);
					logger.debug("[EcmEngineManagementBean::unLinkContent] Associazione child eliminata.");
					dumpElapsed("EcmEngineManagementBean", "unLinkContent", logCtx, "Associazione eliminata.");
				}
			} else {
				//associazione di tipo reference
				List<AssociationRef> associations = nodeService.getTargetAssocs(sourceRef, assocTypeQName);
				AssociationRef assocRef=null;

				for (AssociationRef assoc: associations) {
					if (assoc != null
							&& assoc.getTargetRef() != null
							&& assoc.getTargetRef().getId().equals(targetRef.getId())) {
						assocRef=assoc;
						break;
					}
				}

				if (assocRef != null){
					//associazione di tipo reference
					nodeService.removeAssociation(sourceRef, targetRef, assocTypeQName);
					logger.debug("[EcmEngineManagementBean::unLinkContent] Associazione di tipo reference eliminata.");
					dumpElapsed("EcmEngineManagementBean", "unLinkContent", logCtx, "Associazione eliminata.");
				}
			}
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "unLinkContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "unLinkContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::unLinkContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "unLinkContent", InsertException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::unLinkContent] END");
		}
	}


	public void linkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, RemoteException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineManagementBean::linkContent] BEGIN");

		validate(ValidationType.NODE, "source", source);
		validate(ValidationType.NODE, "destination", destination);
		validate(ValidationType.ASSOCIATION, "association", association);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		//		if (isCrossRepository(source, destination)) {
		//		final String errorMsg = "Cross-repository link not allowed: " +
		//		"SRC REP: " + source.getRepository() +
		//		"DEST REP: " + destination.getRepository();
		//		logger.error("[EcmEngineManagementBean::linkContent] ERROR: " + errorMsg);

		//		throw new InvalidParameterException(errorMsg);
		//		}

		final String logCtx = "P: " + source.getUid() + " - C: " + destination.getUid();

		start(); // Avvia stopwatch
		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "linkContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef sourceRef = checkNodeExists(source, transaction);
			final NodeRef destinationRef = checkNodeExists(destination, transaction);

			QName assocTypeQName = dictionaryService.resolvePrefixNameToQName(association.getTypePrefixedName());

			if (association.isChildAssociation()){
				QName assocQName = dictionaryService.resolvePrefixNameToQName(association.getPrefixedName());
				nodeService.addChild(sourceRef, destinationRef, assocTypeQName, assocQName);
				logger.debug("[EcmEngineManagementBean::linkContent] creazione associazione padre-figlio.");
			} else {
				nodeService.createAssociation(sourceRef, destinationRef, assocTypeQName);
				logger.debug("[EcmEngineManagementBean::linkContent] creazione reference.");
			}

			dumpElapsed("EcmEngineManagementBean", "linkContent", logCtx, "Associazione creata.");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "linkContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "linkContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::linkContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "linkContent", InsertException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::linkContent] END");
		}
	}

	public void updateMetadata(Node node, Content newContent, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		UserTransaction transaction=null;
		logger.debug("[EcmEngineManagementBean::updateMetadata] BEGIN");
		start();
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			updateMetadataNoTransaction(node, newContent, context);
			transaction.commit();
		} catch(InvalidParameterException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(UpdateException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(NoSuchNodeException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(InvalidCredentialsException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(PermissionDeniedException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(RemoteException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<UpdateException>checkIntegrityException(
					e, "EcmEngineManagementBean", "updateMetadata", UpdateException.class);
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		}finally{
			logger.debug("[EcmEngineManagementBean::updateMetadata] END");
			stop();
		}
	}

	//******************** Metodi Audit Trail **************************************/

	public void logTrail(AuditInfo auditTrail, OperationContext context)
	throws InvalidParameterException, AuditTrailException, RemoteException, InvalidCredentialsException, EcmEngineTransactionException {
		logger.debug("[EcmEngineManagementBean::logTrail] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);
		validate(ValidationType.AUDIT_INFO, "auditTrail", auditTrail);

		final String logCtx = "User: " + auditTrail.getUtente() +
		" - Operazione: " + auditTrail.getOperazione() +
		" - Id oggetto: " + auditTrail.getIdOggetto() ;

		logger.debug("[EcmEngineManagementBean::logTrail] Parametri - Op: "
				+ auditTrail.getOperazione() + " U: " + auditTrail.getUtente()
				+ "Id Oggetto: " + auditTrail.getIdOggetto());

		// avvia stopwatch
		start();

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			logger.debug("[EcmEngineManagementBean::logTrail] Autenticazione - U: " + context.getUsername());
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineManagementBean", "logTrail", logCtx, "Autenticazione completata");

			transaction.begin();

			// Effettua l'audit trail
			auditTrailService.logTrail(auditTrail);

			// log della misurazione effettuata
			dumpElapsed("EcmEngineManagementBean", "logTrail", logCtx, "Inserimento audit trail terminato");

			logger.debug("[EcmEngineManagementBean::logTrail] Inserimento audit trail terminato.");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "logTrail", context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::logTrail] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new AuditTrailException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<AuditTrailException>checkIntegrityException(
					e, "EcmEngineManagementBean", "logTrail", AuditTrailException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::logTrail] END");
		}
	}


	public AuditInfo[] ricercaAuditTrail(
			AuditTrailSearchParams parametriRicerca, OperationContext context)
	throws InvalidParameterException, AuditTrailException,
	PermissionDeniedException, InvalidCredentialsException, EcmEngineTransactionException, RemoteException {
		logger.debug("[EcmEngineManagementBean::ricercaAuditTrail] BEGIN");

		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "User: " + parametriRicerca.getUtente() ;

		AuditInfo[] auditInfo=null;

		// avvia stopwatch
		start();

		try {
			logger.debug("[EcmEngineManagementBean::ricercaAuditTrail] Autenticazione - U: " + context.getUsername());
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineManagementBean", "ricercaAuditTrail", logCtx, "Autenticazione completata");

			// Solo gli utenti ADMIN possono eseguire ricerche
			if (!authorityService.hasAdminAuthority()) {
				logger.warn("[EcmEngineSearchBean::ricercaAudit] Permission denied for user " + context.getUsername());
				throw new PermissionDeniedException("Permission denied for user " + context.getUsername());
			}

			auditInfo = auditTrailService.ricercaAuditTrail(parametriRicerca);

			dumpElapsed("EcmEngineManagementBean", "ricercaAuditTrail", logCtx, "Ricerca terminata.");

			int size = (auditInfo != null) ? auditInfo.length : 0;

			logger.debug("[EcmEngineManagementBean::ricercaAuditTrail] Ricerca terminata. Trovati " + size + " record.");
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "ricercaAuditTrail", context.getUsername(), null);
			checkAccessException(e, "EcmEngineManagementBean", "ricercaAuditTrail", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineManagementBean::ricercaAuditTrail] Foundation services error: " + e.getCode());
			throw new AuditTrailException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineManagementBean::ricercaAuditTrail] END");
		}
		return auditInfo;
	}


	//************************************ PRIVATE ***********************************/
	public Version[] getAllVersions(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineTransactionException,
	EcmEngineException, RemoteException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::getAllVersions] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();

		Version[] versions = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "getAllVersions", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			// Get versions
			VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
			versions = translateVersionHistory(versionHistory);
			dumpElapsed("EcmEngineManagementBean", "getAllVersions", logCtx,
					"Trovate " + versions.length + " versioni.");

			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getAllVersions", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getAllVersions", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::getAllVersions] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getAllVersions] END");
		}
		return versions;
	}

	public Version getVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, EcmEngineTransactionException, EcmEngineException,
	InvalidCredentialsException, RemoteException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::getVersion] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();

		Version version = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "getVersion", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			// Get versions
			VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
			if(versionHistory==null){
				rollbackQuietely(transaction);
				throw new NoSuchNodeException("Non esistono versioni per questo nodo.");
			}
			org.alfresco.service.cmr.version.Version labeledVersion = versionHistory.getVersion(versionLabel);
			if(labeledVersion==null){
				rollbackQuietely(transaction);
				throw new NoSuchNodeException("Versione inesistente per questo nodo.");
			}
			version = translateVersion(labeledVersion);
			dumpElapsed("EcmEngineManagementBean", "getVersion", logCtx,
			"Versione trovata.");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getVersion", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getVersion", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::getVersion] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getVersion] END");
		}
		return version;
	}

	public void revertVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, EcmEngineException, RemoteException {
		logger.debug("[EcmEngineManagementBean::revertVersion] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid() + " - Label: " + versionLabel;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "revertVersion", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			// Get versions
			VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
			org.alfresco.service.cmr.version.Version labeledVersion = versionHistory.getVersion(versionLabel);
			if (labeledVersion == null) {
				final String errorMsg = "Version not found for label: " + versionLabel;
				logger.error("[EcmEngineManagementBean::revertVersion] ERROR: " + errorMsg);
				rollbackQuietely(transaction);
				throw new InvalidParameterException(errorMsg);
			}
			versionService.revert(nodeRef, labeledVersion);
			dumpElapsed("EcmEngineManagementBean", "revertVersion", logCtx,
			"Versione ripristinata.");

			// Inserimento audit
			insertAudit("EcmEngineManagementBean", "revertVersion", logCtx, context, node.getUid(),
					"N: " + node.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "revertVersion", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "revertVersion", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::revertVersion] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<EcmEngineException>checkIntegrityException(
					e, "EcmEngineManagementBean", "revertVersion", EcmEngineException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::revertVersion] END");
		}
	}

	public Node cancelCheckOutContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, CheckInCheckOutException,
	RemoteException, EcmEngineTransactionException, PermissionDeniedException {

		logger.debug("[EcmEngineManagementBean::cancelCheckOutContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();
		Node resultNode = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "cancelCheckOutContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			// Il nodo deve essere una working copy.
			if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
				final String errorMsg = "Node is not a working copy: " + nodeRef.getId();
				logger.error("[EcmEngineManagementBean::cancelCheckOutContent] ERROR: " + errorMsg);
				rollbackQuietely(transaction);
				throw new InvalidParameterException(errorMsg);
			}

			logger.debug("[EcmEngineManagementBean::cancelCheckOutContent] " +
					"Annullamento del check-out sul nodo: " + node.getUid());
			final NodeRef origRef = checkOutCheckInService.cancelCheckout(nodeRef);

			dumpElapsed("EcmEngineManagementBean", "cancelCheckOutContent", logCtx, "Annullamento checkout eseguito.");
			resultNode = new Node();
			resultNode.setUid(origRef.getId());

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "cancelCheckOutContent", logCtx, context, node.getUid(),
					"N: " + node.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "cancelCheckOutContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "cancelCheckOutContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::cancelCheckOutContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new CheckInCheckOutException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<CheckInCheckOutException>checkIntegrityException(
					e, "EcmEngineManagementBean", "cancelCheckOutContent", CheckInCheckOutException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::cancelCheckOutContent] END");
		}
		return resultNode;
	}

	public Node getWorkingCopy(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException,
	EcmEngineTransactionException, EcmEngineException, RemoteException {

		logger.debug("[EcmEngineManagementBean::getWorkingCopy] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();
		Node workingCopy = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "getWorkingCopy", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			logger.debug("[EcmEngineManagementBean::getWorkingCopy] " +
					"Esecuzione check-out sul nodo: " + node.getUid());
			final NodeRef wcRef = checkOutCheckInService.getWorkingCopy(nodeRef);

			dumpElapsed("EcmEngineManagementBean", "getWorkingCopy", logCtx, "Checkout eseguito.");
			workingCopy = new Node(wcRef.getId());

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "getWorkingCopy", logCtx, context, node.getUid(),
					"N: " + node.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getWorkingCopy", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getWorkingCopy", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::getWorkingCopy] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new EcmEngineException("Backend services error: " + e.getCode());
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getWorkingCopy] END");
		}
		return workingCopy;
	}

	public byte[] retrieveVersionContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, ReadException,
	RemoteException, EcmEngineTransactionException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::retrieveVersionContentData] BEGIN");

		validate(ValidationType.NODE, "node", node);

		/*
		 * Le informazioni sul tipo del contenuto vengono ignorate, quindi non
		 * vengono validate. L'unica informazione verificata e` quella relativa
		 * al nome con prefisso del contenuto, che verra` utilizzato per un
		 * ulteriore check sulla selezione del nodo.
		 */
		//		validate(ValidationType.CONTENT_ITEM, "content", content);
		//		validate(ValidationType.PREFIXED_NAME, "content.contentPropertyPrefixedName", content.getContentPropertyPrefixedName());
		validate(ValidationType.CONTENT_READ, "content", content);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid() + " - CN: "
		+ content.getPrefixedName() + " - CP: "
		+ content.getContentPropertyPrefixedName();
		byte [] binaryData = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "retrieveVersionContentData", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = new NodeRef(DictionarySvc.VERSIONS_STORE,
					node.getUid());

			if (!nodeService.exists(nodeRef)) {
				logger.error("[EcmEngineManagementBean::retrieveVersionContentData] "
						+ "Node not found in version store: " + node.getUid());
				rollbackQuietely(transaction);
				throw new NoSuchNodeException(node.getUid());
			}

			final QName contentProperty = dictionaryService.resolvePrefixNameToQName(
					content.getContentPropertyPrefixedName());
			final ContentReader reader = contentService.getReader(nodeRef,
					contentProperty);

			if (reader == null || !reader.exists()) {
				logger.warn("[EcmEngineManagementBean::retrieveVersionContentData]"
						+ " Non ci sono dati binari associati al nodo: "
						+ node.getUid() + " [Name: "
						+ content.getPrefixedName() + "]");
			} else {
				try {
					logger.debug("[EcmEngineManagementBean::retrieveVersionContentData]"
							+ " Lettura dei dati binari associati al nodo: "
							+ node.getUid() + " [Name: "
							+ content.getPrefixedName() + " - Prop: "
							+ content.getContentPropertyPrefixedName()
							+ " - Size: " + reader.getSize() + " byte]");
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
							(int) reader.getSize());

					reader.getContent(outputStream);
					binaryData = outputStream.toByteArray();

					dumpElapsed("EcmEngineManagementBean", "retrieveVersionContentData",
							logCtx, "Lettura completata: " + binaryData.length
							+ " byte letti.");
				} catch (ContentIOException e) {
					final String errorMsg = "Errore in lettura! Nodo: "
						+ node.getUid() + " [Name: "
						+ content.getPrefixedName() + " - Prop: "
						+ content.getContentPropertyPrefixedName()
						+ " - Size: " + reader.getSize() + " byte]";

					logger.error("[EcmEngineManagementBean::retrieveVersionContentData] "
							+ errorMsg);
					rollbackQuietely(transaction);
					throw new ReadException(errorMsg);
				}
			}

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "retrieveVersionContentData", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "retrieveVersionContentData", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::retrieveVersionContentData] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new ReadException("Backend services error: " + e.getCode());
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::retrieveVersionContentData] END");
		}
		return binaryData;
	}

	public ResultContent getContentMetadata(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, PermissionDeniedException,
	EcmEngineTransactionException, ReadException, InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineManagementBean::getContentMetadata] BEGIN");
		start();
		UserTransaction transaction=null;
		ResultContent result=null;
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			result=getContentMetadataNoTransaction(node, context);
			transaction.commit();
		} catch (EcmEngineException e) {
			logger.error("[EcmEngineManagementBean::getContentMetadata] Foundation services error: " + e);
			rollbackQuietely(transaction);
			throw new ReadException("Backend services error: "+e);
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getContentMetadata] END");
		}
		return result;
	}

	public ResultContent getVersionMetadata(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, PermissionDeniedException,
	EcmEngineTransactionException, ReadException, InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineManagementBean::getVersionMetadata] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultContent result = null;
		final String logCtx = "N: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction(true);

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "getVersionMetadata", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = new NodeRef(DictionarySvc.VERSIONS_STORE, node.getUid());

			if (!nodeService.exists(nodeRef)) {
				logger.error("[EcmEngineManagementBean::getVersionMetadata] "
						+ "Node not found in version store: " + node.getUid());
				rollbackQuietely(transaction);
				throw new NoSuchNodeException(node.getUid());
			}

			result = new ResultContent();

			final QName typeQName = nodeService.getType(nodeRef);
			final Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
			ChildAssociationRef parentRef = null;

			final boolean encrypted = nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

			try {
				parentRef = nodeService.getPrimaryParent(nodeRef);

				result.setPrefixedName((parentRef.getQName() != null)
						? dictionaryService.resolveQNameToPrefixName(parentRef.getQName())
								: null);

				final TypeDefinition typeDef = dictionaryService.getType(typeQName);
				final QName modelQName = typeDef.getModel().getName();

				result.setModelPrefixedName(dictionaryService.resolveQNameToPrefixName(modelQName));
			} catch (EcmEngineFoundationException e) {
				if (e.getCode().equals(FoundationErrorCodes.ACCESS_DENIED_ERROR)) {
					logger.debug("[EcmEngineManagementBean::getVersionMetadata] " +
							"Access denied reading parent of node: " + node);
				} else {
					logger.warn("[EcmEngineManagementBean::getVersionMetadata] " +
							"Unexpected error reading parent of node: " + node + " [" + e.getCode() + "]");
				}

				// XXX: nome fittizio!
				result.setPrefixedName("sys:unreadable-parent");
			}

			result.setUid((String) props.get(ContentModel.PROP_NODE_UUID));

			// Process encryption information if needed
			if (encrypted) {
				EncryptionInfo encInfo = new EncryptionInfo();

				encInfo.setKeyId((String) props.get(EcmEngineModelConstants.PROP_ENCRYPTION_KEY_ID));
				encInfo.setSourceIV((String) props.get(EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));
				encInfo.setSourceEncrypted(((Boolean) props.get(EcmEngineModelConstants.PROP_ENCRYPTED_FROM_SOURCE)).booleanValue());

				final String rawTransformationString = (String) props.get(EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

				try {
					CryptoTransformationSpec transform = CryptoTransformationSpec.buildTransformationSpec(
							rawTransformationString);

					encInfo.setAlgorithm(transform.getAlgorithm());
					encInfo.setMode(transform.getMode());
					encInfo.setPadding(transform.getPadding());

					encInfo.setCorruptedEncryptionInfo(false);

				} catch (IllegalArgumentException e) {

					// Fallback per cercare di recuperare almeno informazioni parziali
					String [] parts = rawTransformationString.split("/");
					if (parts != null && parts.length >= 1) {
						encInfo.setAlgorithm(parts[0]);

						if (parts.length >= 2) {
							encInfo.setMode(parts[1]);

							if (parts.length == 3) {
								encInfo.setPadding(parts[2]);
							}
						}
					}

					encInfo.setCorruptedEncryptionInfo(true);
				}

				result.setEncryptionInfo(encInfo);
			}

			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
				if (entry.getValue() instanceof ContentData) {
					final ContentData data = (ContentData) entry.getValue();
					result.setContentPropertyPrefixedName(
							dictionaryService.resolveQNameToPrefixName(entry.getKey()));
					result.setEncoding(data.getEncoding());
					result.setMimeType(data.getMimetype());
					result.setSize(data.getSize());
				}
			}
			result.setTypePrefixedName(dictionaryService.resolveQNameToPrefixName(typeQName));
			result.setParentAssocTypePrefixedName(
					(parentRef != null) ?
							dictionaryService.resolveQNameToPrefixName(parentRef.getTypeQName()) :
								null
			);

			ResultProperty [] resultProps = translatePropertyMap(props, null);
			result.setProperties(resultProps);

			// Gestione aspect associati al risultato
			final Set<QName> aspects = nodeService.getAspects(nodeRef);
			final int aspectsSize = aspects.size();
			int j = 0;

			if (aspectsSize > 0) {
				ResultAspect [] resultAspects = new ResultAspect[aspectsSize];

				for (QName aspect : aspects) {
					resultAspects[j] = new ResultAspect();
					resultAspects[j].setPrefixedName(dictionaryService.resolveQNameToPrefixName(aspect));
					j++;
				}
				result.setAspects(resultAspects);
			}
			dumpElapsed("EcmEngineManagementBean", "getVersionMetadata", logCtx, "Letti metadati");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getVersionMetadata", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getVersionMetadata", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::getVersionMetadata] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new ReadException("Backend services error: " + e.getCode());
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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getVersionMetadata] END");
		}
		return result;
	}

	public byte [] transformContent(Node node, String targetMimeType, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, UnsupportedTransformationException,
	TransformException, RemoteException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {

		logger.debug("[EcmEngineManagementBean::transformContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NAME, "targetMimeType", targetMimeType);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid() + " - target MimeType: " +targetMimeType;

		//		ContentTransformer contentTransformer = null;

		byte [] binaryData = null;

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "transformContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);
			final Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

			String sourceMimeType = null;
			//			String encoding = null;
			//			long size = 0;

			QName contentProp = null;

			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {

				if (entry.getValue() instanceof ContentData) {

					final ContentData data = (ContentData) entry.getValue();
					sourceMimeType = data.getMimetype();
					//					encoding = data.getEncoding();
					//					size = data.getSize();
					contentProp = entry.getKey();
					break;
				}
			}

			try {
				validate(ValidationType.NAME, "sourceMimeType", sourceMimeType);
			} catch (InvalidParameterException ipe) {

				// Rollback if needed and rethrow
				rollbackQuietely(transaction);
				throw ipe;
			}

			if (contentProp == null) {

				final String errorMsg = "Node doesn't have content: " + nodeRef.getId();

				logger.error("[EcmEngineManagementBean::transformContent] ERROR: " + errorMsg);
				rollbackQuietely(transaction);
				throw new InvalidParameterException(errorMsg);
			}


			final ContentReader reader = contentService.getReader(nodeRef, contentProp);

			reader.setMimetype(sourceMimeType);

			if (!reader.exists()) {
				String errorMsg = "Non ci sono dati binari associati al nodo: " + nodeRef.getId();

				logger.warn("[EcmEngineManagementBean::transformContent]" +
						" Non ci sono dati binari associati al nodo: " + nodeRef.getId());
				rollbackQuietely(transaction);
				throw new InvalidParameterException(errorMsg);
			}

			//final ContentWriter writer = contentService.getWriter(nodeRef, contentProp, true);
			final ContentWriter tempWriter = contentService.getTempWriter();
			tempWriter.setMimetype(targetMimeType);


			//			contentTransformer =  contentService.getTransformer(sourceMimeType, targetMimeType);

			//			if(contentTransformer==null){

			//			String errorMsg="Trasformazione contenuto non supportata - Nodo: "
			//			+ nodeRef.getId() + " - Source MimeType : " + sourceMimeType
			//			+" - Target MimeType : "+targetMimeType;
			//			logger.error("[EcmEngineManagementBean::transformContent] ERROR: " + errorMsg);

			//			throw new UnsupportedTransformationException(errorMsg);
			//			}

			//			contentTransformer.transform(reader,tempWriter);
			//			dumpElapsed("EcmEngineManagementBean", "transformContent", logCtx, "Contenuto trasformato");
			//			logger.debug("[EcmEngineManagementBean::transformContent] Contenuto trasformato tramite contentTransformer");

			if (contentService.isTransformable(reader,tempWriter)){

				logger.debug("[EcmEngineManagementBean::transformContent] Inizio trasformazione tramite contentService");
				contentService.transform(reader,tempWriter);
				dumpElapsed("EcmEngineManagementBean", "transformContent", logCtx, "Contenuto trasformato");
				logger.debug("[EcmEngineManagementBean::transformContent] Contenuto trasformato tramite contentService");
			} else {
				String errorMsg = "Trasformazione non supportata - Nodo: " + nodeRef.getId() +
				" - Source MimeType : " + sourceMimeType +
				" - Target MimeType : " + targetMimeType;

				logger.error("[EcmEngineManagementBean::transformContent] ERROR: " + errorMsg);
				rollbackQuietely(transaction);
				throw new UnsupportedTransformationException(errorMsg);
			}

			ContentReader tempReader = tempWriter.getReader();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
					(int) tempReader.getSize());

			tempReader.getContent(outputStream);
			binaryData = outputStream.toByteArray();

			dumpElapsed("EcmEngineManagementBean", "transformContent", logCtx,
					"Lettura Content trasformato completata: " + binaryData.length + " byte letti.");

			logger.debug("[EcmEngineManagementBean::transformContent] " +
					"Lettura Content trasformato completata: " + binaryData.length + " byte letti.");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "transformContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "transformContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::transformContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new TransformException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<TransformException>checkIntegrityException(
					e, "EcmEngineManagementBean", "transformContent", TransformException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::transformContent] END");
		}
		return binaryData;
	}

	public void startSimpleWorkflow(Node node, SimpleWorkflow workflow, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {

		logger.debug("[EcmEngineManagementBean::startSimpleWorkflow] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NOT_NULL, "workflow", workflow);
		validate(ValidationType.NODE, "workflow.approveNode", workflow.getApproveNode());
		validate(ValidationType.NODE, "workflow.rejectNode", workflow.getRejectNode());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "startSimpleWorkflow", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef approveNodeRef = checkNodeExists(workflow.getApproveNode(), transaction);
			final NodeRef rejectNodeRef = checkNodeExists(workflow.getRejectNode(), transaction);
			final NodeRef contentNodeRef = checkNodeExists(node, transaction);

			Map<String,Serializable> params = new HashMap<String, Serializable>();
			params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP, "Approve");
			params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER, approveNodeRef);
			params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE, Boolean.valueOf(workflow.isMoveContent()));
			params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP, "Reject");
			params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER, rejectNodeRef);
			params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE, Boolean.valueOf(workflow.isMoveContent()));

			Action actionObject = this.actionService.createAction(ECMENGINE_SIMPLE_WORKFLOW_ACTION);

			dumpElapsed("EcmEngineManagementBean", "startSimpleWorkflow", logCtx, "Action creata");

			actionObject.setParameterValues(params);
			this.actionService.executeAction(actionObject, contentNodeRef);
			dumpElapsed("EcmEngineManagementBean", "startSimpleWorkflow", logCtx, "Action eseguita");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "startSimpleWorkflow", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "startSimpleWorkflow", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::startSimpleWorkflow] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new WorkflowException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<WorkflowException>checkIntegrityException(
					e, "EcmEngineManagementBean", "startSimpleWorkflow", WorkflowException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::startSimpleWorkflow] END");
		}
	}

	public void addSimpleWorkflowRule(Node node, SimpleWorkflow workflow, Rule rule, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::addSimpleWorkflowRule] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.NOT_NULL, "workflow", workflow);
		validate(ValidationType.NODE, "workflow.approveNode", workflow.getApproveNode());
		validate(ValidationType.NODE, "workflow.rejectNode", workflow.getRejectNode());
		validate(ValidationType.NOT_NULL, "rule", rule);
		validate(ValidationType.RULE_TYPE, "rule.type", rule.getType());
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "addSimpleWorkflowRule", logCtx, "Autenticazione completata");

			transaction.begin();

			// Prepare workflow action object
			final NodeRef nodeRef = checkNodeExists(node, transaction);
			final NodeRef approveNodeRef = checkNodeExists(workflow.getApproveNode(), transaction);
			final NodeRef rejectNodeRef = checkNodeExists(workflow.getRejectNode(), transaction);

			Action actionObject = this.actionService.createAction(ECMENGINE_SIMPLE_WORKFLOW_ACTION);
			Map<String,Serializable> params = new HashMap<String, Serializable>();
			params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP, "Approve");
			params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER, approveNodeRef);
			params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE, Boolean.valueOf(workflow.isMoveContent()));
			params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP, "Reject");
			params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER, rejectNodeRef);
			params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE, Boolean.valueOf(workflow.isMoveContent()));
			actionObject.setParameterValues(params);
			dumpElapsed("EcmEngineManagementBean", "addSimpleWorkflowRule", logCtx, "Action creata");

			// Prepare condition for composite action object and save rule.
			// CompositeAction must contain:
			//   - an Action
			//   - an ActionCondition
			// Simple workflows use a simple condition that always resolves to 'true'.
			// TODO Add additional ActionCondition support.
			org.alfresco.service.cmr.rule.Rule newRule = new org.alfresco.service.cmr.rule.Rule();
			CompositeAction compositeAction = actionService.createCompositeAction();
			compositeAction.addAction(actionObject);
			compositeAction.addActionCondition(actionService.createActionCondition(NoConditionEvaluator.NAME));
			newRule.setAction(compositeAction);
			newRule.setRuleType(decodeRuleType(rule.getType()));
			newRule.applyToChildren(rule.isApplyToChildren());
			newRule.setExecuteAsynchronously(false);
			newRule.setRuleDisabled(false);
			this.ruleService.saveRule(nodeRef, newRule);
			dumpElapsed("EcmEngineManagementBean", "addSimpleWorkflowRule", logCtx, "Rule creata");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "addSimpleWorkflowRule", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "addSimpleWorkflowRule", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::addSimpleWorkflowRule] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new WorkflowException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<WorkflowException>checkIntegrityException(
					e, "EcmEngineManagementBean", "addSimpleWorkflowRule", WorkflowException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::addSimpleWorkflowRule] END");
		}
	}

	public void approveContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::approveContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "approveContent", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef contentNodeRef = checkNodeExists(node, transaction);

			Action actionObject = this.actionService.createAction(SimpleWorkflowApproveActionExecuter.NAME);
			dumpElapsed("EcmEngineManagementBean", "approveContent", logCtx, "Action creata");

			this.actionService.executeAction(actionObject, contentNodeRef);
			dumpElapsed("EcmEngineManagementBean", "approveContent", logCtx, "Action eseguita");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "approveContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "approveContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::approveContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new WorkflowException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<WorkflowException>checkIntegrityException(
					e, "EcmEngineManagementBean", "approveContent", WorkflowException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::approveContent] END");
		}
	}

	public void rejectContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException,
	EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException {
		logger.debug("[EcmEngineManagementBean::rejectContent] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "rejectContent", logCtx, "Autenticazione completata");

			transaction.begin();

			NodeRef contentNodeRef = checkNodeExists(node, transaction);
			Action actionObject = this.actionService.createAction(SimpleWorkflowRejectActionExecuter.NAME);
			dumpElapsed("EcmEngineManagementBean", "rejectContent", logCtx, "Action creata");

			this.actionService.executeAction(actionObject, contentNodeRef);
			dumpElapsed("EcmEngineManagementBean", "rejectContent", logCtx, "Action eseguita");

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "rejectContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "rejectContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::rejectContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new WorkflowException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<WorkflowException>checkIntegrityException(
					e, "EcmEngineManagementBean", "rejectContent", WorkflowException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::rejectContent] END");
		}
	}

	public Mimetype[] getMimetype(Mimetype mimetype)
	throws InvalidParameterException, RemoteException{
		logger.debug("[EcmEngineManagementBean::getMimeType] BEGIN");
		if(mimetype==null || mimetype.getFileExtension()==null || mimetype.getFileExtension().length()<1){
			throw new InvalidParameterException("DTO non valido.");
		}
		Mimetype[] risposta=null;
		start(); // Avvia stopwatch

		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			risposta = mimetypeService.getMimetype( mimetype );

		} catch (EcmEngineFoundationException e) {
			logger.error("[EcmEngineManagementBean::getMimeType] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new RemoteException("Backend services error: " + e.getCode());
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getMimeType] END");
		}
		return risposta;
	}

	public FileFormatInfo[] getFileFormatInfo(Node node, Content content, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException,
	RemoteException, EcmEngineException {
		logger.debug("[EcmEngineManagementBean::getFileFormatInfo] BEGIN");

		validate(ValidationType.NODE, "node", node);

		final String logCtx = "";

		start(); // Avvia stopwatch

		it.doqui.index.fileformat.dto.FileFormatInfo[] result = null;
		FileFormatInfo[] result_transform=null;
		//UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();
		FileFormatInputStream is=null;

		try {
			authenticateOnRepository(context, null);
			//AF: qui era presente lo switch sul tenant temporaneo - tolto perche' essendo su
			//su un metodo di management si e' pensato di estendere la funzionalita' a qualsiasi nodo:
			//questo comporta pero' la definizione da parte del chiamante del context esatto dove si trova il nodo.
			dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Autenticazione completata");

			//transaction.begin();

			//final NodeRef contentNodeRef = checkNodeExists(node, transaction);
			final NodeRef contentNodeRef = checkNodeExists(node, null);
			dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Nodo verificato");

			// DO Generalizzare property che contiene il contenuto

			QName contentProperty = null;
			try {
				contentProperty = dictionaryService.resolvePrefixNameToQName(
						content.getContentPropertyPrefixedName());
				PropertyDefinition contentPropertyDef = dictionaryService.getProperty(contentProperty);
				if (contentPropertyDef == null || !contentPropertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
					throw new InvalidParameterException("Invalid content property: "+content.getContentPropertyPrefixedName());
				}
			} catch(DictionaryRuntimeException dre) {
				throw new InvalidParameterException("Invalid content property: "+content.getContentPropertyPrefixedName());
			}

			Object contentData = nodeService.getProperty(contentNodeRef, contentProperty);
			dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Lettura property del contenuto");

			if (contentData != null && contentData instanceof ContentData) {
				// TODO Completare implementazione
				//String contentUrl = ((ContentData)contentData).getContentUrl();
				final boolean isEncrypted = nodeService.hasAspect(contentNodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] IsEncrypted: " + isEncrypted);
				final boolean encryptionSupported = contentService.supportsCryptography();
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] EncryptionSupported: " + encryptionSupported);
				CustomSecretKey decryptionKey = null;
				CryptoTransformationSpec decryptionSpec = null;
				String decryptionTransformation = null;
				byte [] iv = null;
				if (isEncrypted && encryptionSupported) {
					decryptionKey = new CustomSecretKey(content.getEncryptionInfo().getAlgorithm(),
							content.getEncryptionInfo().getKey().getBytes());
					logger.debug("[EcmEngineManagementBean::getFileFormatInfo] Algorithm: " + decryptionKey.getAlgorithm() + " - Key: " + decryptionKey.getEncoded());

					decryptionTransformation = (String) nodeService.getProperty(contentNodeRef,
							EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

					decryptionSpec = CryptoTransformationSpec.buildTransformationSpec(decryptionTransformation);

					if (decryptionSpec.getMode() != null && !decryptionSpec.getMode().equalsIgnoreCase("ECB")) {
						iv = Base64.decode((String) nodeService.getProperty(contentNodeRef,
								EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));

						decryptionSpec.setIv(iv);
					}

					logger.debug("[EcmEngineManagementBean::getFileFormatInfo] Ottenuto stato crittazione per il nodo: " + contentNodeRef);
					logger.debug("[EcmEngineManagementBean::getFileFormatInfo] Decryption: " + decryptionTransformation);
				}
				final ContentReader reader = (isEncrypted && encryptionSupported)
				? contentService.getDecryptingReader(contentNodeRef, contentProperty, decryptionKey, decryptionSpec)
						: contentService.getReader(contentNodeRef, contentProperty);
				//viene utilizzato un seocndo reader per leggere la dimensione del file in quanto due invocazioni di getContentInputStream() sullo stesso reader generano una eccezione.
				final ContentReader len_reader = (isEncrypted && encryptionSupported)
				? contentService.getDecryptingReader(contentNodeRef, contentProperty, decryptionKey, decryptionSpec)
						: contentService.getReader(contentNodeRef, contentProperty);
				ChildAssociationRef parentRef = nodeService.getPrimaryParent(contentNodeRef);
				String filename=((parentRef.getQName() != null)
						? dictionaryService.resolveQNameToPrefixName(parentRef.getQName())
								: null);
				//calcolo della dimensione del file
				InputStream len_is=len_reader.getContentInputStream();
				int len_i=0;
				int len_n=0;
				byte[] len_byte=new byte[1048576];
				while((len_i=len_is.read(len_byte))>=0){
					len_n=len_n+len_i;
				}
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] len_n: " + len_n);
				try{len_is.close();}catch(Exception e){}
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] Filename: " + filename);
				is= new FileFormatInputStream(reader.getContentInputStream(),len_n);
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] Inputstream: " + is);
				//byte[] bufprova=new byte[4096];
				//is.read(bufprova);
				//logger.debug("[EcmEngineManagementBean::getFileFormatInfo] Contenuto is:");
				//for(int i=0;i<4096;i++){
				//	logger.debug(bufprova[i]);
				//}
				result = fileFormatService.getFileFormatInfo(filename,is);
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] result: " + result);
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] result.length: " + result.length);
				result_transform=new FileFormatInfo[result.length];
				for(int i=0;i<result.length;i++){
					result_transform[i]=transformFileFormatInfo(result[i]);
					logger.debug("[EcmEngineManagementBean::getFileFormatInfo] result_transform["+i+"]: " + result_transform[i].toString());
				}
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] result_transform: " + result_transform);
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] result_transform.length: " + result_transform.length);
				dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Formato individuato");
			}
			else {
				logger.debug("[EcmEngineManagementBean::getFileFormatInfo] property non valida");
				//rollbackQuietely(transaction);
				throw new InvalidParameterException("Content property non valida o contenuto inesistente");
			}
			//transaction.commit();
		} catch (EcmEngineFoundationException e) {
			//checkCredentialsException(e, "EcmEngineManagementBean", "getFileFormatInfo", context.getUsername(), transaction);
			//checkAccessException(e, "EcmEngineManagementBean", "getFileFormatInfo", "User: " + context.getUsername(), transaction);
			checkCredentialsException(e, "EcmEngineManagementBean", "getFileFormatInfo", context.getUsername(), null);
			checkAccessException(e, "EcmEngineManagementBean", "getFileFormatInfo", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineManagementBean::getFileFormatInfo] Foundation services error: " + e.getCode());
			//rollbackQuietely(transaction);
			throw new WorkflowException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
			/*		} catch (RollbackException e) {
			this.<WorkflowException>checkIntegrityException(
					e, "EcmEngineManagementBean", "getFileFormatInfo", WorkflowException.class);

			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");*/
		} catch (java.io.IOException e) {
			handleTransactionException(e, "ioexception.");
		} finally {
			try{is.close();}catch(Exception e){}
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getFileFormatInfo] END");
		}
		return result_transform;
	}

	public FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException,
	RemoteException, EcmEngineException {
		logger.debug("[EcmEngineManagementBean::getFileFormatInfo] BEGIN");
		validate(ValidationType.NOT_NULL, "fileInfo", fileInfo);
		validate(ValidationType.NOT_NULL, "fileInfo contents", fileInfo.getContents());
		validate(ValidationType.NOT_NULL, "fileInfo name", fileInfo.getName());
		validate(ValidationType.NOT_ZERO, "fileInfo name", new Long(fileInfo.getName().length()));
		//AF: Sostituito controllo ad if con controlli validate.
		/*if(fileInfo==null || fileInfo.getContents()==null || fileInfo.getName()==null || fileInfo.getName().length()<1){
			throw new InvalidParameterException("DTO fileinfo non valido");
		}*/
		final String logCtx = "FILE: "+fileInfo.getName();
		start(); // Avvia stopwatch
		it.doqui.index.fileformat.dto.FileFormatInfo[] result = null;
		FileFormatInfo[] result_transform=null;
		String uid_temp=null;
		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();
		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Autenticazione completata");
			transaction.begin();
			it.doqui.index.fileformat.dto.FileInfo fileInfo_transform=transformFileInfo(fileInfo);
			result = fileFormatService.getFileFormatInfo(fileInfo_transform);
			dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Formato individuato.");
			transaction.commit();
			if(fileInfo.isStore()){
				if(isTemporaneyPresent()){
                    OperationContext temp = getTemporaneyContext(context);

					Node parent_node = new Node();
					parent_node.setUid(getTemporaneyParentID());

					String contentName = getTemporaneyContentName();
					Property[] props = new Property[1];
					props[0] = createPropertyDTO("cm:name", "text", false);
					props[0].setValues(new String [] { contentName });
					Property [] authorProps = new Property[1];
					authorProps[0] = createPropertyDTO("cm:author", "text", false);
					authorProps[0].setValues(new String [] { temp.getUsername() + " da fileformat" });
					Property [] titledProps = new Property[2];
					titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
					titledProps[0].setValues(new String [] { contentName });
					titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
					titledProps[1].setValues(new String [] { "Contenuto aggiunto da fileformat." });
					Aspect titled = new Aspect();
					titled.setPrefixedName("cm:titled");
					titled.setModelPrefixedName("cm:contentmodel");
					titled.setProperties(titledProps);
					Content content = new Content();
					content.setPrefixedName("cm:" + contentName);
					content.setParentAssocTypePrefixedName("cm:contains");
					content.setModelPrefixedName("cm:contentmodel");
					content.setTypePrefixedName("cm:content");
					content.setContentPropertyPrefixedName("cm:content");
					content.setMimeType("application/octet-stream");
					content.setEncoding("UTF-8");
					content.setContent(fileInfo.getContents());
					content.setProperties(props);
					Node responseNodo=createContent(parent_node, content, temp);
					uid_temp=responseNodo.getUid();
					dumpElapsed("EcmEngineManagementBean", "getFileFormatInfo", logCtx, "Nodo temporaneo creato.");
				} else {
					//TODO: AF: Da sostituire con una eccezione piu' parlante, anche se non retrocompatibile.
					throw new EcmEngineException("Tenant Temporaney does not exists.");
				}
			}

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getFileFormatInfo", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getFileFormatInfo", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::getFileFormatInfo] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new WorkflowException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<WorkflowException>checkIntegrityException(
					e, "EcmEngineManagementBean", "getFileFormatInfo", WorkflowException.class);

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
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::getFileFormatInfo] END");
		}
		result_transform=new FileFormatInfo[result.length];
		for(int i=0;i<result.length;i++){
			result_transform[i]=transformFileFormatInfo(result[i]);
			result_transform[i].setUid(uid_temp);
		}
		return result_transform;
	}

	public FileFormatVersion getFileFormatVersion(OperationContext context)throws InvalidParameterException, InvalidCredentialsException, RemoteException, EcmEngineException{
		FileFormatVersion result=null;
		logger.debug("[EcmEngineManagementBean::getFileFormatVersion] BEGIN");
		start();
		try{
			authenticateOnRepository(context, null);
			result=translateFileFormatVersion(fileFormatService.getFileFormatVersion());
		}catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getFileFormatVersion", context.getUsername(), null);
			checkAccessException(e, "EcmEngineManagementBean", "getFileFormatVersion", "User: " + context.getUsername(), null);
			logger.error("[EcmEngineManagementBean::getFileFormatVersion] Foundation services error: " + e.getCode());
			throw new EcmEngineException(e.getMessage());
		}
		finally{
			logger.debug("[EcmEngineManagementBean::getFileFormatVersion] END");
			stop();
		}
		return result;
	}

	public Node createCategory(Node categoryParent, Category category,OperationContext context) throws InvalidParameterException,
	InsertException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
	EcmEngineTransactionException
	{
		logger.debug("[EcmEngineManagementBean::createCategory] BEGIN");

		validate(ValidationType.NODE             , "categoryParent" , categoryParent);
		validate(ValidationType.CATEGORY         , "category", category);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		String categoryName = category.getName();

		final String logCtx = "CP: " + categoryParent.getUid() + " - CAN: " + categoryName;

		Node result = null;

		start(); // Avvia stopwatch
		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try{

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "createCategory", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef categoryParentRef = checkNodeExists(categoryParent, transaction);

			final NodeRef nodeRef = categoryService.createCategory(categoryParentRef, categoryName);

			result = new Node();
			result.setUid(nodeRef.getId());

			dumpElapsed("EcmEngineManagementBean", "createCategory", logCtx, "Category creata.");
			logger.debug("[EcmEngineManagementBean::createRootCategory] Category Uid : "+result.getUid());

			//INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "createCategory", logCtx, context, result.getUid(),"Category Name: " + category.getName());
			transaction.commit();

		}catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "createCategory", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "createCategory", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::createCategory] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		} catch (InvalidParameterException ipe) {
			logger.error("[EcmEngineManagementBean::createCategory] Invalid parameter error: " + ipe.getMessage());
			rollbackQuietely(transaction);
			throw ipe;
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "createCategory", InsertException.class);

			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		}finally{
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::createCategory] END");
		}
		return result;
	}

	public Node createRootCategory(Category rootCategory,OperationContext context) throws InvalidParameterException,
	InsertException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
	EcmEngineTransactionException
	{
		logger.debug("[EcmEngineManagementBean::createRootCategory] BEGIN");
		//validate(ValidationType.NODE             , "parent" , parent);
		validate(ValidationType.CATEGORY_ROOT    , "rootCategory", rootCategory);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		String categoryName = rootCategory.getName();

		//final String logCtx = "P: " + parent.getUid() + " - CARN: " + categoryName;
		final String logCtx = "CARN: " + categoryName + "- USER: " +context.getUsername() ;

		Node result = null;

		start(); // Avvia stopwatch
		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try{

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "createRootCategory", logCtx, "Autenticazione completata");

			transaction.begin();

			//final NodeRef parentRef = checkNodeExists(parent, transaction);
			//StoreRef storeRef = parentRef.getStoreRef();

			//Lo SpacesStore STANDARD
			StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			final QName aspectName = dictionaryService.resolvePrefixNameToQName(rootCategory.getAspectPrefixedName());

			//final NodeRef nodeRef = categoryService.createRootCategory(storeRef, aspectName, categoryName);

			final NodeRef nodeRef = categoryService.createRootCategory(SPACES_STORE, aspectName, categoryName);

			result = new Node();
			result.setUid(nodeRef.getId());

			dumpElapsed("EcmEngineManagementBean", "createRootCategory", logCtx, "Category Root creata.");
			logger.debug("[EcmEngineManagementBean::createRootCategory] Category Root Uid : "+result.getUid());

			//INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "createRootCategory", logCtx, context,
					result.getUid(),"Category Root Name: " + rootCategory.getName() +" [Aspect : "+rootCategory.getAspectPrefixedName()+"]");
			transaction.commit();

		}catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "createRootCategory", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "createRootCategory", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::createRootCategory] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		} catch (InvalidParameterException ipe) {
			logger.error("[EcmEngineManagementBean::createRootCategory] Invalid parameter error: " + ipe.getMessage());
			rollbackQuietely(transaction);
			throw ipe;
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "createRootCategory", InsertException.class);

			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		}finally{
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::createRootCategory] END");
		}
		return result;
	}

	public void deleteCategory(Node categoryNode,OperationContext context) throws InvalidParameterException,
	NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException
	{
		logger.debug("[EcmEngineManagementBean::deleteCategory] BEGIN");

		validate(ValidationType.NODE, "categoryNode", categoryNode);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "CAN: " + categoryNode.getUid();

		start(); // Avvia stopwatch
		UserTransaction transaction = transactionService.getService().getNonPropagatingUserTransaction();

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineManagementBean", "deleteCategory", logCtx, "Autenticazione completata");

			transaction.begin();

			final NodeRef nodeRef = checkNodeExists(categoryNode, transaction);

			categoryService.deleteCategory(nodeRef);

			dumpElapsed("EcmEngineManagementBean", "deleteCategory", logCtx, "Commit transazione.");
			logger.debug("[EcmEngineManagementBean::deleteCategory] Nodo Category eliminato");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineManagementBean", "deleteCategory", logCtx, context, categoryNode.getUid(),
					categoryNode.getUid());

			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "deleteCategory", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "deleteCategory", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineManagementBean::deleteCategory] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new DeleteException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(
					e, "EcmEngineManagementBean", "deleteCategory", DeleteException.class);

			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		}finally{
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineManagementBean::deleteCategory] END");
		}
	}

	public Node copyNode(Node source, Node parent, OperationContext context)
	throws InvalidParameterException, InsertException, CopyException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException{
		logger.debug("[EcmEngineManagementBean::copyNode] BEGIN");
		start();
		UserTransaction transaction=null;
		Node result=null;
		try{
			try{
				ResultAssociation[] resultassociation=getAssociations(source, "CHILD", 0, context);
				if(resultassociation!=null){
					logger.debug("[EcmEngineManagementBean::copyNode] Trovate "+resultassociation.length+" associazioni CHILD.");
					if(resultassociation.length>10){
						throw new Exception("Too many children for source node. (MAX 10)");
					}
				}
				else{
					logger.debug("[EcmEngineManagementBean::copyNode] Trovate 0 associazioni CHILD.");
				}
			}catch(Exception e){
				throw new CopyException(e.getMessage());
			}
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			authenticateOnRepository(context, null);
			transaction.begin();
			NodeRef refSource=checkNodeExists(source, transaction);
			NodeRef refParent=checkNodeExists(parent, transaction);
			ChildAssociationRef sourceParentRef = nodeService.getPrimaryParent(refSource);
			QName destinationAssocTypeQName = sourceParentRef.getTypeQName();
			QName destinationQName = sourceParentRef.getQName();
			NodeRef copyNodeRef = copyService.copyAndRename(refSource, refParent,destinationAssocTypeQName,destinationQName, false);
			result=new Node();
			result.setUid(copyNodeRef.getId());
			dumpElapsed("EcmEngineManagementBean", "copyNode", "SOURCE: "+source.getUid()+" - DEST: "+parent.getUid(), "Nodi copiati.");
			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "copyNode", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "copyNode", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::copyNode] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InvalidCredentialsException("Backend services error: " + e.getCode());
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported:\n"+e.getMessage());
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(e, "EcmEngineManagementBean", "copyNode", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			logger.debug("[EcmEngineManagementBean::copyNode] END");
			stop();
		}
		return result;
	}

	public void moveNode(Node source, Node parent, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException{
		UserTransaction transaction=null;
		start();
		try{
			logger.debug("[EcmEngineManagementBean::moveNode] BEGIN");
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			authenticateOnRepository(context, null);
			transaction.begin();
			NodeRef refSource=checkNodeExists(source, transaction);
			NodeRef refParent=checkNodeExists(parent, transaction);
			ChildAssociationRef sourceParentRef = nodeService.getPrimaryParent(refSource);
			QName destinationAssocTypeQName = sourceParentRef.getTypeQName();
			QName destinationQName = sourceParentRef.getQName();
			/*NodeRef copyNodeRef = copyService.copyAndRename(refSource, refParent,destinationAssocTypeQName,destinationQName, false);
			result=new Node();
			result.setUid(copyNodeRef.getId());
			nodeService.deleteNode(refSource);*/
			nodeService.moveNode(refSource, refParent,destinationAssocTypeQName,destinationQName);
			dumpElapsed("EcmEngineManagementBean", "moveNode", "SOURCE: "+source.getUid()+" - DEST: "+parent.getUid(), "Nodi spostati.");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "copyNode", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "copyNode", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::copyNode] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InvalidCredentialsException("Backend services error: " + e.getCode());
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(e, "EcmEngineManagementBean", "moveNode", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			logger.debug("[EcmEngineManagementBean::moveNode] END");
			stop();
		}
	}

	private FileFormatInfo transformFileFormatInfo(it.doqui.index.fileformat.dto.FileFormatInfo ffi)
	{
		FileFormatInfo res=new FileFormatInfo();
		res.setDescription(ffi.getDescription());
		res.setFormatVersion(ffi.getFormatVersion());
		res.setMimeType(ffi.getMimeType());
		res.setPuid(ffi.getPuid());
		res.setTypeCode(ffi.getTypeCode());
		res.setTypeDescription(ffi.getTypeDescription());
		res.setWarning(ffi.getWarning());
		res.setIdentificationDate(ffi.getIdentificationDate());
		return res;
	}
	private it.doqui.index.fileformat.dto.FileInfo transformFileInfo(FileInfo fileInfo)
	{
		it.doqui.index.fileformat.dto.FileInfo res=new it.doqui.index.fileformat.dto.FileInfo();
		//byte[] c1=fileInfo.getContents();
		//byte[] c2=new byte[c1.length];
		//for(int i=0; i<c1.length;i++){
		//c2[i]=c1[i];
		//}
		//res.setContents(c2);
		res.setContents(fileInfo.getContents());
		res.setName(fileInfo.getName());
		return res;
	}
	private FileFormatVersion translateFileFormatVersion(it.doqui.index.fileformat.dto.FileFormatVersion version){
		FileFormatVersion result=new FileFormatVersion();
		result.setVersion(version.getVersion());
		return result;
	}

	private Version[] translateVersionHistory(VersionHistory versionHistory) {
		logger.debug("[EcmEngineManagementBean::translateVersionHistory] BEGIN");
		Vector<Version> versions = new Vector<Version>();
		try {
			if (versionHistory != null) {
				org.alfresco.service.cmr.version.Version currentVersion = versionHistory.getRootVersion();
				while (currentVersion != null) {
					versions.add(translateVersion(currentVersion));
					Collection<org.alfresco.service.cmr.version.Version> successorList = versionHistory.getSuccessors(currentVersion);
					if (successorList != null && successorList.size() > 0) {
						currentVersion = successorList.iterator().next();
					} else {
						currentVersion = null;
					}
				}
			}
		} finally {
			logger.debug("[EcmEngineManagementBean::translateVersionHistory] END");
		}
		return versions.toArray(new Version[]{});
	}

	private Version translateVersion(org.alfresco.service.cmr.version.Version version) {
		logger.debug("[EcmEngineManagementBean::translateVersion] BEGIN");
		Version result = null;
		try {
			result = new Version();
			// TODO gestire frozen state node ref come property aggiuntiva del bean
			result.setVersionedNode(new Node(version.getFrozenStateNodeRef().getId(), getCurrentRepository()));
			result.setCreatedDate(version.getCreatedDate());
			result.setCreator(version.getCreator());
			result.setDescription(version.getDescription());
			result.setVersionLabel(version.getVersionLabel());
			// Translate version properties
			Map<String,Serializable> propertyMap = version.getVersionProperties();
			Property[] properties = new Property[propertyMap.size()];
			int k=0;
			for (String key : propertyMap.keySet()) {
				properties[k] = new Property();
				properties[k].setPrefixedName(key);
				if (propertyMap.get(key) != null) {
					properties[k].setValues(new String[]{ propertyMap.get(key).toString() });
				}
				k++;
			}
			result.setVersionProperties(properties);
		} finally {
			logger.debug("[EcmEngineManagementBean::translateVersion] END");
		}
		return result;
	}

	private String decodeRuleType(String ruleType) {
		String result = null;
		if (EcmEngineConstants.ECMENGINE_RULE_TYPE_INBOUND.equals(ruleType)) {
			result = RuleType.INBOUND;
		} else if (EcmEngineConstants.ECMENGINE_RULE_TYPE_OUTBOUND.equals(ruleType)) {
			result = RuleType.OUTBOUND;
		} else if (EcmEngineConstants.ECMENGINE_RULE_TYPE_UPDATE.equals(ruleType)) {
			result = RuleType.UPDATE;
		}
		return result;
	}

	//-------------------METODI DI RENDITION--------------------------------------------------------------------------

	public Node addRenditionTransformer(Node nodoXml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException  {

		Node nodoXsl = null;
		UserTransaction transaction=null;
		logger.debug("[EcmEngineManagementBean::addRenditionTransformer] BEGIN");
		start();
		validate(ValidationType.NODE,"Nodo XML",nodoXml);
		validate(ValidationType.CONTENT_WRITE_NEW,"Rendition Transformer",renditionTransformer);
		validate(ValidationType.OPERATION_CONTEXT,"Operation Context",context);
		try{
			SearchParams searchParams=new SearchParams();
			searchParams.setXPathQuery("/app:company_home/cm:rendition");
			Node cartellaRendition = null;
			cartellaRendition = xpathSearchNoMetadata(searchParams,context).getNodeArray()[0];

			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);

			NodeRef nodeRefXml = checkNodeExists(nodoXml,transaction);

			//controllo se sotto nodoXml c'è veramente un XML
			ContentReader xml = contentService.getReader(nodeRefXml, ContentModel.PROP_CONTENT);
			if(!xml.getMimetype().equals("application/xml")){
				logger.error("[EcmEngineManagementBean::addRenditionTransformer] Il contenuto a cui si vuole associare un RenditionTransformer non è un XML.");
				throw new PermissionDeniedException("Operazione annullata. Il contenuto a cui si vuole associare un RenditionTransformer non è un XML.");
			}
			else{
				nodoXsl = createContentNoTransaction(cartellaRendition, renditionTransformer, context);
				renditionTransformer.setNodeId(nodoXsl.getUid());

				//aggiungo già all'XSL l'Aspect RenditionTransformer, con la Property vuota
				HashMap<QName, Serializable> transformerProps = new HashMap<QName, Serializable>(2);
				String rendition = new String();
				String genMimeType = new String();
				transformerProps.put(EcmEngineModelConstants.PROP_RENDITION_ID, rendition);
				transformerProps.put(EcmEngineModelConstants.PROP_RENDITION_GENMIMETYPE, genMimeType);
				NodeRef nodeRefXsl = checkNodeExists(nodoXsl,transaction);
				nodeService.addAspect(nodeRefXsl, EcmEngineModelConstants.ASPECT_RENDITIONTRANSFORMER, transformerProps);

				// se il nodo non ha associato l'Aspect Renditionable
				if(! nodeService.hasAspect(nodeRefXml, EcmEngineModelConstants.ASPECT_RENDITIONABLE)){

					//aggiungo all'XML l'Aspect Renditionable, settando come Property l'UID dell'XSL
					HashMap<QName, Serializable> renditionProps = new HashMap<QName, Serializable>(1);
					List<String> listaDegliXsl = new ArrayList<String>();
					listaDegliXsl.add(renditionTransformer.getNodeId());
					renditionProps.put(EcmEngineModelConstants.PROP_RENDITION_XSL_ID, (Serializable)listaDegliXsl);
					nodeService.addAspect(nodeRefXml, EcmEngineModelConstants.ASPECT_RENDITIONABLE, renditionProps);
				}

				// se il nodo ha già associato l'Aspect Renditionable
				else{
					List<String> listaDegliXsl = (List<String>) nodeService.getProperty(nodeRefXml, EcmEngineModelConstants.PROP_RENDITION_XSL_ID);
					listaDegliXsl.add(renditionTransformer.getNodeId());
					// setProperty() funziona anche da update
					nodeService.setProperty(nodeRefXml, EcmEngineModelConstants.PROP_RENDITION_XSL_ID, (Serializable)listaDegliXsl);
				}
				dumpElapsed("EcmEngineManagementBean", "addRenditionTransformer","NODE XML: "+nodoXml.getUid(), "Transformer associato.");
				try{
					ByteArrayOutputStream osxml = new ByteArrayOutputStream();
					xml.getContent(osxml);
					byte[] bytesxml = osxml.toByteArray();
					ByteArrayInputStream isxml=new ByteArrayInputStream(bytesxml);
					ByteArrayInputStream isxsl=new ByteArrayInputStream(renditionTransformer.getContent());
					javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(isxml);
					javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(isxsl);
					ByteArrayOutputStream osgen=new ByteArrayOutputStream();
					javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(osgen);
					javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
					javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);
					trans.transform(xmlSource, result);
					RenditionDocument rd=new RenditionDocument();
					//TODO: Come imposto rd su aspect e properties?
					rd.setContent(osgen.toByteArray());
					rd.setDescription("Generated by ECMEngine at "+new Date());
					Node nodeRendition=setRendition(nodoXsl, rd, context);
					nodeService.setProperty(new NodeRef(nodoXsl.getUid()), EcmEngineModelConstants.PROP_RENDITION_ID, nodeRendition.getUid());
					osgen.close();
					isxsl.close();
					isxml.close();
				}catch(Exception e){
					throw new InsertException(e.getMessage());
				}
				dumpElapsed("EcmEngineManagementBean", "addRenditionTransformer","NODE XML: "+nodoXml.getUid(), "Transformer associato.");
				transaction.commit();
			}
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "addRenditionTransformer", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "addRenditionTransformer", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::addRenditionTransformer] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		} catch(InvalidParameterException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(TooManyResultsException e){
			rollbackQuietely(transaction);
			throw new NoSuchNodeException("/app:company_home/cm:rendition");
		} catch(SearchException e){
			rollbackQuietely(transaction);
			throw new NoSuchNodeException("/app:company_home/cm:rendition");
		} catch(InsertException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(NoSuchNodeException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(InvalidCredentialsException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(PermissionDeniedException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(RemoteException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "addRenditionTransformer", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			logger.debug("[EcmEngineManagementBean::addRenditionTransformer] END");
			stop();
		}
		return nodoXsl;
	}

	public Node setRendition(Node nodoTransformer, RenditionDocument renditionDocument, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException  {

		Node nodoRendition = null;
		UserTransaction transaction=null;
		logger.debug("[EcmEngineManagementBean::setRendition] BEGIN");
		start();
		validate(ValidationType.NODE,"Nodo Transformer",nodoTransformer);
		validate(ValidationType.CONTENT_WRITE_NEW,"Rendition Document",renditionDocument);
		validate(ValidationType.OPERATION_CONTEXT,"Operation Context",context);
		try{
			SearchParams searchParams=new SearchParams();
			searchParams.setXPathQuery("/app:company_home/cm:rendition");
			Node cartellaRendition = null;
			cartellaRendition = xpathSearchNoMetadata(searchParams,context).getNodeArray()[0];
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);

			NodeRef nodeRefXsl = checkNodeExists(nodoTransformer,transaction);

			//controllo se il nodo dell'XSL ha associato l'Aspect RenditionTransformer
			if(! nodeService.getAspects(nodeRefXsl).contains(EcmEngineModelConstants.ASPECT_RENDITIONTRANSFORMER)){
				logger.error("[EcmEngineManagementBean::setRendition] Il nodo a cui si vuole associare una rendition non è un trasformatore XSL.");
				throw new PermissionDeniedException("Operazione annullata. Il nodo a cui si vuole associare una rendition non è un trasformatore XSL.");
			}
			else{
				nodoRendition = createContentNoTransaction(cartellaRendition, renditionDocument, context);
				renditionDocument.setNodeId(nodoRendition.getUid());
				String renditionId = renditionDocument.getNodeId();
				nodeService.setProperty(nodeRefXsl, EcmEngineModelConstants.PROP_RENDITION_ID, renditionId);
			}
			dumpElapsed("EcmEngineManagementBean", "setRendition","NODE TRANSFORMER: "+nodoTransformer.getUid(), "Rendition impostata.");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "setRendition", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "setRendition", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::setRendition] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		} catch(InvalidParameterException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(TooManyResultsException e){
			rollbackQuietely(transaction);
			throw new NoSuchNodeException("/app:company_home/cm:rendition");
		} catch(SearchException e){
			rollbackQuietely(transaction);
			throw new NoSuchNodeException("/app:company_home/cm:rendition");
		} catch(InsertException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(NoSuchNodeException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(InvalidCredentialsException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(PermissionDeniedException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(RemoteException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "setRendition", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			logger.debug("[EcmEngineManagementBean::setRendition] END");
			stop();
		}
		return nodoRendition;
	}

	public RenditionTransformer getRenditionTransformer(Node nodoTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{

		RenditionTransformer renditionTransformer = null;
		UserTransaction transaction=null;
		logger.debug("[EcmEngineManagementBean::getRenditionTransformer] BEGIN");
		start();
		validate(ValidationType.NODE,"Nodo Transformer",nodoTransformer);
		validate(ValidationType.OPERATION_CONTEXT,"Operation Context",context);
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);

			NodeRef nodeRefXsl = checkNodeExists(nodoTransformer,transaction);
			ContentReader xsl = contentService.getReader(nodeRefXsl, ContentModel.PROP_CONTENT);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			xsl.getContent(os);
			byte[] bytes = os.toByteArray();
			renditionTransformer = new RenditionTransformer();
			renditionTransformer.setContent(bytes);
			renditionTransformer.setNodeId(nodoTransformer.getUid());
			renditionTransformer.setMimeType(xsl.getMimetype());
			String desc=(String)nodeService.getProperty(new NodeRef(nodoTransformer.getUid()),EcmEngineModelConstants.PROP_RENDITION_TRANSFORMER_DESCRIPTION);
			renditionTransformer.setDescription(desc);
			os.close();

			dumpElapsed("EcmEngineManagementBean", "getRenditionTransformer","NODE TRANSFORMER: "+nodoTransformer.getUid(), "Rendition Transformer restituito.");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "copyNode", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "copyNode", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::copyNode] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		}  catch(RemoteException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "copyNode", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			logger.debug("[EcmEngineManagementBean::addRenditionTransformer] END");
			stop();
		}
		return renditionTransformer;
	}

	public RenditionDocument getRendition(Node nodoTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{

		RenditionDocument renditionDocument = null;
		UserTransaction transaction=null;
		logger.debug("[EcmEngineManagementBean::getRendition] BEGIN");
		start();
		validate(ValidationType.NODE,"Nodo Transformer",nodoTransformer);
		validate(ValidationType.OPERATION_CONTEXT,"Operation Context",context);
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);

			NodeRef nodeRefXsl = checkNodeExists(nodoTransformer,transaction);
			String renditionId = (String) nodeService.getProperty(nodeRefXsl, EcmEngineModelConstants.PROP_RENDITION_ID);
			Node nodoRendition = new Node(renditionId);
			NodeRef nodoRefRendition = checkNodeExists(nodoRendition, transaction);
			ContentReader rendition = contentService.getReader(nodoRefRendition, ContentModel.PROP_CONTENT);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			rendition.getContent(os);
			byte[] bytes = os.toByteArray();
			renditionDocument = new RenditionDocument();
			renditionDocument.setContent(bytes);
			renditionDocument.setNodeId(nodoRendition.getUid());
			String desc=(String)nodeService.getProperty(new NodeRef(nodoRendition.getUid()),EcmEngineModelConstants.PROP_RENDITION_DOCUMENT_DESCRIPTION);
			renditionDocument.setDescription(desc);

			dumpElapsed("EcmEngineManagementBean", "getRendition","NODE TRANSFORMER: "+nodoTransformer.getUid(), "Rendition restituita.");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getRendition", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getRendition", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::getRendition] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		}   catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "getRendition", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} finally{
			logger.debug("[EcmEngineManagementBean::getRendition] END");
			stop();
		}
		return renditionDocument;
	}

	public void deleteRenditionTransformer(Node xml, Node renditionTransformer, OperationContext context)throws InvalidParameterException, DeleteException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		logger.debug("[EcmEngineManagementBean::deleteRenditionTransformer] BEGIN");
		validate(ValidationType.NODE,"Nodo XML",xml);
		validate(ValidationType.NODE,"Nodo Transofrmer",renditionTransformer);
		validate(ValidationType.OPERATION_CONTEXT,"Operation context",context);
		start();
		UserTransaction transaction=null;
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);

			checkNodeExists(xml, transaction);
			checkNodeExists(renditionTransformer, transaction);
			String renditionuid=(String)nodeService.getProperty(new NodeRef(renditionTransformer.getUid()),EcmEngineModelConstants.PROP_RENDITION_ID);
			deleteContentNoTransaction(new Node(renditionuid), context);
			deleteContentNoTransaction(renditionTransformer, context);
			List<String> xsl=(List<String>)nodeService.getProperty(new NodeRef(xml.getUid()),EcmEngineModelConstants.PROP_RENDITION_XSL_ID);
			xsl.remove(renditionTransformer.getUid());
			nodeService.setProperty(new NodeRef(xml.getUid()),EcmEngineModelConstants.PROP_RENDITION_XSL_ID,(Serializable)xsl);

			dumpElapsed("EcmEngineManagementBean", "deleteRenditionTransformer","NODE TRANSFORMER: "+renditionTransformer.getUid(), "Rendition Transformer rimosso.");
			transaction.commit();
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getRendition", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getRendition", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::getRendition] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new DeleteException("Backend services error: " + e.getCode());
		}  catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(
					e, "EcmEngineManagementBean", "getRendition", DeleteException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			stop();
			logger.debug("[EcmEngineManagementBean::deleteRenditionTransformer] END");
		}
	}

	public RenditionTransformer[] getRenditionTransformers(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		logger.debug("[EcmEngineManagementBean::getRenditionTransformers] BEGIN");
		validate(ValidationType.NODE,"Nodo XML",xml);
		validate(ValidationType.OPERATION_CONTEXT,"Operation context",context);
		start();
		UserTransaction transaction=null;
		RenditionTransformer[] result=null;
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);
			checkNodeExists(xml, transaction);
			List<String> xsl=(List<String>)nodeService.getProperty(new NodeRef(xml.getUid()),EcmEngineModelConstants.PROP_RENDITION_XSL_ID);
			result=new RenditionTransformer[xsl.size()];
			for(int i=0;i<result.length;i++){
				NodeRef nodort = checkNodeExists(new Node(xsl.get(i)), transaction);
				result[i]=new RenditionTransformer();
				result[i].setNodeId(nodort.getId());
				String desc=(String)nodeService.getProperty(nodort,EcmEngineModelConstants.PROP_RENDITION_TRANSFORMER_DESCRIPTION);
				result[i].setDescription(desc);
				ContentReader rendition = contentService.getReader(nodort, ContentModel.PROP_CONTENT);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				rendition.getContent(os);
				result[i].setContent(os.toByteArray());
			}
			dumpElapsed("EcmEngineManagementBean", "getRenditionTransformers","NODE XML: "+xml.getUid(), "Rendition Transformers restituiti.");
			transaction.commit();
			return result;
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getRendition", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getRendition", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::getRendition] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new PermissionDeniedException("Backend services error: " + e.getCode());
		}  catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<PermissionDeniedException>checkIntegrityException(
					e, "EcmEngineManagementBean", "getRendition", PermissionDeniedException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			stop();
			logger.debug("[EcmEngineManagementBean::getRenditionTransformers] END");
		}
		return result;
	}

	public RenditionDocument[] getRenditions(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		logger.debug("[EcmEngineManagementBean::getRenditions] BEGIN");
		validate(ValidationType.NODE,"Nodo XML",xml);
		validate(ValidationType.OPERATION_CONTEXT,"Operation context",context);
		start();
		UserTransaction transaction=null;
		RenditionDocument[] result=null;
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			authenticateOnRepository(context, transaction);
			checkNodeExists(xml, transaction);
			List<String> xsl=(List<String>)nodeService.getProperty(new NodeRef(xml.getUid()),EcmEngineModelConstants.PROP_RENDITION_XSL_ID);
			result=new RenditionDocument[xsl.size()];
			for(int i=0;i<result.length;i++){
				NodeRef nodort = checkNodeExists(new Node(xsl.get(i)), transaction);
				String unodor=(String)nodeService.getProperty(nodort,EcmEngineModelConstants.PROP_RENDITION_ID);
				NodeRef nodor = checkNodeExists(new Node(unodor), transaction);
				result[i]=new RenditionDocument();
				result[i].setNodeId(nodor.getId());
				String desc=(String)nodeService.getProperty(nodor,EcmEngineModelConstants.PROP_RENDITION_DOCUMENT_DESCRIPTION);
				result[i].setDescription(desc);
				ContentReader rendition = contentService.getReader(nodor, ContentModel.PROP_CONTENT);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				rendition.getContent(os);
				result[i].setContent(os.toByteArray());
			}
			dumpElapsed("EcmEngineManagementBean", "getRenditions","NODE XML: "+xml.getUid(), "Renditions restituite.");
			transaction.commit();
			return result;
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "getRendition", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "getRendition", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::getRendition] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new PermissionDeniedException("Backend services error: " + e.getCode());
		}  catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<PermissionDeniedException>checkIntegrityException(
					e, "EcmEngineManagementBean", "getRendition", PermissionDeniedException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			stop();
			logger.debug("[EcmEngineManagementBean::getRenditions] END");
		}
		return result;
	}

	public RenditionDocument generateRendition(Content xml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException{
		RenditionDocument resultrend=null;
		logger.debug("[EcmEngineManagementBean::generateRendition] BEGIN");
		start();
		try{
			authenticateOnRepository(context, null);
			ByteArrayInputStream isxml=new ByteArrayInputStream(xml.getContent());
			ByteArrayInputStream isxsl=new ByteArrayInputStream(renditionTransformer.getContent());
			javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(isxml);
			javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(isxsl);
			ByteArrayOutputStream osgen=new ByteArrayOutputStream();
			javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(osgen);
			javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
			javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, result);
			resultrend=new RenditionDocument();
			resultrend.setContent(osgen.toByteArray());
			osgen.close();
			isxsl.close();
			isxml.close();
		}catch(Exception e){
			throw new InvalidParameterException(e.getMessage());
		}finally{
			stop();
			logger.debug("[EcmEngineManagementBean::generateRendition] END");
		}
		return resultrend;
	}
}

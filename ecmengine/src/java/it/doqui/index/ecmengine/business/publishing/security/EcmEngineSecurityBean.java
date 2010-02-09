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

package it.doqui.index.ecmengine.business.publishing.security;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import javax.transaction.NotSupportedException;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;

import org.alfresco.service.cmr.repository.ContentData;

import org.alfresco.service.cmr.repository.ContentReader;

import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.Base64;

import it.doqui.dosign.dosign.dto.envelope.EnvelopedBuffer;
import it.doqui.dosign.dosign.dto.signature.SignedBuffer;

import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.CustomSecretKey;

import it.doqui.index.ecmengine.business.publishing.EcmEngineFeatureBean;

import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.security.Document;
import it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent;
import it.doqui.index.ecmengine.dto.engine.security.Signature;
import it.doqui.index.ecmengine.dto.engine.security.VerifyReport;

import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException;
import it.doqui.index.ecmengine.integration.exception.EcmEngineIntegrationException;
import it.doqui.index.ecmengine.integration.security.EcmEngineIntegrationSecurityDelegate;
import it.doqui.index.ecmengine.integration.security.EcmEngineIntegrationSecurityFactory;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

public class EcmEngineSecurityBean extends EcmEngineFeatureBean{
//TODO: creare una transazione intorno a createContentNoTransaction

	private static final long serialVersionUID = 6318502163631402227L;

	public VerifyReport verifyDocument(EnvelopedContent envelopedContent, OperationContext context) throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,RemoteException{
		logger.debug("[EcmEngineSecurityBean::verifyDocument] BEGIN");
		final String logCtx = "U: " +context.getUsername();
		VerifyReport response=new VerifyReport();
		start();
		try{
            authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineSecurityBean", "verifyDocument", logCtx, "Autenticazione completata");

			SignedBuffer buffer=new SignedBuffer();
			buffer.setBuffer(envelopedContent.getData());

			logger.debug("[EcmEngineSecurityBean::verifyDocument] Pre getSecurityDelegate");
			EcmEngineIntegrationSecurityDelegate delegate=EcmEngineIntegrationSecurityFactory.getSecurityDelegate();
			response=transformVeryfyReport(delegate.verifyDocument(buffer));
			if(logger.isDebugEnabled()) {
			    logger.debug("[EcmEngineSecurityBean::verifyDocument] Post getSecurityDelegate - response: " + response.toString());
            }

			if(envelopedContent.isStore()){
                OperationContext temp = getTemporaneyContext( context );

                Node parent_node = new Node();
                parent_node.setUid(getTemporaneyParentID());

                String contentName = getTemporaneyContentName();

                Property[] props = new Property[1];
                props[0] = createPropertyDTO("cm:name", "text", false);
                props[0].setValues(new String [] { contentName });
                Property [] authorProps = new Property[1];
                authorProps[0] = createPropertyDTO("cm:author", "text", false);
                authorProps[0].setValues(new String [] { temp.getUsername() + " da security" });
                Property [] titledProps = new Property[2];
                titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
                titledProps[0].setValues(new String [] { contentName });
                titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
                titledProps[1].setValues(new String [] { "Contenuto aggiunto da security." });
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
                content.setContent(envelopedContent.getData());
                content.setProperties(props);
                logger.debug("[EcmEngineSecurityBean::verifyDocument] Pre-createcontent");
                try{
                    Node responseNodo=createContentNoTransaction(parent_node, content, temp);
                    response.setUid(responseNodo.getUid());
                    logger.debug("[EcmEngineSecurityBean::verifyDocument] Post-createcontent - uid: " + responseNodo.getUid());
                } catch(Exception e) {
                    logger.error("[EcmEngineSecurityBean::verifyDocument] InsertException");
                    throw new InsertException(e.getMessage());
                }
			}
			dumpElapsed("EcmEngineSecurityBean", "verifyDocument", logCtx, "Verifica completata");

		}catch(EcmEngineIntegrationException e){
			logger.error("[EcmEngineSecurityBean::verifyDocument] EcmEngineIntegrationException");
			throw new EcmEngineTransactionException(e.getMessage());
		}catch(AuthenticationRuntimeException e){
			logger.error("[EcmEngineSecurityBean::verifyDocument] AuthenticationRuntimeException");
			throw new InvalidCredentialsException(e.getMessage());
		}finally{
			logger.debug("[EcmEngineSecurityBean::verifyDocument] END");
			stop();
		}
		return response;
	}

	public VerifyReport verifyDocument(Node node, OperationContext context) throws InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,RemoteException{
		logger.debug("[EcmEngineSecurityBean::verifyDocument] BEGIN");
		final String logCtx = "U: " +context.getUsername();
		VerifyReport response=new VerifyReport();
		UserTransaction transaction =null;
		start();
		try{
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineSecurityBean", "verifyDocument", logCtx, "Autenticazione completata");

			transaction = transactionService.getService().getNonPropagatingUserTransaction();

            OperationContext temp = getTemporaneyContext( context );
			authenticateOnRepository(temp, null);
			dumpElapsed("EcmEngineSecurityBean", "verifyDocument", logCtx, "Autenticazione temp completata");

			transaction.begin();

			Property[] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { getTemporaneyContentName() });

			Content content = new Content();
			byte [] buf = null;
			content.setPrefixedName("cm:content");
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setEncoding("UTF-8");
			content.setContent(buf);
			content.setProperties(props);

			final NodeRef contentNodeRef = checkNodeExists(node, transaction);
			logger.debug("[EcmEngineSecurityBean::verifyDocument] Check node exixst eseguito");
			QName contentProperty = null;
			try {
				contentProperty = dictionaryService.resolvePrefixNameToQName(content.getContentPropertyPrefixedName());
				PropertyDefinition contentPropertyDef = dictionaryService.getProperty(contentProperty);
				if (contentPropertyDef == null || !contentPropertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
					throw new InvalidParameterException("Invalid content property.");
				}
			} catch(DictionaryRuntimeException dre) {
				throw new InvalidParameterException("Invalid content property.");
			}

			Object contentData = nodeService.getProperty(contentNodeRef, contentProperty);

			if (contentData != null && contentData instanceof ContentData) {
				final boolean isEncrypted = nodeService.hasAspect(contentNodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

				final boolean encryptionSupported = contentService.supportsCryptography();

				CustomSecretKey decryptionKey = null;
				CryptoTransformationSpec decryptionSpec = null;
				String decryptionTransformation = null;
				byte [] iv = null;
				if (isEncrypted && encryptionSupported) {
					decryptionKey = new CustomSecretKey(content.getEncryptionInfo().getAlgorithm(),
							content.getEncryptionInfo().getKey().getBytes());


					decryptionTransformation = (String) nodeService.getProperty(contentNodeRef,
							EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

					decryptionSpec = CryptoTransformationSpec.buildTransformationSpec(decryptionTransformation);

					if (decryptionSpec.getMode() != null && !decryptionSpec.getMode().equalsIgnoreCase("ECB")) {
						iv = Base64.decode((String) nodeService.getProperty(contentNodeRef,
								EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));

						decryptionSpec.setIv(iv);
					}
				}
				final ContentReader reader = (isEncrypted && encryptionSupported)
				? contentService.getDecryptingReader(contentNodeRef, contentProperty, decryptionKey, decryptionSpec)
						: contentService.getReader(contentNodeRef, contentProperty);
				ByteArrayOutputStream baos=new ByteArrayOutputStream((int) reader.getSize());
				reader.getContent(baos);

				SignedBuffer buffer=new SignedBuffer();
				buffer.setBuffer(baos.toByteArray());

    			logger.debug("[EcmEngineSecurityBean::verifyDocument] Pre getSecurityDelegate");
                EcmEngineIntegrationSecurityDelegate delegate=EcmEngineIntegrationSecurityFactory.getSecurityDelegate();
				response=transformVeryfyReport(delegate.verifyDocument(buffer));
                if(logger.isDebugEnabled()) {
                    logger.debug("[EcmEngineSecurityBean::verifyDocument] Post getSecurityDelegate - response: " + response.toString());
                }
			}
			dumpElapsed("EcmEngineSecurityBean", "verifyDocument", logCtx, "Verifica completata");
			transaction.commit();

		}catch(EcmEngineFoundationException e){
			checkCredentialsException(e, "EcmEngineSecurityBean", "createContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineSecurityBean", "createContent", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineSecurityBean::createContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new PermissionDeniedException("Backend services error: " + e.getCode());
		}catch(EcmEngineIntegrationException e){
			rollbackQuietely(transaction);
			throw new PermissionDeniedException("Backend services error: " + e.getMessage());
		}catch (SecurityException e) {
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
		}
		finally{
			logger.debug("[EcmEngineSecurityBean::verifyDocument] END");
			stop();
		}
		return response;
	}

	public Document extractDocumentFromEnvelope(EnvelopedContent envelopedContent, OperationContext context)throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,RemoteException{
		logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] BEGIN");
		final String logCtx = "U: " +context.getUsername();
		Document response=null;
		start();
		try{
		    authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineSecurityBean", "extractDocumentFromEnvelope", logCtx, "Autenticazione completata");

			EnvelopedBuffer buffer=new EnvelopedBuffer();
			buffer.setBuffer(envelopedContent.getData());

  			logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Pre getSecurityDelegate");
			EcmEngineIntegrationSecurityDelegate delegate=EcmEngineIntegrationSecurityFactory.getSecurityDelegate();
			response=transformDocument(delegate.extractDocumentFromEnvelope(buffer));
            if(logger.isDebugEnabled()) {
                logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Post getSecurityDelegate - response: " + response.toString());
            }

			if(envelopedContent.isStore()){
                OperationContext temp = getTemporaneyContext( context );

                Node parent_node = new Node();
                parent_node.setUid(getTemporaneyParentID());
                String contentName = getTemporaneyContentName();
                Property[] props = new Property[1];
                props[0] = createPropertyDTO("cm:name", "text", false);
                props[0].setValues(new String [] { contentName });
                Property [] authorProps = new Property[1];
                authorProps[0] = createPropertyDTO("cm:author", "text", false);
                authorProps[0].setValues(new String [] { temp.getUsername() + " da security" });
                Property [] titledProps = new Property[2];
                titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
                titledProps[0].setValues(new String [] { contentName });
                titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
                titledProps[1].setValues(new String [] { "Contenuto aggiunto da security." });
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
                content.setContent(response.getBuffer());
                content.setProperties(props);
                logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Pre-createcontent");
                try{
                    Node responseNodo=createContentNoTransaction(parent_node, content, temp);
                    logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Post-createcontent - uid: " + responseNodo.getUid());
                    response.setUid(responseNodo.getUid());
                    response.setBuffer(null);
                } catch(Exception e) {
                    logger.error("[EcmEngineSecurityBean::extractDocumentFromEnvelope] InsertException");
                    throw new InsertException(e.getMessage());
                }
			}
			dumpElapsed("EcmEngineSecurityBean", "extractDocumentFromEnvelope", logCtx, "Estrazione completata");

		}catch(EcmEngineIntegrationException e){
			logger.error("[EcmEngineSecurityBean::extractDocumentFromEnvelope] EcmEngineIntegrationException");
			throw new EcmEngineTransactionException(e.getMessage());
		}catch(AuthenticationRuntimeException e){
			logger.error("[EcmEngineSecurityBean::extractDocumentFromEnvelope] AuthenticationRuntimeException");
			throw new InvalidCredentialsException(e.getMessage());
		}finally{
			logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] END");
			stop();
		}
		return response;
	}

	public Document extractDocumentFromEnvelope(Node node, OperationContext context) throws InsertException,InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,RemoteException{
		logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] BEGIN");
		final String logCtx = "U: " +context.getUsername();
		Document response=new Document();
		UserTransaction transaction =null;
		start();
		try{
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineSecurityBean", "extractDocumentFromEnvelope", logCtx, "Autenticazione completata");

			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();

            OperationContext temp = getTemporaneyContext( context );
			authenticateOnRepository(temp, null);
			dumpElapsed("EcmEngineSecurityBean", "extractDocumentFromEnvelope", logCtx, "Autenticazione temp completata");

			String contentName = getTemporaneyContentName();

			Property[] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			Property [] authorProps = new Property[1];
			authorProps[0] = createPropertyDTO("cm:author", "text", false);
			authorProps[0].setValues(new String [] { temp.getUsername() + " da security" });
			Property [] titledProps = new Property[2];
			titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
			titledProps[0].setValues(new String [] { contentName });
			titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
			titledProps[1].setValues(new String [] { "Contenuto aggiunto da security." });
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
			content.setProperties(props);

			final NodeRef contentNodeRef = checkNodeExists(node, transaction);
			logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Check node exixst eseguito");
			QName contentProperty = null;
			try {
				contentProperty = dictionaryService.resolvePrefixNameToQName(content.getContentPropertyPrefixedName());
				PropertyDefinition contentPropertyDef = dictionaryService.getProperty(contentProperty);
				if (contentPropertyDef == null || !contentPropertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
					throw new InvalidParameterException("Invalid content property.");
				}
			} catch(DictionaryRuntimeException dre) {
				throw new InvalidParameterException("Invalid content property.");
			}

			Object contentData = nodeService.getProperty(contentNodeRef, contentProperty);

			if (contentData != null && contentData instanceof ContentData) {
				final boolean isEncrypted = nodeService.hasAspect(contentNodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

				final boolean encryptionSupported = contentService.supportsCryptography();

				CustomSecretKey decryptionKey = null;
				CryptoTransformationSpec decryptionSpec = null;
				String decryptionTransformation = null;
				byte [] iv = null;
				if (isEncrypted && encryptionSupported) {
					decryptionKey = new CustomSecretKey(content.getEncryptionInfo().getAlgorithm(),
							content.getEncryptionInfo().getKey().getBytes());


					decryptionTransformation = (String) nodeService.getProperty(contentNodeRef,
							EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

					decryptionSpec = CryptoTransformationSpec.buildTransformationSpec(decryptionTransformation);

					if (decryptionSpec.getMode() != null && !decryptionSpec.getMode().equalsIgnoreCase("ECB")) {
						iv = Base64.decode((String) nodeService.getProperty(contentNodeRef,
								EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));

						decryptionSpec.setIv(iv);
					}
				}
				final ContentReader reader = (isEncrypted && encryptionSupported)
				? contentService.getDecryptingReader(contentNodeRef, contentProperty, decryptionKey, decryptionSpec)
						: contentService.getReader(contentNodeRef, contentProperty);
				ByteArrayOutputStream baos=new ByteArrayOutputStream((int) reader.getSize());
				reader.getContent(baos);

				EnvelopedBuffer buffer=new EnvelopedBuffer();
				buffer.setBuffer(baos.toByteArray());

    			logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Pre getSecurityDelegate");
    			EcmEngineIntegrationSecurityDelegate delegate=EcmEngineIntegrationSecurityFactory.getSecurityDelegate();
				response=transformDocument(delegate.extractDocumentFromEnvelope(buffer));
                if(logger.isDebugEnabled()) {
                    logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Post getSecurityDelegate - response: " + response.toString());
                }

				content.setContent(response.getBuffer());
			}
			Node parent_node = new Node();
			parent_node.setUid(getTemporaneyParentID());
			try{
				Node responseNodo=createContentNoTransaction(parent_node, content, temp);
				response.setUid(responseNodo.getUid());
				response.setBuffer(null);
			}catch(Exception e){
				rollbackQuietely(transaction);
				throw new InsertException(e.getMessage());
			}
			dumpElapsed("EcmEngineSecurityBean", "extractDocumentFromEnvelope", logCtx, "Estrazione completata");
			transaction.commit();

		}catch(EcmEngineFoundationException e){
			checkCredentialsException(e, "EcmEngineSecurityBean", "extractDocumentFromEnvelope", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineSecurityBean", "extractDocumentFromEnvelope", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineSecurityBean::extractDocumentFromEnvelope] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new PermissionDeniedException("Backend services error: " + e.getCode());
		}catch(EcmEngineIntegrationException e){
			rollbackQuietely(transaction);
			throw new PermissionDeniedException("Backend services error: " + e.getMessage());
		}catch (SecurityException e) {
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
		}
		finally{
			logger.debug("[EcmEngineSecurityBean::extractDocumentFromEnvelope] END");
			stop();
		}
		return response;
	}

	public Node createContentFromTemporaney(Node parentNode, Content content,OperationContext context, Node tempNode)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, RemoteException{
		logger.debug("[EcmEngineSecurityBean::createContentFromTemporaney] BEGIN");
		final String logCtx = "U: " +context.getUsername();
		Node response=null;
		UserTransaction transaction =null;
		start();
		try{
            OperationContext temp = getTemporaneyContext( context );
			authenticateOnRepository(temp, null);
			dumpElapsed("EcmEngineSecurityBean", "createContentFromTemporaney", logCtx, "Autenticazione temp completata");

			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();

			Property[] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { getTemporaneyContentName() });
			Content temp_content = new Content();
			byte [] buf = null;
			temp_content.setPrefixedName("cm:content");
			temp_content.setParentAssocTypePrefixedName("cm:contains");
			temp_content.setModelPrefixedName("cm:contentmodel");
			temp_content.setTypePrefixedName("cm:content");
			temp_content.setContentPropertyPrefixedName("cm:content");
			temp_content.setEncoding("UTF-8");
			temp_content.setContent(buf);
			temp_content.setProperties(props);
			final NodeRef contentNodeRef = checkNodeExists(tempNode, transaction);
			logger.debug("[EcmEngineSecurityBean::createContentFromTemporaney] Check node exixst eseguito");
			QName contentProperty = null;
			try {
				contentProperty = dictionaryService.resolvePrefixNameToQName(temp_content.getContentPropertyPrefixedName());
				PropertyDefinition contentPropertyDef = dictionaryService.getProperty(contentProperty);
				if (contentPropertyDef == null || !contentPropertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
					throw new InvalidParameterException("Invalid content property.");
				}
			} catch(DictionaryRuntimeException dre) {
				throw new InvalidParameterException("Invalid content property.");
			}

			Object contentData = nodeService.getProperty(contentNodeRef, contentProperty);

			if (contentData != null && contentData instanceof ContentData) {
				final boolean isEncrypted = nodeService.hasAspect(contentNodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

				final boolean encryptionSupported = contentService.supportsCryptography();

				CustomSecretKey decryptionKey = null;
				CryptoTransformationSpec decryptionSpec = null;
				String decryptionTransformation = null;
				byte [] iv = null;
				if (isEncrypted && encryptionSupported) {
					decryptionKey = new CustomSecretKey(temp_content.getEncryptionInfo().getAlgorithm(),
							temp_content.getEncryptionInfo().getKey().getBytes());


					decryptionTransformation = (String) nodeService.getProperty(contentNodeRef,
							EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

					decryptionSpec = CryptoTransformationSpec.buildTransformationSpec(decryptionTransformation);

					if (decryptionSpec.getMode() != null && !decryptionSpec.getMode().equalsIgnoreCase("ECB")) {
						iv = Base64.decode((String) nodeService.getProperty(contentNodeRef,
								EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));

						decryptionSpec.setIv(iv);
					}
				}
				final ContentReader reader = (isEncrypted && encryptionSupported)
				? contentService.getDecryptingReader(contentNodeRef, contentProperty, decryptionKey, decryptionSpec)
						: contentService.getReader(contentNodeRef, contentProperty);
				ByteArrayOutputStream baos=new ByteArrayOutputStream((int) reader.getSize());
				reader.getContent(baos);
				content.setContent(baos.toByteArray());
				try{
					response=createContentNoTransaction(parentNode,content,context);
				}catch(Exception e){
					rollbackQuietely(transaction);
					throw new InsertException(e.getMessage());
				}
				transaction.commit();
			}
			dumpElapsed("EcmEngineSecurityBean", "createContentFromTemporaney", logCtx, "Creazione completata");

		}catch(EcmEngineFoundationException e){
			checkCredentialsException(e, "EcmEngineSecurityBean", "createContentFromTemporaney", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineSecurityBean", "createContentFromTemporaney", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineSecurityBean::createContentFromTemporaney] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new InsertException("Backend services error: " + e.getCode());
		}catch (SecurityException e) {
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
		}finally{
			stop();
			logger.debug("[EcmEngineSecurityBean::createContentFromTemporaney] END");
		}
		return response;
	}

	private VerifyReport transformVeryfyReport(it.doqui.dosign.dosign.dto.signature.VerifyReport vr){
		VerifyReport response=new VerifyReport();
		response.setDate(vr.getDate());
		response.setErrorCode(vr.getErrorCode());
		it.doqui.dosign.dosign.dto.signature.Signature[] signatures=vr.getSignature();
		Signature[] responseSignatures=null;
		if(signatures!=null){
			responseSignatures=new Signature[signatures.length];
			for(int i=0;i<signatures.length;i++){
				responseSignatures[i]=transformSignature(signatures[i]);
			}
		}
		response.setSignature(responseSignatures);
		if(vr.getChild()!=null){
			response.setChild(transformVeryfyReport(vr.getChild()));
		}
		return response;
	}
	private Signature transformSignature(it.doqui.dosign.dosign.dto.signature.Signature signature){
		Signature response=new Signature();
		response.setAnnoFirma(signature.getAnnoFirma());
		response.setCa(signature.getCa());
		response.setCert(signature.getCert());
		response.setCodiceFiscale(signature.getCodiceFiscale());
		response.setDipartimento(signature.getDipartimento());
		response.setDnQualifier(signature.getDnQualifier());
		response.setErrorCode(signature.getErrorCode());
		response.setFineValidita(signature.getFineValidita());
		response.setGiornoFirma(signature.getGiornoFirma());
		response.setGivenname(signature.getGivenname());
		response.setInizioValidita(signature.getInizioValidita());
		response.setMeseFirma(signature.getMeseFirma());
		response.setMinutiFirma(signature.getMinutiFirma());
		response.setNominativoFirmatario(signature.getNominativoFirmatario());
		response.setNumeroControfirme(signature.getNumeroControfirme());
		response.setOraFirma(signature.getOraFirma());
		response.setOrganizzazione(signature.getOrganizzazione());
		response.setPaese(signature.getPaese());
		response.setSecondiFirma(signature.getSecondiFirma());
		response.setSerialNumber(signature.getSerialNumber());
		response.setSurname(signature.getSurname());
		response.setTimestamped(signature.isTimestamped());
		response.setTipoFirma(signature.getTipoFirma());

		//AF: Nuovi campi dalla 6.2.0 di ecmengine
		response.setDataOra(signature.getDataOra());
		response.setFirmatario(signature.getFirmatario());

		it.doqui.dosign.dosign.dto.signature.Signature[] signatures=signature.getSignature();
		Signature[] responseSignatures=null;
		if(signatures!=null){
			responseSignatures=new Signature[signatures.length];
			for(int i=0;i<signatures.length;i++){
				responseSignatures[i]=transformSignature(signatures[i]);
			}
		}
		response.setSignature(responseSignatures);
		return response;
	}

	private Document transformDocument(it.doqui.dosign.dosign.dto.envelope.Document document){
		Document response=new Document();
		response.setBuffer(document.getBuffer());
		return response;
	}
}

package it.doqui.index.ecmengine.business.publishing;

import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvc;
import it.doqui.index.ecmengine.business.foundation.search.SearchSvc;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.foundation.util.QueryBuilder;
import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.CustomSecretKey;
import it.doqui.index.ecmengine.business.personalization.encryption.exception.EncryptionRuntimeException;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;
import it.doqui.index.ecmengine.business.publishing.EcmEnginePublisherBean.ValidationType;
import it.doqui.index.ecmengine.business.publishing.util.IntegrityViolationHandler;
import it.doqui.index.ecmengine.business.publishing.util.MergeSort;
import it.doqui.index.ecmengine.business.publishing.util.Sort;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.Path;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultProperty;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SortField;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.TooManyResultsException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.exception.repository.NodeRuntimeException;
import it.doqui.index.ecmengine.exception.repository.TenantRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;
import it.doqui.index.ecmengine.util.ISO8601DateFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Base64;

public class EcmEngineFeatureBean extends EcmEnginePublisherBean{
	private static final long serialVersionUID = -143436028971163358L;

	protected static final int MAX_QNAME_LENGTH = 255;

	public NodeResponse xpathSearchNoMetadata(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineFeatureBean::xpathSearchNoMetadata] BEGIN");

		validate(ValidationType.XPATH, "xpath", xpath);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		NodeResponse response = null;

		ResultSet resultSet = null;

		final String logCtx = "XPATH: " + xpath.getXPathQuery() + " U: " + context.getUsername();
		Node [] results = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineFeatureBean", "xpathSearchNoMetadata", logCtx, "Autenticazione completata.");

			final String escapedQuery;
			try {
				escapedQuery = QueryBuilder.escapeXPathQuery(xpath.getXPathQuery());
			} catch (IllegalArgumentException e) {
				String errorMessage = "Errore di sintassi nella query: " + xpath.getXPathQuery();

				logger.warn("[EcmEngineFeatureBean::xpathSearchNoMetadata] " + errorMessage);
				throw new InvalidParameterException(errorMessage);
			}

			if(logger.isDebugEnabled()) {
				logger.debug("[EcmEngineFeatureBean::xpathSearchNoMetadata] Query - Q: " + escapedQuery);
			}
			final int limit = xpath.getLimit();

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(DictionarySvc.SPACES_STORE);
			searchParams.setLimitBy((limit > 0) ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED);
			searchParams.setLimit(limit);
			searchParams.setLanguage(SearchSvc.LANGUAGE_XPATH);
			searchParams.setQuery(escapedQuery);

			resultSet = searchService.query(searchParams);
			dumpElapsed("EcmEngineFeatureBean", "xpathSearchNoMetadata", logCtx, "Ricerca completata - "	+resultSet.length() +" risultati trovati. "
					+"(Page size: " + xpath.getPageSize()
					+" - index: " + xpath.getPageIndex()
					+" - limit: " + xpath.getLimit()     +")");

			results = translateResultSetToNodeArray(resultSet, xpath.getPageSize(), xpath.getPageIndex(), xpath.getSortFields());
			dumpElapsed("EcmEngineFeatureBean", "xpathSearchNoMetadata", logCtx, "Risultati processati.");

			response = new NodeResponse();
			response.setNodeArray(results);
			response.setTotalResults(resultSet.length());
			response.setPageSize(xpath.getPageSize());
			response.setPageIndex(xpath.getPageIndex());

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineFeatureBean", "xpathSearchNoMetadata", context.getUsername(), null);
			logger.error("[EcmEngineFeatureBean::xpathSearchNoMetadata] Foundation services error: " + e.getCode());

			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			stop();
			logger.debug("[EcmEngineFeatureBean::xpathSearchNoMetadata] END");
		}
		return response;
	}

	public Path[] getPaths(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, PermissionDeniedException, SearchException,
	RemoteException, InvalidCredentialsException {

		logger.debug("[EcmEngineFeatureBean::getPath] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid() + " U: " + context.getUsername();
		Vector<Path> tempPaths = new Vector<Path>();

		start();

		try {
			final String repository = context.getRepository();

			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineFeatureBean", "getPaths", logCtx, "Autenticazione completata.");

			final NodeRef nodeRef = checkNodeExists(node, null);

			final List<org.alfresco.service.cmr.repository.Path> paths = nodeService.getPaths(nodeRef, false);

			if (paths.isEmpty()) {
				logger.error("[EcmEngineFeatureBean::getPaths] No paths found for node " + node.getUid()
						+ " on repository " + repository);
				throw new SearchException("No paths found for node " + node.getUid() + " on repository " + repository);
			}

			final org.alfresco.service.cmr.repository.Path primaryPath = nodeService.getPath(nodeRef);

			dumpElapsed("EcmEngineFeatureBean", "getPaths", logCtx,
					"Lettura dei path completata: " + paths.size() + " path(s)");

			boolean primaryFound = false;
			int primaryIndex = -1;

			for (int i = 0; i < paths.size(); i++) {
				org.alfresco.service.cmr.repository.Path p = paths.get(i);
				Path curPath = new Path();
				curPath.setPath(dictionaryService.resolvePathToPrefixNameString(p));

				if (!primaryFound && p.equals(primaryPath)) {
					curPath.setPrimary(true);
					primaryFound = true;
					primaryIndex = i;

					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::getPaths] Found primary path for node: " + node.getUid()
								+ " [Path: " + curPath.getPath() + "]");
					}
				}

				tempPaths.add(curPath);
			}
			dumpElapsed("EcmEngineFeatureBean", "getPaths", logCtx,
					"Formazione DTO completata: " + tempPaths.size() + " path(s)");

			Path[] resultArray = tempPaths.toArray(new Path [] {});

			if (primaryFound && primaryIndex > 0) {
				Path temp = resultArray[primaryIndex];
				resultArray[primaryIndex] = resultArray[0];
				resultArray[0] = temp;
			}

			return resultArray;
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineFeatureBean", "getPaths", context.getUsername(), null);
			checkAccessException(e, "EcmEngineFeatureBean", "getPaths", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineFeatureBean::getPaths] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} finally {
			stop();
			logger.debug("[EcmEngineFeatureBean::getPaths] END");
		}
	}


	protected Node createContentNoTransaction(Node parent, Content content, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] BEGIN");

		validate(ValidationType.NODE             , "parent" , parent);
		validate(ValidationType.CONTENT_WRITE_NEW, "content", content);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final boolean encryptionRequired = (content.getEncryptionInfo() != null);
		final boolean encryptionSupported = contentService.supportsCryptography();

		if(logger.isDebugEnabled()) {
			logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Cryptography: " +
					((encryptionSupported) ? "" : "NOT ") + "supported");
		}

		if (encryptionRequired && encryptionSupported) {
			validate(ValidationType.ENCRYPTION_INFO_ENCRYPT, "content.encryptionInfo", content.getEncryptionInfo());
		}

		final String logCtx = "U: " + context.getUsername() +" - P: " + parent.getUid() + " - CN: " + content.getPrefixedName();

		Node result = null;

		start(); // Avvia stopwatch

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx, "Autenticazione completata");

			final NodeRef parentRef = checkNodeExists(parent, null);

			logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Conversione metadati.");
			final Property [] propArray = content.getProperties();

			final Map<QName, Serializable> properties = translatePropertyArray(propArray);

			dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx,
					"Conversione metadati completata: " + ((propArray != null) ? propArray.length : "0") + " metadati.");

			final QName assocName = dictionaryService.resolvePrefixNameToQName(	content.getPrefixedName()               );
			final QName assocType = dictionaryService.resolvePrefixNameToQName(	content.getParentAssocTypePrefixedName());
			final QName typeName  = dictionaryService.resolvePrefixNameToQName(	content.getTypePrefixedName()           );

			// assocName non deve superare la dimensione massima di 255 caratteri
			int qnameLength = assocName.toString().length();
			if (qnameLength > MAX_QNAME_LENGTH) {
				int uriLength = assocName.getNamespaceURI().length()+2;
				int localnameLength = assocName.getLocalName().length();
				int exceedingChars = qnameLength-MAX_QNAME_LENGTH;
				throw new InvalidParameterException("prefixed name exceeds "+MAX_QNAME_LENGTH+" characters [namespace uri length: "+uriLength+", name length: "+localnameLength+", exceeding chars: "+exceedingChars+"]");
			}

			// If name property "cm:name" is not set => force to assocName
			// even if model doesn't support "cm" namespace.
			if (properties.get(ContentModel.PROP_NAME) == null) {
				properties.put(ContentModel.PROP_NAME, assocName.getLocalName());
			}

			final ChildAssociationRef childAssociationRef;

			childAssociationRef = nodeService.createNode(parentRef, assocType, assocName, typeName, properties);

			final NodeRef childRef = childAssociationRef.getChildRef();
			result = new Node();
			result.setUid(childRef.getId());

			dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx, "Nodo creato.");

			// BEGIN GESTIONE ASPECT PER L'ENCRYPTION
			String encryptionTransformation = null;
			CustomSecretKey encryptionKey = null;
			CryptoTransformationSpec encryptionSpec = null;

			if (encryptionRequired && encryptionSupported) {
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

				byte [] iv = null;
				String encodedIv = null;

				encryptionTransformation = CryptoTransformationSpec.buildTransformationString(encryptionSpec);

				HashMap<QName, Serializable> encryptionProps = new HashMap<QName, Serializable>(6);

				encryptionProps.put(EcmEngineModelConstants.PROP_ENCRYPTION_KEY_ID, 		content.getEncryptionInfo().getKeyId());
				encryptionProps.put(EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION,	encryptionTransformation);
				encryptionProps.put(EcmEngineModelConstants.PROP_ENCRYPTED_FROM_SOURCE, 	content.getEncryptionInfo().isSourceEncrypted());

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

				nodeService.addAspect(childRef, EcmEngineModelConstants.ASPECT_ENCRYPTED, encryptionProps);
				if(logger.isDebugEnabled()) {
					logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Aspect \"encrypted\" impostato sul nodo: " + childRef);
				}
			}

			// END GESTIONE ASPECT PER L'ENCRYPTION

			QName contentPropertyName = null;
			if (content.getContentPropertyPrefixedName() != null) {
				try {
					contentPropertyName = dictionaryService.resolvePrefixNameToQName(content.getContentPropertyPrefixedName());
					PropertyDefinition contentPropertyDef = dictionaryService.getProperty(contentPropertyName);
					if (contentPropertyDef == null || !contentPropertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
						throw new InvalidParameterException("Invalid content property: "+content.getContentPropertyPrefixedName());
					}
				} catch(DictionaryRuntimeException dre) {
					throw new InvalidParameterException("Invalid content property: "+content.getContentPropertyPrefixedName());
				}
			}

			// MB: aggiunta dell'aspect EcmEngineModelConstants.ASPECT_STORAGE
			// Serve per prendere il giusto writer
			final Aspect [] aspects = content.getAspects();
			if (aspects != null) {
				// Ciclo sugli aspect
				int nMeta = 0;
				logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Impostazione degli aspect.");
				for (Aspect a: aspects) {
					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Impostazione dei metadati dell'aspect before: " +a.getPrefixedName());
					}
					if( a.getPrefixedName().equals( "ecm-sys:storage" ) ){
						nMeta++;
						if(logger.isDebugEnabled()) {
							logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Impostazione dei metadati dell'aspect before-imposto: " +a.getPrefixedName());
						}
						final QName aspectName  = dictionaryService.resolvePrefixNameToQName(a.getPrefixedName());
						final Property [] props = a.getProperties();
						final Map<QName, Serializable> aspectProps = translatePropertyArray(props);

						// MB: prendo lo storageID del content corrente
						String storageId = (String) aspectProps.get(EcmEngineModelConstants.PROP_STORAGE_ID);
						if(logger.isDebugEnabled()) {
							logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] StorageID: " +storageId);
						}
						if( storageId!=null && storageId.length()>0 ){
							// MB: prende il tenant corrente
							String tenantName = "";
							if (context.getUsername().indexOf("@")>0) {
								tenantName = context.getUsername().substring(context.getUsername().indexOf("@")+1);
							}
							if(logger.isDebugEnabled()) {
								logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Tenant: " +tenantName);
							}

							// MB: estraggo la lista dei contentStore
							List<ContentStoreDefinition> contentStores = null;
							if( tenantName.length()>0 ) {
								Tenant tenant = tenantAdminService.getTenant( tenantName );
								if( tenant!=null && tenant.getContentStores()!=null ){
									contentStores = tenant.getContentStores();
								}
							} else {
								// MB: Se tenant vuoto, controllo sul repository
								Repository repository = RepositoryManager.getInstance().getRepository( RepositoryManager.getCurrentRepository() );
								if( repository!=null && repository.getContentStores()!=null ){
									contentStores = repository.getContentStores();
								}
							}

							boolean bGestito = false;
							// MB: Verifica se il protocol e' gestito
							if( contentStores!=null )
							{
								for( ContentStoreDefinition tcs : contentStores )
								{
									// Dal protocol assegnato al Tenant, accedo al bean del repository che gestisce quel protocol
									if( tcs.getProtocol().equals( storageId ) ){
										bGestito = true;
									}
								}
							}

							if( !bGestito ){
								throw new InvalidParameterException("StorageID (" +storageId +") non gestito per tenant (" +tenantName +")");
							}
						}
						// Aggiunta dell'aspect al nodo appena creato
						nodeService.addAspect(childRef, aspectName, aspectProps);
					}
				}
				dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx, "Aspect totali " +aspects.length +" aggiunti: " +nMeta);
			}

			final byte [] data = content.getContent();
			if (data != null && contentPropertyName != null) {
				// Marcare il contenuto come "modificato" - TODO: valutare se portare fuori dall'if
				Map<QName, Serializable> modifiedAspectProps = new HashMap<QName, Serializable>(2);

				modifiedAspectProps.put(EcmEngineModelConstants.PROP_DATA_MODIFICA, new Date());	// data e ora correnti
				nodeService.addAspect(childRef, EcmEngineModelConstants.ASPECT_MODIFIED, modifiedAspectProps);
				if(logger.isDebugEnabled()) {
					logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Add aspect per il Job di Backup: "	+ EcmEngineModelConstants.ASPECT_MODIFIED);
				}

				// Scrittura contenuto
				logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Scrittura contenuto fisico.");
				final ContentWriter writer = (encryptionRequired && !content.getEncryptionInfo().isSourceEncrypted())
				? contentService.getEncryptingWriter(childRef, contentPropertyName, true, encryptionKey, encryptionSpec)
						: contentService.getWriter(childRef, contentPropertyName, true);

				writer.setMimetype(content.getMimeType());
				writer.setEncoding(content.getEncoding());

				dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx, "Inizio scrittura contenuto: " + data.length + " byte.");

				try {
					writer.putContent(new ByteArrayInputStream(data));
				} catch (ContentQuotaException e) {
					logger.warn("[EcmEngineFeatureBean::createContentNoTransaction] Content quota exceeded: " + e.getMessage(), e);
					throw new InsertException("Content quota exceeded: " + e.getMessage());
				} catch (ContentIOException e) {
					logger.warn("[EcmEngineFeatureBean::createContentNoTransaction] Unable to write content: " + e.getMessage(), e);
					throw new InsertException("Unable to write content: " + e.getMessage());
				}

				dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx,
						"Scrittura contenuto completata: " + data.length + " byte.");
			}

			// IMPOSTAZIONE ASPECT DEL CONTENUTO
			// Nota (LB - 08/01/2008)
			// Gli aspect devono essere impostati DOPO l'inserimento del
			// contenuto del byte array, perche` nel caso in cui viene
			// specificato l'aspect "cm:versionable" l'operazione di scrittura
			// del contenuto causa la creazione di una nuova versione
			// (cm:autoVersion di default e` valorizzato a true).
			// MB: viene escluso l'aspect EcmEngineModelConstants.ASPECT_STORAGE
			//final Aspect [] aspects = content.getAspects();
			if (aspects != null) {
				// Ciclo sugli aspect
				int nMeta = 0;
				logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Impostazione degli aspect.");
				for (Aspect a: aspects) {
					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Impostazione dei metadati dell'aspect after: " +a.getPrefixedName());
					}
					if( !a.getPrefixedName().equals( "ecm-sys:storage" ) ){
						nMeta++;
						if(logger.isDebugEnabled()) {
							logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Impostazione dei metadati dell'aspect after-imposto: " +a.getPrefixedName());
						}
						final QName aspectName  = dictionaryService.resolvePrefixNameToQName(a.getPrefixedName());
						final Property [] props = a.getProperties();
						final Map<QName, Serializable> aspectProps = translatePropertyArray(props);

						// Aggiunta dell'aspect al nodo appena creato
						nodeService.addAspect(childRef, aspectName, aspectProps);
					}
				}
				dumpElapsed("EcmEngineFeatureBean", "createContentNoTransaction", logCtx, "Aspect totali " +aspects.length +" aggiunti: " +nMeta);
			}

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineFeatureBean", "createContentNoTransaction", logCtx, context, result.getUid(),
					content.getTypePrefixedName() + " [Name: " + content.getPrefixedName() + "]");
		} catch (EncryptionRuntimeException e) {	// FIXME: questa eccezione non dovrebbe arrivare fino a qui!
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

			logger.error("[EcmEngineFeatureBean::createContentNoTransaction] Foundation services error: " + code);
			throw new InsertException("Backend services error: " + code);
		} catch (EcmEngineFoundationException e) {
			if (e.getCode().equals(FoundationErrorCodes.DUPLICATE_CHILD_ERROR)) {
				logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] Duplicate child. " +
						"Cannot create child node: " + content.getPrefixedName());
				throw new InsertException("Cannot create child node. Duplicate child: " + content.getPrefixedName());
			}

			checkCredentialsException(e, "EcmEngineFeatureBean", "createContentNoTransaction", context.getUsername(), null);
			checkAccessException(e, "EcmEngineFeatureBean", "createContentNoTransaction", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineFeatureBean::createContentNoTransaction] Foundation services error: " + e.getCode());
			throw new InsertException("Backend services error: " + e.getCode());
		} catch (InvalidParameterException ipe) {
			logger.error("[EcmEngineFeatureBean::createContentNoTransaction] Invalid parameter error: " + ipe.getMessage());
			throw ipe;
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineFeatureBean::createContentNoTransaction] END");
		}
		return result;
	}

	public Node createContent(Node parent, Content content, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		UserTransaction transaction=null;
		Node result=null;
		logger.debug("[EcmEngineFeatureBean::createContent] BEGIN");
		start();
		try{
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			result=createContentNoTransaction(parent, content, context);
			transaction.commit();
		} catch(InvalidParameterException e){
			rollbackQuietely(transaction);
			throw e;
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
			this.<InsertException>checkIntegrityException(e, "EcmEngineFeatureBean", "createContent", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			logger.debug("[EcmEngineFeatureBean::createContent] END");
			stop();
		}
		return result;
	}

	protected byte [] retrieveContentDataNoTransaction(Node node, Content content, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, RemoteException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] BEGIN");

		validate(ValidationType.NODE, "node", node);

		/*
		 * Le informazioni sul tipo del contenuto vengono ignorate,
		 * quindi non vengono validate. L'unica informazione verificata e` quella relativa
		 * al nome con prefisso del contenuto, che verra` utilizzato per un ulteriore check
		 * sulla selezione del nodo.
		 */
		//		validate(ValidationType.CONTENT_ITEM, "content", content);
		//		validate(ValidationType.PREFIXED_NAME, "content.contentPropertyPrefixedName", content.getContentPropertyPrefixedName());
		validate(ValidationType.CONTENT_READ, "content", content);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final boolean encryptionSupported = contentService.supportsCryptography();
		if(logger.isDebugEnabled()) {
			logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Cryptography: " +
					((encryptionSupported) ? "" : "NOT ") + "supported");
		}

		final String logCtx = "N: " + node.getUid() +
		" - CN: " + content.getPrefixedName() +
		" - CP: " + content.getContentPropertyPrefixedName();
		byte [] binaryData = null;

		start(); // Avvia stopwatch

		// Read-only
		UserTransaction transaction = null; //transactionService.getService().getNonPropagatingUserTransaction(true); // MBNT

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineFeatureBean", "retrieveContentDataNoTransaction", logCtx, "Autenticazione completata");

			//transaction.begin(); // MBNT

			final NodeRef nodeRef = checkNodeExists(node, transaction);

			if(nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_STREAMED_CONTENT)){
				throw new PermissionDeniedException("Content is streamed.");
			}

			final boolean isEncrypted = nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED);

			if (isEncrypted) {
				try {
					validate(ValidationType.ENCRYPTION_INFO_DECRYPT, "content.encryptionInfo", content.getEncryptionInfo());
				} catch (InvalidParameterException ipe) {

					// Rollback if needed and rethrow...
					//rollbackQuietely(transaction);
					throw ipe;
				}
			}

			CustomSecretKey decryptionKey = null;
			CryptoTransformationSpec decryptionSpec = null;
			String decryptionTransformation = null;
			byte [] iv = null;

			if (isEncrypted && encryptionSupported) {
				decryptionKey = new CustomSecretKey(content.getEncryptionInfo().getAlgorithm(),
						content.getEncryptionInfo().getKey().getBytes());
				if(logger.isDebugEnabled()) {
					logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Algorithm: " + decryptionKey.getAlgorithm() + " - Key: " + decryptionKey.getEncoded());
				}

				decryptionTransformation = (String) nodeService.getProperty(nodeRef,
						EcmEngineModelConstants.PROP_ENCRYPTION_TRANSFORMATION);

				decryptionSpec = CryptoTransformationSpec.buildTransformationSpec(decryptionTransformation);

				if (decryptionSpec.getMode() != null && !decryptionSpec.getMode().equalsIgnoreCase("ECB")) {
					iv = Base64.decode((String) nodeService.getProperty(nodeRef,
							EcmEngineModelConstants.PROP_INITIALIZATION_VECTOR));

					decryptionSpec.setIv(iv);
				}

				if(logger.isDebugEnabled()) {
					logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Ottenuto stato crittazione per il nodo: " + nodeRef);
					logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Decryption: " + decryptionTransformation);
				}
			}

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

			final ContentReader reader = (isEncrypted && encryptionSupported)
			? contentService.getDecryptingReader(nodeRef, contentProperty, decryptionKey, decryptionSpec)
					: contentService.getReader(nodeRef, contentProperty);

			if (reader == null || !reader.exists()) {
				logger.warn("[EcmEngineFeatureBean::retrieveContentDataNoTransaction]" +
						" Non ci sono dati binari associati al nodo: " + node.getUid() +
						" [Name: " + content.getPrefixedName() + "]");
			} else {
				try {
					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction]" +
								" Lettura dei dati binari associati al nodo: " + node.getUid() +
								" [Name: " + content.getPrefixedName() +
								" - Prop: " + content.getContentPropertyPrefixedName() +
								" - Size: " + reader.getSize() + " byte]");
					}
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
							(int) reader.getSize());

					reader.getContent(outputStream);
					binaryData = outputStream.toByteArray();

					dumpElapsed("EcmEngineFeatureBean", "retrieveContentDataNoTransaction", logCtx,
							"Lettura completata: " + binaryData.length + " byte letti.");
				} catch (ContentIOException e) {
					final String errorMsg = "Errore in lettura! Nodo: " + node.getUid() +
					" [Name: " + content.getPrefixedName() +
					" - Prop: " + content.getContentPropertyPrefixedName() +
					" - Size: " + reader.getSize() + " byte]";

					logger.error("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] " + errorMsg);

					rollbackQuietely(transaction);
					throw new ReadException(errorMsg);
				}
			}

			//transaction.commit(); // MBNT
		} catch (EncryptionRuntimeException e) {	// FIXME: questa eccezione non dovrebbe arrivare fino a qui!
			//rollbackQuietely(transaction); // MBNT

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

			logger.error("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Foundation services error: " + code);
			throw new ReadException("Backend services error: " + code);
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineFeatureBean", "retrieveContentDataNoTransaction", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineFeatureBean", "retrieveContentDataNoTransaction", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Foundation services error: " + e.getCode());
			//rollbackQuietely(transaction); // MBNT
			throw new ReadException("Backend services error: " + e.getCode());
		} catch (InvalidParameterException ipe) {
			logger.error("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] Invalid parameter error: " + ipe.getMessage());
			//rollbackQuietely(transaction); // MBNT
			throw ipe;
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
			//} catch (RollbackException e) {                                                       // MBNT
			//handleTransactionException(e, "transaction rolled-back.");                        // MBNT
			//} catch (HeuristicMixedException e) {                                                 // MBNT
			//handleTransactionException(e, "transaction rolled-back (partial, heuristic).");   // MBNT
			//} catch (HeuristicRollbackException e) {                                              // MBNT
			//handleTransactionException(e, "transaction rolled-back (heuristic).");            // MBNT
			//} catch (SystemException e) {                                                         // MBNT
			//handleTransactionException(e, "system error.");                                   // MBNT
			//} catch (NotSupportedException e) {                                                   // MBNT
			//handleTransactionException(e, "not supported.");                                  // MBNT
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineFeatureBean::retrieveContentDataNoTransaction] END");
		}
		return binaryData;
	}

	public ResultContent getContentMetadataNoTransaction(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, PermissionDeniedException,
	EcmEngineTransactionException, ReadException, InvalidCredentialsException, RemoteException {
		logger.debug("[EcmEngineFeatureBean::getContentMetadataNoTransaction] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		ResultContent result = null;
		final String logCtx = "N: " + node.getUid();

		start(); // Avvia stopwatch

		UserTransaction transaction = null; //transactionService.getService().getNonPropagatingUserTransaction(true); // MBNT

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineFeatureBean", "getContentMetadataNoTransaction", logCtx, "Autenticazione completata");

			//transaction.begin();

			final NodeRef nodeRef = checkNodeExists(node, transaction);

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
					logger.debug("[EcmEngineFeatureBean::getContentMetadataNoTransaction] " +
							"Access denied reading parent of node: " + node);
				} else {
					logger.warn("[EcmEngineFeatureBean::getContentMetadataNoTransaction] " +
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

			ResultProperty [] resultProps = translatePropertyMap(props,null);
			result.setProperties(resultProps);

			// Gestione aspect associati al risultato
			final Set<QName> aspects = nodeService.getAspects(nodeRef);
			final int aspectsSize = aspects.size();
			int j = 0;

			if (aspectsSize > 0) {
				ResultAspect [] resultAspects = new ResultAspect[aspectsSize];

				for (QName aspect : aspects) {
					resultAspects[j] = new ResultAspect();
					resultAspects[j].setPrefixedName(
							dictionaryService.resolveQNameToPrefixName(aspect));
					j++;
				}
				result.setAspects(resultAspects);
			}
			dumpElapsed("EcmEngineFeatureBean", "getContentMetadataNoTransaction", logCtx, "Letti metadati");

			//transaction.commit(); // MBNT
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineFeatureBean", "getContentMetadataNoTransaction", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineFeatureBean", "getContentMetadataNoTransaction", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineFeatureBean::getContentMetadataNoTransaction] Foundation services error: " + e.getCode());
			//rollbackQuietely(transaction); // MBNT
			throw new ReadException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
			//} catch (RollbackException e) {                                                       // MBNT
			//handleTransactionException(e, "transaction rolled-back.");                        // MBNT
			//} catch (HeuristicMixedException e) {                                                 // MBNT
			//handleTransactionException(e, "transaction rolled-back (partial, heuristic).");   // MBNT
			//} catch (HeuristicRollbackException e) {                                              // MBNT
			//handleTransactionException(e, "transaction rolled-back (heuristic).");            // MBNT
			//} catch (SystemException e) {                                                         // MBNT
			//handleTransactionException(e, "system error.");                                   // MBNT
			//} catch (NotSupportedException e) {                                                   // MBNT
			//handleTransactionException(e, "not supported.");                                  // MBNT
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineFeatureBean::getContentMetadataNoTransaction] END");
		}
		return result;
	}

	protected Node [] translateNodeListToNodeArray(List<NodeRef> nodeRefList, int pageSize, int pageIndex, SortField[] sortFields)
	throws DictionaryRuntimeException, NodeRuntimeException, InvalidParameterException {

		final List<Node> results = new ArrayList<Node>((pageSize > 0) ? pageSize : nodeRefList.size());

		logger.debug("[EcmEngineFeatureBean::translateNodeListToNodeArray] BEGIN");

		try {
			logger.debug("[EcmEngineFeatureBean::translateNodeListToNodeArray] Translating "+ nodeRefList.size() +" results.");

			// Ordinamento risultati
			List<NodeRef> rows = new Vector<NodeRef>();
			for (int i = 0; i < nodeRefList.size(); i++) {
				rows.add(nodeRefList.get(i));
			}
			if (sortFields != null && sortFields.length > 0) {
				logger.info("[EcmEngineFeatureBean::translateNodeListToNodeArray] Sorting results.");

				// Viene utilizzato l'algoritmo di in-place mergesort ( stabile, tempo: O(n*log(n)), memoria: O(1) )
				for (int i = sortFields.length - 1; i >= 0; i--) {
					if (sortFields[i] != null) {
						logger.info("[EcmEngineFeatureBean::translateNodeListToNodeArray] Sorting by " +
								sortFields[i].getFieldName() + (sortFields[i].isAscending() ? " ASC" : " DESC"));
						if (sortFields[i].getFieldName() == null || sortFields[i].getFieldName().length() == 0) {
							throw new InvalidParameterException("Invalid sort field: "+sortFields[i].getFieldName());
						}
						QName sortFieldQName = dictionaryService.resolvePrefixNameToQName(sortFields[i].getFieldName());
						PropertyDefinition propertyDef = dictionaryService.getProperty(sortFieldQName);
						if (propertyDef == null) {
							throw new InvalidParameterException("Invalid sort field: " + sortFields[i].getFieldName());
						}
						Sort sorter = new MergeSort(rows, sortFieldQName,
								sortFields[i].isAscending(), Sort.SORT_CASEINSENSITIVE, nodeService);
						sorter.sort();
						logger.info("[EcmEngineFeatureBean::translateNodeListToNodeArray] Sorted by " + sortFields[i].getFieldName());
					} else {
						logger.info("[EcmEngineFeatureBean::translateNodeListToNodeArray] Found null sort field: ignoring...");
					}
				}
			}

			// La paginazione viene implementata estraendo il sottinsieme
			// di dati interessati alla ricerca.
			int startIndex = 0;
			int endIndex = rows.size();
			if (pageSize > 0 && pageIndex >= 0) {
				startIndex = pageSize * pageIndex;
				int upperLimit = pageSize * (pageIndex+1);
				if (upperLimit < rows.size()) {
					endIndex = upperLimit;
				}
			}

			for (int i = startIndex; i < endIndex; i++) {
				final Node n = new Node();
				n.setUid(rows.get(i).getId());

				results.add(n);
			}
		} finally {
			logger.debug("[EcmEngineFeatureBean::translateNodeListToNodeArray] END");
		}
		return results.toArray(new Node [] {});
	}


	protected Node [] translateResultSetToNodeArray(ResultSet resultSet, int pageSize, int pageIndex, SortField[] sortFields)
	throws DictionaryRuntimeException, NodeRuntimeException, InvalidParameterException {
		final List<Node> results = new ArrayList<Node>((pageSize > 0) ? pageSize : resultSet.length());

		logger.debug("[EcmEngineFeatureBean::translateResultSetToNodeArray] BEGIN");

		try {
			if(logger.isDebugEnabled()) {
				logger.debug("[EcmEngineFeatureBean::translateResultSetToNodeArray] Translating " + resultSet.length() + " results.");
			}

			// La paginazione viene implementata estraendo il sottinsieme
			// di dati interessati alla ricerca.
			int startIndex = 0;
			int endIndex = resultSet.length();
			if (pageSize > 0 && pageIndex >= 0) {
				startIndex = pageSize * pageIndex;
				int upperLimit = pageSize * (pageIndex+1);
				if (upperLimit < resultSet.length()) {
					endIndex = upperLimit;
				}
			}

			// Ordinamento risultati
			List<ResultSetRow> rows = new Vector<ResultSetRow>();
			if (sortFields != null && sortFields.length > 0) {
				List<ResultSetRow> rows2 = new Vector<ResultSetRow>();
				for (int i = 0; i < resultSet.length(); i++) {
					rows2.add(resultSet.getRow(i));
				}
				logger.info("[EcmEngineFeatureBean::translateResultSetToNodeArray] Sorting results.");

				// Viene utilizzato l'algoritmo di in-place mergesort ( stabile, tempo: O(n*log(n)), memoria: O(1) )
				for (int i = sortFields.length - 1; i >= 0; i--) {
					if (sortFields[i] != null) {
						logger.info("[EcmEngineFeatureBean::translateResultSetToNodeArray] Sorting by " +
								sortFields[i].getFieldName() + (sortFields[i].isAscending() ? " ASC" : " DESC"));
						if (sortFields[i].getFieldName() == null || sortFields[i].getFieldName().length() == 0) {
							throw new InvalidParameterException("Invalid sort field: "+sortFields[i].getFieldName());
						}
						QName sortFieldQName = dictionaryService.resolvePrefixNameToQName(sortFields[i].getFieldName());
						PropertyDefinition propertyDef = dictionaryService.getProperty(sortFieldQName);
						if (propertyDef == null) {
							throw new InvalidParameterException("Invalid sort field: " +sortFields[i].getFieldName());
						}
						Sort sorter = new MergeSort(rows2, sortFieldQName,
								sortFields[i].isAscending(), Sort.SORT_CASEINSENSITIVE, nodeService);
						sorter.sort();
						logger.info("[EcmEngineFeatureBean::translateResultSetToNodeArray] Sorted by " + sortFields[i].getFieldName());
					} else {
						logger.info("[EcmEngineFeatureBean::translateResultSetToNodeArray] Found null sort field: ignoring...");
					}
				}
				for (int i = startIndex; i < endIndex; i++) {
					rows.add(rows2.get(i));
				}
			} else {
				for (int i = startIndex; i < endIndex; i++) {
					rows.add(resultSet.getRow(i));
				}
			}

			for (int i = 0; i < rows.size(); i++) {
				final Node n = new Node();
				n.setUid(rows.get(i).getNodeRef().getId());

				results.add(n);
			}
		} finally {
			logger.debug("[EcmEngineFeatureBean::translateResultSetToNodeArray] END");
		}
		return results.toArray(new Node [] {});
	}

	protected Map<QName, Serializable> translatePropertyArray(Property [] props) throws InvalidParameterException {
		logger.debug("[EcmEngineFeatureBean::translatePropertyArray] BEGIN");
		final Map<QName, Serializable> propMap = new HashMap<QName, Serializable>();
		Property current = null;

		try {
			if (props == null) {
				return propMap;
			}

			if(logger.isDebugEnabled()) {
				logger.debug("[EcmEngineFeatureBean::translatePropertyArray] Creazione mappa metadati: " + props.length);
			}

			// Ciclo sulle proprieta`
			for (int i = 0; i < props.length; i++) {
				current = props[i];
				final QName currentQName = dictionaryService.resolvePrefixNameToQName(current.getPrefixedName());
				if(logger.isDebugEnabled()) {
    				final String type = current.getDataType();
					logger.debug("[EcmEngineFeatureBean::translatePropertyArray] " +
            							"Gestione property: " + current.getPrefixedName() + " [T: " + type +
			            				" MV: " + current.isMultivalue() + "]");
				}

				propMap.put(currentQName, translatePropertyValuesToSerializable(current));
			}
		} catch (DictionaryRuntimeException e) {
			logger.warn("[EcmEngineFeatureBean::translatePropertyArray] " +
    					"Errore nella traduzione della property " + current.getPrefixedName() +
	            		": property sconosciuta!");
			throw new InvalidParameterException("Invalid property: "+current.getPrefixedName());
		} finally {
			logger.debug("[EcmEngineFeatureBean::translatePropertyArray] END");
		}

		return propMap;
	}

	protected Serializable translatePropertyValuesToSerializable(Property prop) throws InvalidParameterException {

		final String type = prop.getDataType();

		try {
			if (!prop.isMultivalue()) {

				// Valore singolo
				if (type == null) {
					return prop.getValue();
				} else if (type.equals("mltext")) {
					return new MLText(prop.getValue());
				} else if (type.equals("long")) {
					return new Long(prop.getValue());
				} else if (type.equals("int")) {
					return new Integer(prop.getValue());
				} else if (type.equals("float")) {
					return new Float(prop.getValue());
				} else if (type.equals("double")) {
					return new Double(prop.getValue());
				} else if (type.equals("datetime") || type.equals("date")) {
					return ISO8601DateFormat.parse(prop.getValue());
				} else {

					// Default
					return prop.getValue();
				}
			} else {

				// Valori multipli
				final String [] valuesArray = prop.getValues();
				final ArrayList<Serializable> valuesList =
					new ArrayList<Serializable>(valuesArray.length);

				// Ciclo sui valori della singola proprieta`
				for (int j = 0; valuesArray != null && j < valuesArray.length; j++) {
					if (type == null) {
						valuesList.add(valuesArray[j]);
					} else if (type.equals("mltext")) {
						valuesList.add(new MLText(valuesArray[j]));
					} else if (type.equals("long")) {
						valuesList.add(new Long(valuesArray[j]));
					} else if (type.equals("int")) {
						valuesList.add(new Integer(valuesArray[j]));
					} else if (type.equals("float")) {
						valuesList.add(new Float(valuesArray[j]));
					} else if (type.equals("double")) {
						valuesList.add(new Double(valuesArray[j]));
					} else if (type.equals("datetime") || type.equals("date")) {
						valuesList.add(ISO8601DateFormat.parse(valuesArray[j]));
					} else {

						// Default
						valuesList.add(valuesArray[j]);
					}
				}
				return valuesList;
			}
		} catch(Exception e) {
			logger.error("unable to set value of property "+prop.getPrefixedName(), e);
			throw new InvalidParameterException("unable to set value of property "+prop.getPrefixedName()+": "+e.getMessage());
		}
	}


	/**
	 * Verifica se l'eccezione {@code RollbackException} ricevuta in input &egrave; causata da una
	 * violazione di integrita`.
	 *
	 * @param <T> Il tipo di eccezione da lanciare in caso di violazione di vincoli di integrit&agrave;.
	 * @param e L'ecezzione da analizzare.
	 * @param serviceName Il nome del servizio da cui questo metodo &egrave; richiamato.
	 * @param methodName Il nome del metodo da cui questo metodo &egrave; richiamato.
	 * @param exClass La {@code Class} corrispondente al tipo di eccezione da sollevare.
	 *
	 * @throws T Se l'eccezione specificata corrisponde ad una violazione dei vincoli di integrit&agrave;.
	 */
	//Spostato dal publisher per questioni di errodi di compilazione
	//TODO: verificare il perchè.
	public <T extends Exception> void checkIntegrityException(RollbackException e, String serviceName,
			String methodName, Class<T> exClass)
	throws T {
		Throwable cause = e.getCause();

		if (cause == null) {
			return;
		}

		if (cause instanceof IntegrityException) {
			IntegrityException ie = (IntegrityException) cause;
			Constructor<T> exConstructor = null;
			StringBuilder messageBuilder = new StringBuilder(64);

			messageBuilder.append("Errore di integrita`: \n");

			for (IntegrityRecord record : ie.getRecords()) {
				String translated = null;
				try {
					translated = IntegrityViolationHandler.translateIntegrityRecordMessage(record, dictionaryService);
				} catch (DictionaryRuntimeException dre) {
					translated = record.getMessage();
				}

				messageBuilder.append(translated).append("\n");
			}

			logger.info("[" + serviceName + "::" + methodName + "] " + messageBuilder.toString());

			try {
				exConstructor = exClass.getConstructor(String.class);
				T exception = exConstructor.newInstance(messageBuilder.toString());

				throw exception;
			} catch (SecurityException ex) {
			} catch (NoSuchMethodException ex) {
			} catch (IllegalArgumentException ex) {
			} catch (InstantiationException ex) {
			} catch (IllegalAccessException ex) {
			} catch (InvocationTargetException ex) {
			}
		}
	}

	public ResultAssociation[] getAssociations(Node node, String assocType, int maxResults, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		logger.debug("[EcmEngineFeatureBean::getAssociations] BEGIN");

		validate(ValidationType.NODE, "node", node);
		validate(ValidationType.ASSOC_TYPE, "assocType", assocType);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "Uid: " + node.getUid() + " - U: " + context.getUsername();
		ResultAssociation [] associations = null;

		start();

		try {
			authenticateOnRepository(context, null);

			dumpElapsed("EcmEngineFeatureBean", "getAssociations", logCtx, "Autenticazione completata.");

			final NodeRef nodeRef = checkNodeExists(node, null);

			associations = translateAssociations(nodeRef, assocType, maxResults);

			dumpElapsed("EcmEngineFeatureBean", "getAssociations", logCtx, "Ricerca Associazioni completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

			logger.debug("[EcmEngineFeatureBean::getAssociations] Ricerca Associazioni completata -" +
					" " + (associations != null ? associations.length : 0) + " risultati.");

		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineFeatureBean", "getAssociations", context.getUsername(), null);
			checkAccessException(e, "EcmEngineFeatureBean", "getAssociations", "User: " + context.getUsername(), null);

			logger.error("[EcmEngineFeatureBean::getAssociations] Foundation services error: " + e.getCode());
			throw new SearchException("Errore dei servizi applicativi: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} finally {
			stop();
			logger.debug("[EcmEngineFeatureBean::getAssociations] END");
		}
		return associations;
	}

	protected ResultAssociation[] translateAssociations(NodeRef node, String type, int maxResults)
	throws NodeRuntimeException, DictionaryRuntimeException {

		logger.debug("[EcmEngineFeatureBean::translateAssociations] BEGIN");
		if(logger.isDebugEnabled()) {
			logger.debug("[EcmEngineFeatureBean::translateAssociations] Traduzione associazioni del nodo: " + node + " [T: " + type + "]");
		}
		ResultAssociation[] associations = null;

		try	{
			if (type.equals(ECMENGINE_ASSOC_TYPE_PARENT)) {

				// Padri del nodo
				List<ChildAssociationRef> parentAssociations = nodeService.getParentAssocs(node);

				if (parentAssociations.isEmpty()) {

					// Non ci sono padri
					return null;
				}

				int j = 0;

				if (maxResults == 0) {
					associations = new ResultAssociation[parentAssociations.size()];
				} else {
					associations = new ResultAssociation[(parentAssociations.size() > maxResults) ? maxResults : parentAssociations.size()];
				}

				for (ChildAssociationRef parentAssoc : parentAssociations){
					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::translateAssociations] Associazione parent: " +parentAssoc);
					}
					associations[j] = new ResultAssociation();
					associations[j].setChildAssociation(true);
					associations[j].setPrefixedName(
							dictionaryService.resolveQNameToPrefixName(parentAssoc.getQName()));
					associations[j].setTypePrefixedName(
							dictionaryService.resolveQNameToPrefixName(parentAssoc.getTypeQName()));
					if (parentAssoc.getParentRef() != null)
						associations[j].setTargetUid(parentAssoc.getParentRef().getId());

					if (j == (maxResults - 1)) {
						break;
					} else {
						j++;
					}
				}
			} else if (type.equals(ECMENGINE_ASSOC_TYPE_CHILD)) {

				// Figli del nodo
				List<ChildAssociationRef> childAssociations = nodeService.getChildAssocs(node);
				logger.debug("[EcmEngineFeatureBean::translateAssociations] Children: " + childAssociations.size());

				if (childAssociations.isEmpty()) {

					// Non ci sono padri
					return null;
				}

				int j = 0;

				if (maxResults == 0){
					associations = new ResultAssociation[childAssociations.size()];
				} else {
					associations = new ResultAssociation[(childAssociations.size() > maxResults) ? maxResults : childAssociations.size()];
				}

				for (ChildAssociationRef childAssoc : childAssociations){
					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::translateAssociations] Associazione child: " +
								childAssoc);
					}
					associations[j] = new ResultAssociation();
					associations[j].setChildAssociation(true);
					associations[j].setPrefixedName(
							dictionaryService.resolveQNameToPrefixName(childAssoc.getQName()));
					associations[j].setTypePrefixedName(
							dictionaryService.resolveQNameToPrefixName(childAssoc.getTypeQName()));
					associations[j].setTargetUid(childAssoc.getChildRef().getId());

					if (j == (maxResults - 1)) {
						break;
					} else {
						j++;
					}
				}
			} else if (type.equals(ECMENGINE_ASSOC_TYPE_TARGET)) {

				// Link del nodo
				List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(node,
						RegexQNamePattern.MATCH_ALL);

				if (targetAssocs.isEmpty()) {

					// Non ci sono associazioni semplici che partono da questo nodo
					return null;
				}

				int j = 0;

				if (maxResults == 0){
					associations = new ResultAssociation[targetAssocs.size()];
				} else {
					associations = new ResultAssociation[(targetAssocs.size() > maxResults) ? maxResults : targetAssocs.size()];
				}

				for (AssociationRef targetAssoc : targetAssocs) {

					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::translateAssociations] Associazione semplice target: " +
								targetAssoc);
					}
					associations[j] = new ResultAssociation();
					associations[j].setChildAssociation(false);
					associations[j].setTypePrefixedName(
							dictionaryService.resolveQNameToPrefixName(targetAssoc.getTypeQName()));
					associations[j].setTargetUid(targetAssoc.getTargetRef().getId());

					if (j == (maxResults - 1)) {
						break;
					} else {
						j++;
					}
				}
			} else if (type.equals(ECMENGINE_ASSOC_TYPE_SOURCE)) {

				// Link del nodo
				List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(node,
						RegexQNamePattern.MATCH_ALL);

				if (sourceAssocs.isEmpty()) {

					// Non ci sono associazioni semplici che portano a questo nodo
					return null;
				}

				int j = 0;

				if (maxResults == 0) {
					associations = new ResultAssociation[sourceAssocs.size()];
				} else {
					associations = new ResultAssociation[(sourceAssocs.size() > maxResults) ? maxResults : sourceAssocs.size()];
				}

				for (AssociationRef sourceAssoc : sourceAssocs) {

					if(logger.isDebugEnabled()) {
						logger.debug("[EcmEngineFeatureBean::translateAssociations] Associazione semplice source: " +
								sourceAssoc);
					}
					associations[j] = new ResultAssociation();
					associations[j].setChildAssociation(false);
					associations[j].setTypePrefixedName(
							dictionaryService.resolveQNameToPrefixName(sourceAssoc.getTypeQName()));
					associations[j].setTargetUid(sourceAssoc.getSourceRef().getId());

					if (j == (maxResults - 1)) {
						break;
					} else {
						j++;
					}
				}
			} else {

				// Questo punto dovrebbe essere irraggiungibile.
				return null;
			}
		} finally {
			logger.debug("[EcmEngineFeatureBean::translateAssociations] END");
		}
		return associations;
	}

	protected void updateMetadataNoTransaction(Node node, Content newContent, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] BEGIN");

		validate(ValidationType.NODE, "node", node);
		//		validate(ValidationType.NOT_NULL, "newContent", newContent);
		validate(ValidationType.CONTENT_WRITE_METADATA, "newContent", newContent);
		validate(ValidationType.OPERATION_CONTEXT, "context", context);

		final String logCtx = "N: " + node.getUid() + " - CN: " + newContent.getPrefixedName();

		start(); // Avvia stopwatch

		try {
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineFeatureBean", "updateMetadataNoTransaction", logCtx, "Autenticazione completata");

			final NodeRef nodeRef = checkNodeExists(node,null);

			/*
			 * Prima di aggiornare i metadati rimuoviamo gli aspect non piu` utilizzati.
			 */
			Set<QName> aspectsToRemove = nodeService.getAspects(nodeRef);
			final Aspect[] newAspects = newContent.getAspects();

			for (int i = 0; newAspects != null && i < newAspects.length; i++) {
				final QName aspectName = dictionaryService.resolvePrefixNameToQName(
						newAspects[i].getPrefixedName());
				aspectsToRemove.remove(aspectName);
			}

			logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Rimozione " +
					aspectsToRemove.size() + " vecchi aspect.");
			for (QName aspect : aspectsToRemove) {
				nodeService.removeAspect(nodeRef, aspect);
			}
			dumpElapsed("EcmEngineFeatureBean", "updateMetadataNoTransaction", logCtx,
					"Rimozione vecchi aspect completata: " +
					aspectsToRemove.size() + " aspect.");

			logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Conversione metadati.");
			final Property [] newProps = newContent.getProperties();
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

			/*
			 * Aggiornamento della mappa delle proprieta` 'properties':
			 * - nuove proprieta` con valore null vengono rimosse;
			 * - nuove proprieta` con valore vengono aggiornate;
			 * - proprieta` non modificabili rimangono inalterate;
			 * - vecchie proprieta` non specificate nel nuovo array rimangono inalterate.
			 */
			for (int i = 0; newProps != null && i < newProps.length; i++) {
				QName propName = null;
				PropertyDefinition propDef = null;
				boolean isProtected = false;
				try {
					propName = dictionaryService.resolvePrefixNameToQName(newProps[i].getPrefixedName());
					propDef = dictionaryService.getProperty(propName);
					if (propDef != null) {
						isProtected = propDef.isProtected();
					}
				} catch(DictionaryRuntimeException dre) {
					throw new InvalidParameterException("Invalid property: "+newProps[i].getPrefixedName());
				}

				if (isProtected) {
					if (newProps[i].getValue() != null) {
						throw new InvalidParameterException("property "+newProps[i].getPrefixedName()+" is protected and cannot be overwritten");
					}
				} else {
					if (newProps[i].getValue() != null) {
						// Overwrite values
						properties.put(propName, translatePropertyValuesToSerializable(newProps[i]));
					} else {
						// Remove
						properties.remove(propName);
					}
				}
			}

			dumpElapsed("EcmEngineFeatureBean", "updateMetadataNoTransaction", logCtx,
					"Conversione nuovi metadati completata: " +
					((newProps != null) ? newProps.length : "0") + " metadati.");

			nodeService.setProperties(nodeRef, properties);
			dumpElapsed("EcmEngineFeatureBean", "updateMetadataNoTransaction", logCtx, "Metadati aggiornati.");

			// BEGIN AGGIUNTA NUOVI ASPECT

			// Ciclo sugli aspect
			logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Impostazione degli aspect.");
			for (int i = 0; newAspects != null && i < newAspects.length; i++) {
				final QName aspectName = dictionaryService.resolvePrefixNameToQName(
						newAspects[i].getPrefixedName());
				final Property [] props = newAspects[i].getProperties();
				final boolean alreadySet = nodeService.hasAspect(nodeRef, aspectName);

				if (props == null) {

					if (!alreadySet) {
						// Aspect mancante ma senza property.
						if(logger.isDebugEnabled()) {
							logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Impostazione dei " +
									"metadati dell'aspect: " + newAspects[i].getPrefixedName());
						}
						nodeService.addAspect(nodeRef, aspectName, null);
					}

					// Gli aspect senza property gia` presenti rimangono inalterati
				} else {

					// Aspect con property, quindi da aggiungere.
					final Map<QName, Serializable> aspectProps = translatePropertyArray(props);
					nodeService.addAspect(nodeRef, aspectName, aspectProps);
				}
			}
			dumpElapsed("EcmEngineFeatureBean", "updateMetadataNoTransaction", logCtx,
					"Aspect modificati: " + ((newAspects != null) ? newAspects.length : "0"));
			// END GESTIONE ASPECT

			QName type = nodeService.getType(nodeRef);

			if (ContentModel.TYPE_CONTENT.toString().equalsIgnoreCase(type.toString())) {

				logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Tipo Nodo: " + type.toString());

				Map<QName, Serializable> modifiedAspectProps = new HashMap<QName, Serializable>();

				Date dataOdierna = new Date();
				//"yyyy-MM-dd'T'HH:mm:ss.sssZ"

				modifiedAspectProps.put(EcmEngineModelConstants.PROP_DATA_MODIFICA, dataOdierna);

				//add Aspect ecm-sys:modified
				//E' necessario inserire questo aspect per il job di backup(MetaDataBackupJob)
				nodeService.addAspect(nodeRef, EcmEngineModelConstants.ASPECT_MODIFIED, modifiedAspectProps);
				logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Add aspect per il Job di Backup: "
						+ EcmEngineModelConstants.ASPECT_MODIFIED.getLocalName());
			}

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineFeatureBean", "updateMetadataNoTransaction", logCtx, context, node.getUid(),
					"Name: " + newContent.getPrefixedName());
		} catch (EcmEngineFoundationException e) {
			if (e.getCode().equals(FoundationErrorCodes.DUPLICATE_CHILD_ERROR)) {
				logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] Cannot create node. " +
						"Duplicate child: " + newContent.getPrefixedName());
				throw new UpdateException("Cannot create node. Duplicate child: " + newContent.getPrefixedName());
			}

			checkCredentialsException(e, "EcmEngineFeatureBean", "updateMetadataNoTransaction", context.getUsername(),null);
			checkAccessException(e, "EcmEngineFeatureBean", "updateMetadataNoTransaction", "User: " + context.getUsername(),null);

			logger.error("[EcmEngineFeatureBean::updateMetadataNoTransaction] Foundation services error: " + e.getCode());
			throw new UpdateException("Backend services error: " + e.getCode());
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineFeatureBean::updateMetadataNoTransaction] END");
		}
	}


	protected Property createPropertyDTO(String prefixedName, String dataType, boolean multivalue) {
		Property prop = new Property();
		prop.setPrefixedName(prefixedName);
		prop.setDataType(dataType);
		prop.setMultivalue(multivalue);
		return prop;
	}

    // Cache della XPATH id su temp
	protected static Object TEMPORANEY_XPATH_CHECK = new Object();
	protected static String TEMPORANEY_XPATH_PARENT_ID="";
	protected String getTemporaneyParentID(){
	    synchronized(TEMPORANEY_XPATH_CHECK){
           if( TEMPORANEY_XPATH_PARENT_ID.equals("") ){
              SearchParams searchParams=new SearchParams();
                           searchParams.setXPathQuery(ECMENGINE_TEMPORANEY_HOME+ECMENGINE_TEMPORANEY_XPATH);

              OperationContext temp = getTemporaneyContext(null);
              try{
                  Node[] nodeResponse = xpathSearchNoMetadata(searchParams,temp).getNodeArray();
      			  if(nodeResponse!=null && nodeResponse.length>0){
                     TEMPORANEY_XPATH_PARENT_ID=nodeResponse[0].getUid();
                     logger.debug("[EcmEngineFeatureBean::getTemporaneyParentID] Impostato TEMPORANEY_XPATH_PARENT_ID: " +TEMPORANEY_XPATH_PARENT_ID);
                  } else {
                     logger.info("[EcmEngineFeatureBean::getTemporaneyParentID] Non trovato TEMPORANEY_XPATH_PARENT_ID: " +TEMPORANEY_XPATH_PARENT_ID);
                  }
              } catch (Exception e){
                  //throw new RuntimeException(e.getMessage());
                  logger.error("[EcmEngineFeatureBean::getTemporaneyParentID] Errore nell'accesso a " +TEMPORANEY_XPATH_PARENT_ID);
                  logger.error("[EcmEngineFeatureBean::getTemporaneyParentID] " +e.getMessage());
              }


              try {
                  // Se non ho il PATH per i temp
                  if( TEMPORANEY_XPATH_PARENT_ID.length()<1 ){
                      // Creo il folder
                      SearchParams searchParamsHome=new SearchParams();
                                   searchParamsHome.setXPathQuery(ECMENGINE_TEMPORANEY_HOME);
                      String homeUID = xpathSearchNoMetadata(searchParamsHome,temp).getNodeArray()[0].getUid();

                      // Creo il temp
                      Node parentNode = new Node(homeUID);
                      Content content = new Content();
                      content.setPrefixedName( ECMENGINE_TEMPORANEY_XPATH.substring(1) );
                      content.setParentAssocTypePrefixedName("cm:contains");
                      content.setModelPrefixedName("cm:contentmodel");
                      content.setTypePrefixedName("cm:folder");

                      Property [] props = new Property[1];
                      props[0] = new Property("cm:name", "text", false);
                      props[0].setValues(new String [] { ECMENGINE_TEMPORANEY_XPATH.substring(4) });

                      content.setProperties(props);
                      try {
                          TEMPORANEY_XPATH_PARENT_ID = createContent(parentNode, content, temp).getUid();
                          logger.info("[EcmEngineFeatureBean::getTemporaneyParentID] Creato TEMPORANEY_XPATH_PARENT_ID: " +TEMPORANEY_XPATH_PARENT_ID);
                      } catch (Exception e) {
                          logger.error("[EcmEngineFeatureBean::getTemporaneyParentID] Errore di creazione di " +TEMPORANEY_XPATH_PARENT_ID);
                          throw new RuntimeException(e.getMessage());
                      }

                  }
              } catch (Exception e) {
                  throw new RuntimeException(e.getMessage());
              }
           }
        }
		return TEMPORANEY_XPATH_PARENT_ID;
	}

    /*
      Prende l'OperationContext corrente, e crea uno nuovo, con le credenziali del TEMP, mantenendo il fruitore
      In mancanza di context, il fruitore viene impostato a ECMENGINE_TEMPORANEY_USERNAME
    */
	protected OperationContext getTemporaneyContext(OperationContext context){
        OperationContext ret = new OperationContext();
		ret.setUsername(ECMENGINE_TEMPORANEY_USERNAME+"@"+ECMENGINE_TEMPORANEY_TENANT_NAME);
		ret.setPassword(ECMENGINE_TEMPORANEY_PASSWORD);
        if( context==null ) {
            ret.setFruitore( ECMENGINE_TEMPORANEY_USERNAME );
        } else {
            ret.setFruitore( context.getFruitore() );
        }
		return ret;
	}

    // Crea un nome univoco per i content temporanei
	protected static Object TEMPORANEY_COUNT = new Object();
	protected static long TEMPORANEY_CONTENT_NAME_COUNT = System.currentTimeMillis();
	protected String getTemporaneyContentName(){
        String cRet = "";
	    synchronized(TEMPORANEY_COUNT){
		    TEMPORANEY_CONTENT_NAME_COUNT++;
            // MB: Eventualmente aggiungere un random
            cRet = "temp_" +TEMPORANEY_CONTENT_NAME_COUNT;
	    }
        return cRet;
	}

    // Crea un nome univoco per i content temporanei
	protected boolean isTemporaneyPresent(){
        boolean bRet = false;
        try {
            bRet = tenantAdminService.existsTenant(ECMENGINE_TEMPORANEY_TENANT_NAME);
        } catch (TenantRuntimeException tre){
    		logger.error("[EcmEngineFeatureBean::isTemporaneyPresent] Tenant temporaneo: [" +ECMENGINE_TEMPORANEY_TENANT_NAME +"] non presente");
	    }
        return bRet;
	}


	protected void deleteContentNoTransaction(Node node, OperationContext context)
	throws EcmEngineFoundationException,InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException {
		logger.debug("[EcmEngineFeatureBean::deleteContentNoTransaction] BEGIN");

        validate(ValidationType.NODE, "node", node);
        validate(ValidationType.OPERATION_CONTEXT, "context", context);

		start(); // Avvia stopwatch
		try{
			final String logCtx = "P: " + node.getUid();
			authenticateOnRepository(context, null);
			dumpElapsed("EcmEngineFeatureBean", "deleteContentNoTransaction", logCtx, "Autenticazione completata");

			final NodeRef nodeRef = checkNodeExists(node, null);

			//AF: Aggiungo controllo in caso di contenuto renditionable
			if(nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_RENDITIONABLE)){
				List<String> listaDegliXsl = (List<String>) nodeService.getProperty(nodeRef, EcmEngineModelConstants.PROP_RENDITION_XSL_ID);
				for(int i=0;i<listaDegliXsl.size();i++){
						String rendition = (String) nodeService.getProperty(new NodeRef(listaDegliXsl.get(i)), EcmEngineModelConstants.PROP_RENDITION_ID);
					try{
						deleteContentNoTransaction(new Node(rendition), context);
					}catch(EcmEngineFoundationException e){
						logger.warn("[EcmEngineFeatureBean::deleteContentNoTransaction] Trovata associazione nulla o non esistente tra Rendition Transformer e Rendition Document");
					}
					try{
						deleteContentNoTransaction(new Node(listaDegliXsl.get(i)), context);
					}catch(EcmEngineFoundationException e){
						logger.warn("[EcmEngineFeatureBean::deleteContentNoTransaction] Trovata associazione nulla o non esistente tra Renditionable e Rendition Transformer");
					}
				}
			}

			nodeService.deleteNode(nodeRef);
			dumpElapsed("EcmEngineFeatureBean", "deleteContentNoTransaction", logCtx, "Commit transazione.");
			logger.debug("[EcmEngineFeatureBean::deleteContentNoTransaction] Nodo eliminato");

			// INSERIMENTO AUDIT
			insertAudit("EcmEngineFeatureBean", "deleteContentNoTransaction", logCtx, context, node.getUid(),node.getUid());
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineFeatureBean", "deleteContentNoTransaction", context.getUsername(), null);
			checkAccessException(e, "EcmEngineFeatureBean", "deleteContentNoTransaction", "User: " + context.getUsername(), null);
			logger.error("[EcmEngineFeatureBean::deleteContentNoTransaction] Foundation services error: " + e.getCode());
			throw e;
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		}finally{
			stop();// Ferma stopwatch
			logger.debug("[EcmEngineFeatureBean::deleteContentNoTransaction] END");
		}
	}

	/**
	 * Metodo testResources
	 */
	public boolean testResources() throws RemoteException {

		logger.debug("[EcmEngineFeatureBean::testResources] BEGIN");
		final String logCtx = "";
		boolean returnValue = false;

		start();

		try {
			returnValue = true;
			OperationContext context=new OperationContext();
			context.setNomeFisico("TestResources");
			context.setFruitore("TestResources");
			insertAudit("EcmEngineFeatureBean", "TestResources", "TestResources", context, "TestResources", "TestResources");
			return returnValue;
		} finally {
			dumpElapsed("EcmEngineFeatureBean", "testResources", logCtx, "testResources");
			stop();
			logger.debug("[EcmEngineFeatureBean::testResources] END");
		}
	}
}

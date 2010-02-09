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

package it.doqui.index.ecmengine.business.personalization.encryption.content;

import it.doqui.index.ecmengine.business.personalization.encryption.CryptoContentService;
import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.CustomSecretKey;
import it.doqui.index.ecmengine.business.personalization.encryption.util.EncryptionUtils;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

// MB per gestione contentStore
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;
import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.TenantService;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.EmptyContentReader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.NodeContentContext;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.repo.content.ContentServicePolicies.OnContentReadPolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.content.transform.magick.ImageMagickContentTransformer;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CryptoContentServiceImpl implements CryptoContentService {

	private static Log logger = LogFactory.getLog(EncryptionUtils.ENCRYPTION_LOG_CATEGORY);

	private ContentStore store;
	private ContentStore tempStore;

	private RetryingTransactionHelper transactionHelper;
	private NodeService nodeService;
	private AVMService avmService;
	private DictionaryService dictionaryService;
	private ImageMagickContentTransformer imageMagickContentTransformer;
    private ContentTransformerRegistry transformerRegistry;
	private PolicyComponent policyComponent;

	ClassPolicyDelegate<ContentServicePolicies.OnContentUpdatePolicy> onContentUpdateDelegate;
    ClassPolicyDelegate<ContentServicePolicies.OnContentReadPolicy> onContentReadDelegate;

	public CryptoContentServiceImpl() {
		logger.debug("[CryptoContentServiceImpl::constructor] BEGIN");
		this.tempStore = new FileContentStore(TempFileProvider.getTempDir().getAbsolutePath());
		logger.debug("[CryptoContentServiceImpl::constructor] END");
	}

    public void init() {
        // Bind on update properties behavior
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                this, new JavaBehaviour(this, "onUpdateProperties"));

        // Register on content update policy
        this.onContentUpdateDelegate = this.policyComponent.registerClassPolicy(OnContentUpdatePolicy.class);
        this.onContentReadDelegate = this.policyComponent.registerClassPolicy(OnContentReadPolicy.class);
    }

    public void setTransactionService(TransactionService transactionService) {
        logger.warn("[CryptoContentService::setTransactionService] Property 'transactionService' has been replaced by 'retryingTransactionHelper'.");
    }

    public TransactionService getTransactionService() {
    	return null;
    }

    private TenantService tenantService;
    /**
     * Set the tenant service
     *
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setImageMagickContentTransformer(ImageMagickContentTransformer imageMagickContentTransformer) {
        this.imageMagickContentTransformer = imageMagickContentTransformer;
    }

    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        logger.debug("[CryptoContentServiceImpl::onUpdateProperties] BEGIN");

		try {
			boolean fire = false;
			boolean newContent = false;
			// check if any of the content properties have changed
			for (QName propertyQName : after.keySet()) {
				// is this a content property?
				PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
				if (propertyDef == null
						|| !propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
					// the property is not recognized or not a content type
					continue;
				}

				try {
					ContentData beforeValue = (ContentData) before.get(propertyQName);
					ContentData afterValue = (ContentData) after.get(propertyQName);
					if (afterValue != null
							&& afterValue.getContentUrl() == null) {
						// no URL - ignore
					} else if (!EqualsHelper.nullSafeEquals(beforeValue, afterValue)) {
						// So debug ...
						if (logger.isDebugEnabled()) {
							String beforeString = "";
							if (beforeValue != null) {
								beforeString = beforeValue.toString();
							}
							String afterString = "";
							if (afterValue != null) {
								afterString = afterValue.toString();
							}
	    				    logger.debug("[CryptoContentServiceImpl::onUpdateProperties] Before: '" + beforeString +"' -> After: '" + afterString + "'");
						}

						// Figure out if the content is new or not
						String beforeContentUrl = null;
						if (beforeValue != null) {
							beforeContentUrl = beforeValue.getContentUrl();
						}

						String afterContentUrl = null;
						if (afterValue != null) {
							afterContentUrl = afterValue.getContentUrl();
						}
						if (beforeContentUrl == null && afterContentUrl != null) {
							newContent = true;
						}

						// the content changed
						// at the moment, we are only interested in this one change
						fire = true;
						break;
					}
				} catch (ClassCastException e) {
					// properties don't conform to model
					continue;
				}
			}
			// fire?
			if (fire) {
				// Fire the content update policy
				Set<QName> types = new HashSet<QName>(this.nodeService.getAspects(nodeRef));
				types.add(this.nodeService.getType(nodeRef));
				OnContentUpdatePolicy policy = this.onContentUpdateDelegate.get(nodeRef, types);
				policy.onContentUpdate(nodeRef, newContent);
			}
		} finally {
			logger.debug("[CryptoContentServiceImpl::onUpdateProperties] END");
		}
    }


	public ContentTransformer getImageTransformer() {
		logger.debug("[CryptoContentServiceImpl::getImageTransformer] BEGIN");

		try {
			return imageMagickContentTransformer;
		} finally {
			logger.debug("[CryptoContentServiceImpl::getImageTransformer] END");
		}
	}

	public ContentReader getRawReader(String contentUrl) {
		logger.debug("[CryptoContentServiceImpl::getRawReader] BEGIN");

		try {
			ContentReader reader = null;
			// TODO: gestire encryption?
			try {
				reader = store.getReader(contentUrl);
			} catch (UnsupportedContentUrlException e) {
				// The URL is not supported, so we spoof it
				reader = new EmptyContentReader(contentUrl);
			}
			if (reader == null) {
				throw new AlfrescoRuntimeException("ContentStore implementations may not return null ContentReaders");
			}
			// set extra data on the reader
			reader.setMimetype(MimetypeMap.MIMETYPE_BINARY);
			reader.setEncoding("UTF-8");
			reader.setLocale(I18NUtil.getLocale());
			if (logger.isDebugEnabled()) {
			    logger.debug("[CryptoContentServiceImpl::getRawReader] Got RAW reader for URL: " +contentUrl +" [Reader: " +reader + "]");
		    }
			return reader;
		} finally {
			logger.debug("[CryptoContentServiceImpl::getRawReader] END");
		}
	}

	public ContentReader getReader(NodeRef nodeRef, QName propertyQName)
			throws InvalidNodeRefException, InvalidTypeException {
		logger.debug("[CryptoContentServiceImpl::getReader] BEGIN");

		try {
			return getDecryptingReader(nodeRef, propertyQName, null, null);
		} finally {
			logger.debug("[CryptoContentServiceImpl::getReader] END");
		}
	}

	public ContentReader getDecryptingReader(NodeRef nodeRef, QName propertyQName, CustomSecretKey key,
			CryptoTransformationSpec transform) {
		logger.debug("[CryptoContentServiceImpl::getDecryptingReader] BEGIN");

		try {
			return getDecryptingReader(nodeRef, propertyQName, key, transform, true);
		} finally {
			logger.debug("[CryptoContentServiceImpl::getDecryptingReader] END");
		}
	}

	private ContentReader getDecryptingReader(NodeRef nodeRef, QName propertyQName, CustomSecretKey key,
			CryptoTransformationSpec transform, boolean fireContentReadPolicy) {
        logger.debug("[CryptoContentServiceImpl::getDecryptingReader] BEGIN");

		try {
			ContentData contentData = null;
			Serializable propValue = nodeService.getProperty(nodeRef, propertyQName);

			if (propValue instanceof Collection<?>) {
				Collection<?> colPropValue = (Collection<?>) propValue;
				if (!colPropValue.isEmpty()) {
					propValue = (Serializable) colPropValue.iterator().next(); // Get the first element
				}
			}
			if (propValue instanceof ContentData) {
				contentData = (ContentData) propValue;
			}
			if (contentData == null) {
				PropertyDefinition contentPropDef = dictionaryService.getProperty(propertyQName);

				if (contentPropDef != null
						&& (!(contentPropDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)
								|| contentPropDef.getDataType().getName().equals(DataTypeDefinition.ANY)))) {
					throw new InvalidTypeException(
							"The node property must be of type content: \n   node: " + nodeRef
									+ "\n   property name: " + propertyQName
									+ "\n   property type: " + ((contentPropDef == null)
											? "unknown"
											: contentPropDef.getDataType()),
							propertyQName);
				}
			}
			// check that the URL is available
			if (contentData == null || contentData.getContentUrl() == null) {
				// there is no URL - the interface specifies that this is not an error condition
				return null;
			}

			if (logger.isDebugEnabled()) {
			    logger.debug("[CryptoContentServiceImpl::getDecryptingReader] ContentData: " + contentData);
			}

//			boolean needsDecryption = nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED)
//					&& (key != null)
//					&& (transform != null);
			boolean needsDecryption = (key != null) && (transform != null);

			String contentUrl = contentData.getContentUrl();
			ContentReader reader = null;

			ContentStore dynamicCS;
			try {
				dynamicCS = getContentStoreFromList( nodeRef );
			} finally {
				logger.debug("[CryptoContentServiceImpl::getDecryptingReader] error during ContentStore search");
			}
			if( dynamicCS==null ) {
				dynamicCS = store;
			}

			if (needsDecryption) {
				// The context of the read is entirely described by the URL
	        	if (transform.getIv() == null) {
	        		logger.debug("[CryptoContentServiceImpl::getDecryptingReader] Got null IV... creating new.");
	        		byte [] iv = CryptoTransformationSpec.generateIv(transform, key);
		        	transform.setIv(iv);
	        	}

				reader = new DecryptingContentReaderDecorator(dynamicCS.getReader(contentUrl), key, transform);
    			if (logger.isDebugEnabled()) {
	    			logger.debug("[CryptoContentServiceImpl::getDecryptingReader] Got decrypting reader: " + reader);
			    }
			} else {
				reader = dynamicCS.getReader(contentUrl);
    			if (logger.isDebugEnabled()) {
				    logger.debug("[CryptoContentServiceImpl::getDecryptingReader] Got plain reader: " + reader);
			    }
			}
			if (reader == null) {
				throw new AlfrescoRuntimeException(
						"ContentStore implementations may not return null ContentReaders");
			}
			// set extra data on the reader
			reader.setMimetype(contentData.getMimetype());
			reader.setEncoding(contentData.getEncoding());
			reader.setLocale(contentData.getLocale());
			// Fire the content read policy
			if (reader != null && fireContentReadPolicy == true) {

    			if (logger.isDebugEnabled()) {
				    logger.debug("[CryptoContentServiceImpl::getDecryptingReader] Firing OnContentReadPolicy on node: " + nodeRef);
			    }

				// Fire the content update policy
				Set<QName> types = new HashSet<QName>(this.nodeService.getAspects(nodeRef));
				types.add(this.nodeService.getType(nodeRef));
				OnContentReadPolicy policy = this.onContentReadDelegate.get(nodeRef, types);
				policy.onContentRead(nodeRef);

    			if (logger.isDebugEnabled()) {
    				logger.debug("[CryptoContentServiceImpl::getDecryptingReader] OnContentReadPolicy fired on node: " + nodeRef);
			    }
			}
			// we don't listen for anything
			// result may be null - but interface contract says we may return null
			return reader;
		} finally {
			logger.debug("[CryptoContentServiceImpl::getDecryptingReader] END");
		}
	}

	public ContentWriter getTempWriter() {
		logger.debug("[CryptoContentServiceImpl::getTempWriter] BEGIN");

		try {
			return tempStore.getWriter(ContentContext.NULL_CONTEXT);
		} finally {
			logger.debug("[CryptoContentServiceImpl::getTempWriter] END");
		}
	}

	public ContentTransformer getTransformer(String sourceMimeType, String targetMimeType) {
        logger.debug("[CryptoContentServiceImpl::getTransformer] BEGIN");

		try {
			return transformerRegistry.getTransformer(sourceMimeType, targetMimeType);
		} finally {
			logger.debug("[CryptoContentServiceImpl::getTransformer] END");
		}
	}

    public ContentWriter getEncryptingWriter(NodeRef nodeRef, QName propertyQName, boolean update,
    		CustomSecretKey key, CryptoTransformationSpec transform) {
        logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] BEGIN");

		try {
			if (nodeRef == null) {
				ContentContext ctx = new ContentContext(null, null);
				// for this case, we just give back a valid URL into the content store
				ContentWriter writer = store.getWriter(ctx);

				return writer;
			}

			// Needs encryption if marked with "encrypted" aspect and isn't encrypted from source
//			boolean needsEncryption = nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_ENCRYPTED)
//					&& !((Boolean)nodeService.getProperty(nodeRef, EcmEngineModelConstants.PROP_ENCRYPTED_FROM_SOURCE)).booleanValue()
//					&& (key != null) && (transform != null);
			boolean needsEncryption = (key != null) && (transform != null);
    		if (logger.isDebugEnabled()) {
		    	logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] Node \"" + nodeRef + "\" needs encryption: " + needsEncryption);
			}

			// FIXME: ottimizzare numero di chiamate per controllo aspect (una e` interna a getDecryptingReader())
			// check for an existing URL - the get of the reader will perform type checking
			ContentReader existingContentReader = getDecryptingReader(nodeRef,
					propertyQName, key, transform, false);


			// get the content using the (potentially) existing content - the new content
			// can be wherever the store decides.
			ContentContext ctx = new NodeContentContext(existingContentReader, null, nodeRef, propertyQName);
			ContentWriter writer = null;

			ContentStore dynamicCS;
			try {
				dynamicCS = getContentStoreFromList( nodeRef );
			} finally {
				logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] error during ContentStore search");
			}
    		if (logger.isDebugEnabled()) {
			    logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] ContentStore dynamic "+dynamicCS);
			}
			if( dynamicCS==null ) {
				dynamicCS = store;
			}

	        if (needsEncryption) {
	            // The context of the read is entirely described by the URL
	        	if (transform.getIv() == null) {
	        		logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] Got null IV... creating new.");
	        		byte [] iv = CryptoTransformationSpec.generateIv(transform, key);
		        	transform.setIv(iv);
	        	}

	            writer = new EncryptingContentWriterDecorator(dynamicCS.getWriter(ctx), key, transform);
	    		if (logger.isDebugEnabled()) {
	                logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] Got encrypting writer: " + writer);
	            }
	        } else {
	        	writer = dynamicCS.getWriter(ctx);
	    		if (logger.isDebugEnabled()) {
	        	    logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] Got plain writer: " + writer);
	            }
	        }
	    	if (logger.isDebugEnabled()) {
		    	logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] Writer "+writer);
	        }

			// Special case for AVM repository.
			Serializable contentValue = null;
			if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM)) {
				Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
				contentValue = avmService.getContentDataForWrite(avmVersionPath.getSecond());
			} else {
				contentValue = nodeService.getProperty(nodeRef, propertyQName);
			}

			// set extra data on the reader if the property is pre-existing
			if (contentValue != null && contentValue instanceof ContentData) {
				ContentData contentData = (ContentData) contentValue;

    	    	if (logger.isDebugEnabled()) {
	    			logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] ContentData: " + contentData);
		    	}

				writer.setMimetype(contentData.getMimetype());
				writer.setEncoding(contentData.getEncoding());
				writer.setLocale(contentData.getLocale());
			}

			// attach a listener if required
			if (update) {
				WriteStreamListener listener = new WriteStreamListener(nodeService, avmService, nodeRef, propertyQName, writer);

    	    	if (logger.isDebugEnabled()) {
				    logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] Adding WriteStreamListener: " + listener);
			    }

				writer.addListener(listener);
				writer.setRetryingTransactionHelper(transactionHelper);
			}

			return writer;
		} finally {
			logger.debug("[CryptoContentServiceImpl::getEncryptingWriter] END");
		}
    }

	public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update)
			throws InvalidNodeRefException, InvalidTypeException {
		logger.debug("[CryptoContentServiceImpl::getWriter] BEGIN");

		try {
			return getEncryptingWriter(nodeRef, propertyQName, update, null, null);
		} finally {
			logger.debug("[CryptoContentServiceImpl::getWriter] END");
		}
	}

	public boolean isTransformable(ContentReader reader, ContentWriter writer) {
		logger.debug("[CryptoContentServiceImpl::isTransformable] BEGIN");

		try {
			return !(reader instanceof DecryptingContentReaderDecorator || writer instanceof EncryptingContentWriterDecorator);
		} finally {
			logger.debug("[CryptoContentServiceImpl::isTransformable] END");
		}
	}

	public void transform(ContentReader reader, ContentWriter writer)
			throws NoTransformerException, ContentIOException {
		logger.debug("[CryptoContentServiceImpl::transform] BEGIN");

		try {
			if ((reader instanceof DecryptingContentReaderDecorator || writer instanceof EncryptingContentWriterDecorator)) {
				throw new AlfrescoRuntimeException(
						"Cannot transform content if source or destination requires encryption!");
			}
			// check that source and target mimetypes are available
			String sourceMimetype = reader.getMimetype();
			if (sourceMimetype == null) {
				throw new AlfrescoRuntimeException(
						"The content reader mimetype must be set: " + reader);
			}
			String targetMimetype = writer.getMimetype();
			if (targetMimetype == null) {
				throw new AlfrescoRuntimeException(
						"The content writer mimetype must be set: " + writer);
			}
			// look for a transformer
			ContentTransformer transformer = transformerRegistry
					.getTransformer(sourceMimetype, targetMimetype);
			if (transformer == null) {
				throw new NoTransformerException(sourceMimetype, targetMimetype);
			}
			transformer.transform(reader, writer);
		} finally {
			logger.debug("[CryptoContentServiceImpl::transform] END");
		}
	}

	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

    public void setRetryingTransactionHelper(RetryingTransactionHelper helper) {
        this.transactionHelper = helper;
    }

    public RetryingTransactionHelper getRetryingTransactionHelper() {
    	return transactionHelper;
    }

	public ContentStore getStore() {
		return store;
	}

	public void setStore(ContentStore store) {
		this.store = store;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

    private static class WriteStreamListener implements ContentStreamListener {
        private NodeService nodeService;
        private NodeRef nodeRef;
        private QName propertyQName;
        private ContentWriter writer;

        public WriteStreamListener(NodeService nodeService, AVMService avmService,
                NodeRef nodeRef, QName propertyQName, ContentWriter writer) {
            this.nodeService = nodeService;
            this.nodeRef = nodeRef;
            this.propertyQName = propertyQName;
            this.writer = writer;
        }

        public void contentStreamClosed() throws ContentIOException {
        	logger.debug("[WriteStreamListener::contentStreamClosed] BEGIN");
            try {
                // set the full content property
                ContentData contentData = writer.getContentData();

                // Bypass NodeService for AVM stores.
                if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM)) {
                    nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, contentData);
                } else {
                    nodeService.setProperty(nodeRef, propertyQName, contentData);
                }

                if (logger.isDebugEnabled()) {
                	logger.debug("[WriteStreamListener::contentStreamClosed] Updated node: " + nodeRef +
                			" [Property: " + propertyQName + " - ContentData: " + contentData + "]");
                }
            } catch (ContentQuotaException qe) {
            	// Throw up the chain...
                throw qe;
            } catch (Throwable e) {
                throw new ContentIOException("Failed to set content property on stream closure: \n" +
                        "   node: " + nodeRef + "\n" +
                        "   property: " + propertyQName + "\n" +
                        "   writer: " + writer,
                        e);
            } finally {
            	logger.debug("[WriteStreamListener::contentStreamClosed] END");
            }
        }
    }


	public ContentTransformerRegistry getTransformerRegistry() {
		return transformerRegistry;
	}

	public void setTransformerRegistry(
			ContentTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	public AVMService getAvmService() {
		return avmService;
	}

	public void setAvmService(AVMService avmService) {
		this.avmService = avmService;
	}

	private ContentStore getContentStoreFromList( NodeRef nodeRef ){
		ContentStore csRet = null;

		String storageId = null;
		// Vedo se c'e' l'aspect Storage
		final boolean storage = this.nodeService.hasAspect(nodeRef, EcmEngineModelConstants.ASPECT_STORAGE);
		if( storage ){
			// Prendo la property PROP_STORAGE_ID
			Map<QName, Serializable> props = this.nodeService.getProperties(nodeRef);
			storageId = (String) props.get(EcmEngineModelConstants.PROP_STORAGE_ID);
     		if (logger.isDebugEnabled()) {
                logger.debug("[CryptoContentServiceImpl::getContentStoreFromList] StorageID (" +storageId +") in (" +nodeRef +")");
			}
		}

		if( storageId!=null && storageId.length()>0 ){
            String tenantName = tenantService.getCurrentUserDomain();

            // MB: estraggo la lista dei contentStore
            List<ContentStoreDefinition> contentStores = null;
            if( tenantName.length()>0 ) {
    			Tenant tenant = tenantService.getTenant( tenantName );
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

			// Se ho il tenant, con dei contentStore provo a prendere quello associato
			if( contentStores!=null )
			{
			    int nProg = 0;
			    for( ContentStoreDefinition tcs : contentStores )
			    {
			        nProg++;

			        // Dal protocol assegnato al Tenant, accedo al bean del repository che gestisce quel protocol
					if( tcs.getProtocol().equals( storageId ) ){
                   		if (logger.isDebugEnabled()) {
			    			logger.debug("[CryptoContentServiceImpl::getContentStoreFromList] Storage FOUND " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource() );
		    			}

						try {
							// Istanzia il nuovo content store passando dal manager
							csRet = ContentStoreManager.getInstance().getContentStore( tcs );

							// Se il ContentStore non e' null, lo inizializzo col path dentro al ContentStoreDefinition
							if( csRet==null ) {
								logger.error("[CryptoContentServiceImpl::getContentStoreFromList] unable to get contentStore (" +storageId +") not found in tenant (" +tenantService.getCurrentUserDomain() +")");
								logger.error("[CryptoContentServiceImpl::getContentStoreFromList] ContentStoreDefinition " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource());
							} else {
    				    		if (logger.isDebugEnabled()) {
	    							logger.debug("[CryptoContentServiceImpl::getContentStoreFromList] Found contentStore for tenant domain '" +tenantService.getCurrentUserDomain()+"-"+nProg +" " +csRet.toString());
		    					}
							}
						} catch( org.alfresco.error.AlfrescoRuntimeException e ){
							// Errore nell'uso del ContentStore
					    	logger.error("[CryptoContentServiceImpl::getContentStoreFromList] unable to get contentStore (" +storageId +") not found in tenant (" +tenantService.getCurrentUserDomain() +")");
						}
					}
					if( csRet!=null ) break;
			    }
			}
       		if (logger.isDebugEnabled()) {
	        	logger.debug("[CryptoContentServiceImpl::getContentStoreFromList] (" +nodeRef +") " +storageId +" " +csRet);
			}

	    	if( csRet==null ){
	    		logger.error("[CryptoContentServiceImpl::getContentStoreFromList] ContentStore (" +storageId +") not found in tenant (" +tenantService.getCurrentUserDomain() +")");
	    		if( contentStores!=null ){
	    			for( Object s : contentStores ) {
	   		            ContentStoreDefinition tcs = (ContentStoreDefinition)s;
	 					logger.error("[CryptoContentServiceImpl::getContentStoreFromList] ContentStoreDefinition " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource());
					}
	    		}
                // MB: provvisoria
				throw new AlfrescoRuntimeException("ContentStore (" +storageId +") not found in tenant (" +tenantService.getCurrentUserDomain() +")");
	    	}
		}

		return csRet;
	}


}

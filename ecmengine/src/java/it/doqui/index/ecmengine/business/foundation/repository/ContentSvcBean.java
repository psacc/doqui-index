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
 
package it.doqui.index.ecmengine.business.foundation.repository;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationBeansConstants;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.personalization.encryption.CryptoContentService;
import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.CustomSecretKey;
import it.doqui.index.ecmengine.business.personalization.encryption.exception.EncryptionRuntimeException;
import it.doqui.index.ecmengine.exception.repository.ContentRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


public class ContentSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants, FoundationBeansConstants {	

	private static final long serialVersionUID = 8233020265470978732L;

	public ContentWriter getWriter(NodeRef nodeRef, QName property, boolean arg2) 
	throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::getWriter] BEGIN");
		ContentWriter writer = null;
		try {
			ContentService cs = (ContentService) serviceRegistry.getContentService();
			writer = cs.getWriter(nodeRef, property, arg2);
		} catch (Exception e) {
			handleContentServiceException("getWriter", e);
		} finally {			
			logger.debug("[ContentSvcBean::getWriter] END");
		}
		return writer;
	}
	
	public ContentWriter getTempWriter() throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::getTempWriter] BEGIN");
		ContentWriter writer = null;
		try {
			ContentService cs = (ContentService)serviceRegistry.getContentService();
			writer = cs.getTempWriter();
		} catch (Exception e) {
			handleContentServiceException("getTempWriter", e);
		} finally {			
			logger.debug("[ContentSvcBean::getTempWriter] END");
		}
		return writer;
	}
	
	public ContentTransformer getImageTransformer() throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::getImageTransformer] BEGIN");
		ContentTransformer transformer = null;
		try {
			ContentService cs = (ContentService)serviceRegistry.getContentService();
			transformer = cs.getImageTransformer();
		} catch (Exception e) {
			handleContentServiceException("getImageTransformer", e);
		} finally {			
			logger.debug("[ContentSvcBean::getImageTransformer] END");
		}
		return transformer;
	}
	
	public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype) throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::getTransformer] BEGIN");
		ContentTransformer transformer = null;
		try {
			ContentService cs = (ContentService)serviceRegistry.getContentService();
			transformer = cs.getTransformer(sourceMimetype, targetMimetype);
		} catch (Exception e) {
			handleContentServiceException("getTransformer", e);
		} finally {			
			logger.debug("[ContentSvcBean::getTransformer] END");
		}
		return transformer;
	}

	public ContentReader getReader(NodeRef nodeRef, QName propertyQName) 
	throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::getReader] BEGIN");
		ContentReader reader = null;
		
		try {
			ContentService cs = (ContentService)serviceRegistry.getContentService();
			reader = cs.getReader(nodeRef, propertyQName);
		} catch (Exception e) {
			handleContentServiceException("getReader", e);
		} finally {			
			logger.debug("[ContentSvcBean::getReader] END");
		}
		return reader;
	}
	
	public boolean isTransformable(ContentReader reader, ContentWriter writer) throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::isTransformable] BEGIN");
		boolean transformable = false;
		
		try {
			ContentService cs = (ContentService)serviceRegistry.getContentService();
			transformable = cs.isTransformable(reader, writer);
		} catch (Exception e) {
			handleContentServiceException("isTransformable", e);
		} finally {			
			logger.debug("[ContentSvcBean::isTransformable] END");
		}
		return transformable;
	}
	
	public void transform(ContentReader reader, ContentWriter writer)  throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::transform] BEGIN");
		
		try {
			ContentService cs = (ContentService)serviceRegistry.getContentService();
			cs.transform(reader, writer);
		} catch (Exception e) {
			handleContentServiceException("transform", e);
		} finally {			
			logger.debug("[ContentSvcBean::transform] END");
		}		
	}
	
	public ContentReader getDecryptingReader(NodeRef nodeRef, QName propertyQName, CustomSecretKey key, 
			CryptoTransformationSpec transform) throws ContentRuntimeException {
		logger.debug("[ContentSvcBean::getDecryptingReader] BEGIN");
		ContentReader reader = null;
		
		try {
			ContentService cs = (ContentService) serviceRegistry.getContentService();
			
			if (cs instanceof CryptoContentService) {
				reader = ((CryptoContentService) cs).getDecryptingReader(
						nodeRef, propertyQName, key, transform);
			} else {
				logger.warn("[ContentSvcBean::getDecryptingReader] Encryption not supported. Returning plain reader.");
				reader = cs.getReader(nodeRef, propertyQName);
			}
		} catch (Exception e) {
			handleContentServiceException("getDecryptingReader", e);
		} finally {
			logger.debug("[ContentSvcBean::getDecryptingReader] END");
		}
		return reader;
	}
	
    public ContentWriter getEncryptingWriter(NodeRef nodeRef, QName propertyQName, boolean update, 
    		CustomSecretKey key, CryptoTransformationSpec transform) throws ContentRuntimeException {
    	logger.debug("[ContentSvcBean::getEncryptingWriter] BEGIN");
    	ContentWriter writer = null;
    	
		try {
			ContentService cs = serviceRegistry.getContentService();
			if (cs instanceof CryptoContentService) {
				writer = ((CryptoContentService) cs).getEncryptingWriter(
						nodeRef, propertyQName, update, key, transform);
			} else {
				logger.warn("[ContentSvcBean::getEncryptingWriter] Encryption not supported. Returning plain writer.");
				writer = cs.getWriter(nodeRef, propertyQName, update);
			}
		} catch (Exception e) {
			handleContentServiceException("getEncryptingWriter", e);
		} finally {
			logger.debug("[ContentSvcBean::getEncryptingWriter] END");
		}
		return writer;
    }
    
    public boolean supportsCryptography() {
    	logger.debug("[ContentSvcBean::supportsCryptography] BEGIN");

		try {
			return (serviceRegistry.getContentService() instanceof CryptoContentService);
		} finally {
			logger.debug("[ContentSvcBean::supportsCryptography] END");
		}
    }
    
	private void handleContentServiceException(String methodName, Throwable e) throws ContentRuntimeException {
		logger.warn("[ContentSvcBean::handleContentServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new ContentRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new ContentRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new ContentRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof InvalidTypeException) {
			throw new ContentRuntimeException(FoundationErrorCodes.INVALID_TYPE_ERROR);
		} else if (e instanceof EncryptionRuntimeException) {
			if (e.getCause() == null) {
				// Causa sconosciuta -> errore generico
				throw new ContentRuntimeException(FoundationErrorCodes.ENCRYPTION_GENERIC_ERROR);
			} else if (e.getCause() instanceof NoSuchAlgorithmException) {
				throw new ContentRuntimeException(FoundationErrorCodes.ENCRYPTION_INVALID_ALGORITHM_ERROR);
			} else if (e.getCause() instanceof InvalidKeyException) {
				throw new ContentRuntimeException(FoundationErrorCodes.ENCRYPTION_INVALID_KEY_ERROR);
			} else if (e.getCause() instanceof NoSuchPaddingException) {
				throw new ContentRuntimeException(FoundationErrorCodes.ENCRYPTION_INVALID_PADDING_ERROR);
			} else if (e.getCause() instanceof InvalidAlgorithmParameterException) {
				throw new ContentRuntimeException(FoundationErrorCodes.ENCRYPTION_INVALID_PARAM_ERROR);
			} else {
				throw new ContentRuntimeException(FoundationErrorCodes.ENCRYPTION_GENERIC_ERROR);
			}
		} else {
			throw new ContentRuntimeException(FoundationErrorCodes.GENERIC_CONTENT_SERVICE_ERROR);
		}
	}
}

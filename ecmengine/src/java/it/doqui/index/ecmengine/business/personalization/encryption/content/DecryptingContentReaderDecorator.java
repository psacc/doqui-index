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

import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.DecryptingContentReader;
import it.doqui.index.ecmengine.business.personalization.encryption.exception.EncryptionRuntimeException;
import it.doqui.index.ecmengine.business.personalization.encryption.util.EncryptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

public class DecryptingContentReaderDecorator implements DecryptingContentReader {

	private Log logger = LogFactory.getLog(EncryptionUtils.ENCRYPTION_LOG_CATEGORY);
	
	private ContentReader reader;
	private SecretKey secretKey;
	private CryptoTransformationSpec transformationSpec;
	
	public DecryptingContentReaderDecorator(ContentReader reader, SecretKey secretKey, CryptoTransformationSpec transformSpec) {
		logger.debug("[DecryptingContentReaderDecorator::constructor] BEGIN");
		this.reader = reader;
		this.secretKey = secretKey;
		this.transformationSpec = transformSpec;
		logger.debug("[DecryptingContentReaderDecorator::constructor] END");
	}
	
	public boolean exists() {
		logger.debug("[DecryptingContentReaderDecorator::exists] BEGIN");
		
		try {
			return reader.exists();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::exists] END");
		}
	}

	public void getContent(OutputStream os) throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getContent] BEGIN");

		try {
			FileCopyUtils.copy(getContentInputStream(), os);		
		} catch (IOException e) {
        	logger.error("[DecryptingContentReaderDecorator::getContent] I/O Error reading from: " + reader, e);
            throw new ContentIOException("Failed to copy content to output stream: \n" +
                    "   accessor: " + this, e);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getContent] END");
		}
	}

	public void getContent(File file) throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getContent] BEGIN");

		try {
			FileCopyUtils.copy(getContentInputStream(), new FileOutputStream(file));
		} catch (IOException e) {
        	logger.error("[DecryptingContentReaderDecorator::getContent] I/O Error reading from: " + reader, e);
            throw new ContentIOException("Failed to copy content to file: \n" +
                    "   accessor: " + this, e);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getContent] END");
		}
	}

	public InputStream getContentInputStream() throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getContentInputStream] BEGIN");

		IvParameterSpec iv = null;
		
		try {
			Cipher cipher = null;
			try {
				cipher = Cipher.getInstance(CryptoTransformationSpec.buildTransformationString(transformationSpec), "SunJCE");
			} catch (NoSuchProviderException e) {
				logger.warn("[DecryptingContentReaderDecorator::getContentInputStream] Unknown provider \"SunJCE\". Using default...");
				cipher = Cipher.getInstance(CryptoTransformationSpec.buildTransformationString(transformationSpec));
			}
			
			if (transformationSpec.getMode() != null && !transformationSpec.getMode().equalsIgnoreCase("ECB")) {
				iv = new IvParameterSpec(transformationSpec.getIv());
				logger.debug("[DecryptingContentReaderDecorator::getContentInputStream] IvParameterSpec: " + iv);
				cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
			}
			
			logger.debug("[DecryptingContentReaderDecorator::getContentInputStream] " +
					"Cipher initialized: DECRYPT - " + cipher.getProvider() + " - " + cipher.getAlgorithm());
			
			CipherInputStream cis = new CipherInputStream(
					reader.getContentInputStream(), cipher);
			return cis;
		} catch (NoSuchPaddingException e) {
			logger.warn("[DecryptingContentReaderDecorator::getContentInputStream] Invalid padding: " + transformationSpec.getPadding());
			throw new EncryptionRuntimeException("Invalid padding: " + transformationSpec.getPadding(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.warn("[DecryptingContentReaderDecorator::getContentInputStream] Invalid algorithm: " + transformationSpec.getAlgorithm());
			throw new EncryptionRuntimeException("Invalid algorithm: " + transformationSpec.getAlgorithm(), e);
		} catch (InvalidKeyException e) {
			logger.warn("[DecryptingContentReaderDecorator::getContentInputStream] Invalid key!");
			throw new EncryptionRuntimeException("Invalid key!", e);
		} catch (InvalidAlgorithmParameterException e) {
			logger.warn("[DecryptingContentReaderDecorator::getContentInputStream] Invalid algorithm parameter: " + iv);
			throw new EncryptionRuntimeException("Invalid algorithm parameter: " + iv, e);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getContentInputStream] END");
		}
	}

	public String getContentString() throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getContentString] BEGIN");
		
        try {
            // read from the stream into a byte[]
            InputStream is = getContentInputStream();	// Decryption happens here!

            byte [] bytes = FileCopyUtils.copyToByteArray(is);
            
            // get the encoding for the string
            String encoding = getEncoding();
            
            // create the string from the byte[] using encoding if necessary
            String content = (encoding == null) ? new String(bytes) : new String(bytes, encoding);
            
            return content;
        } catch (IOException e) {
        	logger.error("[DecryptingContentReaderDecorator::getContentString] I/O Error reading from: " + reader, e);
            throw new ContentIOException("Failed to copy content to string: \n" +
                    "   accessor: " + this, e);
        } finally {
        	logger.debug("[DecryptingContentReaderDecorator::getContentString] END");
        }
	}

	public String getContentString(int length) throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getContentString] BEGIN");
        
        Reader localReader = null;
        try {
            if (length < 0 || length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Character count must be positive and within range");
            }
        	
            // just create buffer of the required size
            char[] buffer = new char[length];
            
            String encoding = getEncoding();
            
            // create a reader from the input stream
            localReader = (encoding == null) 
            		? new InputStreamReader(getContentInputStream()) 
            		: new InputStreamReader(getContentInputStream(), getEncoding());
            
            // read it all, if possible
            int count = localReader.read(buffer, 0, length);
            
            // there may have been fewer characters - create a new string as the result
            return ((count != -1) ? new String(buffer, 0, count) : "");
        } catch (IOException e) {
        	logger.error("[DecryptingContentReaderDecorator::getContentString] I/O Error reading from: " + localReader, e);
            throw new ContentIOException("Failed to copy content to string: \n" +
                    "   accessor: " + this + "\n" +
                    "   length: " + length, e);
        } finally {
            if (localReader != null) {
                try { 
                	localReader.close(); 
                } catch (Throwable e) { 
                	logger.warn("[DecryptingContentReaderDecorator::getContentString] Error closing local reader: " + localReader, e);
                }
            }
            logger.debug("[DecryptingContentReaderDecorator::getContentString] END");
        }
	}

	public FileChannel getFileChannel() throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getFileChannel] BEGIN");

		try {
			logger.debug("[DecryptingContentReaderDecorator::getFileChannel] Returning encrypted file channel from reader: " + reader);
			return reader.getFileChannel();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getFileChannel] END");
		}
	}

	public long getLastModified() {
		logger.debug("[DecryptingContentReaderDecorator::getLastModified] BEGIN");

		try {
			return reader.getLastModified();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getLastModified] END");
		}
	}

	public ReadableByteChannel getReadableChannel() throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getReadableChannel] BEGIN");

		try {
			return Channels.newChannel(getContentInputStream());
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getReadableChannel] END");
		}
	}

	public ContentReader getReader() throws ContentIOException {
		logger.debug("[DecryptingContentReaderDecorator::getReader] BEGIN");

		try {
			return new DecryptingContentReaderDecorator(reader.getReader(), secretKey, transformationSpec);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getReader] END");
		}
	}

	public boolean isClosed() {
		logger.debug("[DecryptingContentReaderDecorator::isClosed] BEGIN");

		try {
			return reader.isClosed();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::isClosed] END");
		}
	}

	public void addListener(ContentStreamListener listener) {
		logger.debug("[DecryptingContentReaderDecorator::addListener] BEGIN");

		try {
			reader.addListener(listener);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::addListener] END");
		}
	}

	public ContentData getContentData() {
		logger.debug("[DecryptingContentReaderDecorator::getContentData] BEGIN");

		try {
			return reader.getContentData();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getContentData] END");
		}
	}

	public String getContentUrl() {
		logger.debug("[DecryptingContentReaderDecorator::getContentUrl] BEGIN");

		try {
			return reader.getContentUrl();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getContentUrl] END");
		}
	}

	public String getEncoding() {
		logger.debug("[DecryptingContentReaderDecorator::getEncoding] BEGIN");

		try {
			return reader.getEncoding();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getEncoding] END");
		}
	}

	public Locale getLocale() {
		logger.debug("[DecryptingContentReaderDecorator::getLocale] BEGIN");

		try {
			return reader.getLocale();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getLocale] END");
		}
	}

	public String getMimetype() {
		logger.debug("[DecryptingContentReaderDecorator::getMimetype] BEGIN");

		try {
			return reader.getMimetype();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getMimetype] END");
		}
	}

	public long getSize() {
		logger.debug("[DecryptingContentReaderDecorator::getSize] BEGIN");

		try {
			return reader.getSize();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::getSize] END");
		}
	}

	public boolean isChannelOpen() {
		logger.debug("[DecryptingContentReaderDecorator::isChannelOpen] BEGIN");

		try {
			return reader.isChannelOpen();
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::isChannelOpen] END");
		}
	}

	public void setEncoding(String encoding) {
		logger.debug("[DecryptingContentReaderDecorator::setEncoding] BEGIN");

		try {
			reader.setEncoding(encoding);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::setEncoding] END");
		}
	}

	public void setLocale(Locale locale) {
		logger.debug("[DecryptingContentReaderDecorator::setLocale] BEGIN");

		try {
			reader.setLocale(locale);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::setLocale] END");
		}
	}

	public void setMimetype(String mimeType) {
		logger.debug("[DecryptingContentReaderDecorator::setMimetype] BEGIN");

		try {
			reader.setMimetype(mimeType);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::setMimetype] END");
		}
	}

	public void setRetryingTransactionHelper(RetryingTransactionHelper transactionHelper) {
		logger.debug("[DecryptingContentReaderDecorator::setRetryingTransactionHelper] BEGIN");

		try {
			reader.setRetryingTransactionHelper(transactionHelper);
		} finally {
			logger.debug("[DecryptingContentReaderDecorator::setRetryingTransactionHelper] END");
		}
	}
}

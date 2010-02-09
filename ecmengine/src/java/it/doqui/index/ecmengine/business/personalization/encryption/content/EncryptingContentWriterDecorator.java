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
import it.doqui.index.ecmengine.business.personalization.encryption.EncryptingContentWriter;
import it.doqui.index.ecmengine.business.personalization.encryption.exception.EncryptionRuntimeException;
import it.doqui.index.ecmengine.business.personalization.encryption.util.EncryptionUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

public class EncryptingContentWriterDecorator implements EncryptingContentWriter {

	private Log logger = LogFactory.getLog(EncryptionUtils.ENCRYPTION_LOG_CATEGORY);
	
	private ContentWriter writer;
	private SecretKey secretKey;
	private CryptoTransformationSpec transformationSpec;
	
	public EncryptingContentWriterDecorator(ContentWriter writer, SecretKey key, CryptoTransformationSpec transformationSpec) {
		logger.debug("[EncryptingContentWriterDecorator::EncryptingContentWriter] BEGIN");
		this.writer = writer;
		this.secretKey = key;
		this.transformationSpec = transformationSpec;
		logger.debug("[EncryptingContentWriterDecorator::EncryptingContentWriter] END");
	}
	
	public OutputStream getContentOutputStream() throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::getContentOutputStream] BEGIN");
		
		IvParameterSpec iv = null;
		
		try {
			Cipher cipher = null;
			try {
				cipher = Cipher.getInstance(CryptoTransformationSpec.buildTransformationString(transformationSpec), "SunJCE");
			} catch (NoSuchProviderException e) {
				logger.warn("[EncryptingContentWriterDecorator::getContentOutputStream] Unknown provider \"SunJCE\". Using default...");
				cipher = Cipher.getInstance(CryptoTransformationSpec.buildTransformationString(transformationSpec));
			}
			
			if (transformationSpec.getMode() != null && !transformationSpec.getMode().equalsIgnoreCase("ECB")) {
				iv = new IvParameterSpec(transformationSpec.getIv());
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			}
			
			logger.debug("[EncryptingContentWriterDecorator::getContentOutputStream] " +
					"Cipher initialized: ENCRYPT - " + cipher.getProvider() + " - " + cipher.getAlgorithm());
			
			CipherOutputStream cos = new CipherOutputStream(
					writer.getContentOutputStream(), cipher);
			
			return cos;
		} catch (NoSuchPaddingException e) {
			logger.warn("[EncryptingContentWriterDecorator::getContentOutputStream] Invalid padding: " + transformationSpec.getPadding());
			throw new EncryptionRuntimeException("Invalid padding: " + transformationSpec.getPadding(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.warn("[EncryptingContentWriterDecorator::getContentOutputStream] Invalid algorithm: " + transformationSpec.getAlgorithm());
			throw new EncryptionRuntimeException("Invalid algorithm: " + transformationSpec.getAlgorithm(), e);
		} catch (InvalidKeyException e) {
			logger.warn("[EncryptingContentWriterDecorator::getContentOutputStream] Invalid key!");
			throw new EncryptionRuntimeException("Invalid key!", e);
		} catch (InvalidAlgorithmParameterException e) {
			logger.warn("[EncryptingContentWriterDecorator::getContentOutputStream] Invalid algorithm parameter: " + iv);
			throw new EncryptionRuntimeException("Invalid algorithm parameter: " + iv, e);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getContentOutputStream] END");
		}
	}

	public FileChannel getFileChannel(boolean truncate) throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::getWritableChannel] BEGIN");

		try {
			throw new UnsupportedOperationException("Cannot get random file access to encrypted content.");
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getWritableChannel] END");
		}
	}

	public ContentReader getReader() throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::getReader] BEGIN");

		try {
			return new DecryptingContentReaderDecorator(writer.getReader(),
					secretKey, transformationSpec);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getReader] END");
		}
	}

	public WritableByteChannel getWritableChannel() throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::getWritableChannel] BEGIN");

		try {
			return Channels.newChannel(getContentOutputStream());
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getWritableChannel] END");
		}
	}

	public boolean isClosed() {
		logger.debug("[EncryptingContentWriterDecorator::isClosed] BEGIN");

		try {
			return writer.isClosed();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::isClosed] END");
		}
	}

	public void putContent(ContentReader reader) throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::putContent] BEGIN");

		try {
			FileCopyUtils.copy(reader.getContentInputStream(), getContentOutputStream());
        } catch (IOException e) {
        	logger.error("[EncryptingContentWriterDecorator::putContent] I/O Error writing to: " + writer, e);
            throw new ContentIOException("Failed to copy content from content reader: \n" +
                    "   accessor: " + this, e);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::putContent] END");
		}
	}

	public void putContent(InputStream is) throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::putContent] BEGIN");

		try {
			FileCopyUtils.copy(is, getContentOutputStream());
        } catch (IOException e) {
        	logger.error("[EncryptingContentWriterDecorator::putContent] I/O Error writing to: " + writer, e);
            throw new ContentIOException("Failed to copy content from input stream: \n" +
                    "   accessor: " + this, e);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::putContent] END");
		}
	}

	public void putContent(File file) throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::putContent] BEGIN");

		try {
			FileCopyUtils.copy(new FileInputStream(file), getContentOutputStream());
        } catch (IOException e) {
        	logger.error("[EncryptingContentWriterDecorator::putContent] I/O Error writing to: " + writer, e);
            throw new ContentIOException("Failed to copy content from file: \n" +
                    "   accessor: " + this, e);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::putContent] END");
		}
	}

	public void putContent(String content) throws ContentIOException {
		logger.debug("[EncryptingContentWriterDecorator::putContent] BEGIN");

		try {
			FileCopyUtils.copy(new ByteArrayInputStream(content.getBytes()), getContentOutputStream());
        } catch (IOException e) {
        	logger.error("[EncryptingContentWriterDecorator::putContent] I/O Error writing to: " + writer, e);
            throw new ContentIOException("Failed to copy content from string: \n" +
                    "   accessor: " + this, e);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::putContent] END");
		}
	}

	public void addListener(ContentStreamListener listener) {
		logger.debug("[EncryptingContentWriterDecorator::addListener] BEGIN");

		try {
			this.writer.addListener(listener);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::addListener] END");
		}
	}

	public ContentData getContentData() {
		logger.debug("[EncryptingContentWriterDecorator::getContentData] BEGIN");

		try {
			return this.writer.getContentData();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getContentData] END");
		}
	}

	public String getContentUrl() {
		logger.debug("[EncryptingContentWriterDecorator::getContentUrl] BEGIN");

		try {
			return this.writer.getContentUrl();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getContentUrl] END");
		}
	}

	public String getEncoding() {
		logger.debug("[EncryptingContentWriterDecorator::getEncoding] BEGIN");

		try {
			return this.writer.getEncoding();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getEncoding] END");
		}
	}

	public Locale getLocale() {
		logger.debug("[EncryptingContentWriterDecorator::getLocale] BEGIN");

		try {
			return this.writer.getLocale();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getLocale] END");
		}
	}

	public String getMimetype() {
		logger.debug("[EncryptingContentWriterDecorator::getMimetype] BEGIN");

		try {
			return this.writer.getMimetype();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getMimetype] END");
		}
	}

	public long getSize() {
		logger.debug("[EncryptingContentWriterDecorator::getSize] BEGIN");

		try {
			return this.writer.getSize();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::getSize] END");
		}
	}

	public boolean isChannelOpen() {
		logger.debug("[EncryptingContentWriterDecorator::isChannelOpen] BEGIN");

		try {
			return this.writer.isChannelOpen();
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::isChannelOpen] END");
		}
	}

	public void setEncoding(String encoding) {
		logger.debug("[EncryptingContentWriterDecorator::setEncoding] BEGIN");

		try {
			this.writer.setEncoding(encoding);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::setEncoding] END");
		}
	}

	public void setLocale(Locale locale) {
		logger.debug("[EncryptingContentWriterDecorator::setLocale] BEGIN");

		try {
			this.writer.setLocale(locale);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::setLocale] END");
		}
	}

	public void setMimetype(String mimeType) {
		logger.debug("[EncryptingContentWriterDecorator::setMimetype] BEGIN");

		try {
			this.writer.setMimetype(mimeType);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::setMimetype] END");
		}
	}

	public void setRetryingTransactionHelper(RetryingTransactionHelper transactionHelper) {
		logger.debug("[EncryptingContentWriterDecorator::setRetryingTransactionHelper] BEGIN");

		try {
			this.writer.setRetryingTransactionHelper(transactionHelper);
		} finally {
			logger.debug("[EncryptingContentWriterDecorator::setRetryingTransactionHelper] END");
		}
	}
}

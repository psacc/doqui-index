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

package it.doqui.index.ecmengine.business.personalization.encryption;

import it.doqui.index.ecmengine.business.personalization.encryption.exception.EncryptionRuntimeException;
import it.doqui.index.ecmengine.business.personalization.encryption.util.EncryptionUtils;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CryptoTransformationSpec implements Serializable {

	private static final long serialVersionUID = 3262144019751183302L;

	public static CryptoTransformationSpec NULL_TRANSFORMATION = new CryptoTransformationSpec("Null", "ECB", "None");

	private static Log logger = LogFactory.getLog(EncryptionUtils.ENCRYPTION_LOG_CATEGORY);

	private String algorithm;
	private String mode;
	private String padding;

	private byte [] iv;

	public CryptoTransformationSpec() {

	}

	public CryptoTransformationSpec(String algorithm, String mode, String padding) {
		this.algorithm = algorithm;
		this.mode = mode;
		this.padding = padding;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getPadding() {
		return padding;
	}

	public void setPadding(String padding) {
		this.padding = padding;
	}

	public static String buildTransformationString(CryptoTransformationSpec spec) {
		logger.debug("[CryptoTransformationSpec::buildTransformationString] BEGIN");

		try {
			StringBuilder t = new StringBuilder();
			t.append(spec.algorithm);
			if (spec.mode != null) {
				t.append('/').append(spec.mode);

				if (spec.padding != null) {
					t.append('/').append(spec.padding);
				}
			}

     		if (logger.isDebugEnabled()) {
    			logger.debug("[CryptoTransformationSpec::buildTransformationString] Transformation string: " + t.toString());
	    	}
			return t.toString();
		} finally {
			logger.debug("[CryptoTransformationSpec::buildTransformationString] END");
		}
	}

	public static CryptoTransformationSpec buildTransformationSpec(String specString) {
		logger.debug("[CryptoTransformationSpec::buildTransformationSpec] BEGIN");

		try {
			if (specString == null) {
				logger.debug("[CryptoTransformationSpec::buildTransformationSpec] Null transformation string.");
				return null;
			}

			String [] parts = specString.split("/");
			CryptoTransformationSpec spec = new CryptoTransformationSpec();

			if (parts.length == 1) {
				spec.algorithm = parts[0];
         		if (logger.isDebugEnabled()) {
	    			logger.debug("[CryptoTransformationSpec::buildTransformationSpec] Algorithm: " + spec.algorithm);
        		}

				return spec;
			} else if (parts.length == 3) {
				spec.setAlgorithm(parts[0]);
				spec.setMode(parts[1]);
				spec.setPadding(parts[2]);
         		if (logger.isDebugEnabled()) {
    				logger.debug("[CryptoTransformationSpec::buildTransformationSpec] " +
						"Algorithm: " + spec.algorithm + " - Mode: " + spec.mode + " - Padding: " + spec.padding);
        		}

				return spec;
			}

       		if (logger.isDebugEnabled()) {
    			logger.debug("[CryptoTransformationSpec::buildTransformationSpec] Invalid transformation string: " + specString);
       		}
			throw new IllegalArgumentException("Invalid spec string: " + specString);
		} finally {
			logger.debug("[CryptoTransformationSpec::buildTransformationSpec] END");
		}
	}

	public byte[] getIv() {
		return iv;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

	public static byte [] generateIv(CryptoTransformationSpec spec, SecretKey key) {
		logger.debug("[CryptoTransformationSpec::generateIv] BEGIN");
		try {
			Cipher cipher = null;
			byte [] iv = null;

			try {
				cipher = Cipher.getInstance(CryptoTransformationSpec.buildTransformationString(spec), "SunJCE");
			} catch (NoSuchProviderException e) {
				logger.warn("[CryptoTransformationSpec::generateIv] Unknown provider \"SunJCE\". Using default...");
				cipher = Cipher.getInstance(CryptoTransformationSpec.buildTransformationString(spec));
			}

			cipher.init(Cipher.ENCRYPT_MODE, key);

			iv = cipher.getIV();

			return iv;
		} catch (NoSuchPaddingException e) {
			logger.warn("[CryptoTransformationSpec::generateIv] Invalid padding: " + spec.getPadding());
			throw new EncryptionRuntimeException("Invalid padding: " + spec.getPadding(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.warn("[CryptoTransformationSpec::generateIv] Invalid algorithm: " + spec.getAlgorithm());
			throw new EncryptionRuntimeException("Invalid algorithm: " + spec.getAlgorithm(), e);
		} catch (InvalidKeyException e) {
			logger.warn("[CryptoTransformationSpec::generateIv] Invalid key!");
			throw new EncryptionRuntimeException("Invalid key!", e);
		} finally {
			logger.debug("[CryptoTransformationSpec::generateIv] END");
		}
	}
}

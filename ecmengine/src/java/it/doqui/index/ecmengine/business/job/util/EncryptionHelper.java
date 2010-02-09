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
 
package it.doqui.index.ecmengine.business.job.util;

import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Classe di utilit&agrave; per le operazioni di encrypting/decrypting basati su
 * algoritmo password-based con MD5 e DES.
 * 
 * @author DoQui
 * 
 */
public abstract class EncryptionHelper {

	private static final String ENCRYPTION = "PBEWithMD5AndDES";
	private static final String PASSPHRASE = "!ecm/555/dOqUI?";
	private static final String SALT = "01234567";
	private static final int COUNT = 20;

	public static String encrypt(String val) throws Exception {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION);
		PBEKeySpec keySpec = new PBEKeySpec(PASSPHRASE.toCharArray());
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(SALT.getBytes(), COUNT);
		Cipher cipher = Cipher.getInstance(ENCRYPTION);
		cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		byte[] ciphertext = cipher.doFinal(val.getBytes());
		return toHexString(ciphertext);
	}

	public static String decrypt(String val) throws Exception {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION);
		PBEKeySpec keySpec = new PBEKeySpec(PASSPHRASE.toCharArray());
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(SALT.getBytes(), COUNT);
		Cipher cipher = Cipher.getInstance(ENCRYPTION);
		cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		byte[] decryptedtext = cipher.doFinal(toByteArray(val));
		return new String(decryptedtext);
	}

	private static String toHexString(byte[] in) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0) {
			return null;
		}

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };

		StringBuffer out = new StringBuffer(in.length * 2);
		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(pseudo[(int) ch]);
			ch = (byte) (in[i] & 0x0F);
			out.append(pseudo[(int) ch]);
			i++;
		}

		String rslt = new String(out);
		return rslt;
	}

	private static byte[] toByteArray(String hexstring) throws Exception {
		byte[] result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		char[] charArray = hexstring.toCharArray();

		for (int i=0; i<charArray.length; i+=2) {
			baos.write(new byte[]{ Integer.decode("0x"+charArray[i]+charArray[i+1]).byteValue() });
		}

		result = baos.toByteArray();
		baos.close();
		return result;
	}

}

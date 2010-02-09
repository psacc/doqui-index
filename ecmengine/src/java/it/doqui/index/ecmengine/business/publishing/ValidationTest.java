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
 
package it.doqui.index.ecmengine.business.publishing;

import org.alfresco.util.Base64;

import it.doqui.index.ecmengine.business.publishing.EcmEnginePublisherBean.ValidationType;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import junit.framework.TestCase;

public class ValidationTest extends TestCase {

	private EncryptionInfo testEncInfo;
	private Content testContent;
	
	private void printEncryptionInfo(EncryptionInfo info) {
		System.out.println("EncryptionInfo:\n");
		System.out.println("\tKey                         : " + info.getKey());
		System.out.println("\tKeyID                       : " + info.getKeyId());
		System.out.println("\tAlgoritmo                   : " + info.getAlgorithm());
		System.out.println("\tMode                        : " + info.getMode());
		System.out.println("\tPadding                     : " + info.getPadding());
		System.out.println("\tEncrypted from source       : " + info.isSourceEncrypted());
		System.out.println("\tSource Initialization Vector: " + info.getSourceIV());
		System.out.println("\tCorrotto                    : " + info.isCorruptedEncryptionInfo());
		System.out.println("\n\n");
	}
	
	public void setUp() {
		
		testEncInfo = new EncryptionInfo();
		testContent = new Content();
	}
	
	public void tearDown() {
		
		testEncInfo = null;
		testContent = null;
	}
	
	public void testEncryptionInfoEncryptValidECB() {
		
		testEncInfo.setKey("1234567890123456");
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setMode("ECB");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceEncrypted(false);
		
		System.out.println("TEST: DTO valido in crittazione con mode ECB");
		printEncryptionInfo(testEncInfo);
		
		assertTrue(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptValidCBC() {
		
		testEncInfo.setKey("1234567890123456");
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setMode("CBC");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceEncrypted(false);
		
		System.out.println("TEST: DTO valido in crittazione con mode CBC");
		printEncryptionInfo(testEncInfo);
		
		assertTrue(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptValidNoMode() {
		
		testEncInfo.setKey("1234567890123456");
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setSourceEncrypted(false);
		
		System.out.println("TEST: DTO valido in crittazione senza mode e padding");
		printEncryptionInfo(testEncInfo);
		
		assertTrue(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptedFromSourceValidECB() {
		
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setMode("ECB");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceEncrypted(true);
		
		System.out.println("TEST: DTO valido in crittazione con mode ECB e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertTrue(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptedFromSourceValidCBC() {
		
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setMode("CBC");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceIV(Base64.encodeBytes("1234567890123456".getBytes()));
		testEncInfo.setSourceEncrypted(true);
		
		System.out.println("TEST: DTO valido in crittazione con mode CBC e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertTrue(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptedFromSourceValidNoMode() {
		
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setSourceEncrypted(true);
		
		System.out.println("TEST: DTO valido in crittazione senza mode e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertTrue(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptedFromSourceInvalidNoMode() {
		
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceEncrypted(true);
		
		System.out.println("TEST: DTO non valido in crittazione senza mode e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertFalse(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptInvalidModeNoPadding() {
		
		testEncInfo.setKey("1234567890123456");
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setMode("ECB");
		testEncInfo.setSourceEncrypted(false);
		
		System.out.println("TEST: DTO non valido in crittazione con mode e senza padding");
		printEncryptionInfo(testEncInfo);
		
		assertFalse(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptInvalidPaddingNoMode() {
		
		testEncInfo.setKey("1234567890123456");
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceEncrypted(false);
		
		System.out.println("TEST: DTO non valido in crittazione con padding e senza mode");
		printEncryptionInfo(testEncInfo);
		
		assertFalse(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptedFromSourceInvalidModeNoPadding() {
		
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setMode("ECB");
		testEncInfo.setSourceIV(Base64.encodeBytes("1234567890123456".getBytes()));
		testEncInfo.setSourceEncrypted(true);
		
		System.out.println("TEST: DTO non valido in crittazione con mode e senza padding e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertFalse(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptedFromSourceInvalidPaddingNoMode() {
		
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceIV(Base64.encodeBytes("1234567890123456".getBytes()));
		testEncInfo.setSourceEncrypted(true);
		
		System.out.println("TEST: DTO non valido in crittazione con padding e senza mode e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertFalse(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testEncryptionInfoEncryptInvalidNotBase64() {
		
		testEncInfo.setKey("1234567890123456");
		testEncInfo.setKeyId("Test Key");
		testEncInfo.setAlgorithm("AES");
		testEncInfo.setPadding("PKCS5Padding");
		testEncInfo.setSourceEncrypted(false);
		
		System.out.println("TEST: DTO non valido in crittazione con IV non valido e file gia` criptato");
		printEncryptionInfo(testEncInfo);
		
		assertFalse(EcmEnginePublisherBean.isValidEncryptionInfo(testEncInfo, false));
	}
	
	public void testContentForReadValid() {
		testContent.setContentPropertyPrefixedName("cm:content");
		
		assertTrue(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_READ));
	}
	
	public void testContentForReadInvalidNotPrefixedName() {
		testContent.setContentPropertyPrefixedName("invalid");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_READ));
	}
	
	public void testContentForReadInvalidNullPrefixedName() {
		testContent.setContentPropertyPrefixedName(null);
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_READ));
	}

	public void testContentForWriteMetadataValid() {
		testContent.setAspects(new Aspect[] { new Aspect() });
		
		assertTrue(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_METADATA));
	}

	public void testContentForWriteMetadataInvalid() {
		testContent.setAspects(null);

		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_METADATA));
	}
	
//	public void testContentForWriteMetadataInvalidWithContentProperty() {
//		testContent.setAspects(new Aspect[] { new Aspect() });
//		testContent.setContentPropertyPrefixedName("cm:content");
//		
//		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_METADATA));
//	}
//	
//	public void testContentForWriteMetadataInvalidWithMimeType() {
//		testContent.setAspects(new Aspect[] { new Aspect() });
//		testContent.setMimeType("application/binary");
//		
//		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_METADATA));
//	}
//	
//	public void testContentForWriteMetadataInvalidWithEncoding() {
//		testContent.setAspects(new Aspect[] { new Aspect() });
//		testContent.setEncoding("UTF-8");
//		
//		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_METADATA));
//	}
//	
//	public void testContentForWriteMetadataInvalidWithContent() {
//		testContent.setAspects(new Aspect[] { new Aspect() });
//		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
//		
//		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_METADATA));
//	}
	
	public void testContentForWriteContentValid() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		
		assertTrue(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_CONTENT));
	}
	
	public void testContentForWriteContentInvalidNoContentProperty() {
		testContent.setContentPropertyPrefixedName(null);
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_CONTENT));
	}
	
	public void testContentForWriteContentInvalidNoMimeType() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType(null);
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_CONTENT));
	}
	
	public void testContentForWriteContentInvalidNoEncoding() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding(null);
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_CONTENT));
	}
	
	public void testContentForWriteContentInvalidNoContent() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(null);
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_CONTENT));
	}

	public void testContentForWriteNewValidWithContent() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertTrue(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewValidNoContent() {
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertTrue(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoContentProperty() {
		testContent.setContentPropertyPrefixedName(null);
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoMimeType() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType(null);
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoEncoding() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding(null);
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoContent() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(null);
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoType() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName(null);
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoPrefixedName() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName(null);
		testContent.setParentAssocTypePrefixedName("cm:contains");
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
	
	public void testContentForWriteNewInvalidNoParentAssocType() {
		testContent.setContentPropertyPrefixedName("cm:content");
		testContent.setMimeType("application/binary");
		testContent.setEncoding("UTF-8");
		testContent.setContent(new byte[] { 't', 'e', 's', 't'});
		testContent.setTypePrefixedName("cm:content");
		testContent.setPrefixedName("cm:testContent");
		testContent.setParentAssocTypePrefixedName(null);
		
		assertFalse(EcmEnginePublisherBean.isValidContent(testContent, ValidationType.CONTENT_WRITE_NEW));
	}
}

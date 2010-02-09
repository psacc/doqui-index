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

package it.doqui.index.ecmengine.test;

import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.client.engine.EcmEngineDelegate;
import it.doqui.index.ecmengine.client.engine.EcmEngineDelegateFactory;
import it.doqui.index.ecmengine.client.engine.exception.EcmEngineDelegateInstantiationException;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Association;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.dto.engine.security.Document;
import it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent;
import it.doqui.index.ecmengine.dto.engine.security.VerifyReport;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.test.util.EcmEngineTestConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestSecurity extends TestCase implements EcmEngineTestConstants {
	private static String TEST_FILE_FIRMA;

	private static String LAST_TEMP_UID="";
	private static String LAST_TEMP_UID_2="";

	private static String TEMP_CONTENT_NAME="temp_"+System.currentTimeMillis()+".pdf";
	private static String TEMP_MIMETYPE="application/pdf";

	// Creato nel Costruttore
	private EcmEngineDelegate ecmEngineDelegateImpl = null;

	// Messo a TRUE al primo giro di setup
	private static boolean flag=false;

	// Caricati da SETUP leggendo dal file di configurazione
	private static Properties properties = null;
	private static String folderParent;
	private static String folder;
	private static String contenuto;
	private static OperationContext defaultContext;
	// Repository e sleep time
	private static String REPOSITORY = "primary";
	private static int SLEEP_TIME    = 5000;

	private static String TEST_TARGET;

	// Creato con i dati letti in configurazione
	private static String uidFolderParent = null;
	private static String uidFolder       = null;
	private static String uidDocument     = null;

	// Altre informazioni sparse
	private static boolean down=false;

	private static Node[] testNodes=null;

	protected transient Log log;
	protected transient StopWatch stopwatch;

	public TestSecurity(String name) {
		super(name);
		this.log = LogFactory.getLog(ECMENGINE_TEST_LOG_CATEGORY);
		log.debug("[TestSecurity::Constructor] BEGIN - "+name);
		try {
			ecmEngineDelegateImpl = EcmEngineDelegateFactory.getEcmEngineDelegate();
			log.debug("[TestSecurity::setUp] Delegate instantiate");
		} catch(EcmEngineDelegateInstantiationException e) {
			log.error("[TestSecurity::setUp] Instantiation problem "+e);
		}
		log.debug("[TestSecurity::Constructor] END");
	}

	@SuppressWarnings("static-access")
	private String insertDocument(String parent, String filePath, String mimetype,String enc, EncryptionInfo encryptionInfo, OperationContext ctx)
	{
		log.debug("[TestSecurity::insertDocument] BEGIN");
		Content content = new Content();
		File file = new File(filePath);
		FileInputStream fis = null;
		String contentName = file.getName();
		byte [] buf = null;
		log.debug("[TestSecurity::insertDocument] ==============================");
		log.debug("[TestSecurity::insertDocument] INSERIMENTO CONTENUTO GENERICO");
		log.debug("[TestSecurity::insertDocument] Nome file: " + filePath);
		log.debug("[TestSecurity::insertDocument] Nome contenuto: " + contentName);
		log.debug("[TestSecurity::insertDocument] Uid padre: " + parent);
		log.debug("[TestSecurity::insertDocument] MIME-TYPE: " + mimetype);
		log.debug("[TestSecurity::insertDocument] Encoding: " + enc);
		log.debug("[TestSecurity::insertDocument] User: " + ctx.getUsername());
		log.debug("[TestSecurity::insertDocument] ===============================");
		try {
			fis = new FileInputStream(file);
			buf = new byte[(int)file.length()];
			fis.read(buf);
		} catch (Exception e) {
			log.error("[TestSecurity::insertDocument] Errore", e);
		} finally {
			if (fis == null) {
				log.debug("[TestSecurity::insertDocument] INSERIMENTO FALLITO");
				log.debug("[TestSecurity::insertDocument] Nome file: " + filePath);
				log.debug("[TestSecurity::insertDocument] Causa: errore nell'accesso al file.");
			}
		}
		Node parentNode = createNodeDTO(parent);
		content.setPrefixedName("cm:" + contentName);
		content.setParentAssocTypePrefixedName("cm:contains");
		content.setModelPrefixedName("cm:contentmodel");
		content.setTypePrefixedName("cm:content");
		content.setContentPropertyPrefixedName("cm:content");
		content.setMimeType(mimetype);
		content.setEncoding(enc);
		Property [] props = new Property[1];
		props[0] = createPropertyDTO("cm:name", "text", false);
		props[0].setValues(new String [] { contentName });
		Property [] authorProps = new Property[1];
		authorProps[0] = createPropertyDTO("cm:author", "text", false);
		authorProps[0].setValues(new String [] { ctx.getUsername() + " da client TestJUNIT" });
		Aspect author = new Aspect();
		author.setPrefixedName("cm:author");
		author.setModelPrefixedName("cm:contentmodel");
		author.setProperties(authorProps);
		Property [] titledProps = new Property[2];
		titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
		titledProps[0].setValues(new String [] { contentName });
		titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
		titledProps[1].setValues(new String [] { "Contenuto aggiunto da client TestJUNIT." });
		Aspect titled = new Aspect();
		titled.setPrefixedName("cm:titled");
		titled.setModelPrefixedName("cm:contentmodel");
		titled.setProperties(titledProps);
		content.setProperties(props);
		content.setAspects(new Aspect [] { author, titled });
		content.setContent(buf);
		content.setEncryptionInfo(encryptionInfo);
		Node result=null;
		String uid=null;
		try {
			//Thread.currentThread().sleep(2000);
			Thread.sleep(SLEEP_TIME);
			result = ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
			uid=result.getUid();
			log.debug("[TestSecurity::insertDocument] INSERIMENTO COMPLETATO");
			log.debug("[TestSecurity::insertDocument] Nome file: " + filePath);
			log.debug("[TestSecurity::insertDocument] Uid padre: " + parent);
			log.debug("[TestSecurity::insertDocument] Uid nodo: " + uid);
			String metadati=getPropertyValue("cm:name", content.getProperties());
			AuditInfo auditTrail = createAuditInfoDTO("TestJUNIT","createContent",uid,metadati);
			ecmEngineDelegateImpl.logTrail(auditTrail, ctx);
			log.debug("[TestSecurity::insertDocument] Audit Trail inserito.");
		} catch (InsertException e) {
			log.error("[TestSecurity::insertDocument] Caricamento fallito", e);
		}  catch (Exception e) {
			log.error("[TestSecurity::insertDocument] Errore", e);
			if(e instanceof AuditTrailException){
				log.error("[TestSecurity::insertDocument] Si e` verificato un errore " +
				"nell'inserimento dell'audit Trail.");
			}
			if (e instanceof NoSuchNodeException){
				try {
					//Thread.currentThread().sleep(2000);
					Thread.sleep(SLEEP_TIME);
					result =  ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
				} catch (Exception e1) {
					log.error("[TestSecurity::insertDocument] Errore", e);
				}
				uid=result.getUid();
			}
		}finally {
			log.debug("[TestSecurity::insertDocument] END");
		}
		return uid;
	}

	private String createFolder(String parent, String name, OperationContext ctx)
	{
		log.debug("[TestSecurity::createFolder] BEGIN");
		Node parentNode = createNodeDTO(parent);
		Content content = new Content();
		content.setPrefixedName("cm:" + name);
		content.setParentAssocTypePrefixedName("cm:contains");
		content.setModelPrefixedName("cm:contentmodel");
		content.setTypePrefixedName("cm:folder");
		Property [] props = new Property[1];
		props[0] = createPropertyDTO("cm:name", "text", false);
		props[0].setValues(new String [] { name });
		content.setProperties(props);
		Node result=null;
		String uid=null;
		try {
			result = ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
			uid = result.getUid();
			log.debug("[TestSecurity::createFolder] CREAZIONE COMPLETATA");
			log.debug("[TestSecurity::createFolder] Nome folder: " + name);
			log.debug("[TestSecurity::createFolder] Uid padre: " + parent);
			log.debug("[TestSecurity::createFolder] Uid nodo: " + uid);
		} catch (Exception e) {
			log.error("[TestSecurity::createFolder] Errore: ", e);
		}finally{
			log.debug("[TestSecurity::createFolder] END");
		}
		return uid;
	}

	private void linkContent(String parent, String child, String type,boolean childAssociation, String name){
		log.debug("[TestSecurity::linkContent] BEGIN");
		Node sourceNode = createNodeDTO(parent);
		Node destinationNode = createNodeDTO(child);
		Association association = new Association();
		association.setChildAssociation(childAssociation);
		if (association.isChildAssociation()) {
			association.setPrefixedName(name);
		}
		association.setTypePrefixedName(type);
		try {
			ecmEngineDelegateImpl.linkContent(sourceNode, destinationNode, association, defaultContext);
			log.debug("[TestSecurity::linkContent] CREAZIONE LINK COMPLETATA");
		} catch (Exception e) {
			log.error("[TestSecurity::linkContent] Errore: " + e);
		} finally {
			log.debug("[TestSecurity::linkContent] END");
		}
	}

	private String getUidCompanyHome(){
		log.debug("[TestSecurity::getUidCompanyHome] BEGIN");
		SearchParams xpath = new SearchParams();
		//query xpath per la ricerca del folder CompanyHome
		String xquery = "/app:company_home";
		xpath.setXPathQuery(xquery);
		xpath.setLimit(1);
		xpath.setPageSize(0);
		xpath.setPageIndex(0);
		SearchResponse response = null;
		ResultContent[] results=null;
		String uid="";
		try {
			response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);
			results = response.getResultContentArray();
			int sizeLista = results == null ? 0 : results.length;
			log.debug("[TestSecurity::getUidCompanyHome] estratto "+sizeLista+" record.");
			if (sizeLista>0 && results[0] != null) {
				uid=results[0].getUid();
				log.debug("[TestSecurity::getUidCompanyHome] Uid CompanyHome : "+results[0].getUid());
			}
			else log.debug("[TestSecurity::getUidCompanyHome] Uid CompanyHome Non Trovato.");
		} catch (Exception e) {
			log.error("[TestSecurity::getUidCompanyHome] Errore: " + e);
		} finally {
			log.debug("[TestSecurity::getUidCompanyHome] END");
		}
		return uid;
	}

	protected void setUp() throws Exception {
		super.setUp();
		log.debug("[TestSecurity::Setup] BEGIN");
		try {
			if (flag == false){
				log.debug("[TestSecurity::setUp] Carico file di properties.");
				InputStream is = this.getClass().getResourceAsStream("/" + ECMENGINE_TEST_PROPERTIES_FILE);
				properties = new Properties();
				if (is != null) {
					properties.load(is);
					log.debug("[TestSecurity::setUp] File di properties caricato : "+ECMENGINE_TEST_PROPERTIES_FILE);
					// Setup dei folder e del contenuto
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
					String dateStr = sdf.format(new Date());
					folderParent = properties.getProperty(ECMENGINE_TEST_FOLDER_PARENT)+"_"+dateStr;
					folder       = properties.getProperty(ECMENGINE_TEST_FOLDER)+"_security_"+dateStr;
					contenuto    = properties.getProperty(ECMENGINE_TEST_CONTENT);
					TEST_FILE_FIRMA    = properties.getProperty(ECMENGINE_TEST_SECURITY_CONTENT);
					log.debug("[TestSecurity::setUp] folderParent vale: " + folderParent);
					log.debug("[TestSecurity::setUp] folder vale      : " + folder);
					log.debug("[TestSecurity::setUp] contenuto vale   : " + contenuto);
					// Setup del default context
					String repository = properties.getProperty(ECMENGINE_TEST_REPOSITORY);
					if (repository != null && repository.length() > 0) {
						REPOSITORY = repository;
					}
					String user     = properties.getProperty(ECMENGINE_TEST_USERNAME);
					String pass     = properties.getProperty(ECMENGINE_TEST_PASSWORD);
					String tenant   = properties.getProperty(ECMENGINE_TEST_TENANT);
					if (tenant != null && tenant.length() > 0) {
						if (!tenant.startsWith("@")) {
							tenant = "@" + tenant;
						}
					} else {
						tenant = "";
					}
					if (user == null || user.length() == 0 || pass == null || pass.length() == 0) {
						user = "admin";
						pass = "admin";
					}
					defaultContext = new OperationContext();
					defaultContext.setFruitore("TestJUNIT");
					defaultContext.setNomeFisico("Client TestJUNIT");
					defaultContext.setUsername(user+tenant);
					defaultContext.setPassword(pass);
					defaultContext.setRepository(REPOSITORY);
					try {
						SLEEP_TIME = Integer.parseInt(properties.getProperty(ECMENGINE_TEST_SLEEP_TIME));
					} catch(NumberFormatException nfe) {}

					TEST_TARGET=properties.getProperty(ECMENGINE_TEST_TARGET);
				}
				else{
					log.error("[TestSecurity::setUp] Si sono verificati problemi nella lettura del file di properties: "+ECMENGINE_TEST_PROPERTIES_FILE);
				}
				String uidCompanyHome = getUidCompanyHome();
				//creazione Folder sotto la CompanyHome
				uidFolder = createFolder(uidCompanyHome, folder, defaultContext);
				log.debug("[TestSecurity::setUp] Creato folder " +uidFolder);
				Thread.sleep(SLEEP_TIME);
				//caricamento del documento generic_content.pdf all'interno del folder appena creato
				// logTrail
				// nel metodo insertDocument e` presenta anche la chiamata a logTrail con utente TestJUNIT
				uidDocument = insertDocument(uidFolder, contenuto, "application/pdf", "UTF-8", null, defaultContext);
				log.debug("[TestSecurity::setUp] Creato document " +uidDocument);
				Thread.sleep(SLEEP_TIME);
				// Aggiunta di contenuto testuale, server per la fulltext search
				insertDocument(uidFolder, ECMENGINE_TEST_PROPERTIES_FILE, "text/plain", "UTF-8", null, defaultContext);
				log.debug("[TestSecurity::setUp] Creato contenuto testuale");
				Thread.sleep(SLEEP_TIME);
				//creazione di un altro Folder sotto la CompanyHome
				uidFolderParent = createFolder(uidCompanyHome, folderParent, defaultContext);
				log.debug("[TestSecurity::setUp] Creato folderParent " +uidFolderParent);
				Thread.sleep(SLEEP_TIME);
				//creazione associazione child di nome linkTestJUNIT tra il folder appena creato e il documento generic_content.pdf
				linkContent(uidFolderParent, uidDocument, "cm:contains", true, "cm:linkTestJUNIT");
				Thread.sleep(SLEEP_TIME);
				flag = true;
			}
		}catch (Exception e) {
			log.error("[TestSecurity::setUp] Errore: " + e);
		}
		log.debug("[TestSecurity::Setup] END");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		/*
		if(down)
		{
			SearchParams xpath = new SearchParams();
			xpath.setXPathQuery("/app:company_home/*");
			xpath.setRepository(REPOSITORY);
			xpath.setLimit(0);

			SearchResponse response = null;

			try{

				response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);

				ResultContent[] risultati=null;
				Node nodo = null;

				if(response.getTotalResults()>0){
					risultati = response.getResultContentArray();

					for (int i = 0; risultati!=null && i < risultati.length; i++) {

						String uid = risultati[i].getUid();

						nodo = createNode(uid,REPOSITORY);

						ecmEngineDelegateImpl.deleteContent(nodo, defaultContext);
						log.debug("[TestSecurity::tearDown] Eliminato nodo con uid: "+uid);
					}
				}

			}catch (Exception e) {
				log.error("[TestSecurity::tearDown] Errore: " + e);
			}
		}
		 */
		ecmEngineDelegateImpl = null;
	}

	private Node createNodeDTO(String uid) {
		Node node = new Node();
		node.setUid(uid);
		return node;
	}

	private Property createPropertyDTO(String prefixedName, String dataType, boolean multivalue) {
		Property prop = new Property();
		prop.setPrefixedName(prefixedName);
		prop.setDataType(dataType);
		prop.setMultivalue(multivalue);
		return prop;
	}

	private AuditInfo createAuditInfoDTO(String utente, String operazione, String idOggetto, String metaDati) {
		AuditInfo auditInfo = new AuditInfo();
		auditInfo.setUtente(utente);
		auditInfo.setOperazione(operazione);
		auditInfo.setIdOggetto(idOggetto);
		auditInfo.setMetaDati(metaDati);
		return auditInfo;
	}

	private Property getProperty(String prefixedName, Property[] properties) {
		for (int i = 0; properties != null && i < properties.length; i++) {
			if (properties[i].getPrefixedName().equals(prefixedName)) {
				return properties[i];
			}
		}
		return null;
	}

	private final String getPropertyValue(String prefixedName, Property[] properties) {
		Property prop = getProperty(prefixedName, properties);

		return (prop != null) ? prop.getValues()[0] : null;
	}

	/**
	 * Azzera e avvia la misurazione dei tempi da parte dello stopwatch.
	 */
	protected void start() {
		this.stopwatch = new StopWatch(ECMENGINE_TEST_STOPWATCH_LOG_CATEGORY);
		this.stopwatch.start();
	}

	/**
	 * Arresta la misurazione dei tempi da parte dello stopwatch.
	 */
	protected void stop() {
		this.stopwatch.stop();
	}

	/**
	 * Registra sul logger dello stowpatch il tempo misurato al momento della chiamata.
	 *
	 * @param className Il nome della classe chiamante.
	 * @param methodName Il nome del metodo chiamante.
	 * @param ctx Il contesto in cui il metodo &egrave; stato chiamato.
	 * @param message Un messaggio da registrare nel log assieme al tempo.
	 */
	protected void dumpElapsed(String className, String methodName, String ctx, String message) {
		this.stopwatch.dumpElapsed(className, methodName, ctx, message);
	}

	public void testVerifyDocumentData(){
		try{
			log.debug("[TestSecurity::testVerifyDocumentData] BEGIN");
			EnvelopedContent envelopedContent=new EnvelopedContent();
			File file=new File(TEST_FILE_FIRMA);
			FileInputStream fis=new FileInputStream(file);
			byte[] buf=new byte[(int)file.length()];
			fis.read(buf);
			envelopedContent.setData(buf);
			envelopedContent.setStore(true);
			fis.close();
			VerifyReport report=ecmEngineDelegateImpl.verifyDocument(envelopedContent, defaultContext);
			log.debug("[TestSecurity::testVerifyDocumentData] report: " + report.getUid() + " " + report.getErrorCode());
			LAST_TEMP_UID=report.getUid();
			Thread.sleep(SLEEP_TIME);
			assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			log.error("[TestSecurity::testVerifyDocumentData] ERROR: " + e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestSecurity::testVerifyDocumentData] END");
		}
	}

	public void testVerifyDocumentNode(){
		try{
			log.debug("[TestSecurity::testVerifyDocumentNode] BEGIN");
			if(LAST_TEMP_UID.equals("")){
				throw new Exception("Non esiste nessun nodo temporaneo.");
			}
			Node node=new Node();
			node.setUid(LAST_TEMP_UID);
			VerifyReport report=ecmEngineDelegateImpl.verifyDocument(node, defaultContext);
			log.debug("[TestSecurity::testVerifyDocumentNode] report: " + report.getErrorCode());
			assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			log.error("[TestSecurity::testVerifyDocumentNode] ERROR: " + e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestSecurity::testVerifyDocumentNode] END");
		}
	}

	public void testExtractDocumentFromEnvelopeData(){
		try{
			log.debug("[TestSecurity::testExtractDocumentFromEnvelopeData] BEGIN");
			EnvelopedContent envelopedContent=new EnvelopedContent();
			File file=new File(TEST_FILE_FIRMA);
			FileInputStream fis=new FileInputStream(file);
			byte[] buf=new byte[(int)file.length()];
			fis.read(buf);
			envelopedContent.setData(buf);
			envelopedContent.setStore(true);
			fis.close();
			Document document=ecmEngineDelegateImpl.extractDocumentFromEnvelope(envelopedContent, defaultContext);
			log.debug("[TestSecurity::testExtractDocumentFromEnvelopeData] document: " + document.getUid());
			LAST_TEMP_UID_2=document.getUid();
			Thread.sleep(SLEEP_TIME);
			assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			log.error("[TestSecurity::testExtractDocumentFromEnvelopeData] ERROR: " + e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestSecurity::testExtractDocumentFromEnvelopeData] END");
		}
	}

	public void testExtractDocumentFromEnvelopeNode(){
		try{
			log.debug("[TestSecurity::testExtractDocumentFromEnvelopeNode] BEGIN");
			if(LAST_TEMP_UID.equals("")){
				throw new Exception("Non esiste nessun nodo temporaneo.");
			}
			Node node=new Node();
			node.setUid(LAST_TEMP_UID);
			Document document=ecmEngineDelegateImpl.extractDocumentFromEnvelope(node, defaultContext);
			log.debug("[TestSecurity::testExtractDocumentFromEnvelopeNode] document: " + document.getBuffer());
			assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			log.error("[TestSecurity::testExtractDocumentFromEnvelopeNode] ERROR: " + e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestSecurity::testExtractDocumentFromEnvelopeNode] END");
		}
	}

	public void testCreateContentFromTemporaney(){
		try{
			log.debug("[TestSecurity::testCreateContentFromTemporaney] BEGIN");
			Node node=new Node();
			if(LAST_TEMP_UID.equals("")){
				if(LAST_TEMP_UID_2.equals("")){
					throw new Exception("Non esiste nessun nodo temporaneo.");
				}else{
					node.setUid(LAST_TEMP_UID_2);
				}
			}else{
				node.setUid(LAST_TEMP_UID);
			}
			Node parentNode = createNodeDTO(uidFolder);
			Content content=new Content();
			TEMP_CONTENT_NAME="temp_"+System.currentTimeMillis()+".pdf";
			content.setPrefixedName("cm:" + TEMP_CONTENT_NAME);
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setMimeType(TEMP_MIMETYPE);
			content.setEncoding("UTF-8");
			Node result=ecmEngineDelegateImpl.createContentFromTemporaney(parentNode, content, defaultContext, node);
			log.debug("[TestSecurity::testCreateContentFromTemporaney] result: " + result.getUid());
			assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			log.error("[TestSecurity::testCreateContentFromTemporaney] ERROR: " + e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestSecurity::testCreateContentFromTemporaney] END");
		}
	}

}

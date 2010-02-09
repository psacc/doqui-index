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
import it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.dto.engine.search.ResultContentData;
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

public class TestMassive extends TestCase implements EcmEngineTestConstants {
	// Creato nel Costruttore
	private EcmEngineDelegate ecmEngineDelegateImpl = null;

	// Messo a TRUE al primo giro di setup
	private static boolean flag=false;

	// Caricati da SETUP leggendo dal file di configurazione
	private static Properties properties = null;
	//private static String folderParent;
	private static String folder;
	private static String contenuto;
	private static OperationContext defaultContext;
	// Repository e sleep time
	private static String REPOSITORY = "primary";
	private static int SLEEP_TIME    = 5000;

	private static String TEST_TARGET;

	// Creato con i dati letti in configurazione
	//private static String uidFolderParent = null;
	private static String uidFolder       = null;
	private static String uidDocument     = null;

	// Altre informazioni sparse
	private static boolean down=false;

	private static Node[] testNodes=null;

	protected transient Log log;
	protected transient StopWatch stopwatch;

	public TestMassive(String name) {
		super(name);
		this.log = LogFactory.getLog(ECMENGINE_TEST_LOG_CATEGORY);
		log.debug("[TestMassive::Constructor] BEGIN - "+name);
		try {
			ecmEngineDelegateImpl = EcmEngineDelegateFactory.getEcmEngineDelegate();
			log.debug("[TestMassive::setUp] Delegate instantiate");
		} catch(EcmEngineDelegateInstantiationException e) {
			log.error("[TestMassive::setUp] Instantiation problem "+e);
		}
		log.debug("[TestMassive::Constructor] END");
	}

	@SuppressWarnings("static-access")
	private String insertDocument(String parent, String filePath, String mimetype,String enc, EncryptionInfo encryptionInfo, OperationContext ctx)
	{
		log.debug("[TestMassive::insertDocument] BEGIN");
		Content content = new Content();
		File file = new File(filePath);
		FileInputStream fis = null;
		String contentName = file.getName();
		byte [] buf = null;
		log.debug("[TestMassive::insertDocument] ==============================");
		log.debug("[TestMassive::insertDocument] INSERIMENTO CONTENUTO GENERICO");
		log.debug("[TestMassive::insertDocument] Nome file: " + filePath);
		log.debug("[TestMassive::insertDocument] Nome contenuto: " + contentName);
		log.debug("[TestMassive::insertDocument] Uid padre: " + parent);
		log.debug("[TestMassive::insertDocument] MIME-TYPE: " + mimetype);
		log.debug("[TestMassive::insertDocument] Encoding: " + enc);
		log.debug("[TestMassive::insertDocument] User: " + ctx.getUsername());
		log.debug("[TestMassive::insertDocument] ===============================");
		try {
			fis = new FileInputStream(file);
			buf = new byte[(int)file.length()];
			fis.read(buf);
		} catch (Exception e) {
			log.error("[TestMassive::insertDocument] Errore", e);
		} finally {
			if (fis == null) {
				log.debug("[TestMassive::insertDocument] INSERIMENTO FALLITO");
				log.debug("[TestMassive::insertDocument] Nome file: " + filePath);
				log.debug("[TestMassive::insertDocument] Causa: errore nell'accesso al file.");
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
			log.debug("[TestMassive::insertDocument] INSERIMENTO COMPLETATO");
			log.debug("[TestMassive::insertDocument] Nome file: " + filePath);
			log.debug("[TestMassive::insertDocument] Uid padre: " + parent);
			log.debug("[TestMassive::insertDocument] Uid nodo: " + uid);
			String metadati=getPropertyValue("cm:name", content.getProperties());
			AuditInfo auditTrail = createAuditInfoDTO("TestJUNIT","createContent",uid,metadati);
			ecmEngineDelegateImpl.logTrail(auditTrail, ctx);
			log.debug("[TestMassive::insertDocument] Audit Trail inserito.");
		} catch (InsertException e) {
			log.error("[TestMassive::insertDocument] Caricamento fallito", e);
		}  catch (Exception e) {
			log.error("[TestMassive::insertDocument] Errore", e);
			if(e instanceof AuditTrailException){
				log.error("[TestMassive::insertDocument] Si e` verificato un errore " +
				"nell'inserimento dell'audit Trail.");
			}
			if (e instanceof NoSuchNodeException){
				try {
					//Thread.currentThread().sleep(2000);
					Thread.sleep(SLEEP_TIME);
					result =  ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
				} catch (Exception e1) {
					log.error("[TestMassive::insertDocument] Errore", e);
				}
				uid=result.getUid();
			}
		}finally {
			log.debug("[TestMassive::insertDocument] END");
		}
		return uid;
	}

	private String createFolder(String parent, String name, OperationContext ctx)
	{
		log.debug("[TestMassive::createFolder] BEGIN");
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
			log.debug("[TestMassive::createFolder] CREAZIONE COMPLETATA");
			log.debug("[TestMassive::createFolder] Nome folder: " + name);
			log.debug("[TestMassive::createFolder] Uid padre: " + parent);
			log.debug("[TestMassive::createFolder] Uid nodo: " + uid);
		} catch (Exception e) {
			log.error("[TestMassive::createFolder] Errore: ", e);
		}finally{
			log.debug("[TestMassive::createFolder] END");
		}
		return uid;
	}

    /*
	private void linkContent(String parent, String child, String type,boolean childAssociation, String name){
		log.debug("[TestMassive::linkContent] BEGIN");
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
			log.debug("[TestMassive::linkContent] CREAZIONE LINK COMPLETATA");
		} catch (Exception e) {
			log.error("[TestMassive::linkContent] Errore: " + e);
		} finally {
			log.debug("[TestMassive::linkContent] END");
		}
	}
    */

	private String getUidCompanyHome(){
		log.debug("[TestMassive::getUidCompanyHome] BEGIN");
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
			log.debug("[TestMassive::getUidCompanyHome] estratto "+sizeLista+" record.");
			if (sizeLista>0 && results[0] != null) {
				uid=results[0].getUid();
				log.debug("[TestMassive::getUidCompanyHome] Uid CompanyHome : "+results[0].getUid());
			}
			else log.debug("[TestMassive::getUidCompanyHome] Uid CompanyHome Non Trovato.");
		} catch (Exception e) {
			log.error("[TestMassive::getUidCompanyHome] Errore: " + e);
		} finally {
			log.debug("[TestMassive::getUidCompanyHome] END");
		}
		return uid;
	}

	private String getUid( String xquery ){
		log.debug("[TestMassive::getUidCompanyHome] BEGIN");
		SearchParams xpath = new SearchParams();
		//query xpath per la ricerca del folder CompanyHome
		//String xquery = "/app:company_home";
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
			log.debug("[TestMassive::getUidCompanyHome] estratto "+sizeLista+" record.");
			if (sizeLista>0 && results[0] != null) {
				uid=results[0].getUid();
				log.debug("[TestMassive::getUidCompanyHome] Uid CompanyHome : "+results[0].getUid());
			}
			else log.debug("[TestMassive::getUidCompanyHome] Uid CompanyHome Non Trovato.");
		} catch (Exception e) {
			log.error("[TestMassive::getUidCompanyHome] Errore: " + e);
		} finally {
			log.debug("[TestMassive::getUidCompanyHome] END");
		}
		return uid;
	}

	protected void setUp() throws Exception {
		super.setUp();
		log.debug("[TestMassive::Setup] BEGIN");
		try {
			if (flag == false){
				log.debug("[TestMassive::setUp] Carico file di properties.");
				InputStream is = this.getClass().getResourceAsStream("/" + ECMENGINE_TEST_PROPERTIES_FILE);
				properties = new Properties();
				if (is != null) {
					properties.load(is);
					log.debug("[TestMassive::setUp] File di properties caricato : "+ECMENGINE_TEST_PROPERTIES_FILE);
					// Setup dei folder e del contenuto
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
					String dateStr = sdf.format(new Date());
					//folderParent = properties.getProperty(ECMENGINE_TEST_FOLDER_PARENT)+"_"+dateStr;
					folder       = properties.getProperty(ECMENGINE_TEST_FOLDER)+"_massive_"+dateStr;
					contenuto    = properties.getProperty(ECMENGINE_TEST_CONTENT);
					//log.debug("[TestMassive::setUp] folderParent vale: " + folderParent);
					log.debug("[TestMassive::setUp] folder vale      : " + folder);
					log.debug("[TestMassive::setUp] contenuto vale   : " + contenuto);
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
					log.error("[TestMassive::setUp] Si sono verificati problemi nella lettura del file di properties: "+ECMENGINE_TEST_PROPERTIES_FILE);
				}
				String uidCompanyHome = getUidCompanyHome();
				//creazione Folder sotto la CompanyHome
				uidFolder = createFolder(uidCompanyHome, folder, defaultContext);
				log.debug("[TestMassive::setUp] Creato folder " +uidFolder);
				Thread.sleep(SLEEP_TIME);
				//caricamento del documento generic_content.pdf all'interno del folder appena creato
				// logTrail
				// nel metodo insertDocument e` presenta anche la chiamata a logTrail con utente TestJUNIT
				uidDocument = insertDocument(uidFolder, contenuto, "application/pdf", "UTF-8", null, defaultContext);
				log.debug("[TestMassive::setUp] Creato document " +uidDocument);
				Thread.sleep(SLEEP_TIME);
				// Aggiunta di contenuto testuale, server per la fulltext search
				insertDocument(uidFolder, ECMENGINE_TEST_PROPERTIES_FILE, "text/plain", "UTF-8", null, defaultContext);
				log.debug("[TestMassive::setUp] Creato contenuto testuale");
				Thread.sleep(SLEEP_TIME);
				//creazione di un altro Folder sotto la CompanyHome
				//uidFolderParent = createFolder(uidCompanyHome, folderParent, defaultContext);
				//log.debug("[TestMassive::setUp] Creato folderParent " +uidFolderParent);
				//Thread.sleep(SLEEP_TIME);
				//creazione associazione child di nome linkTestJUNIT tra il folder appena creato e il documento generic_content.pdf
				//linkContent(uidFolderParent, uidDocument, "cm:contains", true, "cm:linkTestJUNIT");
				//Thread.sleep(SLEEP_TIME);
				flag = true;
			}
		}catch (Exception e) {
			log.error("[TestMassive::setUp] Errore: " + e);
		}
		log.debug("[TestMassive::Setup] END");
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
						log.debug("[TestMassive::tearDown] Eliminato nodo con uid: "+uid);
					}
				}

			}catch (Exception e) {
				log.error("[TestMassive::tearDown] Errore: " + e);
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

	private static String normalizeNumber(int number){
		if(number<10){
			return "0000"+number;
		}
		else if(number<100){
			return "000"+number;
		}
		else if(number<1000){
			return "00"+number;
		}
		else if(number<10000){
			return "0"+number;
		}
		else{
			return ""+number;
		}
	}

	public void testMassiveCreateContent(){
		log.debug("[TestMassive::testMassiveCreateContent] BEGIN");
		FileInputStream fis=null;
		try{
			String folderMassive=createFolder(uidFolder, "massivecreatecontent", defaultContext);
			log.debug("[TestMassive::testMassiveCreateContent] UID cartella creata: "+folderMassive);
			Node[] parentNodes=new Node[10];
			log.debug("[TestMassive::testMassiveCreateContent] Numero contenuti: "+parentNodes.length);
			log.debug("[TestMassive::testMassiveCreateContent] Creo i padri.");
			Content[] contents=new Content[parentNodes.length];
			for(int i=0;i<parentNodes.length;i++){
				parentNodes[i]=new Node();
				parentNodes[i].setUid(folderMassive);
			}

			log.debug("[TestMassive::testMassiveCreateContent] Creo i dati.");
			File file=new File(contenuto);
			fis=new FileInputStream(file);
			byte[] data=new byte[(int)file.length()];
			fis.read(data);
			fis.close();

			log.debug("[TestMassive::testMassiveCreateContent] Creo i contenuti.");
			for(int i=0;i<contents.length;i++){
				String contentName=normalizeNumber(i)+"_generic_content.pdf";

				Property[] props = new Property[1];
				props[0] = createPropertyDTO("cm:name", "text", false);
				props[0].setValues(new String [] { contentName });

				Property [] authorProps = new Property[1];
				authorProps[0] = createPropertyDTO("cm:author", "text", false);
				authorProps[0].setValues(new String [] { defaultContext.getUsername() + " da testcanccont" });
                Aspect author = new Aspect();
                author.setPrefixedName("cm:author");
                author.setModelPrefixedName("cm:contentmodel");
                author.setProperties(authorProps);

				Property [] titledProps = new Property[2];
				titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
				titledProps[0].setValues(new String [] { contentName });
				titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
				titledProps[1].setValues(new String [] { "Contenuto aggiunto da testcanccont" });
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
				content.setMimeType("application/pdf");
				content.setEncoding("UTF-8");
				content.setContent(data);
  				content.setProperties(props);
        		content.setAspects(new Aspect [] { author, titled });
				contents[i]=content;
			}
			log.debug("[TestMassive::testMassiveCreateContent] Inovo il metodo.");
			Node[] result=ecmEngineDelegateImpl.massiveCreateContent(parentNodes, contents, defaultContext);
			log.debug("[TestMassive::testMassiveCreateContent] Controllo il ritorno del metodo.");
			if(result.length==contents.length){
				for(int i=0;i<result.length;i++){
					if(result[i].getUid()==null||result[i].getUid()==""){
						assertTrue(false);
					}
				}
				testNodes=result;
				assertTrue(true);
			}
			else{
				assertTrue(false);
			}
		}catch(Exception e){
			log.error("[TestMassive::testMassiveCreateContent] ERROR\n"+e.getMessage());
			assertTrue(false);
		}finally{
			try{fis.close();}catch(Exception e){}
			log.debug("[TestMassive::testMassiveCreateContent] END");
		}
	}

	public void testUpdateMedatada(){
		log.debug("[TestMassive::testUpdateMedatada] BEGIN");
		try{
			if(testNodes==null){
				log.error("[TestMassive::testUpdateMedatada] Eseguire prima con successo il test testMassiveCreateContent().");
				assertTrue(false);
			}
			else{
				log.debug("[TestMassive::testUpdateMedatada] Preparo i contenuti.");
				Content[] newContents=new Content[testNodes.length];
				for(int i=0;i<newContents.length;i++){
					Content content=new Content();

					Aspect[] aspects=new Aspect[3];
					aspects[0]=new Aspect();
					aspects[0].setPrefixedName("sys:referenceable");
					aspects[1]=new Aspect();
					aspects[1].setPrefixedName("cm:auditable");
					aspects[2]=new Aspect();
					aspects[2].setPrefixedName("ecm-sys:modified");
					content.setAspects(aspects);
					content.setContentPropertyPrefixedName("");

					Property[] props=new Property[1];
					props[0]=new Property();
					props[0].setPrefixedName("cm:name");
					props[0].setMultivalue(false);
					props[0].setDataType("d:text");
					props[0].setValues(new String[]{normalizeNumber(i)+"_modificato.pdf"});
					content.setProperties(props);

					newContents[i]=content;
				}
				log.debug("[TestMassive::testUpdateMedatada] Invoco il metodo.");
				ecmEngineDelegateImpl.massiveUpdateMetadata(testNodes, newContents, defaultContext);
				assertTrue(true);
			}
		}catch(Exception e){
			log.error("[TestMassive::testUpdateMedatada] ERROR\n"+e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestMassive::testUpdateMedatada] END");
		}
	}

	public void testMassiveRetrieveDataeMetadata(){
		log.debug("[TestMassive::testMassiveRetrieveDataeMetadata] BEGIN");
		FileInputStream fis=null;
		try{
            // Dopo aver creato i dati con il MassiveCreate e updateMetadata
            // Faccio la getTotalResults() di una Xpath con tutti i contenuti di massivecreatecontent
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e1) {}

            SearchParams xpath = new SearchParams();
            xpath.setXPathQuery("/app:company_home/cm:"+folder+"/cm:massivecreatecontent/*");
            //xpath.setXPathQuery("/app:company_home/cm:1242901996722/*");

            SearchResponse response = null;
            start();

            try {
                int nTotal = ecmEngineDelegateImpl.getTotalResults(xpath, defaultContext);
                log.debug("[TestEngine::testXpathSearch] Numero risultati: "+xpath.getXPathQuery());
                log.debug("[TestEngine::testXpathSearch] Numero risultati: "+nTotal);

                if(nTotal>0){
                    assertTrue(true);
                    try {
                        // Ora prendo i metadati delle informazioni che ci sono sotto la ricerca
                        Node documento = new Node();
                        documento.setUid(uidFolder);
                        //documento.setUid( getUid(xpath.getXPathQuery().substring(0,xpath.getXPathQuery().length()-2)) );

                        SearchParamsAggregate parameterAggregate = new SearchParamsAggregate();
                        parameterAggregate.setXPathQuery( xpath.getXPathQuery() );
                        parameterAggregate.setLimit(200);
                        parameterAggregate.setPageSize(10);
                        parameterAggregate.setPageIndex(1);

                        // Poi con la select nodes prendo tutti i nodi
                        NodeResponse nr = ecmEngineDelegateImpl.selectNodes(documento, parameterAggregate, defaultContext);

                        log.debug("[TestEngine::testXpathSearch] Numero .getTotalResults(): "+nr.getTotalResults());
                        log.debug("[TestEngine::testXpathSearch] Numero .getPageSize()    : "+nr.getPageSize()    );
                        log.debug("[TestEngine::testXpathSearch] Numero .getPageIndex()   : "+nr.getPageIndex()   );
                        log.debug("[TestEngine::testXpathSearch] Numero .getNodeArray()   : "+nr.getNodeArray()   );
                        int nNodeSize = nr.getNodeArray().length;
                        if( nNodeSize>0 ){
                            assertTrue(true);

                            // Blocco
                            int nFrame = 4;
                            for( int n=0; n<nNodeSize; n+=nFrame ){
                                // Numero valori
                                int nV = nFrame;
                                if( (nNodeSize-n)<nV ) nV = nNodeSize-n;

                                // Creo la struttura
                                Node    []na = new Node[nV];
                                Content []ca = new Content[nV];
                                for( int m=n; m<n+nFrame && m<nNodeSize; m++ ){
                                   na[m-n] = new Node();
                                   na[m-n].setUid( nr.getNodeArray()[m].getUid() );
                                }

                                // poi con getContentMetadata prendo i metadati a blocchi
	                            ResultContent[] rc = ecmEngineDelegateImpl.massiveGetContentMetadata( na, defaultContext );
                                log.debug("[TestEngine::testXpathSearch] len: " +rc.length);
                                for( int m=n; m<n+nFrame && m<nNodeSize; m++ ){
                                   log.debug("[TestEngine::testXpathSearch] getPrefixedName: " +rc[m-n].getPrefixedName());
                                   Content content = new Content();
                                   content.setPrefixedName( rc[m-n].getPrefixedName() );
                                   content.setContentPropertyPrefixedName("cm:content");
                                   ca[m-n] = content;
                                }

                                // poi con retrieveContentData prendo i dati a blocchi
                                ResultContentData[] rcd = ecmEngineDelegateImpl.massiveRetrieveContentData( na, ca, defaultContext );
                                byte[] doc = rcd[0].getContent();
                                log.debug("[TestEngine::testXpathSearch] Contenuto lunghezza: "+doc.length);
                            }
                        }

                    } catch (Exception e) {
                        log.error("[TestEngine::testNodeExists] Eccezione", e);
                        assertTrue(false);
                    } finally{
                        stop();
                        log.debug("[TestEngine::testNodeExists] END");
                    }
                } else {
                    assertTrue(false);
                }

            } catch (Exception e) {
                log.error("[TestEngine::testXpathSearch] Eccezione", e);
                assertTrue(false);
            } finally{
                stop();
                log.debug("[TestEngine::testXpathSearch] END");
            }

		}catch(Exception e){
			log.error("[TestMassive::testMassiveRetrieveDataeMetadata] ERROR\n"+e.getMessage());
			assertTrue(false);
		}finally{
			try{fis.close();}catch(Exception e){}
			log.debug("[TestMassive::testMassiveRetrieveDataeMetadata] END");
		}
	}

}

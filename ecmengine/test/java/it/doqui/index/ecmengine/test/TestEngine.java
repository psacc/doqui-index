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
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Association;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatVersion;
import it.doqui.index.ecmengine.dto.engine.management.FileInfo;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.management.Rule;
import it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow;
import it.doqui.index.ecmengine.dto.engine.management.Version;
import it.doqui.index.ecmengine.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultProperty;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CheckInCheckOutException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.test.util.EcmEngineTestConstants;
import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestEngine extends TestCase implements EcmEngineTestConstants {
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


	protected transient Log log;
	protected transient StopWatch stopwatch;

	public TestEngine(String name) {
		super(name);
		this.log = LogFactory.getLog(ECMENGINE_TEST_LOG_CATEGORY);
        log.debug("[TestEngine::Constructor] BEGIN - "+name);
        try {
            ecmEngineDelegateImpl = EcmEngineDelegateFactory.getEcmEngineDelegate();
            log.debug("[TestEngine::setUp] Delegate instantiate");
        } catch(EcmEngineDelegateInstantiationException e) {
            log.error("[TestEngine::setUp] Instantiation problem "+e);
        }
		log.debug("[TestEngine::Constructor] END");
	}

	@SuppressWarnings("static-access")
	private String insertDocument(String parent, String filePath, String mimetype,String enc, EncryptionInfo encryptionInfo, OperationContext ctx)
	{
		log.debug("[TestEngine::insertDocument] BEGIN");
		Content content = new Content();

		File file = new File(filePath);
		FileInputStream fis = null;
		String contentName = file.getName();
		byte [] buf = null;

		log.debug("[TestEngine::insertDocument] ==============================");
		log.debug("[TestEngine::insertDocument] INSERIMENTO CONTENUTO GENERICO");
		log.debug("[TestEngine::insertDocument] Nome file: " + filePath);
		log.debug("[TestEngine::insertDocument] Nome contenuto: " + contentName);
		log.debug("[TestEngine::insertDocument] Uid padre: " + parent);
		log.debug("[TestEngine::insertDocument] MIME-TYPE: " + mimetype);
		log.debug("[TestEngine::insertDocument] Encoding: " + enc);
		log.debug("[TestEngine::insertDocument] User: " + ctx.getUsername());
		log.debug("[TestEngine::insertDocument] ===============================");

		try {
			fis = new FileInputStream(file);
			buf = new byte[(int)file.length()];
			fis.read(buf);
		} catch (Exception e) {
			log.error("[TestEngine::insertDocument] Errore", e);
		} finally {
			if (fis == null) {
				log.debug("[TestEngine::insertDocument] INSERIMENTO FALLITO");
				log.debug("[TestEngine::insertDocument] Nome file: " + filePath);
				log.debug("[TestEngine::insertDocument] Causa: errore nell'accesso al file.");
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

			log.debug("[TestEngine::insertDocument] INSERIMENTO COMPLETATO");
			log.debug("[TestEngine::insertDocument] Nome file: " + filePath);
			log.debug("[TestEngine::insertDocument] Uid padre: " + parent);
			log.debug("[TestEngine::insertDocument] Uid nodo: " + uid);

			String metadati=getPropertyValue("cm:name", content.getProperties());

			AuditInfo auditTrail = createAuditInfoDTO("TestJUNIT","createContent",uid,metadati);
			ecmEngineDelegateImpl.logTrail(auditTrail, ctx);

			log.debug("[TestEngine::insertDocument] Audit Trail inserito.");
		} catch (InsertException e) {
			log.error("[TestEngine::insertDocument] Caricamento fallito", e);
		}  catch (Exception e) {
			log.error("[TestEngine::insertDocument] Errore", e);
			if(e instanceof AuditTrailException){
				log.error("[TestEngine::insertDocument] Si e` verificato un errore " +
				"nell'inserimento dell'audit Trail.");
			}
			if (e instanceof NoSuchNodeException){
				try {
					//Thread.currentThread().sleep(2000);
		            Thread.sleep(SLEEP_TIME);
					result =  ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
				} catch (Exception e1) {
					log.error("[TestEngine::insertDocument] Errore", e);
				}
				uid=result.getUid();
			}
		}finally {
			log.debug("[TestEngine::insertDocument] END");
		}
		return uid;
	}

	private String createFolder(String parent, String name, OperationContext ctx)
	{
		log.debug("[TestEngine::createFolder] BEGIN");

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
			log.debug("[TestEngine::createFolder] CREAZIONE COMPLETATA");
			log.debug("[TestEngine::createFolder] Nome folder: " + name);
			log.debug("[TestEngine::createFolder] Uid padre: " + parent);
			log.debug("[TestEngine::createFolder] Uid nodo: " + uid);

		} catch (Exception e) {
			log.error("[TestEngine::createFolder] Errore: ", e);
		}finally{
			log.debug("[TestEngine::createFolder] END");
		}
		return uid;
	}

	private String createTextDocument(String parent, String text, String contentName, Property[] properties, boolean enableVersioning, OperationContext ctx)
	{
		log.debug("[TestEngine::insertDocument] BEGIN");
		Content content = new Content();

		Node parentNode = createNodeDTO(parent);

		content.setPrefixedName("cm:" + contentName);
		content.setParentAssocTypePrefixedName("cm:contains");
		content.setModelPrefixedName("cm:contentmodel");
		content.setTypePrefixedName("cm:content");
		content.setContentPropertyPrefixedName("cm:content");
		content.setMimeType("text/plain");
		content.setEncoding("UTF-8");

		Property [] authorProps = new Property[1];
		authorProps[0] = createPropertyDTO("cm:author", "text", false);
		authorProps[0].setValues(new String [] { ctx.getUsername() + " da client TestJUNIT" });

		Aspect author = new Aspect();
		author.setPrefixedName("cm:author");
		author.setModelPrefixedName("cm:contentmodel");
		author.setProperties(authorProps);

		Property [] titledProps = new Property[1];
		titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
		titledProps[0].setValues(new String [] { contentName });
//		titledProps[1] = createProperty("cm:description", "mltext", false);
//		titledProps[1].setValues(new String [] { "Contenuto aggiunto da client TestJUNIT." });

		Aspect titled = new Aspect();
		titled.setPrefixedName("cm:titled");
		titled.setModelPrefixedName("cm:contentmodel");
		titled.setProperties(titledProps);

		Aspect[] aspects = null;
		if (enableVersioning) {
			Aspect versionable = new Aspect();
			versionable.setPrefixedName("cm:versionable");
			versionable.setModelPrefixedName("cm:contentmodel");
//			Property [] versionableProps = new Property[1];
//			versionableProps[0] = createProperty("cm:autoVersion", "text", false);
//			versionableProps[0].setValues(new String [] { "false" });
//			versionable.setProperties(versionableProps);
			aspects = new Aspect [] { author, titled, versionable };
		} else {
			aspects = new Aspect [] { author, titled };
		}

		content.setProperties(properties);
		content.setAspects(aspects);
		content.setContent(text.getBytes());

		Node result=null;
		String uid=null;

		try {
			result = ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
			uid=result.getUid();

			log.debug("[TestEngine::insertDocument] INSERIMENTO COMPLETATO");
			log.debug("[TestEngine::insertDocument] Nome file: " + contentName);
			log.debug("[TestEngine::insertDocument] Uid padre: " + parent);
			log.debug("[TestEngine::insertDocument] Uid nodo: " + uid);

			String metadati=getPropertyValue("cm:name", content.getProperties());

			AuditInfo auditTrail = createAuditInfoDTO("TestJUNIT","createContent",uid,metadati);
			ecmEngineDelegateImpl.logTrail(auditTrail, ctx);

			log.debug("[TestEngine::insertDocument] Audit Trail inserito.");
		} catch (InsertException e) {
			log.error("[TestEngine::insertDocument] Caricamento fallito", e);
		}  catch (Exception e) {
			log.error("[TestEngine::insertDocument] Errore", e);
			if(e instanceof AuditTrailException){
				log.error("[TestEngine::insertDocument] Si e` verificato un errore " +
				"nell'inserimento dell'audit Trail.");
			}
			if (e instanceof NoSuchNodeException){
				try {
					//Thread.sleep(2000);
        		    Thread.sleep(SLEEP_TIME);
					result =  ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
				} catch (Exception e1) {
					log.error("[TestEngine::insertDocument] Errore", e);
				}
				uid=result.getUid();
			}
		}finally {
			log.debug("[TestEngine::insertDocument] END");
		}
		return uid;
	}


	private void linkContent(String parent, String child, String type,boolean childAssociation, String name){

		log.debug("[TestEngine::linkContent] BEGIN");

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
			log.debug("[TestEngine::linkContent] CREAZIONE LINK COMPLETATA");
		} catch (Exception e) {
			log.error("[TestEngine::linkContent] Errore: " + e);
		} finally {
			log.debug("[TestEngine::linkContent] END");
		}
	}

	private String getUidCompanyHome(){
		log.debug("[TestEngine::getUidCompanyHome] BEGIN");

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
			log.debug("[TestEngine::getUidCompanyHome] estratto "+sizeLista+" record.");

			if (sizeLista>0 && results[0] != null) {
				uid=results[0].getUid();
				log.debug("[TestEngine::getUidCompanyHome] Uid CompanyHome : "+results[0].getUid());
			}
			else log.debug("[TestEngine::getUidCompanyHome] Uid CompanyHome Non Trovato.");
		} catch (Exception e) {
			log.error("[TestEngine::getUidCompanyHome] Errore: " + e);
		} finally {
			log.debug("[TestEngine::getUidCompanyHome] END");
		}
		return uid;
	}

	protected void setUp() throws Exception {
		super.setUp();

		log.debug("[TestEngine::Setup] BEGIN");
		try {
			if (flag == false){
                log.debug("[TestEngine::setUp] Carico file di properties.");

                InputStream is = this.getClass().getResourceAsStream("/" + ECMENGINE_TEST_PROPERTIES_FILE);
                properties = new Properties();
                if (is != null) {
                    properties.load(is);
                    log.debug("[TestEngine::setUp] File di properties caricato : "+ECMENGINE_TEST_PROPERTIES_FILE);

                    // Setup dei folder e del contenuto
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
                    String dateStr = sdf.format(new Date());
                    folderParent = properties.getProperty(ECMENGINE_TEST_FOLDER_PARENT)+"_"+dateStr;
                    folder       = properties.getProperty(ECMENGINE_TEST_FOLDER)+"_"+dateStr;
                    contenuto    = properties.getProperty(ECMENGINE_TEST_CONTENT);

                    log.debug("[TestEngine::setUp] folderParent vale: " + folderParent);
                    log.debug("[TestEngine::setUp] folder vale      : " + folder);
                    log.debug("[TestEngine::setUp] contenuto vale   : " + contenuto);

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
                    log.error("[TestEngine::setUp] Si sono verificati problemi nella lettura del file di properties: "+ECMENGINE_TEST_PROPERTIES_FILE);
                }

				String uidCompanyHome = getUidCompanyHome();

				//creazione Folder sotto la CompanyHome

				uidFolder = createFolder(uidCompanyHome, folder, defaultContext);
                log.debug("[TestEngine::setUp] Creato folder " +uidFolder);

				Thread.sleep(SLEEP_TIME);

				//caricamento del documento generic_content.pdf all'interno del folder appena creato

				// logTrail
				// nel metodo insertDocument e` presenta anche la chiamata a logTrail con utente TestJUNIT
				uidDocument = insertDocument(uidFolder, contenuto, "application/pdf", "UTF-8", null, defaultContext);
                log.debug("[TestEngine::setUp] Creato document " +uidDocument);

				Thread.sleep(SLEEP_TIME);

                // Aggiunta di contenuto testuale, server per la fulltext search
				insertDocument(uidFolder, ECMENGINE_TEST_PROPERTIES_FILE, "text/plain", "UTF-8", null, defaultContext);
                log.debug("[TestEngine::setUp] Creato contenuto testuale");

				Thread.sleep(SLEEP_TIME);

				//creazione di un altro Folder sotto la CompanyHome
				uidFolderParent = createFolder(uidCompanyHome, folderParent, defaultContext);
                log.debug("[TestEngine::setUp] Creato folderParent " +uidFolderParent);

				Thread.sleep(SLEEP_TIME);

				//creazione associazione child di nome linkTestJUNIT tra il folder appena creato e il documento generic_content.pdf
				linkContent(uidFolderParent, uidDocument, "cm:contains", true, "cm:linkTestJUNIT");

				Thread.sleep(SLEEP_TIME);

				flag = true;
			}

		}catch (Exception e) {
			log.error("[TestEngine::setUp] Errore: " + e);
		}
		log.debug("[TestEngine::Setup] END");
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		/**
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
						log.debug("[TestEngine::tearDown] Eliminato nodo con uid: "+uid);
					}
				}

			}catch (Exception e) {
				log.error("[TestEngine::tearDown] Errore: " + e);
			}
		}
		**/
		ecmEngineDelegateImpl = null;
	}

	/**
	 * Metodo che ricerca le entry di audit utilizzando come filtro
	 * un'operazione non presente sulla tavola ecm_audit.
	 *
	 */
//	public void testAuditNotPresent() {
//		log.debug("[TestEngine::testAuditNotPresent] BEGIN");
//
//		try {
//			Thread.sleep(SLEEP_TIME);
//		} catch (InterruptedException e1) {}
//
//		final String logCtx = "Prova unit test nessun record di audit presente";
//
//		start();
//		try {
//			OperazioneAudit[] risultato = ecmEngineDelegateImpl.ricercaAuditPerOperazione("operazione", new Date(), new Date());
//			dumpElapsed("TestEngine", "testAuditNotPresent", logCtx, "chiamata al servizio ricercaPerOperazione");
//			int sizeLista = (risultato == null) ? 0 : risultato.length;
//			log.debug("[TestEngine::testAuditNotPresent] Numero risultati: " + sizeLista);
//			if(risultato == null) {
//				assertTrue(true);
//			} else {
//				assertTrue(false);
//			}
//
//		} catch(Exception e) {
//			log.error("[TestEngine::testAuditNotPresent] Eccezione vale: " + e);
//			assertTrue(false);
//		} finally {
//			stop();
//			log.debug("[TestEngine::testAuditNotPresent] END");
//		}
//	}



	/**
	 * Test per la ricerca Lucene con un parametro non valido.
	 */
	public void testLuceneSearchFolderInvalidParam() {
		log.debug("[TestEngine::testLuceneSearchFolderInvalidParam] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit ricerca lucene con parametro non valido.";

		SearchParams lucene = new SearchParams();
		lucene.setLimit(0);
		lucene.setLuceneQuery(null); // Parametro non valido

		start();
		try {
			SearchResponse response = ecmEngineDelegateImpl.luceneSearch(lucene, defaultContext);
			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLuceneSearchFolderInvalidParam", logCtx, "Chiamata a EcmEngine.luceneSearch().");

			log.error("[TestEngine::testLuceneSearchFolderInvalidParam] " +
					"ERRORE: La chiamata ha avuto successo (n. risultati = " + results.length + ")");
			assertTrue(false);

		} catch (InvalidParameterException e) {
			assertTrue(true);
			log.debug("[TestEngine::testLuceneSearchFolderInvalidParam] " +
					"Parametro non valido: OK");
		} catch (Exception e) {
			assertTrue(false);
			log.error("[TestEngine::testLuceneSearchFolderInvalidParam] Eccezione", e);
		} finally {
			stop();
			log.debug("[TestEngine::testLuceneSearchFolderInvalidParam] END");
		}
	}

	/**
	 * Test per la ricerca Lucene di un folder NON esistente.
	 */
	public void testLuceneSearchFolderNotExistent() {
		log.debug("[TestEngine::testLuceneSearchFolderNotExistent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit ricerca lucene su folder NON esistente.";

		SearchParams lucene = new SearchParams();
		lucene.setLimit(0);
		lucene.setLuceneQuery("TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\" " +
				"AND PATH:\"/app:company_home/*\"" +
				"AND @cm\\:name:\"folderNonEsistente\"");

		start();
		try {
			SearchResponse response = ecmEngineDelegateImpl.luceneSearch(lucene, defaultContext);
			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLuceneSearchFolderNotExistent", logCtx, "Chiamata a EcmEngine.luceneSearch().");

			assertNotNull(results);
			log.debug("[TestEngine::testLuceneSearchFolderNotExistent] Length: " + results.length);
			assertEquals(0, results.length);

		} catch (Exception e) {
			assertTrue(false);
			log.error("[TestEngine::testLuceneSearchFolderNotExistent] Eccezione", e);
		} finally {
			stop();
			log.debug("[TestEngine::testLuceneSearchFolderNotExistent] END");
		}
	}

	/**
	 * Test per la ricerca Lucene di un folder esistente.
	 */
	public void testLuceneSearchFolder() {
		log.debug("[TestEngine::testLuceneSearchFolder] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit ricerca lucene su folder esistente: " + folderParent;

		SearchParams lucene = new SearchParams();
		lucene.setLimit(0);
		lucene.setLuceneQuery("TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\" " +
				"AND PATH:\"/app:company_home/*\"" +
				"AND @cm\\:name:\"" + folderParent + "\"");

		start();
		try {
			SearchResponse response = ecmEngineDelegateImpl.luceneSearch(lucene, defaultContext);
			log.debug("[TestEngine::testLuceneSearchFolder] Query Lucene : "+lucene.getLuceneQuery());
			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLuceneSearchFolder", logCtx, "Chiamata a EcmEngine.luceneSearch().");

			assertNotNull(results);
			assertEquals(1, results.length);
			assertNotNull(results[0]);

		} catch (Exception e) {
			assertTrue(false);
			log.error("[TestEngine::testLuceneSearchFolder] Eccezione", e);
			e.printStackTrace();
		} finally {
			stop();
			log.debug("[TestEngine::testLuceneSearchFolder] END");
		}
	}

	/**
	 * Test per la ricerca Lucene full text.
	 */
	public void testLuceneSearchFullText() {
		log.debug("[TestEngine::testLuceneSearchFullText] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		String testoRicerca = "commenti";

		final String logCtx = "Test JUnit ricerca lucene full text: " + testoRicerca;
		SearchParams lucene = new SearchParams();

		StringBuffer luceneSearch = new StringBuffer("");
		luceneSearch.append("TEXT:");
		luceneSearch.append("\"" + testoRicerca + "\" AND ");
		luceneSearch.append("PATH:\"/app:company_home//*\"");

		lucene.setLuceneQuery(luceneSearch.toString());
		lucene.setLimit(0);

		start();
		try {
			SearchResponse response = ecmEngineDelegateImpl.luceneSearch(lucene, defaultContext);
			log.debug("[TestEngine::testLuceneSearchFullText] Query Lucene : "+lucene.getLuceneQuery());
			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLuceneSearchFullText", logCtx, "Chiamata a EcmEngine.luceneSearch().");
			log.debug("[TestEngine::testLuceneSearchFullText] Query Lucene restult size : "+(results==null?0:results.length));

			assertNotNull(results);
			//assertEquals(1, results.length);
			assertNotNull(results[0]);

		} catch (Exception e) {
			assertTrue(false);
			log.error("[TestEngine::testLuceneSearchFullText] Eccezione", e);
			e.printStackTrace();
		} finally {
			stop();
			log.debug("[TestEngine::testLuceneSearchFullText] END");
		}
	}

	/**
	 * Test per la verifica dell'esistenza di un nodo
	 * presente sul repository.
	 */
	public void testNodeExists(){
		log.debug("[TestEngine::testNodeExists] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit verifica nodo esistente.";

		SearchParams xpath = new SearchParams();
		xpath.setXPathQuery("/app:company_home/cm:"+folder);
		xpath.setLimit(1);

		String uid="";
		start();

		try {
			uid = ecmEngineDelegateImpl.nodeExists(xpath, defaultContext);
			dumpElapsed("TestEngine", "testNodeExists", logCtx, "Chiamata a nodeExists");
			log.debug("[TestEngine::testNodeExists] Nodo esistente , uid: "+uid);
			assertTrue(true);

		} catch (Exception e) {
			log.error("[TestEngine::testNodeExists] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testNodeExists] END");
		}
	}

	/**
	 * Test per la verifica dell'esistenza di un nodo
	 * non presente sul repository.
	 */
	public void testNodeNotExists(){
		log.debug("[TestEngine::testNodeNotExists] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit verifica nodo non presente.";

		SearchParams xpath = new SearchParams();
		xpath.setXPathQuery("/app:company_home/cm:folderNonPresente");
		xpath.setLimit(1);

		String uid="";

		start();

		try {
			uid = ecmEngineDelegateImpl.nodeExists(xpath, defaultContext);
			dumpElapsed("TestEngine", "testNodeNotExists", logCtx, "Chiamata a nodeExists");
			log.debug("[TestEngine::testNodeNotExists] Errore Nodo esistente , uid: "+uid);
			assertTrue(false);
		} catch(NoDataExtractedException ndee) {
			log.debug("[TestEngine::testNodeNotExists] Nodo non esistente: OK");
			assertTrue(true);
		} catch (Exception e) {
			log.error("[TestEngine::testNodeNotExists] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testNodeNotExists] END");
		}
	}

	/**
	 * Test ricerca xpath di un folder esistente
	 * nel repository.
	 *
	 */
	public void testXpathSearch(){

		log.debug("[TestEngine::testXpathSearch] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit ricerca nodo presente.";

		SearchParams xpath = new SearchParams();
		xpath.setXPathQuery("/app:company_home/cm:"+folder);
		xpath.setLimit(1);

		SearchResponse response = null;
		start();

		try {
			response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);
			dumpElapsed("TestEngine", "testXpathSearch", logCtx, "Chiamata a xpathSearch");
			log.debug("[TestEngine::testXpathSearch] Numero risultati: "+response.getTotalResults());

			if(response.getTotalResults()==1){
				assertTrue(true);
			}
			else assertTrue(false);

		} catch (Exception e) {
			log.error("[TestEngine::testXpathSearch] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testXpathSearch] END");
		}
	}


	/**
	 * Test ricerca xpath di un folder non presente
	 * nel repository.
	 *
	 */
	public void testXpathSearchFolderNotExistent(){

		log.debug("[TestEngine::testXpathSearchFolderNotExistent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit ricerca nodo non presente.";

		SearchParams xpath = new SearchParams();
		xpath.setXPathQuery("/app:company_home/cm:folderNonPresente");
		xpath.setLimit(1);

		SearchResponse response = null;
		start();

		try {
			response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);
			dumpElapsed("TestEngine", "testXpathSearchFolderNotExistent", logCtx, "Chiamata a xpathSearch");
			log.debug("[TestEngine::testXpathSearchFolderNotExistent] Numero risultati: "+response.getTotalResults());

			if(response.getTotalResults()==0){
				assertTrue(true);
			}
			else assertTrue(false);

		} catch (Exception e) {
			log.error("[TestEngine::testXpathSearchFolderNotExistent] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testXpathSearchFolderNotExistent] END");
		}
	}



	/**
	 * Metodo che ricerca le entry nella tavola dell'audit utilizzando come filtro
	 * un'operazione presente sulla tavola ecm_audit.
	 */
//	public void testAuditPresent() {
//		log.debug("[TestEngine::testAuditPresent] BEGIN");
//
//		try {
//			Thread.sleep(SLEEP_TIME);
//		} catch (InterruptedException e1) {}
//
//		final String logCtx = "Prova unit test operazione ricercata presente";
//
//		start();
//		try {
//			OperazioneAudit[] risultato = ecmEngineDelegateImpl.ricercaAuditPerOperazione("createContent", null, null);
//
//			dumpElapsed("TestEngine", "testAuditPresent", logCtx, "chiamata al servizio ricercaPerOperazione");
//
//			int sizeLista = risultato == null ? 0 : risultato.length;
//
//			dumpElapsed("TestEngine", "testAuditPresent", logCtx, "estratto "+sizeLista+" record");
//
//			if(risultato == null) {
//				assertTrue(false);
//			} else {
//				assertTrue(true);
//			}
//
//		} catch(Exception e) {
//			log.error("[TestEngine::testAuditPresent] Eccezione", e);
//			assertTrue(false);
//		} finally {
//			stop();
//			log.debug("[TestEngine::testAuditPresent] END");
//		}
//	}

	/**
	 * Metodo che ricerca le entry di audit trail per utente con un utente presente sulla
	 * tavola ecm_audit_trail
	 */

	public void testLogAuditTrailPresent(){

		log.debug("[TestEngine::testLogAuditTrailPresent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test operazione get trail x utente";

		AuditInfo[] risultato= null;

		start();
		try {
			AuditTrailSearchParams parametriRicerca = new AuditTrailSearchParams();
			parametriRicerca.setUtente("TestJUNIT");
			risultato=ecmEngineDelegateImpl.ricercaAuditTrail(parametriRicerca, defaultContext);
			dumpElapsed("TestEngine", "testLogAuditTrailPresent", logCtx, "chiamata al servizio getAuditTrailForUser");
			int sizeLista = risultato == null ? 0 : risultato.length;
			dumpElapsed("TestEngine", "testLogAuditTrailPresent", logCtx, "estratto "+sizeLista+" record");
			log.debug("[TestEngine::testLogAuditTrailPresent] estratto "+sizeLista+" record");

			if(risultato == null) {
				assertTrue(false);
			} else {
				assertTrue(true);
			}

		} catch(Exception e) {
			log.error("[TestEngine::testLogAuditTrailPresent] Eccezione", e);
			assertTrue(false);
		}  finally {
			stop();
			log.debug("[TestEngine::testLogAuditTrailPresent] END");
		}
	}

	/**
	 * Metodo che ricerca le entry dell' audit trail per operazione con un'operazione non presente sulla
	 * tavola ecm_audit_trail
	 *
	 */
	public void testLogAuditTrailNotPresent(){

		log.debug("[TestEngine::testLogAuditTrailNotPresent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test nessun record di audit trail presente";

		AuditInfo[] result= null;

		start();

		try {
			AuditTrailSearchParams parametriRicerca = new AuditTrailSearchParams();
			parametriRicerca.setNomeOperazione("operazione");
			result=ecmEngineDelegateImpl.ricercaAuditTrail(parametriRicerca, defaultContext);

			dumpElapsed("TestEngine", "testLogAuditTrailNotPresent", logCtx, "chiamata al servizio getAuditTrailForOperation");
			int sizeLista = (result == null) ? 0 : result.length;

			log.debug("[TestEngine::testLogAuditTrailNotPresent] Numero risultati: " + sizeLista);
			if(result == null || result.length == 0) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}

		} catch (Exception e) {
			log.error("[TestEngine::testLogAuditTrailNotPresent] Eccezione", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testLogAuditTrailNotPresent] END");
		}
	}

	/**
	 * Metodo che , dopo un operazione di inserimetno di audit trail, effettua una ricerca
	 * nella tavola ecm_audit_trail utilizzando un filtro composto dai dati appena inseriti.
	 */

	public void testGetAudiTrailWork(){
		log.debug("[TestEngine::testGetAudiTrailWork] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test get audit trail work";

		AuditInfo[] result= null;

		start();

		try {

			AuditInfo audit = createAuditInfoDTO("ClientJUNIT","testGetAudiTrailWork","idOggetto","MetaDatiXml");

			ecmEngineDelegateImpl.logTrail(audit, defaultContext);
			log.debug("[TestEngine::testGetAudiTrailWork] Audit Trail inserito.");

			AuditTrailSearchParams params = new AuditTrailSearchParams();
			params.setUtente(audit.getUtente());
			params.setNomeOperazione(audit.getOperazione());
			params.setIdOggetto(audit.getIdOggetto());
			result=ecmEngineDelegateImpl.ricercaAuditTrail(params, defaultContext);

			dumpElapsed("TestEngine", "testGetAudiTrailWork", logCtx, "chiamata al servizio getAuditTrail");
			log.debug("[TestEngine::testGetAudiTrailWork] Chiamata al servizio getAuditTrail.");

			int sizeLista = (result == null) ? 0 : result.length;

			log.debug("[TestEngine::testGetAudiTrailWork] Numero risultati: " + sizeLista);

			if(result == null) {
				assertTrue(false);
			} else
				if(sizeLista>0)
					assertTrue(true);
		} catch (InvalidParameterException e) {
			log.debug("[TestEngine::testGetAudiTrailWork] Eccezione vale: " + e);
			assertTrue(false);
		} catch (AuditTrailException e) {
			log.debug("[TestEngine::testGetAudiTrailWork] Eccezione vale: " + e);
			assertTrue(false);
		} catch (Exception e) {
			log.debug("[TestEngine::testGetAudiTrailWork] Eccezione vale: " + e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetAudiTrailWork] END");
		}
	}

	/**
	 * Metodo che verifica la non presenza di un associazione padre-figlio tra un folder e un
	 * contenuto(file pdf) presenti sul repository.
	 * Verifica l'assenza dell link linkTestJUNIT_Mancante all'interno del folder company_home/Folder-JUNIT.
	 */

	public void testLinkContentNotExist(){
		log.debug("[TestEngine::testLinkContentNotExist] BEGIN");
		log.debug("[TestEngine::testLinkContentNotExist] uidDocument :"+uidDocument);
		log.debug("[TestEngine::testLinkContentNotExist] uidFolderParent :"+uidFolderParent);

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test link content non presente";
		SearchParams xpath = new SearchParams();
		//query xpath per la ricerca del nodo presente all'interno del folder Folder-JUNIT
		//verifico se esiste l'associazione linkTestJUNIT_Mancante all'interno di Folder-JUNIT

		String parent="cm:" + folderParent;
		//xpath.setXPathQuery("/app:company_home/cm:Folder-JUNIT/*");
		String xquery="/app:company_home/" + parent + "/*";
		xpath.setXPathQuery(xquery);
		xpath.setLimit(1);

		start();
		try {
			SearchResponse response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLinkContentNotExist", logCtx,
					"chiamata al servizio xpathSearch");
			int sizeLista = (results == null) ? 0 : results.length;
			dumpElapsed("TestEngine", "testLinkContentNotExist", logCtx, "estratto " +
					sizeLista + " record.");
			log.debug("[TestEngine::testLinkContentNotExist] estratto " + sizeLista + " record.");


			if (sizeLista > 0) {
				log.debug("[TestEngine::testLinkContentNotExist] Ci sono risultati.");
				String name="cm:"+contenuto;
				if (results[0].getUid().equalsIgnoreCase(uidDocument)
						&& results[0].getTypePrefixedName().equalsIgnoreCase("cm:content")
						&& results[0].getPrefixedName().equalsIgnoreCase(name)) {

					//TODO: parlare con Francesco per l'implementazione della ricerca delle associazioni
					//getAssociations() mi da le associazioni figlie , qui servono quelli padri

					//ResultAssociation[] association = results[0].getAssociations();

					String uid=results[0].getUid();

					Node nodo= createNodeDTO(uid);

					ResultAssociation[] associazioni = ecmEngineDelegateImpl.getAssociations(nodo, "PARENT", 0, defaultContext);

					if (associazioni != null){
						log.debug("[TestEngine::testLinkContentNotExist] Ci sono Associazioni PARENT.");

						for(int i=0;i<associazioni.length;i++){
							if(associazioni[i]!=null){
								log.debug("[TestEngine::testLinkContentNotExist] Verifico se tra le associazioni PARENT c'e` cm:linkTestJUNIT_Mancante.");
								if (associazioni[i].getPrefixedName().equalsIgnoreCase("cm:linkTestJUNIT_Mancante")
										&& associazioni[i].getTypePrefixedName().equalsIgnoreCase("cm:contains")
										&& associazioni[i].isChildAssociation()){
									assertTrue(false);
									return;
								}
							}
						}
						assertTrue(true);
					} else
						assertTrue(true);


					/**
    			  if(association!=null){
    				  log.debug("[TestEngine::testLinkContentNotExist] Ci sono associazioni.");
    				  for(int i=0;i<association.length;i++){
    					  if(association[i]!=null){
    						  if (association[i].getPrefixedName().equalsIgnoreCase("cm:linkTestJUNIT_Mancante")
    								  && association[i].getTypePrefixedName().equalsIgnoreCase("cm:contains")
    								  && association[i].isChildAssociation()){
    							  assertTrue(false);
    						  }
    					  }
    				  }
    				  assertTrue(true);
    			  } else
    				  assertTrue(true);
					 **/

				}
				else
					assertTrue(true);
			} else
				assertTrue(true);
		} catch (Exception e) {
			log.debug("[TestEngine::testLinkContentNotExist] Eccezione vale : "+e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testLinkContentNotExist] END");
		}
	}

	public void testLinkParentExist(){
		log.debug("[TestEngine::testLinkParentExist] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test link parent presente";

		SearchParams xpath = new SearchParams();
		//query xpath per la ricerca del folder Folder-JUNIT
		//verifico se esiste il nodo Folder-JUNIT


		String parent="cm:" + folderParent;
		//xpath.setXPathQuery("/app:company_home/cm:Folder-JUNIT");
		String xquery = "/app:company_home/" + parent;
		xpath.setXPathQuery(xquery);
		xpath.setLimit(1);

		start();

		SearchResponse response = null;
		ResultContent[] results=null;
		try {
			response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);
			results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLinkParentExist", logCtx, "chiamata al servizio xpathSearch");
			int sizeLista = results == null ? 0 : results.length;
			dumpElapsed("TestEngine", "testLinkParentExist", logCtx, "estratto "+sizeLista+" record.");
			log.debug("[TestEngine::testLinkParentExist] estratto "+sizeLista+" record.");

			if (sizeLista>0){
				log.debug("[TestEngine::testLinkParentExist] Uid dalla ricerca: "+results[0].getUid());
				log.debug("[TestEngine::testLinkParentExist] Uid del folder: "+uidFolderParent);

				log.debug("[TestEngine::testLinkParentExist] Name: "+results[0].getPrefixedName());
				log.debug("[TestEngine::testLinkParentExist] Name del folder : "+parent);

				log.debug("[TestEngine::testLinkParentExist] Tipo: "+results[0].getTypePrefixedName());

				if(results[0].getUid().equalsIgnoreCase(uidFolderParent) &&
						results[0].getPrefixedName().equalsIgnoreCase(parent) &&
						results[0].getTypePrefixedName().equalsIgnoreCase("cm:folder")){
					assertTrue(true);
				} else
					assertTrue(false);
			} else
				assertTrue(false);
		} catch (Exception e) {
			log.debug("[TestEngine::testLinkParentExist] Eccezione vale : "+e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testLinkParentExist] END");
		}
	}

	public void testLinkChildExist(){
		log.debug("[TestEngine::testLinkChildExist] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test link child presente";

		SearchParams xpath = new SearchParams();
		//query xpath per la ricerca del document generic_content.pdf all'interno del folder Folder-Test

		//cartella="Test-Folder";
		String parent ="cm:" + folder;
		String xquery="/app:company_home/"+parent+"/cm:"+ contenuto;
		xpath.setXPathQuery(xquery);
		xpath.setLimit(1);

		start();

		SearchResponse response = null;
		ResultContent[] results=null;
		try {
			response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);
			results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLinkChildExist", logCtx, "chiamata al servizio xpathSearch");
			int sizeLista = results == null ? 0 : results.length;
			dumpElapsed("TestEngine", "testLinkChildExist", logCtx, "estratto "+sizeLista+" record.");
			log.debug("[TestEngine::testLinkChildExist] estratto "+sizeLista+" record.");

			if (sizeLista>0){
				log.debug("[TestEngine::testLinkChildExist] Uid dalla ricerca: "+results[0].getUid());
				log.debug("[TestEngine::testLinkChildExist] Uid del documento: "+uidDocument);

				log.debug("[TestEngine::testLinkChildExist] Name: "+results[0].getPrefixedName());
				log.debug("[TestEngine::testLinkChildExist] Tipo: "+results[0].getTypePrefixedName());

				String name="cm:"+contenuto;
				if(results[0].getUid().equalsIgnoreCase(uidDocument) &&
						results[0].getPrefixedName().equalsIgnoreCase(name) &&
						results[0].getTypePrefixedName().equalsIgnoreCase("cm:content")){
					assertTrue(true);
				}else
					assertTrue(false);
			} else
				assertTrue(false);
		} catch (Exception e) {
			log.debug("[TestEngine::testLinkChildExist] Eccezione vale : "+e);
			assertTrue(false);
		}finally {
			stop();
			log.debug("[TestEngine::testLinkChildExist] END");
		}
	}



	/**
	 * Metodo che verifica l'esistenza di un associazione padre-figlio tra un folder e un
	 * contenuto(file pdf) presenti sul repository.
	 * Verifica se esiste un link all'interno di un folder creato nella company_home.
	 */

	@SuppressWarnings("static-access")
	public void testLinkContentExist(){

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		log.debug("[TestEngine::testLinkContentExist] BEGIN");

		final String logCtx = "Prova unit test link content presente";

		SearchParams xpath = new SearchParams();
		//query xpath per la ricerca del nodo presente all'interno del folder Folder-JUNIT
		//verifico se esiste il link linkTestJUNIT all'interno di Folder-JUNIT

		String parent="cm:" + folderParent;

		String xquery="/app:company_home/" + parent + "/cm:linkTestJUNIT";
		xpath.setXPathQuery(xquery);
		xpath.setLimit(1);

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		start();
		try {
			SearchResponse response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testLinkContentExist", logCtx, "chiamata al servizio xpathSearch");
			int sizeLista = results == null ? 0 : results.length;
			dumpElapsed("TestEngine", "testLinkContentExist", logCtx, "estratto "+sizeLista+" record.");
			log.debug("[TestEngine::testLinkContentExist] estratto "+sizeLista+" record.");

			if (sizeLista>0){
				log.debug("[TestEngine::testLinkContentExist] Ci sono risultati.");

				log.debug("[TestEngine::testLinkContentExist] Uid dalla ricerca: "+results[0].getUid());
				log.debug("[TestEngine::testLinkContentExist] Uid del documento: "+uidDocument);

				log.debug("[TestEngine::testLinkContentExist] Name: "+results[0].getPrefixedName());

				log.debug("[TestEngine::testLinkContentExist] Tipo: "+results[0].getTypePrefixedName());

				String name = "cm:"+contenuto;

				if(results[0].getUid().equalsIgnoreCase(uidDocument)
						&& results[0].getTypePrefixedName().equalsIgnoreCase("cm:content")
						&& results[0].getPrefixedName().equalsIgnoreCase(name)){

					log.debug("[TestEngine::testLinkContentExist] Result : "+results[0].getPrefixedName());

					String uid=results[0].getUid();

					Node nodo= createNodeDTO(uid);

					ResultAssociation[] associazioni=null;

					log.debug("[TestEngine::testLinkContentExist] Prima della chiamata a getAssociations");
					associazioni = ecmEngineDelegateImpl.getAssociations(nodo, "PARENT", 0, defaultContext);
					log.debug("[TestEngine::testLinkContentExist] Dopo la chiamata a getAssociations");

					if(associazioni!=null){
						log.debug("[TestEngine::testLinkContentExist] Ci sono Associazioni PARENT.");
						for(int i=0;i<associazioni.length;i++){
							if(associazioni[i]!=null){
								log.debug("[TestEngine::testLinkContentExist] Verifico se tra le associazioni PARENT c'e` cm:linkTestJUNIT.");
								log.debug("[TestEngine::testLinkContentExist] Nome :"+associazioni[i].getPrefixedName());
								log.debug("[TestEngine::testLinkContentExist] Tipo :"+associazioni[i].getTypePrefixedName());
								log.debug("[TestEngine::testLinkContentExist] Realzione PAdre-Figlio :"+associazioni[i].isChildAssociation());
								if (associazioni[i].getPrefixedName().equalsIgnoreCase("cm:linkTestJUNIT")
										&& associazioni[i].getTypePrefixedName().equalsIgnoreCase("cm:contains")
										&& associazioni[i].isChildAssociation()){

									assertTrue(true);
									return;
								}
							}
						}
						log.debug("[TestEngine::testLinkContentExist] Test Fallito.");
						assertTrue(false);
					} else
						assertTrue(false);

					//TODO: parlare con francesco per vedere l'imeplementazione della ricerca (Parent,child,ecc..)
					/**
					 * getAssociations() ritonrna le associazioni figlie; qui mi servono invece quelli padri
    			  ResultAssociation[] association = results[0].getAssociations();

    			  if(association!=null){
    				  log.debug("[TestEngine::testLinkContentExist] Ci sono associazioni.");
    				  for(int i=0;i<association.length;i++){
    					  if(association[i]!=null){
    						  if (association[i].getPrefixedName().equalsIgnoreCase("cm:linkTestJUNIT")
    								  && association[i].getTypePrefixedName().equalsIgnoreCase("cm:contains")
    								  && association[i].isChildAssociation()){
    							  assertTrue(true);
    						  }
    					  }
    				  }
    				  log.debug("[TestEngine::testLinkContentExist] Test Fallito.");
    				  assertTrue(false);
    			  } else
    				  assertTrue(false);
					 **/
				} else {
					assertTrue(false);
				}
			} else {
				assertTrue(false);
			}
		} catch (Exception e) {
			log.error("[TestEngine::testLinkContentExist] Eccezione", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testLinkContentExist] END");
		}
	}


	@SuppressWarnings("static-access")
	public void testUnLinkContentWork(){

		log.debug("[TestEngine::testUnLinkContentWork] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Prova unit test unlink content work";

		//linkContent(uidFolderParent,uidDocument,"cm:contains",true,"cm:linkTestJUNIT");

		Node sourceNode = createNodeDTO(uidFolderParent);
		Node destinationNode = createNodeDTO(uidDocument);

		Association association = new Association();
		association.setChildAssociation(true);
		association.setPrefixedName("cm:linkTestJUNIT");
		association.setTypePrefixedName("cm:contains");


		start();

		try {
			ecmEngineDelegateImpl.unLinkContent(sourceNode, destinationNode, association, defaultContext);
			log.debug("[TestEngine::testUnLinkContentWork] Link Eliminato.");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SearchParams xpath = new SearchParams();
			String parent="cm:" + folderParent;

			String xquery="/app:company_home/" + parent + "/cm:linkTestJUNIT";
			xpath.setXPathQuery(xquery);
			xpath.setLimit(1);

			SearchResponse response = ecmEngineDelegateImpl.xpathSearch(xpath, defaultContext);
			ResultContent[] results = response.getResultContentArray();
			dumpElapsed("TestEngine", "testUnLinkContentWork", logCtx, "chiamata al servizio xpathSearch");
			int sizeLista = results == null ? 0 : results.length;
			dumpElapsed("TestEngine", "testUnLinkContentWork", logCtx, "estratto "+sizeLista+" record.");
			log.debug("[TestEngine::testUnLinkContentWork] estratto "+sizeLista+" record.");
			log.debug("[TestEngine::testUnLinkContentWork] Numero risultati:  " + response.getTotalResults());


			if(sizeLista==0 || (results!=null && results[0]==null)){
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (Exception e) {
			log.error("[TestEngine::testUnLinkContentWork] Eccezione", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testUnLinkContentWork] END");
		}
	}

	/**
	 * Test per la verifica della creazione di un contenuto.
	 * Il contenuto e' quello creato nel metodo setUp() con
	 * uid uidDocument.
	 *
	 */

	public void testCreateContent(){
		log.debug("[TestEngine::testCreateContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione createContent";

		SearchParams xpath = new SearchParams();
		xpath.setLimit(1);
		String path="/app:company_home/cm:"+folder+"/cm:"+contenuto;
		xpath.setXPathQuery(path);

		String uid = null;

		start();

		try {
		     uid = ecmEngineDelegateImpl.nodeExists(xpath, defaultContext);
		     dumpElapsed("TestEngine", "testCreateContent", logCtx, "chiamata al servizio nodeExists");
		     log.debug("[TestEngine::testCreateContent] estratto nodo con uid: "+uid);

		 	if(uid == null) {
				assertTrue(false);
			} else if(uidDocument.equalsIgnoreCase(uid)){
				assertTrue(true);
			}

		} catch (Exception e) {
			log.debug("[TestEngine::testCreateContent] Eccezione vale :"+e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testCreateContent] END");
		}
	}

	/**
	 * Test per la verifica di un contenuto non esistente.
	 *
	 */

	public void testCreateContentNotExistent(){
		log.debug("[TestEngine::testCreateContentNotExistent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione createContent non esistente";

		SearchParams xpath = new SearchParams();
		xpath.setLimit(1);
		String path="/app:company_home/cm:"+folder+"/cm:contenutoNonEsistente";
		xpath.setXPathQuery(path);

		String uid = null;

		start();

		try {
		     uid = ecmEngineDelegateImpl.nodeExists(xpath, defaultContext);
		     dumpElapsed("TestEngine", "testCreateContentNotExistent", logCtx, "chiamata al servizio nodeExists");
		     log.debug("[TestEngine::testCreateContentNotExistent] estratto nodo con uid: "+uid);

		 	if(uid == null) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}

		} catch (NoDataExtractedException e) {
			dumpElapsed("TestEngine", "testCreateContentNotExistent", logCtx, "chiamata al servizio nodeExists");
			log.debug("[TestEngine::testCreateContentNotExistent] " +
					"Eccezione di tipo NoDataExtractedException. Test Valido.");
			assertTrue(true);
		} catch (Exception e) {
			log.debug("[TestEngine::testCreateContentNotExistent] Eccezione vale :"+e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testCreateContentNotExistent] END");
		}
	}

	/**
	 * Test per la verifica della lettura di un contenuto
	 * presente nel repository.
	 *
	 */
	public void testRetrieveContentData(){
		log.debug("[TestEngine::testRetrieveContentData] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione retrieveContentData";


		Node documento = new Node();
		documento.setUid(uidDocument);

		Content content = new Content();
		content.setPrefixedName("cm:generic_content.pdf");
		content.setContentPropertyPrefixedName("cm:content");

		byte[] doc = null;

		start();

		try {
			  doc = ecmEngineDelegateImpl.retrieveContentData(documento, content, defaultContext);
			  dumpElapsed("TestEngine", "testRetrieveContentData", logCtx, "chiamata al servizio retrieveContentData");
			  log.debug("[TestEngine::testRetrieveContentData] Letto contenuto con uid: "+uidDocument);

			  if(doc==null){
				  assertTrue(false);
			  } else{
				  assertTrue(true);
			  }

		} catch (Exception e) {
			log.debug("[TestEngine::testRetrieveContentData] Eccezione vale :"+e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testRetrieveContentData] END");
		}
	}


	/**
	 * Test per la verifica dell'aggiornamento di un contenuto
	 * presente nel repository.
	 *
	 */
	public void testUpdateContentData(){
		log.debug("[TestEngine::testUpdateContentData] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione updateContentData";


		Node documento = new Node();
		documento.setUid(uidDocument);

		Content content = new Content();
		content.setPrefixedName("cm:generic_content.pdf");
		content.setContentPropertyPrefixedName("cm:content");

		byte[] doc = null;

		start();

		try {
			doc = ecmEngineDelegateImpl.retrieveContentData(documento, content, defaultContext);

			dumpElapsed("TestEngine", "testUpdateContentData", logCtx, "chiamata al servizio retrieveContentData");
			log.debug("[TestEngine::testUpdateContentData] Letto contenuto con uid: "+uidDocument);

			if(doc!=null){
				content = new Content();
				content.setPrefixedName("cm:generic_content.pdf");
				content.setParentAssocTypePrefixedName("cm:contains");
				content.setModelPrefixedName("cm:contentmodel");
				content.setTypePrefixedName("cm:content");
				content.setContentPropertyPrefixedName("cm:content");
				content.setMimeType("application/pdf");
				content.setEncoding("UTF-8");
				content.setContent(doc);

				ecmEngineDelegateImpl.updateContentData(documento, content, defaultContext);
				dumpElapsed("TestEngine", "testUpdateContentData", logCtx, "chiamata al servizio updateContentData");
				log.debug("[TestEngine::testUpdateContentData] update contenuto con uid: "+uidDocument);
				assertTrue(true);
				return;
			}
			assertTrue(false);
		} catch (Exception e) {
			log.debug("[TestEngine::testUpdateContentData] Eccezione vale :"+e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testUpdateContentData] END");
		}
	}


//	public void testMoveAggregation(){
//		log.debug("[TestEngine::testMoveAggregation] BEGIN");
//
//		try {
//			Thread.sleep(SLEEP_TIME);
//		} catch (InterruptedException e1) {}
//
//		final String logCtx = "Unit test operazione testMoveAggregation";
//
//		Node source = new Node();
//		source.setUid(uidDocument);
//		source.setRepository(REPOSITORY);
//
//		Node destinationParent = new Node();
//		destinationParent.setUid(uidFolderParent);
//		destinationParent.setRepository(REPOSITORY);
//
//		SearchResponse risultati = null;
//
//		start();
//
//		try {
//
//			ecmEngineDelegateImpl.moveAggregation(source, destinationParent, defaultContext);
//			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "chiamata al servizio moveAggregation");
//			log.debug("[TestEngine::testMoveAggregation] Nodo da spostare "+source.getUid() +" nel folder: "
//					+destinationParent.getUid());
//
//			//proprieta ecm-sys:stato (dell'aspect ecm-sys:state)
//			SearchParams lucene = new SearchParams();
//			lucene.setLuceneQuery("@ecm-sys\\:stato:\"spostabile\"");
//			lucene.setLimit(0);
//			lucene.setRepository(REPOSITORY);
//
//			Thread.sleep(SLEEP_TIME);
//
//			risultati = ecmEngineDelegateImpl.luceneSearch(lucene, defaultContext);
//			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "chiamata al servizio luceneSearch");
//			log.debug("[TestEngine::testMoveAggregation] chiamata al servizio luceneSearch");
//			int size = risultati != null ? risultati.getTotalResults() : 0;
//			log.debug("[TestEngine::testMoveAggregation] risultati ricerca lucene: "+size);
//
//			ResultContent[] contents = null;
//
//			if(size>0){
//				contents = risultati.getResultContentArray();
//				if(contents!=null){
//					ResultProperty[] props = null;
//					for(int i=0;i<contents.length;i++){
//						props = contents[i].getProperties();
//						if(props!=null){
//							for(int j=0;j<props.length;j++){
//								if(props[j]!=null && "ecm-sys:stato".equalsIgnoreCase(props[j].getPrefixedName())
//										&& "spostabile".equalsIgnoreCase(props[j].getValues()[0])){
//									if(contents[i].getUid().equalsIgnoreCase(source.getUid())){
//										assertTrue(true);
//										return;
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			assertTrue(false);
//		} catch (InvalidParameterException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (MoveException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (NoSuchNodeException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (InvalidCredentialsException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (PermissionDeniedException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (RemoteException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (TooManyResultsException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		} catch (SearchException e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		}catch (Exception e) {
//			log.debug("[TestEngine::testMoveAggregation] Eccezione vale :"+e);
//			assertTrue(false);
//		}finally{
//			stop();
//			log.debug("[TestEngine::testMoveAggregation] END");
//		}
//	}


	/**
	 * Test per la verifica dell'aggiornamento dei metadati
	 * di un contenuto presente nel repository.
	 * Viene aggiunto un nuovo aspect a quelli presenti.
	 *
	 */
	public void testUpdateMetadata(){
		log.debug("[TestEngine::testUpdateMetadata] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione UpdateMetadata";

		Node documento = new Node();
		documento.setUid(uidDocument);

		String value = "normale";

		Property [] propArray = new Property[1];
		propArray[0] = new Property();
		propArray[0].setPrefixedName("ecm-sys:stato");
		propArray[0].setDataType("text");
		propArray[0].setMultivalue(false);
		propArray[0].setValues(new String [] { value });

		//cm:titled,cm:auditable,sys:referenceable,cm:author,ecm-sys:state
		Aspect [] aspectArray = new Aspect[5];
		aspectArray[0] = new Aspect();
		aspectArray[0].setPrefixedName("cm:titled");
		aspectArray[1] = new Aspect();
		aspectArray[1].setPrefixedName("cm:auditable");
		aspectArray[2] = new Aspect();
		aspectArray[2].setPrefixedName("sys:referenceable");
		aspectArray[3] = new Aspect();
		aspectArray[3].setPrefixedName("cm:author");
		//aggiungo l'aspect ecm-sys:state a quelli gia` posseduti dal content
		aspectArray[4] = new Aspect();
		aspectArray[4].setModelPrefixedName("ecm-sys:ecmengineSystemModel");
		aspectArray[4].setPrefixedName("ecm-sys:state");
		aspectArray[4].setProperties(propArray);

		Content newContent = new Content();
		newContent.setPrefixedName("cm:generic_content.pdf");
		//newContent.setContentPropertyPrefixedName("cm:content");
		//newContent.setProperties(propArray);
		newContent.setAspects(aspectArray);

		SearchParams xpath = new SearchParams();
		String xquery="/app:company_home/cm:"+folder+"/cm:"+contenuto;

		xpath.setXPathQuery(xquery);
		xpath.setLimit(1);

		start();

		try {
			ecmEngineDelegateImpl.updateMetadata(documento, newContent, defaultContext);

			dumpElapsed("TestEngine", "testUpdateMetadata", logCtx, "chiamata al servizio updateMetadata");
			log.debug("[TestEngine::testUpdateMetadata] Aggiornato meta dati content; add aspect: ecm-sys:state");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = ecmEngineDelegateImpl.getUid(xpath, defaultContext);
			dumpElapsed("TestEngine", "testUpdateMetadata", logCtx, "chiamata al servizio getUid");

			ResultContent resultContent = ecmEngineDelegateImpl.getContentMetadata(contentNode, defaultContext);

			ResultAspect[] aspects = resultContent.getAspects();
			if(aspects!=null && aspects.length > 0){
				for(int i=0;i<aspects.length;i++) {
					if(aspects[i]!=null
							&& "ecm-sys:state".equalsIgnoreCase(aspects[i].getPrefixedName())){
						log.debug("found aspect: ecm-sys:state");
						ResultProperty[] props = resultContent.getProperties();
						for(int j=0;j<props.length;j++) {
							log.debug(" >> "+props[j].getPrefixedName()+"="+props[j].getValues()[0]);
							if(props[j]!=null
									&& "ecm-sys:stato".equalsIgnoreCase(props[j].getPrefixedName())
									&& "normale".equalsIgnoreCase(props[j].getValues()[0])){
								log.debug("found property: ecm-sys:stato");
								assertTrue(true);
								return;
							}
						}
					}
				}
			} else {
				throw new Exception("No aspects found");
			}

			assertTrue(false);
		} catch (Exception e) {
			log.debug("[TestEngine::testUpdateMetadata] Eccezione vale :"+e, e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testUpdateMetadata] END");
		}
	}



	/**
	 * Test per la verifica del servizio di CheckOut
	 * di un contenuto presente nel repository.
	 */

	public void testCheckOutContent(){
		log.debug("[TestEngine::testCheckOutContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione CheckOutContent";

		Node documento = new Node();
		documento.setUid(uidDocument);

		Node workingCopy = null;

		start();

		try {
			workingCopy = ecmEngineDelegateImpl.checkOutContent(documento, defaultContext);
			dumpElapsed("TestEngine", "testCheckOutContent", logCtx, "chiamata al servizio checkOutContent");
			log.debug("[TestEngine::testCheckOutContent] Id del nodo workingCopy: " +workingCopy.getUid());


			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try{
				//si prova a fare un secondo check out sullo stesso contenuto
				ecmEngineDelegateImpl.checkOutContent(documento, defaultContext);
			}catch (CheckInCheckOutException e) {
				log.debug("[TestEngine::testCheckOutContent] Seconda chiamata al " +
						"servizio di checkOutContent sullo stesso contentuto.");
				log.debug("[TestEngine::testCheckOutContent] Scatenata CheckInCheckOutException: OK");

				assertTrue(true);
				return;
			}
			assertTrue(false);

		} catch (Exception e) {
			log.debug("[TestEngine::testCheckOutContent] Eccezione", e);
			assertTrue(false);
		} finally{

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			try {
				ecmEngineDelegateImpl.checkInContent(workingCopy, defaultContext);
				log.debug("[TestEngine::testCheckOutContent] Eseguito checkin del nodo di origine.");
			} catch (Exception e) {
				log.debug("[TestEngine::testCheckOutContent] Eccezione nel checkInContent", e);
				assertTrue(false);
			}

			stop();
			log.debug("[TestEngine::testCheckOutContent] END");
		}
	}

	/**
	 * Test per la verifica del servizio di CheckIn
	 * di un contenuto presente nel repository.
	 */

	public void testCheckInContent(){
		log.debug("[TestEngine::testCheckInContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione CheckInContent";

		Node documento = new Node();
		documento.setUid(uidDocument);

		Node wcNode = null;
		Node resultNode = null;

		start();

		try {
			wcNode = ecmEngineDelegateImpl.checkOutContent(documento, defaultContext);
			dumpElapsed("TestEngine", "testCheckInContent", logCtx, "chiamata al servizio checkOutContent");
			log.debug("[TestEngine::testCheckInContent] Id nodo WC: " +wcNode.getUid());


			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			resultNode = ecmEngineDelegateImpl.checkInContent(wcNode, defaultContext);
			dumpElapsed("TestEngine", "testCheckInContent", logCtx, "chiamata al servizio checkInContent");
			log.debug("[TestEngine::testCheckInContent] Id nodo del checkIn : " +resultNode.getUid());


			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			try{
				//si prova a fare un secondo check in su un contenuto gia sottoposto a checkin
				resultNode = ecmEngineDelegateImpl.checkInContent(documento, defaultContext);
			}catch (InvalidParameterException e) {
				log.debug("[TestEngine::testCheckInContent] Seconda chiamata al " +
						"servizio di checkInContent su un contentuto gia` sottoposto a checkin.");
				log.debug("[TestEngine::testCheckInContent] Scatenata InvalidParameterException: OK");
				assertTrue(true);
				return;
			}
			assertTrue(false);

		} catch (Exception e) {
			log.debug("[TestEngine::testCheckInContent] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testCheckInContent] END");
		}
	}

	public void testCheckOutInContent(){

		log.debug("[TestEngine::testCheckOutInContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione testCheckOutInContent";

		Node documento = new Node();
		documento.setUid(uidDocument);

		Node wcNode = null;
		Node resultNode = null;

		start();

		try {
			wcNode = ecmEngineDelegateImpl.checkOutContent(documento, defaultContext);
			dumpElapsed("TestEngine", "testCheckOutInContent", logCtx, "chiamata al servizio checkOutContent");
			log.debug("[TestEngine::testCheckOutInContent] Id nodo WC: " +wcNode.getUid());


			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Content content = new Content();
			//cm:generic_content (Working Copy).pdf
			//content.setPrefixedName("cm:generic_content (Working Copy).pdf");
			content.setPrefixedName("cm:generic_content.pdf");
			content.setContentPropertyPrefixedName("cm:content");

			//byte[] doc = ecmEngineDelegateImpl.retrieveContentData(wcNode, content, ctx);
			byte[] doc = ecmEngineDelegateImpl.retrieveContentData(documento, content, defaultContext);

			if(doc!=null){
				log.debug("[TestEngine::testCheckOutInContent] retrieveContentData eseguita correttamente");
			}

			resultNode = ecmEngineDelegateImpl.checkInContent(wcNode, defaultContext);
			dumpElapsed("TestEngine", "testCheckOutInContent", logCtx, "chiamata al servizio checkInContent");
			log.debug("[TestEngine::testCheckOutInContent] Id nodo del checkIn : " +resultNode.getUid());

			if(resultNode.getUid().equalsIgnoreCase(documento.getUid())){
				log.debug("[TestEngine::testCheckOutInContent] Id nodo del checkin coincide con l'id " +
				"del nodo originario");
			}
			if(resultNode.getUid().equalsIgnoreCase(documento.getUid()) && doc!=null){
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (Exception e) {
			log.debug("[TestEngine::testCheckOutInContent] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testCheckOutInContent] END");
		}
	}

	public void testCancelCheckOutContent(){

		log.debug("[TestEngine::testCancelCheckOutContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione testCancelCheckOutContent";

		Node documento = new Node();
		documento.setUid(uidDocument);

		Node wcNode = null;
		Node resultNode = null;

		start();

		try {
			wcNode = ecmEngineDelegateImpl.checkOutContent(documento, defaultContext);
			dumpElapsed("TestEngine", "testCancelCheckOutContent", logCtx, "chiamata al servizio checkOutContent");
			log.debug("[TestEngine::testCancelCheckOutContent] Id nodo WC: " +wcNode.getUid());

			Content content = new Content();
			//cm:generic_content (Working Copy).pdf
			//content.setPrefixedName("cm:generic_content (Working Copy).pdf");
			content.setPrefixedName("cm:generic_content.pdf");
			content.setContentPropertyPrefixedName("cm:content");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			//byte[] doc = ecmEngineDelegateImpl.retrieveContentData(wcNode, content, ctx);
			byte[] doc = ecmEngineDelegateImpl.retrieveContentData(documento, content, defaultContext);

			if(doc!=null){
				log.debug("[TestEngine::testCancelCheckOutContent] retrieveContentData eseguita correttamente");
			}

			resultNode = ecmEngineDelegateImpl.cancelCheckOutContent(wcNode, defaultContext);
			dumpElapsed("TestEngine", "testCancelCheckOutContent", logCtx, "chiamata al servizio cancelCheckOutContent");
			log.debug("[TestEngine::testCancelCheckOutContent] Id nodo originale dopo anullamento checkout : " +resultNode.getUid());

			if(resultNode.getUid().equalsIgnoreCase(documento.getUid())){
				log.debug("[TestEngine::testCancelCheckOutContent] Id nodo coincide con l'id " +
				"del nodo originario");
			}
			if(resultNode.getUid().equalsIgnoreCase(documento.getUid()) && doc!=null){
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (Exception e) {
			log.debug("[TestEngine::testCancelCheckOutContent] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testCancelCheckOutContent] END");
		}
	}

	public void testDeleteContent(){
		log.debug("[TestEngine::testDeleteContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione deleteContent";

		SearchParams xpath = new SearchParams();
		xpath.setLimit(1);
		String path="/app:company_home/cm:"+contenuto;
		xpath.setXPathQuery(path);

		Node node = null;

		start();

		try {

			 String uidCompanyHome = this.getUidCompanyHome();
			 log.debug("[TestEngine::testDeleteContent] letto uid company_home: "+uidCompanyHome);

			 String uidContent = this.insertDocument(uidCompanyHome, contenuto, "application/pdf", "UTF-8", null, defaultContext);
			 log.debug("[TestEngine::testDeleteContent] caricato content "+ contenuto
					 +" sotto la company_home . Uid content: "+uidContent);

				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e1) {}

			 String uid =ecmEngineDelegateImpl.nodeExists(xpath, defaultContext);
			 dumpElapsed("TestEngine", "testDeleteContent", logCtx, "chiamata al servizio nodeExists");
			 log.debug("[TestEngine::testDeleteContent] chiamata al servizio nodeExists; Uid : "+uid);

			 node = createNodeDTO(uidContent);

			 ecmEngineDelegateImpl.deleteContent(node, defaultContext);
			 dumpElapsed("TestEngine", "testDeleteContent", logCtx, "chiamata al servizio deleteContent");
			 log.debug("[TestEngine::testDeleteContent] chiamata al servizio deleteContent");

				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e1) {}

			 try{
				 ecmEngineDelegateImpl.nodeExists(xpath, defaultContext);
				 assertTrue(false);
			 }catch (NoDataExtractedException e) {
				log.debug("[TestEngine::testDeleteContent] Seconda chiamata al servizio nodeExists.");
				log.debug("[TestEngine::testDeleteContent] Scatenata NoDataExtractedException: OK");
				assertTrue(true);
				return;
			 }

		} catch (Exception e) {
			log.debug("[TestEngine::testDeleteContent] Eccezione", e);
			assertTrue(false);
		} finally {
			stop();
			//impostare la variabile down a true solo nell'ultimo metodo di test
			// in modo che i nodi creati vengano tutti eliminati solo alla fine dei test
			down = true;
			log.debug("[TestEngine::testDeleteContent] END");
		}
	}

	public void testGetUid(){

		log.debug("[TestEngine::testGetUid] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit ricerca nodo presente.";

		SearchParams xpath = new SearchParams();
		xpath.setXPathQuery("/app:company_home/cm:"+folder);
		xpath.setLimit(1);

		Node node = null;
		start();

		try {
			node = ecmEngineDelegateImpl.getUid(xpath, defaultContext);

			dumpElapsed("TestEngine", "testGetUid", logCtx, "Chiamata a getUid");

			log.debug("[TestEngine::testGetUid] Risultato: "+node.getUid());
			if (node.getUid().equals(uidFolder)) {
				log.debug("[TestEngine::testGetUid] Id nodo coincide con l'id del nodo originario");
				assertTrue(true);
			} else {
				log.debug("[TestEngine::testGetUid] Id nodo NON coincide con l'id del nodo originario");
				assertTrue(false);
			}
		} catch (Exception e) {
			log.debug("[TestEngine::testGetUid] Eccezione", e);
			assertTrue(false);
		} finally {
			stop();
		}

	}

	public void testAddSimpleWorkflowRule() {
		log.debug("[TestEngine::testAddSimpleWorkflowRule] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit impostazione regola workflow semplice.";

		start();

		try {
			String workflowFolderName = "add_workflow_rule";
			String workflowFolderUid = createFolder(uidFolder, workflowFolderName, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			String acceptFolderUid = createFolder(workflowFolderUid, "accepted", defaultContext);
			String rejectFolderUid = createFolder(workflowFolderUid, "rejected", defaultContext);

			dumpElapsed("TestEngine", "testAddSimpleWorkflowRule", logCtx, "Creazione folder workflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SimpleWorkflow workflow = new SimpleWorkflow();
			Node acceptFolderNode = createNodeDTO(acceptFolderUid);
			Node rejectFolderNode = createNodeDTO(rejectFolderUid);
			workflow.setApproveNode(acceptFolderNode);
			workflow.setRejectNode(rejectFolderNode);
			workflow.setMoveContent(true);

			Node folderNode = createNodeDTO(workflowFolderUid);
			Rule rule = new Rule();
			rule.setApplyToChildren(false);
			rule.setType(EcmEngineConstants.ECMENGINE_RULE_TYPE_INBOUND);
			ecmEngineDelegateImpl.addSimpleWorkflowRule(folderNode, workflow, rule, defaultContext);

			dumpElapsed("TestEngine", "testAddSimpleWorkflowRule", logCtx, "Chiamata a addSimpleWorkflowRule");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Verifica esistenza rule
			SearchParams params = new SearchParams();
			params.setXPathQuery("/app:company_home/cm:"+folder+"/cm:"+workflowFolderName+"/rule:ruleFolder");
			SearchResponse response = ecmEngineDelegateImpl.xpathSearch(params, defaultContext);

			dumpElapsed("TestEngine", "testAddSimpleWorkflowRule", logCtx, "Chiamata a xpathSearch");

			ResultContent[] resultContentArray = response.getResultContentArray();
			if (resultContentArray != null && resultContentArray.length == 1) {
				log.debug("[TestEngine::testAddSimpleWorkflowRule] Rule presente: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testAddSimpleWorkflowRule] Rule NON presente: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testAddSimpleWorkflowRule] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testAddSimpleWorkflowRule] END");
		}
	}

	public void testStartSimpleWorkflow() {
		log.debug("[TestEngine::testStartSimpleWorkflow] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit avvio workflow semplice.";

		start();

		try {
			String workflowFolderName = "start_workflow";
			String workflowFolderUid = createFolder(uidFolder, workflowFolderName, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			String acceptFolderUid = createFolder(workflowFolderUid, "accepted", defaultContext);
			String rejectFolderUid = createFolder(workflowFolderUid, "rejected", defaultContext);

			dumpElapsed("TestEngine", "testStartSimpleWorkflow", logCtx, "Creazione folder workflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SimpleWorkflow workflow = new SimpleWorkflow();
			Node acceptFolderNode = createNodeDTO(acceptFolderUid);
			Node rejectFolderNode = createNodeDTO(rejectFolderUid);
			workflow.setApproveNode(acceptFolderNode);
			workflow.setRejectNode(rejectFolderNode);
			workflow.setMoveContent(true);

			Node folderNode = createNodeDTO(workflowFolderUid);
			String contentUid = insertDocument(workflowFolderUid, contenuto, "application/pdf", "UTF-8", null, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);
			ecmEngineDelegateImpl.startSimpleWorkflow(contentNode, workflow, defaultContext);

			dumpElapsed("TestEngine", "testStartSimpleWorkflow", logCtx, "Chiamata a startSimpleWorkflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Verifica applicazione workflow
			SearchParams params = new SearchParams();
			params.setLuceneQuery("PATH:\"/app:company_home/cm:"+folder+"/cm:"+workflowFolderName+"/*\" AND ASPECT:\"{http://www.alfresco.org/model/application/1.0}simpleworkflow\"");
			SearchResponse response = ecmEngineDelegateImpl.luceneSearch(params, defaultContext);

			dumpElapsed("TestEngine", "testStartSimpleWorkflow", logCtx, "Chiamata a luceneSearch");

			ResultContent[] resultContentArray = response.getResultContentArray();
			if (resultContentArray != null && resultContentArray.length == 1) {
				log.debug("[TestEngine::testStartSimpleWorkflow] Aspect app:simpleworkflow presente: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testStartSimpleWorkflow] Aspect app:simpleworkflow NON presente: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testStartSimpleWorkflow] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testStartSimpleWorkflow] END");
		}

	}

	public void testApproveContent() {
		log.debug("[TestEngine::testApproveContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit approvazione contenuto.";

		start();

		try {
			String workflowFolderName = "approve_content";
			String workflowFolderUid = createFolder(uidFolder, workflowFolderName, defaultContext);

			String acceptFolderUid = createFolder(workflowFolderUid, "accepted", defaultContext);
			String rejectFolderUid = createFolder(workflowFolderUid, "rejected", defaultContext);

			dumpElapsed("TestEngine", "testApproveContent", logCtx, "Creazione folder workflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SimpleWorkflow workflow = new SimpleWorkflow();
			Node acceptFolderNode = createNodeDTO(acceptFolderUid);
			Node rejectFolderNode = createNodeDTO(rejectFolderUid);
			workflow.setApproveNode(acceptFolderNode);
			workflow.setRejectNode(rejectFolderNode);
			workflow.setMoveContent(true);

			Node folderNode = createNodeDTO(workflowFolderUid);
			String contentUid = insertDocument(workflowFolderUid, contenuto, "application/pdf", "UTF-8", null, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);
			ecmEngineDelegateImpl.startSimpleWorkflow(contentNode, workflow, defaultContext);

			dumpElapsed("TestEngine", "testApproveContent", logCtx, "Chiamata a startSimpleWorkflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Verifica approvazione
			ecmEngineDelegateImpl.approveContent(contentNode, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SearchParams params = new SearchParams();
			params.setXPathQuery("/app:company_home/cm:"+folder+"/cm:"+workflowFolderName+"/cm:accepted/*");
			SearchResponse response = ecmEngineDelegateImpl.xpathSearch(params, defaultContext);

			dumpElapsed("TestEngine", "testApproveContent", logCtx, "Chiamata a xpathSearch");

			ResultContent[] resultContentArray = response.getResultContentArray();
			if (resultContentArray != null && resultContentArray.length == 1) {
				log.debug("[TestEngine::testApproveContent] Contenuto presente in 'accepted': OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testApproveContent] Contenuto NON presente in 'accepted': ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testApproveContent] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testApproveContent] END");
		}

	}

	public void testRejectContent() {
		log.debug("[TestEngine::testRejectContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit rifiuto contenuto.";

		start();

		try {
			String workflowFolderName = "reject_content";
			String workflowFolderUid = createFolder(uidFolder, workflowFolderName, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			String acceptFolderUid = createFolder(workflowFolderUid, "accepted", defaultContext);
			String rejectFolderUid = createFolder(workflowFolderUid, "rejected", defaultContext);

			dumpElapsed("TestEngine", "testRejectContent", logCtx, "Creazione folder workflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SimpleWorkflow workflow = new SimpleWorkflow();
			Node acceptFolderNode = createNodeDTO(acceptFolderUid);
			Node rejectFolderNode = createNodeDTO(rejectFolderUid);
			workflow.setApproveNode(acceptFolderNode);
			workflow.setRejectNode(rejectFolderNode);
			workflow.setMoveContent(true);

			Node folderNode = createNodeDTO(workflowFolderUid);
			String contentUid = insertDocument(workflowFolderUid, contenuto, "application/pdf", "UTF-8", null, defaultContext);
			Node contentNode = createNodeDTO(contentUid);
			ecmEngineDelegateImpl.startSimpleWorkflow(contentNode, workflow, defaultContext);

			dumpElapsed("TestEngine", "testRejectContent", logCtx, "Chiamata a startSimpleWorkflow");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Verifica rifiuto
			ecmEngineDelegateImpl.rejectContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testRejectContent", logCtx, "Chiamata a rejectContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			SearchParams params = new SearchParams();
			params.setXPathQuery("/app:company_home/cm:"+folder+"/cm:"+workflowFolderName+"/cm:rejected/*");
			SearchResponse response = ecmEngineDelegateImpl.xpathSearch(params, defaultContext);

			dumpElapsed("TestEngine", "testRejectContent", logCtx, "Chiamata a xpathSearch");

			ResultContent[] resultContentArray = response.getResultContentArray();
			if (resultContentArray != null && resultContentArray.length == 1) {
				log.debug("[TestEngine::testRejectContent] Contenuto presente in 'rejected': OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testRejectContent] Contenuto NON presente in 'rejected': ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testRejectContent] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testRejectContent] END");
		}

	}

	public void testGetTotalResults() {
		log.debug("[TestEngine::testGetTotalResults] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit getTotalResults.";

		start();

		try {
			String workFolderName = "total_results";
			String workFolderUid = createFolder(uidFolder, workFolderName, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			String folder1Uid = createFolder(workFolderUid, "folder1", defaultContext);
			String folder2Uid = createFolder(workFolderUid, "folder2", defaultContext);
			String folder3Uid = createFolder(workFolderUid, "folder3", defaultContext);

			dumpElapsed("TestEngine", "testGetTotalResults", logCtx, "Creazione folder");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Verifica
			SearchParams params = new SearchParams();
			params.setXPathQuery("/app:company_home/cm:"+folder+"/cm:"+workFolderName+"/*");
			int resultCount = ecmEngineDelegateImpl.getTotalResults(params, defaultContext);

			dumpElapsed("TestEngine", "testGetTotalResults", logCtx, "Chiamata a getTotalResults");

			if (resultCount == 3) {
				log.debug("[TestEngine::testGetTotalResults] Il numero di risultati coincide con il numero di contenuti creati: OK");
				assertTrue(true);
			} else {
				log.debug("[TestEngine::testGetTotalResults] Il numero di risultati NON coincide con il numero di contenuti creati: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testGetTotalResults] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetTotalResults] END");
		}

	}

	public void testGetAssociations() {
		log.debug("[TestEngine::testGetAssociations] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit getAssociations.";

		start();

		try {
			String workFolderName = "get_associations";
			String workFolderUid = createFolder(uidFolder, workFolderName, defaultContext);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node workFolderNode = new Node();
			workFolderNode.setUid(workFolderUid);

			String folder1Uid = createFolder(workFolderUid, "folder1", defaultContext);
			String folder2Uid = createFolder(workFolderUid, "folder2", defaultContext);
			String folder3Uid = createFolder(workFolderUid, "folder3", defaultContext);

			dumpElapsed("TestEngine", "testGetAssociations", logCtx, "Creazione folder");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Verifica associazioni
			ResultAssociation[] assocs = ecmEngineDelegateImpl.getAssociations(workFolderNode, EcmEngineConstants.ECMENGINE_ASSOC_TYPE_CHILD, 0, defaultContext);

			dumpElapsed("TestEngine", "testGetAssociations", logCtx, "Chiamata a getAssociations");

			boolean ok = true;
			if (assocs != null && assocs.length == 3) {
				for (int i=0; i<assocs.length; i++) {
					if (assocs[i].getPrefixedName().equals("cm:folder1")) {
						ok = ok && true;
					} else if (assocs[i].getPrefixedName().equals("cm:folder2")) {
						ok = ok && true;
					} else if (assocs[i].getPrefixedName().equals("cm:folder3")) {
						ok = ok && true;
					} else {
						ok = ok && false;
					}
				}
			} else {
				ok = false;
			}
			if (ok) {
				log.debug("[TestEngine::testGetAssociations] Le associazioni caricate coincidono con quelle create: OK");
				assertTrue(true);
			} else {
				log.debug("[TestEngine::testGetAssociations] Le associazioni caricate NON coincidono con quelle create: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testGetAssociations] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetAssociations] END");
		}

	}

	public void testCheckEncryption() {
		log.debug("[TestEngine::testCheckEncryption] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit checkEncryption.";

		start();

		try {
			String workFolderName = "check_encryption";
			String workFolderUid = createFolder(uidFolder, workFolderName, defaultContext);
			dumpElapsed("TestEngine", "testCheckEncryption", logCtx, "Creazione folder di lavoro");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			String key = "0123456789ABCDEF";
			EncryptionInfo encInfo = new EncryptionInfo();
			encInfo.setSourceEncrypted(false);
			encInfo.setAlgorithm("AES");
			encInfo.setMode("CBC");
			encInfo.setPadding("PKCS5Padding");
			encInfo.setKey(key);
			encInfo.setKeyId(key);

			String contentUid = insertDocument(workFolderUid, contenuto, "application/pdf", "UTF-8", encInfo, defaultContext);
			dumpElapsed("TestEngine", "testCheckEncryption", logCtx, "Creazione contenuto criptato");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = new Node();
			contentNode.setUid(contentUid);
			EncryptionInfo newEncInfo = ecmEngineDelegateImpl.checkEncryption(contentNode, defaultContext);

			dumpElapsed("TestEngine", "testCheckEncryption", logCtx, "Verifica cifratura");

			if (newEncInfo != null
					&& newEncInfo.getAlgorithm().equals(encInfo.getAlgorithm())
					&& newEncInfo.getMode().equals(encInfo.getMode())
					&& newEncInfo.getPadding().equals(encInfo.getPadding())) {
				log.debug("[TestEngine::testCheckEncryption] Cifratura verificata: OK");
				assertTrue(true);
			} else {
				log.debug("[TestEngine::testCheckEncryption] Cifratura errata: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testCheckEncryption] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testCheckEncryption] END");
		}

	}

	public void testGetVersion() {
		log.debug("[TestEngine::testGetVersion] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit get version.";

		start();

		final String contentName = "test.txt";
		final String version1Text = "Prima versione (1.0)\n";
		final String version2Text = "Seconda versione (1.1)\n";
		final String version1Description = "Prima versione";
		final String version2Description = "Seconda versione";
		try {
			String testFolderName = "get_version";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[2];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			props[1] = createPropertyDTO("cm:description", "text", false);
			props[1].setValues(new String [] { version1Description });
			String contentUid = createTextDocument(testFolderUid, version1Text, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			Node workingCopyNode = ecmEngineDelegateImpl.checkOutContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Chiamata a checkOutContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Content content = new Content();
			content.setPrefixedName("cm:" + contentName);
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setMimeType("text/plain");
			content.setEncoding("UTF-8");
			content.setContent(version2Text.getBytes());
			ecmEngineDelegateImpl.updateContentData(workingCopyNode, content, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Chiamata a updateContentData");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Property [] tmpProps = new Property[1];
			tmpProps[0] = createPropertyDTO("cm:description", "text", false);
			tmpProps[0].setValues(new String [] { version2Description });
			content.setProperties(tmpProps);
			ResultContent tmpContent = ecmEngineDelegateImpl.getContentMetadata(workingCopyNode, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Chiamata a getContentMetadata");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Aspect[] aspects = new Aspect[tmpContent.getAspects().length];
			for (int i=0; i<aspects.length; i++) {
				aspects[i] = new Aspect();
				aspects[i].setPrefixedName(tmpContent.getAspects()[i].getPrefixedName());
				aspects[i].setModelPrefixedName("cm:contentmodel");
			}
			content.setContentPropertyPrefixedName(null);
			content.setMimeType(null);
			content.setEncoding(null);
			content.setAspects(aspects);
			content.setContent(null);
			ecmEngineDelegateImpl.updateMetadata(workingCopyNode, content, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Chiamata a updateMetadata");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ecmEngineDelegateImpl.checkInContent(workingCopyNode, defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Chiamata a checkInContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Version firstVersion = ecmEngineDelegateImpl.getVersion(contentNode, "1.0", defaultContext);
			dumpElapsed("TestEngine", "testGetVersion", logCtx, "Chiamata a getVersion");

			if (firstVersion != null && firstVersion.getVersionedNode() != null) {
				log.debug("[TestEngine::testGetVersion] Trovata versione "+firstVersion.getVersionLabel()+" con uid "+firstVersion.getVersionedNode().getUid()+": OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testGetVersion] Versione NON presente: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testGetVersion] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetVersion] END");
		}

	}

	public void testRetrieveVersionContentData() {
		log.debug("[TestEngine::testRetrieveVersionContentData] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit retrieve version content data.";

		start();

		final String contentName = "test.txt";
		final String version1Text = "Prima versione (1.0)\n";
		final String version2Text = "Seconda versione (1.1)\n";
		try {
			String testFolderName = "retrieve_version_content_data";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			String contentUid = createTextDocument(testFolderUid, version1Text, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			Node workingCopyNode = ecmEngineDelegateImpl.checkOutContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Chiamata a checkOutContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Content content = new Content();
			content.setPrefixedName("cm:" + contentName);
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setMimeType("text/plain");
			content.setEncoding("UTF-8");
			content.setContent(version2Text.getBytes());
			ecmEngineDelegateImpl.updateContentData(workingCopyNode, content, defaultContext);
			dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Chiamata a updateContentData");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ecmEngineDelegateImpl.checkInContent(workingCopyNode, defaultContext);
			dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Chiamata a checkInContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Version firstVersion = ecmEngineDelegateImpl.getVersion(contentNode, "1.0", defaultContext);
			dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Chiamata a getVersion");

			log.debug("got version: "+firstVersion.getVersionLabel()+", "+firstVersion.getVersionedNode().getUid());
			if (firstVersion != null && firstVersion.getVersionedNode() != null) {
				byte[] versionData = ecmEngineDelegateImpl.retrieveVersionContentData(firstVersion.getVersionedNode(), content, defaultContext);
				dumpElapsed("TestEngine", "testRetrieveVersionContentData", logCtx, "Chiamata a retrieveVersionContentData");
				String versionDataText = new String(versionData);
				if (version1Text.equals(versionDataText)) {
					log.debug("[TestEngine::testRetrieveVersionContentData] Versione ritrovata: OK");
					assertTrue(true);
				} else {
					log.error("[TestEngine::testRetrieveVersionContentData] La versione trovata non coincide con quella attesa: ERRORE");
					assertTrue(false);
				}
			} else {
				log.error("[TestEngine::testRetrieveVersionContentData] Versione NON presente: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testRetrieveVersionContentData] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testRetrieveVersionContentData] END");
		}

	}

	public void testGetAllVersions() {
		log.debug("[TestEngine::testGetAllVersions] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit get all versions.";

		start();

		final String contentName = "test.txt";
		final String version1Text = "Prima versione (1.0)\n";
		final String version2Text = "Seconda versione (1.1)\n";
		final String version1Description = "Prima versione";
		final String version2Description = "Seconda versione";
		try {
			String testFolderName = "get_all_versions";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[2];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			props[1] = createPropertyDTO("cm:description", "text", false);
			props[1].setValues(new String [] { version1Description });
			String contentUid = createTextDocument(testFolderUid, version1Text, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			Node workingCopyNode = ecmEngineDelegateImpl.checkOutContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Chiamata a checkOutContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Content content = new Content();
			content.setPrefixedName("cm:" + contentName);
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setMimeType("text/plain");
			content.setEncoding("UTF-8");
			content.setContent(version2Text.getBytes());
			ecmEngineDelegateImpl.updateContentData(workingCopyNode, content, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Chiamata a updateContentData");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Property [] tmpProps = new Property[1];
			tmpProps[0] = createPropertyDTO("cm:description", "text", false);
			tmpProps[0].setValues(new String [] { version2Description });
			content.setProperties(tmpProps);
			ResultContent tmpContent = ecmEngineDelegateImpl.getContentMetadata(workingCopyNode, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Chiamata a getContentMetadata");

			Aspect[] aspects = new Aspect[tmpContent.getAspects().length];
			for (int i=0; i<aspects.length; i++) {
				aspects[i] = new Aspect();
				aspects[i].setPrefixedName(tmpContent.getAspects()[i].getPrefixedName());
				aspects[i].setModelPrefixedName("cm:contentmodel");
			}
			content.setContentPropertyPrefixedName(null);
			content.setMimeType(null);
			content.setEncoding(null);
			content.setAspects(aspects);
			content.setContent(null);
			ecmEngineDelegateImpl.updateMetadata(workingCopyNode, content, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Chiamata a updateMetadata");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ecmEngineDelegateImpl.checkInContent(workingCopyNode, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Chiamata a checkInContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Version[] versions = ecmEngineDelegateImpl.getAllVersions(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testGetAllVersions", logCtx, "Chiamata a getAllVersions");

			for (Version v : versions) {
				log.debug("[TestEngine::testGetAllVersions] found version: "+v.getVersionLabel());
			}
			if (versions != null && versions.length == 2) {
				log.debug("[TestEngine::testGetAllVersions] Trovate "+versions.length+" versioni: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testGetAllVersions] Le versioni recuperate non coincidono con quelle attese: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testGetAllVersions] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetAllVersions] END");
		}

	}

	public void testGetWorkingCopy() {
		log.debug("[TestEngine::testGetWorkingCopy] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit get working copy.";

		start();

		final String contentName = "test.txt";
		final String contentText = "Prima versione (1.0)\n";
		try {
			String testFolderName = "get_working_copy";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testGetWorkingCopy", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			String contentUid = createTextDocument(testFolderUid, contentText, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testGetWorkingCopy", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			Node workingCopyNode = ecmEngineDelegateImpl.checkOutContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testGetWorkingCopy", logCtx, "Chiamata a checkOutContent");

			log.debug("checked out working copy: "+workingCopyNode.getUid()+" ["+defaultContext.getRepository()+"]");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node retrievedWorkingCopyNode = ecmEngineDelegateImpl.getWorkingCopy(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testGetWorkingCopy", logCtx, "Chiamata a getWorkingCopy");

			log.debug("retrieved working copy: "+retrievedWorkingCopyNode.getUid()+" ["+defaultContext.getRepository()+"]");

			log.debug("workingCopyNode: "+workingCopyNode.getUid()+", retrievedWorkingCopyNode: "
					+ (retrievedWorkingCopyNode != null ? retrievedWorkingCopyNode.getUid() : null));

			if (retrievedWorkingCopyNode != null
					&& retrievedWorkingCopyNode.getUid().equals(workingCopyNode.getUid())) {
				log.debug("[TestEngine::testGetWorkingCopy] La working copy coincide con quella attesa: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testGetWorkingCopy] La working copy recuperata non coincidono con quella attesa: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testGetWorkingCopy] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetWorkingCopy] END");
		}

	}

	public void testRevertVersion() {
		log.debug("[TestEngine::testRevertVersion] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit revert version.";

		start();

		final String contentName = "test.txt";
		final String version1Text = "Prima versione (1.0)\n";
		final String version2Text = "Seconda versione (1.1)\n";
		try {
			String testFolderName = "revert_version";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			String contentUid = createTextDocument(testFolderUid, version1Text, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			// Eseguo il checkout del nodo
			Node workingCopyNode = ecmEngineDelegateImpl.checkOutContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Chiamata a checkOutContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Aggiorno il contenuto ed eseguo il checkin
			Content content = new Content();
			content.setPrefixedName("cm:" + contentName);
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setMimeType("text/plain");
			content.setEncoding("UTF-8");
			content.setContent(version2Text.getBytes());
			ecmEngineDelegateImpl.updateContentData(workingCopyNode, content, defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Chiamata a updateContentData");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ecmEngineDelegateImpl.checkInContent(workingCopyNode, defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Chiamata a checkInContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Imposto come versione corrente la versione 1.0
			ecmEngineDelegateImpl.revertVersion(contentNode, "1.0", defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Chiamata a revertVersion");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			// Leggo il contenuto del nodo originale, che dovrebbe essere
			// quello della versione 1.0
			byte[] versionData = ecmEngineDelegateImpl.retrieveContentData(contentNode, content, defaultContext);
			dumpElapsed("TestEngine", "testRevertVersion", logCtx, "Chiamata a retrieveContentData");

			String versionDataText = new String(versionData);
			if (version1Text.equals(versionDataText)) {
				log.debug("[TestEngine::testRevertVersion] Versione impostata correttamente: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testRevertVersion] La versione trovata non coincide con quella attesa: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testRevertVersion] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testRevertVersion] END");
		}

	}

	public void testGetContentMetadata() {
		log.debug("[TestEngine::testGetContentMetadata] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit get content metadata.";

		start();

		final String contentName = "test.txt";
		final String contentText = "Testo contenuto\n";
		final String contentDescription = "Descrizione contenuto";

		try {
			String testFolderName = "get_content_metadata";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testGetContentMetadata", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[2];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			props[1] = createPropertyDTO("cm:description", "text", false);
			props[1].setValues(new String [] { contentDescription });
			String contentUid = createTextDocument(testFolderUid, contentText, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testGetContentMetadata", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			ResultContent resultContent = ecmEngineDelegateImpl.getContentMetadata(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testGetContentMetadata", logCtx, "Chiamata a getContentMetadata");

			String description = null;
			for (ResultProperty prop : resultContent.getProperties()) {
				if (prop.getPrefixedName().equals("cm:description")) {
					description = prop.getValues()[0];
				}
			}
			log.debug("contentDescription: "+contentDescription+", retrievedDescription: "+description);
			if (contentDescription.equals(description)) {
				log.debug("[TestEngine::testGetContentMetadata] Metadati verificati: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testGetContentMetadata] I metadati recuperati non coincidono con quelli attesi: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testGetContentMetadata] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testGetContentMetadata] END");
		}

	}

	public void testRestoreContent() {
		log.debug("[TestEngine::testRestoreContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit restore content.";

		start();

		final String contentName = "test.txt";
		final String contentText = "Testo contenuto\n";
		final String contentDescription = "Descrizione contenuto";

		try {
			String testFolderName = "restore_content";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testRestoreContent", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[2];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			props[1] = createPropertyDTO("cm:description", "text", false);
			props[1].setValues(new String [] { contentDescription });
			String contentUid = createTextDocument(testFolderUid, contentText, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testRestoreContent", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			ecmEngineDelegateImpl.deleteContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testRestoreContent", logCtx, "Chiamata a deleteContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node restoredNode = ecmEngineDelegateImpl.restoreContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testRestoreContent", logCtx, "Chiamata a restoreContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			try {
				ecmEngineDelegateImpl.getContentMetadata(restoredNode, defaultContext);
				log.debug("[TestEngine::testRestoreContent] Contenuto ripristinato: OK");
				assertTrue(true);
			} catch(NoSuchNodeException nsne) {
				log.error("[TestEngine::testRestoreContent] Il nodo non esiste: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testRestoreContent] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testRestoreContent] END");
		}

	}

	public void testPurgeContent() {
		log.debug("[TestEngine::testPurgeContent] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit purge content.";

		start();

		final String contentName = "test.txt";
		final String contentText = "Testo contenuto\n";
		final String contentDescription = "Descrizione contenuto";

		try {
			String testFolderName = "purge_content";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testPurgeContent", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node testFolderNode = createNodeDTO(testFolderUid);
			Property [] props = new Property[2];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			props[1] = createPropertyDTO("cm:description", "text", false);
			props[1].setValues(new String [] { contentDescription });
			String contentUid = createTextDocument(testFolderUid, contentText, contentName, props, true, defaultContext);
			dumpElapsed("TestEngine", "testPurgeContent", logCtx, "Creazione documento di testo");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node contentNode = createNodeDTO(contentUid);

			ecmEngineDelegateImpl.deleteContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testPurgeContent", logCtx, "Chiamata a deleteContent");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ecmEngineDelegateImpl.purgeContent(contentNode, defaultContext);
			dumpElapsed("TestEngine", "testPurgeContent", logCtx, "Chiamata a purgeContent");

			log.debug("[TestEngine::testPurgeContent] Contenuto eliminato definitivamente: OK");
			assertTrue(true);
		} catch(Exception e) {
			log.error("[TestEngine::testPurgeContent] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testPurgeContent] END");
		}

	}

	public void testMoveAggregation() {
		log.debug("[TestEngine::testMoveAggregation] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Test JUnit move aggregation.";

		start();

		final String sourceFolderName = "source";
		final String destFolderName = "dest";
		try {
			String testFolderName = "move_aggregation";
			String testFolderUid = createFolder(uidFolder, testFolderName, defaultContext);
			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "Creazione folder di test");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			String sourceFolderUid = createFolder(testFolderUid, sourceFolderName, defaultContext);
			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "Creazione folder sorgente");

			String destFolderUid = createFolder(testFolderUid, destFolderName, defaultContext);
			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "Creazione folder destinazione");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			Node sourceNode = createNodeDTO(sourceFolderUid);
			Node destNode = createNodeDTO(destFolderUid);
			ecmEngineDelegateImpl.moveAggregation(sourceNode, destNode, defaultContext);
			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "Chiamata a moveAggregation");

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {}

			ResultContent resultContent = ecmEngineDelegateImpl.getContentMetadata(sourceNode, defaultContext);
			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "Chiamata a getContentMetadata");

			boolean aspectFound = false;
			for (ResultAspect aspect : resultContent.getAspects()) {
				if (aspect.getPrefixedName().equals("ecm-sys:destination")) {
					aspectFound = true;
				}
			}
			if (aspectFound) {
				log.debug("[TestEngine::testMoveAggregation] Aspect impostato correttamente: OK");
				assertTrue(true);
			} else {
				log.error("[TestEngine::testMoveAggregation] Aspect impostato in modo errato: ERRORE");
				assertTrue(false);
			}
		} catch(Exception e) {
			log.error("[TestEngine::testMoveAggregation] Error", e);
			assertTrue(false);
		} finally {
			stop();
			log.debug("[TestEngine::testMoveAggregation] END");
		}

	}

	public void testGetMimeType(){
		final String logCtx="Test JUnit get MIMEType.";
		log.debug("[TestEngine::testGetMimeType] BEGIN");
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}
		try{
			start();
			Mimetype[] mt=null;
			String[] fileext={"pdf","txt","bmp","jpg","tif","xml","zip","tar","tgz"};
			String[] res={"application/pdf","text/plain","image/bmp","image/jpeg","image/tiff","application/xml","application/zip","application/x-tar","application/gnutar"};
			for(int i=0;i<fileext.length;i++){
				Mimetype temp=new Mimetype();
				temp.setFileExtension(fileext[i]);
				mt=ecmEngineDelegateImpl.getMimetype(temp);
				if(mt==null || mt.length==0){
					throw new Exception("Per l'estensione " + fileext[i] + " non sono stati trovati risultati.\nmt: "+mt);
				}
				/*System.out.println("Per l'estensione " + fileext[i] + " mi aspetto il MIMEtype " + res[i] + ".");
				System.out.println("Mimetype trovati l'estensione " + fileext[i] + ":");
				for(int j=0;j<mt.length;j++){
					System.out.println(mt[j].getMimetype());
				}*/
				if(!mt[0].getMimetype().equals(res[i])){
					throw new Exception("All'estensione " + fileext[i] + " non corrisponde il MIMEtype preferito " + res[i] + " ma il MIMEtype " + mt[0].getMimetype()+".");
				}
			}

			assertTrue(true);
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
			assertTrue(false);
		}
		finally{
			dumpElapsed("TestEngine", "testMoveAggregation", logCtx, "Creazione folder di test");
			stop();
			log.debug("[TestEngine::testGetMimeType] END");
		}

	}

	public void testGetFileFormatInfoNode(){
		log.debug("[TestEngine::testGetFileFormatInfoNode] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione getFileFormatInfo";

		SearchParams xpath = new SearchParams();
		xpath.setLimit(1);
		String path="/app:company_home/cm:"+contenuto;
		xpath.setXPathQuery(path);

		Node node = null;

		start();

		try {
			node = createNodeDTO(uidDocument);
			Content content=new Content();
			content.setContentPropertyPrefixedName("cm:content");
			FileFormatInfo[] res=ecmEngineDelegateImpl.getFileFormatInfo(node, content, defaultContext);
			if(res.length==0){
				throw new Exception("Nessun formato trovato.");
			}
			if(res.length!=1){
				throw new Exception("Riconoscimento non univoco. " + res.length + " risultati trovati.");
			}
			if(!res[0].getMimeType().equals("application/pdf")){
				throw new Exception("Riconoscimento errato. Mimetype restituito: " + res[0].getMimeType() + " al posto di application/pdf");
			}
			assertTrue(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.debug("[TestEngine::testGetFileFormatInfoNode] ERROR");
			assertTrue(false);
		}
		finally{
			stop();
			log.debug("[TestEngine::testGetFileFormatInfoNode] END");
		}
	}

	public void testGetFileFormatInfoFileInfo(){
		log.debug("[TestEngine::testGetFileFormatInfoFileInfo] BEGIN");

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

		final String logCtx = "Unit test operazione getFileFormatInfo";

		SearchParams xpath = new SearchParams();
		xpath.setLimit(1);
		String path="/app:company_home/cm:"+contenuto;
		xpath.setXPathQuery(path);

		Node node = null;

		start();
		FileInputStream fis=null;
		try {
			File file=new File("generic_content.pdf");
			fis=new FileInputStream(file);
			FileInfo fileInfo=new FileInfo();
			byte[] buf=new byte[(int)file.length()];
			fis.read(buf);
			fileInfo.setName(file.getName());
			fileInfo.setContents(buf);
			fileInfo.setStore(true);
			FileFormatInfo[] res=ecmEngineDelegateImpl.getFileFormatInfo(fileInfo, defaultContext);

			//System.out.println("Valore di res: " + res);

			if(res==null || res.length==0){
				//System.out.println("************Lunghezza 0***********");
				throw new Exception("Nessun formato trovato.");
			}
			if(res.length!=1){
				throw new Exception("Riconoscimento non univoco. " + res.length + " risultati trovati.");
			}
			if(res[0].getTypeDescription().equals("Negative")){
				throw new Exception("Riconoscimento errato. Identificazione negativa.");
			}
			if(!res[0].getMimeType().equals("application/pdf")){
				throw new Exception("Riconoscimento errato. Mimetype restituito: " + res[0].getMimeType() + " al posto di application/pdf");
			}
			assertTrue(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.debug("[TestEngine::testGetFileFormatInfoFileInfo] ERROR");
			assertTrue(false);
		}
		finally{
			try{fis.close();}catch(Exception e){}
			stop();
			log.debug("[TestEngine::testGetFileFormatInfoFileInfo] END");
		}
	}

	public void testGetFileFormatVersion(){
		log.debug("[TestEngine::testGetFileFormatVersion] BEGIN");
		try{
			FileFormatVersion result=ecmEngineDelegateImpl.getFileFormatVersion(defaultContext);
			if(result!=null && result.getVersion()!=null && !result.getVersion().trim().equals("")){
				log.debug("[TestEngine::testGetFileFormatVersion] Versione FileFormat: "+result.getVersion());
				assertTrue(true);
			}
			else{
				log.debug("[TestEngine::testGetFileFormatVersion] Versione FileFormat nulla o vuota.");
				assertTrue(false);
			}
		}catch(Exception e){
			log.debug("[TestEngine::testGetFileFormatVersion] ERROR",e);
			assertTrue(false);
		}finally{
			log.debug("[TestEngine::testGetFileFormatVersion] END");
		}
	}


	public void testCopyNode(){
		try{
			log.debug("[TestEngine::testCopyNode] BEGIN");
			Node copyFolder=createNodeDTO(createFolder(uidFolder, System.currentTimeMillis()+"_testCopyNode", defaultContext));
			Node result=ecmEngineDelegateImpl.copyNode(createNodeDTO(uidDocument), copyFolder, defaultContext);
			if(result!=null&&result.getUid()!=null&&!result.getUid().equals("")){
				log.debug("[TestEngine::testCopyNode] Copiato nodo. Resituito id: "+result.getUid());
				assertTrue(true);
			}else{
				throw new Exception("Nodo non copiato correttamente.");
			}
		}catch(Exception e){
			assertTrue(false);
			log.error("[TestEngine::testCopyNode] ERROR " + e.getMessage());
		}finally{
			log.debug("[TestEngine::testCopyNode] END");
		}
	}

	public void testMoveNode(){
		try{
			log.debug("[TestEngine::testMoveNode] BEGIN");
			long moveTime=System.currentTimeMillis();
			Node moveFolderFrom=createNodeDTO(createFolder(uidFolder, moveTime+"_testMoveNode_from", defaultContext));
			Node moveFolderTo=createNodeDTO(createFolder(uidFolder, moveTime+"_testMoveNode_to", defaultContext));

			Node resultCopy=ecmEngineDelegateImpl.copyNode(createNodeDTO(uidDocument), moveFolderFrom, defaultContext);
			ecmEngineDelegateImpl.moveNode(resultCopy, moveFolderTo, defaultContext);
			SearchParams xpath=new SearchParams();
			xpath.setXPathQuery("//app:contentStore/cm:"+folder+"/cm:"+moveTime+"_testMoveNode_from/*");
			if(ecmEngineDelegateImpl.xpathSearchNoMetadata(xpath, defaultContext).getNodeArray().length!=0){
				throw new Exception("Nodo non spostato.");
			}
		}catch(Exception e){
			assertTrue(false);
			log.error("[TestEngine::testMoveNode] ERROR " + e.getMessage());
		}finally{
			log.debug("[TestEngine::testMoveNode] END");
		}
	}

	public void testSelectNodes(){
		log.debug("[TestEngine::testGetMimeType] BEGIN");
		start();

		try {
            Node documento = new Node();
            documento.setUid(uidDocument);

            SearchParamsAggregate parameterAggregate = new SearchParamsAggregate();
		    parameterAggregate.setXPathQuery("/app:company_home/cm:"+folder+"/*");
		    parameterAggregate.setLimit(1);

            //TODO: per ora testiamo la chiamata, poi occorrera' capire se il valore che torna e' quello aspettato
	        NodeResponse n = ecmEngineDelegateImpl.selectNodes(documento, parameterAggregate, defaultContext);

			assertTrue(true);

		} catch (Exception e) {
			log.error("[TestEngine::testNodeExists] Eccezione", e);
			assertTrue(false);
		} finally{
			stop();
			log.debug("[TestEngine::testNodeExists] END");
		}
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


}

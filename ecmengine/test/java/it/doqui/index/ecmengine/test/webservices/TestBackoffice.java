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

package it.doqui.index.ecmengine.test.webservices;

import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.client.webservices.backoffice.EcmEngineWebServiceBackofficeDelegate;
import it.doqui.index.ecmengine.client.webservices.backoffice.EcmEngineWebServiceBackofficeDelegateServiceLocator;
import it.doqui.index.ecmengine.client.webservices.engine.EcmEngineWebServiceDelegate;
import it.doqui.index.ecmengine.client.webservices.engine.EcmEngineWebServiceDelegateServiceLocator;
import it.doqui.index.ecmengine.client.webservices.dto.AclRecord;
import it.doqui.index.ecmengine.client.webservices.dto.Node;
import it.doqui.index.ecmengine.client.webservices.dto.OperationContext;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.AclListParams;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.Tenant;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.Group;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.Repository;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.SystemProperty;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.User;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.CustomModel;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.ModelDescriptor;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.ModelMetadata;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Content;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Property;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.client.webservices.exception.InvalidParameterException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.GroupAlreadyExistsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.GroupCreateException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.NoSuchGroupException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.NoSuchUserException;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.IntegrityReport;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.test.util.EcmEngineTestConstants;
import it.doqui.index.ecmengine.test.webservices.util.EcmEngineWebservicesTestConstants;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestBackoffice extends TestCase implements EcmEngineWebservicesTestConstants {

  private EcmEngineWebServiceBackofficeDelegate ecmEngineBackofficeDelegateImpl = null;
  protected transient Log log;
  protected transient StopWatch stopwatch;

  private static boolean flag = false;


  private EcmEngineWebServiceDelegate ecmEngineDelegateImpl = null;
  private static OperationContext defaultContext;

  private String folderParent;
  private String folder;
  private String contenuto;
  private static String folderBko;
  private static String zipArchive;
  private static String contentModel;

  private static Node folderTest = null;
  private static String userprop;
  private static String user;
  private static String tenant;

  private static String dateStr;

  private static String REPOSITORY = "primary";

  private static int SLEEP_TIME = 5000;

  private static String TEST_TARGET;

  public TestBackoffice(String name) {
    super(name);
	this.log = LogFactory.getLog(ECMENGINE_TEST_LOG_CATEGORY);
    log.debug("[TestBackoffice::Constructor] BEGIN - " +name);

	defaultContext = new OperationContext();
	defaultContext.setUsername("admin");
	defaultContext.setPassword("admin");
	defaultContext.setFruitore("TestJUNIT");
	defaultContext.setNomeFisico("Client TestJUNIT");
	defaultContext.setRepository(REPOSITORY);

	String managementUrl = null;
	String backofficeUrl = null;
	try {
		InputStream wsis = this.getClass().getResourceAsStream("/" + ECMENGINE_WEBSERVICES_TEST_PROPERTIES_FILE);
		Properties wsProperties= new Properties();
		if (wsis != null) {
			wsProperties.load(wsis);
			managementUrl = wsProperties.getProperty(ECMENGINE_WEBSERVICES_MANAGEMENT_URL);
			backofficeUrl = wsProperties.getProperty(ECMENGINE_WEBSERVICES_BACKOFFICE_URL);
		}
	} catch(Exception e) {
		log.error("[TestEngine::Constructor] Errore nella lettura del file di properties", e);
	}

	try {
		// La classe locator viene generata da Axis.
		EcmEngineWebServiceBackofficeDelegateServiceLocator locator = new EcmEngineWebServiceBackofficeDelegateServiceLocator();
		ecmEngineBackofficeDelegateImpl = locator.getEcmEngineBackoffice(new URL(backofficeUrl));
	    log.debug("[TestBackoffice::Constructor] Delegate instantiate");
	} catch(Exception e) {
	    log.debug("[TestBackoffice::Constructor] Instantiation problem", e);
	}
	try {
		// La classe locator viene generata da Axis.
		EcmEngineWebServiceDelegateServiceLocator locator = new EcmEngineWebServiceDelegateServiceLocator();
		ecmEngineDelegateImpl = locator.getEcmEngineManagement(new URL(managementUrl));
		log.debug("[TestBackoffice::Constructor] Delegate EcmEngine instantiate");
	} catch(Exception e) {
		log.error("[TestBackoffice::Constructor] Instantiation EcmEngine problem", e);
	}

    log.debug("[TestBackoffice::Constructor] END");
  }

  protected void setUp() throws Exception {
	  super.setUp();

      log.debug("[TestBackoffice::setUp] BEGIN");
	  if (flag == false){
		  readProperties();
		  folderTest = getFolderTest(folderBko, this.getUidCompanyHome());
		  flag = true;
	  }
      log.debug("[TestBackoffice::setUp] END");

   }

  protected void tearDown() throws Exception {
	  super.tearDown();

	  /**
	  if(down)
	  {
		  User filter = new User();
		  filter.setRepository(REPOSITORY);
		  filter.setUsername("*");

		  try{

			  User [] utenti =  ecmEngineBackofficeDelegateImpl.listAllUsers(filter, defaultContext);

			  ecmEngineBackofficeDelegateImpl.removeUserFromGroup(utente, gruppo, context)

			  Node nodo = null;

			  if(utenti!=null && utenti.length>0){

				  for (int i = 0; i < utenti.length; i++) {

					  String uid = utenti[i].getOrganizationId();

					  nodo = new Node(uid,REPOSITORY);

					  ecmEngineDelegateImpl.deleteContent(nodo, defaultContext);
					  log.debug("[TestBackoffice::tearDown] Eliminato nodo con uid: "+uid);
				  }
			  }

		  }catch (Exception e) {
			  log.error("[TestBackoffice::tearDown] Errore: " + e);
		  }
	  }
	  **/

	  ecmEngineBackofficeDelegateImpl = null;
  }

  /**
   * Metodo che ricerca un gruppo non presente sulla piattaforma EcmEngine
   *
   */
  public void testRicercaGruppoNotPresent() {
      log.debug("[TestBackoffice::testRicercaGruppoNotPresent] BEGIN");

      try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

      final String logCtx = "Prova unit test ricerca gruppo non presente";

		Group group = new Group();
		group.setName("gruppoprovatestjunit");

	  start();
      try {
		  Group[] results = ecmEngineBackofficeDelegateImpl.listAllGroups(group, defaultContext);
		  log.debug("[TestBackoffice::testRicercaGruppoNotPresent] results vale: " + results);
      } catch(NoSuchGroupException e) {
          log.debug("[TestBackoffice::testRicercaGruppoNotPresent] NoSuchGroupException: OK");
    	  assertTrue(true);
      } catch(NoDataExtractedException e) {
          log.debug("[TestBackoffice::testRicercaGruppoNotPresent] NoDataExtractedException: OK");
    	  assertTrue(true);
      } catch(Exception e) {
          log.debug("[TestBackoffice::testRicercaGruppoNotPresent] Eccezione", e);
    	  assertTrue(false);
      } finally {
    	  dumpElapsed("TestBackoffice", "testRicercaGruppoNotPresent", logCtx, "chiamata al servizio listaUtentiGruppo");
          stop();
          log.debug("[TestBackoffice::testRicercaGruppoNotPresent] END");
      }
  }

  /**
   * Metodo che verifica la creazione di un utente sulla piattaforma EcmEngine
   *
   */
  static int nUser = 0;
  public void testCreaUser() {
	  log.debug("[TestBackoffice::testCreaUser] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test creaUser";

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
      dateStr = sdf.format(new Date());
      nUser++;

      user = userprop+"_"+dateStr+"_"+nUser+tenant;
      log.debug("[TestBackoffice::testCreaUser] user vale: " + user);

	  User nuovoUtente = new User();
	  nuovoUtente.setName("mario");
	  nuovoUtente.setSurname("rossi");

	  nuovoUtente.setUsername(user);
	  nuovoUtente.setPassword("alfresco");

	  start();

	  try {
		  String utente = null;
		  utente = ecmEngineBackofficeDelegateImpl.createUser(nuovoUtente, defaultContext);
		  dumpElapsed("TestBackoffice", "testCreaUser", logCtx, "chiamata al servizio createUser");
		  log.debug("[TestBackoffice::testCreaUser] chiamata al servizio createUser. Utente creato: "+utente);

		  Thread.sleep(SLEEP_TIME);

		  User filter = new User();
		  filter.setUsername(user);

		  User[] utenti = ecmEngineBackofficeDelegateImpl.listAllUsers(filter, defaultContext);
		  dumpElapsed("TestBackoffice", "testCreaUser", logCtx, "chiamata al servizio listAllUsers");
		  log.debug("[TestBackoffice::testCreaUser] chiamata al servizio listAllUsers");

		  if(utenti!=null && utenti.length==1 && utenti[0].getUsername().equalsIgnoreCase(user)){
			  log.debug("[TestBackoffice::testCreaUser] Utente trovato : "+utenti[0].getUsername());
			  assertTrue(true);
			  return;
		  }
		  assertTrue(false);
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testCreaUser] Eccezione", e);
		  assertTrue(false);
	  }finally {
		  stop();
		  log.debug("[TestBackoffice::testCreaUser] END");
	  }
  }

  /*
   * Metodo che verifica la creazione di un gruppo gia esistente sul repository.
   */

  public void testCreateGroupDuplicate(){
	  log.debug("[TestBackoffice::testCreateGroupDuplicate] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test creaGroup";

	  Group nuovoGruppo= new Group();
	  nuovoGruppo.setName("JUnit_"+user);

	  Group gruppoPadre=null;

	  start();

	  try {

		  String gruppo = ecmEngineBackofficeDelegateImpl.createGroup(nuovoGruppo, gruppoPadre, defaultContext);
		  dumpElapsed("TestBackoffice", "testCreateGroupDuplicate", logCtx, "chiamata al servizio createGroup");
		  log.debug("[TestBackoffice::testCreateGroupDuplicate] chiamata al servizio createGroup." +
		  		" Gruppo creato: "+gruppo);

		  Thread.sleep(SLEEP_TIME);

		  log.debug("[TestBackoffice::testCreateGroupDuplicate] Seconda chiamata al servizio createGroup " +
		  "per la creazione dello stesso gruppo");

		  gruppo = ecmEngineBackofficeDelegateImpl.createGroup(nuovoGruppo, gruppoPadre , defaultContext);
		  log.debug("[TestBackoffice::testCreateGroupDuplicate] Seconda chiamata al servizio createGroup." +
			  		" Gruppo creato: "+gruppo);

		  //la seconda chiamata al metodo createGroup deve sollevare l'eccezione GroupAlreadyExistsException

		  assertTrue(false);
	  } catch (GroupCreateException e) {
		  log.debug("[TestBackoffice::testCreateGroupDuplicate] GroupCreateException: OK");
		  assertTrue(true);
	  } catch (GroupAlreadyExistsException e) {
		  log.debug("[TestBackoffice::testCreateGroupDuplicate] GroupAlreadyExistsException: OK");
		  assertTrue(true);
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testCreateGroupDuplicate] Eccezione", e);
		  assertTrue(false);
	  }finally{
		  stop();
		  log.debug("[TestBackoffice::testCreateGroupDuplicate] END");
	  }
  }


  public void testAddUserToGroup(){
	  log.debug("[TestBackoffice::testAddUserToGroup] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}


	  final String logCtx = "Prova unit test addUserToGroup";

	  Group gruppo = new Group();
	  gruppo.setName("JUnit_"+user);

	  User utente = new User();
	  utente.setName("mario");
	  utente.setSurname("rossi");
	  utente.setUsername(user);
	  utente.setPassword("alfresco");

	  start();

	  try {
		  ecmEngineBackofficeDelegateImpl.addUserToGroup(utente, gruppo, defaultContext);
		  dumpElapsed("TestBackoffice", "testAddUserToGroup", logCtx, "chiamata al servizio addUserToGroup");
		  log.debug("[TestBackoffice::testAddUserToGroup] chiamata al servizio addUserToGroup");

		  Thread.sleep(SLEEP_TIME);

		  User[] utenti = ecmEngineBackofficeDelegateImpl.listUsers(gruppo, defaultContext);
		  dumpElapsed("TestBackoffice", "testAddUserToGroup", logCtx, "chiamata al servizio listUsers");
		  log.debug("[TestBackoffice::testAddUserToGroup] chiamata al servizio listUsers");

		  for (User user : utenti) {
			  log.debug("[TestBackoffice::testAddUserToGroup] Utente : "+user.getUsername());
			  if(user.getUsername().equalsIgnoreCase(utente.getUsername())){
				  assertTrue(true);
				  return;
			  }
		  }
		  log.debug("[TestBackoffice::testAddUserToGroup] User non trovato: "+utente.getUsername());
		  assertTrue(false);
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testAddUserToGroup] Eccezione", e);
		  assertTrue(false);
	  }finally{
		  stop();
		  log.debug("[TestBackoffice::testAddUserToGroup] END");
	  }
  }

  /**
   * Metodo che verifica l'aggiornamento dei metadati di un utente sulla piattaforma EcmEngine
   *
   */
  public void testUpdateUserMetadata() {
	  log.debug("[TestBackoffice::testUpdateUserMetadata] BEGIN");

	  try {
		  Thread.sleep(SLEEP_TIME);
	  } catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test Update User Metadata";

	  User utente = new User();

	  utente.setName("MARIANO");
	  utente.setSurname("ROSSI");
	  utente.setUsername(user);
	  utente.setPassword("alfresco");
	  utente.setOrganizationId("CSI Piemonte");

	  start();

	  try {

		  ecmEngineBackofficeDelegateImpl.updateUserMetadata(utente, defaultContext);

		  dumpElapsed("TestBackoffice", "testUpdateUserMetadata", logCtx, "chiamata al servizio updateUserMetadata");
		  log.debug("[TestBackoffice::testUpdateUserMetadata] chiamata al servizio UpdateUserMetadata. Utente aggiornato: "
				  +utente.getUsername());

		  Thread.sleep(SLEEP_TIME);

		  User filter = new User();
		  filter.setUsername(user);

		  User[] utenti = ecmEngineBackofficeDelegateImpl.listAllUsers(filter, defaultContext);
		  dumpElapsed("TestBackoffice", "testUpdateUserMetadata", logCtx, "chiamata al servizio listAllUsers");
		  log.debug("[TestBackoffice::testUpdateUserMetadata] chiamata al servizio listAllUsers");

		  if(utenti!=null && utenti.length==1 && utenti[0].getUsername().equalsIgnoreCase(user)
				  && utenti[0].getOrganizationId().equalsIgnoreCase("CSI Piemonte")){
			  log.debug("[TestBackoffice::testUpdateUserMetadata] Utente trovato e metadati aggiornati : "+utenti[0].getUsername());
			  assertTrue(true);
			  return;
		  }
		  assertTrue(false);
	  } catch (Exception e) {
		  log.error("[TestBackoffice::testUpdateUserMetadata] Eccezione", e);
		  assertTrue(false);
	  } finally {
		  stop();
		  log.debug("[TestBackoffice::testUpdateUserMetadata] END");
	  }
  }

  /**
   * Metodo che verifica l'aggiornamento della pwd di un utente sulla piattaforma EcmEngine
   *
   */
  public void testUpdateUserPassword() {
	  log.debug("[TestBackoffice::testUpdateUserPassword] BEGIN");

	  try {
		  Thread.sleep(SLEEP_TIME);
	  } catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test Update User Password";

	  start();

	  try {
		  String username = "junit-upd-pwd-"+dateStr;
		  String firstPassword = "password";
		  String updatePassword = "update";
		  User utente = new User();
		  utente.setUsername(username);
		  utente.setPassword(firstPassword);
		  utente.setName("JUnit");
		  utente.setSurname("Test");
		  ecmEngineBackofficeDelegateImpl.createUser(utente, defaultContext);
		  dumpElapsed("TestBackoffice", "testUpdateUserPassword", logCtx, "chiamata al servizio createUser");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  OperationContext testContext = new OperationContext();
		  testContext.setUsername(username+tenant);
		  testContext.setPassword(firstPassword);
		  testContext.setNomeFisico("JUnit Test User");
		  testContext.setFruitore("JUnit Test Client");
		  testContext.setRepository(defaultContext.getRepository());
		  ecmEngineBackofficeDelegateImpl.getRepositories(testContext);
		  dumpElapsed("TestBackoffice", "testUpdateUserPassword", logCtx, "chiamata al servizio getRepositories");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  utente.setPassword(updatePassword);
		  ecmEngineBackofficeDelegateImpl.updateUserPassword(utente, defaultContext);
		  dumpElapsed("TestBackoffice", "testUpdateUserPassword", logCtx, "chiamata al servizio updateUserPassword");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  try {
			  testContext.setPassword(firstPassword);
			  ecmEngineBackofficeDelegateImpl.getRepositories(testContext);
		  } catch(InvalidCredentialsException ice) {
			  // OK
		  }
		  dumpElapsed("TestBackoffice", "testUpdateUserPassword", logCtx, "chiamata al servizio getRepositories con password precedente");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  testContext.setPassword(updatePassword);
		  ecmEngineBackofficeDelegateImpl.getRepositories(testContext);
		  dumpElapsed("TestBackoffice", "testUpdateUserPassword", logCtx, "chiamata al servizio getRepositories con password nuova");

		  assertTrue(true);
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testUpdateUserPassword] Eccezione", e);
		  assertTrue(false);
	  }finally {
		  stop();
		  log.debug("[TestBackoffice::testUpdateUserPassword] END");
	  }
  }

  /**
   * Metodo che verifica la cancellazione di un utente sulla piattaforma EcmEngine
   *
   */
  public void testDeleteUser() {
	  log.debug("[TestBackoffice::testDeleteUser] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test deleteUser";

	  User utente = new User();
	  utente.setName("finto");
	  utente.setSurname("fittizio");

	  utente.setUsername("nonEsiste");
	  utente.setPassword("nonValida");

	  start();

	  try {

		  try{
			  log.debug("[TestBackoffice::testDeleteUser] chiamata al servizio deleteUser. Provo a cancellare " +
			  		"un utente non presente sul repository: "+utente.getUsername());

			  ecmEngineBackofficeDelegateImpl.deleteUser(utente, defaultContext);

		  } catch (NoSuchUserException e) {
			  dumpElapsed("TestBackoffice", "deleteUser", logCtx, "chiamata al servizio deleteUser");
			  log.debug("[TestBackoffice::testDeleteUser] Eccezione corretta di tipo NoSuchUserException: " + e.getMessage());
			  assertTrue(true);
			  return;
		  }

		  log.debug("[TestBackoffice::testDeleteUser] Cancellato un utente inesistente e non e` stata sollevata " +
		  		"l'eccezione di tipo NoSuchUserException!!");
		  assertTrue(false);
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testDeleteUser] Eccezione", e);
		  assertTrue(false);
	  }finally {
		  stop();
		  log.debug("[TestBackoffice::testDeleteUser] END");
	  }
  }


  /**
   * Metodo che verifica la lettura dei metadati di un utente sulla piattaforma EcmEngine
   *
   */
  public void testRetrieveUserMetadata() {
	  log.debug("[TestBackoffice::testRetrieveUserMetadata] BEGIN");

	  try {
		  Thread.sleep(SLEEP_TIME);
	  } catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test Retrieve User Metadata";

	  User utente = new User();
	  utente.setUsername(user);

	  start();

	  try {

		  utente = ecmEngineBackofficeDelegateImpl.retrieveUserMetadata(utente, defaultContext);

		  dumpElapsed("TestBackoffice", "testRetrieveUserMetadata", logCtx, "chiamata al servizio retrieveUserMetadata");
		  log.debug("[TestBackoffice::testRetrieveUserMetadata] chiamata al servizio retrieveUserMetadata. Utente letto: "
				  +utente.getUsername());


		  if(utente.getUsername().equalsIgnoreCase(user)
				  && utente.getSurname().equalsIgnoreCase("rossi")){
			  log.debug("[TestBackoffice::testRetrieveUserMetadata] Letti metadati utente : "+utente.getUsername());
			  assertTrue(true);
			  return;
		  }

		  assertTrue(false);

	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testRetrieveUserMetadata] Eccezione", e);
		  assertTrue(false);
	  }finally {
		  stop();
		  log.debug("[TestBackoffice::testRetrieveUserMetadata] END");
	  }
  }



  /**
   * Metodo che verifica la cancellazione di un gruppo sulla piattaforma EcmEngine
   *
   */
  public void testDeleteGroup() {
	  log.debug("[TestBackoffice::testDeleteGroup] BEGIN");

	  try {
		  Thread.sleep(SLEEP_TIME);
	  } catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test deleteGroup";

	  Group gruppo =  new Group();
	  gruppo.setName("GruppoInesistente");

	  start();

	  try {

		  try{
			  log.debug("[TestBackoffice::testDeleteGroup] chiamata al servizio deleteGroup. Provo a cancellare " +
					  "un gruppo non presente sul repository: "+gruppo.getName());

			  ecmEngineBackofficeDelegateImpl.deleteGroup(gruppo, defaultContext);

		  } catch (NoSuchGroupException e) {
			  dumpElapsed("TestBackoffice", "testDeleteGroup", logCtx, "chiamata al servizio deleteGroup");
			  log.debug("[TestBackoffice::testDeleteGroup] Eccezione corretta di tipo NoSuchGroupException: " + e.getMessage());
			  assertTrue(true);
			  return;
		  }

		  log.debug("[TestBackoffice::testDeleteGroup] Cancellato un gruppo inesistente e non e` stata sollevata " +
		  "l'eccezione di tipo NoSuchGroupException!!");
		  assertTrue(false);

	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testDeleteGroup] Eccezione", e);
		  assertTrue(false);
	  }finally {
		  stop();
		  log.debug("[TestBackoffice::testDeleteGroup] END");
	  }
  }

  public void testAddAcl(){
	  log.debug("[TestBackoffice::testAddAcl] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test addAcl";

	  String testFolderUid = createFolder(getUidCompanyHome(), "add-acl_"+dateStr, defaultContext);
	  Node testFolderNode = new Node();
	  testFolderNode.setUid(testFolderUid);

	  AclRecord acl = new AclRecord();
	  acl.setAuthority(user);
	  acl.setPermission("Read");
	  acl.setAccessAllowed(true);

	  AclListParams params = new AclListParams();
	  params.setShowInherited(false);


	  start();

	  try {
		  ecmEngineBackofficeDelegateImpl.addAcl(testFolderNode,  new AclRecord [] { acl }, defaultContext);
		  dumpElapsed("TestBackoffice", "testAddAcl", logCtx, "chiamata al servizio addAcl");
		  log.debug("[TestBackoffice::testAddAcl] chiamata al servizio addAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  AclRecord[] acls = ecmEngineBackofficeDelegateImpl.listAcl(testFolderNode, params, defaultContext);
		  dumpElapsed("TestBackoffice", "testAddAcl", logCtx, "chiamata al servizio listAcl");
		  int size = acls != null ? acls.length : 0;
		  log.debug("[TestBackoffice::testAddAcl] chiamata al servizio listAcl. Trovati " + size
				  + " records di acl");

		  for (AclRecord record : acls) {
			  log.debug("[TestBackoffice::testAddAcl] Authority Acl : "+record.getAuthority());
			  if(record.getPermission().equalsIgnoreCase("Read")
					  && record.getAuthority().equalsIgnoreCase(user) ){
				  assertTrue(true);
				  return;
			  }
		  }
		  assertTrue(false);
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testAddAcl] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testAddAcl] END");
	  }
  }


  public void testGetAllModelDescriptors() {
	  log.debug("[TestBackoffice::testGetAllModelDescriptors] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test getAllModelDescriptors";

	  start();

	  try {
		  ModelDescriptor[] descriptors = ecmEngineBackofficeDelegateImpl.getAllModelDescriptors(defaultContext);
		  dumpElapsed("TestBackoffice", "testGetAllModelDescriptors", logCtx, "chiamata al servizio getAllModelDescriptors");

		  // Di default esistono 14 data model (Alfresco 2.1)
		  if (descriptors != null && descriptors.length >= 14) {
			  log.debug("[TestBackoffice::testGetAllModelDescriptors] Trovati "+descriptors.length+" data model: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testGetAllModelDescriptors] Trovati "+(descriptors != null ? descriptors.length : 0)+" data model: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testGetAllModelDescriptors] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testGetAllModelDescriptors] END");
	  }
  }

  public void testGetModelDefinition() {
	  log.debug("[TestBackoffice::testGetModelDefinition] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test getModelDefinition";

	  start();

	  try {
		  ModelDescriptor modelDescriptor = new ModelDescriptor();
		  modelDescriptor.setPrefixedName("cm:contentmodel");
		  ModelMetadata modelMetadata = ecmEngineBackofficeDelegateImpl.getModelDefinition(modelDescriptor, defaultContext);
		  dumpElapsed("TestBackoffice", "testGetModelDefinition", logCtx, "chiamata al servizio getModelDefinition");

		  if (modelMetadata != null && modelMetadata.getPrefixedName().equals("cm:contentmodel")) {
			  log.debug("[TestBackoffice::testGetModelDefinition] Definizione del model '"+modelMetadata.getPrefixedName()+"' trovata: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testGetModelDefinition] Definizione del model '"+modelMetadata.getPrefixedName()+"' NON trovata: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testGetModelDefinition] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testGetModelDefinition] END");
	  }
  }

  public void testGetRepositories() {
	  log.debug("[TestBackoffice::testGetRepositories] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test getRepositories";

	  start();

	  try {
		  Repository[] repositories = ecmEngineBackofficeDelegateImpl.getRepositories(defaultContext);
		  dumpElapsed("TestBackoffice", "testGetRepositories", logCtx, "chiamata al servizio getRepositories");

		  if (repositories != null && repositories.length > 0) {
			  log.debug("[TestBackoffice::testGetRepositories] Trovati "+repositories.length+" repository: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testGetRepositories] Nessun repository trovato: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testGetRepositories] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testGetRepositories] END");
	  }
  }

  public void testGetSystemProperties() {
	  log.debug("[TestBackoffice::testGetSystemProperties] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test getRepositories";

	  start();

	  try {
		  SystemProperty[] sysProps = ecmEngineBackofficeDelegateImpl.getSystemProperties(defaultContext);
		  dumpElapsed("TestBackoffice", "testGetSystemProperties", logCtx, "chiamata al servizio getRepositories");

		  if (sysProps != null && sysProps.length > 0) {
			  log.debug("[TestBackoffice::testGetSystemProperties] Trovate "+sysProps.length+" system property: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testGetSystemProperties] Nessuna system property trovata: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testGetSystemProperties] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testGetSystemProperties] END");
	  }
  }

  public void testListAcl() {
	  log.debug("[TestBackoffice::testListAcl] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listAcl";

	  start();

	  try {
		  Node testFolder = getFolderTest("list_acl", folderTest.getUid());

		  AclRecord[] acls = new AclRecord[2];
		  acls[0] = new AclRecord();
		  acls[0].setAuthority("admin");
		  acls[0].setPermission("Editor");
		  acls[0].setAccessAllowed(true);
		  acls[1] = new AclRecord();
		  acls[1].setAuthority("admin");
		  acls[1].setPermission("Consumer");
		  acls[1].setAccessAllowed(true);
		  ecmEngineBackofficeDelegateImpl.addAcl(testFolder, acls, defaultContext);
		  dumpElapsed("TestBackoffice", "testListAcl", logCtx, "chiamata al servizio addAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  AclRecord[] records = ecmEngineBackofficeDelegateImpl.listAcl(testFolder, new AclListParams(), defaultContext);
		  dumpElapsed("TestBackoffice", "testListAcl", logCtx, "chiamata al servizio listAcl");

		  if (records != null && records.length == 2) {
			  log.debug("[TestBackoffice::testListAcl] Trovate "+records.length+" acl: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testListAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testListAcl] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testListAcl] END");
	  }
  }

  public void testUpdateAcl() {
	  log.debug("[TestBackoffice::testUpdateAcl] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test updateAcl";

	  start();

	  try {
		  Node testFolder = getFolderTest("update_acl", folderTest.getUid());

		  AclRecord[] acls = new AclRecord[1];
		  acls[0] = new AclRecord();
		  acls[0].setAuthority(defaultContext.getUsername());
		  acls[0].setPermission("Editor");
		  acls[0].setAccessAllowed(true);
		  ecmEngineBackofficeDelegateImpl.addAcl(testFolder, acls, defaultContext);
		  dumpElapsed("TestBackoffice", "testUpdateAcl", logCtx, "chiamata al servizio addAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  acls[0] = new AclRecord();
		  acls[0].setAuthority(defaultContext.getUsername());
		  acls[0].setPermission("Consumer");
		  acls[0].setAccessAllowed(true);
		  ecmEngineBackofficeDelegateImpl.updateAcl(testFolder, acls, defaultContext);
		  dumpElapsed("TestBackoffice", "testUpdateAcl", logCtx, "chiamata al servizio updateAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  AclRecord[] records = ecmEngineBackofficeDelegateImpl.listAcl(testFolder, new AclListParams(), defaultContext);
		  dumpElapsed("TestBackoffice", "testUpdateAcl", logCtx, "chiamata al servizio listAcl");

		  if (records != null && records.length == 1
				  && records[0].getAuthority().equals(defaultContext.getUsername())
				  && records[0].getPermission().equals("Consumer")) {
			  log.debug("[TestBackoffice::testUpdateAcl] Trovata acl ("+defaultContext.getUsername()+", Consumer): OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testUpdateAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testUpdateAcl] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testUpdateAcl] END");
	  }
  }

  public void testInheritsAcl() {
	  log.debug("[TestBackoffice::testInheritsAcl] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test setInheritsAcl/isInheritsAcl";

	  start();

	  try {
		  Node testFolder = getFolderTest("inherits_acl", folderTest.getUid());

		  boolean setOk = false;
		  boolean unsetOk = false;

		  boolean targetInheritsAcl = true;
		  ecmEngineBackofficeDelegateImpl.setInheritsAcl(testFolder, targetInheritsAcl, defaultContext);
		  dumpElapsed("TestBackoffice", "testInheritsAcl", logCtx, "chiamata al servizio setInheritsAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  boolean resultInheritsAcl = ecmEngineBackofficeDelegateImpl.isInheritsAcl(testFolder, defaultContext);
		  dumpElapsed("TestBackoffice", "testInheritsAcl", logCtx, "chiamata al servizio isInheritsAcl");

		  setOk = (targetInheritsAcl == resultInheritsAcl);

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  targetInheritsAcl = false;
		  ecmEngineBackofficeDelegateImpl.setInheritsAcl(testFolder, targetInheritsAcl, defaultContext);
		  dumpElapsed("TestBackoffice", "testInheritsAcl", logCtx, "chiamata al servizio setInheritsAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  resultInheritsAcl = ecmEngineBackofficeDelegateImpl.isInheritsAcl(testFolder, defaultContext);
		  dumpElapsed("TestBackoffice", "testInheritsAcl", logCtx, "chiamata al servizio isInheritsAcl");

		  unsetOk = (targetInheritsAcl == resultInheritsAcl);

		  if (setOk && unsetOk) {
			  log.debug("[TestBackoffice::testInheritsAcl] Ereditarieta` impostata correttamente: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testInheritsAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testInheritsAcl] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testInheritsAcl] END");
	  }
  }

  public void testResetAcl() {
	  log.debug("[TestBackoffice::testResetAcl] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listAcl";

	  start();

	  try {
		  Node testFolder = getFolderTest("reset_acl", folderTest.getUid());

		  AclRecord[] acls = new AclRecord[2];
		  acls[0] = new AclRecord();
		  acls[0].setAuthority("admin");
		  acls[0].setPermission("Editor");
		  acls[0].setAccessAllowed(true);
		  acls[1] = new AclRecord();
		  acls[1].setAuthority("admin");
		  acls[1].setPermission("Consumer");
		  acls[1].setAccessAllowed(true);
		  ecmEngineBackofficeDelegateImpl.addAcl(testFolder, acls, defaultContext);
		  dumpElapsed("TestBackoffice", "testResetAcl", logCtx, "chiamata al servizio addAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  AclRecord[] records = ecmEngineBackofficeDelegateImpl.listAcl(testFolder, new AclListParams(), defaultContext);
		  dumpElapsed("TestBackoffice", "testResetAcl", logCtx, "chiamata al servizio listAcl");

		  if (records != null && records.length == 2) {
			  try {
				  Thread.sleep(SLEEP_TIME);
			  } catch (InterruptedException e1) {}

			  ecmEngineBackofficeDelegateImpl.resetAcl(testFolder, null, defaultContext);
			  dumpElapsed("TestBackoffice", "testResetAcl", logCtx, "chiamata al servizio resetAcl");

			  try {
				  Thread.sleep(SLEEP_TIME);
			  } catch (InterruptedException e1) {}

			  records = ecmEngineBackofficeDelegateImpl.listAcl(testFolder, new AclListParams(), defaultContext);
			  dumpElapsed("TestBackoffice", "testResetAcl", logCtx, "chiamata al servizio listAcl");

			  if (records == null || records.length == 0) {
				  log.debug("[TestBackoffice::testResetAcl] Trovate "+records.length+" acl: OK");
				  assertTrue(true);
			  } else {
				  log.debug("[TestBackoffice::testResetAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
				  assertTrue(false);
			  }
		  } else {
			  log.debug("[TestBackoffice::testResetAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testResetAcl] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testResetAcl] END");
	  }
  }

  public void testRemoveAcl() {
	  log.debug("[TestBackoffice::testRemoveAcl] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listAcl";

	  start();

	  try {
		  Node testFolder = getFolderTest("remove_acl", folderTest.getUid());

		  AclRecord[] acls = new AclRecord[1];
		  acls[0] = new AclRecord();
		  acls[0].setAuthority("admin");
		  acls[0].setPermission("Editor");
		  acls[0].setAccessAllowed(true);
		  ecmEngineBackofficeDelegateImpl.addAcl(testFolder, acls, defaultContext);
		  dumpElapsed("TestBackoffice", "testRemoveAcl", logCtx, "chiamata al servizio addAcl");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  AclRecord[] records = ecmEngineBackofficeDelegateImpl.listAcl(testFolder, new AclListParams(), defaultContext);
		  dumpElapsed("TestBackoffice", "testRemoveAcl", logCtx, "chiamata al servizio listAcl");

		  if (records != null && records.length == 1) {
			  try {
				  Thread.sleep(SLEEP_TIME);
			  } catch (InterruptedException e1) {}

			  ecmEngineBackofficeDelegateImpl.removeAcl(testFolder, acls, defaultContext);
			  dumpElapsed("TestBackoffice", "testRemoveAcl", logCtx, "chiamata al servizio removeAcl");

			  try {
				  Thread.sleep(SLEEP_TIME);
			  } catch (InterruptedException e1) {}

			  records = ecmEngineBackofficeDelegateImpl.listAcl(testFolder, new AclListParams(), defaultContext);
			  dumpElapsed("TestBackoffice", "testRemoveAcl", logCtx, "chiamata al servizio listAcl");

			  if (records == null || records.length == 0) {
				  log.debug("[TestBackoffice::testRemoveAcl] Trovate "+records.length+" acl: OK");
				  assertTrue(true);
			  } else {
				  log.debug("[TestBackoffice::testRemoveAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
				  assertTrue(false);
			  }
		  } else {
			  log.debug("[TestBackoffice::testRemoveAcl] Le acl trovate non corrispondono con quelle attese: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testRemoveAcl] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testRemoveAcl] END");
	  }
  }

  public void testListAllGroups() {
	  log.debug("[TestBackoffice::testListAllGroups] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listAllGroups";

	  start();

	  try {
		  Group group = new Group();
		  group.setName("JUnit-ListAllGroups-"+dateStr);
		  ecmEngineBackofficeDelegateImpl.createGroup(group, null, defaultContext);
		  dumpElapsed("TestBackoffice", "testListAllGroups", logCtx, "chiamata al servizio createGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  Group filter = new Group();
		  filter.setName("*");
		  Group[] allGroups = ecmEngineBackofficeDelegateImpl.listAllGroups(filter, defaultContext);
		  dumpElapsed("TestBackoffice", "testListAllGroups", logCtx, "chiamata al servizio listAllGroups");

		  if (allGroups != null && allGroups.length > 0) {
			  log.debug("[TestBackoffice::testListAllGroups] Trovati "+allGroups.length+" gruppi: OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testListAllGroups] I gruppi trovati non corrispondono con quelli attesi: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testListAllGroups] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testListAllGroups] END");
	  }
  }

  public void testListGroups() {
	  log.debug("[TestBackoffice::testListGroups] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listGroups";

	  start();

	  try {
		  Group group = new Group();
		  group.setName("JUnit-ListGroups-"+dateStr);
		  ecmEngineBackofficeDelegateImpl.createGroup(group, null, defaultContext);
		  dumpElapsed("TestBackoffice", "testListGroups", logCtx, "chiamata al servizio createGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  String subgroupName = "Subgroup-"+dateStr;
		  Group subgroup = new Group();
		  subgroup.setName(subgroupName);
		  ecmEngineBackofficeDelegateImpl.createGroup(subgroup, group, defaultContext);
		  dumpElapsed("TestBackoffice", "testListGroups", logCtx, "chiamata al servizio createGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  Group[] groups = ecmEngineBackofficeDelegateImpl.listGroups(group, defaultContext);
		  dumpElapsed("TestBackoffice", "testListGroups", logCtx, "chiamata al servizio listGroups");

		  if (groups != null && groups.length == 1 && groups[0].getName().equals(subgroupName)) {
			  log.debug("[TestBackoffice::testListGroups] Trovato gruppo "+groups[0].getName()+": OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testListGroups] I gruppi trovati non corrispondono con quelli attesi: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testListGroups] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testListGroups] END");
	  }
  }

  public void testListAllUsers() {
	  log.debug("[TestBackoffice::testListAllUsers] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listAllUsers";

	  start();

	  try {
		  String username = "junit-all"+dateStr;
		  User user = new User();
		  user.setUsername(username);
		  user.setName("JUnit");
		  user.setSurname("Test");
		  user.setPassword("password");
		  ecmEngineBackofficeDelegateImpl.createUser(user, defaultContext);
		  dumpElapsed("TestBackoffice", "testListAllUsers", logCtx, "chiamata al servizio createUser");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  User filter = new User();
		  filter.setUsername(username);
		  User[] users = ecmEngineBackofficeDelegateImpl.listAllUsers(filter, defaultContext);
		  dumpElapsed("TestBackoffice", "testListAllUsers", logCtx, "chiamata al servizio listAllUsers");

		  if (users != null && users.length == 1) {
			  log.debug("[TestBackoffice::testListAllUsers] Trovato l'utente "+username+": OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testListAllUsers] Gli utenti trovati non corrispondono con quelli attesi: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testListAllUsers] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testListAllUsers] END");
	  }
  }

  public void testListUsers() {
	  log.debug("[TestBackoffice::testListUsers] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test listUsers";

	  start();

	  try {
		  Group group = new Group();
		  group.setName("JUnit-ListUsers-"+dateStr);
		  ecmEngineBackofficeDelegateImpl.createGroup(group, null, defaultContext);
		  dumpElapsed("TestBackoffice", "testListUsers", logCtx, "chiamata al servizio createGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  String username = "junit-"+dateStr;
		  User user = new User();
		  user.setUsername(username);
		  user.setName("JUnit");
		  user.setSurname("Test");
		  user.setPassword("password");
		  ecmEngineBackofficeDelegateImpl.createUser(user, defaultContext);
		  dumpElapsed("TestBackoffice", "testListUsers", logCtx, "chiamata al servizio createUser");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  ecmEngineBackofficeDelegateImpl.addUserToGroup(user, group, defaultContext);
		  dumpElapsed("TestBackoffice", "testListUsers", logCtx, "chiamata al servizio addUserToGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  User[] users = ecmEngineBackofficeDelegateImpl.listUsers(group, defaultContext);
		  dumpElapsed("TestBackoffice", "testListUsers", logCtx, "chiamata al servizio listUsers");

		  if (users != null && users.length == 1) {
			  log.debug("[TestBackoffice::testListUsers] Trovato l'utente "+username+": OK");
			  assertTrue(true);
		  } else {
			  log.debug("[TestBackoffice::testListUsers] Gli utenti trovati non corrispondono con quelli attesi: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testListUsers] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testListUsers] END");
	  }
  }

  public void testRemoveUserFromGroup() {
	  log.debug("[TestBackoffice::testRemoveUserFromGroup] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test removeUserFromGroup";

	  start();

	  try {
		  Group group = new Group();
		  group.setName("JUnit-RemoveUserFromGroup-"+dateStr);
		  ecmEngineBackofficeDelegateImpl.createGroup(group, null, defaultContext);
		  dumpElapsed("TestBackoffice", "testRemoveUserFromGroup", logCtx, "chiamata al servizio createGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  String username = "junit-remove-"+dateStr;
		  User user = new User();
		  user.setUsername(username);
		  user.setName("JUnit");
		  user.setSurname("Test");
		  user.setPassword("password");
		  ecmEngineBackofficeDelegateImpl.createUser(user, defaultContext);
		  dumpElapsed("TestBackoffice", "testRemoveUserFromGroup", logCtx, "chiamata al servizio createUser");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  ecmEngineBackofficeDelegateImpl.addUserToGroup(user, group, defaultContext);
		  dumpElapsed("TestBackoffice", "testRemoveUserFromGroup", logCtx, "chiamata al servizio addUserToGroup");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  User[] users = ecmEngineBackofficeDelegateImpl.listUsers(group, defaultContext);
		  dumpElapsed("TestBackoffice", "testRemoveUserFromGroup", logCtx, "chiamata al servizio listUsers");

		  if (users != null && users.length == 1) {
			  try {
				  Thread.sleep(SLEEP_TIME);
			  } catch (InterruptedException e1) {}

			  ecmEngineBackofficeDelegateImpl.removeUserFromGroup(user, group, defaultContext);
			  dumpElapsed("TestBackoffice", "testRemoveUserFromGroup", logCtx, "chiamata al servizio removeUserFromGroup");

			  users = ecmEngineBackofficeDelegateImpl.listUsers(group, defaultContext);
			  dumpElapsed("TestBackoffice", "testRemoveUserFromGroup", logCtx, "chiamata al servizio listUsers");

			  if (users != null && users.length == 0) {
				  log.debug("[TestBackoffice::testRemoveUserFromGroup] Utente "+username+" eliminato dal gruppo: OK");
				  assertTrue(true);
			  } else {
				  log.debug("[TestBackoffice::testRemoveUserFromGroup] Gli utenti trovati non corrispondono con quelli attesi: ERRORE");
				  assertTrue(false);
			  }
		  } else {
			  log.debug("[TestBackoffice::testRemoveUserFromGroup] Gli utenti trovati non corrispondono con quelli attesi: ERRORE");
			  assertTrue(false);
		  }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testRemoveUserFromGroup] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testRemoveUserFromGroup] END");
	  }
  }

  public void testDeployUndeployContentModel() {
	  log.debug("[TestBackoffice::testDeployUndeployContentModel] BEGIN");

	  try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e1) {}

	  final String logCtx = "Prova unit test testDeployUndeployContentModel";

	  start();

	  try {
		if(!TEST_TARGET.equals("batch")){
		  try {
			  // Verify if content model has been deployed previously
			  Repository repository = new Repository();
			  repository.setId(REPOSITORY);
			  CustomModel[] customModels = ecmEngineBackofficeDelegateImpl.getAllCustomModels(defaultContext);
			  dumpElapsed("TestBackoffice", "testDeployUndeployContentModel", logCtx, "chiamata al servizio getAllCustomModels");

			  if (customModels != null && customModels.length > 0) {
				  for (CustomModel customModel : customModels) {
					  if (contentModel.equals(customModel.getFilename())) {
						  ecmEngineBackofficeDelegateImpl.undeployCustomModel(customModel, defaultContext);
					  }
					  if ((contentModel+"DMM").equals(customModel.getFilename())) {
						  ecmEngineBackofficeDelegateImpl.undeployCustomModel(customModel, defaultContext);
					  }
				  }
			  }
		  } catch(NoDataExtractedException ndee) {
			  // Ignore exception
		  }
		  dumpElapsed("TestBackoffice", "testDeployUndeployContentModel", logCtx, "verifica model precedentemente deployato");

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}

		  File file = new File(contentModel);
		  FileInputStream fis = new FileInputStream(file);
		  byte[] buffer = new byte[(int)file.length()];
		  fis.read(buffer);

          // Deploy di uno attivo
		  CustomModel model = new CustomModel();
		  model.setFilename(contentModel);
		  model.setData(buffer);
		  model.setActive(true);
		  ecmEngineBackofficeDelegateImpl.deployCustomModel(model, defaultContext);
		  dumpElapsed("TestBackoffice", "testDeployUndeployContentModel", logCtx, "chiamata al servizio deployCustomModel");

          // Deploy di uno disattivo
		  model.setActive(false);
		  model.setFilename(contentModel+"DMM");
		  model.setData(new String(buffer).replaceAll("test","testDMM").getBytes());
		  ecmEngineBackofficeDelegateImpl.deployCustomModel(model, defaultContext);
		  dumpElapsed("TestBackoffice", "testDeployUndeployContentModel", logCtx, "chiamata al servizio deployCustomModel");

          // Ora verifico se e' stato deployato in modo disattivo
          CustomModel[] customModels = ecmEngineBackofficeDelegateImpl.getAllCustomModels(defaultContext);
          dumpElapsed("TestBackoffice", "testDeployUndeployContentModel", logCtx, "chiamata al servizio getAllCustomModels");

          boolean bDeployT = false;
          boolean bDeployF = false;
          if (customModels != null && customModels.length > 0) {
              for (CustomModel customModel : customModels) {
                  if (contentModel.equals(customModel.getFilename())) {
                      if( customModel.isActive() ){
                          bDeployT = true;
                      } else {
                		  log.debug("[TestBackoffice::testDeployUndeployContentModel] content model attivo non attivo");
                      }
                  }
                  if ((contentModel+"DMM").equals(customModel.getFilename())) {
                      if( !customModel.isActive() ){
                          bDeployF = true;
                      } else {
                		  log.debug("[TestBackoffice::testDeployUndeployContentModel] content model disattivo non disattivo");
                      }
                  }
              }
          }

          // Se non ho il model disattivo, c'e' stato un errore di deploy
          assertTrue(bDeployT&&bDeployF);

		  try {
			  Thread.sleep(SLEEP_TIME);
		  } catch (InterruptedException e1) {}
		  ecmEngineBackofficeDelegateImpl.undeployCustomModel(model, defaultContext);
		  dumpElapsed("TestBackoffice", "testDeployUndeployContentModel", logCtx, "chiamata al servizio undeployCustomModel");
      }
      else
      {
         log.debug("[TestBackoffice::testDeployUndeployContentModel] Test disabilitato per la parte batch.");
         assertTrue(true);
      }
	  } catch (Exception e) {
		  log.debug("[TestBackoffice::testDeployUndeployContentModel] Eccezione", e);
		  assertTrue(false);
	  } finally{
		  stop();
		  log.debug("[TestBackoffice::testDeployUndeployContentModel] END");
	  }
  }

	public void testImportArchive(){
		try{
			log.debug("[TestBackoffice::testImportArchive] BEGIN");
 			if(!TEST_TARGET.equals("batch")){
			   long moveTime=System.currentTimeMillis();

            // Archivio da importare
            DataArchive da = new DataArchive();
            da.setContent( getBinary( zipArchive ) );
            da.setFormat( "zip" );
            da.setMappedContentTypePrefixedName(            "cm:content"       );
            da.setMappedContentNamePropertyPrefixedName(    "cm:name"          );
            da.setMappedContainerTypePrefixedName(          "cm:folder"        );
            da.setMappedContainerNamePropertyPrefixedName(  "cm:name"          );
            da.setMappedContainerAssocTypePrefixedName(     "cm:contains"      );
            da.setParentContainerAssocTypePrefixedName(     "cm:contains"      );

			log.debug("[TestBackoffice::testImportArchive] prima di chiamate importDataArchive");

            ecmEngineBackofficeDelegateImpl.importDataArchive( da, folderTest, defaultContext );

		    assertTrue(true);
			} else{
				log.debug("[TestBackoffice::testImportArchive] Test disabilitato per la parte batch.");
				assertTrue(true);
			}
		}catch(Exception e){
			log.error("[TestBackoffice::testImportArchive] ERROR " + e.getMessage());
			assertTrue(false);
		}finally{
			log.debug("[TestBackoffice::testImportArchive] END");
		}
	}

	public void testImportArchiveErrato(){
		try{
			log.debug("[TestBackoffice::testImportArchive] BEGIN");
			long moveTime=System.currentTimeMillis();

            // Archivio da importare
            DataArchive da = new DataArchive();
            da.setContent( getBinary( zipArchive ) );
            da.setFormat( "zzz" );
            da.setMappedContentTypePrefixedName(            "cm:content"       );
            da.setMappedContentNamePropertyPrefixedName(    "cm:name"          );
            da.setMappedContainerTypePrefixedName(          "cm:folder"        );
            da.setMappedContainerNamePropertyPrefixedName(  "cm:name"          );
            da.setMappedContainerAssocTypePrefixedName(     "cm:contains"      );
            da.setParentContainerAssocTypePrefixedName(     "cm:contains"      );

			log.debug("[TestBackoffice::testImportArchive] prima di chiamate importDataArchive");

            ecmEngineBackofficeDelegateImpl.importDataArchive( da, folderTest, defaultContext );

		    assertTrue(false);
		} catch (InvalidParameterException e) {
			assertTrue(true);
			log.debug("[TestBackoffice::testImportArchive] Parametro non valido: OK");
		}catch(Exception e){
			assertTrue(false);
			log.error("[TestBackoffice::testImportArchive] ERROR " + e.getMessage());
		}finally{
			log.debug("[TestBackoffice::testImportArchive] END");
		}
	}

	public void testCheckRepositoryIntegrity(){
		log.debug("[TestBackoffice::testCheckRepositoryIntegrity] BEGIN");
		try{
			IntegrityReport[] result=ecmEngineBackofficeDelegateImpl.checkRepositoryIntegrity(folderTest, defaultContext);
			if(result!=null){
				log.debug("[TestBackoffice::testCheckRepositoryIntegrity] Report di integrita' valido.");
				assertTrue(true);
			}
			else{
				log.debug("[TestBackoffice::testCheckRepositoryIntegrity] Report di integrita' nullo.");
				assertTrue(false);
			}
		}catch(NoSuchNodeException e){
			log.debug("[TestBackoffice::testCheckRepositoryIntegrity] Report di integrita' non eseguibile in quanto il nodo passato è nullo.");
			assertTrue(true);
		}catch(Exception e){
			log.error("[TestBackoffice::testCheckRepositoryIntegrity] ERROR",e);
			assertTrue(false);
		}finally{
			log.debug("[TestBackoffice::testCheckRepositoryIntegrity] END");
		}
	}

	/*
	public void testCreateTenant(){
		try{
			log.debug("[TestBackoffice::testCreateTenant] BEGIN");
			long moveTime=System.currentTimeMillis();

            Tenant oTenant = new Tenant();
            oTenant.setDomain( folderBko );          // Nome del tenant
            oTenant.setAdminPassword( "password" );  // Password
            oTenant.setEnabled(true);                // Attivo

            ecmEngineBackofficeDelegateImpl.createTenant( oTenant, defaultContext );

		    assertTrue(true);
		}catch(Exception e){
			assertTrue(false);
			log.error("[TestBackoffice::testCreateTenant] ERROR " + e.getMessage());
		}finally{
			log.debug("[TestBackoffice::testCreateTenant] END");
		}
	}
    //*/

  private String getUidCompanyHome(){
	  log.debug("[TestBackoffice::getUidCompanyHome] BEGIN");

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
		  log.debug("[TestBackoffice::getUidCompanyHome] chiamata al servizio xpathSearch");
		  results = response.getResultContentArray();

		  int sizeLista = results == null ? 0 : results.length;
		  log.debug("[TestBackoffice::getUidCompanyHome] estratto "+sizeLista+" record.");

		  if (sizeLista>0 && results[0] != null) {
			  uid=results[0].getUid();
			  log.debug("[TestBackoffice::getUidCompanyHome] Uid CompanyHome : "+results[0].getUid());
		  }
		  else log.debug("[TestBackoffice::getUidCompanyHome] Uid CompanyHome Non Trovato.");
	  } catch (Exception e) {
		  log.error("[TestBackoffice::getUidCompanyHome] Errore", e);
	  } finally {
		  log.debug("[TestBackoffice::getUidCompanyHome] END");
	  }
	  return uid;
  }


  private Node getFolderTest(String folder, String parentUid) {
	  log.debug("[TestBackoffice::getFolderTest] BEGIN");

	  Content content = new Content();
	  content.setPrefixedName("cm:"+folder);
	  content.setParentAssocTypePrefixedName("cm:contains");
	  content.setModelPrefixedName("cm:contentmodel");
	  content.setTypePrefixedName("cm:folder");

	  Property [] props = new Property[1];
	  props[0] = new Property();
	  props[0].setPrefixedName("cm:name");
	  props[0].setDataType("text");
	  props[0].setMultivalue(false);
	  props[0].setValues(new String [] { folder });

	  content.setProperties(props);

	  Node parentNode = new Node();
	  parentNode.setUid(parentUid);

	  Node nodo = null;

	  try {
		  nodo = ecmEngineDelegateImpl.createContent(parentNode, content, defaultContext);
		  log.debug("[TestBackoffice::getFolderTest] chiamata al servizio createContent");
		  log.debug("[TestBackoffice::getFolderTest] Uid Folder creato: "+nodo.getUid());
	  } catch (Exception e) {
		  log.error("[TestBackoffice::getFolderTest] Errore", e);
	  } finally{
		  log.debug("[TestBackoffice::getFolderTest] END");
	  }
	  return nodo;
  }

  private void readProperties(){

	  InputStream is = null;
	  Properties properties = null;
	  log.debug("[TestBackoffice::readProperties] BEGIN");
	  try{

		  is = this.getClass().getResourceAsStream("/" + ECMENGINE_TEST_PROPERTIES_FILE);
		  properties= new Properties();
		  if (is != null) {
			  properties.load(is);
			  log.debug("[TestBackoffice::readProperties] File di properties caricato : "+ECMENGINE_TEST_PROPERTIES_FILE);

			  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
			  dateStr = sdf.format(new Date());

			  folderParent = properties.getProperty(ECMENGINE_TEST_FOLDER_PARENT);
			  folder = properties.getProperty(ECMENGINE_TEST_FOLDER);
			  contenuto = properties.getProperty(ECMENGINE_TEST_CONTENT);
			  folderBko = properties.getProperty(ECMENGINE_TEST_FOLDER_BKO)+"_"+dateStr;
			  userprop=properties.getProperty(ECMENGINE_TEST_USER_BKO);

			  zipArchive = properties.getProperty(ECMENGINE_TEST_ZIP_ARCHIVE);
			  contentModel = properties.getProperty(ECMENGINE_TEST_CONTENT_MODEL);

			  tenant = properties.getProperty(ECMENGINE_TEST_TENANT);
			  if (tenant != null && tenant.length() > 0) {
				  if (!tenant.startsWith("@")) {
					  tenant = "@" + tenant;
				  }
			  } else {
				  tenant = "";
			  }
			  String username = properties.getProperty(ECMENGINE_TEST_USERNAME);
			  String password = properties.getProperty(ECMENGINE_TEST_PASSWORD);
			  if (username != null && username.length() > 0 && password != null && password.length() > 0) {
				  defaultContext.setUsername(username);
				  defaultContext.setPassword(password);
			  }
			  defaultContext.setUsername(defaultContext.getUsername()+tenant);

			  String repository = properties.getProperty(ECMENGINE_TEST_REPOSITORY);
			  if (repository != null && repository.length() > 0) {
				  REPOSITORY = repository;
			  }
			  defaultContext.setRepository(REPOSITORY);

			  try {
			  	  SLEEP_TIME = Integer.parseInt(properties.getProperty(ECMENGINE_TEST_SLEEP_TIME));
			  } catch(NumberFormatException nfe) {}

				TEST_TARGET=properties.getProperty(ECMENGINE_TEST_TARGET);

			  log.debug("[TestBackoffice::readProperties] folderParent vale: " + folderParent);
			  log.debug("[TestBackoffice::readProperties] folder vale: " + folder);
			  log.debug("[TestBackoffice::readProperties] contenuto vale: " + contenuto);
			  log.debug("[TestBackoffice::readProperties] folderBko vale: " + folderBko);
			  log.debug("[TestBackoffice::readProperties] zipArchive vale: " + zipArchive);
			  log.debug("[TestBackoffice::readProperties] TEST_TARGET vale: " + TEST_TARGET);
		  }
		  else{
			  log.error("[TestBackoffice::readProperties] Si sono verificati problemi nella lettura " +
					  "del file di properties: "+ECMENGINE_TEST_PROPERTIES_FILE);
		  }
	  }catch (Exception e) {
		  log.error("[TestBackoffice::readProperties] Errore", e);
	  }finally{
		  log.debug("[TestBackoffice::readProperties] END");
	  }
  }

	private String createFolder(String parent, String name, OperationContext ctx)
	{
		log.debug("[TestBackoffice::createFolder] BEGIN");

		Node parentNode = new Node();
		parentNode.setUid(parent);

		Content content = new Content();
		content.setPrefixedName("cm:" + name);
		content.setParentAssocTypePrefixedName("cm:contains");
		content.setModelPrefixedName("cm:contentmodel");
		content.setTypePrefixedName("cm:folder");

		Property [] props = new Property[1];
		props[0] = new Property();
		props[0].setPrefixedName("cm:name");
		props[0].setDataType("text");
		props[0].setMultivalue(false);
		props[0].setValues(new String [] { name });

		content.setProperties(props);
		Node result=null;
		String uid=null;
		try {
			result = ecmEngineDelegateImpl.createContent(parentNode, content, ctx);
			uid = result.getUid();
			log.debug("[TestBackoffice::createFolder] CREAZIONE COMPLETATA");
			log.debug("[TestBackoffice::createFolder] Nome folder: " + name);
			log.debug("[TestBackoffice::createFolder] Uid padre: " + parent);
			log.debug("[TestBackoffice::createFolder] Uid nodo: " + uid);

		} catch (Exception e) {
			log.error("[TestBackoffice::createFolder] Errore", e);
		}finally{
			log.debug("[TestBackoffice::createFolder] END");
		}
		return uid;
	}
	/*
	private String getTenantUsername(String username, OperationContext context) {
		String result = username;
		if (context.getUsername().indexOf("@") > 0 && username.indexOf("@") < 0) {
			String tenant = context.getUsername().substring(context.getUsername().indexOf("@"));
			result = username + tenant;
		}
		return result;
	}
	 */

   private byte[] getBinary( String cFile ) throws Exception {
      FileInputStream fInput = new FileInputStream( cFile );
      byte[] aByte = new byte[ fInput.available() ];
      fInput.read( aByte );
      fInput.close();
      return aByte;
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

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

package it.doqui.index.ecmengine.business.job.backup;

import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.business.audit.AuditBusinessInterface;
import it.doqui.index.ecmengine.business.job.backup.util.MetaDati;
import it.doqui.index.ecmengine.business.job.backup.util.MetaDatiAutoreValore;
import it.doqui.index.ecmengine.business.job.backup.util.MetaDatiOggettoValore;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.exception.ServerInfoException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MetaDataBackupJob implements Job, EcmEngineConstants{

    private static Log logger = LogFactory.getLog(ECMENGINE_JOB_BACKUP_METADATI_LOG_CATEGORY);

	/** Lo stopwatch da utilizzare per la registrazione dei tempi. */
	private StopWatch stopwatch;

	private AuditBusinessInterface auditManager;

	private AuthenticationComponent authenticationComponent;

	private TransactionService transactionService;

	private SearchService searchService;

    private NodeService nodeService;

	private NamespaceService namespaceService;

	private DictionaryService dictionaryService;

	private String contentDir;

	private static final char PREFIXED_NAME_SEPARATOR = ':';

	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		//il job deve effettuare il backup dei metadati modificati in giornata
		// quei metadati che hanno:
		//property name="cm:modified"(<aspect name="cm:auditable">) impostata alla data odierna
		// meglio un aspect apposito in cui memorizzare se il content e` stato modificato
		// Quindi aggiungere un Aspect apposito

		Authentication auth = null;

		try {
			logger.debug("[MetaDataBackupJob::execute] BEGIN");

			RepositoryManager.setCurrentRepository(
					RepositoryManager.getInstance().getDefaultRepository().getId());

			final Object object = ctx.getJobDetail().getJobDataMap().get(ECMENGINE_AUTHENTICATION_BEAN);
			if (object == null
					|| !(object instanceof AuthenticationComponent)) {
				throw new IllegalArgumentException("MetaDataBackupJob must contain valid " +
				"'authenticationComponent' reference");
			}
			this.authenticationComponent = (AuthenticationComponent) object;

			auth = AuthenticationUtil.getCurrentAuthentication();

			// authenticate as the system user
			this.authenticationComponent.setSystemUserAsCurrentUser();

			logger.debug("[MetaDataBackupJob::execute] Job: " +
					ctx.getJobDetail().getName() + " - Trigger: " + ctx.getTrigger().getName());

			init(ctx);

			final RetryingTransactionCallback<Object> backupWork = new RetryingTransactionCallback<Object>() {
				public Object execute() throws Exception
				{
					logger.info("[MetaDataBackupJob::execute] " +
							"Inizio backup metadati sul Repository: " +
							RepositoryManager.getCurrentRepository());

					backupModifiedMetaData();

					logger.info("[MetaDataBackupJob::execute] " +
							"Fine backup metadati sul Repository: "	+
							RepositoryManager.getCurrentRepository());
					return null;
				}
			};

			transactionService.getRetryingTransactionHelper().doInTransaction(backupWork);

		} catch (Exception e) {
			String msg = "";
			if (e instanceof UnknownHostException) {
				logger.error("[MetaDataBackupJob::execute] Failed to get server IP address.");
				msg = "Failed to get server IP address";
			} else if (e instanceof ServerInfoException) {
				logger.error("[MetaDataBackupJob::execute] Failed to get server info from alf_server.");
				msg = "Failed to get server info from alf_server";
			} else if (e instanceof FileNotFoundException) {
				logger.error("[MetaDataBackupJob::execute] Failed to get xml file with backup metadata.");
				msg = "Failed to get xml file with backuped metadata";
			} else if (e instanceof JiBXException) {
				logger.error("[MetaDataBackupJob::execute] Failed to marshall/unmarshall xml file with metadata.");
				msg = "Failed to marshall/unmarshall xml file with metadata";
			} else if (e instanceof DictionaryRuntimeException) {
				logger.error("[MetaDataBackupJob::execute] Error resolving QName to prefix name.");
				msg = "Error resolving QName to prefix name";
			} else {
				logger.error("[MetaDataBackupJob::execute] Exception nell'esecuzione del Job: "+e.getMessage());
				msg = e.getMessage();
			}
			throw new JobExecutionException("Exception : " + msg, e);
		} finally {
			if (auth != null) {
				this.authenticationComponent.setCurrentAuthentication(auth);
			}
			logger.debug("[MetaDataBackupJob::execute] END");
		}
	}

	private void backupModifiedMetaData()
	throws FileNotFoundException, JiBXException, DictionaryRuntimeException{

		start(); // Avvia stopwatch

		List<NodeRef> listaNodi = null;
		Map<QName,Serializable> propMap = null;
		try{
			logger.debug("[MetaDataBackupJob::backupModifiedMetaData] BEGIN");

			//ricerca nodi con metadati modificati in data odierna
			listaNodi = searchNodeWithModifiedMetaData();

			//PropertyDefinition propDef = null;
			if (listaNodi != null){
				logger.debug("[MetaDataBackupJob::backupModifiedMetaData] Numero Nodi con metadati modificati oggi : "
						+ listaNodi.size());

				for (NodeRef nodeRef : listaNodi) {
					propMap = this.nodeService.getProperties(nodeRef);

					String logCtx = "Uid: "+nodeRef.getId();

					generaXmlMetaDati(nodeRef,propMap);

					dumpElapsed("MetaDataBackupJob", "backupModifiedMetaData",logCtx,"Generato file xml.");

                    //INSERIMENTO AUDIT
					insertAudit("MetaDataBackupJob", "backupModifiedMetaData", logCtx, nodeRef.getId() ,
							"Generazione file xml per disaster recovery: "+(nodeRef.getId()+".xml"));


//					for(QName prop : propMap.keySet()){
//
//						propDef = dictionaryService.getProperty(prop);
//						//se si decide di aggiungere l'attributo backuped solamente
//						//alle property che devono essere replicate nello storage
//						//allora si fa questo ulteriore controllo "propDef.isBackuped()"
//
//
//						//if (propDef.isBackuped()){
//
//						//generaXmlMetaDati(nodeRef,prop);
//
//						logCtx = "Uid: "+nodeRef.getId() +"-- Prop: "+prop.getLocalName();
//						dumpElapsed("MetaDataBackupJob", "backupModifiedMetaData",logCtx,"Generato file xml.");
//
//						//end-if }
//					}
				}
			}
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[MetaDataBackupJob::backupModifiedMetaData] END");
		}
	}


	private String getContentUrl(Map<QName,Serializable> propertyMap){

		String url = null;
		try {
			logger.debug("[MetaDatiManager::getContentUrl] BEGIN");

			for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet()) {

				if (entry.getValue() instanceof ContentData) {
					ContentData data = (ContentData) entry.getValue();

					url = data.getContentUrl();
				}
			}
		} finally {
			logger.debug("[MetaDatiManager::getContentUrl] Url Content : "+url);
			logger.debug("[MetaDatiManager::getContentUrl] END");
		}
		return url;
	}

	private void generaXmlMetaDati(NodeRef nodeRef, Map<QName,Serializable> propertyMap)
	throws JiBXException, FileNotFoundException, DictionaryRuntimeException {
		logger.debug("[MetaDatiManager::generaXmlMetaDati] BEGIN");

		String nomeFile;

		//String folderFileMetaDati = "/usr/prod/fs/alfresco2/metadati";

        //contentDir
		//dir.contenstore = /usr/prod/fs/alfresco2/content/contentstore
		String folderContent = this.contentDir ;

		logger.debug("[MetaDatiManager::generaXmlMetaDati] Directory Contenuti: " + folderContent);

		try {

			nomeFile = nodeRef.getId() + ".xml";
			logger.debug("[MetaDatiManager::generaXmlMetaDati] Nome file xml da ricercare : "+nomeFile);

			String urlRelative = getContentUrl(propertyMap);

			if (urlRelative!=null) {
				// Se il file xml dei metadati deve essere memorizzato nella stessa cartella dove e` memorizzato
				// il content ,allora bisogna individiare il path dove tale content e` memorizzato e controllare se
				// il file xml e` presente o no in questa cartella; urlRelative contiene il path relativo
				// del content; bisogna sapere il path fisico su fs di partenza che e` contenuto nel file di property
				// custom-data-location.properties
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Path relativo content : "+urlRelative);

				// store://2007/11/14/14/5/53d84b63-92b2-11dc-bd5f-372cc7f30666.bin

				// /usr/prod/fs/alfresco2/content/contentstore/2007/11/14/14/5

				// store:/
				urlRelative = urlRelative.substring(7);
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Url relativo senza store: "+urlRelative);
				// /usr/prod/fs/alfresco2/content/contentstore/2007/11/14/14/5/53d84b63-92b2-11dc-bd5f-372cc7f30666.bin

				folderContent = folderContent + urlRelative;
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Url comprensivo del binario : "+folderContent);

				int idLength = nodeRef.getId().length() + 4;
				int l = folderContent.length();
				l = l - idLength;

				folderContent = folderContent.substring(0,l);
				// /usr/prod/fs/alfresco2/content/contentstore/2007/11/14/14/5/
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Path cartella content : "+folderContent);

			} else {
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Il content modificato non e' un documento");
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Esco dal Job di backup");
				return;
			}

			boolean creato = false;
			final File file = new File(folderContent);

			if (!file.exists()) {
				file.mkdir();
			}

			if (file.isDirectory()) {
				final File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (nomeFile.equalsIgnoreCase(files[i].getName())) {
						creato = true;
						logger.debug("[MetaDatiManager::generaXmlMetaDati] File xml gia creato.");
						break;
					}
				}
			}

			final IBindingFactory factory = BindingDirectory.getFactory(MetaDati.class);
			IMarshallingContext mctx = null;
			MetaDati metaDati = null;
			MetaDatiOggettoValore oggetto = null;
			MetaDatiAutoreValore autore = null;

			final String path = folderContent + nomeFile;

			if (!creato) {
				//file xml dei metadati per il nodo in esame deve ancora essere creato

				logger.debug("[MetaDatiManager::generaXmlMetaDati] File xml da generare");

				mctx = factory.createMarshallingContext();
				mctx.setIndent(1);

				metaDati = new MetaDati();

				//Ciclo sulle proprieta`
				for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet()) {

					final QName propName = entry.getKey();
					final Serializable valore = entry.getValue();

					PropertyDefinition propDef = this.dictionaryService.getProperty(propName);

               		if (logger.isDebugEnabled()) {
		    			logger.debug("[MetaDatiManager::generaXmlMetaDati] Nome Proprieta : "+propName.toString());
			    		logger.debug("[MetaDatiManager::generaXmlMetaDati] Valore Proprieta : "
							+ (valore!=null ? valore.toString() : "null"));
                    }

					//MetaDatiAutoreValore valoreAutore = null;
					//MetaDatiOggettoValore valoreOggetto = null;

					if (propDef.isMultiValued()){
						//valore multiplo
						logger.debug("[MetaDatiManager::generaXmlMetaDati] Property multivalue");
						String [] values = null;

						String value = null;

						if (valore instanceof Collection<?>) {
							final Collection<?> valuesCollection = (Collection<?>) valore;
							values = new String[valuesCollection.size()];

							int j = 0;
							for (Object o : valuesCollection) {
								values[j] = o.toString();
								j++;
							}
						} else {
							value = "Type not supported: " + valore.getClass().getName();
							logger.debug("[MetaDatiManager::generaXmlMetaDati] "+value);
						}

						if ("side-doc:autore".equalsIgnoreCase(resolveQNameToPrefixName(propName))){

							if (values!=null){
								for(int i=0;i<values.length;i++){
									autore = new MetaDatiAutoreValore();
									autore.setCognome(values[i]);
									autore.setNome(values[i]);
									autore.setDataInizio(new Date());
									metaDati.addAutore(autore);
								}
							} else {
								autore = new MetaDatiAutoreValore();
								autore.setCognome("Type not supported");
								autore.setNome("Type not supported");
								autore.setDataInizio(new Date());
								metaDati.addAutore(autore);

							}
						} else if ("cm:name".equalsIgnoreCase(resolveQNameToPrefixName(propName))) {

							logger.debug("[MetaDatiManager::generaXmlMetaDati] Property cm:name multivalue");
							if (values!=null) {
								for(int i=0;i<values.length;i++){
									oggetto = new MetaDatiOggettoValore();
									oggetto.setCampo(values[i]);
									oggetto.setDataInizio(new Date());

									metaDati.addOggetto(oggetto);
								}
							} else {
								oggetto = new MetaDatiOggettoValore();
								oggetto.setCampo("Type not supported");
								oggetto.setDataInizio(new Date());

								metaDati.addOggetto(oggetto);
							}
						}
					} else {
						//singolo valore
						logger.debug("[MetaDatiManager::generaXmlMetaDati] Property single value");

						if ("side-doc:oggetto".equalsIgnoreCase(resolveQNameToPrefixName(propName))) {
							oggetto = new MetaDatiOggettoValore();

							if (valore == null) {
								oggetto.setCampo("- null -");
								oggetto.setDataInizio(new Date());

							} else if (valore instanceof String
									|| valore instanceof Long
									|| valore instanceof Integer
									|| valore instanceof Date
									|| valore instanceof MLText
									|| valore instanceof ContentData
									|| valore instanceof Boolean
									|| valore instanceof NodeRef) {

								oggetto.setCampo(valore.toString());
								oggetto.setDataInizio(new Date());
							} else {
								oggetto.setCampo("Type not supported: " + valore.getClass().getName());
								oggetto.setDataInizio(new Date());

							}

							metaDati.addOggetto(oggetto);

						} else {
							if ("cm:name".equalsIgnoreCase(resolveQNameToPrefixName(propName))) {
								logger.debug("[MetaDatiManager::generaXmlMetaDati] " +
										"Property cm:name single value");
								oggetto = new MetaDatiOggettoValore();

								if(valore == null) {
									oggetto.setCampo("- null -");
									oggetto.setDataInizio(new Date());
								}
								else if (valore instanceof String
										|| valore instanceof Long
										|| valore instanceof Integer
										|| valore instanceof Date
										|| valore instanceof MLText
										|| valore instanceof ContentData
										|| valore instanceof Boolean
										|| valore instanceof NodeRef) {

									oggetto.setCampo(valore.toString());
									logger.debug("[MetaDatiManager::generaXmlMetaDati] " +
											"Property cm:name --> value : "+valore.toString());
									oggetto.setDataInizio(new Date());

								} else {
									oggetto.setCampo("Type not supported: " + valore.getClass().getName());
									oggetto.setDataInizio(new Date());
								}

								metaDati.addOggetto(oggetto);
							} else if ("sys:node-uuid".equalsIgnoreCase(resolveQNameToPrefixName(propName))) {

								logger.debug("[MetaDatiManager::generaXmlMetaDati] " +
								"Property sys:node-uuid single value");
								oggetto = new MetaDatiOggettoValore();

								if (valore == null) {
									oggetto.setCampo("- null -");
									oggetto.setDataInizio(new Date());
								} else if (valore instanceof String
										|| valore instanceof Long
										|| valore instanceof Integer
										|| valore instanceof Date
										|| valore instanceof MLText
										|| valore instanceof ContentData
										|| valore instanceof Boolean
										|| valore instanceof NodeRef) {

									oggetto.setCampo(valore.toString());
									logger.debug("[MetaDatiManager::generaXmlMetaDati] " +
											"Property sys:node-uuid --> valore : "+valore.toString());

									oggetto.setDataInizio(new Date());

								} else {
									oggetto.setCampo("Type not supported: " + valore.getClass().getName());
									oggetto.setDataInizio(new Date());
								}
								metaDati.addOggetto(oggetto);
							}
						}
					}
				}

				mctx.marshalDocument(metaDati, "UTF-8", null, new FileOutputStream(path));
				logger.debug("[MetaDatiManager::generaXmlMetaDati] Marshall file xml : " + path);
			} else {

				logger.debug("[MetaDatiManager::generaXmlMetaDati] File xml gia` creato, devo prima leggerlo");

				final IUnmarshallingContext uctx = factory.createUnmarshallingContext();

				final Object obj = uctx.unmarshalDocument(new FileInputStream(path), null);

				if (obj instanceof MetaDati) {
					logger.debug("[MetaDatiManager::generaXmlMetaDati] OK : obj e' un'istanza della classe MetaDati");

					metaDati = (MetaDati) obj ;

					for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet()) {

						final QName propName = entry.getKey();
						final Serializable valore = entry.getValue();

						PropertyDefinition propDef = dictionaryService.getProperty(propName);

                   		if (logger.isDebugEnabled()) {
	    					logger.debug("[MetaDatiManager::generaXmlMetaDati] Nome Proprieta`: " + propName.toString());
		    				logger.debug("[MetaDatiManager::generaXmlMetaDati] Valore Proprieta`: "
								+ ((valore != null) ? valore.toString() : "null"));
                        }

						if (propDef.isMultiValued()){
							//multi value
							logger.debug("[MetaDatiManager::generaXmlMetaDati] property multivalue");

							String [] values = null;
							String value = null;

							if (valore instanceof Collection<?>) {
								Collection<?> valuesCollection = (Collection<?>) valore;
								values = new String[valuesCollection.size()];

								int j = 0;
								for (Object o : valuesCollection) {
									values[j] = o.toString();
									j++;
								}
							} else {
								value = "Type not supported: " + valore.getClass().getName();
								logger.debug("[MetaDatiManager::generaXmlMetaDati] "+value);
							}

							if ("side-doc:autore".equalsIgnoreCase(resolveQNameToPrefixName(propName))){

								if (values != null) {

									for (int i=0; i<values.length; i++){
										autore = new MetaDatiAutoreValore();
										autore.setCognome(values[i]);
										autore.setNome(values[i]);
										autore.setDataInizio(new Date());
										metaDati.addAutore(autore);
									}
								} else {
									autore = new MetaDatiAutoreValore();
									autore.setCognome("Type not supported");
									autore.setNome("Type not supported");
									autore.setDataInizio(new Date());
									metaDati.addAutore(autore);
								}

							} else if ("cm:name".equalsIgnoreCase(resolveQNameToPrefixName(propName))) {
								logger.debug("[MetaDatiManager::generaXmlMetaDati] Property cm:name multivalue");
								if (values != null){

									for (int i=0; i<values.length; i++) {
										oggetto = new MetaDatiOggettoValore();
										oggetto.setCampo(values[i]);
										oggetto.setDataInizio(new Date());
										metaDati.addOggetto(oggetto);
									}

								} else {
									oggetto = new MetaDatiOggettoValore();
									oggetto.setCampo("Type not supported");
									oggetto.setDataInizio(new Date());
									metaDati.addOggetto(oggetto);
								}
							}
						} else {
							//single value
							logger.debug("[MetaDatiManager::generaXmlMetaDati] property single value");
							if("side-doc:oggetto".equalsIgnoreCase(resolveQNameToPrefixName(propName))){

								oggetto = new MetaDatiOggettoValore();

								if (valore == null) {
									oggetto.setCampo("- null -");
									oggetto.setDataInizio(new Date());

								} else if (valore instanceof String
										|| valore instanceof Long
										|| valore instanceof Integer
										|| valore instanceof Date
										|| valore instanceof MLText
										|| valore instanceof ContentData
										|| valore instanceof Boolean
										|| valore instanceof NodeRef) {

									oggetto.setCampo(valore.toString());
									oggetto.setDataInizio(new Date());

								} else {
									oggetto.setCampo("Type not supported: " + valore.getClass().getName());
									oggetto.setDataInizio(new Date());
								}

								metaDati.addOggetto(oggetto);
							} else {
								if ("cm:name".equalsIgnoreCase(resolveQNameToPrefixName(propName))) {
									logger.debug("[MetaDatiManager::generaXmlMetaDati] Property cm:name " +
											"single value");
									oggetto = new MetaDatiOggettoValore();

									if(valore == null) {
										oggetto.setCampo("- null -");
										oggetto.setDataInizio(new Date());
									} else if (valore instanceof String
											|| valore instanceof Long
											|| valore instanceof Integer
											|| valore instanceof Date
											|| valore instanceof MLText
											|| valore instanceof ContentData
											|| valore instanceof Boolean
											|| valore instanceof NodeRef) {

										oggetto.setCampo(valore.toString());
										oggetto.setDataInizio(new Date());
										logger.debug("[MetaDatiManager::generaXmlMetaDati] " +
												"Property cm:name --> value : "+valore.toString());

									} else {
										oggetto.setCampo("Type not supported: " + valore.getClass().getName());
										oggetto.setDataInizio(new Date());
									}

									metaDati.addOggetto(oggetto);
								} else if("sys:node-uuid".equalsIgnoreCase(resolveQNameToPrefixName(propName))){

									logger.debug("[MetaDatiManager::generaXmlMetaDati] Property sys:node-uuid");
									oggetto = new MetaDatiOggettoValore();

									if(valore == null) {
										oggetto.setCampo("- null -");
										oggetto.setDataInizio(new Date());
									} else if (valore instanceof String
											|| valore instanceof Long
											|| valore instanceof Integer
											|| valore instanceof Date
											|| valore instanceof MLText
											|| valore instanceof ContentData
											|| valore instanceof Boolean
											|| valore instanceof NodeRef) {

										oggetto.setCampo(valore.toString());
										logger.debug("[MetaDatiManager::generaXmlMetaDati] " +
												"Property sys:node-uuid --> valore : "+valore.toString());
										oggetto.setDataInizio(new Date());

									} else {
										oggetto.setCampo("Type not supported: " + valore.getClass().getName());
										oggetto.setDataInizio(new Date());
									}
									metaDati.addOggetto(oggetto);
								}
							}
						}
					}

					mctx = factory.createMarshallingContext();
					mctx.setIndent(1);

					mctx.marshalDocument(metaDati, "UTF-8", null,new FileOutputStream(path));
					logger.debug("[MetaDatiManager::generaXmlMetaDati] Marshall file xml : "+path);

				} else {
					logger.error("[MetaDatiManager::generaXmlMetaDati] Errore :" +
					" obj non e` una istanza della classe Metadati");
					throw new JiBXException("UnmarshalDocument Errato: " +
							"obj non e' una istanza della classe Metadati");
				}
			}
		} finally {
			logger.debug("[MetaDatiManager::generaXmlMetaDati] END");
		}
	}



	private List<NodeRef> searchNodeWithModifiedMetaData(){
		logger.debug("[MetaDataBackupJob::searchNodeWithModifiedMetaData] BEGIN");
		ResultSet resultSet = null;
		List<NodeRef> listaNodi = null;

		try{
		    logger.debug("[MetaDataBackupJob::searchNodeWithModifiedMetaData] Ricerca nel repository : "
					+ RepositoryManager.getCurrentRepository());

			final StoreRef spacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			final SearchParameters searchParams = new SearchParameters();
			searchParams.addStore(spacesStore);
			searchParams.setLimitBy(LimitBy.UNLIMITED);
			searchParams.setLimit(0);
			searchParams.setLanguage(SearchService.LANGUAGE_LUCENE);

			//aspect name="ecm-sys:modified"
			//<property name="ecm-sys:dataModifica">
			// <type>d:datetime</type>
			final Date dataOdierna = new Date();
			//ricerco i nodi le cui properties sono state modificate in giornata
			//"yyyy-MM-dd'T'HH:mm:ss.sssZ"

			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(dataOdierna);
			date = date + "T00:00:00.000Z";

			final StringBuffer query = new StringBuffer("@ecm-ssys\\:dataModifica:\"" + date + "\"");
			searchParams.setQuery(query.toString());

			logger.debug("[MetaDataBackupJob::searchNodeWithModifiedMetaData] searchService is "+ ((this.searchService != null) ? " <> null" :" null" ));
			logger.debug("[MetaDataBackupJob::searchNodeWithModifiedMetaData] Query : " + searchParams.getQuery());
			resultSet = this.searchService.query(searchParams);
			dumpElapsed("MetaDataBackupJob", "searchNodeWithModifiedMetaData", query.toString(), "Ricerca Nodi modificati terminata.");
			logger.debug("[MetaDataBackupJob::searchNodeWithModifiedMetaData] Risultati trovati: " + resultSet.length());

			if (resultSet.length() > 0) {
				listaNodi = resultSet.getNodeRefs();
			}
		} finally {
			if(resultSet != null) {
				resultSet.close();
			}
			logger.debug("[MetaDataBackupJob::searchNodeWithModifiedMetaData] END");
		}
		return listaNodi;
	}

	private String resolveQNameToPrefixName(QName qname) {
	    logger.debug("[MetaDataBackupJob::resolveQNameToPrefixName] BEGIN");

		String result = null;
		try {
    		if (logger.isDebugEnabled()) {
	    		logger.debug("[MetaDataBackupJob::resolveQNameToPrefixName] Resolving to prefix name: " + qname.toString());
            }

			final String [] nameParts = QName.splitPrefixedQName(qname.toPrefixString(this.namespaceService));

			result = nameParts[0] + PREFIXED_NAME_SEPARATOR + nameParts[1];

    		if (logger.isDebugEnabled()) {
		    	logger.debug("[MetaDataBackupJob::resolveQNameToPrefixName] Prefix name: " + result);
            }
		} catch (RuntimeException e) {
			logger.warn("[MetaDataBackupJob::resolveQNameToPrefixName] " +
					"Error resolving to prefix name \"" + qname.toString() + "\": " + e.getMessage());
			throw new RuntimeException();	// FIXME!!!
		} finally {
			logger.debug("[MetaDataBackupJob::resolveQNameToPrefixName] END");
		}

		return result;
	}

	private void init(JobExecutionContext ctx) {
		try {
			logger.debug("[MetaDataBackupJob::init] BEGIN");

			JobDataMap jobData = ctx.getJobDetail().getJobDataMap();

			//extract the object to use
			Object object = null;

			//contentDirectory
			object = jobData.get(ECMENGINE_CONTENT_DIRECTORY);
			if (object == null || !(object instanceof String)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid 'contentDirectory' property.");
			}
			this.contentDir = (String) object;

			logger.debug("[MetaDataBackupJob::init] Directory dei contenuti : " + this.contentDir);

			//object = jobData.get("nodeService");
			object = jobData.get(ECMENGINE_NODE_SERVICE_BEAN);
			if (object == null || !(object instanceof NodeService)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid 'nodeService' reference.");
			}
			this.nodeService = (NodeService) object;

			//object = jobData.get("transactionService");
			object = jobData.get(ECMENGINE_TRANSACTION_SERVICE_BEAN);
			if (object == null || !(object instanceof TransactionService)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid 'transactionService' reference.");
			}
			this.transactionService = (TransactionService) object;

			//object = jobData.get("searchService");
			object = jobData.get(ECMENGINE_SEARCH_SERVICE_BEAN);
			if (object == null || !(object instanceof SearchService)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid 'searchService' reference.");
			}
			this.searchService = (SearchService) object;


			//object = jobData.get("namespaceService");
			object = jobData.get(ECMENGINE_NAMESPACE_SERVICE_BEAN);

			if (object == null || !(object instanceof NamespaceService)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid 'namespaceService' reference.");
			}
			this.namespaceService = (NamespaceService) object;

			object = jobData.get(ECMENGINE_DICTIONARY_SERVICE_BEAN);
			if (object == null || !(object instanceof DictionaryService)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid 'dictionaryService' reference.");
			}
			this.dictionaryService = (DictionaryService) object;

//			object = jobData.get("repository-properties");
//			if (object == null || !(object instanceof PropertyPlaceholderConfigurer)) {
//			throw new IllegalArgumentException(
//			"MetaDataBackupJob must contain valid 'repository-properties' reference");
//			}
//			propConfig = (PropertyPlaceholderConfigurer) object;

			object = jobData.get(ECMENGINE_AUDIT_MANAGER_BEAN);
			if (object == null || !(object instanceof AuditBusinessInterface)) {
				throw new IllegalArgumentException(
						"MetaDataBackupJob must contain valid '"+ECMENGINE_AUDIT_MANAGER_BEAN+"' reference.");
			}
			this.auditManager = (AuditBusinessInterface) object;
		} finally {
			logger.debug("[MetaDataBackupJob::init] END");
		}
	}

	private void insertAudit(String className, String methodName, String logContext, String idOggetto, String descrizioneOggetto) {
		logger.debug("[MetaDataBackupJob::insertAudit] BEGIN");
		try {
			OperazioneAudit operazioneAudit = new OperazioneAudit();
			operazioneAudit.setUtente("jobQuartz");
			operazioneAudit.setNomeOperazione(methodName);
			operazioneAudit.setServizio(className);
			operazioneAudit.setFruitore("jobQuartz");
			operazioneAudit.setDataOra(new Date());
			operazioneAudit.setIdOggetto(idOggetto);
			operazioneAudit.setTipoOggetto(descrizioneOggetto);
			auditManager.insertAudit(operazioneAudit);

			dumpElapsed(className, methodName, logContext, "Audit inserito.");

			logger.debug("[" + className + "::" + methodName + "] Audit inserito.");
		} catch (Exception e) {
			logger.error("[" + className + "::" + methodName + "] Errore nell'inserimento Audit: " + e.getMessage());
		} finally {
			logger.debug("[MetaDataBackupJob::insertAudit] END");
		}
	}

	/** Azzera e avvia la misurazione dei tempi da parte dello stopwatch. */
	protected void start() {
		stopwatch = new StopWatch(ECMENGINE_STOPWATCH_LOG_CATEGORY);
		stopwatch.start();
	}

	/** Arresta la misurazione dei tempi da parte dello stopwatch. */
	protected void stop() {
		stopwatch.stop();
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
		stopwatch.dumpElapsed(className, methodName, ctx, message);
	}
}

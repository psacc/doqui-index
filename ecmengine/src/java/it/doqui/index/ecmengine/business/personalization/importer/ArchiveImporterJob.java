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

package it.doqui.index.ecmengine.business.personalization.importer;

import it.doqui.index.ecmengine.business.job.JobBusinessInterface;
import it.doqui.index.ecmengine.business.job.dto.BatchJob;
import it.doqui.index.ecmengine.business.job.dto.BatchJobParam;
import it.doqui.index.ecmengine.business.job.util.EncryptionHelper;
import it.doqui.index.ecmengine.business.job.util.JobStatus;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;

import it.doqui.index.ecmengine.dto.backoffice.DataArchive;

import it.doqui.index.ecmengine.exception.EcmEngineException;
import org.alfresco.service.cmr.security.AuthenticationService;

import static it.doqui.index.ecmengine.util.EcmEngineConstants.*;
import static it.doqui.index.ecmengine.business.personalization.importer.ArchiveImporterJobConstants.*;

import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

import javax.transaction.RollbackException;

import org.alfresco.util.TempFileProvider;
import it.doqui.index.ecmengine.dto.OperationContext;

import java.util.Map;
import java.util.List;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashMap;
import javax.activation.MimetypesFileTypeMap;
import javax.transaction.UserTransaction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.nio.channels.FileChannel;
import java.io.IOException;

/**
 * Classe che implementa il job ...
 *
 * @author DoQui
 *
 */
public class ArchiveImporterJob implements Job {

	// Logger
	private static Log logger = LogFactory.getLog(ECMENGINE_ROOT_LOG_CATEGORY);

	// Variabile privata per verificare se il JOB e' in esecuzione
	// Occorre infatti l'uso in sigleton di archive importer
	private static boolean running = false;

	// Service da utilizzare per l'importazione dei contenuti
	private TransactionService  transactionService;
	private ContentService      contentService;
	private NodeService         nodeService;
	private NamespaceService    namespaceService;
	private AuthenticationService         authenticationService;

	/** Tipo predefinito per i file. */
	public static final String DEFAULT_CONTENT_TYPE				= "cm:content";

	/** Tipo predefinito per i folder. */
	public static final String DEFAULT_CONTAINER_TYPE			= "cm:folder";

	/** Property contenente il nome dei file. */
	public static final String DEFAULT_CONTENT_NAME_PROPERTY	= "cm:name";

	/** Property contenente il nome dei folder. */
	public static final String DEFAULT_CONTAINER_NAME_PROPERTY	= "cm:name";

	/** Tipo di associazione predefinito per i folder. */
	public static final String DEFAULT_CONTAINER_ASSOC_TYPE		= "cm:contains";

	/** Tipo di associazione predefinito per i file. */
	public static final String DEFAULT_PARENT_ASSOC_TYPE		= "cm:contains";


	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("[ArchiveImporterJob::execute] BEGIN");
		if (!running) {
			synchronized (this) {
				if (!running) {
					running = true;
				} else {
					logger.debug("[ArchiveImporterJob::execute] job already running 1");
					logger.debug("[ArchiveImporterJob::execute] END");
					return;
				}
			}
		} else {
			logger.debug("[ArchiveImporterJob::execute] job already running 2");
			logger.debug("[ArchiveImporterJob::execute] END");
			return;
		}

		logger.debug("[ArchiveImporterJob::execute] START");

		JobBusinessInterface jobManager = null;
		BatchJob batchJob = null;
		try {
			// Chiedo un'istanza di JobManager
			jobManager = (JobBusinessInterface)context.getJobDetail().getJobDataMap().get(ECMENGINE_JOB_MANAGER_BEAN);

			// In caso di successo
			if( jobManager!=null ){
				List<Repository> repositories = RepositoryManager.getInstance().getRepositories();
				for (Repository repository : repositories) {
					logger.debug("[ArchiveImporterJob::execute] import archive on repository '"+repository.getId()+"'");
					RepositoryManager.setCurrentRepository(repository.getId());

					// Faccio la lista dei job di tipo ECMENGINE_ARCHIVE_IMPORTER_JOB_REF, attivi
					// Possono essere
					// Ready - da processare
					// Running - in esecuzione : sto importando il file
					// Finished - ho finito

					// In questo stato, essendo un job singleton, dovrei avere solo stati ready (nuovi job) o finished (job finiti)
					// Se icontro un RUNNING, sicuramente si tratta di una condizione di errore, riporto a ready il job, e faccio in
					// modo che l'algoritmo sottostante continui l'importazione in modo incrementale

					// Prendo tutti i job di un certo esecutore: in futuro, se la cosa dovesse dare problemi di performance
					// aggiungere un filtro sullo status RUNNING
					BatchJob[] bjs = jobManager.getJobsByExecutor( ECMENGINE_ARCHIVE_IMPORTER_JOB_REF );
					if( bjs!=null ){
						// Se ho dei BatchJob
						for( BatchJob bj : bjs ){
							logger.debug("[ArchiveImporterJob::execute] job status " +bj.getId() +":" +bj.getStatus());
							// Se lo stato e' running
							if( bj.getStatus().equalsIgnoreCase( JobStatus.RUNNING ) ){
								logger.debug("[ArchiveImporterJob::execute] update status to ready " +bj.getId() );
								// Reimposto lo stato a ready
								bj.setStatus( JobStatus.READY );
								jobManager.updateJob(bj);
							}
						}
					} else {
						logger.debug("[ArchiveImporterJob::execute] BatchJob NULL per " +ECMENGINE_ARCHIVE_IMPORTER_JOB_REF );
					}

					// A questo punto, sono in una situazione consistente, con dei job ready
					// I job ready possono essere sia nuovi job, che resume di situazioni precedenti
					while( (batchJob=jobManager.getNextJob( ECMENGINE_ARCHIVE_IMPORTER_JOB_REF ))!=null ){

						// Prendo il prossimo job
						logger.debug("[ArchiveImporterJob::execute] start batchJob " +batchJob.getId());
						if( batchJob!=null ){
							try {
								// Estraggo i parametri di esecuzione
								BatchJobParam pName    = batchJob.getParam(PARAM_NAME                   );
								BatchJobParam pFormat  = batchJob.getParam(PARAM_FORMAT                 );
								BatchJobParam pStore   = batchJob.getParam(PARAM_CONTENTSTORE_DIR       );
								String importDirectory = (String)context.getJobDetail().getJobDataMap().get(ECMENGINE_IMPORT_DIRECTORY);

								// verifico se sono corretti
								checkParam( batchJob, pName             , "Archive name not found" );
								checkParam( batchJob, pFormat           , "Archive format not found" );
								checkParam( batchJob, pStore            , "Archive store not found" );
								checkParam( batchJob, importDirectory   , "importDirectory null" );

								// Validati i parametri di estrazione del fie, procedo con lo
								// spostamento da TEMP alla directory di import, ed esplodo il file

								// Sposto il file dalla temp alla workdir del progetto
								String cFrom = pStore.getValue() +File.separator +pName.getValue();
								File oFrom = new File(cFrom);
								logger.debug( "[ArchiveImporterJob::execute] From:" +cFrom );

								// Crea la directory di output se non esiste
								File oDir = new File(importDirectory);
								if( !oDir.exists() ){
									oDir.mkdir();
								}

								// File di importazione
								String cFile = importDirectory +File.separator +pName.getValue();
								File oFile = new File(cFile);
								logger.debug( "[ArchiveImporterJob::execute] Import:" +cFile );

								// Se esiste il file, lo sposto nella dir dove deve essere processato
								if( oFrom.exists() ){
									// Provo lo spostamento, perche' piu' veloce
									if( !oFrom.renameTo( oFile ) ){
										// Se fallisce provo la copia, piu' lenta
										if( !copyFile(oFrom, oFile) ){
											batchJob.setMessage("Unable to copy from (" +cFrom +") to (" +cFile +")");
											throw new EcmEngineException("ArchiveImporterJob: " +batchJob.getMessage());
										} else {
											// Se la copia va a buon fine, cancello il file
											oFrom.delete();
										}
									}
								}

								// Prima cosa, provo a caricare in RAM il file indicato.
								// Attenzione: vista la dimensione a 256 char dei parametri dei job,
								// ho preferito spezzare path e nome file in due variabili diverse, in modo
								// da all'ungare il piu' possibile la lunghezza del path utilizzabile

								// Una volta processato il file, lo rinomino in processed, per poterne tenere traccia
								// I processed andrebbero alimintati ogni tanto, per ridurne la coda
								// Alternativamente, si puo' variare il codice per rinominarli al posto di rinominare
								// Un'altra ragione della rinomina e' data dal fatto che, in caso di zip corrotti, puo' accadere che
								// il file risulti importato, ma non presente, senza nessun messaggio d'errore
								String cFileRenamed = cFile +".processed";
								File oFileRenamed = new File( cFileRenamed );

								// Il temp path lo formo col nome del file compresso +".extract", in modo da avere l'univocita' data gia
								// dal nome file
								String tempPath = cFile +".extract";
								File tmpWorkDir = new File(tempPath);

								// Se il file non esiste
								if( !oFile.exists() ){
									logger.debug("[ArchiveImporterJob::execute] File di importazione non presente, controllo directory di esplosione");

									// Se ho la dir di estrazione, vuol dire che stavo importando e c'e' stato un errore
									// Quindi do errore solo se non esiste manco la dir di output
									if( !tmpWorkDir.exists() ) {
										batchJob.setMessage("Archive not found");
										throw new EcmEngineException("ArchiveImporterJob: " +batchJob.getMessage() +" (" +cFile +")");
									} else {
										logger.debug("[ArchiveImporterJob::execute] Directory di importazione presente, procedo con l'importazione");
									}
								}

								// Se sono qui, posso avere il file o la dir di esplosione

								// Se ho il file, prendo il contenuto da disco e lo decomprimo
								if( oFile.exists() ){
									byte[] content = getBinary( cFile );
									logger.debug( "[ArchiveImporterJob::execute] Content size: " +content.length );

									// La directory la creo solo se non esiste, devo infatti gestire il caso che sia gia' creata
									// e sto andando in aggiornamento
									if( !tmpWorkDir.exists() ) {
										if( !tmpWorkDir.mkdirs() ) {
											batchJob.setMessage("Cant' creare working dir");
											throw new EcmEngineException("ArchiveImporterJob: " +batchJob.getMessage() +" (" +tempPath +")");
										}
									}

									// A questo punto, estraggo i file presenti nello zip
									String cFormat = pFormat.getValue();

									logger.debug("[ArchiveImporterJob::execute] estrazione archivio (" +cFile +")(" +cFormat +") in "+tempPath);
									if (ECMENGINE_ARCHIVE_FORMAT_TAR.equals( cFormat )) {
										extractTar(     new ByteArrayInputStream( content ), tempPath);

									} else if (ECMENGINE_ARCHIVE_FORMAT_TAR_GZ.equals( cFormat )) {
										extractTarGz(   new ByteArrayInputStream( content ), tempPath);

									} else if (ECMENGINE_ARCHIVE_FORMAT_ZIP.equals( cFormat )) {
										extractZip(     new ByteArrayInputStream( content ), tempPath);

									} else {
										// In caso di formato non gestito, esco con errore
										batchJob.setMessage("Format not supported");
										throw new EcmEngineException("ArchiveImporterJob: " +batchJob.getMessage() +" (" +cFormat +")");
									}

									// A questo punto, ho l'esplosione dello ZIP
									// Una volta esploso in modo corretto, cancello eventuali copie non previste
									// e rinomino il file
									oFileRenamed.delete();
									oFile.renameTo( oFileRenamed );

									// A fine processo, cancello i file interessati dalla import
									// Commentare questa riga se si volesse verificare come mai non viene importato un file
									oFile.delete();
									oFileRenamed.delete();
								}

								// Creo i service e verifico siano presi in modo corretto
								transactionService    = (TransactionService)context.getJobDetail().getJobDataMap().get(ECMENGINE_TRANSACTION_SERVICE_BEAN);
								namespaceService      = (NamespaceService)context.getJobDetail().getJobDataMap().get(ECMENGINE_NAMESPACE_SERVICE_BEAN);
								contentService        = (ContentService)context.getJobDetail().getJobDataMap().get(ECMENGINE_CONTENT_SERVICE_BEAN);
								nodeService           = (NodeService)context.getJobDetail().getJobDataMap().get(ECMENGINE_NODE_SERVICE_BEAN);
								authenticationService = (AuthenticationService)context.getJobDetail().getJobDataMap().get(ECMENGINE_AUTHENTICATION_SERVICE_BEAN);

								checkParam( batchJob, transactionService     , "transactionService null" );
								checkParam( batchJob, namespaceService       , "namespaceService null" );
								checkParam( batchJob, contentService         , "contentService null" );
								checkParam( batchJob, nodeService            , "nodeService null" );
								checkParam( batchJob, authenticationService  , "authenticationService null" );

								// Vengono presi i parametri del batch e ne viene controllata la conguenza, uscendo in caso di errore
								BatchJobParam pUID                      = batchJob.getParam(PARAM_UID                    );
								BatchJobParam pStoreProtocol            = batchJob.getParam(PARAM_STORE_PROTOCOL         );
								BatchJobParam pStoreIdentifier          = batchJob.getParam(PARAM_STORE_IDENTIFIER       );
								BatchJobParam pUser                     = batchJob.getParam(PARAM_USER                   );
								BatchJobParam pPassword                 = batchJob.getParam(PARAM_PASSWORD               );

								BatchJobParam pContentType              = batchJob.getParam(PARAM_CONTENT_TYPE           );
								BatchJobParam pNameProperty             = batchJob.getParam(PARAM_CONTENT_NAME_PROPERTY  );
								BatchJobParam pContainerType            = batchJob.getParam(PARAM_CONTAINER_TYPE         );
								BatchJobParam pContainerNameProperty    = batchJob.getParam(PARAM_CONTAINER_NAME_PROPERTY);
								BatchJobParam pContainerAssocType       = batchJob.getParam(PARAM_CONTAINER_ASSOC_TYPE   );
								BatchJobParam pParentAssocType          = batchJob.getParam(PARAM_PARENT_ASSOC_TYPE      );

								checkParam( batchJob, pUID                    , "Node UID not found" );
								checkParam( batchJob, pStoreProtocol          , "Store Protocol not found" );
								checkParam( batchJob, pStoreIdentifier        , "Store Identifier not found" );
								checkParam( batchJob, pUser                   , "User not found" );
								checkParam( batchJob, pPassword               , "Password not found" );

								checkParam( batchJob, pContentType            , "Content Type not found" );
								checkParam( batchJob, pNameProperty           , "Content Name not found" );
								checkParam( batchJob, pContainerType          , "Container Type not found" );
								checkParam( batchJob, pContainerNameProperty  , "Container Name not found" );
								checkParam( batchJob, pContainerAssocType     , "Container Assoc not found" );
								checkParam( batchJob, pParentAssocType        , "Parent Assoc not found" );

								// Trasformazione dei parametri in QName
								QName contentTypeQName              = resolvePrefixNameToQName(pContentType           .getValue() );
								QName contentNamePropertyQName      = resolvePrefixNameToQName(pNameProperty          .getValue() );
								QName containerTypeQName            = resolvePrefixNameToQName(pContainerType         .getValue() );
								QName containerNamePropertyQName    = resolvePrefixNameToQName(pContainerNameProperty .getValue() );
								QName containerAssocTypeQName       = resolvePrefixNameToQName(pContainerAssocType    .getValue() );
								QName parentAssocTypeQName          = resolvePrefixNameToQName(pParentAssocType       .getValue() );

								// Prendo un oggetto UserTransaction
								UserTransaction transaction = transactionService.getNonPropagatingUserTransaction();

								try {
									// Inizio la transazione
									transaction.begin();

									// Cambio l'utente, con l'utente che ha deve importare
									authenticationService.authenticate( pUser.getValue() ,
											EncryptionHelper.decrypt( pPassword.getValue() ).toCharArray() );

								} catch(Exception e) {
									logger.debug( e );
									throw e;

								}finally{
									// Anche se non ho fatto
									try{transaction.rollback();}catch(Exception e){}
								}

								// Creo un nodo, usando l'UID del folder dove devo mettere i dati
								StoreRef sr = new StoreRef( pStoreProtocol.getValue(), pStoreIdentifier.getValue() );
								// DictionarySvc.SPACES_STORE
								NodeRef nodeRef = new NodeRef( sr , pUID.getValue());

								// Attivo l'importazione ricorsiva
								int nContent = handleRootFolder(tmpWorkDir                    ,
										nodeRef                       ,
										parentAssocTypeQName          ,
										containerTypeQName            ,
										containerNamePropertyQName    ,
										containerAssocTypeQName       ,
										contentTypeQName              ,
										contentNamePropertyQName);

								// Reimposto lo status e vado al prossimo JOB
								batchJob.setMessage("Content nuovi: " +nContent +" Datafile " +pName.getValue() +".processed");
								batchJob.setStatus( JobStatus.FINISHED );
								jobManager.updateJob(batchJob);

							} catch(Exception e) {
								logger.error("[ArchiveImporterJob::execute] ERROR", e);
								try {
									// Reimposto il getMessage(), nel caso arrivi vuoto
									if( batchJob.getMessage().length()==0 ){
										batchJob.setMessage( e.getMessage() );
									}

									// Reimposto lo status e vado al prossimo JOB
									batchJob.setStatus( JobStatus.ERROR );
									jobManager.updateJob(batchJob);
								} catch(Exception ee) {
									// TODO: vedere se e' giusto tenerlo muto
								}

							} finally {
								// Non posso toccare lo stato a ready, altrimenti vado in loop
							}
						}
					}


				}
			} else {
				logger.error("[ArchiveImporterJob::execute] JobManager NULL per "+ECMENGINE_JOB_MANAGER_BEAN);
			}
		} catch(Exception e) {
			logger.error("[ArchiveImporterJob::execute] ERROR", e);
			throw new JobExecutionException(e);
		} finally {
			running = false;
			logger.debug("[ArchiveImporterJob::execute] END");
		}

		logger.debug("[ArchiveImporterJob::execute] END run");

	}

	/**
	 * Metodo statico di utilit&agrave; per la creazione del job da fornire come
	 * parametro al job manager.
	 *
	 * @param archive
	 *           Un oggetto DataArchive con i dati dell'archivio da decomprimere
	 * @param node
	 *           Il nodo dove importare i dti
	 * @param context
	 *           L'istanza di {@code OperationContext} con la quale autenticarsi
	 * @return L'istanza di {@code BatchJob} contenente i dati necessati al job
	 *           di gestione di un DataArchive
	 * @throws Exception
	 * @see {@link it.doqui.index.ecmengine.business.job.JobBusinessInterface}
	 */
	public static BatchJob createBatchJob( DataArchive archive,
			NodeRef node,
			OperationContext context
	) throws Exception {
		BatchJob job = new BatchJob( ECMENGINE_ARCHIVE_IMPORTER_JOB_REF );

		// UID sotto al quale agganciare o ZIP e suo store Ref
		String cUID = node.getId();
		StoreRef sr = node.getStoreRef();

		job.addParam(new BatchJobParam(PARAM_UID                     , cUID 			  ));
		job.addParam(new BatchJobParam(PARAM_STORE_PROTOCOL          , sr.getProtocol()   ));
		job.addParam(new BatchJobParam(PARAM_STORE_IDENTIFIER        , sr.getIdentifier() ));

		// Aggiungo l'utente che effettuera' l'operazione
		job.addParam(new BatchJobParam(PARAM_USER        , context.getUsername()   ));
		job.addParam(new BatchJobParam(PARAM_PASSWORD    , EncryptionHelper.encrypt(context.getPassword()) ));

		// Serve creare un file temporaneo con dentro archive.getContent() e mettere il nome in un parametro
		String cTimeStamp = ""+new java.util.Date().getTime();
		String cName = cTimeStamp +"_" +Math.random() +"_" +cUID +"." +archive.getFormat();
		job.addParam(new BatchJobParam(PARAM_NAME                    , cName ));

		// Path di memorizzazione del file PARAM_NAME
		String workDir = TempFileProvider.getTempDir().getAbsolutePath();
		job.addParam(new BatchJobParam(PARAM_CONTENTSTORE_DIR        , workDir ));

		// Metto il content nel file temporaneo
		String cFile = workDir +File.separator +cName;
		putBinary( cFile, archive.getContent() );

		// Formato del file da importare
		job.addParam(new BatchJobParam(PARAM_FORMAT                  , archive.getFormat()));

		// Parametri necessari all'esecuzione
		job.addParam(new BatchJobParam(PARAM_CONTENT_TYPE            , getValue(archive.getMappedContentTypePrefixedName()             , DEFAULT_CONTENT_TYPE              )));
		job.addParam(new BatchJobParam(PARAM_CONTENT_NAME_PROPERTY   , getValue(archive.getMappedContentNamePropertyPrefixedName()     , DEFAULT_CONTENT_NAME_PROPERTY     )));
		job.addParam(new BatchJobParam(PARAM_CONTAINER_TYPE          , getValue(archive.getMappedContainerTypePrefixedName()           , DEFAULT_CONTAINER_TYPE            )));
		job.addParam(new BatchJobParam(PARAM_CONTAINER_NAME_PROPERTY , getValue(archive.getMappedContainerNamePropertyPrefixedName()   , DEFAULT_CONTAINER_NAME_PROPERTY   )));
		job.addParam(new BatchJobParam(PARAM_CONTAINER_ASSOC_TYPE    , getValue(archive.getMappedContainerAssocTypePrefixedName()      , DEFAULT_CONTAINER_ASSOC_TYPE      )));
		job.addParam(new BatchJobParam(PARAM_PARENT_ASSOC_TYPE       , getValue(archive.getParentContainerAssocTypePrefixedName()      , DEFAULT_PARENT_ASSOC_TYPE         )));

		return job;
	}

	/**
	 * Metodo di comodit&agrave; per validare un valore ed eventualmente utilizzare un
	 * valore di default.
	 *
	 * <p>
	 * Questo metodo verifica se {@code targetValue} &egrave; diverso da null o
	 * stringa vuota e ne restituisce il valore. Altrimenti restituisce il
	 * valore fornito come default.
	 * </p>
	 *
	 * @param targetValue
	 *            Il valore da validare ed eventualmente restituire.
	 * @param defaultValule
	 *            Il valore di default.
	 *
	 * @return {@code targetValue} se diverso da null o stringa vuota;
	 *         {@code defaultValue} altrimenti.
	 */
	static private String getValue(String targetValue, String defaultValue) {
		return (targetValue != null && targetValue.length() > 0)
		? targetValue : defaultValue;
	}

	/**
	 * Estrae il contenuto di un archivio in formato TAR.
	 *
	 * @param archiveInputStream L'input stream da cui leggere il contenuto dell'archivio.
	 * @param path Il path della directory in cui estrarre il contenuto dell'archivio.
	 *
	 * @throws Exception
	 */
	private void extractTar(InputStream archiveInputStream, String path) throws Exception {
		logger.debug("[ArchiveImporterJob::extractTar] BEGIN");
		TarInputStream inputStream = null;
		try {
			inputStream = new TarInputStream(archiveInputStream);
			String entryName = null;
			for (TarEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
				entryName = entry.getName();
				File entryFile = new File(path+File.separator+entryName);
				if (entry.isDirectory()) {
					if (!entryFile.mkdirs()) {
						throw new EcmEngineException("cannot create directory: "+entryFile.getAbsolutePath());
					}
				} else {
					File parentDir = entryFile.getParentFile();
					if (!parentDir.exists()) {
						if (!parentDir.mkdirs()) {
							throw new EcmEngineException("cannot create directory: "+parentDir.getAbsolutePath());
						}
					}
					inputStream.copyEntryContents(new FileOutputStream(path+File.separator+entryName));
				}
			}
		} catch(Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				try { inputStream.close(); } catch(Exception ee) {}
			}
			logger.debug("[ArchiveImporterJob::extractTar] END");
		}
	}

	/**
	 * Estrae il contenuto di un archivio in formato TAR con compressione GZip.
	 *
	 * @param archiveInputStream L'input stream da cui leggere il contenuto dell'archivio.
	 * @param path Il path della directory in cui estrarre il contenuto dell'archivio.
	 *
	 * @throws Exception
	 */
	private void extractTarGz(InputStream archiveInputStream, String path) throws Exception {
		logger.debug("[ArchiveImporterJob::extractTarGz] BEGIN");
		try {
			extractTar(new GZIPInputStream(archiveInputStream), path);
		} catch(Exception e) {
			throw e;
		} finally {
			logger.debug("[ArchiveImporterJob::extractTarGz] END");
		}
	}

	/**
	 * Estrae il contenuto di un archivio in formato ZIP.
	 *
	 * @param archiveInputStream L'input stream da cui leggere il contenuto dell'archivio.
	 * @param path Il path della directory in cui estrarre il contenuto dell'archivio.
	 *
	 * @throws Exception
	 */
	// TODO: verificare come mai se il file non e' ZIP non va in errore
	private void extractZip(InputStream archiveInputStream, String path) throws Exception {
		logger.debug("[ArchiveImporterJob::extractZip] BEGIN");
		ZipInputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			inputStream = new ZipInputStream(archiveInputStream);
			String entryName = null;
			byte[] buffer = new byte[1024];
			int n = 0;
			for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
				entryName = entry.getName();
				File entryFile = new File(path+File.separator+entryName);
				if (entry.isDirectory()) {
					if (!entryFile.mkdirs()) {
						throw new EcmEngineException("cannot create directory: "+entryFile.getAbsolutePath());
					}
				} else {
					File parentDir = entryFile.getParentFile();
					if (!parentDir.exists()) {
						if (!parentDir.mkdirs()) {
							throw new EcmEngineException("cannot create directory: "+parentDir.getAbsolutePath());
						}
					}
					fileOutputStream = new FileOutputStream(entryFile);
					while ((n=inputStream.read(buffer, 0, buffer.length)) > -1) {
						fileOutputStream.write(buffer, 0, n);
					}
					fileOutputStream.close();
				}
				inputStream.closeEntry();
			}
		} catch(Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				try { inputStream.close(); } catch(Exception e) {}
				try { fileOutputStream.close(); } catch(Exception e) {}
			}
			logger.debug("[ArchiveImporterJob::extractZip] END");
		}
	}

	private byte[] getBinary( String cFile ) throws Exception {
		File file = new File( cFile );
		FileInputStream fileinputstream = new FileInputStream(file);
		byte abyte0[] = new byte[(int)file.length()];
		fileinputstream.read(abyte0);
		fileinputstream.close();
		return abyte0;
	}

	static private void putBinary( String cFile, byte[] bOut ) throws Exception {
		File file = new File( cFile );
		FileOutputStream fileoutputstream = new FileOutputStream(file);
		fileoutputstream.write(bOut);
		fileoutputstream.close();
		return;
	}

	private int handleRootFolder(
			File folder,
			NodeRef parentNodeRef,
			QName parentAssocTypeQName,
			QName containerTypeQName,
			QName containerNamePropertyQName,
			QName containerAssocTypeQName,
			QName contentTypeQName,
			QName contentNamePropertyQName) throws Exception {
		logger.debug("[ArchiveImporterJob::handleRootFolder] BEGIN");

		// Conto quanti dati sono stati scritti
		int nContent = 0;

		try {
			// Prima si inizia col creare i singoli contenuti
			boolean bContent = false;

			{
				// Prendo un oggetto UserTransaction
				UserTransaction transaction = transactionService.getNonPropagatingUserTransaction();

				try {
					// Inizio la transazione
					transaction.begin();

					// Conto i content creati
					int nSubContent = 0;

					// Prima creo i content in una transazione
					File[] folderEntries = folder.listFiles();
					for (File entry : folderEntries) {

						// Se e' una directory
						if( !entry.isDirectory() ){
							logger.debug("[ArchiveImporterJob::handleRootFolder] creating content: "+entry.getName()+", nodeRef="+parentNodeRef+", association="+parentAssocTypeQName);

							// Creo il contenuti
							if( createContent(entry, parentNodeRef, contentTypeQName, contentNamePropertyQName, parentAssocTypeQName) ){
								nSubContent++;
							}
						}
					}

					// Se ho inserito 0 content, e non si e' generata una eccezione, vuol dire che i dati inseriti
					// sono tutti dei doppioni, in questo caso, meto bContent a true, e lascio andare avanti l'algoritmo
					bContent = (nSubContent==0);
					nContent += nSubContent;

					logger.debug( "[ArchiveImporterJob::handleRootFolder] Content inseriti: " +nContent );

					// Nel caso che si chiami una commit, senza righe da committare
					// C'e' una eccezione.
					// TODO: gestire le transazioni da 0 contenuti .. ma .. cosa fare in questa situazione?
					transaction.commit();

					// Se non ho ecezione sulla commit, indico come true la creazione content
					bContent = true;

					logger.debug( "[ArchiveImporterJob::handleRootFolder] Content bool "+bContent );

				} catch (RollbackException re) {
					try {
						transaction.rollback();
					} catch(Exception ee) {
						logger.debug( "[ArchiveImporterJob::handleRootFolder] RollbackException" );
					}
				} catch (EcmEngineFoundationException e) {
					// Rollback
					try {
						transaction.rollback();
					} catch(Exception ee) {
						logger.debug( "[ArchiveImporterJob::handleRootFolder] EcmEngineFoundationException" );
					}
				} catch(Exception e) {
					logger.debug( e );
					throw e;
				}
			}

			// Se i contenuti vanno a buon fine, inizio a cancellarli da disco
			boolean bDelete = false;
			if( bContent ){
				try {
					// Prima creo i content in una transazione
					File[] folderEntries = folder.listFiles();
					for (File entry : folderEntries) {

						// Se e' una directory
						if( !entry.isDirectory() ){
							// Cancello il contenuto
							entry.delete();
						}
					}
					bDelete = true;
				} catch(Exception e) {
					logger.debug( e );
					throw e;
				}
			}

			// Se le delete vanno a buon fine, inizio a creare le directory
			if( bDelete ){
				try {
					boolean bDeleteFolder = true;
					// Per tutti i file della cartella
					File[] folderEntries = folder.listFiles();
					for (File entry : folderEntries) {

						// Se e' una directory
						if (entry.isDirectory()) {
							// Create directory
							logger.debug("[ArchiveImporterJob::handleRootFolder] creating directory: "+entry.getName()+", nodeRef="+parentNodeRef+", association="+parentAssocTypeQName);

							// nodo di riferimento
							NodeRef nr = null;

							// Stranamente, per una get di dati, viene espressamente richiesta una transazione

							// Prendo un oggetto UserTransaction
							UserTransaction transaction = transactionService.getNonPropagatingUserTransaction();
							try {
								// Inizio la transazione
								transaction.begin();

								// Verifico se la cartella e' presente nel nodo padre
								nr = nodeService.getChildByName( parentNodeRef, parentAssocTypeQName, entry.getName() );

								// Anche se non ho fatto
								transaction.rollback();
							} catch(Exception e) {
								logger.debug( e );
								throw e;
							}finally{
								// Anche se non ho fatto
								try{transaction.rollback();}catch(Exception e){}
							}

							// Prendo un oggetto UserTransaction
							transaction = transactionService.getNonPropagatingUserTransaction();
							boolean bTrans = false;
							try {
								// Se non e' presente, provo a crearla
								if( nr==null ){
									bTrans = true;

									// Preparo le properties di un folder
									QName prefixedNameQName = resolvePrefixNameToQName("cm:"+entry.getName());
									Map<QName,Serializable> props = new HashMap<QName, Serializable>();
									props.put(containerNamePropertyQName, entry.getName());

									// Inizio la transazione
									transaction.begin();

									// Creo il folder
									ChildAssociationRef folderNodeRef = nodeService.createNode(parentNodeRef,
											parentAssocTypeQName,
											prefixedNameQName,
											containerTypeQName,
											props);

									// Nel caso che si chiami una commit, senza righe da committare
									// C'e' una eccezione.
									// TODO: gestire le transazioni da 0 contenuti
									transaction.commit();

									nr = folderNodeRef.getChildRef();
								}

								// Creazione del subfolder
								nContent += handleRootFolder(	entry,
										nr,
										containerAssocTypeQName, // Non passo il parent, ma passo il containerAssocType nei folder figli
										containerTypeQName,
										containerNamePropertyQName,
										containerAssocTypeQName,
										contentTypeQName,
										contentNamePropertyQName);

							} catch (RollbackException re) {
								if( bTrans ){
									try {
										transaction.rollback();
									} catch(Exception ee) {
										logger.debug( re );
									}
								}

							} catch (EcmEngineFoundationException e) {
								bDeleteFolder = false;
								// Rollback
								try {
									transaction.rollback();
								} catch(Exception ee) {
									logger.debug( e );
								}

							} catch(Exception e) {
								logger.debug( e );
								throw e;
							}

						}
					}

					// Rimuovo la directory, se non ho avuto problemi rimuovendo le subdir
					if( bDeleteFolder ){
						folder.delete();
					}
				} catch(Exception e) {
					logger.debug( e );
					throw e;
				}
			}

		} catch(Exception e) {
			logger.debug( e );
			throw e;
		} finally {
			logger.debug("[ArchiveImporterJob::handleRootFolder] END");
		}

		return nContent;
	}

	/**
	 * Crea un contenuto sul repository a partire da un file su filesystem.
	 *
	 * @param content
	 *            Il file da creare sul repository.
	 * @param parentNodeRef
	 *            Il {@code NodeRef} del nodo sotto cui creare il contenuto.
	 * @param contentTypeQName
	 *            Il {@code QName} del tipo di contenuto da creare.
	 * @param contentNamePropertyQName
	 *            Il {@code QName} del nome del contenuto da creare.
	 * @param containerAssocTypeQName
	 *            Il {@code QName} dell'associazione che lega contenuto e nodo
	 *            padre.
	 *
	 * @return Il {@code NodeRef} del contenuto creato.
	 *
	 * @throws Exception
	 */
	private boolean createContent(File content,
			NodeRef parentNodeRef,
			QName contentTypeQName,
			QName contentNamePropertyQName,
			QName containerAssocTypeQName) throws Exception {
		logger.debug("[ArchiveImporterJob::createContent] BEGIN");
		boolean bRet = false;
		try {
			// Verifico se il contenuto e' presente nel nodo padre
			NodeRef nr = nodeService.getChildByName( parentNodeRef, containerAssocTypeQName, content.getName() );

			// Se non e' presente, provo a crearlo
			if( nr==null ){
				// Creazione nodo
				QName prefixedNameQName = resolvePrefixNameToQName("cm:"+content.getName());
				Map<QName,Serializable> props = new HashMap<QName, Serializable>();
				props.put(contentNamePropertyQName, content.getName());

				ChildAssociationRef contentChildRef = nodeService.createNode(parentNodeRef,
						containerAssocTypeQName,
						prefixedNameQName,
						contentTypeQName,
						props);

				// Scrittura contenuto
				final ContentWriter writer = contentService.getWriter(contentChildRef.getChildRef(), ContentModel.PROP_CONTENT, true);

				writer.setMimetype(new MimetypesFileTypeMap().getContentType(content));
				writer.setEncoding(getEncoding(content));
				writer.putContent(content);

				// Se riesco a scrivere
				bRet = true;
			}

		} catch(DuplicateChildNodeNameException dc){
			// In caso di contenuto duplicato, viene usata una policy conservativa, e viene tenuto
			// Il valore presente in repository
			logger.debug( "[ArchiveImporterJob::createContent] Contenuto presente (" +content.getName() +")" );
		} catch(Exception e) {
			throw e;
		} finally {
			logger.debug("[ArchiveImporterJob::createContent] END");
		}

		return bRet;
	}

	/**
	 * Restituisce l'encoding del file specificato.
	 *
	 * @param file Il file di cui ricavare l'encoding.
	 * @return L'encoding del file specificato.
	 */
	private String getEncoding(File file) {
		String encoding = "";
		try {
			FileReader fr = new FileReader(file);
			encoding = fr.getEncoding();
			fr.close();
		} catch (Exception e) {}
		return encoding;
	}

	private QName resolvePrefixNameToQName(String prefixName) {
		logger.debug("[ArchiveImporterJob::resolvePrefixNameToQName] BEGIN");
		QName result = null;
		String [] nameParts = QName.splitPrefixedQName(prefixName);

		try {
			logger.debug("[ArchiveImporterJob::resolvePrefixNameToQName] Resolving to QName: " + prefixName);
			result = QName.createQName(nameParts[0], nameParts[1],namespaceService);
			logger.debug("[ArchiveImporterJob::resolvePrefixNameToQName] QName: " + result.toString());
		} catch (RuntimeException e) {
			logger.debug("[ArchiveImporterJob::resolvePrefixNameToQName] " +
					"Error resolving to QName \"" + prefixName + "\": " + e.getMessage());
			throw new RuntimeException();	// FIXME
		} finally {
			logger.debug("[ArchiveImporterJob::resolvePrefixNameToQName] END");
		}

		return result;
	}

	private void checkParam( BatchJob batchJob, Object pParam, String cError ) throws EcmEngineException {
		if( pParam==null ){
			batchJob.setMessage( cError );
			throw new EcmEngineException("ArchiveImporterJob: " +batchJob.getMessage());
		}
	}

	// http://www.rgagnon.com/javadetails/java-0064.html
	private boolean copyFile(File in, File out) {
		boolean bRet = false;

		FileChannel inChannel  = null;
		FileChannel outChannel = null;

		try {
			inChannel  = new FileInputStream(in).getChannel();
			outChannel = new FileOutputStream(out).getChannel();

			// On the Windows plateform, you can't copy a file bigger than 64Mb, an
			// Exception in thread "main" java.io.IOException: Insufficient system
			// resources exist to complete the requested service is thrown.
			// inChannel.transferTo(0, inChannel.size(), outChannel);

			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel.transferTo(position, maxCount, outChannel);
			}

			bRet = true;

		} catch (IOException e) {
			//System.out.println( e );
			//throw e;
		} finally {
			try { if (inChannel  != null) inChannel.close(); } catch (IOException e) {}
			try { if (outChannel != null) outChannel.close();} catch (IOException e) {}
		}
		return bRet;
	}

}

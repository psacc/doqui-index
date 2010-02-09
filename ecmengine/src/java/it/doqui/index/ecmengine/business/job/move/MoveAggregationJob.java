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

package it.doqui.index.ecmengine.business.job.move;

import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.business.audit.AuditBusinessInterface;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.ServerInfoException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.MoveException;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.exception.repository.NodeRuntimeException;
import it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException;
import it.doqui.index.ecmengine.exception.security.PermissionRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MoveAggregationJob implements Job, EcmEngineConstants {

	private static final long serialVersionUID = 0L;

	private static Log logger = LogFactory.getLog(ECMENGINE_JOB_MOVE_AGGREG_LOG_CATEGORY);

	/** Lo stopwatch da utilizzare per la registrazione dei tempi. */
	private StopWatch stopwatch;

	private AuditBusinessInterface auditManager;

	private NodeService nodeService;

	private SearchService searchService;

	private CopyService copyService;

	private PermissionService permissionService;

	private AuthenticationService authenticationService;

	private AuthenticationComponent authenticationComponent;

	private TransactionService transactionService;

	private NamespaceService namespaceService;

	/**
	 * Costruttore predefinito.
	 */
	public MoveAggregationJob() {}

	public void execute(JobExecutionContext ctx) throws JobExecutionException {

		Authentication auth = null;
//		String job = null;

		try {
			logger.debug("[MoveAggregationJob::execute] BEGIN");

//			job = ctx.getJobDetail().getName();

//			if (exitJob(job)) {
//				logger.debug("[MoveAggregationJob::execute] Il Job non deve essere eseguito su questo nodo.");
//				return;
//			}

//			cleanJob(job);

			RepositoryManager.setCurrentRepository(
					RepositoryManager.getInstance().getDefaultRepository().getId());

//			Object object = ctx.getJobDetail().getJobDataMap().get("authenticationComponent");
			Object object = ctx.getJobDetail().getJobDataMap().get(ECMENGINE_AUTHENTICATION_BEAN);
			if (object == null
					|| !(object instanceof AuthenticationComponent)) {
				throw new IllegalArgumentException("MoveAggregationJob must contain valid " +
				"'authenticationComponent' reference");
			}
			authenticationComponent = (AuthenticationComponent) object;

			auth = AuthenticationUtil.getCurrentAuthentication();

			// authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			logger.debug("[MoveAggregationJob::execute] Job: " +
					ctx.getJobDetail().getName() + " - Trigger: " + ctx.getTrigger().getName());

			init(ctx);

//			RetryingTransactionCallback<Object> moveWork = new RetryingTransactionCallback<Object>()
//			{
//				public Object execute() throws Exception
//				{
//					logger.info("[MoveAggregationJob::execute] " +
//							"Inizio spostamento sul Repository: " +
//							RepositoryManager.getCurrentRepository());
//					moveAggregation();
//					logger.info("[MoveAggregationJob::execute] " +
//							"Fine spostamento sul Repository: "	+
//							RepositoryManager.getCurrentRepository());
//					return null;
//				}
//			};
//			transactionService.getRetryingTransactionHelper().doInTransaction(moveWork);

			logger.info("[MoveAggregationJob::execute] " +
					"Inizio spostamento sul Repository: " +RepositoryManager.getCurrentRepository());

			moveAggregation();

			logger.info("[MoveAggregationJob::execute] Fine spostamento sul Repository: "	+
					RepositoryManager.getCurrentRepository());

		} catch (Exception e) {
			String msg = "";
			if (e instanceof UnknownHostException) {
				logger.error("[MoveAggregationJob::execute] Failed to get server IP address.");
				msg = "Failed to get server IP address";
			} else if (e instanceof ServerInfoException) {
				logger.error("[MoveAggregationJob::execute] Failed to get server info from ecm_job.");
				msg = "Failed to get server info from ecm_job";
			} else {
				logger.error("[MoveAggregationJob::execute] Exception nell'esecuzione del Job: "+e.getMessage());
				msg = e.getMessage();
			}
			throw new JobExecutionException("Exception : "+msg, e);
		} finally{
			if (auth != null) {
				authenticationComponent.setCurrentAuthentication(auth);
			}
			logger.debug("[MoveAggregationJob::execute] END");
		}
	}

//	private void cleanJob(String job) throws ServerInfoException {
//
//		try{
//			logger.debug("[MoveAggregationJob::cleanJob] BEGIN");
//			serverInfoManager = ServerInfoManager.getInstance();
//			logger.debug("[MoveAggregationJob::cleanJob] serverInfoManager Istanziato");
//
//			serverInfoManager.cleanJob(job);
//			logger.debug("[MoveAggregationJob::cleanJob] Pulita tabella ecm_job per il job: "+job);
//		}finally{
//			logger.debug("[MoveAggregationJob::deleteJob] END");
//		}
//	}

//	private boolean exitJob(String job) throws UnknownHostException, ServerInfoException {
//		ServerInfo serverInfo = null;
//		String ipAddress = null;
//
//		try{
//			logger.debug("[MoveAggregationJob::exitJob] BEGIN");
//
//			ipAddress = InetAddress.getLocalHost().getHostAddress();
//			logger.debug("[MoveAggregationJob::exitJob] ip (da HostAddress): "+ipAddress);
//
//			serverInfoManager = ServerInfoManager.getInstance();
//
//			logger.debug("[MoveAggregationJob::exitJob] serverInfoManager Istanziato");
//
//			/*
//			if(serverInfoManager.executeJob(job)){
//				logger.debug("[MoveAggregationJob::exitJob] Execute Job on node");
//				return false;
//			}
//			*/
//
//			// primo record della tabella alf_server di alfresco
//			long id = 1;
//
//			serverInfo = serverInfoManager.getServerInfo(id);
//
//			String ip = (serverInfo != null) ? serverInfo.getIpAddress() : "";
//			logger.debug("[MoveAggregationJob::exitJob] ip (da alf_server): "+ip);
//
//			if (ipAddress != null && !ipAddress.equalsIgnoreCase(ip)) {
//				logger.debug("[MoveAggregationJob::exitJob] Exit Job");
//				return true;
//			}
//
//
//		} finally {
//			logger.debug("[MoveAggregationJob::exitJob] END");
//		}
//		return false;
//	}

	private void moveAggregation() throws MoveException{

		logger.debug("[MoveAggregationJob::moveAggregation] BEGIN");

		start(); // Avvia stopwatch

		try{

			//bisogna ricercare nel repository quei nodi marcati come "spostabili"
			//solo questi dovranno essere spostati
			// 1) ricerca dei nodi spostabili
			// 2) ciclo per ogni nodo trovato, effettuare lo spostamento
			// 3) nodo spostato viene cancellato dalla source oppure viene marcato
			// come non visibile (aspect state valore riclassificato)

			List<NodeRef> listaNodi = searchNodeWithProp();
			int size = (listaNodi!=null ? listaNodi.size() : 0);
			dumpElapsed("MoveAggregationJob", "moveAggregation","Numero Nodi da Spostare: "+size, "Fine SearchNodeWithProp");

			logger.debug("[MoveAggregationJob::moveAggregation] Numero Nodi da Spostare: "+size);

			if(listaNodi!=null){

				MoveAggregation aggregation = null;

				String idDestinationParent = null;
				String idSourceNode = null;
				String destinationRepository = null;
				String sourceRepository = null;

				for (NodeRef sourceNodeRef : listaNodi) {

					aggregation = getPropertiesFromAspect(sourceNodeRef);

					if(aggregation!=null){
						idDestinationParent = aggregation.getIdDestinationParent();
						idSourceNode = aggregation.getIdSourceNode();

						destinationRepository = aggregation.getDestinationRepository();
						sourceRepository = aggregation.getSourceRepository();
					}

					boolean crossRepo = isCrossRepository(sourceRepository,destinationRepository);

					if(crossRepo)
					{
                        //spostamento da corrente a deposito
						logger.debug("[MoveAggregationJob::moveAggregation] Spostamento da corrente a deposito");
						long start = System.currentTimeMillis();
						moveCrossRepo(sourceRepository,idSourceNode,destinationRepository,
								idDestinationParent,sourceNodeRef);
						long finished = System.currentTimeMillis();
						logger.debug("[MoveAggregationJob::moveAggregation] Tempo Spostamento da corrente a deposito: "
								+(finished-start)+" ms");
						dumpElapsed("MoveAggregationJob", "moveAggregation","Spostamento Cross Repository da " + idSourceNode
								+ " a "+idDestinationParent, "Fine Move Cross Repository");

					} else {

						//spostamento da corrente a corrente
						logger.debug("[MoveAggregationJob::moveAggregation] Spostamento da corrente a corrente");
						long start = System.currentTimeMillis();
						moveIntraRepo(sourceNodeRef,sourceRepository,idSourceNode,idDestinationParent);
						long finished = System.currentTimeMillis();
						logger.debug("[MoveAggregationJob::moveAggregation] Tempo Spostamento da corrente a corrente: "+(finished-start)+" ms");
						dumpElapsed("MoveAggregationJob", "moveAggregation","Spostamento da " + idSourceNode
								+ " a "+idDestinationParent, "Fine Move Intra Repository");

					}
				}
			}
			// prima di fare una modifica bisogna controllare se il nodo padre possiede l'aspect state
			// con valore spostabile;  se e` cosi non e` possibile effettuare nessuna modifica al nodo
			// bisogna modificare i servizi fin qui fatti inserendo prima di tutto questo controllo
			// Quando poi l'aspect diventa spostato , il nodo e i suoi figli non sono visibili
		} catch (NotSupportedException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"FATAL: transaction not supported: " + e.getMessage());
			throw new MoveException("Transaction not supported.", e);
		} catch (SystemException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"FATAL: transaction manager error: " + e.getMessage());
			throw new MoveException("Transaction manager error.", e);
		} catch (EcmEngineFoundationException e){
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"FATAL: backend services error: " +
					e.getClass().getName() + " - " + e.getCode());
			throw new MoveException("Backend services error: " +
					e.getClass().getName() + " - " + e.getCode());
		} catch (SecurityException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"FATAL: SecurityException error: " + e.getMessage());
			throw new MoveException("SecurityException.", e);
		} catch (IllegalStateException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"FATAL: IllegalStateException error: " + e.getMessage());
			throw new MoveException("IllegalStateException.", e);
		} catch (RollbackException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"Transaction rolled back: " + e.getMessage());
			throw new MoveException("Transaction rolled back.", e);
		} catch (HeuristicMixedException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"Transaction partially rolled back (heuristic rollback): " + e.getMessage());
			throw new MoveException("Transaction partially rolled back (heuristic rollback).", e);
		} catch (HeuristicRollbackException e) {
			logger.error("[MoveAggregationJob::moveAggregation] " +
					"Transaction rolled back (heuristic rollback): " + e.getMessage());
			throw new MoveException("Transaction rolled back (heuristic rollback).", e);
		}finally{
			stop(); // Ferma stopwatch
			logger.debug("[MoveAggregationJob::moveAggregation] END");
		}
	}

	private void moveCrossRepo(String sourceRepository,String idSourceNode,String destinationRepository,
			String idDestinationParent,NodeRef sourceNodeRef) throws NotSupportedException, SystemException,
			NodeRuntimeException, PermissionRuntimeException, AuthenticationRuntimeException, DictionaryRuntimeException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException{

		logger.debug("[MoveAggregationJob::moveCrossRepo] BEGIN");

		Node result = null;

		UserTransaction userTxSource = null;

        UserTransaction userTxDest = null;

		String logCtx = "S: " + idSourceNode
			+ " - SourceRepo: " + sourceRepository
			+ " - D: " + idDestinationParent
			+ " - DestRepo: " + destinationRepository;

		try {

			logger.debug("[MoveAggregationJob::moveCrossRepo] "+logCtx);

            userTxSource = transactionService.getNonPropagatingUserTransaction();

            userTxSource.begin();

			RepositoryManager.setCurrentRepository(sourceRepository);

            //authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			ChildAssociationRef sourceParentRef = nodeService.getPrimaryParent(sourceNodeRef);

			logger.debug("[MoveAggregationJob::moveCrossRepo] Nodo Source Padre : "
					+sourceParentRef.getParentRef().getId());

			QName destinationQName = sourceParentRef.getQName();
			QName destinationAssocTypeQName = sourceParentRef.getTypeQName();

			userTxSource.commit();

			userTxDest = transactionService.getNonPropagatingUserTransaction();

			userTxDest.begin();

			RepositoryManager.setCurrentRepository(destinationRepository);

            //authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			StoreRef spacesStoreDest = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			NodeRef destinationParentRef = new NodeRef(spacesStoreDest, idDestinationParent);

			boolean exist = nodeService.exists(destinationParentRef);

			logger.debug("[MoveAggregationJob::moveCrossRepo] Nodo Destination Padre: "
					+ destinationParentRef.getId() + " esiste? " + exist);

			userTxDest.commit();

			//copyAggregation

			userTxSource = transactionService.getNonPropagatingUserTransaction();

			userTxSource.begin();

			Map<NodeRef, NodeRef> copiedChildren = new HashMap<NodeRef, NodeRef>();

			RepositoryManager.setCurrentRepository(sourceRepository);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			NodeRef parentRef = sourceParentRef.getParentRef();

			userTxSource.commit();

			boolean copyChildren = true;
			NodeRef destinationNodeRef = null;

			//recursiveCopy -->permette di ricreare in deposito la stessa struttura in corrente
			//in realta crea i figli primari , invece i figli secondari e i nodi target delle associazioni
			//normali non vengono create nel deposito ma viene creata uan relazione verso il nodo
			//originario presente in corrente.
			//TODO
			//Eliminare o non fare creare relazioni dal secondario verso nodi del primario
			logger.debug("[MoveAggregationJob::moveCrossRepo] Inizio metodo ricorsivo : 'recursiveCopy'");


			destinationNodeRef = recursiveCopy(sourceNodeRef, parentRef, destinationParentRef,
					destinationAssocTypeQName, destinationQName, copyChildren, copiedChildren,
					sourceRepository,destinationRepository);

			logger.debug("[MoveAggregationJob::moveCrossRepo] Fine metodo ricorsivo : 'recursiveCopy'");

			dumpElapsed("MoveAggregationJob", "moveCrossRepo", logCtx, "Nodo Copia creato.");

			if(destinationNodeRef!=null){
				result = new Node(destinationNodeRef.getId(),destinationRepository);

				logger.debug("[MoveAggregationJob::moveCrossRepo] Uid Nodo Copia creato: "
						+ result.getUid());
			}

			userTxDest = transactionService.getNonPropagatingUserTransaction();

			userTxDest.begin();
			//Dal nodo padre sposato sul deposito elimino gli aspect state e destination
			RepositoryManager.setCurrentRepository(destinationRepository);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			QName stateAspect = resolvePrefixNameToQName("ecm-sys:state");
			nodeService.removeAspect(destinationNodeRef,stateAspect);
			dumpElapsed("MoveAggregationJob", "moveCrossRepo", logCtx, "Rimosso Aspect 'state' dal Nodo spostato.");
			logger.debug("[MoveAggregationJob::moveCrossRepo] Rimosso Aspect 'state' dal nodo : " + destinationNodeRef.getId());

			QName destinationAspect = resolvePrefixNameToQName("ecm-sys:destination");
			nodeService.removeAspect(destinationNodeRef,destinationAspect);
			dumpElapsed("MoveAggregationJob", "moveCrossRepo", logCtx, "Rimosso Aspect 'destination' dal Nodo spostato.");
			logger.debug("[MoveAggregationJob::moveCrossRepo] Rimosso Aspect 'destination' dal nodo : " + destinationNodeRef.getId());

			userTxDest.commit();

			// BEGIN DISABLE AGGREGATION

			userTxSource = transactionService.getNonPropagatingUserTransaction();

			userTxSource.begin();
            //Cancello i nodi figli del nodo source
			RepositoryManager.setCurrentRepository(sourceRepository);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			List<ChildAssociationRef> childAssociations = nodeService.getChildAssocs(sourceNodeRef);

			int size = childAssociations!=null ? childAssociations.size() : 0;

			logger.debug("[MoveAggregationJob::moveCrossRepo] Cancello "+ size +" nodi/o figli.");
			if(size>0){
				for (ChildAssociationRef childAssoc : childAssociations) {
					if(childAssoc!=null){
						nodeService.removeChildAssociation(childAssoc);
						logger.debug("[MoveAggregationJob::moveCrossRepo] Associazione child eliminata.");
						dumpElapsed("MoveAggregationJob", "moveCrossRepo", logCtx, "Associazione child eliminata.");
					}
				}
			}

			//ecm-sys:ecmengineSystemModel
			//aspect ecm-sys:state
			//<property name="ecm-sys:stato">

			//aspect ecm-sys:state
			//<property name="ecm-sys:stato"> proprieta dell'aspect

			QName stateProp = resolvePrefixNameToQName("ecm-sys:stato");
			String valoreStatoNodo = "spostato";

			//setto la proprieta ecm-sys:stato dell'aspect ecm-sys:state
			//del nodo source con valore "spostato"
			nodeService.setProperty(sourceNodeRef, stateProp, valoreStatoNodo);
			dumpElapsed("MoveAggregationJob", "moveCrossRepo", logCtx, "Modificata property 'stato' dell'Aspect 'state'");

			//TODO: in realta l'aggregazione deve essere Cancellata del tutto e non disabilitata
			//con l'aggiunta di un aspect; si dovrebbe cancellare l'aggregazione solo dopo che
			//il job di spostamento e` andato a buon fine.
			logger.debug("[MoveAggregationJob::moveCrossRepo] Modificata property 'stato' dell'Aspect 'state' del nodo : "
					+ sourceNodeRef.getId());

            //Dal nodo sorgente presente nel corrente elimino l'aspect destination
			nodeService.removeAspect(sourceNodeRef,destinationAspect);
			dumpElapsed("MoveAggregationJob", "moveCrossRepo", logCtx, "Rimosso Aspect 'destination' dal Nodo.");
			logger.debug("[MoveAggregationJob::moveCrossRepo] Rimosso Aspect 'destination' dal nodo : " + sourceNodeRef.getId());

			//END DISABLE AGGREGATION
			userTxSource.commit();

			//INSERIMENTO AUDIT
			insertAudit("MoveAggregationJob", "moveCrossRepo", logCtx, result.getUid(),
					"Source: "+sourceNodeRef.getId()+" RepoSource: "+sourceRepository
					+" -- Dest Parent: " + destinationParentRef.getId()+" RepoDest: "+destinationRepository);

		}finally{
			logger.debug("[MoveAggregationJob::moveCrossRepo] END");
		}
	}

	private void moveIntraRepo(NodeRef sourceNodeRef,String sourceRepository,String idSourceNode,
			String idDestinationParent) throws NotSupportedException, SystemException, DictionaryRuntimeException
			, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException{

		//riclassificazioni(voci di titolario differenti)
		//spostamenti (la stessa voce di titolario)
		logger.debug("[MoveAggregationJob::moveIntraRepo] BEGIN");

		Node result = null;

		String logCtx = "S: " + idSourceNode + " - D: " + idDestinationParent;

		try{

			UserTransaction userTxSource = transactionService.getNonPropagatingUserTransaction();

			userTxSource.begin();

			RepositoryManager.setCurrentRepository(sourceRepository);

			logger.debug("[MoveAggregationJob::moveIntraRepo] Spostamento da Corrente a Corrente");

			StoreRef spacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

			NodeRef destinationParentRef = new NodeRef(spacesStore, idDestinationParent);

			ChildAssociationRef sourceParentRef = nodeService.getPrimaryParent(sourceNodeRef);

			QName destinationQName = sourceParentRef.getQName();
			QName destinationAssocTypeQName = sourceParentRef.getTypeQName();

			logger.debug("[MoveAggregationJob::moveIntraRepo] Nome Nuova Associazione : "
					+destinationQName.toString());

			logger.debug("[MoveAggregationJob::moveIntraRepo] Tipo Nuova Associazione : "
					+destinationAssocTypeQName.toString());

			NodeRef copyNodeRef = copyService.copyAndRename(sourceNodeRef, destinationParentRef,
					destinationAssocTypeQName,destinationQName, true);
			//NodeRef copyNodeRef = copyService.copy(sourceNodeRef, destinationParentRef, destinationAssocTypeQName,destinationQName, true);

			result = new Node(copyNodeRef.getId());
			dumpElapsed("MoveAggregationJob", "moveIntraRepo", logCtx, "Nodo Copia creato.");
			logger.debug("[MoveAggregationJob::moveIntraRepo] Uid Nodo Copia creato: "+ result.getUid());

			QName stateAspect = resolvePrefixNameToQName("ecm-sys:state");
			nodeService.removeAspect(copyNodeRef,stateAspect);
			dumpElapsed("MoveAggregationJob", "moveIntraRepo", logCtx, "Rimosso Aspect 'state' dal Nodo copiato.");
			logger.debug("[MoveAggregationJob::moveIntraRepo] Rimosso Aspect 'state' dal nodo : " + copyNodeRef.getId());

			QName destinationAspect = resolvePrefixNameToQName("ecm-sys:destination");
			nodeService.removeAspect(copyNodeRef,destinationAspect);
			dumpElapsed("MoveAggregationJob", "moveIntraRepo", logCtx, "Rimosso Aspect 'destination' dal Nodo copiato.");
			logger.debug("[MoveAggregationJob::moveIntraRepo] Rimosso Aspect 'destination' dal nodo : " + copyNodeRef.getId());


			//TODO:
			//in questo caso (da corrente a corrente) cosa fare dell'aggragazione sorgente??
			// si deve distinguere tra riclassificazione e spostamento?
			// a quanto pare in caso di riclassificazione l' aggregazione nella source deve rimanere
			// ma senza contenuti;
			// in caso di spostamento invece l'aggregazione va spostata in destination e cancellata
			// dalla source
			//Riepilogando:
			// Riclassificazione : --> metodo copy di copyService e succesive modifiche all'aggregazione
			//in source:
			//assume uno stato &quot;R&quot; = riclassificato
			//e` vuoto di contenuti,conserva i suoi metadati

			// Spostamento : --> metodo moveNode di nodeService?? questo metodo va bene?? Non copia i figli?

			//Implementazione solo di Riclassificazione
			//Cancello i nodi figli del nodo source(basta questo??)
			List<ChildAssociationRef> childAssociations = nodeService.getChildAssocs(sourceNodeRef);

			int size = childAssociations!=null ? childAssociations.size() : 0;

			logger.debug("[MoveAggregationJob::moveIntraRepo] Cancellare "+ size +" nodi figli.");
			if(size>0){
				for (ChildAssociationRef childAssoc : childAssociations) {
					if(childAssoc!=null){
						nodeService.removeChildAssociation(childAssoc);
						logger.debug("[MoveAggregationJob::moveIntraRepo] Associazione child eliminata.");
						dumpElapsed("MoveAggregationJob", "moveIntraRepo", logCtx, "Associazione child eliminata.");
					}
				}
			}

			//<property name="ecm-sys:stato"> proprieta dell'aspect "state"
			QName stateProp = resolvePrefixNameToQName("ecm-sys:stato");
			String valoreStatoNodo = "riclassificato";

			//add aspect ecm-sys:state con proprieta ecm-sys:stato di valore "ri-classificato"
			//al nodo source
			//nodeService.addAspect(sourceNodeRef, stateAspect, stateAspectProps);

			//in realta l'aspect e` gia esistente; bisogna modificare il valore
			// della proprieta da spostabile a ri-classificato
			nodeService.setProperty(sourceNodeRef, stateProp, valoreStatoNodo);
			dumpElapsed("MoveAggregationJob", "moveIntraRepo", logCtx, "Modificata property 'stato' dell'Aspect 'state' del nodo.");
			logger.debug("[MoveAggregationJob::moveIntraRepo] Modificata property 'stato' dell'Aspect 'state' " +
					"del nodo : " + sourceNodeRef.getId());

			userTxSource.commit();

			// INSERIMENTO AUDIT
			insertAudit("MoveAggregationJob", "moveIntraRepo", logCtx, result.getUid(),
					"Source :"+sourceNodeRef.getId() +" -- Destination Parent : " + destinationParentRef.getId());
		}finally{
			logger.debug("[MoveAggregationJob::moveIntraRepo] END");
		}
	}

	private List<NodeRef> searchNodeWithProp() throws DictionaryRuntimeException, NotSupportedException,
	SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException,
	HeuristicRollbackException{

		logger.debug("[MoveAggregationJob::searchNodeWithProp] BEGIN");
		//1) ricerca dei nodi spostati
		//a partire dalla company_home(radice) prendere ogni nodo figlio
		// e verificare se possiede un aspect con proprieta` di valore "spostabile"
		// oppure e` possibile fare una ricerca con lucene e prendere i nodi con quel particolare aspect
		// Utilizzare searchService per puntare direttamente ai nodi che hanno un certo aspect

		//RepositoryManager.setCurrentRepository("primary");
		logger.debug("[MoveAggregationJob::searchNodeWithProp] Ricerca nel repository : "
				+RepositoryManager.getCurrentRepository());

		StoreRef spacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

		//proprieta ecm-sys:stato (dell'aspect ecm-sys:state)
		SearchParameters searchParams = new SearchParameters();
		searchParams.addStore(spacesStore);
		searchParams.setLimitBy(LimitBy.UNLIMITED);
		searchParams.setLimit(0);
		searchParams.setLanguage(SearchService.LANGUAGE_LUCENE);
		//ricerca per aspect e poi filtrare sulla property relativa
		//searchParams.setQuery("ASPECT:\"{http://www.doqui.it/model/ecmengine/system/1.0}state\"");

		//ricerca direttamente per property
		//@ecm-sys\\:stato:\"spostabile\"
		//searchParams.setQuery("@ecm-sys\\:stato:\"spostabile\"");
		//searchParams.setQuery("@{http://www.doqui.it/model/ecmengine/system/1.0}\\:stato:\"spostabile\"");

		searchParams.setQuery("@ecm-sys\\:stato:\"spostabile\"");

		ResultSet resultSet = null;
		List<NodeRef> listaNodi = null;
		//List<NodeRef> nodiConProp = null;

		UserTransaction userTxSource = transactionService.getNonPropagatingUserTransaction();

		try
		{
			userTxSource.begin();

			logger.debug("[MoveAggregationJob::searchNodeWithProp] searchService is "
					+ (searchService!=null ? " <> null" :" null" ));
			logger.debug("[MoveAggregationJob::searchNodeWithProp] Query : " + searchParams.getQuery());
			resultSet = searchService.query(searchParams);
			logger.debug("[MoveAggregationJob::searchNodeWithProp] Risultati trovati: " + resultSet.length());

			if(resultSet.length()>0){
				listaNodi = resultSet.getNodeRefs();
			}

			userTxSource.commit();

			/*
			QName stateProp = resolvePrefixNameToQName("ecm-sys:stato");
			String valoreStatoNodo = "spostabile";

			if (resultSet.length() > 0){
				listaNodi = resultSet.getNodeRefs();
				if(listaNodi!=null){
					Map<QName,Serializable> propMap = null;
					nodiConProp = new ArrayList<NodeRef>();
					for (NodeRef ref : listaNodi) {
						propMap = nodeService.getProperties(ref);
						if(propMap.containsKey(stateProp) && propMap.containsValue(valoreStatoNodo))
						{
							nodiConProp.add(ref);
						}
					}
				}
			}

			searchParams.setLanguage(SearchService.LANGUAGE_XPATH);
			searchParams.setQuery("/app:company_home/cm:TestSposta");
			searchParams.setLimitBy(LimitBy.FINAL_SIZE);
			searchParams.setLimit(1);

			resultSet = searchService.query(searchParams);
			logger.debug("[MoveAggregationJob::searchNodeWithProp] Query per XPATH : "+searchParams.getQuery());
			if(resultSet.length()>0){
				logger.debug("[MoveAggregationJob::searchNodeWithProp] resultSet per XPATH diverso da Null");
				logger.debug("[MoveAggregationJob::searchNodeWithProp] Numero Risultati trovati : "
						+resultSet.length());
				if (resultSet.getNodeRefs()!=null && resultSet.getNodeRefs().size()>0){
					NodeRef nodo = resultSet.getNodeRef(0);
					Map<QName,Serializable> prop = nodeService.getProperties(nodo);
					String valore =(String) prop.get(stateProp);
					logger.debug("[MoveAggregationJob::searchNodeWithProp] valore property 'ecm-sys:stato' e` : "
							+valore);
				}
			}
			*/
		}
		finally{
			if(resultSet != null)
			{
				resultSet.close();
			}
			logger.debug("[MoveAggregationJob::searchNodeWithProp] END");
		}
		return listaNodi;
	}

	private MoveAggregation getPropertiesFromAspect(NodeRef sourceNodeRef)
	throws DictionaryRuntimeException, NotSupportedException, SystemException,
	SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		logger.debug("[MoveAggregationJob::getPropertiesFromAspect] BEGIN");
		MoveAggregation aggreg = null;

		try{

			UserTransaction userTxSource = transactionService.getNonPropagatingUserTransaction();

			userTxSource.begin();

			QName destinationAspect = resolvePrefixNameToQName("ecm-sys:destination");

			if(nodeService.hasAspect(sourceNodeRef, destinationAspect)){

				Map<QName,Serializable> nodeProp = nodeService.getProperties(sourceNodeRef);
				QName idNodeDestinationProp = resolvePrefixNameToQName("ecm-sys:idNodeDestination");
				QName repoDestinationProp = resolvePrefixNameToQName("ecm-sys:repoDestination");
				QName idNodeSourceProp = resolvePrefixNameToQName("ecm-sys:idNodeSource");
				QName repoSourceProp = resolvePrefixNameToQName("ecm-sys:repoSource");

				aggreg =  new MoveAggregation();

				aggreg.setIdDestinationParent((String)nodeProp.get(idNodeDestinationProp));
				aggreg.setDestinationRepository((String)nodeProp.get(repoDestinationProp));

				aggreg.setIdSourceNode((String)nodeProp.get(idNodeSourceProp));
				aggreg.setSourceRepository((String)nodeProp.get(repoSourceProp));
			}

			userTxSource.commit();

		}finally{
			logger.debug("[MoveAggregationJob::getPropertiesFromAspect] END");
		}
		return aggreg;
	}


	private QName resolvePrefixNameToQName(String prefixName) {
		logger.debug("[MoveAggregationJob::resolvePrefixNameToQName] BEGIN");
		QName result = null;
		String [] nameParts = QName.splitPrefixedQName(prefixName);

		try {
            if (logger.isDebugEnabled()) {
			    logger.debug("[MoveAggregationJob::resolvePrefixNameToQName] Resolving to QName: " + prefixName);
		    }
			result = QName.createQName(nameParts[0], nameParts[1],namespaceService);
            if (logger.isDebugEnabled()) {
			    logger.debug("[MoveAggregationJob::resolvePrefixNameToQName] QName: " + result.toString());
		    }
		} catch (RuntimeException e) {
			logger.warn("[MoveAggregationJob::resolvePrefixNameToQName] " +
					"Error resolving to QName \"" + prefixName + "\": " + e.getMessage());
			throw new RuntimeException();	// FIXME
		} finally {
			logger.debug("[MoveAggregationJob::resolvePrefixNameToQName] END");
		}

		return result;
	}


	/**
	 * Recursive copy algorithm
	 *
	 * @throws NodeRuntimeException
	 * @throws AuthenticationRuntimeException
	 * @throws PermissionRuntimeException
	 */
	private NodeRef recursiveCopy(NodeRef sourceNodeRef, NodeRef sourceParentRef,
			NodeRef destinationParentRef, QName destinationAssocTypeQName,
			QName destinationQName, boolean copyChildren, Map<NodeRef, NodeRef> copiedChildren,
			String sourceRepo, String destRepo)
	throws NodeRuntimeException,PermissionRuntimeException, AuthenticationRuntimeException {

		NodeRef destinationNodeRef = null;

		UserTransaction userTxSource = null;

		UserTransaction userTxDest = null;

		try {

			logger.debug("[MoveAggregationJob::recursiveCopy] BEGIN");

            userTxSource = transactionService.getNonPropagatingUserTransaction();

            userTxSource.begin();

			Map<QName, Serializable> properties =  null;
			Set<QName> sourceAspects = null;

			RepositoryManager.setCurrentRepository(sourceRepo);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			QName sourceType = nodeService.getType(sourceNodeRef);
			properties = nodeService.getProperties(sourceNodeRef);
			sourceAspects = nodeService.getAspects(sourceNodeRef);

			userTxSource.commit();

			// Create the new node
			userTxDest = transactionService.getNonPropagatingUserTransaction();

			userTxDest.begin();

			RepositoryManager.setCurrentRepository(destRepo);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			boolean esisteStore = nodeService.exists(destinationParentRef.getStoreRef());

			logger.debug("[MoveAggregationJob::recursiveCopy] Lo Store Destination esiste ? : "+esisteStore);

			final boolean destExists = nodeService.exists(destinationParentRef);

			logger.debug("[MoveAggregationJob::recursiveCopy] " +
					"Repository di Destinazione : " + RepositoryManager.getCurrentRepository());
			logger.debug("[MoveAggregationJob::recursiveCopy] '" + destinationParentRef +
					"' esiste: " + destExists);

			ChildAssociationRef destinationChildAssocRef = nodeService.createNode(
					destinationParentRef, destinationAssocTypeQName,
					destinationQName, sourceType, null);

			destinationNodeRef = destinationChildAssocRef.getChildRef();

			logger.debug("[MoveAggregationJob::recursiveCopy] Nodo spostato: " +
					destinationNodeRef.getId());

			copiedChildren.put(sourceNodeRef, destinationNodeRef);

			for (QName aspect : sourceAspects) {
				nodeService.addAspect(destinationNodeRef, aspect, null);
				logger.debug("[MoveAggregationJob::recursiveCopy] Aspect copiato: " + aspect);
			}

			//setto sul nuovo nodo appena creato tutte le properties, anche quelle degli aspects
			nodeService.setProperties(destinationNodeRef, properties);
			logger.debug("[MoveAggregationJob::recursiveCopy] Property copiate: " +
					properties.size());

			// Prevent any rules being fired on the new destination node
			//ruleService.disableRules(destinationNodeRef);


			//	Apply the copy aspect to the new node
			//Map<QName, Serializable> copyProperties = new HashMap<QName, Serializable>();
			//copyProperties.put(ContentModel.PROP_COPY_REFERENCE, sourceNodeRef);
			//nodeService.addAspect(destinationNodeRef, ContentModel.ASPECT_COPIEDFROM, copyProperties);

			// Copy the aspects
			//copyAspects(destinationNodeRef, copyDetails);

			userTxDest.commit();

			// Copy the associations
			copyAssociations(sourceNodeRef, destinationNodeRef, copyChildren,
					copiedChildren, sourceRepo, destRepo);

			// Copy permissions
			copyPermissions(sourceNodeRef, destinationNodeRef, sourceRepo, destRepo);


		} catch (NotSupportedException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
		} catch (SystemException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
			e.printStackTrace();
		} catch (RollbackException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			logger.error("[MoveAggregationJob::recursiveCopy] Eccezione: "+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			//ruleService.enableRules(destinationNodeRef);
			logger.debug("[MoveAggregationJob::recursiveCopy] END");
		}
		return destinationNodeRef;
	}

	/**
	 * Copies the permissions of the source node reference onto the destination node reference
	 *
	 * @param sourceNodeRef			the source node reference
	 * @param destinationNodeRef	the destination node reference
	 * @throws AuthenticationRuntimeException
	 * @throws PermissionRuntimeException
	 */
	private void copyPermissions(NodeRef sourceNodeRef, NodeRef destinationNodeRef,String sourceRepo ,String destRepo)
	throws PermissionRuntimeException, AuthenticationRuntimeException
	{
		try{
			logger.debug("[MoveAggregationJob::copyPermissions] BEGIN");

			UserTransaction userTxSource = transactionService.getNonPropagatingUserTransaction();

            UserTransaction userTxDest = transactionService.getNonPropagatingUserTransaction();

            userTxSource.begin();

			RepositoryManager.setCurrentRepository(sourceRepo);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			AccessStatus accessStatus = permissionService.hasPermission(sourceNodeRef, PermissionService.READ_PERMISSIONS);

			userTxSource.commit();

			if(accessStatus == AccessStatus.ALLOWED)
			{
				userTxSource = transactionService.getNonPropagatingUserTransaction();

				userTxSource.begin();

				// Get the permission details of the source node reference
				Set<AccessPermission> permissions = permissionService.getAllSetPermissions(sourceNodeRef);
				boolean includeInherited = permissionService.getInheritParentPermissions(sourceNodeRef);

				userTxSource.commit();

				userTxDest.begin();

				RepositoryManager.setCurrentRepository(destRepo);

				// authenticate as the system user
				authenticationComponent.setSystemUserAsCurrentUser();

				AccessStatus writePermission = permissionService.hasPermission(destinationNodeRef, PermissionService.CHANGE_PERMISSIONS);
				if (writePermission.equals(AccessStatus.ALLOWED) || authenticationService.isCurrentUserTheSystemUser() )
				{
					// Set the permission values on the destination node
					for (AccessPermission permission : permissions)
					{
						permissionService.setPermission(
								destinationNodeRef,
								permission.getAuthority(),
								permission.getPermission(),
								permission.getAccessStatus().equals(AccessStatus.ALLOWED));
					}
					permissionService.setInheritParentPermissions(destinationNodeRef, includeInherited);
				}

				userTxDest.commit();
			}

		} catch (NotSupportedException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());
			e.printStackTrace();
		} catch (SystemException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (IllegalStateException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (RollbackException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			logger.error("[MoveAggregationJob::copyPermissions] Eccezione: "+e.getMessage());

			e.printStackTrace();
		}finally{
			logger.debug("[MoveAggregationJob::copyPermissions] END");
		}
	}
	/**
	 * Copies the associations (child and target) for the node type and aspects onto the
	 * destination node.
	 * <p>
	 * If copyChildren is true then all child nodes of primary child associations are copied
	 * before they are associatied with the destination node.
	 *
	 * @param destinationNodeRef	the destination node reference
	 *
	 * @param copyChildren			indicates whether the primary children are copied or not
	 * @param copiedChildren        set of children already copied
	 * @throws NodeRuntimeException
	 * @throws AuthenticationRuntimeException
	 * @throws PermissionRuntimeException
	 */
	private void copyAssociations(NodeRef sourceNodeRef,NodeRef destinationNodeRef,boolean copyChildren,
			Map<NodeRef, NodeRef> copiedChildren,String sourceRepo,String destRepo)
	throws NodeRuntimeException, PermissionRuntimeException, AuthenticationRuntimeException
	{
		try{
			logger.debug("[MoveAggregationJob::copyAssociations] BEGIN");
			copyChildAssociations(sourceNodeRef , destinationNodeRef,copyChildren, copiedChildren,sourceRepo,destRepo);
			copyTargetAssociations(sourceNodeRef, destinationNodeRef,sourceRepo,destRepo);
		}finally{
			logger.debug("[MoveAggregationJob::copyAssociations] END");
		}

	}


	/**
	 * Copies the child associations onto the destiantion node reference.
	 * <p>
	 * If copyChildren is true then the nodes at the end of a primary assoc will be copied before they
	 * are associated.
	 *
	 * @param sourceNodeRef			the source node reference
	 * @param destinationNodeRef	the destination node reference
	 * @param copyChildren			indicates whether to copy the primary children
	 * @throws AuthenticationRuntimeException
	 * @throws PermissionRuntimeException
	 * @throws NodeRuntimeException
	 */
	private void copyChildAssociations(NodeRef sourceNodeRef,NodeRef destinationNodeRef,boolean copyChildren,
			Map<NodeRef, NodeRef> copiedChildren ,String sourceRepo ,String destRepo)
	throws NodeRuntimeException, PermissionRuntimeException, AuthenticationRuntimeException
	{

		try{
			logger.debug("[MoveAggregationJob::copyChildAssociations] BEGIN");

			UserTransaction userTxSource = transactionService.getNonPropagatingUserTransaction();

	        UserTransaction userTxDest = transactionService.getNonPropagatingUserTransaction();

	        userTxSource.begin();

			RepositoryManager.setCurrentRepository(sourceRepo);

            //authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(sourceNodeRef);

			userTxSource.commit();

			if (childAssocs != null)
			{
				logger.debug("[MoveAggregationJob::copyChildAssociations] Nodi figli da ricreare in Repo Secondary: "
						+childAssocs.size());
				for (ChildAssociationRef childAssoc : childAssocs)
				{
					if (copyChildren == true)
					{
						if (childAssoc.isPrimary() == true)
						{
							logger.debug("[MoveAggregationJob::copyChildAssociations]" +
									" Nodo figlio primario da ricreare in Repo Secondary.");
							// Do not recurse further, if we've already copied this node
							if (copiedChildren.containsKey(childAssoc.getChildRef()) == false &&
									copiedChildren.containsValue(childAssoc.getChildRef()) == false)
							{
								// Copy the child
								recursiveCopy(childAssoc.getChildRef(),childAssoc.getParentRef(),destinationNodeRef,
										childAssoc.getTypeQName(),
										childAssoc.getQName(),
										copyChildren,
										copiedChildren,sourceRepo,destRepo);
							}
						}
						else
						{
							logger.debug("[MoveAggregationJob::copyChildAssociations] Nodo figlio Non Primario da ricreare.");

							//Add the child (I figli non primari non vengono ricreati nel deposito)Cosa fare??
							//TODO: NB i figli secondari non vengono ricreati, ma solo viene creata la relazione
							//tra padre e figlio( e il figlio si trova nel deposito)
							NodeRef childRef = childAssoc.getChildRef();

							userTxDest.begin();

							RepositoryManager.setCurrentRepository(destRepo);

							// authenticate as the system user
							authenticationComponent.setSystemUserAsCurrentUser();

							nodeService.addChild(destinationNodeRef, childRef, childAssoc.getTypeQName(), childAssoc.getQName());

							userTxDest.commit();
						}
					}
				}
			}
		} catch (NotSupportedException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (SystemException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (IllegalStateException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (RollbackException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			logger.error("[MoveAggregationJob::copyChildAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		}finally{
			logger.debug("[MoveAggregationJob::copyChildAssociations] END");
		}
	}

	/**
	 * Copies the target associations onto the destination node reference.
	 *
	 * @param sourceNodeRef	the destination node reference
	 * @param destinationNodeRef	the destination node reference	 *
	 * @throws NodeRuntimeException
	 */
	private void copyTargetAssociations(NodeRef sourceNodeRef, NodeRef destinationNodeRef,
			String sourceRepo ,String destRepo)
	throws NodeRuntimeException
	{
		try{
			logger.debug("[MoveAggregationJob::copyTargetAssociations] BEGIN");

			UserTransaction userTxSource = transactionService.getNonPropagatingUserTransaction();

            UserTransaction userTxDest = transactionService.getNonPropagatingUserTransaction();

            userTxSource.begin();

			RepositoryManager.setCurrentRepository(sourceRepo);

			//authenticate as the system user
			authenticationComponent.setSystemUserAsCurrentUser();

			List<AssociationRef> nodeAssocRefs = nodeService.getTargetAssocs(sourceNodeRef,
					RegexQNamePattern.MATCH_ALL);

			userTxSource.commit();


			if (nodeAssocRefs != null)
			{

				userTxDest.begin();

				RepositoryManager.setCurrentRepository(destRepo);

				//authenticate as the system user
				authenticationComponent.setSystemUserAsCurrentUser();

				for (AssociationRef assocRef : nodeAssocRefs)
				{
					NodeRef targetRef = assocRef.getTargetRef();

					boolean exists = false;
					for (AssociationRef assocRef2 : nodeService.getTargetAssocs(destinationNodeRef, assocRef.getTypeQName()))
					{
						if (targetRef.equals(assocRef2.getTargetRef()) == true)
						{
							exists = true;
							break;
						}
					}

					if (exists == false)
					{
						// Add the association(aggiunge le associazioni di tipo reference verso il nodo che si trova
						//nel corrente ma questo nodo non viene ricreato nel deposito; Cosa fare? )
						//TODO:
						// crea la relazione verso il nodo presente in corrente , ma non crea il nodo in deposito
						nodeService.createAssociation(destinationNodeRef, targetRef, assocRef.getTypeQName());
					}
				}
				userTxDest.commit();
			}
		} catch (NotSupportedException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (SystemException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (IllegalStateException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (RollbackException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			logger.error("[MoveAggregationJob::copyTargetAssociations] Eccezione: "+e.getMessage());

			e.printStackTrace();
		}finally{
			logger.debug("[MoveAggregationJob::copyTargetAssociations] END");
		}
	}

    private void init(JobExecutionContext ctx){

    	try {
    		logger.debug("[MoveAggregationJob::init] BEGIN");

    		JobDataMap jobData = ctx.getJobDetail().getJobDataMap();

    		//extract the object to use
    		Object object = null;

    		//object = jobData.get("nodeService");
    		object = jobData.get(ECMENGINE_NODE_SERVICE_BEAN);
    		if (object == null || !(object instanceof NodeService)) {
    			throw new IllegalArgumentException(
    					"MoveAggregationJob must contain valid 'nodeService' reference");
    		}
    		nodeService =(NodeService)object;

    		//object = jobData.get("transactionService");
    		object = jobData.get(ECMENGINE_TRANSACTION_SERVICE_BEAN);
    		if (object == null || !(object instanceof TransactionService)) {
    			throw new IllegalArgumentException(
    					"MoveAggregationJob must contain valid 'transactionService' reference");
    		}
    		transactionService =(TransactionService)object;

    		//object = jobData.get("authenticationService");
    		object = jobData.get(ECMENGINE_AUTHENTICATION_SERVICE_BEAN);
    		if (object == null || !(object instanceof AuthenticationService)) {
    			throw new IllegalArgumentException(
    					"MoveAggregationJob must contain valid 'authenticationService' reference");
    		}
    		authenticationService =(AuthenticationService)object;

    		//object = jobData.get("searchService");
    		object = jobData.get(ECMENGINE_SEARCH_SERVICE_BEAN);
    		if (object == null || !(object instanceof SearchService)) {
    			throw new IllegalArgumentException(
    					"MoveAggregationJob must contain valid 'searchService' reference");
    		}
    		searchService = (SearchService) object;

    		//copyService
    		//object = jobData.get("copyService");
    		object = jobData.get(ECMENGINE_COPY_SERVICE_BEAN);

    		if (object == null || !(object instanceof CopyService)) {
    			throw new IllegalArgumentException("MoveAggregationJob must contain valid 'copyService' reference");
    		}
    		copyService =(CopyService)object;

    		//object = jobData.get("namespaceService");
    		object = jobData.get(ECMENGINE_NAMESPACE_SERVICE_BEAN);

    		if (object == null || !(object instanceof NamespaceService)) {
    			throw new IllegalArgumentException("MoveAggregationJob must contain valid 'namespaceService' reference");
    		}
    		namespaceService = (NamespaceService)object;

    		//object = jobData.get("permissionService");
    		object = jobData.get(ECMENGINE_PERMISSION_SERVICE_BEAN);

    		if (object == null || !(object instanceof PermissionService)) {
    			throw new IllegalArgumentException("MoveAggregationJob must contain valid 'permissionService' reference");
    		}
    		permissionService = (PermissionService)object;

    		object = jobData.get(ECMENGINE_AUDIT_MANAGER_BEAN);
    		if (object == null || !(object instanceof AuditBusinessInterface)) {
    			throw new IllegalArgumentException("MoveAggregationJob must contain valid '"+ECMENGINE_AUDIT_MANAGER_BEAN+"' reference");
    		}
    		auditManager = (AuditBusinessInterface)object;

    	} finally {
    		logger.debug("[MoveAggregationJob::init] END");
    	}
	}

    private static boolean isCrossRepository(String source, String destination) {
		final boolean sourceSpecified = isValidName(source);
		final boolean destinationSpecified = isValidName(destination);

		if (!sourceSpecified && !destinationSpecified) {
			return false;
		}
		if (sourceSpecified && destinationSpecified
				&& source.equals(destination)) {
			return false;
		}
		return true;
	}

	private static boolean isValidName(String name) {
		return (name != null)
			? (name.length() > 0)
			: false;
	}

	private void insertAudit(String className, String methodName, String logContext,
			String idOggetto, String descrizioneOggetto) {
		logger.debug("[MoveAggregationJob::insertAudit] BEGIN");
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
			logger.debug("[MoveAggregationJob::insertAudit] END");
		}
	}

	/**
	 * Azzera e avvia la misurazione dei tempi da parte dello stopwatch.
	 */
	protected void start() {
		stopwatch = new StopWatch(ECMENGINE_STOPWATCH_LOG_CATEGORY);
		stopwatch.start();
	}

	/**
	 * Arresta la misurazione dei tempi da parte dello stopwatch.
	 */
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

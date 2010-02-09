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

package it.doqui.index.ecmengine.business.job.model;

import static it.doqui.index.ecmengine.util.EcmEngineConstants.*;

import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.ecmengine.business.job.JobBusinessInterface;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.repo.dictionary.DictionaryModelType;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CustomModelActivationJob implements Job {

	private static Log logger = LogFactory.getLog(ECMENGINE_JOB_MODEL_LOG_CATEGORY);
	private static boolean running = false;

	private RepoAdminService repoAdminService;
	private SearchService searchService;
	private NodeService nodeService ;
	private TransactionService transactionService;
	private NamespaceService namespaceService;
	private TenantAdminService tenantAdminService;
	private RepositoryLocation repoModelsLocation;
	private DictionaryModelType dictionaryModelType;
    private JobBusinessInterface jobManager;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("[CustomModelActivationJob::execute] BEGIN");

        // MB abilito il singleton del JOB per decongestionare ECMEngine in caso di troppe esecuzioni parallele
		if (!running) {
			synchronized (this) {
				if (!running) {
					running = true;
				} else {
					logger.debug("[CustomModelActivationJob::execute] job already running");
					logger.debug("[CustomModelActivationJob::execute] END");
					return;
				}
			}
		} else {
			logger.debug("[CustomModelActivationJob::execute] job already running");
			logger.debug("[CustomModelActivationJob::execute] END");
			return;
		}

		//final String originalRepoId = RepositoryManager.getCurrentRepository();

		try {
			repoAdminService    = (RepoAdminService)context.getJobDetail().getJobDataMap().get(ECMENGINE_REPO_ADMIN_SERVICE_BEAN);
			searchService       = (SearchService)context.getJobDetail().getJobDataMap().get(ECMENGINE_SEARCH_SERVICE_BEAN);
			nodeService         = (NodeService)context.getJobDetail().getJobDataMap().get(ECMENGINE_NODE_SERVICE_BEAN);
			transactionService  = (TransactionService)context.getJobDetail().getJobDataMap().get(ECMENGINE_TRANSACTION_SERVICE_BEAN);
			namespaceService    = (NamespaceService)context.getJobDetail().getJobDataMap().get(ECMENGINE_NAMESPACE_SERVICE_BEAN);
			tenantAdminService  = (TenantAdminService)context.getJobDetail().getJobDataMap().get(ECMENGINE_TENANT_ADMIN_SERVICE_BEAN);
			repoModelsLocation  = (RepositoryLocation)context.getJobDetail().getJobDataMap().get(ECMENGINE_CUSTOM_MODEL_LOCATION_BEAN);
			dictionaryModelType = (DictionaryModelType)context.getJobDetail().getJobDataMap().get(ECMENGINE_DICTIONARY_MODEL_TYPE_BEAN);
            jobManager          = (JobBusinessInterface)context.getJobDetail().getJobDataMap().get(ECMENGINE_JOB_MANAGER_BEAN);
			for (Repository repository : RepositoryManager.getInstance().getRepositories()) {
				RepositoryManager.setCurrentRepository(repository.getId());
                if (logger.isDebugEnabled()) {
				    logger.debug("[CustomModelActivationJob::execute] current repository: "+RepositoryManager.getCurrentRepository());
                }
				AuthenticationUtil.setSystemUserAsCurrentUser();
                // Se su quel repository non ho dei tenant in creazione o delete
                if( !isTenantJobRunning() ) {
                    List<RepoModelDefinition> models = repoAdminService.getModels();
                    for (final RepoModelDefinition model : models) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[CustomModelActivationJob::execute] repository admin current model: "+model.toString());
                        }
                        // MB
                        // E' stato segnalato un problema di reploy dei singoli custom model
                        // Il caso e' quello di custom model a cascata, dipendenti l'uno dall'altro
                        // In quel caso, se non sono stati deployate le dipendenze c'e' un errore di
                        // org.alfresco.service.cmr.dictionary.DictionaryException: Failed to compile model acta-arc:model]
                        // In questo modo, con dei tentativi di deploy dei singoli custom model, dovremmo riuscire a fare un deploy
                        // incrementale
                        // Strano cmq perche' processModel e' in un try-catch
                        try {
                           final RetryingTransactionCallback<Object> processModelCallback = new RetryingTransactionCallback<Object>()
                           {
                               public Object execute() throws Throwable
                               {
                                   processModel(model);
                                   return null;
                               }
                           };
                           transactionService.getRetryingTransactionHelper().doInTransaction(processModelCallback, false);
                        } catch(org.alfresco.service.cmr.dictionary.DictionaryException de) {
                           logger.error("[CustomModelActivationJob::execute] ERROR", de);
                        }

                    }
                    // Check all tenants
                    List<Tenant> tenantList = tenantAdminService.getAllTenants();
                    for (Tenant tenant : tenantList) {
                        // MB: evito che un errore di deploy su un Tenant mi invalidi il deploy del CustomModel su un altro tenant
                        try {
                            if (tenant.isEnabled()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("[CustomModelActivationJob::execute] processing tenant '"+tenant.getTenantDomain()+"', repository: "+RepositoryManager.getCurrentRepository());
                                }

                                // MB; Su ogni tenant
                                RunAsWork<Object> tenantWork = new RunAsWork<Object>() {
                                    public Object doWork() throws Exception {
                                        // MB: Per ogni model del tenant
                                        List<RepoModelDefinition> tenantModels = repoAdminService.getModels();
                                        for (final RepoModelDefinition model : tenantModels) {

                                            // MB: Processo i content model, in entry catch
                                            final RetryingTransactionCallback<Object> processTenantModelCallback = new RetryingTransactionCallback<Object>() {
                                                public Object execute() throws Throwable {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug("[CustomModelActivationJob::execute] tenant current model: "+model.toString());
                                                    }
                                                    processModel(model);
                                                    return null;
                                                }
                                            };

                                            // MB:
                                            // E' stato segnalato un problema di reploy dei singoli custom model
                                            // Il caso e' quello di custom model a cascata, dipendenti l'uno dall'altro
                                            // In quel caso, se non sono stati deployate le dipendenze c'e' un errore di
                                            // org.alfresco.service.cmr.dictionary.DictionaryException: Failed to compile model acta-arc:model]
                                            // In questo modo, con dei tentativi di deploy dei singoli custom model, dovremmo riuscire a fare un deploy
                                            // incrementale
                                            try {
                                                 transactionService.getRetryingTransactionHelper().doInTransaction(processTenantModelCallback, false);
                                            } catch(org.alfresco.service.cmr.dictionary.DictionaryException de) {
                                               logger.error("[CustomModelActivationJob::execute] ERROR", de);
                                            }
                                        }
                                        return null;
                                    }
                                };

                                AuthenticationUtil.runAs(tenantWork, TenantService.ADMIN_BASENAME+TenantService.SEPARATOR+tenant.getTenantDomain());
                            }
                        } catch(org.alfresco.service.cmr.repository.InvalidStoreRefException isre) {
                            logger.error("[CustomModelActivationJob::execute] invalid store su tenant '"+tenant.getTenantDomain()+"', repository: "+RepositoryManager.getCurrentRepository());
                            //MB: Evitiamo di dare lo stack. E' un problema che si evidenzia in caso di TENANT Rotto
                            //logger.error("[CustomModelActivationJob::execute] ERROR", isre);
                        } catch(Exception e) {
                            logger.error("[CustomModelActivationJob::execute] Exception su tenant '"+tenant.getTenantDomain()+"', repository: "+RepositoryManager.getCurrentRepository());
                            //MB: L'errore lo passo solo in DEBUG. Altrimenti su tenant rotti abbiamo tonnellate di stack trace
                            logger.debug("[CustomModelActivationJob::execute] ERROR", e);
                        }
                    }
                }
			}
		} catch(Exception e) {
			logger.error("[CustomModelActivationJob::execute] ERROR", e);
			throw new JobExecutionException(e);
		} finally {
			running = false;
			//RepositoryManager.setCurrentRepository(originalRepoId);
			logger.debug("[CustomModelActivationJob::execute] END ");
		}
	}

	private void processModel(final RepoModelDefinition model) throws Exception {
		logger.debug("[CustomModelActivationJob::processModel] BEGIN");
		try {
            if (logger.isDebugEnabled()) {
    			logger.debug("[CustomModelActivationJob::processModel] processing model: "+model.getRepoName());
	    		logger.debug("[CustomModelActivationJob::processModel] current model: "+model.toString());
            }

			String modelFileName = model.getRepoName();
	        StoreRef storeRef = repoModelsLocation.getStoreRef();

            // MB: Provo a prendere il root note. Nei Tenant in costruzione non e' ancora creato
	        NodeRef rootNode = null;
            try {
                rootNode = nodeService.getRootNode(storeRef);
            } catch(Exception e) {
                logger.error("[CustomModelActivationJob::processModel] root node non trovato " +storeRef);
            }

            // MB: controllo l'esistenza del root node
            if( rootNode!=null ){
                List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoModelsLocation.getPath()+"//.[@cm:name='"+modelFileName+"' and subtypeOf('cm:dictionaryModel')]", null, namespaceService, false);

                // Verifico quanti model vengono trovati
                if (nodeRefs.size() == 0) {
                    logger.error("[CustomModelActivationJob::processModel] Could not find custom model " + modelFileName);
                } else if (nodeRefs.size() > 1) {
                    // unexpected: should not find multiple nodes with same name
                    logger.error("[CustomModelActivationJob::processModel] Found multiple custom models " + modelFileName);
                } else {
                    NodeRef modelNodeRef = nodeRefs.get(0);

                    boolean isActive = false;
                    Boolean value = (Boolean)nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE);
                    if (value != null)
                    {
                        isActive = value.booleanValue();
                    }
                    // Note: dictionaryModelType.onContentUpdate() generates a refresh event on
                    //       DictionaryDAO in order to load models.
                    if (model.getModel() == null && isActive) {
                        logger.debug("[CustomModelActivationJob::processModel] model "+modelFileName+" is not active: activating...");
                        dictionaryModelType.onContentUpdate(modelNodeRef, true);
                        logger.debug("[CustomModelActivationJob::processModel] model "+modelFileName+" activated");
                    } else if (model.getModel() != null && !isActive) {
                        logger.debug("[CustomModelActivationJob::processModel] model "+modelFileName+" is active: deactivating...");
                        dictionaryModelType.onContentUpdate(modelNodeRef, true);
                        logger.debug("[CustomModelActivationJob::processModel] model "+modelFileName+" deactivated");
                    }
                }
	        }
		} catch(Exception e) {
			logger.error("[CustomModelActivationJob::processModel] ERROR", e);
		} finally {
			logger.debug("[CustomModelActivationJob::processModel] END");
		}
	}

    private boolean isTenantJobRunning(){
        // Fermo l'IndexTransactionTracker se e' attivo il job di creazione dei tenant
        boolean isTenantBootstrapRunning = false;

        // Fermo l'IndexTransactionTracker se e' attivo il job di delete dei tenant
        boolean isTenantDeleteRunning = false;
        try {

            try {
                isTenantBootstrapRunning = jobManager.isExecuting(EcmEngineConstants.ECMENGINE_TENANT_ADMIN_JOB_REF);
            } catch (Exception e) {
                // Riportiamo semplicemente un warning
                logger.warn("[CustomModelActivationJob::execute] Exception accessing Job DAO on repository (" +RepositoryManager.getCurrentRepository() +")", e);
            }
            if (isTenantBootstrapRunning) {
                // ECM Engine sta eseguendo il bootstrap di un nuovo tenant. Sospendiamo momentaneamente l'index tracking.
                logger.info("[CustomModelActivationJob::execute] Tenant bootstrap running on repository (" +RepositoryManager.getCurrentRepository() +") skipping CustomModelActivationJob.");
            }

            try {
                isTenantDeleteRunning = jobManager.isExecuting(EcmEngineConstants.ECMENGINE_TENANT_DELETE_JOB_REF);
            } catch (Exception e) {
                // Riportiamo semplicemente un warning
                logger.warn("[CustomModelActivationJob::execute] Exception accessing Job DAO on repository (" +RepositoryManager.getCurrentRepository() +")", e);
            }
            if (isTenantDeleteRunning) {
                // ECM Engine sta eseguendo il bootstrap di un nuovo tenant. Sospendiamo momentaneamente l'index tracking.
                logger.info("[CustomModelActivationJob::execute] Tenant delete running on repository (" +RepositoryManager.getCurrentRepository() +") skipping CustomModelActivationJob.");
            }
        } catch (Exception e) {
            // Riportiamo semplicemente un warning
            logger.warn("[CustomModelActivationJob::execute] Exception on repository (" +RepositoryManager.getCurrentRepository() +")", e);
        }

        return (isTenantBootstrapRunning || isTenantDeleteRunning);
    }
}

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

package it.doqui.index.ecmengine.business.job.tenant;

import java.util.List;

import it.doqui.index.ecmengine.business.job.JobBusinessInterface;
import it.doqui.index.ecmengine.business.job.dto.BatchJob;
import it.doqui.index.ecmengine.business.job.dto.BatchJobParam;
import it.doqui.index.ecmengine.business.job.util.EncryptionHelper;
import it.doqui.index.ecmengine.business.job.util.JobStatus;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;
import it.doqui.index.ecmengine.dto.backoffice.Tenant;


import static it.doqui.index.ecmengine.util.EcmEngineConstants.*;
import static it.doqui.index.ecmengine.business.job.tenant.TenantAdminJobConstants.*;

import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.MultiTTenantAdminService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;




/**
 * Classe che implementa il job per la creazione di tenant. L'esecuzione del job
 * avviene in maniera sincronizzata, evitando l'esecuzione in concorrenza nel
 * caso di schedulazioni sovrapposte o nel caso in cui l'esecuzione venga
 * avviata prima del termine di una esecuzione precedente. Questa classe include
 * anche alcuni metodi di utilit&agrave; per la gestione delle istanze di
 * {@code BatchJob} relative a questo tipo di job.
 *
 * @author DoQui
 *
 */
public class TenantAdminJob implements Job {

	private static Log logger = LogFactory.getLog(ECMENGINE_JOB_TENANT_LOG_CATEGORY);
	private static boolean running = false;

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("[TenantAdminJob::execute] BEGIN");
		if (!running) {
			synchronized (this) {
				if (!running) {
					running = true;
				} else {
					logger.debug("[TenantAdminJob::execute] job already running");
					logger.debug("[TenantAdminJob::execute] END");
					return;
				}
			}
		} else {
			logger.debug("[TenantAdminJob::execute] job already running");
			logger.debug("[TenantAdminJob::execute] END");
			return;
		}

		JobBusinessInterface jobManager = null;
		BatchJob batchJob = null;
		String status = null;
		String message = null;
		try {
			jobManager = (JobBusinessInterface)context.getJobDetail().getJobDataMap().get(ECMENGINE_JOB_MANAGER_BEAN);
			MultiTTenantAdminService tenantAdminService = (MultiTTenantAdminService)context.getJobDetail().getJobDataMap().get(ECMENGINE_TENANT_ADMIN_SERVICE_BEAN);
			List<Repository> repositories = RepositoryManager.getInstance().getRepositories();
			for (Repository repository : repositories) {
				logger.debug("[TenantAdminJob::execute] creating tenants on repository '"+repository.getId()+"'");
				RepositoryManager.setCurrentRepository(repository.getId());
                // TODO: MB: verificare se serve impostarsi come utente di sistema e fare un lock in write del repository .. forse utente di sistema si e lock no
                batchJob = jobManager.getNextJob(ECMENGINE_TENANT_ADMIN_JOB_REF);
                if (batchJob != null) {
                    while (batchJob != null) {
                        try {
                            String domain               = batchJob.getParam(PARAM_DOMAIN).getValue();
                            String adminPassword        = EncryptionHelper.decrypt(batchJob.getParam(PARAM_ADMIN_PASSWORD).getValue());
                            String contentRootLocation  = batchJob.getParam(PARAM_CONTENT_ROOT_LOCATION) != null ? batchJob.getParam(PARAM_CONTENT_ROOT_LOCATION).getValue() : null;

                            // MB: Leggo i contentStore

                            List<ContentStoreDefinition> lcs = new ArrayList<ContentStoreDefinition>();
                            int nStore = 0;
                            while( true ){
                                BatchJobParam tjp = batchJob.getParam(PARAM_CONTENT_STORE_TYPE    +nStore);
                                if( tjp==null ) break;

                                BatchJobParam pjp = batchJob.getParam(PARAM_CONTENT_STORE_PROTOCOL+nStore);
                                if( pjp==null ) break;

                                BatchJobParam rjp = batchJob.getParam(PARAM_CONTENT_STORE_RESOURCE+nStore);
                                if( rjp==null ) break;

                                ContentStoreDefinition cs = new ContentStoreDefinition();
                                					   cs.setType(     tjp.getValue() );
                                					   cs.setProtocol( pjp.getValue() );
                                					   cs.setResource( rjp.getValue() );

                                lcs.add( cs );
                                logger.debug("[TenantAdminJob::execute] contentStore " +cs.getType() +":" +cs.getProtocol() +"->" +cs.getResource() );
                                nStore++;
                            }

                            //MB: ora prendo il percorso impostato per questo tenant
                            //    e verifico che non sia utilizzato all'interno di un repository diverso quello nel quale e' stato
                            //    chiesto di creare il tenant: il senso e' quello di non usare lo stesso path su reposytory diversi.
                            //
                            //TODO: verificare la consistenza anche relativamente alle contentStoreDefinition
                            //      occorre verificare che tutte le csd del tenant da creare, non vadano a sovrapporsi con le csd
                            //      di un qualsiasi altro repository/tenant
                            // MASTER: EcmEngineBackofficeBean.java
                            String cRepID = RepositoryManager.getCurrentRepository();
                            try {
                                // Creo il path nel quale andranno i dati
                                String cPath = contentRootLocation;//tenant.getRootContentStoreDir();

                                // Se e' presente un percorso, provo a vedere se esiste un tenant, in un altro
                                // repository, con lo stesso path
                                if( cPath!=null && cPath.length()>0 ) {
                                   //List<it.doqui.index.ecmengine.business.personalization.multirepository.Repository> repositories = RepositoryManager.getInstance().getRepositories();
                                   for (it.doqui.index.ecmengine.business.personalization.multirepository.Repository repositorySub : repositories) {
                                       logger.debug("[TenantAdminJob::createTenant] check tenants on repository '"+repositorySub.getId()+"'");

                                       // Salto il repository corrente
                                       if( !cRepID.equals(repositorySub.getId()) ){
                                           // Imposto il nuovo repository
                                           RepositoryManager.setCurrentRepository(repositorySub.getId());

                                           // Mi loggo come utente di sistema
                                           AuthenticationUtil.setSystemUserAsCurrentUser();

                                           // Prendo i tenant
                                           List<org.alfresco.repo.tenant.Tenant> tenantList = tenantAdminService.getAllTenants();
                                           for (org.alfresco.repo.tenant.Tenant tenantTarget : tenantList) {
                                               // Prendo il path nel quale andranno i dati
                                               String cPathTarget = tenantTarget.getRootContentStoreDir();
                                               if( cPathTarget!=null && cPathTarget.length()>0 ) {
                                                  if( cPath.equals(cPathTarget) ){
                                                     throw new Exception("Utilizzo illegale del path (" +cPath +") in quanto gia' utilizzato dal tenant (" +tenantTarget.getTenantDomain() +") del repository (" +repositorySub.getId() +")");
                                                  }
                                               }
                                           }
                                       }
                                   }
                                }
                            //} catch (InvalidParameterException ipe) {
                                // Rimbalzo l'eccezione
                                //throw ipe;
                            } catch (Exception e) {
                                // Qualsiasi errore genera la creazione di un invalidParameter
                                throw e; //new InvalidParameterException("Impossibile determinare l'univocita' del path del tenant");
                            } finally {
                                RepositoryManager.setCurrentRepository(cRepID);
                            }
                            //--------------------------------------------------------------------------------------------------


                            if(logger.isDebugEnabled()) {
                                logger.debug("[TenantAdminJob::execute] creating tenant '"+domain+"' on repository '"+RepositoryManager.getCurrentRepository()+"', root location: "+contentRootLocation);
                            }

                            tenantAdminService.createTenant(domain, adminPassword.toCharArray(), contentRootLocation, lcs);
                            logger.debug("[TenantAdminJob::execute] tenant '"+domain+"' created successfully");
                            status  = JobStatus.FINISHED;
                            message = "";
                        } catch(Exception e) {
                            logger.error("[TenantAdminJob::execute] ERROR", e);
                            status  = JobStatus.ERROR;
                            message = e.getMessage();
                        } finally {
                            try {
                                batchJob.setStatus(status);
                                batchJob.setMessage(message);
                                jobManager.updateJob(batchJob);
                            } catch(Exception e) {
                                logger.warn("[TenantAdminJob::execute] error updating job status", e);
                            }
                        }
                        batchJob = jobManager.getNextJob(ECMENGINE_TENANT_ADMIN_JOB_REF);
                    }
                } else {
                    logger.debug("[TenantAdminJob::execute] no tenant jobs found");
                }
			}
		} catch(Exception e) {
			logger.error("[TenantAdminJob::execute] ERROR", e);
			throw new JobExecutionException(e);
		} finally {
			running = false;
			logger.debug("[TenantAdminJob::execute] END");
		}
	}

	/**
	 * Metodo statico di utilit&agrave; per la creazione del job da fornire come
	 * parametro al job manager.
	 *
	 * @param tenant
	 *            L'istanza di {@code Tenant} contenente i dati del tenant.
	 * @return L'istanza di {@code BatchJob} contenente i dati necessati al job
	 *         di creazione del tenant.
	 * @throws Exception
	 * @see {@link it.doqui.index.ecmengine.business.job.JobBusinessInterface}
	 */
	public static BatchJob createBatchJob(Tenant tenant) throws Exception {
        // TODO: controllo di non esistenza del tenant
        // TODO: controllo che non ci sia un JOB in coda, per la creazione di questo tenant
		BatchJob job = new BatchJob(ECMENGINE_TENANT_ADMIN_JOB_REF);
		job.addParam(new BatchJobParam(TenantAdminJobConstants.PARAM_DOMAIN                 , tenant.getDomain())                                   );
		job.addParam(new BatchJobParam(TenantAdminJobConstants.PARAM_ADMIN_PASSWORD         , EncryptionHelper.encrypt(tenant.getAdminPassword()))  );
		job.addParam(new BatchJobParam(TenantAdminJobConstants.PARAM_CONTENT_ROOT_LOCATION  , tenant.getRootContentStoreDir())                      );

        // MB: aggiungo la lista dei bean di content store
    	logger.debug("[TenantAdminJob::createBatchJob] tenant.getContentStores");
        if( tenant.getContentStores()!=null ){
           int nStore = 0;
           for( Object cs: tenant.getContentStores() ){
        	   it.doqui.index.ecmengine.dto.backoffice.ContentStoreDefinition tcs = (it.doqui.index.ecmengine.dto.backoffice.ContentStoreDefinition)cs;

               logger.debug("[TenantAdminJob::createBatchJob] contentStore (" +nStore +") " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource() );

               job.addParam(new BatchJobParam(TenantAdminJobConstants.PARAM_CONTENT_STORE_TYPE    +nStore, tcs.getType()     ) );
         	   job.addParam(new BatchJobParam(TenantAdminJobConstants.PARAM_CONTENT_STORE_PROTOCOL+nStore, tcs.getProtocol() ) );
         	   job.addParam(new BatchJobParam(TenantAdminJobConstants.PARAM_CONTENT_STORE_RESOURCE+nStore, tcs.getResource() ) );

               nStore++;
           }
        }

		return job;
	}

}

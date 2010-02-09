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
import it.doqui.index.ecmengine.business.job.util.JobStatus;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.dto.backoffice.Tenant;


import static it.doqui.index.ecmengine.util.EcmEngineConstants.*;
import static it.doqui.index.ecmengine.business.job.tenant.TenantDeleteJobConstants.*;

import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.MultiTTenantAdminService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;





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
public class TenantDeleteJob implements Job {

    private static Log logger = LogFactory.getLog(ECMENGINE_JOB_TENANT_LOG_CATEGORY);
    private static boolean running = false;

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("[TenantDeleteJob::execute] BEGIN");
        if (!running) {
            synchronized (this) {
                if (!running) {
                    running = true;
                } else {
                    logger.debug("[TenantDeleteJob::execute] job already running");
                    logger.debug("[TenantDeleteJob::execute] END");
                    return;
                }
            }
        } else {
            logger.debug("[TenantDeleteJob::execute] job already running");
            logger.debug("[TenantDeleteJob::execute] END");
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
                logger.debug("[TenantDeleteJob::execute] delete tenants on repository '"+repository.getId()+"'");
                RepositoryManager.setCurrentRepository(repository.getId());
                // TODO: MB: verificare se serve impostarsi come utente di sistema e fare un lock in write del repository .. forse utente di sistema si e lock no
                batchJob = jobManager.getNextJob(ECMENGINE_TENANT_DELETE_JOB_REF);
                if (batchJob != null) {
                    while (batchJob != null) {
                        try {
                            String domain = batchJob.getParam(PARAM_DOMAIN).getValue();
                            tenantAdminService.deleteTenant(domain);
                            logger.debug("[TenantDeleteJob::execute] tenant '"+domain+"' deleted successfully");
                            status  = JobStatus.FINISHED;
                            message = "";

                        } catch(Exception e) {
                            logger.error("[TenantDeleteJob::execute] ERROR", e);
                            status  = JobStatus.ERROR;
                            message = e.getMessage();
                        } finally {
                            try {
                                batchJob.setStatus(status);
                                batchJob.setMessage(message);
                                jobManager.updateJob(batchJob);
                            } catch(Exception e) {
                                logger.warn("[TenantDeleteJob::execute] error updating job status", e);
                            }
                        }
                        batchJob = jobManager.getNextJob(ECMENGINE_TENANT_DELETE_JOB_REF);
                    }
                } else {
                    logger.debug("[TenantDeleteJob::execute] no tenant jobs found");
                }
            }
        } catch(Exception e) {
            logger.error("[TenantDeleteJob::execute] ERROR", e);
            throw new JobExecutionException(e);
        } finally {
            running = false;
            logger.debug("[TenantDeleteJob::execute] END");
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
        // TODO: controllo di esistenza del tenant
        // TODO: controllo che non ci sia un JOB in coda, per la delete di questo tenant
        BatchJob job = new BatchJob(ECMENGINE_TENANT_DELETE_JOB_REF);
        job.addParam(new BatchJobParam(TenantDeleteJobConstants.PARAM_DOMAIN, tenant.getDomain()));
        return job;
    }

}

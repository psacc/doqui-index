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

package it.doqui.index.ecmengine.business.job;

import java.util.Date;

import it.doqui.index.ecmengine.business.job.dto.BatchJob;
import it.doqui.index.ecmengine.business.job.dto.BatchJobParam;
import it.doqui.index.ecmengine.business.job.util.JobDtoHelper;
import it.doqui.index.ecmengine.business.job.util.JobStatus;
import it.doqui.index.ecmengine.integration.job.dao.JobDAO;
import it.doqui.index.ecmengine.integration.job.vo.JobParamVO;
import it.doqui.index.ecmengine.integration.job.vo.JobVO;

import static it.doqui.index.ecmengine.util.EcmEngineConstants.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che implementa i metodi della interface {@code JobBusinessInterface}.
 *
 * @author DoQui
 * @see JobBusinessInterface
 *
 */
public class JobManager implements JobBusinessInterface {

	private static Log logger = LogFactory.getLog(ECMENGINE_DAO_LOG_CATEGORY);

	private static int MAX_MSG_LENGTH = 128;

	private JobDAO jobDAO;

	public JobManager() {}

	public void setJobDAO(JobDAO jobDAO) {
		this.jobDAO = jobDAO;
	}

	public void createJob(BatchJob job) throws Exception {
		logger.debug("[JobManager::createJob] BEGIN");
		int jobId=-1;
		try {
			Date now = new Date();
			JobVO jobVO = new JobVO();
			jobVO.setStatus(JobStatus.READY);
			jobVO.setRef(job.getRef());
			jobVO.setTimestampCreazione(now);
			jobVO.setLastUpdate(now);
			final String message = (job.getMessage() != null && job.getMessage().length() > MAX_MSG_LENGTH) ? job.getMessage().substring(0, MAX_MSG_LENGTH) : job.getMessage();
			jobVO.setMessage(message);
			jobId = jobDAO.insertJob(jobVO);
			if (job.getParams() != null && job.getParams().size() > 0) {
				for (BatchJobParam param : job.getParams()) {
					JobParamVO jobParamVO = JobDtoHelper.getJobParamVO(param);
					jobParamVO.setJobId(jobId);
					jobDAO.insertJobParam(jobParamVO);
				}
			}
		} catch(Exception e) {			
			logger.error("[JobManager::createJob] ERROR", e);
			throw new RuntimeException(e);
		} finally {
			logger.debug("[JobManager::createJob] END");
		}
	}

	public BatchJob getNextJob(String jobRef) throws Exception {
		logger.debug("[JobManager::getNextJob] BEGIN");
		// TODO
		// Valutare possibili estensioni della gestione della coda dei job.
		// Per esempio si potrebbe aggiungere la gestione di un campo 'priority' ...
		BatchJob result = null;
		try {
			JobVO[] jobList = jobDAO.getJobsByStatus(jobRef, JobStatus.READY);
			if (jobList != null && jobList.length > 0) {
				jobList[0].setStatus(JobStatus.RUNNING);
				jobDAO.updateJob(jobList[0]);
				result = JobDtoHelper.getBatchJob(jobList[0]);
				JobParamVO[] jobParamVOs = jobDAO.getJobParamsByJob(jobList[0]);
				for (int i=0; i<jobParamVOs.length; i++) {
					result.addParam(JobDtoHelper.getBatchJobParam(jobParamVOs[i]));
				}
			}
		} catch(Exception e) {
			logger.error("[JobManager::getNextJob] ERROR", e);
			throw new RuntimeException(e);
		} finally {
			logger.debug("[JobManager::getNextJob] END");
		}
		return result;
	}

	public void updateJob(BatchJob job) throws Exception {
		logger.debug("[JobManager::updateJob] BEGIN");
		try {
			JobVO jobVO = JobDtoHelper.getJobVO(job);
			Date now = new Date();
			jobVO.setLastUpdate(now);
			final String message = (job.getMessage() != null && job.getMessage().length() > MAX_MSG_LENGTH) ? job.getMessage().substring(0, MAX_MSG_LENGTH) : job.getMessage();
			jobVO.setMessage(message);
			jobDAO.updateJob(jobVO);
		} catch(Exception e) {
			logger.error("[JobManager::updateJob] ERROR", e);
			throw new RuntimeException(e);
		} finally {
			logger.debug("[JobManager::updateJob] END");
		}
	}

	public boolean isExecuting(String jobRef) throws Exception {
		logger.debug("[JobManager::isExecuting] BEGIN");
		try {
			JobVO [] jobList = jobDAO.getJobsByStatus(jobRef, JobStatus.RUNNING);
			return (jobList != null && jobList.length > 0);
		} catch (Exception e) {
			logger.error("[JobManager::isExecuting] ERROR");
			throw new RuntimeException(e);
		} finally {
			logger.debug("[JobManager::isExecuting] END");
		}
	}

	public BatchJob[] getJobsByExecutor(String jobRef) throws Exception {
		logger.debug("[JobManager::getJobsByExecutor] BEGIN");
		BatchJob results[] = null;
		try {
            // Creo il vettore di ritorno
            results = getJobsByStatus( jobRef, null );
		} catch(Exception e) {
			logger.error("[JobManager::getJobsByExecutor] ERROR", e);
			throw new RuntimeException(e);
		} finally {
			logger.debug("[JobManager::getJobsByExecutor] END");
		}
		return results;
	}

	public BatchJob[] getJobsByStatus(String jobRef, String status) throws Exception {
		logger.debug("[JobManager::getJobsByStatus] BEGIN");
		BatchJob results[] = null;
		try {
            // Prendo tutti i job legati a un certo executor
			JobVO[] jobList = null;
            // In base al fatto che ho, o non ho lo status, uso un metodo al posto di un altro
            if( status==null ){
			    jobList = jobDAO.getJobsByExecutor(jobRef);
            } else {
			    jobList = jobDAO.getJobsByStatus(jobRef,status);
            }

            // Se ci sono degli elementi
			if (jobList != null && jobList.length > 0) {

                // Creo il vettore di ritorno
                results = new BatchJob[jobList.length];
                int n = 0;

                // Per ogni BatchJob
                for( JobVO jv : jobList ){

                   // Prendo valori e parametri
                   results[n] = JobDtoHelper.getBatchJob(jv);
                   JobParamVO[] jobParamVOs = jobDAO.getJobParamsByJob(jv);
                   for (int i=0; i<jobParamVOs.length; i++) {
                       results[n].addParam(JobDtoHelper.getBatchJobParam(jobParamVOs[i]));
                   }
                   n++;
				}
			}
		} catch(Exception e) {
			logger.error("[JobManager::getJobsByStatus] ERROR", e);
			throw new RuntimeException(e);
		} finally {
			logger.debug("[JobManager::getJobsByStatus] END");
		}
		return results;
	}

}

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

package it.doqui.index.ecmengine.business.foundation.job;

import org.alfresco.service.namespace.QName;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.job.JobBusinessInterface;
import it.doqui.index.ecmengine.business.job.dto.BatchJob;
import it.doqui.index.ecmengine.exception.repository.JobRuntimeException;

public class JobSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -8337349714051012405L;

	public void createJob(BatchJob job) throws Exception {
		logger.debug("[JobSvcBean::createJob] BEGIN");
		try {
			getJobManager().createJob(job);
		} catch (Exception e) {
			logger.error("[JobSvcBean::createJob] Exception : "+e.getMessage());
			handleJobServiceException("createJob", e);
		} finally {
			logger.debug("[JobSvcBean::createJob] END");
		}
	}

	public BatchJob getNextJob(String jobRef) throws Exception {
		logger.debug("[JobSvcBean::getNextJob] BEGIN");
		BatchJob result = null;
		try {
			result = getJobManager().getNextJob(jobRef);
		} catch (Exception e) {
			handleJobServiceException("getNextJob", e);
		} finally {
			logger.debug("[JobSvcBean::getNextJob] END");
		}
		return result;
	}

	public void updateJob(BatchJob job) throws Exception {
		logger.debug("[JobSvcBean::updateJob] BEGIN");
		try {
			getJobManager().updateJob(job);
		} catch (Exception e) {
			handleJobServiceException("updateJob", e);
		} finally {
			logger.debug("[JobSvcBean::updateJob] END");
		}
	}

	public boolean isExecuting(String jobRef) throws Exception {
		logger.debug("[JobSvcBean::isExecuting] BEGIN");
		boolean result = false;
		try {
			result = getJobManager().isExecuting(jobRef);
		} catch (Exception e) {
			handleJobServiceException("isExecuting", e);
		} finally {
			logger.debug("[JobSvcBean::isExecuting] END");
		}
		return result;
	}

	public BatchJob[] getJobsByExecutor(String jobRef) throws Exception {
		logger.debug("[JobSvcBean::getJobsByExecutor] BEGIN");
		BatchJob[] result = null;
		try {
			result = getJobManager().getJobsByExecutor(jobRef);
		} catch (Exception e) {
			handleJobServiceException("getJobsByExecutor", e);
		} finally {
			logger.debug("[JobSvcBean::getJobsByExecutor] END");
		}
		return result;
	}

	public BatchJob[] getJobsByStatus(String jobRef, String status) throws Exception {
		logger.debug("[JobSvcBean::getJobsByStatus] BEGIN");
		BatchJob[] result = null;
		try {
			result = getJobManager().getJobsByStatus(jobRef,status);
		} catch (Exception e) {
			handleJobServiceException("getJobsByStatus", e);
		} finally {
			logger.debug("[JobSvcBean::getJobsByStatus] END");
		}
		return result;
	}

	private JobBusinessInterface getJobManager() {
		return (JobBusinessInterface)serviceRegistry.getService(QName.createQName(ECMENGINE_JOB_MANAGER_BEAN));
	}

	private void handleJobServiceException(String methodName, Throwable e) throws JobRuntimeException {
		logger.warn("[JobSvcBean::handleJobServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new JobRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new JobRuntimeException(FoundationErrorCodes.GENERIC_JOB_SERVICE_ERROR);
		}
	}

}

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
 
package it.doqui.index.ecmengine.business.job.util;

import it.doqui.index.ecmengine.business.job.dto.BatchJob;
import it.doqui.index.ecmengine.business.job.dto.BatchJobParam;
import it.doqui.index.ecmengine.integration.job.vo.JobParamVO;
import it.doqui.index.ecmengine.integration.job.vo.JobVO;

/**
 * Classe di utilit&agrave; per la traduzione da oggetti DTO a VO e viceversa.
 * 
 * @author DoQui
 * 
 */
public abstract class JobDtoHelper {

	public static BatchJob getBatchJob(JobVO jobVO) {
		if (jobVO == null) {
			return null;
		}
		BatchJob job = new BatchJob();
		job.setId(jobVO.getId());
		job.setRef(jobVO.getRef());
		job.setStatus(jobVO.getStatus());
		job.setMessage(jobVO.getMessage());
		job.setTimestampCreazione(jobVO.getTimestampCreazione());
		job.setLastUpdate(jobVO.getLastUpdate());
		return job;
	}

	public static JobVO getJobVO(BatchJob job) {
		if (job == null) {
			return null;
		}
		JobVO jobVO = new JobVO();
		jobVO.setId(job.getId());
		jobVO.setRef(job.getRef());
		jobVO.setStatus(job.getStatus());
		jobVO.setMessage(job.getMessage());
		jobVO.setTimestampCreazione(job.getTimestampCreazione());
		jobVO.setLastUpdate(job.getLastUpdate());
		return jobVO;
	}

	public static BatchJobParam getBatchJobParam(JobParamVO jobParamVO) {
		if (jobParamVO == null) {
			return null;
		}
		BatchJobParam jobParam = new BatchJobParam();
		jobParam.setName(jobParamVO.getName());
		jobParam.setValue(jobParamVO.getVal());
		return jobParam;
	}

	public static JobParamVO getJobParamVO(BatchJobParam jobParam) {
		if (jobParam == null) {
			return null;
		}
		JobParamVO jobParamVO = new JobParamVO();
		jobParamVO.setName(jobParam.getName());
		jobParamVO.setVal(jobParam.getValue());
		return jobParamVO;
	}

}

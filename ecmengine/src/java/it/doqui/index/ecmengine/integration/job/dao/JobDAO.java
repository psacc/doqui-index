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

package it.doqui.index.ecmengine.integration.job.dao;

import it.doqui.index.ecmengine.integration.job.vo.JobParamVO;
import it.doqui.index.ecmengine.integration.job.vo.JobVO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO per l'interrogazione e l'inserimento di dati sulla tabella dei job sul database
 * di ECMENGINE.
 *
 * @author DoQui
 */
public class JobDAO extends HibernateDaoSupport {

	private static Log logger = LogFactory.getLog(EcmEngineConstants.ECMENGINE_DAO_LOG_CATEGORY);

	/**
	 * Inserisce una riga relativa ad un job.
	 *
	 * @param job
	 *            L'istanza di {@code JobVO} contenente i dati del job da
	 *            inserire.
	 * @return L'id generato per il job inserito.
	 *
	 * @throws Exception
	 */
	public int insertJob(JobVO job) throws Exception {
		logger.debug("[JobDAO::insertJob] BEGIN");

		int result = -1;
		Object generatedId = null;
		try {
			generatedId = getHibernateTemplate().save(job);
			//NOTA: flush inserito per gestire la sequence di oracle 
			//getHibernateTemplate().flush();
			if (generatedId instanceof Integer) {
				result = ((Integer)generatedId).intValue();
			} else if (generatedId instanceof Long) {
				result = ((Long)generatedId).intValue();
			} else if (generatedId instanceof BigInteger) {
				result = ((BigInteger)generatedId).intValue();
			}			
		} catch(Exception e) {			
			logger.error("[JobDAO::insertJob] ERROR", e);
			throw e;
		} finally {			
			logger.debug("[JobDAO::insertJob] END");
		}
		return result;
	}

	/**
	 * Inserisce una riga relativa ad un parametro di un job.
	 *
	 * @param jobParam
	 *            L'istanza di {@code JobParamVO} contenente i dati del
	 *            parametro da inserire.
	 * @throws Exception
	 */
	public void insertJobParam(JobParamVO jobParam) throws Exception {
		logger.debug("[JobDAO::insertJobParam] BEGIN");
		try {
			getHibernateTemplate().save(jobParam);
		} catch(Exception e) {
			logger.error("[JobDAO::insertJobParam] ERROR", e);
		} finally {
			logger.debug("[JobDAO::insertJobParam] END");
		}
	}

	/**
	 * Aggiorna la riga contenente i dati di un job. I campi che devono essere
	 * impostati nel VO sono:
	 * <ul>
	 * <li>id</li>
	 * <li>status</li>
	 * <li>message</li>
	 * </ul>
	 * Il campo timestampUltimoAggiornamento viene sempre valorizzato con il
	 * current time dell'operazione.
	 *
	 * @param job
	 *            Istanza di {@code JobVO} contenente i dati da aggiornare.
	 *
	 * @throws Exception
	 */
	public void updateJob(JobVO job) throws Exception {
		logger.debug("[JobDAO::updateJob] BEGIN");
		try {
			getHibernateTemplate().update(job);
		} catch(Exception e) {
			logger.error("[JobDAO::updateJob] ERROR", e);
			throw e;
		} finally {
			logger.debug("[JobDAO::updateJob] END");
		}
	}

	/**
	 * Reperisce le righe dei job filtrati per id dell'esecutore e per stato.
	 *
	 * @param jobRef
	 *            Stringa che identifica l'esecutore che ha rischiesto
	 *            l'accodamento del job.
	 * @param status
	 *            Stato dei job da reperire.
	 * @return Un array di {@code JobVO} contenente i job trovati dalla query.
	 * @throws Exception
	 *
	 * @see it.doqui.index.ecmengine.business.job.util.JobStatus
	 */
	@SuppressWarnings("unchecked")
	public JobVO[] getJobsByStatus(String jobRef, String status) throws Exception {
		logger.debug("[JobDAO::getJobsByStatus] BEGIN");
    	logger.debug("[JobDAO::getJobsByStatus] Param [" +jobRef +"][" +status +"]");
		JobVO[] result = null;
		try {
			Conjunction filters = Restrictions.conjunction();
			filters.add(Restrictions.eq("ref", jobRef));
			// Se ho passato lo status, lo aggiungo alla query
			if( status != null ){
				filters.add(Restrictions.eq("status", status));
			}
			Criteria criteria = getSession().createCriteria(JobVO.class);
			criteria.add(filters);

			// Ordino per progressivo, non e' garantita la temporarita'
			criteria.addOrder(Order.asc("id"));

			List<JobVO> queryResult = criteria.list();
			if (queryResult != null) {
				result = queryResult.toArray(new JobVO[]{});
			} else {
				result = new JobVO[0];
			}
		} catch(Exception e) {
			logger.error("[JobDAO::getJobsByStatus] ERROR", e);
			throw e;
		} finally {
            if( result!=null ){
	    		logger.debug("[JobDAO::getJobsByStatus] Result size " +result.length );
    		} else {
	    		logger.debug("[JobDAO::getJobsByStatus] Result size NULL" );
    		}
			logger.debug("[JobDAO::getJobsByStatus] Result " +result);
			logger.debug("[JobDAO::getJobsByStatus] END");
		}
		return result;
	}

	/**
	 * Reperisce le righe dei job filtrati per id dell'esecutore
	 *
	 * @param jobRef
	 *            Stringa che identifica l'esecutore che ha rischiesto
	 *            l'accodamento del job.
     *
	 * @return Un array di {@code JobVO} contenente i job trovati dalla query.
	 * @throws Exception
	 */
	public JobVO[] getJobsByExecutor(String jobRef) throws Exception {
		logger.debug("[JobDAO::getJobsByExecutor] BEGIN");
		JobVO[] result = null;
		try {
	    	result = getJobsByStatus( jobRef, null );
		} catch(Exception e) {
			logger.error("[JobDAO::getJobsByExecutor] ERROR", e);
			throw e;
		} finally {
			logger.debug("[JobDAO::getJobsByExecutor] END");
		}
		return result;
	}

	/**
	 * Reperisce i parametri relativi ad un job specifico.
	 *
	 * @param jobVO
	 *            L'istanza di {@code JobVO} relativa al job di cui reperire i
	 *            parametri.
	 * @return Un array di {@code JobParamVO} contenente i parametri.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public JobParamVO[] getJobParamsByJob(JobVO jobVO) throws Exception {
		logger.debug("[JobDAO::getJobParamsByJob] BEGIN");
		JobParamVO[] result = null;
		try {
			Conjunction filters = Restrictions.conjunction();
			filters.add(Restrictions.eq("jobId", jobVO.getId()));
			Criteria criteria = getSession().createCriteria(JobParamVO.class);
			criteria.add(filters);
			List<JobParamVO> queryResult = criteria.list();
			if (queryResult != null) {
				result = queryResult.toArray(new JobParamVO[]{});
			} else {
				result = new JobParamVO[0];
			}
		} catch(Exception e) {
			logger.error("[JobDAO::getJobParamsByJob] ERROR", e);
			throw e;
		} finally {
			logger.debug("[JobDAO::getJobParamsByJob] END");
		}
		return result;
	}

}

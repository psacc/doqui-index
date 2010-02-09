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
 
package it.doqui.index.ecmengine.integration.audittrail.dao;

import it.doqui.index.ecmengine.integration.audittrail.vo.AuditTrailVO;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *  <p>Classe DAO che permette , dopo aver ottenuto una connessione 
 *  verso la base dati dell'ECMENGINE, tramite data source definito 
 *  sull'application server, di effetuare inserimenti e interrogare 
 *  la tabella di audit trail.</p> 
 */

public class AuditTrailDAO extends HibernateDaoSupport {

	private static final int DEFAULT_MAX_ROLLING_ROWS	= 10000;

	private static final String PROP_ID			= "id";
	private static final String PROP_DATA		= "data";
	private static final String PROP_UTENTE		= "utente";
	private static final String PROP_ID_OGGETTO	= "idOggetto";
	private static final String PROP_OPERAZIONE	= "operazione";
	private static final String PROP_METADATI	= "metaDati";


	private static Log logger = LogFactory.getLog(
			it.doqui.index.ecmengine.util.EcmEngineConstants.ECMENGINE_AUDIT_TRAIL_LOG_CATEGORY);

	private int maxRollingRows;

	public AuditTrailDAO() {
		super();
		this.maxRollingRows = DEFAULT_MAX_ROLLING_ROWS;
	}

	public void setMaxRollingRows(int maxRollingRows) {
		this.maxRollingRows = maxRollingRows;
	}

	/**
	 * <p>Inserisce una riga nella tabella dell'audit trail.</p>
	 *  
	 * @param auditTrailVO Le informazioni sull'audit trail da inserire nella tabella di audit trail.
	 * 
	 * @throws AuditTrailException Se si verifica un errore in fase di inserimento o ricerca 
	 * nella tabella dell'audit trail dell'ECMENGINE.
	 */
	@SuppressWarnings("unchecked")
	public void logTrail(AuditTrailVO auditTrailVO) throws Exception {
		logger.debug("[AuditTrailDAO::logTrail] BEGIN");
		try {			
			// Rolling table implementation
			String rowCountHQL = "select count(*) from AuditTrailVO a";
			Query rowCountQuery = getSession().createQuery(rowCountHQL);
			int rowCount = ((Long)rowCountQuery.list().get(0)).intValue();
			if (rowCount < maxRollingRows) {
				getHibernateTemplate().save(auditTrailVO);
			} else {
				Criteria criteria = getSession().createCriteria(AuditTrailVO.class);
				criteria.addOrder(Order.asc(PROP_DATA));
				criteria.addOrder(Order.asc(PROP_ID));
				criteria.setMaxResults(1);
				List<AuditTrailVO> queryResult = criteria.list();
				AuditTrailVO auditTrailToUpdate = queryResult.get(0);
				auditTrailToUpdate.setData(auditTrailVO.getData());
				auditTrailToUpdate.setIdOggetto(auditTrailVO.getIdOggetto());
				auditTrailToUpdate.setMetaDati(auditTrailVO.getMetaDati());
				auditTrailToUpdate.setOperazione(auditTrailVO.getOperazione());
				auditTrailToUpdate.setUtente(auditTrailVO.getUtente());
				getHibernateTemplate().update(auditTrailToUpdate);
			}
			logger.debug("[AuditTrailDAO::logTrail] logTrail eseguito con successo");						
		} catch (Exception e) {
			logger.error("[AuditTrailDAO::logTrail] ERROR", e);
			throw e; 
		} finally {
			logger.debug("[AuditTrailDAO::logTrail] END");
		}		
	}

	/**
	 * Effettua una ricerca nella tabella di audit trail in base ai parametri specificati.
	 * 
	 * @param parametriRicerca specifica i parametri da utilizzare per effettuare la
	 * ricerca.
	 * @param dataInizio specifica la data a partire dalla quale ricercare l'audit trail.
	 * Se settato a "null", il parametro e&egrave; ignorato.
	 * @param dataFine specifica la data massima entro cui ricercare l'audit trail.
	 * Se settato a "null", il parametro e&egrave; ignorato.
	 * 
	 * @return Un array di {@link AuditInfo}.
	 * 
	 * @throws AuditTrailException Se si verifica un errore in fase di inserimento o ricerca 
	 * nella tabella dell'audit trail dell'ECMENGINE.
	 */

	@SuppressWarnings("unchecked")
	public AuditTrailVO[] ricercaAuditTrail(String utente, String idOggetto, String operazione, String metadati, Date inizioIntervallo, Date fineIntervallo) 
	throws Exception {
		logger.debug("[AuditTrailDAO::ricercaAuditTrail] BEGIN");
		AuditTrailVO[] risultato = null;		
		try {
			Conjunction filters = Restrictions.conjunction();
			if (inizioIntervallo != null) {
				filters.add(Restrictions.ge(PROP_DATA, inizioIntervallo));
			}
			if (fineIntervallo != null) {
				filters.add(Restrictions.le(PROP_DATA, fineIntervallo));
			}
			if (utente != null) { 
				filters.add(Restrictions.eq(PROP_UTENTE, utente));
			}
			if (idOggetto != null) {
				filters.add(Restrictions.eq(PROP_ID_OGGETTO, idOggetto));
			}
			if (operazione != null) {
				filters.add(Restrictions.eq(PROP_OPERAZIONE, operazione));
			}
			if (metadati != null) {
				filters.add(Restrictions.eq(PROP_METADATI, metadati));
			}

			Criteria criteria = getSession().createCriteria(AuditTrailVO.class);
			criteria.add(filters);

			// Ordinamento per data crescente
			criteria.addOrder(Order.asc(PROP_DATA));

			List<AuditTrailVO> queryResult = criteria.list();
			logger.debug("[AuditTrailDAO::ricercaAuditTrail] Query di ricerca eseguita!");

			// Ricostruzione risultato
			if (queryResult != null) {
				risultato = queryResult.toArray(new AuditTrailVO[]{});
			} else {
				risultato = new AuditTrailVO[0];
			}
		} catch (Exception e) {
			logger.error("[AuditTrailDAO::ricercaAuditTrail] ERROR", e);
			throw e;
		}finally{	
			logger.debug("[AuditTrailDAO::ricercaAuditTrail] END");
		}
		return risultato;
	}

}

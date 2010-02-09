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

package it.doqui.index.ecmengine.integration.audit.dao;

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

import it.doqui.index.ecmengine.integration.audit.vo.AuditVO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;


/**
 * DAO per l'interrogazione e l'inserimento di dati sulla tabella di audit sul database
 * di ECMENGINE.
 *
 * @author DoQui
 */
public class AuditDAO extends HibernateDaoSupport {

	private static final int DEFAULT_MAX_ROLLING_ROWS	= 10000;

	private static final String PROP_ID					= "id";
	private static final String PROP_DATAORA			= "dataOra";
	private static final String PROP_UTENTE				= "utente";
	private static final String PROP_FRUITORE			= "fruitore";
	private static final String PROP_SERVIZIO			= "servizio";
	private static final String PROP_TIPO_OGGETTO		= "tipoOggetto";
	private static final String PROP_ID_OGGETTO			= "idOggetto";
	private static final String PROP_NOME_OPERAZIONE	= "nomeOperazione";

	private static Log logger = LogFactory.getLog(EcmEngineConstants.ECMENGINE_AUDIT_LOG_CATEGORY);

	private int maxRollingRows;

	public AuditDAO() {
		super();
		this.maxRollingRows = DEFAULT_MAX_ROLLING_ROWS;
	}

	public void setMaxRollingRows(int maxRollingRows) {
		this.maxRollingRows = maxRollingRows;
	}

	/**
	 * Inserisce una riga nella tabella di audit, se il livello di trace configurato
	 * nel file di configurazione di log4j per "audit.flag" e' diverso da "OFF".
	 *
	 * @param operazione L'operazione da inserire nella tabella di audit
	 */
	@SuppressWarnings("unchecked")
	public void insert(AuditVO operazione) throws Exception {
		logger.debug("[AuditDAO::insert] BEGIN");
		try {
			// Rolling table implementation
			String rowCountHQL = "select count(*) from AuditVO a";
			Query rowCountQuery = getSession().createQuery(rowCountHQL);
			int rowCount = ((Long)rowCountQuery.list().get(0)).intValue();
			if (rowCount < maxRollingRows) {
				getHibernateTemplate().save(operazione);
			} else {
				Criteria criteria = getSession().createCriteria(AuditVO.class);
				criteria.addOrder(Order.asc(PROP_DATAORA));
				criteria.addOrder(Order.asc(PROP_ID));
				criteria.setMaxResults(1);
				List<AuditVO> queryResult = criteria.list();
				AuditVO auditToUpdate = queryResult.get(0);

				auditToUpdate.setDataOra(operazione.getDataOra());
				auditToUpdate.setFruitore(operazione.getFruitore());
				auditToUpdate.setIdOggetto(operazione.getIdOggetto());
				auditToUpdate.setNomeOperazione(operazione.getNomeOperazione());
				auditToUpdate.setServizio(operazione.getServizio());
				auditToUpdate.setTipoOggetto(operazione.getTipoOggetto());
				auditToUpdate.setUtente(operazione.getUtente());
				getHibernateTemplate().update(auditToUpdate);
			}
		} catch (Exception e) {
			logger.error("[AuditDAO::insert] ERROR", e);
			throw e;
		} finally {
			logger.debug("[AuditDAO::insert] END");
		}
	}

	/**
	 * Effettua una ricerca nella tabella di audit in base ai parametri specificati
	 * @param parametriRicerca specifica i parametri da utilizzare per effettuare la
	 * ricerca
	 * @param dataIniziale specifica la data a partire dalla quale ricercare l'audit.
	 * Se settato a "null", il parametro e&egrave; ignorato.
	 * @param dataMassima specifica la data massima entro cui ricercare l'audit.
	 * Se settato a "null", il parametro e&egrave; ignorato.
	 * @return Una lista di {@link OperazioneAudit}
	 */
	@SuppressWarnings("unchecked")
	public AuditVO[] ricercaAudit(String utente, String fruitore, String servizio, String idOggetto, String tipoOggetto, String nomeOperazione, Date inizioIntervallo, Date fineIntervallo) throws Exception {
		logger.debug("[AuditDAO::ricercaAudit] BEGIN");
		AuditVO[] risultato = null;

		try {
			Conjunction filters = Restrictions.conjunction();
			if (inizioIntervallo != null) {
				filters.add(Restrictions.ge(PROP_DATAORA, inizioIntervallo));
			}
			if (fineIntervallo != null) {
				filters.add(Restrictions.le(PROP_DATAORA, fineIntervallo));
			}
			if (utente != null) {
				filters.add(Restrictions.eq(PROP_UTENTE, utente));
			}
			if (fruitore != null) {
				filters.add(Restrictions.eq(PROP_FRUITORE, fruitore));
			}
			if (servizio != null) {
				filters.add(Restrictions.eq(PROP_SERVIZIO, servizio));
			}
			if (idOggetto != null) {
				filters.add(Restrictions.eq(PROP_ID_OGGETTO, idOggetto));
			}
			if (tipoOggetto != null) {
				filters.add(Restrictions.eq(PROP_TIPO_OGGETTO, tipoOggetto));
			}
			if (nomeOperazione != null) {
				filters.add(Restrictions.eq(PROP_NOME_OPERAZIONE, nomeOperazione));
			}

			Criteria criteria = getSession().createCriteria(AuditVO.class);
			criteria.add(filters);

			// Ordinamento per data crescente
			criteria.addOrder(Order.asc(PROP_DATAORA));

			List<AuditVO> queryResult = criteria.list();
			logger.debug("[AuditDAO::ricercaAudit] Query di ricerca eseguita!");

			// Ricostruzione risultato
			if (queryResult != null) {
				risultato = queryResult.toArray(new AuditVO[]{});
			} else {
				risultato = new AuditVO[0];
			}

            if(logger.isDebugEnabled()) {
    			logger.debug("[AuditDAO::ricercaAudit] Numero risultati ricerca su audit: " + risultato.length);
		    }
		} catch (Exception e) {
			logger.error("[AuditDAO::ricercaAudit] ERROR", e);
			throw e;
		} finally {
			logger.debug("[AuditDAO::ricercaAudit] END");
		}
		return risultato;
	}

}

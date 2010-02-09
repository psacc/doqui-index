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

package it.doqui.index.ecmengine.integration.mimetype.dao;

import it.doqui.index.ecmengine.integration.mimetype.vo.MimetypeVO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO per l'interrogazione della tabella sui MIMEtype nel database
 * di ECMENGINE.
 *
 * @author DoQui
 */
public class MimetypeDAO extends HibernateDaoSupport {

	private static Log logger = LogFactory.getLog(EcmEngineConstants.ECMENGINE_DAO_LOG_CATEGORY);

	/**
	 * Reperisce dal db i MIMEtype associati alle estensioni.
	 * @return Un array di MIMEtypeVO trovati dalla query.
	 * @throws Exception In caso di errore.
	 *
	 */
     ///*
	public MimetypeVO[] getAllMimetypes() throws Exception {
		logger.debug("[MimetypeDAO::getAllMimetypes] BEGIN");
		MimetypeVO[] result = null;
		try {
			Criteria criteria = getSession().createCriteria(MimetypeVO.class);
			criteria.addOrder(Order.asc("priority"));
			List<MimetypeVO> queryResult = criteria.list();
			if (queryResult != null) {
				result = queryResult.toArray(new MimetypeVO[]{});
			} else {
				result = new MimetypeVO[0];
			}
		} catch(Exception e) {
			logger.error("[MimetypeDAO::getAllMimetypes] ERROR", e);
			throw e;
		} finally {
            if( result!=null ){
	    		logger.debug("[MimetypeDAO::getAllMimetypes] Result size " +result.length );
    		} else {
	    		logger.debug("[MimetypeDAO::getAllMimetypes] Result size NULL" );
    		}
			logger.debug("[MimetypeDAO::getAllMimetypes] Result " +result);
			logger.debug("[MimetypeDAO::getAllMimetypes] END");
		}
		return result;
	}
}

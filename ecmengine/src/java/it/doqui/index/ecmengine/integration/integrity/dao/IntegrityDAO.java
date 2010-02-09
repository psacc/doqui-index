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

package it.doqui.index.ecmengine.integration.integrity.dao;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.integration.integrity.vo.IntegrityAssociationVO;
import it.doqui.index.ecmengine.integration.integrity.vo.IntegrityNodeVO;
import it.doqui.index.ecmengine.integration.mimetype.vo.MimetypeVO;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO per l'interrogazione della tabella sui nodi nel database
 * di ECMENGINE.
 *
 * @author DoQui
 */
public class IntegrityDAO extends HibernateDaoSupport {

	private static Log logger = LogFactory.getLog(EcmEngineConstants.ECMENGINE_DAO_LOG_CATEGORY);

	/**
	 * Reperisce dal db i nodi presenti.
	 *
	 */
     ///*
	public IntegrityNodeVO[] getAllNodes() throws Exception {
		logger.debug("[IntegrityDAO::getAllNodes] BEGIN");
		IntegrityNodeVO[] result = null;
		try {
			Criteria criteria = getSession().createCriteria(IntegrityNodeVO.class);
			List<IntegrityNodeVO> queryResult = criteria.list();
			if (queryResult != null) {
				result = queryResult.toArray(new IntegrityNodeVO[]{});
			} else {
				result = new IntegrityNodeVO[0];
			}
		} catch(Exception e) {
			logger.error("[IntegrityNodeVO::getAllNodes] ERROR", e);
			throw e;
		} finally {
            if( result!=null ){
	    		logger.debug("[IntegrityNodeVO::getAllNodes] Result size " +result.length );
    		} else {
	    		logger.debug("[IntegrityNodeVO::getAllNodes] Result size NULL" );
    		}
			logger.debug("[IntegrityNodeVO::getAllNodes] Result " +result);
			logger.debug("[IntegrityNodeVO::getAllNodes] END");
		}
		return result;
	}
	
	public IntegrityNodeVO getDBID(String uid)throws Exception{
		logger.debug("[IntegrityDAO::getDBID] BEGIN");
		IntegrityNodeVO result = null;
		List<IntegrityNodeVO> queryResult=null;
		try {
			Criteria criteria = getSession().createCriteria(IntegrityNodeVO.class);
			criteria.add(Restrictions.eq("uuid", uid));
			queryResult = criteria.list();
			if (queryResult != null) {
				result = queryResult.toArray(new IntegrityNodeVO[queryResult.size()])[0];
			}
		} catch(Exception e) {
			logger.error("[IntegrityNodeVO::getDBID] ERROR", e);
			throw e;
		} finally {
            if( queryResult!=null ){
	    		logger.debug("[IntegrityNodeVO::getDBID] Result size " +queryResult.size() );
    		} else {
	    		logger.debug("[IntegrityNodeVO::getDBID] Result size NULL" );
    		}
			logger.debug("[IntegrityNodeVO::getDBID] Result " +result);
			logger.debug("[IntegrityNodeVO::getDBID] END");
		}
		return result;		
	}
	
	public IntegrityAssociationVO[] getAllAssociations()throws Exception{
		logger.debug("[IntegrityDAO::getAllAssociations] BEGIN");
		IntegrityAssociationVO[] result = null;
		try {
			//System.out.println("Arrivo al dao");
			Criteria criteria = getSession().createCriteria(IntegrityAssociationVO.class);
		    /*ProjectionList proList = Projections.projectionList();
		    proList.add(Projections.property("parent_node_id"));
		    proList.add(Projections.property("child_node_id"));
		    proList.add(Projections.property("child_node_name"));
			criteria.setProjection(proList);*/
			List<IntegrityAssociationVO> queryResult = criteria.list();
			if (queryResult != null) {
				result = queryResult.toArray(new IntegrityAssociationVO[]{});
			} else {
				result = new IntegrityAssociationVO[0];
			}
		} catch(Exception e) {
			logger.error("[IntegrityNodeVO::getAllAssociations] ERROR", e);
			throw e;
		} finally {
            if( result!=null ){
	    		logger.debug("[IntegrityNodeVO::getAllAssociations] Result size " +result.length );
    		} else {
	    		logger.debug("[IntegrityNodeVO::getAllAssociations] Result size NULL" );
    		}
			logger.debug("[IntegrityNodeVO::getAllAssociations] Result " +result);
			logger.debug("[IntegrityNodeVO::getAllAssociations] END");
			//System.out.println("Esco dal dao");
		}
		return result;			
	}
}

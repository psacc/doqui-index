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

package it.doqui.index.ecmengine.business.personalization.integrity;

import it.doqui.index.ecmengine.integration.integrity.dao.IntegrityDAO;
import it.doqui.index.ecmengine.integration.integrity.vo.IntegrityAssociationVO;
import it.doqui.index.ecmengine.integration.integrity.vo.IntegrityNodeVO;
import it.doqui.index.ecmengine.integration.mimetype.dao.MimetypeDAO;
import it.doqui.index.ecmengine.integration.mimetype.vo.MimetypeVO;

import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.integrity.IntegrityBusinessInterface;
import it.doqui.index.ecmengine.business.mimetype.MimetypeBusinessInterface;
//import it.doqui.index.ecmengine.business.mimetype.dto.Mimetype;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.exception.repository.EcmEngineIntegrityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IntegrityManager implements IntegrityBusinessInterface
{
    
    private IntegrityDAO integrityDAO;

    public void setIntegrityDAO(IntegrityDAO integrityDAO){
    	this.integrityDAO = integrityDAO;
    }
    
    public Map<String,Node> getAllNodes()throws EcmEngineIntegrityException{
    	Map<String,Node> result=null;
    	try{
    		IntegrityNodeVO[] resultNode=integrityDAO.getAllNodes();
    		if(resultNode!=null && resultNode.length!=0){
    			result=new LinkedHashMap<String,Node>();
    			Node temp=null;
    			for(int i=0;i<resultNode.length;i++){
    				temp=new Node();
    				temp.setUid(resultNode[i].getUuid());
    				result.put(temp.getUid(),temp);
    			}
    		}
    	}catch(Exception e){
    		throw new EcmEngineIntegrityException(FoundationErrorCodes.GENERIC_INTEGRITY_SERVICE_ERROR);
    	}
    	return result;
    }
    
    public Long getDBID(Node node)throws EcmEngineIntegrityException{
    	Long result=null;
    	try{
    		IntegrityNodeVO resultNode=integrityDAO.getDBID(node.getUid());
    		if(resultNode!=null){
    			result=resultNode.getId();
    		}
    	}catch(Exception e){
    		throw new EcmEngineIntegrityException(FoundationErrorCodes.GENERIC_INTEGRITY_SERVICE_ERROR);
    	}
    	return result;
    }
    
    public Map<Long,Set<Long>> getAllAssociations()throws EcmEngineIntegrityException{
    	Map<Long,Set<Long>> result=null;
    	try{
    		IntegrityAssociationVO[] resultAssociation=integrityDAO.getAllAssociations();
    		if(resultAssociation!=null && resultAssociation.length!=0){
    			result=new LinkedHashMap<Long,Set<Long>>();
    			for(int i=0;i<resultAssociation.length;i++){
    				if(result.get(resultAssociation[i].getParent_node_id())==null){
    					Set<Long>tempSet=new HashSet<Long>();
    					result.put(resultAssociation[i].getParent_node_id(), tempSet);
    				}
    				result.get(resultAssociation[i].getParent_node_id()).add(resultAssociation[i].getChild_node_id());
    			}
    		}
    	}catch(Exception e){
    		throw new EcmEngineIntegrityException(FoundationErrorCodes.GENERIC_INTEGRITY_SERVICE_ERROR);
    	}
    	return result;
    }

    public Map<Long,String> getAllDBIDUID()throws EcmEngineIntegrityException{
    	Map<Long,String> result=null;
    	try{
    		//System.out.println("Arrivo al manager");
    		IntegrityNodeVO[] resultAssociation=integrityDAO.getAllNodes();
    		if(resultAssociation!=null && resultAssociation.length!=0){
    			result=new LinkedHashMap<Long,String>();
    			for(int i=0;i<resultAssociation.length;i++){
    				result.put(resultAssociation[i].getId(), resultAssociation[i].getUuid());
    			}
    		}
    		//System.out.println("Esco dal manager");
    	}catch(Exception e){
    		throw new EcmEngineIntegrityException(FoundationErrorCodes.GENERIC_INTEGRITY_SERVICE_ERROR);
    	}
    	return result;
    }
    
}

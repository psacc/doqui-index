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
 
package it.doqui.index.ecmengine.business.personalization.security;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class AOOServiceImpl implements AOOService, InitializingBean {

	private NodeService nodeService;
	
	private AuthenticationService authenticationService;
	
	private SimpleCache<NodeRef, String> aooAdminCache;
	
	private Log logger;
	
	public String getAooAdministrator(NodeRef nodeRef) {
		logger.debug("[AOOServiceImpl::getAooAdministrator] BEGIN");
		final long start = System.currentTimeMillis();
		
		boolean hit = true;
		String userName = aooAdminCache.get(nodeRef);
		
		try {
			if (userName == null) {
				hit = false;
				
				if (nodeService.hasAspect(nodeRef, ASPECT_AOO_ADMINISTRABLE)) {
					userName = DefaultTypeConverter.INSTANCE.convert(String.class, 
							nodeService.getProperty(nodeRef, PROP_AOO_ADMINISTRATOR));
					aooAdminCache.put(nodeRef, userName);
				}
			}
			
			logger.debug("[AOOServiceImpl::getAooAdministrator] Got AOO admin: " +
					"N: " + nodeRef + " - U: " + userName);
		} finally {
			final long stop = System.currentTimeMillis();
			logger.debug("[AOOServiceImpl::getAooAdministrator] " +
					((hit) ? "HIT" : "MISS") + " - Elapsed: " + (stop - start) + " ms");
			logger.debug("[AOOServiceImpl::getAooAdministrator] END");
		}
		
		return userName;
	}

	public boolean hasAooAdministrator(NodeRef nodeRef) {
		logger.debug("[AOOServiceImpl::hasAooAdministrator] BEGIN");
		final boolean hasAdmin = (getAooAdministrator(nodeRef) != null);
		logger.debug("[AOOServiceImpl::hasAooAdministrator] END");
		
		return hasAdmin;
	}

	public void setAooAdministrator(NodeRef nodeRef, String userName) {
		logger.debug("[AOOServiceImpl::setAooAdministrator] BEGIN");
		
		if (!nodeService.hasAspect(nodeRef, ASPECT_AOO_ADMINISTRABLE)) {
			
			HashMap<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
			props.put(PROP_AOO_ADMINISTRATOR, userName);
			nodeService.addAspect(nodeRef, ASPECT_AOO_ADMINISTRABLE, props);
		} else {
			nodeService.setProperty(nodeRef, PROP_AOO_ADMINISTRATOR, userName);
		}
		aooAdminCache.put(nodeRef, userName);
		
		logger.debug("[AOOServiceImpl::setAooAdministrator] END");
	}

	public void afterPropertiesSet() throws Exception {
		if (nodeService == null) {
			throw new IllegalArgumentException("nodeService must be set!");
		}
		if (authenticationService == null) {
			throw new IllegalArgumentException("authenticationService must be set!");
		}
		if (aooAdminCache == null) {
			throw new IllegalArgumentException("aooAdminCache must be set!");
		}

		logger = LogFactory.getLog(ECMENGINE_PERSONALIZATION_AOO);
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setAooAdminCache(SimpleCache<NodeRef, String> aooAdminCache) {
		this.aooAdminCache = aooAdminCache;
	}

}

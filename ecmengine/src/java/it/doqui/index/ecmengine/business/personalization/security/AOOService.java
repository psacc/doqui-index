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

import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


public interface AOOService {
	String AOO_ADMINISTRATOR_AUTHORITY = "ROLE_AOO_ADMINISTRATOR";
	
	String ECMENGINE_SYS_MODEL_URI = "http://www.doqui.it/model/ecmengine/system/1.0";
	String ECMENGINE_SYS_MODEL_PREFIX = "ecm-sys";
	
	QName ASPECT_AOO_ADMINISTRABLE = QName.createQName(ECMENGINE_SYS_MODEL_URI, "aooadministrable");
	
	QName PROP_AOO_ADMINISTRATOR = QName.createQName(ECMENGINE_SYS_MODEL_URI, "aooadministrator");
	QName PROP_AOO_ID = QName.createQName(ECMENGINE_SYS_MODEL_URI, "aooid");
	
	String ECMENGINE_PERSONALIZATION_AOO = EcmEngineConstants.ECMENGINE_ROOT_LOG_CATEGORY + 
			".business.personalization.aoo";
	
	boolean hasAooAdministrator(NodeRef nodeRef);
	
	void setAooAdministrator(NodeRef nodeRef, String userName);
	
	String getAooAdministrator(NodeRef nodeRef);
}

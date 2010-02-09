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

import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.EqualsHelper;
import org.springframework.beans.factory.InitializingBean;

public class AOOAdministratorDynamicAuthority implements DynamicAuthority,
		InitializingBean {

	private AOOService aooService;
	
	public void setAooService(AOOService aooService) {
		this.aooService = aooService;
	}

	public String getAuthority() {
		return AOOService.AOO_ADMINISTRATOR_AUTHORITY;
	}

	public boolean hasAuthority(NodeRef nodeRef, String userName) {
		return EqualsHelper.nullSafeEquals(aooService.getAooAdministrator(nodeRef), userName);
	}

	public void afterPropertiesSet() throws Exception {
		if (aooService == null) {
			throw new IllegalArgumentException("aooService must be set!");
		}
	}
}

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

package it.doqui.index.ecmengine.business.personalization.security.acl;

import it.doqui.index.ecmengine.business.personalization.security.permissions.MultipleInheritancePermissionServiceImpl;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO che implementa la verifica delle ACL sul database mediante stored procedure.
 *
 * @author DoQui
 */
public class AclCheckDao extends HibernateDaoSupport {

	/** Category per il log. */
	public static final String ECMENGINE_PERSONALIZATION_ACL_CHECK =
		EcmEngineConstants.ECMENGINE_ROOT_LOG_CATEGORY + ".business.personalization.aclcheck";

	private static final String HAS_PERMISSION_SP = "sp_has_permission";
	private static final String HAS_PERMISSION_SP_MULTI = "sp_has_permission_mi";

	private static Log logger = LogFactory.getLog(AclCheckDao.ECMENGINE_PERSONALIZATION_ACL_CHECK);

	private PermissionService permissionService;

	/**
	 * Esegue la verifica delle permission direttamente sulla base dati servendosi di una
	 * stored procedure.
	 *
	 * @param nodes Una {@code List} degli ID dei nodi da verificare.
	 * @param authorities Una {@code List} dei nomi di authority da verificare.
	 * @param permissions Una {@code List} di permission delle quali almeno una deve essere presente.
	 *
	 * @return Una {@code List} di ID di nodi sui quali almeno una delle authority specificate ha
	 * almeno una delle permission specificate.
	 */
	@SuppressWarnings("unchecked")
	public List<Long> checkHasPermissionsOnNodes(List<Long> nodes, Set<String> authorities,
			Set<PermissionReference> permissions) {
    	if (logger.isDebugEnabled()) {
		    logger.debug("[AclCheckDao::checkHasPermissionsOnNodes] BEGIN");
		}

		try {
			final List<Long> readables = new ArrayList<Long>();
			int cNodes = 0;

			while (cNodes < nodes.size()) {

				final StringBuilder nodeList = new StringBuilder();
				boolean first = true;
				int i = 0;
				while (cNodes < nodes.size()) {
					final Long node = nodes.get(cNodes);
					if (first) {
						nodeList.append(node);
						first = false;
					} else {
						if (i >= 500 || nodeList.length() + node.toString().length() > 4095 ) {
							break;
						}
						nodeList.append(',').append(node);
					}
					i++;
					cNodes++;
				}
            	if (logger.isDebugEnabled()) {
		    		logger.debug("[AclCheckDao::checkHasPermissionsOnNodes] Nodes to check: " + i);
		        }

				final StringBuilder authorityList = new StringBuilder();
				first = true;
				for (String authority : authorities) {
					if (first) {
						authorityList.append(authority);
						first = false;
					} else {
						authorityList.append(',').append(authority);
					}
				}
				final StringBuilder permissionList = new StringBuilder();
				first = true;
				for (PermissionReference permission : permissions) {
					if (first) {
						permissionList.append(permission.getName());
						first = false;
					} else {
						permissionList.append(',').append(permission.getName());
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("[AclCheckDao::checkHasPermissionsOnNodes] " +
							"Calling SP with params: " + nodeList.toString() +
							" - " + authorityList.toString() +
							" - " + permissionList.toString());
				}

				final String spName = (permissionService instanceof MultipleInheritancePermissionServiceImpl)
						? HAS_PERMISSION_SP_MULTI
						: HAS_PERMISSION_SP;

            	if (logger.isDebugEnabled()) {
    				logger.debug("[AclCheckDao::checkHasPermissionsOnNodes] Stored Procedure: " + spName);
		        }

				final Query hasPermission = getSession().getNamedQuery(spName);

				hasPermission.setString("nodes", nodeList.toString());
				hasPermission.setString("authorities", authorityList.toString());
				hasPermission.setString("permissions", permissionList.toString());

				readables.addAll(hasPermission.list());
			}

          	if (logger.isDebugEnabled()) {
    			logger.debug("[AclCheckDao::checkHasPermissionsOnNodes] Readables: " + readables.size());
		    }

			return readables;
		} finally {
          	if (logger.isDebugEnabled()) {
    			logger.debug("[AclCheckDao::checkHasPermissionsOnNodes] END");
		    }
		}
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
}

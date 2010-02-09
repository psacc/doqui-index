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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.InitializingBean;

public class ReadersServiceImpl implements ReadersService, InitializingBean {

	private static Log logger = LogFactory.getLog(ECMENGINE_PERSONALIZATION_READERS);

	private static PermissionReference readPermission;

	/** Node service to get parents. */
	private NodeService nodeService;

    /** Permission service to get ACLs. */
    private PermissionService permissionService;

    /** Authority service to read authority relations. */
    private AuthorityService authorityService;

    /** Ownable service to read node owner. */
    private OwnableService ownableService;

    /** Permission model DAO. */
    private ModelDAO permissionModelDAO;

	public void afterPropertiesSet() throws Exception {
		logger.debug("[ReadersServiceImpl::afterPropertiesSet] BEGIN");

		try {
			readPermission = permissionModelDAO.getPermissionReference(
					ContentModel.TYPE_BASE, "Read");
		} finally {
			logger.debug("[ReadersServiceImpl::afterPropertiesSet] END");
		}
	}
	
	public Set<String> getReaders(NodeRef nodeRef) {
		logger.debug("[ReadersServiceImpl::getReaders] BEGIN");
		
		try {
			if (!nodeRef.getStoreRef().getProtocol().equals("workspace") 
					&& !nodeRef.getStoreRef().getProtocol().equals("avm")) {
				logger.debug("[ReadersServiceImpl::getReaders] Ignoring node from store: " + nodeRef.getStoreRef());
				
				return Collections.<String>emptySet();
			}

			Set<String> results = new HashSet<String>();
			
			final Set<AccessPermission> nodeAcl = permissionService.getAllSetPermissions(nodeRef);

			for (AccessPermission aclEntry: nodeAcl) {
				// Il primo parametro (QName type) non e` utilizzato in Alfresco 2.1
				PermissionReference permGroup = permissionModelDAO.getPermissionReference(null, aclEntry.getPermission());
				final String authority = aclEntry.getAuthority(); 
				
				if (permissionModelDAO.getGranteePermissions(permGroup).contains(readPermission)
						&& authority != null) {
					results.add(authority);
				}
			}
			
			/* Se il nodo eredita i permessi dal padre e` necessario risalire l'albero per ottenere
			 * una lista completa dei permessi.
			 */
			while (permissionService.getInheritParentPermissions(nodeRef)) {
				final ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
				final NodeRef parentRef = parentAssocRef.getParentRef();

				if (parentRef == null) {
					// Non possiamo risalire oltre il root node dello store
					break;
				}

				final Set<AccessPermission> parentAcl = permissionService.getAllSetPermissions(parentRef);

				for (AccessPermission aclEntry: parentAcl) {
					// Il primo parametro (QName type) non e` utilizzato in Alfresco 2.1
					PermissionReference permGroup = permissionModelDAO.getPermissionReference(null, aclEntry.getPermission());
					final String authority = aclEntry.getAuthority(); 

					if (permissionModelDAO.getGranteePermissions(permGroup).contains(readPermission)
							&& authority != null) {
						results.add(authority);
					}
				}
				nodeRef = parentRef; // Risali lungo l'albero
			}
			
			// L'owner puo` sempre leggere
			final String owner = ownableService.getOwner(nodeRef);
			
			if (owner != null) {
				results.add(owner);
			}
			
			// ATTENZIONE: si suppone che l'amministratore abbia accesso, almeno in lettura, su tutto il repository
			if (authorityService instanceof AdministrableAuthorityServiceImpl) {
				Set<String> adminUsers = ((AdministrableAuthorityServiceImpl) authorityService).getAdminUsers();
				
				for (String admin : adminUsers) {
					results.add(admin);
				}
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("[ReadersServiceImpl::getReaders] Reader authorities: " + results.size());
				logger.debug("[ReadersServiceImpl::getReaders] Authorities: " + results);
			}
			return results;
		} finally {
			logger.debug("[ReadersServiceImpl::getReaders] END");
		}
	}
	
	public Set<String> getAuthoritiesForCurrentUser() {
		logger.debug("[ReadersServiceImpl::getAuthoritiesForCurrentUser] BEGIN");
		
		try {
			final String currentUser = AuthenticationUtil.getCurrentUserName();
			if (currentUser.equals(AuthenticationUtil.getSystemUserName())) {
				// L'utente system puo` vedere tutto --> predicato vuoto
				return null;
			}
			
			if (authorityService.hasAdminAuthority()) {
				return null;
			}
			
			Set<String> authorities = new LinkedHashSet<String>();
			if (currentUser.equals(AuthenticationUtil.getGuestUserName())) {
				// L'utente guest puo` vedere solo cio` che gli e` esplicitamente assegnato via ACL
				authorities.add(currentUser);
				return authorities;
			}
			authorities.add(currentUser);
			authorities.add(PermissionService.ALL_AUTHORITIES);
			authorities.addAll(authorityService.getContainingAuthorities(null, currentUser, false));
			logger.debug("[ReadersServiceImpl::getAuthoritiesForCurrentUser] Authorities: " + authorities);
			
			return authorities;
		} finally {
			logger.debug("[ReadersServiceImpl::getAuthoritiesForCurrentUser] END");
		}
	}
	
	public String getLuceneFilterPredicate() {
		logger.debug("[ReadersServiceImpl::getLuceneFilterPredicate] BEGIN");

		try {
			final String currentUser = AuthenticationUtil.getCurrentUserName();
			
			if (currentUser.equals(AuthenticationUtil.getSystemUserName())) {
				// L'utente system puo` vedere tutto --> predicato vuoto
				logger.debug("[ReadersServiceImpl::getLuceneFilterPredicate] System user can see everything!");
				return "";
			}
			
			if (currentUser.equals(AuthenticationUtil.getGuestUserName())) {
				// L'utente guest puo` vedere solo cio` che gli e` esplicitamente assegnato via ACL
				logger.debug("[ReadersServiceImpl::getLuceneFilterPredicate] Got predicate for guest user.");
				return " AND ACL_READER:" + currentUser;
			}
			
			StringBuilder filter = new StringBuilder();
			
			Set<String> authorities = authorityService.getContainingAuthorities(null, currentUser, false);
			logger.debug("[ReadersServiceImpl::getLuceneFilterPredicate] Building predicate for authorities: " + authorities);
			
			filter.append(" AND (");
			
			boolean first = true;
			for (String authority : authorities) {
				filter.append((first) ? "" : " OR ").append("ACL_READER:").append(authority);
				first = false;
			}
			filter.append(")");
			
			logger.debug("[ReadersServiceImpl::getLuceneFilterPredicate] Predicate: \"" + filter + "\"");
			return filter.toString();
		} finally {
			logger.debug("[ReadersServiceImpl::getLuceneFilterPredicate] END");
		}
	}
	
	public Query getLuceneFilterQuery() {
		logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] BEGIN");

		try {
			final String currentUser = AuthenticationUtil.getCurrentUserName();
			
			if (currentUser == null) {
				/*
				 * Nessun utente autenticato. Probabilmente l'autenticazione e` in corso: non impostiamo alcun filtro
				 * per consentire la corretta ricerca degli utenti.
				 */
				logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] Authentication still in progress!");
				return null;
			}
			
			if (currentUser.equals(AuthenticationUtil.getSystemUserName())) {
				// L'utente system puo` vedere tutto --> predicato vuoto
				logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] System user can see everything!");
				return null;
			}
			
			if (currentUser.equals(AuthenticationUtil.getGuestUserName())) {
				// L'utente guest puo` vedere solo cio` che gli e` esplicitamente assegnato via ACL
				logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] Got predicate for guest user.");
				return new TermQuery(new Term("ACL_READER", currentUser));
			}
			
			BooleanQuery query = new BooleanQuery();
			
			query.add(new TermQuery(new Term("ACL_READER", currentUser)), BooleanClause.Occur.SHOULD);
			query.add(new TermQuery(new Term("ACL_READER", PermissionService.ALL_AUTHORITIES)), BooleanClause.Occur.SHOULD);
			
			Set<String> authorities = authorityService.getContainingAuthorities(null, currentUser, false);
			logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] Building predicate for authorities: " + authorities);
			
			for (String authority : authorities) {
				query.add(new TermQuery(new Term("ACL_READER", authority)), BooleanClause.Occur.SHOULD);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] Predicate: \"" + query.toString() + "\"");
			}
			return query;
		} finally {
			logger.debug("[ReadersServiceImpl::getLuceneFilterQuery] END");
		}
	}
	
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setPermissionModelDAO(ModelDAO permissionModelDAO) {
		this.permissionModelDAO = permissionModelDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}
}

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
 
package it.doqui.index.ecmengine.business.personalization.security.permissions;

import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.impl.PermissionServiceImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor che pilota l'aggiornamento degli indici a seguito di modifiche
 * alle ACL associate ad un nodo.
 * 
 * @author Doqui
 */
public class ReindexingPermissionServiceInterceptor implements MethodInterceptor {

    private static Log logger = LogFactory.getLog(EcmEngineConstants.ECMENGINE_ROOT_LOG_CATEGORY + ".business.personalization.permissions");

    private Indexer indexer;
    private NodeDaoService nodeDaoService;
    
    /** Costruttore vuoto. */
    public ReindexingPermissionServiceInterceptor() {}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		logger.debug("[ReindexingPermissionServiceInterceptor::invoke] BEGIN");

		try {
			final Object retValue = invocation.proceed();
			
			final String methodName = invocation.getMethod().getName();
			final Object[] args = invocation.getArguments();
			
			logger.debug("[ReindexingPermissionServiceInterceptor::invoke] " +
					"Intercepting method \"" + methodName + "\": " + args.length + " arguments.");
			
			final Object target = invocation.getThis();
			PermissionServiceImpl permissionService = null;

			logger.debug("[ReindexingPermissionServiceInterceptor::invoke] Target: " + target);
			if (target instanceof PermissionServiceImpl) {
				permissionService = (PermissionServiceImpl) target;
			} else {
				throw new IllegalStateException("Wrong target!");
			}
			
			if (methodName.equals("deletePermissions")) {
				
				if (args[0] instanceof NodeRef) {
					updateIndex((NodeRef) args[0], permissionService);
				} else if (args[0] instanceof NodePermissionEntry) {
					NodeRef nodeRef = ((NodePermissionEntry) args[0]).getNodeRef();
					updateIndex(nodeRef, permissionService);
				} else if (args[0] instanceof String) {

					Map<NodeRef, Set<AccessPermission>> permissions = 
						permissionService.getAllSetPermissionsForAuthority((String) args[0]);

					for (NodeRef nodeRef : permissions.keySet()) {
						updateIndex(nodeRef, permissionService);
					}
				}
			} else if (methodName.equals("deletePermission")) {
				if (args.length == 1) {
					NodeRef nodeRef = ((PermissionEntry) args[0]).getNodeRef();
					updateIndex(nodeRef, permissionService);
				} else if (args.length == 3) {
					updateIndex((NodeRef) args[0], permissionService);
				}
			} else if (methodName.equals("clearPermission")
					|| methodName.equals("setInheritParentPermissions")) {
				updateIndex((NodeRef) args[0], permissionService);
			} else if (methodName.equals("setPermission")) {
				if (args.length == 1) {
					if (args[0] instanceof NodePermissionEntry) {
						NodeRef nodeRef = ((NodePermissionEntry) args[0]).getNodeRef();
						updateIndex(nodeRef, permissionService);
					} else if (args[0] instanceof PermissionEntry) {
						NodeRef nodeRef = ((PermissionEntry) args[0]).getNodeRef();
						updateIndex(nodeRef, permissionService);
					}
				} else if (args.length == 4) {
					updateIndex((NodeRef) args[0], permissionService);
				}
			}
			
			return retValue;
		} finally {
			logger.debug("[ReindexingPermissionServiceInterceptor::invoke] END");
		}
	}

	private void updateIndex(NodeRef nodeRef, PermissionServiceImpl permissionService) {
		logger.debug("[ReindexingPermissionServiceInterceptor::updateIndex] BEGIN");

		try {
			logger.debug("[ReindexingPermissionServiceInterceptor::updateIndex] Requesting reindex of node: " + nodeRef);
			final String protocol = nodeRef.getStoreRef().getProtocol();
			
			if (protocol.equals("workspace") || protocol.equals("archive")) {
				// XXX: workaround per forzare la ri-indicizzazione dei figli in cascata
				
				Map<Long, NodeRef> toReindex = getNodeHierarchyToReindex(
						nodeDaoService.getNodeStatus(nodeRef, false), null, permissionService);
				
				for (NodeRef ref : toReindex.values()) {
					indexer.updateChildRelationship(new ChildAssociationRef(null, null, null, ref), null);
					// indexer.updateNode(nodeRef);
				
					// Registra la transazione sul DB per l'index tracking in ambiente cluster
					nodeDaoService.recordChangeId(nodeRef);
				}
			}
		} finally {
			logger.debug("[ReindexingPermissionServiceInterceptor::updateIndex] END");
		}
	}
	
    private Map<Long, NodeRef> getNodeHierarchyToReindex(NodeStatus nodeStatus, Map<Long, NodeRef> nodesById, PermissionServiceImpl permissionService) {
    	logger.debug("[ReindexingPermissionServiceInterceptor::getNodeHierarchyToReindex] BEGIN");

		try {
			// XXX: ispirato a DbNodeServiceImpl.getNodeHierarchy()
			if (nodesById == null) {
				nodesById = new HashMap<Long, NodeRef>(23);
				// Inizio della gerarchia
				nodeDaoService.flush();
			}
			final Node node = nodeStatus.getNode();
			if (node == null) {
				// Il nodo e` stato eliminato
				return nodesById;
			}
			Long nodeId = node.getId();
			if (nodesById.containsKey(nodeId)) {
				// ID gia` aggiunto... dipendenza circolare
				logger
						.warn("Circular hierarchy found including node "
								+ nodeId);
				return nodesById;
			}
			final NodeRef nodeRef = node.getNodeRef();
			// Ottimizzabile con: nodeStatus.getKey().getGuid(); ???
			// Se il nodo non eredita ACL dal padre blocco la navigazione dell'albero
			if (!permissionService.getInheritParentPermissions(nodeRef)) {
				return nodesById;
			}
			nodesById.put(nodeId, nodeRef);
			// Ricorsione
			Collection<NodeStatus> primaryChildNodeStatuses = nodeDaoService
					.getPrimaryChildNodeStatuses(node);
			for (NodeStatus primaryChildNodeStatus : primaryChildNodeStatuses) {
				nodesById = getNodeHierarchyToReindex(primaryChildNodeStatus,
						nodesById, permissionService);
			}
			return nodesById;
		} finally {
			logger.debug("[ReindexingPermissionServiceInterceptor::getNodeHierarchyToReindex] END");
		}
    }
	
	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public void setNodeDaoService(NodeDaoService nodeDaoService) {
		this.nodeDaoService = nodeDaoService;
	}
}

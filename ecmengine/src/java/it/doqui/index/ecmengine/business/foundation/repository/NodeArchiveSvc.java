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

package it.doqui.index.ecmengine.business.foundation.repository;

import it.doqui.index.ecmengine.exception.repository.NodeArchiveRuntimeException;

import java.util.List;

import javax.ejb.EJBLocalObject;

import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

public interface NodeArchiveSvc extends EJBLocalObject {
	
	NodeRef getStoreArchiveNode(StoreRef storeRef) throws NodeArchiveRuntimeException;
	
	NodeRef getArchivedNode(NodeRef originalNodeRef) throws NodeArchiveRuntimeException;
	
	RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef) throws NodeArchiveRuntimeException;
	
	RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef, NodeRef destinationNodeRef,
            QName assocTypeQName, QName assocQName) throws NodeArchiveRuntimeException;

	List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs) throws NodeArchiveRuntimeException;
	
	List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs, NodeRef destinationNodeRef, 
			QName assocTypeQName, QName assocQName) throws NodeArchiveRuntimeException;
	 
	List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef) throws NodeArchiveRuntimeException;
	 
	List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef,
	            NodeRef destinationNodeRef, QName assocTypeQName, QName assocQName) throws NodeArchiveRuntimeException;
	 
	void purgeArchivedNode(NodeRef archivedNodeRef) throws NodeArchiveRuntimeException;
	 
	void purgeArchivedNodes(List<NodeRef> archivedNodes) throws NodeArchiveRuntimeException;
	 
	void purgeAllArchivedNodes(StoreRef originalStoreRef) throws NodeArchiveRuntimeException;
}

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
 
package it.doqui.index.ecmengine.business.personalization.splitting.index;

import it.doqui.index.ecmengine.business.personalization.splitting.SplittingNodeService;
import it.doqui.index.ecmengine.business.personalization.splitting.util.SplittingNodeServiceConstants;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Gestisce l'indicizzazione dei nodi con il supporto ai nodi "splitted".
 * 
 * @author Doqui
 */
public class SplittingNodeIndexer implements NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.OnUpdateNodePolicy,
		NodeServicePolicies.OnDeleteNodePolicy,
		NodeServicePolicies.OnCreateChildAssociationPolicy,
		NodeServicePolicies.OnDeleteChildAssociationPolicy {

	private static Log logger;
	
	private SplittingNodeService nodeService;
    private PolicyComponent policyComponent;
    private Indexer indexer;
    
    public void setSplittingNodeService(SplittingNodeService nodeService) {
    	this.nodeService = nodeService;
    }
    
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    /** Registers the policy behavior methods. */
    public void init() {
    	logger = LogFactory.getLog(SplittingNodeServiceConstants.ECMENGINE_SPLITTING_LOG_CATEGORY);
    	
    	logger.debug("[SplittingNodeIndexer::init] BEGIN");
		
		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), this,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateNode"), this,
				new JavaBehaviour(this, "onUpdateNode"));
		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), this,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), this, 
				new JavaBehaviour(this, "onCreateChildAssociation"));
		policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteChildAssociation"), this, 
				new JavaBehaviour(this, "onDeleteChildAssociation"));

		logger.debug("[SplittingNodeIndexer::init] END");   
    }

    public void onCreateNode(ChildAssociationRef childAssocRef) {
    	logger.debug("[SplittingNodeIndexer::onCreateNode] BEGIN");

		try {
			final String protocol = childAssocRef.getChildRef().getStoreRef().getProtocol();
			final boolean isADMProtocol = (protocol.equals("workspace") || protocol.equals("archive"));
			// Non considerare relazioni di tipo "ecm-sys:parts"
			if (isADMProtocol) {
				if (!childAssocRef.getTypeQName().equals(EcmEngineModelConstants.ASSOC_PARTS)) {
					indexer.createNode(nodeService.translateChildAssociationRef(childAssocRef));
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("[SplittingNodeIndexer::onCreateNode] Part ignored: "
								+ childAssocRef.getQName().getLocalName());
					}
				}
			} else {
				indexer.createNode(childAssocRef);
			}
		} finally {
			logger.debug("[SplittingNodeIndexer::onCreateNode] END");
		}
    }

    public void onUpdateNode(NodeRef nodeRef) {
//    	final String protocol = nodeRef.getStoreRef().getProtocol();
//    	final boolean isADMProtocol = (protocol.equals("workspace") || protocol.equals("archive"));
    	
    	logger.debug("[SplittingNodeIndexer::onUpdateNode] BEGIN");

		try {
			indexer.updateNode(nodeRef);
		} finally {
			logger.debug("[SplittingNodeIndexer::onUpdateNode] END");
		}
    }

    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isArchivedNode) {
    	logger.debug("[SplittingNodeIndexer::onDeleteNode] BEGIN");

		try {
			final String protocol = childAssocRef.getChildRef().getStoreRef().getProtocol();
			final boolean isADMProtocol = (protocol.equals("workspace") || protocol.equals("archive"));
			// Non considerare relazioni di tipo "ecm-sys:parts"
			if (isADMProtocol) {
				if (!childAssocRef.getTypeQName().equals(EcmEngineModelConstants.ASSOC_PARTS)) {
					indexer.deleteNode(nodeService.translateChildAssociationRef(childAssocRef));
				} else {
					logger.debug("[SplittingNodeIndexer::onDeleteNode] Part ignored: "
							+ childAssocRef.getQName().getLocalName());
				}
			} else {
				indexer.deleteNode(childAssocRef);
			}
		} finally {
			logger.debug("[SplittingNodeIndexer::onDeleteNode] END");
		}
    }

    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNew) {
    	logger.debug("[SplittingNodeIndexer::onCreateChildAssociation] BEGIN");

		try {
			final String protocol = childAssocRef.getChildRef().getStoreRef().getProtocol();
			final boolean isADMProtocol = (protocol.equals("workspace") || protocol.equals("archive"));
			if (isNew) {
				return;
			}
			// Non considerare relazioni di tipo "ecm-sys:parts"
			if (isADMProtocol) {
				if (!childAssocRef.getTypeQName().equals(EcmEngineModelConstants.ASSOC_PARTS)) {
					indexer.createChildRelationship(nodeService.translateChildAssociationRef(childAssocRef));
				} else {
					logger.debug("[SplittingNodeIndexer::onCreateChildAssociation] Part ignored: "
								+ childAssocRef.getQName().getLocalName());
				}
			} else {
				indexer.createChildRelationship(childAssocRef);
			}
		} finally {
			logger.debug("[SplittingNodeIndexer::onCreateChildAssociation] END");
		}
    }

    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef) {
    	logger.debug("[SplittingNodeIndexer::onDeleteChildAssociation] BEGIN");

		try {
			final String protocol = childAssocRef.getChildRef().getStoreRef().getProtocol();
			final boolean isADMProtocol = (protocol.equals("workspace") || protocol.equals("archive"));
			if (isADMProtocol) {
				// Non considerare relazioni di tipo "ecm-sys:parts"
				if (!childAssocRef.getTypeQName().equals(EcmEngineModelConstants.ASSOC_PARTS)) {
					indexer.deleteChildRelationship(nodeService.translateChildAssociationRef(childAssocRef));
				} else {
					logger.debug("[SplittingNodeIndexer::onDeleteChildAssociation] Part ignored: "
								+ childAssocRef.getQName().getLocalName());
				}
			} else {
				indexer.deleteChildRelationship(childAssocRef);
			}
		} finally {
			logger.debug("[SplittingNodeIndexer::onDeleteChildAssociation] END");
		}
    }
}

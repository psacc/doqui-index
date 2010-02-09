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
 
package it.doqui.index.ecmengine.business.personalization.workflow;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class SimpleWorkflowRejectActionExecuter extends ActionExecuterAbstractBase {

	public static final String NAME = "simple-workflow-reject";

	private NodeService nodeService;
	private CopyService copyService;

	private static Logger logger = Logger.getLogger(SimpleWorkflowRejectActionExecuter.class);

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		if (nodeService.hasAspect(nodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW) == false) {
			throw new AlfrescoRuntimeException("Cannot reject a node that is not part of a workflow.");
		}

		// get the simple workflow aspect properties
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		String rejectStep = (String) props.get(ApplicationModel.PROP_REJECT_STEP);
		Boolean rejectMove = (Boolean) props.get(ApplicationModel.PROP_REJECT_MOVE);
		NodeRef rejectFolder = (NodeRef) props.get(ApplicationModel.PROP_REJECT_FOLDER);

		if (rejectStep == null && rejectMove == null && rejectFolder == null) {
			throw new AlfrescoRuntimeException("The workflow does not have a reject step defined.");
		}

		// first we need to take off the simpleworkflow aspect
		nodeService.removeAspect(nodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW);

		String qname = QName.createValidLocalName((String)props.get(ContentModel.PROP_NAME));
		if (rejectMove != null && rejectMove.booleanValue()) {
			// move the document to the specified folder
			nodeService.moveNode(nodeRef, rejectFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname));
		} else {
			// copy the document to the specified folder
			NodeRef newNode = copyService.copy(nodeRef, rejectFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname), true);

			// the copy service does not copy the name of the node so we
			// need to update the property on the copied item
			nodeService.setProperty(newNode, ContentModel.PROP_NAME, name);
		}

		if (logger.isDebugEnabled()) {
			String movedCopied = rejectMove ? "moved" : "copied";
			logger.debug("Node has been rejected and " + movedCopied + " to folder with id of " + rejectFolder.getId());
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// no parameter definitions for this action
	}

}

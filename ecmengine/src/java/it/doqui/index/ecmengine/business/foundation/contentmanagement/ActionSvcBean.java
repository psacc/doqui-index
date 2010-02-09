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

package it.doqui.index.ecmengine.business.foundation.contentmanagement;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.contentmanagement.ActionRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

public class ActionSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = -3292338043130524452L;

	public Action createAction(String actionName) throws ActionRuntimeException {
		logger.debug("[ActionSvcBean::createAction] BEGIN");
		Action action = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[ActionSvcBean::createAction] creating action : " + actionName);
    		}
			action = serviceRegistry.getActionService().createAction(actionName);
		} catch (Exception e) {
			handleActionServiceException("createAction", e);
		} finally {
			logger.debug("[ActionSvcBean::createAction] END");
		}
		return action;
	}

	public void executeAction(Action action, NodeRef node) throws ActionRuntimeException {
		logger.debug("[ActionSvcBean::executeAction] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[ActionSvcBean::executeAction] executing action on node " + node + ": " + action);
    		}
			serviceRegistry.getActionService().executeAction(action, node);
		} catch (Exception e) {
			handleActionServiceException("executeAction", e);
		} finally {
			logger.debug("[ActionSvcBean::executeAction] END");
		}
	}

	public CompositeAction createCompositeAction() throws ActionRuntimeException {
		logger.debug("[ActionSvcBean::createCompositeAction] BEGIN");
		CompositeAction compositeAction = null;

		try {
			compositeAction = serviceRegistry.getActionService().createCompositeAction();
		} catch (Exception e) {
			handleActionServiceException("createCompositeAction", e);
		} finally {
			logger.debug("[ActionSvcBean::createCompositeAction] END");
		}
		return compositeAction;
	}

	public ActionCondition createActionCondition(String actionConditionName) throws ActionRuntimeException {
		logger.debug("[ActionSvcBean::createActionCondition] BEGIN");
		ActionCondition actionCondition = null;

		try {
			actionCondition = serviceRegistry.getActionService().createActionCondition(actionConditionName);
		} catch (Exception e) {
			handleActionServiceException("createActionCondition", e);
		} finally {
			logger.debug("[ActionSvcBean::createActionCondition] END");
		}
		return actionCondition;
	}

	private void handleActionServiceException(String methodName, Throwable e) throws ActionRuntimeException {
		logger.warn("[ActionSvcBean::handleActionServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new ActionRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new ActionRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new ActionRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new ActionRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new ActionRuntimeException(FoundationErrorCodes.GENERIC_ACTION_SERVICE_ERROR);
		}
	}
}

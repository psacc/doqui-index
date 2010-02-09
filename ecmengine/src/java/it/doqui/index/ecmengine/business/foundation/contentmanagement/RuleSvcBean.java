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
import it.doqui.index.ecmengine.exception.contentmanagement.RuleRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;

public class RuleSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 861496109140516482L;

	public void saveRule(NodeRef node, Rule rule) throws RuleRuntimeException {
		logger.debug("[RuleSvcBean::saveRule] BEGIN");

		try {
			logger.debug("[RuleSvcBean::saveRule] saving rule : " + rule);
			serviceRegistry.getRuleService().saveRule(node, rule);
		} catch (Exception e) {
			handleRuleServiceException("saveRule", e);
		} finally {
			logger.debug("[RuleSvcBean::saveRule] END");
		}
	}

	private void handleRuleServiceException(String methodName, Throwable e) throws RuleRuntimeException {
		logger.warn("[RuleSvcBean::handleRuleServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new RuleRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof NodeLockedException) {
			throw new RuleRuntimeException(FoundationErrorCodes.NODE_LOCKED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new RuleRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new RuleRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else {
			throw new RuleRuntimeException(FoundationErrorCodes.GENERIC_RULE_SERVICE_ERROR);
		}
	}
}

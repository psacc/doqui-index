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

import java.io.InputStream;
import java.util.List;

import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.namespace.QName;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.RepoAdminRuntimeException;

public class RepoAdminSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = 6798959149809715925L;

	private static final String REPO_ADMIN_SERVICE = "RepoAdminService";

	public QName activateModel(String modelFileName) throws RepoAdminRuntimeException {
		logger.debug("[RepoAdminSvcBean::activateModel] BEGIN");
		QName result = null;
		try {
			result = getRepoAdminService().activateModel(modelFileName);
		} catch (Exception e) {
			handleRepoAdminServiceException("activateModel", e);
		} finally {
			logger.debug("[RepoAdminSvcBean::activateModel] END");
		}
		return result;
	}

	public QName deactivateModel(String modelFileName) throws RepoAdminRuntimeException {
		logger.debug("[RepoAdminSvcBean::deactivateModel] BEGIN");
		QName result = null;
		try {
			result = getRepoAdminService().deactivateModel(modelFileName);
		} catch (Exception e) {
			handleRepoAdminServiceException("deactivateModel", e);
		} finally {
			logger.debug("[RepoAdminSvcBean::deactivateModel] END");
		}
		return result;
	}

	public void deployModel(InputStream modelStream, String modelFileName) throws RepoAdminRuntimeException {
		logger.debug("[RepoAdminSvcBean::deployModel] BEGIN");
		try {
			getRepoAdminService().deployModel(modelStream, modelFileName);
		} catch (Exception e) {
			handleRepoAdminServiceException("deployModel", e);
		} finally {
			logger.debug("[RepoAdminSvcBean::deployModel] END");
		}
	}

	public void undeployModel(String modelFileName) throws RepoAdminRuntimeException {
		logger.debug("[RepoAdminSvcBean::undeployModel] BEGIN");
		try {
			getRepoAdminService().undeployModel(modelFileName);
		} catch (Exception e) {
			handleRepoAdminServiceException("undeployModel", e);
		} finally {
			logger.debug("[RepoAdminSvcBean::undeployModel] END");
		}
	}

	public List<RepoModelDefinition> getModels() throws RepoAdminRuntimeException {
		logger.debug("[RepoAdminSvcBean::getModels] BEGIN");
		List<RepoModelDefinition> result = null;
		try {
			result = getRepoAdminService().getModels();
		} catch (Exception e) {
			handleRepoAdminServiceException("getModels", e);
		} finally {
			logger.debug("[RepoAdminSvcBean::getModels] END");
		}
		return result;
	}

	private RepoAdminService getRepoAdminService() {
    	return (RepoAdminService)serviceRegistry.getService(QName.createQName(REPO_ADMIN_SERVICE));
    }
   
	private void handleRepoAdminServiceException(String methodName, Throwable e) throws RepoAdminRuntimeException {
		logger.warn("[RepoAdminSvcBean::handleRepoAdminServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException || e.getCause() instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new RepoAdminRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new RepoAdminRuntimeException(FoundationErrorCodes.GENERIC_REPO_ADMIN_SERVICE_ERROR);
		}
	}

}

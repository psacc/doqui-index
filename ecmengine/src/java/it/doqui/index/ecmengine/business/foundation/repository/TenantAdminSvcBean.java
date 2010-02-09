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

import java.util.List;

import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;
import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.MultiTTenantAdminService;
import org.alfresco.service.namespace.QName;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.TenantRuntimeException;

public class TenantAdminSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -9084583399572312012L;

    public void createTenant(String tenantDomain, char[] adminRawPassword) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::createTenant] BEGIN");
		try {
			getTenantAdminService().createTenant(tenantDomain, adminRawPassword);
		} catch (Exception e) {
			handleTenantAdminServiceException("createTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::createTenant] END");
		}
    }

	public void createTenant(String tenantDomain, char[] adminRawPassword, String rootContentStoreDir) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::createTenant] BEGIN");
		try {
			getTenantAdminService().createTenant(tenantDomain, adminRawPassword, rootContentStoreDir);
		} catch (Exception e) {
			handleTenantAdminServiceException("createTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::createTenant] END");
		}
	}

	public boolean existsTenant(String tenantDomain) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::existsTenant] BEGIN");
		boolean result = false;
		try {
			result = getTenantAdminService().existsTenant(tenantDomain);
		} catch (Exception e) {
			handleTenantAdminServiceException("existsTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::existsTenant] END");
		}
		return result;
	}

	//public void bootstrapWorkflows() throws TenantRuntimeException {}

	public void deleteTenant(String tenantDomain) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::deleteTenant] BEGIN");
		try {
			getTenantAdminService().deleteTenant(tenantDomain);
		} catch (Exception e) {
			handleTenantAdminServiceException("deleteTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::deleteTenant] END");
		}
	}

	public List<Tenant> getAllTenants() throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::getAllTenants] BEGIN");
		List<Tenant> result = null;
		try {
			result = getTenantAdminService().getAllTenantsDoqui();
		} catch (Exception e) {
			handleTenantAdminServiceException("getAllTenants", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::getAllTenants] END");
		}
		return result;
	}

	public void enableTenant(String tenantDomain) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::enableTenant] BEGIN");
		try {
			getTenantAdminService().enableTenant(tenantDomain);
		} catch (Exception e) {
			handleTenantAdminServiceException("enableTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::enableTenant] END");
		}
	}

	public void disableTenant(String tenantDomain) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::disableTenant] BEGIN");
		try {
			getTenantAdminService().disableTenant(tenantDomain);
		} catch (Exception e) {
			handleTenantAdminServiceException("disableTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::disableTenant] END");
		}
	}

	public Tenant getTenant(String tenantDomain) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::getTenant] BEGIN");
		Tenant result = null;
		try {
			result = getTenantAdminService().getTenant(tenantDomain);
		} catch (Exception e) {
			handleTenantAdminServiceException("getTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::getTenant] END");
		}
		return result;
	}

	public boolean isEnabledTenant(String tenantDomain) throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::isEnabledTenant] BEGIN");
		boolean result = false;
		try {
			result = getTenantAdminService().isEnabledTenant(tenantDomain);
		} catch (Exception e) {
			handleTenantAdminServiceException("isEnabledTenant", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::isEnabledTenant] END");
		}
		return result;
	}

	public boolean isEnabled() throws TenantRuntimeException {
		logger.debug("[TenantAdminSvcBean::isEnabled] BEGIN");
		boolean result = false;
		try {
			result = getTenantAdminService().isEnabled();
		} catch (Exception e) {
			handleTenantAdminServiceException("isEnabled", e);
		} finally {
			logger.debug("[TenantAdminSvcBean::isEnabled] END");
		}
		return result;
	}

    private MultiTTenantAdminService getTenantAdminService() {
    	return (MultiTTenantAdminService)serviceRegistry.getService(QName.createQName(ECMENGINE_TENANT_ADMIN_SERVICE_BEAN));
    }
    
    private MultiTTenantAdminService getExportService() {
    	return (MultiTTenantAdminService)serviceRegistry.getService(QName.createQName(ECMENGINE_TENANT_ADMIN_SERVICE_BEAN));
    }

	private void handleTenantAdminServiceException(String methodName, Throwable e) throws TenantRuntimeException {
		logger.warn("[TenantAdminSvcBean::handleTenantAdminServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new TenantRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new TenantRuntimeException(FoundationErrorCodes.GENERIC_TENANT_ADMIN_SERVICE_ERROR);
		}
	}

}

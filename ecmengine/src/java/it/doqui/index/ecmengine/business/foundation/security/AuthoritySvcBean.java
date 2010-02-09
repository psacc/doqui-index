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

package it.doqui.index.ecmengine.business.foundation.security;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.security.AuthorityRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Set;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.AuthorityType;


public class AuthoritySvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 5683403331500327469L;

	public void addAuthority(String parentName, String childName) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::addAuthority] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::addAuthority] Adding child authority to: " + parentName + " (child: " + childName + ")");
    		}
			serviceRegistry.getAuthorityService().addAuthority(parentName, childName);
		} catch (Exception e) {
			handleAuthorityServiceException("addAuthority", e);
		} finally {

			logger.debug("[AuthoritySvcBean::addAuthority] END");
		}
	}

	public boolean authorityExists(String name) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::authorityExists] BEGIN");
		boolean exists = false;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::authorityExists] Checking if authority exists: " + name);
    		}
			exists = serviceRegistry.getAuthorityService().authorityExists(name);
		} catch (Exception e) {
			handleAuthorityServiceException("authorityExists", e);
		} finally {
			logger.debug("[AuthoritySvcBean::authorityExists] END");
		}
		return exists;
	}

	public String createAuthority(AuthorityType type, String parentName, String shortName)
	throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::createAuthority] BEGIN");
		String newAuthority = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::createAuthority] Creating new authority: " + shortName + " (parent: " + parentName + ")");
    		}
			newAuthority = serviceRegistry.getAuthorityService().createAuthority(type, parentName, shortName);
		} catch (Exception e) {
			handleAuthorityServiceException("createAuthority", e);
		} finally {
			logger.debug("[AuthoritySvcBean::createAuthority] END");
		}
		return newAuthority;
	}

	public void deleteAuthority(String name) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::deleteAuthority] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::deleteAuthority] Deleting authority: " + name);
    		}
			serviceRegistry.getAuthorityService().deleteAuthority(name);
		} catch (Exception e) {
			handleAuthorityServiceException("deleteAuthority", e);
		} finally {
			logger.debug("[AuthoritySvcBean::deleteAuthority] END");
		}
	}

	public Set<String> getAllAuthorities(AuthorityType type) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getAllAuthorities] BEGIN");
		Set<String> allAuthorities = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::getAllAuthorities] Retrieving all authorities of type: " + ((type != null) ? type.toString() : "(null)"));
    		}
			allAuthorities = serviceRegistry.getAuthorityService().getAllAuthorities(type);
		} catch (Exception e) {
			handleAuthorityServiceException("getAllAuthorities", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getAllAuthorities] END");
		}
		return allAuthorities;
	}

	public Set<String> getAllRootAuthorities(AuthorityType type) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getAllRootAuthorities] BEGIN");
		Set<String> allRootAuthorities = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::getAllRootAuthorities] Retrieving all root authorities of type: " +((type != null) ? type.toString() : "(null)"));
    		}
			allRootAuthorities = serviceRegistry.getAuthorityService().getAllRootAuthorities(type);
		} catch (Exception e) {
			handleAuthorityServiceException("getAllRootAuthorities", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getAllRootAuthorities] END");
		}
		return allRootAuthorities;
	}

	public Set<String> getAuthorities() throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getAuthorities] BEGIN");
		Set<String> authorities = null;

		try {
			logger.debug("[AuthoritySvcBean::getAuthorities] Retrieving authorities for current user.");
			authorities = serviceRegistry.getAuthorityService().getAuthorities();
		} catch (Exception e) {
			handleAuthorityServiceException("getAuthorities", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getAuthorities] END");
		}
		return authorities;
	}

	public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
	throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getContainedAuthorities] BEGIN");
		Set<String> containedAuthorities = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::getContainedAuthorities] " +
					"Retrieving authorities contained in: " + name + " " +
					"(type: " + ((type != null) ? type.toString() : "(null)") + " " +
					"immediate: " + immediate + ")");
    		}
			containedAuthorities = serviceRegistry.getAuthorityService().getContainedAuthorities(type, name, immediate);
		} catch (Exception e) {
			handleAuthorityServiceException("getContainedAuthorities", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getContainedAuthorities] END");
		}
		return containedAuthorities;
	}

	public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
	throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getContainingAuthorities] BEGIN");
		Set<String> containingAuthorities = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::getContainingAuthorities] " +
	    				"Retrieving authorities containing: " + name + " (type: " +
		    			((type != null) ? type.toString() : "(null)") + " immediate: " + immediate + ")");
    		}
			containingAuthorities = serviceRegistry.getAuthorityService().getContainingAuthorities(type,
					name, immediate);
		} catch (Exception e) {
			handleAuthorityServiceException("getContainingAuthorities", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getContainingAuthorities] END");
		}
		return containingAuthorities;
	}

	public String getName(AuthorityType type, String shortName) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getName] BEGIN");
		String name = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::getName] Retrieving full name of authority: " + shortName);
    		}
			name = serviceRegistry.getAuthorityService().getName(type, shortName);
		} catch (Exception e) {
			handleAuthorityServiceException("getName", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getName] END");
		}
		return name;
	}

	public String getShortName(String name) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::getShortName] BEGIN");
		String shortName = null;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::getShortName] Retrieving short name of authority: " + name);
    		}
			shortName = serviceRegistry.getAuthorityService().getShortName(name);
		} catch (Exception e) {
			handleAuthorityServiceException("getShortName", e);
		} finally {
			logger.debug("[AuthoritySvcBean::getShortName] END");
		}
		return shortName;
	}

	public boolean hasAdminAuthority() throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::hasAdminAuthority] BEGIN");
		boolean hasAdmin = false;

		try {
			logger.debug("[AuthoritySvcBean::hasAdminAuthority] Checking if current user is an admin.");
			hasAdmin = serviceRegistry.getAuthorityService().hasAdminAuthority();
		} catch (Exception e) {
			handleAuthorityServiceException("hasAdminAuthority", e);
		} finally {
			logger.debug("[AuthoritySvcBean::hasAdminAuthority] END");
		}
		return hasAdmin;
	}

	public void removeAuthority(String parentName, String childName) throws AuthorityRuntimeException {
		logger.debug("[AuthoritySvcBean::removeAuthority] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthoritySvcBean::removeAuthority] " +
					"Removing child authority from: " + parentName + " (child: " + childName + ")");
    		}
			serviceRegistry.getAuthorityService().removeAuthority(parentName, childName);
		} catch (Exception e) {
			handleAuthorityServiceException("removeAuthority", e);
		} finally {
			logger.debug("[AuthoritySvcBean::removeAuthority] END");
		}
	}

	private void handleAuthorityServiceException(String methodName, Throwable e) throws AuthorityRuntimeException {
		logger.warn("[AuthoritySvcBean::handleAuthorityServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof UnknownAuthorityException) {
			throw new AuthorityRuntimeException(FoundationErrorCodes.UNKNOWN_AUTHORITY_ERROR);
		} else if (e instanceof AccessDeniedException) {
			throw new AuthorityRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new AuthorityRuntimeException(FoundationErrorCodes.GENERIC_AUTHORITY_SERVICE_ERROR);
		}
	}
}

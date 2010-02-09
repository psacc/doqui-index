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
import it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Set;

import net.sf.acegisecurity.BadCredentialsException;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.permissions.AccessDeniedException;

public class AuthenticationSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = 337994163281601450L;

	public void createAuthentication(String userName, char [] password)
			throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::createAuthentication] BEGIN");

		try {

            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::createAuthentication] Creating authentication for user: " + userName);
    		}
			serviceRegistry.getAuthenticationService().createAuthentication(userName, password);
		} catch (Exception e) {
			handleAuthenticationServiceException("createAuthentication", e);
		} finally {

			logger.debug("[AuthenticationSvcBean::createAuthentication] END");
		}
	}

	public void updateAuthentication(String userName, char [] oldPassword, char [] newPassword)
			throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::updateAuthentication] BEGIN");

		try {

            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::updateAuthentication] " +
					"Updating authentication for user: " + userName);
    		}
			serviceRegistry.getAuthenticationService().updateAuthentication(
					userName, oldPassword, newPassword);
		} catch (Exception e) {
			handleAuthenticationServiceException("updateAuthentication", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::updateAuthentication] END");
		}
	}

	public void setAuthentication(String userName, char [] password)
			throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::setAuthentication] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::setAuthentication] Setting authentication for user: " + userName);
    		}
			serviceRegistry.getAuthenticationService().setAuthentication(userName, password);
		} catch (Exception e) {
			handleAuthenticationServiceException("setAuthentication", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::setAuthentication] END");
		}
	}

	public void deleteAuthentication(String userName) throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::deleteAuthentication] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::deleteAuthentication] Deleting authentication for user: " + userName);
    		}
			serviceRegistry.getAuthenticationService().deleteAuthentication(userName);
		} catch (Exception e) {
			handleAuthenticationServiceException("deleteAuthentication", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::deleteAuthentication] END");
		}
	}

	public boolean getAuthenticationEnabled(String userName) throws AuthenticationRuntimeException {
		boolean status = false;

		logger.debug("[AuthenticationSvcBean::getAuthenticationEnabled] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::getAuthenticationEnabled] " +
					"Retrieving authentication status for user: " + userName);
    		}
			status = serviceRegistry.getAuthenticationService().getAuthenticationEnabled(userName);
		} catch (Exception e) {
			handleAuthenticationServiceException("getAuthenticationEnabled", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getAuthenticationEnabled] END");
		}

		return status;
	}

	public void setAuthenticationEnabled(String userName, boolean enabled)
			throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::setAuthenticationEnabled] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::setAuthenticationEnabled] " +
					"Setting authentication status for user: " + userName + " (" + enabled + ")");
    		}
			serviceRegistry.getAuthenticationService().setAuthenticationEnabled(userName, enabled);
		} catch (Exception e) {
			handleAuthenticationServiceException("setAuthenticationEnabled", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::setAuthenticationEnabled] END");
		}
	}

	public void authenticateAsGuest()
	throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::authenticateAsGuest] BEGIN");

		try {
			logger.debug("[AuthenticationSvcBean::authenticateAsGuest] Authenticating as guest.");
			serviceRegistry.getAuthenticationService().authenticateAsGuest();
		} catch (Exception e) {
			handleAuthenticationServiceException("authenticateAsGuest", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::authenticateAsGuest] END");
		}
	}

	public boolean guestUserAuthenticationAllowed() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::guestUserAuthenticationAllowed] BEGIN");
		boolean result = false;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::guestUserAuthenticationAllowed] " +
					"Retrieving allowed flag for guest authentication.");
    		}
			result = serviceRegistry.getAuthenticationService().guestUserAuthenticationAllowed();
		} catch (Exception e) {
			handleAuthenticationServiceException("guestUserAuthenticationAllowed", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::guestUserAuthenticationAllowed] END");
		}

		return result;
	}

	public boolean authenticationExists(String userName) throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::authenticationExists] BEGIN");
		boolean exists = false;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::authenticationExists] " +
					"Checking if authentication exists for user: " + userName);
    		}
			exists = serviceRegistry.getAuthenticationService().authenticationExists(userName);
		} catch (Exception e) {
			handleAuthenticationServiceException("authenticationExists", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::authenticationExists] END");
		}

		return exists;
	}

	public String getCurrentUserName() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::getCurrentUserName] BEGIN");
		String userName = null;

		try {
			logger.debug("[AuthenticationSvcBean::getCurrentUserName] Retrieving username for current user.");
			userName = serviceRegistry.getAuthenticationService().getCurrentUserName();
		} catch (Exception e) {
			handleAuthenticationServiceException("getCurrentUserName", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getCurrentUserName] END");
		}

		return userName;
	}

	public void invalidateUserSession(String userName) throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::invalidateUserSession] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::invalidateUserSession] Invalidating session for user: " + userName);
    		}
			serviceRegistry.getAuthenticationService().invalidateUserSession(userName);
		} catch (Exception e) {
			handleAuthenticationServiceException("invalidateUserSession", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::invalidateUserSession] END");
		}
	}

	public void invalidateTicket(String ticket) throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::invalidateTicket] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::invalidateTicket] Invalidating ticket: " + ticket);
    		}
			serviceRegistry.getAuthenticationService().invalidateTicket(ticket);
		} catch (Exception e) {
			handleAuthenticationServiceException("invalidateTicket", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::invalidateTicket] END");
		}
	}

	public String getCurrentTicket() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::getCurrentTicket] BEGIN");
		String ticket = null;

		try {
			logger.debug("[AuthenticationSvcBean::getCurrentTicket] Retrieving ticket for current user.");
			ticket = serviceRegistry.getAuthenticationService().getCurrentTicket();
		} catch (Exception e) {
			handleAuthenticationServiceException("getCurrentTicket", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getCurrentTicket] END");
		}

		return ticket;
	}

	public void clearCurrentSecurityContext() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::clearCurrentSecurityContext] BEGIN");

		try {
			logger.debug("[AuthenticationSvcBean::clearCurrentSecurityContext] Cleaning current security context.");
			serviceRegistry.getAuthenticationService().clearCurrentSecurityContext();
		} catch (Exception e) {
			handleAuthenticationServiceException("clearCurrentSecurityContext", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::clearCurrentSecurityContext] END");
		}
	}

	public boolean isCurrentUserTheSystemUser() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::isCurrentUserTheSystemUser] BEGIN");
		boolean isSystemUser = false;

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::isCurrentUserTheSystemUser] " +
					"Checking if current user is system user.");
    		}
			isSystemUser = serviceRegistry.getAuthenticationService().isCurrentUserTheSystemUser();
		} catch (Exception e) {
			handleAuthenticationServiceException("isCurrentUserTheSystemUser", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::isCurrentUserTheSystemUser] END");
		}

		return isSystemUser;
	}

	public Set<String> getDomains() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::getDomains] BEGIN");
		Set<String> domains = null;

		try {
			logger.debug("[AuthenticationSvcBean::getDomains] Retrieving domains list.");
			domains = serviceRegistry.getAuthenticationService().getDomains();
		} catch (Exception e) {
			handleAuthenticationServiceException("getDomains", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getDomains] END");
		}

		return domains;
	}

	public Set<String> getDomainsThatAllowUserCreation() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::getDomainsThatAllowUserCreation] BEGIN");
		Set<String> domains = null;

		try {
			logger.debug("[AuthenticationSvcBean::getDomainsThatAllowUserCreation] Retrieving domains list.");
			domains = serviceRegistry.getAuthenticationService().getDomainsThatAllowUserCreation();
		} catch (Exception e) {
			handleAuthenticationServiceException("getDomainsThatAllowUserCreation", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getDomainsThatAllowUserCreation] END");
		}

		return domains;
	}

	public Set<String> getDomainsThatAllowUserDeletion() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::getDomainsThatAllowUserDeletion] BEGIN");
		Set<String> domains = null;

		try {
			logger.debug("[AuthenticationSvcBean::getDomainsThatAllowUserDeletion] Retrieving domains list.");
			domains = serviceRegistry.getAuthenticationService().getDomainsThatAllowUserDeletion();
		} catch (Exception e) {
			handleAuthenticationServiceException("getDomainsThatAllowUserDeletion", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getDomainsThatAllowUserDeletion] END");
		}

		return domains;
	}

	public Set<String> getDomiansThatAllowUserPasswordChanges() throws AuthenticationRuntimeException {
		logger.debug("[AuthenticationSvcBean::getDomiansThatAllowUserPasswordChanges] BEGIN");
		Set<String> domains = null;

		try {
			logger.debug("[AuthenticationSvcBean::getDomiansThatAllowUserPasswordChanges] Retrieving domains list.");
			domains = serviceRegistry.getAuthenticationService().getDomiansThatAllowUserPasswordChanges();
		} catch (Exception e) {
			handleAuthenticationServiceException("getDomiansThatAllowUserPasswordChanges", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::getDomiansThatAllowUserPasswordChanges] END");
		}

		return domains;
	}

	public void authenticate(String userName, char [] password)
	throws AuthenticationRuntimeException {
	    logger.debug("[AuthenticationSvcBean::authenticate] BEGIN");

		try {
            if(logger.isDebugEnabled()) {
    			logger.debug("[AuthenticationSvcBean::authenticate] Authenticating user: " + userName);
    		}
			serviceRegistry.getAuthenticationService().authenticate(userName, password);
		} catch (Exception e) {
			handleAuthenticationServiceException("authenticate", e);
		} finally {
		    logger.debug("[AuthenticationSvcBean::authenticate] END");
		}
	}

	public void validate(String ticket) throws AuthenticationRuntimeException {
	    logger.debug("[AuthenticationSvcBean::validate] BEGIN");

		try {
			logger.debug("[AuthenticationSvcBean::validate] Validating ticket...");
			serviceRegistry.getAuthenticationService().validate(ticket);
		} catch (Exception e) {
			handleAuthenticationServiceException("validate", e);
		} finally {
			logger.debug("[AuthenticationSvcBean::validate] END");
		}
	}

	private void handleAuthenticationServiceException(String methodName, Throwable e) throws AuthenticationRuntimeException {
		logger.warn("[AuthenticationSvcBean::handleAuthenticationServiceException] " +
				"Exception in method '" + methodName + "': " + e.getMessage(), e);

		/*
		 * Workaround per particolare gestione dell'eccezione in AuthenticationComponentImpl.authenticate()
		 * (AuthenticationComponentImpl.java, linea 78 e seguenti).
		 */
		if (e instanceof AuthenticationException
				&& e.getMessage().startsWith("net.sf.acegisecurity.BadCredentialsException")) {
			throw new AuthenticationRuntimeException(FoundationErrorCodes.BAD_CREDENTIALS_ERROR);
		} else if (e instanceof BadCredentialsException) {
			throw new AuthenticationRuntimeException(FoundationErrorCodes.BAD_CREDENTIALS_ERROR);
		} else if (e instanceof AccessDeniedException) {
			throw new AuthenticationRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new AuthenticationRuntimeException(FoundationErrorCodes.GENERIC_AUTHENTICATION_SERVICE_ERROR);
		}
	}
}

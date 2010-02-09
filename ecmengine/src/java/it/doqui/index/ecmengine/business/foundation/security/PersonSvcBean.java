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
import it.doqui.index.ecmengine.exception.security.PersonRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.namespace.QName;


public class PersonSvcBean extends EcmEngineWrapperBean implements EcmEngineConstants {

	private static final long serialVersionUID = -132202770052778654L;

	public boolean createMissingPeople() throws PersonRuntimeException {
		boolean createMissing = false;

		logger.debug("[PersonSvcBean::createMissingPeople] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::createMissingPeople] Retrieving createMissingPeople flag.");
			createMissing = serviceRegistry.getPersonService().createMissingPeople();
		} catch (Exception e) {
			handlePersonServiceException("createMissingPeople", e);
		} finally {
			logger.debug("[PersonSvcBean::createMissingPeople] END");
		}
		
		return createMissing;
	}

	public NodeRef createPerson(Map<QName, Serializable> properties) throws PersonRuntimeException {
		NodeRef newPerson = null;

		logger.debug("[PersonSvcBean::createPerson] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::createPerson] Creating new person with properties: " + properties);
			newPerson = serviceRegistry.getPersonService().createPerson(properties);
		} catch (Exception e) {
			handlePersonServiceException("createPerson", e);
		} finally {
			logger.debug("[PersonSvcBean::createPerson] END");
		}
		
		return newPerson;
	}

	public void deletePerson(String userName) throws PersonRuntimeException {
		logger.debug("[PersonSvcBean::deletePerson] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::deletePerson] Deleting person by username: " + userName);
			serviceRegistry.getPersonService().deletePerson(userName);
		} catch (Exception e) {
			handlePersonServiceException("deletePerson", e);
		} finally {
			logger.debug("[PersonSvcBean::deletePerson] END");
		}
	}

	public Set<NodeRef> getAllPeople() throws PersonRuntimeException {
		Set<NodeRef> allPeople = null;

		logger.debug("[PersonSvcBean::getAllPeople] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::getAllPeople] Retrieving all people.");
			allPeople = serviceRegistry.getPersonService().getAllPeople();
		} catch (Exception e) {
			handlePersonServiceException("getAllPeople", e);
		} finally {
			logger.debug("[PersonSvcBean::getAllPeople] END");
		}
		
		return allPeople;
	}

	public Set<QName> getMutableProperties() throws PersonRuntimeException {
		Set<QName> properties = null;

		logger.debug("[PersonSvcBean::getMutableProperties] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::getMutableProperties] Retrieving mutable properties.");
			properties = serviceRegistry.getPersonService().getMutableProperties();
		} catch (Exception e) {
			handlePersonServiceException("getMutableProperties", e);
		} finally {
			logger.debug("[PersonSvcBean::getMutableProperties] END");
		}
		
		return properties;
	}

	public NodeRef getPeopleContainer() throws PersonRuntimeException {
		NodeRef container = null;
		
		logger.debug("[PersonSvcBean::getPeopleContainer] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::getPeopleContainer] Retrieving people container NodeRef.");
			container = serviceRegistry.getPersonService().getPeopleContainer();
		} catch (Exception e) {
			handlePersonServiceException("getPeopleContainer", e);
		} finally {
			logger.debug("[PersonSvcBean::getPeopleContainer] END");
		}
		
		return container;
	}

	public NodeRef getPerson(String userName) throws PersonRuntimeException {
		NodeRef person = null;
		boolean createMissingPeople = false;
		
		logger.debug("[PersonSvcBean::getPerson] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::getPerson] Checking createMissingPeople flag...");
			createMissingPeople = serviceRegistry.getPersonService().createMissingPeople();
			logger.debug("[PersonSvcBean::getPerson] " +
					"Missing people " + ((createMissingPeople) ? "will" : "won't") + " be created " +
					"(createMissingPeople = " + createMissingPeople + ")");
			
			logger.debug("[PersonSvcBean::getPerson] Retrieving person by username: " + userName);
			person = serviceRegistry.getPersonService().getPerson(userName);
		} catch (Exception e) {
			handlePersonServiceException("getPerson", e);
		} finally {
			logger.debug("[PersonSvcBean::getPerson] END");
		}
		
		return person;
	}

	public String getUserIdentifier(String caseSensitiveUserName) throws PersonRuntimeException {
		String id = null;
		
		logger.debug("[PersonSvcBean::getUserIdentifier] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::getUserIdentifier] " +
					"Retrieving user identifier by username: " + caseSensitiveUserName);
			id = serviceRegistry.getPersonService().getUserIdentifier(caseSensitiveUserName);
		} catch (Exception e) {
			handlePersonServiceException("getUserIdentifier", e);
		} finally {
			logger.debug("[PersonSvcBean::getUserIdentifier] END");
		}
		
		return id;
	}

	public boolean getUserNamesAreCaseSensitive() throws PersonRuntimeException {
		boolean caseSensitive = false;
		
		logger.debug("[PersonSvcBean::getUserNamesAreCaseSensitive] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::getUserNamesAreCaseSensitive] Retrieving userNamesAreCaseSensitive flag.");
			caseSensitive = serviceRegistry.getPersonService().getUserNamesAreCaseSensitive();
		} catch (Exception e) {
			handlePersonServiceException("getUserNamesAreCaseSensitive", e);
		} finally {
			logger.debug("[PersonSvcBean::getUserNamesAreCaseSensitive] END");
		}
		
		return caseSensitive;
	}

	public boolean isMutable() throws PersonRuntimeException {
		boolean mutable = false;
		
		logger.debug("[PersonSvcBean::isMutable] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::isMutable] Retrieving mutable flag.");
			mutable = serviceRegistry.getPersonService().isMutable();
		} catch (Exception e) {
			handlePersonServiceException("isMutable", e);
		} finally {
			logger.debug("[PersonSvcBean::isMutable] END");
		}
		
		return mutable;
	}

	public boolean personExists(String userName) throws PersonRuntimeException {
		boolean personExists = false;
		
		logger.debug("[PersonSvcBean::personExists] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::personExists] Checking if person exists: " + userName);
			personExists = serviceRegistry.getPersonService().personExists(userName);
		} catch (Exception e) {
			handlePersonServiceException("personExists", e);
		} finally {
			logger.debug("[PersonSvcBean::personExists] END");
		}
		
		return personExists;
	}

	public void setCreateMissingPeople(boolean createMissing) throws PersonRuntimeException {
		logger.debug("[PersonSvcBean::setCreateMissingPeople] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::setCreateMissingPeople] Setting createMissingPeople flag: " + createMissing);
			serviceRegistry.getPersonService().setCreateMissingPeople(createMissing);
		} catch (Exception e) {
			handlePersonServiceException("setCreateMissingPeople", e);
		} finally {
			logger.debug("[PersonSvcBean::setCreateMissingPeople] END");
		}
	}

	public void setPersonProperties(String userName, Map<QName, Serializable> properties) 
			throws PersonRuntimeException {
		logger.debug("[PersonSvcBean::setPersonProperties] BEGIN");
		
		try {
			logger.debug("[PersonSvcBean::setPersonProperties] Setting person properties by username: " + userName + 
					" (properties: " + properties + ")");
			serviceRegistry.getPersonService().setPersonProperties(userName, properties);
		} catch (Exception e) {
			handlePersonServiceException("setPersonProperties", e);
		} finally {
			logger.debug("[PersonSvcBean::setPersonProperties] END");
		}
	}
	
	private void handlePersonServiceException(String methodName, Throwable e) throws PersonRuntimeException {
		logger.warn("[PersonSvcBean::handlePersonServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);
		
		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new PersonRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else if (e instanceof InvalidNodeRefException) {
			throw new PersonRuntimeException(FoundationErrorCodes.INVALID_NODE_REF_ERROR);
		} else if (e instanceof DuplicateChildNodeNameException) {
			throw new PersonRuntimeException(FoundationErrorCodes.DUPLICATE_CHILD_ERROR);
		} else if (e instanceof NoSuchPersonException) {
			throw new PersonRuntimeException(FoundationErrorCodes.NO_SUCH_PERSON_ERROR);
		} else {
			throw new PersonRuntimeException(FoundationErrorCodes.GENERIC_PERSON_SERVICE_ERROR);
		}
	}
}

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

package it.doqui.index.ecmengine.client.webservices.backoffice;

import it.doqui.index.ecmengine.client.webservices.dto.*;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.*;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.*;
import it.doqui.index.ecmengine.client.webservices.exception.EcmEngineException;
import it.doqui.index.ecmengine.client.webservices.exception.InvalidParameterException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.AclEditException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.GroupAlreadyExistsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.GroupCreateException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.GroupDeleteException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.GroupEditException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.NoSuchGroupException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.NoSuchUserException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.TooManyNodesException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.UserAlreadyExistsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.UserCreateException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.UserDeleteException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.backoffice.UserUpdateException;

public interface EcmEngineWebServiceBackofficeDelegate {

	String createUser(User nuovoUtente, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserCreateException, UserAlreadyExistsException,
	EcmEngineTransactionException, PermissionDeniedException, Exception;

	void updateUserMetadata(User utente,OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException,
	EcmEngineTransactionException, PermissionDeniedException, Exception;

	User [] listAllUsers(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	EcmEngineTransactionException, Exception;

	User retrieveUserMetadata(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	EcmEngineTransactionException, PermissionDeniedException, Exception;

	String createGroup(Group nuovoGruppo, Group gruppoPadre, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupCreateException, GroupAlreadyExistsException,
	EcmEngineTransactionException, NoSuchGroupException, PermissionDeniedException, Exception;

	void addUserToGroup(User utente, Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupEditException, NoSuchUserException, EcmEngineTransactionException,
	NoSuchGroupException, PermissionDeniedException, Exception;

	void removeUserFromGroup(User utente, Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupEditException, NoSuchUserException, EcmEngineTransactionException,
	NoSuchGroupException, PermissionDeniedException, Exception;

	User [] listUsers(Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineException, EcmEngineTransactionException, Exception;

	void updateUserPassword(User utente, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	void deleteUser(User utente,OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserDeleteException, NoSuchUserException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	void deleteGroup(Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupDeleteException, NoSuchGroupException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	void addAcl(Node node, AclRecord [] acls, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	void removeAcl(Node node, AclRecord [] acls, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	void updateAcl(Node node, AclRecord [] acls, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	AclRecord [] listAcl(Node node, AclListParams params, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	void setInheritsAcl(Node node, boolean inherits, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	boolean isInheritsAcl(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, Exception;

	void resetAcl(Node node, AclRecord filter ,OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException,
	EcmEngineTransactionException, PermissionDeniedException, Exception;

	IntegrityReport [] checkRepositoryIntegrity(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, TooManyNodesException,
	EcmEngineTransactionException, Exception;

    void importDataArchive(DataArchive data, Node parent, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException,
    EcmEngineTransactionException, EcmEngineException, PermissionDeniedException, Exception;

	SystemProperty [] getSystemProperties(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, Exception;

	ModelMetadata getModelDefinition(ModelDescriptor modelDescriptor, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineTransactionException,
	EcmEngineException, Exception;

	TypeMetadata getTypeDefinition(ModelDescriptor typeDescriptor, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException,
	InvalidCredentialsException, Exception;

	ModelDescriptor [] getAllModelDescriptors(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineTransactionException, EcmEngineException, Exception;

	Repository [] getRepositories(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, Exception;

	Group [] listGroups(Group parentGroup, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineTransactionException,
	EcmEngineException, Exception;

	Group [] listAllGroups(Group filter, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, EcmEngineException,
	NoDataExtractedException, Exception;

	void createTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	void enableTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	void disableTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	Tenant[] getAllTenants(OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, Exception;

	Tenant getTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, Exception;

	boolean tenantExists(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	void tenantDelete(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	CustomModel[] getAllCustomModels(OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, Exception;

	void deployCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	void undeployCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, NoDataExtractedException,EcmEngineException, Exception;

	void activateCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	void deactivateCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception;

	boolean testResources() throws Exception;
}

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

package it.doqui.index.ecmengine.client.backoffice;

import it.doqui.index.ecmengine.business.publishing.backoffice.EcmEngineBackofficeBusinessInterface;
import it.doqui.index.ecmengine.dto.AclRecord;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.backoffice.AclListParams;
import it.doqui.index.ecmengine.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.dto.backoffice.ExportedContent;
import it.doqui.index.ecmengine.dto.backoffice.Group;
import it.doqui.index.ecmengine.dto.backoffice.IntegrityReport;
import it.doqui.index.ecmengine.dto.backoffice.Repository;
import it.doqui.index.ecmengine.dto.backoffice.SystemProperty;
import it.doqui.index.ecmengine.dto.backoffice.Tenant;
import it.doqui.index.ecmengine.dto.backoffice.User;
import it.doqui.index.ecmengine.dto.backoffice.model.CustomModel;
import it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor;
import it.doqui.index.ecmengine.dto.backoffice.model.ModelMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.TypeMetadata;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.AclEditException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupAlreadyExistsException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupCreateException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupDeleteException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupEditException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchUserException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.TooManyNodesException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserAlreadyExistsException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserCreateException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserDeleteException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserUpdateException;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;

public abstract class AbstractEcmEngineBackofficeDelegateImpl implements EcmEngineBackofficeDelegate {

    protected Log log = null;

    private EcmEngineBackofficeBusinessInterface ecmEngineBackofficeInterface = null;

    protected AbstractEcmEngineBackofficeDelegateImpl(Log inLog) {
		inLog.debug("["+getClass().getSimpleName()+"::constructor] BEGIN");
		this.log = inLog;
		try {
        	initializeService();
		} catch (Throwable ex) {
			log.error("["+getClass().getSimpleName()+"::constructor] eccezione", ex);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::constructor] END");
		}
    }

    protected abstract EcmEngineBackofficeBusinessInterface createBackofficeService() throws Throwable;

    private void initializeService() throws Throwable {
		log.debug("["+getClass().getSimpleName()+"::initializeManagement] BEGIN");
		try {
			this.ecmEngineBackofficeInterface = createBackofficeService();
		} catch(Throwable t) {
			log.error("["+getClass().getSimpleName()+"::initializeManagement] eccezione", t);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::initializeManagement] END");
		}
	}

	public String createUser(User nuovoUtente, OperationContext context)
	throws InvalidParameterException, UserCreateException, RemoteException, UserAlreadyExistsException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
	   	this.log.debug("["+getClass().getSimpleName()+"::createUser] BEGIN");
	   	String result = null;
	   	try {
	   		result = this.ecmEngineBackofficeInterface.createUser(nuovoUtente, context);
	   	} finally {
	   		this.log.debug("["+getClass().getSimpleName()+"::createUser] END");
	   	}
	   	return result;
    }

	public void updateUserMetadata(User utente,OperationContext context)
	throws InvalidParameterException, UserUpdateException, NoSuchUserException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

	 	log.debug("["+getClass().getSimpleName()+"::updateUserMetadata] BEGIN");

	   	try {
	   		 this.ecmEngineBackofficeInterface.updateUserMetadata(utente, context);
	   	} finally {
	   		log.debug("["+getClass().getSimpleName()+"::updateUserMetadata] END");
	   	}
	}


	public String createGroup(Group nuovoGruppo, Group gruppoPadre, OperationContext context)
	throws InvalidParameterException, GroupCreateException, NoSuchGroupException, RemoteException,
	GroupAlreadyExistsException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::createGroup] BEGIN");
		String result = null;
		try {
			result = this.ecmEngineBackofficeInterface.createGroup(nuovoGruppo, gruppoPadre, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::createGroup] END");
		}
		return result;
	}

	public void addUserToGroup(User utente, Group gruppo, OperationContext context)
	throws InvalidParameterException, GroupEditException, RemoteException, NoSuchUserException, NoSuchGroupException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::addUserToGroup] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.addUserToGroup(utente, gruppo, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::addUserToGroup] END");
		}
	}

	public void removeUserFromGroup(User utente, Group gruppo, OperationContext context)
	throws InvalidParameterException, RemoteException, GroupEditException, NoSuchUserException, NoSuchGroupException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::removeUserFromGroup] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.removeUserFromGroup(utente, gruppo, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::removeUserFromGroup] END");
		}
	}

	public User [] listAllUsers(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, RemoteException, EcmEngineException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::listAllUsers] BEGIN");
		User [] results = null;
		try {
			results = this.ecmEngineBackofficeInterface.listAllUsers(filter, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::listAllUsers] END");
		}

		return results;
	}

	public User [] listUsers(Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, RemoteException, EcmEngineException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::listUsers] BEGIN");
		User [] results = null;
		try {
			results = this.ecmEngineBackofficeInterface.listUsers(gruppo, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::listUsers] END");
		}

		return results;
	}

	public User retrieveUserMetadata(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	RemoteException, PermissionDeniedException, EcmEngineTransactionException {
		log.debug("["+getClass().getSimpleName()+"::retrieveUserMetadata] BEGIN");
		User result = null;
		try {
			result = this.ecmEngineBackofficeInterface.retrieveUserMetadata(filter, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::retrieveUserMetadata] END");
		}
		return result;
	}



	public void updateUserPassword(User utente, OperationContext context)
	throws InvalidParameterException, UserUpdateException, NoSuchUserException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		log.debug("["+getClass().getSimpleName()+"::updateUserPassword] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.updateUserPassword(utente, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::updateUserPassword] END");
		}
	}

	public void deleteUser(User utente,OperationContext context)
	throws InvalidParameterException, UserDeleteException, NoSuchUserException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		log.debug("["+getClass().getSimpleName()+"::deleteUser] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.deleteUser(utente, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::deleteUser] END");
		}
	}

	public void deleteGroup(Group gruppo, OperationContext context)
	throws InvalidParameterException, GroupDeleteException, NoSuchGroupException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		log.debug("["+getClass().getSimpleName()+"::deleteGroup] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.deleteGroup(gruppo, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::deleteGroup] END");
		}
	}


	public void addAcl(Node node, AclRecord[] acls, OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::addAcl] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.addAcl(node, acls, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::addAcl] END");
		}
	}

	public boolean isInheritsAcl(Node node, OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::isInheritsAcl] BEGIN");
		boolean result = false;

		try {
			result = this.ecmEngineBackofficeInterface.isInheritsAcl(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::isInheritsAcl] END");
		}
		return result;
	}

	public AclRecord[] listAcl(Node node, AclListParams params, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, AclEditException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::listAcl] BEGIN");
		AclRecord [] result = null;

		try {
			result = this.ecmEngineBackofficeInterface.listAcl(node, params, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::listAcl] END");
		}
		return result;
	}

	public void removeAcl(Node node, AclRecord[] acls, OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::removeAcl] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.removeAcl(node, acls, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::removeAcl] END");
		}
	}

	public void setInheritsAcl(Node node, boolean inherits,	OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::setInheritsAcl] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.setInheritsAcl(node, inherits, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::setInheritsAcl] END");
		}
	}

	public void updateAcl(Node node, AclRecord[] acls, OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::updateAcl] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.updateAcl(node, acls, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::updateAcl] END");
		}
	}

	public void resetAcl(Node node, AclRecord filter ,OperationContext context)
	throws InvalidParameterException, AclEditException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		log.debug("["+getClass().getSimpleName()+"::resetAcl] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.resetAcl(node, filter, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::resetAcl] END");
		}
	}

	public IntegrityReport[] checkRepositoryIntegrity(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, TooManyNodesException, RemoteException,
	InvalidCredentialsException, EcmEngineTransactionException {
		log.debug("["+getClass().getSimpleName()+"::checkRepositoryIntegrity] BEGIN");
		try {
			return this.ecmEngineBackofficeInterface.checkRepositoryIntegrity(node, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::checkRepositoryIntegrity] END");
		}
	}

    public void importDataArchive(DataArchive data, Node parent, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException,
    EcmEngineTransactionException, EcmEngineException,
    PermissionDeniedException, RemoteException {
		this.log.debug("["+getClass().getSimpleName()+"::revertVersion] BEGIN");
		try {
			this.ecmEngineBackofficeInterface.importDataArchive(data, parent, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::importDataArchive] END");
		}
	}

    public boolean testResources() throws EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::testResources] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.testResources();
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::testResources] END");
        }

    }

	public ModelDescriptor[] getAllModelDescriptors(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, RemoteException, EcmEngineTransactionException {
    	this.log.debug("["+getClass().getSimpleName()+"::getAllModelDescriptors] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getAllModelDescriptors(context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getAllModelDescriptors] END");
        }
	}

	public ModelMetadata getModelDefinition(ModelDescriptor modelDescriptor, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	RemoteException, EcmEngineTransactionException {
    	this.log.debug("["+getClass().getSimpleName()+"::getModelDefinition] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getModelDefinition(modelDescriptor, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getModelDefinition] END");
        }
	}

	public TypeMetadata getTypeDefinition(ModelDescriptor typeDescriptor, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	RemoteException, EcmEngineTransactionException {
    	this.log.debug("["+getClass().getSimpleName()+"::getTypeDefinition] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getTypeDefinition(typeDescriptor, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getTypeDefinition] END");
        }
	}

	public Repository[] getRepositories(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::getRepositories] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getRepositories(context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getRepositories] END");
        }
	}

	public SystemProperty[] getSystemProperties(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::getSystemProperties] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getSystemProperties(context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getSystemProperties] END");
        }
	}

	public Group[] listAllGroups(Group filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	RemoteException, EcmEngineTransactionException {
    	this.log.debug("["+getClass().getSimpleName()+"::listAllGroups] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.listAllGroups(filter, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::listAllGroups] END");
        }
	}

	public Group[] listGroups(Group parentGroup, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineException,
	RemoteException, EcmEngineTransactionException {
    	this.log.debug("["+getClass().getSimpleName()+"::listGroups] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.listGroups(parentGroup, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::listGroups] END");
        }
	}

	public void createTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::createTenant] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.createTenant(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::createTenant] END");
        }
	}

	public void disableTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::disableTenant] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.disableTenant(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::disableTenant] END");
        }
	}

	public void enableTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::enableTenant] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.enableTenant(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::enableTenant] END");
        }
	}

	public Tenant[] getAllTenants(OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::getAllTenants] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getAllTenants(context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getAllTenants] END");
        }
	}

	public Tenant getTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::getTenant] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getTenant(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getTenant] END");
        }
	}

	public boolean tenantExists(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::tenantExists] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.tenantExists(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::tenantExists] END");
        }
	}

	public void tenantDelete(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::tenantDelete] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.tenantDelete(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::tenantDelete] END");
        }
	}

	public void activateCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::activateCustomModel] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.activateCustomModel(model, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::activateCustomModel] END");
        }
	}

	public void deactivateCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::deactivateCustomModel] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.deactivateCustomModel(model, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::deactivateCustomModel] END");
        }
	}

	public void deployCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::deployCustomModel] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.deployCustomModel(model, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::deployCustomModel] END");
        }
	}

	public CustomModel[] getAllCustomModels(OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::getAllCustomModels] BEGIN");

        try {
            return this.ecmEngineBackofficeInterface.getAllCustomModels(context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getAllCustomModels] END");
        }
	}

	public void undeployCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, PermissionDeniedException, NoDataExtractedException,EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::undeployCustomModel] BEGIN");

        try {
            this.ecmEngineBackofficeInterface.undeployCustomModel(model, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::undeployCustomModel] END");
        }
	}

	public ExportedContent exportTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException{
    	this.log.debug("["+getClass().getSimpleName()+"::exportTenant] BEGIN");
        try {
            return this.ecmEngineBackofficeInterface.exportTenant(tenant, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::exportTenant] END");
        }
	}
	
	public void importTenant(ExportedContent content,Tenant dest, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException{
    	this.log.debug("["+getClass().getSimpleName()+"::importTenant] BEGIN");
        try {
            this.ecmEngineBackofficeInterface.importTenant(content,dest, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::importTenant] END");
        }
	}
	
}

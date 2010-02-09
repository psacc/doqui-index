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

import it.doqui.index.ecmengine.client.backoffice.EcmEngineBackofficeDelegate;
import it.doqui.index.ecmengine.client.backoffice.EcmEngineBackofficeDirectDelegateImpl;
import it.doqui.index.ecmengine.client.webservices.AbstractWebServiceDelegateBase;
import it.doqui.index.ecmengine.client.webservices.dto.AclRecord;
import it.doqui.index.ecmengine.client.webservices.dto.Node;
import it.doqui.index.ecmengine.client.webservices.dto.OperationContext;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.AclListParams;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.Group;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.IntegrityReport;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.Repository;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.SystemProperty;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.Tenant;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.User;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.CustomModel;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.ModelDescriptor;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.ModelMetadata;
import it.doqui.index.ecmengine.client.webservices.dto.backoffice.model.TypeMetadata;
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

public class EcmEngineWebServiceBackofficeDelegateImpl extends AbstractWebServiceDelegateBase implements EcmEngineWebServiceBackofficeDelegate {

	private EcmEngineBackofficeDelegate ecmEngineBackofficeDelegate;

    public EcmEngineWebServiceBackofficeDelegateImpl() {
    	ecmEngineBackofficeDelegate = new EcmEngineBackofficeDirectDelegateImpl(log);
    }

    public void activateCustomModel(CustomModel model, OperationContext context)
    throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::activateCustomModel] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.activateCustomModel(
					(it.doqui.index.ecmengine.dto.backoffice.model.CustomModel)convertDTO(model, it.doqui.index.ecmengine.dto.backoffice.model.CustomModel.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::activateCustomModel] END");
		}
	}

	public void addAcl(Node node, AclRecord[] acls, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::addAcl] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.addAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.AclRecord[])convertDTOArray(acls, it.doqui.index.ecmengine.dto.AclRecord.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::addAcl] END");
		}
	}

	public void addUserToGroup(User utente, Group gruppo, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, GroupEditException, NoSuchUserException, EcmEngineTransactionException, NoSuchGroupException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::addUserToGroup] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.addUserToGroup(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(utente, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(gruppo, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::addUserToGroup] END");
		}
	}

	public IntegrityReport[] checkRepositoryIntegrity(Node node, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, TooManyNodesException, EcmEngineTransactionException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::checkRepositoryIntegrity] BEGIN");
    	IntegrityReport[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.IntegrityReport[] resultNodeArray = this.ecmEngineBackofficeDelegate.checkRepositoryIntegrity(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (IntegrityReport[])convertDTOArray(resultNodeArray, IntegrityReport.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::checkRepositoryIntegrity] END");
		}
		return result;
	}

	public String createGroup(Group nuovoGruppo, Group gruppoPadre, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, GroupCreateException, GroupAlreadyExistsException, EcmEngineTransactionException, NoSuchGroupException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::createGroup] BEGIN");
    	String result = null;
    	try {
    		result = this.ecmEngineBackofficeDelegate.createGroup(
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(nuovoGruppo, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(gruppoPadre, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::createGroup] END");
		}
		return result;
	}

	public void createTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::createTenant] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.createTenant(
					(it.doqui.index.ecmengine.dto.backoffice.Tenant)convertDTO(tenant, it.doqui.index.ecmengine.dto.backoffice.Tenant.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::createTenant] END");
		}
	}

	public String createUser(User nuovoUtente, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, UserCreateException, UserAlreadyExistsException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::createUser] BEGIN");
    	String result = null;
    	try {
    		result = this.ecmEngineBackofficeDelegate.createUser(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(nuovoUtente, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::createUser] END");
		}
		return result;
	}

	public void deactivateCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deactivateCustomModel] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.deactivateCustomModel(
					(it.doqui.index.ecmengine.dto.backoffice.model.CustomModel)convertDTO(model, it.doqui.index.ecmengine.dto.backoffice.model.CustomModel.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deactivateCustomModel] END");
		}
	}

	public void deleteGroup(Group gruppo, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, GroupDeleteException, NoSuchGroupException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deleteGroup] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.deleteGroup(
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(gruppo, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deleteGroup] END");
		}
	}

	public void deleteUser(User utente, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, UserDeleteException, NoSuchUserException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deleteUser] BEGIN");
    	try {
    		this.ecmEngineBackofficeDelegate.deleteUser(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(utente, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deleteUser] END");
		}
	}

	public void deployCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deployCustomModel] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.deployCustomModel(
					(it.doqui.index.ecmengine.dto.backoffice.model.CustomModel)convertDTO(model, it.doqui.index.ecmengine.dto.backoffice.model.CustomModel.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::deployCustomModel] END");
		}
	}

	public void disableTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::disableTenant] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.disableTenant(
					(it.doqui.index.ecmengine.dto.backoffice.Tenant)convertDTO(tenant, it.doqui.index.ecmengine.dto.backoffice.Tenant.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::disableTenant] END");
		}
	}

	public void enableTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::enableTenant] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.enableTenant(
					(it.doqui.index.ecmengine.dto.backoffice.Tenant)convertDTO(tenant, it.doqui.index.ecmengine.dto.backoffice.Tenant.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::enableTenant] END");
		}
	}

	public CustomModel[] getAllCustomModels(OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getAllCustomModels] BEGIN");
    	CustomModel[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.model.CustomModel[] resultCustomModelArray = this.ecmEngineBackofficeDelegate.getAllCustomModels(
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (CustomModel[])convertDTOArray(resultCustomModelArray, CustomModel.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getAllCustomModels] END");
		}
		return result;
	}

	public ModelDescriptor[] getAllModelDescriptors(OperationContext context) throws InvalidParameterException, InvalidCredentialsException, EcmEngineTransactionException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getAllModelDescriptors] BEGIN");
    	ModelDescriptor[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor[] resultModelDescriptorArray = this.ecmEngineBackofficeDelegate.getAllModelDescriptors(
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (ModelDescriptor[])convertDTOArray(resultModelDescriptorArray, ModelDescriptor.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getAllModelDescriptors] END");
		}
		return result;
	}

	public Tenant[] getAllTenants(OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getAllTenants] BEGIN");
    	Tenant[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.Tenant[] resultTenantArray = this.ecmEngineBackofficeDelegate.getAllTenants(
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (Tenant[])convertDTOArray(resultTenantArray, Tenant.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getAllTenants] END");
		}
		return result;
	}

	public ModelMetadata getModelDefinition(ModelDescriptor modelDescriptor, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineTransactionException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getModelDefinition] BEGIN");
    	ModelMetadata result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.model.ModelMetadata resultModelMetadata = this.ecmEngineBackofficeDelegate.getModelDefinition(
					(it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor)convertDTO(modelDescriptor, it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (ModelMetadata)convertDTO(resultModelMetadata, ModelMetadata.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getModelDefinition] END");
		}
		return result;
	}

	public Repository[] getRepositories(OperationContext context) throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getRepositories] BEGIN");
    	Repository[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.Repository[] resultRepositoryArray = this.ecmEngineBackofficeDelegate.getRepositories(
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (Repository[])convertDTOArray(resultRepositoryArray, Repository.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getRepositories] END");
		}
		return result;
	}

	public SystemProperty[] getSystemProperties(OperationContext context) throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getSystemProperties] BEGIN");
    	SystemProperty[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.SystemProperty[] resultSystemPropertyArray = this.ecmEngineBackofficeDelegate.getSystemProperties(
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (SystemProperty[])convertDTOArray(resultSystemPropertyArray, SystemProperty.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getSystemProperties] END");
		}
		return result;
	}

	public Tenant getTenant(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getTenant] BEGIN");
    	Tenant result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.Tenant resultTenant = this.ecmEngineBackofficeDelegate.getTenant(
					(it.doqui.index.ecmengine.dto.backoffice.Tenant)convertDTO(tenant, it.doqui.index.ecmengine.dto.backoffice.Tenant.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (Tenant)convertDTO(resultTenant, Tenant.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getTenant] END");
		}
		return result;
	}

	public TypeMetadata getTypeDefinition(ModelDescriptor typeDescriptor, OperationContext context) throws InvalidParameterException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException, InvalidCredentialsException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getTypeDefinition] BEGIN");
    	TypeMetadata result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.model.TypeMetadata resultTypeMetadata = this.ecmEngineBackofficeDelegate.getTypeDefinition(
					(it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor)convertDTO(typeDescriptor, it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (TypeMetadata)convertDTO(resultTypeMetadata, TypeMetadata.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::getTypeDefinition] END");
		}
		return result;
	}

    public void importDataArchive(DataArchive data, Node parent, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException,
    EcmEngineTransactionException, EcmEngineException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::importDataArchive] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.importDataArchive(
					(it.doqui.index.ecmengine.dto.backoffice.DataArchive)convertDTO(data, it.doqui.index.ecmengine.dto.backoffice.DataArchive.class),
					(it.doqui.index.ecmengine.dto.Node)convertDTO(parent, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::importDataArchive] END");
		}
	}

	public boolean isInheritsAcl(Node node, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::isInheritsAcl] BEGIN");
    	boolean result = false;
    	try {
			result = this.ecmEngineBackofficeDelegate.isInheritsAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::isInheritsAcl] END");
		}
		return result;
	}

	public AclRecord[] listAcl(Node node, AclListParams params, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listAcl] BEGIN");
    	AclRecord[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.AclRecord[] resultAclRecordArray = this.ecmEngineBackofficeDelegate.listAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.backoffice.AclListParams)convertDTO(params, it.doqui.index.ecmengine.dto.backoffice.AclListParams.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (AclRecord[])convertDTOArray(resultAclRecordArray, AclRecord.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listAcl] END");
		}
		return result;
	}

	public Group[] listAllGroups(Group filter, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, EcmEngineException, NoDataExtractedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listAllGroups] BEGIN");
    	Group[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.Group[] resultGroupArray = this.ecmEngineBackofficeDelegate.listAllGroups(
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(filter, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (Group[])convertDTOArray(resultGroupArray, Group.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listAllGroups] END");
		}
		return result;
	}

	public User[] listAllUsers(User filter, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listAllUsers] BEGIN");
    	User[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.User[] resultUserArray = this.ecmEngineBackofficeDelegate.listAllUsers(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(filter, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (User[])convertDTOArray(resultUserArray, User.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listAllUsers] END");
		}
		return result;
	}

	public Group[] listGroups(Group parentGroup, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineTransactionException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listGroups] BEGIN");
    	Group[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.Group[] resultGroupArray = this.ecmEngineBackofficeDelegate.listGroups(
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(parentGroup, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (Group[])convertDTOArray(resultGroupArray, Group.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listGroups] END");
		}
		return result;
	}

	public User[] listUsers(Group gruppo, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineException, EcmEngineTransactionException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listUsers] BEGIN");
    	User[] result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.User[] resultUserArray = this.ecmEngineBackofficeDelegate.listUsers(
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(gruppo, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (User[])convertDTOArray(resultUserArray, User.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::listUsers] END");
		}
		return result;
	}

	public void removeAcl(Node node, AclRecord[] acls, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::removeAcl] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.removeAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.AclRecord[])convertDTOArray(acls, it.doqui.index.ecmengine.dto.AclRecord.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::removeAcl] END");
		}
	}

	public void removeUserFromGroup(User utente, Group gruppo, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, GroupEditException, NoSuchUserException, EcmEngineTransactionException, NoSuchGroupException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::removeUserFromGroup] BEGIN");
    	try {
    		this.ecmEngineBackofficeDelegate.removeUserFromGroup(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(utente, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.backoffice.Group)convertDTO(gruppo, it.doqui.index.ecmengine.dto.backoffice.Group.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::removeUserFromGroup] END");
		}
	}

	public void resetAcl(Node node, AclRecord filter, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::resetAcl] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.resetAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.AclRecord)convertDTO(filter, it.doqui.index.ecmengine.dto.AclRecord.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::resetAcl] END");
		}
	}

	public User retrieveUserMetadata(User filter, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::retrieveUserMetadata] BEGIN");
    	User result = null;
    	try {
    		it.doqui.index.ecmengine.dto.backoffice.User resultUser = this.ecmEngineBackofficeDelegate.retrieveUserMetadata(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(filter, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
    		result = (User)convertDTO(resultUser, User.class);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::retrieveUserMetadata] END");
		}
		return result;
	}

	public void setInheritsAcl(Node node, boolean inherits, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::setInheritsAcl] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.setInheritsAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					inherits,
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::setInheritsAcl] END");
		}
	}

	public boolean tenantExists(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::tenantExists] BEGIN");
    	boolean result = false;
    	try {
			result = this.ecmEngineBackofficeDelegate.tenantExists(
					(it.doqui.index.ecmengine.dto.backoffice.Tenant)convertDTO(tenant, it.doqui.index.ecmengine.dto.backoffice.Tenant.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::tenantExists] END");
		}
		return result;
	}

	public void tenantDelete(Tenant tenant, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::tenantDelete] BEGIN");
    	try {
			 this.ecmEngineBackofficeDelegate.tenantDelete(
			(it.doqui.index.ecmengine.dto.backoffice.Tenant)convertDTO(tenant, it.doqui.index.ecmengine.dto.backoffice.Tenant.class),
			(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
			 );
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::tenantDelete] END");
		}
	}

	public void undeployCustomModel(CustomModel model, OperationContext context) throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, NoDataExtractedException,EcmEngineException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::undeployCustomModel] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.undeployCustomModel(
					(it.doqui.index.ecmengine.dto.backoffice.model.CustomModel)convertDTO(model, it.doqui.index.ecmengine.dto.backoffice.model.CustomModel.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::undeployCustomModel] END");
		}
	}

	public void updateAcl(Node node, AclRecord[] acls, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::updateAcl] BEGIN");
    	try {
			this.ecmEngineBackofficeDelegate.updateAcl(
					(it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
					(it.doqui.index.ecmengine.dto.AclRecord[])convertDTOArray(acls, it.doqui.index.ecmengine.dto.AclRecord.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::updateAcl] END");
		}
	}

	public void updateUserMetadata(User utente, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::updateUserMetadata] BEGIN");
    	try {
    		this.ecmEngineBackofficeDelegate.updateUserMetadata(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(utente, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::updateUserMetadata] END");
		}
	}

	public void updateUserPassword(User utente, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException, EcmEngineTransactionException, PermissionDeniedException, Exception {
    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::updateUserPassword] BEGIN");
    	try {
    		this.ecmEngineBackofficeDelegate.updateUserPassword(
					(it.doqui.index.ecmengine.dto.backoffice.User)convertDTO(utente, it.doqui.index.ecmengine.dto.backoffice.User.class),
					(it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
				);
		} catch (Exception e) {
			handleException(e);
		} finally {
	    	log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::updateUserPassword] END");
		}
	}

	public boolean testResources()throws Exception{
        log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::testResources] BEGIN");
        boolean result = false;
        try {
        	result=this.ecmEngineBackofficeDelegate.testResources();
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceBackofficeDelegateImpl::testResources] END");
        }
        return result;		
	}
}

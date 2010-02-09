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

package it.doqui.index.ecmengine.client.webservices;

import static it.doqui.index.ecmengine.client.webservices.util.EcmEngineWebServiceConstants.ECMENGINE_WEB_SERVICE_LOG_CATEGORY;
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
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.CheckInCheckOutException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.MoveException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.ReadException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.TransformException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.UnsupportedTransformationException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.WorkflowException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.search.TooManyResultsException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

public abstract class AbstractWebServiceDelegateBase {

	protected static Log log = LogFactory.getLog(ECMENGINE_WEB_SERVICE_LOG_CATEGORY);

	@SuppressWarnings("unchecked")
	protected Object convertDTO(Object sourceDTO, Class destDTOClass) throws Exception {
	    if (log.isDebugEnabled()) {
            log.debug("["+getClass().getSimpleName()+"::convertDTO] BEGIN");
        }

		Object destDTO = null;
		try {
			if (sourceDTO != null) {
        	    if (log.isDebugEnabled()) {
		    	    log.debug("["+getClass().getSimpleName()+"::convertDTO] converting DTO from type "+sourceDTO.getClass().getName()+" to type "+destDTOClass.getName());
                }
				destDTO = BeanUtils.instantiateClass(destDTOClass);
				PropertyDescriptor[] targetpds = BeanUtils.getPropertyDescriptors(destDTOClass);
				PropertyDescriptor sourcepd = null;
        	    if (log.isDebugEnabled()) {
				    log.debug(" found "+targetpds.length+" properties for type "+destDTOClass.getName());
    	        }

				for (int i=0; i<targetpds.length; i++) {
					if (targetpds[i].getWriteMethod() != null) {
						Method writeMethod = targetpds[i].getWriteMethod();
						sourcepd = BeanUtils.getPropertyDescriptor(sourceDTO.getClass(), targetpds[i].getName());
						if (sourcepd != null && sourcepd.getReadMethod() != null) {
                    	    if (log.isDebugEnabled()) {
			    				log.debug("["+getClass().getSimpleName()+"::convertDTO] found property: "+targetpds[i].getName());
				    			log.debug("["+getClass().getSimpleName()+"::convertDTO] source type: "+sourcepd.getPropertyType().getName()+", dest type: "+targetpds[i].getPropertyType().getName());
    	                    }
							Method readMethod = sourcepd.getReadMethod();
							Object valueObject = null;
							if (!BeanUtils.isSimpleProperty(targetpds[i].getPropertyType())) {
								if (sourcepd.getPropertyType().isArray()) {
									valueObject = convertDTOArray((Object[])readMethod.invoke(sourceDTO, new Object[]{}), targetpds[i].getPropertyType().getComponentType());
								} else if (sourcepd.getPropertyType().equals(java.util.Calendar.class) && targetpds[i].getPropertyType().equals(java.util.Date.class)) {
									// if java.util.Calendar => convert to java.util.Date
									valueObject = readMethod.invoke(sourceDTO, new Object[0]);
									if (valueObject != null) {
										valueObject = ((Calendar)valueObject).getTime();
									}
								} else if (sourcepd.getPropertyType().equals(java.util.Date.class) && targetpds[i].getPropertyType().equals(java.util.Calendar.class)) {
									// if java.util.Date => convert to java.util.Calendar
									Calendar calendar = Calendar.getInstance();
									valueObject = readMethod.invoke(sourceDTO, new Object[0]);
									if (valueObject != null) {
										calendar.setTime((Date)valueObject);
										valueObject = calendar;
									}
								} else {
									valueObject = convertDTO(readMethod.invoke(sourceDTO, new Object[0]), targetpds[i].getPropertyType());
								}
							} else {
								valueObject = readMethod.invoke(sourceDTO, new Object[0]);
							}
                    	    if (log.isDebugEnabled()) {
    							log.debug("["+getClass().getSimpleName()+"::convertDTO] writing value: "+valueObject);
    	                    }
							writeMethod.invoke(destDTO, new Object[]{ valueObject });
						} else {
                    	    if (log.isDebugEnabled()) {
    							log.debug("["+getClass().getSimpleName()+"::convertDTO] skipping property: "+targetpds[i].getName());
    	                    }
						}
					}
				}
			}
		} catch(Exception e) {
	    	log.error("["+getClass().getSimpleName()+"::convertDTO] ERROR", e);
	    	throw e;
		} finally {
            if (log.isDebugEnabled()) {
	        	log.debug("["+getClass().getSimpleName()+"::convertDTO] END");
            }
		}
		return destDTO;
	}

	@SuppressWarnings("unchecked")
	protected Object[] convertDTOArray(Object[] sourceDTOArray, Class destDTOClass) throws Exception {
        if (log.isDebugEnabled()) {
        	log.debug("["+getClass().getSimpleName()+"::convertDTOArray] BEGIN");
        }
		Object[] destDTOArray = null;
		try {
			if (sourceDTOArray != null) {
                if (log.isDebugEnabled()) {
		        	log.debug("["+getClass().getSimpleName()+"::convertDTOArray] converting array with "+sourceDTOArray.length+" elements");
                }
				destDTOArray = (Object[])Array.newInstance(destDTOClass, sourceDTOArray.length);
				for (int i=0; i<sourceDTOArray.length; i++) {
					destDTOArray[i] = convertDTO(sourceDTOArray[i], destDTOClass);
				}
			}
		} catch(Exception e) {
	    	log.debug("["+getClass().getSimpleName()+"::convertDTOArray] ERROR", e);
	    	throw e;
		} finally {
            if (log.isDebugEnabled()) {
	        	log.debug("["+getClass().getSimpleName()+"::convertDTOArray] END");
            }
		}
		return destDTOArray;
	}

	protected void handleException(Exception e) throws EcmEngineException {
        if (log.isDebugEnabled()) {
        	log.debug("["+getClass().getSimpleName()+"::handleException] BEGIN");
    	    log.debug("["+getClass().getSimpleName()+"::handleException] caught: "+e.getClass().getName());
        }

    	try {
			if (e instanceof it.doqui.index.ecmengine.exception.InvalidParameterException) {
				throw new InvalidParameterException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException) {
				throw new InvalidCredentialsException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException) {
				throw new NoDataExtractedException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException) {
				throw new NoSuchNodeException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException) {
				throw new PermissionDeniedException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException) {
				throw new EcmEngineTransactionException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.AuditTrailException) {
				throw new AuditTrailException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.CheckInCheckOutException) {
				throw new CheckInCheckOutException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException) {
				throw new DeleteException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException) {
				throw new InsertException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.MoveException) {
				throw new MoveException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException) {
				throw new ReadException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.TransformException) {
				throw new TransformException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.UnsupportedTransformationException) {
				throw new UnsupportedTransformationException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException) {
				throw new UpdateException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.management.WorkflowException) {
				throw new WorkflowException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.AclEditException) {
				throw new AclEditException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.GroupAlreadyExistsException) {
				throw new GroupAlreadyExistsException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.GroupCreateException) {
				throw new GroupCreateException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.GroupDeleteException) {
				throw new GroupDeleteException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.GroupEditException) {
				throw new GroupEditException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException) {
				throw new NoSuchGroupException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchUserException) {
				throw new NoSuchUserException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.TooManyNodesException) {
				throw new TooManyNodesException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.UserAlreadyExistsException) {
				throw new UserAlreadyExistsException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.UserCreateException) {
				throw new UserCreateException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.UserDeleteException) {
				throw new UserDeleteException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.backoffice.UserUpdateException) {
				throw new UserUpdateException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.search.SearchException) {
				throw new SearchException(e.getMessage());
			} else if (e instanceof it.doqui.index.ecmengine.exception.publishing.engine.search.TooManyResultsException) {
				throw new TooManyResultsException(e.getMessage());
			} else {
				throw new EcmEngineException(e.getMessage());
			}
    	} catch(EcmEngineException te) {
        	log.debug("["+getClass().getSimpleName()+"::handleException] thrown: "+te.getClass().getName());
    		throw te;
    	} finally {
            if (log.isDebugEnabled()) {
            	log.debug("["+getClass().getSimpleName()+"::handleException] END");
    	    }
    	}
	}

}

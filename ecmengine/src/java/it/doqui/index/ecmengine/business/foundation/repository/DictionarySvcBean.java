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

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationBeansConstants;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

public class DictionarySvcBean extends EcmEngineWrapperBean implements EcmEngineConstants, FoundationBeansConstants {

	private static final long serialVersionUID = -2804503127551746089L;

	public QName[] getAllModels() throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getAllModels] BEGIN");
		Collection <QName> modelDefinitions = null;

		try {
			modelDefinitions = serviceRegistry.getDictionaryService().getAllModels();

    	  	if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getAllModels] found "+modelDefinitions.size()+" models");
		    }

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getAllModels] " +
					"Error retrieving models: " + e.getMessage());
			handleDictionaryServiceException("getAllModels", e);
		} finally {
			logger.debug("[DictionarySvcBean::getAllModels] END");
		}

		return modelDefinitions.toArray(new QName[]{});
	}

	public ModelDefinition getModelByName(QName modelQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getModelByName] BEGIN");
		ModelDefinition result = null;

		try {
    	  	if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::getModelByName] " +
					"Retrieving model: " + modelQName);
		    }

			result = serviceRegistry.getDictionaryService().getModel(modelQName);

    	  	if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getModelByName] found model: " + (result != null ? result.getName() : null));
		    }

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getModelByName] " +
					"Error retrieving model \"" + modelQName + "\": " + e.getMessage());
			handleDictionaryServiceException("getModelByName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getModelByName] END");
		}

		return result;
	}

	public PropertyDefinition getProperty(QName propertyName) throws DictionaryRuntimeException	{
		logger.debug("[DictionarySvcBean::getProperty] BEGIN");

		PropertyDefinition propDef = null;
		try{
			propDef = serviceRegistry.getDictionaryService().getProperty(propertyName);
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getProperty] " +
					"Error retrieving property : " + e.getMessage());
			handleDictionaryServiceException("getProperty", e);
		} finally {
			logger.debug("[DictionarySvcBean::getProperty] END");
		}
		return propDef;
	}

	public TypeDefinition[] getTypesByModelName(QName modelQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getTypesByModelName] BEGIN");

		final Collection<QName> types;
		List<TypeDefinition> result = new Vector<TypeDefinition>();
		try {
    	  	if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::getTypesByModelName] " +
					"Retrieving types for model: " + modelQName);
			}

			types = serviceRegistry.getDictionaryService().getTypes(modelQName);

    	  	if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getTypesByModelName] " + types.size() + " type(s) found.");
			}

			for (QName type : types) {
				result.add(serviceRegistry.getDictionaryService().getType(type));
			}
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getTypesByModelName] " +
					"Error retrieving types for model \"" + modelQName + "\": " + e.getMessage());
			handleDictionaryServiceException("getTypesByModelName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getTypesByModelName] END");
		}

		return result.toArray(new TypeDefinition[]{});
	}

	public AspectDefinition[] getAspectsByModelName(QName modelQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getAspectsByModelName] BEGIN");

		List<AspectDefinition> result = new Vector<AspectDefinition>();
		try {
    	  	if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getAspectsByModelName] " +
					"Retrieving aspects for model: " + modelQName +
					" (M: " + modelQName.toPrefixString(serviceRegistry.getNamespaceService()) + ")");
			}

			final Collection<QName> aspects = serviceRegistry.getDictionaryService().getAspects(modelQName);

    	  	if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getAspectsByModelName] " + aspects.size() + " aspects found.");
			}

			for (QName aspect : aspects) {
				result.add(serviceRegistry.getDictionaryService().getAspect(aspect));
			}
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getAspectsByModelName] " +
					"Error retrieving aspects for model \"" + modelQName + ": " + e.getMessage());
			handleDictionaryServiceException("getAspectsByModelName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getAspectsByModelName] END");
		}

		return result.toArray(new AspectDefinition[]{});
	}

	public TypeDefinition getType(QName sourceType) throws DictionaryRuntimeException {
		TypeDefinition type = null;

		try {
    		if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::getType] " +
					"Retrieving type for qname : " + sourceType.toString());
    		}

			type = serviceRegistry.getDictionaryService().getType(sourceType);

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getType] " +
					"Error retrieving type for qname  \"" + sourceType.toString() + "\": " + e.getMessage());
			handleDictionaryServiceException("getType", e);
		} finally {
			logger.debug("[DictionarySvcBean::getType] END");
		}
		return type;
	}

	public AspectDefinition getAspect(QName sourceAspect) throws DictionaryRuntimeException {
		AspectDefinition aspect = null;

		try {
    		if (logger.isDebugEnabled()) {
    			logger.debug("[DictionarySvcBean::getAspect] " +
					"Retrieving aspect for qname : " + sourceAspect.toString());
	    	}

			aspect = serviceRegistry.getDictionaryService().getAspect(sourceAspect);

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getAspect] " +
					"Error retrieving aspect for qname  \"" + sourceAspect.toString() + "\": " + e.getMessage());
			handleDictionaryServiceException("getAspect", e);
		} finally {
			logger.debug("[DictionarySvcBean::getAspect] END");
		}
		return aspect;
	}

	public PropertyDefinition[] getPropertiesByTypeName(QName typeQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getPropertiesByTypeName] BEGIN");

		List<PropertyDefinition> properties = new Vector<PropertyDefinition>();
		try {
    		if (logger.isDebugEnabled()) {
    			logger.debug("[DictionarySvcBean::getPropertiesByTypeName] " +
					"Retrieving properties for type: " + typeQName +
					" (T: " + typeQName.toPrefixString(serviceRegistry.getNamespaceService()) + ")");
			}

			final TypeDefinition type = serviceRegistry.getDictionaryService().getType(typeQName);
			if (type == null) {
				return null;
			}

    		final Collection<QName> propertyKeys = type.getProperties().keySet();

    		if (logger.isDebugEnabled()) {
	    		logger.debug("[DictionarySvcBean::getPropertiesByTypeName] " + propertyKeys.size() + " properties found.");
			}

			for (QName property: propertyKeys) {
				properties.add(type.getProperties().get(property));
			}

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getPropertiesByTypeName] " +
					"Error retrieving properties for type \"" + typeQName + ": " + e.getMessage());
			handleDictionaryServiceException("getPropertiesByTypeName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getPropertiesByTypeName] END");
		}

		return properties.toArray(new PropertyDefinition[]{});
	}

	public AspectDefinition[] getAspectsByTypeName(QName typeQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getAspectsByTypeName] BEGIN");

		List<AspectDefinition> aspects = new Vector<AspectDefinition>();
		try {
    		if (logger.isDebugEnabled()) {
    			logger.debug("[DictionarySvcBean::getAspectsByTypeName] " +
					"Retrieving aspects for type: " + typeQName +
					" (T: " + typeQName.toPrefixString(serviceRegistry.getNamespaceService()) + ")");
			}

			final TypeDefinition type = serviceRegistry.getDictionaryService().getType(typeQName);

			if (type == null) {
				return null;
			}

			aspects = type.getDefaultAspects();

    		if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getAspectsByTypeName] " + aspects.size() + " aspects found.");
			}
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getAspectsByTypeName] " +
					"Error retrieving aspects for type \"" + typeQName + ": " + e.getMessage());
			handleDictionaryServiceException("getAspectsByTypeName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getAspectsByTypeName] END");
		}

		return aspects.toArray(new AspectDefinition[]{});
	}

	public PropertyDefinition[] getPropertiesByAspectName(QName aspectQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getPropertiesByAspectName] BEGIN");

		List<PropertyDefinition> properties = new Vector<PropertyDefinition>();
		try {
    		if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::getPropertiesByAspectName] " +
					"Retrieving properties for aspect: " + aspectQName +
					" (A: " + aspectQName.toPrefixString(serviceRegistry.getNamespaceService()) + ")");
			}

			final AspectDefinition aspect = serviceRegistry.getDictionaryService().getAspect(aspectQName);

			if (aspect == null) {
				return null;
			}

    		final Collection<QName> propertyKeys = aspect.getProperties().keySet();

    		if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::getPropertiesByAspectName] " + propertyKeys.size() + " properties found.");
			}

			for (QName property: propertyKeys) {
				properties.add(aspect.getProperties().get(property));
			}

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getPropertiesByAspectName] " +
					"Error retrieving properties for type \"" + aspectQName + ": " + e.getMessage());
			handleDictionaryServiceException("getPropertiesByAspectName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getPropertiesByAspectName] END");
		}

		return properties.toArray(new PropertyDefinition[]{});
	}

	public AssociationDefinition[] getAssociationsByTypeName(QName typeQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getAssociationsByTypeName] BEGIN");

		final Collection<QName> associationKeys;
		List<AssociationDefinition> associations = new Vector<AssociationDefinition>();
		try {
    		if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getAssociationsByTypeName] " +
					"Retrieving associations for type: " + typeQName +
					" (T: " + typeQName.toPrefixString(serviceRegistry.getNamespaceService()) + ")");
			}

			final TypeDefinition type = serviceRegistry.getDictionaryService().getType(typeQName);

			if (type == null) {
				return null;
			}

			associationKeys = type.getAssociations().keySet();

    		if (logger.isDebugEnabled()) {
    			logger.debug("[DictionarySvcBean::getAssociationsByTypeName] " +
					associationKeys.size() + " child association(s) found.");
			}

			for (QName association : associationKeys) {
				associations.add(type.getAssociations().get(association));
			}

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getAssociationsByTypeName] " +
					"Error retrieving child associations for type \"" + typeQName + ": " + e.getMessage());
			handleDictionaryServiceException("getAssociationsByTypeName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getAssociationsByTypeName] END");
		}

		return associations.toArray(new AssociationDefinition[]{});
	}

	public ChildAssociationDefinition[] getChildAssociationsByTypeName(QName typeQName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::getChildAssociationsByTypeName] BEGIN");

		final Collection<QName> childAssociationKeys;
		List<ChildAssociationDefinition> childAssociations = new Vector<ChildAssociationDefinition>();
		try {
    		if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::getChildAssociationsByTypeName] " +
					"Retrieving child associations for type: " + typeQName +
					" (T: " + typeQName.toPrefixString(serviceRegistry.getNamespaceService()) + ")");
			}

			final TypeDefinition type = serviceRegistry.getDictionaryService().getType(typeQName);

			if (type == null) {
				return null;
			}

			childAssociationKeys = type.getChildAssociations().keySet();

    		if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::getChildAssociationsByTypeName] " +
					childAssociations.size() + " child association(s) found.");
			}

			for (QName childAssociation : childAssociationKeys) {
				childAssociations.add(type.getChildAssociations().get(childAssociation));
			}

		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::getChildAssociationsByTypeName] " +
					"Error retrieving child associations for type \"" + typeQName + ": " + e.getMessage());
			handleDictionaryServiceException("getChildAssociationsByTypeName", e);
		} finally {
			logger.debug("[DictionarySvcBean::getChildAssociationsByTypeName] END");
		}

		return childAssociations.toArray(new ChildAssociationDefinition[]{});
	}

	// Metodi di traduzione

	public String resolvePathToPrefixNameString(Path path) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::resolvePathToPrefixNameString] BEGIN");
		String result = null;

		try {
			// Decodifichiamo la stringa per ripristinare i caratteri speciali
			result = ISO9075.decode(path.toPrefixString(serviceRegistry.getNamespaceService()));
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::resolveQNameToPrefixName] " +
					"Error resolving path \"" + path + "\" to prefixed name string: " + e.getMessage());
			handleDictionaryServiceException("resolvePrefixNameToQName", e);
		} finally {
			logger.debug("[DictionarySvcBean::resolvePathToPrefixNameString] END");
		}
		return result;
	}

	public String resolveQNameToPrefixName(QName qname) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::resolveQNameToPrefixName] BEGIN");

		String result = null;
		try {
    		if (logger.isDebugEnabled()) {
		    	logger.debug("[DictionarySvcBean::resolveQNameToPrefixName] Resolving to prefix name: " + qname.toString());
            }

			final String [] nameParts = QName.splitPrefixedQName(qname.toPrefixString(serviceRegistry.getNamespaceService()));

			result = nameParts[0] + DictionarySvc.PREFIXED_NAME_SEPARATOR + nameParts[1];

    		if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::resolveQNameToPrefixName] Prefix name: " + result);
            }
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::resolveQNameToPrefixName] " +
					"Error resolving to prefix name \"" + qname.toString() + "\": " + e.getMessage());
			handleDictionaryServiceException("resolvePrefixNameToQName", e);
		} finally {
			logger.debug("[DictionarySvcBean::resolveQNameToPrefixName] END");
		}

		return result;
	}

	public QName resolvePrefixNameToQName(String prefixName) throws DictionaryRuntimeException {
		logger.debug("[DictionarySvcBean::resolvePrefixNameToQName] BEGIN");

		QName result = null;
		String [] nameParts = QName.splitPrefixedQName(prefixName);
		try {
    		if (logger.isDebugEnabled()) {
			    logger.debug("[DictionarySvcBean::resolvePrefixNameToQName] Resolving to QName: " + prefixName);
	    	}

			result = QName.createQName(nameParts[0], nameParts[1], serviceRegistry.getNamespaceService());

    		if (logger.isDebugEnabled()) {
    			logger.debug("[DictionarySvcBean::resolvePrefixNameToQName] QName: " + result.toString());
    		}
		} catch (Exception e) {
			logger.warn("[DictionarySvcBean::resolvePrefixNameToQName] " +
					"Error resolving to QName \"" + prefixName + "\": " + e.getMessage());
			handleDictionaryServiceException("resolvePrefixNameToQName", e);
		} finally {
			logger.debug("[DictionarySvcBean::resolvePrefixNameToQName] END");
		}

		return result;
	}

	private void handleDictionaryServiceException(String methodName, Throwable e) throws DictionaryRuntimeException {
		logger.warn("[DictionarySvcBean::handleDictionaryServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		throw new DictionaryRuntimeException(FoundationErrorCodes.GENERIC_DICTIONARY_SERVICE_ERROR);
	}
}

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

package it.doqui.index.ecmengine.business.foundation.category;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.category.CategoryRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

public class CategorySvcBean extends EcmEngineWrapperBean implements EcmEngineConstants{

	/**
	 *
	 */
	private static final long serialVersionUID = 0L;

	public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth)
	throws CategoryRuntimeException{

		Collection<ChildAssociationRef> result = null;
		logger.debug("[CategorySvcBean::getChildren] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.getChildren(categoryRef, mode, depth);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::getChildren] Error executing getChildren: " + e.getMessage());
			handleCategoryServiceException("getChildren", e);
		} finally {
			logger.debug("[CategorySvcBean::getChildren] END");
		}
		return result;
	}

	public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth)
	throws CategoryRuntimeException{

		Collection<ChildAssociationRef> result = null;
		logger.debug("[CategorySvcBean::getCategories] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.getCategories(storeRef, aspectQName, depth);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::getCategories] Error executing getCategories: " + e.getMessage());
			handleCategoryServiceException("getCategories", e);
		} finally {
			logger.debug("[CategorySvcBean::getCategories] END");
		}
		return result;
	}

	public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef) throws CategoryRuntimeException{

		Collection<ChildAssociationRef> result = null;
		logger.debug("[CategorySvcBean::getClassifications] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.getClassifications(storeRef);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::getClassifications] Error executing getClassifications: " + e.getMessage());
			handleCategoryServiceException("getClassifications", e);
		} finally {
			logger.debug("[CategorySvcBean::getClassifications] END");
		}
		return result;
	}

	public Collection<QName> getClassificationAspects() throws CategoryRuntimeException{

		Collection<QName> result = null;
		logger.debug("[CategorySvcBean::getClassificationAspects] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.getClassificationAspects();
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::getClassificationAspects] Error executing getClassificationAspects: " + e.getMessage());
			handleCategoryServiceException("getClassificationAspects", e);
		} finally {
			logger.debug("[CategorySvcBean::getClassificationAspects] END");
		}
		return result;
	}

	public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName) throws CategoryRuntimeException{

		Collection<ChildAssociationRef> result = null;
		logger.debug("[CategorySvcBean::getRootCategories] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.getRootCategories(storeRef, aspectName);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::getRootCategories] Error executing getRootCategories: " + e.getMessage());
			handleCategoryServiceException("getRootCategories", e);
		} finally {
			logger.debug("[CategorySvcBean::getRootCategories] END");
		}
		return result;
	}

	public NodeRef createCategory(NodeRef parent, String name) throws CategoryRuntimeException{
		NodeRef result = null;
		logger.debug("[CategorySvcBean::createCategory] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.createCategory(parent,name);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::createCategory] Error executing createCategory: " + e.getMessage());
			handleCategoryServiceException("createCategory", e);
		} finally {
			logger.debug("[CategorySvcBean::createCategory] END");
		}
		return result;
	}

	public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name)
	throws CategoryRuntimeException{

		NodeRef result = null;
		logger.debug("[CategorySvcBean::createRootCategory] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.createRootCategory(storeRef, aspectName, name);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::createRootCategory] Error executing createRootCategory: " + e.getMessage());
			handleCategoryServiceException("createRootCategory", e);
		} finally {
			logger.debug("[CategorySvcBean::createRootCategory] END");
		}
		return result;
	}

	public void deleteCategory(NodeRef nodeRef) throws CategoryRuntimeException{

		logger.debug("[CategorySvcBean::deleteCategory] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			categoryService.deleteCategory(nodeRef);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::deleteCategory] Error executing deleteCategory: " + e.getMessage());
			handleCategoryServiceException("deleteCategory", e);
		} finally {
			logger.debug("[CategorySvcBean::deleteCategory] END");
		}
	}

	public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
	throws CategoryRuntimeException{

		List<Pair<NodeRef, Integer>> result = null;
		logger.debug("[CategorySvcBean::getTopCategories] BEGIN");
		try{
			CategoryService categoryService = serviceRegistry.getCategoryService();
			result = categoryService.getTopCategories(storeRef, aspectName, count);
		}catch (RuntimeException e) {
			logger.warn("[CategorySvcBean::getTopCategories] Error executing getTopCategories: " + e.getMessage());
			handleCategoryServiceException("getTopCategories", e);
		} finally {
			logger.debug("[CategorySvcBean::getTopCategories] END");
		}
		return result;
	}


	private void handleCategoryServiceException(String methodName, Throwable e) throws CategoryRuntimeException {
		logger.warn("[CategorySvcBean::handleCategoryServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		throw new CategoryRuntimeException(FoundationErrorCodes.GENERIC_CATEGORY_SERVICE_ERROR);
	}
}

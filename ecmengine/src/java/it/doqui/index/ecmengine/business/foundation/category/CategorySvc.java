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

import it.doqui.index.ecmengine.exception.category.CategoryRuntimeException;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;


/**
 * Interfaccia pubblica del servizio di category esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link CategorySvcBean}.
 *
 * <p>Tutti i metodi rimappano le {@code RuntimeException} ricevute in
 * {@link CategoryRuntimeException}.
 * </p>
 *
 * @author Doqui
 *
 * @see CategorySvcBean
 * @see CategoryRuntimeException
 */


public interface CategorySvc extends EJBLocalObject {

	public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth)
	throws CategoryRuntimeException;

	public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth)
	throws CategoryRuntimeException;

	public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef) throws CategoryRuntimeException;

	public Collection<QName> getClassificationAspects() throws CategoryRuntimeException;

	public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName) throws CategoryRuntimeException;

	public NodeRef createCategory(NodeRef parent, String name) throws CategoryRuntimeException;

	public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name)
	throws CategoryRuntimeException;

	public void deleteCategory(NodeRef nodeRef) throws CategoryRuntimeException;

	public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
	throws CategoryRuntimeException;


}

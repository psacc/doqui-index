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
 

package it.doqui.index.ecmengine.business.foundation.contentmanagement;


import it.doqui.index.ecmengine.exception.contentmanagement.FileFolderRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>Interfaccia pubblica del servizio di fileFolder esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe <code>{@link FileFolderSvcBean}</code>.
 * </p>
 * <p>Tutti i metodi esportati dal bean di FileFolderSvcBean rimappano le 
 * <code>RuntimeException</code> ricevute in 
 * <code>{@link FileFolderRuntimeException}</code>.
 * </p>
 * 
 * @author Doqui
 * 
 * @see FileFolderSvcBean
 * @see FileFolderRuntimeException
 */

public interface FileFolderSvc extends EJBLocalObject {
	
	
	public NodeRef searchSimple(NodeRef inNodeRef, String inName) throws FileFolderRuntimeException ;
	
	public FileInfo copy(NodeRef inSourceNodeRef, NodeRef inDestinationNodeRef, String inName) throws FileFolderRuntimeException;

}

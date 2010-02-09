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

import it.doqui.index.ecmengine.exception.contentmanagement.ActionRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interfaccia pubblica del servizio di esecuzione delle action esportato come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link ActionSvcBean}.
 * 
 * <p>Tutti i metodi esportati dal bean di esecuzione delle action rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link ActionRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see ActionSvcBean
 * @see ActionRuntimeException
 */
public interface ActionSvc extends EJBLocalObject {

	/**
	 * Crea una action identificata da {@code actionName}.
	 * 
	 * @param actionName Nome che identifica la action da creare.
	 * 
	 * @throws ActionRuntimeException Se si verifica un errore durante l'operazione.
	 */
	Action createAction(String actionName) throws ActionRuntimeException;

	/**
	 * Esegue la action specificata sul nodo passato in input.
	 * 
	 * @param action La action da eseguire.
	 * @param node Il nodo su cui eseguire la action.
	 * 
	 * @throws ActionRuntimeException Se si verifica un errore durante l'operazione.
	 */
	void executeAction(Action action, NodeRef node) throws ActionRuntimeException;

	/**
	 * Crea una nuova istanza di {@code CompositeAction}.
	 * 
	 * @throws ActionRuntimeException Se si verifica un errore durante l'operazione.
	 */
	CompositeAction createCompositeAction() throws ActionRuntimeException;

	/**
	 * Crea una action condition del tipo specificato.
	 * 
	 * @throws ActionRuntimeException Se si verifica un errore durante l'operazione.
	 */
	ActionCondition createActionCondition(String name) throws ActionRuntimeException;
}

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

import it.doqui.index.ecmengine.exception.contentmanagement.RuleRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;

/**
 * Interfaccia pubblica del servizio di gestione delle rule esportato come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link RuleSvcBean}.
 * 
 * <p>Tutti i metodi esportati dal bean di gestione delle rule rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link RuleRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see RuleSvcBean
 * @see RuleRuntimeException
 */
public interface RuleSvc extends EJBLocalObject {

	/**
	 * Salva una rule associata ad un nodo.
	 * 
	 * @param node
	 * @param rule
	 * 
	 * @throws RuleRuntimeException Se si verifica un errore durante l'operazione.
	 */
	void saveRule(NodeRef node, Rule rule) throws RuleRuntimeException;

}

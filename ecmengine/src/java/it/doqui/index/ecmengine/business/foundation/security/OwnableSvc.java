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
 
package it.doqui.index.ecmengine.business.foundation.security;

import it.doqui.index.ecmengine.exception.security.OwnableRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>Interfaccia pubblica del servizio di gestione delle ownership esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link OwnableSvcBean}.
 * </p>
 * <p>Tutti i metodi esportati dal bean di gestione delle ownership rimappano le 
 * {@code RuntimeException} ricevute in {@link OwnableRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see OwnableSvcBean
 * @see OwnableRuntimeException
 */

public interface OwnableSvc extends EJBLocalObject {
	
	/**
	 * Reperisce il proprietario del nodo specificato. Esso pu&ograve;
	 * essere l'owner (se al nodo &egrave; applicato l'aspect &quot;Ownable&quot;)
	 * oppure il creatore (se manca l'aspect &quotOwnable&quot; ed &egrave, quindi,
	 * presente l'aspect &quotAuditable&quot;).
	 * 
	 * @param nodeRef Il riferimento al nodo di cui si vuole sapere il proprietario.
	 * 
	 * @return Il proprietario, come oggetto NodeRef, del nodo specificato.
	 * 
	 * @throws OwnableRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getOwner(NodeRef nodeRef) throws OwnableRuntimeException;

	/**
	 * Verifica che a un nodo sia assegnato un proprietario. Utilizza internamente
	 * il metodo <code>{@link #getOwner(NodeRef)}</code>, quindi segue gli stessi criteri
	 * per la determinazione del proprietario.
	 * 
	 * @param nodeRef Il riferimento al nodo di cui si vuole verificare l'esistenza del
	 * proprietario.
	 * 
	 * @return <code>true</code> se il nodo specificato ha un proprietario, 
	 * <code>false</code> altrimenti.
	 * 
	 * @throws OwnableRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean hasOwner(NodeRef nodeRef) throws OwnableRuntimeException;

	/**
	 * Imposta il proprietario di un nodo. Se il nodo specificato non ha l'aspect
	 * &quot;Ownable&quot; settato, lo aggiunge prima di impostare il proprietario.
	 * 
	 * @param nodeRef Il nodo di cui si vuole modificare il proprietario.
	 * @param userName Il nome utente che identifica il nuovo proprietario del nodo.
	 * 
	 * @throws OwnableRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void setOwner(NodeRef nodeRef, String userName) throws OwnableRuntimeException;

	/**
	 * Imposta l'utente corrente come proprietario di un nodo.
	 * 
	 * @param nodeRef Il nodo di cui si vuole modificare il proprietario.
	 * 
	 * @throws OwnableRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void takeOwnership(NodeRef nodeRef) throws OwnableRuntimeException;
}

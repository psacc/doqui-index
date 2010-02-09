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


import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.exception.contentmanagement.NamespaceRuntimeException;

import java.util.Collection;

import javax.ejb.EJBLocalObject;

/**
 * <p>Interfaccia pubblica del servizio di gestione del namespace esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe <code>{@link NamespaceSvcBean}</code>.
 * </p>
 * <p>Tutti i metodi esportati dal bean di NamespaceSvcBean rimappano le 
 * <code>RuntimeException</code> ricevute in 
 * <code>{@link NamespaceRuntimeException}</code>.
 * </p>
 * 
 * @author Doqui
 * 
 * @see NamespaceSvcBean
 * @see NamespaceRuntimeException
 */

public interface NamespaceSvc extends EJBLocalObject {
	
	
	/**
	 * Permette di eliminare la registrazione di un namespace dal NamespaceService
	 * 
     * @param inNamespaceUri				Namespace che si vuol eliminare dalla registrazione
     * 
     * @throws NamespaceRuntimeException 	Se si verifica un errore durante l'esecuzione.
	 * 
	 */
	void unregisterNamespace(String inNamespaceUri) throws NamespaceRuntimeException;
	
	/**
	 * Permette di registrare un namespace sul servizio di Namespace
	 * 
	 * @param inPrefix						Prefisso con identificare il namespace
	 * @param inNamespaceUri				Uri del namespace da registrare
	 * @throws NamespaceRuntimeException	Se si verifica un errore durante l'esecuzione.
	 */
	void registerNamespace(String inPrefix, String inNamespaceUri) throws NamespaceRuntimeException;

	/**
	 * Operazione che restituisce l'elenco delle URI dei namespace registrati sul servizio di Namespace
	 * 
	 * @return								Oggetto {@link Collection<String>} contenente l'elenco degli URI dei namespace registrati
	 * 
	 * @throws 	NamespaceRuntimeException	Se si verifica un errore durante l'esecuzione.
	 */
	Collection<String> getUriList() throws NamespaceRuntimeException;
	
	/**
	 * Operazione che ricerca tutti i prefissi definiti per il namespace passato in input
	 * 
	 * @param inNamespace					Namespace di cui ricercare l'elenco dei prefissi
	 * 
	 * @return 								Oggetto {@link Collection<String>} contenente l'elenco dei prefissi registrati per il namespace 
	 * 										passato in input
	 * 
	 * @throws NamespaceRuntimeException	Se si verifica un errore durante l'esecuzione.
	 */
	Collection<String> getPrefixes(String inNamespace) throws NamespaceRuntimeException;
	
	
	/**
	 * Operazione che restituisce l'elenco dei prefissi dei namespace definiti sul NamespaceService
	 * 
	 * @return								Oggetto {@link Collection<String>} contenente l'elenco degli URI dei namespace registrati
	 * 
	 * @throws NamespaceRuntimeException	Se si verifica un errore durante l'esecuzione.
	 */
	Collection<String> getPrefixes() throws NamespaceRuntimeException;

	/**
	 * Operazione che restituisce l'URI del namespace dal prefisso passato in input
	 * 
	 * @param inPrefix 						prefisso di cui ricercare l'URI del namespace
	 * @return 								Oggetto {@link String} contenente l'URI del namespace ricercato 
	 * 
	 * @throws NamespaceRuntimeException	Se si verifica un errore durante l'esecuzione.
	 */
	String getNamespaceURI(String inPrefix) throws NamespaceRuntimeException;
}

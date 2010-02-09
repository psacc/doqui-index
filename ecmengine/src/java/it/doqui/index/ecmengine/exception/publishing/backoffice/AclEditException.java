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
 
package it.doqui.index.ecmengine.exception.publishing.backoffice;

import it.doqui.index.ecmengine.exception.EcmEngineException;

/**
 * Eccezione che rappresenta un errore verificatosi durante un'operazione di modifica
 * di una ACL.
 * 
 * <p>Questa eccezione pu&ograve; essere sollevata dal componente di
 * backoffice qualora un operazione di modifica di una ACL (per esempio: aggiunta o rimozione
 * di uno o pi&ugrave; record) fallisca a causa di un errore interno dell'ECMENGINE.</p>
 * 
 * @author Doqui
 */
public class AclEditException extends EcmEngineException {

	private static final long serialVersionUID = 682859939719784559L;
	
	/**
	 * Costruttore che crea una nuova istanza di {@code AclEditException}
	 * inizializzandola con un messaggio che specifica il nome del gruppo che ha
	 * causato il problema.
	 * 
	 * @param nodeId L'ID univoco del nodo la cui modifica ha evidenziato il problema.
	 */
	public AclEditException(String nodeId) {
		super("Errore nella modifica della ACL del nodo: " + nodeId);
	}
	
	/**
	 * Costruttore che crea una nuova istanza di {@code AclEditException}
	 * inizializzandola con un messaggio che specifica il nome del gruppo che ha
	 * causato il problema e con la causa dell'errore.
	 * 
	 * @param nodeId L'ID univoco del nodo la cui modifica ha evidenziato il problema.
	 * @param cause La causa dell'errore.
	 */
	public AclEditException(String nodeId, Throwable cause) {
		super("Errore nella modifica della ACL del nodo: " + nodeId);
		initCause(cause);
	}
}

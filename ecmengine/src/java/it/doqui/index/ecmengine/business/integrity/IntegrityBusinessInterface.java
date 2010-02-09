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

package it.doqui.index.ecmengine.business.integrity;

//import it.doqui.index.ecmengine.business.mimetype.dto.Mimetype;

import java.util.Map;
import java.util.Set;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.exception.repository.EcmEngineIntegrityException;

public interface IntegrityBusinessInterface {
	/**
	 * Restituisce una mappa rappresentante tutti i nodi trovati sul db associati al loro UID.
	 * @return Una mappa rappresentante tutti i nodi trovati sul db associati al loro UID.
	 * @throws EcmEngineIntegrityException Se si verifica un errore durante l'esecuzione del metodo.
	 */
	public Map<String,Node> getAllNodes() throws EcmEngineIntegrityException;
	
	/**
	 * Restituisce il DBID di un determinato nodo.
	 * @param node Il nodo di cui si vuole trovare il DBID associato.
	 * @return IL DBID del nodo.
	 * @throws EcmEngineIntegrityException Se si verifica un errore durante l'esecuzione del metodo.
	 */
	public Long getDBID(Node node) throws EcmEngineIntegrityException;
	
	/**
	 * Restituisce un mappa contenente insiemi di DBID associati al DBID padre.
	 * @return Un mappa contenente insiemi di DBID associati al DBID padre.
	 * @throws EcmEngineIntegrityException Se si verifica un errore durante l'esecuzione del metodo.
	 */
	public Map<Long,Set<Long>> getAllAssociations() throws EcmEngineIntegrityException;
	
	/**
	 * Restituisce una mappa contenente tutti gli UID dei nodi associati al loro DBID.
	 * @return Una mappa contenente tutti gli UID dei nodi associati al loro DBID.
	 * @throws EcmEngineIntegrityException Se si verifica un errore durante l'esecuzione del metodo.
	 */
	public Map<Long,String> getAllDBIDUID() throws EcmEngineIntegrityException;
}

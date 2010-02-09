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
 
package it.doqui.index.ecmengine.business.personalization.security;

import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.lucene.search.Query;

/**
 * Servizio che restituisce una lista delle authority che hanno accesso in lettura
 * a un determinato contenuto.
 * 
 * @author Doqui
 */
public interface ReadersService {
	
	/** Category Log4J. */
	String ECMENGINE_PERSONALIZATION_READERS = EcmEngineConstants.ECMENGINE_ROOT_LOG_CATEGORY + ".business.personalization.readers";
	
	/**
	 * Restituisce un insieme di nomi di authority che hanno accesso in lettura
	 * al nodo specificato.
	 * 
	 * @param nodeRef Il riferimento al nodo.
	 * 
	 * @return Un insieme di nomi di authority.
	 */
	Set<String> getReaders(NodeRef nodeRef);
	
	/**
	 * Restituisce un predicato Lucene per il filtraggio delle ricerche costruito sulla base delle
	 * authority associate all'utente corrente.
	 * 
	 * @return Un predicato &quot;AND&quot;.
	 */
	String getLuceneFilterPredicate();
	
	/**
	 * Restituisce un insieme di nomi di authority associate all'utente corrente.
	 * 
	 * <p>Ogni utente ha sempre almeno un'authority associata. Se il valore restituito
	 * &egrave; {@code null} l'utente corrente ha i privilegi amministrativi o di sistema.</p>
	 * 
	 * @return L'insieme delle authority associate all'utente corrente oppure {@code null}.
	 */
	Set<String> getAuthoritiesForCurrentUser();
	
	/**
	 * Restituisce una query Lucene per il filtraggio delle ricerche costruita sulla base delle authority
	 * associate all'utente corrente.
	 * 
	 * <p>La query restituita pu&ograve; essere aggiunta in AND ad una query di ricerca per ottenere il filtraggio
	 * dei risultati.</p>
	 * 
	 * @return Una query Lucene.
	 */
	Query getLuceneFilterQuery();
}

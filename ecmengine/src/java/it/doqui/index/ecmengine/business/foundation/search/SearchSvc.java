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
 
package it.doqui.index.ecmengine.business.foundation.search;

import java.util.List;

import it.doqui.index.ecmengine.exception.search.SearchRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;


/**
 * Interfaccia pubblica del servizio di ricerca esportata come
 * componente EJB 2. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link SearchSvcBean}.
 * 
 * <p>Tutti i metodi rimappano le {@code RuntimeException} ricevute in
 * {@link SearchRuntimeException}.
 * </p>
 *
 * @author Doqui
 *
 * @see SearchSvcBean
 * @see SearchRuntimeException
 */
public interface SearchSvc extends EJBLocalObject {

	/** Il linguaggio Lucene. */
	String LANGUAGE_LUCENE = "lucene";

	/** Il linguaggio XPath. */
	String LANGUAGE_XPATH = SearchService.LANGUAGE_XPATH;

	public List<NodeRef> selectNodes(NodeRef nodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks) throws SearchRuntimeException;
	
	public List<NodeRef> selectNodes(NodeRef nodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language) throws SearchRuntimeException;

	
	/**
	 * Esegue una ricerca sullo store specificato utilizzanto la query
	 * specificata.
	 *
	 * @param store Lo store su cui eseguire la ricerca.
	 * @param language Il linguaggio della query specificata.
	 * @param query La query da eseguire.
	 *
	 * @return Il {@code ResultSet} che contiene i risultati della ricerca.
	 * 
	 * @throws SearchRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	ResultSet query(StoreRef store, String language, String query) throws SearchRuntimeException;

	/**
	 * Effettua la query utilizzado i parametri di ricerca forniti.
	 *
	 * @param searchParameters parametri di ricerca.
	 *
	 * @return I risultati della ricerca in un oggetto {@code ResultSet}.
	 * 
	 * @throws SearchRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	ResultSet query(SearchParameters searchParameters) throws SearchRuntimeException;

	boolean contains(NodeRef nodeRef, String googleLikePattern) throws SearchRuntimeException;

	
	
	
	
}

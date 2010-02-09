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
 
package it.doqui.index.ecmengine.dto.engine.search;

import it.doqui.index.ecmengine.dto.EcmEngineDto;
import it.doqui.index.ecmengine.dto.Node;

/**
 * Data Transfer Object che rappresenta la response di una ricerca.
 * 
 * <p>I dati contenuti in questa classe sono:</p>
 * <ul>
 * 	<li>Il numero totale di risultati presenti.</li>
 * 	<li>La dimensione delle pagine della ricerca (nel caso di ricarca paginata).</li>
 * 	<li>L'indice della pagina richiesta (nel caso di ricarca paginata).</li>
 * 	<li>L'array dei risultati della ricerca.</li>
 * </ul>
 * <p>La ricerca restituita &egrave; paginata se e solo se {@code pageIndex} &gt;= 0 AND {@code pageSize} &gt; 0.</p>
 * 
 * @author Doqui
 *
 */
public class NodeResponse extends EcmEngineDto {

	private static final long serialVersionUID = -2949360590843206136L;

	private int totalResults;
	private int pageSize;
	private int pageIndex;
	private Node[] nodeArray;

	
	/**
	 * Costruttore predefinito.
	 */
	public NodeResponse() {
		super();
	}

	/**
	 * Restituisce l'indice della pagina della ricerca.
	 * 
	 * @return L'indice della pagina corrente.
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * Imposta l'indice della pagina della ricerca.
	 * 
	 * @param pageIndex
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * Restituisce la dimensione della pagina della ricerca.
	 * 
	 * @return La dimensione della pagina della ricerca.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Imposta la dimensione della pagina della ricerca.
	 * 
	 * @param pageSize La dimensione della pagina della ricerca.
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * Restituisce l'array dei risultati della ricerca.
	 * 
	 * @return L'array dei risultati della ricerca.
	 */
	public Node[] getNodeArray() {
		return nodeArray;
	}

	/**
	 * Imposta l'array dei risultati della ricerca.
	 * 
	 * @param nodeArray L'array dei risultati della ricerca.
	 */
	public void setNodeArray(Node[] nodeArray) {
		this.nodeArray = nodeArray;
	}

	/**
	 * Restituisce il numero totale di risultati.
	 * 
	 * @return Il numero totale di risultati.
	 */
	public int getTotalResults() {
		return totalResults;
	}

	/**
	 * Imposta il numero totale di risultati.
	 * 
	 * @param totalResults Il numero totale di risultati.
	 */
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}
}

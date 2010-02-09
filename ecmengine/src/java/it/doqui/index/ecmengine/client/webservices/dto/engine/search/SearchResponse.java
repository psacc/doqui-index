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

package it.doqui.index.ecmengine.client.webservices.dto.engine.search;

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
public class SearchResponse {

	private int totalResults;
	private int pageSize;
	private int pageIndex;
	private ResultContent[] resultContentArray;

	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public ResultContent[] getResultContentArray() {
		return resultContentArray;
	}
	public void setResultContentArray(ResultContent[] resultContentArray) {
		this.resultContentArray = resultContentArray;
	}
	public int getTotalResults() {
		return totalResults;
	}
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

}

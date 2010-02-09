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

package it.doqui.index.ecmengine.client.webservices.dto.engine;

public class NodeArchiveParams {



	private String typePrefixedName;
	private int pageSize;
	private int pageIndex;
	private int limit;
	private boolean typeAsAspect;

	/**
	 * Restituisce il nome completo di prefisso del tipo di contenuto
	 * da utilizzare come filtro di ricerca.
	 *
	 * @return Il nome completo di prefisso del tipo da utilizzare come filtro.
	 */
	public String getTypePrefixedName() {
		return typePrefixedName;
	}

	/**
	 * Imposta il nome completo di prefisso del tipo di contenuto
	 * da utilizzare come filtro di ricerca.
	 *
	 * @param typePrefixedName Il nome completo di prefisso del tipo da utilizzare come filtro.
	 */
	public void setTypePrefixedName(String typePrefixedName) {
		this.typePrefixedName = typePrefixedName;
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
	 * La ricerca viene paginata se e solo se {@code pageIndex} &gt;= 0 AND {@code pageSize} &gt; 0.
	 *
	 * @param pageSize La dimensione della pagina della ricerca.
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * Restituisce l'indice della pagina della ricerca.
	 *
	 * @return L'indice della pagina della ricerca.
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * Imposta l'indice della pagina della ricerca.
	 * La ricerca viene paginata se e solo se {@code pageIndex} &gt;= 0 AND {@code pageSize} &gt; 0.
	 *
	 * @param pageIndex L'indice della pagina della ricerca.
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * Restituisce il numero massimo di risultati da restituire al chiamante.
	 *
	 * @return Il numero massimo di risultati (0 significa &quot;illimitato&quot;).
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Imposta il numero massimo di risultati da restituire al chiamante.
	 *
	 * @param limit il numero massimo di risultati (0 significa &quot;illimitato&quot;).
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Restituisce il flag che indica se il valore specificato in {@link #typePrefixedName} deve+
	 * essere utilizzato come nome di aspect.
	 *
	 * @return {@code true} se il nome contenuto in {@link #typePrefixedName} deve essere utilizzato
	 * come nome di un aspect, {@code false} se deve essere utilizzato come nome di un tipo.
	 */
	public boolean isTypeAsAspect() {
		return typeAsAspect;
	}

	/**
	 * Imposta il flag che indica se il valore specificato in {@link #typePrefixedName} deve+
	 * essere utilizzato come nome di aspect.
	 *
	 * @param typeAsAspect {@code true} se il nome contenuto in {@link #typePrefixedName} deve essere utilizzato
	 * come nome di un aspect, {@code false} se deve essere utilizzato come nome di un tipo.
	 */
	public void setTypeAsAspect(boolean typeAsAspect) {
		this.typeAsAspect = typeAsAspect;
	}
}

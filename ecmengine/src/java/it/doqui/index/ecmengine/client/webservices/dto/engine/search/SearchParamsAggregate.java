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

import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Namespace;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Property;


public class SearchParamsAggregate {




	private boolean followAllParentLinks;
	private Namespace[] namespace = null;



	public boolean isFollowAllParentLinks() {
		return followAllParentLinks;
	}

	public void setFollowAllParentLinks(boolean followAllParentLinks) {
		this.followAllParentLinks = followAllParentLinks;
	}

	public Namespace[] getNamespace() {
		return namespace;
	}

	public void setNamespace(Namespace[] namespace) {
		this.namespace = namespace;
	}

    // SearchParams -----------------------------------------------------
	private String typePrefixedName;
	private String mimeType;
	private Property[] properties;
	private int limit;
	private String fullTextQuery;
	private boolean fullTextAllWords;
	private String xPathQuery;
	private String luceneQuery;
	private int pageSize;
	private int pageIndex;
	private SortField[] sortFields;

	public boolean isFullTextAllWords() {
		return fullTextAllWords;
	}

	public void setFullTextAllWords(boolean fullTextAllWords) {
		this.fullTextAllWords = fullTextAllWords;
	}

	public String getFullTextQuery() {
		return fullTextQuery;
	}

	public void setFullTextQuery(String fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getLuceneQuery() {
		return luceneQuery;
	}

	public void setLuceneQuery(String luceneQuery) {
		this.luceneQuery = luceneQuery;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

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

	public Property[] getProperties() {
		return properties;
	}

	public void setProperties(Property[] properties) {
		this.properties = properties;
	}

	public SortField[] getSortFields() {
		return sortFields;
	}

	public void setSortFields(SortField[] sortFields) {
		this.sortFields = sortFields;
	}

	public String getTypePrefixedName() {
		return typePrefixedName;
	}

	public void setTypePrefixedName(String typePrefixedName) {
		this.typePrefixedName = typePrefixedName;
	}

	public String getXPathQuery() {
		return xPathQuery;
	}

	public void setXPathQuery(String pathQuery) {
		xPathQuery = pathQuery;
	}
    // SearchParams -----------------------------------------------------
}

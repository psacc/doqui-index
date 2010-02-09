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
 
package it.doqui.index.ecmengine.business.foundation.util;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO interno contenente i parametri da passare al {@link QueryBuilder}
 * per la generazione della query.
 * 
 * @author DoQui
 */
public class QueryBuilderParams implements Serializable {

	private static final long serialVersionUID = 7082528747772541928L;

	private String contentType;
	private String mimeType;
	private Map<String, String> attributes;
	private String fullTextQuery;
	private boolean fullTextAllWords;
	
	/** Costruttore predefinito. */
	public QueryBuilderParams() {
		this.attributes = new LinkedHashMap<String, String>();
	}

	public void addAttribute(String name, String value) {
		this.attributes.put(name, value);
	}
	
	public Map<String, String> getAttributes() {
		return this.attributes;
	}
	
	public String getContentType() {
		return this.contentType;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFullTextQuery() {
		return this.fullTextQuery;
	}

	public void setFullTextQuery(String fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
	}

	public boolean isFullTextAllWords() {
		return this.fullTextAllWords;
	}

	public void setFullTextAllWords(boolean fullTextAllWords) {
		this.fullTextAllWords = fullTextAllWords;
	}
}

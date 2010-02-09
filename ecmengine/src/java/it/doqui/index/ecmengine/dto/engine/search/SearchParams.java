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
import it.doqui.index.ecmengine.dto.engine.management.Property;

/**
 * Data Transfer Object che rappresenta i parametri di ricerca utilizzati
 * per una ricerca generica globale all'interno del repository.
 *
 * <p>I parametri di ricerca che possono essere specificati mediante questa classe sono:</p>
 * <ul>
 * 	<li>Il tipo di contenuto che si sta cercando.</li>
 * 	<li>Il tipo MIME dei contenuti che si stanno cercando.</li>
 * 	<li>Una lista di metadati, anche vuota, con i relativi valori da cercare (i valori possono
 *      contenere un carattere '*' come wildcard, ma questo non deve apparire all'inizio della stringa
 *      di ricerca).</li>
 *  <li>La dimensione delle pagine (nel caso di ricerca paginata).</li>
 *  <li>L'indice della pagina da recuperare (nel caso di ricerca paginata).</li>
 * </ul>
 * <p>La ricerca viene paginata se e solo se {@code pageIndex} &gt;= 0 AND {@code pageSize} &gt; 0.</p>
 *
 * @author Doqui
 */
public class SearchParams extends EcmEngineDto {

	private static final long serialVersionUID = 1L;
	private String typePrefixedName;
	private String mimeType;
	private Property [] properties;
	private int limit;
	private String fullTextQuery;
	private boolean fullTextAllWords;
	private String xPathQuery;
	private String luceneQuery;
	private int pageSize;
	private int pageIndex;
	private SortField[] sortFields;
	private boolean fullProperty;

	/**
	 * Costruttore predefinito.
	 */
	public SearchParams() {
		super();
	}

	/**
	 * Restituisce il nome completo di prefisso del tipo di contenuto da cercare.
	 *
	 * @return Il nome completo di prefisso.
	 */
	public String getTypePrefixedName() {
		return typePrefixedName;
	}

	/**
	 * Imposta il nome completo di prefisso del tipo di contenuto da cercare.
	 *
	 * @param prefixedName Il nome completo di prefisso.
	 */
	public void setTypePrefixedName(String prefixedName) {
		this.typePrefixedName = prefixedName;
	}

	/**
	 * Restituisce il MIME-Type del contenuto da cercare.
	 *
	 * @return Il MIME-Type.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Imposta il MIME-Type del contenuto da cercare.
	 *
	 * @param mimeType Il MIME-Type.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Restituisce un insieme di metadati da utilizzare come parametri di ricerca.
	 *
	 * @return I metadati, come array di {@link Property}, da utilizzare come parametri di ricerca.
	 */
	public Property[] getProperties() {
		return properties;
	}

	/**
	 * Imposta un insieme di metadati da utilizzare come parametri di ricerca.
	 *
	 * @param properties Un array di {@link Property} contenente i metadati da utilizzare come
	 * parametri di ricerca.
	 */
	public void setProperties(Property[] properties) {
		this.properties = properties;
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
	 * Restituisce la stringa da usare per la ricerca full-text.
	 *
	 * @return La stringa di ricerca full-text.
	 */
	public String getFullTextQuery() {
		return fullTextQuery;
	}

	/**
	 * Imposta la stringa da usare per la ricerca full-text.
	 *
	 * @param fullTextQuery La stringa da usare per la ricerca full-text.
	 */
	public void setFullTextQuery(String fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
	}

	/**
	 * Restituisce la modalit&agrave; con cui eseguire le ricerche full-text.
	 *
	 * @return {@code true} se i risultati devono contenere tutte le parole chiave,
	 * {@code false} altimenti.
	 */
	public boolean isFullTextAllWords() {
		return fullTextAllWords;
	}

	/**
	 * Imposta la modalit&agrave; con cui eseguire le ricerche full-text.
	 *
	 * @param fullTextAllWords {@code true} per cercare tutte le parole chiave
	 * contemporaneamente, {@code false} altrimenti.
	 */
	public void setFullTextAllWords(boolean fullTextAllWords) {
		this.fullTextAllWords = fullTextAllWords;
	}

	/**
	 * Restituisce la stringa di recerca XPath con cui eseguire la ricerca.
	 *
	 * @return La stringa XPath.
	 */
	public String getXPathQuery() {
		return xPathQuery;
	}

	/**
	 * Imposta la stringa di ricerca XPath con cui eseguire la ricerca.
	 *
	 * @param pathQuery La stringa di ricerca XPath.
	 */
	public void setXPathQuery(String pathQuery) {
		xPathQuery = pathQuery;
	}

	/**
	 * Restituisce la stringa di ricerca Lucene con cui eseguire la ricerca.
	 *
	 * @return La stringa Lucene.
	 */
	public String getLuceneQuery() {
		return luceneQuery;
	}

	/**
	 * Imposta la stringa di ricerca Lucene con cui eseguire la ricerca.
	 *
	 * @param luceneQuery La stringa di ricerca Lucene.
	 */
	public void setLuceneQuery(String luceneQuery) {
		this.luceneQuery = luceneQuery;
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
	 * Restituisce i campi su cui eseguire l'ordinamento del risultato della ricerca.
	 *
	 * @return L'array degli oggetti {@code SortField}
	 */
	public SortField[] getSortFields() {
		return sortFields;
	}

	/**
	 * Imposta i campi su cui eseguire l'ordinamento del risultato della ricerca.
	 *
	 * @param sortFields L'array degli oggetti {@code SortField}
	 */
	public void setSortFields(SortField[] sortFields) {
		this.sortFields = sortFields;
	}

	/**
	 * Restituisce la modalit&agrave; con la quale sono prese le property dei singoli risultati
	 *
	 * @return {@code true} se le property non sono filtrate
	 * {@code false} altimenti.
	 */
	public boolean isFullProperty() {
		return fullProperty;
	}

	/**
	 * Imposta la modalit&agrave; con la quale sono prese le property dei singoli risultati
	 *
	 * @param fullProperty {@code true} per restituire tutte le property associate ai singoli
	 * nodi, {@code false} altrimenti.
	 */
	public void setFullProperty(boolean fullProperty) {
		this.fullProperty = fullProperty;
	}
}

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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

//import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.util.ISO9075;

/**
 * Classe che implementa la logica di costruzione delle query Lucene
 * a partire da un oggetto contenente tutti i parametri di ricerca.
 * 
 * <p>Questa classe non &egrave; istanziabile, ma fornisce un metodo
 * {@code static} che incapsula tutta la logica di costruzione della
 * stringa di ricerca.</p>
 * 
 * @author Doqui
 * 
 * @see ParametriRicerca
 *
 */
public class QueryBuilder {
	
	private enum QueryType { NORMAL, WILDCARD, RANGE };
	
    /**
     * Costruttore protected definito per nascondere il costruttore
     * pubblico di default poich&eacute; la classe non deve essere
     * istanziabile.
     */
	protected QueryBuilder() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Costruisce la query Lucene a partire dai parametri di ricerca
	 * passati in input.
	 * 
	 * @param params L'oggetto contenente i parametri di ricerca.
	 * 
	 * @return La query Lucene da utilizzare nella ricerca.
	 */
	public static String buildQuery(QueryBuilderParams params) {
		final StringBuilder query = new StringBuilder();
		String typeQuery = null;
		String mimeQuery = null;
		final StringBuilder fullTextQuery = new StringBuilder();
		final StringBuilder attributesQuery = new StringBuilder();
		final Vector<String> queryParts = new Vector<String>();
		final Map<String, String> attributes = params.getAttributes();
		final Iterator<String> attributesIter = params.getAttributes().keySet().iterator();
		
		if (params.getContentType() != null) {
			
			typeQuery = "TYPE:\"" + params.getContentType() + "\"";
			queryParts.add(typeQuery);
		}
		
		if (params.getMimeType() != null) {
			mimeQuery = attributePredicate("cm:content.mimetype", params.getMimeType());
			queryParts.add(mimeQuery);
		}
		
		if (params.getFullTextQuery() != null) {
			final String [] keywords = params.getFullTextQuery().split("\\s+"); 
			
			for (int i = 0; i < keywords.length; i++) {
				final String fullTextPart = "TEXT:\"" + keywords[i] + "\"";
				
				fullTextQuery.append(fullTextPart);
				if (i < (keywords.length - 1)) {
					fullTextQuery.append((params.isFullTextAllWords()) ? " AND " : " OR ");
				}
			}
			if (keywords.length > 1) {
				queryParts.add("(" + fullTextQuery.toString() + ")");
			} else if (keywords.length == 1) {
				queryParts.add(fullTextQuery.toString());
			}
		}
		
		while (attributesIter.hasNext()) {
			final String key = attributesIter.next();
			final boolean last = !attributesIter.hasNext();
			
			attributesQuery.append(attributePredicate(key, attributes.get(key)));
			
			if (!last) {
				attributesQuery.append(" AND ");
			}
		}
		
		if (attributesQuery.toString().length() > 0) {
			queryParts.add(attributesQuery.toString());
		}
		
		for (int i = 0; i < queryParts.size(); i++) {
			query.append(queryParts.elementAt(i));
			if (i < (queryParts.size() - 1)) {
				query.append(" AND ");
			}
		}
		
		return query.toString();
	}
	
	private static String attributePredicate(String prefixKey, String value) {
		final StringBuilder predicate = new StringBuilder();
		QueryType type = QueryType.NORMAL;
		
		final String trimmedValue = value.trim();
			
		// Se la ricerca contiene una wildcard '*' non devono esserci i doppi apici
		type = trimmedValue.contains("*") ? QueryType.WILDCARD : QueryType.NORMAL;
		type = (type == QueryType.NORMAL && (trimmedValue.charAt(0) == '[' || trimmedValue.charAt(0) == '{'))
				? QueryType.RANGE : type;
		
		predicate.append("@");
		predicate.append(escapePrefixQName(prefixKey));
		
		switch (type) {
		case NORMAL:
			predicate.append(":\"").append(escapeLuceneSpecialChars(trimmedValue, false)).append("\"");
			break;
		case WILDCARD:
			predicate.append(":").append(escapeLuceneSpecialChars(trimmedValue, true));
			break;
		case RANGE:
			predicate.append(":").append(escapeRangeQuery(trimmedValue));
			break;
		}
		
		return predicate.toString();
	}
	
	private static String escapePrefixQName(String input) {
		return input.replace(":", "\\:");
	}
	
	private static String escapeRangeQuery(String range) {
		final StringBuilder output = new StringBuilder();
		
		for (int i = 0; i < range.length(); i++) {
			final char c = range.charAt(i);
			
			if (c == '-') {
				output.append("\\-");
			} else {
				output.append(c);
			}
		}
		
		return output.toString();
	}
	
	private static String escapeLuceneSpecialChars(String input, boolean escapeSpace) {
		final StringBuilder output = new StringBuilder();
		
//		TODO: valutare escape utilizzando LuceneQueryParser
//		// Escape della stringa passata in input (esclusi i caratteri wildcard
//		return LuceneQueryParser.escape(input).replace("\\*", "*");
		
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			// These characters are part of the query syntax and must be escaped
			// Need to investigate which other characters need to be escaped.
			if (c == ',' 
				|| c == '(' 
				|| c == ')' 
				|| (escapeSpace && c == ' ')) {
				output.append(String.format("_x%04x_", new Integer(c)));
			} else {
				output.append(c);
			}
		}
		
		return output.toString();
	}
	
	/**
	 * Esegue l'escaping della stringa XPath specificata.
	 * 
	 * <p>L'escaping &egrave; effettuato scomponendo il path in elementi e codificando
	 * ogni elemento corrispondente ad un QName completo in formato ISO9075.</p>
	 * 
	 * @param input La stringa XPath su cui operare.
	 * 
	 * @return La stringa risultante dall'operazione di escaping.
	 */
	public static String escapeXPathQuery(String input) {
		final StringBuilder output = new StringBuilder();
		boolean hasNonEmptyElements = false;
		
		final String [] elements = input.trim().split("/");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].length() > 0) {
				hasNonEmptyElements = true;
				break;
			}
		}
		
		// Caso particolare: tutti elementi "vuoti"
		if (!hasNonEmptyElements) {
			return input;
		}
		
		int startIndex = (elements[0].length() == 0) ? 1 : 0;

		for (int i = startIndex; i < elements.length; i++) {
			if (elements[i].length() == 0
					|| elements[i].contains("*")) {
				output.append('/').append(elements[i]);
			} else if (elements[i].equals(".")
					|| elements[i].equals("..")) {
				if (output.length() != 0) {
					output.append('/');
				}
				output.append(elements[i]);
			} else if (!elements[i].contains(":") 
					&& !elements[i].equalsIgnoreCase("node()")) {
				// Default namespace
				output.append('/').append(ISO9075.encode(elements[i]));
			} else if (!elements[i].contains("::")) {
				final String [] parts = elements[i].split(":");
				
				output.append('/').append(parts[0]).append(":").append((parts.length > 1) ? ISO9075.encode(parts[1]) : "");
			} else {
				output.append('/').append(elements[i]);
			}
		}
		
		return output.toString();
	}
	
	/**
	 * Esegue l'escaping della stringa Lucene specificata.
	 * 
	 * <p>L'escaping &egrave; effettuato riconoscendo nella stringa i termini &quot;PATH&quot;
	 * e i termini &quot;QNAME&quot;: ai sono applicate le medesime regole valide
	 * per l'escaping XPath, i secondi vengono codificati in formato ISO9075.</p>
	 * 
	 * <p><strong>NB:</strong> questo metodo effettua un controllo di validit&agrave; sul numero
	 * di doppi apici trovati nella stringa. Gli eventuali errori sono segnalati sollevando una
	 * {@code IllegalArgumentException}.</p>
	 * 
	 * @param input La stringa Lucene su cui operare.
	 * 
	 * @return La stringa risultante dall'operazione di escaping.
	 * 
	 * @see #escapeXPathQuery(String)
	 */
	public static String escapeLuceneQuery(String input) {
		final String trimmedInput = input.trim();

		int quotesCount = countOccurrences("\"", input);
		
		if (quotesCount % 2 == 1) {
			throw new IllegalArgumentException("Uneven quotes in query: " + input);
		}
		
		String result = escapeLucenePathTermsInQuery(trimmedInput);
		result = escapeLuceneQNameTermsInQuery(result);
		
		return result;
	}
	
	private static String escapeLucenePathTermsInQuery(String input) {
		final StringBuilder output = new StringBuilder();
		
		int pathBegin = input.indexOf("PATH:\"");
		
		if (pathBegin == -1) {
			return input;
		}
		
		output.append(input.substring(0, pathBegin));
		
		while (pathBegin >= 0) {
			final int pathEnd = input.indexOf("\"", pathBegin + 6);
			
			if (pathEnd < 0) {
				throw new IllegalArgumentException("Underquoted PATH term.");
			}
			
			final String escapedPath = escapeXPathQuery(input.substring(pathBegin + 6, pathEnd));
			
			output.append(input.substring(pathBegin, pathBegin + 6)).append(escapedPath);

			pathBegin = input.indexOf("PATH:\"", pathEnd + 1);
			if (pathBegin == -1) {
				output.append(input.substring(pathEnd));
			} else {
				output.append(input.substring(pathEnd, pathBegin));
			}
		}
		
		return output.toString();
	}
	
	private static String escapeLuceneQNameTermsInQuery(String input) {
		final StringBuilder output = new StringBuilder();
		
		int qnameBegin = input.indexOf("QNAME:\"");
		
		if (qnameBegin == -1) {
			return input;
		}
		
		output.append(input.substring(0, qnameBegin));
		
		while (qnameBegin >= 0) {
			final int qnameEnd = input.indexOf("\"", qnameBegin + 7);
			
			if (qnameEnd < 0) {
				throw new IllegalArgumentException("Underquoted QNAME term.");
			}
			
			final String [] parts = input.substring(qnameBegin + 7, qnameEnd).split(":");
			final String escapedQName = parts[0] + ":" + ISO9075.encode(parts[1]);
			
			output.append(input.substring(qnameBegin, qnameBegin + 7)).append(escapedQName);

			qnameBegin = input.indexOf("QNAME:\"", qnameEnd + 1);
			if (qnameBegin == -1) {
				output.append(input.substring(qnameEnd));
			} else {
				output.append(input.substring(qnameEnd, qnameBegin));
			}
		}
		
		return output.toString();
	}
	
	private static int countOccurrences(String search, String input) {
		int count = 0;
		int len = input.length();
		int incr = search.length();
		int i = 0;
		
		while (i < len) {
			int index = input.indexOf(search, i);
			
			if (index >= 0) {
				count++;
				i = index + incr;
			} else {
				break;
			}
		}
		
		return count;
	}
}

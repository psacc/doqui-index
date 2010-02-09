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

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;

public class QueryBuilderTest extends TestCase {

	private QueryBuilderParams ricerca1;
	private QueryBuilderParams ricercaMime;
	private QueryBuilderParams ricercaStar;
	private QueryBuilderParams ricercaFT;
	private QueryBuilderParams ricercaRange;
	
	public QueryBuilderTest() {
	}

	public QueryBuilderTest(String name) {
		super(name);
	}

	public void setUp() {
		this.ricerca1 = new QueryBuilderParams();
		this.ricercaMime = new QueryBuilderParams();
		this.ricercaStar = new QueryBuilderParams();
		this.ricercaFT = new QueryBuilderParams();
		this.ricercaRange = new QueryBuilderParams();
	}
	
	public void tearDown() {
		this.ricerca1 = null;
		this.ricercaMime = null;
		this.ricercaStar = null;
		this.ricercaFT = null;
		this.ricercaRange = null;
	}
	
	public void testRicerca1() {
		String expected = "TYPE:\"{http://www.doqui.it/model/side/aggregation/1.0}fascicoloReale\" " +
				"AND @side-aggr\\:descrizione:\"descrizione 1\" " +
				"AND @side-aggr\\:num:\"123\"";
		
		ricerca1.setContentType("{http://www.doqui.it/model/side/aggregation/1.0}fascicoloReale");
		ricerca1.addAttribute("side-aggr:descrizione", "descrizione 1");
		ricerca1.addAttribute("side-aggr:num", "123");
		
		String query = QueryBuilder.buildQuery(ricerca1);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaMimetype() {		
		String expected = "@cm\\:content.mimetype:\"text/plain\"";
		
		ricercaMime.setMimeType("text/plain");
		
		String query = QueryBuilder.buildQuery(ricercaMime);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaStar() {
		String expected = "@side-doss\\:descrizione:a*";
		
		ricercaStar.addAttribute("side-doss:descrizione", "a*");
		
		String query = QueryBuilder.buildQuery(ricercaStar);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaFullTextOr() {
		String expected = "(TEXT:\"word1\" OR TEXT:\"word2\" OR TEXT:\"word3\")";
		
		ricercaFT.setFullTextQuery("word1 word2  word3 ");
		ricercaFT.setFullTextAllWords(false);
		
		String query = QueryBuilder.buildQuery(ricercaFT);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaFullTextAnd() {
		String expected = "(TEXT:\"word1\" AND TEXT:\"word2\" AND TEXT:\"word3\")";
		
		ricercaFT.setFullTextQuery("word1 word2  word3 ");
		ricercaFT.setFullTextAllWords(true);
		
		String query = QueryBuilder.buildQuery(ricercaFT);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaFullTextOneWord() {
		String expected = "TEXT:\"install\"";
		
		ricercaFT.setFullTextQuery("install");
		ricercaFT.setFullTextAllWords(true);
		
		String query = QueryBuilder.buildQuery(ricercaFT);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaTypeAndFullText() {
		String expected = "TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" " +
				"AND TEXT:\"install\"";
		
		ricercaFT.setContentType(ContentModel.TYPE_CONTENT.toString());
		ricercaFT.setFullTextQuery("install");
		ricercaFT.setFullTextAllWords(true);
		
		String query = QueryBuilder.buildQuery(ricercaFT);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaRange() {
		String expected = "@cm\\:created:[2003\\-12\\-16T00:00:00 TO 2003\\-12\\-17T00:00:00]";
		
		ricercaRange.addAttribute("cm:created", "[2003-12-16T00:00:00 TO 2003-12-17T00:00:00]");
		
		String query = QueryBuilder.buildQuery(ricercaRange);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testRicercaRange2() {
		String expected = "@cm\\:created:{2003\\-12\\-16T00:00:00 TO 2003\\-12\\-17T00:00:00}";
		
		ricercaRange.addAttribute("cm:created", "{2003-12-16T00:00:00 TO 2003-12-17T00:00:00}");
		
		String query = QueryBuilder.buildQuery(ricercaRange);
		System.out.println("Query: " + query);
		assertEquals(expected, query);
	}
	
	public void testEscapeXPathFirstNumber() {
		String input = "/app:company_home/cm:Cedolini/cm:Regione/cm:MATR_1000/cm:1000-1.pdf";
		String expected = "/app:company_home/cm:Cedolini/cm:Regione/cm:MATR_1000/cm:_x0031_000-1.pdf";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathTrailingSlash() {
		String input = "/app:company_home//cm:MATR_1000/";
		String expected = "/app:company_home//cm:MATR_1000";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathOnlySlash() {
		String input = "/";
		String expected = "/";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathOnlyDoubleSlash() {
		String input = "//";
		String expected = "//";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathPrefixNoLocalName() {
		String input = "/app:company_home/cm:TestIndex/cmd:";
		String expected = "/app:company_home/cm:TestIndex/cmd:";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathNoQName() {
		String input = "/app:company_home/test";
		String expected = "/app:company_home/test";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathOnlyDot() {
		String input = ".";
		String expected = ".";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathDepth() {
		String input = "/app:company_home//cm:MATR_1000/cm:1000-1.pdf";
		String expected = "/app:company_home//cm:MATR_1000/cm:_x0031_000-1.pdf";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathExact() {
		String input = "/app:company_home//cm:MATR_1000/cm:M1000-1.pdf";
		String expected = "/app:company_home//cm:MATR_1000/cm:M1000-1.pdf";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathStar() {
		String input = "/app:company_home//cm:MATR_1000/*";
		String expected = "/app:company_home//cm:MATR_1000/*";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathPrefix() {
		String input = "/app:company_home//cm:MATR_1000/cm:*";
		String expected = "/app:company_home//cm:MATR_1000/cm:*";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathNode() {
		String input = "/app:company_home//cm:MATR_1000/self::node()";
		String expected = "/app:company_home//cm:MATR_1000/self::node()";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathSpecial() {
		String input = ".//cm:MATR_1000/cm:M1000-1.pdf";
		String expected = ".//cm:MATR_1000/cm:M1000-1.pdf";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeXPathInnerSpecial() {
		String input = "/app:company_home//cm:MATR_1000/../cm:M1000-1.pdf";
		String expected = "/app:company_home//cm:MATR_1000/../cm:M1000-1.pdf";
		
		String escaped = QueryBuilder.escapeXPathQuery(input);
		System.out.println("XPath: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLucenePath() {
		String input = "PATH:\"/app:company_home/cm:Cedolini/cm:Regione//cm:1000-1.pdf\"";
		String expected = "PATH:\"/app:company_home/cm:Cedolini/cm:Regione//cm:_x0031_000-1.pdf\"";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneMorePaths() {
		String input = "PATH:\"/app:company_home/cm:Cedolini/cm:Regione//cm:1000-1.pdf\" OR PATH:\"/app:company_home/cm:321/*\"";
		String expected = "PATH:\"/app:company_home/cm:Cedolini/cm:Regione//cm:_x0031_000-1.pdf\" OR PATH:\"/app:company_home/cm:_x0033_21/*\"";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneQName() {
		String input = "QNAME:\"cm:1000-1.pdf\"";
		String expected = "QNAME:\"cm:_x0031_000-1.pdf\"";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneMoreQNames() {
		String input = "QNAME:\"cm:1000-1.pdf\" OR QNAME:\"cm:321\"";
		String expected = "QNAME:\"cm:_x0031_000-1.pdf\" OR QNAME:\"cm:_x0033_21\"";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneMix() {
		String input = "PATH:\"/app:company_home/cm:Cedolini//*\" AND QNAME:\"cm:1000-1.pdf\"";
		String expected = "PATH:\"/app:company_home/cm:Cedolini//*\" AND QNAME:\"cm:_x0031_000-1.pdf\"";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneMixIsNode() {
		String input = "PATH:\"/app:company_home/cm:Cedolini//*\" AND QNAME:\"cm:1000-1.pdf\" AND ISNODE:T";
		String expected = "PATH:\"/app:company_home/cm:Cedolini//*\" AND QNAME:\"cm:_x0031_000-1.pdf\" AND ISNODE:T";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneComplexNoEscape() {
		String input = "(@cedo\\:stato:2 OR @cedo\\:stato:3) AND " +
				"PATH:\"/app:company_home/app:user_homes/cm:cedolini/cm:cedolini_pdf_folder_R1//*\" AND @cmd\\:matricola:3186";
		String expected = "(@cedo\\:stato:2 OR @cedo\\:stato:3) AND " +
				"PATH:\"/app:company_home/app:user_homes/cm:cedolini/cm:cedolini_pdf_folder_R1//*\" AND @cmd\\:matricola:3186";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLuceneComplexNoEscape2() {
		String input = "(@cedo\\:stato:1 OR @cedo\\:stato:2 OR @cedo\\:stato:3) " +
				"AND PATH:\"/app:company_home/app:user_homes/cm:cedolini/cm:cedolini_pdf_folder_R1//*\" AND @cmd\\:anno:2007";
		String expected = "(@cedo\\:stato:1 OR @cedo\\:stato:2 OR @cedo\\:stato:3) " +
				"AND PATH:\"/app:company_home/app:user_homes/cm:cedolini/cm:cedolini_pdf_folder_R1//*\" AND @cmd\\:anno:2007";
		
		String escaped = QueryBuilder.escapeLuceneQuery(input);
		System.out.println("Lucene: " + escaped);
		assertEquals(expected, escaped);
	}
	
	public void testEscapeLucenePathOpenQuotes() {
		String input = "PATH:\"/app:company_home";
		
		try {
			String escaped = QueryBuilder.escapeLuceneQuery(input);
			System.out.println("Lucene: " + escaped);
			assertTrue(false);
		} catch (IllegalArgumentException iae) {
			System.out.println("Lucene: " + iae.getMessage());
			assertTrue(true);
		}
	}
	
	public void testEscapeLuceneQNameOpenQuotes() {
		String input = "QNAME:\"cm:1000-1.pdf";
		
		try {
			String escaped = QueryBuilder.escapeLuceneQuery(input);
			System.out.println("Lucene: " + escaped);
			assertTrue(false);
		} catch (IllegalArgumentException iae) {
			System.out.println("Lucene: " + iae.getMessage());
			assertTrue(true);
		}
	}
	
	public void testEscapeLuceneMixOpenQuotes() {
		String input = "QNAME:\"cm:1000-1.pdf OR QNAME:\"cm:321\"";
		
		try {
			String escaped = QueryBuilder.escapeLuceneQuery(input);
			System.out.println("Lucene: " + escaped);
			assertTrue(false);
		} catch (IllegalArgumentException iae) {
			System.out.println("Lucene: " + iae.getMessage());
			assertTrue(true);
		}
	}
}

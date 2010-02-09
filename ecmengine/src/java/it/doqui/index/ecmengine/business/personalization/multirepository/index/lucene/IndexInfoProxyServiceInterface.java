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

package it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene;

import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.search.impl.lucene.LuceneAnalyser;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.alfresco.repo.search.impl.lucene.LuceneIndexException;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;
import org.alfresco.repo.search.impl.lucene.index.TransactionStatus;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

public interface IndexInfoProxyServiceInterface {
    public void init(DictionaryService dictionaryService, LuceneConfig luceneConfig, String repoPath);
    public boolean isIntegritySafe();
    public void insert(Document doc, String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public void insert(List<Document> docs, String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public void delete(List<Integer> docs, String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public void delete(Term term, String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public void delete(int doc, String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public IndexReader getReader(String repoPath) throws LuceneIndexException, IOException;
    public IndexReader getDeltaReader(String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public void closeDeltaReader(String deltaId, String repoPath) throws IOException;
    public void closeDeltaWriter(String deltaId, String repoPath) throws IOException;
    public void saveDelta(String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public long getDocCount(String deltaId, String repoPath) throws LuceneIndexException, IOException;
    public void closeMainReader(String repoPath) throws LuceneIndexException, IOException;
    public void setStatus(String deltaId, String repoPath, TransactionStatus state) throws IOException;
    public void setPreparedState(String deltaId, String repoPath, Set<String> toDelete, long documents, boolean deleteNodesOnly) throws IOException;
    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader(String deltaId, String repoPath, Set<String> deletions, boolean deleteOnlyNodes) throws IOException;
    public void setDictionaryService(DictionaryService dictionaryService);
    public String toStringStatic();

}

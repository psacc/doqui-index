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

/**
 * Classe che proxa tutte le chiamate a {@link IndexInfo} di alfresco. Tutti i metodi sono stateless e ricevono sempre il deltaid e il repopath che
 * servono al'inexInfo sottostante per accedere al giusto indice. Questi sono gli unici due parametri con cui l'indexInfo può referenziare un indice
 * locale: se l'indice non è presente viene creato. Questo comportamento permette di ricreare su ogni macchina la stessa strutura di indici. Tutti i
 * membri della classe sono transient in quanto NON devono essere condivisi da terracotta. Tutti i metodi sono synchronized in modo che un solo thread
 * alla volta possa accedervi, anche in configurazione distribuita. Molte chiamate sono semplicemente una replica di ciò che prima veniva fatto con
 * IndexInfo. Si faccia riferimento a {@link IndexInfo} per maggiori dettagli.
 *
 * @see IndexInfo
 * @author Roberto Franchini
 */

public class IndexInfoProxyServiceNoTC implements IndexInfoProxyServiceInterface {

	private static transient Logger s_logger = Logger.getLogger(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LUCENE_LOG_CATEGORY);

	/**
     * The Lucene configuration options.
     */
	private transient LuceneConfig config;

	/**
	 * Il sottostante indexInfo
	 */
	private transient IndexInfo indexInfo;

	/**
     * The dictionary service.
     */
	private static transient DictionaryService dictionaryService;

	/**
	 * Metodo di inizializzazione. E' chiamato localmente (e.g.: sulla macchina) per settare il valore dei membri della classe ed ottenere il corretto
	 * IndexInfo
	 *
	 * @param dictionaryService
	 * @param luceneConfig
	 * @param repoPath
	 */
	public synchronized void init(DictionaryService dictionaryService, LuceneConfig luceneConfig, String repoPath) {
		setDictionaryService(dictionaryService);
		this.config            = luceneConfig;

		initIndexInfo(repoPath);

		if(s_logger.isDebugEnabled()){
           s_logger.debug("[IndexInfoProxyServiceNoTC::init] (" +this +") Status: " + toStringStatic() + " - isSafe: " + isIntegritySafe());
           s_logger.debug("[IndexInfoProxyServiceNoTC::init] DST: " +this.dictionaryService  );
           s_logger.debug("[IndexInfoProxyServiceNoTC::init] DS: " +dictionaryService  );
           s_logger.debug("[IndexInfoProxyServiceNoTC::init] LC: " +luceneConfig       );
           s_logger.debug("[IndexInfoProxyServiceNoTC::init] RP: " +repoPath           );
	    }
	}

	/**
	 * Dato il repoPath inizializza l'Indexinfo. Viene chiamato priam di ogni operazione per ottenere l'indexInfo che gestisce il repoPath. Questa
	 * operazione viene fatta per essere certi che il repoPath di una macchina appartenente al cluster sia replicato anche sulle altre.
	 *
	 * @param repoPath il path dello store degli indici
	 */
	private synchronized void initIndexInfo(String repoPath) {
		if(s_logger.isDebugEnabled()){
   		   s_logger.debug("[IndexInfoProxyServiceNoTC::initIndexInfo] (" +this +") IndexInfo : " +repoPath);
        }

		String basePath = config.getIndexRootLocation() + File.separator + repoPath;
		final File baseDir = new File(basePath);

		if(s_logger.isDebugEnabled()){
		   s_logger.debug("[IndexInfoProxyServiceNoTC::initIndexInfo] (" +this +") IndexInfo pre : " + indexInfo);
        }

		indexInfo = IndexInfo.getIndexInfo(baseDir, config);

		if(s_logger.isDebugEnabled()){
		   s_logger.debug("[IndexInfoProxyServiceNoTC::initIndexInfo] (" +this +") IndexInfo post: " + indexInfo);
        }
	}

	/**
	 * Metodo di utilità per verificare che il proxy sia "integro". Utile in debug.
	 *
	 * @return true se il proxy è in situazione integra
	 */
	public synchronized boolean isIntegritySafe() {
		return config != null && indexInfo != null;
	}

	/**
	 * Inserisce un documento lucene in un indice identificato da deltaId e repoPath
	 *
	 * @param doc il documento da inserire
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void insert(Document doc, String deltaId, String repoPath) throws LuceneIndexException, IOException {
		IndexWriter writer = getDeltaWriter(deltaId, repoPath);
		insert(doc, writer);
	}

	/**
	 * Inserisce una lista di documenti lucene in un indice identificato da deltaId e repoPath
	 *
	 * @param docs lista dei documenti da inserire
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void insert(List<Document> docs, String deltaId, String repoPath) throws LuceneIndexException, IOException {
		IndexWriter writer = getDeltaWriter(deltaId, repoPath);
		for (Document doc : docs) {
			try {
				insert(doc, writer);
			} catch (IOException e) {
      		    s_logger.error("[IndexInfoProxyServiceNoTC::insert] (" +this +") Failed to add document to index: " +e);
				throw new LuceneIndexException("Failed to add document to index", e);
			}
		}
	}

	/**
	 * Metodo di utilità privato che inserisce un singolo documento lucene nell'indice su cui è aperto il writer
	 *
	 * @param doc documento lucene da inserire
	 * @param writer da utilizzare per inserire il documento
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	private synchronized void insert(Document doc, IndexWriter writer) throws LuceneIndexException, IOException {
		if(s_logger.isDebugEnabled()){
			s_logger.debug("[IndexInfoProxyServiceNoTC::insert] (" +this +") Writing doc: " + doc.getField("ID") + " [To directory: " + writer.getDirectory() + "]");
			//s_logger.debug("[IndexInfoProxyServiceNoTC::insert] " + "Document: ["+doc.toString().replaceAll(" ", "\r\n")+"]");
		}
		writer.addDocument(doc);
	}

	/**
	 * Cacella una lista di documenti identificati dalla loro id (lucene id)
	 *
	 * @param docs lista di id
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void delete(List<Integer> docs, String deltaId, String repoPath) throws LuceneIndexException, IOException {
		IndexReader reader = getDeltaReader(deltaId, repoPath);
		for (Integer doc : docs) {
			try {
				reader.deleteDocument(doc.intValue());
			} catch (IOException e) {
				throw new LuceneIndexException("Failed to add document to index", e);
			}
		}
	}

	/**
	 * Cancella una lista di documenti indentificati da un Term
	 *
	 * @param term il Term che identifica i documenti
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void delete(Term term, String deltaId, String repoPath) throws LuceneIndexException, IOException {
    	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::delete] (" +this +") deltaId: " + deltaId);
    	}
		getDeltaReader(deltaId, repoPath).deleteDocuments(term);
	}

	/**
	 * Cancella un singolo documento identificato dalla id lucene
	 *
	 * @param doc id del documento
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void delete(int doc, String deltaId, String repoPath) throws LuceneIndexException, IOException {
    	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::delete] (" +this +") deltaId: " + deltaId);
    	}
		getDeltaReader(deltaId, repoPath).deleteDocument(doc);
	}

	/**
	 * Ottiene dall'IndexInfo il main reader per un certo RepoPath
	 *
	 * @param repoPath path dello store degli indici
	 * @return il reader
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized IndexReader getReader(String repoPath) throws LuceneIndexException, IOException {
		if(s_logger.isDebugEnabled()){
		   s_logger.debug("[IndexInfoProxyServiceNoTC::getReader] (" +this +") Status: " + toStringStatic() + " - isSafe: " + isIntegritySafe());
	    }
		initIndexInfo(repoPath);
		return indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader();
	}

	/**
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @return il reader
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized IndexReader getDeltaReader(String deltaId, String repoPath) throws LuceneIndexException, IOException {
		if(s_logger.isDebugEnabled()){
		   s_logger.debug("[IndexInfoProxyServiceNoTC::getDeltaReader] (" +this +") deltaId: " + deltaId);
	    }
		initIndexInfo(repoPath);
		return indexInfo.getDeltaIndexReader(deltaId);
	}

	/**
	 * Chiude il reader associato ad un certo repoPath e deltaId
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws IOException
	 */
	public synchronized void closeDeltaReader(String deltaId, String repoPath) throws IOException {
    	if(s_logger.isDebugEnabled()){
    		s_logger.debug("[IndexInfoProxyServiceNoTC::closeDeltaReader] (" +this +") deltaId: " + deltaId);
	    }
		initIndexInfo(repoPath);
		indexInfo.closeDeltaIndexReader(deltaId);
	}

	/**
	 * Dati deltaId e repoPath ottiene un writer per l'indice lucene così identificato
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @return il writer aperto sull'indice corretto
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	private synchronized IndexWriter getDeltaWriter(String deltaId, String repoPath) throws LuceneIndexException, IOException {
    	if(s_logger.isDebugEnabled()){
		    s_logger.debug("[IndexInfoProxyServiceNoTC::getDeltaWriter] (" +this +") DS(" +(dictionaryService==null?"null":"object") +") deltaId: " + deltaId);
    	}
		initIndexInfo(repoPath);
		return indexInfo.getDeltaIndexWriter(deltaId, new LuceneAnalyser(dictionaryService, config.getDefaultMLIndexAnalysisMode()));
	}

	/**
	 * Dati deltaId e repoPath chiude writer per l'indice lucene così identificato
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws IOException
	 */
	public synchronized void closeDeltaWriter(String deltaId, String repoPath) throws IOException {
    	if(s_logger.isDebugEnabled()){
		    s_logger.debug("[IndexInfoProxyServiceNoTC::closeDeltaWriter] (" +this +") deltaId: " + deltaId);
    	}
	    initIndexInfo(repoPath);
		indexInfo.closeDeltaIndexWriter(deltaId);
	}

	/**
	 * Il "salvataggio" di un delta implica la chiusura di reader e writer aperti
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void saveDelta(String deltaId, String repoPath) throws LuceneIndexException, IOException {
    	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::saveDelta] (" +this +") deltaId: " + deltaId);
    	}
		closeDeltaReader(deltaId, repoPath);
		closeDeltaWriter(deltaId, repoPath);
	}

	/**
	 * Ritorna in numero di documenti presenti delta di un certorepository
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @return il numero di documenti presenti
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized long getDocCount(String deltaId, String repoPath) throws LuceneIndexException, IOException {
		initIndexInfo(repoPath);
		long nDoc = getDeltaWriter(deltaId, repoPath).docCount();
        return nDoc;
	}

	/**
	 * Chiude il mainReader associato ad un repository
	 *
	 * @param repoPath path dello store degli indici
	 * @throws LuceneIndexException
	 * @throws IOException
	 */
	public synchronized void closeMainReader(String repoPath) throws LuceneIndexException, IOException {
    	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::closeMainReader] (" +this +") Status: " + toString() + " - isSafe: " + isIntegritySafe());
    	}
        // Creo un nuovo riferimento al main
	    IndexReader main = getReader(repoPath);
        // Chiudo il primo invocato dalla chiamata getReader()
	    main.close();
        // Chiudo il secondo che era quello che dovevo effettivamente chiudere
	    main.close();
	}

	/**
	 * Setta sull'indexInfo sottostante lo stato di un dato deltaId. Utilizzato per gestire la transazionalità sui delta
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @param state
	 * @throws IOException
	 */
	public synchronized void setStatus(String deltaId, String repoPath, TransactionStatus state) throws IOException {
		if(s_logger.isDebugEnabled()){
		   s_logger.debug("[IndexInfoProxyServiceNoTC::setStatus] (" +this +") deltaId: " + deltaId + " - TransactionStatus: " + state);
	    }
		initIndexInfo(repoPath);

        // MB: Eccezione in caso di stato attivo
        // Si e' verificato che creando un tenant, a volte vengono fatte 2 operazioni, nello stesso giro di
        // transazione, sullo stesto STORE. Questo crea l'utilizzo di 2 delta indexer.
        // Solo che essendo creati su ThreadLocal, c'e' una cache che restituisce 2 volte lo stesso indexer
        // La cosa e' cosa e' corretta, se non fosse che, impostando 2 volte a ACTIVE il deta, Alfresco torna
        // una eccezione.
        // Per evitare l'eccezione, evitiamo di impostare 2 volte ad active un indice :)

        // Prima di impostare, leggo lo stato attuale
        /*
		TransactionStatus ts = indexInfo.getIndexEntryStatus( deltaId );
        if( ts!=null                         && // Ho il delta
            ts==TransactionStatus.ACTIVE     && // Il vecchio valore e' ACTIVE
            state==TransactionStatus.ACTIVE  ){ // Il nuovo   valore e' ACTIVE
            // Non imposto lo stato, dato che Alfresco mi darebbe eccezione
    		s_logger.debug("[IndexInfoProxyServiceNoTC::setStatus] (" +this +") deltaId: " + deltaId + " - mantenuto TransactionStatus: " + state);
        } else {
    		s_logger.debug("[IndexInfoProxyServiceNoTC::setStatus] (" +this +") deltaId: " + deltaId + " - DA: " +ts +" A: " + state);
		    indexInfo.setStatus(deltaId, state, null, null);
	    }
        */
        indexInfo.setStatus(deltaId, state, null, null);
	}

	/**
	 * Setta sull'indexInfo sottostante lo stato di un dato deltaId. Utilizzato per gestire la transazionalità sui delta
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @param toDelete
	 * @param documents
	 * @param deleteNodesOnly
	 * @throws IOException
	 */
	public synchronized void setPreparedState(String deltaId, String repoPath, Set<String> toDelete, long documents, boolean deleteNodesOnly) throws IOException {
    	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::setPreparedState] (" +this +") deltaId: " + deltaId);
    	}
		initIndexInfo(repoPath);
		indexInfo.setPreparedState(deltaId, toDelete, documents, deleteNodesOnly);
	}

	/**
	 * Ottiene dal indexInfo un reader in readOnly
	 *
	 * @param deltaId dell'indice da usare
	 * @param repoPath path dello store degli indici
	 * @param deletions
	 * @param deleteOnlyNodes
	 * @return IndexReader
	 * @throws IOException
	 */
	public synchronized IndexReader getMainIndexReferenceCountingReadOnlyIndexReader(String deltaId, String repoPath, Set<String> deletions, boolean deleteOnlyNodes) throws IOException {
    	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::getMainIndexReferenceCountingReadOnlyIndexReader] (" +this +") Deltaid: " +deltaId + " - repoPath: " +repoPath);
    	}
		initIndexInfo(repoPath);
		return indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader(deltaId, deletions, deleteOnlyNodes);
	}

	/**
	 * Setter per la luceneConfig FIXME: non più usato
	 *
	 * @param config
	 */
	//public synchronized void setConfig(LuceneConfig config) {
		//this.config = config;
	//}

	/**
	 * Setter per indexInfo FIXME: non più usato
	 *
	 * @param indexInfo
	 */
	//public synchronized void setIndexInfo(IndexInfo indexInfo) {
		//this.indexInfo = indexInfo;
	//}

	/**
	 * Setter per dictionaryService usato localmente e non condiviso fra le macchine
	 *
	 * @param dictionaryService
	 */
	public synchronized void setDictionaryService(DictionaryService dictionaryService) {
      	if(s_logger.isDebugEnabled()){
	    	s_logger.debug("[IndexInfoProxyServiceNoTC::setDictionaryService] (" +this +") setDictionaryService: " +dictionaryService);
    	}
        // Imposta il dictionary service solo se null
        if( this.dictionaryService==null ){
		    this.dictionaryService = dictionaryService;
	    }
        if( dictionaryService!=null ){
           if( !(this.dictionaryService==dictionaryService) ){
               s_logger.error("[IndexInfoProxyServiceNoTC::setDictionaryService] (" +this +") setDictionaryService: " +this.dictionaryService);
               s_logger.error("[IndexInfoProxyServiceNoTC::setDictionaryService] (" +this +") setDictionaryService: " +dictionaryService);
           }
	    }
	}

	/**
	 * Un toString che riporta lo stato interno del proxy. Utile per logging e debug.
	 *
	 * @return String
	 */
	public synchronized String toStringStatic() {
		return " -  luceneConfig: " + (config != null) + "- dictionaryService:  " + (dictionaryService != null);
	}

}

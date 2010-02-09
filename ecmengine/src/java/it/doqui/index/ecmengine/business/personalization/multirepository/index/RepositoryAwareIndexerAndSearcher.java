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
 
package it.doqui.index.ecmengine.business.personalization.multirepository.index;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Interface for multirepository-enabled Indexer and Searcher Factories to implement.
 */
public interface RepositoryAwareIndexerAndSearcher {
    /**
     * Get an indexer for a store and a repository.
     * 
     * @param storeRef The store to index.
     * @param repository The ID of the repository to index.
     * 
     * @return An indexer.
     * 
     * @throws IndexerException If there are errors while getting the indexer.
     */
    public abstract Indexer getIndexer(StoreRef storeRef, String repository) 
    throws IndexerException;

    /**
     * Get a searcher for a store and a repository.
     * 
     * @param storeRef The store to search.
     * @param repository The ID of the repository to search.
     * @param searchDelta Search the in progress transaction as well as the main index
     *            (this is ignored for searches that do full text).
     *            
     * @return A searcher.
     * 
     * @throws SearcherException If there are errors while getting the searcher.
     */
    public abstract SearchService getSearcher(StoreRef storeRef, String repository, boolean searchDelta) 
    throws SearcherException;
    
    /** Do any indexing that may be pending on behalf of the current transaction. */
    public abstract void flush();
}

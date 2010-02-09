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

package it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts;

import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * API for full text search indexing in the background, using the correct repository
 *
 * @author doqui
 */
public interface RepositoryAwareFullTextSearchIndexer extends BeanFactoryPostProcessor
{
    /**
     * Mark a store as dirty, requiring a background index update to fix it up.
     *
     * @param storeRef
     * @param repository
     */
    void requiresIndex(StoreRef storeRef, String repository);

    /**
     * Call back to report state back to the indexer
     *
     * @param storeRef
     * @param repository
     * @param remaining
     * @param e
     */
    void indexCompleted(StoreRef storeRef, String repository, int remaining, Exception e);

    /**
     * Pause indexing 9no back ground indexing until a resume is called)
     * @throws InterruptedException
     */
    void pause() throws InterruptedException;

    /**
     * Resume after a pause
     *
     * @throws InterruptedException
     */
    void resume() throws InterruptedException;

    /**
     * Do a chunk of outstanding indexing work
     *
     */
    void index();

}

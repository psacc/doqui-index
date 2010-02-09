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

import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryBackgroundIndexerAware;

import org.alfresco.repo.search.IndexMode;

public interface RepositoryAwareAVMLuceneIndexer
extends RepositoryAwareLuceneIndexer, RepositoryBackgroundIndexerAware {
    /**
     * Index a specified change to a store between two snapshots
     *
     * @param store - the name of the store
     * @param srcVersion - the id of the snapshot before the change set
     * @param dstVersion - the id of the snapshot created by the change set
     * @param mode
     */
    public void index(String store, int srcVersion, int dstVersion, IndexMode mode);

    /**
     * Delete the index for the specified store.
     *
     * @param store
     * @param mode
     */
    public void deleteIndex(String store, IndexMode mode);

    /**
     * Create an index for the specified store.
     * This makes sure that the root node for the store is indexed correctly.
     *
     * @param store
     * @param mode
     *    - IndexMode.SYNCHRONOUS - the last searchable snapshot
     *    - IndexMode.ASYNCHRONOUS - the last pending snapshot to be indexed. It may or may not be searchable.
     */
    public void createIndex(String store, IndexMode mode);

    /**
     * Get the id of the last snapshot added to the index
     * @param store
     *
     * @return - the snapshot id
     */
    public int getLastIndexedSnapshot(String store);

    /**
     * Is the snapshot applied to the index?
     *
     * Is there an entry for any node that was added OR have all the nodes in the transaction been deleted as expected?
     *
     * @param store
     * @param id
     * @return - true if applied, false if not
     */
    public boolean isSnapshotIndexed(String store, int id);

    /**
     * Is snapshot searchable
     * @param store
     * @param id
     * @return - true if snapshot has been fully indexed, false if pending or unindexed.
     */
    public boolean isSnapshotSearchable(String store, int id);

    /**
     * Has the index been created
     *
     * @param store
     * @return - true if index has been created
     */
    public boolean hasIndexBeenCreated(String store);
}

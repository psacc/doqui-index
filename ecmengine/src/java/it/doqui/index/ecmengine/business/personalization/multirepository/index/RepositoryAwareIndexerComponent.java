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

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;

import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Classe che implementa un IndexerComponent &quot;repository-aware&quot;.
 * 
 * Questo indexer component &egrave; in grado di indicizzare automaticamente i nodi sulla base del repository associato
 * al thread corrente.
 *
 * @see RepositoryManager
 */
public class RepositoryAwareIndexerComponent implements Indexer {
	
    private RepositoryAwareIndexerAndSearcher indexerAndSearcherFactory;

    public void setIndexerAndSearcherFactory(RepositoryAwareIndexerAndSearcher indexerAndSearcherFactory) {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }

    public void createNode(ChildAssociationRef relationshipRef) {
    	final String repo = RepositoryManager.getCurrentRepository();
        final Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef(), repo);
        
        indexer.createNode(relationshipRef);
    }

    public void updateNode(NodeRef nodeRef) {
    	final String repo = RepositoryManager.getCurrentRepository();
        final Indexer indexer = indexerAndSearcherFactory.getIndexer(nodeRef.getStoreRef(), repo);
        
        indexer.updateNode(nodeRef);
    }

    public void deleteNode(ChildAssociationRef relationshipRef) {
    	final String repo = RepositoryManager.getCurrentRepository();
        final Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef(), repo);
        
        indexer.deleteNode(relationshipRef);
    }

    public void createChildRelationship(ChildAssociationRef relationshipRef) {
    	final String repo = RepositoryManager.getCurrentRepository();
        final Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef(), repo);
        
        indexer.createChildRelationship(relationshipRef);
    }

    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef) {
    	final String repo = RepositoryManager.getCurrentRepository();
        final Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipBeforeRef.getChildRef().getStoreRef(), repo);
        
        indexer.updateChildRelationship(relationshipBeforeRef, relationshipAfterRef);
    }

    public void deleteChildRelationship(ChildAssociationRef relationshipRef) {
    	final String repo = RepositoryManager.getCurrentRepository();
        final Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef(), repo);
        
        indexer.deleteChildRelationship(relationshipRef);
    }
}

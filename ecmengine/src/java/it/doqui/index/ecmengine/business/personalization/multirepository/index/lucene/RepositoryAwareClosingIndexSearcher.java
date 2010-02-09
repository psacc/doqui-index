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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

/**
 * Implementazione che replica la logica gi&agrave; inclusa in {@code org.alfresco.repo.search.impl.lucene.ClosingIndexSearcher}.
 *
 * {@strong NB:} questa classe &egrave; necessaria perch&eacute; il metodo {@code getReader()} di tale classe ha
 * visibilit&acute; &quot;package&quot;.
 *
 * @author DoQui
 */
public class RepositoryAwareClosingIndexSearcher extends IndexSearcher
{

	IndexReader reader;
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

    public RepositoryAwareClosingIndexSearcher(String path) throws IOException {
        super(path);
        if (logger.isDebugEnabled()) {
            logger.debug("[RepositoryAwareClosingIndexSearcher::constructor] Searcher constructed with path: " + path);
        }
    }

    public RepositoryAwareClosingIndexSearcher(Directory directory) throws IOException {
        super(directory);
        if (logger.isDebugEnabled()) {
            logger.debug("[RepositoryAwareClosingIndexSearcher::constructor] Searcher constructed with directory: " + directory);
        }
    }

    public RepositoryAwareClosingIndexSearcher(IndexReader r) {
        super(r);
        this.reader = r;
        if (logger.isDebugEnabled()) {
            logger.debug("[RepositoryAwareClosingIndexSearcher::constructor] Searcher constructed with IndexReader: " + r);
        }
    }

    /*package*/ IndexReader getReader() {
        return reader;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (reader != null) {
            reader.close();
        }
        logger.debug("[RepositoryAwareClosingIndexSearcher::close] Searcher closed.");
    }
}

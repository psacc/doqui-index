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
 
package it.doqui.index.ecmengine.business.personalization.multirepository.patch;

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareIndexerAndSearcher;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

public class UserAndPersonTokenisationPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.userAndPersonUserNamesAsIdentifiers.result";
    
    private ImporterBootstrap spacesImporterBootstrap;
    private ImporterBootstrap userImporterBootstrap;
    private RepositoryAwareIndexerAndSearcher indexerAndSearcher;
    

    public UserAndPersonTokenisationPatch()
    {
        
    }

    public void setSpacesImporterBootstrap(ImporterBootstrap spacesImporterBootstrap)
    {
        this.spacesImporterBootstrap = spacesImporterBootstrap;
    }
    
    public void setUserImporterBootstrap(ImporterBootstrap userImporterBootstrap)
    {
        this.userImporterBootstrap = userImporterBootstrap;
    }
    
    public void setIndexerAndSearcher(RepositoryAwareIndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        reindex("TYPE:\"usr:user\"", userImporterBootstrap.getStoreRef());
        reindex("TYPE:\"cm:person\"", spacesImporterBootstrap.getStoreRef());
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
    
    private void reindex(String query, StoreRef store)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(store);
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            for(ResultSetRow row : rs)
            {
                Indexer indexer = indexerAndSearcher.getIndexer(row.getNodeRef().getStoreRef(), RepositoryManager.getCurrentRepository());
                indexer.updateNode(row.getNodeRef());
            }
        }
        finally
        {
          if(rs != null)
          {
              rs.close();
          }
        }
    }
}

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

package it.doqui.index.ecmengine.business.personalization.multirepository;

import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap.TenantService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TenantRoutingFileContentStore extends org.alfresco.repo.content.TenantRoutingFileContentStore {

    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);

    Map<String,Map<String, ContentStore>> tenantFileStoreMap = new HashMap<String,Map<String, ContentStore>>();

    private TenantService tenantService;

    private RepositoryManager repositoryManager;

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
    	this.repositoryManager = repositoryManager;
    }

    protected ContentStore selectWriteStore(ContentContext ctx)
    {
    	if (logger.isDebugEnabled()) {
    	    logger.debug("[TenantRoutingFileContentStore::selectWriteStore] selecting write store for domain '"+ctx+"'");
    	    logger.debug("[TenantRoutingFileContentStore::selectWriteStore] selecting write store for domain '"+tenantService.getCurrentUserDomain()+"'");
        }
        return getTenantFileStore(tenantService.getCurrentUserDomain());
    }

    public List<ContentStore> getAllStores()
    {
    	logger.debug("[TenantRoutingFileContentStore::getAllStores] getting all stores");
    	// Se il tenantService e' attivo (situazione di multitenant)
        if (tenantService.isEnabled())
        {
        	logger.debug("[TenantRoutingFileContentStore::getAllStores] tenantService enabled");

        	Map<String, ContentStore> tenantFileStores = getTenantFileStores();
            String currentUser = AuthenticationUtil.getCurrentUserName();
        	logger.debug("[TenantRoutingFileContentStore::getAllStores] current user: "+currentUser);
        	// Se non ho un utente corrente
        	// Oppure l'utente corrente e' uguale all'utente di sistema
            if ((currentUser == null) || (currentUser.equals(AuthenticationUtil.getSystemUserName())) || (currentUser.startsWith(AuthenticationUtil.getSystemUserName()+TenantService.SEPARATOR)))
            {
                // return enabled stores across all tenants, if running as system/null user, for example, ContentStoreCleaner scheduled job
                List<ContentStore> allEnabledStores = new ArrayList<ContentStore>();
                for (String tenantDomain : tenantFileStores.keySet())
                {
                	logger.debug("[TenantRoutingFileContentStore::getAllStores] adding store for domain '"+tenantDomain+"'");
                    allEnabledStores.add(tenantFileStores.get(tenantDomain)); // note: cache should only contain enabled stores

                    // MB: E aggiungendo tutti quelli presenti in repository, per quel domain,
                    int nProg = 0;
                    do {
                        ContentStore cs = tenantFileStores.get( tenantDomain +"@" +nProg );
                        if( cs==null ) break;

                        allEnabledStores.add( cs );
                        logger.debug("[TenantRoutingFileContentStore::getAllStores] adding substore (" +nProg +") for domain " +tenantDomain);
                        nProg++;
                    } while( true );

                }
                return allEnabledStores;
            }
        }

        // MB: Creo la lista di ContentStore, partendo dal default
        logger.debug("[TenantRoutingFileContentStore::getAllStores] AGGIUNGO CS master");
        List <ContentStore> aStores = new ArrayList<ContentStore>();
        aStores.add( getTenantFileStore( tenantService.getCurrentUserDomain() ) );

        // MB: E aggiungendo tutti quelli presenti in repository, per quel domain,
        Map<String, ContentStore> tenantFileStores = getTenantFileStores();
        int nProg = 0;
        do {
        	ContentStore cs = tenantFileStores.get( tenantService.getCurrentUserDomain() +"@" +nProg );
        	if( cs==null ) break;

       		aStores.add( cs );
            if(logger.isDebugEnabled()) {
                logger.debug("[TenantRoutingFileContentStore::getAllStores] AGGIUNGO CS " +cs.toString());
            }
        	nProg++;
        } while( true );

        return aStores;
    }

    private ContentStore getTenantFileStore(String tenantDomain)
    {
    	logger.debug("[TenantRoutingFileContentStore::getTenantFileStore] getting file store for domain '"+tenantDomain+"'");
    	Map<String, ContentStore> tenantFileStores = getTenantFileStores();
        ContentStore store = tenantFileStores.get(tenantDomain);
        if (store == null && !TenantService.DEFAULT_DOMAIN.equals(tenantService.getCurrentUserDomain())) {
        	// try to create store for tenant
        	init(RepositoryManager.getCurrentRepository());
        }
        return store;
    }

    private void putTenantFileStore(String tenantDomain, ContentStore fileStore)
    {
    	logger.debug("[TenantRoutingFileContentStore::putTenantFileStore] putting file store for domain '"+tenantDomain+"'");
    	Map<String, ContentStore> tenantFileStores = getTenantFileStores();
        tenantFileStores.put(tenantDomain, fileStore);
    }

    private void removeTenantFileStore(String tenantDomain)
    {
    	logger.debug("[TenantRoutingFileContentStore::removeTenantFileStore] removing file store for domain '"+tenantDomain+"'");
    	Map<String, ContentStore> tenantFileStores = getTenantFileStores();
        tenantFileStores.remove(tenantDomain);
    }

    public void init() {
    	logger.debug("[TenantRoutingFileContentStore::init] BEGIN");
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		init(RepositoryManager.getCurrentRepository());
    	}
    	logger.debug("[TenantRoutingFileContentStore::init] END");
    }

    protected void init(String currentRepositoryId)
    {
    	logger.debug("[TenantRoutingFileContentStore::init] BEGIN");
        Repository repository = RepositoryManager.getInstance().getRepository(currentRepositoryId);

        String tenantDomain = "";
        String rootDir 		= repository.getContentRootLocation();

        if(logger.isDebugEnabled()) {
            logger.debug("[TenantRoutingFileContentStore::init] getting file stores for repository '"+currentRepositoryId+"'");
        }

        Tenant tenant = tenantService.getTenant(tenantService.getCurrentUserDomain());
        if (tenant != null)
        {
            if (tenant.getRootContentStoreDir() != null && !tenant.getRootContentStoreDir().equals(""))
            {
               rootDir = tenant.getRootContentStoreDir();
            }
            tenantDomain = tenant.getTenantDomain();
        }

        if(logger.isDebugEnabled()) {
        	logger.debug("[TenantRoutingFileContentStore::init] getting tenant domain  '" +tenantDomain +"'"); // ''
    	    logger.debug("[TenantRoutingFileContentStore::init] getting tenant rootDir '" +rootDir      +"'"); // '/ecmengine/content/contentstore'
        }

        putTenantFileStore(tenantDomain, new FileContentStore(new File(rootDir)));

        // MB: estraggo la lista dei contentStore
        List<ContentStoreDefinition> contentStores = null;
        if( tenantDomain.length()>0 ) {
            if( tenant!=null && tenant.getContentStores()!=null ){
                contentStores = tenant.getContentStores();
            }
        } else {
            // MB: Se tenant vuoto, controllo sul repository
            if( repository!=null && repository.getContentStores()!=null ){
                contentStores = repository.getContentStores();
            }
        }

        // Se ho il tenant, con dei contentStores e sul repository ho dei contentStore .. allora prendo i contentStore associati
    	if( contentStores!=null )
        {
        	int nProg = 0;
        	for( ContentStoreDefinition tcs : contentStores )
        	{
        		nProg++;

                // MB: dal protocol assegnato al Tenant, accedo al bean del repository che gestisce quel protocol
        		//ContentStoreDefinition tcs = (ContentStoreDefinition)s;

                logger.debug("[TenantRoutingFileContentStore::init] ContentStoreDefinition " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource());

                // MB: I contentStore sono riprogrammabili runtime, quindi un errore e' da intercettare, ma non deve invalidare
                //     la creazione del tenant, dato che puo' essere recuperato runtime
                try {
                    // Istanzia il nuovo content store passando dal manager
                    ContentStoreDynamic cs = ContentStoreManager.getInstance().getContentStore( tcs );

                    // Se il ContentStore non e' null, lo inizializzo col path dentro al ContentStoreDefinition
                    if( cs==null ) {
                        logger.error("[TenantRoutingFileContentStore::init] unable to get contentStore for tenant " +tenantDomain);
                        logger.error("[TenantRoutingFileContentStore::init] ContentStoreDefinition " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource());
                    } else {
                        if(logger.isDebugEnabled()) {
                            logger.debug("[TenantRoutingFileContentStore::init] get contentStore for tenant domain '" +tenantDomain+"-"+nProg +" " +cs.toString());
                        }
                        putTenantFileStore( tenantDomain+"@"+nProg, cs );
                    }
                } catch( org.alfresco.error.AlfrescoRuntimeException e ){
                    // Errore nell'uso del ContentStore
                    logger.error("[TenantRoutingFileContentStore::init] unable to get contentStore for tenant " +tenantDomain);
                }
        	}
        }

    	logger.debug("[TenantRoutingFileContentStore::init] END");
    }

    public void destroy()
    {
        removeTenantFileStore(tenantService.getCurrentUserDomain());
    }

    public void onEnableTenant()
    {
        init(RepositoryManager.getCurrentRepository());
    }

    public void onDisableTenant()
    {
        destroy();
    }

    public String getDefaultRootDir()
    {
        String currentRepositoryId = RepositoryManager.getCurrentRepository();
    	String rootLocation = RepositoryManager.getInstance().getRepository(currentRepositoryId).getContentRootLocation();
        if(logger.isDebugEnabled()) {
        	logger.debug("[TenantRoutingFileContentStore::getDefaultRootDir] getting default root dir for repository '"+currentRepositoryId+"': "+rootLocation);
        }
        return rootLocation;
    }

    private Map<String, ContentStore> getTenantFileStores() {
        String currentRepositoryId = RepositoryManager.getCurrentRepository();
    	if (logger.isDebugEnabled()) {
    		logger.debug("[TenantRoutingFileContentStore::getTenantFileStores] getting file stores for repository '"+currentRepositoryId+"'");
    	}
    	Map<String, ContentStore> tenantFileStores = tenantFileStoreMap.get(currentRepositoryId);
    	if (tenantFileStores == null) {
    		tenantFileStores = new HashMap<String, ContentStore>();
    		tenantFileStoreMap.put(currentRepositoryId, tenantFileStores);
    	}
    	if (logger.isDebugEnabled()) {
	    	logger.debug("[TenantRoutingFileContentStore::getTenantFileStores] found "+tenantFileStores.size()+" file stores");
	    	for (String tenantDomain : tenantFileStores.keySet()) {
	        	logger.debug("[TenantRoutingFileContentStore::getTenantFileStores] found file store for domain "+tenantDomain+": "+tenantFileStores.get(tenantDomain));
	    	}
    	}
    	return tenantFileStores;
    }

}

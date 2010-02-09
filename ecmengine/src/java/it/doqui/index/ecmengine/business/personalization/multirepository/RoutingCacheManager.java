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

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.ObjectExistsException;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerListener;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.event.CacheManagerEventListener;
import net.sf.ehcache.event.CacheManagerEventListenerRegistry;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;

public class RoutingCacheManager extends CacheManager {

    private Map<String, CacheManager> cacheManagerMap = new HashMap<String, CacheManager>();
    
    private static RoutingCacheManager theInstance = null;

    private static Logger logger = Logger.getLogger(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_CACHE_LOG_CATEGORY);

    /** Costruttore predefinito <strong>privato</strong>. */
    private RoutingCacheManager() {
    	logger.debug("[RoutingCacheManager::constructor] BEGIN");
    	logger.debug("[RoutingCacheManager::constructor] END");
    }
    
    /**
     * Metodo che restituisce l'istanza singleton di {@code RoutingCacheManager}.
     * 
     * @return L'istanza singleton.
     */
    public static synchronized RoutingCacheManager getInstance() {
    	logger.debug("[RoutingCacheManager::getInstance] BEGIN");

		try {
			if (theInstance == null) {
				theInstance = new RoutingCacheManager();
				logger.debug("[RoutingCacheManager::getInstance] Created new CacheManager instance: " + theInstance);
			}
			
			logger.debug("[RoutingCacheManager::getInstance] Using CacheManager instance: " + theInstance);
			return theInstance;

		} finally {
			logger.debug("[RoutingCacheManager::getInstance] END");
		}
    }
    
    /**
     * Restituisce l'istanza di {@code CacheManager} appropriata per il
     * repository corrente.
     * 
     * @return Il {@code CacheManager} associato al repository corrente.
     */
    public CacheManager getCacheManager() {
   	
    	// Il fallback sul default e` realizzato all'interno di getCurrentRepository();
    	final String repositoryId = RepositoryManager.getCurrentRepository();
    	
    	CacheManager currentCacheManager = null;
    	
    	synchronized (cacheManagerMap) {
    		currentCacheManager = cacheManagerMap.get(repositoryId);
    	}
        
        if (currentCacheManager != null) {
        	logger.debug("[RoutingCacheManager::getCacheManager] " +
        			"Repository '" + repositoryId + "' -- Got cache manager from map: " + currentCacheManager);
        	return currentCacheManager;
        }

        logger.info("[RoutingCacheManager::getCacheManager] Repository '" + repositoryId + 
        		"' -- No cache manager found... creating new.");
        
        URL currentConfigUrl = null;

        try {
        	if (RepositoryManager.getInstance().getRepository(repositoryId).getCacheConfigLocation() != null) {
        		currentConfigUrl = ResourceUtils.getURL(
        				RepositoryManager.getInstance().getRepository(repositoryId).getCacheConfigLocation());
        	} else {
            	logger.error("[RoutingCacheManager::getCacheManager] " +
            			"Repository '" + repositoryId + "' -- Configuration file not set!");
            	throw new IllegalArgumentException("Repository '" + repositoryId + "' has no cache configuration set!");
        	}
        	
        	logger.debug("[RoutingCacheManager::getCacheManager] " +
        			"Repository '" + repositoryId + "' -- Loading cache configuration: " + currentConfigUrl);
        	currentCacheManager = new CacheManager(currentConfigUrl);
        	
        	synchronized (cacheManagerMap) {
        		cacheManagerMap.put(repositoryId, currentCacheManager);
        	}

        	// done
        	if (logger.isDebugEnabled()) {
        		logger.debug("[RoutingCacheManager::getCacheManager] " +
        				"Repository '" + repositoryId + "' -- Created EHCache CacheManager instance: " + currentConfigUrl);
        	}
        } catch (FileNotFoundException fnfe) {

        	/* Se non trovo il file di configurazione specificato nella definizione del repository 
        	 * non posso fare fallback sul default: rischierei di trovarmi con l'ECMENGINE avviato e tutte le cache
        	 * dei diversi repository con la medesima configurazione!
        	 * 
        	 * - FF
        	 */
        	logger.error("[RoutingCacheManager::getCacheManager] " +
        			"Repository '" + repositoryId + "' -- Configuration file not found: " + currentConfigUrl);
        	throw new AlfrescoRuntimeException("EHCache configuration file not found: " + currentConfigUrl, fnfe);
        }

    	return currentCacheManager;
    }

	@Override
	public synchronized void addCache(Cache arg0) throws ObjectExistsException, CacheException {
		getCacheManager().addCache(arg0);
	}

	@Override
	public synchronized void addCache(Ehcache arg0) throws ObjectExistsException, CacheException {
		getCacheManager().addCache(arg0);
	}

	@Override
	public synchronized void addCache(String arg0) throws ObjectExistsException, CacheException {
		getCacheManager().addCache(arg0);
	}

	@Override
	public synchronized boolean cacheExists(String arg0) {
		return getCacheManager().cacheExists(arg0);
	}

	@Override
	public void clearAll() throws CacheException {
		getCacheManager().clearAll();
	}

	@Override
	public synchronized Cache getCache(String arg0) throws ClassCastException {
		return getCacheManager().getCache(arg0);
	}

	@Override
	public CacheManagerEventListener getCacheManagerEventListener() {
		return getCacheManager().getCacheManagerEventListener();
	}

	@Override
	public CacheManagerEventListenerRegistry getCacheManagerEventListenerRegistry() {
		return getCacheManager().getCacheManagerEventListenerRegistry();
	}

	@Override
	public CacheManagerPeerProvider getCacheManagerPeerProvider() {
		return getCacheManager().getCacheManagerPeerProvider();
	}

	@Override
	public synchronized String[] getCacheNames() {
		return getCacheManager().getCacheNames();
	}

	@Override
	public CacheManagerPeerListener getCachePeerListener() {
		return getCacheManager().getCachePeerListener();
	}

	@Override
	public CacheManagerPeerProvider getCachePeerProvider() {
		return getCacheManager().getCachePeerProvider();
	}

	@Override
	public synchronized Ehcache getEhcache(String arg0) {
		return getCacheManager().getEhcache(arg0);
	}

	@Override
	public String getName() {
		return getCacheManager().getName();
	}

	@Override
	public Status getStatus() {
		return getCacheManager().getStatus();
	}

	@Override
	public synchronized void removalAll() {
		getCacheManager().removalAll();
	}

	@Override
	public synchronized void removeCache(String arg0) {
		getCacheManager().removeCache(arg0);
	}

	@Override
	public synchronized void replaceCacheWithDecoratedCache(Ehcache arg0, Ehcache arg1) throws CacheException {
		getCacheManager().replaceCacheWithDecoratedCache(arg0, arg1);
	}

	@Override
	@Deprecated
	public void setCacheManagerEventListener(CacheManagerEventListener arg0) {
		getCacheManager().setCacheManagerEventListener(arg0);
	}

	@Override
	public void setName(String arg0) {
		getCacheManager().setName(arg0);
	}

	@Override
	public void shutdown() {
		getCacheManager().shutdown();
	}

	@Override
	public String toString() {
		return getCacheManager().toString();
	}
}

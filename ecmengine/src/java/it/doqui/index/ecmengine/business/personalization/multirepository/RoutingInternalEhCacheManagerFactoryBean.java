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

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.Timestamper;
import org.springframework.beans.factory.FactoryBean;

public class RoutingInternalEhCacheManagerFactoryBean implements FactoryBean, CacheProvider {

    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_CACHE_LOG_CATEGORY);
    private static CacheManager cacheManager;

    /**
     * Default constructor required by Hibernate. In fact, we anticipate several
     * instances of this class to be created.
     */
    public RoutingInternalEhCacheManagerFactoryBean() {}

    /**
     * News up the singleton cache manager according to the rules set out
     * in the class comments.
     */
    private static synchronized void initCacheManager() {
    	logger.debug("[RoutingInternalEhCacheManagerFactoryBean::initCacheManager] BEGIN");

    	final String repositoryId = RepositoryManager.getCurrentRepository();
        if (logger.isDebugEnabled()) {
    	    logger.debug("[RoutingInternalEhCacheManagerFactoryBean::initCacheManager] Current repository: " + repositoryId);
        }

    	try {
        	if (cacheManager != null) {
        		return;
        	}

    		logger.debug("[RoutingInternalEhCacheManagerFactoryBean::initCacheManager] Initializing internal RoutingCacheManager...");
    		cacheManager = RoutingCacheManager.getInstance();
    	} catch (Throwable e) {
    		throw new AlfrescoRuntimeException("EHCache configuration failed", e);
    	} finally {
    		logger.debug("[RoutingInternalEhCacheManagerFactoryBean::initCacheManager] END");
    	}
    }

    /**
     * @return Returns the properly initialized instance for Alfresco internal use
     *
     * @see #initCacheManager()
     */
    public static CacheManager getInstance() {
    	logger.debug("[RoutingInternalEhCacheManagerFactoryBean::getInstance] BEGIN");
		try {
			if (cacheManager == null) {
				initCacheManager();
			}

			return cacheManager;
		} finally {
			logger.debug("[RoutingInternalEhCacheManagerFactoryBean::getInstance] END");
		}
    }

    public Cache buildCache(String regionName, Properties properties) throws CacheException {
    	logger.debug("[RoutingInternalEhCacheManagerFactoryBean::buildCache] BEGIN");
        CacheManager manager = RoutingInternalEhCacheManagerFactoryBean.getInstance();

        try {
            net.sf.ehcache.Cache cache = manager.getCache(regionName);
            if (cache == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[RoutingInternalEhCacheManagerFactoryBean::buildCache] " +
                         		 "Repository '" + RepositoryManager.getCurrentRepository() + "' -- " +
                        		 "Using default cache configuration for region: " + regionName);
                }

                manager.addCache(regionName);
                cache = manager.getCache(regionName);

                if (logger.isDebugEnabled()) {
                	logger.debug("[RoutingInternalEhCacheManagerFactoryBean::buildCache] " +
                	             "Repository '" + RepositoryManager.getCurrentRepository() + "' -- " +
                			     "Started EHCache region: " + cache + " [Region: " + regionName + "]");
                }
            }

            return new EhCache(cache);
        } catch (net.sf.ehcache.CacheException e) {
        	logger.error("[RoutingInternalEhCacheManagerFactoryBean::buildCache] Error building cache region: " + regionName, e);
            throw new CacheException(e);
        } finally {
        	logger.debug("[RoutingInternalEhCacheManagerFactoryBean::buildCache] END");
        }
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * @see #initCacheManager()
     */
    public void start(Properties properties) throws CacheException {
    	RoutingInternalEhCacheManagerFactoryBean.initCacheManager();
    }

    /**
     * @see #initCacheManager()
     */
    public void stop() {
    	RoutingInternalEhCacheManagerFactoryBean.getInstance().shutdown();
    }

    /**
     * @return Returns the singleton cache manager
     *
     * @see #initCacheManager()
     */
    public Object getObject() throws Exception {
        return RoutingInternalEhCacheManagerFactoryBean.getInstance();
    }

    /**
     * @return Returns the singleton cache manager type
     */
    public Class<? extends CacheManager> getObjectType() {
        return CacheManager.class;
    }

    /**
     * @return Returns true always
     */
    public boolean isSingleton() {
        return true;
    }
}

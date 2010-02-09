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

import java.io.IOException;

import net.sf.ehcache.CacheManager;

import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class RoutingEhCacheManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
    protected static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_CACHE_LOG_CATEGORY);

    private CacheManager cacheManager;
    private RepositoryManager repositoryManager;

    /** 
     * Setter <i>IoC</i> per l'impostazione del repository manager.
     * 
     * @param manager L'istanza di {@link RepositoryManager}.
     */
    public void setRepositoryManager(RepositoryManager manager) {
		this.repositoryManager = manager;
	}

    /**
     * Metodo di inizializzazione.
     * 
     * <p>Questo metodo viene eseguito immediatamente dopo l'inizializzazione
     * degli attributi da parte del container Spring.</p>
     */
    public void afterPropertiesSet() throws IOException {
    	PropertyCheck.mandatory(this, "repositoryManager", repositoryManager);

    	logger.info("[RoutingEhCacheManagerFactoryBean::afterPropertiesSet] " +
    			"Initializing EHCache CacheManager");
    	this.cacheManager = RoutingCacheManager.getInstance();
    }

    public Object getObject() {
        if (logger.isDebugEnabled()) {
        	logger.debug("[RoutingEhCacheManagerFactoryBean::getObject] Retrieving cache manager...");
        }
    	return this.cacheManager;
    }

    public Class<? extends CacheManager> getObjectType() {
        return ((this.cacheManager != null) ? this.cacheManager.getClass() : CacheManager.class);
    }

    /**
     * Restituisce {@code true} poich&eacute; questa classe &egrave; un singleton.
     * 
     * @return {@code true}.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Metodo di shutdown.
     * 
     * <p>Eseguito automaticamente dal container Spring quando la {@code BeanFactory} 
     * viene distrutta.
     */
    public void destroy() {
        logger.info("[RoutingEhCacheManagerFactoryBean::getObject] Shutting down EHCache CacheManager...");
        this.cacheManager.shutdown();
    }
}

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

package it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap;

import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

//public class AvmBootstrap extends AbstractLifecycleBean
public class AvmBootstrap extends org.alfresco.repo.avm.AvmBootstrap
{
//    private List<Issuer> issuers;
//
//    private AVMLockingAwareService avmLockingAwareService;

    private RepositoryManager repositoryManager;

    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);

//    public AvmBootstrap() {
//        this.issuers = new ArrayList<Issuer>(0);
//    }
//
//    public void setAvmLockingAwareService(AVMLockingAwareService service) {
//        this.avmLockingAwareService = service;
//    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
    	this.repositoryManager = repositoryManager;
    }

//    /**
//     * Provide a list of {@link Issuer issuers} to bootstrap on context initialization.
//     *
//     * @see #onBootstrap(ApplicationEvent)
//     */
//    public void setIssuers(List<Issuer> issuers) {
//        this.issuers = issuers;
//    }

    /**
     * Initialize the issuers.
     */
    @Override
    protected void onBootstrap(ApplicationEvent event) {
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		logger.info("[AvmBootstrap::onBootstrap] " +
    				"Repository '" + RepositoryManager.getCurrentRepository() + "' -- Executing AVM bootstrap.");

//	        for (Issuer issuer : issuers) {
//	            issuer.initialize();
//	        }
//	        avmLockingAwareService.init();
    		super.onBootstrap(event);
    	}
    }

    /** NO-OP. */
    @Override
    protected void onShutdown(ApplicationEvent event) {
        // Nothing
    }
}

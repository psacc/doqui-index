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

//import org.alfresco.repo.avm.locking.AVMLockingServiceImpl;
//import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

//public class AVMLockingBootstrap extends AbstractLifecycleBean {
public class AVMLockingBootstrap extends org.alfresco.repo.avm.locking.AVMLockingBootstrap 
{
//    private AVMLockingServiceImpl fLockingService;
    
    private RepositoryManager repositoryManager;
    
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);

//    public void setAvmLockingService(AVMLockingServiceImpl service) {
//        fLockingService = service;
//    }
    
    public void setRepositoryManager(RepositoryManager repositoryManager) {
    	this.repositoryManager = repositoryManager;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		logger.info("[AVMLockingBootstrap::onBootstrap] " +
    				"Repository '" + RepositoryManager.getCurrentRepository() + "' -- Executing AVM Locking bootstrap.");
//            fLockingService.init();
    		super.onBootstrap(event);
    	}
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // Do nothing.
    }
}

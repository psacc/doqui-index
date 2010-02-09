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

//import org.alfresco.service.cmr.module.ModuleService;
//import org.alfresco.util.AbstractLifecycleBean;
//import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

//public class ModuleStarter extends AbstractLifecycleBean {
public class ModuleStarter extends org.alfresco.repo.module.ModuleStarter 
{
//    private ModuleService moduleService;

    private RepositoryManager repositoryManager;

    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);
    
//    /**
//     * @param moduleService the service that will do the actual work.
//     */
//    public void setModuleService(ModuleService moduleService) {
//        this.moduleService = moduleService;
//    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
    	this.repositoryManager = repositoryManager;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
//        PropertyCheck.mandatory(this, "moduleService", moduleService);
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		
    		logger.info("[ModuleStarter::onBootstrap] " +
    				"Repository '" + RepositoryManager.getCurrentRepository() + "' -- Executing Module Starter.");
    		
//            moduleService.startModules();
    		super.onBootstrap(event);
    	}
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // NOOP
    }
}

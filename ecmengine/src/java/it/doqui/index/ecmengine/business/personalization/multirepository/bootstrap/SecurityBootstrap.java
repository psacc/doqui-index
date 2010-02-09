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
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.repo.tenant.TenantService;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

public class SecurityBootstrap extends AbstractLifecycleBean {

    // Logger
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);

    private RepositoryManager repositoryManager;
    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    private MultiTTenantAdminService tenantAdminService;
    public void setTenantAdminService(MultiTTenantAdminService tenantAdminService) {
        this.tenantAdminService = tenantAdminService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        for (Repository repository : repositoryManager.getRepositories()) {
            RepositoryManager.setCurrentRepository(repository.getId());
            if( !tenantAdminService.existsTenant(EcmEngineConstants.ECMENGINE_TEMPORANEY_TENANT_NAME) ){
                logger.error("[SecurityBootstrap::onBootstrap] Repository '" +RepositoryManager.getCurrentRepository() + "' -- Missing temporary tenant [" +EcmEngineConstants.ECMENGINE_TEMPORANEY_TENANT_NAME +"].");
                logger.error("+---------------------------------------------------------------------------------+");
                logger.error("|                                                                                 |");
                logger.error("|             Temporary tenant is missing! Security feature disabled              |");
                logger.error("|       Create a temporary tenant on this repository to solve this problem        |");
                logger.error("|                                                                                 |");
                logger.error("+---------------------------------------------------------------------------------+");
            }
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // NOOP
    }
}

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

//import java.io.InputStream;
//import java.util.List;
//
//import javax.transaction.UserTransaction;
//
//import org.alfresco.error.AlfrescoRuntimeException;
//import org.alfresco.repo.importer.system.SystemExporterImporter;
//import org.alfresco.repo.security.authentication.AuthenticationComponent;
//import org.alfresco.service.cmr.repository.NodeService;
//import org.alfresco.service.cmr.repository.StoreRef;
//import org.alfresco.service.cmr.view.ImporterException;
//import org.alfresco.service.transaction.TransactionService;
//import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

//public class SystemInfoBootstrap extends AbstractLifecycleBean
public class SystemInfoBootstrap extends org.alfresco.repo.importer.system.SystemInfoBootstrap
{
    // dependencies
//    private TransactionService transactionService;
//    private NodeService nodeService;
//    private AuthenticationComponent authenticationComponent;
//    private SystemExporterImporter systemImporter;
    
    private RepositoryManager repositoryManager;

//    private List<String> mustNotExistStoreUrls = null;
//    private String bootstrapView = null;
    
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);
    
//    /**
//     * Sets the Transaction Service
//     * 
//     * @param userTransaction the transaction service
//     */
//    public void setTransactionService(TransactionService transactionService)
//    {
//        this.transactionService = transactionService; 
//    }
//
//    /**
//     * Sets the node service
//     * 
//     * @param nodeService the node service
//     */
//    public void setNodeService(NodeService nodeService)
//    {
//        this.nodeService = nodeService;
//    }
//    
//    /**
//     * Set the authentication component
//     * 
//     * @param authenticationComponent
//     */
//    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
//    {
//        this.authenticationComponent = authenticationComponent;
//    }
//
//    /**
//     * Set the System Importer
//     * 
//     * @param systemImporter
//     */
//    public void setSystemImporter(SystemExporterImporter systemImporter)
//    {
//        this.systemImporter = systemImporter;
//    }

//    /**
//     * If any of the store urls exist, the bootstrap does not take place
//     * 
//     * @param storeUrls  the list of store urls to check
//     */
//    public void setMustNotExistStoreUrls(List<String> storeUrls)
//    {
//        this.mustNotExistStoreUrls = storeUrls;
//    }
//        
//    /**
//     * Set the bootstrap view containing the system information
//     * 
//     * @param bootstrapView
//     */
//    public void setBootstrapView(String bootstrapView)
//    {
//        this.bootstrapView = bootstrapView;
//    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
    	this.repositoryManager = repositoryManager;
    }

//    /**
//     * Bootstrap
//     */
//    public void bootstrap() {
//        UserTransaction userTransaction = transactionService.getUserTransaction();
//        authenticationComponent.setSystemUserAsCurrentUser();
//
//        try {
//            userTransaction.begin();
//        
//            // check the repository exists, create if it doesn't
//            if (performBootstrap()) {
//                InputStream viewStream = getClass().getClassLoader().getResourceAsStream(bootstrapView);
//                
//                logger.info("[SystemInfoBootstrap::bootstrap] " +
//                		"Repository '" + RepositoryManager.getCurrentRepository() + 
//                		"' -- Importing system info from: " + bootstrapView);
//                
//                if (viewStream == null) {
//                    throw new ImporterException("Could not find system info file " + bootstrapView);
//                }
//                
//                try {
//                    systemImporter.importSystem(viewStream);
//                } finally {
//                    viewStream.close();
//                }
//            }
//            userTransaction.commit();
//        } catch(Throwable e) {
//            // rollback the transaction
//            try { 
//            	if (userTransaction != null) {
//            		userTransaction.rollback();
//            	} 
//            } catch (Exception ex) {}
//            
//            try {
//            	authenticationComponent.clearCurrentSecurityContext(); 
//            } catch (Exception ex) {}
//            
//            throw new AlfrescoRuntimeException("System Info Bootstrap failed on repository: " + 
//            		RepositoryManager.getCurrentRepository(), e);
//        } finally {
//            authenticationComponent.clearCurrentSecurityContext();
//        }
//    }
    
//    /**
//     * Determine if bootstrap should take place
//     * 
//     * @return  true => yes, it should
//     */
//    private boolean performBootstrap()
//    {
//        if (bootstrapView == null || bootstrapView.length() == 0)
//        {
//            return false;
//        }
//        if (mustNotExistStoreUrls != null)
//        {
//            for (String storeUrl : mustNotExistStoreUrls)
//            {
//                StoreRef storeRef = new StoreRef(storeUrl);
//                if (nodeService.exists(storeRef))
//                {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
        
    @Override
    protected void onBootstrap(ApplicationEvent event) {
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		logger.info("[SystemInfoBootstrap::onBootstrap] Repository '" +
    				RepositoryManager.getCurrentRepository() + "' -- Executing System Info bootstrap.");
            bootstrap();
    	}
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
    
}

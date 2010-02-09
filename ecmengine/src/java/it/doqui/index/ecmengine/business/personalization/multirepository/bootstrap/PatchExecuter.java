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

//import java.util.Date;
//import java.util.List;
//
//import org.alfresco.error.AlfrescoRuntimeException;
//import org.alfresco.i18n.I18NUtil;
//import org.alfresco.repo.admin.patch.PatchInfo;
//import org.alfresco.repo.admin.patch.PatchService;
//import org.alfresco.service.transaction.TransactionService;
//import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

//public class PatchExecuter extends AbstractLifecycleBean {
public class PatchExecuter extends org.alfresco.repo.admin.patch.PatchExecuter {
//    private static final String MSG_CHECKING = "patch.executer.checking";
//    private static final String MSG_NO_PATCHES_REQUIRED = "patch.executer.no_patches_required";
//    private static final String MSG_SYSTEM_READ_ONLY = "patch.executer.system_readonly";
//    private static final String MSG_NOT_EXECUTED = "patch.executer.not_executed";
//    private static final String MSG_EXECUTED = "patch.executer.executed";
//    private static final String MSG_FAILED = "patch.executer.failed";
    
    private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);
    
//    private PatchService patchService;
//    private TransactionService transactionService;

    private RepositoryManager repositoryManager;

//    /**
//     * @param patchService the server that actually executes the patches
//     */
//    public void setPatchService(PatchService patchService)
//    {
//        this.patchService = patchService;
//    }
//
//    /**
//     * @param transactionService provides the system read-only state
//     */
//    public void setTransactionService(TransactionService transactionService) {
//        this.transactionService = transactionService;
//    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
    	this.repositoryManager = repositoryManager;
    }
    
//    /**
//     * Ensures that all outstanding patches are applied.
//     */
//    public void applyOutstandingPatches() {
//        // Avoid read-only systems
//        if (transactionService.isReadOnly()) {
//            logger.warn("[PatchExecuter::applyOutstandingPatches] " +
//            		"Repository '" + RepositoryManager.getCurrentRepository() + 
//            		"' -- " + I18NUtil.getMessage(MSG_SYSTEM_READ_ONLY));
//            return;
//        }
//        
//        logger.info("[PatchExecuter::applyOutstandingPatches] " +
//        		"Repository '" + RepositoryManager.getCurrentRepository() + 
//        		"' -- " + I18NUtil.getMessage(MSG_CHECKING));
//        
//        Date before = new Date(System.currentTimeMillis() - 60000L);  // 60 seconds ago
//        patchService.applyOutstandingPatches();
//        Date after = new Date(System .currentTimeMillis() + 20000L);  // 20 seconds ahead
//        
//        // get all the patches executed in the time
//        List<PatchInfo> appliedPatches = patchService.getPatches(before, after);
//        
//        // don't report anything if nothing was done
//        if (appliedPatches.isEmpty()) {
//            logger.info("[PatchExecuter::applyOutstandingPatches] " +
//            		"Repository '" + RepositoryManager.getCurrentRepository() + 
//            		"' -- " + I18NUtil.getMessage(MSG_NO_PATCHES_REQUIRED));
//        } else {
//            boolean succeeded = true;
//            // list all patches applied, including failures
//            for (PatchInfo patchInfo : appliedPatches) {
//            	if (!patchInfo.getWasExecuted()) {
//            		// the patch was not executed
//            		logger.debug("[PatchExecuter::applyOutstandingPatches] " +
//                    		"Repository '" + RepositoryManager.getCurrentRepository() + 
//                    		"' -- " + I18NUtil.getMessage(MSG_NOT_EXECUTED, patchInfo.getId(), patchInfo.getReport()));
//            	} else if (patchInfo.getSucceeded()) {
//            		logger.info("[PatchExecuter::applyOutstandingPatches] " +
//                    		"Repository '" + RepositoryManager.getCurrentRepository() + 
//                    		"' -- " + I18NUtil.getMessage(MSG_EXECUTED, patchInfo.getId(), patchInfo.getReport()));
//            	} else {
//            		succeeded = false;
//            		logger.error("[PatchExecuter::applyOutstandingPatches] " +
//                    		"Repository '" + RepositoryManager.getCurrentRepository() + 
//                    		"' -- " + I18NUtil.getMessage(MSG_FAILED, patchInfo.getId(), patchInfo.getReport()));
//            	}
//            }
//            
//            // generate an error if there was a failure
//            if (!succeeded) {
//                throw new AlfrescoRuntimeException("Not all patches could be applied on repository: " + 
//                		RepositoryManager.getCurrentRepository());
//            }
//        }
//    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		logger.info("[PatchExecuter::onBootstrap] " +
            		"Repository '" + RepositoryManager.getCurrentRepository() + 
            		"' -- Executing Patch Executer.");
            applyOutstandingPatches();
    	}
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // NOOP
    }
}

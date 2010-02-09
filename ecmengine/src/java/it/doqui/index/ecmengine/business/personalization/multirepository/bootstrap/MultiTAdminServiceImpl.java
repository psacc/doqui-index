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
import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;
import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import it.doqui.index.ecmengine.business.personalization.multirepository.index.RepositoryAwareIndexerAndSearcher;
import org.alfresco.service.cmr.security.AuthenticationService;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.transaction.UserTransaction;

import net.sf.acegisecurity.providers.encoding.PasswordEncoder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.repo.attributes.BooleanAttributeValue;
import org.alfresco.repo.attributes.MapAttribute;
import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.MapAttributeValue;
import org.alfresco.repo.attributes.StringAttributeValue;
import org.alfresco.repo.content.TenantRoutingFileContentStore;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;

import it.doqui.index.ecmengine.business.personalization.splitting.SplittingDbNodeServiceImpl;

public class MultiTAdminServiceImpl extends org.alfresco.repo.tenant.MultiTAdminServiceImpl implements MultiTTenantAdminService
{
    // Logger
	private static Logger logger = Logger.getLogger(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_BOOTSTRAP_LOG_CATEGORY);

    // Dependencies
    private DbNodeServiceImpl nodeService; // TODO - replace with NodeService, when deleteStore is exposed via public API
    private DictionaryComponent dictionaryComponent;
    private RepoAdminService repoAdminService;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private MultiTServiceImpl tenantService;
    private AttributeService attributeService;
    private PasswordEncoder passwordEncoder;
    private TenantRoutingFileContentStore tenantFileContentStore;
    private WorkflowService workflowService;


    protected final static String REGEX_VALID_TENANT_NAME = "^[a-zA-Z0-9]([a-zA-Z0-9]|.[a-zA-Z0-9])*$"; // note: must also be a valid filename

    public void setNodeService(DbNodeServiceImpl dbNodeService)
    {
        this.nodeService = dbNodeService;
    }

    public void setDictionaryComponent(DictionaryComponent dictionaryComponent)
    {
        this.dictionaryComponent = dictionaryComponent;
    }

    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setTenantService(MultiTServiceImpl tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setTenantFileContentStore(TenantRoutingFileContentStore tenantFileContentStore)
    {
        this.tenantFileContentStore = tenantFileContentStore;
    }

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

	private RepositoryManager repositoryManager;

	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	private AuthenticationService authenticationService;
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public static final String PROTOCOL_STORE_USER = "user";
    public static final String PROTOCOL_STORE_WORKSPACE = "workspace";
    public static final String PROTOCOL_STORE_SYSTEM = "system";
    public static final String PROTOCOL_STORE_ARCHIVE = "archive";
    public static final String STORE_BASE_ID_USER = "alfrescoUserStore";
    public static final String STORE_BASE_ID_SYSTEM = "system";
    public static final String STORE_BASE_ID_VERSION = "lightWeightVersionStore";
    public static final String STORE_BASE_ID_SPACES = "SpacesStore";


    private static final String TENANTS_ATTRIBUTE_PATH          = "alfresco-tenants";
    private static final String TENANT_ATTRIBUTE_ENABLED        = "enabled";
    private static final String TENANT_ROOT_CONTENT_STORE_DIR   = "rootContentStoreDir";

    private static final String TENANT_CONTENT_STORE_TYPE       = "contentStore-type";
    private static final String TENANT_CONTENT_STORE_PROTOCOL   = "contentStore-protocol";
    private static final String TENANT_CONTENT_STORE_RESOURCE   = "contentStore-resource";

    private static final String ADMIN_BASENAME = TenantService.ADMIN_BASENAME;

    private Map<String,List<TenantDeployer>> tenantDeployerMap = new HashMap<String, List<TenantDeployer>>();

    // Occorre riassegnare ns nel modo corretto .. forse basta aggiustare il nodeService
    // per il momento lo splitting lo usiamo solo per la tenant delete, in futuro verrà utilizzato per tutte le operazioni
    SplittingDbNodeServiceImpl ns = null;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
    	logger.debug("[MultiTAdminServiceImpl::onBootstrap] BEGIN");

        ns = (SplittingDbNodeServiceImpl)getApplicationContext().getBean("splittingDbNodeServiceImpl");

    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
    		logger.info("[MultiTAdminServiceImpl::onBootstrap] Repository '" + RepositoryManager.getCurrentRepository() + "' -- Executing multi-tenant admin bootstrap.");

    		// initialise the tenant admin service and status of tenants (using attribute service)
			// note: this requires that the repository schema has already been initialised

			// register dictionary - to allow enable/disable tenant callbacks
			register(dictionaryComponent);

			// register file store - to allow enable/disable tenant callbacks
			register(tenantFileContentStore);

			UserTransaction userTransaction = transactionService.getUserTransaction();
			authenticationComponent.setSystemUserAsCurrentUser();

			try
			{
			    userTransaction.begin();

			    // bootstrap Tenant Service internal cache
			    List<org.alfresco.repo.tenant.Tenant> tenants = getAllTenants();

			    int enabledCount = 0;
			    int disabledCount = 0;

			    if (tenants != null)
			    {
			        for (org.alfresco.repo.tenant.Tenant tenant : tenants)
			        {
			            if (tenant.isEnabled())
			            {
			                // this will also call tenant deployers registered so far ...
			                enableTenant(tenant.getTenantDomain(), true);
			                enabledCount++;
			            }
			            else
			            {
			                // explicitly disable, without calling disableTenant callback
			                disableTenant(tenant.getTenantDomain(), false);
			                disabledCount++;
			            }
			        }

			        tenantService.register(this); // callback to refresh tenantStatus cache
			    }

			    userTransaction.commit();

			    if (logger.isInfoEnabled())
			    {
			        logger.info(String.format("Alfresco Multi-Tenant startup - %d enabled tenants, %d disabled tenants",
			                                  enabledCount, disabledCount));
			    }
			}
			catch(Throwable e)
			{
			    // rollback the transaction
			    try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
			    try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
			    throw new AlfrescoRuntimeException("Failed to bootstrap tenants", e);
			}
    	}
    	logger.debug("[MultiTAdminServiceImpl::onBootstrap] END");
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        //tenantDeployerMap.clear();
    }

    /**
     * @see TenantAdminService.createTenant()
     */
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword)
    {
        createTenant(tenantDomain, tenantAdminRawPassword, null);
    }

    /**
     * @see TenantAdminService.createTenant()
     */
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword, String rootContentStoreDir)
    {
        createTenant(tenantDomain, tenantAdminRawPassword, rootContentStoreDir, null);
    }

    /**
     * @see TenantAdminService.createTenant()
     */
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword, String rootContentStoreDir, List<ContentStoreDefinition> stores )
    {
    	logger.debug("[MultiTAdminServiceImpl::createTenant] BEGIN");
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain"          , tenantDomain          );
        ParameterCheck.mandatory("tenantAdminRawPassword", tenantAdminRawPassword);

        if (! Pattern.matches(REGEX_VALID_TENANT_NAME, tenantDomain))
        {
            throw new IllegalArgumentException(tenantDomain + " is not a valid tenant name (must match " + REGEX_VALID_TENANT_NAME + ")");
        }

        if (existsTenant(tenantDomain))
        {
            throw new AlfrescoRuntimeException("Tenant already exists: " + tenantDomain);
        }
        else
        {
            authenticationComponent.setSystemUserAsCurrentUser();

            // Se rootContentStoreDir e' null o vuoto, lo riempo col path del repository padre
            if( rootContentStoreDir==null || rootContentStoreDir.length()==0 ) {
                rootContentStoreDir = tenantFileContentStore.getDefaultRootDir();
            }

            logger.debug("[MultiTAdminServiceImpl::createTenant] rootContentStoreDir: "+rootContentStoreDir);

            // init - need to enable tenant (including tenant service) before stores bootstrap
            Tenant t = new Tenant(tenantDomain, true, rootContentStoreDir);
            // MB: Imposto i contentStore
            t.setContentStores( stores );

            putTenantAttributes(tenantDomain, t);

            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {
                        	logger.info("[MultiTAdminServiceImpl::createTenant] INIT doWork");
                            dictionaryComponent.init();
                            // DoQui 06/05/2008
                            // WORKAROUND:
                            //   Nella configurazione multirepository il metodo init() esegue
                            //   l'inizializzazione per tutti i repository configurati, ma in
                            //   questo caso e` necessario inizializzare solo il repository
                            //   corrente.
                            //   Il metodo onEnableTenant() esegue la stessa operazione, ma solo
                            //   sul repository corrente (vedere implementazione della classe
                            //   it.doqui.index.ecmengine.business.personalization.multirepository.TenantRoutingFileContentStore
                            //   per i dettagli).
                            //tenantFileContentStore.init();
                            tenantFileContentStore.onEnableTenant();

                            // create tenant-specific stores
                            bootstrapUserTenantStore(tenantDomain, tenantAdminRawPassword);
                            bootstrapSystemTenantStore(tenantDomain);
                            bootstrapVersionTenantStore(tenantDomain);
                            bootstrapSpacesArchiveTenantStore(tenantDomain);
                            bootstrapSpacesTenantStore(tenantDomain);

                            List <TenantDeployer> tenantDeployers = getTenantDeployers();
                            // notify listeners that tenant has been created & hence enabled
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onEnableTenant();
                            }

                            /*
                            // MB: Aggiorno la password dell'utente ADMIN, in modo da forzare la creazione
                            // di una nuova alf_transaction. Questa transazione e' replicata fra i nodi
                            // dei cluster, e anche sulla partizione batch, forzando la reindicizzazione
                            // dell'utente admin. Utente che, a volte, non e' indicizzato e non permette
                            // il login su partizioni batch, dopo la creazione di un tenant sulla partizione
                            // online
                            // Ci si accorge del problema, guardando la directory lucene degli user, che rimane
                            // senza document
                            */
                            /*
                            // Non risolve .. anzi .. potrebbe essere la causa di un errore di transaction already exist
                            // Nel momento in cui un nodo dlave indicizza il grappolo di transazioni
                			UserTransaction userTransaction = transactionService.getUserTransaction();
                            try
                            {
                                // Inizio la transazione
                    		    userTransaction.begin();

                                // Cambio la PWD e creo una nuova transazione
                                authenticationService.updateAuthentication( getTenantAdminUser(tenantDomain)   ,
                                                                            tenantAdminRawPassword             ,
                                                                            tenantAdminRawPassword             );

                                // Chiudo la transazione
                			    userTransaction.commit();

                            } catch(Throwable e) {
                               logger.error("[MultiTAdminServiceImpl::createTenant] update user failed " + e);
                               try {
                                 if (userTransaction != null) {
                                     userTransaction.rollback();
                                 }
                               } catch (Throwable ex) {}
                            }
                            // ---------------------------------------------------------------------------
                            */

                        	logger.info("[MultiTAdminServiceImpl::createTenant] END doWork");

                            return null;
                        }
                    }, getTenantAdminUser(tenantDomain));
        }

        logger.info("Tenant created: " + tenantDomain);
    	logger.debug("[MultiTAdminServiceImpl::createTenant] END");
    }

    public boolean existsTenant(String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        return (getTenantAttributes(tenantDomain) != null);
    }

    private void putTenantAttributes(String tenantDomain, Tenant tenant)
    {
        if (! attributeService.exists(TENANTS_ATTRIBUTE_PATH))
        {
            // bootstrap
            attributeService.setAttribute("", TENANTS_ATTRIBUTE_PATH, new MapAttributeValue());
        }

        MapAttribute tenantProps = new MapAttributeValue();
        if( logger.isDebugEnabled() ){
           logger.debug("[MultiTAdminServiceImpl::putTenantAttributes] BooleanAttributeValue(tenant.isEnabled()): "+new BooleanAttributeValue(tenant.isEnabled()));
           logger.debug("[MultiTAdminServiceImpl::putTenantAttributes] StringAttributeValue(tenant.getRootContentStoreDir()): "+new StringAttributeValue(tenant.getRootContentStoreDir()));
        }
        tenantProps.put(TENANT_ATTRIBUTE_ENABLED     , new BooleanAttributeValue(tenant.isEnabled()));
        tenantProps.put(TENANT_ROOT_CONTENT_STORE_DIR, new StringAttributeValue(tenant.getRootContentStoreDir()));

        // MB: aggiungo la lista dei bean di content store
    	logger.debug("[MultiTAdminServiceImpl::putTenantAttributes] getContentStore");
        if( tenant.getContentStores()!=null ){
           int nStore = 0;
           for( Object cs: tenant.getContentStores() ){
        	   ContentStoreDefinition tcs = (ContentStoreDefinition)cs;

               if( logger.isDebugEnabled() ){
                  logger.debug("[MultiTAdminServiceImpl::putTenantAttributes] contentStore (" +nStore +") " +tcs.getType() +":" +tcs.getProtocol() +"->" +tcs.getResource() );
               }

               tenantProps.put(TENANT_CONTENT_STORE_TYPE	+nStore, new StringAttributeValue( tcs.getType()     ));
               tenantProps.put(TENANT_CONTENT_STORE_PROTOCOL+nStore, new StringAttributeValue( tcs.getProtocol() ));
               tenantProps.put(TENANT_CONTENT_STORE_RESOURCE+nStore, new StringAttributeValue( tcs.getResource() ));
               nStore++;
           }
        }

        attributeService.setAttribute(TENANTS_ATTRIBUTE_PATH, tenantDomain, tenantProps);
        //attributeService.setAttribute(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain, tenantDomain, tenantProps);
        // update tenant status cache
        ((MultiTServiceImpl)tenantService).putTenant(tenantDomain, tenant);
    }

    private Tenant getTenantAttributes(String tenantDomain)
    {

        if( logger.isDebugEnabled() ){
           logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] exist11 " + attributeService.exists(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain));
           logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] exist12 " + attributeService.exists(TENANTS_ATTRIBUTE_PATH));
           logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] exist13 " + attributeService.exists(tenantDomain));
           logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] getAttribute11 "+(MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain));
           logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] getAttribute12 "+(MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH));
           logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] getAttribute13 "+(MapAttribute)attributeService.getAttribute(tenantDomain));
        }

    	/*orig*/ if (attributeService.exists(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain))
    	//if (attributeService.exists(TENANTS_ATTRIBUTE_PATH))
        {
            /*orig*/MapAttribute map = (MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain);
            //MapAttribute map = (MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH);
            if (map != null)
            {
                if( logger.isDebugEnabled() ){
                   logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] map " +map);
                   logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] tenantDomain: " +tenantDomain);
                   logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] map.get TENANT_ATTRIBUTE_ENABLED: " +map.get(TENANT_ATTRIBUTE_ENABLED));
                   logger.debug("[MultiTAdminServiceImpl::getTenantAttributes] map.get TENANT_ROOT_CONTENT_STORE_DIR: " +map.get(TENANT_ROOT_CONTENT_STORE_DIR));
                }
                Tenant t = new Tenant(tenantDomain,
                                      map.get(TENANT_ATTRIBUTE_ENABLED     ).getBooleanValue(),
                                      map.get(TENANT_ROOT_CONTENT_STORE_DIR).getStringValue());

                // MB: Leggo i contentStore
                List<ContentStoreDefinition> tcs = new ArrayList<ContentStoreDefinition>();
                int nStore = 0;
                while( true ){
                    Attribute at = map.get( TENANT_CONTENT_STORE_TYPE	 +nStore );
                    if( at==null ) break;

                    Attribute ap = map.get( TENANT_CONTENT_STORE_PROTOCOL+nStore );
                    if( ap==null ) break;

                    Attribute ar = map.get( TENANT_CONTENT_STORE_RESOURCE+nStore );
                    if( ar==null ) break;

                    ContentStoreDefinition cs = new ContentStoreDefinition();
                                           cs.setType(     at.getStringValue() );
                                           cs.setProtocol( ap.getStringValue() );
                                           cs.setResource( ar.getStringValue() );

                    tcs.add( cs );
                    nStore++;
                }
                t.setContentStores( tcs );

                return t;
            }
        }

        return null;
    }

    public void enableTenant(String tenantDomain)
    {
        if (isEnabledTenant(tenantDomain))
        {
            logger.warn("Tenant already enabled: " + tenantDomain);
        }

        enableTenant(tenantDomain, true);
    }

    private void enableTenant(String tenantDomain, boolean notifyTenantDeployers)
    {
    	logger.debug("[MultiTAdminServiceImpl::enableTenant] BEGIN");
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        Tenant tenantAtr = getTenantAttributes(tenantDomain);
        Tenant tenant = new Tenant(tenantDomain, true, tenantAtr.getRootContentStoreDir()); // enable
        tenant.setContentStores(tenantAtr.getContentStores());
        putTenantAttributes(tenantDomain, tenant);

        if (notifyTenantDeployers)
        {
            // notify listeners that tenant has been enabled
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {
                            List <TenantDeployer> tenantDeployers = getTenantDeployers();
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onEnableTenant();
                            }
                            return null;
                        }
                    }, getTenantAdminUser(tenantDomain));
        }

        logger.debug("[MultiTAdminServiceImpl::enableTenant] Tenant enabled: " + tenantDomain);
    	logger.debug("[MultiTAdminServiceImpl::enableTenant] END");
    }

    public void disableTenant(String tenantDomain)
    {
        if (! isEnabledTenant(tenantDomain))
        {
            logger.warn("Tenant already disabled: " + tenantDomain);
        }

        disableTenant(tenantDomain, true);
    }

    public void disableTenant(String tenantDomain, boolean notifyTenantDeployers)
    {
        if (notifyTenantDeployers)
        {
            // notify listeners that tenant has been disabled
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {
                            List <TenantDeployer> tenantDeployers = getTenantDeployers();
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onDisableTenant();
                            }
                            return null;
                        }
                    }, getTenantAdminUser(tenantDomain));
        }

        // update tenant attributes / tenant cache - need to disable after notifying listeners (else they cannot disable)
        Tenant tenantAtr = getTenantAttributes(tenantDomain);
        Tenant tenant = new Tenant(tenantDomain, false, tenantAtr.getRootContentStoreDir()); // disable
        tenant.setContentStores(tenantAtr.getContentStores());
        putTenantAttributes(tenantDomain, tenant);

        logger.debug("[MultiTAdminServiceImpl::disableTenant] Tenant disabled: " + tenantDomain);
    }

    public boolean isEnabledTenant(String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        Tenant tenant = getTenantAttributes(tenantDomain);
        if (tenant != null)
        {
            return tenant.isEnabled();
        }

        return false;
    }

    protected String getRootContentStoreDir(String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        Tenant tenant = getTenantAttributes(tenantDomain);
        if (tenant != null)
        {
            return tenant.getRootContentStoreDir();
        }

        return null;
    }

    protected void putRootContentStoreDir(String tenantDomain, String rootContentStoreDir)
    {
        Tenant tenantAtr = getTenantAttributes(tenantDomain);
        Tenant tenant = new Tenant(tenantDomain, tenantAtr.isEnabled(), rootContentStoreDir);
        tenant.setContentStores(tenantAtr.getContentStores());
        putTenantAttributes(tenantDomain, tenant);
    }

    public Tenant getTenant(String tenantDomain)
    {
        Tenant tenant = new Tenant(tenantDomain, isEnabledTenant(tenantDomain), getRootContentStoreDir(tenantDomain));

        Tenant tenantAtr = getTenantAttributes(tenantDomain);
        if( tenantAtr!=null ){
            tenant.setContentStores(tenantAtr.getContentStores());
        }
        return tenant;
    }

    public void bootstrapWorkflows()
    {
        // use this to deploy standard workflow process defs to the JBPM engine
        WorkflowDeployer workflowBootstrap = (WorkflowDeployer)getApplicationContext().getBean("workflowBootstrap");

        String resourceClasspath = null;

        // Workflow process definitions
        try
        {
            List<Properties> workflowDefs = workflowBootstrap.getWorkflowDefinitions();
            if (workflowDefs != null)
            {
                for (Properties workflowDefProps : workflowDefs)
                {
                    resourceClasspath = workflowDefProps.getProperty(WorkflowDeployer.LOCATION);
                    ClassPathResource resource = new ClassPathResource(resourceClasspath);
                    workflowService.deployDefinition(workflowDefProps.getProperty(WorkflowDeployer.ENGINE_ID), resource.getInputStream(), workflowDefProps.getProperty(WorkflowDeployer.MIMETYPE));
                }
            }
        }
        catch (IOException ioe)
        {
            throw new AlfrescoRuntimeException("Failed to find workflow process def: " + resourceClasspath);
        }

        logger.info("Tenant workflows bootstrapped: " + tenantService.getCurrentUserDomain());
    }

    /**
     * @see TenantAdminService.deleteTenant()
     */
    public void deleteTenant(String tenantDomain)
    {
        if (! existsTenant(tenantDomain))
        {
            throw new RuntimeException("Tenant does not exist: " + tenantDomain);
        }
        else
        {
            try
            {
                final String tenantAdminUser = getTenantAdminUser(tenantDomain);
                //final String tenantAdminUser = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);

                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
                        if (workflowDefs != null)
                        {
                            for (WorkflowDefinition workflowDef : workflowDefs)
                            {
                                workflowService.undeployDefinition(workflowDef.getId());
                            }
                        }

                        List<String> messageResourceBundles = repoAdminService.getMessageBundles();
                        if (messageResourceBundles != null)
                        {
                            for (String messageResourceBundle : messageResourceBundles)
                            {
                                repoAdminService.undeployMessageBundle(messageResourceBundle);
                            }
                        }

                        List<RepoModelDefinition> models = repoAdminService.getModels();
                        if (models != null)
                        {
                            for (RepoModelDefinition model : models)
                            {
                                repoAdminService.undeployModel(model.getRepoName());
                            }
                        }

                        return null;
                    }
                }, tenantAdminUser);


                //-------------------------------------
                UserTransaction userTransaction = transactionService.getUserTransaction();
                authenticationComponent.setSystemUserAsCurrentUser();
                try
                {
                    // TODO: occorre usare lo SplittingDbNodeServiceImpl
                    // che ha dentro un deleteStore che aggiorna gli indici
                    // ora e' usata l'imlementation di ALF che ha il metodo ma non aggiorna gli indici di lucene
                    userTransaction.begin();

                    ns.deleteStore( tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_SPACES)  ) );
                    ns.deleteStore( tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_ARCHIVE, STORE_BASE_ID_SPACES)    ) );
                    ns.deleteStore( tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_VERSION) ) );
                    ns.deleteStore( tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_SYSTEM, STORE_BASE_ID_SYSTEM)     ) );
                    ns.deleteStore( tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_USER, STORE_BASE_ID_USER)         ) );

                    userTransaction.commit();

                }
                catch(Throwable e)
                {
                    // rollback the transaction
                    try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
                    try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
                    throw new AlfrescoRuntimeException("Failed to delete tenant", e);
                }

                // notify listeners that tenant has been deleted & hence disabled
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        List <TenantDeployer> tenantDeployers = getTenantDeployers();
                        for (TenantDeployer tenantDeployer : tenantDeployers)
                        {
                            tenantDeployer.onDisableTenant();
                        }
                        return null;
                    }
                }, tenantAdminUser);

                // remove tenant
                attributeService.removeAttribute(TENANTS_ATTRIBUTE_PATH, tenantDomain);
            }
            catch (Throwable t)
            {
                throw new AlfrescoRuntimeException("Failed to delete tenant: " + tenantDomain, t);
            }
        }
    }


    /**
     * @see TenantAdminService.getAllTenants()
     */
    public List<Tenant> getAllTenantsDoqui()
    {
        MapAttribute map = (MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH);

        List<Tenant> tenants = new ArrayList<Tenant>();

        if (map != null)
        {
            // note: getAllTenants is called first, by TenantDeployer - hence need to initialise the TenantService status cache
            Set<String> tenantDomains = map.keySet();

            for (String tenantDomain : tenantDomains)
            {
                Tenant tenant = getTenantAttributes(tenantDomain);

                // Create Tenant
                Tenant tenant2Add = new Tenant(tenantDomain, tenant.isEnabled(), tenant.getRootContentStoreDir());
                tenant2Add.setContentStores(tenant.getContentStores());

                // Add it to tenants list
                tenants.add(tenant2Add);
            }
        }

        return tenants; // list of tenants or empty list
    }

    /**
     * @see TenantAdminService.getAllTenants()
     */
    public List<org.alfresco.repo.tenant.Tenant> getAllTenants()
    {
    	List<org.alfresco.repo.tenant.Tenant> tenants = new ArrayList<org.alfresco.repo.tenant.Tenant>();

    	List<Tenant> lt = getAllTenantsDoqui();
    	for( Tenant t : lt){
    		// Add it to tenants list
    		tenants.add(t);
    	}

    	return tenants; // list of tenants or empty list
    }


    private void bootstrapUserTenantStore(String tenantDomain, char[] tenantAdminRawPassword)
    {
        // Bootstrap Tenant-Specific User Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_USER, tenantService.getName(STORE_BASE_ID_USER, tenantDomain));

        ImporterBootstrap userImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("userBootstrap");
        userImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());

        // override admin username property
        String salt = null; // GUID.generate();
        Properties props = userImporterBootstrap.getConfiguration();

        props.put("alfresco_user_store.adminusername", getTenantAdminUser(tenantDomain));
        props.put("alfresco_user_store.adminpassword", passwordEncoder.encodePassword(new String(tenantAdminRawPassword), salt));

        userImporterBootstrap.bootstrap();

        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    private void bootstrapSystemTenantStore(String tenantDomain)
    {
        // Bootstrap Tenant-Specific System Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_SYSTEM, tenantService.getName(STORE_BASE_ID_SYSTEM, tenantDomain));

        ImporterBootstrap systemImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("systemBootstrap");
        systemImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());

        // override default property (workspace://SpacesStore)
        List<String> mustNotExistStoreUrls = new ArrayList<String>();
        mustNotExistStoreUrls.add(new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_USER, tenantDomain)).toString());
        systemImporterBootstrap.setMustNotExistStoreUrls(mustNotExistStoreUrls);

        systemImporterBootstrap.bootstrap();

        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    private void bootstrapVersionTenantStore(String tenantDomain)
    {
        // Bootstrap Tenant-Specific Version Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_VERSION, tenantDomain));

        ImporterBootstrap versionImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("versionBootstrap");
        versionImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());

        versionImporterBootstrap.bootstrap();

        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    private void bootstrapSpacesArchiveTenantStore(String tenantDomain)
    {
        // Bootstrap Tenant-Specific Spaces Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_ARCHIVE, tenantService.getName(STORE_BASE_ID_SPACES, tenantDomain));

        ImporterBootstrap spacesArchiveImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesArchiveBootstrap");
        spacesArchiveImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());

        // override default property (archive://SpacesStore)
        List<String> mustNotExistStoreUrls = new ArrayList<String>();
        mustNotExistStoreUrls.add(new StoreRef(PROTOCOL_STORE_ARCHIVE, tenantService.getName(STORE_BASE_ID_SPACES, tenantDomain)).toString());
        spacesArchiveImporterBootstrap.setMustNotExistStoreUrls(mustNotExistStoreUrls);

        spacesArchiveImporterBootstrap.bootstrap();

        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    private void bootstrapSpacesTenantStore(String tenantDomain)
    {
        // Bootstrap Tenant-Specific Spaces Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_SPACES, tenantDomain));

        final ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesBootstrap");
        spacesImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());

        // override admin username property
        Properties props = spacesImporterBootstrap.getConfiguration();
        props.put("alfresco_user_store.adminusername", getTenantAdminUser(tenantDomain));

        // override guest username property
        props.put("alfresco_user_store.guestusername", getTenantGuestUser(tenantDomain));

        spacesImporterBootstrap.bootstrap();

        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    public void deployTenants(final TenantDeployer deployer, Log logger)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }
        if (logger == null)
        {
            throw new AlfrescoRuntimeException("Logger must be provided");
        }

        if (tenantService.isEnabled())
        {
            UserTransaction userTransaction = transactionService.getUserTransaction();
            authenticationComponent.setSystemUserAsCurrentUser();

            List<org.alfresco.repo.tenant.Tenant> tenants = null;
            try
            {
                userTransaction.begin();
                tenants = getAllTenants();
                userTransaction.commit();
            }
            catch(Throwable e)
            {
                // rollback the transaction
                try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
                try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
                throw new AlfrescoRuntimeException("Failed to get tenants", e);
            }

            String currentUser = AuthenticationUtil.getCurrentUserName();

            if (tenants != null)
            {
                try
                {
                    for (org.alfresco.repo.tenant.Tenant tenant : tenants)
                    {
                        if (tenant.isEnabled())
                        {
                            try
                            {
                                // switch to admin in order to deploy within context of tenant domain
                                // assumes each tenant has default "admin" user
                                AuthenticationUtil.runAs(new RunAsWork<Object>()
                                {
                                    public Object doWork()
                                    {
                                        // init the service within tenant context
                                        deployer.init();
                                        return null;
                                    }
                                }, getTenantAdminUser(tenant.getTenantDomain()));

                            }
                            catch (Throwable e)
                            {
                                logger.error("Deployment failed" + e);

                                StringWriter stringWriter = new StringWriter();
                                e.printStackTrace(new PrintWriter(stringWriter));
                                logger.error(stringWriter.toString());

                                // tenant deploy failure should not necessarily affect other tenants
                            }
                        }
                    }
                }
                finally
                {
                    if (currentUser != null) { AuthenticationUtil.setCurrentUser(currentUser); }
                }
            }
        }
    }

    public void undeployTenants(final TenantDeployer deployer, Log logger)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }
        if (logger == null)
        {
            throw new AlfrescoRuntimeException("Logger must be provided");
        }

        if (tenantService.isEnabled())
        {
            UserTransaction userTransaction = transactionService.getUserTransaction();
            authenticationComponent.setSystemUserAsCurrentUser();

            List<org.alfresco.repo.tenant.Tenant> tenants = null;
            try
            {
                userTransaction.begin();
                tenants = getAllTenants();
                userTransaction.commit();
            }
            catch(Throwable e)
            {
                // rollback the transaction
                try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
                try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
                throw new AlfrescoRuntimeException("Failed to get tenants", e);
            }

            String currentUser = AuthenticationUtil.getCurrentUserName();

            if (tenants != null)
            {
                try
                {
                    for (org.alfresco.repo.tenant.Tenant tenant : tenants)
                    {
                        if (tenant.isEnabled())
                        {
                            try
                            {
                                // switch to admin in order to deploy within context of tenant domain
                                // assumes each tenant has default "admin" user
                                AuthenticationUtil.runAs(new RunAsWork<Object>()
                                {
                                    public Object doWork()
                                    {
                                        // destroy the service within tenant context
                                        deployer.destroy();
                                        return null;
                                    }
                                }, getTenantAdminUser(tenant.getTenantDomain()));

                            }
                            catch (Throwable e)
                            {
                                logger.error("Undeployment failed" + e);

                                StringWriter stringWriter = new StringWriter();
                                e.printStackTrace(new PrintWriter(stringWriter));
                                logger.error(stringWriter.toString());

                                // tenant undeploy failure should not necessarily affect other tenants
                            }
                        }
                    }
                }
                finally
                {
                    if (currentUser != null) { AuthenticationUtil.setCurrentUser(currentUser); }
                }
            }
        }
    }

    public void register(TenantDeployer deployer)
    {
    	logger.debug("[MultiTAdminServiceImpl::register] BEGIN");
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }

        List <TenantDeployer> tenantDeployers = getTenantDeployers();
        if (! tenantDeployers.contains(deployer))
        {
            tenantDeployers.add(deployer);
        }
    	logger.debug("[MultiTAdminServiceImpl::register] END");
    }

    public void unregister(TenantDeployer deployer)
    {
    	String currentRepository = RepositoryManager.getCurrentRepository();
    	for (Repository repository : repositoryManager.getRepositories()) {
    		RepositoryManager.setCurrentRepository(repository.getId());
	        if (deployer == null)
	        {
	            throw new AlfrescoRuntimeException("Deployer must be provided");
	        }

	        List <TenantDeployer> tenantDeployers = getTenantDeployers();
	        if (tenantDeployers != null)
	        {
	            tenantDeployers.remove(deployer);
	        }
    	}
    	RepositoryManager.setCurrentRepository(currentRepository);
    	tenantDeployerMap.clear();
    }

    public boolean isEnabled()
    {
        return tenantService.isEnabled();
    }

    public void resetCache(String tenantDomain)
    {
        if (existsTenant(tenantDomain))
        {
            if (isEnabledTenant(tenantDomain))
            {
                enableTenant(tenantDomain);
            }
            else
            {
                disableTenant(tenantDomain);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("No such tenant " + tenantDomain);
        }
    }

    // local helper
    private String getTenantAdminUser(String tenantDomain)
    {
        return tenantService.getDomainUser(ADMIN_BASENAME, tenantDomain);
    }

    // local helper
    private String getTenantGuestUser(String tenantDomain)
    {
        return tenantService.getDomainUser(authenticationComponent.getGuestUserName(), tenantDomain);
    }

    private List<TenantDeployer> getTenantDeployers() {
        String currentRepositoryId = RepositoryManager.getCurrentRepository();
        if(logger.isDebugEnabled()) {
    	    logger.debug("[MultiTAdminServiceImpl::getTenantDeployers] getting tenant deployers for repository '"+currentRepositoryId+"'");
    	}
    	List<TenantDeployer> tenantDeployers = tenantDeployerMap.get(currentRepositoryId);
    	if (tenantDeployers == null) {
    		tenantDeployers = new ArrayList<TenantDeployer>();
    		tenantDeployerMap.put(currentRepositoryId, tenantDeployers);
    	}
    	return tenantDeployers;
    }

}

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
 
package it.doqui.index.ecmengine.business.personalization.hibernate;

import it.doqui.index.ecmengine.business.personalization.hibernate.util.EcmEngineHibernateConstants;
import it.doqui.index.ecmengine.business.personalization.multirepository.Repository;
import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.SessionFactoryProxy;

import java.io.File;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.event.EventListeners;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateInterceptor;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalTransactionManagerLookup;
import org.springframework.orm.hibernate3.TransactionAwareDataSourceConnectionProvider;
import org.springframework.orm.hibernate3.TypeDefinitionBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Implementazione alternativa di {@code RoutingLocalSessionFactoryBean} con supporto al multirepository.
 * 
 * @author DoQui
 * 
 * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean
 */
public class RoutingLocalSessionFactoryBean extends org.springframework.orm.hibernate3.LocalSessionFactoryBean {

	private static final ThreadLocal<DataSource> CONFIG_TIME_DS_HOLDER = new ThreadLocal<DataSource>();

	private static final ThreadLocal<TransactionManager> CONFIG_TIME_TM_HOLDER = new ThreadLocal<TransactionManager>();

	private static final ThreadLocal<LobHandler> CONFIG_TIME_LOB_HANDLER_HOLDER = new ThreadLocal<LobHandler>();

	private static RepositoryManager repositoryManager;

	/**
	 * Setter <i>IoC</i> per l'impostazione del <i>repository manager</i>.
	 * 
	 * @param repositoryManager L'istanza di {@code RepositoryManager}.
	 */
	public void setRepositoryManager(RepositoryManager repositoryManager) {
		RoutingLocalSessionFactoryBean.repositoryManager = repositoryManager;
	}

	/**
	 * Return the DataSource for the currently configured Hibernate SessionFactory,
	 * to be used by LocalDataSourceConnectionProvoder.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setDataSource
	 * @see RoutingLocalDataSourceConnectionProvider
	 */
	public static DataSource getConfigTimeDataSource() {
		return (DataSource) CONFIG_TIME_DS_HOLDER.get();
	}

	/**
	 * Return the JTA TransactionManager for the currently configured Hibernate
	 * SessionFactory, to be used by LocalTransactionManagerLookup.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setJtaTransactionManager
	 * @see LocalTransactionManagerLookup
	 */
	public static TransactionManager getConfigTimeTransactionManager() {
		return (TransactionManager) CONFIG_TIME_TM_HOLDER.get();
	}

	/**
	 * Return the LobHandler for the currently configured Hibernate SessionFactory,
	 * to be used by UserType implementations like ClobStringType.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setLobHandler
	 * @see org.springframework.orm.hibernate3.support.ClobStringType
	 * @see org.springframework.orm.hibernate3.support.BlobByteArrayType
	 * @see org.springframework.orm.hibernate3.support.BlobSerializableType
	 */
	public static LobHandler getConfigTimeLobHandler() {
		return (LobHandler) CONFIG_TIME_LOB_HANDLER_HOLDER.get();
	}

	private Class<?> configurationClass = Configuration.class;

	private Resource[] configLocations;

	private Resource[] mappingLocations;

	private Resource[] cacheableMappingLocations;

	private Resource[] mappingJarLocations;

	private Resource[] mappingDirectoryLocations;

	private Properties hibernateProperties;

	private boolean useTransactionAwareDataSource = false;

	private TransactionManager jtaTransactionManager;

	private LobHandler lobHandler;

	private Interceptor entityInterceptor;

	private NamingStrategy namingStrategy;

	private TypeDefinitionBean[] typeDefinitions;

	private FilterDefinition[] filterDefinitions;

	private Properties entityCacheStrategies;

	private Properties collectionCacheStrategies;

	@SuppressWarnings("unchecked")
	private Map<?, ?> eventListeners;

	private boolean schemaUpdate = false;
	
	private Configuration configuration;
	
	private static Log logger = LogFactory.getLog(EcmEngineHibernateConstants.HIBERNATE_LOG_CATEGORY);

	/**
	 * Specify the Hibernate Configuration class to use.
	 * 
	 * <p>Default is "org.hibernate.cfg.Configuration"; any subclass of
	 * this default Hibernate Configuration class can be specified.</p>
	 * 
	 * <p>Can be set to "org.hibernate.cfg.AnnotationConfiguration" for
	 * using Hibernate3 annotation support (initially only available as
	 * alpha download separate from the main Hibernate3 distribution).</p>
	 * 
	 * <p>Annotated packages and annotated classes can be specified via the
	 * corresponding tags in "hibernate.cfg.xml" then, so this will usually
	 * be combined with a "configLocation" property that points at such a
	 * standard Hibernate configuration file.</p>
	 * 
	 * @see #setConfigLocation
	 * @see org.hibernate.cfg.Configuration
	 * @see org.hibernate.cfg.AnnotationConfiguration
	 */
	@SuppressWarnings("unchecked")
	public void setConfigurationClass(Class configurationClass) {
		if (configurationClass == null || !Configuration.class.isAssignableFrom(configurationClass)) {
			throw new IllegalArgumentException(
					"configurationClass must be assignable to [org.hibernate.cfg.Configuration]");
		}
		this.configurationClass = configurationClass;
	}

	/**
	 * Set the location of a single Hibernate XML config file, for example as
	 * classpath resource "classpath:hibernate.cfg.xml".
	 * 
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean.</p>
	 * 
	 * @see org.hibernate.cfg.Configuration#configure(java.net.URL)
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocations = new Resource[] {configLocation};
	}

	/**
	 * Set the locations of multiple Hibernate XML config files, for example as
	 * classpath resources "classpath:hibernate.cfg.xml,classpath:extension.cfg.xml".
	 * 
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean.</p>
	 * 
	 * @see org.hibernate.cfg.Configuration#configure(java.net.URL)
	 */
	public void setConfigLocations(Resource[] configLocations) {
		this.configLocations = configLocations;
	}

	/**
	 * Set Hibernate mapping resources to be found in the class path,
	 * like "example.hbm.xml" or "mypackage/example.hbm.xml".
	 * 
	 * Analogous to mapping entries in a Hibernate XML config file.
	 * Alternative to the more generic setMappingLocations method.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see #setMappingLocations
	 * @see org.hibernate.cfg.Configuration#addResource
	 */
	public void setMappingResources(String[] mappingResources) {
		this.mappingLocations = new Resource[mappingResources.length];
		for (int i = 0; i < mappingResources.length; i++) {
			this.mappingLocations[i] = new ClassPathResource(mappingResources[i].trim());
		}
	}

	/**
	 * Set locations of Hibernate mapping files, for example as classpath
	 * resource "classpath:example.hbm.xml". Supports any resource location
	 * via Spring's resource abstraction, for example relative paths like
	 * "WEB-INF/mappings/example.hbm.xml" when running in an application context.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addInputStream
	 */
	public void setMappingLocations(Resource[] mappingLocations) {
		this.mappingLocations = mappingLocations;
	}

	/**
	 * Set locations of cacheable Hibernate mapping files, for example as web app
	 * resource "/WEB-INF/mapping/example.hbm.xml". Supports any resource location
	 * via Spring's resource abstraction, as long as the resource can be resolved
	 * in the file system.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addCacheableFile(java.io.File)
	 */
	public void setCacheableMappingLocations(Resource[] cacheableMappingLocations) {
		this.cacheableMappingLocations = cacheableMappingLocations;
	}

	/**
	 * Set locations of jar files that contain Hibernate mapping resources,
	 * like "WEB-INF/lib/example.hbm.jar".
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addJar(java.io.File)
	 */
	public void setMappingJarLocations(Resource[] mappingJarLocations) {
		this.mappingJarLocations = mappingJarLocations;
	}

	/**
	 * Set locations of directories that contain Hibernate mapping resources,
	 * like "WEB-INF/mappings".
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addDirectory(java.io.File)
	 */
	public void setMappingDirectoryLocations(Resource[] mappingDirectoryLocations) {
		this.mappingDirectoryLocations = mappingDirectoryLocations;
	}

	/**
	 * Set Hibernate properties, such as "hibernate.dialect".
	 * <p>Can be used to override values in a Hibernate XML config file,
	 * or to specify all necessary properties locally.
	 * <p>Note: Do not specify a transaction provider here when using
	 * Spring-driven transactions. It is also advisable to omit connection
	 * provider settings and use a Spring-set DataSource instead.
	 * @see #setDataSource
	 */
	public void setHibernateProperties(Properties hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	/**
	 * Return the Hibernate properties, if any. Mainly available for
	 * configuration through property paths that specify individual keys.
	 */
	public Properties getHibernateProperties() {
		if (this.hibernateProperties == null) {
			this.hibernateProperties = new Properties();
		}
		return this.hibernateProperties;
	}

	/**
	 * Set whether to use a transaction-aware DataSource for the SessionFactory,
	 * i.e. whether to automatically wrap the passed-in DataSource with Spring's
	 * TransactionAwareDataSourceProxy.
	 * <p>Default is "false": RoutingLocalSessionFactoryBean is usually used with Spring's
	 * HibernateTransactionManager or JtaTransactionManager, both of which work nicely
	 * on a plain JDBC DataSource. Hibernate Sessions and their JDBC Connections are
	 * fully managed by the Hibernate/JTA transaction infrastructure in such a scenario.
	 * <p>If you switch this flag to "true", Spring's Hibernate access will be able to
	 * <i>participate in JDBC-based transactions managed outside of Hibernate</i>
	 * (for example, by Spring's DataSourceTransactionManager). This can be convenient
	 * if you need a different local transaction strategy for another O/R mapping tool,
	 * for example, but still want Hibernate access to join into those transactions.
	 * <p>A further benefit of this option is that <i>plain Sessions opened directly
	 * via the SessionFactory</i>, outside of Spring's Hibernate support, will still
	 * participate in active Spring-managed transactions. However, consider using
	 * Hibernate's <code>getCurrentSession()</code> method instead (see javadoc of
	 * "exposeTransactionAwareSessionFactory" property).
	 * <p>As a further effect, using a transaction-aware DataSource will <i>apply
	 * remaining transaction timeouts to all created JDBC Statements</i>. This means
	 * that all operations performed by the SessionFactory will automatically
	 * participate in Spring-managed transaction timeouts, not just queries.
	 * This adds value even for HibernateTransactionManager, but only on Hibernate 3.0,
	 * as there is a direct transaction timeout facility in Hibernate 3.1.
	 * <p><b>WARNING:</b> When using a transaction-aware JDBC DataSource in combination
	 * with OpenSessionInViewFilter/Interceptor, whether participating in JTA or
	 * external JDBC-based transactions, it is strongly recommended to set Hibernate's
	 * Connection release mode to "after_transaction" or "after_statement", which
	 * guarantees proper Connection handling in such a scenario. In contrast to that,
	 * HibernateTransactionManager generally requires release mode "on_close".
	 * <p>Note: If you want to use Hibernate's Connection release mode "after_statement"
	 * with a DataSource specified on this RoutingLocalSessionFactoryBean (for example, a
	 * JTA-aware DataSource fetched from JNDI), switch this setting to "true".
	 * Else, the ConnectionProvider used underneath will vote against aggressive
	 * release and thus silently switch to release mode "after_transaction".
	 * @see #setDataSource
	 * @see #setExposeTransactionAwareSessionFactory
	 * @see org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
	 * @see HibernateTransactionManager
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	public void setUseTransactionAwareDataSource(boolean useTransactionAwareDataSource) {
		this.useTransactionAwareDataSource = useTransactionAwareDataSource;
	}

	/**
	 * Set the JTA TransactionManager to be used for Hibernate's
	 * TransactionManagerLookup. If set, this will override corresponding
	 * settings in Hibernate properties. Allows to use a Spring-managed
	 * JTA TransactionManager for Hibernate's cache synchronization.
	 * <p>Note: If this is set, the Hibernate settings should not define a
	 * transaction manager lookup to avoid meaningless double configuration.
	 * @see LocalTransactionManagerLookup
	 */
	public void setJtaTransactionManager(TransactionManager jtaTransactionManager) {
		this.jtaTransactionManager = jtaTransactionManager;
	}

	/**
	 * Set the LobHandler to be used by the SessionFactory.
	 * Will be exposed at config time for UserType implementations.
	 * @see #getConfigTimeLobHandler
	 * @see org.hibernate.usertype.UserType
	 * @see org.springframework.orm.hibernate3.support.ClobStringType
	 * @see org.springframework.orm.hibernate3.support.BlobByteArrayType
	 * @see org.springframework.orm.hibernate3.support.BlobSerializableType
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this factory.
	 * <p>Such an interceptor can either be set at the SessionFactory level, i.e. on
	 * RoutingLocalSessionFactoryBean, or at the Session level, i.e. on HibernateTemplate,
	 * HibernateInterceptor, and HibernateTransactionManager. It's preferable to set
	 * it on RoutingLocalSessionFactoryBean or HibernateTransactionManager to avoid repeated
	 * configuration and guarantee consistent behavior in transactions.
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 * @see HibernateTransactionManager#setEntityInterceptor
	 * @see org.hibernate.cfg.Configuration#setInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Set a Hibernate NamingStrategy for the SessionFactory, determining the
	 * physical column and table names given the info in the mapping document.
	 * @see org.hibernate.cfg.Configuration#setNamingStrategy
	 */
	public void setNamingStrategy(NamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * Specify the Hibernate type definitions to register with the SessionFactory,
	 * as Spring TypeDefinitionBean instances. This is an alternative to specifying
	 * <&lt;typedef&gt; elements in Hibernate mapping files.
	 * <p>Unfortunately, Hibernate itself does not define a complete object that
	 * represents a type definition, hence the need for Spring's TypeDefinitionBean.
	 * @see TypeDefinitionBean
	 * @see org.hibernate.cfg.Mappings#addTypeDef(String, String, java.util.Properties)
	 */
	public void setTypeDefinitions(TypeDefinitionBean[] typeDefinitions) {
		this.typeDefinitions = typeDefinitions;
	}

	/**
	 * Specify the Hibernate FilterDefinitions to register with the SessionFactory.
	 * This is an alternative to specifying <&lt;filter-def&gt; elements in
	 * Hibernate mapping files.
	 * <p>Typically, the passed-in FilterDefinition objects will have been defined
	 * as Spring FilterDefinitionFactoryBeans, probably as inner beans within the
	 * RoutingLocalSessionFactoryBean definition.
	 * @see FilterDefinitionFactoryBean
	 * @see org.hibernate.cfg.Configuration#addFilterDefinition
	 */
	public void setFilterDefinitions(FilterDefinition[] filterDefinitions) {
		this.filterDefinitions = filterDefinitions;
	}

	/**
	 * Specify the cache strategies for entities (persistent classes or named entities).
	 * This configuration setting corresponds to the &lt;class-cache&gt; entry
	 * in the "hibernate.cfg.xml" configuration format.
	 * <p>For example:
	 * <pre>
	 * &lt;property name="entityCacheStrategies"&gt;
	 *   &lt;props&gt;
	 *     &lt;prop key="com.mycompany.Customer"&gt;read-write&lt;/prop&gt;
	 *     &lt;prop key="com.mycompany.Product"&gt;read-only,myRegion&lt;/prop&gt;
	 *   &lt;/props&gt;
	 * &lt;/property&gt;</pre>
	 * Note that appending a cache region name (with a comma separator) is only
	 * supported on Hibernate 3.1, where this functionality is publically available.
	 * @param entityCacheStrategies properties that define entity cache strategies,
	 * with class names as keys and cache concurrency strategies as values
	 * @see org.hibernate.cfg.Configuration#setCacheConcurrencyStrategy(String, String)
	 */
	public void setEntityCacheStrategies(Properties entityCacheStrategies) {
		this.entityCacheStrategies = entityCacheStrategies;
	}

	/**
	 * Specify the cache strategies for persistent collections (with specific roles).
	 * This configuration setting corresponds to the &lt;collection-cache&gt; entry
	 * in the "hibernate.cfg.xml" configuration format.
	 * <p>For example:
	 * <pre>
	 * &lt;property name="collectionCacheStrategies"&gt;
	 *   &lt;props&gt;
	 *     &lt;prop key="com.mycompany.Order.items">read-write&lt;/prop&gt;
	 *     &lt;prop key="com.mycompany.Product.categories"&gt;read-only,myRegion&lt;/prop&gt;
	 *   &lt;/props&gt;
	 * &lt;/property&gt;</pre>
	 * Note that appending a cache region name (with a comma separator) is only
	 * supported on Hibernate 3.1, where this functionality is publically available.
	 * @param collectionCacheStrategies properties that define collection cache strategies,
	 * with collection roles as keys and cache concurrency strategies as values
	 * @see org.hibernate.cfg.Configuration#setCollectionCacheConcurrencyStrategy(String, String)
	 */
	public void setCollectionCacheStrategies(Properties collectionCacheStrategies) {
		this.collectionCacheStrategies = collectionCacheStrategies;
	}

	/**
	 * Specify the Hibernate event listeners to register, with listener types
	 * as keys and listener objects as values.
	 * <p>Instead of a single listener object, you can also pass in a list
	 * or set of listeners objects as value. However, this is only supported
	 * on Hibernate 3.1.
	 * <p>See the Hibernate documentation for further details on listener types
	 * and associated listener interfaces.
	 * @param eventListeners Map with listener type Strings as keys and
	 * listener objects as values
	 * @see org.hibernate.cfg.Configuration#setListener(String, Object)
	 */
	@SuppressWarnings("unchecked")
	public void setEventListeners(Map eventListeners) {
		this.eventListeners = eventListeners;
	}

	/**
	 * Set whether to execute a schema update after SessionFactory initialization.
	 * <p>For details on how to make schema update scripts work, see the Hibernate
	 * documentation, as this class leverages the same schema update script support
	 * in org.hibernate.cfg.Configuration as Hibernate's own SchemaUpdate tool.
	 * @see org.hibernate.cfg.Configuration#generateSchemaUpdateScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaUpdate
	 */
	public void setSchemaUpdate(boolean schemaUpdate) {
		this.schemaUpdate = schemaUpdate;
	}


	protected SessionFactory buildSessionFactory() throws Exception {
		logger.debug("[RoutingLocalSessionFactoryBean::buildSessionFactory] BEGIN");
		SessionFactory sf = null;

		// Create Configuration instance.
		Configuration config = newConfiguration();
		
		DataSource currentDataSource = getCurrentDataSource();
		logger.debug("[RoutingLocalSessionFactoryBean::buildSessionFactory] " +
				"Repository '" + RepositoryManager.getCurrentRepository() + "' -- Got currentDataSource: " + currentDataSource);

		if (currentDataSource == null) {
			throw new IllegalStateException("Null DataSource!");
		}

		// Make given DataSource available for SessionFactory configuration.
		logger.debug("[RoutingLocalSessionFactoryBean::buildSessionFactory] " +
				"Thread '" + Thread.currentThread().getName() + "' -- Setting DataSource for current thread: " + currentDataSource);
		CONFIG_TIME_DS_HOLDER.set(currentDataSource);

		if (this.jtaTransactionManager != null) {
			// Make Spring-provided JTA TransactionManager available.
			CONFIG_TIME_TM_HOLDER.set(this.jtaTransactionManager);
		}

		if (this.lobHandler != null) {
			// Make given LobHandler available for SessionFactory configuration.
			// Do early because because mapping resource might refer to custom types.
			CONFIG_TIME_LOB_HANDLER_HOLDER.set(this.lobHandler);
		}

		try {
			// Set connection release mode "on_close" as default.
			// This was the case for Hibernate 3.0; Hibernate 3.1 changed
			// it to "auto" (i.e. "after_statement" or "after_transaction").
			// However, for Spring's resource management (in particular for
			// HibernateTransactionManager), "on_close" is the better default.
			config.setProperty(Environment.RELEASE_CONNECTIONS, ConnectionReleaseMode.ON_CLOSE.toString());

			if (!isExposeTransactionAwareSessionFactory()) {
				// Not exposing a SessionFactory proxy with transaction-aware
				// getCurrentSession() method -> set Hibernate 3.1 CurrentSessionContext
				// implementation instead, providing the Spring-managed Session that way.
				// Can be overridden by a custom value for corresponding Hibernate property.
				config.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS,
						"org.springframework.orm.hibernate3.SpringSessionContext");
			}

			if (this.entityInterceptor != null) {
				// Set given entity interceptor at SessionFactory level.
				config.setInterceptor(this.entityInterceptor);
			}

			if (this.namingStrategy != null) {
				// Pass given naming strategy to Hibernate Configuration.
				config.setNamingStrategy(this.namingStrategy);
			}

			if (this.typeDefinitions != null) {
				// Register specified Hibernate type definitions.
				Mappings mappings = config.createMappings();
				for (int i = 0; i < this.typeDefinitions.length; i++) {
					TypeDefinitionBean typeDef = this.typeDefinitions[i];
					mappings.addTypeDef(typeDef.getTypeName(), typeDef.getTypeClass(), typeDef.getParameters());
				}
			}

			if (this.filterDefinitions != null) {
				// Register specified Hibernate FilterDefinitions.
				for (int i = 0; i < this.filterDefinitions.length; i++) {
					config.addFilterDefinition(this.filterDefinitions[i]);
				}
			}

			if (this.configLocations != null) {
				for (int i = 0; i < this.configLocations.length; i++) {
					// Load Hibernate configuration from given location.
					config.configure(this.configLocations[i].getURL());
				}
			}

			if (this.hibernateProperties != null) {
				// Add given Hibernate properties to Configuration.
				config.addProperties(this.hibernateProperties);
			}

			if (currentDataSource != null) {
				boolean actuallyTransactionAware =
						(this.useTransactionAwareDataSource 
								|| currentDataSource instanceof TransactionAwareDataSourceProxy);
				// Set Spring-provided DataSource as Hibernate ConnectionProvider.
				config.setProperty(Environment.CONNECTION_PROVIDER,
						actuallyTransactionAware ?
						TransactionAwareDataSourceConnectionProvider.class.getName() :
						RoutingLocalDataSourceConnectionProvider.class.getName());
			}

			if (this.jtaTransactionManager != null) {
				// Set Spring-provided JTA TransactionManager as Hibernate property.
				config.setProperty(
						Environment.TRANSACTION_MANAGER_STRATEGY, 
						LocalTransactionManagerLookup.class.getName());
			}

			if (this.mappingLocations != null) {
				// Register given Hibernate mapping definitions, contained in resource files.
				for (int i = 0; i < this.mappingLocations.length; i++) {
					config.addInputStream(this.mappingLocations[i].getInputStream());
				}
			}

			if (this.cacheableMappingLocations != null) {
				// Register given cacheable Hibernate mapping definitions, read from the file system.
				for (int i = 0; i < this.cacheableMappingLocations.length; i++) {
					config.addCacheableFile(this.cacheableMappingLocations[i].getFile());
				}
			}

			if (this.mappingJarLocations != null) {
				// Register given Hibernate mapping definitions, contained in jar files.
				for (int i = 0; i < this.mappingJarLocations.length; i++) {
					Resource resource = this.mappingJarLocations[i];
					config.addJar(resource.getFile());
				}
			}

			if (this.mappingDirectoryLocations != null) {
				// Register all Hibernate mapping definitions in the given directories.
				for (int i = 0; i < this.mappingDirectoryLocations.length; i++) {
					File file = this.mappingDirectoryLocations[i].getFile();
					if (!file.isDirectory()) {
						throw new IllegalArgumentException(
								"Mapping directory location [" + this.mappingDirectoryLocations[i] +
								"] does not denote a directory");
					}
					config.addDirectory(file);
				}
			}

			if (this.entityCacheStrategies != null) {
				// Register cache strategies for mapped entities.
				for (Enumeration<?> classNames = this.entityCacheStrategies.propertyNames(); classNames.hasMoreElements(); /* */) {
					String className = (String) classNames.nextElement();
					String[] strategyAndRegion =
							StringUtils.commaDelimitedListToStringArray(this.entityCacheStrategies.getProperty(className));
					if (strategyAndRegion.length > 1) {
						config.setCacheConcurrencyStrategy(className, strategyAndRegion[0], strategyAndRegion[1]);
					} else if (strategyAndRegion.length > 0) {
						config.setCacheConcurrencyStrategy(className, strategyAndRegion[0]);
					}
				}
			}

			if (this.collectionCacheStrategies != null) {
				// Register cache strategies for mapped collections.
				for (Enumeration<?> collRoles = this.collectionCacheStrategies.propertyNames(); collRoles.hasMoreElements(); /* */) {
					String collRole = (String) collRoles.nextElement();
					String[] strategyAndRegion =
							StringUtils.commaDelimitedListToStringArray(this.collectionCacheStrategies.getProperty(collRole));
					if (strategyAndRegion.length > 1) {
						config.setCollectionCacheConcurrencyStrategy(collRole, strategyAndRegion[0], strategyAndRegion[1]);
					} else if (strategyAndRegion.length > 0) {
						config.setCollectionCacheConcurrencyStrategy(collRole, strategyAndRegion[0]);
					}
				}
			}

			if (this.eventListeners != null) {
				// Register specified Hibernate event listeners.
				for (Map.Entry<?, ?> entry : this.eventListeners.entrySet()) {
					Assert.isTrue(entry.getKey() instanceof String, "Event listener key needs to be of type String");
					String listenerType = (String) entry.getKey();
					Object listenerObject = entry.getValue();
					
					if (listenerObject instanceof Collection) {
						Collection<?> listeners = (Collection<?>) listenerObject;
						EventListeners listenerRegistry = config.getEventListeners();
						Object[] listenerArray =
								(Object[]) Array.newInstance(listenerRegistry.getListenerClassFor(listenerType), listeners.size());
						listenerArray = listeners.toArray(listenerArray);
						config.setListeners(listenerType, listenerArray);
					} else {
						config.setListener(listenerType, listenerObject);
					}
				}
			}

			// Perform custom post-processing in subclasses.
			postProcessConfiguration(config);

			// Build SessionFactory instance.
			logger.debug("[RoutingLocalSessionFactoryBean::buildSessionFactory] Building new Hibernate SessionFactory.");
			this.configuration = config;

			SessionFactoryProxy sessionFactoryProxy = new SessionFactoryProxy(repositoryManager.getDefaultRepository().getId());
			for (Repository repository : repositoryManager.getRepositories()) {
				logger.debug("[RoutingLocalSessionFactoryBean::buildSessionFactory] " +
						"Repository '" + repository.getId() + "' -- Building SessionFactory...");
				
				RepositoryManager.setCurrentRepository(repository.getId());
				sessionFactoryProxy.addSessionFactory(repository.getId(), newSessionFactory(config));
			}
			RepositoryManager.setCurrentRepository(repositoryManager.getDefaultRepository().getId());
			sf = sessionFactoryProxy;
		} finally {
			if (currentDataSource != null) {
				// Reset DataSource holder.
				CONFIG_TIME_DS_HOLDER.set(null);
			}

			if (this.jtaTransactionManager != null) {
				// Reset TransactionManager holder.
				CONFIG_TIME_TM_HOLDER.set(null);
			}

			if (this.lobHandler != null) {
				// Reset LobHandler holder.
				CONFIG_TIME_LOB_HANDLER_HOLDER.set(null);
			}
		}

		// Execute schema update if requested.
		if (this.schemaUpdate) {
			updateDatabaseSchema();
		}

		return sf;
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the Configuration instance used for SessionFactory creation.
	 * The properties of this RoutingLocalSessionFactoryBean will be applied to
	 * the Configuration object that gets returned here.
	 * <p>The default implementation creates a new Configuration instance.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom Configuration subclass.
	 * @return the Configuration instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see org.hibernate.cfg.Configuration#Configuration()
	 */
	protected Configuration newConfiguration() {
		return (Configuration) BeanUtils.instantiateClass(this.configurationClass);
	}

	/**
	 * To be implemented by subclasses that want to to perform custom
	 * post-processing of the Configuration object after this FactoryBean
	 * performed its default initialization.
	 * @param config the current Configuration object
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	protected void postProcessConfiguration(Configuration config) {}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the SessionFactory instance, creating it via the given Configuration
	 * object that got prepared by this RoutingLocalSessionFactoryBean.
	 * <p>The default implementation invokes Configuration's buildSessionFactory.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom SessionFactoryImpl subclass.
	 * 
	 * @param config Configuration prepared by this RoutingLocalSessionFactoryBean
	 * @return the SessionFactory instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see org.hibernate.cfg.Configuration#buildSessionFactory
	 */
	protected SessionFactory newSessionFactory(Configuration config) {
		return config.buildSessionFactory();
	}

	/**
	 * Allow for schema export on shutdown.
	 */
	public void destroy() {
		DataSource currentDataSource = getCurrentDataSource();

		if (currentDataSource != null) {
			// Make given DataSource available for potential SchemaExport,
			// which unfortunately reinstantiates a ConnectionProvider.
			CONFIG_TIME_DS_HOLDER.set(currentDataSource);
		}
		try {
			super.destroy();
		} finally {
			if (currentDataSource != null) {
				// Reset DataSource holder.
				CONFIG_TIME_DS_HOLDER.set(null);
			}
		}
	}


	/**
	 * Execute schema drop script, determined by the Configuration object
	 * used for creating the SessionFactory. A replacement for Hibernate's
	 * SchemaExport class, to be invoked on application setup.
	 * <p>Fetch the RoutingLocalSessionFactoryBean itself rather than the exposed
	 * SessionFactory to be able to invoke this method, e.g. via
	 * <code>RoutingLocalSessionFactoryBean lsfb = (RoutingLocalSessionFactoryBean) ctx.getBean("&mySessionFactory");</code>.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script.
	 * @throws org.springframework.dao.DataAccessException in case of script execution errors
	 * @see org.hibernate.cfg.Configuration#generateDropSchemaScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaExport#drop
	 */
	public void dropDatabaseSchema() {
		HibernateTemplate hibernateTemplate = new HibernateTemplate(getSessionFactory());
		hibernateTemplate.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection con = session.connection();
					Dialect dialect = Dialect.getDialect(getConfiguration().getProperties());
					String[] sql = getConfiguration().generateDropSchemaScript(dialect);
					executeSchemaScript(con, sql);
					return null;
				}
			}
		);
	}

	/**
	 * Execute schema creation script, determined by the Configuration object
	 * used for creating the SessionFactory. A replacement for Hibernate's
	 * SchemaExport class, to be invoked on application setup.
	 * <p>Fetch the RoutingLocalSessionFactoryBean itself rather than the exposed
	 * SessionFactory to be able to invoke this method, e.g. via
	 * <code>RoutingLocalSessionFactoryBean lsfb = (RoutingLocalSessionFactoryBean) ctx.getBean("&mySessionFactory");</code>.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script.
	 * @throws DataAccessException in case of script execution errors
	 * @see org.hibernate.cfg.Configuration#generateSchemaCreationScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaExport#create
	 */
	public void createDatabaseSchema() {
		HibernateTemplate hibernateTemplate = new HibernateTemplate(getSessionFactory());
		hibernateTemplate.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection con = session.connection();
					Dialect dialect = Dialect.getDialect(getConfiguration().getProperties());
					String[] sql = getConfiguration().generateSchemaCreationScript(dialect);
					executeSchemaScript(con, sql);
					return null;
				}
			}
		);
	}

	/**
	 * Execute schema update script, determined by the Configuration object
	 * used for creating the SessionFactory. A replacement for Hibernate's
	 * SchemaUpdate class, for automatically executing schema update scripts
	 * on application startup. Can also be invoked manually.
	 * <p>Fetch the RoutingLocalSessionFactoryBean itself rather than the exposed
	 * SessionFactory to be able to invoke this method, e.g. via
	 * <code>RoutingLocalSessionFactoryBean lsfb = (RoutingLocalSessionFactoryBean) ctx.getBean("&mySessionFactory");</code>.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script.
	 * @throws DataAccessException in case of script execution errors
	 * @see #setSchemaUpdate
	 * @see org.hibernate.cfg.Configuration#generateSchemaUpdateScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaUpdate
	 */
	public void updateDatabaseSchema() {
		HibernateTemplate hibernateTemplate = new HibernateTemplate(getSessionFactory());
		hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
		hibernateTemplate.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection con = session.connection();
					Dialect dialect = Dialect.getDialect(getConfiguration().getProperties());
					DatabaseMetadata metadata = new DatabaseMetadata(con, dialect);
					String[] sql = getConfiguration().generateSchemaUpdateScript(dialect, metadata);
					executeSchemaScript(con, sql);
					return null;
				}
			}
		);
	}

	/**
	 * Execute the given schema script on the given JDBC Connection.
	 * <p>Note that the default implementation will log unsuccessful statements
	 * and continue to execute. Override the <code>executeSchemaStatement</code>
	 * method to treat failures differently.
	 * @param con the JDBC Connection to execute the script on
	 * @param sql the SQL statements to execute
	 * @throws SQLException if thrown by JDBC methods
	 * @see #executeSchemaStatement
	 */
	protected void executeSchemaScript(Connection con, String[] sql) throws SQLException {
		if (sql != null && sql.length > 0) {
			boolean oldAutoCommit = con.getAutoCommit();
			if (!oldAutoCommit) {
				con.setAutoCommit(true);
			}
			try {
				Statement stmt = con.createStatement();
				try {
					for (int i = 0; i < sql.length; i++) {
						executeSchemaStatement(stmt, sql[i]);
					}
				}
				finally {
					JdbcUtils.closeStatement(stmt);
				}
			}
			finally {
				if (!oldAutoCommit) {
					con.setAutoCommit(false);
				}
			}
		}
	}

	/**
	 * Execute the given schema SQL on the given JDBC Statement.
	 * <p>Note that the default implementation will log unsuccessful statements
	 * and continue to execute. Override this method to treat failures differently.
	 * @param stmt the JDBC Statement to execute the SQL on
	 * @param sql the SQL statement to execute
	 * @throws SQLException if thrown by JDBC methods (and considered fatal)
	 */
	protected void executeSchemaStatement(Statement stmt, String sql) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("[RoutingLocalSessionFactoryBean::executeSchemaStatement] Executing schema statement: " + sql);
		}
		try {
			stmt.executeUpdate(sql);
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unsuccessful schema statement: " + sql, ex);
			}
		}
	}

	protected static DataSource getCurrentDataSource() {
		logger.debug("[RoutingLocalSessionFactoryBean::getCurrentDataSource] BEGIN");

		try {
			DataSource currentDataSource = null;
			String currentRepositoryId = RepositoryManager.getCurrentRepository();
			Repository currentRepository = RepositoryManager.getInstance().getRepository(currentRepositoryId);

			if (currentRepository != null) {
				currentDataSource = repositoryManager.getRepository(currentRepositoryId).getDataSource();
				logger.debug("[RoutingLocalSessionFactoryBean::getCurrentDataSource]" +
						" Repository '" + currentRepositoryId + "' -- DataSource: " + currentDataSource);
				return currentDataSource;
			} else {
				logger.error("[RoutingLocalSessionFactoryBean::getCurrentDataSource]" +
						" Repository '" + currentRepositoryId + "' -- No such repository!");
				throw new IllegalStateException("No Repository instance for known repository ID!");
			}
		} finally {
			logger.debug("[RoutingLocalSessionFactoryBean::getCurrentDataSource] END");
		}
	}
	
	public Configuration getConfigurationOverride() {
		if (this.configuration == null) {
			throw new IllegalStateException("Configuration not initialized yet");
		}
		return this.configuration;
	}
}

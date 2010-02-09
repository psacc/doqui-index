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

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

@SuppressWarnings("unchecked")
public class SessionFactoryProxy implements SessionFactory {

	private static final long serialVersionUID = 2630792801838327829L;

	private Map<String, SessionFactory> sessionFactoryMap;
	private String defaultRepository;
    private Logger logger = Logger.getLogger(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);

	public SessionFactoryProxy(String defaultRepository) {
		this.sessionFactoryMap = new HashMap<String, SessionFactory>();
		this.defaultRepository = defaultRepository;
	}

	public void addSessionFactory(String repositoryId, SessionFactory sessionFactory) {
		sessionFactoryMap.put(repositoryId, sessionFactory);
	}

	private SessionFactory getSessionFactory() {
		String repositoryId = defaultRepository;
		SessionFactory factory = null;

		repositoryId = RepositoryManager.getCurrentRepository();

		factory = sessionFactoryMap.get(repositoryId);

        /*
		try {
            if (logger.isDebugEnabled()) {
    		    logger.debug("[SessionFactoryProxy::getSessionFactory] Repository '" +repositoryId +"' -- SessionFactory: " +factory.getReference());
            }
		} catch (NamingException e) {
			logger.debug("[SessionFactoryProxy::getSessionFactory]", e);
		}
        //*/

		return factory;
	}

	public void close() throws HibernateException {
		getSessionFactory().close();
	}

	public void evict(Class persistentClass) throws HibernateException {
		getSessionFactory().evict(persistentClass);
	}

	public void evict(Class persistentClass, Serializable id) throws HibernateException {
		getSessionFactory().evict(persistentClass, id);
	}

	public void evictCollection(String roleName) throws HibernateException {
		getSessionFactory().evictCollection(roleName);
	}

	public void evictCollection(String roleName, Serializable id) throws HibernateException {
		getSessionFactory().evictCollection(roleName, id);
	}

	public void evictEntity(String entityName) throws HibernateException {
		getSessionFactory().evictEntity(entityName);
	}

	public void evictEntity(String entityName, Serializable id) throws HibernateException {
		getSessionFactory().evictEntity(entityName, id);
	}

	public void evictQueries() throws HibernateException {
		getSessionFactory().evictQueries();
	}

	public void evictQueries(String cacheRegion) throws HibernateException {
		getSessionFactory().evictQueries(cacheRegion);
	}

	public Map getAllClassMetadata() throws HibernateException {
		return getSessionFactory().getAllClassMetadata();
	}

	public Map getAllCollectionMetadata() throws HibernateException {
		return getSessionFactory().getAllCollectionMetadata();
	}

	public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
		return getSessionFactory().getClassMetadata(persistentClass);
	}

	public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
		return getSessionFactory().getClassMetadata(entityName);
	}

	public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
		return getSessionFactory().getCollectionMetadata(roleName);
	}

	public Session getCurrentSession() throws HibernateException {
		return getSessionFactory().getCurrentSession();
	}

	public Set getDefinedFilterNames() {
		return getSessionFactory().getDefinedFilterNames();
	}

	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
		return getSessionFactory().getFilterDefinition(filterName);
	}

	public Statistics getStatistics() {
		return getSessionFactory().getStatistics();
	}

	public boolean isClosed() {
		return getSessionFactory().isClosed();
	}

	public Session openSession() throws HibernateException {
		return getSessionFactory().openSession();
	}

	public Session openSession(Connection connection) {
		return getSessionFactory().openSession(connection);
	}

	public Session openSession(Interceptor interceptor) throws HibernateException {
		return getSessionFactory().openSession(interceptor);
	}

	public Session openSession(Connection connection, Interceptor interceptor) {
		return getSessionFactory().openSession(connection, interceptor);
	}

	public StatelessSession openStatelessSession() {
		return getSessionFactory().openStatelessSession();
	}

	public StatelessSession openStatelessSession(Connection connection) {
		return getSessionFactory().openStatelessSession(connection);
	}

	public Reference getReference() throws NamingException {
		return getSessionFactory().getReference();
	}
}

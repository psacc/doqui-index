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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.util.JDBCExceptionReporter;

/**
 * Classe che implementa un connection provider per DataSource locali con supporto
 * al multirepository.
 * 
 * @author DoQui
 *
 * @see org.springframework.orm.hibernate3.LocalDataSourceConnectionProvider
 */
public class RoutingLocalDataSourceConnectionProvider implements ConnectionProvider {

	private DataSource dataSource;
	private Log logger = LogFactory.getLog(EcmEngineHibernateConstants.HIBERNATE_LOG_CATEGORY);

	/**
	 * Configura il provider.
	 * 
	 * <strong>NB:</strong> le propriet&agrave; specificate in input vengono ignorate da questa implementazione.
	 * 
	 * @param props Un oggetto {@code Properties} contenente i parametri di configurazione (ignorate).
	 * 
	 * @throws HibernateException Se si verifica un errore durante la configurazione di hibernate.
	 */
	public void configure(Properties props) throws HibernateException {
		logger.debug("[RoutingLocalDataSourceConnectionProvider::configure] BEGIN");

		try {
			this.dataSource = RoutingLocalSessionFactoryBean.getConfigTimeDataSource();
			if (this.dataSource == null) {
				throw new HibernateException("No local DataSource found for configuration - "
						+ "dataSource property must be set on RoutingLocalSessionFactoryBean");
			}
		} finally {
			logger.debug("[RoutingLocalDataSourceConnectionProvider::configure] END");
		}
	}

	/**
	 * Restituisce il {@code DataSource} corrispondente al repository fisico corrente.
	 * 
	 * @return Il {@code DataSource} corrispondente al repository fisico corrente.
	 */
	public DataSource getDataSource() {
		logger.debug("[RoutingLocalDataSourceConnectionProvider::getDataSource] BEGIN");

		try {
			return RoutingLocalSessionFactoryBean.getCurrentDataSource();
		} finally {
			logger.debug("[RoutingLocalDataSourceConnectionProvider::getDataSource] END");
		}
	}

	/**
	 * Restituisce una connessione al repository fisico corrente.
	 * 
	 * @see javax.sql.DataSource#getConnection()
	 * 
	 * @return Una {@code Connection}.
	 * 
	 * @throws SQLException In caso di errori sul DB.
	 */
	public Connection getConnection() throws SQLException {
		logger.debug("[RoutingLocalDataSourceConnectionProvider::getConnection] BEGIN");
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("[RoutingLocalDataSourceConnectionProvider::getConnection] Getting connection: " 
						+ RoutingLocalSessionFactoryBean.getCurrentDataSource());
			}
			
			return RoutingLocalSessionFactoryBean.getCurrentDataSource().getConnection();
		} catch (SQLException ex) {
			JDBCExceptionReporter.logExceptions(ex);
			throw ex;
		} finally {
			logger.debug("[RoutingLocalDataSourceConnectionProvider::getConnection] END");
		}
	}

	/**
	 * Chiude la connessione specificata.
	 * 
	 * @param con La connessione da chiudere.
	 * 
	 * @see java.sql.Connection#close()
	 * 
	 * @throws SQLException In caso di errori sul DB.
	 */
	public void closeConnection(Connection con) throws SQLException {
		logger.debug("[RoutingLocalDataSourceConnectionProvider::closeConnection] BEGIN");
		
		try {
			con.close();
		} catch (SQLException ex) {
			JDBCExceptionReporter.logExceptions(ex);
			throw ex;
		} finally {
			logger.debug("[RoutingLocalDataSourceConnectionProvider::closeConnection] END");
		}
	}

	/**
	 * Implementazione vuota: il {@code DataSource} &egrave; gestito esternamente.
	 */
	public void close() {
	}

	/**
	 * Restituisce {@code false}: non possiamo garantire di ottenere sempre la stessa
	 * connessione durante una transazione.
	 * 
	 * @return {@code false}.
	 */
	public boolean supportsAggressiveRelease() {
		return false;
	}
}

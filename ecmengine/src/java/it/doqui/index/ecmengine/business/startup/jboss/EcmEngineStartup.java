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
 
package it.doqui.index.ecmengine.business.startup.jboss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

public class EcmEngineStartup implements EcmEngineStartupMBean {

	// Our message attribute
	private Log logger = LogFactory.getLog(ECMENGINE_STARTUP_LOG_CATEGORY);
	private BeanFactoryReference factory = null;
	private String contextFile = null;
	private String factoryBean = null;

	// The lifecycle
	public void start() throws Exception {
		logger.debug("[EcmEngineStartup::start] BEGIN");
		try {
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(contextFile);
			factory = locator.useBeanFactory(factoryBean);
			logger.info("[EcmEngineStartup::start] BeanFactory avviata!");
		} catch (Exception e) {
			logger.warn("[EcmEngineStartup::start] Exception during startup!", e);
		} finally {
			logger.debug("[EcmEngineStartup::start] END");
		}
	}

	public void stop() {
		logger.debug("[EcmEngineStartup::stop] BEGIN");
		try {
			factory.release();
			logger.info("[EcmEngineStartup::stop] BeanFactory rilasciata!");
		} catch (Exception e) {
			logger.warn("[EcmEngineStartup::stop] Exception during shutdown!");
		} finally {
			logger.debug("[EcmEngineStartup::stop] END");
		}
	}

	public String getContextFile() {
		return contextFile;
	}

	public void setContextFile(String contextFile) {
		this.contextFile = contextFile;
	}

	public String getFactoryBean() {
		return factoryBean;
	}

	public void setFactoryBean(String factoryBean) {
		this.factoryBean = factoryBean;
	}
}



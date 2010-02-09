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
 
package it.doqui.index.ecmengine.business.startup.weblogic;

import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import weblogic.application.ApplicationLifecycleEvent;
import weblogic.application.ApplicationLifecycleListener;

public class EcmEngineStartupListener extends ApplicationLifecycleListener {
	private static final String CONTEXT_FILE = "beanRefContext.xml";
	private static final String FACTORY_BEAN = "businessBeanFactory";
	
	private static final String ECMENGINE_STARTUP_LOG_CATEGORY = EcmEngineConstants.ECMENGINE_ROOT_LOG_CATEGORY + ".startup";

	private Log logger = LogFactory.getLog(ECMENGINE_STARTUP_LOG_CATEGORY);
	private BeanFactoryReference factory = null;

	public void postStart(ApplicationLifecycleEvent evt) {

		logger.debug("[EcmEngineStartupListener::start] BEGIN");
		try {
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(CONTEXT_FILE);
			factory = locator.useBeanFactory(FACTORY_BEAN);
			logger.info("[EcmEngineStartupListener::start] BeanFactory avviata!");
		} catch (Exception e) {
			logger.warn("[EcmEngineStartupListener::start] Exception during startup!", e);
		} finally {
			logger.debug("[EcmEngineStartupListener::start] END");
		}

	}
	
	public void preStop(ApplicationLifecycleEvent evt) {
		logger.debug("[EcmEngineStartupListener::stop] BEGIN");
		try {
			if (factory != null) {
				factory.release();
			}

			logger.info("[EcmEngineStartupListener::stop] BeanFactory rilasciata!");
		} catch (Exception e) {
			logger.warn("[EcmEngineStartupListener::stop] Exception during shutdown!", e);
		} finally {
			logger.debug("[EcmEngineStartupListener::stop] END");
		}
	}
}

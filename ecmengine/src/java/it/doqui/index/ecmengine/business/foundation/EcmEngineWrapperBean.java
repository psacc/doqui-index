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
 
package it.doqui.index.ecmengine.business.foundation;

import it.doqui.index.ecmengine.util.EcmEngineConstants;

import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.SessionContext;

/**
 * Classe astratta che implementa un generico wrapper EJB 2.x dei servizi applicativi.
 * 
 * <p>Questa classe implementa i metodi {@link #onEjbCreate()}
 * e {@link #setSessionContext(javax.ejb.SessionContext)} di base per il caricamento
 * dell'istanza dell'Application Context di Alfresco in modalit&agrave; <i>singleton</i>.
 * &Egrave; importante che ogni sottoclasse che ridefinisca tali due metodi ne richiami la
 * versione definita in questa classe per evitare funzionamenti imprevisti.</p>
 * 
 * @author Doqui
 *
 */
public abstract class EcmEngineWrapperBean extends AbstractStatelessSessionBean implements EcmEngineConstants {

	/**
	 * Il reference al Service Registry di Alfresco.
	 */
	protected transient ServiceRegistry serviceRegistry;
	
	/**
	 * Il log utilizzato da tutti i wrapper dei servizi applicativi.
	 */
	protected transient Log logger = LogFactory.getLog(ECMENGINE_FOUNDATION_LOG_CATEGORY);
	
	/**
	 * Metodo richiamato alla creazione dell'EJB wrapper. Si occupa di recuperare il
	 * riferimento al Service Registry di Alfresco dall'application context Spring,
	 * inizializzando il campo {@link #serviceRegistry}
	 */
	public void onEjbCreate() {
		this.logger.debug("[EcmEngineWrapperBean::onEjbCreate] BEGIN ");
		this.serviceRegistry = (ServiceRegistry)getBeanFactory().getBean(ServiceRegistry.SERVICE_REGISTRY);
		this.logger.debug("[EcmEngineWrapperBean::onEjbCreate] END ");
	}

	/**
	 * Imposta il contesto di esecuzione dell'EJB wrapper.
	 * 
	 * @param context Il contesto di esecuzione (passato dal container EJB).
	 * 
	 * @see #initBeanFactory()
	 */
	public void setSessionContext(SessionContext context)  {
		this.logger.debug("[EcmEngineWrapperBean::setSessionContext] BEGIN ");
		super.setSessionContext(context);
		initBeanFactory();
		this.logger.debug("[EcmEngineWrapperBean::setSessionContext] END ");
	}

	/**
	 * Inizializza la Bean Factory utilizzata per caricare l'application
	 * context Spring utilizzando un {@code ContextSingletonBeanFactoryLocator}.
	 * In questo modo &egrave; possibile condividere una singola istanza
	 * del context fra tutti gli EJB wrapper.
	 */
	protected void initBeanFactory() {
		this.logger.debug("[EcmEngineWrapperBean::initBeanFactory] BEGIN ");
	    setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey(ECMENGINE_BUSINESS_BEAN_KEY);
		this.logger.debug("[EcmEngineWrapperBean::initBeanFactory] END ");
	}
}
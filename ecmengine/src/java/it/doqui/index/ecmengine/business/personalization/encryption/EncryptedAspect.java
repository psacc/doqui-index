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
 
package it.doqui.index.ecmengine.business.personalization.encryption;

import it.doqui.index.ecmengine.business.personalization.encryption.exception.CannotRemoveEncryptedAspectException;
import it.doqui.index.ecmengine.business.personalization.encryption.util.EncryptionUtils;
import it.doqui.index.ecmengine.util.EcmEngineModelConstants;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che definisce il comportamento associato all'applicazione dell'aspect &quot;ecm-sys:encrypted&quot;.
 * 
 * <p>In particolare sui contenuti a cui &egrave; associato tale aspect non &egrave; possibile:</p>
 * <ul>
 *  <li>eseguire la rimozione dell'aspect stesso;</li>
 *  <li>rimuovere uno o pi&ugrave; metadati definiti nell'aspect stesso.</li>
 * </ul>
 * 
 * <p><strong>NB:</strong> non &egrave; possibile sostituire un contenuto criptato con un contenuto non 
 * criptato. Il contenuto criptato deve prima essere rimosso dal repository.</p>
 * 
 * @author Doqui
 *
 */
public class EncryptedAspect implements NodeServicePolicies.OnUpdatePropertiesPolicy,
	NodeServicePolicies.BeforeRemoveAspectPolicy {
	private static Log logger = LogFactory.getLog(EncryptionUtils.ENCRYPTION_LOG_CATEGORY);
	
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private DictionaryService dictionaryService;
	
	/**
	 * Imposta l'istanza del Node Service.
	 * 
	 * @param nodeService L'istanza da impostare.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Imposta l'istanza del Policy Component.
	 * 
	 * @param policyComponent L'istanza da impostare.
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	/**
	 * Imposta l'istanza del Dictionary Service.
	 * 
	 * @param dictionaryService L'istanza da impostare.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Inizializza l'aspect.
	 */
	public void init() {
		logger.debug("[EncryptedAspect::init] BEGIN");

		try {
			PropertyCheck.mandatory("EncryptedAspect", "nodeService", nodeService);
			PropertyCheck.mandatory("EncryptedAspect", "policyComponent", policyComponent);
			PropertyCheck.mandatory("EncryptedAspect", "dictionaryService", dictionaryService);
			
	        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
	                EcmEngineModelConstants.ASPECT_ENCRYPTED, new JavaBehaviour(this, "onUpdateProperties"));
	        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveAspect"), 
	        		EcmEngineModelConstants.ASPECT_ENCRYPTED, new JavaBehaviour(this, "beforeRemoveAspect"));
		} finally {
			logger.debug("[EncryptedAspect::init] END");
		}
	}

	/**
	 * Comportamento associato alla modifica dei metadati di un nodo.
	 * 
	 * @param nodeRef Il riferimento al nodo.
	 * @param before La {@code Map} contenente i valori dei metadati <i>prima</i> della modifica.
	 * @param after La {@code Map} contenente i valori dei metadati <i>dopo</i> della modifica.
	 */
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		logger.debug("[EncryptedAspect::onUpdateProperties] BEGIN");

		Map<QName, PropertyDefinition> propertyDefinitions = 
			dictionaryService.getAspect(EcmEngineModelConstants.ASPECT_ENCRYPTED).getProperties();
		
		try {
			for (Map.Entry<QName, PropertyDefinition> property : propertyDefinitions.entrySet()) {
				final QName key = property.getKey();
				
				if (before.containsKey(key) && !after.containsKey(key)) {
					logger.debug("[EncryptedAspect::onUpdateProperties] Restored property \"" + key + "\" on node: " + nodeRef);
					nodeService.setProperty(nodeRef, key, before.get(property.getKey()));
				}
			}
		} finally {
			logger.debug("[EncryptedAspect::onUpdateProperties] END");
		}
	}

	/**
	 * Comportamento associato alla rimozione di un aspect.
	 * 
	 * @param nodeRef Il riferimento al nodo.
	 * @param aspectQName Il {@code QName} dell'aspect rimosso.
	 */
	public void beforeRemoveAspect(NodeRef nodeRef, QName aspectQName) {
		logger.debug("[EncryptedAspect::beforeRemoveAspect] BEGIN");

		try {
			if (aspectQName.equals(EcmEngineModelConstants.ASPECT_ENCRYPTED)) {
				throw new CannotRemoveEncryptedAspectException(nodeRef);
			}
		} finally {
			logger.debug("[EncryptedAspect::beforeRemoveAspect] END");
		}
	}
}

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
 
package it.doqui.index.ecmengine.business.foundation.repository;

import it.doqui.index.ecmengine.exception.repository.NodeRuntimeException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * Interfaccia pubblica del servizio di gestione dei nodi
 * esportata come componente EJB 2.1.
 * 
 * <p>L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link NodeSvcBean}.</p>
 * 
 * <p>Tutti i metodi esportati dal bean di gestione dei contenuti rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link it.doqui.index.ecmengine.exception.repository.NodeRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see ContentSvcBean
 * @see it.doqui.index.ecmengine.exception.repository.NodeRuntimeException
 */
public interface NodeSvc extends EJBLocalObject {

	// TODO: Completare JavaDoc
	
	/**
	 * Crea un nuovo nodo figlio sotto il nodo specificato in input.
	 * 
	 * @param nodeRef Il riferimento al nodo padre.
	 * @param assocTypeQName Il tipo di associazione fra il nodo padre e il nuovo nodo figlio.
	 * @param assocQName Il nome dell'associazione fra il nodo padre e il nuovo nodo figlio.
	 * @param typeQName Il tipo del nuovo nodo figlio.
	 * @param props Le propriet&agrave; iniziali da impostare sul nuovo nodo.
	 * 
	 * @return Il riferimento alla nuova associazione che lega il padre al nuovo nodo figlio.
	 *  
	 * @throws NodeRuntimeException Se si verifica un errore generico nella creazione del nodo.
	 */
	ChildAssociationRef createNode(NodeRef nodeRef, QName assocTypeQName, QName assocQName, QName typeQName, Map<QName, Serializable> props) 
	throws NodeRuntimeException;
	
	/**
	 * Elimina il nodo specificato. Tutte le associazioni (sia figlie che relazioni normali) 
	 * verranno cancellate, e se il nodo dato e' il padre primary, i nodi figli verrano cancellati in cascata.
	 * 
	 * @param nodeRef Il riferimento al nodo da cancellare.
	 * @throws NodeRuntimeException Se si verifica un errore generico nella cancellazione del nodo.
	 */
	
	void deleteNode(NodeRef nodeRef) throws NodeRuntimeException;	
	
	/**
	 * Aggiunge l'aspect specificato ad un nodo esistente.
	 * 
	 * @param nodeRef Il riferimento al nodo a cui aggiungere l'aspect.
	 * @param aspectTypeQName Il nome dell'aspect da aggiungere.
	 * @param props Le propriet&agrave; (definite dall'aspect) da impostare sul nodo.
	 * 
	 * @throws NodeRuntimeException Se si verifica un errore generico nell'impostazione dell'aspect.
	 */
	void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> props)
	throws NodeRuntimeException;
	
	/**
	 * Restituisce gli aspect impostati su un nodo.
	 * 
	 * @param nodeRef Il riferimento al nodo di cui leggere gli aspect.
	 * 
	 * @return Un {@code Set} contenente i nomi degli aspect impostati sul nodo.
	 * 
	 * @throws NodeRuntimeException Se si verifica un errore generico nell'impostazione dell'aspect.
	 */
    Set<QName> getAspects(NodeRef nodeRef) throws NodeRuntimeException;
    
	/**
	 * Rimuove l'aspect specificato da un nodo esistente.
	 * 
	 * Se l'aspect rimosso definisce dei metadati, tali metadati saranno automaticamente rimossi
	 * dal nodo.
	 * 
	 * @param nodeRef Il riferimento al nodo da cui rimuovere l'aspect.
	 * @param aspectTypeQName Il nome dell'aspect da rimuovere.
	 * 
	 * @throws NodeRuntimeException Se si verifica un errore generico nella rimozione dell'aspect.
	 */
    void removeAspect(NodeRef nodeRef, QName aspectTypeQName) throws NodeRuntimeException;
    
	/**
	 * Verifica se un aspect &egrave; impostato su un nodo.
	 * 
	 * @param nodeRef Il riferimento al nodo su cui eseguire la verifica.
	 * @param aspectRef L'aspect da cercare.
	 * 
	 * @return {@code true} se l'aspect &egrave; impostato, {@code false} altrimenti.
	 * 
	 * @throws NodeRuntimeException Se si verifica un errore generico nella verifica.
	 */
    boolean hasAspect(NodeRef nodeRef, QName aspectRef) throws NodeRuntimeException;

	Map<QName, Serializable> getProperties(NodeRef nodeRef) throws NodeRuntimeException;

	Serializable getProperty(NodeRef nodeRef, QName propertyQName) throws NodeRuntimeException;
	
	void setProperty(NodeRef nodeRef, QName propertyQName, Serializable value) throws NodeRuntimeException;
	
    void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws NodeRuntimeException;

    ChildAssociationRef moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName, QName assocQName) 
    throws NodeRuntimeException;
    
    ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws NodeRuntimeException;
    
    ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName qname) 
    throws NodeRuntimeException;
    
    AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName) 
	throws NodeRuntimeException;
    
    void removeChild(NodeRef parentRef, NodeRef childRef) throws NodeRuntimeException;
        
    boolean removeChildAssociation(ChildAssociationRef childAssocRef) throws NodeRuntimeException;
    
    boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef) 
    throws NodeRuntimeException;

    void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName) 
    throws NodeRuntimeException;
    
    QName getType(NodeRef nodeRef) throws NodeRuntimeException;
    
    List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) throws NodeRuntimeException;
    
    List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef) throws NodeRuntimeException;
   
    List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern) 
	throws NodeRuntimeException;
    
    List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern) 
	throws NodeRuntimeException;
    
	/**
	 * Restituisce un boolean che indica se il NodeRef passato in input esiste.
	 * 
	 * @param nodeRef reference al nodo da controllare.
	 * 
	 * @return Un boolean value che indica se il nodo esiste oppure no.
	 * 
	 * @throws NodeRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean exists(NodeRef nodeRef) throws NodeRuntimeException;

	/**
	 * Restituisce un boolean che indica se lo StoreRef passato in input esiste.
	 * 
	 * @param storeRef reference allo store da controllare.
	 * 
	 * @return Un boolean value che indica se lo store esiste oppure no.
	 * 
	 * @throws NodeRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean exists(StoreRef storeRef) throws NodeRuntimeException;
	
	Path getPath(NodeRef nodeRef) throws NodeRuntimeException;
	
	List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws NodeRuntimeException;
}

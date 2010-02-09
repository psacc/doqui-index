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
 
package it.doqui.index.ecmengine.business.foundation.contentmanagement;

import it.doqui.index.ecmengine.exception.contentmanagement.VersionRuntimeException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;

/**
 * <p>Interfaccia pubblica del servizio di copy esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe <code>{@link VersionSvcBean}</code>.
 * </p>
 * <p>Tutti i metodi esportati dal bean di VersionSvcBean rimappano le 
 * <code>RuntimeException</code> ricevute in 
 * <code>{@link VersionRuntimeException}</code>.
 * </p>
 * 
 * @author Doqui
 * 
 * @see VersionSvcBean
 * @see VersionRuntimeException
 */
public interface VersionSvc extends EJBLocalObject {
	
	/**
	 * Restore del nodo passato in input.
     * <p>
     * Il nodo "restorato" sara' posizionato nella head version
     * <p>
     * L'operazione di restore fallira' se non e' presente una storia delle versioni sul nodo specificato 
     * sullo store
     * <p>
     * Se il nodo esiste gia' sullo store verra' sollevata un'eccezione.
     * <p>
	 * Quando il nodo subisce un'operazione di restore verra' riportato nella versione piu' recente nel
	 * tree del version history.
	 * Se deep e' settato a true verra' eseguito un deep revert, altrimenti no.
       *
     * @param nodeRef           reference al nodo che non esiste nello store
     * @param parentNodeRef  	il nuovo parent del nodo restored
     * @param assocTypeQName 	the assoc type qname      
     * @param assocQName 		the assoc qname
     * 
     * @return                  la nuova reference al nodo restored
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	NodeRef restore(NodeRef nodeRef, NodeRef parentNodeRef, QName assocTypeQName, QName assocQName) 
	throws VersionRuntimeException;
	
	/**
	 * Restore del nodo passato in input non ancora presente sullo store, ma che ha una version history.
     * <p>
     * Il nodo "restorato" sara' posizionato nella head version
     * <p>
     * L'operazione di restore fallira' se non e' presente una storia delle versioni sul nodo specificato 
     * sullo store
     * <p>
     * Se il nodo esiste gia' sullo store verra' sollevata un'eccezione.
     * <p>
	 * Quando il nodo subisce un'operazione di restore verra' riportato nella versione piu' recente nel
	 * tree del version history.
	 * Se deep e' settato a true verra' eseguito un deep revert, altrimenti no.
     *
     * @param nodeRef           reference al nodo che non esiste nello store
     * @param parentNodeRef  	il nuovo parent del nodo restored
     * @param assocTypeQName 	the assoc type qname      
     * @param assocQName 		the assoc qname
     * @param deep 				true  -> una volta che il nodo viene restored dovrebbe essere applicata una deep revert                         
     * 							false -> no 
     * 
     * @return                  la nuova reference al nodo restored
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	NodeRef restore(NodeRef nodeRef, NodeRef parentNodeRef, QName assocTypeQName, QName assocQName, boolean deep) 
	throws VersionRuntimeException;
	
	
    /**
     * Crea una nuova versione in base alla lista di node references forniti.
     * <p>
     * Se un nodo non e' stato precedentemente versionato, sara' creata una storia della
     * versione e un versione iniziale.
     * <p>
     * Se il nodo non ha associato l'aspect della versione verra' restituita un'eccezione.
     * <p>
     * Le properties della versione sono memorizzate come meta dati. 
     * 
     * 
     * @param nodeRefs              lista di node reference
     * @param versionProperties     properties della version memorizzate con la versione creata
     * @return                      collezione di versioni
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	Collection<Version> createVersion(Collection<NodeRef> nodeRefs, Map<String, Serializable> versionProperties) 
	throws VersionRuntimeException;

	/**
	 * 
     * Crea una nuova versione in base al node reference passato in input.
     * <p>
     * Se un nodo non e' stato precedentemente versionato, sara' creata una storia della
     * versione e un versione iniziale.
     * <p>
     * Se il nodo non ha associato l'aspect della versione verra' restituita un'eccezione.
     * <p>
     * Le properties della versione sono memorizzate come meta dati.
     * <p>
     * Il parametro versionChildren indica che se passato a true il figlio del nodo viene ulteriormente
     * versionato, se false no.
     *  
     * @param nodeRef               node reference
     * @param versionProperties     properties della version memorizzate con la versione creata
     * @param versionChildren       se true il children del node referenziato viene versionato altrimenti no
     * @return                      Oggetti Version creati
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	Collection<Version> createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties, boolean versionChildren) 
	throws VersionRuntimeException;

	
	/**
	 * 
     * Crea una nuova versione in base al node reference passato in input.
     * <p>
     * Se un nodo non e' stato precedentemente versionato, sara' creata una storia della
     * versione e un versione iniziale.
     * <p>
     * Se il nodo non ha associato l'aspect della versione verra' restituita un'eccezione.
     * <p>
     * Le properties della versione sono memorizzate come meta dati.
     * 
     * @param nodeRef               node reference
     * @param versionProperties     properties della version memorizzate con la versione creata
     * @return                      Oggetto Version creato
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */	
	Version createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties) 
	throws VersionRuntimeException;

	/**
	 * Restituisce l'oggetto Version per la corrente versione del node reference passato.
	 * <p>
	 * Restituisce null se il nodo non e' versionabile o non e' stato possibile versionarlo.
	 * 
	 * @param nodeRef   node reference
	 * @return			oggetto Versione per la versione corrente
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	Version getCurrentVersion(NodeRef nodeRef) 
	throws VersionRuntimeException;

	/**
	 * Cancella storia delle versioni associata al node reference passato in input.
	 * <p>
	 * L'operazione ha valenza definitiva, ossia tutte le versioni nella storia vengono cancellate e 
	 * non possono essere recuperate.
	 * <p>
	 * La versione corrente per il reference del nodo viene resettata e ogni versione successiva
	 * del nodo sara' presente nella nuova storia delle versioni che viene creata.
	 * 
	 * @param 	nodeRef					node reference
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */	
	void deleteVersionHistory(NodeRef nodeRef) throws VersionRuntimeException;
	
	
    /**
     * Restituisce la reference al Version Store
     * 
     * @return  reference al version store
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	StoreRef getVersionStoreReference() throws VersionRuntimeException;
	

	/**
	 * Il reference node passato in input sara' riportato alla versione corrente.
	 * Sara' applicata una deep revert.
 	 *  
	 * @param 	nodeRef					node reference
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	void revert(NodeRef nodeRef) throws VersionRuntimeException;
	
	
	/**
	 * Il reference node passato in input sara' riportato alla versione corrente.
	 * Sara' applicata una deep revert se deep e' passato a true altrimenti no.
 	 *  
	 * @param 	nodeRef					node reference
	 * @param 	deep					true se il revert deve essere di tipo deep altrimenti no
	 *
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	void revert(NodeRef nodeRef, boolean deep) throws VersionRuntimeException;

	
	/**
	 * Il reference node passato in input sara' riportato alla versione passata in input.
	 * Sara' applicata una deep revert
 	 *  
	 * @param 	nodeRef					node reference
	 * @param 	version					versione a cui fare revert
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	void revert(NodeRef nodeRef, Version version) throws VersionRuntimeException;
	
	/**
	 * Il reference node passato in input sara' riportato alla versione passata in input.
	 * Sara' applicata una deep revert se deep e' passato a true altrimenti no.
 	 *  
	 * @param 	nodeRef					node reference
	 * @param 	version					versione a cui fare revert
	 * @param 	deep					true se il revert deve essere di tipo deep altrimenti no
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	void revert(NodeRef nodeRef, Version version, boolean deep) throws VersionRuntimeException;

	/**
	 * Restituisce l'oggetto VersionHistory del node reference passato.
	 * <p>
	 * Restituisce null se il nodo non e' stato versionato.
	 * 
	 * @param nodeRef   node reference
	 * @return			oggetto VersionHistory del node reference
     * 
     * @throws VersionRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	VersionHistory getVersionHistory(NodeRef nodeRef) throws VersionRuntimeException;
}

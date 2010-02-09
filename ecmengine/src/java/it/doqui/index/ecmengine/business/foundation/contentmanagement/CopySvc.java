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


import it.doqui.index.ecmengine.exception.contentmanagement.CopyRuntimeException;

import java.util.List;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>Interfaccia pubblica del servizio di copy esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe <code>{@link CopySvcBean}</code>.
 * </p>
 * <p>Tutti i metodi esportati dal bean di CopySvcBean rimappano le 
 * <code>RuntimeException</code> ricevute in 
 * <code>{@link CopyRuntimeException}</code>.
 * </p>
 * 
 * @author Doqui
 * 
 * @see CopySvcBean
 * @see CopyRuntimeException
 */

public interface CopySvc extends EJBLocalObject {
	
	
	/**
	 * Permette di creare una copia del nodo passato in input.
     * (di default il figlio di un nodo source node non e' copiato)
     * 
     * @param sourceNodeRef             Nodo sorgente della copia.
     * @param destinationParent  		Nodo destinazione della copia.
     * @param destinationAssocTypeQName Tipo della nuova associazione padre - figlio.        
     * @param destinationQName 			Qualified name dell'associazione padre - figlio verso il nuovo nodo.
     * 
     * @return                          Reference al nuovo nodo.
     * 
     * @throws CopyRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	NodeRef copy(NodeRef sourceNodeRef, NodeRef destinationParent, 
			QName destinationAssocTypeQName, QName destinationQName) throws CopyRuntimeException;
	
	
	/**
	 * Permette di copiare il nodo da un nodo sorgente a un nodo destinazione 
	 * (Crea una copia del nodo passato in input).
     * 
     * <p>
     * Se il nuovo nodo e' posizionato su un workspace differente il nuovo
     * nodo avra' lo stesso identificativo
     * <p>
     * Se il nuovo nodo e' posizionato sullo stesso workspace, il nuovo nodo avra'
     * associato il Copy aspect che puntera' al nodo originale.
     * <p>
     * Gli aspect e le properties applicate al nodo sorgente saranno applicati anche al 
     * nodo di destinazione tranne per gli aspect con caratteristiche 'Non-Transferable State'
     * che verranno applicati alla copia mentre le properties verranno mantenute al valore di 
     * default.
     * <p>
     * Le child associations saranno copiate sul nodo di destinazione.
     * Se la child o la target di un'associazione copiata non e' presente nel workspace di destinazione 
     * la child association non viene copiata.
     * <p>
     * La source association non vengono copiate. 
     *
     *
     * @param sourceNodeRef             Nodo sorgente della copia.
     * @param destinationParent  		Nodo destinazione della copia.
     * @param destinationAssocTypeQName Tipo della nuova associazione padre - figlio.        
     * @param destinationQName 			Qualified name dell'associazione padre - figlio verso il nuovo nodo.
     * @param copyChildren				Indica se i figli del nodo specificato devono essere copiati.                            
     * 
     * @return                          Reference al nuovo nodo.
     * 
     * @throws CopyRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	NodeRef copy(NodeRef sourceNodeRef, NodeRef destinationParent, 
			QName destinationAssocTypeQName, QName destinationQName, boolean copyChildren) 
			throws CopyRuntimeException;

	/**
 	 * Assicura che il nome della copia sia il medesimo dell'originale oppure 
 	 * la rimomina in modo da evitare la duplicazione dei nomi.
	 * 
     * @param sourceNodeRef             Nodo sorgente della copia.
     * @param destinationParent  		Nodo destinazione della copia.
     * @param destinationAssocTypeQName Tipo della nuova associazione padre - figlio.        
     * @param destinationQName 			Qualified name dell'associazione padre - figlio verso il nuovo nodo.
     * @param copyChildren				Indica se i figli del nodo specificato devono essere copiati.                              
     * 
     * @return                          Reference al nuovo nodo.
     * 
     * @throws CopyRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	NodeRef copyAndRename(NodeRef sourceNodeRef, NodeRef destinationParent, 
			QName destinationAssocTypeQName, QName destinationQName, boolean copyChildren) 
			throws CopyRuntimeException;

		
	/**
     * Copia lo stato di un nodo su un altro.
     * <p>
     * Lo stato del nodo destinazione viene sovrascritto con lo stato del nodo source.
     * Ogni conflitto viene risolto settando lo stato del nodo source.
     * <p>
     * Se alcuni dati (associazioni) non esistono sul nodo sorgente, ma devono esistere sul nodo 
     * destination tali non viene cancellato dal nodo destination.
     * <p>
     * Le child e target associations e sono modificate sulla destination basandosi sullo stato corrente
     * del nodo source.
     * <p>
	 * 
	 * 
     * @param sourceNodeRef             Nodo sorgente della copia.
     * @param destinationParent  		Nodo destinazione della copia.
     * 
     * @throws CopyRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	void copy(NodeRef sourceNodeRef, NodeRef destinationParent) throws CopyRuntimeException;
	
	
    /**
     * Restituisce tutte le copie del nodo passato in input che sono state create usando questo servizio.
     * 
     * @param nodeRef   Il reference al nodo originale.
     * 
     * @return          Una lista di copie, eventualmente vuota.
     * 
     * @throws CopyRuntimeException Se si verifica un errore durante l'esecuzione.
  	 */
	List<NodeRef> getCopies(NodeRef nodeRef) throws CopyRuntimeException;
}

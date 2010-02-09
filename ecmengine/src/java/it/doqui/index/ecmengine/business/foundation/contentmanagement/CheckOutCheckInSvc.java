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

import it.doqui.index.ecmengine.exception.contentmanagement.CheckOutCheckInRuntimeException;

import java.io.Serializable;
import java.util.Map;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interfaccia pubblica del servizio di check-out e check-in esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link CheckOutCheckInSvcBean}.
 * 
 * <p>Tutti i metodi esportati dal bean di check-out e check-in rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link CheckOutCheckInRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see CheckOutCheckInSvcBean
 * @see CheckOutCheckInRuntimeException
 */
public interface CheckOutCheckInSvc extends EJBLocalObject {
	
	/**
	 * Permette di cancellare il checkuot sul la working copy del nodo passato in input.
	 * <p>
	 * Il lock in read only sul nodo originale e' rimosso e la working copy viene rimossa.
	 * <p>
	 * Tutte le modifiche effettuate sulla working copy vengono perdute e il nodo originale non verra'
	 * modificato.
	 * 
	 * @param node Nodo su cui cancellare il checkout.
	 * @return NodeRef del nodo su cui si e' cancellato il checkout
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef cancelCheckout(NodeRef node) throws CheckOutCheckInRuntimeException;
	
	/**
	 * Restituisce il nodo dopo l'operazione di checkin (Se non viene specificato il content url per l'operazione di 
	 * check in, il content settato sulla working copy e' considerato come il corrente)
	 * 
	 * @param node Nodo su cui applicare il checkin.
	 * @param versionProperties se valorizzato crea una nuova versione del contenuto con i valori passati in input.
	 * @return NodeRef puntamento al nodo su cui viene fatto il checkin
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef checkin(NodeRef node, Map<String, Serializable> versionProperties) 
	throws CheckOutCheckInRuntimeException;

	/**
	 * Restituisce il nodo dopo l'operazione di checkin. (per default il node su cui viene richiesto il check in non mantiene
	 * lo stato di check in. 
	 * 
	 * @param node Nodo su cui applicare il checkin.
	 * @param versionProperties se valorizzato crea una nuova versione del contenuto con i valori passati in input.
	 * @param contentUrl se valorizzato crea l aworking copy sulla posizione contentUrl.
	 * @return NodeRef puntamento al nodo su cui viene fatto il checkin)
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef checkin(NodeRef node, Map<String, Serializable> versionProperties, String contentUrl) 
	throws CheckOutCheckInRuntimeException;

	/**
	 * Effettua l'operazione di check in sul working node specificato.
	 * <p>
	 * Quando viene effettuata un'operazione di check in la working copy viene copiata sul nodo originale 
	 * (Questa operazione include ogni modifica al contenuto effettuato sul working node.
	 * <p>
	 * Se viene fornita una versione, il nodo originale verra' modificata in base alla versione passata in 
	 * input.
	 * <p>
	 * Se viene fornito un contentUrl, questo verra' utilizzato per modificare il contenuto della working copy 
	 * prima che venga effettua l'operazione di check in.
	 * <p>
	 * Terminata l'operazione di check in il lock in lettura sul nodo originale, applicato durante l'operazione
	 * di check out sara' rimosso e la working copy del nodo verra' rimossa dal repository, a meno che venga 
	 * impostato il parametro keepCheckedOut in modoo da mantenere il check out sul nodo originale (In tal
	 * caso il read only lock e la working copy rimarranno sul repository).
	 * <p>
	 * 
	 * @param node Nodo su cui applicare il checkin.
	 * @param versionProperties se valorizzato crea una nuova versione del contenuto con i valori passati in input.
	 * @param contentUrl se valorizzato crea la working copy sulla posizione contentUrl.
	 * @param keepCheckedOut se valorizzato a true il nodo viene lockato con permessi di READ_ONLY, se valorizzato
	 * a false (valore di default) cancellazione della working copy del nodo creata dall'operazione di checkout.
	 * @return NodeRef puntamento al nodo originale
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef checkin(NodeRef node, Map<String, Serializable> versionProperties, String contentUrl, boolean keepCheckedOut) 
	throws CheckOutCheckInRuntimeException;
	
	/**
	 * Effettua l'operazione di checkout di un nodo ponendo la working copy nel medesimo parent node con
	 * la medesima child association.
	 * 
	 * @param node Nodo su cui applicare il checkout.
	 * @return NodeRef del nodo che contiene la working copy del nodo.
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef checkout(NodeRef node) throws CheckOutCheckInRuntimeException;
	
	/** 
	 * Effettua l'operazione di checkout mettendo la working copy nella destinazione specificata.
	 * <p>
	 * Quando ad un nodo e' applicata un'operazione di check-out, su tale nodo viene inserito un lock
	 * in read-only e la working copy viene inserita nella destinazione specificata.
	 * <p>
	 * Sulla working copy viene applicato un copy aspect in modo che venga identificato come working copy
	 * di un nodo a cui e' stato applicato un checkout.
	 * <p>
	 * Il nodo working copy e' restituito come parametro di output. 
	 * 
	 * @param node Nodo su cui applicare il checkout.
	 * @param destinationParentNodeRef Nodo parent di destinazione.
	 * @param destinationAssocTypeQName tipo di asscociazione della destination della working copy.
	 * @param destinationAssocQName associazione della destination della working copy.
	 * @return NodeRef del nodo che contiene la working copy del nodo.
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef checkout(NodeRef node, NodeRef destinationParentNodeRef, QName destinationAssocTypeQName, QName destinationAssocQName) 
	throws CheckOutCheckInRuntimeException;
	
	/**
	 * Restituisce la reference alla working copy del nodo a cui per un nodo su cui e' stato applicata
	 * l'operazione di check out.
     * <p>
     * Se il nodo non e' in stato di check out viene restituito null.
	 * 
	 * @param node Nodo su cui controllare la working copy
	 * @return NodeRef contenente la working copy, null se al nodo non e' associata alcuna working copy. 
	 * 
	 * @throws CheckOutCheckInRuntimeException Se si verifica un errore durante l'operazione.
	 */
	NodeRef getWorkingCopy(NodeRef node) throws CheckOutCheckInRuntimeException;
}

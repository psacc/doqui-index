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


import it.doqui.index.ecmengine.exception.contentmanagement.LockRuntimeException;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * <p>Interfaccia pubblica del servizio di lock esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe <code>{@link LockSvcBean}</code>.
 * </p>
 * <p>Tutti i metodi esportati dal bean di lovk rimappano le 
 * <code>RuntimeException</code> ricevute in 
 * <code>{@link LockRuntimeException}</code>.
 * </p>
 * 
 * @author Doqui
 * 
 * @see LockSvcBean
 * @see LockRuntimeException
 */

public interface LockSvc extends EJBLocalObject {
	
	/**
	 * Controlla se un nodo e' locked o no. 
	 * 
	 * @param node Nodo su cui controllare la possibilita' di lock.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	 void checkForLock(NodeRef node) throws LockRuntimeException;
	
	/**
	 * Restituisce tutti i node reference (oggetto LockStatus) che l'utente corrente ha in stato locked. 
	 * 
	 * @param inStore Store su cui vedere i nodi lockati.
	 * @return Lista di oggetti NodeRef su cui controllare i locks 
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	List<NodeRef> getLocks(StoreRef inStore) throws LockRuntimeException;

	/**
	 * Restituisce tutti i node reference (oggetto LockStatus) che l'utente corrente ha in stato locked,
	 * relativi al LockType passato in input
	 *
	 * @param inStore Store su cui vedere i nodi lockati.
	 * @param inLockType Tipologia di lock da testare.
	 * @return Lista di oggetti NodeRef su cui controllare i locks 
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	List<NodeRef> getLocks(StoreRef inStore, LockType inLockType) throws LockRuntimeException;
	
	/**
	 * Restituisce lo stato (oggetto LockStatus) di lock di un nodo passato in input 
	 * relativo all'utente con cui si sta richiamando il servizio 
 	 * 
	 * @param inNodeRef Nodo su cui restituire lo stato.
	 * @return Stato dei lock del nodo 
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	LockStatus getLockStatus(NodeRef inNodeRef) throws LockRuntimeException;
	
	/**
	 * Restituisce il tipo (oggetto LockType) di lock di un nodo passato in input.
	 * <p>
	 * Restituisce null se il nodo non e' locked.
	 * 
	 * @param inNodeRef Nodo su cui restituire lo stato.
	 * @return Tipo dei lock del nodo 
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	LockType getLockType(NodeRef inNodeRef) throws LockRuntimeException;
	
	/**
	 * Lock del nodo passato in input con la tipologia del lock passato in input.
     * <p>
     * Il lock vieta che altri utenti e processi committino modifiche sul nodo prima che il lock sia
     * rilasciato.
     * <p>
     * Lo user reference passato in input indica chi sara' il proprietario del lock.
     * <p>
     * Il lock richiamato con questo non ha scadenza temporale.
   	 * 
	 * @param inNodeRef Nodo da lockare.
	 * @param inLockType Tipo di lock da impostare.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void lock(NodeRef inNodeRef, LockType inLockType) throws LockRuntimeException;
	
	/**
	 * Lock del nodo passato in input con la tipologia del lock passato in input.
     * Il lock vieta che altri utenti e processi committino modifiche sul nodo prima che il lock sia
     * rilasciato.
     * <p>
     * Lo user reference passato in input indica chi sara' il proprietario del lock.
     * <p>
     * Se inTimeToExpire e' valorizzato a 0 il lock non scadra' mai, altrimenti inTimeToExpire
     * indica il numero di secondi prima che il lock scada. Quando inTimeToExpire scade il lock
     * viene considerato rilasciato.
     * <p>
     * Se un nodo e' gia lockato e l'utente e' proprietario del lock il lock verra' aggiornato con il valore
     * passato inTimeToExpire.
	 * 
	 * @param inNodeRef Nodo da lockare.
	 * @param inLockType Tipo di lock da impostare.
	 * @param inTimeToExpire Tempo di scadenza del lock.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void lock(NodeRef inNodeRef, LockType inLockType, int inTimeToExpire) throws LockRuntimeException;
	
	/**
	 * Lock del nodo passato in input con la tipologia del lock passato in input, il tempo di scadenza del
	 * lock e se il lock debba essere propagato ai figli dei nodi.
     * Il lock vieta che altri utenti e processi committino modifiche sul nodo prima che il lock sia
     * rilasciato.
     * <p>
     * Lo user reference passato in input indica chi sara' il proprietario del lock.
     * Se uno dei lock sui child non puo' essere forzato, verra' restituita un'eccezione e tutti
     * i lock richiesti verranno cancellati.
     * <p>
     * Se inTimeToExpire e' valorizzato a 0 il lock non scadra' mai, altrimenti inTimeToExpire
     * indica il numero di secondi prima che il lock scada. Quando inTimeToExpire scade il lock
     * viene considerato rilasciato.
     * <p>
     * Se un nodo e' gia lockato e l'utente e' proprietario del lock il lock verra' aggiornato con il valore
     * passato inTimeToExpire.
	 * 
	 * @param inNodeRef Nodo da lockare.
	 * @param inLockType Tipo di lock da impostare.
	 * @param inTimeToExpire Tempo di scadenza del lock.
	 * @param inLockChildren Lock sui figli del nodo.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void lock(NodeRef inNodeRef, LockType inLockType, int inTimeToExpire, 
			boolean inLockChildren) throws LockRuntimeException;
	
	/**
	 * Lock della collezione di nodi passati in input con la tipologia del lock passato in input e il
	 * tempo di scadenza del lock.
     * Il lock vieta che altri utenti e processi committino modifiche sul nodo prima che il lock sia
     * rilasciato.
     * <p>
     * Lo user reference passato in input indica chi sara' il proprietario del lock.
     * Se uno dei lock sui child non puo' essere forzato, verra' restituita un'eccezione e tutti
     * i lock richiesti verranno cancellati.
     * <p>
     * Se inTimeToExpire e' valorizzato a 0 il lock non scadra' mai, altrimenti inTimeToExpire
     * indica il numero di secondi prima che il lock scada. Quando inTimeToExpire scade il lock
     * viene considerato rilasciato.
     * <p>
     * Se un nodo e' gia lockato e l'utente e' proprietario del lock il lock verra' aggiornato con il valore
     * passato inTimeToExpire.
	 * 
	 * 
	 * @param inCollectionNodeRef Collezione di nodi da lockare.
	 * @param inLockType Tipo di lock da impostare.
	 * @param inTimeToExpire Tempo di scadenza del lock.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void lock(Collection<NodeRef> inCollectionNodeRef, LockType inLockType, 
			int inTimeToExpire) throws LockRuntimeException;
	
	/**
	 * Rimozione dei lock sulla collezione di nodi passati in input.
     * <p>
     * L'utente deve avere sufficienti privilegi per la rimozione dei locks (proprietario dei locks
     * o diritti di utente admin) altrimenti verra' restituita un'eccezione.
     * <p>
     * Se uno dei nodi passati in input non e' locked verra' ignorato e l'operazione di unlock 
     * continuera' senza errori. 
     * <p>
     * Se il lock di uno dei nodi non puo' essere rilasciato verra' sollevata un'eccezione e l'operazione
     * di unlock terminera' con un'operazione di rollback.
  	 * 
	 * @param inCollectionNodeRef Collezione di nodi su cui rimuovere il lock.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void unlock(Collection<NodeRef> inCollectionNodeRef) throws LockRuntimeException;
	
	/**
	 * Rimozione dei lock sul nodo passato in input.
     * <p>
     * L'utente deve avere sufficienti permessi per rimuovere il lock (proprietario del lock o 
     * permessi da utente admin) altrimenti viene restituita un'eccezione.
     * 
	 * 
	 * @param inNodeRef Nodo su cui rimuovere il lock.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void unlock(NodeRef inNodeRef) throws LockRuntimeException;
	
	/**
	 * Rimozione dei lock sul nodo passato in input e sui nodi figli di quello passato.
     * <p>
     * L'utente deve avere sufficienti permessi per rimuovere il lock (proprietario del lock o 
     * permessi da utente admin) altrimenti viene restituita un'eccezione.
     * <p>
     * Se uni dei child non e' in stato locked verra' ignorato e il processo di rilascio del lock continuera' 
     * senza errori.
     * <p>
     * Se il lock di un dei nodi child non puo' essere rilasciato verra' restituita un'eccezione.
	 * 
	 * @param inNodeRef Nodo su cui rimuovere il lock.
	 * @param inUnlockChildren La rimozione del lock deve essere esteso ai nodi figli.
	 * 
	 * @throws LockRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void unlock(NodeRef inNodeRef, boolean inUnlockChildren) throws LockRuntimeException;
}

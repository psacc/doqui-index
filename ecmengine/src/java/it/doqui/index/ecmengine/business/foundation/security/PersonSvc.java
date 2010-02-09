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
 
package it.doqui.index.ecmengine.business.foundation.security;

import it.doqui.index.ecmengine.exception.security.PersonRuntimeException;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interfaccia pubblica del servizio di gestione delle persone esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link PersonSvcBean}.
 * 
 * <p>Tutti i metodi esportati dal bean di gestione delle persone rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link PersonRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see PersonSvcBean
 * @see PersonRuntimeException
 */

public interface PersonSvc extends EJBLocalObject {
    /**
     * Cerca il nodo che rappresenta la persona specificata all'interno
     * del repository Alfresco. La persona potrebbe essere creata come
     * side-effect della chiamata a questo metodo.
     * 
     * @param userName Il nome utente usato come chiave nella ricerca.
     * 
     * @return Il riferimento al nodo che rappresenta la persona, 
     * sia esso nuovo o precedentemente esistente. Se l'utente non esiste
     * e non &egrave; possibile crearlo restituisce <code>null</code>.
     * 
     * @see #setCreateMissingPeople(boolean)
     * @see #createMissingPeople()
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    NodeRef getPerson(String userName) throws PersonRuntimeException;

    /**
     * Controlla se nel repository Alfresco esiste una persona con il
     * nome utente specificato.
     * 
     * @param userName Il nome utente da cercare.
     * 
     * @return <code>true</code> se la persona esiste, <code>false</code> 
     * altrimenti.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    boolean personExists(String userName) throws PersonRuntimeException;

    /**
     * Restituisce un valore booleano che indica il comportamento del
     * servizio con riferimento alle persone mancanti.
     * 
     * @return <code>true</code> se il sistema crea automaticamente 
     * una persona mancante richiesta mediante una chiamata al metodo
     * <code>getPerson()</code>, <code>false</code> se le persone 
     * mancanti non vengono create in modo automatico.
     * 
     * @see #getPerson(String)
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    boolean createMissingPeople() throws PersonRuntimeException;

    /**
     * Imposta il valore booleano che modifica il comportamento del
     * servizio con riferimento alle persone mancanti.
     * 
     * @param createMissing Il valore boolean da impostare:<br/>
     * <ul>
     *   <li><code>true</code> = creazione automatica degli utenti mancanti attivata</li>
     *   <li><code>false</code> = creazione automatica degli utenti mancanti disattivata</li>
     * </ul>
     * 
     * @see #getPerson(String)
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    void setCreateMissingPeople(boolean createMissing) throws PersonRuntimeException;

    /**
     * Restituisce una lista delle propriet&agrave; delle persone che
     * possono essere modificate. Alcuni sistemi potrebbero consentire
     * la modifica di un insieme ridotto di propriet&agrave;
     * 
     * @return Un insieme degli identificativi delle propriet&agrave; 
     * modificabili.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    Set<QName> getMutableProperties() throws PersonRuntimeException;

    /**
     * Imposta le propriet&agrave; di una persona.
     * 
     * @param userName Il nome utente della persona alla quale saranno applicate le propriet&agrave;.
     * @param properties La mappa di propriet&agrave; che devono essere impostate.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    void setPersonProperties(String userName, Map<QName, Serializable> properties)
    		throws PersonRuntimeException;

    /**
     * Restituisce il valore booleano che indica se questo servizio
     * consente di modificare le persone.
     * 
     * @return <code>true</code> se questo servizio permette di modificare
     * le persone, <code>false</code> altrimenti.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    boolean isMutable() throws PersonRuntimeException;

    /**
     * Crea una nuova persona assegnandole l'insieme di propriet&agrave;
     * specificato. Il nome utente deve essere specificato all'interno
     * di tale insieme. <strong>Persone diverse con lo stesso nome utente 
     * non sono ammesse.</strong>
     * 
     * @param properties La mappa di propriet&agrave; che 
     * devono essere impostate sulla nuova persona creata.
     * 
     * @return Il riferimento al nodo che rappresenta la persona appena
     * creata.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    NodeRef createPerson(Map<QName, Serializable> properties) throws PersonRuntimeException;

    /**
     * Elimina la persona identificata dal nome utente specificato.
     * 
     * @param userName Il nome utente della persona da eliminare.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    void deletePerson(String userName) throws PersonRuntimeException;

    /**
     * Restituisce tutte le persone note a questo servizio.
     * 
     * @return L'insieme dei riferimenti a tutte le persone note a 
     * questo servizio.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    Set<NodeRef> getAllPeople() throws PersonRuntimeException;

    /**
     * Restituisce il riferimento al nodo che contiene tutte le persone.
     * 
     * @return Il riferimento al nodo contenente tutte le persone.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    NodeRef getPeopleContainer() throws PersonRuntimeException;
    
    /**
     * Restituisce un valore booleano che indica se questo servizio
     * interpreta i nomi utente in modo &quot;case sensitive&quot;.
     * 
     * @return <code>true</code> se i nomi utente sono interpretati
     * in modo &quot;case sensitive&quot;, <code>false</code> altrimenti.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    boolean getUserNamesAreCaseSensitive() throws PersonRuntimeException;

    /**
     * Restituisce l'identificativo della persona il cui nome utente
     * (case-sensitive) &egrave; specificato in input.
     * 
     * @param caseSensitiveUserName Il nome utente usato come chiave 
     * di ricerca (case-sensitive).
     * 
     * @return L'identificativo associato al nome utente specificato, 
     * oppure <code>null</code> se nessuna persona corrispondente &egrave;
     * stata trovata.
     * 
     * @throws PersonRuntimeException Se si verifica un errore durante l'esecuzione.
     */
    String getUserIdentifier(String caseSensitiveUserName) throws PersonRuntimeException;
}

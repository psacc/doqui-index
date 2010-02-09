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

import it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException;

import java.util.Set;

import javax.ejb.EJBLocalObject;

/**
 * <p>Interfaccia pubblica del servizio di autenticazione esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link AuthenticationSvcBean}.
 * </p>
 * <p>Tutti i metodi esportati dal bean di autenticazione rimappano le
 * {@code RuntimeException} ricevute in
 * {@link it.doqui.index.ecmengine.exception.security.AuthenticationRuntimeException}.
 * </p>
 *
 * @author Doqui
 *
 * @see AuthenticationSvcBean
 * @see AuthenticationRuntimeException
 */

public interface AuthenticationSvc extends EJBLocalObject {

	/**
	 * Permette di associare le informazioni di autenticazione (nome utente e password)
	 * ad una persona.
	 *
	 * @param userName Il nome utente della persona a cui associare la password.
	 * @param password La password da associare alla persona.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void createAuthentication(String userName, char [] password) throws AuthenticationRuntimeException;

	/**
	 * Aggiorna la password associata a un utente sul repository Alfresco.
	 *
	 * @param userName Il nome utente la cui password deve essere aggiornata.
	 * @param oldPassword La vecchia password.
	 * @param newPassword La nuova password.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void updateAuthentication(String userName, char [] oldPassword, char [] newPassword)
	throws AuthenticationRuntimeException;

	/**
	 * Imposta la password associata a un utente sul repository Alfresco
	 * (non richiede la vecchia password).
	 *
	 * @param userName Il nome utente la cui password deve essere impostata.
	 * @param password La password da impostare.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void setAuthentication(String userName, char [] password) throws AuthenticationRuntimeException;

	/**
	 * Rimuove le informazioni di autenticazione per un utente.
	 *
	 * @param userName Il nome utente le cui informazioni di
	 * autenticazione devono essere rimosse.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void deleteAuthentication(String userName) throws AuthenticationRuntimeException;

	/**
	 * Controlla lo stato di abilitazione del record di autenticazione di un utente.
	 *
	 * @param userName Il nome utente.
	 *
	 * @return <code>true</code> se il record di autenticazione per l'utente specificato
	 * &egrave; abilitato, <code>false</code> altrimenti.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean getAuthenticationEnabled(String userName)
	throws AuthenticationRuntimeException;

	/**
	 * Imposta lo stato di abilitazione del record di autenticazione di un utente.
	 *
	 * @param userName Il nome utente.
	 * @param enabled Lo stato di abilitazione:<br/>
	 * <ul>
	 *   <li><code>true</code> = abilitato</li>
	 *   <li><code>false</code> = disabilitato</li>
	 * </ul>
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void setAuthenticationEnabled(String userName, boolean enabled)
	throws AuthenticationRuntimeException;

	/**
	 * Esegue l'autenticazione come utente guest sul repository Alfresco.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void authenticateAsGuest() throws AuthenticationRuntimeException;

	/**
	 * Controlla se &egrave; consentita l'autenticazione come utente
	 * guest sul repository Alfresco.
	 *
	 * @return <code>true</code> se l'autenticazione come utente
	 * guest &egrave; consentita, <code>false</code> altrimenti.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean guestUserAuthenticationAllowed() throws AuthenticationRuntimeException;

	/**
	 * Controlla se un utente ha delle informazioni di autenticazioni
	 * associate.
	 *
	 * @param userName Il nome utente su cui effettuare il controllo.
	 *
	 * @return <code>true</code> se le informazioni di autenticazione esistono,
	 * <code>false</code> altrimenti.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean authenticationExists(String userName) throws AuthenticationRuntimeException;

	/**
	 * Restituisce il nome utente associato all'utente correntemente
	 * autenticato sul repository Alfresco.
	 *
	 * @return Il nome utente dell'utente corrente.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getCurrentUserName() throws AuthenticationRuntimeException;

	/**
	 * Invalida le sessioni dell'utente associato al nome utente specificato.
	 *
	 * @param userName Il nome utente le cui sessioni devono essere invalidate.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void invalidateUserSession(String userName) throws AuthenticationRuntimeException;

	/**
	 * Invalida la sessione identificata dal ticket specificato.
	 *
	 * @param ticket Il ticket da invalidare.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void invalidateTicket(String ticket) throws AuthenticationRuntimeException;

	/**
	 * Restituisce il ticket associato alla sessione corrente.
	 *
	 * @return Il ticket associato alla sessione corrente.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getCurrentTicket() throws AuthenticationRuntimeException;

	/**
	 * Ripulisce il security context corrente da tutte le informazioni
	 * di sicurezza.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void clearCurrentSecurityContext() throws AuthenticationRuntimeException;

	/**
	 * Verifica se l'utente corrente &egrave; l'utente di sistema.
	 * Nell'implementazione di default il nome dell'utente di sistema
	 * &egrave; definito in <code>AbstractAuthenticationComponent.SYSTEM_USER_NAME</code>
	 * (package: <code>org.alfresco.repo.security.authentication</code>)
	 *
	 * @return <code>true</code> se l'utente corrente &egrave; l'utente di sistema,
	 * <code>false</code> altrimenti.
	 *
	 * @see org.alfresco.repo.security.authentication.AbstractAuthenticationComponent#isCurrentUserTheSystemUser()
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean isCurrentUserTheSystemUser() throws AuthenticationRuntimeException;

	/**
	 * Restituisce la lista dei domini supportati dal servizio di
	 * autenticazione corrente.
	 *
	 * @return La lista dei domini.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getDomains() throws AuthenticationRuntimeException;

	/**
	 * Restituisce la lista dei domini sui quali &egrave; possibile
	 * creare nuovi utenti.
	 *
	 * @return La lista dei domini.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getDomainsThatAllowUserCreation() throws AuthenticationRuntimeException;

	/**
	 * Restituisce la lista dei domini dai quali &egrave; possibile
	 * eliminare utenti.
	 *
	 * @return La lista dei domini.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getDomainsThatAllowUserDeletion() throws AuthenticationRuntimeException;

	/**
	 * Restituisce la lista dei domini sui quali gli utenti possono
	 * cambiare la loro password.
	 *
	 * @return La lista dei domini.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getDomiansThatAllowUserPasswordChanges() throws AuthenticationRuntimeException;

	/**
	 * Esegue l'autenticazione di un utente sul repository Alfresco.
	 *
	 * @param userName Il nome utente.
	 * @param password La password.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void authenticate(String userName, char [] password)
	throws AuthenticationRuntimeException;

	/**
	 * Valida il ticket passato in input rispetto alla sessione dell'utente corrente.
	 *
	 * @param ticket Il ticket da validare.
	 *
	 * @throws AuthenticationRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void validate(String ticket) throws AuthenticationRuntimeException;
}

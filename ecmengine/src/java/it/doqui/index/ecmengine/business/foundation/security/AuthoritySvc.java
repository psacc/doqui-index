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

import it.doqui.index.ecmengine.exception.security.AuthorityRuntimeException;

import java.util.Set;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Interfaccia pubblica del servizio di gestione delle authority esportata come
 * componente EJB 2.1. L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link AuthoritySvcBean}.
 *
 * <p>Tutti i metodi esportati dal bean rimappano le
 * {@code RuntimeException} ricevute in
 * <code>{@link AuthorityRuntimeException}</code>.
 * </p>
 * <p>Un'authority &egrave; identificata da un <i>nome completo</i> costruito a partire
 * dal tipo e dal <i>nome breve</i>. Se non specificato diversamente in seguito si
 * intender&agrave; con &quot;nome&quot; il nome completo.
 * </p>
 *
 * @author Doqui
 *
 * @see AuthoritySvcBean
 * @see AuthorityRuntimeException
 */
public interface AuthoritySvc extends EJBLocalObject  {

	/**
	 * Aggiunge una authority come figlia di un'altra authority.
	 *
	 * @param parentName Il nome dell'authority madre.
	 * @param childName Il nome dell'authority figlia.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void addAuthority(String parentName, String childName) throws AuthorityRuntimeException;

	/**
	 * Verifica se l'authority identificata dal nome specificato in input esiste.
	 *
	 * @param name Il nome completo dell'authority di cui si vuole verificare
	 * l'esistenza.
	 *
	 * @return <code>true</code> se l'authority esiste, <code>false</code> altrimenti.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean authorityExists(String name) throws AuthorityRuntimeException;

	/**
	 * Crea una nuova authority a partire la nome breve e dal tipo indicati in input
	 * e la aggiunge come figlia dell'authority specificata. L'authority viene creata
	 * solo se il tipo specificato &egrave; un tipo modificabile, altrimenti la
	 * chiamata al metodo genera una condizione di errore di tipo
	 * <code>{@link it.doqui.index.ecmengine.exception.security.AuthorityRuntimeException}</code>.
	 *
	 * @param type Il tipo della nuova authority (deve essere un tipo modificabile).
	 * @param parentName Il nome dell'authority madre (o <code>null</code> per
	 * creare una <i>root authority</i>).
	 * @param shortName Il nome breve dell'authority da creare.
	 *
	 * @return Il nome della nuova authority creata.
	 *
	 * @see it.doqui.index.ecmengine.exception.security.AuthorityRuntimeException
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String createAuthority(AuthorityType type, String parentName, String shortName)
	throws AuthorityRuntimeException;

	/**
	 * Elimina dal repository un'authority e tutte le sue relazioni con altri nodi.
	 *
	 * @param name Il nome dell'authority da eliminare.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void deleteAuthority(String name) throws AuthorityRuntimeException;

	/**
	 * Fornisce un insieme di tutte le authority conosciute dal sistema filtrate
	 * in base al tipo specificato. Specificare <code>null</code> come tipo
	 * equivale a cercare tutte le authority.
	 *
	 * @param type Il tipo in base al quale filtrare il risultato (o
	 * <code>null</code> per includere tutte le authority).
	 *
	 * @return Un <code>Set</code> contenente tutte le authority che corrispondono
	 * al tipo specificato.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getAllAuthorities(AuthorityType type) throws AuthorityRuntimeException;

	/**
	 * <p>Fornisce un insieme di tutte le <i>root authority</i> conosciute dal
	 * sistema filtrate in base al tipo specificato. Specificare
	 * <code>null</code> come tipo equivale a cercare tutte le authority.</p>
	 *
	 * <p>Una <i>root authority</i> &egrave; un'authority per la quale non esiste
	 * un'authority madre.</p>
	 *
	 * @param type Il tipo in base al quale filtrare il risultato (o
	 * <code>null</code> per includere tutte le authority).
	 *
	 * @return Un <code>Set</code> contenente tutte le root authority che corrispondono
	 * al tipo specificato.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getAllRootAuthorities(AuthorityType type) throws AuthorityRuntimeException;

	/**
	 * Restituisce un insieme di tutte le authority (di qualunque tipo esse siano)
	 * associate all'utente correnteo.
	 *
	 * @return Un <code>Set</code> contenente tutte le authority associate all'utente
	 * corrente.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getAuthorities() throws AuthorityRuntimeException;

	/**
	 * Restituisce un insieme di authority contenute nell'authority specificata,
	 * filtrando il risultato in base al tipo specificato ed eventualmente limitando
	 * la ricerca al primo livello di annidamento.
	 *
	 * @param type Il tipo in base al quale filtrare il risultato (o
	 * <code>null</code> per includere tutte le authority).
	 * @param name Il nome dell'authority contenitore in cui effettuare la ricerca.
	 * @param immediate La profondit&agrave; di ricerca:
	 * <ul>
	 *   <li><code>true</code> = limitata al primo livello di annidamento</li>
	 *   <li><code>false</code> = ricorsiva</li>
	 * </ul>
	 *
	 * @return Un <code>Set</code> contenente tutte le root authority che corrispondono
	 * ai criteri specificati.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
	throws AuthorityRuntimeException;

	/**
	 * Restituisce un insieme di authority che contengono l'authority specificata,
	 * filtrando il risultato in base al tipo specificato ed eventualmente limitando
	 * la ricerca al primo livello di contenimento.
	 *
	 * @param type Il tipo in base al quale filtrare il risultato (o
	 * <code>null</code> per includere tutte le authority).
	 * @param name Il nome dell'authority contenuta su cui effettuare la ricerca.
	 * @param immediate La profondit&agrave; di ricerca:
	 * <ul>
	 *   <li><code>true</code> = limitata al primo livello di contenimento</li>
	 *   <li><code>false</code> = ricorsiva</li>
	 * </ul>
	 *
	 * @return Un <code>Set</code> contenente tutte le root authority che corrispondono
	 * ai criteri specificati.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
	throws AuthorityRuntimeException;

	/**
	 * Restituisce il nome completo di un'authority partendo dal tipo e dal nome
	 * breve specificati.
	 *
	 * @param type Il tipo di authority.
	 * @param shortName Il nome breve dell'authority.
	 *
	 * @return Il nome completo che identifica l'authority.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getName(AuthorityType type, String shortName) throws AuthorityRuntimeException;

	/**
	 * Restituisce il nome breve di un'authority partendo dal nome completo
	 * specificato.
	 *
	 * @param name Il nome completo dell'authority.
	 *
	 * @return Il nome breve dell'authority.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getShortName(String name) throws AuthorityRuntimeException;

	/**
	 * Controlla se all'utente corrente &egrave; associata l'authority di
	 * amministrazione.
	 *
	 * @return <code>true</code> se l'utente corrente &egrave; associato
	 * all'authority amministrativa, <code>false</code> altrimenti.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean hasAdminAuthority() throws AuthorityRuntimeException;

	/**
	 * <p>Rimuove un'authority dall'insieme delle figlie di un'altra authority. Se
	 * l'authority rimossa non ha pi&ugrave; un'authority madre, essa diventa una
	 * <i>root authority</i>.</p>
	 *
	 * <p><strong>NB:</strong> questo metodo non elimina l'authority, ma la sola
	 * associazione &quot;madre - figlia&quot;. Per eliminare un'authority &egrave;
	 * necessario usare il metodo <code>{@link #deleteAuthority(String)}</code>.</p>
	 *
	 * @param parentName Il nome dell'authority madre.
	 * @param childName Il nome dell'authority figlia.
	 *
	 * @throws AuthorityRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void removeAuthority(String parentName, String childName) throws AuthorityRuntimeException;

}

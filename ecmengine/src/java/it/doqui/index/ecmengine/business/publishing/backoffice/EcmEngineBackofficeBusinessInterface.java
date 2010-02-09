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

package it.doqui.index.ecmengine.business.publishing.backoffice;

import it.doqui.index.ecmengine.dto.AclRecord;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.backoffice.AclListParams;
import it.doqui.index.ecmengine.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.dto.backoffice.ExportedContent;
import it.doqui.index.ecmengine.dto.backoffice.Group;
import it.doqui.index.ecmengine.dto.backoffice.IntegrityReport;
import it.doqui.index.ecmengine.dto.backoffice.Repository;
import it.doqui.index.ecmengine.dto.backoffice.SystemProperty;
import it.doqui.index.ecmengine.dto.backoffice.Tenant;
import it.doqui.index.ecmengine.dto.backoffice.User;
import it.doqui.index.ecmengine.dto.backoffice.model.CustomModel;
import it.doqui.index.ecmengine.dto.backoffice.model.ModelDescriptor;
import it.doqui.index.ecmengine.dto.backoffice.model.ModelMetadata;
import it.doqui.index.ecmengine.dto.backoffice.model.TypeMetadata;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.AclEditException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupAlreadyExistsException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupCreateException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupDeleteException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.GroupEditException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchUserException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.TooManyNodesException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserAlreadyExistsException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserCreateException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserDeleteException;
import it.doqui.index.ecmengine.exception.publishing.backoffice.UserUpdateException;

import java.rmi.RemoteException;

/**
 * <p>Interfaccia di business che raccoglie i servizi pubblici
 * orchestrati che vengono esportati dall'ECMENGINE per le funzionalit&agrave;
 * di backoffice.</p>
 *
 * @author Doqui
 */
public interface EcmEngineBackofficeBusinessInterface {

	/**
	 * Crea un nuovo utente sul repository.
	 *
	 * <p>
	 * Questo metodo esegue la creazione di un nuovo utente applicativo sul
	 * repository dell'ECMENGINE in accordo con i valori contenuti nell'istanza
	 * di {@link User} passata in input. Poich&eacute; non possono esistere sul
	 * repository pi&ugrave; utenti con lo stesso username questo metodo
	 * potrebbe sollevare un'eccezione di tipo
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.UserAlreadyExistsException}.
	 * </p>
	 *
	 * <p>
	 * Se l'oggetto {@link User} contiene anche una password, questo metodo crea
	 * le infomazioni di autenticazione per l'utente applicativo.
	 * </p>
	 *
	 * <p>
	 * Il repository destinazione per la creazione dell'utente viene letto dal
	 * DTO {@link OperationContext}. Se il valore non &egrave; specificato la
	 * creazione &egrave; effettuata sul repository configurato come default.
	 * </p>
	 *
	 * <p>
	 * Nel DTO {@code User} pu&ograve; essere specificato un <i>home folder</i>
	 * per l'utente sotto forma di path. Il path specificato puo` rappresentare
	 * una delle seguenti:
	 * <ul>
	 * <li>la posizione in cui creare il folder, per esempio,
	 * &quot;/app:company_home/app:user_homes&quot; (tale path deve essere
	 * gi&agrave; presente nel repository)</li>
	 * <li>il nome del folder da creare nel formato
	 * {@code &lt;path&gt;/sys:&lt;username&gt;}, per esempio
	 * &quot;/app:company_home/cm:doqui/cm:users/app:doquiuser&quot; ({@code &lt;path&gt;}
	 * deve essere gi&agrave; presente nel repository)</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * <strong>NB:</strong> questo metodo crea un nuovo <i>home folder</i> per
	 * l'utente in una posizione predefinita non configurabile. Questo
	 * comportamento &egrave; voluto poich&eacute; tale folder non sar&agrave;
	 * mai accessibile direttamente all'utente applicativo.
	 * </p>
	 *
	 * @param nuovoUtente
	 *            L'oggetto {@link User} contenente i dati del nuovo utente da
	 *            creare.
	 * @param context
	 *            L'oggetto {@link OperationContext} contenente i dati di
	 *            autenticazione dell'amministratore che esegue la creazione del
	 *            nuovo utente.
	 *
	 * @return Lo username univoco del nuovo utente creato.
	 *
	 * @throws InvalidParameterException
	 *             Se uno o pi&ugrave; parametri specificati sono inconsistenti.
	 * @throws InvalidCredentialsException
	 *             Se vengono fornite credenziali di autenticazione errate.
	 * @throws UserCreateException
	 *             Se si verifica un errore durante la creazione dell'utente (e
	 *             la causa dell'errore non &egrave; la presenza di un utente
	 *             con lo stesso username).
	 * @throws UserAlreadyExistsException
	 *             Se l'utente di cui &egrave; stata richiesta la creazione
	 *             esiste gi&agrave;.
	 * @throws PermissionDeniedException
	 *             Se l'utente non ha permessi sufficienti per compiere
	 *             l'operazione richiesta.
	 * @throws EcmEngineTransactionException
	 *             Se si verifica un errore durante l'esecuzione della
	 *             transazione.
	 * @throws RemoteException
	 *             Se si verifica un errore durante la comunicazione.
	 */
	String createUser(User nuovoUtente, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserCreateException, UserAlreadyExistsException,
	EcmEngineTransactionException, PermissionDeniedException, RemoteException;

	/**
	 * Aggiorna i metadati dell'utente specificato.
	 *
	 * @param utente L'oggetto {@link User} contenente i dati dell' utente da aggiornare.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di autenticazione
	 * dell'amministratore che esegue la modifica dei metadati dell' utente.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchUserException Se l'utente specificato non esiste.
	 * @throws UserUpdateException Se si verifica un errore durante la modifica dei metadati dell'utente (e
	 * la causa dell'errore non &egrave; l'assenza dell'utente).
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void updateUserMetadata(User utente,OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException,
	EcmEngineTransactionException, PermissionDeniedException, RemoteException;

	/**
	 * Restituisce la lista degli utenti sull'ECMENGINE.
	 *
	 * <p>Questo metodo esegue una ricerca degli utenti sull'ECMENGINE restituendo i risultati
	 * sotto forma di array di DTO {@link User}. Il parametro {@link User} passato in input
	 * permette di specificare il repository su cui effettuare la ricerca e i filtri da
	 * impostare sullo username dell'utente. </p>
	 *
	 * <p><strong>NB:</strong> l'unico campo utilizzato del DTO {@link User} utilizzato come filtro
	 * &egrave; il campo {@code userName} e permette di specificare il carattere '*' come
	 * wildcard. Se non viene specificato uno {@code userName} vengono cercati tutti gli utenti.</p>
	 *
	 * @param filter L'oggetto {@link User} da utilizzare come filtro.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di autenticazione
	 * dell'amministratore che esegue la ricerca degli utenti.
	 *
	 * @return Un array di risultati sotto forma di oggetti {@link User}.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws EcmEngineException Se si verifica un errore durante l'esecuzione della ricerca.
	 * @throws NoDataExtractedException Se non viene trovato nessun risultato che corrisponde
	 * al filtro specificato.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	User [] listAllUsers(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	EcmEngineTransactionException, RemoteException;


	/**
	 * Ritorna i metadati dell'utente specificato.
	 *
	 * <p>Il parametro {@link User} passato in input permette di specificare il repository
	 * su cui effettuare la ricerca e il filtro da impostare sullo username dell'utente.
	 * Il campo del DTO {@link User} utilizzato come filtro &egrave; il campo {@code nomeUtente}.</p>
	 *
	 * @param filter L'oggetto {@link User} contenente il filtro utilizzato per la ricerca.
	 * @param context  L'oggetto {@link OperationContext} contenente i dati di autenticazione
	 * dell'amministratore che esegue la ricerca dell'utente.
	 *
	 * @return Un oggetto {@link User}.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoDataExtractedException Se non viene trovato nessun risultato che corrisponde
	 * al filtro specificato.
	 * @throws EcmEngineException Se si verifica un errore durante la ricerca dei metadati
	 * dell'utente (e la causa dell'errore non &egrave; l'assenza dell'utente).
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	User retrieveUserMetadata(User filter, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineException,
	EcmEngineTransactionException, PermissionDeniedException, RemoteException;

	/**
	 * Crea un nuovo gruppo sul repository.
	 *
	 * <p>Questo metodo esegue la creazione di un nuovo gruppo applicativo sul repository dell'ECMENGINE
	 * utilizzando il nome specificato in input. Poich&eacute; non possono esistere
	 * sul repository pi&ugrave; gruppi con lo stesso nome questo metodo potrebbe sollevare
	 * un'eccezione di tipo
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.GroupAlreadyExistsException}.
	 * </p>
	 *
	 * <p>Il repository destinazione per la creazione del gruppo viene letto dal DTO
	 * {@link Group}. Se il valore non &egrave; specificato la creazione &egrave; effettuata
	 * sul repository configurato come default.</p>
	 *
	 * @param nuovoGruppo L'oggetto {@link Group} corrispondente al nuovo gruppo da creare.
	 * @param gruppoPadre L'oggetto {@link Group} corrispondente al gruppo padre, oppure
	 * {@code null} se il nuovo gruppo deve essere un <i>gruppo root</i>.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di autenticazione
	 * dell'amministratore che esegue la creazione del nuovo gruppo.
	 *
	 * @return Il nome del nuovo gruppo utente creato.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws GroupCreateException Se si verifica un errore durante la creazione del gruppo (e la
	 * causa dell'errore non &egrave; la presenza di un gruppo con lo stesso nome).
	 * @throws GroupAlreadyExistsException Se il gruppo di cui &egrave; stata richiesta la creazione
	 * esiste gi&agrave;.
	 * @throws NoSuchGroupException Se il gruppo padre specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	String createGroup(Group nuovoGruppo, Group gruppoPadre, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupCreateException, GroupAlreadyExistsException,
	EcmEngineTransactionException, NoSuchGroupException, PermissionDeniedException, RemoteException;

	/**
	 * Aggiunge l'utente identificato dallo username univoco fornito in input al gruppo
	 * specificato.
	 *
	 * <p>Questo metodo associa un utente del repository ECMENGINE ad un gruppo specificato. Sia l'utente
	 * che il gruppo devono esistere gi&agrave; sul repository. Se uno dei due non &egrave; presente
	 * questo metodo pu&ograve; sollevare le eccezioni
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchUserException} oppure
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException}, a seconda dei casi.
	 * </p>
	 *
	 * @param utente L'oggetto {@link User} corrispondente all'utente.
	 * @param gruppo L'oggetto {@link Group} corrispondente al gruppo a cui aggiungere l'utente.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di
	 * autenticazione dell'amministratore che esegue l'aggiunta al gruppo.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws GroupEditException Se si verifica un errore durante l'associazione dell'utente al gruppo
	 * (e la causa dell'errore non &egrave; l'assenza di uno dei due).
	 * @throws NoSuchUserException Se l'utente specificato non esiste.
	 * @throws NoSuchGroupException Se il gruppo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void addUserToGroup(User utente, Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupEditException, NoSuchUserException, EcmEngineTransactionException,
	NoSuchGroupException, PermissionDeniedException, RemoteException;

	/**
	 * Rimuove l'utente identificato dallo username univoco fornito in input dal gruppo
	 * specificato.
	 *
	 * <p>Questo metodo rimuove un utente del repository ECMENGINE da un gruppo specificato. Sia l'utente
	 * che il gruppo devono esistere gi&agrave; sul repository. Se uno dei due non &egrave; presente
	 * questo metodo pu&ograve; sollevare le eccezioni
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchUserException} oppure
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException}, a seconda dei casi.
	 * </p>
	 *
	 * @param utente L'oggetto {@link User} corrispondente all'utente.
	 * @param gruppo L'oggetto {@link Group} corrispondente al gruppo da cui rimuovere l'utente.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di
	 * autenticazione dell'amministratore che esegue la rimozione dal gruppo.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws GroupEditException Se si verifica un errore durante la rimozione dell'utente dal gruppo
	 * (e la causa dell'errore non &egrave; l'assenza di uno dei due).
	 * @throws NoSuchUserException Se l'utente specificato non esiste.
	 * @throws NoSuchGroupException Se il gruppo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void removeUserFromGroup(User utente, Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupEditException, NoSuchUserException, EcmEngineTransactionException,
	NoSuchGroupException, PermissionDeniedException, RemoteException;

	/**
	 * Restituisce i dati degli utenti appartenenti al gruppo specificato.
	 *
	 * <p>Questo metodo restituisce una lista degli utenti che appartengono al gruppo specificato
	 * sul repository dell'ECMENGINE. Se il gruppo non esiste questo metodo pu&ograve; sollevare un'eccezione
	 * di tipo
	 * {@link it.doqui.index.ecmengine.exception.publishing.backoffice.NoSuchGroupException}.</p>
	 *
	 * <p><strong>NB:</strong> gli oggetti restituiti non contengono le password.</p>
	 *
	 * @param gruppo L'oggetto {@link Group} corrispondente al gruppo di cui si chiede
	 * la lista degli utenti.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di
	 * autenticazione dell'amministratore che esegue la lettura.
	 *
	 * @return Un array di oggetti {@link User} contenente i dati degli utenti trovati nel gruppo
	 * specificato.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws EcmEngineException Se si verifica un errore durante la lettura della lista degli utenti (e
	 * la causa dell'errore non &egrave; l'assenza del gruppo).
	 * @throws NoSuchGroupException Se il gruppo specificato non esiste.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	User [] listUsers(Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineException, EcmEngineTransactionException,
	RemoteException;

	/**
	 * Permette di modificare la password dell'utente specificato.
	 * Il DTO utente{@link User} passato in input deve contenere la nuova password.
	 *
	 * @param utente L'oggetto {@link User} corrispondente all'utente.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di
	 * autenticazione dell'amministratore che esegue la modifica della password.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchUserException Se l'utente specificato non esiste.
	 * @throws UserUpdateException Se si verifica un errore durante la modifica della password dell'utente (e
	 * la causa dell'errore non &egrave; l'assenza dell'utente).
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void updateUserPassword(User utente, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserUpdateException, NoSuchUserException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Elimina l'utente specificato.
	 *
	 * <p>Il repository destinazione per la creazione dell'utente viene letto dal DTO
	 * {@link User}. Se il valore non &egrave; specificato la creazione &egrave; effettuata
	 * sul repository configurato come default.</p>
	 *
	 * @param utente L'oggetto {@link User} corrispondente all'utente da eliminare.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di
	 * autenticazione dell'amministratore che esegue l'eliminazione dell'utente.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchUserException Se l'utente specificato non esiste.
	 * @throws UserDeleteException Se si verifica un errore durante l'eliminazione dell'utente (e
	 * la causa dell'errore non &egrave; l'assenza dell'utente).
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void deleteUser(User utente,OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, UserDeleteException, NoSuchUserException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Elimina il gruppo specificato.
	 *
	 * @param gruppo L'oggetto {@link Group} corrispondente al gruppo che si vuole eliminare.
	 * @param context L'oggetto {@link OperationContext} contenente i dati di
	 * autenticazione dell'amministratore che esegue l'eliminazione del gruppo.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchGroupException Se il gruppo specificato non esiste.
	 * @throws GroupDeleteException Se si verifica un errore durante la modifica del gruppo (e
	 * la causa dell'errore non &egrave; l'assenza del gruppo).
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void deleteGroup(Group gruppo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, GroupDeleteException, NoSuchGroupException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Aggiunge i record di ACL specificati al nodo passato in input.
	 *
	 * <p>Questo metodo associa dei record di ACL ad un nodo specificato. Il nodo deve esistere
	 * sul repository e nel caso in cui non esista questo metodo pu&ograve; sollevare
	 * l'eccezione
	 * {@link it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException}.
	 * </p>
	 *
	 * <p><strong>NB:</strong> solo gli amministratori sono abilitati alla gestione delle ACL.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo a cui
	 * associare i record di ACL.
	 * @param acls Un array di {@link AclRecord} che contiene le definizioni dei record di ACL.
	 * @param context L'oggetto {@link OperationContext} contenente le informazioni per
	 * l'autenticazione e l'esecuzione della chiamata al servizio.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore durante l'associazione delle ACL al nodo
	 * (e la causa non &egrave; l'assenza del nodo).
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void addAcl(Node node, AclRecord [] acls, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Rimuove i record di ACL specificati dal nodo passato in input.
	 *
	 * <p>Questo metodo rimuove dei record di ACL da un nodo specificato. Il nodo deve esistere
	 * sul repository e nel caso in cui non esista questo metodo pu&ograve; sollevare
	 * l'eccezione
	 * {@link it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException}.
	 * </p>
	 *
	 * <p><strong>NB:</strong> solo gli amministratori sono abilitati alla gestione delle ACL.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo da cui
	 * rimuovere i record di ACL.
	 * @param acls Un array di {@link AclRecord} che contiene le definizioni dei record di ACL.
	 * @param context L'oggetto {@link OperationContext} contenente le informazioni per
	 * l'autenticazione e l'esecuzione della chiamata al servizio.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore durante la rimozione delle ACL al nodo
	 * (e la causa non &egrave; l'assenza del nodo).
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void removeAcl(Node node, AclRecord [] acls, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Sostituisce i record di ACL specificati per il nodo passato in input.
	 *
	 * <p>Questo metodo associa dei record di ACL ad un nodo specificato, eliminando
	 * i record esistenti. Il nodo deve esistere sul repository e nel caso in cui non
	 * esista questo metodo pu&ograve; sollevare l'eccezione
	 * {@link it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException}.
	 * </p>
	 *
	 * <p><strong>NB:</strong> solo gli amministratori sono abilitati alla gestione delle ACL.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo a cui
	 * associare i record di ACL.
	 * @param acls Un array di {@link AclRecord} che contiene le definizioni dei record di ACL.
	 * @param context L'oggetto {@link OperationContext} contenente le informazioni per
	 * l'autenticazione e l'esecuzione della chiamata al servizio.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore durante l'associazione delle ACL al nodo
	 * (e la causa non &egrave; l'assenza del nodo).
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void updateAcl(Node node, AclRecord [] acls, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Restituisce la lista dei record di Access Control List associati al nodo specificato.
	 *
	 * <p>Questo metodo legge la ACL del nodo specificato e la restituisce sotto forma di
	 * array di oggetti {@link AclRecord}. Se il nodo non viene trovato il metodo solleva
	 * un'eccezione di tipo
	 * {@link it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException}.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo di cui
	 * si vuole leggere l'ACL.
	 * @param params Un'istanza del DTO {@link AclListParams} che specifica il comportamento
	 * del servizio.
	 * @param context L'oggetto {@link OperationContext} corrispondente all'amministratore
	 * che esegue la lettura della ACL.
	 *
	 * @return L'ACL del nodo specificato come array di {@link AclRecord}.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore in accesso ai dati sulle ACL.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	AclRecord [] listAcl(Node node, AclListParams params, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Imposta l'ereditariet&agrave; delle ACL sul nodo specificato.
	 *
	 * <p>Questo imposta il valore del flag di ereditariet&agrave; delle ACL sul nodo specificato.
	 * Se il nodo non viene trovato il metodo solleva un'eccezione di tipo
	 * {@link it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException}.</p>
	 *
	 * <p><strong>NB:</strong> solo gli amministratori sono abilitati alla gestione delle ACL.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo di cui
	 * si vuole impostare il flag.
	 * @param inherits Il valore ({@code true} o {@code false}) da impostare.
	 * @param context L'oggetto {@link OperationContext} corrispondente all'amministratore
	 * che esegue la lettura della ACL.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore durante l'impostazione del flag.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void setInheritsAcl(Node node, boolean inherits, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Restituisce l'ereditariet&agrave; delle ACL sul nodo specificato.
	 *
	 * <p>Questo restituisce il valore del flag di ereditariet&agrave; delle ACL sul nodo specificato.
	 * Se il nodo non viene trovato il metodo solleva un'eccezione di tipo
	 * {@link it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException}.</p>
	 *
	 * <p><strong>NB:</strong> solo gli amministratori sono abilitati alla gestione delle ACL.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo di cui
	 * si vuole leggere il flag.
	 * @param context L'oggetto {@link OperationContext} corrispondente all'amministratore
	 * che esegue la lettura della ACL.
	 *
	 * @return Il valore ({@code true} o {@code false}) del flag di ereditariet&agrave;
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore in accesso ai dati sulle ACL.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	boolean isInheritsAcl(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException, EcmEngineTransactionException,
	RemoteException;


	/**
	 * Permette di eliminare le ACL definite su un nodo per l'authority
	 * specificata in input come filtro.
	 *
	 * <p>Il DTO filter{@link AclRecord} passato in input deve contenere nell'attributo
	 * {@code authority} l'utente o il gruppo di cui si vogliono eliminare le ACL. Se invece
	 * il valore di tale DTO filter e' {@code null} allora le ACL vengono eliminate per tutte
	 * le authority.</p>
	 *
	 * <p><strong>NB:</strong> solo gli amministratori sono abilitati alla gestione delle ACL.</p>
	 *
	 * @param node Un'istanza del DTO {@link Node} che contiene l'ID univoco del nodo di cui
	 * si vuole modificare le ACL.
	 * @param filter Un'istanza del DTO {@link AclRecord} che contiene l'authority utilizzata come filtro
	 * per la cancellazione delle ACL.
	 * @param context L'oggetto {@link OperationContext} corrispondente all'amministratore
	 * che esegue la modifica delle ACL.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AclEditException Se si verifica un errore in accesso ai dati sulle ACL.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	void resetAcl(Node node, AclRecord filter ,OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, AclEditException, NoSuchNodeException,
	EcmEngineTransactionException, PermissionDeniedException, RemoteException;


	/**
	 * Verifica l'integrita' del repository a partire dal nodo specificato. Il controllo
	 * verifica che il contenuto degli indici sia conforme al contenuto del db.
	 * Nel caso in cui il repository non sia integro, il metodo restituisce
	 * un array di {@link IntegrityReport} contenente tutti gli errori riscontrati e i nodi coinvolti.
	 *
	 * @param node Un'istanza del DTO {@link Node} che indica il nodo da cui partire per svolgere il
	 * controllo di integrit&agrave;.
	 * @param context L'oggetto {@link OperationContext} corrispondente all'amministratore
	 * che esegue la verifica del repository.
	 *
	 * @return Un array di risultati sotto forma di oggetti {@link IntegrityReport}.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws TooManyNodesException Se la ricerca dei nodi da verificare restituisce troppi risultati.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore durante la comunicazione.
	 */
	IntegrityReport [] checkRepositoryIntegrity(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, TooManyNodesException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Importa un archivio con i contenuti da creare sul sistema.
	 *
	 * @param data Il {@link DataArchive} con i contenuti da importare.
	 * @param parent Un'istanza del DTO {@link Node} che contiene l'informazione sul nodo
	 * sotto il quale importare i contenuti.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'importazione
	 * dell'archivio.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
    void importDataArchive(DataArchive data, Node parent, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException,
    EcmEngineTransactionException, EcmEngineException,
    PermissionDeniedException, RemoteException;

	/**
	 * Metodo che restituisce le system property della piattaforma.
	 *
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un array di oggetti {@link SystemProperty}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	SystemProperty [] getSystemProperties(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, RemoteException;

	/**
	 * Restituisce i metadati del content model specificato in input.
	 *
	 * @param modelDescriptor L'oggetto {@link ModelDescriptor} corrispondente al modello richiesto.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un oggetto {@code ModelMetadata} contenente i metadati del content model.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws NoDataExtractedException Se non esiste alcun modello corrispondente al descrittore specificato.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	ModelMetadata getModelDefinition(ModelDescriptor modelDescriptor, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoDataExtractedException, EcmEngineTransactionException,
	EcmEngineException, RemoteException;

	/**
	 * Restituisce i metadati del tipo specificato in input.
	 *
	 * @param typeDescriptor L'oggetto {@link ModelDescriptor} corrispondente al tipo richiesto.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un oggetto {@code TypeMetadata} contenente i metadati del tipo.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws NoDataExtractedException Se non esiste alcun tipo corrispondente al descrittore specificato.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	TypeMetadata getTypeDefinition(ModelDescriptor typeDescriptor, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, EcmEngineException, EcmEngineTransactionException,
	InvalidCredentialsException, RemoteException;

	/**
	 * Restituisce i nomi di tutti i content model definiti completi di prefisso.
	 *
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un array di {@link ModelDescriptor} contenente i descrittori dei modelli definiti nel content model
	 *
	 * @throws InvalidParameterException e uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	ModelDescriptor [] getAllModelDescriptors(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineTransactionException, EcmEngineException, RemoteException;

	/**
	 * Restituisce la lista dei repository configurati sull'istanza di ECMENGINE.
	 *
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un array di oggetti {@link Repository}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Repository [] getRepositories(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, EcmEngineException, RemoteException;

	/**
	 * Restituisce tutti i gruppi contenuti nel gruppo specificato.
	 *
	 * @param parentGroup L'oggetto {@link Group} che rappresenta il gruppo padre.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un array di oggetti {@link Group}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchGroupException Se il gruppo specificato non esiste.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Group [] listGroups(Group parentGroup, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchGroupException, EcmEngineTransactionException,
	EcmEngineException, RemoteException;

	/**
	 * Restituisce tutti i gruppi che corrispondo al filtro specificato.
	 *
	 * @param filter L'oggetto {@link Group} contenente il filtro di ricerca (specificato nell'attributo {@code name}).
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un array di oggetti {@link Group}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws NoDataExtractedException Se non viene trovato nessun risultato che corrisponde
	 * al filtro specificato.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Group [] listAllGroups(Group filter, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, EcmEngineException,
	NoDataExtractedException, RemoteException;

	/**
	 * Crea un tenant (repository logico) con i dati specificati.
	 *
	 * @param tenant Un oggetto {@code Tenant} contenente i metadati del tenant.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void createTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Abilita l'accesso ad un tenant precedentemente disabilitato.
	 *
	 * <p><strong>NB:</strong>Il parametro enabled del DTO Tenant non viene considerato</p>
	 *
	 * @param tenant Un oggetto {@code Tenant} contenente i metadati del tenant.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void enableTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Disabilita l'accesso ad un tenant.
	 *
	 * <p><strong>NB:</strong>Il parametro enabled del DTO Tenant non viene considerato</p>
	 *
	 * @param tenant Un oggetto {@code Tenant} contenente i metadati del tenant.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
     *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void disableTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
     *
	 * @return Un array di risultati sotto forma di oggetti {@link Tenant}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoDataExtractedException Se non viene trovato nessun risultato che corrisponde
	 * al filtro specificato.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Tenant[] getAllTenants(OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Restituisce i metadati realtivi al tenant specificato.
	 *
	 * @param tenant Un oggetto {@code Tenant} contenente il nome del tenant.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
     *
	 * @return Un oggetto di tipo {@link Tenant}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoDataExtractedException Se non viene trovato nessun risultato che corrisponde
	 * al filtro specificato.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Tenant getTenant(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Verifica l'esistenza del tenant specificato.
	 *
	 * @param tenant Un oggetto {@code Tenant} contenente i metadati del tenant.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @return {@code true} se il tenant esiste; {@code false} altrimenti.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	boolean tenantExists(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Cancella un tenant specificato.
	 *
	 * @param tenant Un oggetto {@code Tenant} contenente i metadati del tenant.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void tenantDelete(Tenant tenant, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Restituisce l'elenco dei custom model esistenti.
	 *
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
     *
	 * @return Un array di risultati sotto forma di oggetti {@link CustomModel}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoDataExtractedException Se non viene trovato nessun risultato che corrisponde
	 * al filtro specificato.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	CustomModel[] getAllCustomModels(OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, NoDataExtractedException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Predispone l'utilizzo del custom model specificato sul repository.
	 *
	 * @param model Un oggetto {@code CustomModel} contenente i metadati del custom model.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void deployCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Elimina il custom model specificato dal repository rendendolo non pi&ugrave;
	 * utilizzabile.
	 *
	 * @param model Un oggetto {@code CustomModel} contenente i metadati del custom model.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void undeployCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Abilita l'utilizzo di un custom model precedentemente disabilitato.
	 *
	 * @param model Un oggetto {@code CustomModel} contenente i metadati del custom model.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void activateCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Disabilita l'utilizzo del custom model specificato.
	 *
	 * @param model Un oggetto {@code CustomModel} contenente i metadati del custom model.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineException Se si verifica un errore inatteso durante l'esecuzione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void deactivateCustomModel(CustomModel model, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	/**
	 * Metodo di test per CSI Framework.
	 *
	 * @return {@code true} se le risorse sono disponibili, {@code false} altrimenti.
	 *
	 * @throws EcmEngineException Se si verifica un'eccezione durante il controllo.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */

	ExportedContent exportTenant(Tenant t, OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;

	void importTenant(ExportedContent content,Tenant dest,OperationContext context)
	throws InvalidParameterException, EcmEngineTransactionException, InvalidCredentialsException, PermissionDeniedException, EcmEngineException, RemoteException;
	
	
	public boolean testResources()
    throws EcmEngineException, RemoteException;
}

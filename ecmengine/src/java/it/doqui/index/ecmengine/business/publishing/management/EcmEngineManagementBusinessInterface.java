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

package it.doqui.index.ecmengine.business.publishing.management;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.dto.engine.management.Association;
import it.doqui.index.ecmengine.dto.engine.management.Category;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatVersion;
import it.doqui.index.ecmengine.dto.engine.management.FileInfo;
import it.doqui.index.ecmengine.dto.engine.management.RenditionDocument;
import it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer;
import it.doqui.index.ecmengine.dto.engine.management.Rule;
import it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow;
import it.doqui.index.ecmengine.dto.engine.management.Version;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CheckInCheckOutException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CopyException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.MoveException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.RenditionException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.TransformException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UnsupportedTransformationException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.WorkflowException;

import java.rmi.RemoteException;

/**
 * <p>Interfaccia di business che raccoglie i servizi pubblici
 * orchestrati che vengono esportati dall'ECMENGINE per la gestione
 * dei contenuti del repository e dell'audit trail.</p>
 *
 * @author DoQui
 */
public interface EcmEngineManagementBusinessInterface {

	/*===============================================================*
	 *                                                               *
	 *        NUOVI SERVIZI GENERICI RISPETTO AL CONTENT MODEL       *
	 *                                                               *
	 *===============================================================*/

	/**
	 * Esegue il checkout di un contenuto generando una nuova working copy.
	 *
	 * @param node L'oggetto {@code Node} che identifica il contenuto su cui eseguire il checkout.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'oggetto {@code Node} che identifica la nuova working copy.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws CheckInCheckOutException
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Node checkOutContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException, RemoteException;

	/**
	 * Esegue il checkin di una working copy precedentemente ottenuta mediante un checkout.
	 *
	 * @param node L'oggetto {@code Node} che identifica la working copy su cui eseguire il checkin.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'oggetto {@code Node} che identifica il contenuto originale.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws CheckInCheckOutException Se si verifica un problema durante l'operazione di checkin.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Node checkInContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException, RemoteException;

	/**
	 * Sposta un nodo e i suoi figli in una destination differente. Lo spostamento viene realizzato in
	 * maniera asincrona tramite un processo batch che viene eseguito in automatico ogni giorno alle 22.
	 *
	 * <p>Questo metodo consente di spostare un node del repository insieme ai nodi figlio in una
	 * destination differente. La posizione di destinazione pu&ograve; essere un workspace diverso
	 * oppure lo stesso. In realt&agrave; non si tratta di un vero spostamento , ma bens&igrave; di una copia;
	 * il nodo source viene poi marcato come spostato (tramite l'aggiunta di un aspect).</p>
	 *
	 * @param source Il {@link Node} che punta al nodo da spostare.
	 * @param destinationParent Il {@link Node} che punta al nodo padre a cui agganciare il nodo source.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws MoveException Se si verifica un errore durante lo spostamento.
	 * @throws NoSuchNodeException Se uno dei nodi di input non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante lo spostamento.
	 */
	void moveAggregation(Node source, Node destinationParent,OperationContext context)
	throws InvalidParameterException, MoveException, NoSuchNodeException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException, RemoteException;


	/**
	 * Inserisce un contenuto generico come figlio di uno specifico nodo.
	 *
	 * <p>Questo metodo inserisce un contenuto generico nel repository dell'ECMENGINE
	 * come figlio del nodo specificato. Tale nodo deve essere gi&agrave; presente sul
	 * repository dell'ECMENGINE.</p>
	 *
	 * @param parent Il {@link Node} che punta al padre del contenuto da inserire.
	 * @param content Il {@link Content} che descrive il nuovo contenuto da inserire.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return Il {@link Node} che punta al nuovo contenuto creato.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	Node createContent(Node parent, Content content, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Elimina un contenuto generico.
	 *
	 * <p>Questo metodo elimina un contenuto generico dal repository dell'ECMENGINE.</p>
	 *
	 * @param node Il {@link Node} che punta al contenuto da eliminare.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws DeleteException Se si verifica un errore durante la cancellazione.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	void deleteContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Recupera i dati binari associati ad un contenuto dell'ECMENGINE.
	 *
	 * <p>Questo metodo restituisce, sotto forma di array di {@code byte}, il contenuto
	 * associato ad un nodo nel repository dell'ECMENGINE. Il nodo specificato in input
	 * deve essere presente, mentre l'oggetto {@link Content} deve definire necessariamente
	 * il nome della property che rappresenta il contenuto binario da restituire (esempio: per il
	 * tipo &quot;{@code cm:content}&quot; il nome della property &egrave;
	 * &quot;{@code cm:content}&quot;).</p>
	 *
	 * <p><strong>NB:</strong> questo metodo effettua una verifica ulteriore di validit&agrave;
	 * dei parametri controllando che il nome del contenuto specificato e il nome della <i>primary
	 * parent association</i> del nodo specificato coincidano.</p>
	 *
	 * @param node Il {@link Node} che punta al nodo di cui si richiede il contenuto.
	 * @param content Il {@link Content} che descrive il contenuto di cui si richiedono i dati binari.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return Un array di {@code byte} contenente i dati binari richiesti oppure {@code null} se non
	 * ci sono dati associati al contenuto.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws ReadException Se si verifica un errore durante la lettura del contenuto.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la lettura.
	 */
	byte [] retrieveContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Crea una relazione tra due nodi gi&agrave; presenti sul repository dell'ECMENGINE.
	 *
	 * <p>Questo metodo mette in relazione due contenuti generici gi&agrave; presenti
	 * nel repository dell'ECMENGINE. La relazione pu&ograve; essere un semplice legame tra due
	 * contenuti di livello paritario oppure una associazione di tipo padre-figlio.</p>
	 *
	 * @param source Il {@link Node} che punta al primo dei contenuti generici da collegare.
	 * @param destination Il {@link Node} che punta al secondo contenuto generico da collegare.
	 * @param association L'{@link Association} che descrive la relazione tra i due nodi.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante la creazione del legame.
	 * @throws NoSuchNodeException Se uno dei nodi specificati in input non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la creazione del legame.
	 */
	void linkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Elimina una relazione tra due nodi presenti sul repository dell'ECMENGINE.
	 *
	 * <p>Questo metodo elimina una relazione tra due contenuti generici gi&agrave; presenti
	 * nel repository dell'ECMENGINE. La relazione pu&ograve; essere un semplice legame tra due
	 * contenuti di livello paritario oppure una associazione di tipo padre-figlio.</p>
	 *
	 * @param source Il {@link Node} che punta al primo dei contenuti generici da scollegare.
	 * @param destination Il {@link Node} che punta al secondo contenuto generico da scollegare.
	 * @param association L'{@link Association} che descrive la relazione tra i due nodi da eliminare.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante la cancellazione del legame.
	 * @throws NoSuchNodeException Se uno dei nodi specificati in input non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'eliminazione del legame.
	 */
	void unLinkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Aggiorna i metadati associati ad un contenuto sul repository dell'ECMENGINE.
	 *
	 * <p>Questo metodo aggiorna le propriet&agrave; associate ad un contenuto presente
	 * nel repository dell'ECMENGINE. Il DTO contenente i parametri da aggiornare &agrave; lo stesso
	 * utilizzato per la creazione del contenuto, tuttavia di esso <strong>non saranno presi in
	 * considerazione i dati relativi al nome del contenuto e alla sua associazione con il
	 * proprio padre primario</strong>.</p>
	 *
	 * <p>Il DTO contenente i nuovi metadati deve specificare i nuovi valori in questo modo:</p>
	 * <ul>
	 *   <li>Le property da rimuovere devono avere valore {@code null}.</li>
	 *   <li>Le property da aggiornare devono contenere i nuovi valori che saranno sovrascritti.</li>
	 *   <li>Le property da lasciare inalterate <strong>non</strong> devono essere specificate.</li>
	 *   <li>Gli aspect da aggiungere devono specificare le eventuali property da impostare.</li>
	 *   <li>Gli aspect da lasciare inalterati devono essere specificati senza nessuna property.</li>
	 *   <li>Gli aspect da rimuovere <strong>non</strong> devono essere specificati.</li>
	 * </ul>
	 *
	 * @param node Il {@link Node} che punta al contenuto da modificare.
	 * @param newContent Il {@link Content} che definisce i nuovi dati da modificare.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws UpdateException Se si verifica un errore durante l'aggiornamento dei dati.
	 * @throws NoSuchNodeException Se il nodo specificato in input non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la modifica.
	 */
	void updateMetadata(Node node, Content newContent, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;


	/*===============================================================*
	 *        SERVIZI DI AUDIT TRAIL							     *
	 *===============================================================*/

	/**
	 * <p>Inserisce un record nella tabella dell'audit trail.</p>
	 *
	 * @param auditTrail Il dto {@link AuditInfo} contenente le info(utente,operazione,idOggetto,metaDati)
	 * di audit trail da tracciare sulla base dati dell'ECMENGINE.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AuditTrailException Se si verifica un errore nell'inserimento dell'audit trail.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	void logTrail(AuditInfo auditTrail, OperationContext context) throws InvalidParameterException,
	AuditTrailException, InvalidCredentialsException, EcmEngineTransactionException, RemoteException;

	/**
	 * <p>Esegue una ricerca nella tabella di audit trail.</p>
	 *
	 * @param parametriRicerca DTO contenente i parametri di ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return un array di {@link AuditInfo}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws AuditTrailException Se si verifica un errore in fase di ricerca nella tabella
	 * dell'audit trail.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione di ricerca.
	 */
	AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca, OperationContext context)
	throws InvalidParameterException, AuditTrailException, PermissionDeniedException, InvalidCredentialsException, EcmEngineTransactionException, RemoteException;

	// Version management
	/**
	 * Aggiorna il contenuto fisico associato ad un nodo del repository.
	 *
	 * @param node Il {@link Node} che identifica il contenuto da modificare.
	 * @param content L'oggetto {@link Content} contenente i nuovi dati binari da associare al nodo.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws UpdateException Se si verifica un errore durante l'aggiornamento del contenuto.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione
	 */
	void updateContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException, RemoteException, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException;

	/**
	 * Restituisce l'elenco ordinato di tutte le versioni disponibili per un contenuto.
	 *
	 * @param node Il {@link Node} di cui reperire le versioni.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Un array di {@code Version} contenenti le informazioni relative alle diverse versioni.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws EcmEngineException Se si verifica un problema durante la lettura della version history.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione
	 */
	Version[] getAllVersions(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, EcmEngineException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Restituisce la versione del contenuto identificata dalla label specificata in input.
	 *
	 * @param node L'oggetto {@link Node} di cui ricercare la versione.
	 * @param versionLabel L'etichetta che identifica la versione desiderata.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'oggetto {@link Version} contenente le informazioni di versione.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws EcmEngineException Se si verifica un problema durante la ricerca della versione.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione
	 */
	Version getVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, EcmEngineTransactionException,
	PermissionDeniedException, RemoteException;

	/**
	 * Restituisce il contenuto fisico della versione specificata.
	 *
	 * @param node L'oggetto {@link Node} che identifica la versione desiderata.
	 * @param content Il {@link Content} che descrive il contenuto di cui si richiedono i dati binari.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return Il contenuto binario sotto forma di array di {@code byte}.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws ReadException Se si verifica un errore durante la lettura del contenuto.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	byte [] retrieveVersionContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, ReadException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * Esegue il revert su un nodo (non in checkout) riportandolo alla versione specificata.
	 *
	 * @param node L'oggetto {@link Node} su cui eseguire il revert.
	 * @param versionLabel L'etichetta di versione a cui riportare il nodo.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void revertVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * Annulla un'operazione di checkout precedentemente eseguita rimuovendo la working copy specificata.
	 *
	 * @param node L'oggetto {@link Node} che identifica la working copy da rimuovere.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'oggetto {@link Node} che identifica il contenuto originale a cui era associata la working copy.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws CheckInCheckOutException Se si verifica un problema durante l'annullamento del checkout.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Node cancelCheckOutContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, CheckInCheckOutException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * Metodo che restituisce la working copy di un nodo su cui &egrave; stata effettuata un'operaione di checkout.
	 *
	 * @param node L'oggetto {@link Node} di cui si ricerca la working copy.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'oggetto {@link Node} corrispondente alla working copy.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws CheckInCheckOutException Se si verifica un problema durante la ricerca della working copy.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	Node getWorkingCopy(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * Reperisce i metadati di un contenuto tramite il suo identificatore.
	 *
	 * @param node L'oggetto {@link Node} che identifica il contenuto.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'istanza di {@code Content} che rappresenta il contenuto richiesto.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws ReadException Se si verifica un problema durante la lettura dei metadati.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	ResultContent getContentMetadata(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, ReadException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * Reperisce i metadati di una specifica versione di un contenuto tramite il suo identificatore.
	 *
	 * @param node L'oggetto {@link Node} che identifica la versione del contenuto.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @return L'istanza di {@code Content} che rappresenta la versione richiesta.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws ReadException Se si verifica un problema durante la lettura dei metadati.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	ResultContent getVersionMetadata(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, PermissionDeniedException,
	EcmEngineTransactionException, ReadException, InvalidCredentialsException, RemoteException;

	/**
	 * Verifica la cifratura di un generico contenuto identificato dal nodo
	 * specificato.
	 *
	 * @param node Il {@link Node} che punta al contenuto di cui deve essere verificata la cifratura.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return Informazioni relative allo stato di crittazione del contenuto.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws EcmEngineException Se l'operazione di verifica cifratura non va a buon fine.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	EncryptionInfo checkEncryption(Node node, OperationContext context)
	throws InvalidParameterException, EcmEngineException, NoSuchNodeException, InvalidCredentialsException,
	PermissionDeniedException, RemoteException, EcmEngineTransactionException;

	/**
	 * <p>Trasforma un contenuto da un formato ad un altro</p>
	 *
	 * @param node
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	byte [] transformContent(Node node, String targetMimeType, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, UnsupportedTransformationException,
	TransformException, RemoteException, EcmEngineTransactionException, PermissionDeniedException;

	/**
	 * <p>Inserisce un contenuto in un workflow semplice.</p>
	 *
	 * @param node
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws WorkflowException Se il nodo specificato fa gi&agrave; parte di un workflow semplice.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void startSimpleWorkflow(Node node, SimpleWorkflow workflow, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * <p>Applica un workflow semplice ad un nodo secondo una regola.</p>
	 *
	 * @param node
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws WorkflowException .
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void addSimpleWorkflowRule(Node node, SimpleWorkflow workflow, Rule rule, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * <p>Approva un contenuto che fa parte di un workflow semplice.</p>
	 *
	 * @param node
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws WorkflowException Se il nodo specificato non fa parte di un workflow semplice.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void approveContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * <p>Rifiuta un contenuto che fa parte di un workflow semplice.</p>
	 *
	 * @param node
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws WorkflowException Se il nodo specificato non fa parte di un workflow semplice.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	void rejectContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException;

	/**
	 * Elimina definitivamente il nodo archiviato dal repository di ECMENGINE.
	 *
	 * <p>Il nodo specificato viene cercato all'interno dell'archivio dei nodi eliminati,
	 * quindi deve essere gi&agrave; stato eliminato in precedenza mediante un'operazione
	 * di delete.</p>
	 *
	 * @see #deleteContent(Node, OperationContext)
	 *
	 * @param node Il {@link Node} corrispondente al contenuto da eliminare definitivamente.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste nell'archivio dei contenuti eliminati.
	 * @throws DeleteException Se si verifica un errore durante la cancellazione definitiva.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	void purgeContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException;

	/**
	 * Svuota l'archivio dei nodi eliminati sul repository di ECMENGINE.
	 *
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws DeleteException Se si verifica un errore durante la cancellazione definitiva.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	void purgeAllContents(OperationContext context)
	throws InvalidParameterException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException;

	/**
	 * Ripristina il nodo archiviato dal repository di ECMENGINE.
	 *
	 * <p>Il nodo specificato viene cercato all'interno dell'archivio dei nodi eliminati,
	 * quindi deve essere gi&agrave; stato eliminato in precedenza mediante un'operazione
	 * di delete.</p>
	 *
	 * @see #deleteContent(Node, OperationContext)
	 *
	 * @param node Il {@link Node} corrispondente al contenuto da ripristinare.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return Un oggetto {@link Node} contenente l'uid del contenuto ripristinato.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste nell'archivio dei contenuti eliminati.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 */
	Node restoreContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException, EcmEngineException;

	/**
	 * Ripristina tutti i nodi archiviati sul repository di ECMENGINE.
	 *
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return Un array di oggetti {@link Node} contenenti gli uid dei contenuti ripristinati.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 */
	Node[] restoreAllContents(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException, EcmEngineException;

	/**
	 * Reperisce le informazioni relative al formato di un file basandosi sull'estensione del file stesso.
	 * @param mimetype Il DTO contenente le informazioni sul nome del file.
	 * @return Un DTO contente il MIMEType riconosciuto.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	Mimetype[] getMimetype(Mimetype mimetype)
	throws InvalidParameterException, RemoteException;

	/**
	 * Reperisce le informazioni relative al formato del contenuto di un nodo utilizzando fileformat.
	 * @param node L'UID del nodo di cui bisgna riconoscere il tipo del contenuto.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 * @return Un array di riconoscimenti o di tentativi di riconoscimento.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws FileFormatExcpetion Se si verifica un errore durante il riconoscimento del file.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 */
	FileFormatInfo[] getFileFormatInfo(Node node, Content content, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, RemoteException, EcmEngineException;

	/**
	 * Reperisce le informazioni relative al formato del contenuto di un DTO FileInfo utilizzando fileformat.
	 * @param fileInfo Un DTO contentente il file da riconoscere.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 * @return Un array di riconoscimenti o di tentativi di riconoscimento.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws FileFormatExcpetion Se si verifica un errore durante il riconoscimento del file.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 */
	FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, RemoteException, EcmEngineException;

	/**
	 * Restituisce la versione del signature file utilizzato da FileFormat per
	 * il riconoscimento della firma.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 * @return Un DTO che contiente la versione del signature file.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws EcmEngineException Se si verifica un problema durante l'esecuzione dell'operazione.
	 */
	FileFormatVersion getFileFormatVersion(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, RemoteException, EcmEngineException;

	/**
	 * Crea una nuova categoria.
	 * @param categoryParent La categoria padre.
	 * @param category La nuova categoria.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 * @return Un DTO che rappresenta l'uid della nuova categoria creata.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	Node createCategory(Node categoryParent, Category category,OperationContext context) throws InvalidParameterException,
	InsertException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
	EcmEngineTransactionException;

	/**
	 * Crea la category di root.
	 * @param rootCategory La category di root da creare.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 * @return Un DTO che rappresenta l'uid della nuova categoria di root creata.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	Node createRootCategory(Category rootCategory,OperationContext context) throws InvalidParameterException,
	InsertException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
	EcmEngineTransactionException;

	/**
	 * Cancella una category.
	 * @param categoryNode Il DTO contenente l'UID della categoria da cancellare.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws DeleteException Se si verifica un errore durante la cancellazione.
	 * @throws InvalidCredentialsException
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante la cancellazione.
	 */
	void deleteCategory(Node categoryNode,OperationContext context) throws InvalidParameterException,
	NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException;

	/**
	 * Copia un nodo sotto un altro nodo.
	 * @param source il nodo da copiare
	 * @param parent il nodo sotto il quale copiare il nodo
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws CopyException Se si verifca un errore durante la copia.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	Node copyNode(Node source, Node parent, OperationContext context)
	throws InvalidParameterException, InsertException, CopyException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	/**
	 * Sposta un nodo sotto un altro nodo.
	 * @param source il nodo da spostare
	 * @param parent il nodo sotto il quale spostare il nodo
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	void moveNode(Node source, Node parent, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, RemoteException;

	// metodi di Rendition
	Node addRenditionTransformer(Node nodoXml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	Node setRendition(Node nodoTransformer, RenditionDocument renditionDocument, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	RenditionTransformer getRenditionTransformer(Node nodoTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	RenditionDocument getRendition(Node nodoTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	void deleteRenditionTransformer(Node xml, Node renditionTransformer, OperationContext context)throws InvalidParameterException, DeleteException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	RenditionTransformer[] getRenditionTransformers(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	RenditionDocument[] getRenditions(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException;

	RenditionDocument generateRendition(Content xml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException, RemoteException;
	// metodi di Rendition

	/**
	 * Metodo di test per CSI Framework.
	 *
	 * @return {@code true} se le risorse sono disponibili, {@code false} altrimenti.
	 *
	 * @throws EcmEngineException Se si verifica un'eccezione durante il controllo.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	public boolean testResources() throws EcmEngineException, RemoteException;
}

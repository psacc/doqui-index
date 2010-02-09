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

package it.doqui.index.ecmengine.business.publishing.search;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.Path;
import it.doqui.index.ecmengine.dto.engine.NodeArchiveParams;
import it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.dto.engine.management.Category;
import it.doqui.index.ecmengine.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.dto.engine.search.TopCategory;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.TooManyResultsException;

import java.rmi.RemoteException;


/**
 * <p>Interfaccia di business che raccoglie i servizi pubblici
 * orchestrati che vengono esportati dall'ECMENGINE per la ricerca
 * dei contenuti del repository e per le funzionalita di audit.</p>
 *
 * @author Doqui
 */
public interface EcmEngineSearchBusinessInterface {


	public NodeResponse selectNodes(Node node, SearchParamsAggregate parameterAggregate, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException;


	/**
	 * Ritorna le associazioni appartenenti al nodo indicato.
	 *
	 * @param node L'oggetto {@link Node} di cui ricercare le associazioni.
	 * @param assocType Il tipo di associazione che si vuole ricercare.
	 * @param maxResults Il numero max di risultati che si vogliono ottenere.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Un array di risultati.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws NoSuchNodeException Se il nodo passato come parametro non esiste.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	ResultAssociation[] getAssociations(Node node, String assocType, int maxResults, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, SearchException, InvalidCredentialsException, PermissionDeniedException,
			RemoteException, EcmEngineTransactionException, RemoteException;


	/**
	 *TODO
	 * @param category
	 * @param depth
	 * @param context
	 * @return
	 * @throws InvalidParameterException
	 * @throws NoSuchNodeException
	 * @throws SearchException
	 * @throws RemoteException
	 * @throws InvalidCredentialsException
	 * @throws PermissionDeniedException
	 * @throws EcmEngineTransactionException
	 */
	public ResultAssociation[] getCategories(Category category, String depth,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException;


	public ResultAssociation[] getCategoryChildren(Node categoryNode, String mode, String depth,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException;


	public ResultAspect[] getClassificationAspects(OperationContext context) throws InvalidParameterException,
	SearchException, RemoteException, InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException;


	public ResultAssociation[] getClassifications(OperationContext context) throws InvalidParameterException,
	NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException;

	public ResultAssociation[] getRootCategories(Category category,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException,
	InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException;


	public TopCategory[] getTopCategories(Category category,int count,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException,
	InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException;


	/**
	 * Verifica l'esistenza di un nodo sul repository mediante una ricerca per path.
	 * Se il nodo esiste ritorna l'uid associato, altrimenti
	 * solleva l'eccezione {@link NoDataExtractedException}.
	 *
	 * <p>La query XPath specificata in input viene trasformata con l'applicazione di <i>sequenze di escape</i> secondo lo standard ISO9075,
	 * percui &egrave; possibile utilizzare questo metodo per effettuare ricerche di nodi il cui local name (la parte
	 * di nome che segue i due punti) contiene caratteri speciali oppure inizia per numero.</p>
	 *
	 * <p><strong>NB:</strong> se l'utente non ha accesso al folder richiesto questo metodo non resituisce risultati
	 * e solleva quindi un'eccezione {@link NoDataExtractedException}.</p>
	 *
	 * @param xpath  L'oggetto {@link SearchParams} contenente la stringa XPath
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return L'uuid del nodo se questo &egrave; presente sul repository ,
	 * altrimenti l'eccezione {@link NoDataExtractedException}.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
	 * @throws NoDataExtractedException Se al path di ricerca immesso non corrisponde nessun dato.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	String nodeExists(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, NoDataExtractedException, InvalidCredentialsException, RemoteException,
	EcmEngineTransactionException;

	/**
	 * Esegue una ricerca per stringa Lucene sul repository.
	 *
	 * <p>La query Lucene specificata in input viene trasformata con l'applicazione di <i>sequenze di escape</i>. In particolare:</p>
	 * <ul>
	 *   <li>Ai termini &quot;{@code PATH:}&quot; vengono applicate le stesse regole di escape valide per la ricerca XPath.</li>
	 *   <li>Ai termini &quot;{@code QNAME:}&quot; viene effettuato l'escape secondo lo standard ISO9075 del solo <i>localName</i>,
	 *   cio&egrave; della parte che segue il carattere {@code ':'}.</li>
	 *   <li>In tutti gli altri termini non &egrave;, al momento, applicato nessun escape.</li>
	 * </ul>
	 *
	 * <p><strong>NB:</strong> questo metodo restituisce solo gli oggetti {@code Node} corrispondenti ai risultati.</p>
	 *
	 * @param lucene L'oggetto {@link SearchParams} contenente la stringa Lucene
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Una response contenente l'array di risultati.
	 *
	 * @see SearchParams
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws TooManyResultsException Se la ricerca restituisce pi&ugrave; risultati
	 * di un valore massimo prefissato.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	NodeResponse luceneSearchNoMetadata(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
			RemoteException, EcmEngineTransactionException;

	/**
	 * Esegue una ricerca per stringa Lucene sul repository.
	 *
	 * <p>La query Lucene specificata in input viene trasformata con l'applicazione di <i>sequenze di escape</i>. In particolare:</p>
	 * <ul>
	 *   <li>Ai termini &quot;{@code PATH:}&quot; vengono applicate le stesse regole di escape valide per la ricerca XPath.</li>
	 *   <li>Ai termini &quot;{@code QNAME:}&quot; viene effettuato l'escape secondo lo standard ISO9075 del solo <i>localName</i>,
	 *   cio&egrave; della parte che segue il carattere {@code ':'}.</li>
	 *   <li>In tutti gli altri termini non &egrave;, al momento, applicato nessun escape.</li>
	 * </ul>
	 *
	 * @param lucene L'oggetto {@link SearchParams} contenente la stringa Lucene
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Una response contenente l'array di risultati.
	 *
	 * @see SearchParams
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws TooManyResultsException Se la ricerca restituisce pi&ugrave; risultati
	 * di un valore massimo prefissato.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	SearchResponse luceneSearch(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
			RemoteException, EcmEngineTransactionException;

	/**
	 * Esegue una ricerca per path sul repository.
	 *
	 * <p>La query XPath specificata in input viene trasformata con l'applicazione di <i>sequenze di escape</i> secondo lo standard ISO9075,
	 * percui &egrave; possibile utilizzare questo metodo per effettuare ricerche di nodi il cui local name (la parte
	 * di nome che segue i due punti) contiene caratteri speciali oppure inizia per numero.</p>
	 *
	 * <p><strong>NB:</strong> questo metodo restituisce solo gli oggetti {@code Node} corrispondenti ai risultati.</p>
	 *
	 * @param xpath L'oggetto {@link SearchParams} contenente la stringa XPath
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Una response contenente l'array di risultati.
	 *
	 * @see SearchParams
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws TooManyResultsException Se la ricerca restituisce pi&ugrave; risultati
	 * di un valore massimo prefissato.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	NodeResponse xpathSearchNoMetadata(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
			RemoteException, EcmEngineTransactionException;

	/**
	 * Esegue una ricerca per path sul repository.
	 *
	 * <p>La query XPath specificata in input viene trasformata con l'applicazione di <i>sequenze di escape</i> secondo lo standard ISO9075,
	 * percui &egrave; possibile utilizzare questo metodo per effettuare ricerche di nodi il cui local name (la parte
	 * di nome che segue i due punti) contiene caratteri speciali oppure inizia per numero.</p>
	 *
	 * @param xpath L'oggetto {@link SearchParams} contenente la stringa XPath
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Una response contenente l'array di risultati.
	 *
	 * @see SearchParams
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws TooManyResultsException Se la ricerca restituisce pi&ugrave; risultati
	 * di un valore massimo prefissato.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	SearchResponse xpathSearch(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
			RemoteException, EcmEngineTransactionException;

	/**
	 * Esegue una ricerca generica su tutto il repository.
	 *
	 * <p><strong>NB:</strong> questo metodo restituisce solo gli oggetti {@code Node} corrispondenti ai risultati.</p>
	 *
	 * @param params L'oggetto che raccoglie tutti i parametri da usare
	 * nell'eseguire la ricerca sul repository.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Una response contenente l'array di risultati.
	 *
	 * @see SearchParams
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws TooManyResultsException Se la ricerca restituisce pi&ugrave; risultati
	 * di un valore massimo prefissato.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	NodeResponse genericGlobalSearchNoMetadata(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException;

	/**
	 * Esegue una ricerca generica su tutto il repository.
	 *
	 * @param params L'oggetto che raccoglie tutti i parametri da usare
	 * nell'eseguire la ricerca sul repository.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Una response contenente l'array di risultati.
	 *
	 * @see SearchParams
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws TooManyResultsException Se la ricerca restituisce pi&ugrave; risultati
	 * di un valore massimo prefissato.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	SearchResponse genericGlobalSearch(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException;


	/**
	 * Restituisce l'uid (contenuto nel DTO {@link Node}) del nodo identificato dalla
	 * query xpath specificata. La query xpath passata in input deve individuare univocamente
	 * il nodo di cui si vuole conoscere l'uid.
	 *
	 * @param xpath L'oggetto {@link SearchParams} contenente la stringa XPath
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Il {@link Node} che contiene l'uid e il repository del nodo ricercato.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws NoDataExtractedException Se la ricerca non restituisce risultati.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	Node getUid(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, SearchException, InvalidCredentialsException,
	PermissionDeniedException, RemoteException, EcmEngineTransactionException;

	/**
	 * Restituisce il numero totale di risultati della ricerca impostata.
	 *
	 * @param xpath L'oggetto {@link SearchParams} contenente la stringa XPath
	 * e i parametri da utilizzare per la ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Il numero totali di risultati della ricerca.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	int getTotalResults(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, InvalidCredentialsException, RemoteException, EcmEngineTransactionException;

	int getTotalResultsLucene(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, SearchException, InvalidCredentialsException, RemoteException, EcmEngineTransactionException;

	/**
	 * Restituisce l'insieme dei path che individuano il nodo specificato in input.
	 *
	 * <p><strong>NB:</strong> se il nodo esiste ma non esistono path che lo individuano viene
	 * sollevata un'eccezione {@link SearchException} poich&eacute; questa specifica condizione
	 * non si dovrebbe mai verificare (non possono esistere nodi nel repository che non siano
	 * collegati ad almeno un padre).</p>
	 *
	 * @param node L'oggetto {@link Node} corrispondente al nodo di cui sono richiesti i path.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Un array contenente tutti i {@link Path} trovati.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati sono inconsistenti.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws PermissionDeniedException L'utente non ha accesso al nodo specificato in lettura.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione dell'operazione.
	 * @throws RemoteException Se si verifica un errore di comunizazione durante l'operazione.
	 */
	Path[] getPaths(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException,
	RemoteException;

	/**
	 * Esegue una ricerca nella tabella di audit in base ai valori specificati
	 * nei parametri di ricerca.
	 *
	 * @param parametriRicerca parametri utilizzati per effettuare la ricerca nella
	 * tabella di audit.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Un array di risultati.
	 *
	 * @throws EcmEngineException Se si verifica un errore fatale nell'esecuzione
	 * della ricerca.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, PermissionDeniedException, SearchException,
	NoDataExtractedException, EcmEngineTransactionException, RemoteException;

	/**
	 * Restituisce un elenco dei nodi eliminati e archiviati che corrispondono ai vincoli
	 * specificati nei parametri ricevuti in input.
	 *
	 * <p>Il result set restituito &egrave; paginabile in maniera analoga a quanto previsto
	 * dagli altri metodi di ricerca.</p>
	 *
	 * @param params L'{@link NodeArchiveParams} che definisce i parametri di ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Un oggetto {@link SearchResponse} contenente i risultati della ricerca.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	NodeResponse listDeletedNodesNoMetadata(NodeArchiveParams params, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException,PermissionDeniedException, SearchException, RemoteException;

	/**
	 * Restituisce un elenco dei nodi eliminati e archiviati che corrispondono ai vincoli
	 * specificati nei parametri ricevuti in input.
	 *
	 * <p>Il result set restituito &egrave; paginabile in maniera analoga a quanto previsto
	 * dagli altri metodi di ricerca.</p>
	 *
	 * <p><strong>NB:</strong> questo metodo restituisce solo gli oggetti {@code Node} corrispondenti ai risultati.</p>
	 *
	 * @param params L'{@link NodeArchiveParams} che definisce i parametri di ricerca.
	 * @param context L'{@link OperationContext} contenente i dati di
	 * autenticazione.
	 *
	 * @return Un oggetto {@link SearchResponse} contenente i risultati della ricerca.
	 *
	 * @throws InvalidParameterException Se uno o pi&ugrave; parametri specificati
	 * sono inconsistenti.
	 * @throws SearchException Se si verifica un errore durante l'esecuzione della ricerca.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws RemoteException Se si verifica un errore di comunicazione durante
	 * l'operazione.
	 */
	SearchResponse listDeletedNodes(NodeArchiveParams params, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, SearchException, RemoteException;

	Path getAbsolutePath(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, SearchException,	RemoteException;

	public boolean testResources() throws EcmEngineException, RemoteException;
}

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

package it.doqui.index.ecmengine.business.publishing.massive;

import java.rmi.RemoteException;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultContentData;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException;

/**
 * <p>Interfaccia di business che raccoglie i servizi pubblici
 * orchestrati che vengono esportati dall'ECMENGINE per le operazioni massive
 * del repository.</p>
 *
 * @author Doqui
 */
public interface EcmEngineMassiveBusinessInterface {

	/**
	 * Inserisce un insieme di contenuti generici come figli dei nodi specificati.
	 *
	 * @param parents Array di {@link Node} che rappresenta i padri dei nuovi contenuti che si vogliono creare.
	 * @param contents Array di {@link Content} che descrive i nuovi contenuti da inserire.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
	 * altre informazioni di contesto per la chiamata.
	 *
	 * @return Il {@link Node} che punta al nuovo contenuto creato.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se uno o più nodi padre specificati non esistono.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	public Node[] massiveCreateContent(Node[] parents, Content[] contents, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException;

    /**
     * Aggiorna i metadati associati ad un insieme di contenuti sul repository dell'ECMENGINE.
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
     * @param nodes Array di {@link Node} che punta ai contenuti da modificare.
     * @param newContents Array di {@link Content} che definisce i nuovi dati da modificare.
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
	public void massiveUpdateMetadata(Node[] nodes, Content[] newContents, OperationContext context) throws InvalidParameterException, UpdateException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException;

	/**
	 * Restituisce i dati binari dei contenuti di un insieme di nodi.
	 * @param nodes Array di DTO che rappresenta i nodi dei quali si vogliono ricevere i dati binari dei contenuti.
	 * @param contents Array di DTO che che descrive i contenuti di cui si richiedono i dati binari.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws NoSuchNodeException Se il nodo specificato in input non esiste.
	 * @throws ReadException Se si verifica un errore durante la lettura del contenuto.
	 * @throws InvalidCredentialsException
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
     * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws RemoteException Se si verifica un errore di comunicazione durante la modifica.
	 */
    public ResultContentData[] massiveRetrieveContentData(Node[] nodes, Content[] contents, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException;

	/**
	 * Reperisce i metadati di un insieme di contenuti tramite gli UID.
	 *
	 * @param nodes L'array di {@link Node} che identifica i contenuti.
     * @param context L'{@link OperationContext} contenente i dati di autenticazione e le
     * altre infomazioni di contesto per la chiamata.
     *
	 * @return Un array di DTO che rappresenta i metadati.
	 *
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
     * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo specificato non esiste.
	 * @throws ReadException Se si verifica un problema durante la lettura dei metadati.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
     * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
     * @throws RemoteException Se si verifica un errore di comunicazione durante l'operazione.
	 */
	public ResultContent[] massiveGetContentMetadata(Node[] nodes, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException;

	public void massiveDeleteContent(Node[] nodes, OperationContext context)throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,RemoteException, EcmEngineTransactionException;
	
	public boolean testResources() throws EcmEngineException, RemoteException;
}

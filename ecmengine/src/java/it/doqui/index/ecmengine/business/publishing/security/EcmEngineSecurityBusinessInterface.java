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

package it.doqui.index.ecmengine.business.publishing.security;

import java.rmi.RemoteException;

import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.security.*;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;

public interface EcmEngineSecurityBusinessInterface {
	
	/**
	 * Verifica la presenza della firma digitale di un documento e, se presente, restituisce tutte le informazioni legate alla firma.
	 * Il documento firmato può essere salvato opzionalmente nel tenant temporaneo per la futura esecuzione di operazioni.
	 * @param envelopedContent DTO che rappresenta il documento imbustato di cui analizzare l'eventuale fimra digitale.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le altre infomazioni di contesto per la chiamata. 
	 * @return DTO che rappresenta il rapporto della verifica della firma digitale.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	VerifyReport verifyDocument(EnvelopedContent envelopedContent, OperationContext context) throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,RemoteException;
	
	/**
	 * Verifica la presenza della firma digitale del contenuto di un nodo e, se presente, restituisce tutte le informazioni legate alla firma.
	 * @param node il nodo da verficare
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le altre infomazioni di contesto per la chiamata.
	 * @return DTO che rappresenta il rapporto della verifica della firma digitale.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	VerifyReport verifyDocument(Node node, OperationContext context) throws InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,RemoteException;
	
	/**
	 * Sbusta un documento imbustato.
	 * Il documento sbustato può essere salvato opzionalmente nel tenant temporaneo per la futura esecuzione di operazioni.
	 * @param envelopedContent DTO che rappresenta il documento imbustato di cui analizzare l'eventuale fimra digitale.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le altre infomazioni di contesto per la chiamata.
	 * @return DTO che rappresenta il documento sbustato.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	Document extractDocumentFromEnvelope(EnvelopedContent envelopedContent, OperationContext context)throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,RemoteException;

	/**
	 * Sbusta il contenuto imbustato di un nodo.
	 * @param node Il nodo di cui sbustarne il contenuto.
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le altre infomazioni di contesto per la chiamata.
	 * @return DTO che rappresenta il documento sbustato.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	Document extractDocumentFromEnvelope(Node node, OperationContext context) throws InsertException,InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,RemoteException;
	
	/**
	 * Copia il nodo indicato, presente nel tenant temporaneo, sotto un altro nodo.
	 * @param parentNode il nodo sotto il quale verr&agrave; copiato il nodo presente nel tenant temporaneo
	 * @param content Il {@link Content} che descrive il nuovo contenuto da inserire. 
	 * @param context L'{@link OperationContext} contenente i dati di autenticazione e le altre infomazioni di contesto per la chiamata.
	 * @param tempNode il nodo temporaneo da copiare altrove
	 * @return
	 * @throws InvalidParameterException Se uno dei parametri in input non &egrave; valido.
	 * @throws InsertException Se si verifica un errore durante l'inserimento.
	 * @throws NoSuchNodeException Se il nodo padre specificato non esiste.
	 * @throws InvalidCredentialsException Se vengono fornite credenziali di autenticazione errate.
	 * @throws PermissionDeniedException Se l'utente non ha permessi sufficienti per compiere l'operazione richiesta.
	 * @throws EcmEngineTransactionException Se si verifica un errore durante l'esecuzione della transazione.
	 * @throws RemoteException Se si verifica un errore di comunicazione durante l'inserimento.
	 */
	Node createContentFromTemporaney(Node parentNode, Content content,OperationContext context, Node tempNode)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, RemoteException;
	
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

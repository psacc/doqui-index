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
 
package it.doqui.index.ecmengine.business.foundation.repository;

import it.doqui.index.ecmengine.business.personalization.encryption.CryptoTransformationSpec;
import it.doqui.index.ecmengine.business.personalization.encryption.CustomSecretKey;
import it.doqui.index.ecmengine.exception.repository.ContentRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Interfaccia pubblica del servizio di gestione dei contenuti
 * esportata come componente EJB 2.1.
 * 
 * <p>L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link ContentSvcBean}.</p>
 * 
 * <p>Tutti i metodi esportati dal bean di gestione dei contenuti rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link it.doqui.index.ecmengine.exception.repository.ContentRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see ContentSvcBean
 * @see it.doqui.index.ecmengine.exception.repository.ContentRuntimeException
 */
public interface ContentSvc extends EJBLocalObject  {
	/**
	 * Restituisce un {@code ContentWriter} che consente di scrivere il contenuto del nodo specificato.
	 * 
	 * @param nodeRef Il riferimento al nodo di cui si vuole scrivere il contenuto.
	 * @param contentPropertyQName La property che contiene i dati del contenuto da scrivere.
	 * @param update Se {@code true} lo stato del nodo verr&agrave; aggiornato in-transaction.
	 * 
     * @return Restituisce un writer per il contenuto del nodo specificato.
     * 
     * @throws ContentRuntimeException Se si verifica un errore generico nella creazione del writer.
	 */	
	ContentWriter getWriter(NodeRef nodeRef, QName contentPropertyQName, boolean update) 
	throws ContentRuntimeException;

    /**
     * Resituisce un oggetto ContentWriter a una posizione temporanea.
     * Il tempo della validita' di tale location e' determinato dal sistema.
     * 
     * @return Restituisce un write ad una location temporanea
     * @throws ContentRuntimeException con il codice di errore del metodo
     */
	ContentWriter getTempWriter() throws ContentRuntimeException;

    /**
     * 
     * Ricerca l'oggetto transformer che e' in grado di trasformare l'image content.
     * 
     * @return Restituisce un trasformer che puo' essere utilizzato o null se non ne e' stato 
     * trovato alcuno disponibile
     * @throws ContentRuntimeException con il codice di errore del metodo
     */
	ContentTransformer getImageTransformer() throws ContentRuntimeException;

    /**
     * Ricerca l'oggetto transformer che e' in grado di trasformare il content dal sourceMimetype
     * al targetMimetype.
     * 
     * @param sourceMimetype the source mimetype
     * @param targetMimetype the target mimetype
     * @return Restituisce un trasformer che puo' essere utilizzato o null se non ne e' stato 
     * trovato alcuno disponibile
     * @throws ContentRuntimeException con il codice di errore del metodo
     */
	ContentTransformer getTransformer(String sourceMimetype, String targetMimetype) throws ContentRuntimeException;
	
	/**
	 * Restituisce un {@code ContentReader} che consente di leggere il contenuto del nodo specificato.
	 * 
	 * @param nodeRef Il riferimento al nodo di cui si vuole leggere il contenuto.
	 * @param propertyQName La property che contiene i dati del contenuto da leggere.
	 * 
     * @return Restituisce un reader per il contenuto del nodo specificato.
     * 
     * @throws ContentRuntimeException Se si verifica un errore generico nella creazione del reader.
	 */	
	ContentReader getReader(NodeRef nodeRef, QName propertyQName) 
	throws ContentRuntimeException;
	
    /**
     * Restituisce, se un transformer esiste, che tale transformer pu&ograve; leggere il content dal reader 
     * e scrive il content sul writer.
     * 
     * I mimetypes utilizzati per la trasformazione devono essere settati sul getMimetype del ContentAccessor dell'oggetto
     * reader e writer.
     * 
     * @param reader la locazione e il mimetype del source content
     * @param writer la locazione e il mimetype del source content
     * @return Restituisce true se il trasformer esiste, altrimenti false
     * @throws ContentRuntimeException con il codice di errore del metodo
     */
	boolean isTransformable(ContentReader reader, ContentWriter writer) throws ContentRuntimeException;
	
    /**
     * Trasforma il contenuto dal reader passato in input e lo scrive sul writer 
     * passato in input.
     * 
     * I mimetypes utilizzati per la trasformazione devono essere settati sul getMimetype del ContentAccessor dell'oggetto
     * reader e writer.
     * 
     * @param reader the source content location and mimetype
     * @param writer the target content location and mimetype
     * @throws ContentRuntimeException con il codice di errore del metodo
     */
	void transform(ContentReader reader, ContentWriter writer) throws ContentRuntimeException;
	
	/**
	 * Verifica se l'implementazione del Content Service sottostante supporta la crittografia.
	 * 
	 * @return {@code true} se la crittografia &egrave; supportata, {@code false} altrimenti.
	 */
	boolean supportsCryptography();
	
	ContentReader getDecryptingReader(NodeRef nodeRef, QName propertyQName, CustomSecretKey key, 
			CryptoTransformationSpec transform) throws ContentRuntimeException;
	
    ContentWriter getEncryptingWriter(NodeRef nodeRef, QName propertyQName, boolean update, 
    		CustomSecretKey key, CryptoTransformationSpec transform) throws ContentRuntimeException;
}

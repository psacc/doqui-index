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
 
package it.doqui.index.ecmengine.client.engine.exception;


/**
 * <p>Eccezione (checked) che rappresenta l'impossibilit&agrave; di istanziare
 * la classe del client delegate per l'accesso ai servizi esportati dall'ECMENGINE.</p>
 * 
 * <p>Questa condizione di errore pu&ograve; essere dovuta a errori di
 * configurazione nel file di propriet&agrave; {@code ecmengine-engine-delegate.properties}
 * che deve essere fornito dal client fruitore del delegate, oppure all'utilizzo
 * di un'implementazione di delegate non conforme alla specifica contenuta
 * nell'interfaccia {@link it.doqui.index.ecmengine.client.engine.EcmEngineDelegate}.</p>
 * 
 * @author Doqui
 *
 * @see it.doqui.index.ecmengine.client.engine.EcmEngineDelegate
 */
public class EcmEngineDelegateInstantiationException extends Exception {

	private static final long serialVersionUID = 7965030407050177698L;

	/**
	 * Costruttore di default, senza parametri.
	 *
	 */
	public EcmEngineDelegateInstantiationException() {
		super();
	}
	
	/**
	 * Costruttore che permette di associare un messaggio di errore alla
	 * nuova istanza di <code>EcmEngineDelegateInstantiationException</code>.
	 * 
	 * @param msg Il messaggio di errore.
	 */
	public EcmEngineDelegateInstantiationException(String msg) {
		super(msg);
	}
	
	/**
	 * Costruttore che permette di associare un messaggio di errore e una
	 * causa alla nuova istanza di <code>EcmEngineDelegateInstantiationException</code>.
	 * 
	 * @param msg Il messaggio di errore.
	 * @param cause La causa dell'errore.
	 */
	public EcmEngineDelegateInstantiationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

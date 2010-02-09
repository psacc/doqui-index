package it.doqui.index.ecmengine.exception.category;

import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;

/**
 * <p>Eccezione generica (unchecked) che rappresenta un errore
 * verificatosi durante l'esecuzione dei metodi del servizio di
 * category.</p>
 *
 * <p>All'istanza dell'eccezione &egrave; sempre associato il codice
 * di errore che identifica il metodo in cui si &egrave; verificato il
 * problema.</p>
 *
 * @author Doqui
 *
 */

public class CategoryRuntimeException extends EcmEngineFoundationException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	
	/**
	 * Costruttore di default richiamabile dalle sottoclassi.
	 *
	 */
	
	protected CategoryRuntimeException(){
		super();
	}
	
	/**
	 * Costruisce una nuova istanza di <code>CategoryRuntimeException</code>
	 * associandovi il codice di errore specificato.
	 *
	 * @param code Il codice di errore.
	 */
	public CategoryRuntimeException(FoundationErrorCodes code) {
		super(code);
	}
	
	

}

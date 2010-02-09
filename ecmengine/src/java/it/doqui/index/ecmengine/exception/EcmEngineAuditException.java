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
 
package it.doqui.index.ecmengine.exception;


/**
 * Generica eccezione (checked) che indica una condizione di
 * errore verificatasi nel servizio di audit dell'ECMENGINE. 
 * 
 * Un'eccezione
 * di questo tipo non deve bloccare il flusso di esecuzione
 * del business dell'ECMENGINE.
 * 
 * @author Doqui
 */
public class EcmEngineAuditException extends Exception{
	
		private static final long serialVersionUID = -5904056134713117856L;

		/**
		 * Costruttore di default utilizzabile nelle sottoclassi.
		 *
		 */
		protected EcmEngineAuditException() {
			super();
		}
		
		/**
		 * Costruttore che permette di associare un messaggio di errore alla
		 * nuova istanza di <code>EcmEngineAuditException</code>.
		 * 
		 * @param msg Il messaggio di errore.
		 */
		public EcmEngineAuditException(String msg) {
			super(msg);
		}

		/**
		 * Costruttore che permette di associare un messaggio di errore e una
		 * causa alla nuova istanza di <code>EcmEngineAuditException</code>.
		 * 
		 * @param msg Il messaggio di errore.
		 * @param cause La causa dell'errore.
		 */
		public EcmEngineAuditException(String msg, Throwable cause) {
			super(msg, cause);
		}
}

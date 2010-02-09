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
 
package it.doqui.index.ecmengine.exception.contentmanagement;

import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;

public class ActionRuntimeException extends EcmEngineFoundationException {

	private static final long serialVersionUID = -2857537603839266392L;

	/**
	 * Costruttore di default richiamabile dalle sottoclassi.
	 *
	 */
	protected ActionRuntimeException() {
		super();
	}
	
	/**
	 * Costruisce una nuova istanza di <code>ActionRuntimeException</code>
	 * associandovi il codice di errore specificato.
	 * 
	 * @param code Il codice di errore.
	 */
	public ActionRuntimeException(FoundationErrorCodes code) {
		super(code);
	}

}

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

package it.doqui.index.ecmengine.dto.backoffice;


import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * Oggetto che rappresenta un errore di integrit&agrave;.
 * Contiene un messaggio che specifica il tipo di errore riscontrato e un array di stringhe
 * contenente l'uid dei nodi coinvolti (numero variabile a seconda dell'errore riscontrato:
 * generalmente uno per gli errori sui nodi, due per gli errori che riguardano le
 * associazioni).
 * @author Doqui
 */
public class IntegrityReport extends EcmEngineDto implements IntegrityMessage{

	private static final long serialVersionUID = -1886535788365861143L;
	private String[] data;
	private String message;

	/**
	 * Costruttore predefinito.
	 */
	public IntegrityReport() {
		super();
	}

	/**
	 * Restituisce l'array degli uid dei nodi coinvolti nell'errore di integrit&agrave;.
	 * @return array di uid.
	 */
	public String[] getData() {
		return data;
	}

	/**
	 * Imposta l'array di uid dei nodi.
	 * @param data l'array di uid dei nodi.
	 */
	public void setData(String[] data) {
		this.data = data;
	}

	/**
	 * Restituisce il messaggio di errore.
	 * @see IntegrityMessage
	 * @return il messaggio di errore.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Imposta il messaggio di errore.
	 * @see IntegrityMessage
	 * @param message il messaggio di errore.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}

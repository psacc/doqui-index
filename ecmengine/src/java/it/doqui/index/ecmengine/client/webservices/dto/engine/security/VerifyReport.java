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

package it.doqui.index.ecmengine.client.webservices.dto.engine.security;

import java.util.Date;

public class VerifyReport{


	private String uid;
	// Array delle firme del documento
    private Signature[] signature;
	// La data nella quale e' stato verificato il documento
	private Date date;
	// Gestione errore di sbustamento
	private int    errorCode;
    // Eventuale figlio se a sua volta il documento e' sbustabile
    private VerifyReport child;

    /**
	 * Restituisce l'array di firme del documento, comprensivo di eventuali errori
	 * di sbustamento.
	 *
	 * @return L'array di firme.
	 */
	public Signature[] getSignature() {
		return signature;
	}

	/**
	 * Imposta l'array di firme del documento.
	 *
	 * @param signature L'array di firme.
	 */
	public void setSignature( Signature[] signature ) {
		this.signature = signature;
	}

	/**
	 * Restituisce la data in cui e' stato chiesto il rapporto di verifica
	 * @return data dell'operazione.
	 */
	public java.util.Date getDate() {
		return date;
	}

	/**
	 * Imposta la data in cui e' stato chiesto il rapporto di verifica
	 * @param date data ed ora in cui l'operazione  e' stata richiamata.
	 */
	public void setDate(java.util.Date date) {
		this.date = date;
	}

	/**
	 * Restituisce l'eventuale codice di errore verificatosi durante il riconoscimento.
	 * @return L'eventuale codice di errore verificatosi durante il riconoscimento.
	 */
	public int getErrorCode() {
        return errorCode;
	}

	/**
	 * Imposta l'eventuale codice di errore verificatosi durante il riconoscimento.
	 * @param errorCode Il codice di errore verificatosi durante il riconoscimento.
	 */
	public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
	}

    /**
     * Restituisce l'eventuale rapporto di verifica figlio.
     * @return L'eventuale rapporto di verifica figlio.
     */
	public VerifyReport getChild() {
		return child;
	}

    /**
     * Imposta l'eventuale rapporto di verifica figlio.
     * @param child Il rapporto di verifica figlio.
     */
	public void setChild(VerifyReport child) {
		this.child = child;
	}

	/**
	 * Restituisce l'eventuale uid del documento a cui il rapporto di verifica e' associato.
	 * @return L'eventuale uid del documento a cui il rapporto di verifica e' associato.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Imposta l'eventuale uid del documento a cui il rapporto di verifica e' associato.
	 * @param uid L'uid del documento a cui il rapporto di verifica e' associato.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

}

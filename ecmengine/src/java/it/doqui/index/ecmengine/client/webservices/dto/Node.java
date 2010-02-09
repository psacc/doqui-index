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

package it.doqui.index.ecmengine.client.webservices.dto;

/**
 * Classe DTO che rappresenta un generico nodo nel repository dell'ECMENGINE.
 *
 * <p>Ogni nodo &egrave; caratterizzato da un identificativo univoco che
 * ne consente l'individuazione all'interno del repository dell'ECMENGINE.</p>
 *
 * @author Doqui
 */
public class Node {


	private String uid;

	/**
	 * Costruttore predefinito.
	 */
	public Node() {


		this.uid = null;
	}

	/**
	 * Costruisce una nuova istanza di {@code Node} a partire dall'identificativo
	 * univoco del nodo.
	 *
	 * @param uid L'identificativo del nodo.
	 */
	public Node(String uid) {


		this.uid = uid;
	}

	public Node(String uid, String repository) {


		this.uid = uid;
	}

	/**
	 * Imposta l'identificativo univoco associato a questo nodo.
	 *
	 * @param uid
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * Restituisce l'identificativo univoco del nodo.
	 *
	 * @return L'identificativo univoco del nodo.
	 */
	public String getUid() {
		return this.uid;
	}





















}

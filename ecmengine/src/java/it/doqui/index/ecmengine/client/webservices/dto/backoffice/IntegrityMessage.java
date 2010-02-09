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

package it.doqui.index.ecmengine.client.webservices.dto.backoffice;

public interface IntegrityMessage{
	/**
	 * Indica che l'errore e' riferito ad un nodo presente sugli indici ma non sul db.
	 */
	public static final String DELETED_NODE="DELETED_NODE";
	/**
	 * Indica che l'errore e' riferito ad un nodo presente sul db ma non sugli indici.
	 */
	public static final String MISSING_NODE="MISSING_NODE";
	/**
	 * Indica che l'errore e' riferito ad un'associazione presente sugli indici ma non sul db.
	 */
	public static final String DELETED_ASSOC="DELETED_ASSOC";
	/**
	 * Indica che l'errore e' riferito ad un'associazione presente sul db ma non sugli indici.
	 */
	public static final String MISSING_ASSOC="MISSING_ASSOC";
}

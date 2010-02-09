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
 * Generica classe DTO madre di tutte le classi utilizzate per il trasferimento
 * di elementi del content model.
 *
 * <p>Tali elementi possono rappresentare contenuti interi o loro parti nella comunicazione
 * fra i fruitori dell'ECMENGINE e l'ECMENGINE stesso e viceversa. Ogni elemento del content model
 * &egrave; identificato da un nome con prefisso. Le due parti del nome sono separate
 * dal carattere specificato nella costante {@link #PREFIXED_NAME_SEPARATOR}.</p>
 *
 * @author Doqui
 */
public abstract class ContentItem {

	/** Carattere separatore usato per separare prefisso e nome. */
	public static final char PREFIXED_NAME_SEPARATOR = ':';

	private String prefixedName;










	/**
	 * Restituisce il nome, completo di prefisso, che identifica questo {@code ContentItem}.
	 *
	 * @return Il nome completo di prefisso.
	 */
	public final String getPrefixedName() {
		return this.prefixedName;
	}

	/**
	 * Imposta il nome ,completo di prefisso, associato a questo {@code ContentItem}.
	 *
	 * @param prefixedName Il nome completo di prefisso.
	 */
	public final void setPrefixedName(String prefixedName) {
		this.prefixedName = prefixedName;
	}
}

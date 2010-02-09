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

package it.doqui.index.ecmengine.client.webservices.dto.engine.search;

/**
 * Classe che rappresenta un'associazione tra contenuti intesa sia come
 * child-association sia come reference. Le istanze di questa classe contengono
 * i riferimenti ai contenuti associati.
 *
 * @author Doqui
 *
 */
public class ResultAssociation {



	private boolean childAssociation;
	private String targetUid;
	private String targetPrefixedName;
	private String typePrefixedName;



	/**
	 * Costruttore predefinito.
	 */
	public ResultAssociation() {
		//super();

		this.childAssociation = false;
		this.targetUid = null;
		this.targetPrefixedName = null;
	}

	/**
	 * Indica se l'associazione &egrave; di tipo padre-figlio.
	 *
	 * @return {@code true} se l'associazione &egrave; di tipo padre-figlio,
	 *         {@code false} altrimenti
	 */
	public boolean isChildAssociation() {
		return childAssociation;
	}

	public void setChildAssociation(boolean childAssociation) {
		this.childAssociation = childAssociation;
	}

	/**
	 * Restituisce il nome del contenuto referenziato.
	 *
	 * @return il nome del contenuto referenziato.
	 * @deprecated
	 */
	@Deprecated
	public String getTargetPrefixedName() {
		return this.targetPrefixedName;
	}

	/**
	 * Imposta il nome del contenuto referenziato.
	 *
	 * @param prefixedName
	 * @deprecated
	 */
	@Deprecated
	public void setTargetPrefixedName(String prefixedName) {
		this.targetPrefixedName = prefixedName;
	}

	/**
	 * Restituisce il tipo dell'associazione completo di prefisso.
	 * @return tipo dell'associazione
	 */

	public String getTypePrefixedName() {
		return typePrefixedName;
	}

	/**
	 * Imposta il tipo dell'associazione
	 * @param typePrefixedName tipo dell'associazione
	 */

	public void setTypePrefixedName(String typePrefixedName) {
		this.typePrefixedName = typePrefixedName;
	}

	/**
	 * Restituisce l'identificatore del contenuto referenziato.
	 *
	 * @return l'identificatore del contenuto referenziato
	 */
	public String getTargetUid() {
		return targetUid;
	}

	public void setTargetUid(String uid) {
		this.targetUid = uid;
	}

    // ContentItem ------------------------------------------------
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
    // ContentItem ------------------------------------------------
}

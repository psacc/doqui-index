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
 
package it.doqui.index.ecmengine.dto.engine.search;

import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * Classe per la definizione dei criteri di ordinamento di una ricerca.
 * 
 * @author DoQui
 *
 */
public class SortField extends EcmEngineDto {

	private static final long serialVersionUID = -2008080177096353758L;
	
	private String fieldName;
	private boolean ascending;

	/**
	 * Costruttore predefinito.
	 */
	public SortField() {
		super();
	}

	/**
	 * Restituisce il valore del flag che indica il modo di ordinare
	 * i risultati della ricerca ({@code true} nel caso di ordinamento
	 * crescente, {@code false} nel caso di ordinamento decrescente).
	 * 
	 * @return {@code true} nel caso di ordinamento
	 * crescente, {@code false} nel caso di ordinamento decrescente
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * Imposta il valore del flag che indica il modo di ordinare i
	 * risultati della ricerca ({@code true} nel caso di ordinamento
	 * crescente, {@code false} nel caso di ordinamento decrescente).
	 * 
	 * @param ascending {@code true} nel caso di ordinamento
	 * crescente, {@code false} nel caso di ordinamento decrescente
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	/**
	 * Restituisce il nome del campo su cui eseguire l'ordinamento.
	 * Il nome del campo deve essere completo di prefisso.
	 * 
	 * @return Il nome del campo su cui eseguire l'ordinamento.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Imposta il campo su cui eseguire l'ordinamento.
	 * Il nome del campo deve essere completo di prefisso.
	 * 
	 * @param fieldName Il nome del campo su cui eseguire l'ordinamento.
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

}

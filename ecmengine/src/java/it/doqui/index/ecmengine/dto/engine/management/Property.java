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
 
package it.doqui.index.ecmengine.dto.engine.management;

import it.doqui.index.ecmengine.dto.ContentItem;

/**
 * Classe DTO che rappresenta una <i>property</i> del content model
 * dell'ECMENGINE.
 * 
 * <p>Una property &egrave; sempre caratterizzata da un nome, dal tipo di valore che
 * pu&ograve; contenere e da un flag che indica se la property pu&ograve; avere valori
 * multipli oppure no.</p>
 * 
 * @author Doqui
 */
public class Property extends ContentItem {

	private static final long serialVersionUID = 1787427376557589600L;

	private String dataType;
	private boolean multivalue;
	private String [] values;
	
	/**
	 * Costruttore predefinito.
	 */
	public Property() {
		super();
		
		this.dataType = null;
		this.multivalue = false;
		this.values = null;
	}
	
	/**
	 * Costruttore che inizializza una nuova istanza di {@code Property}.
	 * 
	 * <p>L'inizializzazione avviene a partire dal nome, dal tipo di dato e dal flag 
	 * che indica se sono consentiti i valori multipli oppure no.</p>
	 * 
	 * @param prefixedName Il nome (completo di prefisso) della property.
	 * @param dataType Il tipo di dato consentito per il valore (o i valori).
	 * @param isMultivalue {@code true} se la property supporta valori multipli, {@code false}
	 * altrimenti.
	 */
	public Property(String prefixedName, String dataType, boolean isMultivalue) {
		super();
		
		setPrefixedName(prefixedName);
		
		this.dataType = dataType;
		this.multivalue = isMultivalue;
	}
	
	/**
	 * Inizializza l'insieme dei valori della property con l'array specificato in input.
	 * 
	 * <p>Per specificare il valore di una property che accetta solo un singolo valore &egrave;
	 * necessario passare in input un array di un elemento.</p>
	 * 
	 * @param values Un array contenente i valori da associare alla property.
	 */
	public void setValues(String [] values) {
		this.values = values;
	}
	
	/**
	 * Restituisce l'insieme dei valori della property sotto forma di array.
	 * 
	 * @return L'insieme dei valori della property.
	 */
	public String [] getValues() {
		return this.values;
	}
	
	/**
	 * Restituisce il primo valore della property.
	 * 
	 * @return Il primo valore della property oppure {@code null} se alla property
	 * non &egrave; associato alcun valore.
	 */
	public String getValue() {
		return (this.values != null) ? this.values[0] : null;
	}

	/**
	 * Restituisce il tipo di dato utilizzato per rappresentare i valori della property.
	 * 
	 * @return Il tipo di dato.
	 */
	public String getDataType() {
		return this.dataType;
	}

	/**
	 * Imposta il tipo di dato con cui rappresentare il valore di questa property.
	 * 
	 * <p><strong>NB:</strong> il tipo specificato deve essere una stringa di almeno
	 * un carattere (non {@code null}), altrimenti il metodo solleva una
	 * {@code IllegalArgumentException}.</p>
	 * 
	 * @param type Il tipo di dato.
	 */
	public void setDataType(String type) {
		this.dataType = type;
	}
	
	/**
	 * Restituisce il valore del flag che indica se la property supporta valori multipli
	 * oppure no.
	 * 
	 * @return {@code true} se i valori multipli sono supportati, {@code false} altrimenti.
	 */
	public boolean isMultivalue() {
		return this.multivalue;
	}
	
	/**
	 * Imposta il valore del flag che indica se questa popriet&agrave; supporta valori
	 * multipli.
	 * 
	 * @param isMultivalue {@code true} se la property supporta valori multipli, {@code false}
	 * altrimenti.
	 */
	public void setMultivalue(boolean isMultivalue) {
		this.multivalue = isMultivalue;
	}
}

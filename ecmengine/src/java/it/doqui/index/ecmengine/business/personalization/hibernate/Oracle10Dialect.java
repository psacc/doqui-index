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
 
package it.doqui.index.ecmengine.business.personalization.hibernate;

import java.sql.Types;

import org.hibernate.dialect.Oracle9Dialect;

/**
 * Semplice dialect Hibernate per Oracle 10g.
 * 
 * <p>Dialect derivato da Oracle9Dialect con l'aggiunta di
 * personalizzazioni per rispettare alcuni alias fra tipi esistenti
 * su Oracle10.</p>
 * 
 * @author DoQui
 *
 */
public class Oracle10Dialect extends Oracle9Dialect {

	/**
	 * Costruttore predefinito.
	 */
	public Oracle10Dialect() {
		super();
		
		/* 
		 * Il tipo JDBC "double" deve essere rimappato sul tipo 
		 * "binary_double" introdotto su Oracle 10g
		 */
		registerColumnType(Types.DOUBLE, "binary_double");
	}
	
	/**
	 * Restituisce la query da utilizzare per ricercare le sequence disponibili.
	 * 
	 * <p>La ricerca &egrave; basata sulla vista di sistema
	 * &quot;all_sequences&quot;, che contiene tutte le sequence a cui
	 * l'utente corrente ha accesso.</p>
	 * 
	 * @return String La query SQL per la ricerca delle sequence. 
	 */
	public String getQuerySequencesString() {
		return "SELECT sequence_name FROM all_sequences";
	}
}

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
 
package it.doqui.index.ecmengine.dto;

/**
 * Classe DTO che rappresenta un generico path nel repository dell'ECMENGINE.
 * 
 * <p>Ogni path &egrave; caratterizzato da una stringa che lo rappresenta e
 * dall'identificativo del repository sul quale il path &egrave; presente.</p>
 * 
 * @author Doqui
 */
public class Path extends EcmEngineDto {

	private static final long serialVersionUID = 6698912026743638382L;

	private String path;
	private boolean primary;

	/**
	 * Restituisce la stringa corrispondente a questo path.
	 * 
	 * @return La rappresentazione in stringa di questo path.
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Imposta la stringa corrispondente a questo path.
	 * 
	 * @param path La rappresentazione in stringa di questo path.
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Restituisce il valore del flag che caratterizza il path come &quot;primario&quot;.
	 * 
	 * <p>Un path primario &egrave; un path costruito ripercorrendo le associazioni 
	 * primarie di un nodo. <i>Esiste un solo path primario per nodo.</i></p>
	 * 
	 * @return {@code true} se il path &egrave; primario, {@code false} altrimenti.
	 */
	public boolean isPrimary() {
		return primary;
	}

	/**
	 * Imposta il valore del flag che caratterizza il path come &quot;primario&quot;.
	 * 
	 * <p>Un path primario &egrave; un path costruito ripercorrendo le associazioni 
	 * primarie di un nodo. <i>Esiste un solo path primario per nodo.</i></p>
	 * 
	 * @param primary {@code true} se il path &egrave; primario, {@code false} altrimenti.
	 */
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
}

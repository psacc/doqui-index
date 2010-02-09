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
 
package it.doqui.index.ecmengine.dto.backoffice.model;

/**
 * Classe DTO che rappresenta un content model custom di ECMENGINE.
 * 
 * <p>Questo DTO viene utilizzato dai servizi di gestione del deployment
 * a caldo dei content model. Un content model &egrave; descritto mediante un file XML:
 * utilizzando questo DTO e il servizio di backoffice di ECMENGINE &egrave; possibile
 * eseguire il deployment e l'undeployment a caldo di nuovi content model.</p>
 * 
 * @author DoQui
 */
public class CustomModel extends ModelDto {

	private static final long serialVersionUID = -8128184649429410910L;

	private String filename;
	private boolean active;
	private byte[] data;

	/**
	 * Restituisce il contenuto del file di descrizione del modello 
	 * sotto forma di array di byte.
	 * 
	 * @return Il contenuto del file di descrizione del modello.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Imposta il contenuto del file di descrizione del modello 
	 * sotto forma di array di byte.
	 * 
	 * @param data Il contenuto del file di descrizione del modello.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Restituisce il nome del file XML di descrizione del modello.
	 * 
	 * @return Il nome del file.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Imposta il nome del file XML di descrizione del modello.
	 * 
	 * @param filename Il nome del file.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Restituisce il valore del flag che indica se questo modello &egrave; 
	 * attivato oppure no.
	 * 
	 * @return {@code true} se questo modello &egrave; attivo, altrimenti 
	 * {@code false}
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Imposta il valore del flag che indica se questo modello &egrave; 
	 * attivato oppure no.
	 * 
	 * @param active {@code true} se questo modello &egrave; attivo, altrimenti 
	 * {@code false}
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

}

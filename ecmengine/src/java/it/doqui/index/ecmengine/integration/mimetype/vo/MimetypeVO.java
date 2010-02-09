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

package it.doqui.index.ecmengine.integration.mimetype.vo;

/**
 * Value object (VO) per lo scambio dei dati della tabella dei job sul database.
 *
 * @author DoQui
 *
 */
public class MimetypeVO {

	private int id;
	private String fileExtension;
	private String mimeType;
	private int priority;

	/**
	 * Costruttore predefinito.
	 */
	public MimetypeVO() {
		super();
	}

	/**
	 * Restituisce l'id del mimetype
	 *
	 * @return L'id del mimetype.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Imposta l'id del mimetype.
	 *
	 * @param jobId L'id del mimetype.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Restituisce la FileExtension del mimetype
	 *
	 * @return La FileExtension del mimetype.
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * Imposta la FileExtension del mimetype.
	 *
	 * @param ref La FileExtension del mimetype
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * Restituisce il mimetype associato a una certa estensione
	 *
	 * @return il mimetype associato a una certa estensione
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Imposta il mimetype associato a una certa estensione
	 *
	 * @param message il mimetype associato a una certa estensione
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	

}

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

import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * Classe DTO utilizzata per il riconoscimento del MIMEtype a partire dall'estensione.
 *
 * @author Doqui
 */
public class Mimetype extends EcmEngineDto {

	private static final long serialVersionUID = 4926456345634560606L;

	private String fileExtension;
	private String mimetype;

	/**
	 * Restituisce l'estensione impostata.
	 * @return L'estensione impostata, null altrimenti.
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * Imposta l'estensione.
	 * @param fileExtension L'estensione da impostare.
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * Restituisce il MIMEtype inpostato.
	 * @return Ll MIMEtype impostato, null altrimenti.
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * Imposta il MIMEtype.
	 * @param mimetype Il MIMEtype da impostare.
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

}

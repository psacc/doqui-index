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
/**
 * DTO che rappresenta un risultato di riconoscimento di un contenuto da parte di FileFormat.
 */
import it.doqui.index.ecmengine.dto.EcmEngineDto;

public class FileFormatInfo extends EcmEngineDto {

	private static final long serialVersionUID = 5719651966051480138L;
	private String puid;
	private String mimeType;
	private String description;
	private String formatVersion;
	private int typeCode;
	private String typeDescription;
	private String warning;
	private java.util.Date identificationDate;
	private String uid;


	public FileFormatInfo() {
		super();
	}

	/**
	 * Restituisce la descrizione del mimetype.
	 * @return La descrizione del mimetype.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Imposta la descrizione del mimetype.
	 * @param description La descrizione del mimetype.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Restituisce il mimetype.
	 * @return Il mimetype.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Imposta il mimetype.
	 * @param mimeType Il mimetype.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Restituisce il puid associato al tipo di file riconosciuto.
	 * @return Il puid.
	 */
	public String getPuid() {
		return puid;
	}

	/**
	 * Imposta il puid associato al tipo di file riconosciuto.
	 * @param puid Il puid.
	 */
	public void setPuid(String puid) {
		this.puid = puid;
	}

	/**
	 * Restituisce la versione del formato riconosciuto.
	 * @return La versione del formato riconosciuto.
	 */
	public String getFormatVersion() {
		return formatVersion;
	}

	/**
	 * Imposta la versione del formato riconosciuto.
	 * @param formatVersion La versione del formato riconosciuto.
	 */
	public void setFormatVersion(String formatVersion) {
		this.formatVersion = formatVersion;
	}

	/**
	 * Restituisce eventuali informazioni aggiuntive sul riconoscimento.
	 * @return Informazioni aggiuntive sul riconoscimento.
	 */
	public String getWarning() {
		return warning;
	}

	/**
	 * Imposta eventuali informazioni aggiuntive sul riconoscimento.
	 * @param warning Informazioni aggiuntive sul riconoscimento.
	 */
	public void setWarning(String warning) {
		this.warning = warning;
	}

	/**
	 * Restituisce un codice di tipo associato al formato.
	 * @return Un codice di tipo associato al formato.
	 */
	public int getTypeCode() {
		return typeCode;
	}

	/**
	 * Imposta un codice di tipo associato al formato.
	 * @param typeCode Un codice di tipo associato al formato.
	 */
	public void setTypeCode(int typeCode) {
		this.typeCode = typeCode;
	}

	/**
	 * Restituisce l'esito del processo di identificazione.
	 * @return L'esito del processo di identificazione.
	 */
	public String getTypeDescription() {
		return typeDescription;
	}

	/**
	 * Imposta l'esito del processo di identificazione.
	 * @param typeDescription L'esito del processo di identificazione.
	 */
	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	/**
	 * Restituisce il timestamp del riconoscimento.
	 * @return Il timestamp del riconoscimento.
	 */
	public java.util.Date getIdentificationDate() {
		return identificationDate;
	}

	/**
	 * Imposta il timestamp del riconoscimento.
	 * @param identificationDate Il timestamp del riconoscimento.
	 */
	public void setIdentificationDate(java.util.Date identificationDate) {
		this.identificationDate = identificationDate;
	}

	/**
	 * Imposta l'eventuale uid del nodo associato al riconoscimento.
	 * @return L'eventuale uid del nodo associato al riconoscimento.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Restituisce l'eventuale uid del nodo associato al riconoscimento.
	 * @param uid L'eventuale uid del nodo associato al riconoscimento.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}
}

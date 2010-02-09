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
 
package it.doqui.index.ecmengine.dto.backoffice;

import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * DTO che rappresenta un archivio compresso da importare nell'ECMENGINE.
 * 
 * <p>
 * Questo DTO fornisce tutte le informazioni necessarie ai servizi di backoffice
 * per importare i contenuti di un archivio nel repository.
 * </p>
 * 
 * <p>
 * I formati di archivio supportati sono:
 * <ul>
 * <li>zip</li>
 * <li>tar</li>
 * <li>tar.gz</li>
 * </ul>
 * </p>
 * 
 * @author Doqui
 */
public class DataArchive extends EcmEngineDto {

	private static final long serialVersionUID = 8525514577555408070L;

	private String format;

	private byte[] content;

	private String mappedContentTypePrefixedName;

	private String mappedContentNamePropertyPrefixedName;

	private String mappedContainerTypePrefixedName;

	private String mappedContainerNamePropertyPrefixedName;

	private String mappedContainerAssocTypePrefixedName;

	private String parentContainerAssocTypePrefixedName;

	/**
	 * Costruttore predefinito.
	 */
	public DataArchive() {
		super();
	}

	/**
	 * Restituisce il contenuto binario dell'archivio.
	 * 
	 * @return Il contenuto binario dell'archivio.
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * Imposta il contenuto binario dell'archivio.
	 * 
	 * @param content Il contenuto binario dell'archivio.
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * Restituisce il formato dell'archivio.
	 * 
	 * @return Il formato dell'archivio.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Imposta il formato dell'archivio.
	 * 
	 * @param format Il formato dell'archivio.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Restituisce il nome, completo di prefisso, della property su cui impostare
	 * il nome di una directory contenuta nell'archivio.
	 * 
	 * @return Il nome, completo di prefisso, della property su cui impostare il
	 *         nome di una directory contenuta nell'archivio.
	 */
	public String getMappedContainerNamePropertyPrefixedName() {
		return mappedContainerNamePropertyPrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, della property su cui impostare il
	 * nome di una directory contenuta nell'archivio.
	 * 
	 * @param mappedContainerNamePropertyPrefixedName
	 *            Il nome, completo di prefisso, della property su cui impostare
	 *            il nome di una directory contenuta nell'archivio.
	 */
	public void setMappedContainerNamePropertyPrefixedName(String mappedContainerNamePropertyPrefixedName) {
		this.mappedContainerNamePropertyPrefixedName = mappedContainerNamePropertyPrefixedName;
	}

	/**
	 * Restituisce il nome, completo di prefisso, del tipo da impostare
	 * per le directory.
	 * 
	 * @return Il nome, completo di prefisso, del tipo da impostare per
	 *         le directory.
	 */
	public String getMappedContainerTypePrefixedName() {
		return mappedContainerTypePrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, del tipo da impostare
	 * per le directory.
	 * 
	 * @param mappedContainerTypePrefixedName
	 *            Il nome, completo di prefisso, del tipo da impostare
	 *            per le directory.
	 */
	public void setMappedContainerTypePrefixedName(String mappedContainerTypePrefixedName) {
		this.mappedContainerTypePrefixedName = mappedContainerTypePrefixedName;
	}

	/**
	 * Restituisce il nome, completo di prefisso, della property cu cui
	 * impostare il nome di un file contenuto nell'archivio.
	 * 
	 * @return Il nome, completo di prefisso, della property cu cui impostare il
	 *         nome di un file contenuto nell'archivio.
	 */
	public String getMappedContentNamePropertyPrefixedName() {
		return mappedContentNamePropertyPrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, della property su cui impostare il
	 * nome di un file contenuto nell'archivio.
	 * 
	 * @param mappedContentNamePropertyPrefixedName
	 *            Il nome, completo di prefisso, della property su cui impostare
	 *            il nome di un file contenuto nell'archivio.
	 */
	public void setMappedContentNamePropertyPrefixedName(String mappedContentNamePropertyPrefixedName) {
		this.mappedContentNamePropertyPrefixedName = mappedContentNamePropertyPrefixedName;
	}

	/**
	 * Restituisce il nome, completo di prefisso, del tipo da impostare per i
	 * file.
	 * 
	 * @return Il nome, completo di prefisso, del tipo da impostare per i file.
	 */
	public String getMappedContentTypePrefixedName() {
		return mappedContentTypePrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, del tipo da impostare per i file.
	 * 
	 * @param mappedContentTypePrefixedName
	 *            Il nome, completo di prefisso, del tipo da impostare per i
	 *            file.
	 */
	public void setMappedContentTypePrefixedName(String mappedContentTypePrefixedName) {
		this.mappedContentTypePrefixedName = mappedContentTypePrefixedName;
	}

	/**
	 * Restituisce il nome, completo di prefisso, dell'associazione da impostare
	 * per i file da creare in una directory.
	 * 
	 * @return Il nome, completo di prefisso, dell'associazione da impostare per
	 *         i file da creare in una directory.
	 */
	public String getMappedContainerAssocTypePrefixedName() {
		return mappedContainerAssocTypePrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, dell'associazione da impostare per
	 * i file da creare in una directory.
	 * 
	 * @param mappedContainerAssocTypePrefixedName
	 *            Il nome, completo di prefisso, dell'associazione da impostare
	 *            per i file da creare in una directory.
	 */
	public void setMappedContainerAssocTypePrefixedName(String mappedContainerAssocTypePrefixedName) {
		this.mappedContainerAssocTypePrefixedName = mappedContainerAssocTypePrefixedName;
	}

	/**
	 * Restituisce il nome, completo di prefisso, dell'associazione da impostare
	 * rispetto al nodo sotto il quale verranno creati i contenuti
	 * dell'archivio.
	 * 
	 * @return Il nome, completo di prefisso, dell'associazione da impostare
	 *         rispetto al nodo sotto il quale verranno creati i contenuti
	 *         dell'archivio.
	 */
	public String getParentContainerAssocTypePrefixedName() {
		return parentContainerAssocTypePrefixedName;
	}

	/**
	 * Imposta il nome, completo di prefisso, dell'associazione da impostare
	 * rispetto al nodo sotto il quale verranno creati i contenuti
	 * dell'archivio.
	 * 
	 * @param parentContainerAssocTypePrefixedName
	 *            Il nome, completo di prefisso, dell'associazione da impostare
	 *            rispetto al nodo sotto il quale verranno creati i contenuti
	 *            dell'archivio.
	 */
	public void setParentContainerAssocTypePrefixedName(String parentContainerAssocTypePrefixedName) {
		this.parentContainerAssocTypePrefixedName = parentContainerAssocTypePrefixedName;
	}

}

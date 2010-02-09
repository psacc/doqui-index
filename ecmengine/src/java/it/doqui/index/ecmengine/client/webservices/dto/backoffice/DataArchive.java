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

package it.doqui.index.ecmengine.client.webservices.dto.backoffice;

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
public class DataArchive {

	private String format;
	private byte[] content;
	private String mappedContentTypePrefixedName;
	private String mappedContentNamePropertyPrefixedName;
	private String mappedContainerTypePrefixedName;
	private String mappedContainerNamePropertyPrefixedName;
	private String mappedContainerAssocTypePrefixedName;
	private String parentContainerAssocTypePrefixedName;

	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getMappedContainerAssocTypePrefixedName() {
		return mappedContainerAssocTypePrefixedName;
	}
	public void setMappedContainerAssocTypePrefixedName(
			String mappedContainerAssocTypePrefixedName) {
		this.mappedContainerAssocTypePrefixedName = mappedContainerAssocTypePrefixedName;
	}
	public String getMappedContainerNamePropertyPrefixedName() {
		return mappedContainerNamePropertyPrefixedName;
	}
	public void setMappedContainerNamePropertyPrefixedName(
			String mappedContainerNamePropertyPrefixedName) {
		this.mappedContainerNamePropertyPrefixedName = mappedContainerNamePropertyPrefixedName;
	}
	public String getMappedContainerTypePrefixedName() {
		return mappedContainerTypePrefixedName;
	}
	public void setMappedContainerTypePrefixedName(
			String mappedContainerTypePrefixedName) {
		this.mappedContainerTypePrefixedName = mappedContainerTypePrefixedName;
	}
	public String getMappedContentNamePropertyPrefixedName() {
		return mappedContentNamePropertyPrefixedName;
	}
	public void setMappedContentNamePropertyPrefixedName(
			String mappedContentNamePropertyPrefixedName) {
		this.mappedContentNamePropertyPrefixedName = mappedContentNamePropertyPrefixedName;
	}
	public String getMappedContentTypePrefixedName() {
		return mappedContentTypePrefixedName;
	}
	public void setMappedContentTypePrefixedName(
			String mappedContentTypePrefixedName) {
		this.mappedContentTypePrefixedName = mappedContentTypePrefixedName;
	}
	public String getParentContainerAssocTypePrefixedName() {
		return parentContainerAssocTypePrefixedName;
	}
	public void setParentContainerAssocTypePrefixedName(
			String parentContainerAssocTypePrefixedName) {
		this.parentContainerAssocTypePrefixedName = parentContainerAssocTypePrefixedName;
	}

}

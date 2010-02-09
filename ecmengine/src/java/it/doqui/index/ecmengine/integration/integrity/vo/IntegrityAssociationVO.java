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

package it.doqui.index.ecmengine.integration.integrity.vo;

/**
 * Value object (VO) per lo scambio dei dati della tabella dei job sul database.
 *
 * @author DoQui
 *
 */
public class IntegrityAssociationVO {

	private Long id;
	private Long version;
	private Long parent_node_id;
	private Long child_node_id;
	private String type_qname;
	private String qname;
	private String child_node_name;
	private Long child_node_name_crc;
	private boolean is_primary;
	private Long assoc_index;
	
	public IntegrityAssociationVO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getParent_node_id() {
		return parent_node_id;
	}

	public void setParent_node_id(Long parent_node_id) {
		this.parent_node_id = parent_node_id;
	}

	public Long getChild_node_id() {
		return child_node_id;
	}

	public void setChild_node_id(Long child_node_id) {
		this.child_node_id = child_node_id;
	}

	public String getType_qname() {
		return type_qname;
	}

	public void setType_qname(String type_qname) {
		this.type_qname = type_qname;
	}

	public String getQname() {
		return qname;
	}

	public void setQname(String qname) {
		this.qname = qname;
	}

	public String getChild_node_name() {
		return child_node_name;
	}

	public void setChild_node_name(String child_node_name) {
		this.child_node_name = child_node_name;
	}

	public Long getChild_node_name_crc() {
		return child_node_name_crc;
	}

	public void setChild_node_name_crc(Long child_node_name_crc) {
		this.child_node_name_crc = child_node_name_crc;
	}

	public boolean isIs_primary() {
		return is_primary;
	}

	public void setIs_primary(boolean is_primary) {
		this.is_primary = is_primary;
	}

	public Long getAssoc_index() {
		return assoc_index;
	}

	public void setAssoc_index(Long assoc_index) {
		this.assoc_index = assoc_index;
	}


}

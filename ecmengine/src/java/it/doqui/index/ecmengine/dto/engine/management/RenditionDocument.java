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
 * Classe DTO che rappresenta una <i>rendition</i> da caricare sull'ECMENGINE.
 *
 * <p>Una <i>rendition</i> &egrave; caratterizzata da un nodeId, che identifica il nodo in cui di trova, e da una descrizione.
 * Viene associata ad un {@link RenditionTransformer} tramite l'Aspect <code>ecm-sys:renditionTransformer</code>.</p>
 *
 *
 * @author Doqui
 */
public class RenditionDocument extends Content{

	private static final long serialVersionUID = -3286234248661642785L;

	private String nodeId;
	private String description;

	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}

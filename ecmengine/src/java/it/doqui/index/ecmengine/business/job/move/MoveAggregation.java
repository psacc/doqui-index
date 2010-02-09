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
 
package it.doqui.index.ecmengine.business.job.move;

import java.io.Serializable;


public class MoveAggregation implements Serializable {

	private static final long serialVersionUID = 0L;
	
	String idDestinationParent; 
	String idSourceNode ;
	String destinationRepository ; 
	String sourceRepository;
	
	public MoveAggregation(){
	}

	public String getDestinationRepository() {
		return destinationRepository;
	}

	public void setDestinationRepository(String destinationRepository) {
		this.destinationRepository = destinationRepository;
	}

	public String getIdDestinationParent() {
		return idDestinationParent;
	}

	public void setIdDestinationParent(String idDestinationParent) {
		this.idDestinationParent = idDestinationParent;
	}

	public String getIdSourceNode() {
		return idSourceNode;
	}

	public void setIdSourceNode(String idSourceNode) {
		this.idSourceNode = idSourceNode;
	}

	public String getSourceRepository() {
		return sourceRepository;
	}

	public void setSourceRepository(String sourceRepository) {
		this.sourceRepository = sourceRepository;
	}
}
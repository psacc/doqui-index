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
 * Classe DTO che rappresenta una regola applicabile ad un nodo.
 * 
 * <p>
 * La regola &egrave; caratterizzata da:
 * <ul>
 * <li>un tipo: INBOUND (applicata ai nodi creati o che vengono spostati sotto
 * il nodo specificato), OUTBOUND (applicata ai nodi eliminati o che vengono
 * spostati dal nodo specificato sotto un altro nodo) o UPDATE (applicata ad un
 * qualsiasi nodo che sia stato modificato sotto il nodo specificato);</li>
 * <li>un flag che indica se la regola viene ereditata dai nodi figli.</li>
 * </ul>
 * </p>
 * 
 * @author Doqui
 */
public class Rule extends EcmEngineDto {

	private static final long serialVersionUID = 4926006653125340606L;

	private String type;

	private boolean applyToChildren;

	/**
	 * Restituisce {@code true} se i figli del nodo specificato ereditano la
	 * regola, {@code false} altrimenti.
	 * 
	 * @return {@code true} se i figli del nodo specificato ereditano la regola,
	 *         {@code false} altrimenti.
	 */
	public boolean isApplyToChildren() {
		return applyToChildren;
	}

	/**
	 * Indica se i figli del nodo specificato ereditano la regola.
	 * 
	 * @param applyToChildren
	 *            {@code true} se i figli del nodo specificato ereditano la
	 *            regola, {@code false} altrimenti.
	 */
	public void setApplyToChildren(boolean applyToChildren) {
		this.applyToChildren = applyToChildren;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}

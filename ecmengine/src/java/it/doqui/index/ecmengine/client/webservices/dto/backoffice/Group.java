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
 * DTO che rappresenta un gruppo dell'ECMENGINE dal punto di vista del backoffice.
 *
 * <p>Questo DTO fornisce tutte le informazioni necessarie ai
 * servizi di backoffice per creare un nuovo gruppo sul repository.</p>
 *
 * @author Doqui
 */
public class Group {



	private String nome;








	/**
	 * Restituisce il nome del gruppo.
	 *
	 * @return Il nome del gruppo.
	 */
	public String getName() {
		return nome;
	}

	/**
	 * Imposta il nome del gruppo.
	 *
	 * @param name Il nome del gruppo.
	 */
	public void setName(String name) {
		this.nome = name;
	}

}

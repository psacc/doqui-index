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
 
package it.doqui.index.ecmengine.business.publishing.util;

/* package */ class SliceableString {
	private String s;

	public SliceableString(String s) {
		this.s = s;
	}

	public SliceableString from(String delim) {
		int i = this.s.indexOf(delim);

		if (i >= 0 
				&& (i + delim.length()) < this.s.length()) {
			return new SliceableString(this.s.substring(i + delim.length()).trim());
		} else {
			return new SliceableString(this.s);
		}
	}

	public SliceableString to(String delim) {
		int i = this.s.indexOf(delim);

		if (i >= 0 
				&& i < this.s.length()) {
			return new SliceableString(this.s.substring(0, i).trim());
		} else {
			return new SliceableString(this.s);
		}
	}

	public String toString() {
		return s;
	}
}

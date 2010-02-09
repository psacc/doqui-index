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

import java.io.IOException;
import java.io.InputStream;

public class FileFormatInputStream extends InputStream {

	private InputStream is;

	private int available;

	public FileFormatInputStream(InputStream _is, int _available){
		is=_is;
		available=_available;
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	public int read(byte[] b) throws IOException{
		return is.read(b);
	}

	public int read(byte[] b,int off,int len) throws IOException{
		return is.read(b, off, len);
	}

	public long skip(long n) throws IOException{
		return is.skip(n);
	}

	public void close() throws IOException{
		is.close();
	}

	public void mark(int readlimit){
		is.mark(readlimit);
	}

	public void reset() throws IOException{
		is.reset();
	}

	public boolean markSupported(){
		return is.markSupported();
	}

	public int available(){
		return available;
	}

}

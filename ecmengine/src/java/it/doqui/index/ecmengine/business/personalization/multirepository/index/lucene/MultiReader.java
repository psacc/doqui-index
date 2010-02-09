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
 
package it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene;

import java.io.IOException;
import java.io.Reader;

/**
 * Implementazione che replica la logica gi&agrave; inclusa in {@code org.alfresco.repo.search.impl.lucene.MultiReader}. 
 * 
 * {@strong NB:} questa classe &egrave; necessaria perch&eacute; il costruttore di tale classe ha 
 * visibilit&acute; &quot;package&quot;.
 * 
 * @author DoQui
 */
public class MultiReader extends Reader {

	Reader first;

	Reader second;

	boolean firstActive = true;

	public MultiReader(Reader first, Reader second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public void close() throws IOException
	{
		IOException ioe = null;
		try
		{
			first.close();
		}
		catch (IOException e)
		{
			ioe = e;
		}

		second.close();
		if (ioe != null)
		{
			throw ioe;
		}

	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		synchronized (lock)
		{
			if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0))
			{
				throw new IndexOutOfBoundsException();
			}
			else if (len == 0)
			{
				return 0;
			}
			for(int i = 0; i < len; i++)
			{
				int c; 
				if(firstActive)
				{
					c = first.read();
					if(c == -1)
					{
						firstActive = false;
						c = second.read();
					}
				}
				else
				{
					c = second.read();
				}
				if(c == -1)
				{
					if(i == 0)
					{
						return -1; 
					}
					else
					{
						return i;
					}
				}
				else
				{
					cbuf[off+i] = (char)c;
				}
			}
			return len;
		}
	}
}

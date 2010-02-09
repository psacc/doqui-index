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
package it.doqui.index.ecmengine.business.personalization.splitting.index.lucene;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;

public class P7mHandler {
	// prende l'array di byte di un m7p e ritorna l'array di byte del file contenuto in esso
	public static byte[] sbusta(byte[] p7m_bytes) {
		byte[] byte_out=null;
		CMSSignedData cms=null;
		ByteArrayOutputStream out=null;
		try {
			cms = new CMSSignedData(p7m_bytes);
			CMSProcessable cmsp=cms.getSignedContent();
			if(cmsp!= null) {
				out = new ByteArrayOutputStream();
				cmsp.write(out);
				byte_out=out.toByteArray();
				out.close();
			}
		}catch(Exception e){
			byte_out=null;
		}finally{
			try{out.close();}catch(Exception e){}
		}
		return byte_out;
	}
}

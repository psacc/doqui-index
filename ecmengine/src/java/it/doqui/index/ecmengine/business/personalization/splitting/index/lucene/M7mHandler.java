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

public class M7mHandler {
	public static byte[] sbusta_m7m(byte[] byte_in){
		byte[] byte_out=null;
		int count=0;
		int inizio=0;

		try{
			for(int i=0; i<byte_in.length-3;i++){
				if(byte_in[i]==(byte)13 && byte_in[i+1]==(byte)10 && byte_in[i+2]==(byte)13 && byte_in[i+3]==(byte)10){
					if(count==1){
						inizio=i+4;
						count++;}
					else{
						count++;
					}
				}
			}

			int count2=0;
			int fine=0;
			if(inizio!=0){
				for(int i=inizio; i<byte_in.length-5;i++){
					if(byte_in[i]==(byte)'-'&&byte_in[i+1]==(byte)'-'&&byte_in[i+2]=='D'&&byte_in[i+3]=='i'&&byte_in[i+4]=='k'&&byte_in[i+5]=='e'){
						if(count2==1){
							fine=i-2;
							count2++;}
						else{
							count2++;
						}
					}
				}
				if(fine!=0){
					byte_out=new byte[fine-inizio];
					for(int i=0;i<byte_out.length;i++){
						byte_out[i]=byte_in[i+inizio];
					}
				}
			}
		}catch(Exception e){
			byte_out=null;
		}
		return byte_out;
	}
}

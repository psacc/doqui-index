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

package it.doqui.index.ecmengine.business.mimetype;

//import it.doqui.index.ecmengine.business.mimetype.dto.Mimetype;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.exception.repository.MimetypeRuntimeException;

/**
 * Interface che descrive le operazioni di riconoscimento del MIMEtype a partire
 * dall'estensione di un file.
 *
 * @author DoQui
 *
 */
public interface MimetypeBusinessInterface {
	/**
	 * 
	 * @param mimetype DTO con il campo fileExtension impostato con il valore dell'estensione da riconoscere.
	 * @return Un array di DTO contententi nel campo mimeType il MIMEtpye riconosciuto in base all'estensione.
	 * @throws MimetypeRuntimeException se avviene un errore imprevisto durante il tentativo di riconoscimento.
	 */
    public Mimetype[] getMimetype(Mimetype mimetype) throws MimetypeRuntimeException;

}

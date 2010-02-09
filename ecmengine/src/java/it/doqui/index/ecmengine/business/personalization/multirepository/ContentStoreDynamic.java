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

package it.doqui.index.ecmengine.business.personalization.multirepository;

public interface ContentStoreDynamic extends org.alfresco.repo.content.ContentStore
{
    /**
     * Imposta il resource da utilizzare in questo ContentStoreDynamic
     *
     * @param resource Il nome della risorsa da utilizzare. Questo
     * valore varia in base alla classe che ne fa uso: nei FileContentStoreDynamic
     * si tratta di una directory
     */
    public void setResource(String resource);

    /**
     * Imposta il protocollo da utilizzare in questo ContentStoreDynamic
     *
     * @param protocol Il nome del protocollo da utilizzare.
     * Tale valore e' definibile a piacere in base al ContentStoreDynamic utilizzato
     * Il protocol di default usato da ecmengine e' "store"
     */
    public void setProtocol(String protocol);

    /**
     * Restituisce il protocollo utilizzato dal ContentStoreDynamic
     *
     * @return Il valore del protocol
     */
    public String getProtocol();
}

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



public class ContentStoreDefinition {



    private String type;
    private String protocol;
    private String resource;


    /**
     * Restituisce il type del ContentStore
     *
     * @return String che identifica il type
     */
    public String getType() {
        return type;
    }

    /**
     * Imposta il type del ContentStore
     *
     * @param type del ContentStore
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Restituisce Il protocollo usato dal ContentStore
     *
     * @return Un vettore di ContentStoreDefinition
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Imposta Il protocollo usato dal ContentStore
     *
     * @param protocol Il protocollo utilizzato
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Restituisce la risorsa nella quale verranno memorizzati i dati relativi al ContentStore
     *
     * @return Una stringa identificante il percorso dei dati
     */
    public String getResource() {
        return resource;
    }

    /**
     * Imposta la risorsa nella quale verranno memorizzati i dati relativi al ContentStore
     *
     * @param resource la stringa identificante il percorso dei dati
     */
    public void setResource(String resource) {
        this.resource = resource;
    }
}

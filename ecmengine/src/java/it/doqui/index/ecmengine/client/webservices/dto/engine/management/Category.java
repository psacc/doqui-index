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

package it.doqui.index.ecmengine.client.webservices.dto.engine.management;


/**
 * Classe DTO che rappresenta una generica category nel repository dell'ECMENGINE.
 *
 * @author Doqui
 */

public class Category {



    private String name;
    private String aspectPrefixedName;

    /**
     * Costruttore predefinito.
     */
    public Category() {
        super();
        this.name = null;
        this.aspectPrefixedName = null;
    }

    /**
     * Restituisce il nome della category.
     * @return Il nome della category.
     */
    public String getName() {
        return name;
    }

    /**
     * Imposta il nome della category.
     * @param name Il nome della category.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Resituisce il nome dell'aspect associato alla category.
     * @see Aspect
     * @return Il nome dell'aspect associato alla category.
     */
    public String getAspectPrefixedName() {
        return aspectPrefixedName;
    }

    /**
     * Imposta il nome dell'aspect associato alla cateogry.
     * @see Aspect
     * @param aspectPrefixedName
     */
    public void setAspectPrefixedName(String aspectPrefixedName) {
        this.aspectPrefixedName = aspectPrefixedName;
    }

}

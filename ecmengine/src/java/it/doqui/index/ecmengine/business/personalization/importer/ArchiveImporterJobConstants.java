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

package it.doqui.index.ecmengine.business.personalization.importer;

/**
 * Interface che raggruppa le costanti utilizzate per la gestione del job di
 * importazione degli Archivi in repository (ArchiveImporter)
 *
 * @author DoQui
 * @see it.doqui.index.ecmengine.business.job.dto.BatchJob
 * @see it.doqui.index.ecmengine.business.job.dto.BatchJobParam
 *
 */
public interface ArchiveImporterJobConstants {

    /**
     * Identificatore dell'UID del nodo dove verranno messi i conutenuti dell'archive
     */
    String PARAM_UID                      = "UID"                    ;
    String PARAM_STORE_IDENTIFIER		  = "store_identifier"		 ;
    String PARAM_STORE_PROTOCOL			  = "store_protocol"		 ;

    /**
     * Identificatore dell'utente che effettua l'import
     */
    String PARAM_USER					  = "user"					 ;
    String PARAM_PASSWORD				  = "password"				 ;

    /**
     * Identificatore dei valori relativi al nodo da creare
     */
    String PARAM_CONTENT_TYPE             = "content_type"           ;
    String PARAM_CONTAINER_TYPE           = "container_type"         ;
    String PARAM_CONTENT_NAME_PROPERTY    = "content_name_property"  ;
    String PARAM_CONTAINER_NAME_PROPERTY  = "container_name_property";
    String PARAM_CONTAINER_ASSOC_TYPE     = "container_assoc_type"   ;
    String PARAM_PARENT_ASSOC_TYPE        = "parent_assoc_type"      ;

    /**
     * Identificatore del nome del file da importare
     */
    String PARAM_NAME                     = "name"                   ;

    /**
     * Identificatore della directory dove e' memorizzato il file da importare
     */
    String PARAM_CONTENTSTORE_DIR		  = "contentstore_dir"		 ;

    /**
     * Identificatore del nome del formato de lfile da importare
     */
    String PARAM_FORMAT                   = "format"                 ;

}

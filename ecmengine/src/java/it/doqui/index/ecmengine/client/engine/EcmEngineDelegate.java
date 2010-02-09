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

package it.doqui.index.ecmengine.client.engine;

import it.doqui.index.ecmengine.business.publishing.management.EcmEngineManagementBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.massive.EcmEngineMassiveBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.search.EcmEngineSearchBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.security.EcmEngineSecurityBusinessInterface;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

/**
 * <p>Interfaccia di delegate utilizzata per semplificare l'accesso ai
 * servizi esportati dall'ECMENGINE a livello del client.</p>
 *
 * <p>I metodi esportati sono definiti nella superinterfaccia
 * {@link EcmEngineManagementBusinessInterface}, da questa estesa.
 * L'implementazione predefinita &egrave; contenuta in
 * {@link EcmEngineDelegateImpl}.</p>
 *
 * <p><strong>NB:</strong> il costruttore della classe che implementa il
 * client delegate deve necessariamente ricevere come unico parametro di
 * ingresso un oggetto di tipo {@link org.apache.commons.logging.Log} che
 * deve utilizzare per scrivere il log.</p>
 *
 * @author Doqui
 *
 * @see EcmEngineManagementBusinessInterface
 */

public interface EcmEngineDelegate extends EcmEngineConstants, EcmEngineManagementBusinessInterface, EcmEngineSearchBusinessInterface, EcmEngineSecurityBusinessInterface, EcmEngineMassiveBusinessInterface {

}

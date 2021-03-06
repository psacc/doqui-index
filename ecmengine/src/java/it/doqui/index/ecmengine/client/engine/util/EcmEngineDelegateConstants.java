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

package it.doqui.index.ecmengine.client.engine.util;

import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.ecmengine.util.EcmEngineConstantsReader;

/**
 * Interfaccia che raccoglie le definizioni delle costanti
 * simboliche utilizzate dal client delegate del motore di
 * gestione e ricerca dei contenuti.
 *
 * @author Doqui
 *
 */
public interface EcmEngineDelegateConstants extends EcmEngineConstants {

	String EJB_EXTENSION=new EcmEngineConstantsReader().getEjbExtension();

	/**
	 * Category per il log del client delegate.
	 *
	 * <p>Il nome completo della category viene composto utilizzando
	 * il nome del chiamante come prefisso.</p>
	 *
	 * @see #ECMENGINE_DELEGATE_CALLER
	 */
	String ECMENGINE_DELEGATE_LOG_CATEGORY = ".ecmengine.delegate";

	/**
	 * Nome della propriet&agrave; (in {@code ecmengine-engine-delegate.properties})
	 * che definisce la classe di implementazione del client delegate da
	 * caricare.
	 */
	String ECMENGINE_DELEGATE_CLASS = "delegateClass";

	/**
	 * Nome della propriet&agrave; (in {@code ecmengine-engine-delegate.properties})
	 * che definisce il context factory su cui fare binding.
	 */
	String ECMENGINE_CONTEXT_FACTORY = "context_factory";

	/**
	 * Nome della propriet&agrave; (in {@code ecmengine-engine-delegate.properties})
	 * che definisce l'url su cui fare binding.
	 */
	String ECMENGINE_URL_TO_CONNECT = "url_to_connect";

	/**
	 * Nome della propriet&agrave; (in {@code ecmengine-engine-delegate.properties})
	 * che definisce la partizione del cluster su cui fare binding per i servizi
	 * (Per la configurazione non in cluster non valorizzare la proprieta').
	 */
	String ECMENGINE_CLUSTER_PARTITION = "cluster_partition";

	/**
	 * Nome della propriet&agrave; (in {@code ecmengine-engine-delegate.properties})
	 * che definisce se deve essere attivato il discovery sulle partizioni
	 * (La proprieta' viene ignorata nel caso in cui la propriet&agrave; ECMENGINE_CLUSTER_PARTITION
	 * non e' valorizzata)
	 * Possibili valori : true se si vuole disabilitare il discovery, false se lo si vuole abilitare.
	 */
	String ECMENGINE_DISABLE_DISCOVERY = "disable_discovery";

	/**
	 * Nome della classe di implementazione del business delegate
	 * da caricare se non va a buon fine il caricamento della classe
	 * specificata nel file di propriet&agrave;
	 * ({@code ecmengine-engine-delegate.properties}).
	 */
	String ECMENGINE_DELEGATE_CLASS_NAME_DEFAULT = "it.doqui.index.ecmengine.client.engine.EcmEngineDelegateImpl";

	/**
	 * Nome della propriet&agrave; (in {@code ecmengine-engine-delegate.properties})
	 * che definisce il nome del chiamante del client delegate.
	 *
	 * <p>Il nome del chiamante viene utilizzato, assieme a
	 * {@link #ECMENGINE_DELEGATE_LOG_CATEGORY}, per comporre la category
	 * del logger del client delegate.</p>
	 */
	String ECMENGINE_DELEGATE_CALLER = "caller";

	/**
	 * Nome del file di propriet&agrave; contenente la configurazione
	 * del client delegate.
	 *
	 * <p>Questo file deve essere presente nella root del pacchetto
	 * client che utilizza il delegate.</p>
	 */
	String ECMENGINE_PROPERTIES_FILE = "ecmengine-engine-delegate";

	/**
	 * File contenente la configurazione dei servizi fruibili attraverso il framework C.S.I.
	 * (Cooperative Systems Infrastructure) per le funzionalit&agrave; di gestione dei contenuti.
	 *
	 * <p>Il fruitore di un servizio dovr&agrave; modificare secondo
	 * le proprie esigenze la sezione seguente:</p>
	 *
	 * <code>&lt;property name="java.naming.provider.url"<br>
	 * &nbsp;&nbsp;value="@@url_to_connect@@" /&gt;<br>
	 * &lt;property name="java.naming.factory.initial"<br>
	 * &nbsp;&nbsp;value="@@context_factory@@" /&gt;</code>
	 *
	 * <p><code>&#64;&#64;url_to_connect&#64;&#64;</code> deve essere
	 * sostituito con l'URL a cui &egrave; stata deployata la componente
	 * ECMENGINE</p>
	 * <p><code>&#64;&#64;context_factory&#64;&#64;</code> deve essere
	 * sostituito con la <i>Context Factory</i> da utilizzare
	 * (per JBoss sar&agrave; {@code org.jnp.interfaces.NamingContextFactory}).</p>
	 *
	 */
	String ECMENGINE_MANAGEMENT_CONFIG_FILE = "/pd_ecmenginemanagement.xml";

	/** Nome dell'EJB ManagementBean su cui fare lookup. */
	String ECMENGINE_MANAGEMENT_JNDI_NAME = "ecmengine/ejb/ManagementBean"+EJB_EXTENSION;

	/**
	 * File contenente la configurazione dei servizi fruibili attraverso il framework C.S.I.
	 * (Cooperative Systems Infrastructure) per le funzionalit&agrave; di ricerca.
	 *
	 * <p>Il fruitore di un servizio dovr&agrave; modificare secondo
	 * le proprie esigenze la sezione seguente:</p>
	 *
	 * <code>&lt;property name="java.naming.provider.url"<br>
	 * &nbsp;&nbsp;value="@@url_to_connect@@" /&gt;<br>
	 * &lt;property name="java.naming.factory.initial"<br>
	 * &nbsp;&nbsp;value="@@context_factory@@" /&gt;</code>
	 *
	 * <p><code>&#64;&#64;url_to_connect&#64;&#64;</code> deve essere
	 * sostituito con l'URL a cui &egrave; stata deployata la componente
	 * ECMENGINE</p>
	 * <p><code>&#64;&#64;context_factory&#64;&#64;</code> deve essere
	 * sostituito con la <i>Context Factory</i> da utilizzare
	 * (per JBoss sar&agrave; {@code org.jnp.interfaces.NamingContextFactory}).</p>
	 *
	 */
	String ECMENGINE_SEARCH_CONFIG_FILE = "/pd_ecmenginesearch.xml";

	/** Nome dell'EJB SearchBean su cui fare lookup. */
	String ECMENGINE_SEARCH_JNDI_NAME = "ecmengine/ejb/SearchBean"+EJB_EXTENSION;

	/**
	 * File contenente la configurazione dei servizi fruibili attraverso il framework C.S.I.
	 * (Cooperative Systems Infrastructure) per le funzionalit&agrave; di security.
	 *
	 * <p>Il fruitore di un servizio dovr&agrave; modificare secondo
	 * le proprie esigenze la sezione seguente:</p>
	 *
	 * <code>&lt;property name="java.naming.provider.url"<br>
	 * &nbsp;&nbsp;value="@@url_to_connect@@" /&gt;<br>
	 * &lt;property name="java.naming.factory.initial"<br>
	 * &nbsp;&nbsp;value="@@context_factory@@" /&gt;</code>
	 *
	 * <p><code>&#64;&#64;url_to_connect&#64;&#64;</code> deve essere
	 * sostituito con l'URL a cui &egrave; stata deployata la componente
	 * ECMENGINE</p>
	 * <p><code>&#64;&#64;context_factory&#64;&#64;</code> deve essere
	 * sostituito con la <i>Context Factory</i> da utilizzare
	 * (per JBoss sar&agrave; {@code org.jnp.interfaces.NamingContextFactory}).</p>
	 *
	 */
	String ECMENGINE_SECURITY_CONFIG_FILE = "/pd_ecmenginesecurity.xml";

	/** Nome dell'EJB SecurityBean su cui fare lookup. */
	String ECMENGINE_SECURITY_JNDI_NAME = "ecmengine/ejb/SecurityBean"+EJB_EXTENSION;

	/**
	 * File contenente la configurazione dei servizi fruibili attraverso il framework C.S.I.
	 * (Cooperative Systems Infrastructure) per le funzionalit&agrave; di servizi massivi.
	 *
	 * <p>Il fruitore di un servizio dovr&agrave; modificare secondo
	 * le proprie esigenze la sezione seguente:</p>
	 *
	 * <code>&lt;property name="java.naming.provider.url"<br>
	 * &nbsp;&nbsp;value="@@url_to_connect@@" /&gt;<br>
	 * &lt;property name="java.naming.factory.initial"<br>
	 * &nbsp;&nbsp;value="@@context_factory@@" /&gt;</code>
	 *
	 * <p><code>&#64;&#64;url_to_connect&#64;&#64;</code> deve essere
	 * sostituito con l'URL a cui &egrave; stata deployata la componente
	 * ECMENGINE</p>
	 * <p><code>&#64;&#64;context_factory&#64;&#64;</code> deve essere
	 * sostituito con la <i>Context Factory</i> da utilizzare
	 * (per JBoss sar&agrave; {@code org.jnp.interfaces.NamingContextFactory}).</p>
	 *
	 */
	String ECMENGINE_MASSIVE_CONFIG_FILE = "/pd_ecmenginemassive.xml";

	/** Nome dell'EJB MassiveBean su cui fare lookup. */
	String ECMENGINE_MASSIVE_JNDI_NAME = "ecmengine/ejb/MassiveBean"+EJB_EXTENSION;
}

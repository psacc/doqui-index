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
 
package it.doqui.index.ecmengine.dto.engine.management;

import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * Classe DTO che rappresenta la chiave utilizzata per cifrare/decifrare un generico
 * content nel repository dell'ECMENGINE.
 *  
 * @author Doqui
 */
public class EncryptionInfo extends EcmEngineDto{

	private static final long serialVersionUID = -3149944851073548314L;
	private String key;
	private String algorithm;
	private String padding;
	private String mode;
	private String keyId;
	private String sourceIV;
	private boolean sourceEncrypted;
	private boolean corruptedEncryptionInfo;
	
	/**
	 * Costruttore predefinito.
	 */
	public EncryptionInfo() {
		
		super();
		key = null;		
	}
	
	/**
	 * Costruisce una nuova istanza di {@code EncryptionInfo} a partire 
	 * dalla chiave di cifratura.
	 * 
	 * @param key La chiave utilizzata per la cifratura.
	 */
	public EncryptionInfo(String key) {
		
		super();
		this.key = key;		
	}
	
	/**
	 * Restituisce la chiave utilizzara per la cifratura.
	 * 
	 * @return La chiave utilizzata per la cifratura.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Imposta la chiave utilizzata per la cifratura.
	 * 
	 * @param key La chiave utilizzata per la cifratura.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Restituisce l'algoritmo utilizzato per la cifratura.
	 * 
	 * @return L'algoritmo utilizzato per la cifratura.
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Imposta l'algoritmo da utilizzare per la cifratura.
	 * 
	 * @param algorithm L'algoritmo da utilizzare per la cifratura.
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Restituisce il tipo di padding utilizzato per la cifratura.
	 * 
	 * @return Il tipo di padding utilizzato per la cifratura.
	 */
	public String getPadding() {
		return padding;
	}

	/**
	 * Imposta il tipo di padding da utilizzare per la cifratura.
	 * 
	 * @param padding Il tipo di padding da utilizzare per la cifratura.
	 */
	public void setPadding(String padding) {
		this.padding = padding;
	}

	/**
	 * Restituisce la modalit&agrave; di cifratura.
	 * 
	 * @return La modalit&agrave; di cifratura.
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Imposta la modalit&agrave; di cifratura.
	 * 
	 * @param mode La modalit&agrave; di cifratura.
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Restituisce l'identificativo della chiave crittografica utilizzata.
	 * 
	 * @return L'identificativo della chiave crittografica utilizzata.
	 */
	public String getKeyId() {
		return keyId;
	}

	/**
	 * Imposta l'identificativo della chiave crittografica utilizzata.
	 * 
	 * @param keyId L'identificativo della chiave crittografica utilizzata.
	 */
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	/**
	 * Verifica se il contenuto &egrave; criptato alla fonte.
	 * 
	 * @return {@code true} se il contenuto &egrave; criptato alla fonte, 
	 * {@code false} altrimenti.
	 */
	public boolean isSourceEncrypted() {
		return sourceEncrypted;
	}

	/**
	 * Imposta il flag che indica se il contenuto &egrave; criptato alla fonte.
	 * 
	 * @param sourceEncrypted {@code true} se il contenuto &egrave; criptato alla fonte, 
	 * {@code false} altrimenti.
	 */
	public void setSourceEncrypted(boolean sourceEncrypted) {
		this.sourceEncrypted = sourceEncrypted;
	}

	/**
	 * Restituisce l'<i>Initialization Vector</i> utilizzato per criptare il contenuto
	 * alla fonte.
	 * 
	 * <p><strong>NB:</strong> il valore &egrave; restituito sottoforma di stringa codificata 
	 * in formato Base64.</p>
	 * 
	 * @return L'Initialization Vector.
	 */
	public String getSourceIV() {
		return sourceIV;
	}

	/**
	 * Imposta l'<i>Initialization Vector</i> utilizzato per criptare il contenuto 
	 * alla fonte.
	 * 
	 * <p><strong>NB:</strong> il valore deve essere passato come stringa codificata in formato
	 * Base64.</p>
	 * 
	 * @param sourceIV L'Initialization Vector.
	 */
	public void setSourceIV(String sourceIV) {
		this.sourceIV = sourceIV;
	}

	/**
	 * Restituisce lo stato di validit&agrave; delle informazioni di criptazione associate
	 * ad un contenuto.
	 * 
	 * <p><strong>NB:</strong> questo campo ha significato solo informativo (viene valorizzato, ad esempio,
	 * nelle operazioni di lettura dei metadati). <i>Questo campo viene ignorato durante le operazioni di
	 * caricamento o modifica.</i></p>
	 * 
	 * @return {@code true} se le informazioni sono corrotte o incomplete, {@code false} se sono
	 * valide.
	 */
	public boolean isCorruptedEncryptionInfo() {
		return corruptedEncryptionInfo;
	}

	/**
	 * Imposta lo stato di validit&agrave; delle informazioni di criptazione associate 
	 * ad un contenuto.
	 * 
	 * <p><strong>NB:</strong> questo campo ha significato solo informativo (viene valorizzato, ad esempio,
	 * nelle operazioni di lettura dei metadati). <i>Questo campo viene ignorato durante le operazioni di
	 * caricamento o modifica.</i></p>
	 * 
	 * @param corruptedEncryptionInfo {@code true} se le informazioni sono corrotte o incomplete, 
	 * {@code false} se sono valide.
	 */
	public void setCorruptedEncryptionInfo(boolean corruptedEncryptionInfo) {
		this.corruptedEncryptionInfo = corruptedEncryptionInfo;
	}
}

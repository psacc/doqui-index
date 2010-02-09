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

package it.doqui.index.ecmengine.dto.engine.security;

/**
 * DTO che rappresenta la firma digitale di un documento firmato.
 * @author Doqui
 */
import it.doqui.index.ecmengine.dto.EcmEngineDto;
import java.util.Date;

public class Signature extends EcmEngineDto{
	private static final long serialVersionUID = 1188310304098182592L;

	private String nominativoFirmatario;
    private String organizzazione;
    private String ca;
    private String dipartimento;
    private String paese;
    private long tipoFirma;
    private long giornoFirma;
    private long oraFirma;
    private long annoFirma;
    private long meseFirma;
    private long minutiFirma;
    private long secondiFirma;
    private String serialNumber;
    private String surname;
    private String givenname;
    private String dnQualifier;
    private int errorCode;

    // Dalla versione 1.3.0
    private boolean timestamped;
    private String inizioValidita;
    private String fineValidita;
    private String codiceFiscale;
    private long numeroControfirme;
	// Array delle controfirme della firma
    private Signature[] signature;
    private byte[] cert;

    // Dalla versione 7.2 di Ecmengine
	private Date dataOra;
    private String firmatario;

	public Signature() {
		super();
	}

	/**
	 * Restituisce l'anno della firma.
	 * @return L''anno della firma.
	 */
	public long getAnnoFirma() {
		return annoFirma;
	}

	/**
	 * Imposta l'anno della firma.
	 * @param annoFirma L'anno della firma.
	 */
	public void setAnnoFirma(long annoFirma) {
		this.annoFirma = annoFirma;
	}

	/**
	 * Restituisce il certification authority.
	 * @return Il certification authority.
	 */
	public String getCa() {
		return ca;
	}

	/**
	 * Imposta il certification authority.
	 * @param ca Il certification authority.
	 */
	public void setCa(String ca) {
		this.ca = ca;
	}

	/**
	 * Restituisce il dipartimento.
	 * @return Il dipartimento.
	 */
	public String getDipartimento() {
		return dipartimento;
	}

	/**
	 * Imposta il dipartimento.
	 * @param dipartimento Il dipartimento.
	 */
	public void setDipartimento(String dipartimento) {
		this.dipartimento = dipartimento;
	}

	/**
	 * Restituisce il giorno della firma.
	 * @return Il giorno della firma.
	 */
	public long getGiornoFirma() {
		return giornoFirma;
	}

	/**
	 * Imposta il giorno della firma.
	 * @param giornoFirma Il giorno della firma.
	 */
	public void setGiornoFirma(long giornoFirma) {
		this.giornoFirma = giornoFirma;
	}

	/**
	 * Restituisce il mese della firma.
	 * @return Il mese della firma.
	 */
	public long getMeseFirma() {
		return meseFirma;
	}

	/**
	 * Imposta il mese della firma.
	 * @param meseFirma Il mese della firma.
	 */
	public void setMeseFirma(long meseFirma) {
		this.meseFirma = meseFirma;
	}

	/**
	 * Restituisce i minuti della firma.
	 * @return I minuti della firma.
	 */
	public long getMinutiFirma() {
		return minutiFirma;
	}

	/**
	 * Imposta i minuti della firma.
	 * @param minutiFirma I minuti della firma.
	 */
	public void setMinutiFirma(long minutiFirma) {
		this.minutiFirma = minutiFirma;
	}

	/**
	 * Restituisce il nominativo del firmatario.
	 * @return Il nominativo del firmatario.
	 */
	public String getNominativoFirmatario() {
		return nominativoFirmatario;
	}

	/**
	 * Imposta il nominativo del firmatario.
	 * @param nominativoFirmatario Il nominativo del firmatario.
	 */
	public void setNominativoFirmatario(String nominativoFirmatario) {
		this.nominativoFirmatario = nominativoFirmatario;
	}

	/**
	 * Restituisce l'ora della firma.
	 * @return L'ora della firma.
	 */
	public long getOraFirma() {
		return oraFirma;
	}

	/**
	 * Imposta l'ora della firma.
	 * @param oraFirma L'ora della firma.
	 */
	public void setOraFirma(long oraFirma) {
		this.oraFirma = oraFirma;
	}

	/**
	 * Restituisce l'organizzazione del firmatario.
	 * @return L'organizzazione del firmatario.
	 */
	public String getOrganizzazione() {
		return organizzazione;
	}

	/**
	 * Imposta l'organizzazione del firmatario.
	 * @param organizzazione L'organizzazione del firmatario.
	 */
	public void setOrganizzazione(String organizzazione) {
		this.organizzazione = organizzazione;
	}

	/**
	 * Restituisce il paese del firmatario.
	 * @return Il paese del firmatario.
	 */
	public String getPaese() {
		return paese;
	}

	/**
	 * Imposta il paese del firmatario.
	 * @param paese Il paese del firmatario.
	 */
	public void setPaese(String paese) {
		this.paese = paese;
	}

	/**
	 * Restituisce i secondi della firma.
	 * @return I secondi della firma.
	 */
	public long getSecondiFirma() {
		return secondiFirma;
	}

	/**
	 * Imposta i secondi della firma.
	 * @param secondiFirma I secondi della firma.
	 */
	public void setSecondiFirma(long secondiFirma) {
		this.secondiFirma = secondiFirma;
	}

	/**
	 * Restituisce il tipo della firma.
	 * @return Il tipo della firma.
	 */
	public long getTipoFirma() {
		return tipoFirma;
	}

	/**
	 * Imposta il tipo della firma.
	 * @param tipoFirma Il tipo della firma.
	 */
	public void setTipoFirma(long tipoFirma) {
		this.tipoFirma = tipoFirma;
	}

	/**
	 * Restituisce il serial number del firmatario.
	 * @return il serial number del firmatario.
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Imposta il serial number del firmatario.
	 * @param serialNumber Il serial number del firmatario.
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * Restituisce il cognome del firmatario.
	 * @return Il cognome del firmatario.
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * Imposta il cognome del firmatario.
	 * @param surname Il cognome del firmatario.
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * Restituisce il nome del firmatario.
	 * @return Il nome del firmatario.
	 */
	public String getGivenname() {
		return givenname;
	}

	/**
	 * Imposta il nome del firmatario.
	 * @param givenname Il nome del firmatario.
	 */
	public void setGivenname(String givenname) {
		this.givenname = givenname;
	}

	/**
	 * Restituisce il dnqualifier del firmatario.
	 * @return Il dnqualifier del firmatario.
	 */
	public String getDnQualifier() {
		return dnQualifier;
	}

	/**
	 * Imposta il dnqualifier del firmatario.
	 * @param dnQualifier Il dnqualifier del firmatario.
	 */
	public void setDnQualifier(String dnQualifier) {
		this.dnQualifier = dnQualifier;
	}

	/**
	 * Verifica se la firma e' predisposta di una marcatura temporale.
	 * @return true, se la firma e' predisposta di una marcatura temporale, false altrimenti.
	 */
    public boolean isTimestamped()    {
        return timestamped;
    }

    /**
     * Imposta se la firma è predisposta di una marcatura temporale.
     * @param timestamped
     */
    public void setTimestamped(boolean timestamped)    {
        this.timestamped = timestamped;
    }

    /**
     * Restituisce la data di inizio di validit&agrave della firma.
     * @return La data di inizio di validit&agrave della firma.
     */
    public String getInizioValidita()    {
        return inizioValidita;
    }

    /**
     * Imposta la data di inizio di validit&agrave della firma.
     * @param inizioValidita La data di inizio di validit&agrave della firma.
     */
    public void setInizioValidita(String inizioValidita)    {
        this.inizioValidita = inizioValidita;
    }

    /**
     * Restituisce la data di fine di validit&agrave della firma.
     * @return La data di fine di validit&agrave della firma.
     */
    public String getFineValidita()    {
        return fineValidita;
    }

    /**
     * Imposta la data di fine di validit&agrave della firma.
     * @param fineValidita La data di fine di validit&agrave della firma.
     */
    public void setFineValidita(String fineValidita)    {
        this.fineValidita = fineValidita;
    }

    /**
     * Restituisce il codice fiscale del firmatario.
     * @return Il codice fiscale del firmatario.
     */
    public String getCodiceFiscale()    {
        return codiceFiscale;
    }

    /**
     * Imposta il codice fiscale del firmatario.
     * @param codiceFiscale Il codice fiscale del firmatario.
     */
    public void setCodiceFiscale(String codiceFiscale)    {
        this.codiceFiscale = codiceFiscale;
    }

    /**
     * Restituisce il numero di controfirme del documento firmato.
     * @return Il numero di controfirme del documento firmato.
     */
    public long getNumeroControfirme()    {
        return numeroControfirme;
    }

    /**
     * Imposta il numero di controfirme del documento firmato.
     * @param numeroControfirme Il numero di controfirme del documento firmato.
     */
    public void setNumeroControfirme(long numeroControfirme)    {
        this.numeroControfirme = numeroControfirme;
    }

	/**
	 * Restituisce l'array di controfirme della firma, comprensivo di eventuali errori
	 * di sbustamento.
	 * @return l'array di signature.
	 */
	public Signature[] getSignature() {
		return signature;
	}

	/**
	 * Imposta l'array di confirme del documento.
	 * @param signature l'array di firme.
	 */
	public void setSignature( Signature[] signature ) {
		this.signature = signature;
	}

	/**
	 * Restituisce il certificato di firma digitale.
	 * @return Il certificato di firma digitale.
	 */
	public byte[] getCert() {
		return cert;
	}

	/**
	 * Imposta il certificato di firma digitale.
	 * @param cert Il certificato di firma digitale.
	 */
	public void setCert( byte[] cert ) {
		this.cert = cert;
	}

	/**
	 * Restituisce l'eventuale codice di errore della verifica della firma.
	 * @return Il codice di errore della verifica della firma.
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Imposta l'eventuale codice di errore della verifica della firma.
	 * @param errorCode Il codice di errore della verifica della firma.
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Restiruisce la data e l'ora della firma.
	 * @return data e ora della firma.
	 */
	public Date getDataOra() {
		return dataOra;
	}

	/**
	 * Imposta la data e l'ora della firma.
	 * @param dataOra data e ora della firma.
	 */
	public void setDataOra(Date dataOra) {
		this.dataOra = dataOra;
	}

	/**
	 * Restituisce il firmatario.
	 * @return il firmatario.
	 */
	public String getFirmatario() {
		return firmatario;
	}

	/**
	 * Imposta il firmatario.
	 * @param firmatario il firmatario.
	 */
	public void setFirmatario(String firmatario) {
		this.firmatario = firmatario;
	}
}

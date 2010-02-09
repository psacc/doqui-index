package it.doqui.index.ecmengine.business.job.backup.util;

import java.util.Date;


public class MetaDatiAutoreValore {
  protected String cognome;

  protected String nome;

  protected Date dataInizio;

  protected Date dataFine;


  public String getCognome() {
    return this.cognome;
  }

  public void setCognome(String cognome) {
    this.cognome = cognome;
  }

  public String getNome() {
    return this.nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public Date getDataInizio() {
    return this.dataInizio;
  }

  public void setDataInizio(Date dataInizio) {
    this.dataInizio = dataInizio;
  }

  public Date getDataFine() {
    return this.dataFine;
  }

  public void setDataFine(Date dataFine) {
    this.dataFine = dataFine;
  }

}

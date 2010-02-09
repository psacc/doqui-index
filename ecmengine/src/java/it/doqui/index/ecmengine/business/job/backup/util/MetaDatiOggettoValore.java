package it.doqui.index.ecmengine.business.job.backup.util;

import java.util.Date;


public class MetaDatiOggettoValore {
  protected String campo;

  protected Date dataInizio;

  protected Date dataFine;


  public String getCampo() {
    return this.campo;
  }

  public void setCampo(String campo) {
    this.campo = campo;
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

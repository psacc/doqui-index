package it.doqui.index.ecmengine.business.job.backup.util;

import java.util.ArrayList;
import java.util.ArrayList;


public class MetaDati {
  public void addOggetto(MetaDatiOggettoValore oggetto) {
    oggettoList.add(oggetto);
  }

  public MetaDatiOggettoValore getOggetto(int index) {
    return (MetaDatiOggettoValore)oggettoList.get( index );
  }

  public int sizeOggettoList() {
    return oggettoList.size();
  }

  public void addAutore(MetaDatiAutoreValore autore) {
    autoreList.add(autore);
  }

  public MetaDatiAutoreValore getAutore(int index) {
    return (MetaDatiAutoreValore)autoreList.get( index );
  }

  public int sizeAutoreList() {
    return autoreList.size();
  }

  protected ArrayList oggettoList = new ArrayList();

  protected ArrayList autoreList = new ArrayList();

}

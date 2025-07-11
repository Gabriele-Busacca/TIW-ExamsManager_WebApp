package it.polimi.tiw.tiwproject2024purehtml.beans;

import java.sql.Timestamp;

public class VerbaleInfo {
    private int idVerbale;
    private int idAppello;
    private Timestamp dataOra;
    private String nomeCorso;

    // Getters e setters
    public int getIdVerbale() { return idVerbale; }
    public void setIdVerbale(int idVerbale) { this.idVerbale = idVerbale; }

    public int getIdAppello() { return idAppello; }
    public void setIdAppello(int idAppello) { this.idAppello = idAppello; }

    public Timestamp getDataOra() { return dataOra; }
    public void setDataOra(Timestamp dataOra) { this.dataOra = dataOra; }

    public String getNomeCorso() { return nomeCorso; }
    public void setNomeCorso(String nomeCorso) { this.nomeCorso = nomeCorso; }
}


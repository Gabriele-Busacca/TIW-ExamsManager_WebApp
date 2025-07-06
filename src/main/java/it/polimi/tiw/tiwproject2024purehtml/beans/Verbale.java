package it.polimi.tiw.tiwproject2024purehtml.beans;

import java.sql.Timestamp;

public class Verbale {
    private int idVerbale;
    private Timestamp data_ora;
    private int idAppello;

    public int getIdVerbale() {
        return idVerbale;
    }
    public void setIdVerbale(int idVerbale) {
        this.idVerbale = idVerbale;
    }

    public Timestamp getData_ora() {
        return data_ora;
    }
    public void setData_ora(Timestamp data_ora) {
        this.data_ora = data_ora;
    }

    public int getIdAppello() {
        return idAppello;
    }
    public void setIdAppello(int idAppello) {
        this.idAppello = idAppello;
    }
}

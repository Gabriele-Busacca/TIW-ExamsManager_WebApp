package it.polimi.tiw.tiwproject2024purehtml.beans;

import java.time.LocalDate;
import java.util.Date;

public class Appello {
    private int idAppello;
    private int idCorso;
    private Date data;

    public int getIdAppello() {
        return idAppello;
    }
    public void setIdAppello(int idAppello) {
        this.idAppello = idAppello;
    }

    public int getIdCorso() {
        return idCorso;
    }
    public void setIdCorso(int idCorso) {
        this.idCorso = idCorso;
    }

    public Date getData() {
        return data;
    }
    public void setData(Date data) {
        this.data = data;
    }
}

package it.polimi.tiw.tiwproject2024purehtml.beans;

import java.time.LocalDate;

public class Appello {
    private int idAppello;
    private int idCorso;
    private LocalDate data;

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

    public LocalDate getData() {
        return data;
    }
    public void setData(LocalDate data) {
        this.data = data;
    }
}

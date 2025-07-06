package it.polimi.tiw.tiwproject2024purehtml.beans;

public class Studente extends Utente {
    private int matricola;
    private String corsoLaurea;

    public int getMatricola() {
        return matricola;
    }
    public void setMatricola(int matricola) {
        this.matricola = matricola;
    }

    public String getCorsoLaurea() {
        return corsoLaurea;
    }
    public void setCorsoLaurea(String corsoLaurea) {
        this.corsoLaurea = corsoLaurea;
    }
}

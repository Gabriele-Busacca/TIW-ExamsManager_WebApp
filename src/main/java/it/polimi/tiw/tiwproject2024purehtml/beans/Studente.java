package it.polimi.tiw.tiwproject2024purehtml.beans;

public class Studente extends Utente {
    private int matricola;
    private String corso_laurea;

    public int getMatricola() {
        return matricola;
    }
    public void setMatricola(int matricola) {
        this.matricola = matricola;
    }

    public String getCorso_laurea() {
        return corso_laurea;
    }
    public void setCorso_laurea(String corso_laurea) {
        this.corso_laurea = corso_laurea;
    }
}

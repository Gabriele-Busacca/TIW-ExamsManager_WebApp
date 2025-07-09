package it.polimi.tiw.tiwproject2024purehtml.beans;

public class DettaglioIscrizioneStudente {
    private int matricola;
    private String nome;
    private String cognome;
    private String email;
    private String corso_laurea;
    private Voto voto;
    private StatoValutazione stato;

    public int getMatricola() {
        return matricola;
    }
    public void setMatricola(int matricola) {
        this.matricola = matricola;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getCorso_laurea() {
        return corso_laurea;
    }
    public void setCorso_laurea(String corso_laurea) {
        this.corso_laurea = corso_laurea;
    }

    public Voto getVoto() {
        return voto;
    }
    public void setVoto(Voto voto) {
        this.voto = voto;
    }

    public StatoValutazione getStato() {
        return stato;
    }
    public void setStato(StatoValutazione stato) {
        this.stato = stato;
    }

}

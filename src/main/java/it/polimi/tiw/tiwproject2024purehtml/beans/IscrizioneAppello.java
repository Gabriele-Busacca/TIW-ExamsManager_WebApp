package it.polimi.tiw.tiwproject2024purehtml.beans;

public class IscrizioneAppello {
    private String idStudente;
    private String idAppello;
    private Voto voto;
    private StatoValutazione stato;

    public String getIdStudente() {
        return idStudente;
    }
    public void setIdStudente(String idStudente) {
        this.idStudente = idStudente;
    }

    public String getIdAppello() {
        return idAppello;
    }
    public void setIdAppello(String idAppello) {
        this.idAppello = idAppello;
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

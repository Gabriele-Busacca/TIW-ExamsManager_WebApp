package it.polimi.tiw.tiwproject2024purehtml.beans;

public enum StatoValutazione {
    NON_INSERITO("non inserito", 0),
    INSERITO("inserito", 1),
    PUBBLICATO("pubblicato", 2),
    RIFIUTATO("rifiutato", 3),
    VERBALIZZATO("verbalizzato", 4);

    private final String label;
    private final int ordine;

    StatoValutazione(String label, int ordine) {
        this.label = label;
        this.ordine = ordine;
    }

    public String getLabel() {
        return label;
    }

    public int getOrdine() {
        return ordine;
    }

    public static StatoValutazione fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NON_INSERITO;
        }
        for (StatoValutazione stato : values()) {
            if (stato.label.equalsIgnoreCase(value.trim())) {
                return stato;
            }
        }
        throw new IllegalArgumentException("Stato non riconosciuto: " + value);
    }
}

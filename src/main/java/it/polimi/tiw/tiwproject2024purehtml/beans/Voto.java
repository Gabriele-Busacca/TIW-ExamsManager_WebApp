package it.polimi.tiw.tiwproject2024purehtml.beans;

public enum Voto {
    NESSUN_VOTO("", 0),
    ASSENTE("assente", 1),
    RIMANDATO("rimandato", 2),
    RIPROVATO("riprovato", 3),
    DICIOTTO("18", 4),
    DICIANNOVE("19", 5),
    VENTI("20", 6),
    VENTUNO("21", 7),
    VENTIDUE("22", 8),
    VENTITRE("23", 9),
    VENTIQUATTRO("24", 10),
    VENTICINQUE("25", 11),
    VENTISEI("26", 12),
    VENTISETTE("27", 13),
    VENTOTTO("28", 14),
    VENTINOVE("29", 15),
    TRENTA("30", 16),
    TRENTA_E_LODE("30 e lode", 17);

    private final String label;
    private final int ordine;

    Voto(String label, int ordine) {
        this.label = label;
        this.ordine = ordine;
    }

    public String getLabel() {
        return label;
    }

    public int getOrdine() {
        return ordine;
    }

    public static Voto fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NESSUN_VOTO;
        }
        for (Voto v : Voto.values()) {
            if (v.label.equalsIgnoreCase(value.trim())) {
                return v;
            }
        }
        throw new IllegalArgumentException("Voto non riconosciuto: " + value);
    }
}

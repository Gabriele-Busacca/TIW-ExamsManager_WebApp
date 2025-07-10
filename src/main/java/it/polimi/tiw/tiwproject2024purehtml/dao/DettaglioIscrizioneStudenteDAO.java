package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.DettaglioIscrizioneStudente;
import it.polimi.tiw.tiwproject2024purehtml.beans.StatoValutazione;
import it.polimi.tiw.tiwproject2024purehtml.beans.Voto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DettaglioIscrizioneStudenteDAO {
    private final Connection connection;

    public DettaglioIscrizioneStudenteDAO(Connection connection) {
        this.connection = connection;
    }

    public DettaglioIscrizioneStudente getStudenteIscritto(int idAppello, int matricola) throws SQLException {
        DettaglioIscrizioneStudente dettaglio = null;

        String query = """
        SELECT s.matricola, u.nome, u.cognome, u.email, s.corso_laurea,
               i.voto, i.stato
        FROM iscrizioneAppello i
        JOIN studente s ON i.idStudente = s.id
        JOIN utente u ON s.id = u.id
        WHERE i.idAppello = ? AND s.matricola = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idAppello);
            ps.setInt(2, matricola);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dettaglio = new DettaglioIscrizioneStudente();
                    dettaglio.setMatricola(rs.getInt("matricola"));
                    dettaglio.setNome(rs.getString("nome"));
                    dettaglio.setCognome(rs.getString("cognome"));
                    dettaglio.setEmail(rs.getString("email"));
                    dettaglio.setCorso_laurea(rs.getString("corso_laurea"));
                    dettaglio.setVoto(Voto.fromString(rs.getString("voto")));
                    dettaglio.setStato(StatoValutazione.fromString(rs.getString("stato")));
                }
            }
        }

        return dettaglio;
    }
}

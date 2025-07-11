package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.DettaglioIscrizioneStudente;
import it.polimi.tiw.tiwproject2024purehtml.beans.StatoValutazione;
import it.polimi.tiw.tiwproject2024purehtml.beans.Voto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IscrizioneAppelloDAO {
    private final Connection connection;

    public IscrizioneAppelloDAO(Connection connection) {
        this.connection = connection;
    }

    public List<DettaglioIscrizioneStudente> getIscrittiByAppelloSorted(int idAppello, String sortBy, String sortOrder) throws SQLException {
        List<DettaglioIscrizioneStudente> iscritti = new ArrayList<>();

        // Sanitize input per sicurezza
        List<String> allowedSortBy = List.of("matricola", "cognome", "nome", "email", "corso_laurea", "voto", "stato");
        if (!allowedSortBy.contains(sortBy)) sortBy = "cognome";
        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) sortOrder = "asc";

        String query = "SELECT s.matricola, u.nome, u.cognome, u.email, s.corso_laurea, " +
                "i.voto, i.stato " +
                "FROM iscrizioneappello i " +
                "JOIN utente u ON i.idStudente = u.id " +
                "JOIN studente s ON s.id = u.id " +
                "WHERE i.idAppello = ? " +
                "ORDER BY " + sortBy + " " + sortOrder;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idAppello);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DettaglioIscrizioneStudente d = new DettaglioIscrizioneStudente();
                    d.setMatricola(rs.getInt("matricola"));
                    d.setNome(rs.getString("nome"));
                    d.setCognome(rs.getString("cognome"));
                    d.setEmail(rs.getString("email"));
                    d.setCorso_laurea(rs.getString("corso_laurea"));
                    d.setVoto(Voto.fromString(rs.getString("voto")));
                    d.setStato(StatoValutazione.fromString(rs.getString("stato")));
                    iscritti.add(d);
                }
            }
        }

        return iscritti;
    }

    public boolean checkVotiInseriti(int idAppello) throws SQLException {
        String query = """
        SELECT COUNT(*)
        FROM iscrizioneAppello
        WHERE idAppello = ? AND stato = 'inserito'
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idAppello);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            return false;
        }
    }

    public int pubblicaVoti(int idAppello) throws SQLException {
        String query = "UPDATE iscrizioneAppello SET stato = 'pubblicato' WHERE idAppello = ? AND stato = 'inserito'";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idAppello);
            return ps.executeUpdate(); // restituisce il numero di righe aggiornate
        }
    }

    public boolean checkVerbalizzabile(int idAppello) throws SQLException {
        String query = "SELECT COUNT(*) FROM iscrizioneAppello WHERE idAppello = ? AND stato IN ('pubblicato', 'rifiutato')";
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, idAppello);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

}

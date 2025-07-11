package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.DettaglioIscrizioneStudente;
import it.polimi.tiw.tiwproject2024purehtml.beans.VerbaleInfo;
import it.polimi.tiw.tiwproject2024purehtml.beans.Voto;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class VerbaleDAO {
    private final Connection connection;

    public VerbaleDAO(Connection connection) {
        this.connection = connection;
    }

    public int creaVerbale(int idAppello) throws SQLException {
        String query = "INSERT INTO verbale (idAppello) VALUES (?)";

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idAppello);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creazione verbale fallita, nessuna riga inserita.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // restituisce idVerbale appena creato
                } else {
                    throw new SQLException("Creazione verbale fallita, nessun ID ottenuto.");
                }
            }
        }
    }

    public void collegaStudentiAVerbale(int idAppello, int idVerbale) throws SQLException {
        String query = """
        INSERT INTO studente_verbale (idStudente, idVerbale)
        SELECT idStudente, ?
        FROM iscrizioneAppello
        WHERE idAppello = ? AND stato = 'verbalizzato'
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idVerbale);
            ps.setInt(2, idAppello);
            ps.executeUpdate();
        }
    }

    public List<DettaglioIscrizioneStudente> getStudentiVerbalizzati(int idVerbale) throws SQLException {
        List<DettaglioIscrizioneStudente> studenti = new ArrayList<>();

        String query = """
        SELECT u.nome, u.cognome, s.matricola, s.corso_laurea, ia.voto
        FROM studente_verbale sv
        JOIN studente s ON sv.idStudente = s.id
        JOIN utente u ON s.id = u.id
        JOIN verbale v ON sv.idVerbale = v.idVerbale
        JOIN iscrizioneAppello ia ON s.id = ia.idStudente AND ia.idAppello = v.idAppello
        WHERE sv.idVerbale = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idVerbale);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DettaglioIscrizioneStudente studente = new DettaglioIscrizioneStudente();
                    studente.setNome(rs.getString("nome"));
                    studente.setCognome(rs.getString("cognome"));
                    studente.setMatricola(rs.getInt("matricola"));
                    studente.setCorso_laurea(rs.getString("corso_laurea"));
                    studente.setVoto(Voto.fromString(rs.getString("voto")));

                    studenti.add(studente);
                }
            }
        }
        return studenti;
    }

    public Timestamp getCurrentTimestamp(int idVerbale) throws SQLException {
        String query = "SELECT data_ora FROM verbale WHERE idVerbale = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idVerbale);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Forza il fuso orario Europe/Rome
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
                    return rs.getTimestamp("data_ora", cal);
                } else {
                    throw new SQLException("Nessun verbale trovato con id: " + idVerbale);
                }
            }
        }
    }

    public List<VerbaleInfo> getVerbaliByDocente(int idDocente) throws SQLException {
        String query = """
            SELECT v.idVerbale, v.data_ora, a.idAppello, c.nome
            FROM verbale v
            JOIN appello a ON v.idAppello = a.idAppello
            JOIN corso c ON a.idCorso = c.idCorso
            WHERE c.idDocente = ?
            ORDER BY c.nome ASC, a.data ASC
        """;

        List<VerbaleInfo> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VerbaleInfo vi = new VerbaleInfo();
                    vi.setIdVerbale(rs.getInt("idVerbale"));
                    vi.setIdAppello(rs.getInt("idAppello"));
                    vi.setDataOra(rs.getTimestamp("data_ora", Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))));
                    vi.setNomeCorso(rs.getString("nome"));
                    result.add(vi);
                }
            }
        }

        return result;
    }

    public int getIdAppelloByIdVerbale(int idVerbale) throws SQLException {
        String query = "SELECT idAppello FROM verbale WHERE idVerbale = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, idVerbale);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("idAppello");
                } else {
                    return -1; // verbale non trovato
                }
            }
        }
    }

}

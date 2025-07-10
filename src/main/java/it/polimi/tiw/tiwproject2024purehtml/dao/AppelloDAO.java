package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.Appello;
import it.polimi.tiw.tiwproject2024purehtml.beans.Voto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppelloDAO {
    private final Connection connection;

    public AppelloDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Appello> getAppellibyCorsoPerDocente(int idCorso) throws SQLException {
        List<Appello> appelli = new ArrayList<>();
        String query = "SELECT idAppello, data FROM appello WHERE idCorso = ? ORDER BY data DESC";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, idCorso);
            try (ResultSet result = pstatement.executeQuery();) {
                while(result.next()) {
                    Appello appello = new Appello();
                    appello.setIdAppello(result.getInt("idAppello"));
                    appello.setData(result.getDate("data"));
                    appelli.add(appello);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return appelli;
    }

    public List<Appello> getAppellibyCorsoPerStudenti(int idStudente, int idCorso) throws SQLException {
        List<Appello> appelli = new ArrayList<>();

        String query = """
        SELECT appello.idAppello, appello.data
        FROM iscrizioneAppello
        JOIN appello ON iscrizioneAppello.idAppello = appello.idAppello
        WHERE iscrizioneAppello.idStudente = ? AND appello.idCorso = ?
        ORDER BY appello.data DESC
    """;

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, idStudente);
            pstatement.setInt(2, idCorso);

            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Appello appello = new Appello();
                    appello.setIdAppello(result.getInt("idAppello"));
                    appello.setData(result.getDate("data"));
                    appelli.add(appello);
                }
            }
        }
        return appelli;
    }

    public boolean checkAppelloByDocente(int idDocente, int idAppello) throws SQLException {
        boolean check;
        String query = """
        SELECT *
        FROM appello
        JOIN corso ON appello.idCorso = corso.idCorso
        WHERE corso.idDocente = ? AND appello.idAppello = ?
        """;
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, idDocente);
            pstatement.setInt(2, idAppello);
            try(ResultSet result = pstatement.executeQuery();) {
                check = result.isBeforeFirst();
            }
        }
        return check;
    }

    public boolean checkModificaVoto(int idAppello, int matricola) throws SQLException {
        boolean isStatusModifiable;

        String query = """
        SELECT *
        FROM iscrizioneAppello ia
        JOIN studente s ON ia.idStudente = s.id
        WHERE s.matricola = ? AND ia.idAppello = ?
          AND (ia.stato = 'non inserito' OR ia.stato = 'inserito')
        """;

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, matricola);
            pstatement.setInt(2, idAppello);

            try (ResultSet result = pstatement.executeQuery()) {
                isStatusModifiable = result.isBeforeFirst(); // true se c'è almeno una riga
            }
        }

        return isStatusModifiable;
    }

    public Date getDataByIdAppello(int idAppello) throws SQLException {
        Date data = null;

        String query = """
        SELECT data
        FROM appello
        WHERE idAppello = ?
        """;

        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, idAppello);

            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    data = result.getDate("data");
                }
            }
        }

        return data;
    }

    public int inserisciVoto(int idAppello, int matricola, Voto voto) throws SQLException {
        int row = 0;
        String query = """
        UPDATE iscrizioneAppello
        SET voto = ?, stato = 'inserito'
        WHERE idAppello = ? AND idStudente = (
            SELECT s.id FROM studente s WHERE s.matricola = ?
        )
        AND (stato = 'non inserito' OR stato = 'inserito')
        """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, voto.getLabel());
            ps.setInt(2, idAppello);
            ps.setInt(3, matricola);

            row = ps.executeUpdate();
        }
        return row;
    }
}

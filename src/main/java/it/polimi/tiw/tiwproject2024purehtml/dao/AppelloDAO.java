package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.Appello;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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


}

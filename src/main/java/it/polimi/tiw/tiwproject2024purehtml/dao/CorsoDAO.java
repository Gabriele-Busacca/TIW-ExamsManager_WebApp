package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.Corso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAO {
    private final Connection connection;

    public CorsoDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Corso> getCorsiByIdDocente(int idDocente) throws SQLException {
        List<Corso> corsi = new ArrayList<>();
        String query = "SELECT idCorso, nome FROM corso WHERE idDocente = ? ORDER BY nome DESC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idDocente);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Corso corso = new Corso();
                    corso.setIdCorso(rs.getInt("idCorso"));
                    corso.setNome(rs.getString("nome"));
                    corsi.add(corso);
                }
            }
        }
        return corsi;
    }

    public List<Corso> getCorsiByIdStudente(int studId) throws SQLException {
        List<Corso> corsi = new ArrayList<>();

        String query = """
        SELECT corso.idCorso, corso.nome, corso.idDocente
        FROM corso
        JOIN iscrizioneCorso ON corso.idCorso = iscrizioneCorso.idCorso
        WHERE iscrizioneCorso.idStudente = ?
        ORDER BY corso.nome DESC
    """;

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, studId);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Corso corso = new Corso();
                    corso.setIdCorso(result.getInt("idCorso"));
                    corso.setNome(result.getString("nome"));
                    corso.setIdDocente(result.getInt("idDocente"));
                    corsi.add(corso);
                }
            }
        }

        return corsi;
    }

}

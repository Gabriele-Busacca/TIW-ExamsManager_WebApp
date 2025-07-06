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

    public List<Corso> getCorsibyIdDocente(int idDocente) throws SQLException {
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
}

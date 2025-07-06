package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.Studente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudenteDAO {
    private Connection connection;

    public StudenteDAO(Connection connection) {
        this.connection = connection;
    }

    public Studente findStudenteById(int id) throws SQLException {
        String query = "SELECT u.id, u.nome, u.cognome, u.email, u.ruolo, s.matricola, s.corso_laurea " +
                "FROM utente u JOIN studente s ON u.id = s.id " +
                "WHERE u.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, id);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    Studente studente = new Studente();
                    studente.setId(result.getInt("id"));
                    studente.setNome(result.getString("nome"));
                    studente.setCognome(result.getString("cognome"));
                    studente.setEmail(result.getString("email"));
                    studente.setRuolo(result.getString("ruolo"));
                    studente.setMatricola(result.getInt("matricola"));
                    studente.setCorsoLaurea(result.getString("corso_laurea"));
                    return studente;
                } else {
                    return null;
                }
            }
        }
    }
}



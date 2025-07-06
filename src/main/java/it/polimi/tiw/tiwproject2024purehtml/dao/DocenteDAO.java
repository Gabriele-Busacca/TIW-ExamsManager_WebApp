package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.Docente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocenteDAO {
    private Connection connection;

    public DocenteDAO(Connection connection) {
        this.connection = connection;
    }

    public Docente findDocenteById(int id) throws SQLException {
        String query = "SELECT u.id, u.nome, u.cognome, u.email, u.ruolo FROM utente u JOIN docente d ON u.id = d.id WHERE u.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, id);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    Docente docente = new Docente();
                    docente.setId(result.getInt("id"));
                    docente.setNome(result.getString("nome"));
                    docente.setCognome(result.getString("cognome"));
                    docente.setEmail(result.getString("email"));
                    docente.setRuolo(result.getString("ruolo"));
                    return docente;
                } else {
                    return null; // Non trovato
                }
            }
        }
    }
}


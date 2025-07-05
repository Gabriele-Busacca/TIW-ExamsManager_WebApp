package it.polimi.tiw.tiwproject2024purehtml.dao;

import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {
    private Connection con;

    public UtenteDAO(Connection connection) {
        this.con = connection;
    }

    public Utente checkCredentials(String email, String hashedPassword) throws SQLException {
        String query = "SELECT id, ruolo FROM utente WHERE email = ? AND password = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, hashedPassword);

            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) return null;
                else {
                    result.next();
                    Utente utente = new Utente();
                    utente.setId(result.getInt("id"));
                    utente.setRuolo(result.getString("ruolo"));
                    return utente;
                }
            }
        }
    }
}

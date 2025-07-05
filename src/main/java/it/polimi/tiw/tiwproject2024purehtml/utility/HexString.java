package it.polimi.tiw.tiwproject2024purehtml.utility;

import java.math.BigInteger;

public class HexString {
    public static String toHexString(byte[] hash) {

        BigInteger number = new BigInteger(1, hash); //creo un intero a partire dai byte
        StringBuilder hexString = new StringBuilder(number.toString(16)); //converto intero in stringa esadecimale

        while (hexString.length() < 32) //aggiungo 0 all'inizio della stringa se la lunghezza non è di 32
        {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}

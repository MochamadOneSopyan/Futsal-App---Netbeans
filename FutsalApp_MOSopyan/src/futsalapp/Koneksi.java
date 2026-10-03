
package futsalapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 *
 * @author Mochamad One
 */
public class Koneksi {
        private static Connection koneksi;
    
    public static Connection getKoneksi() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            koneksi = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/db_futsal",
                "root",
                ""
            );
            System.out.println("Koneksi Database Berhasil!");
            return koneksi;
        } catch (ClassNotFoundException e) {
            System.out.println("Driver tidak ditemukan: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
            return null;
        }
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package futsalapp;

/**
 *
 * @author Mochamad One
 */
public class TestKoneksi {
        public static void main(String[] args) {

        if (Koneksi.getKoneksi() != null) {
            System.out.println("DATABASE TERHUBUNG");
        } else {
            System.out.println("DATABASE TIDAK TERHUBUNG");
        }
    }
}

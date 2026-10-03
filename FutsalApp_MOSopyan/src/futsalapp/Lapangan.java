/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package futsalapp;

/**
 *
 * @author Mochamad One
 */
public class Lapangan {
    private int id;
    private String namaLapangan;
    private String tipeLapangan;
    private int hargaPerJam;
    private String status;
    
    // Constructor Kosong
    public Lapangan() {}
    
    // Constructor dengan parameter
    public Lapangan(int id, String namaLapangan, String tipeLapangan, 
                    int hargaPerJam, String status) {
        this.id = id;
        this.namaLapangan = namaLapangan;
        this.tipeLapangan = tipeLapangan;
        this.hargaPerJam = hargaPerJam;
        this.status = status;
    }
    
    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNamaLapangan() { return namaLapangan; }
    public void setNamaLapangan(String namaLapangan) { 
        this.namaLapangan = namaLapangan; 
    }
    
    public String getTipeLapangan() { return tipeLapangan; }
    public void setTipeLapangan(String tipeLapangan) { 
        this.tipeLapangan = tipeLapangan; 
    }
    
    public int getHargaPerJam() { return hargaPerJam; }
    public void setHargaPerJam(int hargaPerJam) { 
        this.hargaPerJam = hargaPerJam; 
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}

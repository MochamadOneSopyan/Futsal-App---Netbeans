package booking;

import futsalapp.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Mochamad One
 */
public class booking_lapangan extends javax.swing.JFrame {

    /**
     * Creates new form booking
     */
    public booking_lapangan() {
        initComponents();
        tampilNamaLapangan();
        
         // Tambahkan listener untuk mendeteksi saat tanggal pada JDateChooser diubah
    dateChooserTanggal.getDateEditor().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if ("date".equals(evt.getPropertyName())) {
                cekTanggalLewat();
                cekStatusLapanganByTanggal(); // <-- Tambahkan baris ini
            }
        }
    });
        
    }
    
    //Method Pendukung cekTanggalLewat()
    private void cekTanggalLewat() {
    java.util.Date tanggalPilih = dateChooserTanggal.getDate();
    if (tanggalPilih == null) return;

    // Ambil tanggal hari ini tanpa jam
    java.util.Calendar calHariIni = java.util.Calendar.getInstance();
    calHariIni.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calHariIni.set(java.util.Calendar.MINUTE, 0);
    calHariIni.set(java.util.Calendar.SECOND, 0);
    calHariIni.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Date hariIni = calHariIni.getTime();

    // Jika tanggal yang dipilih sebelum hari ini
    if (tanggalPilih.before(hariIni)) {
        javax.swing.JOptionPane.showMessageDialog(
            this, 
            "Peringatan: Tanggal yang Anda pilih sudah lewat!", 
            "Tanggal Tidak Valid", 
            javax.swing.JOptionPane.WARNING_MESSAGE
        );
        // Kosongkan kembali pilihan tanggalnya
        dateChooserTanggal.setDate(null);
    }
}
    
    //Method cekStatusLapanganByTanggal()
    private void cekStatusLapanganByTanggal() {
    String namaLapangan = comboNamaLapangan.getSelectedItem() != null ? comboNamaLapangan.getSelectedItem().toString() : "";
    java.util.Date tanggalPilih = dateChooserTanggal.getDate();

    // Jika lapangan belum dipilih atau tanggal belum diisi, reset label
    if (namaLapangan.equals("-- Pilih Lapangan --") || namaLapangan.isEmpty() || tanggalPilih == null) {
        lblStatusLapangan.setText("-");
        lblStatusLapangan.setForeground(java.awt.Color.BLACK);
        return;
    }

    try {
        Connection conn = Koneksi.getKoneksi();
        java.sql.Date tanggalSql = new java.sql.Date(tanggalPilih.getTime());

        // 1. Ambil harga per jam dari tabel lapangan
        String sqlHarga = "SELECT harga_per_jam FROM lapangan WHERE nama_lapangan = ?";
        PreparedStatement pstHarga = conn.prepareStatement(sqlHarga);
        pstHarga.setString(1, namaLapangan);
        java.sql.ResultSet rsHarga = pstHarga.executeQuery();

        if (rsHarga.next()) {
            int hargaPerJam = rsHarga.getInt("harga_per_jam");
            textTotalHarga.setText(String.valueOf(hargaPerJam));
            hitungSisaPembayaranRealtime();
        }
        rsHarga.close();
        pstHarga.close();

        // 2. Cek apakah sudah ada booking untuk lapangan ini pada tanggal yang dipilih
        String sqlCek = "SELECT * FROM booking WHERE nama_lapangan = ? AND tanggal_booking = ?";
        PreparedStatement pstCek = conn.prepareStatement(sqlCek);
        pstCek.setString(1, namaLapangan);
        pstCek.setDate(2, tanggalSql);
        java.sql.ResultSet rsCek = pstCek.executeQuery();

        if (rsCek.next()) {
            // Jika sudah ada yang booking di tanggal tersebut
            lblStatusLapangan.setText("Terpesan");
            lblStatusLapangan.setForeground(java.awt.Color.RED);
        } else {
            // Jika belum ada yang booking di tanggal tersebut
            lblStatusLapangan.setText("Tersedia");
            lblStatusLapangan.setForeground(new java.awt.Color(0, 153, 0)); // Hijau
        }

        rsCek.close();
        pstCek.close();

    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Gagal mengecek status lapangan: " + e.getMessage());
    }
}
    
    
    private void tampilNamaLapangan() {
    comboNamaLapangan.removeAllItems();

    String sql = "SELECT nama_lapangan FROM lapangan";

    try (java.sql.Connection conn =
            java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/db_futsal",
                "root", "");
         java.sql.PreparedStatement ps =
            conn.prepareStatement(sql);
         java.sql.ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            comboNamaLapangan.addItem(rs.getString("nama_lapangan"));
        }

    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Gagal memuat nama lapangan: " + e.getMessage()
        );
    }
}
    
    // Variabel penanda ID yang sedang diedit
    private String idBookingDipilih = null;

    // Konstruktor khusus untuk mode EDIT (menerima data dari form_transaksi)
    // Konstruktor khusus untuk mode EDIT (diperbarui agar menangkap data lengkap)
public booking_lapangan(String id, String namaLap, String namaPenyewa, String noHp, String tgl, String total, String jenisBayar, String jmlBayar, String sisaBayar, String kembalian) {
    initComponents();
    tampilNamaLapangan(); // Memuat pilihan ComboBox lapangan
    
    // Simpan ID untuk query update nanti
    this.idBookingDipilih = id;
    
    // Masukkan data lama ke komponen form
    comboNamaLapangan.setSelectedItem(namaLap);
    textNamaPenyewa.setText(namaPenyewa);
    textNoHp.setText(noHp);
    textTotalHarga.setText(total);
    comboPembayaran.setSelectedItem(jenisBayar); // Langsung set sesuai teks combobox
    textJumlahBayar.setText(jmlBayar);
    textSisaPembayaran.setText(sisaBayar);
    textKembalian.setText(kembalian);
    
    // Parsing tanggal
    try {
        java.util.Date date = java.sql.Date.valueOf(tgl);
        dateChooserTanggal.setDate(date);
    } catch (Exception e) {
        // Abaikan jika format berbeda
    }
    
    // Ubah teks tombol Simpan menjadi "Update Data"
    btnSimpan.setText("Update Data");
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        textNama = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnSimpan = new javax.swing.JButton();
        textNamaPenyewa = new javax.swing.JTextField();
        textNoHp = new javax.swing.JTextField();
        dateChooserTanggal = new com.toedter.calendar.JDateChooser();
        comboNamaLapangan = new javax.swing.JComboBox<>();
        btnReset = new javax.swing.JButton();
        lblStatusLapangan = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        comboPembayaran = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        textTotalHarga = new javax.swing.JTextField();
        btnCekTransaksi = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        textJumlahBayar = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        textSisaPembayaran = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        textKembalian = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textNama.setText("Nama Lapangan");

        jLabel2.setText("Nama Penyewa");

        jLabel3.setText("No HP");

        jLabel4.setText("Tanggal Booking");

        btnSimpan.setBackground(new java.awt.Color(0, 153, 51));
        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        comboNamaLapangan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboNamaLapanganItemStateChanged(evt);
            }
        });
        comboNamaLapangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboNamaLapanganActionPerformed(evt);
            }
        });

        btnReset.setBackground(new java.awt.Color(204, 204, 0));
        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        lblStatusLapangan.setText("Status: -");

        jLabel1.setText("Metode Pembayaran");

        comboPembayaran.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Pilih Pembayaran --", "DP (Down Payment)", "Bayar Full" }));

        jLabel5.setText("Total Pembayaran");

        textTotalHarga.setEditable(false);
        textTotalHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textTotalHargaActionPerformed(evt);
            }
        });

        btnCekTransaksi.setBackground(new java.awt.Color(0, 153, 153));
        btnCekTransaksi.setText("Cek Data Transaksi");
        btnCekTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekTransaksiActionPerformed(evt);
            }
        });

        jLabel6.setText("Jumlah Bayar");

        textJumlahBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textJumlahBayarKeyReleased(evt);
            }
        });

        jLabel7.setText("Sisa Pembayaran");

        textSisaPembayaran.setEditable(false);

        jLabel8.setText("Kembalian");

        textKembalian.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textNama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(41, 41, 41))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCekTransaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(comboPembayaran, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(textTotalHarga)
                        .addComponent(comboNamaLapangan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(textNamaPenyewa)
                        .addComponent(textNoHp)
                        .addComponent(dateChooserTanggal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(textJumlahBayar)
                        .addComponent(textSisaPembayaran)
                        .addComponent(textKembalian))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSimpan)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(lblStatusLapangan, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(376, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textNama, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(comboNamaLapangan, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblStatusLapangan, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(textNamaPenyewa, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(textNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateChooserTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(textTotalHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboPembayaran, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textJumlahBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(textSisaPembayaran, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textKembalian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnCekTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(93, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void comboNamaLapanganActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        // TODO add your handling code here:
        
        
    }                                                 

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:   
    String namaLapangan = comboNamaLapangan.getSelectedItem().toString();
    String namaPenyewa = textNamaPenyewa.getText().trim();
    String noHp = textNoHp.getText().trim();
    String jenisPembayaran = comboPembayaran.getSelectedItem().toString();
    String totalHargaStr = textTotalHarga.getText().trim();
    java.util.Date tanggalPilih = dateChooserTanggal.getDate();

    // 1. Validasi input kosong
    if (namaLapangan.equals("-- Pilih Lapangan --") || namaPenyewa.isEmpty() || noHp.isEmpty() || 
        jenisPembayaran.equals("-- Pilih Pembayaran --") || totalHargaStr.isEmpty() || tanggalPilih == null) {
        JOptionPane.showMessageDialog(this, "Semua data, metode pembayaran, dan total harga harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        Connection conn = futsalapp.Koneksi.getKoneksi();
        java.sql.Date tanggalSql = new java.sql.Date(tanggalPilih.getTime());
        int totalHarga = Integer.parseInt(totalHargaStr);
        int jumlahBayar = textJumlahBayar.getText().trim().isEmpty() ? 0 : Integer.parseInt(textJumlahBayar.getText().trim());
        int sisaPembayaran = textSisaPembayaran.getText().trim().isEmpty() ? 0 : Integer.parseInt(textSisaPembayaran.getText().trim());
        int kembalian = textKembalian.getText().trim().isEmpty() ? 0 : Integer.parseInt(textKembalian.getText().trim());
        
        String statusPembayaran = jenisPembayaran.toLowerCase().contains("dp") ? "Belum Lunas (DP)" : "Lunas";

        // Validasi tambahan untuk Bayar Full agar uang tidak kurang
        if (jenisPembayaran.contains("Full") && jumlahBayar < totalHarga) {
            JOptionPane.showMessageDialog(this, "Metode 'Bayar Full' mengharuskan jumlah bayar pas atau lebih dari total harga!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            textJumlahBayar.requestFocus();
            return;
        }
        
        // Validasi khusus metode DP agar jumlah bayar tidak melebihi/pas total harga
        if (jenisPembayaran.contains("DP") && jumlahBayar >= totalHarga) {
            JOptionPane.showMessageDialog(this, "Jumlah bayar DP tidak boleh melebihi atau sama dengan Total Harga! Jika ingin melunasi, silakan pilih metode 'Bayar Full'.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            textJumlahBayar.requestFocus();
            return;
        }

        // CEK APAKAH INI MODE EDIT ATAU INSERT BARU
        if (idBookingDipilih == null) {
            // --- VALIDASI: CEK APAKAH LAPANGAN SUDAH DIBOOKING DI TANGGAL YANG SAMA ---
            String sqlCekJadwal = "SELECT * FROM booking WHERE nama_lapangan = ? AND tanggal_booking = ?";
            PreparedStatement pstJadwal = conn.prepareStatement(sqlCekJadwal);
            pstJadwal.setString(1, namaLapangan);
            pstJadwal.setDate(2, tanggalSql);
            java.sql.ResultSet rsJadwal = pstJadwal.executeQuery();
            
            if (rsJadwal.next()) {
                JOptionPane.showMessageDialog(this, "Maaf, lapangan '" + namaLapangan + "' sudah dibooking pada tanggal tersebut!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                rsJadwal.close();
                pstJadwal.close();
                return; // Batalkan penyimpanan karena bentrok jadwal di tanggal yang sama
            }
            rsJadwal.close();
            pstJadwal.close();
            // -----------------------------------------------------------------------

            // --- AMBIL ID LAPANGAN ---
            String sqlCek = "SELECT id FROM lapangan WHERE nama_lapangan = ?";
            PreparedStatement psCek = conn.prepareStatement(sqlCek);
            psCek.setString(1, namaLapangan);
            java.sql.ResultSet rsCek = psCek.executeQuery();
            
            int idLapangan = -1;
            if (rsCek.next()) {
                idLapangan = rsCek.getInt("id");
            }
            rsCek.close();
            psCek.close();

            // --- LOGIKA INSERT (DATA BARU) ---
            String sql = "INSERT INTO booking (id_lapangan, nama_lapangan, nama_penyewa, no_hp, tanggal_booking, total_harga, jenis_pembayaran, status_pembayaran, jumlah_bayar, sisa_pembayaran, kembalian) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, idLapangan);
            pst.setString(2, namaLapangan);
            pst.setString(3, namaPenyewa);
            pst.setString(4, noHp);
            pst.setDate(5, tanggalSql);
            pst.setInt(6, totalHarga);
            pst.setString(7, jenisPembayaran);
            pst.setString(8, statusPembayaran);
            pst.setInt(9, jumlahBayar);
            pst.setInt(10, sisaPembayaran);
            pst.setInt(11, kembalian);
            pst.executeUpdate();
            pst.close();

            // --- UPDATE STATUS LAPANGAN MENJADI 'Terpesan' (Opsional untuk indikator visual) ---
            String sqlUpdateStatus = "UPDATE lapangan SET status = 'Terpesan' WHERE nama_lapangan = ?";
            PreparedStatement pstStatus = conn.prepareStatement(sqlUpdateStatus);
            pstStatus.setString(1, namaLapangan);
            pstStatus.executeUpdate();
            pstStatus.close();

            JOptionPane.showMessageDialog(this, "Booking berhasil disimpan!");

        } else {
            // --- LOGIKA UPDATE (PERBARUI DATA LAMA) ---
            String sql = "UPDATE booking SET nama_lapangan = ?, nama_penyewa = ?, no_hp = ?, tanggal_booking = ?, total_harga = ?, jenis_pembayaran = ?, status_pembayaran = ?, jumlah_bayar = ?, sisa_pembayaran = ?, kembalian = ? WHERE id_booking = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, namaLapangan);
            pst.setString(2, namaPenyewa);
            pst.setString(3, noHp);
            pst.setDate(4, tanggalSql);
            pst.setInt(5, totalHarga);
            pst.setString(6, jenisPembayaran);
            pst.setString(7, statusPembayaran);
            pst.setInt(8, jumlahBayar);
            pst.setInt(9, sisaPembayaran);
            pst.setInt(10, kembalian);
            pst.setString(11, idBookingDipilih);
            pst.executeUpdate();
            pst.close();

            JOptionPane.showMessageDialog(this, "Data transaksi berhasil di-update!");
        }

        // Kembali ke halaman transaksi setelah selesai
        new form_transaksi().setVisible(true);
        this.dispose();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memproses data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }    
    }                                         

    private void comboNamaLapanganItemStateChanged(java.awt.event.ItemEvent evt) {                                                   
        // TODO add your handling code here:
       // Pastikan event tereksekusi saat item dipilih (SELECTED)
     if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String namaLapangan = comboNamaLapangan.getSelectedItem().toString();
        // Panggil method pengecekan status berdasarkan lapangan dan tanggal secara dinamis
        cekStatusLapanganByTanggal();
        
        if (namaLapangan.equals("-- Pilih Lapangan --") || namaLapangan.isEmpty()) {
            lblStatusLapangan.setText("-");
            lblStatusLapangan.setForeground(java.awt.Color.BLACK);
            textTotalHarga.setText("");
            textSisaPembayaran.setText("");
            return;
        }

        try {
            Connection conn = Koneksi.getKoneksi();
            // Ambil status DAN harga_per_jam dari tabel lapangan
            String sql = "SELECT status, harga_per_jam FROM lapangan WHERE nama_lapangan = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, namaLapangan);
            java.sql.ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                int hargaPerJam = rs.getInt("harga_per_jam");
                
                lblStatusLapangan.setText(status);
                
                if (status.equalsIgnoreCase("Tersedia")) {
                    lblStatusLapangan.setForeground(new java.awt.Color(0, 153, 0)); 
                } else {
                    lblStatusLapangan.setForeground(java.awt.Color.RED);
                }
                
                // OTOMATIS ISI TOTAL PEMBAYARAN DARI DATABASE
                textTotalHarga.setText(String.valueOf(hargaPerJam));
                
                // Hitung sisa pembayaran otomatis jika jumlah bayar sudah terisi
                hitungSisaPembayaranRealtime();
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data lapangan: " + e.getMessage());
        }
    }
    }                                                  

    private void textTotalHargaActionPerformed(java.awt.event.ActionEvent evt) {                                               
        // TODO add your handling code here:
    }                                              

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
         // Mengosongkan/mereset seluruh form input booking
    comboNamaLapangan.setSelectedIndex(0);
    textNamaPenyewa.setText("");
    textNoHp.setText("");
    dateChooserTanggal.setDate(null);
    comboPembayaran.setSelectedIndex(0);
    textTotalHarga.setText("");
    textJumlahBayar.setText("");
    textSisaPembayaran.setText("");
    textKembalian.setText("");
    lblStatusLapangan.setText("Status: -");
    lblStatusLapangan.setForeground(java.awt.Color.BLACK);
    
    textNamaPenyewa.requestFocus();
    }                                        

    private void btnCekTransaksiActionPerformed(java.awt.event.ActionEvent evt) {                                                
        // TODO add your handling code here:
        // Membuka form_transaksi
        new form_transaksi().setVisible(true);
    
         // Menutup form booking_lapangan yang sedang aktif
         this.dispose();
    }                                               

    private void textJumlahBayarKeyReleased(java.awt.event.KeyEvent evt) {                                            
        // TODO add your handling code here:
        hitungSisaPembayaranRealtime();
    }                                           

    // method hitungSisaPembayaranRealtime
    private void hitungSisaPembayaranRealtime() {
     try {
        String totalStr = textTotalHarga.getText().trim();
        String bayarStr = textJumlahBayar.getText().trim();
        
        if (totalStr.isEmpty()) {
            textSisaPembayaran.setText("0");
            textKembalian.setText("0");
            return;
        }
        
        int totalHarga = Integer.parseInt(totalStr);
        int jumlahBayar = bayarStr.isEmpty() ? 0 : Integer.parseInt(bayarStr);
        
        // Ambil pilihan metode pembayaran dengan aman
        Object selectedItem = comboPembayaran.getSelectedItem();
        if (selectedItem == null) return;
        String metode = selectedItem.toString();
        
        if (metode.contains("DP")) {
            // Validasi: Jika memilih DP, jumlah bayar tidak boleh melebihi atau sama dengan total harga
            if (jumlahBayar >= totalHarga) {
                javax.swing.JOptionPane.showMessageDialog(
                    this, 
                    "Jumlah bayar DP tidak boleh melebihi atau sama dengan Total Harga! Silakan pilih metode 'Bayar Full'.", 
                    "Peringatan", 
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );
                textJumlahBayar.setText(""); // Kosongkan kembali input jumlah bayar
                textSisaPembayaran.setText("0");
                textKembalian.setText("0");
                return;
            }
            
            // Jika valid, hitung sisa pembayaran normal untuk DP
            int sisa = totalHarga - jumlahBayar;
            textSisaPembayaran.setText(String.valueOf(sisa));
            textKembalian.setText("0"); // DP tidak ada kembalian
            
        } else if (metode.contains("Full")) {
            // Jika kasir memilih Bayar Full: Sisa pembayaran wajib 0
            textSisaPembayaran.setText("0");
            
            if (jumlahBayar >= totalHarga) {
                int kembalian = jumlahBayar - totalHarga;
                textKembalian.setText(String.valueOf(kembalian));
            } else {
                // Jika uang bayar kurang pada mode Bayar Full, kembalian 0
                textKembalian.setText("0");
            }
            
        } else {
            // Jika belum memilih metode pembayaran
            textSisaPembayaran.setText("0");
            textKembalian.setText("0");
        }
        
    } catch (NumberFormatException e) {
        textSisaPembayaran.setText("0");
        textKembalian.setText("0");
    }
}
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(booking_lapangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(booking_lapangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(booking_lapangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(booking_lapangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new booking_lapangan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnCekTransaksi;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> comboNamaLapangan;
    private javax.swing.JComboBox<String> comboPembayaran;
    private com.toedter.calendar.JDateChooser dateChooserTanggal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel lblStatusLapangan;
    private javax.swing.JTextField textJumlahBayar;
    private javax.swing.JTextField textKembalian;
    private javax.swing.JLabel textNama;
    private javax.swing.JTextField textNamaPenyewa;
    private javax.swing.JTextField textNoHp;
    private javax.swing.JTextField textSisaPembayaran;
    private javax.swing.JTextField textTotalHarga;
    // End of variables declaration                   
}

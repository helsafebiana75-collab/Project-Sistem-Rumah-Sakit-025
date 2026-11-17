package com.mycompany.mainrumahsakit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SistemRumahSakit implements Serializable {

    private static final long serialVersionUID = 1L;

    private String namaRumahSakit;

    private HashMap<String, Pasien> daftarPasien;
    private HashMap<String, LayananMedis> daftarLayanan;
    private ArrayList<Transaksi> daftarTransaksi;

    public SistemRumahSakit(String namaRumahSakit) {
        this.namaRumahSakit = namaRumahSakit;

        daftarPasien = new HashMap<>();
        daftarLayanan = new HashMap<>();
        daftarTransaksi = new ArrayList<>();
    }

    public void tambahPasien(String id, String nama) {
        daftarPasien.put(id, new Pasien(id, nama));
    }

    public void tambahLayananMedis(String kode, String nama, int kuota) {
        daftarLayanan.put(kode, new LayananMedis(kode, nama, kuota));
    }

    public void jalankanTransaksiDariFile(String namaFile)
            throws IOException {

        BufferedReader reader = new BufferedReader(
                new FileReader(namaFile));

        StringBuilder isiFile = new StringBuilder();
        String baris;

        try {
            while ((baris = reader.readLine()) != null) {
                isiFile.append(baris).append(" ");
            }
        } finally {
            reader.close();
        }

        String[] bagian = isiFile.toString().split(",");

        String id = null;
        String kode = null;
        int jumlah;
        int tahap = 0;

        for (String data : bagian) {

            data = data.trim();

            if (data.isEmpty()) {
                continue;
            }

            String[] pasangan = data.split("=", 2);

            if (pasangan.length != 2) {
                System.out.println("[DATA TIDAK VALID] " + data);
                continue;
            }

            String kunci = pasangan[0].trim().toUpperCase();
            String nilai = pasangan[1].trim();

            try {

                if (kunci.equals("ID")) {
                    id = nilai;
                    tahap = 1;

                } else if (kunci.equals("KD")) {

                    if (tahap != 1) {
                        throw new IllegalArgumentException(
                                "Urutan data ID dan KD tidak sesuai.");
                    }

                    kode = nilai;
                    tahap = 2;

                } else if (kunci.equals("JML")) {

                    if (tahap != 2) {
                        throw new IllegalArgumentException(
                                "Urutan data KD dan JML tidak sesuai.");
                    }

                    jumlah = Integer.parseInt(nilai);

                    prosesSatuTransaksi(id, kode, jumlah);

                    tahap = 0;
                    id = null;
                    kode = null;

                } else {
                    throw new IllegalArgumentException(
                            "Kolom tidak dikenal: " + kunci);
                }

            } catch (NumberFormatException e) {

                System.out.println(
                        "[TRANSAKSI GAGAL] Jumlah harus berupa angka: "
                        + nilai);

                tahap = 0;

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "[DATA TIDAK VALID] " + e.getMessage());

                tahap = 0;
            }
        }
    }
    
    private void prosesSatuTransaksi(
            String id, String kode, int jumlah) {

        try {

            if (id == null || !daftarPasien.containsKey(id)) {
                throw new DataTidakDitemukanException(
                        "Pasien dengan ID " + id + " tidak ditemukan.");
            }

            if (!daftarLayanan.containsKey(kode)) {
                throw new DataTidakDitemukanException(
                        "Layanan dengan kode " + kode + " tidak ditemukan.");
            }

            if (jumlah <= 0) {
                throw new IllegalArgumentException(
                        "Jumlah transaksi harus lebih dari 0.");
            }

            LayananMedis layanan = daftarLayanan.get(kode);

            if (jumlah > layanan.getKuota()) {
                throw new KuotaTidakCukupException(
                        "Kuota " + layanan.getNama()
                        + " tidak cukup. Tersedia: "
                        + layanan.getKuota()
                        + ", diminta: " + jumlah);
            }

            layanan.kurangiKuota(jumlah);

            daftarTransaksi.add(
                    new Transaksi(id, kode, jumlah));

            System.out.println(
                    "[BERHASIL] Pasien " + id
                    + " menggunakan layanan " + kode
                    + " sebanyak " + jumlah + ".");

        } catch (DataTidakDitemukanException
                | KuotaTidakCukupException
                | IllegalArgumentException e) {

            System.out.println(
                    "[TRANSAKSI GAGAL] " + e.getMessage());
        }
    }

    public void simpanLaporanAkhir() {

        try (ObjectOutputStream output =
                new ObjectOutputStream(
                        new FileOutputStream("data_master.bak"))) {

            output.writeObject(this);

            System.out.println(
                    "[SERIALISASI] Data berhasil disimpan ke data_master.bak.");

        } catch (IOException e) {

            System.out.println(
                    "[ERROR SERIALISASI] " + e.getMessage());
        }

        try (BufferedWriter writer =
                new BufferedWriter(
                        new FileWriter("laporan.txt"))) {

            writer.write("ID_PASIEN,KODE_LAYANAN,JML");
            writer.newLine();

            for (Transaksi transaksi : daftarTransaksi) {

                writer.write(
                        transaksi.getIdPasien() + ","
                        + transaksi.getKodeLayanan() + ","
                        + transaksi.getJumlah());

                writer.newLine();
            }

            System.out.println(
                    "[LAPORAN] Laporan CSV berhasil disimpan ke laporan.txt.");

        } catch (IOException e) {

            System.out.println(
                    "[ERROR LAPORAN] " + e.getMessage());
        }

        System.out.println("\n=== RINGKASAN "
                + namaRumahSakit + " ===");

        System.out.println(
                "Jumlah transaksi berhasil: "
                + daftarTransaksi.size());

        System.out.println("Sisa kuota layanan:");

        for (Map.Entry<String, LayananMedis> entry
                : daftarLayanan.entrySet()) {

            LayananMedis layanan = entry.getValue();

            System.out.println(
                    layanan.getKode() + " - "
                    + layanan.getNama() + ": "
                    + layanan.getKuota());
        }
    }
}
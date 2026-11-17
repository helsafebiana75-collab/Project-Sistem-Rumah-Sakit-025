package com.mycompany.mainrumahsakit;

import java.io.Serializable;

public class Transaksi implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idPasien;
    private String kodeLayanan;
    private int jumlah;

    public Transaksi(String idPasien, String kodeLayanan, int jumlah) {
        this.idPasien = idPasien;
        this.kodeLayanan = kodeLayanan;
        this.jumlah = jumlah;
    }

    public String getIdPasien() {
        return idPasien;
    }

    public String getKodeLayanan() {
        return kodeLayanan;
    }

    public int getJumlah() {
        return jumlah;
    }
}
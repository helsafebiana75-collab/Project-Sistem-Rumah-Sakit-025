package com.mycompany.mainrumahsakit;

import java.io.Serializable;

public class LayananMedis implements Serializable {

    private static final long serialVersionUID = 1L;

    private String kode;
    private String nama;
    private int kuota;

    public LayananMedis(String kode, String nama, int kuota) {
        this.kode = kode;
        this.nama = nama;
        this.kuota = kuota;
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    public int getKuota() {
        return kuota;
    }

    public void kurangiKuota(int jumlah) {
        kuota -= jumlah;
    }
}
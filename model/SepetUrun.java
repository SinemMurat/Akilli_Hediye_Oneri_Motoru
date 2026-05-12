package com.hediye.model;

public class SepetUrun {
    private Urun urun;
    private int yildiz; // 0-5
    private boolean puanVerildi;

    public SepetUrun(Urun urun) {
        this.urun = urun;
        this.yildiz = 0;
        this.puanVerildi = false;
    }

    public Urun getUrun() { return urun; }
    public int getYildiz() { return yildiz; }
    public void setYildiz(int yildiz) { this.yildiz = Math.max(0, Math.min(5, yildiz)); }
    public boolean isPuanVerildi() { return puanVerildi; }
    public void setPuanVerildi(boolean puanVerildi) { this.puanVerildi = puanVerildi; }
}

package api;

public class Ticket {
    private int id_miejsca;
    private int id_seansu;
    private double cena;

    public Ticket() {
        // Pusty konstruktor wymagany przez deserializację JSON
    }

    public Ticket(int id_miejsca, int id_seansu, double cena) {
        this.id_miejsca = id_miejsca;
        this.id_seansu = id_seansu;
        this.cena = cena;
    }

    public int getId_miejsca() {
        return id_miejsca;
    }

    public int getId_seansu() {
        return id_seansu;
    }

    public double getCena() {
        return cena;
    }
}

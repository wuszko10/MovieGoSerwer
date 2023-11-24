package api;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class BookResponse {

    private int userId;
    private double price;
    private List<Ticket> ticketList;

    public BookResponse() {
        // Pusty konstruktor wymagany przez deserializację JSON
    }

    public BookResponse(int userId, double price, List<Ticket> ticketList) {
        this.userId = userId;
        this.price = price;
        this.ticketList = ticketList;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Ticket> getTicketList() {
        return ticketList;
    }

    public void setTicketList(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }
}

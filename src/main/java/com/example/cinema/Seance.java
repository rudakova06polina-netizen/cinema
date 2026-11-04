package com.example.cinema;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seances")
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Укажите название фильма")
    @Column(name = "film_title", nullable = false)
    private String filmTitle;

    // Вариант Н–Т: поле называется кириллицей
    @NotBlank(message = "Укажите киностудию")
    @JsonProperty("киностудия")
    @Column(name = "киностудия", nullable = false)
    private String киностудия;

    @NotNull(message = "Укажите дату и время сеанса")
    @Column(name = "session_date_time", nullable = false)
    private LocalDateTime sessionDateTime;

    @Min(value = 1, message = "Билетов на сеанс должно быть не меньше 1")
    @Column(name = "tickets_total", nullable = false)
    private int ticketsTotal;

    @Min(value = 0, message = "Проданных билетов не может быть меньше 0")
    @Column(name = "tickets_sold", nullable = false)
    private int ticketsSold;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilmTitle() { return filmTitle; }
    public void setFilmTitle(String filmTitle) { this.filmTitle = filmTitle; }
    public String getКиностудия() { return киностудия; }
    public void setКиностудия(String киностудия) { this.киностудия = киностудия; }
    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public void setSessionDateTime(LocalDateTime sessionDateTime) { this.sessionDateTime = sessionDateTime; }
    public int getTicketsTotal() { return ticketsTotal; }
    public void setTicketsTotal(int ticketsTotal) { this.ticketsTotal = ticketsTotal; }
    public int getTicketsSold() { return ticketsSold; }
    public void setTicketsSold(int ticketsSold) { this.ticketsSold = ticketsSold; }
}

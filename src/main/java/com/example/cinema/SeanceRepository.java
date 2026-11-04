package com.example.cinema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // Сортировка по двум полям: дата сеанса, затем название фильма
    List<Seance> findAllByOrderBySessionDateTimeAscFilmTitleAsc();

    @Query("""
           select s from Seance s
           where lower(s.filmTitle) like lower(concat('%', :q, '%'))
              or lower(s.киностудия) like lower(concat('%', :q, '%'))
           order by s.sessionDateTime asc, s.filmTitle asc
           """)
    List<Seance> search(@Param("q") String q);

    List<Seance> findBySessionDateTimeBetween(LocalDateTime from, LocalDateTime to);
}

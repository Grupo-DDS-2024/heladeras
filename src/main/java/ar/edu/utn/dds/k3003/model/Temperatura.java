package ar.edu.utn.dds.k3003.model;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "temperatura")
public class Temperatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Getter
    private Integer temperatura;

    @JoinColumn(name = "heladera_id", nullable = false)
    @Getter
    private Integer heladeraId;


    @Column //algo hay de Locale, ver..
    @Getter
    private LocalDateTime fechaMedicion;

    public Temperatura(Integer temperatura, Integer heladeraId, LocalDateTime fechaMedicion) {
        this.temperatura = temperatura;
        this.heladeraId = heladeraId;
        this.fechaMedicion = fechaMedicion;
    }

    public Temperatura() {

    }
}

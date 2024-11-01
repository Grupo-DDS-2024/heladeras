package ar.edu.utn.dds.k3003.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "heladera")
public class Heladera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Integer id;

    //@ElementCollection
    //@CollectionTable(name = "qr_heladera", joinColumns = @JoinColumn(name = "heladera_id"))
    //@Column(name = "qr")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "heladera_viandas", joinColumns = @JoinColumn(name = "heladera_id"))
    @Column(name = "vianda")
    private Collection<String> viandas;

    @Column(name = "nombre")
    @Getter
    private String nombre;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "heladeraId")
    @Getter
    private List<Temperatura> temperaturas;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "heladeraId")
    @Getter
    private List<ColaboradoresSuscritos> colaboradoresSuscritos;

    @Column
    @Getter
    private Integer cantidadDeViandas;

    @Enumerated(EnumType.STRING)
    @Getter
    private EstadoHeladera estadoHeladera;

    public Heladera() {
        super();
    }

    public Heladera(Integer id, String nombre) {
        super();
        this.id = id;
        this.nombre = nombre;
        this.viandas = new ArrayList<>();
        this.temperaturas = new LinkedList<>();
        this.colaboradoresSuscritos = new ArrayList<>();
        this.estadoHeladera = EstadoHeladera.ACTIVA;

    }

    public Heladera(String nombre, Integer cantidadDeViandas) {
        super();
        this.nombre = nombre;
        this.viandas = new ArrayList<>();
        this.temperaturas = new LinkedList<>();
        this.cantidadDeViandas = cantidadDeViandas;
        this.colaboradoresSuscritos = new ArrayList<>();
        this.estadoHeladera = EstadoHeladera.ACTIVA;

    }

    public void guardar(String qrVianda) {
        this.viandas.add(qrVianda);
    }

    public void retirar(String qrVianda) {
        if (this.viandas.contains(qrVianda)) {
            this.viandas.remove(qrVianda);
        } else {
            throw new NoSuchElementException();
        }
    }

    public Integer getCantidadViandas() {
        return this.viandas.size();
    }

    public void setTemperatura(Temperatura temperatura) {
        //this.temperaturas.addFirst(temperatura);
        this.temperaturas.add(0, temperatura);
    }

    public void marcarInactiva(){
        this.estadoHeladera = EstadoHeladera.INACTIVA;
    }

    public void marcarActiva() {
        this.estadoHeladera = EstadoHeladera.ACTIVA;
    }

    public LocalDateTime getFechaMedicion() {
        return this.temperaturas.get(0).getFechaMedicion();
    }

    public Temperatura getUltimaTemperatura() {
        return this.temperaturas.get(0);
    }

}

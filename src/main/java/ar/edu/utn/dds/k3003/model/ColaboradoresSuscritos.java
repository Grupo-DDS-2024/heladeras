package ar.edu.utn.dds.k3003.model;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "colaboradores_sucritos")
@Getter
public class ColaboradoresSuscritos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_colaborador")
    private Long colaborador_id;
    @Column(name = "heladeraId",nullable = false)
    private Integer heladeraId;
    @Column
    private int cantMinima = -1;
    @Column
    private int viandasDisponibles = -1;
    @Column
    private boolean notificarDesperfecto = false;


    public ColaboradoresSuscritos(Long colaborador_id, Integer heladeraId, int cantMinima, int viandasDisponibles, boolean notificarDesperfecto) {
        this.colaborador_id = colaborador_id;
        this.heladeraId = heladeraId;
        this.cantMinima = cantMinima;
        this.viandasDisponibles = viandasDisponibles;
        this.notificarDesperfecto = notificarDesperfecto;
    }

    public ColaboradoresSuscritos() {

    }
}

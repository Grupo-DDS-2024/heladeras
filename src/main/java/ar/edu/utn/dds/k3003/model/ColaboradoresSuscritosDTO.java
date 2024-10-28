package ar.edu.utn.dds.k3003.model;

import lombok.Getter;

import javax.persistence.*;

@Getter
public class ColaboradoresSuscritosDTO {
    private Long colaborador_id;
    private int heladera_id;
    private int cantMinima = -1;
    private int viandasDisponibles = -1;
    private boolean notificarDesperfecto = false;

    public ColaboradoresSuscritosDTO(Long colaborador_id, int heladera_id, int cantMinima, int viandasDisponibles, boolean notificarDesperfecto) {
        this.colaborador_id = colaborador_id;
        this.heladera_id = heladera_id;
        this.cantMinima = cantMinima;
        this.viandasDisponibles = viandasDisponibles;
        this.notificarDesperfecto = notificarDesperfecto;
    }

    public ColaboradoresSuscritosDTO() {
    }
}

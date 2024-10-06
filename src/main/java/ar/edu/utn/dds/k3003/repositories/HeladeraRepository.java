package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Heladera;
import lombok.Getter;

import java.util.*;


public class HeladeraRepository {


    @Getter
    private Collection<Heladera> heladeras;
    private Collection<Integer> idsHeladeras;

    private static Integer secuencia = 0;

    private Integer nextId() {
        secuencia = secuencia + 1;
        return secuencia;
    }

    public HeladeraRepository() {
        this.heladeras = new ArrayList<>();
        this.idsHeladeras = new ArrayList<>();
    }

    public Heladera save(Heladera heladera) {
        if (!this.existeLaHeladera(heladera.getId())) {
            if (heladera.getId() == null) {
                heladera.setId(nextId());
            }
            this.heladeras.add(heladera);
            this.idsHeladeras.add(heladera.getId());
        }
        return heladera;
    }

    public Heladera findById(Integer id) {
        /*
        Heladera heladera;
        if (this.existeLaHeladera(id)) {
            heladera = this.getHeladera(id);
        } else {
            heladera = new Heladera(id);
        }
        return heladera;
        */

        // Start Test.
        return this.heladeras.stream().filter(heladera -> heladera.getId().equals(id)).findFirst().get();
        // End Test.

    }

    private boolean existeLaHeladera(Integer heladeraId) {
        //return this.idsHeladeras.contains(heladeraId); // podría no tener una lista con los ids e iterar como en getHeladera()..

        // Start Test.
        return this.heladeras.stream().anyMatch(x -> x.getId().equals(heladeraId));
        // End Test.
    }

    private Heladera getHeladera(Integer heladeraId) {
        for (Heladera heladera : this.heladeras) {
            if (heladera.getId().equals(heladeraId)) {
                return heladera;
            }
        }
        return null;
    }

    public Collection<Heladera> all() {
        return this.heladeras;
    }

}

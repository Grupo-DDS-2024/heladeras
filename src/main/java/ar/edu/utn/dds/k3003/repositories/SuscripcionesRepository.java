package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.ColaboradoresSuscritos;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.NoSuchElementException;
import java.util.Objects;

public class SuscripcionesRepository {
    private EntityManagerFactory entityManagerFactory;

    public SuscripcionesRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void save(ColaboradoresSuscritos colaboradoresSuscritos){
        EntityManager entityManager= entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        if (Objects.isNull(colaboradoresSuscritos.getId())) {
            entityManager.persist(colaboradoresSuscritos);
        } else {
            colaboradoresSuscritos = entityManager.merge(colaboradoresSuscritos);
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public ColaboradoresSuscritos findById(Long id) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        ColaboradoresSuscritos suscripcion = entityManager.find(ColaboradoresSuscritos.class, id);
        if (Objects.isNull(suscripcion)) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new NoSuchElementException(String.format("No hay una suscripcion de id: %s", id));
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return suscripcion;
    }

    public void delete(Long id){
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        ColaboradoresSuscritos suscripcion = entityManager.find(ColaboradoresSuscritos.class,id);
        if (Objects.isNull(suscripcion)) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new NoSuchElementException(String.format("No hay una suscripcion de id: %s", id));
        }
        entityManager.remove(suscripcion);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

}


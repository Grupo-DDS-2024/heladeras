package ORM;

import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityManagerTest {

    static EntityManagerFactory entityManagerFactory ;
    EntityManager entityManager ;

    @BeforeAll
    public static void setUpClass() throws Exception {
        entityManagerFactory = Persistence.createEntityManagerFactory("pruebadb");
    }
    @BeforeEach
    public void setup() throws Exception {
        entityManager = entityManagerFactory.createEntityManager();
    }
    @Test
    public void testConectar() {
// vacío, para ver que levante el ORM
    }

    @Test
    public void testGuardarYRecuperarHeladera() throws Exception {
        HeladeraDTO heladeraDTO = new HeladeraDTO("Heladera32");
        Heladera heladera1 = new Heladera(heladeraDTO.getId(), heladeraDTO.getNombre());
        entityManager.getTransaction().begin();
        entityManager.persist(heladera1);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        Heladera heladera2 = entityManager.find(Heladera.class, 1);

        assertEquals(heladera1.getId(), heladera2.getId()); // también puede redefinir el equals
    }
}

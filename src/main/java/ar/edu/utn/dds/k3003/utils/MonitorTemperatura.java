package ar.edu.utn.dds.k3003.utils;

import ar.edu.utn.dds.k3003.app.Fachada;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.swing.text.html.parser.Entity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class MonitorTemperatura implements  Runnable{
    private final EntityManagerFactory entityManagerFactory;
    private Fachada fachada;
    private boolean running = true;

    public MonitorTemperatura(EntityManagerFactory entityManagerFactory,Fachada fachada) {
        this.entityManagerFactory = entityManagerFactory;
        this.fachada=fachada;

    }


    @Override
    public void run() {
        while (running){
            try{
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                entityManager.getTransaction().begin();

                Query query = entityManager.createQuery("SELECT t.heladeraId, MAX(t.fechaMedicion) FROM Temperatura t GROUP BY t.heladeraId");
                List<Object[]> resultados = query.getResultList();

                LocalDateTime tiempoActual = LocalDateTime.now();
                for(Object[] resultado:resultados){
                    Integer heladeraId = (Integer) resultado[0];
                    LocalDateTime ultimaMedicion = (LocalDateTime) resultado[1];

                    if(ChronoUnit.MINUTES.between(ultimaMedicion,tiempoActual) > 5){
                        fachada.fraude(heladeraId);
                    }
                }
                entityManager.getTransaction().commit();
                entityManager.close();

                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        this.running=false;
    }
}

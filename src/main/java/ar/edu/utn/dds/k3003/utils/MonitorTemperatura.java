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
    private boolean running;

    private int tiempo = Integer.parseInt(System.getenv().getOrDefault("tiempo", "1"));
    private int tiempoHilo = Integer.parseInt(System.getenv().getOrDefault("tiempo_hilo", "86400000"));

    public MonitorTemperatura(EntityManagerFactory entityManagerFactory,Fachada fachada) {
        this.entityManagerFactory = entityManagerFactory;
        this.fachada=fachada;
        this.running=true;


    }


    @Override
    public void run() {

        try{
            while (running){
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                try{

                    entityManager.getTransaction().begin();
                    System.out.println("Haciendo query");
                    Query query = entityManager.createQuery("SELECT t.heladeraId, MAX(t.fechaMedicion) FROM Temperatura t GROUP BY t.heladeraId");
                    List<Object[]> resultados = query.getResultList();
                    System.out.println("QUERY:"+resultados);
                    entityManager.getTransaction().commit();
                    entityManager.close();

                    LocalDateTime tiempoActual = LocalDateTime.now();
                    for(Object[] resultado:resultados){
                        Integer heladeraId = (Integer) resultado[0];
                        LocalDateTime ultimaMedicion = (LocalDateTime) resultado[1];

                        if(ChronoUnit.MINUTES.between(ultimaMedicion,tiempoActual) > tiempo){
                            fachada.fraude(heladeraId);
                            System.out.println("Hubo falla de conexión");
                        }
                    }


                }catch (Exception e){
                    if(entityManager.getTransaction().isActive()){
                        entityManager.getTransaction().rollback();
                    }
                    e.printStackTrace();
                }finally {
                    if(entityManager != null && entityManagerFactory.isOpen()){
                        entityManager.close();
                    }
                }

                Thread.sleep(60000);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Error en el hilo: "+e.getMessage());
        }
    }

    public void stop(){
        this.running=false;
    }
}

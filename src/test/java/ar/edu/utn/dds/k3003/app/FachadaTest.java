package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class FachadaTest {

    Fachada fachadaHeladera;
    Integer HELADERA_ID;
    String QR_VIANDA;
    Long COLABORADOR_ID;
    Integer TEMPERATURA;
    @Mock
    FachadaViandas fachadaViandas;

    @BeforeEach
    void setUp() {
        fachadaHeladera = new Fachada();
        HELADERA_ID = 32;
        QR_VIANDA = "ABC";
        COLABORADOR_ID = 10L;
        TEMPERATURA = 25;
        this.fachadaHeladera.setViandasProxy(fachadaViandas);
        this.fachadaHeladera.agregar(new HeladeraDTO(HELADERA_ID, "Heladera 32", 0));

    }

    @Test
    void agregar() {
        HeladeraDTO heladeraDTO = new HeladeraDTO(HELADERA_ID, "Heladera33", 0);
        HeladeraDTO heladeraDTOResultado = this.fachadaHeladera.agregar(heladeraDTO);
        Assertions.assertEquals(heladeraDTO.getId(), heladeraDTOResultado.getId());

    }

    @Test
    void depositar() {
        Mockito.when(this.fachadaViandas.buscarXQR(QR_VIANDA)).thenReturn(new ViandaDTO(QR_VIANDA, LocalDateTime.now(), EstadoViandaEnum.PREPARADA, COLABORADOR_ID, HELADERA_ID));
        this.fachadaHeladera.depositar(HELADERA_ID, QR_VIANDA);
        Mockito.verify(this.fachadaViandas, Mockito.description("no se marco la vianda como depositada")).modificarEstado(QR_VIANDA, EstadoViandaEnum.DEPOSITADA);
        Assertions.assertEquals(1, this.fachadaHeladera.cantidadViandas(HELADERA_ID));
    }

    @Test
    void cantidadViandas() {
        Mockito.when(this.fachadaViandas.buscarXQR(QR_VIANDA)).thenReturn(new ViandaDTO(QR_VIANDA, LocalDateTime.now(), EstadoViandaEnum.PREPARADA, COLABORADOR_ID, HELADERA_ID));
        this.fachadaHeladera.depositar(HELADERA_ID, QR_VIANDA);
        Mockito.when(this.fachadaViandas.buscarXQR("DEF")).thenReturn(new ViandaDTO("DEF", LocalDateTime.now(), EstadoViandaEnum.PREPARADA, COLABORADOR_ID, HELADERA_ID));
        this.fachadaHeladera.depositar(HELADERA_ID, "DEF");
        assertEquals(2, this.fachadaHeladera.cantidadViandas(HELADERA_ID));
    }

    @Test
    void retirar() {
        Mockito.when(this.fachadaViandas.buscarXQR(QR_VIANDA)).thenReturn(new ViandaDTO(QR_VIANDA, LocalDateTime.now(), EstadoViandaEnum.PREPARADA, COLABORADOR_ID, HELADERA_ID));
        this.fachadaHeladera.depositar(HELADERA_ID, QR_VIANDA);
        this.fachadaHeladera.retirar(new RetiroDTO(QR_VIANDA, "567", HELADERA_ID));
        Mockito.verify(this.fachadaViandas, Mockito.description("no se marco la vianda como retirada")).modificarEstado(QR_VIANDA, EstadoViandaEnum.RETIRADA);
        Assertions.assertEquals(0, this.fachadaHeladera.cantidadViandas(HELADERA_ID), "Las viandas no se agregaron correctamente");
    }

    @Test
    void temperatura() {
        TemperaturaDTO temperaturaDTO = new TemperaturaDTO(TEMPERATURA, HELADERA_ID, LocalDateTime.now());
        this.fachadaHeladera.temperatura(temperaturaDTO);
        Assertions.assertEquals(TEMPERATURA, this.fachadaHeladera.obtenerTemperaturas(HELADERA_ID).get(0).getTemperatura());
    }

    @Test
    void obtenerTemperaturas() {
        TemperaturaDTO temperaturaDTO = new TemperaturaDTO(10, HELADERA_ID, LocalDateTime.now());
        this.fachadaHeladera.temperatura(temperaturaDTO);
        TemperaturaDTO temperaturaDTO2 = new TemperaturaDTO(11, HELADERA_ID, LocalDateTime.now());
        this.fachadaHeladera.temperatura(temperaturaDTO2);
        Assertions.assertEquals(11, this.fachadaHeladera.obtenerTemperaturas(HELADERA_ID).get(0).getTemperatura());
        Assertions.assertEquals(10, this.fachadaHeladera.obtenerTemperaturas(HELADERA_ID).get(1).getTemperatura());
    }
}
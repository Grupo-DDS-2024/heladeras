package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.model.ColaboradoresSuscritos;
import ar.edu.utn.dds.k3003.model.ColaboradoresSuscritosDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class AgregarSuscriptorController {
    Fachada fachada;

    public AgregarSuscriptorController(Fachada fachada) {
        this.fachada = fachada;
    }

    public void agregarSuscriptor(Context context) throws JsonProcessingException {
        //try{
            //ColaboradoresSuscritosDTO colaboradoresSuscritosDTO = context.bodyAsClass(ColaboradoresSuscritosDTO.class);
            String requestBody = context.body();
            ObjectMapper objectMapper = new ObjectMapper();
            ColaboradoresSuscritosDTO colaboradoresSuscritosDTO = objectMapper.readValue(requestBody, ColaboradoresSuscritosDTO.class);
            Long colaborador_id = colaboradoresSuscritosDTO.getColaborador_id();
            Integer heladera_id = colaboradoresSuscritosDTO.getHeladera_id();
            int cantMinima = colaboradoresSuscritosDTO.getCantMinima();
            int viandasDisponibles = colaboradoresSuscritosDTO.getViandasDisponibles();
            boolean notificarDesperfecto = colaboradoresSuscritosDTO.isNotificarDesperfecto();
            fachada.agregarSuscriptor(colaborador_id,heladera_id,cantMinima,viandasDisponibles,notificarDesperfecto);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Suscripcion agregada correctamente");
            context.status(200).json(response);
        //} catch(Exception e){
        //    throw new BadRequestResponse("Error de solicitud.");
        // }
    }
}

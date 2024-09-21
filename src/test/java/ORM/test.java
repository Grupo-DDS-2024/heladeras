//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ORM;

import ar.edu.utn.dds.k3003.Evaluador;
import ar.edu.utn.dds.k3003.EvaluadorAPI;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class test {
    public test() {
    }

    @Test
    void testAPIHeladera() {
        try {
            ObjectMapper mapper = Evaluador.createObjectMapper();
            HttpClient client = HttpClient.newHttpClient();
            HeladeraDTO heladeraDTO = new HeladeraDTO("unaHeladera");
            HttpRequest request = this.createRequest("/heladeras").POST(BodyPublishers.ofString(mapper.writeValueAsString(heladeraDTO))).build();
            HttpResponse<String> send = client.send(request, BodyHandlers.ofString());
            HeladeraDTO heladera = (HeladeraDTO)mapper.readValue((String)send.body(), HeladeraDTO.class);
            HttpRequest request2 = this.createRequest("/heladeras/" + heladera.getId().toString()).GET().build();
            HttpResponse<String> send2 = client.send(request2, BodyHandlers.ofString());
            HeladeraDTO heladera2 = (HeladeraDTO)mapper.readValue((String)send2.body(), HeladeraDTO.class);
            Assertions.assertEquals(heladera, heladera2, "La heladera creada con el POST no es igual a la recuperada con el GET");
        } catch (Throwable var10) {
            throw new NoSuchElementException();
        }
    }

    private HttpRequest.Builder createRequest(String path) throws URISyntaxException {
        String baseUrl = "http://localhost:8080";
        return HttpRequest.newBuilder().uri(new URI(baseUrl + path));
    }
}

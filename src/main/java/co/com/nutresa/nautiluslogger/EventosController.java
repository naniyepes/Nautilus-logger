package co.com.nutresa.nautiluslogger;

import co.com.nutresa.nautiluslogger.solicituddespacho.SolicitudDespacho;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins= "*", allowedHeaders = "*")
@Controller
public class EventosController {

    @GetMapping(value="/getEventos", produces = MediaType.APPLICATION_JSON_VALUE)
    //http://localhost:8080/getEventos?codigo=holaDanilo
    public ResponseEntity<List <SolicitudDespacho>> getEvents(String codigo){
        List<SolicitudDespacho> solicitudes = new ArrayList<>();
        solicitudes.add(new SolicitudDespacho("SD1", "Primer Evento"));
        solicitudes.add(new SolicitudDespacho("SD2", "Segundo Evento"));
        return new ResponseEntity<>(solicitudes, HttpStatus.OK);
    }
}

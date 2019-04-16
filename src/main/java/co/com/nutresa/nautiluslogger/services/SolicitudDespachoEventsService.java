package co.com.nutresa.nautiluslogger.services;

import co.com.nutresa.nautiluslogger.solicituddespachoevent.SolicitudDespachoEvent;
import co.com.nutresa.nautiluslogger.solicituddespachoevent.SolicitudDespachoEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SolicitudDespachoEventsService {

    @Autowired
    private SolicitudDespachoEventRepository sdeRepository;

    @GetMapping(value="/getSolicitudesDespachoEvents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List <SolicitudDespachoEvent>> getSolicitudesDespachoEvents(String codigo){
        List<SolicitudDespachoEvent> solicitudes = new ArrayList<>();
        for (SolicitudDespachoEvent solicitudDespachoEvent : sdeRepository.findBySolicitudDespachoNumero(codigo)) {
            solicitudes.add(solicitudDespachoEvent);
        }
        return new ResponseEntity<>(solicitudes, HttpStatus.OK);
    }
}

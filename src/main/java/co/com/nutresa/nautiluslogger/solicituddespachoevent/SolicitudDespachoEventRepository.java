package co.com.nutresa.nautiluslogger.solicituddespachoevent;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SolicitudDespachoEventRepository extends MongoRepository<SolicitudDespachoEvent, String> {

    public List<SolicitudDespachoEvent> findBySolicitudDespachoNumero(String solicitudDespachoNumero);

}
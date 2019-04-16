package co.com.nutresa.nautiluslogger;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Evento que ocurre sobre una SolicitudDespacho
 */
public class SolicitudDespachoEvent {
    @Id
    private String id;

    private String event;

    private Date fechaEvento;

    private Date fechaRegistro;

    public SolicitudDespachoEvent() {}

    public SolicitudDespachoEvent(String id, String event, Date fechaEvento, Date fechaRegistro) {
        this.id = id;
        this.event = event;
        this.fechaEvento = fechaEvento;
        this.fechaRegistro = fechaRegistro;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Date fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}

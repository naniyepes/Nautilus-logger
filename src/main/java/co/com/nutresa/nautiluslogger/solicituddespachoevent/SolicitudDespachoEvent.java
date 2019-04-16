package co.com.nutresa.nautiluslogger.solicituddespachoevent;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class SolicitudDespachoEvent implements Serializable {

	private static final long serialVersionUID = 7918457534372916694L;

	@Id
	public String id;

	private String eventCode;

	private Date eventDate;

	private String solicitudDespachoNumero;

	private String eventGenerator;

	private String eventData;

	public SolicitudDespachoEvent(String eventCode, Date eventDate, String solicitudDespachoNumero, String eventGenerator, String eventData) {
		this.eventCode = eventCode;
		this.eventDate = eventDate;
		this.solicitudDespachoNumero = solicitudDespachoNumero;
		this.eventGenerator = eventGenerator;
		this.eventData = eventData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public String getSolicitudDespachoNumero() {
		return solicitudDespachoNumero;
	}

	public void setSolicitudDespachoNumero(String solicitudDespachoNumero) {
		this.solicitudDespachoNumero = solicitudDespachoNumero;
	}

	public String getEventGenerator() {
		return eventGenerator;
	}

	public void setEventGenerator(String eventGenerator) {
		this.eventGenerator = eventGenerator;
	}

	public String getEventData() {
		return eventData;
	}

	public void setEventData(String eventData) {
		this.eventData = eventData;
	}

	@Override
	public String toString() {
		;
		return String.format(
				new StringBuilder("SolicitudDespacho[eventCode=%s, eventDate='%s', solicitudDespachoNumero='%s'")
						.append("eventGenerator='%s', eventData='%s']").toString(),
				eventCode, eventDate, solicitudDespachoNumero, eventGenerator, eventData);
	}
	

}

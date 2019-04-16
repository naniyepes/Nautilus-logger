package co.com.nutresa.nautiluslogger.solicituddespacho;

import java.io.Serializable;

public class SolicitudDespacho implements Serializable {
	
	private static final long serialVersionUID = 7918457534372916694L;
	
	private String codigo;
	private String evento;		
	
	public SolicitudDespacho(String codigo, String evento) {
		super();
		this.codigo = codigo;
		this.evento = evento;
	}
	public SolicitudDespacho() {
		super();
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	
	

}

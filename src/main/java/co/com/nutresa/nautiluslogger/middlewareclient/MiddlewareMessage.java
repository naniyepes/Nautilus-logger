package co.com.nutresa.nautiluslogger.middlewareclient;

import java.io.Serializable;

/**
 * Mensaje que se env�a o lee del middleware
 * @author pacosta
 * @since 10-Ago-2018
 */
public class MiddlewareMessage implements Serializable {
  private static final long serialVersionUID = -7679076000425890922L;

  /**topic del mensaje*/
  private String topic;
  /**Clave que identifica el mensaje*/
  private String key;
  /**El mensaje en si, puede ser cualquier texto, por ejemplo un json o un objeto serializado, es responsabilidad de quien lo lee saber que tipo de mensaje es*/
  private String data;
  /**Consecutivo del mensaje*/
  private long consecutivo;
  
  /**
   * Contructor
   * @param key
   * @param data
   * @param consecutivo
   * @author Productora de Software SAS
   *         Pablo Andr�s Acosta
   * @since 14/08/2017
   */
  public MiddlewareMessage(String key, String data, long consecutivo, String topic) {
    this.key = key;
    this.data = data;
    this.consecutivo = consecutivo;
    this.topic = topic;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public long getConsecutivo() {
    return consecutivo;
  }

  public void setConsecutivo(long consecutivo) {
    this.consecutivo = consecutivo;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  @Override
  public String toString() {
    return "Key: " + key + ", Topic: " + topic + ", Data: " + data;
  }
  
}

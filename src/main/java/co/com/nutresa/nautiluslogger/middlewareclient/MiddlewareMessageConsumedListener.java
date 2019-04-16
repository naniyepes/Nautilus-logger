package co.com.nutresa.nautiluslogger.middlewareclient;


/**
 * Define una operación para procesar los mensajes leidos del middleware
 * @author Productora de Software SAS
 *         Pablo Andrés Acosta
 * @since 11-Abr-2019
 */
public interface MiddlewareMessageConsumedListener {

  /**
   * procesa un mensaje que fue leido del middlewware
   * @param message
   * @author Productora de Software SAS
   *         Pablo Andrés Acosta
   * @since 11-Abr-2019
   */
  void process(MiddlewareMessage message);
}

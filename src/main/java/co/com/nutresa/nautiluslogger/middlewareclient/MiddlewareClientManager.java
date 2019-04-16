package co.com.nutresa.nautiluslogger.middlewareclient;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import co.com.nutresa.middleware.MiddlewareMessage;
import co.com.nutresa.middleware.TopicListManager;
import co.com.nutresa.middleware.TopicListenerRegister;
import co.com.nutresa.sco.middleware.topic.AdminTopicsReglaNegocio;
import co.com.psl.jbrain.PslError;
import co.com.psl.jbrain.application.Application;
import co.com.psl.jbrain.security.UserSessionClientHandler;
import co.com.psl.jbrain.util.JBrainThread;

/**
 * Singleton Encargado de la comunicaci�n con el MiddleWare. Ofrece servicios de publicaci�n de mensajes y de lectura de los mismos, ambas operaciones para un tema (topic) particular. Esta clase debe
 * asegurar la publicaci�n de los mensajes, para eso utilizar� la cola persistente de JBrain.
 * 
 * @author Productora de Software SAS Pablo Andr�s Acosta
 * @since 10-Ago-2017
 */
public class MiddlewareClientManager implements TopicListManager, TopicListenerRegister {

  /**Mapa de objetos de esta clase por tenant, para manejo del singleton en ambiente multitenant*/  
  private static HashMap<String, MiddlewareClientManager> middlewareClientManagerByTenantIdMap;
  /**Se encarga del consumo de los mensajes del middleware*/
  private MiddlewareClientDaemonReader mwClientDaemonReader;
  /**Se encarga de la publicaci�n de los mensajes en el middleware*/
  private MiddlewareClientDaemonWriter mwClientDaemonWriter;
  
  /**Encargado del commit de los mensajes del middleware en la cola persistente*/
  private MiddlewareMessageCommitManager commitMessageManager;
  
  private MiddlewareClientManager() {
    super();
    String commitManagerClass = Application.getInstance().getProperty("application.MiddlewareMessagesCommitManager", "");
    if(StringUtils.isEmpty(commitManagerClass)){
      commitMessageManager = new MiddlewareCommitManagerDefault();
    }else{
      try {
        commitMessageManager = (MiddlewareMessageCommitManager) Class.forName(commitManagerClass).newInstance();
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        throw new PslError(e);
      }
    }
    mwClientDaemonWriter = new MiddlewareClientDaemonWriter();
    mwClientDaemonReader = new MiddlewareClientDaemonReader(new ArrayList<>());

    // Registra los TOPICS y los LISTENERS usando la regla de negocio definida en la aplicaci�n
    AdminTopicsReglaNegocio.getAdminTopicsReglaNegocio().getManagerObject().registerTopics(this);
    // Registra los LISTENERS usando la regla de negocio definida en la aplicaci�n
    AdminTopicsReglaNegocio.getAdminTopicsReglaNegocio().getManagerObject().registerTopicListeners(this);
    
    mwClientDaemonWriter.start();
    mwClientDaemonReader.start();
  }

  /**
   * Retorna la �nica instancia de esta clase
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 11/08/2017
   */
  public static synchronized MiddlewareClientManager getInstance() {
    String tenantId = UserSessionClientHandler.getInstance().getUserSession().getTenantId();
    if(middlewareClientManagerByTenantIdMap == null){
      middlewareClientManagerByTenantIdMap = new HashMap<>();
      middlewareClientManagerByTenantIdMap.put(tenantId, new MiddlewareClientManager());
    }else if(middlewareClientManagerByTenantIdMap.get(tenantId) == null){
      middlewareClientManagerByTenantIdMap.put(tenantId, new MiddlewareClientManager());
    }
    return middlewareClientManagerByTenantIdMap.get(tenantId);
  }

  /**
   * Reinicia el canala de salida asociado a un topic
   * @param topic
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 18/08/2017
   */
  public void restartOutputChannelByTopic(String topic) {
    MiddlewareClientQueueManager.getInstance().restartOutputQueueByTopic(topic);
  }

  /**
   * Reinicia el canala de entrada asociado a un topic
   * @param topic
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 18/08/2017
   */
  public void restartInputChannelByTopic(String topic) {
    MiddlewareClientQueueManager.getInstance().restartInputQueueByTopic(topic);
  }

  /**
   * Envia el mensaje al middleware
   * @param topic
   * @param message
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 10-Ago-2017
   */
  public void send(String topic, MiddlewareMessage message) {
    addTopic(topic);
    MiddlewareClientQueueManager.getInstance().send(topic, message);
  }

  /**
   * lee del Middleware el siguiente mensaje disponible para un topic espec�fico, si no hay mensajes retorna null
   * @return el siguiente mensaje disponible para un topic espec�fico, si no hay mensajes retorna null
   * @param topic
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 10-Ago-2017
   */
  public MiddlewareMessage nextMessage(String topic) {
    return MiddlewareClientQueueManager.getInstance().nextMessage(topic);
  }

  /**
   * lee del Middleware el siguiente mensaje disponible para un topic espec�fico, Las lecturas se hacen frecuentemente durante el tiempo definido en @time, si despues de ese tiempo no encontr� nada
   * entonces retorna null.
   * @return el siguiente mensaje disponible para un topic espec�fico, si no hay mensajes despues del tiempo definido retorna null
   * @param topic
   * @param time, tiempo en milisegundos que espera antes de retornar as� sea vac�o
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 10-Ago-2017
   */
  public MiddlewareMessage nextMessageWaiting(String topic, long time) {
//    return MiddlewareClientQueueManager.getInstance().nextMessageWaiting(topic,time);
    MessageWrapper mw = new MessageWrapper();
    JBrainThread reader = new JBrainThread(){
      @Override
      public void runThread() {
        MiddlewareMessage message = null;
        while (message == null) {
          try {
            Thread.sleep(100L);
          } catch (InterruptedException e) {
            // no se hace nada
          }
          message = MiddlewareClientQueueManager.getInstance().nextMessage(topic);
          mw.setMessage(message);
        }
      }
    };
    reader.start();
    try {
      reader.join(time);
    } catch (InterruptedException e) {
      // no se hace nada con esta excepci�n
    }
    return mw.getMessage();
  }  

  class MessageWrapper {
    private MiddlewareMessage message;

    public void setMessage(MiddlewareMessage message) {
      this.message = message;
    }

    public MiddlewareMessage getMessage() {
      return message;
    }

  }

  /**
   * Marca el mensaje como procesadoy lo elimina de la cola persistente
   * @param message
   * @param topic
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 11/08/2017
   */
  public void commitMessageProcessing(MiddlewareMessage message, String topic) {
    commitMessageManager.commitMessageProcessing(message, topic);
  }

  /**
   * Agrega un topic a la lista
   * @param topic
   * @author Productora de Software SAS Pablo Andr�s Acosta
   * @since 17/08/2017
   */
  @Override
  public synchronized void addTopic(String topic) {
    mwClientDaemonReader.addTopic(topic);
  }

  /**
   * Agrega a la cola del middleware el Topic y el {@link MiddlewareMessageConsumedListener}.
   * @param topic
   * @param listener
   */
  @Override
  public void addMiddlewareMessageQueuedListener(String topic, MiddlewareMessageConsumedListener listener) {
    mwClientDaemonReader.setMiddlewareTopicListener(topic, listener);
  }

}

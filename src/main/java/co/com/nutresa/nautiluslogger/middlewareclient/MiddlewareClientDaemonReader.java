    package co.com.nutresa.nautiluslogger.middlewareclient;

    import java.time.Duration;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.ConcurrentModificationException;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;

    import org.apache.commons.lang3.StringUtils;
    import org.apache.kafka.clients.consumer.ConsumerConfig;
    import org.apache.kafka.clients.consumer.ConsumerRecord;
    import org.apache.kafka.clients.consumer.ConsumerRecords;
    import org.apache.kafka.clients.consumer.KafkaConsumer;
    import org.apache.kafka.common.TopicPartition;
    import org.apache.kafka.common.serialization.StringDeserializer;
    import org.springframework.beans.factory.annotation.Autowired;

    /**
     * Daemon que constantemente está leyendo mensajes del middleware y los procesa
     * @author Productora de Software SAS
     *         Pablo Andr?s Acosta
     * @since 14/08/2017
     */
    public class MiddlewareClientDaemonReader extends Thread{ que tal tener un objeto de esta clase para un solo topic?

      /**GroupId de los consumers del middleware*/
      public static final String GROUP_ID = "SCO_CONSUMER_GROUP";
      /**URL del middleware*/
      private static final String KAFKA_SERVER = "localhost:9092";
      private static final String MAX_MSG_SIZE = "2000000";
      /**Consumidor para traer los mensajes del middleware*/
      private KafkaConsumer<String, String> consumer;
      /**Lista de topics a los que est? suscrita esta aplicaci?n (o tenant en caso de estar en un ambiente multitenant)*/
      private Collection<String> topicList;
      /**Determina si se han agregado o eliminado topics de la lista*/
      private boolean topicListDirty = false;

      @Autowired
      private TopicOffsetRepository topicOffsetRepository;

      /**Mapa cuya clave es el topic y el valor es el listener que deben ser notificado cuando un mensaje de dicho topic se lee del mw*/
      private HashMap<String, MiddlewareMessageConsumedListener> messageQueuedListener;

      MiddlewareClientDaemonReader(Collection<String> topicList) {
        this.topicList = topicList;
        Map<String, Object> conf = new HashMap<>();
        conf.put("bootstrap.servers", KAFKA_SERVER);
        conf.put("group.id", GROUP_ID + new Date().getTime());
        conf.put("key.deserializer", StringDeserializer.class.getName());
        conf.put("value.deserializer", StringDeserializer.class.getName());
        conf.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try{
          Long.valueOf(MAX_MSG_SIZE);
          conf.put("max.request.size", MAX_MSG_SIZE);
        }catch(NumberFormatException e){
          throw new RuntimeException(e);
        }
        consumer = new KafkaConsumer<>(conf);
        messageQueuedListener = new HashMap<>();
      }

      /**
       * Asigna el listener al topic
       * @param topic, identificador del topic en el que est? interesado el listener
       * @param listener
       * @author Productora de Software SAS
       *         Pablo Andr?s Acosta
       * @since 11/04/2019
       */
      public void setMiddlewareTopicListener(String topic, MiddlewareMessageConsumedListener listener){
        messageQueuedListener.put(topic, listener);
      }

      /**
       * Agrega un topic a la lista si es que no existe ya
       * @param topic
       * @author Productora de Software SAS
       *         Pablo Andr?s Acosta
       * @since 17/08/2017
       */
      public synchronized void addTopic(String topic){
        if(!topicList.contains(topic)){
          topicList.add(topic);
          topicListDirty = true;
        }
      }

      @Override
      public void run() {
        while(true) {
          try {
            if(topicListDirty){
              try{
                Collection<TopicPartition> partitions = new ArrayList();
                topicList.forEach((String topic) -> {partitions.add(new TopicPartition(topic, 0));});
                consumer.assign(partitions);
                inicializarTopicOffset();
                topicListDirty = false;
              }catch(ConcurrentModificationException e){
                try {sleep(10L);} catch (InterruptedException e1) {}
                continue;
              }
            }else if(topicList.isEmpty() || consumer.assignment() == null || consumer.assignment().isEmpty()){
              try {sleep(1000L);} catch (InterruptedException e1) {}
              continue;
            }

            //consume los mensajes de los topics de la suscripcion del consumer
            ConsumerRecords<String, String> messages = consumer.poll(Duration.ofMillis(100L));
            //mapa de mensajes leidos por cada topic, se usará para notificar a los listeners posteriormente
            HashMap<String, Collection<MiddlewareMessage>> messageQueuedMapByTopic = new HashMap<>();
            for(ConsumerRecord<String, String> message: messages) {
              if(message != null && message.topic() != null){
                MiddlewareMessage messageToProcess =
                        new MiddlewareMessage(message.key(), message.value(), new Date().getTime(), message.topic());
                try {
                  processMessage(messageToProcess);
                }catch (Throwable t){
                  //se guarda el mensaje con el respectivo error para ser procesado posteriormente TODO
                }finally {
                  //dado que el mensaje fue almacenado o procesado entonces se mueve el offset del topic
                  updateOffsetByTopic(message.topic());
                }
              }
            }
          } catch (Exception | Error e) {
            //cualquier excepci?n lanzada por el proceso de consumo del los mensajes debera ser logeada pero no debe detener el sistema
            e.printStackTrace();
          }
        }
      }

      /**
       * Procesa un mensaje leido del middleware
       * @param message
       */
      private void processMessage(MiddlewareMessage message){
        MiddlewareMessageConsumedListener listener = messageQueuedListener.get(message.getTopic());
        if(listener != null){
          listener.process(message);
        }
      }

      /**
       * Para cada topic del atributo topicList se asigna el offset del siguiente mensaje a leer
       * @author Pablo Andres Acosta Amaya PSL
       * @since  22/11/2018
       */
      private void inicializarTopicOffset(){
        topicList.forEach((String topic) -> {
          if(!consumer.assignment().isEmpty()){
            long nextOffset = getNextOffsetByTopic(topic);
            consumer.seek(new TopicPartition(topic, 0), nextOffset);
          }
        });
      }

      /**
       * Retorna el offset del siguiente mensaje a leer de un topic dado, si al buscar en DB no se encuentra el TopicOffset del topic dado entonces se crea con valor 1
       * @param topic
       * @return el offset del siguiente mensaje a leer
       * @author Pablo Andres Acosta Amaya PSL
       * @since  11/04/2018
       */
      private long getNextOffsetByTopic(String topic){
        TopicOffset topicOffset = topicOffsetRepository.findByTopicName(topic);
        if(topicOffset == null){
          topicOffset = new TopicOffset(topic, 0);
          topicOffsetRepository.save(topicOffset);
        }
        return topicOffset.getOffset();
      }

      /**
       * Actualiza en base de datos el offset del topic, asignando el valor del siguiente mensaje que ha de ser leido
       * @param topic
       * @author Pablo Andres Acosta Amaya PSL
       * @since  11/04/2019
       */
      private void updateOffsetByTopic(String topic){
        if(StringUtils.isEmpty(topic)){
          throw new RuntimeException("No se puede actulizar el offset deun topic nulo o vac?o");
        }
        //TODO revisar lo de la partici?n 0, no se si funcione cuando hayan muchos datos
        Long offset = consumer.position(new TopicPartition(topic, 0));
        TopicOffset topicOffset = topicOffsetRepository.findByTopicName(topic);
        if(topicOffset == null){
          topicOffset = new TopicOffset(topic, offset);
        }
        topicOffset.setOffset(offset);
        topicOffsetRepository.save(topicOffset);
      }

    }










package co.com.nutresa.nautiluslogger.middlewareclient;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface TopicOffsetRepository extends MongoRepository<TopicOffset, String> {

    TopicOffset findByTopicName(String firstName);

}
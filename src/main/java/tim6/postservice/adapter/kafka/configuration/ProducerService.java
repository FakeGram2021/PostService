package tim6.postservice.adapter.kafka.configuration;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tim6.postservice.adapter.kafka.models.KafkaMessage;
import tim6.postservice.adapter.kafka.models.payloads.PostHistoryPayload;

@Service
public class ProducerService {

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    public ProducerService(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String userId, Collection<String> postTags) {
        this.kafkaTemplate.send(
                "agent_service_topic",
                new KafkaMessage(
                        "POST_HISTORY", new PostHistoryPayload(userId, new ArrayList<>(postTags))));
    }
}

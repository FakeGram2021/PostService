package tim6.postservice.adapter.kafka.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class KafkaMessage {

    private String key;

    private Object value;
}

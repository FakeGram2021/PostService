package tim6.postservice.adapter.kafka.models.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BlockPayload {

    private String blockerId;

    private String blockTarget;

    private boolean apply;
}

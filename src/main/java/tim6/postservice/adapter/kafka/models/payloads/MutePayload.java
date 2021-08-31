package tim6.postservice.adapter.kafka.models.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MutePayload {

    private String muterId;

    private String muteTargetId;

    private boolean apply;
}

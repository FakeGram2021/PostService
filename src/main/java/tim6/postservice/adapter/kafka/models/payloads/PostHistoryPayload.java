package tim6.postservice.adapter.kafka.models.payloads;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostHistoryPayload {

    private String userId;

    private List<String> postHistory;
}

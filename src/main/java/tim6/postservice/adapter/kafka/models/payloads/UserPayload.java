package tim6.postservice.adapter.kafka.models.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserPayload {

    private String id;

    private String username;

    private String userAvatar;

    private boolean publicAccount;
}

package tim6.postservice.adapter.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfoDTO {

    private String id;

    private String username;

    private String userAvatar;
}

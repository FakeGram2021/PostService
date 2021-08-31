package tim6.postservice.adapter.http.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CommentGetDTO {

    private String id;

    private UserInfoDTO commenter;

    private Date commentDate;

    private String comment;
}

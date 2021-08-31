package tim6.postservice.adapter.http.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostGetDTO {

    private String id;

    private String imageUrl;

    private String description;

    private UserInfoDTO poster;

    private Date postDate;

    private Set<String> tags;

    private Set<UserInfoDTO> userTags;

    private Set<String> likes;

    private Set<String> dislikes;

    private Set<String> favorites;

    private List<CommentGetDTO> comments;
}

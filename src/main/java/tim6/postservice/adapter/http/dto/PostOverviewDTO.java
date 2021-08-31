package tim6.postservice.adapter.http.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostOverviewDTO {

    private String id;

    private String imageUrl;

    private String description;

    private UserInfoDTO poster;

    private Date postDate;

    private Set<String> tags = new HashSet<>();

    private Set<UserInfoDTO> userTags = new HashSet<>();

    private int likes_count;

    private int dislikes_count;

    private int favorites_count;

    private int comments_count;
}

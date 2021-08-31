package tim6.postservice.domain.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Post {

    @Id private String id;

    @Field(type = FieldType.Text)
    private String imageUrl;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Nested)
    private UserInfo poster;

    @Field(type = FieldType.Keyword)
    private String posterId;

    @Field(type = FieldType.Date)
    private Date postDate;

    @Field(type = FieldType.Keyword)
    private Set<String> tags = new HashSet<>();

    @Field(type = FieldType.Auto)
    private Set<UserInfo> userTags = new HashSet<>();

    @Field(type = FieldType.Keyword)
    private Set<String> likes = new HashSet<>();

    @Field(type = FieldType.Keyword)
    private Set<String> dislikes = new HashSet<>();

    @Field(type = FieldType.Keyword)
    private Set<String> favorites = new HashSet<>();

    @Field(type = FieldType.Object)
    private List<Comment> comments = new ArrayList<>();

    public Post(
            final String id,
            final String imageUrl,
            final String description,
            final UserInfo poster,
            final Date postDate,
            final Set<String> tags,
            final Set<UserInfo> userTags,
            final Set<String> likes,
            final Set<String> dislikes,
            final Set<String> favorites,
            final List<Comment> comments) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
        this.poster = poster;
        this.posterId = poster.getId();
        this.postDate = postDate;
        this.tags = tags;
        this.userTags = userTags;
        this.likes = likes;
        this.dislikes = dislikes;
        this.favorites = favorites;
        this.comments = comments;
    }

    public Post(
            final String id,
            final String imageUrl,
            final String description,
            final UserInfo poster,
            final Date postDate,
            final Set<String> tags,
            final Set<UserInfo> userTags) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
        this.poster = poster;
        this.posterId = poster.getId();
        this.postDate = postDate;
        this.tags = tags;
        this.userTags = userTags;
    }
}

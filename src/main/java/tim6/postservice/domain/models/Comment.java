package tim6.postservice.domain.models;

import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Comment {

    @Id
    @Field(type = FieldType.Text)
    private String id;

    @Field(type = FieldType.Object)
    private UserInfo commenter;

    @Field(type = FieldType.Date)
    private Date commentDate;

    @Field(type = FieldType.Text)
    private String comment;

    public Comment(final String id, final UserInfo commenter, final String comment) {
        this.id = id;
        this.commenter = commenter;
        this.commentDate = new Date();
        this.comment = comment;
    }

    public Comment(final User commenter, final String comment) {
        this.id = UUID.randomUUID().toString();
        this.commenter = new UserInfo(commenter);
        this.commentDate = new Date();
        this.comment = comment;
    }

    public Comment(final UserInfo commenter, final String comment) {
        this.id = UUID.randomUUID().toString();
        this.commenter = commenter;
        this.commentDate = new Date();
        this.comment = comment;
    }
}

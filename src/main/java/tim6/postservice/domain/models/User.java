package tim6.postservice.domain.models;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User {

    @Id
    @Field(type = FieldType.Text, store = true)
    private String id;

    @Field(type = FieldType.Text, store = true)
    private String username;

    @Field(type = FieldType.Text, store = true)
    private String userAvatar;

    @Field(type = FieldType.Boolean, store = true)
    private boolean publicAccount;

    @Field(type = FieldType.Text, store = true)
    private Set<String> following;

    @Field(type = FieldType.Text, store = true)
    private Set<String> blocked;

    @Field(type = FieldType.Text, store = true)
    private Set<String> muted;

    public User(
            final String id,
            final String username,
            final String userAvatar,
            final boolean publicAccount) {
        this.id = id;
        this.username = username;
        this.userAvatar = userAvatar;
        this.publicAccount = publicAccount;
        this.following = new HashSet<>();
        this.blocked = new HashSet<>();
        this.muted = new HashSet<>();
    }
}

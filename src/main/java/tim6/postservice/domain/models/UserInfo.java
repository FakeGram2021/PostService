package tim6.postservice.domain.models;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfo {

    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String username;

    @Field(type = FieldType.Text)
    private String userAvatar;

    public UserInfo(final String id) {
        this.id = id;
        this.username = "";
        this.userAvatar = "";
    }

    public UserInfo(final User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.userAvatar = user.getUserAvatar();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        UserInfo userInfo = (UserInfo) o;
        return this.getId().equals(userInfo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}

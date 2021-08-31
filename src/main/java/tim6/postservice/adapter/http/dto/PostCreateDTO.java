package tim6.postservice.adapter.http.dto;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostCreateDTO {

    @NotBlank(message = "Id can't be blank")
    private String id;

    @NotBlank(message = "Image url can't be blank")
    private String imageUrl;

    private String description;

    private Set<String> tags;

    private Set<String> userTags;
}

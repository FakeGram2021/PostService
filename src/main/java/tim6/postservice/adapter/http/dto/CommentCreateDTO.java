package tim6.postservice.adapter.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CommentCreateDTO {

    @NotBlank(message = "Id can't be blank")
    private String id;

    @NotBlank(message = "Comment can't be blank")
    private String comment;
}

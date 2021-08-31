package tim6.postservice.adapter.http.mapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import tim6.postservice.adapter.http.dto.CommentCreateDTO;
import tim6.postservice.adapter.http.dto.CommentGetDTO;
import tim6.postservice.domain.models.Comment;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;

public class CommentMapper {

    public static Comment toComment(final CommentCreateDTO dto, final User poster) {
        return Comment.builder()
                .id(dto.getId())
                .commenter(new UserInfo(poster))
                .commentDate(new Date())
                .comment(dto.getComment())
                .build();
    }

    public static CommentGetDTO toCommentGetDTO(final Comment comment) {
        return CommentGetDTO.builder()
                .id(comment.getId())
                .commenter(UserInfoMapper.toUserInfoDTO(comment.getCommenter()))
                .commentDate(comment.getCommentDate())
                .comment(comment.getComment())
                .build();
    }

    public static List<CommentGetDTO> toCommentGetDTOList(final List<Comment> commentList) {
        return commentList.stream()
                .map(CommentMapper::toCommentGetDTO)
                .collect(Collectors.toList());
    }
}

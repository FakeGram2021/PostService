package tim6.postservice.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Repository;
import tim6.postservice.domain.exceptions.EntityAlreadyExistsException;
import tim6.postservice.domain.models.Comment;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.CommentRepository;

@Repository
public class CommentService {

    final CommentRepository postRepository;

    final ElasticsearchOperations elasticsearchOperations;

    final PostService postService;

    final UserService userService;

    @Autowired
    public CommentService(
            final CommentRepository postRepository,
            final ElasticsearchOperations elasticsearchOperations,
            final PostService postService,
            final UserService userService) {
        this.postRepository = postRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.postService = postService;
        this.userService = userService;
    }

    public void commentOnAPost(
            final String postId,
            final String commentId,
            final String commentContent,
            final String authedUserId) {
        final Comment comment = this.createComment(commentId, commentContent, authedUserId);
        this.addCommentToPost(postId, authedUserId, comment);
    }

    public Comment createComment(
            final String id, final String commentContent, final String authedUserId) {
        final User commenter = this.userService.findById(authedUserId);
        return new Comment(id, new UserInfo(commenter), commentContent);
    }

    private void addCommentToPost(final String postId, final String authedUserId, final Comment comment) {
        final Post post = this.postService.getPostById(postId, authedUserId);
        final List<Comment> comments = post.getComments();

        if (comments.stream().anyMatch(c -> c.getId().equals(comment.getId()))) {
            throw new EntityAlreadyExistsException();
        } else {
            comments.add(comment);
            post.setComments(comments);
            this.postService.save(post);
        }
    }
}

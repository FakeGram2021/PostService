package tim6.postservice.adapter.http.controller.v1;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tim6.postservice.adapter.http.dto.CommentCreateDTO;
import tim6.postservice.adapter.http.dto.PostCreateDTO;
import tim6.postservice.adapter.http.dto.PostGetDTO;
import tim6.postservice.adapter.http.dto.PostOverviewDTO;
import tim6.postservice.adapter.http.mapper.PostMapper;
import tim6.postservice.domain.helpers.AuthHelper;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.services.CommentService;
import tim6.postservice.domain.services.PostService;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public PostController(final PostService postService, final CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostGetDTO> createPost(
            @Valid @RequestBody final PostCreateDTO postCreateDTO) {
        final String authedUserId = AuthHelper.getCurrentUserId();
        Post post = PostMapper.toPost(postCreateDTO, authedUserId);
        post = this.postService.createNew(post);

        return new ResponseEntity<>(PostMapper.toPostGetDTO(post), HttpStatus.CREATED);
    }

    @GetMapping(value = "/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPostById(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();
        final Post post = this.postService.getPostById(postId, authedUserId);

        return new ResponseEntity<>(PostMapper.toPostGetDTO(post), HttpStatus.OK);
    }

    @GetMapping(value = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFeed() {
        final String authedUserId = AuthHelper.getCurrentUserId();
        final List<Post> posts = this.postService.getFeed(authedUserId);

        final List<PostOverviewDTO> postOverviewDTOs =
                posts.stream().map(PostMapper::toPostOverviewDTO).collect(Collectors.toList());

        return new ResponseEntity<>(postOverviewDTOs, HttpStatus.OK);
    }

    @GetMapping(value = "/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchPostsByTags(
            @RequestParam(name = "tags") final List<String> tags, final Pageable pageable) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        final SearchPage<Post> postsSearchPage =
                this.postService.getPostsByTags(tags, pageable, authedUserId);
        return new ResponseEntity<>(
                PostMapper.toPostOverviewSearchReturnPage(postsSearchPage), HttpStatus.OK);
    }

    @GetMapping(value = "/poster/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchPostsByUserId(
            @PathVariable final String id, final Pageable pageable) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        final SearchPage<Post> postsSearchPage =
                this.postService.postsByPosterIdQuery(id, pageable, authedUserId);
        return new ResponseEntity<>(
                PostMapper.toPostOverviewSearchReturnPage(postsSearchPage), HttpStatus.OK);
    }

    @PostMapping(
            value = "/{postId}/comment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> commentOnPost(
            @PathVariable final String postId,
            @Valid @RequestBody final CommentCreateDTO commentCreateDTO) {
        final String authedUserId = AuthHelper.getCurrentUserId();
        this.commentService.commentOnAPost(
                postId, commentCreateDTO.getId(), commentCreateDTO.getComment(), authedUserId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/{postId}/likes")
    public ResponseEntity<?> likePost(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        this.postService.likePost(postId, authedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{postId}/likes")
    public ResponseEntity<?> removeLikePost(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        this.postService.removeLikeFromPost(postId, authedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{postId}/dislikes")
    public ResponseEntity<?> dislikePost(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        this.postService.dislikePost(postId, authedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{postId}/dislikes")
    public ResponseEntity<?> removeDislikePost(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        this.postService.removeDislikeFromPost(postId, authedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{postId}/favorites")
    public ResponseEntity<?> favoritePost(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        this.postService.favoritePost(postId, authedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{postId}/favorites")
    public ResponseEntity<?> removeFavoritePost(@PathVariable final String postId) {
        final String authedUserId = AuthHelper.getCurrentUserId();

        this.postService.unfavoritePost(postId, authedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

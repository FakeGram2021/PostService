package tim6.postservice.post.intergration.comments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import tim6.postservice.adapter.http.dto.CommentCreateDTO;
import tim6.postservice.common.CommonTestBase;
import tim6.postservice.domain.models.Comment;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.PostRepository;
import tim6.postservice.domain.repositories.UserRepository;
import tim6.postservice.helpers.AuthHelper;

public class CommentOnPostTest extends CommonTestBase {

    private final String USER_ID = UUID.randomUUID().toString();
    private final String PUBLIC_POSTER_ID = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POSTER_ID = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POSTER_ID = UUID.randomUUID().toString();

    private final String PUBLIC_POST_ID = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POST_ID = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POST_ID = UUID.randomUUID().toString();

    private final String COMMENT_ID_1 = UUID.randomUUID().toString();
    private final String COMMENT_CONTENT_1 = "Comment 1";

    private final String COMMENT_ID_2 = UUID.randomUUID().toString();
    private final String COMMENT_CONTENT_2 = "Comment 2";

    private final String COMMENT_ID_3 = UUID.randomUUID().toString();
    private final String COMMENT_CONTENT_3 = "Comment 3";

    private final String IMAGE_URL = "testImage.jpg";
    private final String DESCRIPTION = "Proper description";
    private final Set<String> TAGS = Set.of("tag1", "tag2", "tag3");
    private final Set<UserInfo> USER_TAGS = new HashSet<>();

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TestRestTemplate testRestTemplate;
    @LocalServerPort private int port;

    @Before
    public void initializeData() {
        this.initializeUsers();
        this.initializePosts();
    }

    @After
    public void cleanUpData() {
        this.userRepository.deleteAll();
        this.postRepository.deleteAll();
    }

    private void initializeUsers() {
        final List<User> usersToSave = new ArrayList<>();

        final User publicPoster =
                new User(this.PUBLIC_POSTER_ID, "publicPosterName", "posterAvatar", true);
        usersToSave.add(publicPoster);

        final User privatePosterNotFollowedByUser =
                new User(
                        this.PRIVATE_NOT_FOLLOWED_POSTER_ID,
                        "privateNotFollowedPosterName",
                        "posterAvatar",
                        false);
        usersToSave.add(privatePosterNotFollowedByUser);

        final User privatePosterFollowedByUser =
                new User(
                        this.PRIVATE_FOLLOWED_POSTER_ID,
                        "privateFollowedPosterName",
                        "posterAvatar",
                        false);
        usersToSave.add(privatePosterFollowedByUser);

        final User user =
                new User(
                        this.USER_ID,
                        "userName",
                        "userAvatar",
                        true,
                        Set.of(this.PRIVATE_FOLLOWED_POSTER_ID),
                        new HashSet<>(),
                        new HashSet<>());
        usersToSave.add(user);

        this.userRepository.saveAll(usersToSave);
    }

    private void initializePosts() {
        final List<Post> postsToSave = new ArrayList<>();

        final Post publicPost =
                new Post(
                        this.PUBLIC_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        Set.of("tag1", "tag2"),
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        postsToSave.add(publicPost);

        final Post privatePostByFollowedPoster =
                new Post(
                        this.PRIVATE_FOLLOWED_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_FOLLOWED_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        postsToSave.add(privatePostByFollowedPoster);

        final Post privatePostByNotFollowedPoster =
                new Post(
                        this.PRIVATE_NOT_FOLLOWED_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_NOT_FOLLOWED_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        postsToSave.add(privatePostByNotFollowedPoster);

        this.postRepository.saveAll(postsToSave);
    }

    @Test
    public void testCommentOnPublicPostNoAuth() {
        final CommentCreateDTO commentCreateDTO =
                new CommentCreateDTO(this.COMMENT_ID_1, this.COMMENT_CONTENT_1);
        final URI apiEndpoint = this.buildCommentUri(this.PUBLIC_POST_ID);
        final ResponseEntity<Void> response =
                this.sendCommentRequest(apiEndpoint, commentCreateDTO);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCommentOnPublicPostWithAuth() {
        final CommentCreateDTO commentCreateDTO =
                new CommentCreateDTO(this.COMMENT_ID_1, this.COMMENT_CONTENT_1);
        final URI apiEndpoint = this.buildCommentUri(this.PUBLIC_POST_ID);
        final ResponseEntity<Void> response =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        final Optional<Post> publicPost = this.postRepository.findById(this.PUBLIC_POST_ID);
        assertTrue(publicPost.isPresent());

        final List<Comment> comments = publicPost.get().getComments();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(comments.stream().map(Comment::getId).collect(Collectors.toList()))
                .containsOnly(this.COMMENT_ID_1);
        softAssertions
                .assertThat(comments.stream().map(Comment::getComment).collect(Collectors.toList()))
                .containsOnly(this.COMMENT_CONTENT_1);
        softAssertions.assertAll();
    }

    @Test
    public void testCommentsInOrder() {
        final CommentCreateDTO commentCreateDTO1 =
                new CommentCreateDTO(this.COMMENT_ID_1, this.COMMENT_CONTENT_1);
        final CommentCreateDTO commentCreateDTO2 =
                new CommentCreateDTO(this.COMMENT_ID_2, this.COMMENT_CONTENT_2);
        final CommentCreateDTO commentCreateDTO3 =
                new CommentCreateDTO(this.COMMENT_ID_3, this.COMMENT_CONTENT_3);

        final URI apiEndpoint = this.buildCommentUri(this.PUBLIC_POST_ID);

        final ResponseEntity<Void> response1 =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO1,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());

        final ResponseEntity<Void> response2 =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO2,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());

        final ResponseEntity<Void> response3 =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO3,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response3.getStatusCode());

        final Optional<Post> publicPost = this.postRepository.findById(this.PUBLIC_POST_ID);
        assertTrue(publicPost.isPresent());

        final List<Comment> comments = publicPost.get().getComments();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(comments.stream().map(Comment::getId).collect(Collectors.toList()))
                .containsExactly(this.COMMENT_ID_1, this.COMMENT_ID_2, this.COMMENT_ID_3);
        softAssertions
                .assertThat(comments.stream().map(Comment::getComment).collect(Collectors.toList()))
                .containsOnly(
                        this.COMMENT_CONTENT_1, this.COMMENT_CONTENT_2, this.COMMENT_CONTENT_3);
        softAssertions
                .assertThat(
                        comments.get(0).getCommentDate().before(comments.get(1).getCommentDate()))
                .isEqualTo(true);
        softAssertions
                .assertThat(
                        comments.get(1).getCommentDate().before(comments.get(2).getCommentDate()))
                .isEqualTo(true);
        softAssertions.assertAll();
    }

    @Test
    public void testDoubleComment() {
        final CommentCreateDTO commentCreateDTO1 =
                new CommentCreateDTO(this.COMMENT_ID_1, this.COMMENT_CONTENT_1);
        final URI apiEndpoint = this.buildCommentUri(this.PUBLIC_POST_ID);

        final ResponseEntity<Void> response1 =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO1,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());

        final ResponseEntity<Void> response2 =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO1,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    public void testCommentOnPrivateFollowedPost() {
        final CommentCreateDTO commentCreateDTO =
                new CommentCreateDTO(this.COMMENT_ID_1, this.COMMENT_CONTENT_1);
        final URI apiEndpoint = this.buildCommentUri(this.PRIVATE_FOLLOWED_POST_ID);
        final ResponseEntity<Void> response =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        final Optional<Post> publicPost =
                this.postRepository.findById(this.PRIVATE_FOLLOWED_POST_ID);
        assertTrue(publicPost.isPresent());

        final List<Comment> comments = publicPost.get().getComments();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(comments.stream().map(Comment::getId).collect(Collectors.toList()))
                .containsOnly(this.COMMENT_ID_1);
        softAssertions
                .assertThat(comments.stream().map(Comment::getComment).collect(Collectors.toList()))
                .containsOnly(this.COMMENT_CONTENT_1);
        softAssertions.assertAll();
    }

    @Test
    public void testCommentOnPrivateNotFollowedPost() {
        final CommentCreateDTO commentCreateDTO =
                new CommentCreateDTO(this.COMMENT_ID_1, this.COMMENT_CONTENT_1);
        final URI apiEndpoint = this.buildCommentUri(this.PRIVATE_NOT_FOLLOWED_POST_ID);
        final ResponseEntity<Void> response =
                this.sendCommentRequest(
                        apiEndpoint,
                        commentCreateDTO,
                        AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private URI buildCommentUri(final String postId) {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format(
                                "http://localhost:%d/api/v1/posts/%s/comment", this.port, postId));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<Void> sendCommentRequest(
            final URI apiEndpoint, final CommentCreateDTO requestBody) {
        final HttpEntity<CommentCreateDTO> entity = new HttpEntity<>(requestBody);

        return this.testRestTemplate.exchange(apiEndpoint, HttpMethod.POST, entity, Void.class);
    }

    private ResponseEntity<Void> sendCommentRequest(
            final URI apiEndpoint, final CommentCreateDTO requestBody, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));

        final HttpEntity<CommentCreateDTO> entity = new HttpEntity<>(requestBody, headers);

        return this.testRestTemplate.exchange(apiEndpoint, HttpMethod.POST, entity, Void.class);
    }
}

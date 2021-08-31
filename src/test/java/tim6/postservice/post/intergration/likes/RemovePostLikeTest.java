package tim6.postservice.post.intergration.likes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static tim6.postservice.helpers.AuthHelper.createAuthToken;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
import tim6.postservice.adapter.http.dto.PostGetDTO;
import tim6.postservice.common.CommonTestBase;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.PostRepository;
import tim6.postservice.domain.repositories.UserRepository;

public class RemovePostLikeTest extends CommonTestBase {

    private final String USER_ID = UUID.randomUUID().toString();
    private final String PUBLIC_POSTER_ID = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POSTER_ID = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POSTER_ID = UUID.randomUUID().toString();

    private final String PUBLIC_POST_ID = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POST_ID = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POST_ID = UUID.randomUUID().toString();
    private final String NOT_PREVIOUSLY_LIKED_POST_ID = UUID.randomUUID().toString();

    private final String IMAGE_URL = "testImage.jpg";
    private final String DESCRIPTION = "Proper description";
    private final Set<String> TAGS = Set.of("tag1", "tag2", "tag3");
    private final Set<UserInfo> USER_TAGS = new HashSet<>();
    private final Set<String> PREVIOUS_LIKES = Set.of(this.USER_ID);

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
        final User publicPoster =
                new User(this.PUBLIC_POSTER_ID, "publicPosterName", "posterAvatar", true);
        this.userRepository.save(publicPoster);

        final User privatePosterNotFollowedByUser =
                new User(
                        this.PRIVATE_NOT_FOLLOWED_POSTER_ID,
                        "privatePosterName",
                        "posterAvatar",
                        false);
        this.userRepository.save(privatePosterNotFollowedByUser);

        final User privatePosterFollowedByUser =
                new User(
                        this.PRIVATE_FOLLOWED_POSTER_ID,
                        "privatePosterName",
                        "posterAvatar",
                        false);
        this.userRepository.save(privatePosterFollowedByUser);

        final User user =
                new User(
                        this.USER_ID,
                        "userName",
                        "userAvatar",
                        true,
                        Set.of(this.PRIVATE_FOLLOWED_POSTER_ID),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(user);
    }

    private void initializePosts() {
        final Post publicPost =
                new Post(
                        this.PUBLIC_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        this.PREVIOUS_LIKES,
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(publicPost);

        final Post privatePostByFollowedPoster =
                new Post(
                        this.PRIVATE_FOLLOWED_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_FOLLOWED_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        this.PREVIOUS_LIKES,
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(privatePostByFollowedPoster);

        final Post privatePostByNotFollowedPoster =
                new Post(
                        this.PRIVATE_NOT_FOLLOWED_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_NOT_FOLLOWED_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        this.PREVIOUS_LIKES,
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(privatePostByNotFollowedPoster);

        final Post notPreviouslyLikedPost =
                new Post(
                        this.NOT_PREVIOUSLY_LIKED_POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(notPreviouslyLikedPost);
    }

    @Test
    public void testRemoveLikeFromPublicPost() {
        final URI removeLikePostUri = this.buildRemoveLikePostUri(this.PUBLIC_POST_ID);
        final ResponseEntity<Void> removeLikePostResponse =
                this.sendRemoveLikePostRequest(
                        removeLikePostUri, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.NO_CONTENT, removeLikePostResponse.getStatusCode());

        final URI getPostEndpoint = this.buildGetPostUri(this.PUBLIC_POST_ID);
        final ResponseEntity<PostGetDTO> getPostResponse = this.sendGetPostRequest(getPostEndpoint);

        assertEquals(HttpStatus.OK, getPostResponse.getStatusCode());
        assertNotNull(getPostResponse.getBody());
        assertEquals(0, getPostResponse.getBody().getLikes().size());
    }

    @Test
    public void testRemoveLikeFromPrivateFollowedPost() {
        final URI removeLikePostUri = this.buildRemoveLikePostUri(this.PRIVATE_FOLLOWED_POST_ID);
        final ResponseEntity<Void> removeLikePostResponse =
                this.sendRemoveLikePostRequest(
                        removeLikePostUri, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.NO_CONTENT, removeLikePostResponse.getStatusCode());

        final URI getPostEndpoint = this.buildGetPostUri(this.PRIVATE_FOLLOWED_POST_ID);
        final ResponseEntity<PostGetDTO> getPostResponse =
                this.sendGetPostRequest(
                        getPostEndpoint, createAuthToken(this.USER_ID, this.JWT_SECRET));

        assertEquals(HttpStatus.OK, getPostResponse.getStatusCode());
        assertNotNull(getPostResponse.getBody());
        assertEquals(0, getPostResponse.getBody().getLikes().size());
    }

    @Test
    public void testRemoveLikeFromNotLikedPost() {
        final URI removeLikePostUri =
                this.buildRemoveLikePostUri(this.NOT_PREVIOUSLY_LIKED_POST_ID);
        final ResponseEntity<Void> removeLikePostResponse =
                this.sendRemoveLikePostRequest(
                        removeLikePostUri, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.NO_CONTENT, removeLikePostResponse.getStatusCode());

        final URI getPostEndpoint = this.buildGetPostUri(this.NOT_PREVIOUSLY_LIKED_POST_ID);
        final ResponseEntity<PostGetDTO> getPostResponse =
                this.sendGetPostRequest(
                        getPostEndpoint, createAuthToken(this.USER_ID, this.JWT_SECRET));

        assertEquals(HttpStatus.OK, getPostResponse.getStatusCode());
        assertNotNull(getPostResponse.getBody());
        assertEquals(0, getPostResponse.getBody().getLikes().size());
    }

    @Test
    public void testRemoveLikeFromPrivateFromNotFollowedPost() {
        final URI removeLikePostUri =
                this.buildRemoveLikePostUri(this.PRIVATE_NOT_FOLLOWED_POST_ID);
        final ResponseEntity<Void> removeLikePostResponse =
                this.sendRemoveLikePostRequest(
                        removeLikePostUri, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.FORBIDDEN, removeLikePostResponse.getStatusCode());
    }

    @Test
    public void testRemoveLikeWhenNotLoggedIn() {
        final URI likePostEndpoint = this.buildRemoveLikePostUri(this.PUBLIC_POST_ID);
        final ResponseEntity<Void> likePostResponse =
                this.sendRemoveLikePostRequest(likePostEndpoint);
        assertEquals(HttpStatus.FORBIDDEN, likePostResponse.getStatusCode());
    }

    @Test
    public void testRemoveLikeFromNonExistingPost() {
        final URI likePostEndpoint = this.buildRemoveLikePostUri("NON_EXISTING_ID");
        final ResponseEntity<Void> likePostResponse =
                this.sendRemoveLikePostRequest(
                        likePostEndpoint, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.NOT_FOUND, likePostResponse.getStatusCode());
    }

    private URI buildRemoveLikePostUri(final String postId) {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format(
                                "http://localhost:%d/api/v1/posts/%s/likes", this.port, postId));
        return builder.build().encode().toUri();
    }

    private URI buildGetPostUri(final String postId) {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format("http://localhost:%d/api/v1/posts/%s", this.port, postId));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<Void> sendRemoveLikePostRequest(
            final URI apiEndpoint, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return this.testRestTemplate.exchange(apiEndpoint, HttpMethod.DELETE, entity, Void.class);
    }

    private ResponseEntity<Void> sendRemoveLikePostRequest(final URI apiEndpoint) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return this.testRestTemplate.exchange(apiEndpoint, HttpMethod.DELETE, entity, Void.class);
    }

    private ResponseEntity<PostGetDTO> sendGetPostRequest(
            final URI apiEndpoint, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.GET, entity, PostGetDTO.class);
    }

    private ResponseEntity<PostGetDTO> sendGetPostRequest(final URI apiEndpoint) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.GET, entity, PostGetDTO.class);
    }
}

package tim6.postservice.post.intergration.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import tim6.postservice.adapter.http.dto.PostOverviewDTO;
import tim6.postservice.common.CommonTestBase;
import tim6.postservice.domain.models.Comment;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.PostRepository;
import tim6.postservice.domain.repositories.UserRepository;
import tim6.postservice.helpers.AuthHelper;
import tim6.postservice.helpers.PostOverviewPage;

public class SearchByTagsTest extends CommonTestBase {

    private final String USER_ID = UUID.randomUUID().toString();
    private final String PUBLIC_POSTER_ID = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POSTER_ID = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POSTER_ID = UUID.randomUUID().toString();

    private final String PUBLIC_POST_ID_1 = UUID.randomUUID().toString();
    private final String PUBLIC_POST_ID_2 = UUID.randomUUID().toString();
    private final String PUBLIC_POST_ID_3 = UUID.randomUUID().toString();
    private final String PUBLIC_POST_ID_4 = UUID.randomUUID().toString();

    private final String PRIVATE_FOLLOWED_POST_ID_1 = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POST_ID_2 = UUID.randomUUID().toString();
    private final String PRIVATE_FOLLOWED_POST_ID_3 = UUID.randomUUID().toString();

    private final String PRIVATE_NOT_FOLLOWED_POST_ID_1 = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POST_ID_2 = UUID.randomUUID().toString();
    private final String PRIVATE_NOT_FOLLOWED_POST_ID_3 = UUID.randomUUID().toString();

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
        final Post publicPost1 =
                new Post(
                        this.PUBLIC_POST_ID_1,
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
        this.postRepository.save(publicPost1);

        final Post publicPost2 =
                new Post(
                        this.PUBLIC_POST_ID_2,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        Set.of("tag2", "tag3"),
                        this.USER_TAGS,
                        Set.of("L1", "L2", "L3"),
                        Set.of("D1", "D2", "D3"),
                        Set.of("F1"),
                        List.of(
                                new Comment(
                                        "CID1",
                                        new UserInfo(
                                                this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                                        "content")));
        this.postRepository.save(publicPost2);

        final Post publicPost3 =
                new Post(
                        this.PUBLIC_POST_ID_3,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        Set.of("tag3", "tag4"),
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(publicPost3);

        final Post publicPost4 =
                new Post(
                        this.PUBLIC_POST_ID_4,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PUBLIC_POSTER_ID, "userName", "userAvatar"),
                        new Date(),
                        new HashSet<>(),
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(publicPost4);

        final Post privatePostByFollowedPoster1 =
                new Post(
                        this.PRIVATE_FOLLOWED_POST_ID_1,
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
        this.postRepository.save(privatePostByFollowedPoster1);

        final Post privatePostByFollowedPoster2 =
                new Post(
                        this.PRIVATE_FOLLOWED_POST_ID_2,
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
        this.postRepository.save(privatePostByFollowedPoster2);

        final Post privatePostByFollowedPoster3 =
                new Post(
                        this.PRIVATE_FOLLOWED_POST_ID_3,
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
        this.postRepository.save(privatePostByFollowedPoster3);

        final Post privatePostByNotFollowedPoster1 =
                new Post(
                        this.PRIVATE_NOT_FOLLOWED_POST_ID_1,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_NOT_FOLLOWED_POST_ID_1, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(privatePostByNotFollowedPoster1);

        final Post privatePostByNotFollowedPoster2 =
                new Post(
                        this.PRIVATE_NOT_FOLLOWED_POST_ID_2,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_NOT_FOLLOWED_POST_ID_2, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(privatePostByNotFollowedPoster2);

        final Post privatePostByNotFollowedPoster3 =
                new Post(
                        this.PRIVATE_NOT_FOLLOWED_POST_ID_3,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        new UserInfo(this.PRIVATE_NOT_FOLLOWED_POST_ID_3, "userName", "userAvatar"),
                        new Date(),
                        this.TAGS,
                        this.USER_TAGS,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new ArrayList<>());
        this.postRepository.save(privatePostByNotFollowedPoster3);
    }

    @Test
    public void testGetPublicPostsNotLoggedIn() {
        final URI uri = this.buildGetPostsByTagsUri("tag1,tag2");
        final ResponseEntity<PostOverviewPage> response =
                this.sendSearchPostsByTagsPostRequest(uri);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        final PostOverviewPage responseBody = response.getBody();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseBody.getTotalElements()).isEqualTo(2);
        softAssertions.assertThat(responseBody.getNumberOfElements()).isEqualTo(2);
        softAssertions
                .assertThat(
                        responseBody.getContent().stream()
                                .map(PostOverviewDTO::getId)
                                .collect(Collectors.toList()))
                .containsOnly(this.PUBLIC_POST_ID_1, this.PUBLIC_POST_ID_2);
        softAssertions.assertAll();
    }

    @Test
    public void testGetPublicPostsNotLoggedInWithTagsNotPresent() {
        final URI uri = this.buildGetPostsByTagsUri("a,b,c");
        final ResponseEntity<PostOverviewPage> response =
                this.sendSearchPostsByTagsPostRequest(uri);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    public void testGetPrivatePosts() {
        final URI uri = this.buildGetPostsByTagsUri("tag1,tag2");
        final ResponseEntity<PostOverviewPage> response =
                this.sendSearchPostsByTagsPostRequest(
                        uri, AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        final PostOverviewPage responseBody = response.getBody();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseBody.getTotalElements()).isEqualTo(5);
        softAssertions.assertThat(responseBody.getNumberOfElements()).isEqualTo(5);
        softAssertions
                .assertThat(
                        responseBody.getContent().stream()
                                .map(PostOverviewDTO::getId)
                                .collect(Collectors.toList()))
                .containsOnly(
                        this.PUBLIC_POST_ID_1,
                        this.PUBLIC_POST_ID_2,
                        this.PRIVATE_FOLLOWED_POST_ID_1,
                        this.PRIVATE_FOLLOWED_POST_ID_2,
                        this.PRIVATE_FOLLOWED_POST_ID_3);
        softAssertions.assertAll();
    }

    @Test
    public void testGetPrivatePostsWithPage0() {
        final URI uri = this.buildGetPostsByTagsUri("tag1,tag2", 0, 2);
        final ResponseEntity<PostOverviewPage> pageResponse =
                this.sendSearchPostsByTagsPostRequest(
                        uri, AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.OK, pageResponse.getStatusCode());
        assertNotNull(pageResponse.getBody());

        final PostOverviewPage pageResponseBody = pageResponse.getBody();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(pageResponseBody.getTotalElements()).isEqualTo(5);
        softAssertions.assertThat(pageResponseBody.getNumberOfElements()).isEqualTo(2);
        softAssertions
                .assertThat(
                        pageResponseBody.getContent().stream()
                                .map(PostOverviewDTO::getId)
                                .collect(Collectors.toList()))
                .containsAnyOf(this.PUBLIC_POST_ID_1, this.PUBLIC_POST_ID_2);
        softAssertions.assertAll();
    }

    @Test
    public void testGetPrivatePostsWithPage1() {
        final URI uri = this.buildGetPostsByTagsUri("tag1,tag2", 1, 2);
        final ResponseEntity<PostOverviewPage> pageResponse =
                this.sendSearchPostsByTagsPostRequest(
                        uri, AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.OK, pageResponse.getStatusCode());
        assertNotNull(pageResponse.getBody());

        final PostOverviewPage pageResponseBody = pageResponse.getBody();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(pageResponseBody.getTotalElements()).isEqualTo(5);
        softAssertions.assertThat(pageResponseBody.getNumberOfElements()).isEqualTo(2);
        softAssertions
                .assertThat(
                        pageResponseBody.getContent().stream()
                                .map(PostOverviewDTO::getId)
                                .collect(Collectors.toList()))
                .containsAnyOf(this.PRIVATE_FOLLOWED_POST_ID_1, this.PRIVATE_FOLLOWED_POST_ID_2);
        softAssertions.assertAll();
    }

    @Test
    public void testGetPrivatePostsWithPage2() {
        final URI uri = this.buildGetPostsByTagsUri("tag1,tag2", 2, 2);
        final ResponseEntity<PostOverviewPage> pageResponse =
                this.sendSearchPostsByTagsPostRequest(
                        uri, AuthHelper.createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.OK, pageResponse.getStatusCode());
        assertNotNull(pageResponse.getBody());

        final PostOverviewPage pageResponseBody = pageResponse.getBody();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(pageResponseBody.getTotalElements()).isEqualTo(5);
        softAssertions.assertThat(pageResponseBody.getNumberOfElements()).isEqualTo(1);
        softAssertions
                .assertThat(
                        pageResponseBody.getContent().stream()
                                .map(PostOverviewDTO::getId)
                                .collect(Collectors.toList()))
                .containsAnyOf(this.PRIVATE_FOLLOWED_POST_ID_3);
        softAssertions.assertAll();
    }

    private URI buildGetPostsByTagsUri(final String tags) {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format(
                                "http://localhost:%d/api/v1/posts/tags?tags=%s", this.port, tags));
        return builder.build().encode().toUri();
    }

    private URI buildGetPostsByTagsUri(final String tags, final int page, final int size) {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format(
                                "http://localhost:%d/api/v1/posts/tags/?tags=%s&page=%s&size=%s",
                                this.port, tags, page, size));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<PostOverviewPage> sendSearchPostsByTagsPostRequest(
            final URI apiEndpoint, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);
        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.GET, entity, PostOverviewPage.class);
    }

    private ResponseEntity<PostOverviewPage> sendSearchPostsByTagsPostRequest(
            final URI apiEndpoint) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);
        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.GET, entity, PostOverviewPage.class);
    }
}

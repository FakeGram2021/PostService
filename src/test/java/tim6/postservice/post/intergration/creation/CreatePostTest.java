package tim6.postservice.post.intergration.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static tim6.postservice.helpers.AuthHelper.createAuthToken;

import java.net.URI;
import java.util.ArrayList;
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
import tim6.postservice.adapter.http.dto.PostCreateDTO;
import tim6.postservice.adapter.http.dto.PostGetDTO;
import tim6.postservice.adapter.http.dto.UserInfoDTO;
import tim6.postservice.common.CommonTestBase;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.PostRepository;
import tim6.postservice.domain.repositories.UserRepository;

public class CreatePostTest extends CommonTestBase {

    private final String USER_ID = UUID.randomUUID().toString();
    private final String POST_ID = UUID.randomUUID().toString();
    private final String TAGGED_USER1 = UUID.randomUUID().toString();
    private final String TAGGED_USER2 = UUID.randomUUID().toString();
    private final String TAGGED_USER3 = UUID.randomUUID().toString();

    private final String IMAGE_URL = "testImage.jpg";
    private final String DESCRIPTION = "Proper description";
    private final Set<String> TAGS = Set.of("tag1", "tag2", "tag3");
    private final Set<UserInfo> TAGGED_USERS =
            Set.of(
                    new UserInfo(this.TAGGED_USER1, "userName1", "userAvatar"),
                    new UserInfo(this.TAGGED_USER2, "userName2", "userAvatar"),
                    new UserInfo(this.TAGGED_USER3, "userName3", "userAvatar"));
    private final Set<UserInfoDTO> TAGGED_USERS_DTOS =
            Set.of(
                    new UserInfoDTO(this.TAGGED_USER1, "userName1", "userAvatar"),
                    new UserInfoDTO(this.TAGGED_USER2, "userName2", "userAvatar"),
                    new UserInfoDTO(this.TAGGED_USER3, "userName3", "userAvatar"));

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TestRestTemplate testRestTemplate;
    @LocalServerPort private int port;

    @Before
    public void initializeData() {
        this.initializeUsers();
    }

    @After
    public void cleanUpData() {
        this.userRepository.deleteAll();
        this.postRepository.deleteAll();
    }

    private void initializeUsers() {
        final List<User> usersToSave = new ArrayList<>();

        final User user = new User(this.USER_ID, "userName", "userAvatar", true);
        usersToSave.add(user);

        final List<User> taggedUsers =
                this.TAGGED_USERS.stream()
                        .map(
                                userInfo ->
                                        new User(
                                                userInfo.getId(),
                                                userInfo.getUsername(),
                                                userInfo.getUserAvatar(),
                                                true))
                        .collect(Collectors.toList());
        usersToSave.addAll(taggedUsers);

        this.userRepository.saveAll(usersToSave);
    }

    @Test
    public void testCreatePost() {
        final PostCreateDTO postToCreate =
                new PostCreateDTO(
                        this.POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        this.TAGS,
                        Set.of("userName1", "userName2", "userName3"));
        final URI apiEndpoint = this.buildPostUri();
        final ResponseEntity<PostGetDTO> response =
                this.sendPostRequest(
                        apiEndpoint, postToCreate, createAuthToken(this.USER_ID, this.JWT_SECRET));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        this.assertResponseHasExpectedFields(response);
    }

    @Test
    public void testDoubleCreatePost() {
        final PostCreateDTO postToCreate =
                new PostCreateDTO(
                        this.POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        this.TAGS,
                        Set.of("userName1", "userName2", "userName3"));
        final URI apiEndpoint = this.buildPostUri();
        final ResponseEntity<PostGetDTO> response1 =
                this.sendPostRequest(
                        apiEndpoint, postToCreate, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(response1.getStatusCode(), HttpStatus.CREATED);

        final ResponseEntity<PostGetDTO> response2 =
                this.sendPostRequest(
                        apiEndpoint, postToCreate, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(response2.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreatePostNoAuth() {
        final PostCreateDTO postToCreate =
                new PostCreateDTO(
                        this.POST_ID,
                        this.IMAGE_URL,
                        this.DESCRIPTION,
                        this.TAGS,
                        Set.of("userName1", "userName2", "userName3"));
        final URI apiEndpoint = this.buildPostUri();
        final ResponseEntity<PostGetDTO> response = this.sendPostRequest(apiEndpoint, postToCreate);

        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    private URI buildPostUri() {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format("http://localhost:%d/api/v1/posts", this.port));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<PostGetDTO> sendPostRequest(
            final URI apiEndpoint, final PostCreateDTO requestBody) {
        final HttpEntity<PostCreateDTO> entity = new HttpEntity<>(requestBody);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.POST, entity, PostGetDTO.class);
    }

    private ResponseEntity<PostGetDTO> sendPostRequest(
            final URI apiEndpoint, final PostCreateDTO requestBody, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));

        final HttpEntity<PostCreateDTO> entity = new HttpEntity<>(requestBody, headers);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.POST, entity, PostGetDTO.class);
    }

    private void assertResponseHasExpectedFields(final ResponseEntity<PostGetDTO> response) {
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getBody().getId()).isEqualTo(this.POST_ID);
        softly.assertThat(response.getBody().getImageUrl()).isEqualTo(this.IMAGE_URL);
        softly.assertThat(response.getBody().getDescription()).isEqualTo(this.DESCRIPTION);
        softly.assertThat(response.getBody().getTags()).isEqualTo(this.TAGS);
        softly.assertThat(response.getBody().getUserTags()).isEqualTo(this.TAGGED_USERS_DTOS);
        softly.assertThat(response.getBody().getComments().size()).isEqualTo(0);
        softly.assertThat(response.getBody().getLikes().size()).isEqualTo(0);
        softly.assertThat(response.getBody().getDislikes().size()).isEqualTo(0);
        softly.assertThat(response.getBody().getFavorites().size()).isEqualTo(0);
        softly.assertAll();
    }
}

package tim6.postservice.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tim6.postservice.adapter.kafka.models.KafkaMessage;
import tim6.postservice.adapter.kafka.models.payloads.BlockPayload;
import tim6.postservice.adapter.kafka.models.payloads.FollowPayload;
import tim6.postservice.adapter.kafka.models.payloads.MutePayload;
import tim6.postservice.adapter.kafka.models.payloads.UserPayload;
import tim6.postservice.domain.exceptions.EntityNotFoundException;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.services.PostService;
import tim6.postservice.domain.services.UserService;

@Service
public class ConsumerService {

    private final UserService userService;
    private final PostService postService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ConsumerService(final UserService userService, final PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @KafkaListener(
            topics = "post_service_topic",
            groupId = "groupId",
            containerFactory = "kafkaMessageListener")
    public void receiveMessage(final KafkaMessage message) {
        final String key = message.getKey();

        switch (key) {
            case "USER":
                this.onReceiveUser(message);
                break;
            case "FOLLOW":
                this.onReceiveFollow(message);
                break;
            case "MUTE":
                this.onReceiveMute(message);
                break;
            case "BLOCK":
                this.onReceiveBlock(message);
                break;
        }
    }

    private void onReceiveUser(final KafkaMessage message) {
        final UserPayload userPayload =
                this.objectMapper.convertValue(message.getValue(), UserPayload.class);
        try {
            this.updateExistingUser(userPayload);
        } catch (final EntityNotFoundException ex) {
            this.createNewUser(userPayload);
        }
    }

    private void createNewUser(final UserPayload userPayload) {
        final User user =
                new User(
                        userPayload.getId(),
                        userPayload.getUsername(),
                        userPayload.getUserAvatar(),
                        userPayload.isPublicAccount(),
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>());
        this.userService.save(user);
    }

    private void updateExistingUser(final UserPayload userPayload) {
        final User user = this.userService.findById(userPayload.getId());
        user.setUserAvatar(userPayload.getUserAvatar());
        user.setUsername(userPayload.getUsername());
        user.setPublicAccount(userPayload.isPublicAccount());
        this.userService.save(user);

        this.updateUserInformationInPosts(user);
    }

    private void updateUserInformationInPosts(final User user) {
        final UserInfo userInfo =
                new UserInfo(user.getId(), user.getUsername(), user.getUserAvatar());
        this.postService.updateUserInfoInRelevantDocuments(userInfo);
    }

    private void onReceiveFollow(final KafkaMessage message) {
        final FollowPayload followPayload =
                this.objectMapper.convertValue(message.getValue(), FollowPayload.class);

        if (followPayload.isApply()) {
            this.userService.followUser(
                    followPayload.getFollowerId(), followPayload.getFollowTargetId());
        } else {
            this.userService.unfollowUser(
                    followPayload.getFollowerId(), followPayload.getFollowTargetId());
        }
    }

    private void onReceiveMute(final KafkaMessage message) {
        final MutePayload mutePayload =
                this.objectMapper.convertValue(message.getValue(), MutePayload.class);

        if (mutePayload.isApply()) {
            this.userService.muteUser(mutePayload.getMuterId(), mutePayload.getMuteTargetId());
        } else {
            this.userService.unmuteUser(mutePayload.getMuterId(), mutePayload.getMuteTargetId());
        }
    }

    private void onReceiveBlock(final KafkaMessage message) {
        final BlockPayload blockPayload =
                this.objectMapper.convertValue(message.getValue(), BlockPayload.class);

        if (blockPayload.isApply()) {
            this.userService.blockUser(blockPayload.getBlockerId(), blockPayload.getBlockTarget());
        } else {
            this.userService.unblockUser(
                    blockPayload.getBlockerId(), blockPayload.getBlockTarget());
        }
    }
}

package tim6.postservice.adapter.http.mapper;

import tim6.postservice.adapter.http.dto.UserInfoDTO;
import tim6.postservice.domain.models.UserInfo;

public class UserInfoMapper {

    public static UserInfoDTO toUserInfoDTO(final UserInfo userInfo) {
        return new UserInfoDTO(userInfo.getId(), userInfo.getUsername(), userInfo.getUserAvatar());
    }

    public static UserInfo toUserInfo(final UserInfoDTO userInfo) {
        return new UserInfo(userInfo.getId(), userInfo.getUsername(), userInfo.getUserAvatar());
    }
}

package tim6.postservice.adapter.http.mapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.SearchPage;
import tim6.postservice.adapter.http.dto.PostCreateDTO;
import tim6.postservice.adapter.http.dto.PostGetDTO;
import tim6.postservice.adapter.http.dto.PostOverviewDTO;
import tim6.postservice.domain.models.Comment;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.UserInfo;

public class PostMapper {

    public static Post toPost(final PostCreateDTO dto, final String posterId) {
        return Post.builder()
                .id(dto.getId())
                .imageUrl(dto.getImageUrl())
                .description(dto.getDescription())
                .poster(new UserInfo(posterId))
                .posterId(posterId)
                .postDate(new Date())
                .tags(dto.getTags())
                .userTags(
                        dto.getUserTags().stream()
                                .map(tag -> new UserInfo(null, tag, null))
                                .collect(Collectors.toSet()))
                .likes(new HashSet<>())
                .dislikes(new HashSet<>())
                .favorites(new HashSet<>())
                .comments(new ArrayList<>())
                .build();
    }

    public static PostGetDTO toPostGetDTO(final Post post) {
        return PostGetDTO.builder()
                .id(post.getId())
                .imageUrl(post.getImageUrl())
                .description(post.getDescription())
                .poster(UserInfoMapper.toUserInfoDTO(post.getPoster()))
                .postDate(post.getPostDate())
                .tags(post.getTags())
                .userTags(
                        post.getUserTags().stream()
                                .map(UserInfoMapper::toUserInfoDTO)
                                .collect(Collectors.toSet()))
                .likes(post.getLikes())
                .dislikes(post.getDislikes())
                .favorites(post.getFavorites())
                .comments(
                        CommentMapper.toCommentGetDTOList(
                                post.getComments().stream()
                                        .sorted(
                                                Comparator.comparing(Comment::getCommentDate)
                                                        .reversed())
                                        .collect(Collectors.toList())))
                .build();
    }

    public static PostOverviewDTO toPostOverviewDTO(final Post post) {
        return PostOverviewDTO.builder()
                .id(post.getId())
                .imageUrl(post.getImageUrl())
                .description(post.getDescription())
                .poster(UserInfoMapper.toUserInfoDTO(post.getPoster()))
                .postDate(post.getPostDate())
                .tags(post.getTags())
                .userTags(
                        post.getUserTags().stream()
                                .map(UserInfoMapper::toUserInfoDTO)
                                .collect(Collectors.toSet()))
                .likes_count(post.getLikes().size())
                .dislikes_count(post.getDislikes().size())
                .favorites_count(post.getFavorites().size())
                .comments_count(post.getComments().size())
                .build();
    }

    public static Page<PostOverviewDTO> toPostOverviewPage(final Page<Post> pagedPosts) {
        return new PageImpl<>(
                pagedPosts.getContent().stream()
                        .map(PostMapper::toPostOverviewDTO)
                        .collect(Collectors.toList()),
                pagedPosts.getPageable(),
                pagedPosts.getTotalElements());
    }

    public static SearchPage<PostOverviewDTO> toPostOverviewSearchReturnPage(
            final SearchPage<Post> searchPage) {
        final SearchHits<Post> searchHits = searchPage.getSearchHits();
        final SearchHits<PostOverviewDTO> overviewSearchHits =
                new SearchHitsImpl<>(
                        searchHits.getTotalHits(),
                        searchHits.getTotalHitsRelation(),
                        searchHits.getMaxScore(),
                        null,
                        searchHits.getSearchHits().stream()
                                .map(
                                        sh ->
                                                new SearchHit<>(
                                                        sh.getIndex(),
                                                        sh.getId(),
                                                        sh.getRouting(),
                                                        sh.getScore(),
                                                        sh.getSortValues().toArray(),
                                                        sh.getHighlightFields(),
                                                        sh.getInnerHits(),
                                                        sh.getNestedMetaData(),
                                                        sh.getExplanation(),
                                                        sh.getMatchedQueries(),
                                                        PostMapper.toPostOverviewDTO(
                                                                sh.getContent())))
                                .collect(Collectors.toList()),
                        searchHits.getAggregations());
        return SearchHitSupport.searchPageFor(overviewSearchHits, searchPage.getPageable());
    }
}

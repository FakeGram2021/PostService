package tim6.postservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import tim6.postservice.post.intergration.comments.CommentOnPostTest;
import tim6.postservice.post.intergration.creation.CreatePostTest;
import tim6.postservice.post.intergration.dislikes.DislikePostTest;
import tim6.postservice.post.intergration.dislikes.RemovePostDislikeTest;
import tim6.postservice.post.intergration.favorites.FavoritePostTest;
import tim6.postservice.post.intergration.favorites.RemoveFavoritePostTest;
import tim6.postservice.post.intergration.feed.GetFeedTest;
import tim6.postservice.post.intergration.likes.LikePostTest;
import tim6.postservice.post.intergration.likes.RemovePostLikeTest;
import tim6.postservice.post.intergration.search.SearchByTagsTest;
import tim6.postservice.post.intergration.search.SearchByUserIdTest;

@RunWith(Suite.class)
@SuiteClasses({
    CreatePostTest.class,
    LikePostTest.class,
    RemovePostLikeTest.class,
    DislikePostTest.class,
    RemovePostDislikeTest.class,
    FavoritePostTest.class,
    RemoveFavoritePostTest.class,
    SearchByUserIdTest.class,
    SearchByTagsTest.class,
    GetFeedTest.class,
    CommentOnPostTest.class
})
class PostServiceApplicationTests {}

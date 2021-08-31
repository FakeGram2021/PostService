package tim6.postservice.domain.repositories;

import java.util.Collection;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import tim6.postservice.domain.models.Post;

@Repository
public interface PostRepository extends ElasticsearchRepository<Post, String> {

    Collection<Post> findAllByPosterId(final String posterId);
}

package tim6.postservice.domain.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import tim6.postservice.domain.models.Comment;

@Repository
public interface CommentRepository extends ElasticsearchRepository<Comment, String> {}

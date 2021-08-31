package tim6.postservice.domain.repositories;

import java.util.Collection;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import tim6.postservice.domain.models.User;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {
    Collection<User> getAllByIdIn(Collection<String> ids);

    Collection<User> getAllByUsernameIn(Collection<String> userNames);
}

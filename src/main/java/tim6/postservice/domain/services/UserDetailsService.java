package tim6.postservice.domain.services;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tim6.postservice.domain.exceptions.EntityNotFoundException;
import tim6.postservice.domain.models.User;

@Service
@Primary
public class UserDetailsService
        implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String userId) throws UsernameNotFoundException {
        try {
            final User user = this.userService.findById(userId);
            return new org.springframework.security.core.userdetails.User(
                    user.getId(), user.getUsername(), new ArrayList<>());
        } catch (final EntityNotFoundException ex) {
            throw new UsernameNotFoundException(
                    String.format("There is no user with id: %s", userId));
        }
    }
}

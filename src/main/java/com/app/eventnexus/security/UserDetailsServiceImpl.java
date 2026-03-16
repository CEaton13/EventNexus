package com.app.eventnexus.security;

import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation.
 * Loads a {@link User} from the database by username and wraps it in a
 * {@link UserPrincipal} for use in the authentication filter chain.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for Spring Security authentication.
     *
     * @param username the username to look up
     * @return a {@link UserPrincipal} wrapping the found user
     * @throws UsernameNotFoundException if no user with that username exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
        return new UserPrincipal(user);
    }
}

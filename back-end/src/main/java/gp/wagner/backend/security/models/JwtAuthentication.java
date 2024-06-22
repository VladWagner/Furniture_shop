package gp.wagner.backend.security.models;

import gp.wagner.backend.domain.entities.users.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
public class JwtAuthentication implements Authentication {

    private final UserDetailsImpl userDetails;

    private boolean isAuthenticated;

    public JwtAuthentication(User user, boolean isAuthenticated) {
        this.userDetails = new UserDetailsImpl(user);
        this.isAuthenticated = isAuthenticated;
    }

    public JwtAuthentication(UserDetailsImpl userDetails, boolean isAuthenticated) {
        this.userDetails = userDetails;
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return userDetails;
    }

    @Override
    public Object getPrincipal() {
        return userDetails.getUser() != null ? userDetails.getUser() : userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return userDetails.getUsername();
    }
}

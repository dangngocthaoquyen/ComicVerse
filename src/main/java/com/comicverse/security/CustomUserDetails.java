package com.comicverse.security;

import com.comicverse.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role trong DB: "ADMIN" / "USER"
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // login bằng email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // ✅ LOCKED -> không cho login (Spring sẽ ném LockedException)
    @Override
    public boolean isAccountNonLocked() {
        String status = user.getStatus();
        return status == null || !"LOCKED".equalsIgnoreCase(status);
    }

    // ✅ Enabled: bạn hiện chưa có DISABLED, nên cứ true
    // (để LOCKED vẫn ra đúng LockedException)
    @Override
    public boolean isEnabled() {
        return true;
    }
}

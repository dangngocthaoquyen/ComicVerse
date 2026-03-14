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
        // hasAuthority("ADMIN") sẽ match với "ADMIN"
        String authority = (user.getRole() == null) ? "USER" : user.getRole().name();
        return List.of(new SimpleGrantedAuthority(authority));
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

    // ✅ LOCKED -> LockedException
    @Override
    public boolean isAccountNonLocked() {
        User.Status status = user.getStatus();
        if (status == null) return true; // dữ liệu cũ
        return status != User.Status.LOCKED;
    }

    // ✅ Vì bạn không có DISABLED nên giữ true để LOCKED ra đúng LockedException
    @Override
    public boolean isEnabled() {
        return true;
    }
}

package com.iot.custom;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private final Integer deviceId;

    public DeviceAuthenticationToken(Integer deviceId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_DEVICE")));
        this.deviceId = deviceId;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return deviceId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}

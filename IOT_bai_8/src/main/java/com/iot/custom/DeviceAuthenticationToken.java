package com.iot.custom;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private final Integer deviceId;

    public DeviceAuthenticationToken(Integer deviceId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_DEVICE")));
        this.deviceId = deviceId;
        setAuthenticated(true);
    }

    public Integer getDeviceId() {
        return deviceId;
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

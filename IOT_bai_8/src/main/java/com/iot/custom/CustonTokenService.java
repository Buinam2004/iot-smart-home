package com.iot.custom;

import com.iot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustonTokenService {

    private final JwtTokenProvider jwtTokenProvider;
}

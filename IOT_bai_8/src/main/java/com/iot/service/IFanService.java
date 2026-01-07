package com.iot.service;

import com.iot.entity.Fan;

import java.util.Optional;

public interface IFanService {
    Optional<Fan> getStateFan(Integer deviceId);
}

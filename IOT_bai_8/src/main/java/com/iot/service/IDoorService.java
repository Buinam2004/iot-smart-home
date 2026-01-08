package com.iot.service;

import com.iot.entity.Door;

import java.util.Optional;

public interface IDoorService {
    Optional<Door> getStateDoor(Integer deviceId);
}

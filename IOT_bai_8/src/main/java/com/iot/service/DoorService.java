package com.iot.service;

import com.iot.entity.Door;
import com.iot.repository.DoorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DoorService implements IDoorService {

    @Autowired
    private DoorRepository doorRepository;

    @Override
    public Optional<Door> getStateDoor(Integer deviceId) {
        return doorRepository.getLastDoorState(deviceId);
    }
}

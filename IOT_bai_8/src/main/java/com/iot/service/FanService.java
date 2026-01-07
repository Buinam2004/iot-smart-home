package com.iot.service;

import com.iot.entity.Fan;
import com.iot.repository.FanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FanService implements IFanService{

    @Autowired
    private FanRepository fanRepository;

    @Override
    public Optional<Fan> getStateFan(Integer deviceId) {
        return fanRepository.getLastFanState(deviceId);
    }
}

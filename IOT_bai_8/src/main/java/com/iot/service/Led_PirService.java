package com.iot.service;

import com.iot.entity.Led_Pir;
import com.iot.repository.Led_PirRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Led_PirService implements ILed_PirService {

    @Autowired
    private Led_PirRepository led_PirRepository;

    @Override
    public Optional<Led_Pir> getStateLed_Pir(Integer deviceId) {
        return led_PirRepository.getLastLed_PirState(deviceId);
    }
}

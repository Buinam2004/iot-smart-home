package com.iot.service;

import com.iot.entity.Led_Pir;

import java.util.Optional;

public interface ILed_PirService {
    Optional<Led_Pir> getStateLed_Pir(Integer deviceId);
}

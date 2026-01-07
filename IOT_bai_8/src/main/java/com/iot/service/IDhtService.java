package com.iot.service;

import com.iot.entity.DhtSensor;
import org.springframework.data.domain.Page;

public interface IDhtService {
    Page<DhtSensor> getDhtData(
            Integer deviceId,
            int page,
            int size
    );
}

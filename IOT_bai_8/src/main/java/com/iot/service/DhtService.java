package com.iot.service;

import com.iot.entity.DhtSensor;
import com.iot.repository.DhtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DhtService implements IDhtService {

    private final DhtRepository dhtRepository;

    @Override
    public Page<DhtSensor> getDhtData(
            Integer deviceId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("receivedAt").descending()
        );

        return dhtRepository.findByDeviceIdOrderByReceivedAtDesc(deviceId, pageable);
    }
}

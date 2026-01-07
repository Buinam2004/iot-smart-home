package com.iot.controller;

import com.iot.entity.DhtSensor;
import com.iot.service.IDhtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dhtsensor")
public class DhtSensorController {

    @Autowired
    private IDhtService dhtService;

    @GetMapping
    public Page<DhtSensor> getRecentDhtDatas(@RequestParam Integer deviceId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return dhtService.getDhtData(deviceId, page, size);
    }
}

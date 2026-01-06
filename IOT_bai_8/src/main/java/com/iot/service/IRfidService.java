package com.iot.service;

import com.iot.entity.Rfid;

public interface IRfidService {
    Rfid createRfid(String uid, Integer deviceId ,Integer userId);
    boolean checkRfid(String uid, Integer deviceId);
}

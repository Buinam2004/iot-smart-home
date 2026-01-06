package com.iot.service;


import com.iot.entity.Rfid;
import com.iot.repository.RfidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RfidService implements IRfidService {

    @Autowired
    private RfidRepository rfidRepository;

    @Override
    public Rfid createRfid(String uid, Integer deviceId ,Integer userId) {
        Rfid rfid = new Rfid();
        rfid.setUid(uid);
        rfid.setUserId(userId);
        rfid.setDeviceId(deviceId);
        rfidRepository.save(rfid);

        return rfid;
    }

    @Override
    public boolean checkRfid(String uid, Integer deviceId) {
        if(rfidRepository.existsByUidAndDeviceId(uid, deviceId)) return true;
        return false;
    }
}

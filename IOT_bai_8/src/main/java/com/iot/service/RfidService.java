package com.iot.service;


import com.iot.entity.Rfid;
import com.iot.repository.RfidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RfidService implements IRfidService {
    private final RfidRepository rfidRepository;

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
        return rfidRepository.existsByUidAndDeviceId(uid, deviceId);
    }
}

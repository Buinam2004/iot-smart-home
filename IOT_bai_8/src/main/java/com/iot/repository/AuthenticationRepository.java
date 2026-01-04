package com.iot.repository;

import com.iot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<User, Integer> {
}

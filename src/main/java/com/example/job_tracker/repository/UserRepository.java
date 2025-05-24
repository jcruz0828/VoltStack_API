package com.example.job_tracker.repository;

import com.example.job_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;



public interface UserRepository extends JpaRepository<User,Long> {

    User findByEmail(String email);
}

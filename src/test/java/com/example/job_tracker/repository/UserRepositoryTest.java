package com.example.job_tracker.repository;

import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.service.user.iUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)// uses H2
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    @Test
    void findByEmail_returnsUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("secret");

        userRepository.save(user);

        User found = userRepository.findByEmail("john@example.com");

        assertNotNull(found);
        assertEquals("John", found.getFirstName());
    }

    @Test
    void findByEmail_notFound() {
        User found = userRepository.findByEmail("missing@example.com");
        assertNull(found);
    }
}

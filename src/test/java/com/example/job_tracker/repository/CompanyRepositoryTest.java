package com.example.job_tracker.repository;

import com.example.job_tracker.model.Company;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class CompanyRepositoryTest {
    @Autowired
    private CompanyRepository companyRepository;


    @Test
    void findByNameStartingWithIgnoreCaseCustom() {
        Company company = new Company();
        company.setName("Test Company");
        company.setDescription("A company for testing purposes");
        companyRepository.save(company);
        Company found = companyRepository.findByNameIgnoreCase("test company");
        assertNotNull(found);
        assertEquals("Test Company", found.getName());
    }
    @Test
    void findByNameStartingWithIgnoreCaseCustom_notFound() {
        Company found = companyRepository.findByNameIgnoreCase("nonexistent company");
        assertNull(found);
    }
    @Test
    void findByNameStartingWithIgnoreCaseCustomTest() {
        Company company = new Company();
        company.setName("Test Company");
        company.setDescription("A company for testing purposes");
        companyRepository.save(company);

        var companies = companyRepository.findByNameStartingWithIgnoreCaseCustom("test");
        assertNotNull(companies);
        assertEquals(1, companies.size());
        assertEquals("Test Company", companies.get(0).getName());
    }
}

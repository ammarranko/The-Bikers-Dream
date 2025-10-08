package com.soen343.tbd;

import com.soen343.tbd.infrastructure.persistence.entity.User;
import com.soen343.tbd.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class StartupFetch {
    private static final Logger logger = LoggerFactory.getLogger(StartupFetch.class);

    @Bean
    CommandLineRunner run(UserRepository repo) {
        return args -> {
            logger.info("Starting database query...");
            try {
                logger.info("Executing findAll() on repository...");
                var users = repo.findAll();
                logger.info("Query executed successfully");

                // Convert to list to check size
                var userList = new java.util.ArrayList<User>();
                users.forEach(userList::add);
                logger.info("Found {} users in database", userList.size());

                if (userList.isEmpty()) {
                    logger.info("No users found in the database.");
                } else {
                    logger.info("Found users in database:");
                    userList.forEach(user -> {
                        logger.info("User data: ID={}, Email={}, Name={}",
                            user.getId(),
                            user.getEmail(),
                            user.getFullName()
                        );
                    });
                }
            } catch (Exception e) {
                logger.error("Error fetching users from database: ", e);
            }
        };
    }
}

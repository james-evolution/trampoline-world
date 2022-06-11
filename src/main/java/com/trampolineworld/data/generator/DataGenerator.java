package com.trampolineworld.data.generator;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.TrampolineOrderRepository;
import com.trampolineworld.data.service.UserRepository;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository,
            TrampolineOrderRepository trampolineOrderRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 2 User entities...");
            User user = new User();
            user.setName("John Normal");
            user.setUsername("user");
            user.setHashedPassword(passwordEncoder.encode("user"));
            user.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);
            User admin = new User();
            admin.setName("Emma Powerful");
            admin.setUsername("admin");
            admin.setHashedPassword(passwordEncoder.encode("admin"));
            admin.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1607746882042-944635dfe10e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            admin.setRoles(Set.of(Role.USER, Role.ADMIN));
            userRepository.save(admin);
            logger.info("... generating 100 Trampoline Order entities...");
            ExampleDataGenerator<TrampolineOrder> trampolineOrderRepositoryGenerator = new ExampleDataGenerator<>(
                    TrampolineOrder.class, LocalDateTime.of(2022, 6, 11, 0, 0, 0));
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setStatus, DataType.BOOLEAN_50_50);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setOrderId, DataType.NUMBER_UP_TO_1000);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setFirstName, DataType.FIRST_NAME);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setLastName, DataType.LAST_NAME);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setPhoneNumber, DataType.PHONE_NUMBER);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setEmail, DataType.EMAIL);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setOrderDescription, DataType.SENTENCE);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setMeasurements, DataType.TWO_WORDS);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setPrice, DataType.NUMBER_UP_TO_1000);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setSubtotal, DataType.NUMBER_UP_TO_1000);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setTotal, DataType.NUMBER_UP_TO_1000);
            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setDate, DataType.DATE_LAST_30_DAYS);
            trampolineOrderRepository.saveAll(trampolineOrderRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}
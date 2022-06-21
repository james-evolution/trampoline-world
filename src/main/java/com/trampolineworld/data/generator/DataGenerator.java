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
import java.util.UUID;

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

            logger.info("Generating demo data");
            logger.info("... generating 2 User entities...");
            
            User user = new User();
            user.setId(UUID.fromString("275626f5-d4a0-4ce7-b90f-f34e236b8c6f"));
            user.setDisplayName("TW User");
            user.setUsername("TW User");
            user.setColorIndex(1);
            user.setHashedPassword(passwordEncoder.encode("user"));
            user.setProfilePictureUrl(
                    "https://static.wixstatic.com/media/759627_2ad5404df0dc4455af631dbeaf83e8bf~mv2.png/v1/fill/w_347,h_347,al_c,q_85,usm_0.66_1.00_0.01,enc_auto/Trampoline-2.png");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);

            User tech = new User();
            tech.setId(UUID.fromString("52d1376a-b8d4-4d60-874e-b804d078f780"));
            tech.setDisplayName("TW Tech");
            tech.setUsername("TW Tech");
            tech.setColorIndex(6);
            tech.setHashedPassword(passwordEncoder.encode("stranger410"));
            tech.setProfilePictureUrl(
            		"https://static.wixstatic.com/media/759627_2ad5404df0dc4455af631dbeaf83e8bf~mv2.png/v1/fill/w_347,h_347,al_c,q_85,usm_0.66_1.00_0.01,enc_auto/Trampoline-2.png");
            tech.setRoles(Set.of(Role.TECH, Role.ADMIN));
            userRepository.save(tech);
       
            User admin = new User();
            admin.setId(UUID.fromString("e7f5d385-b7c6-49d6-9fc2-56b264fa2796"));
            admin.setDisplayName("TW Admin");
            admin.setUsername("TW Admin");
            admin.setColorIndex(3);
            admin.setHashedPassword(passwordEncoder.encode("ozzyandbear410"));
            admin.setProfilePictureUrl(
            		"https://static.wixstatic.com/media/759627_2ad5404df0dc4455af631dbeaf83e8bf~mv2.png/v1/fill/w_347,h_347,al_c,q_85,usm_0.66_1.00_0.01,enc_auto/Trampoline-2.png");
//            admin.setRoles(Set.of(Role.USER, Role.ADMIN));
            admin.setRoles(Set.of(Role.ADMIN));
            userRepository.save(admin);
       
//            logger.info("... generating 100 Trampoline Order entities...");
//            ExampleDataGenerator<TrampolineOrder> trampolineOrderRepositoryGenerator = new ExampleDataGenerator<>(
//                    TrampolineOrder.class, LocalDateTime.of(2022, 6, 11, 0, 0, 0));
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setStatus, DataType.BOOLEAN_50_50);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setFirstName, DataType.FIRST_NAME);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setLastName, DataType.LAST_NAME);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setPhoneNumber, DataType.PHONE_NUMBER);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setEmail, DataType.EMAIL);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setOrderDescription, DataType.SENTENCE);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setMeasurements, DataType.TWO_WORDS);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setPrice, DataType.PRICE);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setSubtotal, DataType.PRICE);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setTotal, DataType.PRICE);
//            trampolineOrderRepositoryGenerator.setData(TrampolineOrder::setDate, DataType.DATE_LAST_30_DAYS);
//            trampolineOrderRepository.saveAll(trampolineOrderRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}
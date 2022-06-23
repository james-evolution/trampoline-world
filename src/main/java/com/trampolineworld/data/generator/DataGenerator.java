package com.trampolineworld.data.generator;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.entity.Webhook;
import com.trampolineworld.data.service.TrampolineOrderRepository;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.WebhookRepository;
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
            TrampolineOrderRepository trampolineOrderRepository, WebhookRepository webhookRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            
            // Check if the user database is NOT EMPTY.
            if (userRepository.count() != 0L) {
            	// If not empty, use existing database.
                logger.info("Using existing database");
//                return; // Return to exit method.
            }
            else {
                /*
                 * GENERATE DEFAULT SYSTEM USERS.
                 */
                
                // Generate TW User with Uer permissions.
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

                // Generate TW Tech with Admin & Tech permissions.
                User tech = new User();
                tech.setId(UUID.fromString("52d1376a-b8d4-4d60-874e-b804d078f780"));
                tech.setDisplayName("TW Tech");
                tech.setUsername("TW Tech");
                tech.setColorIndex(6);
                tech.setHashedPassword(passwordEncoder.encode("stranger410"));
                tech.setProfilePictureUrl(
                		"https://static.wixstatic.com/media/759627_2ad5404df0dc4455af631dbeaf83e8bf~mv2.png/v1/fill/w_347,h_347,al_c,q_85,usm_0.66_1.00_0.01,enc_auto/Trampoline-2.png");
                tech.setRoles(Set.of(Role.ADMIN, Role.TECH));
                userRepository.save(tech);
           
                // Generate TW Admin with Admin permissions.
                User admin = new User();
                admin.setId(UUID.fromString("e7f5d385-b7c6-49d6-9fc2-56b264fa2796"));
                admin.setDisplayName("TW Admin");
                admin.setUsername("TW Admin");
                admin.setColorIndex(3);
                admin.setHashedPassword(passwordEncoder.encode("ozzyandbear410"));
                admin.setProfilePictureUrl(
                		"https://static.wixstatic.com/media/759627_2ad5404df0dc4455af631dbeaf83e8bf~mv2.png/v1/fill/w_347,h_347,al_c,q_85,usm_0.66_1.00_0.01,enc_auto/Trampoline-2.png");
                admin.setRoles(Set.of(Role.ADMIN, Role.TECH));
                userRepository.save(admin);
            }

            
            // Check if the webhook database is NOT EMPTY.
            if (webhookRepository.count() != 0L) {
            	// If not empty, use existing database.
                logger.info("Using existing database");
//                return; // Return to exit method.
            }
            else {
            	// For contacting the developer on Discord via text-to-speech messages.
            	Webhook contactWebhook = new Webhook();
            	contactWebhook.setWebhookName("Developer Contact");
            	contactWebhook.setWebhookUrl("https://ptb.discord.com/api/webhooks/988724055765033000/tSmaOypQVKtCkDBzpWCLWIF-drMcLKun0Otjd0Rrt79evjno_4Bb9bxkYP86nK5F2-SP");
            	webhookRepository.save(contactWebhook);
            	
            	// For notifying the developer of CollaborationEngine license events, as documented at https://vaadin.com/docs/latest/tools/ce/going-to-production
            	Webhook licenseEventsWebhook = new Webhook();
            	licenseEventsWebhook.setWebhookName("CE License Events");
            	licenseEventsWebhook.setWebhookUrl("https://ptb.discord.com/api/webhooks/988366724682379294/g20NbSzfeL_QrZhZVWt-2rJh4I6MmSU_FtkPNv-9qeYq1MHbs5TKsv1g2NkMq8TLYT9o");
            	webhookRepository.save(licenseEventsWebhook);
            	
            	// For backing up the audit logs to Discord.
            	Webhook auditLogWebhook = new Webhook();
            	auditLogWebhook.setWebhookName("Logs (Audit)");
            	auditLogWebhook.setWebhookUrl("https://ptb.discord.com/api/webhooks/988942093059784734/AUdjyyXznFlN1T7IovybkPlu6h_HEdVK4gE80uTgvRiIKEg7UXrvQaHvLtbV66zuwFRY");
            	webhookRepository.save(auditLogWebhook);
            	
            	// For persisting chat logs to Discord (optional - if Admins desire this)
            	Webhook chatLogsWebhook = new Webhook();
            	chatLogsWebhook.setWebhookName("Logs (Chat)");
            	chatLogsWebhook.setWebhookUrl("https://ptb.discord.com/api/webhooks/988770668554375240/r-W13i9JLGQxmvV-6UzLiPVtTCzKBzG_jNUaJiwUtislZ4t_7MqflRb3uPTW1A93SjDL");
            	webhookRepository.save(chatLogsWebhook);

            	// For logging the universally unique identifiers (UUIDS) of newly created users. This allows us to re-use them to avoid surpassing the 20 user CollaborationEngine monthly quota.
            	Webhook userUuidWebhook = new Webhook();
            	userUuidWebhook.setWebhookName("Logs (UUIDs)");
            	userUuidWebhook.setWebhookUrl("https://ptb.discord.com/api/webhooks/988570358691016784/MWE8EIOOh7-Eohofs0Dp6Wu6DiyEmr91hUcUXBMnyt6t0esLraN7XPTc-fKTNpfOjjvW");
            	webhookRepository.save(userUuidWebhook);
            	
            	// For routing debug logs to Discord for developer usage.
            	Webhook debugLogsWebhook = new Webhook();
            	debugLogsWebhook.setWebhookName("Logs (Debug)");
            	debugLogsWebhook.setWebhookUrl("https://ptb.discord.com/api/webhooks/988568130093744218/xoLscoKMWCX3_7t63MESyA4FW3P_KSY6dlLB0hzYxbrqw6mTLlLsMXr7GlBbYd5rI3Ku");
            	webhookRepository.save(debugLogsWebhook);
            }
        };
    }

}
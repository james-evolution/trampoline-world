package com.trampolineworld.views.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.*;

@Route(value = "forgot_password", layout = MainLayout.class)
@PageTitle("Forgot Password")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@PermitAll
public class ForgotPasswordView extends HorizontalLayout {
  @Autowired
  private JavaMailSender emailSender;
  private final UserService userService;
  private final UserRepository userRepository;
  private String resetCode;
  private User userLostPassword;
  private VerticalLayout layout = new VerticalLayout();
  
  public ForgotPasswordView(UserService userService, UserRepository userRepository) {
    addClassNames("userguide-view");
    setId("userguide-view");
    this.userService = userService;
    this.userRepository = userRepository;
    
    // Create components.
    loadResetCodeComponents(userRepository);
    
    // Add vertical layout.
    add(layout);
  }

  private void loadResetCodeComponents(UserRepository userRepository) {
    /*
     * CREATE COMPONENTS FOR SENDING RESET CODE.
     * ADD TO LAYOUT.
     */
    Paragraph paragraphEnterUsernameInstructions = new Paragraph("Please enter your username so a reset code can be sent to your email.");
    TextField inputUsername = new TextField();
    inputUsername.setHelperText("Enter your username here");
    Button sendResetCodeButton = new Button();
    sendResetCodeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_PRIMARY);
    
    sendResetCodeButton.addClickListener(e -> {
      try {
        String username = inputUsername.getValue();
        userLostPassword = userRepository.findByUsername(username);
        String email = userLostPassword.getEmail();
        // Generate UUID.
        resetCode = UUID.randomUUID().toString();
        // Send reset code via email.
        sendEmail(email, "Forgot Password",
            "Hello, " + username + ".\n\nYour reset code: " + resetCode);
      } 
      catch (Exception exception) {
        Notification.show("Email failed to send.", 4000, Position.TOP_CENTER)
        .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    }); // End listener.
    layout.add(paragraphEnterUsernameInstructions, inputUsername, sendResetCodeButton);
    
    /*
     * CREATE COMPONENTS FOR RECEIVING RESET CODE.
     * ADD TO LAYOUT.
     */
    Paragraph instructionsParagraph = new Paragraph(
        "Please enter the reset code that was sent to your email. (Make sure you check your spam folder for the email)");
    TextField inputResetCode = new TextField();
    inputResetCode.setHelperText("Enter your reset code here");
    
    // Create buttons.
    Button submitResetCodeButton = new Button("Submit");
    submitResetCodeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_PRIMARY);

    // Configure click listened for send button.
    submitResetCodeButton.addClickListener(e -> {
      // Get user object from username. Get email from user object.
      String enteredResetCode = inputResetCode.getValue().trim(); // Trim leading & trailing whitespaces.
      
      System.out.println(enteredResetCode);
      System.out.println(resetCode);

      if (enteredResetCode.equals(resetCode)) {
//          userLostPassword
        // Notify of success.
        Notification.show("Reset code verified.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      } else {
        Notification.show("Incorrect code.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    }); // End submitResetCodeButton listener.
    layout.add(instructionsParagraph, inputResetCode, submitResetCodeButton);
    
  } // End method.
  
  public void sendEmail(String recipient, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("james.evolution.1993@gmail.com");
    message.setTo(recipient);
    message.setSubject(subject);
    message.setText(text);
    emailSender.send(message);
  }
}

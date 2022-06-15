package com.trampolineworld.views.userguide;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import com.trampolineworld.views.*;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;

@Route(value = "userguide", layout = MainLayout.class)
@PageTitle("User Guide")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(
	    themeFor = "vaadin-horizontal-layout",
	    value = "/themes/trampolineworld/views/userguide-theme.css"
	)
@RolesAllowed("ADMIN")
public class UserGuideView extends HorizontalLayout {
	
	private Tabs tabs;
	private H2 header;
	private IFrame desktopDemoFrame = new IFrame();
	private Label createLabel, editLabel, viewLabel, deleteLabel, fontSizeLabel, emailLabel, discordLabel;
	private Paragraph createParagraph, editParagraph, viewParagraph, deleteParagraph, fontSizeParagraph, emailParagraph, discordParagraph, contactParagraph;
	private Button goBackButton = new Button("Go Back");
	private VerticalLayout layout;
	private HorizontalLayout contactRow1, contactRow2;

    public UserGuideView() {
    	addClassNames("userguide-view");
    	setId("userguide-view");
    	
    	// Create tabs.
    	Tab documentationTab = new Tab("Documentation");
    	documentationTab.setId("documentationTab");
    	Tab contactTab = new Tab("Support");
    	contactTab.setId("contactTab");
        tabs = new Tabs(documentationTab, contactTab);
        tabs.setWidthFull();
    	
    	header = new H2("How to Use This Application");
    	
		desktopDemoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/-ZnYEI14B8c");
		desktopDemoFrame.getElement().setAttribute("title", "YouTube video player");
		desktopDemoFrame.getElement().setAttribute("frameborder", "0");
		desktopDemoFrame.getElement().setAttribute("allow", "accelerometer");
		desktopDemoFrame.getElement().setAttribute("autoplay", "true");
		desktopDemoFrame.getElement().setAttribute("allowfullscreen", "true");
    	
    	createLabelElements();
        createParagraphElements();
        createContactRows();    	
        
        // Configure button appearance and click listener.
        goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goBackButton.addClickListener(e -> {
        	UI.getCurrent().navigate(TrampolineOrdersView.class); // Send user back to the Trampoline Orders page.
        });
        
        // Create layout & add components to it.
        layout = new VerticalLayout();
        layout.add(tabs);
        layout.add(header);
        layout.add(desktopDemoFrame);
        layout.add(createLabel, createParagraph);
        layout.add(editLabel, editParagraph);
        layout.add(viewLabel, viewParagraph);
        layout.add(deleteLabel, deleteParagraph);
        layout.add(fontSizeLabel, fontSizeParagraph);
        layout.add(goBackButton);
        this.setClassName("userguide-layout");

        // Add layout to the view.
        this.add(layout);
//        setSizeFull();;
        
        // Listen for tab changes, load layout components accordingly.
        tabs.addSelectedChangeListener(event -> {
        	
        	Optional<String> tabID = event.getSelectedTab().getId();
        	
        	if (tabID.equals(documentationTab.getId())) {
        		loadDocumentationTab();
        	}
        	else if (tabID.equals(contactTab.getId())) {
        		loadContactTab();
        	}
        });
    }

	private void createContactRows() {
		// Create contact rows.
        contactRow1 = new HorizontalLayout();
        contactRow1.setAlignItems(Alignment.BASELINE);
        contactRow1.add(emailLabel, emailParagraph);
        contactRow2 = new HorizontalLayout();
        contactRow2.setAlignItems(Alignment.BASELINE);
        contactRow2.add(discordLabel, discordParagraph);
	}

	private void createParagraphElements() {
		// Create HTML paragraph elements.
    	createParagraph = new Paragraph("Simply click the New Order button at the top of the Trampoline Orders page : )");
    	editParagraph = new Paragraph("Left click on whichever row you wish to edit. A form will appear that is automatically populated with that particular order's data. "
    			+ "\nYou can then edit the desired fields and finalize your changes by clicking the Save button at the bottom of the form.");
    	viewParagraph = new Paragraph("Right click the row and select View from the context menu. This will bring up a more detailed view of the order on a separate page. This is the best method of viewing orders with long descriptions.");
    	deleteParagraph = new Paragraph("Right click the row and select Delete from the context menu. A confirmation dialog will appear and you'll have to click the Delete button again to confirm the action. This is to prevent accidental deletions.");
    	fontSizeParagraph = new Paragraph("To change the font size of the application, simply zoom in or out with Ctrl + or Ctrl - on the keyboard.");
    	emailParagraph = new Paragraph("admin@evolutioncoding.net");
    	discordParagraph = new Paragraph("James Z#0136");
    	contactParagraph = new Paragraph("Feel free to reach out to the developer through email or Discord. If you know his personal number, you may reach him there as well.");
	}

	private void createLabelElements() {
		// Create HTML label elements.
        createLabel = new Label("Creating Orders:");
        createLabel.addClassName("coloredLabel");
        editLabel = new Label("Editing Orders:");
        editLabel.addClassName("coloredLabel");
        viewLabel = new Label("Viewing Orders:");
        viewLabel.addClassName("coloredLabel");
        deleteLabel = new Label("Deleting Orders:");
        deleteLabel.addClassName("coloredLabel");
        fontSizeLabel = new Label("Changing Font Size:");
        fontSizeLabel.addClassName("coloredLabel");        
        emailLabel = new Label("Email:");
        emailLabel.addClassName("coloredLabel");
        discordLabel = new Label("Discord:");
        discordLabel.addClassName("coloredLabel");
	}

	private void loadContactTab() {
		layout.removeAll();
        layout.add(tabs);
        header.setText("Need help?");
        layout.add(header);
        layout.add(contactParagraph);
        layout.add(contactRow1, contactRow2);
        layout.add(goBackButton);
	}

	private void loadDocumentationTab() {
		// https://www.youtube.com/embed/-ZnYEI14B8c
		layout.removeAll();
        layout.add(tabs);
        header.setText("How to Use This Application");
        layout.add(header);
        layout.add(desktopDemoFrame);
        layout.add(createLabel, createParagraph);
        layout.add(editLabel, editParagraph);
        layout.add(viewLabel, viewParagraph);
        layout.add(deleteLabel, deleteParagraph);
        layout.add(fontSizeLabel, fontSizeParagraph);
        layout.add(goBackButton);		
	}
}
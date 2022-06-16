package com.trampolineworld.views.installation;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import com.trampolineworld.views.MainLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.shared.ContentMode;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "installationguide", layout = MainLayout.class)
@CssImport(
		themeFor = "vaadin-horizontal-layout",
		value = "./themes/trampolineworld/views/installationguide-theme.css"
		)
@PageTitle("Installation Guide")
@RolesAllowed("ADMIN")
public class InstallationGuideView extends HorizontalLayout {
	
	private VerticalLayout layout = new VerticalLayout();
	private Tabs tabs;
	
	private H2 installationHeader = new H2();
	private H2 demoHeader = new H2();
	
	private Paragraph installationDescription = new Paragraph();
	private Paragraph demoDescription = new Paragraph();
	
	private IFrame desktopInstallationFrame = new IFrame();
	private IFrame mobileInstallationFrame = new IFrame();
	
	private IFrame desktopDemoFrame = new IFrame();
	private IFrame mobileDemoFrame = new IFrame();
	
	public InstallationGuideView() {
    	addClassNames("installationguide-view");
    	setId("installationguide-view");
    	
    	// Create tabs.
    	Tab desktopApplicationTab = new Tab("Desktop Application");
    	desktopApplicationTab.setId("desktopApplicationTab");
    	Tab mobileApplicationTab = new Tab("Mobile Application");
    	mobileApplicationTab.setId("mobileApplicationTab");
        tabs = new Tabs(desktopApplicationTab, mobileApplicationTab);
        tabs.setWidthFull();
        
        loadDesktopComponentData(); // Load components for initial view.
        
        // Configure horizontal layout & add vertical layout.
        this.setClassName("installationguide-layout");
        this.add(layout);
        
        // Listen for tab changes, load layout components accordingly.
        tabs.addSelectedChangeListener(event -> {
        	Optional<String> tabID = event.getSelectedTab().getId();
        	if (tabID.equals(desktopApplicationTab.getId())) {
                loadDesktopComponentData();
        	}
        	else if (tabID.equals(mobileApplicationTab.getId())) {
                loadMobileComponentData();
        	}
        });
	}

	private void loadDesktopComponentData() {
		installationHeader.setText("Installing the Desktop Application");
		installationDescription.setText("\nInstalling this system as a standalone desktop application is quite an easy process! It should take under a minute.\n"
				+ "\nOn Windows, please make sure you're using the Google Chrome browser if you decide to do this, as not all browsers offer this capability."
				+ "\nOn Apple devices, you'll want to use Safari. It's possible that the icon may look like a plus sign instead of the one showcased in the video.\n");
		desktopInstallationFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/t65jdhNLmm8");
		desktopInstallationFrame.getElement().setAttribute("title", "YouTube video player");
		desktopInstallationFrame.getElement().setAttribute("frameborder", "0");
		desktopInstallationFrame.getElement().setAttribute("allow", "accelerometer");
		desktopInstallationFrame.getElement().setAttribute("autoplay", "true");
		desktopInstallationFrame.getElement().setAttribute("allowfullscreen", "true");
		demoHeader.setText("Demo of the Desktop Application");
//		demoDescription.setText("The below video will showcase a full walkthrough of the desktop application's features.");
//		desktopDemoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/t65jdhNLmm8");
//		desktopDemoFrame.getElement().setAttribute("title", "YouTube video player");
//		desktopDemoFrame.getElement().setAttribute("frameborder", "0");
//		desktopDemoFrame.getElement().setAttribute("allow", "accelerometer");
//		desktopDemoFrame.getElement().setAttribute("autoplay", "true");
//		desktopDemoFrame.getElement().setAttribute("allowfullscreen", "true");
		
		
		// Clear layout and add components back into it.
		layout.removeAll();
        layout.add(tabs);
        layout.add(installationHeader);
        layout.add(desktopInstallationFrame);
        layout.add(installationDescription);
//        layout.add(demoHeader);
//        layout.add(demoDescription);
//        layout.add(desktopDemoFrame);
        
        this.setSizeFull();
	}

	private void loadMobileComponentData() {
		installationHeader.setText("Installing the Mobile Application");
		installationDescription.setText("\nThe installation process for the mobile app is just as quick! The steps are relatively the same for both iPhone and Android devices.\n"
				+ "\niPhone users should use Safari."
				+ "\nAndroid users should use Google Chrome.\n\n");
		mobileInstallationFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/9HWKx4NncvM");
		mobileInstallationFrame.getElement().setAttribute("title", "YouTube video player");
		mobileInstallationFrame.getElement().setAttribute("frameborder", "0");
		mobileInstallationFrame.getElement().setAttribute("allow", "accelerometer");
		mobileInstallationFrame.getElement().setAttribute("autoplay", "true");
		mobileInstallationFrame.getElement().setAttribute("allowfullscreen", "true");
		
		
//		demoHeader.setText("Demo of the Mobile Application");
//		demoDescription.setText("The below video will showcase a full walkthrough of the mobile application's features.");
//		mobileDemoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/9HWKx4NncvM");
//		mobileDemoFrame.getElement().setAttribute("title", "YouTube video player");
//		mobileDemoFrame.getElement().setAttribute("frameborder", "0");
//		mobileDemoFrame.getElement().setAttribute("allow", "accelerometer");
//		mobileDemoFrame.getElement().setAttribute("autoplay", "true");
//		mobileDemoFrame.getElement().setAttribute("allowfullscreen", "true");		
		
		// Clear layout and add components back into it.
		layout.removeAll();
        layout.add(tabs);
        layout.add(installationHeader);
        layout.add(mobileInstallationFrame);
        layout.add(installationDescription);
//        layout.add(demoHeader);
//        layout.add(demoDescription);
//        layout.add(mobileDemoFrame);        
	}

	
	
}

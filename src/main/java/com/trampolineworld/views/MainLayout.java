package com.trampolineworld.views;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.security.AuthenticatedUser;
import com.trampolineworld.views.account.AccountView;
import com.trampolineworld.views.archives.ArchivesView;
import com.trampolineworld.views.auditlog.AuditLogView;
import com.trampolineworld.views.chat.ChatView;
import com.trampolineworld.views.contact.ContactView;
import com.trampolineworld.views.debug.DebugView;
import com.trampolineworld.views.export.ExportView;
import com.trampolineworld.views.manageusers.ManageUsersView;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;
import com.trampolineworld.views.userguide.UserGuideView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import java.util.Optional;
import java.util.Set;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

	/**
	 * A simple navigation item component, based on ListItem element.
	 */
	public static class MenuItemInfo extends ListItem {

		private final Class<? extends Component> view;

		public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
			this.view = view;
			RouterLink link = new RouterLink();
			link.addClassNames("menu-item-link");
			link.setRoute(view);

			Span text = new Span(menuTitle);
			text.addClassNames("menu-item-text");

			link.add(new LineAwesomeIcon(iconClass), text);
			add(link);
		}

		public Class<?> getView() {
			return view;
		}

		/**
		 * Simple wrapper to create icons using LineAwesome iconset. See
		 * https://icons8.com/line-awesome
		 */
		@NpmPackage(value = "line-awesome", version = "1.3.0")
		public static class LineAwesomeIcon extends Span {
			public LineAwesomeIcon(String lineawesomeClassnames) {
				addClassNames("menu-item-icon");
				if (!lineawesomeClassnames.isEmpty()) {
					addClassNames(lineawesomeClassnames);
				}
			}
		}

	}

	private H1 viewTitle;

	private AuthenticatedUser authenticatedUser;
	private AccessAnnotationChecker accessChecker;
	private final UserService userService;
	private final UserRepository userRepository;

	public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker,
			UserService userService, UserRepository userRepository) {
		this.authenticatedUser = authenticatedUser;
		this.accessChecker = accessChecker;
		this.userService = userService;
		this.userRepository = userRepository;

		setPrimarySection(Section.DRAWER);
		// First parameter is touchOptimized. If true, navbar will show at the bottom in
		// mobile views.
		addToNavbar(false, createHeaderContent());
		addToDrawer(createDrawerContent());
	}

	private Component createHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		toggle.addClassNames("view-toggle");
		toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		toggle.getElement().setAttribute("aria-label", "Menu toggle");

		viewTitle = new H1();
		viewTitle.addClassNames("view-title");

		Header header = new Header(toggle, viewTitle);
		header.addClassNames("view-header");
		return header;
	}

	private Component createDrawerContent() {
		H2 appName = new H2("Trampoline World");
		appName.addClassNames("app-name");

		appName.addClickListener(e -> {
			UI.getCurrent().navigate(TrampolineOrdersView.class);
		});

		com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName,
				createNavigation(), createFooter());
		section.addClassNames("drawer-section");
		return section;
	}

	private Nav createNavigation() {
		Nav nav = new Nav();
		nav.addClassNames("menu-item-container");
		nav.getElement().setAttribute("aria-labelledby", "views");

		// Wrap the links in a list; improves accessibility
		UnorderedList list = new UnorderedList();
		list.addClassNames("navigation-list");
		nav.add(list);

		for (MenuItemInfo menuItem : createMenuItems()) {
			if (accessChecker.hasAccess(menuItem.getView())) {
				list.add(menuItem);
			}

		}
		return nav;
	}

	private MenuItemInfo[] createMenuItems() {

		/*
		 * Other icons:
				"la la-clipboard-list"
				"la la-clipboard-list"
				"las la-scroll"
				"las la-users"
				"lar la-save"
				"las la-code"
				"lar la-user"
				"las la-cog"
	 	*/
		return new MenuItemInfo[] { //
				new MenuItemInfo("Trampoline Orders", "las la-clipboard-check", TrampolineOrdersView.class), //
				new MenuItemInfo("Chat Room", "la la-comments", ChatView.class), //
				new MenuItemInfo("Export PDF / CSV", "las la-file-pdf", ExportView.class), //
				new MenuItemInfo("", "", ChatView.class),
				new MenuItemInfo("Account", "lar la-user", AccountView.class), //
				new MenuItemInfo("Manage Users", "las la-users", ManageUsersView.class), //
				new MenuItemInfo("Audit Log", "las la-database", AuditLogView.class), //
				new MenuItemInfo("Archives", "las la-database", ArchivesView.class), //
				new MenuItemInfo("", "", ChatView.class),
				new MenuItemInfo("User Guide", "las la-info-circle", UserGuideView.class), //
				new MenuItemInfo("Debug", "las la-bug", DebugView.class), //
				new MenuItemInfo("Contact", "las la-at", ContactView.class), //
		};
	}
	
	
	private Footer createFooter() {
		Footer layout = new Footer();
		layout.addClassNames("footer");

		Optional<User> maybeUser = authenticatedUser.get();	
		if (maybeUser.isPresent()) {
			User user = maybeUser.get();

			Avatar avatar = new Avatar(user.getDisplayName(), user.getProfilePictureUrl());
			avatar.addClassNames("me-xs");

			ContextMenu userMenu = new ContextMenu(avatar);
			userMenu.setOpenOnClick(true);
			userMenu.addItem("Account", e -> {
				UI.getCurrent().navigate(AccountView.class);
			});
			userMenu.addItem("Logout", e -> {
				authenticatedUser.logout();
			});

			Span name = new Span(user.getDisplayName());
			name.addClassNames("font-medium", "text-s", "text-secondary");

			layout.add(avatar, name);
		} else {
			Anchor loginLink = new Anchor("login", "Sign in");
			layout.add(loginLink);
		}

		return layout;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
		return title == null ? "" : title.value();
	}
}

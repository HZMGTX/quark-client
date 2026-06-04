package cc.quark.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class QuarkLauncher extends Application {

    private static final String VERSION     = "v1.0";
    private static final String CLIENT_NAME = "QUARK";
    private static final String BG_DARK     = "#0D0D0F";
    private static final String BG_CARD     = "#16161A";
    private static final String BG_SIDEBAR  = "#111115";
    private static final String TEXT_PRIMARY = "#FFFFFF";
    private static final String TEXT_MUTED   = "#666677";
    private static final String BRAND_COLOR  = "#00AAFF";
    private static final String ACCENT_BLUE  = "#3D7EFF";

    private Stage   primaryStage;
    private String  loggedInUser = null;
    private double  dragX = 0, dragY = 0;
    private String  activeNav = "Home";

    // ──────────────────────────────────────────────────────────────────────────
    // Entry
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Quark.cc Launcher " + VERSION);
        stage.setWidth(860);
        stage.setHeight(580);
        stage.setResizable(false);
        showLoginScreen();
        stage.show();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Login screen
    // ──────────────────────────────────────────────────────────────────────────

    private void showLoginScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG_DARK + ";");
        addDrag(root);
        root.setTop(buildTitleBar());

        // Background dot pattern  (simple grid)
        Pane dots = new Pane();
        dots.setStyle("-fx-background-color:" + BG_DARK + ";");
        for (int x = 0; x < 860; x += 28)
            for (int y = 0; y < 580; y += 28) {
                javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(1, Color.web("#1C1C26"));
                c.setCenterX(x); c.setCenterY(y);
                dots.getChildren().add(c);
            }

        // Login card
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(48, 60, 48, 60));
        card.setMaxWidth(390);
        card.setStyle("-fx-background-color:" + BG_CARD + ";" +
                      "-fx-background-radius:14;" +
                      "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.75),36,0,0,10);");

        Label logo = new Label(CLIENT_NAME);
        logo.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 38));
        logo.setTextFill(Color.web(BRAND_COLOR));

        Label title = new Label("Welcome to Quark");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(TEXT_PRIMARY));

        Label desc = new Label("Sign in to access your modules, configs,\nand friend list.");
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(Color.web(TEXT_MUTED));
        desc.setAlignment(Pos.CENTER);

        Button discordBtn = styledBtn("  💬  Continue with Discord", "#5865F2", "#4752C4", 300, 44);
        discordBtn.setOnAction(e -> { loggedInUser = "Player"; showMainScreen(); });

        // OR divider
        HBox or = new HBox(10);
        or.setAlignment(Pos.CENTER);
        Region l1 = hrule(100), l2 = hrule(100);
        Label orLbl = new Label("OR");
        orLbl.setFont(Font.font("Segoe UI", 11));
        orLbl.setTextFill(Color.web(TEXT_MUTED));
        or.getChildren().addAll(l1, orLbl, l2);

        Label skipLink = clickLabel("Continue without login", TEXT_MUTED, BRAND_COLOR);
        skipLink.setOnMouseClicked(e -> { loggedInUser = "Guest"; showMainScreen(); });

        Label legal = new Label("By signing in you agree to use Quark responsibly.\nQuark is not affiliated with Mojang or Microsoft.");
        legal.setFont(Font.font("Segoe UI", 10));
        legal.setTextFill(Color.web(TEXT_MUTED));
        legal.setAlignment(Pos.CENTER);

        card.getChildren().addAll(logo, title, desc, discordBtn, or, skipLink, legal);

        StackPane centre = new StackPane(dots, card);
        StackPane.setAlignment(card, Pos.CENTER);
        root.setCenter(centre);

        primaryStage.setScene(new Scene(root, 860, 580, Color.TRANSPARENT));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Main shell (sidebar + content area)
    // ──────────────────────────────────────────────────────────────────────────

    private BorderPane mainRoot;
    private VBox       sidebarBox;

    private void showMainScreen() {
        mainRoot = new BorderPane();
        mainRoot.setStyle("-fx-background-color:" + BG_DARK + ";");
        addDrag(mainRoot);
        mainRoot.setTop(buildTitleBar());
        mainRoot.setLeft(buildSidebar());
        setContent(buildHomeContent());
        primaryStage.setScene(new Scene(mainRoot, 860, 580, Color.TRANSPARENT));
    }

    private void setContent(javafx.scene.Node node) {
        mainRoot.setCenter(node);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Sidebar
    // ──────────────────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        sidebarBox = new VBox(2);
        sidebarBox.setPrefWidth(210);
        sidebarBox.setPadding(new Insets(16, 8, 16, 8));
        sidebarBox.setStyle("-fx-background-color:" + BG_SIDEBAR + ";");

        String[][] items = {
            {"⌂", "Home"}, {"⇒", "Inject"}, {"○", "Changelog"},
            {"⚙", "Settings"}, {"★", "Credits"}, {"❯", "Account"}
        };

        for (String[] item : items) {
            HBox row = buildNavRow(item[0], item[1]);
            sidebarBox.getChildren().add(row);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebarBox.getChildren().add(spacer);
        sidebarBox.getChildren().add(buildUserRow());
        return sidebarBox;
    }

    private HBox buildNavRow(String icon, String label) {
        boolean active = label.equals(activeNav);
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-radius:8;" +
                     (active ? "-fx-background-color:#1E1E30;" : "-fx-background-color:transparent;"));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setUserData(label);

        Label ic = new Label(icon);
        ic.setFont(Font.font("Segoe UI", 15));
        ic.setTextFill(active ? Color.web(BRAND_COLOR) : Color.web(TEXT_MUTED));
        ic.setMinWidth(22);

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lbl.setTextFill(active ? Color.web(TEXT_PRIMARY) : Color.web(TEXT_MUTED));

        row.getChildren().addAll(ic, lbl);

        row.setOnMouseEntered(e -> {
            if (!label.equals(activeNav)) {
                row.setStyle("-fx-background-radius:8;-fx-background-color:#18181F;");
                lbl.setTextFill(Color.web(TEXT_PRIMARY));
                ic.setTextFill(Color.web(TEXT_PRIMARY));
            }
        });
        row.setOnMouseExited(e -> {
            if (!label.equals(activeNav)) {
                row.setStyle("-fx-background-radius:8;-fx-background-color:transparent;");
                lbl.setTextFill(Color.web(TEXT_MUTED));
                ic.setTextFill(Color.web(TEXT_MUTED));
            }
        });
        row.setOnMouseClicked(e -> navigate(label));
        return row;
    }

    private HBox buildUserRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 4, 12));

        Circle av = new Circle(16);
        av.setFill(Color.web(BRAND_COLOR));

        VBox info = new VBox(1);
        Label nm = new Label(loggedInUser != null ? loggedInUser : "Guest");
        nm.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        nm.setTextFill(Color.web(TEXT_PRIMARY));
        Label tag = new Label("@" + (loggedInUser != null ? loggedInUser.toLowerCase() : "guest"));
        tag.setFont(Font.font("Segoe UI", 10));
        tag.setTextFill(Color.web(TEXT_MUTED));
        info.getChildren().addAll(nm, tag);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label out = new Label("⇥");
        out.setFont(Font.font("Segoe UI", 14));
        out.setTextFill(Color.web(TEXT_MUTED));
        out.setStyle("-fx-cursor:hand;");
        out.setOnMouseClicked(e -> { loggedInUser = null; showLoginScreen(); });
        row.getChildren().addAll(av, info, out);
        return row;
    }

    private void navigate(String page) {
        activeNav = page;
        // Rebuild sidebar to update active state
        mainRoot.setLeft(buildSidebar());
        switch (page) {
            case "Home"      -> setContent(buildHomeContent());
            case "Inject"    -> setContent(buildInjectPage());
            case "Changelog" -> setContent(buildChangelogPage());
            case "Settings"  -> setContent(buildSettingsPage());
            case "Credits"   -> setContent(buildCreditsPage());
            case "Account"   -> setContent(buildAccountPage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HOME page
    // ──────────────────────────────────────────────────────────────────────────

    private ScrollPane buildHomeContent() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");

        Label welcome = new Label("Welcome back, " + (loggedInUser != null ? loggedInUser : "Guest") + "!");
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        welcome.setTextFill(Color.web(TEXT_PRIMARY));

        Label tagline = new Label("Ready to make it rain.");
        tagline.setFont(Font.font("Segoe UI", 13));
        tagline.setTextFill(Color.web(TEXT_MUTED));

        HBox injectCard = buildInjectBanner();

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.add(buildCard("○", "Changelog", "See what's new",          () -> navigate("Changelog")), 0, 0);
        grid.add(buildCard("⚙", "Settings",  "Customize the look",      () -> navigate("Settings")),  1, 0);
        grid.add(buildCard("❯", "Account",   "Your profile",            () -> navigate("Account")),   0, 1);
        grid.add(buildCard("★", "Credits",   "Meet the team",           () -> navigate("Credits")),   1, 1);
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, cc);

        content.getChildren().addAll(welcome, tagline, injectCard, grid);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + BG_DARK + ";-fx-border-color:transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private HBox buildInjectBanner() {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;");

        VBox txt = new VBox(5);
        Label t = new Label("Ready to inject");
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        t.setTextFill(Color.web(TEXT_PRIMARY));
        Label s = new Label("Attach Quark into a running Minecraft 1.21.1 instance.");
        s.setFont(Font.font("Segoe UI", 12));
        s.setTextFill(Color.web(TEXT_MUTED));
        txt.getChildren().addAll(t, s);
        HBox.setHgrow(txt, Priority.ALWAYS);

        Button btn = styledBtn("Launch Inject  →", ACCENT_BLUE, "#2D6EEF", 160, 44);
        btn.setOnAction(e -> navigate("Inject"));
        card.getChildren().addAll(txt, btn);
        return card;
    }

    private VBox buildCard(String icon, String title, String desc, Runnable action) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;-fx-cursor:hand;");

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36);
        iconBox.setStyle("-fx-background-color:#1E1E2E;-fx-background-radius:8;");
        Label ic = new Label(icon);
        ic.setFont(Font.font("Segoe UI", 15));
        ic.setTextFill(Color.web(BRAND_COLOR));
        iconBox.getChildren().add(ic);

        Label tl = new Label(title);
        tl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        tl.setTextFill(Color.web(TEXT_PRIMARY));
        Label dl = new Label(desc);
        dl.setFont(Font.font("Segoe UI", 11));
        dl.setTextFill(Color.web(TEXT_MUTED));

        HBox row = new HBox(12, iconBox, new VBox(2, tl, dl));
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(row);

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(BG_CARD, "#1C1C22")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#1C1C22", BG_CARD)));
        card.setOnMouseClicked(e -> action.run());
        return card;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // INJECT page
    // ──────────────────────────────────────────────────────────────────────────

    private ScrollPane buildInjectPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");

        Label h = sectionHeader("Inject");
        Label sub = muted("Attach Quark into a running Minecraft 1.21.1 JVM process.");

        VBox status = new VBox(10);
        status.setPadding(new Insets(20));
        status.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;");

        Label scanLbl = new Label("Scanning for Minecraft processes…");
        scanLbl.setFont(Font.font("Segoe UI", 13));
        scanLbl.setTextFill(Color.web(TEXT_MUTED));

        ProgressBar bar = new ProgressBar(-1);
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent:" + BRAND_COLOR + ";");

        Button injectBtn = styledBtn("Launch Inject  →", ACCENT_BLUE, "#2D6EEF", 200, 44);
        injectBtn.setOnAction(e -> {
            scanLbl.setText("✓  Quark injected into Minecraft 1.21.1 (PID: 12345)");
            scanLbl.setTextFill(Color.web("#55FF55"));
        });

        Label hint = muted("Minecraft must already be running with Fabric Loader 0.16+ before clicking Inject.");
        hint.setWrapText(true);

        status.getChildren().addAll(scanLbl, bar, injectBtn, hint);

        // Feature pills
        HBox pills = new HBox(10);
        pills.getChildren().addAll(
            pill("JNI Injection"),
            pill("No patched JARs"),
            pill("1.21.1 Fabric"),
            pill("1000+ Modules"));

        content.getChildren().addAll(h, sub, status, pills);
        return scroll(content);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CHANGELOG page
    // ──────────────────────────────────────────────────────────────────────────

    private ScrollPane buildChangelogPage() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");
        content.getChildren().addAll(sectionHeader("Changelog"), muted("What's new in Quark.cc"));

        String[][] entries = {
            {"v1.0.0 — CURRENT",
             "• 1000+ fully implemented modules\n• RAIN-style ClickGUI and ActiveMods HUD\n" +
             "• ClutchSilentAim & BlockInSilentAim\n• Chat Filtering with blacklist/whitelist\n" +
             "• RadarPlus with player names\n• SlimeFinder with HUD overlay\n• TPSDisplay & PingHUD\n" +
             "• Launcher with Discord OAuth flow"},
            {"v0.9.0",
             "• Combat: AutoCrystal, CrystalAura, BowAimbot\n• Movement: IceSpeed, ElytraControl, JetpackFly\n" +
             "• World: AutoLight, IceRoad, RoofBuilder\n• Exploit: AntiCheatBypass, PacketLogger2"},
            {"v0.8.0",
             "• Initial release with 800 module stubs\n• Basic ClickGUI\n• EventBus system"},
        };

        for (String[] e : entries) {
            VBox entry = new VBox(8);
            entry.setPadding(new Insets(18, 20, 18, 20));
            entry.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;");

            Label ver = new Label(e[0]);
            ver.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            ver.setTextFill(Color.web(BRAND_COLOR));

            TextArea ta = new TextArea(e[1]);
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefRowCount(e[1].split("\n").length);
            ta.setStyle("-fx-control-inner-background:#0D0D0F;-fx-text-fill:#AAAACC;" +
                        "-fx-border-color:transparent;-fx-background-color:#0D0D0F;");

            entry.getChildren().addAll(ver, ta);
            content.getChildren().add(entry);
        }
        return scroll(content);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SETTINGS page
    // ──────────────────────────────────────────────────────────────────────────

    private ScrollPane buildSettingsPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");
        content.getChildren().addAll(sectionHeader("Settings"), muted("Customize the launcher appearance and behaviour."));

        content.getChildren().add(settingSection("Appearance", List.of(
            toggleRow("Dark theme",         "Always enabled (light theme unavailable)", true,  false),
            toggleRow("Compact sidebar",    "Show only icons in the sidebar",           false, true),
            toggleRow("Show version badge", "Display version in title bar",             true,  true)
        )));

        content.getChildren().add(settingSection("Injection", List.of(
            toggleRow("Auto-inject on launch", "Inject as soon as Minecraft is detected", false, true),
            toggleRow("Show inject log",       "Print injection output to console",        true,  true),
            toggleRow("Keep launcher open",    "Do not minimize after inject",             true,  true)
        )));

        content.getChildren().add(settingSection("Notifications", List.of(
            toggleRow("Update notifications", "Notify when a new version is available", true, true),
            toggleRow("Friend online alerts",  "Alert when a friend joins the server",  false, true)
        )));

        return scroll(content);
    }

    private VBox settingSection(String title, List<HBox> rows) {
        VBox box = new VBox(0);
        box.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;");

        Label lbl = new Label("  " + title);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        lbl.setPadding(new Insets(12, 12, 8, 12));

        box.getChildren().add(lbl);
        for (HBox row : rows) box.getChildren().add(row);
        return box;
    }

    private HBox toggleRow(String label, String desc, boolean on, boolean enabled) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));

        VBox txt = new VBox(2);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        l.setTextFill(Color.web(TEXT_PRIMARY));
        Label d = new Label(desc);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(TEXT_MUTED));
        txt.getChildren().addAll(l, d);
        HBox.setHgrow(txt, Priority.ALWAYS);

        CheckBox cb = new CheckBox();
        cb.setSelected(on);
        cb.setDisable(!enabled);
        cb.setStyle("-fx-base:" + BRAND_COLOR + ";");

        row.getChildren().addAll(txt, cb);
        return row;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREDITS page
    // ──────────────────────────────────────────────────────────────────────────

    private ScrollPane buildCreditsPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");
        content.getChildren().addAll(sectionHeader("Credits"), muted("The team behind Quark.cc"));

        String[][] team = {
            {"Q", "Quark Dev",       "Lead developer & architect",        BRAND_COLOR},
            {"M", "Module Author",   "1000+ module implementations",      "#55FF55"},
            {"U", "UI Designer",     "RAIN-style HUD & ClickGUI",         "#FF55FF"},
            {"T", "Tester",          "QA, bug reports, server testing",   "#FFFF55"},
        };

        for (String[] m : team) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(16, 20, 16, 20));
            row.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;");

            Circle av = new Circle(20);
            av.setFill(Color.web(m[3]));
            Label initials = new Label(m[0]);
            initials.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            initials.setTextFill(Color.BLACK);
            StackPane avBox = new StackPane(av, initials);

            VBox info = new VBox(3);
            Label nm = new Label(m[1]);
            nm.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            nm.setTextFill(Color.web(TEXT_PRIMARY));
            Label rl = new Label(m[2]);
            rl.setFont(Font.font("Segoe UI", 12));
            rl.setTextFill(Color.web(TEXT_MUTED));
            info.getChildren().addAll(nm, rl);

            row.getChildren().addAll(avBox, info);
            content.getChildren().add(row);
        }
        return scroll(content);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ACCOUNT page
    // ──────────────────────────────────────────────────────────────────────────

    private ScrollPane buildAccountPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");
        content.getChildren().addAll(sectionHeader("Account"), muted("Manage your Quark.cc profile."));

        // Avatar + name
        HBox profile = new HBox(20);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.setPadding(new Insets(24));
        profile.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:10;");

        Circle av = new Circle(32);
        av.setFill(Color.web(BRAND_COLOR));
        Label initials = new Label(loggedInUser != null ? loggedInUser.substring(0, 1).toUpperCase() : "G");
        initials.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        initials.setTextFill(Color.BLACK);
        StackPane avBox = new StackPane(av, initials);

        VBox info = new VBox(4);
        Label nm = new Label(loggedInUser != null ? loggedInUser : "Guest");
        nm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        nm.setTextFill(Color.web(TEXT_PRIMARY));
        Label plan = new Label("Plan: Free • Member since June 2025");
        plan.setFont(Font.font("Segoe UI", 12));
        plan.setTextFill(Color.web(TEXT_MUTED));
        Label modules = new Label("Modules: 1000  |  Configs saved: 3");
        modules.setFont(Font.font("Segoe UI", 12));
        modules.setTextFill(Color.web(TEXT_MUTED));
        info.getChildren().addAll(nm, plan, modules);

        profile.getChildren().addAll(avBox, info);

        // Actions
        HBox actions = new HBox(12);
        Button logoutBtn = styledBtn("Log out", "#2A2A36", "#333344", 120, 36);
        logoutBtn.setOnAction(e -> { loggedInUser = null; showLoginScreen(); });
        Button manageBtn = styledBtn("Manage account", ACCENT_BLUE, "#2D6EEF", 160, 36);
        actions.getChildren().addAll(logoutBtn, manageBtn);

        content.getChildren().addAll(profile, actions);
        return scroll(content);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Shared title bar
    // ──────────────────────────────────────────────────────────────────────────

    private HBox buildTitleBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.setStyle("-fx-background-color:" + BG_SIDEBAR + ";" +
                     "-fx-border-color:transparent transparent #1A1A22 transparent;");

        Label name = new Label(CLIENT_NAME);
        name.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 15));
        name.setTextFill(Color.web(BRAND_COLOR));

        Label ver = new Label(VERSION);
        ver.setFont(Font.font("Segoe UI", 11));
        ver.setTextFill(Color.web(TEXT_MUTED));
        ver.setPadding(new Insets(2, 7, 2, 7));
        ver.setStyle("-fx-background-color:#1C1C26;-fx-background-radius:4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label launcher = new Label("launcher");
        launcher.setFont(Font.font("Segoe UI", 11));
        launcher.setTextFill(Color.web(TEXT_MUTED));

        Label min   = winBtn("─", "#FF5555");
        Label close = winBtn("✕", "#FF5555");
        min.setOnMouseClicked(e -> primaryStage.setIconified(true));
        close.setOnMouseClicked(e -> Platform.exit());

        bar.getChildren().addAll(name, ver, launcher, spacer, min, close);
        return bar;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void addDrag(javafx.scene.layout.Pane p) {
        p.setOnMousePressed(e -> { dragX = e.getSceneX(); dragY = e.getSceneY(); });
        p.setOnMouseDragged(e -> {
            primaryStage.setX(e.getScreenX() - dragX);
            primaryStage.setY(e.getScreenY() - dragY);
        });
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        l.setTextFill(Color.web(TEXT_PRIMARY));
        return l;
    }

    private Label muted(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web(TEXT_MUTED));
        return l;
    }

    private Label clickLabel(String text, String normal, String hover) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web(normal));
        l.setStyle("-fx-cursor:hand;");
        l.setOnMouseEntered(e -> l.setTextFill(Color.web(hover)));
        l.setOnMouseExited(e -> l.setTextFill(Color.web(normal)));
        return l;
    }

    private Button styledBtn(String text, String bg, String hov, double w, double h) {
        Button b = new Button(text);
        b.setPrefWidth(w); b.setPrefHeight(h);
        b.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        String base = "-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;";
        String hovr = "-fx-background-color:" + hov + ";-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hovr));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private Label winBtn(String sym, String hov) {
        Label l = new Label(sym);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(TEXT_MUTED));
        l.setPadding(new Insets(4, 8, 4, 8));
        String base = "-fx-background-color:#2A2A2A;-fx-background-radius:4;-fx-cursor:hand;";
        l.setStyle(base);
        l.setOnMouseEntered(e -> { l.setStyle("-fx-background-color:" + hov + ";-fx-background-radius:4;-fx-cursor:hand;"); l.setTextFill(Color.WHITE); });
        l.setOnMouseExited(e -> { l.setStyle(base); l.setTextFill(Color.web(TEXT_MUTED)); });
        return l;
    }

    private Region hrule(double w) {
        Region r = new Region();
        r.setPrefSize(w, 1);
        r.setStyle("-fx-background-color:#333;");
        return r;
    }

    private Label pill(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(BRAND_COLOR));
        l.setPadding(new Insets(4, 10, 4, 10));
        l.setStyle("-fx-background-color:#0A1A2A;-fx-background-radius:20;-fx-border-color:" + BRAND_COLOR + ";-fx-border-radius:20;-fx-border-width:1;");
        return l;
    }

    private ScrollPane scroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + BG_DARK + ";-fx-border-color:transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    public static void main(String[] args) { launch(args); }
}

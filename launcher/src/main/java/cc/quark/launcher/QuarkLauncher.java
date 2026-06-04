package cc.quark.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class QuarkLauncher extends Application {

    private static final String VERSION    = "v1.0";
    private static final String CLIENT_NAME = "QUARK";
    private static final String BRAND_COLOR = "#00AAFF";
    private static final String BG_DARK     = "#0D0D0F";
    private static final String BG_CARD     = "#16161A";
    private static final String BG_SIDEBAR  = "#111115";
    private static final String TEXT_PRIMARY = "#FFFFFF";
    private static final String TEXT_MUTED   = "#666677";
    private static final String ACCENT_BLUE  = "#3D7EFF";

    private Stage   primaryStage;
    private String  loggedInUser  = null;
    private double  dragOffsetX   = 0;
    private double  dragOffsetY   = 0;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Quark.cc Launcher");
        stage.setWidth(820);
        stage.setHeight(560);
        stage.setResizable(false);

        showLoginScreen();
        stage.show();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Login screen
    // ──────────────────────────────────────────────────────────────────────────

    private void showLoginScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        addDragHandlers(root);

        // Close button top-right
        HBox titleBar = buildTitleBar(false);
        root.setTop(titleBar);

        // Centre card
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(48, 56, 48, 56));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                      "-fx-background-radius: 14;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 30, 0, 0, 8);");

        // Logo / title
        Label logo = new Label(CLIENT_NAME);
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        logo.setTextFill(Color.web(BRAND_COLOR));

        Label subtitle = new Label("Welcome to Quark");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        subtitle.setTextFill(Color.web(TEXT_PRIMARY));

        Label desc = new Label("Sign in to access your modules, configs, and friend list.");
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(Color.web(TEXT_MUTED));
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);
        desc.setMaxWidth(280);

        // Discord button
        Button discordBtn = new Button("  Continue with Discord");
        discordBtn.setPrefWidth(280);
        discordBtn.setPrefHeight(44);
        discordBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        discordBtn.setStyle("-fx-background-color: #5865F2;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;");
        discordBtn.setOnAction(e -> {
            loggedInUser = "Player";
            showHomeScreen();
        });
        discordBtn.setOnMouseEntered(e ->
            discordBtn.setStyle("-fx-background-color: #4752C4;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"));
        discordBtn.setOnMouseExited(e ->
            discordBtn.setStyle("-fx-background-color: #5865F2;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"));

        // Divider "OR"
        HBox divider = new HBox(10);
        divider.setAlignment(Pos.CENTER);
        Region line1 = new Region(); line1.setPrefHeight(1); line1.setPrefWidth(90);
        line1.setStyle("-fx-background-color: #333;");
        Region line2 = new Region(); line2.setPrefHeight(1); line2.setPrefWidth(90);
        line2.setStyle("-fx-background-color: #333;");
        Label orLabel = new Label("OR");
        orLabel.setFont(Font.font("Segoe UI", 11));
        orLabel.setTextFill(Color.web(TEXT_MUTED));
        divider.getChildren().addAll(line1, orLabel, line2);

        // Skip login link
        Label skipLink = new Label("Continue without login");
        skipLink.setFont(Font.font("Segoe UI", 12));
        skipLink.setTextFill(Color.web(TEXT_MUTED));
        skipLink.setStyle("-fx-cursor: hand;");
        skipLink.setOnMouseEntered(e -> skipLink.setTextFill(Color.web(BRAND_COLOR)));
        skipLink.setOnMouseExited(e -> skipLink.setTextFill(Color.web(TEXT_MUTED)));
        skipLink.setOnMouseClicked(e -> {
            loggedInUser = "Guest";
            showHomeScreen();
        });

        Label legal = new Label("By signing in you agree to use Quark responsibly.\nQuark is provided as-is and is not affiliated with Mojang or Microsoft.");
        legal.setFont(Font.font("Segoe UI", 10));
        legal.setTextFill(Color.web(TEXT_MUTED));
        legal.setWrapText(true);
        legal.setAlignment(Pos.CENTER);
        legal.setMaxWidth(280);

        card.getChildren().addAll(logo, subtitle, desc, discordBtn, divider, skipLink, legal);

        StackPane centre = new StackPane(card);
        centre.setStyle("-fx-background-color: " + BG_DARK + ";");
        StackPane.setAlignment(card, Pos.CENTER);
        root.setCenter(centre);

        Scene scene = new Scene(root, 820, 560);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Home screen
    // ──────────────────────────────────────────────────────────────────────────

    private void showHomeScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        addDragHandlers(root);

        // Title bar
        root.setTop(buildTitleBar(true));

        // Sidebar
        root.setLeft(buildSidebar());

        // Main content
        root.setCenter(buildHomeContent());

        Scene scene = new Scene(root, 820, 560);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: " + BG_SIDEBAR + ";");
        sidebar.setPadding(new Insets(16, 8, 16, 8));

        String[] navLabels = {"Home", "Inject", "Changelog", "Settings", "Credits", "Account"};
        String[] icons     = {"⌂", "⇒", "○", "⚙", "★", "❯"};

        for (int i = 0; i < navLabels.length; i++) {
            final String label = navLabels[i];
            final String icon  = icons[i];
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));
            row.setStyle("-fx-background-radius: 8;" +
                         (label.equals("Home")
                            ? "-fx-background-color: #1E1E2E;"
                            : "-fx-background-color: transparent;"));
            row.setMaxWidth(Double.MAX_VALUE);

            Label iconLbl = new Label(icon);
            iconLbl.setFont(Font.font("Segoe UI", 14));
            iconLbl.setTextFill(label.equals("Home") ? Color.web(BRAND_COLOR) : Color.web(TEXT_MUTED));
            iconLbl.setMinWidth(20);

            Label nameLbl = new Label(label);
            nameLbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            nameLbl.setTextFill(label.equals("Home") ? Color.web(TEXT_PRIMARY) : Color.web(TEXT_MUTED));

            row.getChildren().addAll(iconLbl, nameLbl);

            row.setOnMouseEntered(e -> {
                if (!label.equals("Home")) {
                    row.setStyle("-fx-background-radius: 8; -fx-background-color: #1A1A22;");
                    nameLbl.setTextFill(Color.web(TEXT_PRIMARY));
                    iconLbl.setTextFill(Color.web(TEXT_PRIMARY));
                }
            });
            row.setOnMouseExited(e -> {
                if (!label.equals("Home")) {
                    row.setStyle("-fx-background-radius: 8; -fx-background-color: transparent;");
                    nameLbl.setTextFill(Color.web(TEXT_MUTED));
                    iconLbl.setTextFill(Color.web(TEXT_MUTED));
                }
            });
            row.setOnMouseClicked(e -> handleNav(label));

            sidebar.getChildren().add(row);
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Bottom user row
        HBox userRow = buildUserRow();
        sidebar.getChildren().add(userRow);

        return sidebar;
    }

    private HBox buildUserRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));

        Circle avatar = new Circle(16);
        avatar.setFill(Color.web(BRAND_COLOR));

        VBox nameBox = new VBox(1);
        Label nameLbl = new Label(loggedInUser != null ? loggedInUser : "Guest");
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        nameLbl.setTextFill(Color.web(TEXT_PRIMARY));
        Label tagLbl = new Label(loggedInUser != null ? "@" + loggedInUser.toLowerCase() : "");
        tagLbl.setFont(Font.font("Segoe UI", 10));
        tagLbl.setTextFill(Color.web(TEXT_MUTED));
        nameBox.getChildren().addAll(nameLbl, tagLbl);

        Label logout = new Label("⇥");
        logout.setFont(Font.font("Segoe UI", 14));
        logout.setTextFill(Color.web(TEXT_MUTED));
        logout.setStyle("-fx-cursor: hand;");
        logout.setOnMouseClicked(e -> {
            loggedInUser = null;
            showLoginScreen();
        });
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        row.getChildren().addAll(avatar, nameBox, logout);
        return row;
    }

    private ScrollPane buildHomeContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 32, 32, 32));
        content.setStyle("-fx-background-color: " + BG_DARK + ";");

        // Welcome header
        Label welcome = new Label("Welcome back, " + (loggedInUser != null ? loggedInUser : "Guest") + "!");
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        welcome.setTextFill(Color.web(TEXT_PRIMARY));

        Label tagline = new Label("Ready to make it rain.");
        tagline.setFont(Font.font("Segoe UI", 13));
        tagline.setTextFill(Color.web(TEXT_MUTED));

        // Inject card
        HBox injectCard = buildInjectCard();

        // 2×2 grid of feature cards
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(buildFeatureCard("○",  "Changelog", "See what's new"),      0, 0);
        grid.add(buildFeatureCard("⚙",  "Settings",  "Customize the look"),  1, 0);
        grid.add(buildFeatureCard("❯",  "Account",   "Your profile"),        0, 1);
        grid.add(buildFeatureCard("★",  "Credits",   "Meet the team"),       1, 1);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, new ColumnConstraints());
        grid.getColumnConstraints().get(1).setPercentWidth(50);

        content.getChildren().addAll(welcome, tagline, injectCard, grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG_DARK + "; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private HBox buildInjectCard() {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                      "-fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");

        VBox textBox = new VBox(6);
        Label title = new Label("Ready to inject");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(TEXT_PRIMARY));

        Label sub = new Label("Attach Quark into a running Minecraft 1.21.1 instance.");
        sub.setFont(Font.font("Segoe UI", 12));
        sub.setTextFill(Color.web(TEXT_MUTED));

        textBox.getChildren().addAll(title, sub);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button injectBtn = new Button("Launch Inject  →");
        injectBtn.setPrefHeight(44);
        injectBtn.setPrefWidth(160);
        injectBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        injectBtn.setStyle("-fx-background-color: " + ACCENT_BLUE + ";" +
                           "-fx-text-fill: white;" +
                           "-fx-background-radius: 8;" +
                           "-fx-cursor: hand;");
        injectBtn.setOnMouseEntered(e ->
            injectBtn.setStyle("-fx-background-color: #2D6EEF;" +
                               "-fx-text-fill: white;" +
                               "-fx-background-radius: 8;" +
                               "-fx-cursor: hand;"));
        injectBtn.setOnMouseExited(e ->
            injectBtn.setStyle("-fx-background-color: " + ACCENT_BLUE + ";" +
                               "-fx-text-fill: white;" +
                               "-fx-background-radius: 8;" +
                               "-fx-cursor: hand;"));
        injectBtn.setOnAction(e -> handleInject());

        card.getChildren().addAll(textBox, injectBtn);
        return card;
    }

    private VBox buildFeatureCard(String icon, String title, String desc) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 20, 20, 20));
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                      "-fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);" +
                      "-fx-cursor: hand;");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Icon background
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(36, 36);
        iconBox.setStyle("-fx-background-color: #1E1E2E; -fx-background-radius: 8;");
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("Segoe UI", 16));
        iconLbl.setTextFill(Color.web(BRAND_COLOR));
        iconBox.getChildren().add(iconLbl);

        VBox textBox = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        titleLbl.setTextFill(Color.web(TEXT_PRIMARY));

        Label descLbl = new Label(desc);
        descLbl.setFont(Font.font("Segoe UI", 11));
        descLbl.setTextFill(Color.web(TEXT_MUTED));

        textBox.getChildren().addAll(titleLbl, descLbl);
        row.getChildren().addAll(iconBox, textBox);
        card.getChildren().add(row);

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(BG_CARD, "#1C1C22")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#1C1C22", BG_CARD)));

        return card;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Shared components
    // ──────────────────────────────────────────────────────────────────────────

    private HBox buildTitleBar(boolean showHome) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.setStyle("-fx-background-color: " + BG_SIDEBAR + ";" +
                     "-fx-border-color: transparent transparent #1A1A22 transparent;");

        Label clientName = new Label(CLIENT_NAME);
        clientName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        clientName.setTextFill(Color.web(BRAND_COLOR));

        Label versionBadge = new Label(VERSION);
        versionBadge.setFont(Font.font("Segoe UI", 11));
        versionBadge.setTextFill(Color.web(TEXT_MUTED));
        versionBadge.setPadding(new Insets(2, 8, 2, 8));
        versionBadge.setStyle("-fx-background-color: #1C1C26; -fx-background-radius: 4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Window controls
        Label minBtn  = windowBtn("─", "#333", "#555");
        Label maxBtn  = windowBtn("□", "#333", "#555");
        Label closeBtn = windowBtn("✕", "#333", "#FF5555");

        minBtn.setOnMouseClicked(e -> primaryStage.setIconified(true));
        maxBtn.setOnMouseClicked(e -> {
            if (primaryStage.isMaximized()) primaryStage.setMaximized(false);
            else primaryStage.setMaximized(true);
        });
        closeBtn.setOnMouseClicked(e -> Platform.exit());

        bar.getChildren().addAll(clientName, versionBadge, spacer, minBtn, maxBtn, closeBtn);
        return bar;
    }

    private Label windowBtn(String symbol, String bg, String hoverBg) {
        Label lbl = new Label(symbol);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        lbl.setPadding(new Insets(4, 8, 4, 8));
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 4; -fx-cursor: hand;");
        lbl.setOnMouseEntered(e -> {
            lbl.setStyle("-fx-background-color: " + hoverBg + "; -fx-background-radius: 4; -fx-cursor: hand;");
            lbl.setTextFill(Color.WHITE);
        });
        lbl.setOnMouseExited(e -> {
            lbl.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 4; -fx-cursor: hand;");
            lbl.setTextFill(Color.web(TEXT_MUTED));
        });
        return lbl;
    }

    private void addDragHandlers(Pane root) {
        root.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            primaryStage.setX(e.getScreenX() - dragOffsetX);
            primaryStage.setY(e.getScreenY() - dragOffsetY);
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Actions
    // ──────────────────────────────────────────────────────────────────────────

    private void handleInject() {
        // Attempt to find a running Minecraft 1.21.1 process and open the mod GUI
        System.out.println("[QuarkLauncher] Inject triggered — looking for Minecraft process...");
    }

    private void handleNav(String label) {
        System.out.println("[QuarkLauncher] Nav: " + label);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

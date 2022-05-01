package com.example.messengerclient;

import com.example.messengerclient.controller.LoginFxController;
import com.example.messengerclient.controller.MainFxController;
import com.example.messengerclient.controller.DataResolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientApplicationInitializer extends Application {

    private ConfigurableApplicationContext applicationContext;
    private FxWeaver fxWeaver;
    private Rectangle2D screenBounds;
    private Stage primaryStage;

    /**
     *  this class loads into context only after initialize itself
     *  so if you get this class from context spring re-creates it with null fields
     *  because init() method not invoked in this case
     * */
    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                .sources(SpringBootClientApplication.class)
                .run(args);
        this.fxWeaver = applicationContext.getBean(FxWeaver.class);
        this.screenBounds = Screen.getPrimary().getBounds();
        MainFxController.setClientApplicationInitializer(this);
        LoginFxController.setClientApplicationInitializer(this);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        loadLoginScene(primaryStage);
        primaryStage.show();
    }

    private void loadLoginScene(Stage stage) {

        Parent root = fxWeaver.loadView(LoginFxController.class);
        Scene scene = new Scene(root, screenBounds.getWidth() / 3, screenBounds.getHeight() / 2);
        stage.setScene(scene);
        stage.setMaxWidth(screenBounds.getWidth() / 3);
        stage.setMaxHeight(screenBounds.getHeight() / 2);
        stage.setMinWidth(screenBounds.getWidth() / 5);
        stage.setMinHeight(screenBounds.getHeight() / 3);
    }

    public void afterLogin() {
        loadMainScene(primaryStage);
    }

    public void logout() {
        loadLoginScene(primaryStage);
    }

    public void setTitle(String title) {
        primaryStage.setTitle(title);
    }

    private void loadMainScene(Stage stage) {

        Parent root = fxWeaver.loadView(MainFxController.class);
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        stage.setScene(scene);
//        stage.setMinWidth(screenBounds.getWidth());
//        stage.setMinHeight(screenBounds.getHeight());
        stage.setMaximized(true);
    }

    @Override
    public void stop() {
        Platform.exit();
    }

}
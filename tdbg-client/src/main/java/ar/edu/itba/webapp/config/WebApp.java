package ar.edu.itba.webapp.config;

import ar.edu.itba.executor.QueryExecutor;
import ar.edu.itba.webapp.controller.QueryController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;

public class WebApp {

    private final static String STATIC_FILES_PATH = "/public";
    private final int webAppPort;

    public WebApp(int webAppPort) {
        this.webAppPort = webAppPort;
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles(STATIC_FILES_PATH);
            config.enableCorsForAllOrigins();
        }).start(webAppPort);

        setJsonMapper();
        addRoutes(app);
    }

    private void setJsonMapper() {
        Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);
    }

    private void addRoutes(Javalin app) {
        app.post("/query", queryController()::execute);
    }

    private QueryController queryController() {
        QueryExecutor queryExecutor = new QueryExecutor();
        return new QueryController(queryExecutor);
    }
}

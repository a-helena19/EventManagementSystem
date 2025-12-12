package everoutproject.Event.application.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionCleanup implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public SessionCleanup(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Reihenfolge beachten: zuerst Attributes, dann Sessions
        jdbc.update("DELETE FROM SPRING_SESSION_ATTRIBUTES");
        jdbc.update("DELETE FROM SPRING_SESSION");
        System.out.println("All sessions cleared on startup.");
    }
}

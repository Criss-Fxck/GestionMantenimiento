package mantenimiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"infrastructure", "service"})
@EntityScan(basePackages = "domain.model")
@EnableJpaRepositories(basePackages = "infrastructure.persistence.repository")
@EnableScheduling
public class main {
    public static void main(String[] args) {
        SpringApplication.run(main.class, args);
    }
}


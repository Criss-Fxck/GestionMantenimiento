package mantenimiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"infrastructure", "service"})
public class main {
    public static void main(String[] args) {
        SpringApplication.run(main.class, args);
    }
}

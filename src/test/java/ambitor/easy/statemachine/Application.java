package ambitor.easy.statemachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by Ambitor on 2019/3/20
 */
@SpringBootApplication(scanBasePackages = {"ambitor"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}

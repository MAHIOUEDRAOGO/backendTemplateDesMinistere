package bf.gov.mtdpce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MtdpceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtdpceApplication.class, args);
    }
}

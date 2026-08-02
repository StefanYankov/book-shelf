package bg.softuni.bookshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BookShelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookShelfApplication.class, args);
    }

}

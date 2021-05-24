package agh.sr.REST.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfiguration {
    @Bean
    public DBManager createManager(){ return new DBManager(); }
}

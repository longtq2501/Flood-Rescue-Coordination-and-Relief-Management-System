package com.floodrescue.report.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@Profile("test")
public class E2ETestDataSourceConfig {

    @Bean(name = "authDataSource")
    @ConfigurationProperties("spring.datasource.auth")
    public DataSource authDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "requestDataSource")
    @ConfigurationProperties("spring.datasource.request")
    public DataSource requestDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dispatchDataSource")
    @ConfigurationProperties("spring.datasource.dispatch")
    public DataSource dispatchDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "resourceDataSource")
    @ConfigurationProperties("spring.datasource.resource")
    public DataSource resourceDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

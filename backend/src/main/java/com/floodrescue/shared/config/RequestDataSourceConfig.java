package com.floodrescue.shared.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.floodrescue.module.rescue_request.repository",
        entityManagerFactoryRef = "requestEntityManagerFactory",
        transactionManagerRef = "requestTransactionManager"
)
public class RequestDataSourceConfig {

    @Bean(name = "requestDataSource")
    @ConfigurationProperties("spring.datasource.request")
    public DataSource dataSource() { return DataSourceBuilder.create().build(); }

    @Bean(name = "requestEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("requestDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds)
                .packages("com.floodrescue.module.rescue_request.domain")
                .build();
    }

    @Bean(name = "requestTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("requestEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "requestFlyway")
    public Flyway flyway(@Qualifier("requestDataSource") DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/request")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}

package com.floodrescue.shared.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.floodrescue.module.auth.repository",
        entityManagerFactoryRef = "authEntityManagerFactory",
        transactionManagerRef = "authTransactionManager"
)
public class AuthDataSourceConfig {

    @Primary
    @Bean(name = "authDataSource")
    @ConfigurationProperties("spring.datasource.auth")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "authEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("authDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds)
                .packages("com.floodrescue.module.auth.domain.entity")
                .build();
    }

    @Primary
    @Bean(name = "authTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("authEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "authFlyway")
    public Flyway flyway(@Qualifier("authDataSource") DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/auth")  // hoặc V1__create_auth_schema.sql
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}

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
        basePackages = "com.floodrescue.module.notification.repository",
        entityManagerFactoryRef = "notificationEntityManagerFactory",
        transactionManagerRef = "notificationTransactionManager"
)
public class NotificationDataSourceConfig {

    @Bean(name = "notificationDataSource")
    @ConfigurationProperties("spring.datasource.notification")
    public DataSource dataSource() { return DataSourceBuilder.create().build(); }

    @Bean(name = "notificationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("notificationDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds)
                .packages("com.floodrescue.module.notification.domain")
                .build();
    }

    @Bean(name = "notificationTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("notificationEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "notificationFlyway")
    public Flyway flyway(@Qualifier("notificationDataSource") DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/notification")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}

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
        basePackages = "com.floodrescue.module.dispatch.repository",
        entityManagerFactoryRef = "dispatchEntityManagerFactory",
        transactionManagerRef = "dispatchTransactionManager"
)
public class DispatchDataSourceConfig {

    @Bean(name = "dispatchDataSource")
    @ConfigurationProperties("spring.datasource.dispatch")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dispatchEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("dispatchDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds)
                .packages("com.floodrescue.module.dispatch.domain")
                .build();
    }

    @Bean(name = "dispatchTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("dispatchEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "dispatchFlyway")
    public Flyway flyway(@Qualifier("dispatchDataSource") DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/dispatch")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}

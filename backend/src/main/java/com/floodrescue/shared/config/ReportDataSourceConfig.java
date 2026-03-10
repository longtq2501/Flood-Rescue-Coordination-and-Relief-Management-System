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
        basePackages = "com.floodrescue.module.report.repository",
        entityManagerFactoryRef = "reportEntityManagerFactory",
        transactionManagerRef = "reportTransactionManager"
)
public class ReportDataSourceConfig {

    @Bean(name = "reportDataSource")
    @ConfigurationProperties("spring.datasource.report")
    public DataSource dataSource() { return DataSourceBuilder.create().build(); }

    @Bean(name = "reportEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("reportDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds)
                .packages("com.floodrescue.module.report.domain")
                .build();
    }

    @Bean(name = "reportTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("reportEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "reportFlyway")
    public Flyway flyway(@Qualifier("reportDataSource") DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/report")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}

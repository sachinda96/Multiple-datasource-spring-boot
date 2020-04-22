package com.example;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.entity",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager")
public class MultipleDatabaseSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultipleDatabaseSpringBootApplication.class, args);
	}


	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean
	entityManagerFactory(
			EntityManagerFactoryBuilder builder,
			@Qualifier("dataSource") DataSource dataSource
	) {
        LocalContainerEntityManagerFactoryBean em = builder
				.dataSource(dataSource)
				.packages("com.example.entity")
				.persistenceUnit("db2")
				.build();

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        em.setJpaPropertyMap(properties);

        return em;
	}

	@Primary
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory
					entityManagerFactory
	) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean(name = "db1")
	@ConfigurationProperties(prefix = "spring.second-db")
	public DataSource dataSourceClaim() {
		return DataSourceBuilder.create().build();
	}


	@Bean(name = "secondarydb")
	public JdbcTemplate jdbcTemplateClaim(@Qualifier("db1") DataSource ds) {
		return new JdbcTemplate(ds);
	}






}

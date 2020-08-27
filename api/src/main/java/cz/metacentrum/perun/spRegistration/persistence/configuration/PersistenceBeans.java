package cz.metacentrum.perun.spRegistration.persistence.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.metacentrum.perun.spRegistration.common.configs.JdbcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class PersistenceBeans {

    @Autowired
    @Bean
    public DataSource dataSource(JdbcProperties jdbcProperties) {
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(jdbcProperties.getUrl());
        configuration.setUsername(jdbcProperties.getUsername());
        configuration.setPassword(jdbcProperties.getPassword());
        configuration.setDriverClassName(jdbcProperties.getDriver());
        configuration.setMaximumPoolSize(jdbcProperties.getMaxPoolSize());

        return new HikariDataSource(configuration);
    }

    @Bean
    @Autowired
    public NamedParameterJdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @Autowired
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}

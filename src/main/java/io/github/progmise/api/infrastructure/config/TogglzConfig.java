package io.github.progmise.api.infrastructure.config;

import io.github.progmise.api.domain.FeatureToggle;
import io.github.progmise.commons.infrastructure.FeatureToggleStateRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.GradualActivationStrategy;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;

import javax.sql.DataSource;

// Feature toggles persisted in the API's own datasource (FEATURE_TOGGLE
// table). Auto-configuration so @ConditionalOnBean(DataSource) evaluates
// AFTER the datasource auto-config — remove spring-data-jpa + driver and
// this config silently drops out.
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
public class TogglzConfig {

    @Bean
    public StateRepository stateRepository(DataSource dataSource) {
        return new FeatureToggleStateRepository(
            JDBCStateRepository.newBuilder(dataSource)
                .tableName("FEATURE_TOGGLE")
                .createTable(true)
                .serializer(DefaultMapSerializer.singleline())
                .noCommit(true)
                .build()
        );
    }

    @Bean
    public FeatureManager featureManager(
        StateRepository stateRepository,
        FeatureProvider featureProvider,
        UserProvider userProvider,
        ActivationStrategyProvider activationStrategyProvider
    ) {
        FeatureManager manager = FeatureManagerBuilder
            .begin()
            .stateRepository(stateRepository)
            .featureProvider(featureProvider)
            .userProvider(userProvider)
            .activationStrategyProvider(activationStrategyProvider)
            .build();

        ((FeatureToggleStateRepository) stateRepository).setFeatureManager(manager);

        return manager;
    }

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(FeatureToggle.class);
    }

    @Bean
    public UserProvider userProvider() {
        return () -> null;
    }

    @Bean
    public ActivationStrategyProvider activationStrategyProvider() {
        return () -> java.util.List.of(new GradualActivationStrategy());
    }
}

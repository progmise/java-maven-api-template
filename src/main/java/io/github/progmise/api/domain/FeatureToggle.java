package io.github.progmise.api.domain;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

public enum FeatureToggle implements Feature {

    @Label("Enable Redis cache lookups")
    CACHE_ON;
}

package sanity.nil.security;

import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;
import java.util.UUID;

@ConfigMapping(prefix = "application.security")
@ConfigRoot(
        phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED
)
public interface SecurityConfiguration {

    @ConfigDocDefault("True if identityProvider checks should block secured usecases execution, false if not (local)")
    @WithDefault("true")
    Optional<Boolean> enabled();
    @ConfigDocDefault("UserID for mocked user identity for local env")
    @WithName("default-user-id")
    Optional<UUID> defaultUserID();
}

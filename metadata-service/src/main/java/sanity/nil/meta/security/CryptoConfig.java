package sanity.nil.meta.security;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "application.crypto")
@ConfigRoot(
        phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED
)
public interface CryptoConfig {

    @WithName("iv")
    String aesIV();

    @WithName("keystore")
    KeyStoreConfig keyStore();

    @ConfigGroup
    interface KeyStoreConfig {

        String path();

        String password();

        Key key();

        interface Key {

            String alias();

            String password();
        }
    }
}

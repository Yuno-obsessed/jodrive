package sanity.nil.metadata.security;

import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.component.TestConfigProperty;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sanity.nil.meta.security.LinkEncoder;

@JBossLog
@QuarkusComponentTest
@TestConfigProperty.TestConfigProperties({
        @TestConfigProperty(key = "application.crypto.keystore.path", value = "keystore_crypto.ks"),
        @TestConfigProperty(key = "application.crypto.keystore.password", value = "wordpass"),
        @TestConfigProperty(key = "application.crypto.keystore.key.alias", value = "super_secret_28_key"),
        @TestConfigProperty(key = "application.crypto.keystore.key.password", value = "worpass_secret"),
        @TestConfigProperty(key = "application.crypto.iv", value = "some_value")
})
public class LinkEncoderTest {

    @Inject
    LinkEncoder linkEncoder;

    @Test
    public void given_Link_Cipher_And_Tampered_Link_Cipher_When_Deciphered_Then_Are_Different() throws Exception{
        String link = "linkExample";
        var cipher = linkEncoder.encrypt(link);
        log.info(cipher);
        var linkDecipher = linkEncoder.decrypt(cipher);

        String linkTampered = "linkEzample";
        var cipherTampered = linkEncoder.encrypt(linkTampered);
        log.info(cipherTampered);
        var linkDecipherTampered = linkEncoder.decrypt(cipherTampered);

        Assertions.assertThat(link).isEqualTo(linkDecipher);
        Assertions.assertThat(linkDecipher).isNotEqualTo(linkDecipherTampered);
    }
}

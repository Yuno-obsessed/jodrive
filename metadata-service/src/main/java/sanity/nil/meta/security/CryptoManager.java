package sanity.nil.meta.security;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.security.KeyStore;

public class CryptoManager {

    private String keyStorePath;

    public CryptoManager(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public SecretKey retrieveSecretKey(String keyStorePassword, String keyAlias, String keyPassword) {
        try {
            KeyStore keyStore = createKeyStore(keyStorePath, keyStorePassword);
            KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(keyPassword.toCharArray());

            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, passwordProtection)).getSecretKey();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    private KeyStore createKeyStore(String keyStorePath, String keyStorePassword) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(keyStorePath);
            if (inputStream == null) {
                throw new IllegalStateException("Keystore at path " + keyStorePath + " not found");
            }

            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            if (inputStream.available() > 0) {
                keyStore.load(inputStream, keyStorePassword.toCharArray());
            } else {
                throw new SecurityException("Keystore not found");
            }
            return keyStore;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}

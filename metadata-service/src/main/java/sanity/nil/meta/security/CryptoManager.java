package sanity.nil.meta.security;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            URL keyStoreURL = getClass().getClassLoader().getResource(keyStorePath);
            if (keyStoreURL == null) {
                throw new IllegalStateException("Keystore at path " + keyStorePath + " not found");
            }
            Path resourcePath = Paths.get(keyStoreURL.toURI());
            var keyStoreFile = resourcePath.toFile();

            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            if (keyStoreFile.exists()) {
                keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
            } else {
                throw new SecurityException("Keystore not found");
            }
            return keyStore;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}

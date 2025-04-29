package sanity.nil.meta.security;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import sanity.nil.meta.exceptions.CryptoException;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@JBossLog
@ApplicationScoped
@Startup
public class LinkEncoder {

    private SecretKey secretKey;
    private byte[] aesIV;

    @Inject
    CryptoConfig cryptoConfig;

    @PostConstruct
    void init() {
        CryptoManager cryptoManager = new CryptoManager(cryptoConfig.keyStore().path());
        this.aesIV = cryptoConfig.aesIV().getBytes(StandardCharsets.UTF_8);
        var key = cryptoConfig.keyStore().key();
        this.secretKey = cryptoManager.retrieveSecretKey(
                cryptoConfig.keyStore().password(),
                key.alias(),
                key.password()
        );
    }

    public String encrypt(String plainText) throws CryptoException {
        try {
            log.debug("encrypting");
            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            byte[] iv = new byte[12];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] cipherText = cipher.doFinal(plainTextBytes);
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public String decrypt(String dataToDecrypt) throws SecurityException, CryptoException {
        try {
            log.debug("decrypting");
            ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(dataToDecrypt));

            byte[] iv = new byte[12];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
}

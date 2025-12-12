package com.orhanobut.hawk;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class KeystoreEncryption implements Encryption {

  private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
  private static final String MASTER_KEY_ALIAS = "storage_master_key";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH = 128;
  private static final int IV_LENGTH = 12;

  private SecretKey secretKey;

  public KeystoreEncryption() {
  }

  @Override
  public boolean init() {
    try {
      KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
      keyStore.load(null);

      if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        );

        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build();

        keyGenerator.init(keySpec);
        secretKey = keyGenerator.generateKey();
      } else {
        secretKey = (SecretKey) keyStore.getKey(MASTER_KEY_ALIAS, null);
      }

      return secretKey != null;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String encrypt(String key, String plainText) throws Exception {
    if (secretKey == null) {
      throw new IllegalStateException("Encryption not initialized. Call init() first.");
    }

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

    byte[] iv = cipher.getIV();
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

    // Combine IV + encrypted data
    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

    return Base64.encodeToString(combined, Base64.NO_WRAP);
  }

  @Override
  public String decrypt(String key, String cipherText) throws Exception {
    if (secretKey == null) {
      throw new IllegalStateException("Encryption not initialized. Call init() first.");
    }

    byte[] combined = Base64.decode(cipherText, Base64.NO_WRAP);

    // Extract IV and encrypted data
    byte[] iv = new byte[IV_LENGTH];
    byte[] encrypted = new byte[combined.length - IV_LENGTH];
    System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
    System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

    byte[] decrypted = cipher.doFinal(encrypted);
    return new String(decrypted, StandardCharsets.UTF_8);
  }
}
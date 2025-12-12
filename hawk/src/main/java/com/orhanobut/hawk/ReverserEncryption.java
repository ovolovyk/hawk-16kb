package com.orhanobut.hawk;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class ReverserEncryption implements Encryption {

  public ReverserEncryption() {
  }

  @Override
  public boolean init() {
    // No real init needed anymore
    return true;
  }

  @Override
  public String encrypt(String key, String plainText) {
    if (plainText == null) return null;

    // Reverse plaintext
    String reversed = new StringBuilder(plainText).reverse().toString();

    // Encode reversed text as Base64
    return Base64.encodeToString(
        reversed.getBytes(StandardCharsets.UTF_8),
        Base64.NO_WRAP
    );
  }

  @Override
  public String decrypt(String key, String cipherText) {
    if (cipherText == null) return null;

    // Decode Base64 to reversed text
    String reversed = new String(
        Base64.decode(cipherText, Base64.NO_WRAP),
        StandardCharsets.UTF_8
    );

    // Reverse back to original
    return new StringBuilder(reversed).reverse().toString();
  }
}
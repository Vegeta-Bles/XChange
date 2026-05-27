package org.knowm.xchange.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DigestUtilsTest {

  @Test
  void bytesToHex_emptyArray() {
    byte[] bytes = new byte[0];
    assertThat(DigestUtils.bytesToHex(bytes)).isEmpty();
  }

  @Test
  void bytesToHex_singleByte() {
    byte[] bytes = new byte[] {0x00};
    assertThat(DigestUtils.bytesToHex(bytes)).isEqualTo("00");
  }

  @Test
  void bytesToHex_singleByte_max() {
    byte[] bytes = new byte[] {(byte) 0xFF};
    assertThat(DigestUtils.bytesToHex(bytes)).isEqualTo("ff");
  }

  @Test
  void bytesToHex_multipleBytes() {
    byte[] bytes = new byte[] {0x01, 0x02, 0x03, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF};
    assertThat(DigestUtils.bytesToHex(bytes)).isEqualTo("010203abcdef");
  }

  @Test
  void bytesToHex_allHexValues() {
    byte[] bytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
    assertThat(DigestUtils.bytesToHex(bytes)).isEqualTo("00010203040506070809");
  }

  @Test
  void bytesToHex_uppercaseLetters() {
    byte[] bytes = new byte[] {(byte) 0xA0, (byte) 0xB1, (byte) 0xC2, (byte) 0xD3, (byte) 0xE4, (byte) 0xF5};
    assertThat(DigestUtils.bytesToHex(bytes)).isEqualTo("a0b1c2d3e4f5");
  }

  @Test
  void bytesToHex_largeArray() {
    byte[] bytes = new byte[256];
    for (int i = 0; i < 256; i++) {
      bytes[i] = (byte) i;
    }
    String hex = DigestUtils.bytesToHex(bytes);
    assertThat(hex).hasSize(512);
    assertThat(hex).isEqualTo("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"
        + "202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f"
        + "404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f"
        + "606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f"
        + "808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f"
        + "a0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebf"
        + "c0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedf"
        + "e0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff");
  }

  @ParameterizedTest
  @CsvSource({
      "'00', 0x00",
      "'01', 0x01",
      "'ff', 0xFF",
      "'a0', 0xA0",
      "'b1', 0xB1",
      "'c2', 0xC2",
      "'d3', 0xD3",
      "'e4', 0xE4",
      "'f5', 0xF5",
      "'0a', 0x0A",
      "'1b', 0x1B",
      "'2c', 0x2C",
      "'3d', 0x3D",
      "'4e', 0x4E",
      "'5f', 0x5F",
  })
  void hexToBytes_singleByte(String hex, int expected) {
    byte[] result = DigestUtils.hexToBytes(hex);
    assertThat(result).hasSize(1);
    assertThat(result[0] & 0xFF).isEqualTo(expected & 0xFF);
  }

  @Test
  void hexToBytes_multipleBytes() {
    byte[] result = DigestUtils.hexToBytes("010203abcdef");
    assertThat(result).hasSize(6);
    assertThat(result[0]).isEqualTo((byte) 0x01);
    assertThat(result[1]).isEqualTo((byte) 0x02);
    assertThat(result[2]).isEqualTo((byte) 0x03);
    assertThat(result[3]).isEqualTo((byte) 0xAB);
    assertThat(result[4]).isEqualTo((byte) 0xCD);
    assertThat(result[5]).isEqualTo((byte) 0xEF);
  }

  @Test
  void hexToBytes_allHexValues() {
    StringBuilder hex = new StringBuilder();
    for (int i = 0; i < 256; i++) {
      hex.append(String.format("%02x", i));
    }
    byte[] result = DigestUtils.hexToBytes(hex.toString());
    assertThat(result).hasSize(256);
    for (int i = 0; i < 256; i++) {
      assertThat(result[i] & 0xFF).isEqualTo(i);
    }
  }

  @Test
  void hexToBytes_roundTrip() {
    byte[] original = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0x80, (byte) 0x01};
    String hex = DigestUtils.bytesToHex(original);
    byte[] restored = DigestUtils.hexToBytes(hex);
    assertThat(restored).isEqualTo(original);
  }

  @Test
  void hexToBytes_uppercaseHex() {
    byte[] result = DigestUtils.hexToBytes("010203ABCDEF");
    assertThat(result).hasSize(6);
    assertThat(result[3]).isEqualTo((byte) 0xAB);
    assertThat(result[5]).isEqualTo((byte) 0xEF);
  }

  @Test
  void hexToBytes_mixedCaseHex() {
    byte[] result = DigestUtils.hexToBytes("0aAbCdEf");
    assertThat(result).hasSize(4);
    assertThat(result[0]).isEqualTo((byte) 0x0A);
    assertThat(result[1]).isEqualTo((byte) 0xAB);
    assertThat(result[2]).isEqualTo((byte) 0xCD);
    assertThat(result[3]).isEqualTo((byte) 0xEF);
  }

  @Test
  void hexToBytes_oddLengthThrows() {
    assertThatThrownBy(() -> DigestUtils.hexToBytes("123"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("even number of characters");
  }

  @Test
  void hexToBytes_oddLengthThrows_multiple() {
    assertThatThrownBy(() -> DigestUtils.hexToBytes("abc"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("even number of characters");
  }

  @Test
  void hexToBytes_emptyString() {
    byte[] result = DigestUtils.hexToBytes("");
    assertThat(result).hasSize(0);
  }

  @Test
  void bytesToHex_hexToBytes_roundTrip_large() {
    byte[] original = new byte[1000];
    for (int i = 0; i < 1000; i++) {
      original[i] = (byte) (i % 256);
    }
    String hex = DigestUtils.bytesToHex(original);
    byte[] restored = DigestUtils.hexToBytes(hex);
    assertThat(restored).isEqualTo(original);
  }

  @Test
  void bytesToHex_nullBytes() {
    assertThatThrownBy(() -> DigestUtils.bytesToHex(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void hexToBytes_nullString() {
    assertThatThrownBy(() -> DigestUtils.hexToBytes(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void hexToBytes_invalidHexChar() {
    assertThatThrownBy(() -> DigestUtils.hexToBytes("gg"))
        .isInstanceOf(NumberFormatException.class);
  }

  @Test
  void hexToBytes_invalidHexCharMixed() {
    assertThatThrownBy(() -> DigestUtils.hexToBytes("0g"))
        .isInstanceOf(NumberFormatException.class);
  }

  @Test
  void bytesToHex_zeroBytes() {
    byte[] bytes = new byte[10];
    assertThat(DigestUtils.bytesToHex(bytes)).isEqualTo("00000000000000000000");
  }

  @Test
  void hexToBytes_zeroHex() {
    byte[] result = DigestUtils.hexToBytes("0000000000");
    assertThat(result).hasSize(5);
    for (byte b : result) {
      assertThat(b).isEqualTo((byte) 0x00);
    }
  }
}

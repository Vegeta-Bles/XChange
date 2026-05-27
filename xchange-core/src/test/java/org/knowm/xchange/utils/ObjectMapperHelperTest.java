package org.knowm.xchange.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ObjectMapperHelperTest {

  static class TestDto {
    private String name;
    private int value;
    private String nullableField;

    public TestDto() {}

    public TestDto(String name, int value, String nullableField) {
      this.name = name;
      this.value = value;
      this.nullableField = nullableField;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public String getNullableField() { return nullableField; }
    public void setNullableField(String nullableField) { this.nullableField = nullableField; }
  }

  @Test
  void readValue_deserializesJson() throws IOException {
    String json = "{\"name\":\"test\",\"value\":42}";
    TestDto result = ObjectMapperHelper.readValue(json, TestDto.class);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getValue()).isEqualTo(42);
  }

  @Test
  void readValue_withExtraFields_ignoresExtra() throws IOException {
    String json = "{\"name\":\"test\",\"value\":42,\"extra\":\"ignored\"}";
    TestDto result = ObjectMapperHelper.readValue(json, TestDto.class);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getValue()).isEqualTo(42);
  }

  @Test
  void readValueStrict_deserializesJson() throws IOException {
    String json = "{\"name\":\"test\",\"value\":42}";
    TestDto result = ObjectMapperHelper.readValueStrict(json, TestDto.class);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getValue()).isEqualTo(42);
  }

  @Test
  void toJSON_serializesWithIndentation() throws IOException {
    TestDto dto = new TestDto("test", 42, null);
    String json = ObjectMapperHelper.toJSON(dto);
    assertThat(json).contains("\n");
    assertThat(json).contains("\"name\"");
    assertThat(json).contains("\"test\"");
    assertThat(json).contains("\"value\"");
    assertThat(json).contains("42");
  }

  @Test
  void toCompactJSON_serializesWithoutIndentation() throws IOException {
    TestDto dto = new TestDto("test", 42, null);
    String json = ObjectMapperHelper.toCompactJSON(dto);
    assertThat(json).doesNotContain("\n");
    assertThat(json).contains("\"name\":\"test\"");
    assertThat(json).contains("\"value\":42");
  }

  @Test
  void toJSON_excludesNullFields() throws IOException {
    TestDto dto = new TestDto("test", 42, null);
    String json = ObjectMapperHelper.toJSON(dto);
    assertThat(json).doesNotContain("nullableField");
  }

  @Test
  void toJSON_includesNonNullFields() throws IOException {
    TestDto dto = new TestDto("test", 42, "present");
    String json = ObjectMapperHelper.toJSON(dto);
    assertThat(json).contains("nullableField");
    assertThat(json).contains("present");
  }

  @Test
  void viaJSON_roundTrip() throws IOException {
    TestDto original = new TestDto("test", 42, "roundtrip");
    TestDto result = ObjectMapperHelper.viaJSON(original);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getValue()).isEqualTo(42);
    assertThat(result.getNullableField()).isEqualTo("roundtrip");
  }

  @Test
  void viaJSON_roundTripWithNull() throws IOException {
    TestDto original = new TestDto("test", 42, null);
    TestDto result = ObjectMapperHelper.viaJSON(original);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getValue()).isEqualTo(42);
    assertThat(result.getNullableField()).isNull();
  }

  @Test
  void toCompactJSON_excludesNullFields() throws IOException {
    TestDto dto = new TestDto("test", 42, null);
    String json = ObjectMapperHelper.toCompactJSON(dto);
    assertThat(json).doesNotContain("nullableField");
  }

  @Test
  void readValue_emptyObject() throws IOException {
    String json = "{}";
    TestDto result = ObjectMapperHelper.readValue(json, TestDto.class);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isNull();
    assertThat(result.getValue()).isEqualTo(0);
  }

  @Test
  void toJSON_emptyObject() throws IOException {
    TestDto dto = new TestDto();
    String json = ObjectMapperHelper.toJSON(dto);
    assertThat(json).contains("{}");
  }

  @Test
  void toCompactJSON_emptyObject() throws IOException {
    TestDto dto = new TestDto();
    String json = ObjectMapperHelper.toCompactJSON(dto);
    assertThat(json).contains("{}");
  }

  @Test
  void readValue_specialCharacters() throws IOException {
    String json = "{\"name\":\"test with spaces & symbols!\",\"value\":123}";
    TestDto result = ObjectMapperHelper.readValue(json, TestDto.class);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test with spaces & symbols!");
  }

  @Test
  void toJSON_specialCharacters() throws IOException {
    TestDto dto = new TestDto("test with spaces & symbols!", 123, null);
    String json = ObjectMapperHelper.toJSON(dto);
    assertThat(json).contains("test with spaces & symbols!");
  }

  @Test
  void readValue_numericValue() throws IOException {
    String json = "{\"name\":\"\",\"value\":999999}";
    TestDto result = ObjectMapperHelper.readValue(json, TestDto.class);
    assertThat(result.getValue()).isEqualTo(999999);
  }

  @Test
  void toJSON_largeValue() throws IOException {
    TestDto dto = new TestDto("large", 999999, null);
    String json = ObjectMapperHelper.toJSON(dto);
    assertThat(json).contains("999999");
  }

  @Test
  void readValueStrict_withUnknownProperty_throws() throws IOException {
    String json = "{\"name\":\"test\",\"value\":42,\"unknown\":\"field\"}";
    assertThatThrownBy(() -> ObjectMapperHelper.readValueStrict(json, TestDto.class))
        .isInstanceOf(Exception.class);
  }
}

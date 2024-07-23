package com.thitsaworks.mojaloop.thitsaconnect.fspiop.model;

import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * U2F challenge-response, where payer FSP verifies if the response provided by end-user device matches the previously registered key. 
 **/

@JsonTypeName("U2FPinValue")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-07-23T12:41:40.588154500+06:30[Asia/Rangoon]")
public class U2FPinValue   {
  private @Valid String pinValue;
  private @Valid String counter;

  /**
   * U2F challenge-response, where payer FSP verifies if the response provided by end-user device matches the previously registered key. 
   **/
  public U2FPinValue pinValue(String pinValue) {
    this.pinValue = pinValue;
    return this;
  }

  
  @JsonProperty("pinValue")
  @NotNull
 @Pattern(regexp="^\\S{1,64}$") @Size(min=1,max=64)  public String getPinValue() {
    return pinValue;
  }

  @JsonProperty("pinValue")
  public void setPinValue(String pinValue) {
    this.pinValue = pinValue;
  }

  /**
   * The API data type Integer is a JSON String consisting of digits only. Negative numbers and leading zeroes are not allowed. The data type is always limited to a specific number of digits.
   **/
  public U2FPinValue counter(String counter) {
    this.counter = counter;
    return this;
  }

  
  @JsonProperty("counter")
  @NotNull
 @Pattern(regexp="^[1-9]\\d*$")  public String getCounter() {
    return counter;
  }

  @JsonProperty("counter")
  public void setCounter(String counter) {
    this.counter = counter;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    U2FPinValue u2FPinValue = (U2FPinValue) o;
    return Objects.equals(this.pinValue, u2FPinValue.pinValue) &&
        Objects.equals(this.counter, u2FPinValue.counter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pinValue, counter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class U2FPinValue {\n");
    
    sb.append("    pinValue: ").append(toIndentedString(pinValue)).append("\n");
    sb.append("    counter: ").append(toIndentedString(counter)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}


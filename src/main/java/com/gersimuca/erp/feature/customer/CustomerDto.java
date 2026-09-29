package com.gersimuca.erp.feature.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author gersimuca
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
  private Long customerId;
  private String name;
  private String email;
  private String phone;
  private String company;
  private String addressLine;
  private String city;
  private String state;
  private String postalCode;
  private String country;
  private CustomerStatus status;
  private Long ownerId;
  private String notes;
}

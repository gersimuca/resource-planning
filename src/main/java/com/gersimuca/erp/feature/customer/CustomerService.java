package com.gersimuca.erp.feature.customer;

/**
 * @author gersimuca
 */
public interface CustomerService {
  CustomersPageDto search(
      CustomerStatus customerStatus,
      Long ownerId,
      String search,
      Integer page,
      Integer size,
      String sort);

  CustomerDto findById(Long id);

  CustomerDto create(CustomerDto dto);

  CustomerDto update(Long id, CustomerDto dto);

  void delete(Long id);
}

package com.gersimuca.erp.feature.customer;

import static com.gersimuca.erp.common.AuthorizationExpressions.IS_AUTHORIZED;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import com.gersimuca.erp.api.CustomersApi;
import com.gersimuca.erp.model.CustomerModel;
import com.gersimuca.erp.model.CustomerResponse;
import com.gersimuca.erp.model.CustomerStatus;
import com.gersimuca.erp.model.CustomersPageResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author gersimuca
 */
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {
  private final CustomerService service;
  private final CustomerMapper mapper;

  @Override
  @PreAuthorize(IS_AUTHORIZED)
  public ResponseEntity<CustomersPageResponse> searchCustomers(
      CustomerStatus status, Long ownerId, String search, Integer page, Integer size, String sort) {
    final CustomersPageDto customersPageDto =
        service.search(mapper.toStatus(status), ownerId, search, page, size, sort);
    return ok(new CustomersPageResponse().customersPage(mapper.toResponse(customersPageDto)));
  }

  @Override
  @PreAuthorize(IS_AUTHORIZED)
  public ResponseEntity<CustomerResponse> createCustomer(CustomerModel customerModel) {
    final CustomerDto request = mapper.toDto(customerModel);
    final CustomerDto dto = service.create(request);
    final URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(dto.getCustomerId())
            .toUri();
    return created(location).body(new CustomerResponse().customer(mapper.toModel(dto)));
  }

  @Override
  @PreAuthorize(IS_AUTHORIZED)
  public ResponseEntity<Void> deleteCustomer(Long id) {
    service.delete(id);
    return noContent().build();
  }

  @Override
  @PreAuthorize(IS_AUTHORIZED)
  public ResponseEntity<CustomerResponse> getCustomerById(Long id) {
    final CustomerDto dto = service.findById(id);
    return ok(new CustomerResponse().customer(mapper.toModel(dto)));
  }

  @Override
  @PreAuthorize(IS_AUTHORIZED)
  public ResponseEntity<CustomerResponse> updateCustomer(Long id, CustomerModel request) {
    final CustomerDto dto = service.update(id, mapper.toDto(request));
    return ok(new CustomerResponse().customer(mapper.toModel(dto)));
  }
}

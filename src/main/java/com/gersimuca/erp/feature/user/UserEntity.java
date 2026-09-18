package com.gersimuca.erp.feature.user;

import com.gersimuca.erp.common.AuditedEntity;
import com.gersimuca.erp.feature.customer.CustomerEntity;
import com.gersimuca.erp.feature.deal.DealEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class UserEntity extends AuditedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "business_key", nullable = false, unique = true)
  private Long businessKey;

  @Column(name = "username", nullable = false, unique = true, length = 8)
  private String username;

  @Column(name = "given_name", nullable = false, length = 512)
  private String givenName;

  @Column(name = "family_name", nullable = false, length = 512)
  private String familyName;

  @Column(name = "email", nullable = false, length = 1024)
  private String email;

  @Column(name = "preferred_language", nullable = false, length = 10)
  private String preferredLanguage;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ownerId")
  private Collection<CustomerEntity> customers;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ownerId")
  private Collection<DealEntity> deals;
}

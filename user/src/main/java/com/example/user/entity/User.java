package com.example.user.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EntityListeners;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @Entity: Marks this class as a JPA entity, mapping it to a database table.
 * @EntityListeners(AuditingEntityListener.class): Enables JPA auditing for automatic timestamp management.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable{
  /**
   * @CreatedDate: Automatically sets the creation timestamp when the entity is persisted.
   * @Column(updatable = false): Ensures the creation date is not updated after initial insert.
   */
  @CreatedDate
  @Column(updatable = false)
  private java.time.LocalDateTime createdDate;

  /**
   * @LastModifiedDate: Automatically updates the timestamp whenever the entity is modified.
   */
  @LastModifiedDate
  private java.time.LocalDateTime lastModifiedDate;
  public java.time.LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(java.time.LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public java.time.LocalDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(java.time.LocalDateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
  /**
   * @Id: Marks this field as the primary key of the entity.
   * @GeneratedValue(strategy=GenerationType.AUTO): Automatically generates the primary key value.
   */
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer id;

  private String name;


  /**
   * @Column(unique = true): Ensures the email field is unique in the database.
   */
  @Column(unique = true)
  private String email;

  // Soft delete flag
  private boolean deleted = false;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
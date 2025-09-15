package web.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseEntity {

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_by", length = 26)
  private String createdBy;

  @Column(name = "updated_by", length = 26)
  private String updatedBy;

  @Column(name = "deleted_by", length = 26)
  private String deletedBy;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  // Overload for convenience in tests or callers using OffsetDateTime
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt == null ? null : createdAt.toInstant(); }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  // Overload
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt == null ? null : updatedAt.toInstant(); }

  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
  // Overload
  public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt == null ? null : deletedAt.toInstant(); }

  public String getCreatedBy() { return createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

  public String getUpdatedBy() { return updatedBy; }
  public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

  public String getDeletedBy() { return deletedBy; }
  public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
}

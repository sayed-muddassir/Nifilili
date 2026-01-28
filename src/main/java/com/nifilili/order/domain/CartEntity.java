package com.nifilili.order.domain;

import com.nifilili.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "carts")
@Getter
@Setter
public class CartEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
}

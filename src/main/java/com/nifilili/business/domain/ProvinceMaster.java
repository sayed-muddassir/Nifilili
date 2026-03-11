package com.nifilili.business.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.nifilili.core.entity.BaseEntity;

@Entity
@Table(name = "province_master")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceMaster extends BaseEntity {

    private String name;
}

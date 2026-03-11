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
@Table(name = "municipality_master")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityMaster extends BaseEntity {

    private Long districtId;
    private String name;
    private String type;
}

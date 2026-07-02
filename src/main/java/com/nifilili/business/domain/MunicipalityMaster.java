package com.nifilili.business.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.nifilili.core.entity.BaseEntity;

import java.util.List;

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
    private String description;

    @OneToMany(mappedBy = "municipalityId", cascade = CascadeType.ALL)
    private List<MunicipalityImageMaster> images;
}

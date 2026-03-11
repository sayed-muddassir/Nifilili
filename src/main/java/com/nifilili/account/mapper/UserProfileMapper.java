package com.nifilili.account.mapper;

import com.nifilili.account.domain.UserProfile;
import com.nifilili.account.dto.request.UpdateProfileRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    /**
     * Updates an existing UserProfile from the request DTO.
     * Only non-null fields from the request are applied (partial update).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(UpdateProfileRequest request, @MappingTarget UserProfile profile);
}

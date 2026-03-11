package com.nifilili.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserProfileDetailResponse", description = "Full user profile including auth details and extended profile.")
public class UserProfileDetailResponse {

    // Auth user fields
    @Schema(description = "User ID.", example = "101")
    private Long id;

    @Schema(description = "Full name.", example = "John Doe")
    private String name;

    @Schema(description = "Username.", example = "johndoe")
    private String username;

    @Schema(description = "Email address.", example = "john@nifilili.com")
    private String email;

    @Schema(description = "Phone number.", example = "+977-9800000000")
    private String phone;

    @Schema(description = "Account enabled.", example = "true")
    private boolean enabled;

    @Schema(description = "Email verified.", example = "true")
    private boolean emailVerified;

    @Schema(description = "Roles assigned to the user.")
    private Set<String> roles;

    // Extended profile fields
    @Schema(description = "Avatar URL.")
    private String avatarUrl;

    @Schema(description = "Short bio.")
    private String bio;

    @Schema(description = "Date of birth.", example = "1995-06-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Gender.", example = "Male")
    private String gender;

    @Schema(description = "Street address.")
    private String streetAddress;

    @Schema(description = "City.", example = "Kathmandu")
    private String city;

    @Schema(description = "State or province.", example = "Bagmati")
    private String state;

    @Schema(description = "Country.", example = "Nepal")
    private String country;

    @Schema(description = "Postal code.", example = "44600")
    private String postalCode;
}

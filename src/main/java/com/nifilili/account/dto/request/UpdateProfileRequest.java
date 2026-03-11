package com.nifilili.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateProfileRequest", description = "Request to update the authenticated user's extended profile.")
public class UpdateProfileRequest {

    @Size(max = 1024, message = "Avatar URL must not exceed 1024 characters")
    @Schema(description = "URL of the user's avatar image.", example = "https://cdn.nifilili.com/avatars/user123.jpg")
    private String avatarUrl;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    @Schema(description = "Short biography or description.", example = "Full-stack developer based in Kathmandu.")
    private String bio;

    @Schema(description = "Date of birth.", example = "1995-06-15")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    @Schema(description = "Gender.", example = "Male")
    private String gender;

    @Size(max = 500, message = "Street address must not exceed 500 characters")
    @Schema(description = "Street address line.")
    private String streetAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City.", example = "Kathmandu")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Schema(description = "State or province.", example = "Bagmati")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Schema(description = "Country.", example = "Nepal")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Schema(description = "Postal or ZIP code.", example = "44600")
    private String postalCode;
}

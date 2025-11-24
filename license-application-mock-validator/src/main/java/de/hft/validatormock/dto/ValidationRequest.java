package de.hft.validatormock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {

    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "[A-Za-z ]+", message = "Full name must contain only letters and spaces")
    private String fullName;

    @NotBlank(message = "ID is required")
    //@Pattern(regexp = "[A-Z0-9]{6,12}", message = "ID must be alphanumeric and 6-12 characters long")
    private String id;

    @NotBlank(message = "Property address is required")
    @Pattern(regexp = ".{5,}", message = "Property address must be at least 5 characters long")
    private String propertyAddress;
}


package com.epam.finaltask.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class VoucherDTO {

    private String id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Tour type is required")
    private String tourType;

    @NotNull(message = "Transfer type is required")
    private String transferType;

    @NotNull(message = "Hotel type is required")
    private String hotelType;

    private String status;

    @NotNull
    private LocalDate arrivalDate;

    @NotNull
    private LocalDate evictionDate;

    private UUID userId;

    private Boolean isHot;

}

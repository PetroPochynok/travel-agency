package com.epam.finaltask.dto;

import com.epam.finaltask.model.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VoucherDTO {

    private String id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Tour type is required")
    private TourType tourType;

    @NotNull(message = "Transfer type is required")
    private TransferType transferType;

    @NotNull(message = "Hotel type is required")
    private HotelType hotelType;

    private VoucherStatus status;

    @NotNull
    private LocalDate arrivalDate;

    @NotNull
    private LocalDate evictionDate;

    private UUID userId;

    private String userName;

    private Boolean isHot;

    private String cancellationReason;

    private LocalDateTime cancellationRequestedAt;

}

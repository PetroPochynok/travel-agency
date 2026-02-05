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

    @NotBlank(message = "{voucher.title.notBlank}")
    private String title;

    @NotBlank(message = "{voucher.description.notBlank}")
    private String description;

    @Positive(message = "{voucher.price.positive}")
    private Double price;

    @NotNull(message = "{voucher.tourType.required}")
    private TourType tourType;

    @NotNull(message = "{voucher.transferType.required}")
    private TransferType transferType;

    @NotNull(message = "{voucher.hotelType.required}")
    private HotelType hotelType;

    private VoucherStatus status;

    @NotNull(message = "{voucher.arrivalDate.required}")
    @FutureOrPresent(message = "{voucher.arrivalDate.notPast}")
    private LocalDate arrivalDate;

    @NotNull(message = "{voucher.evictionDate.required}")
    private LocalDate evictionDate;

    private UUID userId;

    private String userName;

    private Boolean isHot;

    private String cancellationReason;

    private LocalDateTime cancellationRequestedAt;

}

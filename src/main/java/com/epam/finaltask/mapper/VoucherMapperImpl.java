package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class VoucherMapperImpl implements VoucherMapper {

    private final ModelMapper modelMapper;

    public VoucherMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public Voucher toVoucher(VoucherDTO voucherDTO) {
        if (voucherDTO == null) return null;
        Voucher voucher = modelMapper.map(voucherDTO, Voucher.class);

        // convert String enums to actual enum types
        if (voucherDTO.getTourType() != null) {
            voucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        }
        if (voucherDTO.getTransferType() != null) {
            voucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        }
        if (voucherDTO.getHotelType() != null) {
            voucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        }
        if (voucherDTO.getStatus() != null) {
            voucher.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));
        } else {
            voucher.setStatus(VoucherStatus.REGISTERED); // default
        }

        voucher.setId(voucherDTO.getId() != null ? UUID.fromString(voucherDTO.getId()) : UUID.randomUUID());
        voucher.setHot(voucherDTO.getIsHot() != null && voucherDTO.getIsHot());

        return voucher;
    }

    @Override
    public VoucherDTO toVoucherDTO(Voucher voucher) {
        if (voucher == null) return null;
        VoucherDTO dto = modelMapper.map(voucher, VoucherDTO.class);

        // Convert enums to String
        if (voucher.getTourType() != null) {
            dto.setTourType(voucher.getTourType().name());
        }
        if (voucher.getTransferType() != null) {
            dto.setTransferType(voucher.getTransferType().name());
        }
        if (voucher.getHotelType() != null) {
            dto.setHotelType(voucher.getHotelType().name());
        }
        if (voucher.getStatus() != null) {
            dto.setStatus(voucher.getStatus().name());
        }

        // Convert UUID to String
        dto.setId(voucher.getId() != null ? voucher.getId().toString() : null);

        return dto;
    }
}
package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapperImpl implements VoucherMapper {

    private final ModelMapper modelMapper;

    public VoucherMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public Voucher toVoucher(VoucherDTO voucherDTO) {
        if (voucherDTO == null) {
            return null;
        }

        Voucher voucher = modelMapper.map(voucherDTO, Voucher.class);

        // default status if not provided
        if (voucher.getStatus() == null) {
            voucher.setStatus(VoucherStatus.REGISTERED);
        }

        return voucher;
    }

    @Override
    public VoucherDTO toVoucherDTO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }

        return modelMapper.map(voucher, VoucherDTO.class);
    }
}
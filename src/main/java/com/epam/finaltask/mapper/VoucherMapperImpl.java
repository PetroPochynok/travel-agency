package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapperImpl implements VoucherMapper {

    private final ModelMapper modelMapper;

    public VoucherMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setAmbiguityIgnored(true);

        TypeMap<Voucher, VoucherDTO> typeMap = this.modelMapper.getTypeMap(Voucher.class, VoucherDTO.class);
        if (typeMap == null) {
            typeMap = this.modelMapper.createTypeMap(Voucher.class, VoucherDTO.class);
        }
        typeMap.addMappings(mapper -> mapper.skip(VoucherDTO::setUserName));
    }

    @Override
    public Voucher toVoucher(VoucherDTO voucherDTO) {
        if (voucherDTO == null) {
            return null;
        }

        Voucher voucher = modelMapper.map(voucherDTO, Voucher.class);

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

        VoucherDTO dto = modelMapper.map(voucher, VoucherDTO.class);
        if (voucher.getUser() != null) {
            dto.setUserId(voucher.getUser().getId());
            dto.setUserName(voucher.getUser().getUsername());
        }
        return dto;
    }
}
package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.VoucherNotFoundException;
import com.epam.finaltask.exception.VoucherOrderException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    public VoucherServiceImpl(
            VoucherRepository voucherRepository,
            UserRepository userRepository,
            VoucherMapper voucherMapper) {
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
        this.voucherMapper = voucherMapper;
    }

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toVoucherDTO(saved);
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.REGISTERED) {
            throw new VoucherOrderException("Voucher cannot be ordered");
        }

        if (user.getBalance().compareTo(BigDecimal.valueOf(voucher.getPrice())) < 0) {
            throw new VoucherOrderException("Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(voucher.getPrice())));
        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.PAID);

        userRepository.save(user);
        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        Voucher existingVoucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        if (voucherDTO.getTitle() != null)
            existingVoucher.setTitle(voucherDTO.getTitle());
        if (voucherDTO.getDescription() != null)
            existingVoucher.setDescription(voucherDTO.getDescription());
        if (voucherDTO.getPrice() != null)
            existingVoucher.setPrice(voucherDTO.getPrice());
        if (voucherDTO.getTourType() != null)
            existingVoucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        if (voucherDTO.getTransferType() != null)
            existingVoucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        if (voucherDTO.getHotelType() != null)
            existingVoucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        if (voucherDTO.getArrivalDate() != null)
            existingVoucher.setArrivalDate(voucherDTO.getArrivalDate());
        if (voucherDTO.getEvictionDate() != null)
            existingVoucher.setEvictionDate(voucherDTO.getEvictionDate());

        return voucherMapper.toVoucherDTO(voucherRepository.save(existingVoucher));
    }

    @Override
    public void delete(String voucherId) {
        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        voucher.setHot(voucherDTO.getIsHot());
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        return voucherRepository.findAllByUserId(UUID.fromString(userId))
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        return voucherRepository.findAllByTourType(tourType)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        return voucherRepository.findAllByTransferType(TransferType.valueOf(transferType))
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        return voucherRepository.findAllByPrice(price)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        return voucherRepository.findAllByHotelType(hotelType)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll()
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }
}

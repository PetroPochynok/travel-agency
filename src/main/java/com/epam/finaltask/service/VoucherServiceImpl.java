package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.VoucherNotFoundException;
import com.epam.finaltask.exception.VoucherOrderException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        if (voucher.getStatus() == null) {
            voucher.setStatus(VoucherStatus.REGISTERED);
        }

        if (voucher.getIsHot() == null) {
            voucher.setIsHot(false);
        }

        Voucher savedVoucher = voucherRepository.save(voucher);
        return voucherMapper.toVoucherDTO(savedVoucher);
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
            existingVoucher.setTourType(voucherDTO.getTourType());
        if (voucherDTO.getTransferType() != null)
            existingVoucher.setTransferType(voucherDTO.getTransferType());
        if (voucherDTO.getHotelType() != null)
            existingVoucher.setHotelType(voucherDTO.getHotelType());
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

        voucher.setIsHot((voucherDTO.getIsHot()));
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }


    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByTourType(tourType, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(TransferType transferType, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByTransferType(transferType, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    public List<VoucherDTO> findAllByPrice(Double price, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByPrice(price, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByHotelType(hotelType, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    public List<VoucherDTO> findMyVouchers(String username) {
        return voucherRepository.findAllByUser_Username(username)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    public List<VoucherDTO> findCatalog(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return voucherRepository
                .findAllByStatusOrderByIsHotDesc(VoucherStatus.REGISTERED, pageable)
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

    public Page<VoucherDTO> findCatalogFiltered(
            TourType tourType,
            TransferType transferType,
            HotelType hotelType,
            String description,
            Double minPrice,
            Double maxPrice,
            Pageable pageable
    ) {
        Specification<Voucher> spec = Specification.where(null);

        if (description != null && !description.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
        }
        if (minPrice != null && maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("price"), minPrice, maxPrice));
        }
        if (tourType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tourType"), tourType));
        }
        if (transferType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("transferType"), transferType));
        }
        if (hotelType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("hotelType"), hotelType));
        }

        Page<Voucher> page = voucherRepository.findAll(spec, pageable);

        return page.map(voucherMapper::toVoucherDTO);
    }

}

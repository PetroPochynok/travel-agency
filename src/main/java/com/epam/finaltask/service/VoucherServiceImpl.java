package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.*;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Override
    @Transactional
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
    @Transactional
    public VoucherDTO order(String id, String userId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (voucher.getIsHot() != null && voucher.getIsHot() && !user.isActive()) {
            throw new VoucherOrderException("Inactive users cannot order HOT vouchers");
        }

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
    @Transactional
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException(id);
        }

        Voucher existingVoucher = voucherRepository.findById(uuid)
                .orElseThrow(() -> new VoucherNotFoundException(id));

        LocalDate arrival = voucherDTO.getArrivalDate();
        LocalDate eviction = voucherDTO.getEvictionDate();
        if (arrival != null && eviction != null) {
            if (!eviction.isAfter(arrival)) {
                throw new InvalidDatesException("Eviction date must be after arrival date");
            }
        }

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

        return voucherMapper.toVoucherDTO(
                voucherRepository.save(existingVoucher)
        );
    }

    @Override
    @Transactional
    public void delete(String voucherId) {
        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    @Transactional
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        voucher.setIsHot((voucherDTO.getIsHot()));
        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherDTO findById(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException(id);
        }

        Voucher voucher = voucherRepository.findById(uuid)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        return voucherMapper.toVoucherDTO(voucher);
    }


    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findAllByTourType(TourType tourType, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByTourType(tourType, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findAllByTransferType(TransferType transferType, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByTransferType(transferType, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findAllByPrice(Double price, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByPrice(price, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> vouchers = voucherRepository.findAllByHotelType(hotelType, pageable);
        return vouchers.stream().map(voucherMapper::toVoucherDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findMyVouchers(String username) {
        return voucherRepository.findAllByUser_Username(username)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findCatalog(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return voucherRepository
                .findAllByStatusOrderByIsHotDesc(VoucherStatus.REGISTERED, pageable)
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll()
                .stream()
                .map(voucherMapper::toVoucherDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherDTO> findCatalogFiltered(
            TourType tourType,
            TransferType transferType,
            HotelType hotelType,
            String description,
            Double minPrice,
            Double maxPrice,
            Pageable pageable,
            String username
    ) {
        Specification<Voucher> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("status"), VoucherStatus.REGISTERED));

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

        if (username != null && !username.isBlank()) {
            var userOpt = userRepository.findUserByUsername(username);
            if (userOpt.isPresent() && !userOpt.get().isActive()) {
                spec = spec.and((root, query, cb) -> cb.isFalse(root.get("isHot")));
            }
        }

        Page<Voucher> page = voucherRepository.findAll(spec, pageable);

        return page.map(voucherMapper::toVoucherDTO);
    }

    @Override
    @Transactional
    public VoucherDTO requestCancellation(String id, String username, String reason) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        if (voucher.getUser() == null || !username.equals(voucher.getUser().getUsername())) {
            throw new VoucherOrderException("You may only request cancellation for your own voucher");
        }

        if (voucher.getStatus() != VoucherStatus.PAID) {
            throw new VoucherOrderException("Voucher cannot be cancelled in its current status");
        }

        voucher.setStatus(VoucherStatus.CANCELLATION_REQUESTED);
        voucher.setCancellationReason(reason);
        voucher.setCancellationRequestedAt(LocalDateTime.now());

        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDTO decideCancellation(String id, boolean approved, String adminUsername) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.CANCELLATION_REQUESTED) {
            throw new VoucherOrderException("Voucher is not awaiting cancellation");
        }

        User user = voucher.getUser();
        if (approved) {
            if (user != null) {
                user.setBalance(user.getBalance().add(BigDecimal.valueOf(voucher.getPrice())));
                userRepository.save(user);
            }
            voucher.setUser(null);
            voucher.setStatus(VoucherStatus.CANCELED);
        } else {
            voucher.setStatus(VoucherStatus.PAID);
        }

        // clear request fields
        voucher.setCancellationReason(null);
        voucher.setCancellationRequestedAt(null);

        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDTO reregisterVoucher(String id, String adminUsername) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.CANCELED) {
            throw new VoucherOrderException("Voucher is not in CANCELED status");
        }

        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setUser(null);
        voucher.setCancellationReason(null);
        voucher.setCancellationRequestedAt(null);

        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

}

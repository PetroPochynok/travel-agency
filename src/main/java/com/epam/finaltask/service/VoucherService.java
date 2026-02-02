package com.epam.finaltask.service;

import java.util.List;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);
    VoucherDTO findById(String id);

    List<VoucherDTO> findAllByTourType(TourType tourType, int page, int size, String sortBy);
    List<VoucherDTO> findAllByTransferType(TransferType transferType, int page, int size, String sortBy);
    List<VoucherDTO> findAllByPrice(Double price, int page, int size, String sortBy);
    List<VoucherDTO> findAllByHotelType(HotelType hotelType, int page, int size, String sortBy);
    List<VoucherDTO> findCatalog(int page, int size, String sortBy);
    List<VoucherDTO> findMyVouchers(String username);
    List<VoucherDTO> findAll();
    Page<VoucherDTO> findCatalogFiltered(
            TourType tourType,
            TransferType transferType,
            HotelType hotelType,
            String description,
            Double minPrice,
            Double maxPrice,
            Pageable pageable
    );
    VoucherDTO requestCancellation(String id, String username, String reason);
    VoucherDTO decideCancellation(String id, boolean approved, String adminUsername);
}

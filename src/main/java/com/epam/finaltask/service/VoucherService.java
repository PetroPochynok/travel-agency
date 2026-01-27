package com.epam.finaltask.service;

import java.util.List;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);

    List<VoucherDTO> findAllByTourType(TourType tourType, int page, int size, String sortBy);
    List<VoucherDTO> findAllByTransferType(TransferType transferType, int page, int size, String sortBy);
    List<VoucherDTO> findAllByPrice(Double price, int page, int size, String sortBy);
    List<VoucherDTO> findAllByHotelType(HotelType hotelType, int page, int size, String sortBy);
    List<VoucherDTO> findCatalog(int page, int size, String sortBy);
    List<VoucherDTO> findCatalog();
    List<VoucherDTO> findMyVouchers(String username);
    List<VoucherDTO> findAll();
}

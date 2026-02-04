package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.*;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;

import com.epam.finaltask.repository.VoucherRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    @Order(1)
    void create_shouldReturnSavedVoucherDTO() {
        VoucherDTO voucherDTO = new VoucherDTO();
        Voucher voucherEntity = new Voucher();
        Voucher savedVoucher = new Voucher();

        savedVoucher.setStatus(VoucherStatus.REGISTERED);
        savedVoucher.setIsHot(false);

        VoucherDTO savedDTO = new VoucherDTO();

        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucherEntity);
        when(voucherRepository.save(voucherEntity)).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(savedDTO);

        VoucherDTO result = voucherService.create(voucherDTO);

        assertNotNull(result);
        assertEquals(savedDTO, result);

        verify(voucherMapper).toVoucher(voucherDTO);
        verify(voucherRepository).save(voucherEntity);
        verify(voucherMapper).toVoucherDTO(savedVoucher);
    }

    @Test
    @Order(2)
    void create_shouldPreserveExistingStatusAndHot() {
        VoucherDTO voucherDTO = new VoucherDTO();
        Voucher voucherEntity = new Voucher();
        Voucher savedVoucher = new Voucher();

        voucherEntity.setStatus(VoucherStatus.PAID);
        voucherEntity.setIsHot(true);

        VoucherDTO savedDTO = new VoucherDTO();

        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucherEntity);
        when(voucherRepository.save(voucherEntity)).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(savedDTO);

        VoucherDTO result = voucherService.create(voucherDTO);

        assertNotNull(result);
        assertEquals(savedDTO, result);
        assertEquals(VoucherStatus.PAID, voucherEntity.getStatus());
        assertTrue(voucherEntity.getIsHot());

        verify(voucherMapper).toVoucher(voucherDTO);
        verify(voucherRepository).save(voucherEntity);
        verify(voucherMapper).toVoucherDTO(savedVoucher);
    }

    @Test
    @Order(3)
    void order_shouldReturnVoucherDTO_whenAllConditionsAreMet() {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setPrice(100.0);
        voucher.setIsHot(false);

        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(150));
        user.setActive(true);

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.order(voucherId.toString(), userId.toString());

        assertNotNull(result);
        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.PAID, voucher.getStatus());

        assertEquals(0, user.getBalance().compareTo(BigDecimal.valueOf(50)));

        assertEquals(user, voucher.getUser());

        verify(userRepository).save(user);
        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }


    @Test
    @Order(4)
    void order_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () ->
                voucherService.order(voucherId.toString(), userId.toString())
        );
    }

    @Test
    @Order(5)
    void order_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setPrice(100.0);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                voucherService.order(voucherId.toString(), userId.toString())
        );
    }

    @Test
    @Order(6)
    void order_shouldThrowVoucherOrderException_whenHotVoucherAndInactiveUser() {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setPrice(100.0);
        voucher.setIsHot(true);

        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(200));
        user.setActive(false);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(VoucherOrderException.class, () ->
                voucherService.order(voucherId.toString(), userId.toString())
        );
    }

    @Test
    @Order(7)
    void order_shouldThrowVoucherOrderException_whenVoucherNotRegistered() {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.PAID);
        voucher.setPrice(100.0);
        voucher.setIsHot(false);

        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(200));
        user.setActive(true);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(VoucherOrderException.class, () ->
                voucherService.order(voucherId.toString(), userId.toString())
        );
    }

    @Test
    @Order(8)
    void order_shouldThrowVoucherOrderException_whenUserBalanceTooLow() {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setPrice(150.0);
        voucher.setIsHot(false);

        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(100));
        user.setActive(true);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(VoucherOrderException.class, () ->
                voucherService.order(voucherId.toString(), userId.toString())
        );
    }

    @Test
    @Order(9)
    void update_shouldReturnUpdatedVoucherDTO_whenAllFieldsAreValid() {
        UUID voucherId = UUID.randomUUID();

        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("New Title");
        voucherDTO.setDescription("New Description");
        voucherDTO.setPrice(200.0);
        voucherDTO.setArrivalDate(LocalDate.of(2026, 3, 1));
        voucherDTO.setEvictionDate(LocalDate.of(2026, 3, 10));

        Voucher existingVoucher = new Voucher();
        existingVoucher.setId(voucherId);

        Voucher savedVoucher = new Voucher();
        savedVoucher.setId(voucherId);

        VoucherDTO updatedDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(existingVoucher));
        when(voucherRepository.save(existingVoucher)).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(updatedDTO);

        VoucherDTO result = voucherService.update(voucherId.toString(), voucherDTO);

        assertNotNull(result);
        assertEquals(updatedDTO, result);
        assertEquals("New Title", existingVoucher.getTitle());
        assertEquals("New Description", existingVoucher.getDescription());
        assertEquals(200.0, existingVoucher.getPrice());
        assertEquals(LocalDate.of(2026, 3, 1), existingVoucher.getArrivalDate());
        assertEquals(LocalDate.of(2026, 3, 10), existingVoucher.getEvictionDate());

        verify(voucherRepository).findById(voucherId);
        verify(voucherRepository).save(existingVoucher);
        verify(voucherMapper).toVoucherDTO(savedVoucher);
    }

    @Test
    @Order(10)
    void update_shouldThrowInvalidUuidException_whenIdIsNotUuid() {
        String invalidId = "invalid-uuid";
        VoucherDTO voucherDTO = new VoucherDTO();

        InvalidUuidException exception = assertThrows(InvalidUuidException.class, () ->
                voucherService.update(invalidId, voucherDTO));

        assertTrue(exception.getMessage().contains(invalidId));
    }

    @Test
    @Order(11)
    void update_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        UUID voucherId = UUID.randomUUID();
        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () ->
                voucherService.update(voucherId.toString(), voucherDTO));

        verify(voucherRepository).findById(voucherId);
    }

    @Test
    @Order(12)
    void update_shouldThrowInvalidDatesException_whenEvictionNotAfterArrival() {
        UUID voucherId = UUID.randomUUID();
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setArrivalDate(LocalDate.of(2026, 3, 10));
        voucherDTO.setEvictionDate(LocalDate.of(2026, 3, 5));

        Voucher existingVoucher = new Voucher();
        existingVoucher.setId(voucherId);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(existingVoucher));

        assertThrows(InvalidDatesException.class, () ->
                voucherService.update(voucherId.toString(), voucherDTO));

        verify(voucherRepository).findById(voucherId);
    }

    @Test
    @Order(13)
    void update_shouldUpdateOnlyNonNullFields() {
        UUID voucherId = UUID.randomUUID();

        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setTitle("Updated Title");

        Voucher existingVoucher = new Voucher();
        existingVoucher.setId(voucherId);
        existingVoucher.setDescription("Old Description");
        existingVoucher.setPrice(100.0);

        Voucher savedVoucher = new Voucher();
        savedVoucher.setId(voucherId);

        VoucherDTO updatedDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(existingVoucher));
        when(voucherRepository.save(existingVoucher)).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(updatedDTO);

        VoucherDTO result = voucherService.update(voucherId.toString(), voucherDTO);

        assertNotNull(result);
        assertEquals(updatedDTO, result);
        assertEquals("Updated Title", existingVoucher.getTitle());
        assertEquals("Old Description", existingVoucher.getDescription());
        assertEquals(100.0, existingVoucher.getPrice());

        verify(voucherRepository).findById(voucherId);
        verify(voucherRepository).save(existingVoucher);
        verify(voucherMapper).toVoucherDTO(savedVoucher);
    }

    @Test
    @Order(14)
    void delete_shouldCallRepositoryWithValidUuid() {
        UUID voucherId = UUID.randomUUID();
        String idString = voucherId.toString();

        voucherService.delete(idString);

        verify(voucherRepository).deleteById(voucherId);
    }

    @Test
    @Order(15)
    void delete_shouldThrowInvalidUuidException_whenIdIsNotValidUuid() {
        String invalidId = "not-a-uuid";

        assertThrows(IllegalArgumentException.class, () ->
                voucherService.delete(invalidId));
    }

    @Test
    @Order(16)
    void changeHotStatus_shouldUpdateIsHotAndReturnDTO() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setIsHot(false);

        VoucherDTO inputDTO = new VoucherDTO();
        inputDTO.setIsHot(true);

        Voucher savedVoucher = new Voucher();
        savedVoucher.setId(voucherId);
        savedVoucher.setIsHot(true);

        VoucherDTO outputDTO = new VoucherDTO();
        outputDTO.setIsHot(true);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(savedVoucher);
        when(voucherMapper.toVoucherDTO(savedVoucher)).thenReturn(outputDTO);

        VoucherDTO result = voucherService.changeHotStatus(voucherId.toString(), inputDTO);

        assertNotNull(result);
        assertEquals(outputDTO, result);
        assertTrue(voucher.getIsHot());

        verify(voucherRepository).findById(voucherId);
        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(savedVoucher);
    }

    @Test
    @Order(17)
    void changeHotStatus_shouldThrowExceptionIfVoucherNotFound() {
        UUID voucherId = UUID.randomUUID();
        VoucherDTO inputDTO = new VoucherDTO();
        inputDTO.setIsHot(true);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () ->
                voucherService.changeHotStatus(voucherId.toString(), inputDTO));
    }

    @Test
    @Order(18)
    void findById_shouldReturnVoucherDTO_whenVoucherExists() {
        UUID voucherId = UUID.randomUUID();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.findById(voucherId.toString());

        assertNotNull(result);
        assertEquals(voucherDTO, result);

        verify(voucherRepository).findById(voucherId);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @Order(19)
    void findById_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        UUID voucherId = UUID.randomUUID();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () ->
                voucherService.findById(voucherId.toString()));

        verify(voucherRepository).findById(voucherId);
    }

    @Test
    @Order(20)
    void findById_shouldThrowInvalidUuidException_whenUuidIsInvalid() {
        String invalidId = "invalid-uuid";

        InvalidUuidException exception = assertThrows(InvalidUuidException.class, () ->
                voucherService.findById(invalidId));

        assertTrue(exception.getMessage().contains(invalidId));
    }

    @Test
    @Order(21)
    void findAllByTourType_shouldReturnVoucherDTOList() {
        TourType tourType = TourType.LEISURE;

        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());
        voucher1.setStatus(VoucherStatus.REGISTERED);

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());
        voucher2.setStatus(VoucherStatus.REGISTERED);

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        int page = 0;
        int size = 10;
        String sortBy = "price";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher1, voucher2));

        when(voucherRepository.findAllByTourType(tourType, pageable)).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findAllByTourType(tourType, page, size, sortBy);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAllByTourType(tourType, pageable);
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(22)
    void findAllByTransferType_shouldReturnVoucherDTOList() {
        TransferType transferType = TransferType.BUS;

        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());
        voucher1.setStatus(VoucherStatus.REGISTERED);

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());
        voucher2.setStatus(VoucherStatus.REGISTERED);

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        int page = 0;
        int size = 10;
        String sortBy = "price";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher1, voucher2));

        when(voucherRepository.findAllByTransferType(transferType, pageable)).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findAllByTransferType(transferType, page, size, sortBy);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAllByTransferType(transferType, pageable);
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(23)
    void findAllByPrice_shouldReturnVoucherDTOList() {
        Double price = 200.0;

        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());
        voucher1.setPrice(price);
        voucher1.setStatus(VoucherStatus.REGISTERED);

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());
        voucher2.setPrice(price);
        voucher2.setStatus(VoucherStatus.REGISTERED);

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        int page = 0;
        int size = 10;
        String sortBy = "price";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher1, voucher2));

        when(voucherRepository.findAllByPrice(price, pageable)).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findAllByPrice(price, page, size, sortBy);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAllByPrice(price, pageable);
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(24)
    void findAllByHotelType_shouldReturnVoucherDTOList() {
        HotelType hotelType = HotelType.FIVE_STARS;

        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());
        voucher1.setHotelType(hotelType);
        voucher1.setStatus(VoucherStatus.REGISTERED);

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());
        voucher2.setHotelType(hotelType);
        voucher2.setStatus(VoucherStatus.REGISTERED);

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        int page = 0;
        int size = 10;
        String sortBy = "hotelType";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher1, voucher2));

        when(voucherRepository.findAllByHotelType(hotelType, pageable)).thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findAllByHotelType(hotelType, page, size, sortBy);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAllByHotelType(hotelType, pageable);
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(25)
    void findMyVouchers_shouldReturnVoucherDTOList() {
        String username = "customer";

        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());
        voucher1.setStatus(VoucherStatus.REGISTERED);

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());
        voucher2.setStatus(VoucherStatus.REGISTERED);

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        when(voucherRepository.findAllByUser_Username(username)).thenReturn(List.of(voucher1, voucher2));
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findMyVouchers(username);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAllByUser_Username(username);
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(26)
    void findCatalog_shouldReturnVoucherDTOList() {
        int page = 0;
        int size = 2;
        String sortBy = "price";

        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());
        voucher1.setStatus(VoucherStatus.REGISTERED);

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());
        voucher2.setStatus(VoucherStatus.REGISTERED);

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher1, voucher2));

        when(voucherRepository.findAllByStatusOrderByIsHotDesc(VoucherStatus.REGISTERED, pageable))
                .thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findCatalog(page, size, sortBy);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAllByStatusOrderByIsHotDesc(VoucherStatus.REGISTERED, pageable);
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(27)
    void findAll_shouldReturnVoucherDTOList() {
        Voucher voucher1 = new Voucher();
        voucher1.setId(UUID.randomUUID());

        Voucher voucher2 = new Voucher();
        voucher2.setId(UUID.randomUUID());

        VoucherDTO dto1 = new VoucherDTO();
        VoucherDTO dto2 = new VoucherDTO();

        List<Voucher> vouchers = List.of(voucher1, voucher2);

        when(voucherRepository.findAll()).thenReturn(vouchers);
        when(voucherMapper.toVoucherDTO(voucher1)).thenReturn(dto1);
        when(voucherMapper.toVoucherDTO(voucher2)).thenReturn(dto2);

        List<VoucherDTO> result = voucherService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(voucherRepository).findAll();
        verify(voucherMapper).toVoucherDTO(voucher1);
        verify(voucherMapper).toVoucherDTO(voucher2);
    }

    @Test
    @Order(28)
    void findCatalogFiltered_shouldReturnMappedVoucherDTOPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Voucher voucher = new Voucher();
        voucher.setId(UUID.randomUUID());
        voucher.setStatus(VoucherStatus.REGISTERED);

        Page<Voucher> voucherPage = new PageImpl<>(List.of(voucher));

        VoucherDTO voucherDTO = new VoucherDTO();

        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(new User()));
        when(voucherRepository.findAll(ArgumentMatchers.<Specification<Voucher>>any(), eq(pageable)))
                .thenReturn(voucherPage);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        Page<VoucherDTO> result = voucherService.findCatalogFiltered(
                TourType.HEALTH,
                TransferType.BUS,
                HotelType.ONE_STAR,
                "description",
                100.0,
                500.0,
                pageable,
                "user"
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(voucherDTO, result.getContent().get(0));

        verify(voucherRepository).findAll(ArgumentMatchers.<Specification<Voucher>>any(), eq(pageable));
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @Order(29)
    void requestCancellation_shouldReturnUpdatedVoucherDTO_whenConditionsAreMet() {
        UUID voucherId = UUID.randomUUID();
        String username = "customer";
        String reason = "Change of plans";

        User user = new User();
        user.setUsername(username);

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.PAID);

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);
        when(voucherRepository.save(voucher)).thenReturn(voucher);

        VoucherDTO result = voucherService.requestCancellation(voucherId.toString(), username, reason);

        assertNotNull(result);
        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.CANCELLATION_REQUESTED, voucher.getStatus());
        assertEquals(reason, voucher.getCancellationReason());
        assertNotNull(voucher.getCancellationRequestedAt());

        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

    @Test
    @Order(30)
    void requestCancellation_shouldThrowVoucherOrderException_ifUserMismatch() {
        UUID voucherId = UUID.randomUUID();
        User user = new User();
        user.setUsername("otherUser");

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.PAID);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        VoucherOrderException exception = assertThrows(
                VoucherOrderException.class,
                () -> voucherService.requestCancellation(voucherId.toString(), "customer", "reason")
        );

        assertEquals("You may only request cancellation for your own voucher", exception.getMessage());
    }

    @Test
    @Order(31)
    void requestCancellation_shouldThrowVoucherOrderException_ifVoucherNotPaid() {
        UUID voucherId = UUID.randomUUID();
        User user = new User();
        user.setUsername("customer");

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.REGISTERED);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        VoucherOrderException exception = assertThrows(
                VoucherOrderException.class,
                () -> voucherService.requestCancellation(voucherId.toString(), "customer", "reason")
        );

        assertEquals("Voucher cannot be cancelled in its current status", exception.getMessage());
    }

    @Test
    @Order(32)
    void decideCancellation_shouldCancelVoucherAndRefundUser_whenApproved() {
        UUID voucherId = UUID.randomUUID();
        User user = new User();
        user.setBalance(BigDecimal.valueOf(100));

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setUser(user);
        voucher.setPrice(50.0);
        voucher.setStatus(VoucherStatus.CANCELLATION_REQUESTED);
        voucher.setCancellationReason("Some reason");
        voucher.setCancellationRequestedAt(LocalDateTime.now());

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(userRepository.save(user)).thenReturn(user);

        VoucherDTO result = voucherService.decideCancellation(voucherId.toString(), true, "admin");

        assertNotNull(result);
        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.CANCELED, voucher.getStatus());
        assertNull(voucher.getUser());
        assertNull(voucher.getCancellationReason());
        assertNull(voucher.getCancellationRequestedAt());

        assertEquals(0, user.getBalance().compareTo(BigDecimal.valueOf(150)));

        verify(voucherRepository).save(voucher);
        verify(userRepository).save(user);
        verify(voucherMapper).toVoucherDTO(voucher);
    }


    @Test
    @Order(33)
    void decideCancellation_shouldRevertVoucherToPaid_whenNotApproved() {
        UUID voucherId = UUID.randomUUID();
        User user = new User();

        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.CANCELLATION_REQUESTED);
        voucher.setCancellationReason("Some reason");
        voucher.setCancellationRequestedAt(LocalDateTime.now());

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);
        when(voucherRepository.save(voucher)).thenReturn(voucher);

        VoucherDTO result = voucherService.decideCancellation(voucherId.toString(), false, "admin");

        assertNotNull(result);
        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.PAID, voucher.getStatus());
        assertNotNull(voucher.getUser());
        assertNull(voucher.getCancellationReason());
        assertNull(voucher.getCancellationRequestedAt());

        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
        verify(userRepository, never()).save(any());
    }

    @Test
    @Order(34)
    void decideCancellation_shouldThrowException_whenVoucherNotAwaitingCancellation() {
        UUID voucherId = UUID.randomUUID();
        Voucher voucher = new Voucher();
        voucher.setStatus(VoucherStatus.PAID);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        VoucherOrderException exception = assertThrows(
                VoucherOrderException.class,
                () -> voucherService.decideCancellation(voucherId.toString(), true, "admin")
        );

        assertEquals("Voucher is not awaiting cancellation", exception.getMessage());
    }

    @Test
    @Order(35)
    void reregisterVoucher_shouldSetStatusToRegisteredAndClearFields() {
        UUID voucherId = UUID.randomUUID();
        Voucher voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setStatus(VoucherStatus.CANCELED);
        voucher.setUser(new User());
        voucher.setCancellationReason("Some reason");
        voucher.setCancellationRequestedAt(LocalDateTime.now());

        VoucherDTO voucherDTO = new VoucherDTO();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.reregisterVoucher(voucherId.toString(), "admin");

        assertNotNull(result);
        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());
        assertNull(voucher.getUser());
        assertNull(voucher.getCancellationReason());
        assertNull(voucher.getCancellationRequestedAt());

        verify(voucherRepository).save(voucher);
        verify(voucherMapper).toVoucherDTO(voucher);
    }

}
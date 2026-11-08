package com.farmerprocurement.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.farmerprocurement.backend.dto.ProcurementCenterRequest;
import com.farmerprocurement.backend.entity.ProcurementCenter;
import com.farmerprocurement.backend.repository.ProcurementCenterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProcurementCenterServiceTest {

    @Mock
    private ProcurementCenterRepository centerRepository;

    @InjectMocks
    private ProcurementCenterService service;

    @Test
    void updateWithoutCoordinatesRetainsPreviouslySavedCoordinates() {
        ProcurementCenter existing = new ProcurementCenter();
        existing.setId(7L);
        existing.setCenterCode("OLD");
        existing.setLatitude(28.6);
        existing.setLongitude(77.2);
        when(centerRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(centerRepository.findByCenterCode("NEW")).thenReturn(Optional.empty());
        when(centerRepository.save(any(ProcurementCenter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateCenter(7L, request());

        assertEquals(28.6, existing.getLatitude());
        assertEquals(77.2, existing.getLongitude());
        verify(centerRepository).save(existing);
    }

    @Test
    void rejectsPartialCoordinateUpdates() {
        ProcurementCenter existing = new ProcurementCenter();
        existing.setId(7L);
        when(centerRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(centerRepository.findByCenterCode("NEW")).thenReturn(Optional.empty());
        ProcurementCenterRequest request = request();
        request.setLatitude(28.6);

        assertThrows(ResponseStatusException.class,
                () -> service.updateCenter(7L, request));
    }

    private ProcurementCenterRequest request() {
        ProcurementCenterRequest request = new ProcurementCenterRequest();
        request.setCenterName("New center");
        request.setCenterCode("NEW");
        request.setAddress("Address");
        request.setVillage("Village");
        request.setDistrict("District");
        request.setState("State");
        return request;
    }
}

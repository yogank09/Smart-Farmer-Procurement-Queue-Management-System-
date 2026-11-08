package com.farmerprocurement.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.farmerprocurement.backend.entity.ProcurementCenter;
import com.farmerprocurement.backend.repository.ProcurementCenterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NearbyCenterServiceTest {

    @Mock
    private ProcurementCenterRepository centerRepository;

    @Mock
    private ReverseGeocoder reverseGeocoder;

    @InjectMocks
    private NearbyCenterService service;

    @Test
    void returnsNearestCentersAndExcludesMissingOrInvalidCoordinates() {
        ProcurementCenter farther = center(2L, 0.1, 0.0);
        ProcurementCenter closer = center(1L, 0.0, 0.0);
        ProcurementCenter noLocation = center(3L, null, null);
        ProcurementCenter invalid = center(4L, Double.NaN, 0.0);
        when(reverseGeocoder.reverse(0.0, 0.0)).thenReturn("Example locality");
        when(centerRepository.findByActiveTrueAndLatitudeIsNotNullAndLongitudeIsNotNull())
                .thenReturn(List.of(farther, noLocation, invalid, closer));

        var result = service.findNearby(0.0, 0.0);

        assertEquals("Example locality", result.locationLabel());
        assertEquals(List.of(1L, 2L), result.centers().stream()
                .map(item -> item.center().getId())
                .toList());
        assertEquals(0.0, result.centers().get(0).distanceKm());
        assertEquals("© OpenStreetMap contributors", result.attribution());
        verify(reverseGeocoder).reverse(0.0, 0.0);
    }

    @Test
    void rejectsInvalidCoordinatesBeforeCallingProvider() {
        assertThrows(ResponseStatusException.class,
                () -> service.findNearby(91.0, 0.0));

        verify(reverseGeocoder, never()).reverse(91.0, 0.0);
        verify(centerRepository, never())
                .findByActiveTrueAndLatitudeIsNotNullAndLongitudeIsNotNull();
    }

    private ProcurementCenter center(Long id, Double latitude, Double longitude) {
        ProcurementCenter center = new ProcurementCenter();
        center.setId(id);
        center.setCenterName("Center " + id);
        center.setCenterCode("CODE" + id);
        center.setAddress("Address");
        center.setVillage("Village");
        center.setDistrict("District");
        center.setState("State");
        center.setLatitude(latitude);
        center.setLongitude(longitude);
        center.setActive(true);
        return center;
    }
}

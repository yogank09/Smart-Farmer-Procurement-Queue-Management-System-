package com.farmerprocurement.backend.service;

import java.util.Comparator;
import java.util.List;

import com.farmerprocurement.backend.dto.NearbyCenterResponse;
import com.farmerprocurement.backend.dto.NearbyCentersResponse;
import com.farmerprocurement.backend.dto.ProcurementCenterResponse;
import com.farmerprocurement.backend.entity.ProcurementCenter;
import com.farmerprocurement.backend.repository.ProcurementCenterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NearbyCenterService {

    private static final int MAX_RESULTS = 20;
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final ProcurementCenterRepository centerRepository;
    private final ReverseGeocoder reverseGeocoder;

    public NearbyCenterService(
            ProcurementCenterRepository centerRepository,
            ReverseGeocoder reverseGeocoder) {
        this.centerRepository = centerRepository;
        this.reverseGeocoder = reverseGeocoder;
    }

    public NearbyCentersResponse findNearby(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        String locationLabel = reverseGeocoder.reverse(latitude, longitude);
        List<NearbyCenterResponse> nearby = centerRepository
                .findByActiveTrueAndLatitudeIsNotNullAndLongitudeIsNotNull()
                .stream()
                .filter(center -> validCoordinates(center.getLatitude(), center.getLongitude()))
                .map(center -> new NearbyCenterResponse(
                        toResponse(center),
                        roundToTenths(distanceKm(
                                latitude, longitude,
                                center.getLatitude(), center.getLongitude()))))
                .sorted(Comparator.comparingDouble(NearbyCenterResponse::distanceKm))
                .limit(MAX_RESULTS)
                .toList();

        return new NearbyCentersResponse(
                locationLabel,
                nearby,
                "© OpenStreetMap contributors",
                "This one-time lookup shares your coordinates with OpenStreetMap Nominatim. "
                        + "Coverage and availability may vary."
        );
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (!validCoordinates(latitude, longitude)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude and longitude must be valid coordinates"
            );
        }
    }

    private boolean validCoordinates(Double latitude, Double longitude) {
        return latitude != null && longitude != null
                && Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    private double distanceKm(
            double latitude1, double longitude1,
            double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double latDelta = lat2 - lat1;
        double lonDelta = Math.toRadians(longitude2 - longitude1);
        double haversine = Math.pow(Math.sin(latDelta / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(lonDelta / 2), 2);
        return EARTH_RADIUS_KM * 2
                * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private double roundToTenths(double distance) {
        return Math.round(distance * 10.0) / 10.0;
    }

    private ProcurementCenterResponse toResponse(ProcurementCenter center) {
        ProcurementCenterResponse response = new ProcurementCenterResponse();
        response.setId(center.getId());
        response.setCenterName(center.getCenterName());
        response.setCenterCode(center.getCenterCode());
        response.setAddress(center.getAddress());
        response.setVillage(center.getVillage());
        response.setDistrict(center.getDistrict());
        response.setState(center.getState());
        response.setPincode(center.getPincode());
        response.setContactNumber(center.getContactNumber());
        response.setLatitude(center.getLatitude());
        response.setLongitude(center.getLongitude());
        response.setActive(center.isActive());
        response.setCreatedAt(center.getCreatedAt());
        return response;
    }
}

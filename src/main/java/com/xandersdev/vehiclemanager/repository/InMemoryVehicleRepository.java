package com.xandersdev.vehiclemanager.repository;

import com.xandersdev.vehiclemanager.model.Vehicle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryVehicleRepository implements VehicleRepository {
    private final Map<String, Vehicle> vehicles = new LinkedHashMap<>();

    public InMemoryVehicleRepository() {
        insertUnchecked(new Vehicle("1234ABC", "Opel", "Astra", 22_500.00, "Hands-free system"));
        insertUnchecked(new Vehicle("2356CBA", "Peugeot", "208", 22_000.00, "No extras"));
        insertUnchecked(new Vehicle("4854YTG", "Ford", "Focus", 18_750.00, "Parking sensors"));
        insertUnchecked(new Vehicle("9876ZYX", "Ford", "Fiesta", 15_950.00, "ABS"));
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles.values());
    }

    @Override
    public Optional<Vehicle> findByRegistration(String registration) {
        return Optional.ofNullable(vehicles.get(normalize(registration)));
    }

    @Override
    public void insert(Vehicle vehicle) throws SQLException {
        String key = normalize(vehicle.registration());
        if (vehicles.containsKey(key)) {
            throw new SQLException("A vehicle with that registration already exists.");
        }
        vehicles.put(key, vehicle);
    }

    @Override
    public boolean update(Vehicle vehicle) {
        String key = normalize(vehicle.registration());
        if (!vehicles.containsKey(key)) {
            return false;
        }
        vehicles.put(key, vehicle);
        return true;
    }

    @Override
    public boolean deleteByRegistration(String registration) {
        return vehicles.remove(normalize(registration)) != null;
    }

    private void insertUnchecked(Vehicle vehicle) {
        vehicles.put(normalize(vehicle.registration()), vehicle);
    }

    private static String normalize(String registration) {
        return registration == null ? "" : registration.trim().toUpperCase();
    }
}

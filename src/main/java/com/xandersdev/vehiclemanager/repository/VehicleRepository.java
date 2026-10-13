package com.xandersdev.vehiclemanager.repository;

import com.xandersdev.vehiclemanager.model.Vehicle;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    List<Vehicle> findAll() throws SQLException;

    Optional<Vehicle> findByRegistration(String registration) throws SQLException;

    void insert(Vehicle vehicle) throws SQLException;

    boolean update(Vehicle vehicle) throws SQLException;

    boolean deleteByRegistration(String registration) throws SQLException;
}

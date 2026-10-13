package com.xandersdev.vehiclemanager.repository;

import com.xandersdev.vehiclemanager.model.Vehicle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcVehicleRepository implements VehicleRepository {
    private final String url;
    private final String user;
    private final String password;

    public JdbcVehicleRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT matricula, marca, modelo, precio, extras FROM coches ORDER BY marca, modelo";
        List<Vehicle> vehicles = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                vehicles.add(mapVehicle(result));
            }
        }
        return vehicles;
    }

    @Override
    public Optional<Vehicle> findByRegistration(String registration) throws SQLException {
        String sql = "SELECT matricula, marca, modelo, precio, extras FROM coches WHERE matricula = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registration);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapVehicle(result)) : Optional.empty();
            }
        }
    }

    @Override
    public void insert(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO coches (matricula, marca, modelo, precio, extras) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindVehicle(statement, vehicle);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE coches SET marca = ?, modelo = ?, precio = ?, extras = ? WHERE matricula = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, vehicle.brand());
            statement.setString(2, vehicle.model());
            statement.setDouble(3, vehicle.price());
            statement.setString(4, vehicle.extras());
            statement.setString(5, vehicle.registration());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteByRegistration(String registration) throws SQLException {
        String sql = "DELETE FROM coches WHERE matricula = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registration);
            return statement.executeUpdate() > 0;
        }
    }

    private static Vehicle mapVehicle(ResultSet result) throws SQLException {
        return new Vehicle(
                result.getString("matricula"),
                result.getString("marca"),
                result.getString("modelo"),
                result.getDouble("precio"),
                result.getString("extras")
        );
    }

    private static void bindVehicle(PreparedStatement statement, Vehicle vehicle) throws SQLException {
        statement.setString(1, vehicle.registration());
        statement.setString(2, vehicle.brand());
        statement.setString(3, vehicle.model());
        statement.setDouble(4, vehicle.price());
        statement.setString(5, vehicle.extras());
    }
}

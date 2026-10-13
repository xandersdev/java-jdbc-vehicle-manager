package com.xandersdev.vehiclemanager.model;

public record Vehicle(
        String registration,
        String brand,
        String model,
        double price,
        String extras
) {
}

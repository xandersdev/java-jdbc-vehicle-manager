package com.xandersdev.vehiclemanager;

import com.xandersdev.vehiclemanager.repository.InMemoryVehicleRepository;
import com.xandersdev.vehiclemanager.repository.JdbcVehicleRepository;
import com.xandersdev.vehiclemanager.repository.VehicleRepository;
import com.xandersdev.vehiclemanager.ui.VehicleManagerFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.Arrays;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        boolean demoMode = Arrays.asList(args).contains("--demo");

        SwingUtilities.invokeLater(() -> {
            VehicleRepository repository = demoMode
                    ? new InMemoryVehicleRepository()
                    : new JdbcVehicleRepository(
                            env("DB_URL", "jdbc:mysql://localhost:3306/concesionario?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"),
                            env("DB_USER", "root"),
                            env("DB_PASSWORD", "")
                    );

            try {
                VehicleManagerFrame frame = new VehicleManagerFrame(repository, demoMode);
                frame.setVisible(true);
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        exception.getMessage(),
                        "Could not start Vehicle Manager",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}

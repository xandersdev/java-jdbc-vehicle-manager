package com.xandersdev.vehiclemanager.ui;

import com.xandersdev.vehiclemanager.model.Vehicle;
import com.xandersdev.vehiclemanager.repository.VehicleRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class VehicleManagerFrame extends JFrame {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IE"));

    private final VehicleRepository repository;
    private final boolean demoMode;

    private final JTextField registrationField = new JTextField(18);
    private final JTextField brandField = new JTextField(18);
    private final JTextField modelField = new JTextField(18);
    private final JTextField priceField = new JTextField(18);
    private final JTextField extrasField = new JTextField(18);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Registration", "Brand", "Model", "Price", "Extras"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");

    public VehicleManagerFrame(VehicleRepository repository, boolean demoMode) {
        super("Vehicle Manager · JDBC / MySQL");
        this.repository = repository;
        this.demoMode = demoMode;

        configureWindow();
        setContentPane(buildContent());
        loadVehicles();
    }

    private void configureWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing falls back to the default look and feel.
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 560));
        setSize(1180, 600);
        setLocationRelativeTo(null);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Vehicle Manager");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JLabel subtitle = new JLabel(demoMode
                ? "Demo mode · in-memory data source"
                : "JDBC · MySQL · CRUD operations");
        subtitle.setForeground(new Color(95, 99, 104));
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Vehicle details"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addField(fields, c, 0, "Registration", registrationField);
        addField(fields, c, 1, "Brand", brandField);
        addField(fields, c, 2, "Model", modelField);
        addField(fields, c, 3, "Price", priceField);
        addField(fields, c, 4, "Extras", extrasField);

        JPanel actions = new JPanel(new GridBagLayout());
        addActionButton(actions, 0, 0, "Add vehicle", this::insertVehicle, true);
        addActionButton(actions, 1, 0, "Update", this::updateVehicle, false);
        addActionButton(actions, 0, 1, "Find", this::findVehicle, false);
        addActionButton(actions, 1, 1, "Delete", this::deleteVehicle, false);
        addActionButton(actions, 0, 2, "Refresh", this::loadVehicles, false);
        addActionButton(actions, 1, 2, "Reset", this::resetForm, false);

        panel.add(fields, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Inventory"));

        table.setRowHeight(27);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                copySelectedRowToForm();
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusLabel.setForeground(new Color(95, 99, 104));
        panel.add(statusLabel);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private void addActionButton(JPanel panel, int column, int row, String text, Runnable action, boolean primary) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = column;
        c.gridy = row;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        JButton button = new JButton(text);
        if (primary) {
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        }
        button.addActionListener(event -> action.run());
        panel.add(button, c);
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = repository.findAll();
            tableModel.setRowCount(0);
            for (Vehicle vehicle : vehicles) {
                tableModel.addRow(new Object[]{
                        vehicle.registration(),
                        vehicle.brand(),
                        vehicle.model(),
                        CURRENCY.format(vehicle.price()),
                        vehicle.extras()
                });
            }
            status("Loaded " + vehicles.size() + " vehicle(s)");
        } catch (SQLException exception) {
            showDatabaseError("Could not load vehicles", exception);
        }
    }

    private void insertVehicle() {
        Vehicle vehicle = readVehicleFromForm();
        if (vehicle == null) {
            return;
        }

        try {
            repository.insert(vehicle);
            status("Vehicle added: " + vehicle.registration());
            loadVehicles();
            resetForm();
        } catch (SQLException exception) {
            showDatabaseError("Could not add the vehicle", exception);
        }
    }

    private void updateVehicle() {
        Vehicle vehicle = readVehicleFromForm();
        if (vehicle == null) {
            return;
        }

        try {
            if (repository.update(vehicle)) {
                status("Vehicle updated: " + vehicle.registration());
                loadVehicles();
            } else {
                info("No vehicle was found with registration " + vehicle.registration() + ".");
            }
        } catch (SQLException exception) {
            showDatabaseError("Could not update the vehicle", exception);
        }
    }

    private void findVehicle() {
        String registration = registrationField.getText().trim();
        if (registration.isEmpty()) {
            info("Enter a registration before searching.");
            return;
        }

        try {
            repository.findByRegistration(registration).ifPresentOrElse(
                    this::fillForm,
                    () -> info("No vehicle was found with registration " + registration + ".")
            );
        } catch (SQLException exception) {
            showDatabaseError("Could not query the vehicle", exception);
        }
    }

    private void deleteVehicle() {
        String registration = registrationField.getText().trim();
        if (registration.isEmpty()) {
            info("Select a row or enter a registration before deleting.");
            return;
        }

        try {
            if (repository.deleteByRegistration(registration)) {
                status("Vehicle deleted: " + registration.toUpperCase());
                loadVehicles();
                resetForm();
            } else {
                info("No vehicle was found with registration " + registration + ".");
            }
        } catch (SQLException exception) {
            showDatabaseError("Could not delete the vehicle", exception);
        }
    }

    private Vehicle readVehicleFromForm() {
        String registration = registrationField.getText().trim().toUpperCase();
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String priceText = priceField.getText().trim().replace(',', '.');
        String extras = extrasField.getText().trim();

        if (registration.isEmpty() || brand.isEmpty() || model.isEmpty() || priceText.isEmpty() || extras.isEmpty()) {
            info("Complete all fields before saving a vehicle.");
            return null;
        }

        if (!registration.matches("[A-Z0-9]{7}")) {
            info("Registration must contain exactly 7 letters or numbers.");
            return null;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                throw new NumberFormatException();
            }
            return new Vehicle(registration, brand, model, price, extras);
        } catch (NumberFormatException exception) {
            info("Price must be a valid positive number.");
            return null;
        }
    }

    private void copySelectedRowToForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        String registration = tableModel.getValueAt(row, 0).toString();
        try {
            repository.findByRegistration(registration).ifPresent(this::fillForm);
        } catch (SQLException exception) {
            showDatabaseError("Could not load the selected vehicle", exception);
        }
    }

    private void fillForm(Vehicle vehicle) {
        registrationField.setText(vehicle.registration());
        brandField.setText(vehicle.brand());
        modelField.setText(vehicle.model());
        priceField.setText(String.format(Locale.ROOT, "%.2f", vehicle.price()));
        extrasField.setText(vehicle.extras());
        status("Selected " + vehicle.registration());
    }

    private void resetForm() {
        registrationField.setText("");
        brandField.setText("");
        modelField.setText("");
        priceField.setText("");
        extrasField.setText("");
        table.clearSelection();
        status("Ready");
    }

    private void status(String message) {
        statusLabel.setText((demoMode ? "DEMO · " : "") + message);
    }

    private void showDatabaseError(String title, SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                exception.getMessage(),
                title,
                JOptionPane.ERROR_MESSAGE
        );
        status(title);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Vehicle Manager", JOptionPane.INFORMATION_MESSAGE);
    }
}

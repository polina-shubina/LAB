package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GUI extends JFrame {
    private DBManager dbManager;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private String currentDbName;
    private String currentTableName;
    private String currentUsername;
    private String currentPassword;

    private JButton btnCreateDB;
    private JButton btnDropDB;
    private JButton btnCreateTable;
    private JButton btnClearTable;
    private JButton btnAddString;
    private JButton btnUpdateGood;
    private JButton btnDeleteGood;

    private DefaultTableModel tableModel;

    public GUI() {
        setTitle("Онлайн магазин");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel loginPanel = createLoginPanel();
        JPanel operationsPanel = createOperationsPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(operationsPanel, "operations");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");

        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblDb = new JLabel("База данных:");
        JTextField tfDb = new JTextField("online_store", 15);

        JLabel lblRole = new JLabel("Роль:");
        JRadioButton rbAdmin = new JRadioButton("Admin");
        JRadioButton rbGuest = new JRadioButton("Guest");
        rbAdmin.setSelected(true);
        ButtonGroup bgRole = new ButtonGroup();
        bgRole.add(rbAdmin);
        bgRole.add(rbGuest);
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(rbAdmin);
        rolePanel.add(rbGuest);

        JLabel lblPassword = new JLabel("Пароль:");
        JPasswordField pfPassword = new JPasswordField(15);
        JButton btnLogin = new JButton("Войти");

        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblDb, gbc);
        gbc.gridx = 1;
        panel.add(tfDb, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblRole, gbc);
        gbc.gridx = 1;
        panel.add(rolePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblPassword, gbc);
        gbc.gridx = 1;
        panel.add(pfPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String dbName = tfDb.getText().trim();
            String username = rbAdmin.isSelected() ? "postgres" : "postgres";
            String password = new String(pfPassword.getPassword());
            try {
                currentDbName = dbName;
                currentUsername = username;
                currentPassword = password;
                dbManager = new DBManager(dbName, username, password);
                dbManager.getConnection().close();
                dbManager.initProcedures();
                JOptionPane.showMessageDialog(this, "Подключение успешно");
                if (currentUsername.equalsIgnoreCase("guest")) {
                    updateOperationsPanelForGuest();
                }
                cardLayout.show(mainPanel, "operations");
                refreshStoreTable("");
            } catch (SQLException ex) {
                if (ex.getMessage().contains("does not exist")) {
                    int response = JOptionPane.showConfirmDialog(
                            this,
                            "Database \"" + dbName + "\" does not exist. Create it automatically?",
                            "Database Not Found",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (response == JOptionPane.YES_OPTION) {
                        try {
                            DBManager tempManager = new DBManager("postgres", username, password);
                            tempManager.createDatabase(dbName);
                            dbManager = new DBManager(dbName, username, password);
                            dbManager.getConnection().close();
                            dbManager.initProcedures();
                            currentDbName = dbName;
                            JOptionPane.showMessageDialog(this, "Database created successfully. Connection successful.");
                            if (currentUsername.equalsIgnoreCase("guest")) {
                                updateOperationsPanelForGuest();
                            }
                            cardLayout.show(mainPanel, "operations");
                            refreshStoreTable("");
                        } catch (SQLException ex2) {
                            JOptionPane.showMessageDialog(this, "Error creating database: " + ex2.getMessage());
                        } catch (Exception ex2) {
                            JOptionPane.showMessageDialog(this, "Error initializing procedures: " + ex2.getMessage());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error initializing procedures: " + ex.getMessage());
            }
        });

        return panel;
    }

    private void updateOperationsPanelForGuest() {
        if (btnCreateDB != null) btnCreateDB.setEnabled(false);
        if (btnDropDB != null) btnDropDB.setEnabled(false);
        if (btnCreateTable != null) btnCreateTable.setEnabled(false);
        if (btnClearTable != null) btnClearTable.setEnabled(false);
        if (btnAddString != null) btnAddString.setEnabled(false);
        if (btnUpdateGood != null) btnUpdateGood.setEnabled(false);
        if (btnDeleteGood != null) btnDeleteGood.setEnabled(false);
    }

    private void refreshStoreTable(String titleFilter) {
        try {
            List<OnlineStore> goods = dbManager.searchGood(currentTableName, titleFilter);
            tableModel.setRowCount(0);
            for (OnlineStore b : goods) {
                tableModel.addRow(new Object[]{b.getCustomerId(), b.getСustomerName(), b.getGoodId(), b.getGoodName(), b.getPrice()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private JPanel createOperationsPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel dbPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());

        JLabel lblNewDB = new JLabel("Имя бд:");
        JTextField tfNewDB = new JTextField(currentDbName != null ? currentDbName : "OnlineShop", 10);
        JLabel lblNewTable = new JLabel("Имя таблицы:");
        JTextField tfNewTable = new JTextField("goods", 10);
        currentTableName = tfNewTable.getText().trim();

        btnCreateDB = new JButton("Создать БД");
        btnDropDB = new JButton("Удалить БД");
        btnCreateTable = new JButton("Создать таблицу");
        btnClearTable = new JButton("Очистить таблицу");

        if (currentUsername != null && currentUsername.equalsIgnoreCase("guest")) {
            btnCreateDB.setEnabled(false);
            btnDropDB.setEnabled(false);
            btnCreateTable.setEnabled(false);
            btnClearTable.setEnabled(false);
        }

        topPanel.add(lblNewDB);
        topPanel.add(tfNewDB);
        topPanel.add(btnCreateDB);
        topPanel.add(btnDropDB);
        topPanel.add(lblNewTable);
        topPanel.add(tfNewTable);
        topPanel.add(btnCreateTable);
        topPanel.add(btnClearTable);

        dbPanel.add(topPanel, BorderLayout.NORTH);

        btnCreateDB.addActionListener(e -> {
            try {
                String newDb = tfNewDB.getText().trim();
                dbManager.createDatabase(newDb);
                dbManager = new DBManager(newDb, currentUsername, currentPassword);
                dbManager.initProcedures();
                currentDbName = newDb;
                JOptionPane.showMessageDialog(this, "База данных создана");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error initializing procedures: " + ex.getMessage());
            }
        });

        btnDropDB.addActionListener(e -> {
            try {
                String newDb = tfNewDB.getText().trim();
                dbManager.dropDatabase(newDb);
                JOptionPane.showMessageDialog(this, "База данных удалена");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnCreateTable.addActionListener(e -> {
            try {
                String tableName = tfNewTable.getText().trim();
                currentTableName = tableName;
                dbManager.createTable(tableName);
                JOptionPane.showMessageDialog(this, "Таблица создана");
                refreshStoreTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnClearTable.addActionListener(e -> {
            try {
                String tableName = tfNewTable.getText().trim();
                currentTableName = tableName;
                dbManager.clearTable(tableName);
                JOptionPane.showMessageDialog(this, "Таблица очищена");
                refreshStoreTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JPanel goodPanel = new JPanel(new BorderLayout());
        JPanel topGoodPanel = new JPanel(new FlowLayout());
        btnAddString = new JButton("Добавить строку");
        btnUpdateGood = new JButton("Обновить товар");
        btnDeleteGood = new JButton("Удалить товар по Id");
        JButton btnSearchGood = new JButton("Найти товар по названию");
        topGoodPanel.add(btnAddString);
        topGoodPanel.add(btnUpdateGood);
        topGoodPanel.add(btnDeleteGood);
        topGoodPanel.add(btnSearchGood);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Данные"));
        JTextField tfcustomerId = new JTextField();
        JTextField tfcustomerName = new JTextField();
        JTextField tfgoodId = new JTextField();
        JTextField tfgoodName = new JTextField();
        JTextField tfprice = new JTextField();
        inputPanel.add(new JLabel("Id покупателя:"));
        inputPanel.add(tfcustomerId);
        inputPanel.add(new JLabel("Имя покупателя:"));
        inputPanel.add(tfcustomerName);
        inputPanel.add(new JLabel("Id товара:"));
        inputPanel.add(tfgoodId);
        inputPanel.add(new JLabel("Название товара:"));
        inputPanel.add(tfgoodName);
        inputPanel.add(new JLabel("Цена товара:"));
        inputPanel.add(tfprice);

        tableModel = new DefaultTableModel(new Object[]{"customerId", "customerName", "goodId", "goodName", "Price"}, 0);
        JTable resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        goodPanel.add(topGoodPanel, BorderLayout.NORTH);
        goodPanel.add(centerPanel, BorderLayout.CENTER);

        btnAddString.addActionListener(e -> {
            try {
                String tableName = currentTableName;
                int customerId = Integer.parseInt(tfcustomerId.getText().trim());
                String customerName = tfcustomerName.getText().trim();
                int goodId = Integer.parseInt(tfgoodId.getText().trim());
                String goodName = tfgoodName.getText().trim();
                int price = Integer.parseInt(tfprice.getText().trim());
                dbManager.addString(tableName, customerId, customerName, goodId, goodName, price);
                JOptionPane.showMessageDialog(this, "Строка добавлена");
                refreshStoreTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            } catch (NumberFormatException ex) {
            }
        });

        btnSearchGood.addActionListener(e -> {
            String title = tfcustomerName.getText().trim();
            refreshStoreTable(title);
        });

        btnUpdateGood.addActionListener(e -> {
            try {
                String tableName = currentTableName;
                int customerId = Integer.parseInt(tfcustomerId.getText().trim());
                String customerName = tfcustomerName.getText().trim();
                int goodId = Integer.parseInt(tfgoodId.getText().trim());
                String goodName = tfgoodName.getText().trim();
                int price = Integer.parseInt(tfprice.getText().trim());
                dbManager.updateGood(tableName, customerId, customerName, goodId, goodName, price);
                JOptionPane.showMessageDialog(this, "Товар обновлен");
                refreshStoreTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid numeric format");
            }
        });

        btnDeleteGood.addActionListener(e -> {
            try {
                String tableName = currentTableName;
                int goodId = Integer.parseInt(tfgoodId.getText().trim());
                dbManager.deleteGood(tableName, goodId);
                JOptionPane.showMessageDialog(this, "Товар удален");
                refreshStoreTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        tabbedPane.addTab("База данных", dbPanel);
        tabbedPane.addTab("Онлайн магазин", goodPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }
}

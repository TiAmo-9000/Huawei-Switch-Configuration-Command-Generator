package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * 设备管理界面（美化版）
 * 实现功能：
 * 1. 显示设备列表（名称、IP、型号、状态等）
 * 2. 添加、编辑、删除设备
 * 3. 支持设备搜索
 * 4. 设备状态刷新（占位，可扩展为真实通信）
 */
public class DevicePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable deviceTable;
    private JButton addButton, editButton, delButton, refreshButton;
    private JTextField searchField;

    public DevicePanel(String 设备管理) {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252)); // 主面板背景

        // 顶部区域（含搜索、操作按钮）
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 239, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // 搜索区
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(18);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        JButton searchButton = createButton("搜索", new Color(37, 81, 166), Color.WHITE);

        searchPanel.add(new JLabel("设备搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 操作按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);

        addButton = createButton("添加设备", new Color(69, 149, 236), Color.WHITE);
        editButton = createButton("编辑设备", new Color(37, 81, 166), Color.WHITE);
        delButton = createButton("删除设备", new Color(240, 61, 70), Color.WHITE);
        refreshButton = createButton("刷新状态", new Color(37, 166, 81), Color.WHITE);

        btnPanel.add(addButton);
        btnPanel.add(editButton);
        btnPanel.add(delButton);
        btnPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 表格区域
        String[] columns = {"设备名称", "IP地址", "设备型号", "管理方式", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接编辑
            }
        };
        deviceTable = new JTable(tableModel);
        deviceTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        deviceTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        deviceTable.setRowHeight(28);
        deviceTable.setSelectionBackground(new Color(208, 227, 255));
        deviceTable.setSelectionForeground(new Color(25, 52, 105));
        deviceTable.setGridColor(new Color(220, 230, 240));

        JScrollPane tableScroll = new JScrollPane(deviceTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(69, 149, 236), 1, true));
        tableScroll.setBackground(Color.WHITE);

        add(tableScroll, BorderLayout.CENTER);

        // 初始化测试数据
        addTestData();

        // 事件监听
        addButton.addActionListener(e -> showAddOrEditDialog(null));
        editButton.addActionListener(e -> editSelectedDevice());
        delButton.addActionListener(e -> deleteSelectedDevice());
        searchButton.addActionListener(e -> searchDevice());
        refreshButton.addActionListener(e -> refreshStatus());

        // 双击编辑
        deviceTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && deviceTable.getSelectedRow() != -1) {
                    editSelectedDevice();
                }
            }
        });
    }

    /**
     * 创建美化按钮
     */
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void addTestData() {
        tableModel.addRow(new Object[]{"核心交换机", "192.168.1.1", "S5735", "SSH", "在线"});
        tableModel.addRow(new Object[]{"汇聚交换机1", "192.168.1.2", "S5720", "Telnet", "离线"});
        tableModel.addRow(new Object[]{"接入交换机A", "192.168.1.101", "S2700", "SSH", "在线"});
    }

    private void showAddOrEditDialog(Vector<Object> existingData) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existingData == null ? "添加设备" : "编辑设备", true);
        dialog.setSize(400, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 6, 18));
        form.setBackground(new Color(248, 250, 252));

        JTextField nameField = new JTextField(existingData == null ? "" : (String) existingData.get(0));
        JTextField ipField = new JTextField(existingData == null ? "" : (String) existingData.get(1));
        JTextField modelField = new JTextField(existingData == null ? "" : (String) existingData.get(2));
        JComboBox<String> mgmtTypeBox = new JComboBox<>(new String[]{"SSH", "Telnet"});
        if (existingData != null) mgmtTypeBox.setSelectedItem(existingData.get(3));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"在线", "离线"});
        if (existingData != null) statusBox.setSelectedItem(existingData.get(4));

        form.add(new JLabel("设备名称:"));
        form.add(nameField);
        form.add(new JLabel("IP地址:"));
        form.add(ipField);
        form.add(new JLabel("设备型号:"));
        form.add(modelField);
        form.add(new JLabel("管理方式:"));
        form.add(mgmtTypeBox);
        form.add(new JLabel("状态:"));
        form.add(statusBox);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
        btnPanel.setBackground(new Color(248, 250, 252));
        JButton okBtn = createButton("确定", new Color(37, 81, 166), Color.WHITE);
        JButton cancelBtn = createButton("取消", new Color(180, 180, 180), Color.WHITE);
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String ip = ipField.getText().trim();
            String model = modelField.getText().trim();
            String mgmt = (String) mgmtTypeBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (name.isEmpty() || ip.isEmpty() || model.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写完整信息", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (existingData == null) {
                tableModel.addRow(new Object[]{name, ip, model, mgmt, status});
            } else {
                int row = deviceTable.getSelectedRow();
                tableModel.setValueAt(name, row, 0);
                tableModel.setValueAt(ip, row, 1);
                tableModel.setValueAt(model, row, 2);
                tableModel.setValueAt(mgmt, row, 3);
                tableModel.setValueAt(status, row, 4);
            }
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedDevice() {
        int row = deviceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的设备", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Vector<Object> vec = new Vector<>();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            vec.add(tableModel.getValueAt(row, i));
        }
        showAddOrEditDialog(vec);
    }

    private void deleteSelectedDevice() {
        int row = deviceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的设备", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选设备？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void searchDevice() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            deviceTable.clearSelection();
            return;
        }
        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean match = false;
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object val = tableModel.getValueAt(i, j);
                if (val != null && val.toString().toLowerCase().contains(kw.toLowerCase())) {
                    match = true;
                    break;
                }
            }
            if (match) {
                deviceTable.setRowSelectionInterval(i, i);
                deviceTable.scrollRectToVisible(deviceTable.getCellRect(i, 0, true));
                found = true;
                break;
            }
        }
        if (!found) {
            JOptionPane.showMessageDialog(this, "未找到相关设备", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshStatus() {
        // 占位：此处可实现真实的设备状态获取（如Ping、SNMP等）
        JOptionPane.showMessageDialog(this, "状态已刷新（演示模式）", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}
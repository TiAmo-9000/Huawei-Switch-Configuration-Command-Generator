package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * IP地址配置界面
 * 主要功能：
 * 1. 支持IP地址的增删改查
 * 2. 支持接口选择：端口类型（选择），端口号（填写）
 *    端口类型：XGE口（XGigabitethernet）、GE口（Gigabitethernet）、FE口（FastEthernet）、E口（Ethernet）
 * 3. 命令生成预览
 */
public class IpPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public IpPanel(String ip配置) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("IP地址配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        String[] columns = {"IP地址", "掩码", "接口"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        addBtn = new JButton("新增IP");
        editBtn = new JButton("编辑IP");
        delBtn = new JButton("删除IP");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        cmdPreviewArea = new JTextArea(8, 36);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        add(new JScrollPane(cmdPreviewArea), BorderLayout.EAST);

        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        previewBtn.addActionListener(e -> previewCmd());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editSelected();
                }
            }
        });

        addTestData();
    }

    private void addTestData() {
        // 默认接口格式：类型-号，例如 GE口1/0/1
        tableModel.addRow(new Object[]{"192.168.1.1", "255.255.255.0", "GE口1/0/1"});
        tableModel.addRow(new Object[]{"10.1.1.254", "255.255.255.0", "XGE口1/0/5"});
        tableModel.addRow(new Object[]{"172.16.0.1", "255.255.0.0", "E口0/0/2"});
        tableModel.addRow(new Object[]{"192.168.2.1", "255.255.255.0", "FE口0/1/1"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "新增IP" : "编辑IP", true);
        dialog.setSize(430, 220);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField ipField = new JTextField(data == null ? "" : data[0].toString());
        JTextField maskField = new JTextField(data == null ? "" : data[1].toString());

        // 端口类型下拉+端口号输入
        JPanel ifacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JComboBox<String> ifaceTypeBox = new JComboBox<>(new String[]{"XGE口", "GE口", "FE口", "E口"});
        JTextField ifaceNumField = new JTextField(10);
        if (data != null && data[2] != null) {
            String iface = data[2].toString();
            if (iface.startsWith("XGE口")) {
                ifaceTypeBox.setSelectedItem("XGE口");
                ifaceNumField.setText(iface.substring(3));
            } else if (iface.startsWith("GE口")) {
                ifaceTypeBox.setSelectedItem("GE口");
                ifaceNumField.setText(iface.substring(2));
            } else if (iface.startsWith("FE口")) {
                ifaceTypeBox.setSelectedItem("FE口");
                ifaceNumField.setText(iface.substring(2));
            } else if (iface.startsWith("E口")) {
                ifaceTypeBox.setSelectedItem("E口");
                ifaceNumField.setText(iface.substring(2));
            } else {
                ifaceNumField.setText(iface);
            }
        }
        ifacePanel.add(ifaceTypeBox);
        ifacePanel.add(ifaceNumField);

        form.add(new JLabel("IP地址:"));
        form.add(ipField);
        form.add(new JLabel("掩码:"));
        form.add(maskField);
        form.add(new JLabel("接口:"));
        form.add(ifacePanel);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String ip = ipField.getText().trim();
            String mask = maskField.getText().trim();
            String ifaceType = (String) ifaceTypeBox.getSelectedItem();
            String ifaceNum = ifaceNumField.getText().trim();
            String iface = ifaceType + ifaceNum;
            if (ip.isEmpty() || mask.isEmpty() || ifaceNum.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写IP、掩码和接口号", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (data == null) {
                tableModel.addRow(new Object[]{ip, mask, iface});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(ip, row, 0);
                tableModel.setValueAt(mask, row, 1);
                tableModel.setValueAt(iface, row, 2);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的IP", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[3];
        for (int i = 0; i < 3; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的IP", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选IP？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择IP条目");
            return;
        }
        String ip = tableModel.getValueAt(row, 0).toString();
        String mask = tableModel.getValueAt(row, 1).toString();
        String iface = tableModel.getValueAt(row, 2).toString();

        // 端口类型映射
        String mappedIface = iface;
        if (iface.startsWith("XGE口")) {
            mappedIface = "XGigabitethernet" + iface.substring(3);
        } else if (iface.startsWith("GE口")) {
            mappedIface = "Gigabitethernet" + iface.substring(2);
        } else if (iface.startsWith("FE口")) {
            mappedIface = "FastEthernet" + iface.substring(2);
        } else if (iface.startsWith("E口")) {
            mappedIface = "Ethernet" + iface.substring(2);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("interface ").append(mappedIface).append("\n");
        sb.append(" ip address ").append(ip).append(" ").append(mask).append("\n");
        sb.append("quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * VLAN配置界面（UI美化版）
 * 主要功能：
 * 1. 支持VLAN的增删改查
 * 2. 支持成员端口填写：端口类型（选择），端口号（填写）
 *    端口类型：XGE口（XGigabitethernet）、GE口（Gigabitethernet）、FE口（FastEthernet）、E口（Ethernet）
 * 3. 命令生成预览
 */
public class VlanPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public VlanPanel(String vlan) {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        JLabel title = new JLabel("VLAN配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(new Color(37, 81, 166));
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        // 表格部分
        String[] columns = {"VLAN ID", "名称", "成员端口"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        table.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(208, 227, 255));
        table.setSelectionForeground(new Color(25, 52, 105));
        table.setGridColor(new Color(220, 230, 240));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(69, 149, 236), 1, true));
        add(tableScroll, BorderLayout.CENTER);

        // 右侧命令预览
        cmdPreviewArea = new JTextArea(10, 32);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        cmdPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        cmdPreviewArea.setBorder(BorderFactory.createTitledBorder("命令预览"));
        cmdPreviewArea.setBackground(new Color(240, 247, 255));
        add(new JScrollPane(cmdPreviewArea), BorderLayout.EAST);

        // 按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btnPanel.setOpaque(false);
        addBtn = createButton("新增VLAN", new Color(69, 149, 236), Color.WHITE);
        editBtn = createButton("编辑VLAN", new Color(37, 81, 166), Color.WHITE);
        delBtn = createButton("删除VLAN", new Color(240, 61, 70), Color.WHITE);
        previewBtn = createButton("命令预览", new Color(37, 166, 81), Color.WHITE);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

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
        tableModel.addRow(new Object[]{"10", "办公网", "GE口1/0/1"});
        tableModel.addRow(new Object[]{"20", "服务器", "XGE口1/0/5"});
        tableModel.addRow(new Object[]{"30", "普通终端", "E口0/0/2"});
        tableModel.addRow(new Object[]{"40", "旧设备", "FE口0/1/1"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "新增VLAN" : "编辑VLAN", true);
        dialog.setSize(430, 220);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField idField = new JTextField(data == null ? "" : data[0].toString());
        JTextField nameField = new JTextField(data == null ? "" : data[1].toString());

        // 端口类型下拉+端口号输入
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JComboBox<String> portTypeBox = new JComboBox<>(new String[]{"XGE口", "GE口", "FE口", "E口"});
        JTextField portNumField = new JTextField(10);
        if (data != null && data[2] != null) {
            String port = data[2].toString();
            if (port.startsWith("XGE口")) {
                portTypeBox.setSelectedItem("XGE口");
                portNumField.setText(port.substring(3));
            } else if (port.startsWith("GE口")) {
                portTypeBox.setSelectedItem("GE口");
                portNumField.setText(port.substring(2));
            } else if (port.startsWith("FE口")) {
                portTypeBox.setSelectedItem("FE口");
                portNumField.setText(port.substring(2));
            } else if (port.startsWith("E口")) {
                portTypeBox.setSelectedItem("E口");
                portNumField.setText(port.substring(2));
            } else {
                portNumField.setText(port);
            }
        }
        portPanel.add(portTypeBox);
        portPanel.add(portNumField);

        form.add(new JLabel("VLAN ID:"));
        form.add(idField);
        form.add(new JLabel("VLAN名称:"));
        form.add(nameField);
        form.add(new JLabel("成员端口:"));
        form.add(portPanel);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = createButton("确定", new Color(37, 81, 166), Color.WHITE);
        JButton cancelBtn = createButton("取消", new Color(180, 180, 180), Color.WHITE);
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String portType = (String) portTypeBox.getSelectedItem();
            String portNum = portNumField.getText().trim();
            String port = portType + portNum;
            if (id.isEmpty() || portNum.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写VLAN ID和端口号", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int vlanId = Integer.parseInt(id);
                if (vlanId < 1 || vlanId > 4094) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "VLAN ID须为1~4094的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                tableModel.addRow(new Object[]{id, name, port});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(id, row, 0);
                tableModel.setValueAt(name, row, 1);
                tableModel.setValueAt(port, row, 2);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的VLAN", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[3];
        for (int i = 0; i < 3; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的VLAN", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选VLAN？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择VLAN条目");
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        String name = tableModel.getValueAt(row, 1).toString();
        String port = tableModel.getValueAt(row, 2).toString();

        // 端口类型映射
        String mappedPort = port;
        if (port.startsWith("XGE口")) {
            mappedPort = "XGigabitethernet" + port.substring(3);
        } else if (port.startsWith("GE口")) {
            mappedPort = "Gigabitethernet" + port.substring(2);
        } else if (port.startsWith("FE口")) {
            mappedPort = "FastEthernet" + port.substring(2);
        } else if (port.startsWith("E口")) {
            mappedPort = "Ethernet" + port.substring(2);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("vlan ").append(id).append("\n");
        if (!name.isEmpty()) sb.append(" description ").append(name).append("\n");
        sb.append(" port ").append(mappedPort).append("\n");
        sb.append("quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
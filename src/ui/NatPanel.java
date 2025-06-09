package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * NAT（网络地址转换）配置界面
 * 主要功能：
 * 1. 支持NAT策略（源、目的、类型、接口等）管理
 * 2. 支持条目的新增、编辑、删除
 * 3. 命令生成预览
 */
public class NatPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public NatPanel(String nat) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("NAT（网络地址转换）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 表格区
        String[] columns = {"策略名称", "类型", "源地址", "目的地址", "接口", "描述"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("NAT策略列表"));
        add(tableScroll, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("新增策略");
        editBtn = new JButton("编辑策略");
        delBtn = new JButton("删除策略");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(10, 40);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        JScrollPane cmdScroll = new JScrollPane(cmdPreviewArea);
        cmdScroll.setBorder(BorderFactory.createTitledBorder("命令生成预览"));
        add(cmdScroll, BorderLayout.EAST);

        // 测试数据
        addTestData();

        // 事件
        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        previewBtn.addActionListener(e -> previewCmd());

        // 双击编辑
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editSelected();
                }
            }
        });
    }

    private void addTestData() {
        tableModel.addRow(new Object[]{"nat_out1", "源NAT", "192.168.10.0/24", "any", "GigabitEthernet0/0/1", "办公区上网"});
        tableModel.addRow(new Object[]{"nat_dmz", "目的NAT", "any", "10.1.1.8", "GigabitEthernet0/0/2", "DMZ服务器"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "新增NAT策略" : "编辑NAT策略", true);
        dialog.setSize(480, 290);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField nameField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"源NAT", "目的NAT"});
        if (data != null && data[1] != null) typeBox.setSelectedItem(data[1].toString());
        JTextField srcField = new JTextField(data == null ? "" : data[2].toString());
        JTextField dstField = new JTextField(data == null ? "" : data[3].toString());
        JTextField ifaceField = new JTextField(data == null ? "" : data[4].toString());
        JTextField descField = new JTextField(data == null ? "" : data[5].toString());

        form.add(new JLabel("策略名称:"));
        form.add(nameField);
        form.add(new JLabel("类型:"));
        form.add(typeBox);
        form.add(new JLabel("源地址:"));
        form.add(srcField);
        form.add(new JLabel("目的地址:"));
        form.add(dstField);
        form.add(new JLabel("接口:"));
        form.add(ifaceField);
        form.add(new JLabel("描述:"));
        form.add(descField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String src = srcField.getText().trim();
            String dst = dstField.getText().trim();
            String iface = ifaceField.getText().trim();
            String desc = descField.getText().trim();

            if (name.isEmpty() || type.isEmpty() || iface.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写策略名称、类型和接口", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (data == null) {
                tableModel.addRow(new Object[]{name, type, src, dst, iface, desc});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(name, row, 0);
                tableModel.setValueAt(type, row, 1);
                tableModel.setValueAt(src, row, 2);
                tableModel.setValueAt(dst, row, 3);
                tableModel.setValueAt(iface, row, 4);
                tableModel.setValueAt(desc, row, 5);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的NAT策略", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[6];
        for (int i = 0; i < 6; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的NAT策略", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选NAT策略？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择NAT策略条目");
            return;
        }
        String name = tableModel.getValueAt(row, 0).toString();
        String type = tableModel.getValueAt(row, 1).toString();
        String src = tableModel.getValueAt(row, 2).toString();
        String dst = tableModel.getValueAt(row, 3).toString();
        String iface = tableModel.getValueAt(row, 4).toString();
        String desc = tableModel.getValueAt(row, 5).toString();

        StringBuilder sb = new StringBuilder();
        sb.append("nat ").append("源NAT".equals(type) ? "address-group " : "server ").append(name).append("\n");
        if (!desc.isEmpty()) sb.append(" description ").append(desc).append("\n");
        if ("源NAT".equals(type)) {
            sb.append(" rule 1 source-address ").append(src.isEmpty() ? "any" : src);
            sb.append(" outbound-interface ").append(iface);
            sb.append("\n");
        } else {
            sb.append(" rule 1 destination-address ").append(dst.isEmpty() ? "any" : dst);
            sb.append(" inbound-interface ").append(iface);
            sb.append("\n");
        }
        sb.append(" quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
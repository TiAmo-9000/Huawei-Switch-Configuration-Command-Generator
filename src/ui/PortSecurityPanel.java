package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * 端口安全（Port Security）配置界面
 * 主要功能：
 * 1. 显示和管理端口安全策略（端口、最大MAC、绑定MAC、违规动作）
 * 2. 支持添加、编辑、删除端口安全配置
 * 3. 支持批量端口配置
 * 4. 命令生成预览
 */
public class PortSecurityPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public PortSecurityPanel(String 端口安全) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("端口安全（Port Security）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 端口安全表格
        String[] columns = {"端口", "最大MAC数", "绑定MAC（逗号隔开）", "违规动作"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("端口安全策略"));
        add(tableScroll, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("添加配置");
        editBtn = new JButton("编辑配置");
        delBtn = new JButton("删除配置");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(8, 38);
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
        tableModel.addRow(new Object[]{"GigabitEthernet0/0/1", "2", "00e0-fc12-3456", "shutdown"});
        tableModel.addRow(new Object[]{"GigabitEthernet0/0/2", "1", "", "restrict"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加端口安全配置" : "编辑端口安全配置", true);
        dialog.setSize(410, 240);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField portField = new JTextField(data == null ? "" : data[0].toString());
        JTextField maxMacField = new JTextField(data == null ? "1" : data[1].toString());
        JTextField macsField = new JTextField(data == null ? "" : data[2].toString());
        JComboBox<String> actionBox = new JComboBox<>(new String[]{"shutdown", "restrict", "protect"});
        if (data != null && data[3] != null) actionBox.setSelectedItem(data[3].toString());

        form.add(new JLabel("端口:"));
        form.add(portField);
        form.add(new JLabel("最大MAC数:"));
        form.add(maxMacField);
        form.add(new JLabel("绑定MAC（逗号隔开）:"));
        form.add(macsField);
        form.add(new JLabel("违规动作:"));
        form.add(actionBox);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String port = portField.getText().trim();
            String maxMac = maxMacField.getText().trim();
            String macs = macsField.getText().trim();
            String action = (String) actionBox.getSelectedItem();

            if (port.isEmpty() || maxMac.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写端口和最大MAC数", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int n = Integer.parseInt(maxMac);
                if (n < 1 || n > 128) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "最大MAC数须为1~128的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                tableModel.addRow(new Object[]{port, maxMac, macs, action});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(port, row, 0);
                tableModel.setValueAt(maxMac, row, 1);
                tableModel.setValueAt(macs, row, 2);
                tableModel.setValueAt(action, row, 3);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的配置", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[4];
        for (int i = 0; i < 4; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的配置", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选端口安全配置？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择端口安全配置条目");
            return;
        }
        String port = tableModel.getValueAt(row, 0).toString();
        String maxMac = tableModel.getValueAt(row, 1).toString();
        String macs = tableModel.getValueAt(row, 2).toString();
        String action = tableModel.getValueAt(row, 3).toString();

        StringBuilder sb = new StringBuilder();
        sb.append("interface ").append(port).append("\n");
        sb.append(" port-security enable\n");
        sb.append(" port-security max-mac-num ").append(maxMac).append("\n");
        if (!macs.isEmpty()) {
            String[] macArr = macs.split(",");
            for (String mac : macArr) {
                String m = mac.trim();
                if (!m.isEmpty()) {
                    sb.append(" port-security mac-address ").append(m).append(" sticky\n");
                }
            }
        }
        if ("shutdown".equals(action))
            sb.append(" port-security violation shutdown\n");
        else if ("restrict".equals(action))
            sb.append(" port-security violation restrict\n");
        else if ("protect".equals(action))
            sb.append(" port-security violation protect\n");
        sb.append(" quit\n");

        cmdPreviewArea.setText(sb.toString());
    }
}
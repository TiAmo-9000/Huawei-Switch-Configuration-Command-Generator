package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * 路由配置界面
 * 主要功能：
 * 1. 支持静态路由和动态路由（RIP/OSPF/BGP）配置（可扩展）
 * 2. 显示所有路由条目
 * 3. 新增、编辑、删除路由配置
 * 4. 命令生成预览
 */
public class RoutePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public RoutePanel(String 路由配置) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("路由配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 表格区
        String[] columns = {"类型", "目的网络", "子网掩码", "下一跳", "协议参数"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("路由表"));
        add(tableScroll, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("新增路由");
        editBtn = new JButton("编辑路由");
        delBtn = new JButton("删除路由");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(8, 40);
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
        tableModel.addRow(new Object[]{"静态", "10.1.1.0", "255.255.255.0", "192.168.1.254", ""});
        tableModel.addRow(new Object[]{"RIP", "0.0.0.0", "0.0.0.0", "-", "version 2"});
        tableModel.addRow(new Object[]{"OSPF", "192.168.2.0", "255.255.255.0", "-", "area 0"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "新增路由" : "编辑路由", true);
        dialog.setSize(430, 265);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"静态", "RIP", "OSPF", "BGP"});
        if (data != null) typeBox.setSelectedItem(data[0]);
        JTextField destField = new JTextField(data == null ? "" : data[1].toString());
        JTextField maskField = new JTextField(data == null ? "" : data[2].toString());
        JTextField nextHopField = new JTextField(data == null ? "" : data[3].toString());
        JTextField paramField = new JTextField(data == null ? "" : data[4].toString());

        form.add(new JLabel("类型:"));
        form.add(typeBox);
        form.add(new JLabel("目的网络:"));
        form.add(destField);
        form.add(new JLabel("子网掩码:"));
        form.add(maskField);
        form.add(new JLabel("下一跳:"));
        form.add(nextHopField);
        form.add(new JLabel("协议参数:"));
        form.add(paramField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        // 动态启用下一跳输入框
        typeBox.addActionListener(e -> {
            String type = (String) typeBox.getSelectedItem();
            boolean enableNextHop = "静态".equals(type);
            nextHopField.setEnabled(enableNextHop);
            if (!enableNextHop) nextHopField.setText("-");
        });

        okBtn.addActionListener(e -> {
            String type = (String) typeBox.getSelectedItem();
            String dest = destField.getText().trim();
            String mask = maskField.getText().trim();
            String nextHop = nextHopField.getText().trim();
            String param = paramField.getText().trim();

            if (dest.isEmpty() || mask.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写目的网络和掩码", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("静态".equals(type) && (nextHop.isEmpty() || "-".equals(nextHop))) {
                JOptionPane.showMessageDialog(dialog, "静态路由请填写下一跳地址", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (data == null) {
                tableModel.addRow(new Object[]{type, dest, mask, nextHop, param});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(type, row, 0);
                tableModel.setValueAt(dest, row, 1);
                tableModel.setValueAt(mask, row, 2);
                tableModel.setValueAt(nextHop, row, 3);
                tableModel.setValueAt(param, row, 4);
            }
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的路由", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[5];
        for (int i = 0; i < 5; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的路由", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选路由？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择路由条目");
            return;
        }
        String type = tableModel.getValueAt(row, 0).toString();
        String dest = tableModel.getValueAt(row, 1).toString();
        String mask = tableModel.getValueAt(row, 2).toString();
        String nextHop = tableModel.getValueAt(row, 3).toString();
        String param = tableModel.getValueAt(row, 4).toString();

        StringBuilder sb = new StringBuilder();
        if ("静态".equals(type)) {
            sb.append("ip route-static ").append(dest).append(" ").append(mask).append(" ").append(nextHop).append("\n");
        } else if ("RIP".equals(type)) {
            sb.append("rip\n");
            sb.append(" version ").append(param.isEmpty() ? "2" : param).append("\n");
            sb.append(" network ").append(dest).append("\n");
            sb.append(" quit\n");
        } else if ("OSPF".equals(type)) {
            sb.append("ospf 1\n");
            sb.append(" area ").append(param.isEmpty() ? "0" : param).append("\n");
            sb.append(" network ").append(dest).append(" ").append(mask).append("\n");
            sb.append(" quit\n");
        } else if ("BGP".equals(type)) {
            sb.append("bgp 100\n");
            sb.append(" network ").append(dest).append(" mask ").append(mask).append("\n");
            if (!param.isEmpty()) sb.append(" ").append(param).append("\n");
            sb.append(" quit\n");
        }
        cmdPreviewArea.setText(sb.toString());
    }
}
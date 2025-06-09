package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * DHCP配置界面
 * 主要功能：
 * 1. 支持DHCP池管理（名称、网段、网关、掩码、DNS等）
 * 2. 支持池的添加、编辑、删除
 * 3. 命令生成预览
 */
public class DhcpPanel extends JPanel {
    private DefaultTableModel poolTableModel;
    private JTable poolTable;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public DhcpPanel(String dhcp) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("DHCP 配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // DHCP池表
        String[] columns = {"池名称", "网段", "掩码", "网关", "DNS", "租期(小时)"};
        poolTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        poolTable = new JTable(poolTableModel);
        poolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane poolScroll = new JScrollPane(poolTable);
        poolScroll.setBorder(BorderFactory.createTitledBorder("DHCP地址池"));

        // 按钮区
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("添加池");
        editBtn = new JButton("编辑池");
        delBtn = new JButton("删除池");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(10, 36);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        JScrollPane cmdScroll = new JScrollPane(cmdPreviewArea);
        cmdScroll.setBorder(BorderFactory.createTitledBorder("命令生成预览"));
        add(cmdScroll, BorderLayout.EAST);

        add(poolScroll, BorderLayout.CENTER);

        // 测试数据
        addTestData();

        // 事件
        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        previewBtn.addActionListener(e -> previewCmd());

        // 双击编辑
        poolTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && poolTable.getSelectedRow() != -1) {
                    editSelected();
                }
            }
        });
    }

    private void addTestData() {
        poolTableModel.addRow(new Object[]{"office", "192.168.10.0", "255.255.255.0", "192.168.10.1", "8.8.8.8", "24"});
        poolTableModel.addRow(new Object[]{"lab", "10.0.0.0", "255.255.255.0", "10.0.0.254", "223.5.5.5", "12"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加DHCP池" : "编辑DHCP池", true);
        dialog.setSize(420, 270);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField nameField = new JTextField(data == null ? "" : data[0].toString());
        JTextField segmentField = new JTextField(data == null ? "" : data[1].toString());
        JTextField maskField = new JTextField(data == null ? "" : data[2].toString());
        JTextField gwField = new JTextField(data == null ? "" : data[3].toString());
        JTextField dnsField = new JTextField(data == null ? "" : data[4].toString());
        JTextField leaseField = new JTextField(data == null ? "24" : data[5].toString());

        form.add(new JLabel("池名称:"));
        form.add(nameField);
        form.add(new JLabel("网段:"));
        form.add(segmentField);
        form.add(new JLabel("掩码:"));
        form.add(maskField);
        form.add(new JLabel("网关:"));
        form.add(gwField);
        form.add(new JLabel("DNS:"));
        form.add(dnsField);
        form.add(new JLabel("租期(小时):"));
        form.add(leaseField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel dialogBtnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        dialogBtnPanel.add(okBtn);
        dialogBtnPanel.add(cancelBtn);
        dialog.add(dialogBtnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String segment = segmentField.getText().trim();
            String mask = maskField.getText().trim();
            String gw = gwField.getText().trim();
            String dns = dnsField.getText().trim();
            String lease = leaseField.getText().trim();

            if (name.isEmpty() || segment.isEmpty() || mask.isEmpty() || gw.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写池名称、网段、掩码、网关", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int leaseHour = Integer.parseInt(lease);
                if (leaseHour < 1 || leaseHour > 168) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "租期须为1~168小时的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                poolTableModel.addRow(new Object[]{name, segment, mask, gw, dns, lease});
            } else {
                int row = poolTable.getSelectedRow();
                poolTableModel.setValueAt(name, row, 0);
                poolTableModel.setValueAt(segment, row, 1);
                poolTableModel.setValueAt(mask, row, 2);
                poolTableModel.setValueAt(gw, row, 3);
                poolTableModel.setValueAt(dns, row, 4);
                poolTableModel.setValueAt(lease, row, 5);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = poolTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的池", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[6];
        for (int i = 0; i < 6; i++) data[i] = poolTableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = poolTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的池", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选DHCP池？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            poolTableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = poolTable.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择DHCP池条目");
            return;
        }
        String name = poolTableModel.getValueAt(row, 0).toString();
        String segment = poolTableModel.getValueAt(row, 1).toString();
        String mask = poolTableModel.getValueAt(row, 2).toString();
        String gw = poolTableModel.getValueAt(row, 3).toString();
        String dns = poolTableModel.getValueAt(row, 4).toString();
        String lease = poolTableModel.getValueAt(row, 5).toString();

        StringBuilder sb = new StringBuilder();
        sb.append("dhcp enable\n");
        sb.append("ip pool ").append(name).append("\n");
        sb.append(" network ").append(segment).append(" ").append(mask).append("\n");
        sb.append(" gateway-list ").append(gw).append("\n");
        if (!dns.isEmpty()) {
            sb.append(" dns-list ").append(dns).append("\n");
        }
        sb.append(" lease day 0 hour ").append(lease).append("\n");
        sb.append(" quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
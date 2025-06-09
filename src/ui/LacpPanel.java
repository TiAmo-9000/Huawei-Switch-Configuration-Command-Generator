package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * 端口聚合（LACP/静态）配置界面
 * 主要功能：
 * 1. 管理聚合组（编号、模式、描述）
 * 2. 添加、编辑、删除聚合组及成员端口
 * 3. 设置聚合模式（LACP/静态）、负载均衡方式
 * 4. 命令生成预览
 */
public class LacpPanel extends JPanel {
    private DefaultTableModel groupTableModel;
    private JTable groupTable;
    private JButton addGroupBtn, editGroupBtn, delGroupBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public LacpPanel(String 端口聚合) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("端口聚合（LACP/静态）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 聚合组表
        String[] columns = {"聚合组号", "聚合模式", "成员端口", "负载均衡", "描述"};
        groupTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        groupTable = new JTable(groupTableModel);
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(groupTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("聚合组信息"));
        add(tableScroll, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        addGroupBtn = new JButton("新增聚合组");
        editGroupBtn = new JButton("编辑聚合组");
        delGroupBtn = new JButton("删除聚合组");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addGroupBtn);
        btnPanel.add(editGroupBtn);
        btnPanel.add(delGroupBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(8, 42);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        JScrollPane cmdScroll = new JScrollPane(cmdPreviewArea);
        cmdScroll.setBorder(BorderFactory.createTitledBorder("命令生成预览"));
        add(cmdScroll, BorderLayout.EAST);

        // 测试数据
        addTestData();

        // 事件
        addGroupBtn.addActionListener(e -> showGroupDialog(null));
        editGroupBtn.addActionListener(e -> editSelectedGroup());
        delGroupBtn.addActionListener(e -> deleteSelectedGroup());
        previewBtn.addActionListener(e -> previewSelectedGroupCmd());

        // 双击编辑
        groupTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && groupTable.getSelectedRow() != -1) {
                    editSelectedGroup();
                }
            }
        });
    }

    private void addTestData() {
        groupTableModel.addRow(new Object[]{"1", "LACP", "GigabitEthernet0/0/1, GigabitEthernet0/0/2", "src-dst-mac", "汇聚上行链路"});
        groupTableModel.addRow(new Object[]{"2", "静态", "GigabitEthernet0/0/3, GigabitEthernet0/0/4", "src-mac", "服务器专用"});
    }

    private void showGroupDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "新增聚合组" : "编辑聚合组", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField groupIdField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> modeBox = new JComboBox<>(new String[]{"LACP", "静态"});
        if (data != null && data[1] != null) modeBox.setSelectedItem(data[1].toString());
        JTextField membersField = new JTextField(data == null ? "" : data[2].toString());
        JComboBox<String> lbBox = new JComboBox<>(new String[]{"src-mac", "dst-mac", "src-dst-mac", "src-ip", "dst-ip", "src-dst-ip"});
        if (data != null && data[3] != null) lbBox.setSelectedItem(data[3].toString());
        JTextField descField = new JTextField(data == null ? "" : data[4].toString());

        form.add(new JLabel("聚合组号:"));
        form.add(groupIdField);
        form.add(new JLabel("聚合模式:"));
        form.add(modeBox);
        form.add(new JLabel("成员端口(逗号分隔):"));
        form.add(membersField);
        form.add(new JLabel("负载均衡方式:"));
        form.add(lbBox);
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
            String groupId = groupIdField.getText().trim();
            String mode = (String) modeBox.getSelectedItem();
            String members = membersField.getText().trim();
            String lb = (String) lbBox.getSelectedItem();
            String desc = descField.getText().trim();

            if (groupId.isEmpty() || members.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "聚合组号和成员端口不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int gid = Integer.parseInt(groupId);
                if (gid < 1 || gid > 64) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "聚合组号应为1~64的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                groupTableModel.addRow(new Object[]{groupId, mode, members, lb, desc});
            } else {
                int row = groupTable.getSelectedRow();
                groupTableModel.setValueAt(groupId, row, 0);
                groupTableModel.setValueAt(mode, row, 1);
                groupTableModel.setValueAt(members, row, 2);
                groupTableModel.setValueAt(lb, row, 3);
                groupTableModel.setValueAt(desc, row, 4);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedGroup() {
        int row = groupTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的聚合组", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[5];
        for (int i = 0; i < 5; i++) data[i] = groupTableModel.getValueAt(row, i);
        showGroupDialog(data);
    }

    private void deleteSelectedGroup() {
        int row = groupTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的聚合组", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选聚合组？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            groupTableModel.removeRow(row);
        }
    }

    private void previewSelectedGroupCmd() {
        int row = groupTable.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择聚合组条目");
            return;
        }
        String groupId = groupTableModel.getValueAt(row, 0).toString();
        String mode = groupTableModel.getValueAt(row, 1).toString();
        String members = groupTableModel.getValueAt(row, 2).toString();
        String lb = groupTableModel.getValueAt(row, 3).toString();
        String desc = groupTableModel.getValueAt(row, 4).toString();

        StringBuilder sb = new StringBuilder();
        // 配置聚合接口
        sb.append("interface Eth-Trunk").append(groupId).append("\n");
        if (!desc.isEmpty()) sb.append(" description ").append(desc).append("\n");
        if ("LACP".equals(mode)) sb.append(" mode lacp\n");
        else sb.append(" mode manual\n");
        sb.append(" load-balance ").append(lb).append("\n");
        sb.append(" quit\n");

        // 配置成员端口
        if (!members.isEmpty()) {
            String[] portArr = members.split(",");
            for (String port : portArr) {
                String p = port.trim();
                if (!p.isEmpty()) {
                    sb.append("interface ").append(p).append("\n");
                    sb.append(" eth-trunk ").append(groupId).append("\n");
                    sb.append(" quit\n");
                }
            }
        }

        cmdPreviewArea.setText(sb.toString());
    }
}
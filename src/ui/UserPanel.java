package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * 用户管理界面
 * 主要功能：
 * 1. 管理本地用户账号（用户名、权限、密码、备注）
 * 2. 增加、编辑、删除用户
 * 3. 命令生成预览
 */
public class UserPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public UserPanel(String 用户管理) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("用户管理", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 表格区
        String[] columns = {"用户名", "权限级别", "密码", "备注"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("本地用户账号"));
        add(tableScroll, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("添加用户");
        editBtn = new JButton("编辑用户");
        delBtn = new JButton("删除用户");
        previewBtn = new JButton("命令预览");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(previewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(8, 36);
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
        tableModel.addRow(new Object[]{"admin", "15", "admin@123", "超级管理员"});
        tableModel.addRow(new Object[]{"netops", "3", "netops@123", "网络运维"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加用户" : "编辑用户", true);
        dialog.setSize(410, 240);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField userField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> levelBox = new JComboBox<>(new String[]{"15(管理员)", "3(操作员)", "2(监控员)", "1(访客)"});
        if (data != null && data[1] != null) {
            String l = data[1].toString();
            switch (l) {
                case "15": levelBox.setSelectedIndex(0); break;
                case "3":  levelBox.setSelectedIndex(1); break;
                case "2":  levelBox.setSelectedIndex(2); break;
                case "1":  levelBox.setSelectedIndex(3); break;
                default:   levelBox.setSelectedIndex(0);
            }
        }
        JTextField pwdField = new JTextField(data == null ? "" : data[2].toString());
        JTextField noteField = new JTextField(data == null ? "" : data[3].toString());

        form.add(new JLabel("用户名:"));
        form.add(userField);
        form.add(new JLabel("权限级别:"));
        form.add(levelBox);
        form.add(new JLabel("密码:"));
        form.add(pwdField);
        form.add(new JLabel("备注:"));
        form.add(noteField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String level = (String) levelBox.getSelectedItem();
            String pwd = pwdField.getText().trim();
            String note = noteField.getText().trim();

            if (username.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写用户名和密码", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String levelNum = "15";
            if (level.startsWith("15")) levelNum = "15";
            else if (level.startsWith("3")) levelNum = "3";
            else if (level.startsWith("2")) levelNum = "2";
            else if (level.startsWith("1")) levelNum = "1";

            if (data == null) {
                tableModel.addRow(new Object[]{username, levelNum, pwd, note});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(username, row, 0);
                tableModel.setValueAt(levelNum, row, 1);
                tableModel.setValueAt(pwd, row, 2);
                tableModel.setValueAt(note, row, 3);
            }
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[4];
        for (int i = 0; i < 4; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选用户？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择用户条目");
            return;
        }
        String username = tableModel.getValueAt(row, 0).toString();
        String level = tableModel.getValueAt(row, 1).toString();
        String pwd = tableModel.getValueAt(row, 2).toString();
        // String note = tableModel.getValueAt(row, 3).toString();

        StringBuilder sb = new StringBuilder();
        sb.append("local-user ").append(username).append("\n");
        sb.append(" password irreversible-cipher ").append(pwd).append("\n");
        sb.append(" privilege level ").append(level).append("\n");
        sb.append(" service-type ssh telnet terminal\n");
        sb.append(" quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * SNMP配置界面
 * 主要功能：
 * 1. 展示和管理SNMP社区、Trap服务器等配置
 * 2. 支持社区字符串、权限、Trap服务器的增删改
 * 3. 命令生成预览
 */
public class SnmpPanel extends JPanel {
    private DefaultTableModel commuTableModel;
    private JTable commuTable;
    private JButton addCommuBtn, editCommuBtn, delCommuBtn;

    private DefaultTableModel trapTableModel;
    private JTable trapTable;
    private JButton addTrapBtn, editTrapBtn, delTrapBtn;

    private JButton previewBtn;
    private JTextArea cmdPreviewArea;

    public SnmpPanel(String snmp) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("SNMP 配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // SNMP社区表
        String[] commuCols = {"社区名称", "权限(RO/RW)", "访问源"};
        commuTableModel = new DefaultTableModel(commuCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        commuTable = new JTable(commuTableModel);
        commuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane commuScroll = new JScrollPane(commuTable);
        commuScroll.setBorder(BorderFactory.createTitledBorder("SNMP社区配置"));

        // Trap服务器表
        String[] trapCols = {"服务器地址", "Trap类型", "版本"};
        trapTableModel = new DefaultTableModel(trapCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        trapTable = new JTable(trapTableModel);
        trapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane trapScroll = new JScrollPane(trapTable);
        trapScroll.setBorder(BorderFactory.createTitledBorder("SNMP Trap服务器"));

        // 按钮区
        JPanel commuBtnPanel = new JPanel();
        addCommuBtn = new JButton("添加社区");
        editCommuBtn = new JButton("编辑社区");
        delCommuBtn = new JButton("删除社区");
        commuBtnPanel.add(addCommuBtn);
        commuBtnPanel.add(editCommuBtn);
        commuBtnPanel.add(delCommuBtn);

        JPanel trapBtnPanel = new JPanel();
        addTrapBtn = new JButton("添加Trap");
        editTrapBtn = new JButton("编辑Trap");
        delTrapBtn = new JButton("删除Trap");
        trapBtnPanel.add(addTrapBtn);
        trapBtnPanel.add(editTrapBtn);
        trapBtnPanel.add(delTrapBtn);

        // 左侧（社区区）和中间（Trap区）布局
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(commuScroll, BorderLayout.CENTER);
        leftPanel.add(commuBtnPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(trapScroll, BorderLayout.CENTER);
        centerPanel.add(trapBtnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(12, 32);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        JScrollPane cmdScroll = new JScrollPane(cmdPreviewArea);
        cmdScroll.setBorder(BorderFactory.createTitledBorder("命令生成预览"));

        // 主体布局
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(leftPanel);
        mainPanel.add(centerPanel);
        add(mainPanel, BorderLayout.CENTER);

        // 底部命令按钮
        JPanel bottomPanel = new JPanel();
        previewBtn = new JButton("命令预览");
        bottomPanel.add(previewBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        add(cmdScroll, BorderLayout.EAST);

        // 测试数据
        addTestData();

        // 事件
        addCommuBtn.addActionListener(e -> showCommuDialog(null));
        editCommuBtn.addActionListener(e -> editSelectedCommu());
        delCommuBtn.addActionListener(e -> deleteSelectedCommu());

        addTrapBtn.addActionListener(e -> showTrapDialog(null));
        editTrapBtn.addActionListener(e -> editSelectedTrap());
        delTrapBtn.addActionListener(e -> deleteSelectedTrap());

        previewBtn.addActionListener(e -> previewCmd());

        // 双击编辑
        commuTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && commuTable.getSelectedRow() != -1) {
                    editSelectedCommu();
                }
            }
        });
        trapTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && trapTable.getSelectedRow() != -1) {
                    editSelectedTrap();
                }
            }
        });
    }

    private void addTestData() {
        commuTableModel.addRow(new Object[]{"public", "RO", "192.168.1.0 255.255.255.0"});
        commuTableModel.addRow(new Object[]{"private", "RW", "any"});
        trapTableModel.addRow(new Object[]{"192.168.1.100", "inform", "v2c"});
        trapTableModel.addRow(new Object[]{"192.168.1.101", "trap", "v3"});
    }

    private void showCommuDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加社区" : "编辑社区", true);
        dialog.setSize(350, 210);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField nameField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> permBox = new JComboBox<>(new String[]{"RO", "RW"});
        if (data != null && data[1] != null) permBox.setSelectedItem(data[1].toString());
        JTextField srcField = new JTextField(data == null ? "" : data[2].toString());

        form.add(new JLabel("社区名称:"));
        form.add(nameField);
        form.add(new JLabel("权限:"));
        form.add(permBox);
        form.add(new JLabel("访问源:"));
        form.add(srcField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String perm = (String) permBox.getSelectedItem();
            String src = srcField.getText().trim();
            if (name.isEmpty() || perm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写社区名称和权限", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (src.isEmpty()) src = "any";
            if (data == null) {
                commuTableModel.addRow(new Object[]{name, perm, src});
            } else {
                int row = commuTable.getSelectedRow();
                commuTableModel.setValueAt(name, row, 0);
                commuTableModel.setValueAt(perm, row, 1);
                commuTableModel.setValueAt(src, row, 2);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedCommu() {
        int row = commuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的社区", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[3];
        for (int i = 0; i < 3; i++) data[i] = commuTableModel.getValueAt(row, i);
        showCommuDialog(data);
    }

    private void deleteSelectedCommu() {
        int row = commuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的社区", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选社区？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            commuTableModel.removeRow(row);
        }
    }

    private void showTrapDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加Trap服务器" : "编辑Trap服务器", true);
        dialog.setSize(370, 210);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField addrField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"trap", "inform"});
        if (data != null && data[1] != null) typeBox.setSelectedItem(data[1].toString());
        JComboBox<String> verBox = new JComboBox<>(new String[]{"v1", "v2c", "v3"});
        if (data != null && data[2] != null) verBox.setSelectedItem(data[2].toString());

        form.add(new JLabel("服务器地址:"));
        form.add(addrField);
        form.add(new JLabel("Trap类型:"));
        form.add(typeBox);
        form.add(new JLabel("SNMP版本:"));
        form.add(verBox);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String addr = addrField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String ver = (String) verBox.getSelectedItem();
            if (addr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入Trap服务器地址", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (data == null) {
                trapTableModel.addRow(new Object[]{addr, type, ver});
            } else {
                int row = trapTable.getSelectedRow();
                trapTableModel.setValueAt(addr, row, 0);
                trapTableModel.setValueAt(type, row, 1);
                trapTableModel.setValueAt(ver, row, 2);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedTrap() {
        int row = trapTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的Trap服务器", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[3];
        for (int i = 0; i < 3; i++) data[i] = trapTableModel.getValueAt(row, i);
        showTrapDialog(data);
    }

    private void deleteSelectedTrap() {
        int row = trapTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的Trap服务器", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选Trap服务器？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            trapTableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        StringBuilder sb = new StringBuilder();
        // 社区配置
        for (int i = 0; i < commuTableModel.getRowCount(); i++) {
            String name = commuTableModel.getValueAt(i, 0).toString();
            String perm = commuTableModel.getValueAt(i, 1).toString();
            String src = commuTableModel.getValueAt(i, 2).toString();
            sb.append("snmp-agent community ").append(perm.toLowerCase()).append(" ").append(name);
            if (!"any".equals(src)) sb.append(" source ").append(src);
            sb.append("\n");
        }
        // Trap配置
        for (int i = 0; i < trapTableModel.getRowCount(); i++) {
            String addr = trapTableModel.getValueAt(i, 0).toString();
            String type = trapTableModel.getValueAt(i, 1).toString();
            String ver = trapTableModel.getValueAt(i, 2).toString();
            sb.append("snmp-agent target-host ").append(addr)
                    .append(" params securityname public")
                    .append(" ").append(type).append(" version-").append(ver).append("\n");
        }
        cmdPreviewArea.setText(sb.toString());
    }
}
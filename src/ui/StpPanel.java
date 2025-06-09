package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * 生成树协议（STP/RSTP/MSTP）配置界面（美化版）
 * 主要功能：
 * 1. 显示和管理STP实例（类型、优先级、状态）
 * 2. 支持启用/关闭STP、切换模式、设置全局和端口参数
 * 3. 一键生成相关华为设备配置命令
 */
public class StpPanel extends JPanel {
    private JComboBox<String> stpModeBox;
    private JCheckBox stpEnableBox;
    private JTextField bridgePriorityField;
    private DefaultTableModel portTableModel;
    private JTable portTable;
    private JButton addPortBtn, delPortBtn, applyBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public StpPanel(String 生成树) {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        // 顶部标题
        JLabel title = new JLabel("生成树协议（STP/RSTP/MSTP）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(new Color(37, 81, 166));
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // 全局配置区
        JPanel stpConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 8));
        stpConfigPanel.setBackground(new Color(240, 247, 255));
        stpEnableBox = new JCheckBox("启用STP", true);
        stpEnableBox.setFont(new Font("微软雅黑", Font.BOLD, 15));
        stpModeBox = new JComboBox<>(new String[]{"STP", "RSTP", "MSTP"});
        stpModeBox.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        bridgePriorityField = new JTextField("32768", 6);
        bridgePriorityField.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        stpConfigPanel.add(stpEnableBox);
        stpConfigPanel.add(new JLabel("模式："));
        stpConfigPanel.add(stpModeBox);
        stpConfigPanel.add(new JLabel("桥优先级："));
        stpConfigPanel.add(bridgePriorityField);

        add(stpConfigPanel, BorderLayout.WEST);

        // 端口参数表
        String[] columns = {"端口名", "端口优先级", "边缘端口", "端口状态"};
        portTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        portTable = new JTable(portTableModel);
        portTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        portTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        portTable.setRowHeight(26);
        portTable.setSelectionBackground(new Color(208, 227, 255));
        portTable.setGridColor(new Color(220, 230, 240));
        portTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(portTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("端口STP参数设置"));
        tableScroll.setPreferredSize(new Dimension(480, 180));

        // 按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        btnPanel.setOpaque(false);
        addPortBtn = createButton("添加端口", new Color(69, 149, 236), Color.WHITE);
        delPortBtn = createButton("删除端口", new Color(240, 61, 70), Color.WHITE);
        applyBtn = createButton("应用配置", new Color(37, 81, 166), Color.WHITE);
        previewBtn = createButton("命令预览", new Color(37, 166, 81), Color.WHITE);
        btnPanel.add(addPortBtn);
        btnPanel.add(delPortBtn);
        btnPanel.add(applyBtn);
        btnPanel.add(previewBtn);

        // 命令预览区
        cmdPreviewArea = new JTextArea(10, 38);
        cmdPreviewArea.setEditable(false);
        cmdPreviewArea.setLineWrap(true);
        cmdPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        JScrollPane cmdScroll = new JScrollPane(cmdPreviewArea);
        cmdScroll.setBorder(BorderFactory.createTitledBorder("命令生成预览"));

        // 主体布局
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(248, 250, 252));
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(cmdScroll, BorderLayout.EAST);

        // 测试数据
        addTestPorts();

        // 事件
        addPortBtn.addActionListener(e -> showPortDialog(null));
        delPortBtn.addActionListener(e -> deleteSelectedPort());
        previewBtn.addActionListener(e -> previewStpCmd());
        applyBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "配置已应用（演示）", "提示", JOptionPane.INFORMATION_MESSAGE));

        portTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && portTable.getSelectedRow() != -1) {
                    editSelectedPort();
                }
            }
        });
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
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

    private void addTestPorts() {
        portTableModel.addRow(new Object[]{"GigabitEthernet0/0/1", "128", "是", "启用"});
        portTableModel.addRow(new Object[]{"GigabitEthernet0/0/2", "128", "否", "启用"});
        portTableModel.addRow(new Object[]{"GigabitEthernet0/0/3", "128", "否", "禁用"});
    }

    private void showPortDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加端口" : "编辑端口", true);
        dialog.setSize(320, 230);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField portNameField = new JTextField(data == null ? "" : data[0].toString());
        JTextField priField = new JTextField(data == null ? "128" : data[1].toString());
        JComboBox<String> edgeBox = new JComboBox<>(new String[]{"是", "否"});
        if (data != null && "是".equals(data[2])) edgeBox.setSelectedIndex(0);
        else edgeBox.setSelectedIndex(1);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"启用", "禁用"});
        if (data != null && "启用".equals(data[3])) statusBox.setSelectedIndex(0);
        else statusBox.setSelectedIndex(1);

        form.add(new JLabel("端口名:"));
        form.add(portNameField);
        form.add(new JLabel("端口优先级:"));
        form.add(priField);
        form.add(new JLabel("边缘端口:"));
        form.add(edgeBox);
        form.add(new JLabel("端口状态:"));
        form.add(statusBox);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton okBtn = createButton("确定", new Color(37, 81, 166), Color.WHITE);
        JButton cancelBtn = createButton("取消", new Color(180, 180, 180), Color.WHITE);
        btns.add(okBtn);
        btns.add(cancelBtn);
        dialog.add(btns, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String port = portNameField.getText().trim();
            String pri = priField.getText().trim();
            String edge = (String) edgeBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();
            if (port.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "端口名不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int p = Integer.parseInt(pri);
                if (p < 0 || p > 240 || p % 16 != 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "端口优先级需为0~240之间16的倍数", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                portTableModel.addRow(new Object[]{port, pri, edge, status});
            } else {
                int row = portTable.getSelectedRow();
                portTableModel.setValueAt(port, row, 0);
                portTableModel.setValueAt(pri, row, 1);
                portTableModel.setValueAt(edge, row, 2);
                portTableModel.setValueAt(status, row, 3);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedPort() {
        int row = portTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择端口", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[4];
        for (int i = 0; i < 4; i++) data[i] = portTableModel.getValueAt(row, i);
        showPortDialog(data);
    }

    private void deleteSelectedPort() {
        int row = portTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的端口", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选端口？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            portTableModel.removeRow(row);
        }
    }

    private void previewStpCmd() {
        StringBuilder sb = new StringBuilder();
        boolean stpEnable = stpEnableBox.isSelected();
        String mode = (String) stpModeBox.getSelectedItem();
        String pri = bridgePriorityField.getText().trim();

        if (!stpEnable) {
            sb.append("undo stp enable\n");
            cmdPreviewArea.setText(sb.toString());
            return;
        }

        sb.append("stp enable\n");
        if ("STP".equals(mode)) sb.append("stp mode stp\n");
        else if ("RSTP".equals(mode)) sb.append("stp mode rstp\n");
        else if ("MSTP".equals(mode)) sb.append("stp mode mstp\n");
        sb.append("stp priority ").append(pri).append("\n");

        for (int i = 0; i < portTableModel.getRowCount(); i++) {
            String port = portTableModel.getValueAt(i, 0).toString();
            String portPri = portTableModel.getValueAt(i, 1).toString();
            String edge = portTableModel.getValueAt(i, 2).toString();
            String status = portTableModel.getValueAt(i, 3).toString();

            sb.append("interface ").append(port).append("\n");
            sb.append(" stp port priority ").append(portPri).append("\n");
            if ("是".equals(edge)) sb.append(" stp edged-port enable\n");
            else sb.append(" stp edged-port disable\n");
            if ("启用".equals(status)) sb.append(" stp enable\n");
            else sb.append(" stp disable\n");
            sb.append(" quit\n");
        }
        cmdPreviewArea.setText(sb.toString());
    }
}
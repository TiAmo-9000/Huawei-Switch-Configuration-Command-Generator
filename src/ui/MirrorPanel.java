package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * 端口镜像（Port Mirroring）配置界面
 * 主要功能：
 * 1. 管理镜像会话（本地、远程），支持会话号、源端口、目的端口、方向、类型等配置
 * 2. 支持新增、编辑、删除镜像配置
 * 3. 命令生成预览
 */
public class MirrorPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addBtn, editBtn, delBtn, previewBtn;
    private JTextArea cmdPreviewArea;

    public MirrorPanel(String 端口镜像) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("端口镜像（Port Mirroring）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 表格区
        String[] columns = {"会话号", "镜像类型", "源端口", "方向", "目的端口", "描述"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("端口镜像会话"));
        add(tableScroll, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("新增镜像");
        editBtn = new JButton("编辑镜像");
        delBtn = new JButton("删除镜像");
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
        tableModel.addRow(new Object[]{"1", "本地", "GigabitEthernet0/0/1", "入+出", "GigabitEthernet0/0/10", "办公区监控"});
        tableModel.addRow(new Object[]{"2", "远程", "GigabitEthernet0/0/2", "入", "GigabitEthernet0/0/20", "远程镜像"});
    }

    private void showDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "新增端口镜像" : "编辑端口镜像", true);
        dialog.setSize(430, 270);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField sessionField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"本地", "远程"});
        if (data != null && data[1] != null) typeBox.setSelectedItem(data[1].toString());
        JTextField srcPortField = new JTextField(data == null ? "" : data[2].toString());
        JComboBox<String> directionBox = new JComboBox<>(new String[]{"入", "出", "入+出"});
        if (data != null && data[3] != null) directionBox.setSelectedItem(data[3].toString());
        JTextField dstPortField = new JTextField(data == null ? "" : data[4].toString());
        JTextField descField = new JTextField(data == null ? "" : data[5].toString());

        form.add(new JLabel("会话号:"));
        form.add(sessionField);
        form.add(new JLabel("镜像类型:"));
        form.add(typeBox);
        form.add(new JLabel("源端口:"));
        form.add(srcPortField);
        form.add(new JLabel("方向:"));
        form.add(directionBox);
        form.add(new JLabel("目的端口:"));
        form.add(dstPortField);
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
            String session = sessionField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String srcPort = srcPortField.getText().trim();
            String direction = (String) directionBox.getSelectedItem();
            String dstPort = dstPortField.getText().trim();
            String desc = descField.getText().trim();

            if (session.isEmpty() || srcPort.isEmpty() || dstPort.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写会话号、源端口和目的端口", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int sess = Integer.parseInt(session);
                if (sess < 1 || sess > 6) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "会话号须为1~6的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                tableModel.addRow(new Object[]{session, type, srcPort, direction, dstPort, desc});
            } else {
                int row = table.getSelectedRow();
                tableModel.setValueAt(session, row, 0);
                tableModel.setValueAt(type, row, 1);
                tableModel.setValueAt(srcPort, row, 2);
                tableModel.setValueAt(direction, row, 3);
                tableModel.setValueAt(dstPort, row, 4);
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
            JOptionPane.showMessageDialog(this, "请先选择要编辑的镜像配置", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[6];
        for (int i = 0; i < 6; i++) data[i] = tableModel.getValueAt(row, i);
        showDialog(data);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的镜像配置", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选镜像配置？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
        }
    }

    private void previewCmd() {
        int row = table.getSelectedRow();
        if (row == -1) {
            cmdPreviewArea.setText("请先选择端口镜像配置条目");
            return;
        }
        String session = tableModel.getValueAt(row, 0).toString();
        String type = tableModel.getValueAt(row, 1).toString();
        String srcPort = tableModel.getValueAt(row, 2).toString();
        String direction = tableModel.getValueAt(row, 3).toString();
        String dstPort = tableModel.getValueAt(row, 4).toString();
        String desc = tableModel.getValueAt(row, 5).toString();

        StringBuilder sb = new StringBuilder();
        sb.append("mirroring-group ").append(session).append(" ").append("local".equals(type) || "本地".equals(type) ? "local" : "remote-source").append("\n");
        if (!desc.isEmpty()) sb.append(" description ").append(desc).append("\n");
        sb.append("mirroring-group ").append(session).append(" ").append("local".equals(type) || "本地".equals(type) ? "local" : "remote-source")
                .append(" source ").append(srcPort).append(" ");
        if ("入".equals(direction)) {
            sb.append("inbound\n");
        } else if ("出".equals(direction)) {
            sb.append("outbound\n");
        } else {
            sb.append("both\n");
        }
        sb.append("mirroring-group ").append(session).append(" ").append("local".equals(type) || "本地".equals(type) ? "local" : "remote-source")
                .append(" monitor-port ").append(dstPort).append("\n");

        sb.append("quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
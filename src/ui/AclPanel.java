package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * ACL（访问控制列表）配置界面
 * 主要功能：
 * 1. 支持管理多条ACL（编号、类型、描述）
 * 2. 支持每条ACL下的规则增删改查（序号、动作、源、目的、协议、端口等）
 * 3. 命令生成预览
 */
public class AclPanel extends JPanel {
    private DefaultTableModel aclTableModel, ruleTableModel;
    private JTable aclTable, ruleTable;
    private JButton addAclBtn, editAclBtn, delAclBtn;
    private JButton addRuleBtn, editRuleBtn, delRuleBtn;
    private JButton previewBtn;
    private JTextArea cmdPreviewArea;

    public AclPanel(String acl) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("ACL（访问控制列表）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // ACL表
        String[] aclCols = {"ACL号", "类型", "描述"};
        aclTableModel = new DefaultTableModel(aclCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        aclTable = new JTable(aclTableModel);
        aclTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane aclScroll = new JScrollPane(aclTable);
        aclScroll.setBorder(BorderFactory.createTitledBorder("ACL列表"));
        aclScroll.setPreferredSize(new Dimension(320, 150));

        // 规则表
        String[] ruleCols = {"序号", "动作", "协议", "源地址", "源端口", "目的地址", "目的端口", "描述"};
        ruleTableModel = new DefaultTableModel(ruleCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        ruleTable = new JTable(ruleTableModel);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane ruleScroll = new JScrollPane(ruleTable);
        ruleScroll.setBorder(BorderFactory.createTitledBorder("ACL规则"));
        ruleScroll.setPreferredSize(new Dimension(820, 180));

        // 按钮区
        JPanel aclBtnPanel = new JPanel();
        addAclBtn = new JButton("添加ACL");
        editAclBtn = new JButton("编辑ACL");
        delAclBtn = new JButton("删除ACL");
        aclBtnPanel.add(addAclBtn);
        aclBtnPanel.add(editAclBtn);
        aclBtnPanel.add(delAclBtn);

        JPanel ruleBtnPanel = new JPanel();
        addRuleBtn = new JButton("添加规则");
        editRuleBtn = new JButton("编辑规则");
        delRuleBtn = new JButton("删除规则");
        ruleBtnPanel.add(addRuleBtn);
        ruleBtnPanel.add(editRuleBtn);
        ruleBtnPanel.add(delRuleBtn);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(aclScroll, BorderLayout.CENTER);
        leftPanel.add(aclBtnPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(ruleScroll, BorderLayout.CENTER);
        centerPanel.add(ruleBtnPanel, BorderLayout.SOUTH);

        // 命令预览区
        cmdPreviewArea = new JTextArea(12, 34);
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
        addAclBtn.addActionListener(e -> showAclDialog(null));
        editAclBtn.addActionListener(e -> editSelectedAcl());
        delAclBtn.addActionListener(e -> deleteSelectedAcl());
        addRuleBtn.addActionListener(e -> showRuleDialog(null));
        editRuleBtn.addActionListener(e -> editSelectedRule());
        delRuleBtn.addActionListener(e -> deleteSelectedRule());
        previewBtn.addActionListener(e -> previewCmd());

        // 选中ACL时，加载对应规则
        aclTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadAclRules();
        });

        // 双击编辑
        aclTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && aclTable.getSelectedRow() != -1) editSelectedAcl();
            }
        });
        ruleTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ruleTable.getSelectedRow() != -1) editSelectedRule();
            }
        });
    }

    // 测试数据
    private void addTestData() {
        aclTableModel.addRow(new Object[]{"3000", "高级", "办公区访问控制"});
        aclTableModel.addRow(new Object[]{"2000", "基础", "外部访问"});
        // 只为第一个ACL添加演示规则（实际应和ACL号关联，可用Map实现，此处简化）
        ruleTableModel.addRow(new Object[]{"5", "permit", "tcp", "192.168.1.0 0.0.0.255", "any", "10.1.1.1", "80", "允许办公区访问Web"});
        ruleTableModel.addRow(new Object[]{"10", "deny", "ip", "any", "any", "any", "any", "拒绝其他流量"});
    }

    private void showAclDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加ACL" : "编辑ACL", true);
        dialog.setSize(360, 210);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField numField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"基础", "高级"});
        if (data != null && data[1] != null) typeBox.setSelectedItem(data[1].toString());
        JTextField descField = new JTextField(data == null ? "" : data[2].toString());

        form.add(new JLabel("ACL号:"));
        form.add(numField);
        form.add(new JLabel("类型:"));
        form.add(typeBox);
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
            String num = numField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String desc = descField.getText().trim();
            if (num.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写ACL号", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int n = Integer.parseInt(num);
                if (n < 2000 || n > 3999) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ACL号须为2000~3999的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                aclTableModel.addRow(new Object[]{num, type, desc});
            } else {
                int row = aclTable.getSelectedRow();
                aclTableModel.setValueAt(num, row, 0);
                aclTableModel.setValueAt(type, row, 1);
                aclTableModel.setValueAt(desc, row, 2);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedAcl() {
        int row = aclTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的ACL", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[3];
        for (int i = 0; i < 3; i++) data[i] = aclTableModel.getValueAt(row, i);
        showAclDialog(data);
    }

    private void deleteSelectedAcl() {
        int row = aclTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的ACL", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选ACL？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            aclTableModel.removeRow(row);
            // 实际应删除对应规则，此处简化不实现
        }
    }

    private void showRuleDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加规则" : "编辑规则", true);
        dialog.setSize(580, 250);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        JTextField idField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> actionBox = new JComboBox<>(new String[]{"permit", "deny"});
        if (data != null && data[1] != null) actionBox.setSelectedItem(data[1].toString());
        JComboBox<String> protoBox = new JComboBox<>(new String[]{"ip", "tcp", "udp", "icmp"});
        if (data != null && data[2] != null) protoBox.setSelectedItem(data[2].toString());
        JTextField srcField = new JTextField(data == null ? "any" : data[3].toString());
        JTextField srcPortField = new JTextField(data == null ? "any" : data[4].toString());
        JTextField dstField = new JTextField(data == null ? "any" : data[5].toString());
        JTextField dstPortField = new JTextField(data == null ? "any" : data[6].toString());
        JTextField descField = new JTextField(data == null ? "" : data[7].toString());

        form.add(new JLabel("序号:"));
        form.add(idField);
        form.add(new JLabel("动作:"));
        form.add(actionBox);
        form.add(new JLabel("协议:"));
        form.add(protoBox);
        form.add(new JLabel("源地址:"));
        form.add(srcField);
        form.add(new JLabel("源端口:"));
        form.add(srcPortField);
        form.add(new JLabel("目的地址:"));
        form.add(dstField);
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
            String id = idField.getText().trim();
            String action = (String) actionBox.getSelectedItem();
            String proto = (String) protoBox.getSelectedItem();
            String src = srcField.getText().trim();
            String srcPort = srcPortField.getText().trim();
            String dst = dstField.getText().trim();
            String dstPort = dstPortField.getText().trim();
            String desc = descField.getText().trim();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写序号", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int n = Integer.parseInt(id);
                if (n < 1 || n > 9999) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "序号须为1~9999的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data == null) {
                ruleTableModel.addRow(new Object[]{id, action, proto, src, srcPort, dst, dstPort, desc});
            } else {
                int row = ruleTable.getSelectedRow();
                ruleTableModel.setValueAt(id, row, 0);
                ruleTableModel.setValueAt(action, row, 1);
                ruleTableModel.setValueAt(proto, row, 2);
                ruleTableModel.setValueAt(src, row, 3);
                ruleTableModel.setValueAt(srcPort, row, 4);
                ruleTableModel.setValueAt(dst, row, 5);
                ruleTableModel.setValueAt(dstPort, row, 6);
                ruleTableModel.setValueAt(desc, row, 7);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedRule() {
        int row = ruleTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的规则", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[8];
        for (int i = 0; i < 8; i++) data[i] = ruleTableModel.getValueAt(row, i);
        showRuleDialog(data);
    }

    private void deleteSelectedRule() {
        int row = ruleTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的规则", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选规则？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ruleTableModel.removeRow(row);
        }
    }

    // 选中不同ACL时应加载对应规则，本示例为简化（仅演示用），实际可用Map<aclNum, List<rules>>
    private void loadAclRules() {
        // 可扩展
        // 这里只清空不做持久化
        ruleTableModel.setRowCount(0);
        int row = aclTable.getSelectedRow();
        if (row == 0) { // 演示：第一个ACL加载测试规则
            ruleTableModel.addRow(new Object[]{"5", "permit", "tcp", "192.168.1.0 0.0.0.255", "any", "10.1.1.1", "80", "允许办公区访问Web"});
            ruleTableModel.addRow(new Object[]{"10", "deny", "ip", "any", "any", "any", "any", "拒绝其他流量"});
        }
    }

    private void previewCmd() {
        int aclRow = aclTable.getSelectedRow();
        if (aclRow == -1) {
            cmdPreviewArea.setText("请先选择ACL条目");
            return;
        }
        String aclNum = aclTableModel.getValueAt(aclRow, 0).toString();
        String aclType = aclTableModel.getValueAt(aclRow, 1).toString();
        String aclDesc = aclTableModel.getValueAt(aclRow, 2).toString();
        StringBuilder sb = new StringBuilder();
        sb.append("acl ").append("高级".equals(aclType) ? "number " : "basic ").append(aclNum).append("\n");
        if (!aclDesc.isEmpty()) sb.append(" description ").append(aclDesc).append("\n");
        for (int i = 0; i < ruleTableModel.getRowCount(); i++) {
            String id = ruleTableModel.getValueAt(i, 0).toString();
            String action = ruleTableModel.getValueAt(i, 1).toString();
            String proto = ruleTableModel.getValueAt(i, 2).toString();
            String src = ruleTableModel.getValueAt(i, 3).toString();
            String srcPort = ruleTableModel.getValueAt(i, 4).toString();
            String dst = ruleTableModel.getValueAt(i, 5).toString();
            String dstPort = ruleTableModel.getValueAt(i, 6).toString();
            String desc = ruleTableModel.getValueAt(i, 7).toString();
            sb.append(" rule ").append(id).append(" ").append(action).append(" ").append(proto);
            sb.append(" source ").append(src);
            if (!"any".equals(srcPort)) sb.append(" source-port eq ").append(srcPort);
            sb.append(" destination ").append(dst);
            if (!"any".equals(dstPort)) sb.append(" destination-port eq ").append(dstPort);
            if (!desc.isEmpty()) sb.append(" // ").append(desc);
            sb.append("\n");
        }
        sb.append("quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * QoS（服务质量）配置界面
 * 主要功能：
 * 1. 支持流量分类、限速、优先级等策略管理
 * 2. 支持策略应用到接口
 * 3. 命令生成预览
 */
public class QosPanel extends JPanel {
    private DefaultTableModel policyTableModel, ruleTableModel;
    private JTable policyTable, ruleTable;
    private JButton addPolicyBtn, editPolicyBtn, delPolicyBtn;
    private JButton addRuleBtn, editRuleBtn, delRuleBtn;
    private JButton previewBtn;
    private JTextArea cmdPreviewArea;

    public QosPanel(String qoS) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("QoS（服务质量）配置", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 策略表
        String[] policyCols = {"策略名称", "描述", "接口"};
        policyTableModel = new DefaultTableModel(policyCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        policyTable = new JTable(policyTableModel);
        policyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane policyScroll = new JScrollPane(policyTable);
        policyScroll.setBorder(BorderFactory.createTitledBorder("QoS策略"));
        policyScroll.setPreferredSize(new Dimension(320, 150));

        // 规则表
        String[] ruleCols = {"序号", "匹配类型", "匹配内容", "动作", "参数", "备注"};
        ruleTableModel = new DefaultTableModel(ruleCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        ruleTable = new JTable(ruleTableModel);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane ruleScroll = new JScrollPane(ruleTable);
        ruleScroll.setBorder(BorderFactory.createTitledBorder("流量分类与动作"));
        ruleScroll.setPreferredSize(new Dimension(820, 180));

        // 按钮区
        JPanel policyBtnPanel = new JPanel();
        addPolicyBtn = new JButton("添加策略");
        editPolicyBtn = new JButton("编辑策略");
        delPolicyBtn = new JButton("删除策略");
        policyBtnPanel.add(addPolicyBtn);
        policyBtnPanel.add(editPolicyBtn);
        policyBtnPanel.add(delPolicyBtn);

        JPanel ruleBtnPanel = new JPanel();
        addRuleBtn = new JButton("添加规则");
        editRuleBtn = new JButton("编辑规则");
        delRuleBtn = new JButton("删除规则");
        ruleBtnPanel.add(addRuleBtn);
        ruleBtnPanel.add(editRuleBtn);
        ruleBtnPanel.add(delRuleBtn);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(policyScroll, BorderLayout.CENTER);
        leftPanel.add(policyBtnPanel, BorderLayout.SOUTH);

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
        addPolicyBtn.addActionListener(e -> showPolicyDialog(null));
        editPolicyBtn.addActionListener(e -> editSelectedPolicy());
        delPolicyBtn.addActionListener(e -> deleteSelectedPolicy());
        addRuleBtn.addActionListener(e -> showRuleDialog(null));
        editRuleBtn.addActionListener(e -> editSelectedRule());
        delRuleBtn.addActionListener(e -> deleteSelectedRule());
        previewBtn.addActionListener(e -> previewCmd());

        // 选中策略时，加载对应规则
        policyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadPolicyRules();
        });

        // 双击编辑
        policyTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && policyTable.getSelectedRow() != -1) editSelectedPolicy();
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
        policyTableModel.addRow(new Object[]{"limit_web", "限制HTTP带宽", "GigabitEthernet0/0/1"});
        policyTableModel.addRow(new Object[]{"voice_priority", "语音优先", "GigabitEthernet0/0/2"});
        // 只为第一个策略加演示规则
        ruleTableModel.addRow(new Object[]{"10", "协议", "tcp/80", "限速", "1000kbit", "限制HTTP"});
        ruleTableModel.addRow(new Object[]{"20", "源地址", "192.168.1.0/24", "优先级", "7", "办公优先"});
    }

    private void showPolicyDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加策略" : "编辑策略", true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField nameField = new JTextField(data == null ? "" : data[0].toString());
        JTextField descField = new JTextField(data == null ? "" : data[1].toString());
        JTextField ifaceField = new JTextField(data == null ? "" : data[2].toString());

        form.add(new JLabel("策略名称:"));
        form.add(nameField);
        form.add(new JLabel("描述:"));
        form.add(descField);
        form.add(new JLabel("应用接口:"));
        form.add(ifaceField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String iface = ifaceField.getText().trim();
            if (name.isEmpty() || iface.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写策略名称和接口", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (data == null) {
                policyTableModel.addRow(new Object[]{name, desc, iface});
            } else {
                int row = policyTable.getSelectedRow();
                policyTableModel.setValueAt(name, row, 0);
                policyTableModel.setValueAt(desc, row, 1);
                policyTableModel.setValueAt(iface, row, 2);
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedPolicy() {
        int row = policyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的策略", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] data = new Object[3];
        for (int i = 0; i < 3; i++) data[i] = policyTableModel.getValueAt(row, i);
        showPolicyDialog(data);
    }

    private void deleteSelectedPolicy() {
        int row = policyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的策略", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选策略？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            policyTableModel.removeRow(row);
            // 实际应删除对应规则，此处简化
        }
    }

    private void showRuleDialog(Object[] data) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), data == null ? "添加规则" : "编辑规则", true);
        dialog.setSize(550, 240);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        JTextField idField = new JTextField(data == null ? "" : data[0].toString());
        JComboBox<String> matchTypeBox = new JComboBox<>(new String[]{"协议", "源地址", "目的地址", "端口"});
        if (data != null && data[1] != null) matchTypeBox.setSelectedItem(data[1].toString());
        JTextField matchValueField = new JTextField(data == null ? "" : data[2].toString());
        JComboBox<String> actionBox = new JComboBox<>(new String[]{"限速", "优先级", "丢弃"});
        if (data != null && data[3] != null) actionBox.setSelectedItem(data[3].toString());
        JTextField paramField = new JTextField(data == null ? "" : data[4].toString());
        JTextField descField = new JTextField(data == null ? "" : data[5].toString());

        form.add(new JLabel("序号:"));
        form.add(idField);
        form.add(new JLabel("匹配类型:"));
        form.add(matchTypeBox);
        form.add(new JLabel("匹配内容:"));
        form.add(matchValueField);
        form.add(new JLabel("动作:"));
        form.add(actionBox);
        form.add(new JLabel("参数:"));
        form.add(paramField);
        form.add(new JLabel("备注:"));
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
            String matchType = (String) matchTypeBox.getSelectedItem();
            String matchValue = matchValueField.getText().trim();
            String action = (String) actionBox.getSelectedItem();
            String param = paramField.getText().trim();
            String desc = descField.getText().trim();

            if (id.isEmpty() || matchValue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写序号和匹配内容", "提示", JOptionPane.WARNING_MESSAGE);
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
                ruleTableModel.addRow(new Object[]{id, matchType, matchValue, action, param, desc});
            } else {
                int row = ruleTable.getSelectedRow();
                ruleTableModel.setValueAt(id, row, 0);
                ruleTableModel.setValueAt(matchType, row, 1);
                ruleTableModel.setValueAt(matchValue, row, 2);
                ruleTableModel.setValueAt(action, row, 3);
                ruleTableModel.setValueAt(param, row, 4);
                ruleTableModel.setValueAt(desc, row, 5);
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
        Object[] data = new Object[6];
        for (int i = 0; i < 6; i++) data[i] = ruleTableModel.getValueAt(row, i);
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

    // 选中不同策略时应加载对应规则，本示例为简化（仅演示用），实际可用Map<策略名, List<规则>>
    private void loadPolicyRules() {
        ruleTableModel.setRowCount(0);
        int row = policyTable.getSelectedRow();
        if (row == 0) { // 演示：第一个策略加载测试规则
            ruleTableModel.addRow(new Object[]{"10", "协议", "tcp/80", "限速", "1000kbit", "限制HTTP"});
            ruleTableModel.addRow(new Object[]{"20", "源地址", "192.168.1.0/24", "优先级", "7", "办公优先"});
        }
    }

    private void previewCmd() {
        int policyRow = policyTable.getSelectedRow();
        if (policyRow == -1) {
            cmdPreviewArea.setText("请先选择QoS策略");
            return;
        }
        String policyName = policyTableModel.getValueAt(policyRow, 0).toString();
        String policyDesc = policyTableModel.getValueAt(policyRow, 1).toString();
        String iface = policyTableModel.getValueAt(policyRow, 2).toString();
        StringBuilder sb = new StringBuilder();
        sb.append("traffic policy ").append(policyName).append("\n");
        if (!policyDesc.isEmpty()) sb.append(" description ").append(policyDesc).append("\n");
        for (int i = 0; i < ruleTableModel.getRowCount(); i++) {
            String id = ruleTableModel.getValueAt(i, 0).toString();
            String matchType = ruleTableModel.getValueAt(i, 1).toString();
            String matchValue = ruleTableModel.getValueAt(i, 2).toString();
            String action = ruleTableModel.getValueAt(i, 3).toString();
            String param = ruleTableModel.getValueAt(i, 4).toString();
            String desc = ruleTableModel.getValueAt(i, 5).toString();
            sb.append(" classifier c").append(id).append(" ");
            switch (matchType) {
                case "协议":
                    sb.append("if-match protocol ").append(matchValue);
                    break;
                case "源地址":
                    sb.append("if-match src-ip ").append(matchValue);
                    break;
                case "目的地址":
                    sb.append("if-match dst-ip ").append(matchValue);
                    break;
                case "端口":
                    sb.append("if-match dport ").append(matchValue);
                    break;
            }
            sb.append("\n behavior b").append(id).append("\n");
            if ("限速".equals(action)) {
                sb.append("  car cir ").append(param).append("\n");
            } else if ("优先级".equals(action)) {
                sb.append("  priority ").append(param).append("\n");
            } else if ("丢弃".equals(action)) {
                sb.append("  discard\n");
            }
            if (!desc.isEmpty()) sb.append("  // ").append(desc).append("\n");
            sb.append(" quit\n");
        }
        sb.append(" quit\n");
        sb.append("interface ").append(iface).append("\n");
        sb.append(" traffic-policy ").append(policyName).append(" inbound\n");
        sb.append(" quit\n");
        cmdPreviewArea.setText(sb.toString());
    }
}
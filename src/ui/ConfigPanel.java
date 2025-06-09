package ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 全局配置管理界面
 * 主要功能：
 * 1. 配置全局参数（如设备登录方式、超时、SNMP参数等）
 * 2. 支持配置的导入导出（JSON/INI/XML等，可扩展）
 * 3. 配置应用与恢复
 */
public class ConfigPanel extends JPanel {
    private JTextField timeoutField;
    private JComboBox<String> loginTypeBox;
    private JTextField snmpVerField;
    private JTextField snmpCommField;
    private JButton applyBtn, resetBtn, importBtn, exportBtn;
    private JFileChooser fileChooser;

    public ConfigPanel(String 配置管理) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("全局配置管理", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 配置表单
        JPanel configPanel = new JPanel(new GridLayout(5, 2, 12, 12));
        loginTypeBox = new JComboBox<>(new String[]{"SSH", "Telnet"});
        timeoutField = new JTextField("30");
        snmpVerField = new JTextField("v2c");
        snmpCommField = new JTextField("public");

        configPanel.add(new JLabel("默认登录方式:"));
        configPanel.add(loginTypeBox);
        configPanel.add(new JLabel("登录超时(秒):"));
        configPanel.add(timeoutField);
        configPanel.add(new JLabel("SNMP版本:"));
        configPanel.add(snmpVerField);
        configPanel.add(new JLabel("SNMP社区:"));
        configPanel.add(snmpCommField);

        add(configPanel, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel();
        applyBtn = new JButton("应用配置");
        resetBtn = new JButton("恢复默认");
        importBtn = new JButton("导入配置");
        exportBtn = new JButton("导出配置");
        btnPanel.add(applyBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(importBtn);
        btnPanel.add(exportBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // 文件选择器
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("配置文件 (*.json, *.ini, *.xml)", "json", "ini", "xml"));

        // 事件绑定
        applyBtn.addActionListener(e -> applyConfig());
        resetBtn.addActionListener(e -> resetConfig());
        importBtn.addActionListener(e -> importConfig());
        exportBtn.addActionListener(e -> exportConfig());
    }

    private void applyConfig() {
        String loginType = (String) loginTypeBox.getSelectedItem();
        String timeout = timeoutField.getText().trim();
        String snmpVer = snmpVerField.getText().trim();
        String snmpComm = snmpCommField.getText().trim();
        // 可扩展：参数校验
        JOptionPane.showMessageDialog(this,
                "已应用配置：\n登录方式: " + loginType +
                        "\n超时: " + timeout +
                        "\nSNMP版本: " + snmpVer +
                        "\nSNMP社区: " + snmpComm,
                "应用成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetConfig() {
        loginTypeBox.setSelectedIndex(0);
        timeoutField.setText("30");
        snmpVerField.setText("v2c");
        snmpCommField.setText("public");
        JOptionPane.showMessageDialog(this, "已恢复默认配置", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void importConfig() {
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 简单演示：仅支持读取JSON格式
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                String content = sb.toString();
                // JSON简单解析（实际建议用Jackson/Gson等库）
                if (content.contains("{")) {
                    if (content.contains("\"loginType\"")) {
                        String v = content.split("\"loginType\"\\s*:\\s*\"")[1].split("\"")[0];
                        loginTypeBox.setSelectedItem(v);
                    }
                    if (content.contains("\"timeout\"")) {
                        String v = content.split("\"timeout\"\\s*:\\s*\"")[1].split("\"")[0];
                        timeoutField.setText(v);
                    }
                    if (content.contains("\"snmpVer\"")) {
                        String v = content.split("\"snmpVer\"\\s*:\\s*\"")[1].split("\"")[0];
                        snmpVerField.setText(v);
                    }
                    if (content.contains("\"snmpComm\"")) {
                        String v = content.split("\"snmpComm\"\\s*:\\s*\"")[1].split("\"")[0];
                        snmpCommField.setText(v);
                    }
                    JOptionPane.showMessageDialog(this, "配置导入成功", "导入", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "暂仅支持JSON格式的简单导入", "格式不支持", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "导入失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportConfig() {
        int ret = fileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String json = "{\n" +
                    "  \"loginType\": \"" + loginTypeBox.getSelectedItem() + "\",\n" +
                    "  \"timeout\": \"" + timeoutField.getText().trim() + "\",\n" +
                    "  \"snmpVer\": \"" + snmpVerField.getText().trim() + "\",\n" +
                    "  \"snmpComm\": \"" + snmpCommField.getText().trim() + "\"\n" +
                    "}";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(json);
                JOptionPane.showMessageDialog(this, "配置已导出为JSON", "导出", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
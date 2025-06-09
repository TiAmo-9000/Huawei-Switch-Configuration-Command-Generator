package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 华为交换机网络配置管理程序 - 主界面（带色彩美化）
 * 依赖：无第三方皮肤库，仅用Java自带API
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel centerPanel;

    public MainFrame() {
        // 设置全局字体
        setUIFont(new javax.swing.plaf.FontUIResource("微软雅黑", Font.PLAIN, 16));

        setTitle("华为交换机网络配置管理程序");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 设置主色
        Color mainColor = new Color(37, 81, 166);
        Color navBgColor = new Color(230, 239, 255);
        Color navHoverColor = new Color(69, 149, 236);
        Color selectedColor = new Color(255, 215, 0);

        // 左侧导航栏
        JPanel navPanel = new JPanel();
        navPanel.setBackground(navBgColor);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(180, getHeight()));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, mainColor));

        String[] modules = {"设备管理", "VLAN", "生成树", "端口聚合", "端口安全", "IP配置", "路由配置", "ACL", "QoS", "DHCP", "NAT", "SNMP", "端口镜像", "用户管理", "配置管理", "拓扑监控"};
        JButton[] buttons = new JButton[modules.length];

        centerPanel = new JPanel(new CardLayout());
        cardLayout = (CardLayout) centerPanel.getLayout();

        // 加载各功能面板（可用占位Panel演示）
        centerPanel.add(new DevicePanel("设备管理"), "设备管理");
        centerPanel.add(new VlanPanel("VLAN"), "VLAN");
        centerPanel.add(new StpPanel("生成树"), "生成树");
        centerPanel.add(new LacpPanel("端口聚合"), "端口聚合");
        centerPanel.add(new PortSecurityPanel("端口安全"), "端口安全");
        centerPanel.add(new IpPanel("IP配置"), "IP配置");
        centerPanel.add(new RoutePanel("路由配置"), "路由配置");
        centerPanel.add(new AclPanel("ACL"), "ACL");
        centerPanel.add(new QosPanel("QoS"), "QoS");
        centerPanel.add(new DhcpPanel("DHCP"), "DHCP");
        centerPanel.add(new NatPanel("NAT"), "NAT");
        centerPanel.add(new SnmpPanel("SNMP"), "SNMP");
        centerPanel.add(new MirrorPanel("端口镜像"), "端口镜像");
        centerPanel.add(new UserPanel("用户管理"), "用户管理");
        centerPanel.add(new ConfigPanel("配置管理"), "配置管理");
        centerPanel.add(new TopologyPanel("拓扑监控"), "拓扑监控");

        // 按钮组实现选中高亮
        ButtonGroup navButtonGroup = new ButtonGroup();

        for (int i = 0; i < modules.length; i++) {
            buttons[i] = new JButton(modules[i]);
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(navBgColor);
            buttons[i].setForeground(mainColor);
            buttons[i].setFont(new Font("微软雅黑", Font.BOLD, 18));
            buttons[i].setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
            buttons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // 高亮选中
            final int idx = i;
            buttons[i].addActionListener(e -> {
                cardLayout.show(centerPanel, modules[idx]);
                // 切换高亮
                for (int j = 0; j < modules.length; j++) {
                    if (j == idx) {
                        buttons[j].setBackground(mainColor);
                        buttons[j].setForeground(selectedColor);
                    } else {
                        buttons[j].setBackground(navBgColor);
                        buttons[j].setForeground(mainColor);
                    }
                }
            });

            // 悬停高亮
            buttons[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (buttons[idx].getBackground() != mainColor) {
                        buttons[idx].setBackground(navHoverColor);
                        buttons[idx].setForeground(Color.WHITE);
                    }
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (buttons[idx].getBackground() != mainColor) {
                        buttons[idx].setBackground(navBgColor);
                        buttons[idx].setForeground(mainColor);
                    }
                }
            });

            navButtonGroup.add(buttons[i]);
            navPanel.add(buttons[i]);
            navPanel.add(Box.createVerticalStrut(2));
        }
        // 默认选中第一个
        buttons[0].doClick();

        // 页面顶部横幅
        JPanel topPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 渐变背景
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, mainColor, getWidth(), 0, navHoverColor);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setPreferredSize(new Dimension(0, 60));
        topPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("华为交换机网络配置管理程序", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // 主体分割
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navPanel, centerPanel);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(3);
        splitPane.setBorder(null);

        // 主布局
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * 创建占位面板（实际项目中请替换为对应功能面板）
     */
    private JPanel createPlaceHolder(String label) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel(label + "模块");
        lbl.setFont(new Font("微软雅黑", Font.BOLD, 36));
        lbl.setForeground(new Color(37, 81, 166));
        panel.setBackground(new Color(248, 250, 252));
        panel.add(lbl);
        return panel;
    }

    /**
     * 全局字体设置
     */
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;

/**
 * 网络拓扑可视化界面
 * 主要功能：
 * 1. 拖拽添加/删除设备节点与链路
 * 2. 节点支持交换机/路由器/PC等图标
 * 3. 支持节点基本属性设置
 * 4. 支持链路属性设置（带宽、延迟等）
 * 5. 可导出拓扑图为图片
 */
public class TopologyPanel extends JPanel {
    private List<TopoNode> nodes = new ArrayList<>();
    private List<TopoLink> links = new ArrayList<>();
    private TopoNode selectedNode = null;
    private TopoNode linkStartNode = null;
    private Point mousePt = null;

    private JButton addSwitchBtn, addRouterBtn, addPcBtn, delBtn, addLinkBtn, delLinkBtn, exportBtn;

    public TopologyPanel(String 拓扑监控) {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel title = new JLabel("网络拓扑可视化", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // 控制按钮区
        JPanel btnPanel = new JPanel();
        addSwitchBtn = new JButton("添加交换机");
        addRouterBtn = new JButton("添加路由器");
        addPcBtn = new JButton("添加PC");
        addLinkBtn = new JButton("添加链路");
        delBtn = new JButton("删除节点");
        delLinkBtn = new JButton("删除链路");
        exportBtn = new JButton("导出图片");
        btnPanel.add(addSwitchBtn);
        btnPanel.add(addRouterBtn);
        btnPanel.add(addPcBtn);
        btnPanel.add(addLinkBtn);
        btnPanel.add(delBtn);
        btnPanel.add(delLinkBtn);
        btnPanel.add(exportBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 绘图区
        TopologyCanvas canvas = new TopologyCanvas();
        JScrollPane scroll = new JScrollPane(canvas);
        add(scroll, BorderLayout.CENTER);

        // 事件绑定
        addSwitchBtn.addActionListener(e -> canvas.setAddMode("switch"));
        addRouterBtn.addActionListener(e -> canvas.setAddMode("router"));
        addPcBtn.addActionListener(e -> canvas.setAddMode("pc"));
        addLinkBtn.addActionListener(e -> canvas.setAddMode("link"));
        delBtn.addActionListener(e -> {
            if (selectedNode != null) {
                nodes.remove(selectedNode);
                // 删除与该节点相关链路
                links.removeIf(l -> l.a == selectedNode || l.b == selectedNode);
                selectedNode = null;
                canvas.repaint();
            }
        });
        delLinkBtn.addActionListener(e -> {
            if (canvas.selectedLink != null) {
                links.remove(canvas.selectedLink);
                canvas.selectedLink = null;
                canvas.repaint();
            }
        });
        exportBtn.addActionListener(e -> canvas.exportImage());
    }

    // 节点类型
    public static class TopoNode {
        public int x, y;
        public String type; // switch, router, pc
        public String name = "";
        public int radius = 32;

        public TopoNode(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.name = type + "_" + (int)(Math.random() * 1000);
        }

        public boolean contains(Point p) {
            int dx = x - p.x, dy = y - p.y;
            return dx * dx + dy * dy <= radius * radius;
        }
    }

    public static class TopoLink {
        public TopoNode a, b;
        public String name = "";
        public String bandwidth = "1G";
        public String delay = "1ms";
        public TopoLink(TopoNode a, TopoNode b) {
            this.a = a;
            this.b = b;
            this.name = a.name + "<->" + b.name;
        }

        public boolean contains(Point p) {
            // 粗略判断点到线段距离小于8像素视为选中
            double dist = ptSegDist(a.x, a.y, b.x, b.y, p.x, p.y);
            return dist < 8.0;
        }
        private double ptSegDist(int x1, int y1, int x2, int y2, int px, int py) {
            double dx = x2 - x1, dy = y2 - y1;
            if (dx == 0 && dy == 0) return Math.hypot(px - x1, py - y1);
            double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
            t = Math.max(0, Math.min(1, t));
            double nx = x1 + t * dx, ny = y1 + t * dy;
            return Math.hypot(px - nx, py - ny);
        }
    }

    // 绘图区
    class TopologyCanvas extends JPanel {
        private String addMode = ""; // switch/router/pc/link
        public TopoLink selectedLink = null;

        public TopologyCanvas() {
            setPreferredSize(new Dimension(1200, 800));
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mousePt = e.getPoint();
                    if ("switch".equals(addMode) || "router".equals(addMode) || "pc".equals(addMode)) {
                        TopoNode node = new TopoNode(mousePt.x, mousePt.y, addMode);
                        nodes.add(node);
                        selectedNode = node;
                        addMode = "";
                        repaint();
                    } else if ("link".equals(addMode)) {
                        selectedNode = getNodeAt(mousePt);
                        if (selectedNode != null) {
                            linkStartNode = selectedNode;
                        }
                    } else {
                        selectedNode = getNodeAt(mousePt);
                        if (selectedNode != null) {
                            // 右键弹出属性框
                            if (SwingUtilities.isRightMouseButton(e)) {
                                showNodePropertyDialog(selectedNode);
                                return;
                            }
                        } else {
                            selectedLink = getLinkAt(mousePt);
                            if (selectedLink != null && SwingUtilities.isRightMouseButton(e)) {
                                showLinkPropertyDialog(selectedLink);
                            }
                        }
                        repaint();
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if ("link".equals(addMode) && linkStartNode != null) {
                        TopoNode end = getNodeAt(e.getPoint());
                        if (end != null && end != linkStartNode) {
                            TopoLink link = new TopoLink(linkStartNode, end);
                            links.add(link);
                        }
                        linkStartNode = null;
                        addMode = "";
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (selectedNode != null && SwingUtilities.isLeftMouseButton(e)) {
                        selectedNode.x = e.getX();
                        selectedNode.y = e.getY();
                        repaint();
                    }
                }
            });
        }

        public void setAddMode(String mode) {
            addMode = mode;
        }

        private TopoNode getNodeAt(Point p) {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                if (nodes.get(i).contains(p)) return nodes.get(i);
            }
            return null;
        }

        private TopoLink getLinkAt(Point p) {
            for (int i = links.size() - 1; i >= 0; i--) {
                if (links.get(i).contains(p)) return links.get(i);
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 画链路
            for (TopoLink link : links) {
                g.setColor(link == selectedLink ? Color.RED : Color.GRAY);
                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g.drawLine(link.a.x, link.a.y, link.b.x, link.b.y);

                // 链路名字
                int mx = (link.a.x + link.b.x) / 2, my = (link.a.y + link.b.y) / 2;
                g.setColor(Color.BLUE);
                g.drawString(link.name + " " + link.bandwidth + " " + link.delay, mx, my);
            }

            // 画节点
            for (TopoNode node : nodes) {
                int r = node.radius;
                switch (node.type) {
                    case "switch":
                        g.setColor(new Color(70, 130, 180));
                        g.fillRect(node.x - r, node.y - r, 2 * r, 2 * r);
                        g.setColor(Color.WHITE);
                        g.drawString(node.name, node.x - r + 4, node.y);
                        break;
                    case "router":
                        g.setColor(new Color(46, 139, 87));
                        g.fillOval(node.x - r, node.y - r, 2 * r, 2 * r);
                        g.setColor(Color.WHITE);
                        g.drawString(node.name, node.x - r + 4, node.y);
                        break;
                    case "pc":
                        g.setColor(new Color(160, 82, 45));
                        g.fillRoundRect(node.x - r, node.y - r, 2 * r, 2 * r, 20, 20);
                        g.setColor(Color.WHITE);
                        g.drawString(node.name, node.x - r + 4, node.y);
                        break;
                }
                if (node == selectedNode) {
                    g.setColor(Color.RED);
                    g.drawRect(node.x - r - 3, node.y - r - 3, 2 * r + 6, 2 * r + 6);
                }
            }
        }

        // 节点属性对话框
        private void showNodePropertyDialog(TopoNode node) {
            JTextField nameField = new JTextField(node.name, 14);
            JTextField xField = new JTextField(String.valueOf(node.x), 6);
            JTextField yField = new JTextField(String.valueOf(node.y), 6);

            JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
            panel.add(new JLabel("名称:"));
            panel.add(nameField);
            panel.add(new JLabel("X:"));
            panel.add(xField);
            panel.add(new JLabel("Y:"));
            panel.add(yField);

            int ret = JOptionPane.showConfirmDialog(this, panel, "节点属性", JOptionPane.OK_CANCEL_OPTION);
            if (ret == JOptionPane.OK_OPTION) {
                node.name = nameField.getText().trim();
                try {
                    node.x = Integer.parseInt(xField.getText().trim());
                    node.y = Integer.parseInt(yField.getText().trim());
                } catch (Exception ignored) {
                }
                repaint();
            }
        }

        // 链路属性对话框
        private void showLinkPropertyDialog(TopoLink link) {
            JTextField nameField = new JTextField(link.name, 14);
            JTextField bwField = new JTextField(link.bandwidth, 8);
            JTextField delayField = new JTextField(link.delay, 8);

            JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
            panel.add(new JLabel("名称:"));
            panel.add(nameField);
            panel.add(new JLabel("带宽:"));
            panel.add(bwField);
            panel.add(new JLabel("延迟:"));
            panel.add(delayField);

            int ret = JOptionPane.showConfirmDialog(this, panel, "链路属性", JOptionPane.OK_CANCEL_OPTION);
            if (ret == JOptionPane.OK_OPTION) {
                link.name = nameField.getText().trim();
                link.bandwidth = bwField.getText().trim();
                link.delay = delayField.getText().trim();
                repaint();
            }
        }

        // 导出图片
        public void exportImage() {
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            paint(g2);
            g2.dispose();
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("保存拓扑图");
            int ret = fc.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    javax.imageio.ImageIO.write(img, "png", fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "已保存为图片", "导出", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
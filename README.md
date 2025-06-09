本项目是一个基于 Java Swing 的图形化界面应用，旨在简化和自动化华为交换机常用的网络配置命令生成与管理。适合网络工程师、校园网/企业网管理员快速进行交换机配置、命令预览和批量管理。

主要功能

- **设备管理**  
  支持交换设备的增删改查、搜索、分组展示，并可查看和修改设备状态。
- **VLAN 配置**  
  图形方式管理 VLAN，支持成员端口类型选择，自动生成相关配置命令。
- **生成树（STP/RSTP/MSTP）配置**  
  支持一键切换生成树协议、优先级设定，端口参数管理，自动命令预览。
- **其它功能（可扩展）**  
  支持端口聚合、端口安全、IP/路由/ACL/QoS/DHCP/NAT/SNMP/端口镜像/用户/配置/拓扑监控等模块的管理与命令生成。

## 环境要求

- JDK 8 及以上
- 操作系统：Windows/Linux/MacOS（均可运行）
- 推荐开发工具：IntelliJ IDEA（或 Eclipse）

## 如何运行

1. 克隆本仓库：
   ```bash
   git clone https://github.com/TiAmo-9000/Huawei-Switch-Configuration-Command-Generator.git
   ```
2. 用 IDEA 或 Eclipse 打开项目文件夹
3. 找到 `ui/MainFrame.java`，以 Java Application 方式运行
4. 即可进入主界面，体验各功能模块

## 目录结构

```
Huawei-Switch-Configuration-Command-Generator/
├─ ui/                   # 所有界面和功能模块源码
│--DevicePanel("设备管理"), "设备管理");
|--VlanPanel("VLAN"), "VLAN");
|--StpPanel("生成树"), "生成树");
|--LacpPanel("端口聚合"), "端口聚合");
|--PortSecurityPanel("端口安全"), "端口安全");
|--IpPanel("IP配置"), "IP配置");
|--RoutePanel("路由配置"), "路由配置");
|--AclPanel("ACL"), "ACL");
|--QosPanel("QoS"), "QoS");
|--DhcpPanel("DHCP"), "DHCP");
|--NatPanel("NAT"), "NAT");
|--SnmpPanel("SNMP"), "SNMP");
|--MirrorPanel("端口镜像"), "端口镜像");
|--UserPanel("用户管理"), "用户管理");
|--ConfigPanel("配置管理"), "配置管理");
|--TopologyPanel("拓扑监控"), "拓扑监控");
├─ .gitignore
└─ README.md
```

## 主要界面说明

- **设备管理**  
  支持设备信息录入、状态刷新、按名称/IP/型号搜索，支持设备编辑与删除。
- **VLAN 配置**  
  可新建/编辑/删除 VLAN，指定端口类型与端口号，命令自动预览。
- **生成树配置**  
  支持协议切换（STP/RSTP/MSTP）、桥优先级设置、端口优先级/边缘端口配置，命令一键生成。

## 贡献指南

欢迎提交 issue 和 PR，完善功能或修复问题。建议每次提交前描述清楚更改内容和用途。

**作者**: [TiAmo-9000](https://github.com/TiAmo-9000)  
如遇到 Bug，欢迎在 Issue 区留言反馈。
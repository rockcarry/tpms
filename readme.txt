===============
 tpms test apk 
===============

一、概述
--------
胎压监测模块测试 apk，在 android 平台上实现了 TPMS 胎压监测。硬件上采用 UART 串口，与 TPMS 模块连接；软件上实现了 TPMS 的串口通信协议。

通信协议代码，在 apk 的 jni 层实现，默认串口设备为 /dev/ttyS0，波特率 9600。android 上要保证 apk 对 /dev/ttyS0 设备有访问权限，同时硬件上保证 TPMS 模块正常供电，即可正常使用本 apk 进行测试。


二、使用说明
------------

1. apk 中 status 显示硬件模块的连接状态
2. refresh all 按钮可以刷新装全部状态（包括 tire 和 alert）
3. tire* 给出了总共 5 个轮胎的状态信息
4. match 按钮用于匹配（学习）轮胎
5. 轮胎匹配过程中可用 macth cancel 按钮取消
6. unwatch 按钮用于删除轮胎
7. match all 匹配全部轮胎
8. unwatch all 删除全部轮胎
9. alert* 给出了总共 6 个报警配置
10.config 和 config all 用于配置 alert


apical ck
2016-8-30


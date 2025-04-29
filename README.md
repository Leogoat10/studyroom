介绍

基于springboot的自习室管理和预约系统，分老师学生管理员三大模块，老师可以租用整个自习室，学生只能租用单个座位并配置了相关的违规惩罚机制，管理员可以新增删除自习室，禁用启用单个座位，管理学生违规行为，还可以发布自习室相关通知

安装教程

安装mysql数据库，在application.xml中修改数据库连接信息，在数据库面板运行resource/sql文件夹下的sql文件可生成数据库。
通过git获取源代码并在本地启动运行即可


springboot [https://spring.io/projects/spring-boot]

Mysql [https://www.mysql.com/]

Shiro安全框架 [https://github.com/greycode/shiro]

Mybatis [https://mybatis.org/mybatis-3/zh/index.html]

TODO

添加ai功能
添加类似论坛功能
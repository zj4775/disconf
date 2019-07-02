

disconf地址：https://github.com/knightliao/disconf

部署方式：
1.分别按照0-init_table.sql>1-init_data.sql>20151225.sql>20160701.sql顺序执行文件夹disconf-web\sql里面的脚本
2.修改maven私服地址并将disconf-core、disconf-client、disconf-spring-boot-starter 分别deploy
3.将disconf-web 打包并部署到tomcat等容器中
4.将disconf-ui 部署至nginx中

使用方式：（可以参考disconf-test）
在springboot启动类上加上@EnableDisconf注解
在配置文件的bean上加上@DisconfYmlFile 注解
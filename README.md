# ChatGPT Java Online Demo

项目基于Java 8 + springboot + maven

1.本地运行
IDEA 打开工程，修改 src/main/resources/application.yml 下的KEY LIST， 按照实例一行一个，自动轮询,至少放2个，如果没有可以放2个一样的

运行ChatgptOnlineJavaApplication即可



1.服务器

打包

```shell
mvn package
```

之后将jar放服务器上，安装Java8，运行

```shell
java -jar chatgptonlinejava.jar
```


即可


注意：国内服务器都不能直接访问了，请使用代理或国外服务器，代理在ChatController里配置


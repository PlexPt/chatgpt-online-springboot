# ChatGPT Java Online Demo


项目基于 Java 8、Spring Boot 和 Maven。

![image](https://github.com/user-attachments/assets/6c3cb9f8-8488-41c9-b1ad-acc1f40593ac)


## 1. 本地运行

1. 使用 IntelliJ IDEA 打开项目。
2. 修改 `src/main/resources/application.yml` 文件中的 `KEY LIST`。每行一个 Key，自动轮询，至少放入 2 个。如果没有不同的 Key，可以重复使用相同的 Key。
3. 运行 `ChatgptOnlineJavaApplication` 类以启动项目。

## 2. 服务器部署

### 打包

在项目根目录下执行以下命令打包项目：

```shell
mvn package
```

打包完成后，将生成的 jar 文件上传到服务器，并确保服务器上已安装 Java 8。然后通过以下命令运行：

```shell
java -jar chatgptonlinejava.jar
```

### 注意事项

由于国内服务器无法直接访问openai，请使用代理或部署在国外服务器。代理设置可在 `ChatController` 类中进行配置。

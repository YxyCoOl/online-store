# online-store

这是一个示例 Java + Spring Boot 项目模板，包含一个简单的 `Product` REST API（内存仓库实现）。

构建与运行：

```bash
# 在容器/本地有 Maven 的情况下：
mvn -q -DskipTests package
# 运行：
java -jar target/online-store-0.0.1-SNAPSHOT.jar

# 或者使用 Spring Boot Maven 插件直接运行：
mvn spring-boot:run
```

示例 API：
- `GET /api/products` — 列出所有产品
- `GET /api/products/{id}` — 根据 ID 获取产品
- `POST /api/products` — 创建产品，body 为 JSON，例如：

```json
{
	"name": "New Product",
	"price": 12.5
}
```

update
another update

项目结构：
- `src/main/java/com/example/onlinestore` — 应用入口和控制器/仓库/模型
- `src/main/resources/application.properties` — 配置

如果你希望我为该项目添加更多示例（例如数据库集成、Swagger 文档或更完整的测试），告诉我下一步的方向。

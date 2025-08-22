# JetStream Demo

统一封装 NATS JetStream 的 Pull / Push 消费模式，并支持基于注解的 @NatsEvent 事件处理。

## 运行

1. 启动 NATS 服务器（开启 JetStream）:
   ```bash
   nats-server -js
   ```
2. 打包:
   ```bash
   mvn -DskipTests package
   ```
3. 启动:
   ```bash
   java -jar target/jetstream-demo-1.0.0.jar
   ```

4. 发送测试事件:
   ```bash
   curl -X POST "http://localhost:8080/test/product/create?id=P1&name=Pen&category=Stationery"
   curl -X POST "http://localhost:8080/test/order/create?id=O1&user=U1&amount=888"
   ```

## 目录结构

- com.example.nats.core.*: 通用框架（流初始化、消费者封装、事件/处理器注册、注解支持）
- com.example.events.*: 各业务域事件类
- com.example.handlers.*: 事件处理器（接口式 + 注解式）
- com.example.controller.TestController: 演示发布事件

## 注解方式 @NatsEvent

在任意 Spring Bean 上用注解声明事件处理方法，无需实现接口：

```java
@Component
public class ProductDeleteAnnotatedHandler {
  @NatsEvent(subject = "product.delete", value = ProductDeleteEvent.class)
  public void onProductDelete(ProductDeleteEvent e) {
     // handle...
  }
}

@Component
public class OrderCancelAnnotatedHandler {
  @NatsEvent(domain = "order", action = "cancel")
  public boolean onOrderCancel(OrderCancelEvent e) {
     // 返回 false 将触发 NAK 重投；抛异常同样触发重投
     return true;
  }
}
```

规则与说明：
- 事件类可通过注解的 `value` 指定，或从方法唯一参数类型推断（必须实现 `DomainEvent`）。
- subject 指定方式：
  - 直接给出 `subject = "product.delete"`；或
  - 指定 `domain + action`（会拼为 `domain.action`）。
- 注册顺序：启动时自动扫描并注册；注解映射优先于约定命名解析。
- ACK/NAK：方法正常返回即 ACK；返回 `boolean false` 或抛异常则 NAK（按 maxDeliver 重投）。

## 新增事件步骤

1. 在 `com.example.events.<domain>` 下增加 `DomainActionEvent` 类，继承 `AbstractBaseEvent`。
2. 设置 `domain` 与 `action`（如 product / update）。
3. 增加对应 Handler：
   - 接口式：实现 `DomainEventHandler<YourEvent>`；或
   - 注解式：在方法上加 `@NatsEvent`。
4. 发布事件时 subject 自动为 `domain.action`（`DomainEventPublisher`）。
5. 若已有匹配的 consumer (filter-subject=domain.*)，无需改配置。

## 新增 Consumer

`application.yml` 中的 `nats.consumers` 增加条目：
```yaml
  - name: inventory-push
    stream: business-stream
    mode: push
    durable: inventory-push-durable
    filter-subject: inventory.*
```

## 广播与共享

- Push 模式设置 `queue-group` 为空：每个 durable/实例都会收到（广播）。
- 共享消费：多个实例使用同 durable + 同 queue-group（或在 Pull 模式同 durable）实现负载均衡。

## 清理/重新测试

删除流:
```bash
nats stream rm business-stream
```
然后重启应用重新创建。

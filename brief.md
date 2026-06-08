# Easy ChatGPT（Android/Java）项目简要架构

## 1. 项目定位
- 这是一个单模块的 Android App（Gradle 工程），用 `OkHttp` 调用 OpenAI `completions` 接口，实现一个最小可用的“聊天式”界面。
- 代码以 Java 为主，UI 采用传统 `Activity + RecyclerView + XML` 的方式组织。

## 2. 模块与目录结构
- 根工程：`/home/runner/work/Android_ChatGPT/Android_ChatGPT`
  - 仅包含一个模块：`:app`（见 `settings.gradle`）
- App 模块：`app/`
  - Java 代码：`app/src/main/java/com/example/easychatgpt/`
  - 资源文件：`app/src/main/res/`
  - 清单：`app/src/main/AndroidManifest.xml`

## 3. 入口与核心类（Java）
### 3.1 入口 Activity
- `app/src/main/java/com/example/easychatgpt/MainActivity.java`
  - 负责：页面初始化、用户输入处理、消息列表维护、发起网络请求、接收响应并更新 UI。
- `app/src/main/java/com/example/easychatgpt/LoginActivity.java`
  - 负责：简易登录交互（账号/密码输入 + 登录按钮），当前不做校验；点击 `login` 后进入 `MainActivity`。
  - 清单入口：`AndroidManifest.xml` 中将 `.LoginActivity` 声明为 LAUNCHER。

### 3.2 消息数据模型
- `app/src/main/java/com/example/easychatgpt/Message.java`
  - 字段：`message`（文本）、`sentBy`（来源：`me`/`bot`）
  - 常量：`SENT_BY_ME`、`SENT_BY_BOT`

### 3.3 列表展示（RecyclerView Adapter）
- `app/src/main/java/com/example/easychatgpt/MessageAdapter.java`
  - 负责：把 `List<Message>` 渲染成左右气泡（我方/机器人）两种布局状态。
  - 通过控制 `left_chat_view` / `right_chat_view` 的 `visibility` 切换对话方向。

## 4. UI 结构（XML）
- 页面布局：`app/src/main/res/layout/activity_main.xml`
  - 上半部分：`RecyclerView`（消息列表）
  - 中间：欢迎文案 `welcome_text`
  - 底部：输入框 `message_edit_text` + 发送按钮 `send_btn`
- 单条消息：`app/src/main/res/layout/chat_item.xml`
  - 左气泡：机器人消息（蓝色）
  - 右气泡：用户消息（绿色）

## 5. 主要运行流程（从点击发送到收到回复）
0. 启动 App：
   - 先进入 `LoginActivity`
   - 点击 `login` 后跳转进入 `MainActivity`
1. 用户点击发送（`send_btn`）后：
   - 从输入框读取 `question`
   - 通过 `addToChat(question, SENT_BY_ME)` 追加到消息列表
   - 清空输入框、隐藏欢迎文案
2. 发起请求（`callAPI(question)`）：
   - 先插入一条 `Typing...` 的机器人占位消息
   - 用 `org.json` 组装请求体（`model/text-davinci-003`、`prompt`、`max_tokens`、`temperature`）
   - 使用 `OkHttpClient#newCall(...).enqueue(...)` 异步请求 `https://api.openai.com/v1/completions`
3. 处理响应：
   - 成功：解析 `choices[0].text`，移除占位消息，追加机器人回复
   - 失败：展示失败原因（网络失败或非 2xx 返回）
4. 线程切换：
   - `addToChat(...)` 内部用 `runOnUiThread(...)` 确保 UI 更新在主线程执行。

## 6. 依赖与构建要点
- 构建系统：Gradle + Android Gradle Plugin（见根 `build.gradle` 与 `app/build.gradle`）
- 关键依赖（`app/build.gradle`）：
  - `androidx.appcompat` / `material` / `constraintlayout`
  - `com.squareup.okhttp3:okhttp`
  - `org.json`（Android 自带）
- 编译参数：
  - `compileSdk 33`，`minSdk 23`，Java 8（`sourceCompatibility/targetCompatibility 1.8`）

## 7. 你最该关注的扩展点（从“Demo”走向“可用”）
- OpenAI Key 管理：当前写死在 `Authorization: Bearer YOUR_API_KEY`，实际项目应放到安全渠道（如后端签发/动态下发，或至少使用 Gradle/CI 注入）。
- API 形态：当前使用 `completions + text-davinci-003`；如果要做“对话”，通常需要自己维护上下文拼接，或改为更适配对话的接口。
- 健壮性：
  - `max_tokens=4000`、错误分支里 `response.body().toString()` 的信息不够准确（可考虑读取 `response.body().string()` 的错误体）
  - 列表并发更新与占位消息移除需要保证边界（例如连续快速发送、失败重试等）

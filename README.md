# Magic Summoner - 魔法召唤师

一个基于 Spring Boot + Vue 3 的多人在线卡牌对战游戏。

## 项目简介

Magic Summoner 是一款策略卡牌对战游戏，玩家可以收集卡牌、组建卡组，与其他玩家进行实时对战。游戏支持多种卡牌类型（生物、法术、装备）和元素属性（火、水、土、风、光、暗）。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: H2 (内存数据库)
- **持久层**: Spring Data JPA
- **安全**: Spring Security + JWT
- **实时通信**: WebSocket (SockJS + STOMP)
- **构建工具**: Maven

### 前端
- **框架**: Vue 3.4
- **构建工具**: Vite 5
- **状态管理**: Pinia 2
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **实时通信**: SockJS + @stomp/stompjs

## 项目结构

```
magic-summoner/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/magicsummoner/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # REST API 控制器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── enums/             # 枚举类
│   │   ├── game/              # 游戏核心逻辑
│   │   ├── repository/        # 数据访问层
│   │   ├── security/          # 安全配置
│   │   └── service/           # 业务逻辑层
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia 状态管理
│   │   ├── views/             # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Maven 3.8+

### 后端启动

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

后端服务将启动在 http://localhost:8080

H2 数据库控制台: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:magicdb`
- 用户名: `sa`
- 密码: (空)

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将启动在 http://localhost:3000

## 默认用户

系统初始化时自动创建以下演示用户：

| 用户名 | 密码 |
|--------|------|
| player1 | 123456 |
| player2 | 123456 |

## 游戏功能

### 卡牌系统
- **生物卡**: 可召唤到战场进行战斗
- **法术卡**: 直接造成伤害或产生效果
- **装备卡**: 增强生物或玩家属性

### 元素属性
- 火 (Fire)
- 水 (Water)
- 土 (Earth)
- 风 (Wind)
- 光 (Light)
- 暗 (Dark)
- 中立 (Neutral)

### 游戏机制
- 回合制对战
- 法力值系统
- 生物攻击与防御
- 特殊效果（嘲讽、冲锋、吸血、剧毒、圣盾、潜行、风怒）
- 实时 WebSocket 通信

### 房间系统
- 创建/加入游戏房间
- 房间码邀请
- 卡组选择
- 观战模式（预留）

## API 文档

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 卡牌相关
- `GET /api/cards` - 获取所有卡牌
- `GET /api/cards/{id}` - 获取卡牌详情
- `POST /api/cards/open-pack` - 开卡包

### 卡组相关
- `GET /api/decks/player/{playerId}` - 获取玩家卡组
- `POST /api/decks` - 创建卡组
- `PUT /api/decks/{id}` - 更新卡组
- `DELETE /api/decks/{id}` - 删除卡组

### 房间相关
- `GET /api/rooms` - 获取房间列表
- `POST /api/rooms` - 创建房间
- `POST /api/rooms/{id}/join` - 加入房间
- `POST /api/rooms/{id}/leave` - 离开房间

### 游戏相关
- `POST /api/game/{roomId}/start` - 开始游戏
- `GET /api/game/{roomId}/state` - 获取游戏状态
- `POST /api/game/{roomId}/play` - 出牌
- `POST /api/game/{roomId}/attack` - 攻击
- `POST /api/game/{roomId}/endTurn` - 结束回合
- `POST /api/game/{roomId}/surrender` - 投降

## WebSocket 端点

- `/ws` - WebSocket 连接端点
- `/topic/game/{roomId}` - 游戏状态订阅

## 开发计划

- [x] 基础项目架构
- [x] 用户认证系统
- [x] 卡牌管理系统
- [x] 卡组管理系统
- [x] 游戏房间系统
- [x] 实时对战系统
- [ ] 排行榜系统
- [ ] 好友系统
- [ ] 观战系统
- [ ] AI 对战模式

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

[MIT](LICENSE)

## 联系方式

如有问题或建议，欢迎提交 Issue 或 Pull Request。

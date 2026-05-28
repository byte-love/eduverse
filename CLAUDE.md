# EduVerse 项目说明

## 项目概述
在线教育平台，包含课程管理、AI 智能客服、交易支付、考试系统等模块。

## 技术栈
- **后端**: Spring Boot 3.3.5、Spring Cloud、Spring AI 1.0.0
- **AI 模型**: DashScope（阿里云，通过 Spring AI 集成）
- **注册/配置中心**: Nacos（地址见下方，用户名/密码 nacos/nacos）
- **数据库**: MySQL（MyBatis Plus）
- **缓存**: Redis
- **向量库**: Spring AI VectorStore（用于 RAG 检索增强）
- **前端**: Vue 3（编译后产物在 `frontend/eduverse-portal/assets/` 和 `frontend/eduverse-admin/assets/`）
- **网关**: Spring Cloud Gateway

## 模块结构
| 模块 | 端口 | 职责 |
|------|------|------|
| ev-gateway | 10010 | API 网关，路由转发 |
| ev-aigc | 8094 | AI 智能客服（多智能体架构） |
| ev-course | - | 课程管理（CRUD、上架下架） |
| ev-auth | - | 认证授权 |
| ev-user | - | 用户服务 |
| ev-pay | - | 支付服务 |
| ev-learning | - | 学习进度 |
| ev-exam | - | 考试系统 |
| ev-message | - | 消息通知 |
| ev-search | - | 搜索服务 |
| ev-data | - | 数据统计 |
| ev-remark | - | 评论/评分 |

## Nacos 配置
- **本地环境**: 192.168.150.101:8848
- **测试环境**: 172.17.2.76:8848
- **认证**: 用户名 `nacos`，密码 `nacos`
- **系统提示词**: 以 `.txt` 文件存储在 DEFAULT_GROUP，`SystemPromptConfig` 通过 `@PostConstruct` + `Listener` 实现热加载
- **API 更新**: `POST /nacos/v1/cs/configs`（需先 `POST /nacos/v1/auth/login` 获取 token）

## 多智能体架构（ev-aigc）
```
RouteAgent（意图识别）
  ├── RecommendAgent（课程推荐 + RAG）
  ├── BuyAgent（课程购买）
  ├── ConsultAgent（课程咨询）
  └── KnowledgeAgent（知识讲解）
```
- **基类**: `AbstractAgent.processStream()` — 流式 SSE 响应，结束后检查 `ToolResultHolder` 并 emit PARAM 事件（课程卡片）
- **RAG**: `RecommendAgent` 使用 `QuestionAnswerAdvisor` 检索向量库（topK=6, threshold=0.6）
- **工具**: `@Tool` 注解方法在 `CourseTools`、`OrderTools` 中，通过 `ToolResultHolder`（ConcurrentHashMap<requestId, Map>）传递卡片数据到前端
- **流式事件**: DATA(1001) → PARAM(1003) → STOP(1002)

## 关键注意事项
1. **前端是编译产物** — `index.a687b3ad.js` 和 CSS 无源码，直接改编译后的文件（注意 Unicode 转义）
2. **Nacos 热加载** — 通过 API 更新配置后无需重启，`SystemPromptConfig.Listener` 自动生效
3. **CourseMapper.updateVariableById** — 之前漏了 `price`、`valid_duration`、`free`、`name` 字段，已修复
4. **CourseDraftServiceImpl.save()** — 已下架课程现在可以修改核心字段（之前只允许从未上架的课程修改）
5. **价格单位** — 数据库存分（Integer），前端展示元（/100），`CourseInfo.of()` 自动转换
6. **评分** — 数据库存整数（45 = 4.5 星），前端展示 `/10`

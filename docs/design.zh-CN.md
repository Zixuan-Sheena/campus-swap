# Design zh-CN

# 校园闲置物品交换 App：CampusSwap

## 简介

CampusSwap 是一个大学课程作业：面向大学生的校园闲置物品交换 Android App。用户发布物品、浏览和收藏物品、通过本地聊天协商交换方案，接受方案后约定线下交接并由双方确认完成

## 技术方案

- **开发环境**：Android Studio
- **语言与界面**：Kotlin + XML/View
- **页面与导航**：单 Activity，多 Fragment，使用 Navigation Component
- **列表**：RecyclerView + ListAdapter
- **数据存储**：Room
- **初始数据**：从本地 JSON 导入示例用户、物品等信息

所有数据保存在设备本地

首次运行导入示例数据，之后保留用户操作结果

示例数据需要提供两个或多个虚构的演示身份，切换身份后读取该身份相关的物品、聊天和交换记录

- **界面架构**：MainActivity 使用 AppCompatActivity 和 setContentView 加载 XML；五个主页面使用 Fragment，底栏使用 Material BottomNavigationView，通过 NavigationUI 连接 NavController
- **代码分层**：待确认

## 初版功能

### 物品发布与管理

用户可以：

- 浏览物品列表，查看图片、分类和交换意向
- 按标题搜索，按分类筛选
- 查看物品详情
- 发布、编辑和撤下自己的物品
- 收藏其他人的物品

发布的物品包含标题、描述、分类（枚举）、希望交换的物品（字符串）和一张图片。图片通过系统照片选择器选取，在本地保存可持续访问的副本或读取权限

只有 `AVAILABLE` 的物品可以由发布者编辑或撤下。已进入约定交接阶段的物品标记为 `RESERVED`，禁止编辑和撤下，避免双方已确认的交换条件发生变化。

物品内容实际变化或被撤下时，在同一数据库事务中更新物品，并将所有包含该物品的 `PENDING` Proposal 标记为 `INVALIDATED`。原样保存不增加 revision，也不触发失效。已接受、拒绝、撤回或被替代的方案保留原状态。

### 对话并编辑交换条件

- 用户在其他人的物品详情页或者主页可以发起新 Chat
- 发起新对话的同时，发起方需要编辑一个 Proposal，加入自己发布的若干物品，并从对方的库存中选取一些物品加入。Proposal 同时包含了交换的时间，地点（Eircode）。确认无误后，锁定 Proposal 并发送给对方
- 对方可以接受，拒绝，或者修改 Proposal，锁定后再发送回请求方
- 这期间双方都可以继续进行对话
- 聊天中的失效方案显示 `Exchange proposal · Invalid`。方案详情顶部显示相同状态，并提示 `One or more items have been modified or withdrawn since this proposal was created.`
- 失效方案只读，不能接受或直接编辑后重发；双方可从聊天中的“创建 Proposal”重新选择物品并发送新方案。
- 以上步骤可以一直重复，直到其中一方选择同意后，创建 Exchange 记录，双方等待进行面交

### Proposal 版本与物品预留

- **发送 Proposal 时**：固定该版本包含的物品 ID 清单、发送时的提供方和 revision，以及交接时间和地点。方案中的物品仍可参与其他协商，此时不预留物品。
- **展示 Proposal 时**：通过物品 ID 读取当前 Item，展示最新标题、图片和状态；点击图片进入当前物品详情。已撤下、预留或已交换的物品仍保留在方案清单中并显示状态。历史方案同样读取最新物品信息，不提供发送时的物品内容快照展示。
- **编辑或撤下物品时**：在同一事务中使相关的全部 `PENDING` 方案变为 `INVALIDATED`；其他终态不变。物品之后恢复可用也不会使旧方案重新生效。
- **修改 Proposal 时**：基于旧版本创建并发送新版本。旧版本保留作为历史记录，但不能再被接受；只有最新的待回应版本可以被接受
- **接受 Proposal 时**：检查方案仍为最新的 `PENDING`，再次检查双方清单中的全部物品是否仍为 `AVAILABLE`，归属及 revision 是否与发送时的记录一致。全部通过后，在同一数据库事务中将 Proposal 标记为 `ACCEPTED`、创建 `AWAITING_HANDOVER` 状态的 Exchange、预留全部相关物品（设置为 `RESERVED`），并将包含其中任意物品的其他待回应 Proposal 标记为已失效。若检查失败，则不接受方案，也不预留任何物品

### 确认完成

面交完成后，双方在交换详情页分别点击「确认已交接」，所有相关物品标记为已交换

### 我的页面

显示当前演示身份，并提供：

- 我发布的物品 -> 详情、编辑、撤下
- 我的收藏
- 演示身份切换
- 关于 APP

## 导航结构

```
MainActivity
- Browse
    - 顶部分类筛选 -> 最近上架或选定分类的物品列表 -> 物品详情
- Search
    - 顶部搜索框 -> 输入关键词 -> 搜索结果列表 -> 物品详情
- List
    - 发布物品
- Chats
    - 会话列表 -> 聊天详情
	    - 文本消息及历史方案卡片
    - 编辑交换清单、时间和 Eircode -> 发送 Proposal
    - 接受、拒绝、撤回或发送修改版
    - 已接受方案 -> Exchange 详情、取消或确认交接
- Profile
    - 我发布的物品 -> 详情、编辑、撤下
    - 我的收藏 -> 物品详情
    - 切换演示身份
	- 关于
```

## 数据模型

### 建模范围与约定

两个用户之间交换物品，双方各提供至少一件物品。同一对用户共用一个 Conversation，保留聊天、多个方案版本和多笔历史交换

**Proposal 负责是否接受方案；Exchange 负责接受之后的交接。Exchange 只在 Proposal 被接受时创建。** 同一会话最多一个 `PENDING` 方案或一笔 `AWAITING_HANDOVER` 交换，两者不能并存。待交接期间可以聊天，但不能发送新方案

Proposal 编辑内容临时保存在 Fragment 的内存变量中，不保存草稿到数据库，也不保证旋转屏幕或进程重启后恢复。只有发送时才保存物品关联、发送时的提供方和 revision、交接时间和地点；这些方案字段发送后不再原地修改。修改后重新发送，原待回应版变为 `SUPERSEDED`。物品展示内容始终来自当前 Item，不属于不可变的方案历史。

统一使用 String 保存 UUID 主键及对应外键；Long 只用于 UTC 毫秒时间戳，Int 用于版本。枚举在数据库中以稳定字符串保存

### User（演示用户）

| 字段 | 类型 | 说明     |
| --- | --- |--------|
| id | String | UUID 主键 |
| displayName | String | 用户昵称 |

### Item（物品）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | UUID 主键 |
| ownerId | String | 外键 -> `User.id`；完成交换后仍保留原发布者 |
| title / description | String | 标题、描述 |
| category | ItemCategory | 物品分类枚举 |
| wantedDescription | String | 交换意向说明，限制字符数量保证 Browse 页卡片排版好看 |
| imagePath | String | 本地图片副本路径或可持久访问的引用 |
| status | ItemStatus | `AVAILABLE` / `RESERVED` / `EXCHANGED` / `WITHDRAWN` |
| revision | Int | 初始为 1；内容实际变化或撤下时递增，原样保存不递增，用于与方案发送时记录的版本校验；预留、取消预留和完成交换使用状态更新，不增加 revision |
| createdAt / updatedAt | Long | UTC 毫秒时间戳 |

### Favorite（收藏）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| userId | String | 外键 -> `User.id` |
| itemId | String | 外键 -> `Item.id`；与 `userId` 构成联合主键 |
| createdAt | Long | 收藏时间 |

### Conversation（双方会话）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | UUID 主键 |
| userLowId / userHighId | String | 均引用 `User.id`；按统一字符串顺序规范化存储，两个用户必须不同 |
| createdAt | Long | 创建时间 |

### Proposal（已发送的方案版本）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | UUID 主键 |
| conversationId | String | 外键 -> `Conversation.id` |
| version | Int | 同一会话内从 1 递增 |
| proposerId | String | 外键 -> `User.id`；必须是发起者 |
| receiverId | String | 外键 -> `User.id` |
| status | ProposalStatus | `PENDING` / `SUPERSEDED` / `ACCEPTED` / `REJECTED` / `WITHDRAWN` / `INVALIDATED` |
| meetingAt | Long | 约定交接时间，发送时必须在未来 |
| eircode | String | 交接地点 Eircode；初版仅输入和展示，不做在线地址验证 |
| createdAt | Long | 发送时间；清单、时间与地点发送后不再原地修改 |


### Exchange（已达成的交换）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | UUID 主键 |
| conversationId | String | 外键 -> `Conversation.id`；必须与所引用 Proposal 的会话一致 |
| proposalId | String | 外键 -> `Proposal.id`，并建立唯一索引；固定指向最终接受的方案，不追踪最新方案 |
| status | ExchangeStatus | `AWAITING_HANDOVER` / `CANCELLED` / `COMPLETED` |
| lowConfirmedAt / highConfirmedAt | Long? | 对应会话两位参与者的确认时间，未确认时为空 |
| createdAt / updatedAt | Long | 创建与更新时间 |

### ProposalItem（方案物品关联及发送时校验信息）

业务使用 `proposalId`、`itemId`、`ownerIdSnapshot` 和 `itemRevision` 记录方案关联及发送时的校验基准。界面通过 `itemId` 查询当前 Item，不使用标题、描述、分类或图片快照。

为保持当前 Room 数据库结构和版本 1，已有的内容快照字段暂时保留并兼容现有写入，但不再承担历史内容展示功能。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| proposalId | String | 外键 -> `Proposal.id` |
| itemId | String | 外键 -> `Item.id`；与 `proposalId` 构成联合主键 |
| ownerIdSnapshot | String | 外键 -> `User.id`；发送时提供物品的一方 |
| itemRevision | Int | 发送时的 `Item.revision` |
| titleSnapshot / descriptionSnapshot | String | 兼容旧结构保留，界面不读取 |
| categorySnapshot | String | 兼容旧结构保留，界面不读取 |
| imagePathSnapshot | String | 兼容旧结构保留，界面使用当前 Item.imagePath |

### Message（聊天消息及方案卡片）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | UUID 主键 |
| conversationId | String | 外键 -> `Conversation.id` |
| senderId | String | 外键 -> `User.id`，且必须属于该会话 |
| type | MessageType | `TEXT` / `PROPOSAL` |
| text | String? | `TEXT` 时非空白；`PROPOSAL` 时为空 |
| proposalId | String? | 外键 -> `Proposal.id`；`PROPOSAL` 时必填，`TEXT` 时为空 |
| createdAt | Long | 发送时间 |

### 状态划分

```
ItemStatus
AVAILABLE -> RESERVED -> EXCHANGED
AVAILABLE -> WITHDRAWN
RESERVED -> AVAILABLE（Exchange 取消）
WITHDRAWN -> AVAILABLE（未来实现：编辑后重新上架）

ProposalStatus
PENDING -> ACCEPTED / REJECTED / WITHDRAWN / INVALIDATED / SUPERSEDED
INVALIDATED：物品被编辑、撤下，或因其他方案被接受而被预留；界面显示 Invalid。
INVALIDATED 不自动恢复为 PENDING，需要重新发送方案。
ACCEPTED 为方案的终态，不再变成 CANCELLED、COMPLETED 或 INVALIDATED。

ExchangeStatus
创建时 AWAITING_HANDOVER
-> CANCELLED（双方均未确认）
-> COMPLETED（双方均确认）
```

## 不纳入第一版的功能

以下功能列入未来改进：

- 用户评价
- 账号注册
- 实时聊天
- 支付、保证金和补差价?
- 预设的校园交换地点?
- 举报、争议裁决和管理员后台
- 实时定位
- 智能推荐

## 分工

待补充

## 最终演示流程

1. 身份 A 发布多件闲置物品
2. 切换身份 B，搜索并查看这些物品
3. B 与 A 发起对话
4. B 在交换列表加入双方的物品，填写时间和 Eircode，发送 Proposal；可演示 A 修改后发送新版本，旧版本变为 `SUPERSEDED`
5. 切换到最新方案的回应方，接受 Proposal，创建 Exchange
6. 展示双方物品已预留及交接安排
7. 双方分别确认，交换完成
8. 可选：用户 C 涉及相同物品的 PENDING 方案，在 A 与 B 接受方案并预留物品后变为 `INVALIDATED`
9. 可选：在接受方案前编辑或撤下其中一件物品，展示所有关联待回应方案变为 Invalid；重新打开方案可看到最新标题、图片和物品状态，以及失效提示。已拒绝、撤回和接受的历史方案状态不受影响。
10. 可选：原样保存物品，确认待回应方案仍有效；尝试编辑或撤下 RESERVED 物品，确认操作被禁止。

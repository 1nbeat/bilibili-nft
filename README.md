# BiliBili-NFT 数字藏品展示

一个用于展示、检索和采集 BiliBili 数字藏品数据的全栈项目。项目包含 Spring Boot 后端和 Vue 前端，支持数字藏品系列展示、系列下藏品列表、藏品详情、属性筛选、图片代理、详情页主色动态背景等功能。

项目演示地址：https://www.arifun.com

藏品系列页：
<img width="2542" height="1266" alt="image" src="https://github.com/user-attachments/assets/6a951251-4db8-4e1d-9bfe-305cd3baa4e3" />

藏品列表页：
<img width="2534" height="1264" alt="image" src="https://github.com/user-attachments/assets/03177f8d-5d74-47d7-98a2-d61543f0786b" />

藏品明细页：
<img width="2528" height="1266" alt="image" src="https://github.com/user-attachments/assets/29e888f4-8b21-4e97-a10e-f34e7b7a8415" />


## 功能特性

- 数字藏品系列展示：首页展示所有已采集的藏品系列，支持系列 ID 和系列名称查询。
- 系列封面轮播：按系列随机取藏品图片作为封面轮播。
- 藏品列表分页：进入系列后分页展示该系列下的藏品。
- 属性筛选：基于藏品详情中的 `attributesJson` 拆表后进行多属性组合筛选。
- 藏品详情页：展示藏品图片、持有人、系列信息、链上信息、描述和属性稀有度。
- 动态背景：详情页根据藏品主图提取颜色生成背景渐变。
- 图片代理：通过后端代理图片，规避部分图片防盗链问题。
- 爬虫任务：支持按系列 ID 抓取藏品列表，并联动抓取详情和属性数据。

## 技术栈

后端：

- Java 21
- Spring Boot 3.3
- MyBatis
- MySQL
- Lombok

前端：

- Vue 3
- Vite
- 原生 CSS

## 项目结构

```text
bilibili-nft
├── backend
│   ├── sql
│   │   ├── bili_nft_series.sql
│   │   ├── bili_nft_detail.sql
│   │   ├── bili_nft_item_detail.sql
│   │   └── bili_nft_item_attribute.sql
│   └── src/main
│       ├── java/com/arifun
│       └── resources
├── frontend
│   ├── public
│   └── src
│       ├── api
│       ├── assets
│       ├── components
│       ├── composables
│       ├── utils
│       └── views
└── README.md
```

## 数据库初始化

先创建 MySQL 数据库，然后依次执行 `backend/sql` 下的建表脚本：

```text
backend/sql/bili_nft_series.sql
backend/sql/bili_nft_detail.sql
backend/sql/bili_nft_item_detail.sql
backend/sql/bili_nft_item_attribute.sql
```

表说明：

- `bili_nft_series`：数字藏品系列信息。
- `bili_nft_detail`：系列下藏品列表数据。
- `bili_nft_item_detail`：单个藏品详情数据。
- `bili_nft_item_attribute`：藏品属性拆表数据，用于属性筛选。

## 后端配置

后端配置文件位于：

```text
backend/src/main/resources/application.yml
backend/src/main/resources/application-dev.yml
backend/src/main/resources/application-prd.yml
```

请在对应环境配置中修改 MySQL 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
```

默认开发环境为 `dev`。生产环境配置请自行维护，不建议提交真实数据库账号和密码。

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认后端地址：

```text
http://localhost:8080
```

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认前端地址：

```text
http://localhost:5173
```

前端开发环境通过 Vite 代理 `/api` 到后端：

```text
http://localhost:8080
```

## 常用接口

系列查询：

```http
GET /api/bili-nft-series
GET /api/bili-nft-series?itemId=1076
GET /api/bili-nft-series?itemName=盛夏
GET /api/bili-nft-series/{itemId}
GET /api/bili-nft-series/{itemId}/images
```

藏品列表：

```http
GET /api/bili-nft-details/page/item/{itemId}?pageNum=1&pageSize=20
```

藏品详情：

```http
GET /api/bili-nft-item-details/{nftId}
GET /api/bili-nft-item-details/facets?itemId=1076
POST /api/bili-nft-item-details/search
```

属性筛选请求示例：

```json
{
  "itemId": 1076,
  "pageNum": 1,
  "pageSize": 20,
  "filters": [
    {
      "traitType": "背景",
      "traitValue": "代码"
    }
  ]
}
```

爬虫任务：

```http
POST /api/spider/bilibili-nft/crawl/{itemId}
POST /api/spider/bilibili-nft/detail/{nftId}
POST /api/spider/bilibili-nft/crawl/test/{startItemId}
```

## 爬虫说明

列表爬虫会抓取指定 `itemId` 的藏品列表，并联动抓取每个藏品详情。详情数据入库时会同步拆分属性数据到 `bili_nft_item_attribute` 表，用于前端属性筛选。

批量测试接口会遍历系列 ID 范围并发抓取，请谨慎使用，避免频繁请求对源站造成压力。

## 前端页面

- `/`：数字藏品系列首页。
- `/series/{itemId}`：指定系列下的藏品列表。
- `/nft/{nftId}`：单个藏品详情页。

## 构建

前端构建：

```bash
cd frontend
npm run build
```

后端编译：

```bash
cd backend
mvn -DskipTests compile
```

## 开源说明

本项目仅用于学习、研究和个人数据展示。项目中涉及的 BiliBili、数字藏品图片、接口数据等内容，其版权和权益归原权利方所有。

使用爬虫功能时请遵守目标站点的服务条款、robots 规则以及相关法律法规。请勿用于高频请求、商业采集、绕过访问限制或其他不当用途。

## License

请根据你的实际开源计划补充许可证，例如 MIT、Apache-2.0 或其他许可证。

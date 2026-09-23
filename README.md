## 项目创建结果

全部项目位于 `SpringBoot4.1_Reference\` 下，共 **267个文件**（160个Java源码、24个YAML配置、23个XML、24个README、10个SQL、模板文件等）。

### 第1组：嵌入式服务器切换 (1个项目)

| 项目 | 路径 | 核心演示内容 |
|------|------|-------------|
| tomcat-jetty-switch | 端口8080/8081 | Maven Profiles 切换 Tomcat/Jetty，虚拟线程行为差异，配置命名空间 |

### 第2组：向量数据库 + Spring AI (5个项目)

| 项目 | 核心演示内容 |
|------|-------------|
| vectorstore-basic | SimpleVectorStore + TransformersEmbeddingModel 本地嵌入，文档摄入与相似度搜索 |
| vectorstore-advanced | 元数据过滤、文档CRUD、REST API、@Observed 可观测性、双 Profile 配置 |
| spring-ai-basic | ChatClient + OpenAI接入、Prompt模板、SSE流式、Handlebars模板变量 |
| spring-ai-advanced | Function Calling(WeatherTool)、结构化输出、自定义Advisor、Multi-Model路由、InMemory记忆 |
| vs-ai-basic | 基础RAG：摄入 → 切块 → 向量存储 → QuestionAnswerAdvisor → LLM问答 |
| vs-ai-advanced | 生产级RAG：查询改写、重排序、混合检索、评估Pipeline、Micrometer指标 |

### 第3组：MyBatis-Plus 生态 (4个项目)

| 项目 | 核心演示内容 |
|------|-------------|
| mybatis-plus-crud | IService/ServiceImpl、分页插件、MetaObjectHandler自动填充、逻辑删除 |
| mybatis-plus-generator | FastAutoGenerator 代码生成、Freemarker模板、自定义方法注入 |
| mybatis-plus-join | MPJLambdaJoinWrapper 多表联查、DTO投影、3表JOIN、分页+联查 |
| dynamic-datasource | @DS注解切换、SpEL动态数据源、读写分离、DynamicDataSourceContextHolder |

### 第4组：Spring Data JPA (2个项目)

| 项目 | 核心演示内容 |
|------|-------------|
| data-jpa-basic | Jakarta JPA注解、派生查询、@Query JPQL、@EntityGraph、审计 |
| data-jpa-advanced | @EmbeddedId复合主键、Specification动态查询、@Version乐观锁、@Lock悲观锁、Slice分页 |

### 第5组：模板引擎 (4个项目)

| 项目 | 端口 | 引擎特性 |
|------|------|---------|
| template-thymeleaf | 8082 | Natural Templates、Fragment、i18n、#temporals |
| template-freemarker | 8083 | FTLH自动转义、宏指令、模板继承 |
| template-mustache | 8084 | 逻辑无关Section、Partials、Lambda |
| template-jte | 8085 | 字节码编译、类型安全@param、热重载 |

### 第6组：显式Bean映射 (5个业务场景)

| 项目 | 端口 | 领域 | 显式映射典型场景 |
|------|------|------|----------------|
| explicit-mapping-ecommerce | 9001 | 电商 | 状态码→中文、金额格式化、多地址合并、计算字段(优惠后金额) |
| explicit-mapping-logistics | 9002 | 物流 | 手机号掩码、Haversine距离计算、GPS格式化、状态异常分级 |
| explicit-mapping-restaurant | 9003 | 餐饮 | 分→元转换、CSV过敏原解析、JSON规格展开、辣度中文映射 |
| explicit-mapping-procurement | 9004 | 采购 | 统一信用代码掩码、银行合并掩码、审批链JSON解析、紧急度中文 |
| explicit-mapping-marketing | 9005 | 营销 | 活动类型中文、CTR/CVR/ROI计算、A/B变体展示、预算百分比 |

每个项目都符合 **Spring Boot 4.1.0 + Java 21** 技术栈要求，使用 **Jakarta 命名空间**，所有代码均包含完整可运行结构（pom.xml + 主类 + 业务代码 + 配置文件 + README）。显式映射项目特别涵盖了参考文档中定义的5类"形状分歧"（改名、编码值转业务文本、计算字段、格式化、多字段合并），并结合真实电商/物流/餐饮/采购/营销业务规则，展示为什么 BeanUtils 反射只能解决名字/类型完全一致的字段，而业务规则驱动的映射只能显式声明。

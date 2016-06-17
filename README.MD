# Cascade

支持并行和子查询的前后端的http通讯框架

您只需在代码中声明实现CascadeAware,即可享用cascade所有功能

```java
@Component
// CascadeAware 是一个空接口
public class Performance implements CascadeAware {
    @Autowired
    private ShopInfoService shopInfoService;
    
    // 前端可以通过cascade({type:"Performance",params:{shopId:123}}) 调用这个方法
    public PerformanceModel query(@Param("shopId") int shopId, @Param("operator") UserContext operator) {
        return shopInfoService.getPerformanceModelByShopId(shopId, operator.getLoginId());
    }

}
```


## MAVEN

```xml
<dependency>
    <groupId>com.dianping</groupId>
    <artifactId>cascade</artifactId>
    <version>${version}</version>
</dependency>
```

version请查看 [http://mvn.dianpingoa.com/webapp/#/artifacts/browse//search/quick/eyJzZWFyY2giOiJxdWljayIsInF1ZXJ5IjoiY2FzY2FkZSJ9]

## 注册cascade

### 通过spring注册

在spring的配置文件中加入```cascadeFactory```和```cascade```

```xml
<bean class="com.dianping.cascade.factory.SpringContextCascadeFactory" id="cascadeFactory" />
<bean id="cascade" factory-bean="cascadeFactory" factory-method="create" />
```

给需要导出的class实现```CascadeAware```接口（这是一个空接口）

```java
@Component
public class Performance implements CascadeAware {
    @Autowired
    private ShopInfoService shopInfoService;

    public PerformanceModel query(@Param("shopId") int shopId, @Param("operator") UserContext operator) {
        return shopInfoService.getPerformanceModelByShopId(shopId, operator.getLoginId());
    }

}
```

### 手动注册

```java
BeansCascadeFactory beansCascadeFactory = new BeansCascadeFactory(Lists.newArrayList(
    new Cooperation(),
    new Shop(),
    new User()
), CascadeFactoryConfig.DEFAULT);

Cascade cascade = beansCascadeFactory.create();
```

## 使用cascade

```cascade```接口只有一个方法

```
public interface Cascade {
    Map process(List<Field> fields, Map contextParams);
}
```

在servlet中使用cascade

```java
public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    Object result = cascade.process(JSON.parseArray(CharStreams.toString(req.getReader()), Field.class), null);
    res.getWriter().write(JSON.toJSONString(result));
}
```

在spring mvc中使用cascade

```
@Autowired
private Cascade cascade;

@RequestMapping(value="", method = RequestMethod.POST)
@ResponseBody
public Object cascade(@RequestBody List<Field> fields) {
    final Integer loginId = LoginUtils.getLoginId();

    return cascade.process(fields, new HashMap() {{
        put("operatorId", loginId);
    }});
}
```

## API

### Map cascade.process(List<Field> fields, Map contextParams);

处理field，返回结果

参数  

- fields： ```Field```列表,请参考Field章节
- contextParams： 调用者传入的```ContextParams```,请参考ContextParams章节

## Field

- type(String)： 表示该field返回数据的类型，必填
- category(String, default=“query"l)： 表示该field在type中的分类
- as(String, optional)：这个field返回的数据将以哪个字段名挂在返回的对象中，如果没有设定的话，就看category  如果没有category的话，用type；有category的话，用$type_$category
- params(Map, optional)：额外的参数
- children(List<Field>, optional)：子属性
- props(List<String>, optional): 需要返回的属性值。默认是返回全部

一个Field例子
    
```js
[
    {
        "type": "User",
        "params":  {
            "shopId": 2334123
        },
        "category":"queryPublicSeaInfo",
        "as":"publicSeaInfo",
        "props": ["loginId", "realName"]
    },
    {
        "type": "Enum",
        "category":"salesTypeEnum",
        "as":"salesTypeEnum"
    }
]
```
    
## ContextParams
在整个cascade处理的流程中ContextParams保存了当前Field所需要的上下文信息，业务方法能通过@Param注释从它里面获取需要的字段。

每个Field在被处理的时候，它能取到的ContextParams都会包括它的上级的ContextParams和它上级的返回结果，直到最外层调用者传入的那个ContextParams

## CascadeFactoryConfig

对casade的配置,可以在声明SpringContextCascadeFactory的时候传入,例如:

```xml
<bean class="com.dianping.cascade.factory.SpringContextCascadeFactory" id="cascadeFactory" >
    <constructor-arg index="0">
        <bean class="com.dianping.cascade.CascadeFactoryConfig">
            <property name="threadCount" value="60" />
            <property name="invocationInterceptorFactories">
                <list>
                    <bean class="com.dianping.cascade.CatInterceptorFactory" />
                </list>
            </property>
        </bean>
    </constructor-arg>
</bean>
```

- threadCount: 同时处理field的线程数,默认为1,单线程
- fieldInvocationInterceptors: 关注于field的自定义处理器列表,默认为空
- methodInvocationInterceptorFactories: 根据关注于Method的自定义处理器工厂列表,默认为空

## 自定义处理器

### CatInterceptor

可以在cat上查看每个Field处理的信息


## 注释

在导入的cascade类中,我们可以通过注释来获取ContextParams中的参数

- @Param(key)

    支持泛型
    从上下文获取某个key的值

    上下文的参数包括
    
    1. params 字段传入的
    2. 它的parent的返回值（如果有的话）
    3. 它的parent的所有参数
            
    ```java
    public Collection<String> query(@Param("shopId") int shopId, @Param("operator") UserContext operator)
    ```

- @Entity

    支持泛型
    用上下文的参数构造一个bean    
        
    ```java
    public Collection<String> query(@Entity UserDTO user, @Param("operator") UserContext operator)
    ```

- @Cacheable

    缓存返回结果
            
    expireMinutes   多少分钟过期，默认永不过期
            
    ```java
    @Cacheable(expireMinutes=30)
    public UserDTO load(@Param("userId") int id)         
    ```

## 异常处理

cascade默认会对抛出的异常标注上异常出现的位置类似于 ```[User.byId] loginId -1233 not exists```

如果不想显示位置的话，请抛出 ``` com.dianping.cascade.BusinessException ```



## 客户端

- [cascade-client](http://code.dianpingoa.com/cascade/cascade-client/tree/master)

## demo
- [cascade-servlet-demo](http://code.dianpingoa.com/cascade/cascade-servlet-demo/tree/master)

  cascade-java + servlet + ajax

- [cascade-spring-demo](http://code.dianpingoa.com/cascade/cascade-spring-demo/tree/master)

  cascade-java + spring + pigeon + cascade-client



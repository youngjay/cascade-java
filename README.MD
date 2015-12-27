# cascade

## current version: 3.0


## 解决的问题
一个页面可能由多个后台模块组成，前端因为性能的关系，会要求服务端把所有的数据拼在一个请求里面返回。

这样就需要后端除了业务的模型以后，还要为这些“拼装”的视图做VO（或者DTO）。

这样做的缺点：

- 这些DTO对于业务来说是没有任何意义的，它们都只是视图的需要。
- 因为java是强类型的语言，所以会生成很多个DTO。
- 页面的变化特别快，从而导致这些DTO也是极不稳定的。
- 拼装的逻辑都是类似的

## 客户端

- [cascade-client](http://code.dianpingoa.com/cascade/cascade-client/tree/master)

## demo
- [cascade-servlet-demo](http://code.dianpingoa.com/cascade/cascade-servlet-demo/tree/master)

cascade-java + servlet + ajax

- [cascade-spring-demo](http://code.dianpingoa.com/cascade/cascade-spring-demo/tree/master)

cascade-java + spring + pigeon + cascade-client


## 方法

- register

注册处理的对象，class.getSimpleName() 作为field的type， 所有public method.getMethodName() 作为category

```
cascade.register(Object bean)
```

- process

```
cascade.process(List<Field> fields, Map contextParams)
```

处理fields，contextParams可以传入上下文数据。


## Field属性

- type(String)： 表示该field返回数据的类型，必填
- category(String, default=“query"l)： 表示该field在type中的分类
- as(String, optional)：这个field返回的数据将以哪个字段名挂在返回的对象中，如果没有设定的话，就看category  如果没有category的话，用type；有category的话，用$type_$category
- params(Map, optional)：额外的参数
- children(List<Field>, optional)：子属性
- props(List<String>, optional): 需要返回的属性值。默认是返回全部

    例子
    
    ```
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
            "as":"salesTypeEnum",
        }
    ]
    ```
    
## 注释

- @Param(key)
    从上下文获取某个key的值

    上下文的参数包括
    
    1. params 字段传入的
    2. 它的parent的返回值（如果有的话）
    3. 它的parent的所有参数
    
        
        ```
         public Collection<String> query(@Param("shopId") int shopId, @Param("operator") UserContext operator)
        ```

        这些参数都会被组织成map的形式，可以通过@Param("key")去获取

- @Entity

    用上下文的参数构造一个bean
    
        
    ```
     public Collection<String> query(@Entity UserDTO user, @Param("operator") UserContext operator)
    ```

- @Cacheable
    此方法的返回结果可以被缓存
    
    参数:
    
        - expireHours: int
            
            可以被缓存多少小时，默认1
            
    ```
    @Cacheable
    public UserDTO load(@Param("userId") int id) 
        
    ```

## 异常处理

cascade默认会对抛出的异常标注上异常出现的位置类似于 ```[User.byId] loginId -1233 not exists```

如果不想显示位置的话，请抛出 ``` com.dianping.cascade.BusinessException ```

## 开始使用
- 先引入maven

```
<dependency>
    <groupId>com.dianping</groupId>
    <artifactId>cascade</artifactId>
    <version>${version}</version>
</dependency>
```

[cascade version] (http://mvn.dianpingoa.com/webapp/search/artifact/?1&q=cascade)


- 注册cascade

如果与spring集成,只要简单的配置2个bean即可

```
<bean class="com.dianping.cascade.spring.CascadeFactory" id="cascadeFactory" />
<bean id="cascade" factory-bean="cascadeFactory" factory-method="create"  />
```

然后给需要注入的bean实现 CascadeAware接口作为标识,例如

```
@Component
public class Action implements CascadeAware {
    @Autowired
    private ModuleConfigService moduleConfigService;


    public Collection<String> query(@Param("shopId") int shopId, @Param("operator") UserContext operator) {
        return moduleConfigService.getActionModuleList(shopId, operator.getSerialNumber(), operator.getLoginId());
    }
}
```

即可

如果没有用spring的话,需要自己创建cascade实例,并且手工调用register方法了. 具体实现可以参考 [cascade-servlet-demo](http://code.dianpingoa.com/cascade/cascade-servlet-demo/tree/master)


- 调用cascade


spring例子

```
public class CascadeController {
    @Autowired
    private Cascade cascade;

    @RequestMapping(value="", method = RequestMethod.POST)
    @WrapWithStatus
    @ResponseBody
    public Object cascade(@RequestBody List<Field> fields) {
        final Integer loginId = LoginUtils.getLoginId();

        return cascade.process(fields, new HashMap() {{
           put("operatorId", loginId);
        }});
    }
}
```


struts例子


```
public class CascadeAction extends AjaxBaseWithStatus {
    @Autowired
    @Qualifier("cascade")
    private Cascade cascade;

    public String query() {
        HttpServletRequest request = ServletActionContext.getRequest();

        List<Field> fields;

        try {
            fields = JSON.parseArray(readString(request.getReader()), Field.class);
        } catch (IOException ex) {
            throw new RuntimeException("cascade field 格式错误");
        }

        Object result = cascade.process(fields, new HashMap() {{
            put("operator", ContextHolder.getUserContext());
        }});

        setResponseData(result);

        return SUCCESS;

    }

    private String readString(Reader reader) {
        // read content
        BufferedReader bufferReader = new BufferedReader(reader);
        String line;
        StringBuilder buffer = new StringBuilder();

        try {
            while ((line = bufferReader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buffer.toString();
    }
}

```






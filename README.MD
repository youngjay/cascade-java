# cascade


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


## cascade处理方式

- 注册typehandler

```
cascade.register(String type, Object handler)
```

- 处理数据

```
cascade.process(List<Field> fields)
```

fields的数据内容类似于

```
[
    {
        "type": "User",
        "params":  {
            "shopId": 2334123
        },
        "category":"queryPublicSeaInfo",
        "as":"publicSeaInfo"
    },
    {
        "type": "Enum",
        "category":"salesTypeEnum",
        "as":"salesTypeEnum",
    }
]
```

cascade会根据之前注册的type自动分发到对应的对象的category指定的方法（如果不指定就是“query”方法）

然后把每个方法的返回值拼装起来，汇总之后整个返回

## 优点
- 服务端开发人员只要实现每个业务的方法就好了,不需要考虑页面的需求
- 页面随时可以根据需求增加或者减少字段,而不用知会后端
- 协议是固定的,不用记多个url


## 开始使用
- 先引入maven

```
<dependency>
    <groupId>com.dianping</groupId>
    <artifactId>cascade</artifactId>
    <version>2.0</version>
</dependency>
```

- 注册cascade

如果与spring继承的话,只要简单的配置2个bean即可

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

- 通过@Param("xxx") 获取参数

每个方法可以获取上下文的参数包括

1. params 字段传入的
2. 它的parent的返回值（如果有的话）
3. 它的parent的所有参数

这些参数都会被组织成map的形式，可以通过@Param("key")去获取

- 异常处理

cascade默认会对抛出的异常标注上异常出现的位置类似于 ```[User.byId] loginId -1233 not exists```

如果不想显示位置的话，请抛出 ``` com.dianping.cascade.BusinessException ```



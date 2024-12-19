# Spring 事务[^1]

## 声明式事务 vs 编程式事务

### 编程式

通过代码的方式告知 JVM 需要做什么。需要自行编程实现

#### 优点

排错简单

#### 缺点

代码量大

### 声明式

通过注解等方式告知框架需要做什么，框架将会完成余下工作

**Spring** 中的事务管理是**声明式**的。

#### 优点

代码量小，可读性高

#### 缺点

封装太多，所以通过 debug 排错时需要深入底层，排错困难



## Module 准备

1. create module in SpringBoot Starter
2. because need to use Mysql, need to import JDBC & MySql driver from springboot starter
3. can also use parent module to manage dependencies by using label modules



## Basic Database Operation

Spring 提供了简易的数据库连接方法，在 application.properties 中，使用 spring.datasource 即可完成数据库配置信息的配置。

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/spring_tx
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### DataSource

当完成 datasource 的配置之后，Spring 会自动在容器中生成一个 `DataSource` 数据源对象，使用自动装配注入后即可使用。

```java
@SpringBootTest
class Spring03TxApplicationTests {
    @Autowired
    DataSource dataSource;

    @Test
    public void test1() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
    }
}
```

*output*:	**Hikari**ProxyConnection@885876140 wrapping com.mysql.cj.jdbc.ConnectionImpl@35c9a231

Spring 默认使用市面最快的 Hikari 数据源。

### JdbcTemplate 

与此同时，Spring 还提供了专门用于数据库增删改查的 `JdbcTemplate` 对象，其中内置一系列 api 可供调用，依旧是自动装配即可用。

```java
@Component
public class BookDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Book getBookById(Integer id) {
        String sql = "select * from book where id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Book.class), id);
    }
}
```

```java
@SpringBootTest
class Spring03TxApplicationTests {
    @Autowired
    DataSource dataSource;
    @Autowired
    BookDao bookDao;

    @Test
    public void test1() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
    }

    @Test
    public void test2() throws SQLException {
        Book bookById = bookDao.getBookById(1);
        System.out.println(bookById);
    }
}
```

*output*:	Book(id=1, bookName=剑指Java, price=100.00, stock=100)

#### queryForObject()

```java
public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args) throws DataAccessException{}
```

有三个参数：

1. sql 可选 **有/无占位符**版本 的，如果用占位符，可用第三个可变参数形参传入。

2. RowMapper 是一个接口，用于描述查询结果与 Javabean 的对应关系。在其下有一个实现类为 **BeanPropertyRowMapper**，用于将 封装好的 JavaBean 对象的每个属性 与 数据库一行中的每一列 完成映射，其参数传入 JavaBean.class 即可。

3. 可变参数列表用于给占位符赋值。

#### update()

##### 添加

```java
public class BookDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addBook(Book book){
        String sql = "insert into book(bookName, price, stock) values(?,?,?)";
        jdbcTemplate.update(sql, book.getBookName(), book.getPrice(), book.getStock());
    }
}
```

##### 修改

```java
@Component
public class BookDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateBookDivideStock(Integer id, Integer stock) {
        String sql = "update book set stock = stock - ? where id = ?";
        jdbcTemplate.update(sql, stock, id);
    }
}
```

##### 删除

```java
@Component
public class BookDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

		public void deleteBookById(Integer id) {
        String sql = "delete from book where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
```



## 无事务参与的交易

```java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    BookDao bookDao;
    @Autowired
    AccountDao accountDao;

    @Override
    public void checkOut(String username, Integer bookId, Integer boughtBookNumber) {
        // 1. 查询图书信息
        Book book = bookDao.getBookById(bookId);
        // 2. 计算价格
        BigDecimal price = book.getPrice();
        BigDecimal howMuch = new BigDecimal(boughtBookNumber).multiply(price);
        // 3. 扣钱
        accountDao.updateAccountDividedBalanceByUsername(username, howMuch);
        // 3. 减库存
        bookDao.updateBookDivideStock(bookId,boughtBookNumber);
    }
}
```

1、2、3 步应连续完成，若其中有多线程执行，可能会导致数据错误。



## @Transactional

1. 首先，想要开启事务功能，需要使用 @EnableTransactionManagement 注解，来**开启基于注解的自动化事务管理**。

   可以标记在主程序上。

   ```java
   @SpringBootApplication
   @EnableTransactionManagement
   public class Spring03TxApplication {
       public static void main(String[] args) {
           SpringApplication.run(Spring03TxApplication.class, args);
       }
   }
   ```

2. 为想要开启事务的方法上标记 @Transactional

   ```java
   @Override
   @Transactional
   public void checkOut(String username, Integer bookId, Integer boughtBookNumber) {
       // 1. 查询图书信息
       Book book = bookDao.getBookById(bookId);
       // 2. 减余额
       BigDecimal price = book.getPrice();
       BigDecimal howMuch = new BigDecimal(boughtBookNumber).multiply(price);
       accountDao.updateAccountDividedBalanceByUsername(username, howMuch);
       // 3. 减库存
       bookDao.updateBookDivideStock(bookId,boughtBookNumber);
   }
   ```

   此时事务管理即开启开启，可除 0 或 throw Exception 测试。

### Spring 事务原理

Spring 底层有一个Transaction Interceptor 事务拦截器切面，还有 TransactionalManager 事务管理器，由这两个器完成事务的管理。

事务管理器只定义（控制）了事务的提交和回滚，事务管理器的调用由切面实现，由事务拦截器切面控制何时提交、何时回滚。即方法前调用 getTransaction()，感知到执行正确调用 commit()，异常则调用 rollback()

### TransactionalManager

> @Transactional 的属性

用于指定容器中的某个 bean 作为事务管理器，为接口，实现类下有三个方法控制事务的获取、提交和回滚。

Spring 底层默认使用 JDBCTransactionalManager

- TransactionalStatus 用于封装事务信息，getTransactional() 用于获取该对象
- commit() 用于提交
- rollback() 用于回滚

### Propagation 传播行为[^2]

Spring 事务传播机制是指，包含多个事务的方法在相互调用时，事务是如何在这些方法间传播的。

既然是“事务传播”，所以事务的数量应该在两个或两个以上，Spring 事务传播机制的诞生是为了规定多个事务在传播过程中的行为的。

**比如方法 A 开启了事务，而在执行过程中又调用了开启事务的 B 方法，那么 B 方法的事务是应该加入到 A 事务当中呢？还是两个事务相互执行互不影响，又或者是将 B 事务嵌套到 A 事务中执行呢？所以这个时候就需要一个机制来规定和约束这两个事务的行为，这就是 Spring 事务传播机制所解决的问题。**



Spring 事务传播机制可使用 @Transactional(propagation=Propagation.REQUIRED) 来定义，Spring 事务传播机制的级别包含以下 7 种：

1. **Propagation.REQUIRED**（需要，总得有一个）：

   默认的事务传播级别，它表示如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。

2. Propagation.SUPPORTS（支持，可以有也可以没有）：

   如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。

3. Propagation.MANDATORY：（强制，有就用，没有就报错）

   如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。

4. **Propagation.REQUIRES_NEW**（总是需要新的）：

   表示创建一个新的事务，如果当前存在事务，则把当前事务挂起。也就是说不管外部方法是否开启事务，Propagation.REQUIRES_NEW 修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。

5. Propagation.NOT_SUPPORTED（不支持，暂停当前事务运行）：

   以非事务方式运行，如果当前存在事务，则把当前事务挂起。

6. Propagation.NEVER（拒绝，有事务就报错）：

   以非事务方式运行，如果当前存在事务，则抛出异常。

7. Propagation.NESTED（基于保存点）：

   基于**保存点/存档点**。

   如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于 PROPAGATION_REQUIRED。

#### 案例

在结账方法中实现：发生异常时，金额的扣减回滚，但库存不回滚

```java
@Transactional
checkout(){
		扣减金额; // required，和大事务绑定，大事务回滚时一同回滚
    扣减库存; // requires_new，创建单独事务，其不参与大事务的回滚
    i = 10/0; // 异常导致外层事务回滚
}
```

由案例得：对传播行为预期的设置应当放在小事务上，用于规范小事务的执行方式

#### 异常的传播链

```java
@Transactional
checkout(){
		扣减金额; // required
    扣减库存 &  i = 10/0; // requires_new，但异常发生在该小事务内
}
```

逻辑上，requires_new 由于是一个新的事务，所以它的回滚不会导致外层大事务的回滚，自然也不会影响到 required 的扣减金额的回滚。但由于 Java 的异常是向上抛出的，扣减金额虽然是独立事务，但由于它异常时会将异常抛给外层，外层接收到异常也会发生回滚，从而导致应当不回滚的 required 也随之回滚了。

所以**应当额外的关注异常的传播链。**

#### 参数设置项的传播

以 timeout 为例，

如果内层事务是 required，则其参数设置项失效，自动使用大事务的，因为其加入了大事务。

如果内层事务是 requires_new，由于其使用了新的事务，则内层事务使用其自己的参数设置项。

### isolation 隔离级别

> 控制读的。因为涉及到**修改**时数据库底层设计就会加**锁**，即使并发修改也无需担心数据安全。

1. Read Uncommitted 读未提交

   事务可以读取 未被提交 的数据，易产生脏读、不可重复读、幻读等问题

2. Read Committed 读已提交

   事务只能读取 已经提交 的数据，可以避免脏读，但可能引发不可重复读和幻读

3. Repeatable Read 可重复读 

   同一事务期间多次重复读取的数据相同，可以避免脏读和不可重复读，但仍然有幻读的情况发生

4. Serializable 串行化

   最高的隔离级别，完全禁止了并发，只允许一个事务执行完毕后才能执行另一个事务

### timeout 控制事务超时时间

一旦超过约定时间，事务即视为回滚。

@Transactional 注解下有 timeout 和 stringTimeOut 两种，分别是以 int 和 String 传入 **秒数** 的。

超时时间指：从方法进入，到最后一次数据库操作结束的时间。

### readOnly

如果事务是只读的，那么就可以将 readOnly 设置为 true，开启底层对于只读事务的自动优化。

### rollBackFor

> 用于额外指定哪些异常需要回滚

对于异常有一个**默认的回滚机制**：**运行时异常回滚、编译时异常不回滚**。

rollBackFor 是用来将默认不回滚的编译时异常手动设置回滚的。

当设置了 rollBackFor 时，需要回滚的异常就变成了：运行时异常 + 手动指定的编译时异常

使用 rollBackFor 指定哪些异常需要被回滚，此处参数应当为 Throwable 的类。另有一个可以使用字符串传入类名的版本，按下不表。

### noRollBackFor

> 额外指明哪些异常不需要回滚

编译时异常 + 额外指明的运行时异常 = 不回滚



------



## 引用

遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

[^1]: https://www.bilibili.com/video/BV14WtLeDEit/?p=33&share_source=copy_web&vd_source=732a79db14c78dbec659a1afbe66586e

[^2]: https://www.cnblogs.com/vipstone/p/16735893.html

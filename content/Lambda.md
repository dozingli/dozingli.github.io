> [[函数对象]] = [[参数]] -> [[逻辑部分]]
> 
> 如果[[逻辑部分]]只有一行，则[[逻辑部分]]整个会被作为返回结果

`student.age < 18` 的[[函数对象]]为， `student -> student.age < 18`

`student.sex.equals('M')` 的[[函数对象]]为，`student -> student.sex.equals('M')`

有[[函数对象]]，则需要一个[[函数式接口]]用于执行该对象。由于上述函数对象的形参均为 Student 对象，返回值均为布尔类型，所以[[函数式接口]]方法应当匹配。

若明确指出[[参数]]类型，[[逻辑部分]]只有一行，自动作为返回值
   
`(int a, int b) -> {int c = a + b; return c;}` 
   
代码多余一行，需要括号及 return
   
`(a, b) -> a + b;` 
   
通过代码上下文（上下文中其接口）能确认[[参数]]、返回值类型，则省略不写。该[[函数对象]]的类型是其[[函数式接口]]类型。
   
```java
   interface Lambda{
	   int op(int a, int b);
   }

	Lambda lambda = (a, b) -> a + b;
```

#Lambda 
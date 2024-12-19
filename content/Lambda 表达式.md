   明确指出[[参数]]类型，[[逻辑部分]]只有一行，自动作为返回值
   
2. `(int a, int b) -> {int c = a + b; return c;}` 
   
   代码多余一行，需要括号及 return
   
3. `(a, b) -> a + b;` 
   
   通过代码上下文（上下文中其接口）能确认[[参数]]、返回值类型，则省略不写。该[[函数对象]]的类型是其[[函数式接口]]类型。
   
   ```java
   interface Lambda{
	   int op(int a, int b);
   }

	Lambda lambda = (a, b) -> a + b;
	```

#Lambda 
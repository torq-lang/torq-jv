
## Other structs

```
package otherexamples

type StreamResponse[T] = Array[T] | eof#{'more': Bool}

type MyTuple = Tuple[Int64, Str | Bool]

type MyArray = Array[Int64]

var x... = [0, 1, 2, 3]

var y

// This is a "rest" syntax for a variable number of arguments
func untypedFunc(params::Any...) -> Any in
    skip
end

// This is an untyped "rest" syntax for a variable number of arguments
func untypedFunc(params...) -> Any in
    skip
end

func funcThatTakesManyParams(a, b, c) -> Any in
    skip
end

func funcThatTakesOnePattern({'name': name, 'age': age}) -> Any in
    skip
end

func funcThatTakesOnePattern[T]({'name': name, 'age': age, 'address': T}) -> Any in
    skip
end

func funcThatTakesOnePattern[T]({'name': name, 'age': age, 'address': T, ...}) -> Any in
    skip
end

// This is an untyped "rest" syntax for a variable number of record fields
func funcThatTakesOnePattern[T]({'name': name, 'age': age, 'address': T, rest...}) -> Any in
    skip
end

// This is an untyped "rest" syntax for a variable number of tuple fields
func funcThatTakesOnePattern[T]([a, b, c, rest...]) -> Any in
    skip
end
```
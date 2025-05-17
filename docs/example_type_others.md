
## Other structs

```
package otherexamples

type StreamResponse[T] = Array[T] | eof#{'more': Bool}

type MyTuple = Tuple[Int64, Str | Bool]

type MyArray = Array[Int64]

var x = [0, 1, 2, 3]

var y

func untypedFunc(params::Array[Any]) -> Any in
    skip
end

func untypedFunc(params::Array) -> Any in
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

func funcThatTakesOnePattern[T]({'name': name, 'age': age, 'address': T, ...}) -> Any in
    skip
end

func funcThatTakesOnePattern[T]([a, b, c, ...]) -> Any in
    skip
end
```

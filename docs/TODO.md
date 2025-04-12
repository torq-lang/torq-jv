# To Do

## Checklist

- Move documentation
  - Remove temporary documents from `/docs` directory
  - Move permanent documentation to `torq-jv-book`
  - Keep project documentation, such as `SETUP.md` 
- Migrate all examples to modules
    - For example, `OrdersHandler.torq`
- NorthwindServer
  - Change to use modules
  - Change to use `Compiler` and `SourceBroker`
- Packs
  - `Str`, `ArrayList`, `Timer`, etc.
  - Create Torq native references
- CommonTools
  - Are they useful enough to keep?
- Benchmark tests
  - Are they meaningful? If not, replace them.
- isWeakKeyword
  - Is it needed outside of testing? 
  - Currently, where a weak keyword may be found, we have used `isIdent(<<keyword>>)`
  - Weak keywords must be detected accurately in syntax highlighters or IDE tools

## Stream protocol

Before we can complete `Timer`, we must enhance the stream protocol.

### Timer Before

```
var timer_pub = spawn(Timer.cfg(1, 'seconds'))
var tick_count = Cell.new(0)
var timer_stream = Stream.new(timer_pub, 'request'#{'ticks': 5})
for tick in Iter.new(timer_stream) do
    tick_count := @tick_count + 1
end
@tick_count
```

### Timer After

Note:
1) The `Stream` class is no longer needed
2) The `stream` handler creates and binds a `StreamRefObj`
3) A `StreamRefObj` is a `ValueIterSource`

```
var timer_pub = spawn(Timer.cfg(1, 'seconds'))
var tick_count = Cell.new(0)
var timer_stream = timer_pub.stream('request'#{'ticks': 5})
for tick in ValueIter.new(timer_stream) do
    tick_count := @tick_count + 1
end    
@tick_count
```

### Int Streams

```
actor IntPublisher(first, last, incr) in
    import system[ArrayList, Cell]
    import system.Procs.respond
    var next_int = Cell.new(first)
    handle stream 'request'#{'count': n} -> Int32[] | eof#{'more': Bool} in
        func calculate_to() in
            var to = @next_int + (n - 1) * incr
            if to < last then to else last end
        end
        var response = ArrayList.new()
        var to = calculate_to()
        while @next_int <= to do
            response.add(@next_int)
            next_int := @next_int + incr
        end
        if response.size() > 0 then
            respond(response.to_tuple())
        end
        if @next_int <= last then
            eof#{'more': true}
        else
            eof#{'more': false}
        end
    end
end
```

#### Consumer Before

```
actor SumOddIntsStream() in
    import system[Cell, Stream, ValueIter]
    import examples.IntPublisher
    handle ask 'sum'#{'first': first, 'last': last} in
        var sum = Cell.new(0)
        var int_pub = spawn(IntPublisher.cfg(first, last, 1))
        var int_stream = Stream.new(int_pub, 'request'#{'count': 3})
        for i in ValueIter.new(int_stream) do
            if i % 2 != 0 then sum := @sum + i end
        end
        @sum
    end
end
```

#### Consumer After

You can generate demand automatically by using `StreamIter`:

```
actor SumOddIntsStream() in
    import system[Cell, StreamIter]
    import examples.IntPublisher
    handle ask 'sum'#{'first': first, 'last': last} in
        var sum = Cell.new(0)
        var int_pub = spawn(IntPublisher.cfg(first, last, 1))
        for i in StreamIter.new(int_pub, 'request'#{'count': 3}) do
            if i % 2 != 0 then sum := @sum + i end
        end
        @sum
    end
end
```

You can handle all demand details by using `ValueIter`:

```
actor SumOddIntsStream() in
    import system[Cell, ValueIter]
    import examples.IntPublisher
    handle ask 'sum'#{'first': first, 'last': last} in
        var sum = Cell.new(0)
        var int_pub = spawn(IntPublisher.cfg(first, last, 1))
        var int_stream = int_pub.stream('request'#{'count': 3})
        while int_stream.has_more() do
            for i in ValueIter.new(int_stream) do
                if i % 2 != 0 then sum := @sum + i end
            end
            int_pub.stream('request'#{'count': 3})
        end
        @sum
    end
end
```

## Composites

Working through type definitions has solidified composites as either "structures" or "objects". Structures are more akin to algebraic types where we have product (tuple and record) and sum types (disjoint union).

- Composite -- defines label and fields (feature-value pairs) as a type with selection
    - Struct -- support unification and entailment
        - Array -- no label, Int32 features starting at 0, undetermined size, single value type
        - Rec -- label, mixed features, determined size, multiple value types
        - Tuple -- label, Int32 features starting at 0, determined size, multiple value types
    - Obj -- no unification, requires equals and hash_code, and can have hidden state

## Document how records can encode XML and JSON

### XML

```
<note date="2025-01-01">
    <from>Bob</from>
    <to>Sue</to>
</note>

'note'#{'date'='2025-01-01',
    'from'#{'Bob'},
    'to'#{'Sue'},
}
```

## torq.g4
```
Next version changes:
    [X] 'new' operator for instance creation
    [ ] Module support
    [ ] Package statement
    [ ] Metadata support
    [ ] Cast expressions, e.g. ident::Int32
    [ ] Spread expressions, e.g. {customer..., person...}
    [ ] protocol statements
    [ ] type statements (Obj is a marker type)
    [ ] Actor implements
    [ ] Stream handlers
    [ ] Type parameters on actor, func, and proc
    [ ] Import is now 'package.{' instead of 'package['
    [ ] Record "rest" patterns, e.g. {'name': name, rest...}
    [ ] Tuple "rest" patterns, e.g. [value1, value2, rest...}
    [X] Dangling commas in record and tuple values, e.g. {'name': name,}
    [ ] Variable input arguments, e.g. func MyFunc(params::Any...) -> Any
    [ ] Type expressions for protocol, record, and tuple
    [ ] Type extension (+) and union (|)
    [ ] Array type constructors, e.g. Array[Int32] or Array[Array[Int32]]
    [ ] Native actor declarations
    [ ] Weak keywords: 'as' | 'ask' | 'handle' | 'implements' | 'meta' | 'native' | 'protocol' | 'stream' | 'tell'
```

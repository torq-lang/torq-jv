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
- Modules
  - `Str`, `ArrayList`, `Timer`, etc.
  - Create native Java references
- CommonTools
  - Are they useful enough to keep?
- Benchmark tests
  - Are they meaningful? If not, replace them.
- isWeakKeyword
  - Is it needed outside of testing? 
  - Currently, where a weak keyword may be found, we have used `isIdent(<<keyword>>)`
  - Weak keywords must be detected accurately in syntax highlighters or IDE tools

## Compiler

Automatically build examples router for:

```
"/customers", CustomersHandler
"/customers/{id::Int32}", CustomersHandler
"/employees", EmployeesHandler
"/employees/{id}", EmployeesHandler
"/orders", OrdersHandlerImage
"/orders/{id}", OrdersHandler
"/orders/{id}/details", OrdersHandler
"/products", ProductsHandler
"/products/{id}", ProductsHandler
"/suppliers", SuppliersHandlerImage
"/suppliers/{id}", SuppliersHandler
```

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
            respond(response.to_array())
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
    import system.lang.{Cell, ValueIter}
    import examples.IntPublisher
    handle ask 'sum'#{'first': first, 'last': last} in
        var sum = new Cell(0)
        var int_pub = spawn(new IntPublisher(first, last, 1))
        var int_stream = new Cell(int_pub.stream('request'#{'count': 3}))
        while @int_stream.has_more() do
            for i in new ValueIter(int_stream) do
                if i % 2 != 0 then sum := @sum + i end
            end
            int_stream := int_pub.stream('request'#{'count': 3})
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

## `type_of` operator

```
type_of(<<expression>>) -> Str | Rec | Tuple 
```

What structures does the `type_of` operator render?

HERE --> ///////////////////////////// UNION VS ENUMERATION????????
REMEMBER --> WE ARE STRUCTURALLY TYPED

- The scalar name if the expression is a scalar type
- A record of specifics if the expression is a record type where the label is `'Rec'` and the features are `'label'` and `'fields'`
- A tuple of scalar values if the expression is an enumeration of values where the label is the first common supertype
- A tuple of types if the expression is union type

Examples:

```
var x::Str
type_of(x) = 'Str'

type_of('Bob') = 'Str'#['Bob']
type_of('Sue') = 'Str'#['Sue']

var x::Bool
type_of(x) = 'Bool'

type_of(true) = 'Bool'#[true]
type_of(false) = 'Bool'#[false]

type_of(Rec) = 'Rec'

type Customer = {
    'name': Str,
    'phone': Int32
}
var x::Customer
type_of(x) = 'Rec'#{
    'label': 'Null',
    'fields': {
      'name': 'Str',
      'phone': 'Int32'
    },
}

var x::Array[Int32]
type_of(x) = 'Array'#{
    'type': 'Int32'
}

type Customer = {
    'name': Str,
    'phone': Int32,
    'address': {
        'street': Str,
        'city': Str
    },
}
var x::Customer
type_of(x) = 'Rec'#{
    'label': 'Null',
    'fields': {
      'name': 'Str',
      'phone': 'Int32',
      'address': 'Rec'#{
          'label': 'Null',
          'fields': {
              'street': 'Str',
              'city': 'Str'
          }
      }
    },
}

var x::Array[Customer]
type_of(x) = 'Array'#{
    'type': 'Rec'#{
        'label': 'Null',
        'fields': {
            'name': 'Str',
            'phone': 'Int32',
            'address': 'Rec'#{
                'label': 'Null',
                'fields': {
                    'street': 'Str',
                    'city': 'Str'
                }
            },
        }
    }
}

type Customer = {
    'name': Str,
    'phone': Int32,
    'address': {
        'street': Str,
        'city': Str
    }
}
type Account = {
  'account': Str
}
type CustomerWithAccount = Customer & Account

// Note that we are structurally typed 
var x::CustomerWithAccount
type_of(x) = 'Rec'#{
    'label': 'Null',
    'fields': {
      'name': 'Str',
      'phone': 'Int32',
      'address': 'Rec'#{
          'label': 'Null',
          'fields': {
              'street': 'Str',
              'city': 'Str'
          }
      }
      'account': 'Str',
    }
}

var x::'Bob'|'Sue'
type_of(x) = 'Union'#[
    'Str'#['Bob'],
    'Str'#['Sue'],
]

type Person = {
    'name': Str,
    'phone': Int32,
    'address': {
        'street': Str,
        'city': Str
    }
}
type Company = {
    'tax_id': Str
    'legal_name': Str,
    'phone': Int32,
    'address': {
        'street': Str,
        'city': Str
    }
}
type PersonOrCompany = Person | Company
var x::PersonOrCompany
type_of(x) = 'Union'#[
    'Rec'#{
        'label': 'Null',
        'fields': {
          'name': 'Str',
          'phone': 'Int32',
          'address': 'Rec'#{
              'label': 'Null',
              'fields': {
                  'street': 'Str',
                  'city': 'Str'
              }
          }
        },
    },
    'Rec'#{
        'label': 'Null',
        'fields': {
          'tax_id': 'Str',
          'legal_name': 'Str',
          'phone': 'Int32',
          'address': 'Rec'#{
              'label': 'Null',
              'fields': {
                  'street': 'Str',
                  'city': 'Str'
              }
          }
        },
    },
]

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

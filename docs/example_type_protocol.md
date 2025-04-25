
## Type protocol

```
package orderentry

type StreamResponse[T] = Array[T] | eof#{'more': Bool}

protocol OrdersStreamApi = {
    ask 'openOrdersStream'#{'fromInclusive': Date, 'toInclusive': Date} -> Token,
    stream 'nextOrders'#{'id': Token, 'count': Int32} -> StreamResponse[T],
    ask 'closeOrdersStream'#{'id': Token} -> Bool
}

protocol CustomersApi = OrdersStreamApi & {
    ask 'findById'#{'id': Str} -> Customer,
    tell 'notify'#{'message': Str},
}

actor CustomersApiHandler() implements CustomersApi in
    handle ask 'openOrdersStream'#{'fromInclusive': Date, 'toInclusive': Date} -> Token in
        skip
    end
    handle stream 'nextOrders'#{'id': Token, 'count': Int32} -> StreamResponse[T] in
        skip
    end
    handle ask 'closeOrdersStream'#{'id': Token} -> Bool in
        skip
    end
    handle ask 'findById'#{'id': Str} -> Customer in
        skip
    end
    handle tell 'notify'#{'message': Str} in
        skip
    end
end

actor CustomersApiHandler() implements
    {
        ask 'openOrdersStream'#{'fromInclusive': Date, 'toInclusive': Date} -> Token,
        stream 'nextOrders'#{'id': Token, 'count': Int32} -> Array[Order] | eof#{'more': Bool},
        ask 'closeOrdersStream'#{'id': Token} -> Bool,
        ask 'findById'#{'id': Str} -> Customer,
        tell 'notify'#{'message': Str},
    }
in
    handle ask 'openOrdersStream'#{'fromInclusive': Date, 'toInclusive': Date} -> Token in
        skip
    end
    handle stream 'nextOrders'#{'id': Token, 'count': Int32} -> Array[Order] | eof#{'more': Bool} in
        skip
    end
    handle ask 'closeOrdersStream'#{'id': Token} -> Bool in
        skip
    end
    handle ask 'findById'#{'id': Str} -> Customer in
        skip
    end
    handle tell 'notify'#{'message': Str} in
        skip
    end
end
```
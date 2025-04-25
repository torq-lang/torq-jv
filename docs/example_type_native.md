
## Native actor

```
package com.example.customers

import system.StreamResponse

protocol OrdersStreamApi = {
    ask 'openOrdersStream'#{'fromInclusive': Date, 'toInclusive': Date} -> Token,
    stream 'nextOrders'#{'id': Token, 'count': Int32} -> StreamResponse[Order],
    ask 'closeOrdersStream'#{'id': Token} -> Bool
}

protocol CustomersApi = OrdersStreamApi & {
    ask 'findById'#{'id': Str} -> Customer,
    tell 'notify'#{'message': Str},
}

meta#{'export': true, 'stereotype': 'API_HANDLER', 'native': 'com.example.customers.CustomersPack.CustomersApiHandler'}
actor CustomersApiHandler() implements CustomersApi in
    native
end
```
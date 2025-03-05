
## Native actor

```
package com.example.customers

import system.StreamResponse

protocol OrdersStreamApi = {
    handle ask 'openOrdersStream'#{'fromInclusive': Date, 'toInclusive': Date} -> Token,
    handle stream 'nextOrders'#{'id': Token, 'count': Int32} -> StreamResponse[Order],
    handle ask 'closeOrdersStream'#{'id': Token} -> Bool
}

protocol CustomersApi = OrdersStreamApi & {
    handle ask 'findById'#{'id': Str} -> Customer,
    handle tell 'notify'#{'message': Str},
}

meta#{'export': true, 'stereotype': 'API_HANDLER', 'native': 'com.example.customers.CustomersPack.CustomersApiHandler'}
actor CustomersApiHandler() implements CustomersApi in
    native
end
```
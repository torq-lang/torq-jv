
## Type: Objects

```
package orderentry.customers

type QueryWithOverloads = Obj & {
    // Essentially, this is an untyped method
    'query': func(params::Any...) -> Any,
}

// Used to add features {'equals': func (other: Any) -> Bool, 'hash_code': () -> Int32}
import system.Obj

type Person = Obj & {
    'name': func () -> Str,
    'set_name': proc (name::Str),
    'age': func () -> Int32,
    'set_age': proc (age::Int32),
    'email': func () -> Str,
    'set_email': proc (email::Str)
}

func create_bob() -> Person in
    var _name::Cell[Str] = new Cell('')
    var _age::Cell[Int32] = new Cell(-1)
    var _email::Cell[Str] = new Cell('')
    func _equals(other::Any) -> Bool in
        if typeof(other) == Person then
            var that = other::Person
            @_name == other.name()
        else
            false
        end
    end
    func _hash_code() -> Int64 in
        @_name.hash_code()
    end
    {
        'name': func () -> Str in @_name end,
        'set_name': proc (name::Str) in _name := name end,
        'age': func () -> Int32 in @_age end,
        'set_age': proc (age::Int32) in _age := age end,
        'email': func () -> Str in @_email end,
        'set_email': proc (email::Str) in _email := email end,
        'equals': _equals,
        'hash_code': _hash_code,
    }
end

type PersonCustomer = Person & {
    'account': func () -> Str,
    'set_account': proc (account::Str)
}

// ----- OR -----

type Customer = {
    'account': func () -> Str,
    'set_account': proc (account::Str)
}

type PersonCustomer = Person & Customer

func create_person_customer() -> PersonCustomer in
    var _name::Cell[Str] = new Cell('')
    var _age::Cell[Int32] = new Cell(-1)
    var _email::Cell[Str] = new Cell('')
    var _account::Cell[Str] = new Cell('')
    func _equals(other::Any) -> Bool in
        if typeof(other) == Person then
            var that = other::Person
            @_name == other.name()
        else
            false
        end
    end
    func _hash_code() -> Int64 in
        @_name.hash_code()
    end
    {
        'name': func () -> Str in @_name end,
        'set_name': proc (name::Str) in _name := name end,
        'age': func () -> Int32 in @_age end,
        'set_age': proc (age::Int32) in _age := age end,
        'email': func () -> Str in @_email end,
        'set_email': proc (email::Str) in _email := email end,
        'account': func () -> Str in @_account end,
        'set_account': proc (account::Str) in _account := account end,
        'equals': _equals,
        'hash_code': _hash_code,
    }
end
```

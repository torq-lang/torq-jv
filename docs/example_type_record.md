
## Type record

```
package orderentry

type Person = {
    'name': Str,
    'age': Int32,
    'email': Str
}

func create_bob() -> Person in
    {'name': 'Bob', 'age': 33, 'email': 'bob@example.com'}
end

func create_bob() -> {'name': Str, 'age': Int32, 'email': Str} in
    {'name': 'Bob', 'age': 33, 'email': 'bob@example.com'}
end

type Customer = Person & {
    'account': Str
}

type CustomerWithAddress = Customer & Person & {
    'address': {
        'age': Int32,
        'street': Str,
        'city': Str,
        'zip': Str
    }
}

type MaybeCustomer = Person & {
    'account': Str | Null
}

func create_bob_with_account() -> Customer in
    {'name': 'Bob', 'age': 33, 'email': 'bob@example.com', 'account': '12345678'}
end

func create_bob() -> {'name': Str, 'age': Int32, 'email': Str} & {'account': Str} in
    {'name': 'Bob', 'age': 33, 'email': 'bob@example.com'}
end

type Company = {
    'legalName': Str,
    'brandName': Str
}

type PersonOrCompany = Person | Company

// An Int32 or a Tuple
type MyUnion =  Int32 | Tuple[Int64, Str | Bool]

var person = {
    'name': 'Bob',
    'age': 32,
    'email': 'bob@example.com'
}

var customer = {
    'account': '987654321',
}

// TODO: How can we use Rec.assign() to accomplish spread operations?
// var person_customer = {person..., customer...}

func create_acme() -> PersonOrCompany in
    if usePerson then
        {'name': 'Bob', 'age': 33, 'email': 'bob@example.com', 'account': '12345678'}
    else
        {'legalName': 'Acme LLC', 'legalBrand': 'ACME'}
    end
end

func create_acme() -> {'name': Str, 'age': Int32, 'email': Str} | {'legalName': Str, 'brandName': Str} in
    if usePerson then
        {'name': 'Bob', 'age': 33, 'email': 'bob@example.com', 'account': '12345678'}
    else
        {'legalName': 'Acme LLC', 'legalBrand': 'ACME'}
    end
end

// Person, Customer, and Addresses

type PostalAddress = Str#{
    'street': Str,
    'city': Str,
    'zip': Str
}

type EmailAddress = {
    'home': Str,
    'work': Str
}

type Person[T] = {
    'name': Str,
    'address': T
}

type Customer[T<:Rec] = Person[T] & {
    'account': Str
}

type HomeAndWorkEmail = 'HomeAndWorkEmail'#[Str, Str]

type EmailList = Array[Str]

type Int32Matrix = Array[Array[Int32]]

type StrAndInt32Tuple = Tuple[Str, Int32]
```

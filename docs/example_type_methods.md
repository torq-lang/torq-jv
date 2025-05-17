
## Type record with static methods

```
package example

meta#{'export', 'native': 'org.torqlang.local.ArrayListMod'}
type ArrayList[T] = Obj & {
    static {
        func of_array[T](array::Array[T]) -> ArrayList[T],
        func of_size[T](size::Int32) -> ArrayList[T],
        func of_tuple[T](tuple::Tuple) -> ArrayList[T],
    }
    func ArrayList[T]() -> ArrayList[T],
    func equals(other::Any) -> Bool,
    func hash_code() -> Int32,
    proc add(elem::T),
    proc clear(),
    func size() -> Int32,
    func to_array() -> Array[T],
}

meta#{'export', 'native': 'org.torqlang.klvm.StrMod'}
type Str = Obj & {
    static {
        func join(delimiter::Str, values::ValueIter[Str]) -> Str,
        func format(format::Str, args::Array[Str]) -> Str,
    }
    // A type can have one constructor, other constructions must be static methods
    func Str(chars::Array[Char]) -> Str,
    func char_at(index::Int32) -> Bool,
    func chars() -> Array[Char],
    func compare_to(other::Str) -> Int32,
    func concat(other::Str) -> Str,
    func contains(string::Str) -> Bool,
    func ends_with(string::Str) -> Bool,
    func equals(other::Any) -> Bool,
    func equals_ignore_case(other::Str) -> Bool,
    func hash_code() -> Int32,
    func index_of_char(char::Char) -> Int32,
    func index_of_char_from(char::Char, from::Int32) -> Int32,
    func index_of_str(str::Str) -> Int32,
    func index_of_str_from(str::Str, from::Int32) -> Int32,
    func is_blank() -> Bool,
    func is_empty() -> Bool,
    func last_index_of_char(char::Char) -> Int32,
    func last_index_of_char_from(char::Char, from::Int32) -> Int32,
    func last_index_of_str(str::Str) -> Int32,
    func last_index_of_str_from(str::Str, from::Int32) -> Int32,
    func length() -> Int32,
    func replace(old_char::Char, new_char::Char) -> Str,
    func starts_with(prefix::Str) -> Bool,
    func starts_with_at_offset(prefix::Str, at_offset::Int32) -> Bool,
    func strip() -> Str,
    func strip_leading() -> Str,
    func strip_trailing() -> Str,
    func substring_from(from::Int32) -> Str,
    func substring_from_to(from::Int32, to::Int32) -> Str,
    func to_char_array() -> Array[Char],
    func to_lower_case() -> Str,
    func to_upper_case() -> Str,
    func trim() -> Str,
}
```

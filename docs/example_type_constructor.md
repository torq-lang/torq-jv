
## Type constructor

```
package factorial

protocol FactorialApi[T] = {
    handle ask x::T -> T
}

actor Factorial() implements FactorialApi[Int64] in
    func fact(x::Int64) -> Int64 in
        func fact_cps(n::Int64, k::Int64) -> Int64 in
            if n < 2 then k
            else fact_cps(n - 1, n * k) end
        end
        fact_cps(x, 1)
    end
    handle ask x::Int64 -> Int64 in
        fact(x)
    end
end

actor Factorial[T <: Num]() implements FactorialApi[T] in
    func fact(x::T) -> T in
        func fact_cps(n::T, k::T) -> T in
            if n < 2 then k
            else fact_cps(n - 1, n * k) end
        end
        fact_cps(x, 1)
    end
    handle ask x::T -> T in
        fact(x)
    end
end
```
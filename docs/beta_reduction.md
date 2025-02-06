## Lambda Form

```
TypeApp(
    TypeApp(
        TypeFn(
            TypeVar("x"),
            TypeFn(
                TypeVar("y"),
                TypeLang(`x + y`)
            )
        ),
        3
    ),
    5
)

\x.\y.(x + y) 3 5 =
    \y.(3 + y) 5 =
    3 + 5 =
    8
```

## Common Form

```
TypeAppCmn(
    TypeFnCmn(TypePat[x, y], TypeLang(`x + y`)),
    [3, 5]
)

[x, y].(x + y) [3, 5] =
    (3 + 5) =
    8
```

## Computing common form using lambda form transforms:

```
TypeAppCmn(
    TypeFnCmn(List.of(TypePat(x), TypePat(y)), TypeLang(`x + y`)),
    [3, 5]
)

TypeApp(
    >>>TypeAppCmn(TypeFnCmn(List.of(TypePat(x), TypePat(y)), TypeLang(`x + y`)), [3])<<<,
    5
)

TypeApp(
    TypeApp(
        >>>TypeFnCmn(List.of(TypePat(x), TypePat(y)), TypeLang(`x + y`))<<<,
        3
    ),
    5
)

TypeApp(
    TypeApp(
        TypeFn(
            TypeVar("x"),
            >>>TypeFnCmn([y], TypeLang(`x + y`))<<<        
        ),
        3
    ),
    5
)

TypeApp(
    TypeApp(
        TypeFn(
            TypeVar("x"),
            TypeFn(
                TypeVar("y"),
                TypeLang(`x + y`)
            )
        ),
        3
    ),
    5
)
```

# Imports

## Paths

A path is used to import a member.

## Path Rules

A path can contain multiple nested package names and end with a module name if the module exports a single
member matching the module name.

```
<<package>>.<<package>>.<<module>>
```

A path can contain multiple nested package names, a module name, and end with a member name if the module exports that member.

```
<<package>>.<<package>>.<<module>>.<<member>>
```

## Examples

```
package example

import package1.package2.member
import package1.{member1, member2 as X}
import package1.{member1}
```

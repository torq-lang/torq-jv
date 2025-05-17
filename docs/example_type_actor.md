
## Type actor

```
package example

meta#{'export'}
protocol Hello = {
    ask 'hello' -> Str
}

actor HelloWorld() implements Hello in
    handle ask 'hello' -> Str in
        'Hello, World!'
    end 
end

// ----- OR ----- //

meta#{'export'}
actor HelloWorld() implements Self as Hello in
    handle ask 'hello' -> Str in
        'Hello, World!'
    end 
end

var HelloWorld::ActorCtor[Hello] = actor () implements Hello in
    handle ask 'hello' -> Str in
        'Hello, World!'
    end 
end 

var hello_world_cfg::ActorCfg[Hello] = new HelloWorld()
var hello_world_ref::ActorRef[Hello] = spawn(hello_world_cfg)
```

# Relay Socket

> 每个学计算机的男孩都有在 IPv4 的世界实现端到端通信的想法。

有很多 bug 还没改。

``` shell
mvn test
```

## Socket 协议

Client => Server

1. `ClientLabel <HexString>`

Server => Client

1. `WaitForPeerConnect`
2. `PeerConnectionPreceding`

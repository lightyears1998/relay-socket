# Relay Socket

> 每个学计算机的男孩都有在 IPv4 的世界实现端到端通信的想法。

``` shell
mvn test
```

## 协议细节

### Server and Peer

- P: Peer
- S: Server

1. P => S `RelaySocket Protocol v1` `ClientLabel <HexString>`
2. S => P `WaitForPeerConnect` 或 `PeerConnectionPreceding`

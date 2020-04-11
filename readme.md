![Demonstration GIF](https://i.imgur.com/1nHwYnp.gif)

A simple plugin that makes enderpearls bounce off of surfaces when they would normally teleport the player. Lots of fun for non-serious gameplay, absolute garbage for serious gameplay.

Permissions:
 - bouncyenderpearls.enabled\
   Denying this permission will cause enderpearls to behave normally. Granted by default.
 - bouncyenderpearls.reload\
 Permission to reload the config

Commands:
 - /bouncyenderpearls or /bep\
   Reloads the config

Config:
```yaml
bounciness: 0.8
max-bounces: 5
bounce-sound:
  enabled: true
  name: BLOCK_SLIME_BLOCK_FALL
  volume: 1.0
  pitch: 1.0
disabled-worlds:
  - world_the_end
```
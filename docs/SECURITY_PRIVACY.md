# Security and Privacy

The app is intentionally low-risk and local-only.

## Requirements

- Do not request internet access.
- Do not request contacts, location, microphone, camera, notifications, storage, Bluetooth or other sensitive permissions.
- Do not include ad/analytics SDKs.
- Do not collect identifiers or telemetry.
- Do not create/import wallets or keys.
- Do not accept real money.
- Do not claim to mine Bitcoin.

## Backup

Because the requirement is local-only and there is no save export/import in V1, disable cloud backup unless explicitly revisited. Document any Android backup behavior in the manifest review.

## Local data

Game save contains only gameplay/settings data. Treat corrupted input defensively because restored/modified local storage can be malformed.

## Reset

Provide a clear destructive confirmation and test that it fully clears game state while leaving the app usable.

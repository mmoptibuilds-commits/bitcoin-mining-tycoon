---
trigger: always_on
description: Prevent features from being marked complete without logic, UI, UX and device verification.
---

# Testing gate

After each feature, before implementing the next:

1. targeted logic tests;
2. persistence/migration tests if relevant;
3. Compose interaction tests if relevant;
4. preview/screenshot + semantics inspection if relevant;
5. emulator/device run;
6. Journey/manual core-flow test;
7. boundary/failure case;
8. regression suite.

A compile-only result is failure to satisfy this rule. Fix failures before proceeding. Do not weaken valid assertions.

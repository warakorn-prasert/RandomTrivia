# Feature: UI

## Overview
- Minimal UI based on Jetpack's Material Design 3 library,
for fast prototyping that supports multiple device platforms.
- Follows MVVM design pattern. (UI ↔ ViewModels ↔ [Repository](../../../../../../../../../repository))

## Architecture
```
               UI
              ⬇⬆
           ViewModels
              ⬇⬆
           Repository
            ⬇⬆  ⬇⬆
Local database    Network calls
```

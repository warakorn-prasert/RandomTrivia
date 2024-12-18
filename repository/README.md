# Feature: Repository

## Overview
`TriviaRepository` acts as abstraction layer for [`TriviaDatabase`](../database) and [`TriviaApiClient`](../network).

## Functionality
- Get official category list from opentdb.com.
- Fetch a new game, or make up one from local database.
- Save/Delete a game.
- Save questions to local database.
- Show/Delete categories in local database.

> To use, instantiate `TriviaRepositoryImpl` in the application class.

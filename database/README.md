# Feature: Database

## Overview
- Provides read & write access to device's persistent storage using Room library.
- There are 4 entities, i.e., Category, Question, GameDetail, and GameAnswer.
- To use, initialize `val db = TriviaDatabase.getDatabase(this)` in application level, and use methods from its DAOs.

## SQLite database entities
### Attributes
| Category |         Question |      GameDetail |    GameAnswer |
|---------:|-----------------:|----------------:|--------------:|
|    🔑 id |            🔑 id |       🔑 gameId |     🔑 gameId |
|     name |         question |       timestamp | 🔑 questionId |
|          |       difficulty | totalTimeSecond |    categoryId |
|          |       categoryId |                 |        answer |
|          |    correctAnswer |                 |               |
|          | incorrectAnswers |                 |               |
### Notes
- Category, Question, and GameDetail, each has one primary key.
- GameAnswer uses gameId and questionId as composite key to identify the owner of an answer.
- When a Category is deleted, categoryId in Question and GameAnswer is set to null.
- When a GameDetail is deleted, every associated GameAnswer will be too.
- timestamp in GameDetail is when the game finishes.
- All ids are UUID except Category's, to match category id in opentdb.com.

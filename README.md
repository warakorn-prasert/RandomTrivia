# Random Trivia
#### Video Demo:  <https://youtu.be/Z_USaXRcqjs?si=0OkE0g-WYl4PXPRd>
#### Description:

This is my final project of CS50x. It is a smartphone Android application that lets you download trivia questions from this API (<http://opentdb.com>) and play them as a game on your phone. You can also replay all the past games.

This app was tested on Android emulator devices (Pixel 6 Pro API 26, Pixel 7 Pro API 34)

The app has 5 menus.
1. Home : Statistics from past games
2. Categories : Available categories and their details
3. Play : Trivia game
4. Past games : List of past games
5. About : About the developer and api provider

When user open the app, the Home page is displayed showing a vertical list of click-to-expand cards. Each card numbers of total questions, correction questions and incorrect questions of the played games grouped by question category and difficulty. When firstly loading categories, either by open the Category menu or click on category dropdown menu in the Play menu, the app will make an API call to get all the categories and stored them in persistent database. This data is deleted everytime the app open to make sure that users get the latest update of the API. This data also includes category detail that is fetched again per category in the Category menu and the Play menu.

In the Play menu, after category is selected and its detail is fetched, user then can choose a difficulty and a number of questions for the game. During gameplay, app's top bar is hidden. The questions are shown in a vertical list of cards with selectable options (multiple choicees only and 50 questions at maximum as per the API request limit). At the bottom of the list, there is a button to end the game regardless if all questions has been answered. It then opens a result screen showing game summary and correct answers of every question compared to user's answers. In the result screen, there is a button at the bottom to save the game. The saved game can be play later offline.

In the Past games menu, there is a vertical list of cards with information about past games. There are two buttons on each card for a replay or delete of the game.

The codes follow Model-View-ViewModel (MVVM) achitecture. Respectively to the project structure,
- ```ui/``` has everything relating to View, including ViewModel.
- ```model/``` has data containers/templates.
- ```data/``` is for interacting with data source.
- ```network/``` has codes that provide info needed to make API calls.

There are a few files that act as layers and links for the MVVM architecture.
1. ```data/TriviaDatabase.kt``` provides access to persistent storage (SQLite database) for caching and saving fetched API results and games.
2. ```network/TriviaApiService.kt``` grants API calls.
3. ```data/TriviaRepository.kt``` acts as a gate for ViewModel to get and manipulate data from persistent storage and API calls.
4. ```ui/TriviaViewModel.kt``` has business logic that connects UI to application data.
5. ```ui/AppScreen.kt``` is the main UI with navigation to other UI codes.
6. ```MyApplication.kt``` holds singleton object of ```data/TriviaDatabase.kt``` and ```data/TriviaRepository.kt```.

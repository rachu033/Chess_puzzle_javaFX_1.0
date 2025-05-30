# ♟️ Chess Puzzle — JavaFX

**Author**: Adam Rachuba  
**Version**: 1.0  
**Architecture**: MVC (Model-View-Controller)  
**Language**: Java  
**Framework**: JavaFX  


## 📖 About the Project

**Chess Puzzle** is a JavaFX desktop application for solving chess puzzles, using a clean MVC architecture and external data from online API.

The application integrates with the [Lichess.org API](https://lichess.org/api), from which it fetches real chess puzzles. These puzzles are saved to a local **SQLite** database to allow offline access and faster puzzle loading.

The project was developed as part of a laboratory course at the **Military University of Technology (WAT)** and serves both as a practical exercise in software architecture and a functional chess training tool.

## 🖼 Chess Piece Textures

Chess piece textures used in this project are based on work by [Cburnett](https://commons.wikimedia.org/wiki/User:Cburnett), available on [Wikimedia Commons](https://commons.wikimedia.org/wiki/Category:PNG_chess_pieces/Standard_transparent).  
The original files are licensed under the [Creative Commons](https://creativecommons.org/licenses/by-sa/3.0/).

I have not modified the images.

You are free to use these textures under the same license terms.

## ⚠️ Limitations

- Full chess logic has been implemented, including **castling** and **en passant**.
- However, game end conditions are currently limited to **checkmate** and **stalemate**.
- Other draw rules such as **insufficient material**, **threefold repetition**, or the **fifty-move rule** are not yet supported.

## 🚀 How to Run the Program

To run the program, execute the `Main` class located in the following directory:  
`src/main/java/code/chess/Main.java`

### Recommended Development Environment:
- **IDE**: IntelliJ IDEA (highly recommended for JavaFX development)
- **Java Version**: Use the **latest stable version** of **Java** (Java 17+ is recommended for best compatibility)

# Sliding Puzzle Game

A classic sliding puzzle game implemented in Java using Swing for the graphical user interface. Players can enjoy solving the puzzle by sliding tiles to arrange them in numerical order.

## Features

- Interactive sliding puzzle with numbered tiles
- Multiple difficulty levels
- Hint system using A* search algorithm
- Move counter to track progress
- Clean and intuitive user interface
- Timer to track solving time

## Requirements

- Java Runtime Environment (JRE) 8 or higher
- Java Development Kit (JDK) for compilation

## How to Run

1. Compile the game:
```bash
javac *.java
```

2. Run the game:
```bash
java PuzzleGame
```

## How to Play

1. The game starts with tiles randomly shuffled
2. Click on tiles adjacent to the empty space to move them
3. Arrange the tiles in numerical order to win
4. Use the hint button if you need assistance
5. Try to solve the puzzle in as few moves as possible

## Controls

- Left Mouse Click: Move tile
- New Game Button: Start a new game
- Hint Button: Get a suggestion for the next move
- Exit: Close the game

## Technical Details

- Built with Java Swing for GUI
- Implements A* search algorithm for the hint system
- Uses Manhattan distance heuristic for path finding
- Custom UI components for consistent styling

## File Structure

- `PuzzleGame.java`: Main game class and entry point
- `GameBoard.java`: Core game logic and board management
- `UIComponents.java`: UI component creation and styling
- `Constants.java`: Game constants and configuration


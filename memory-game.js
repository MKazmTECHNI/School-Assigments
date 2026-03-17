// Select various elements from the DOM
const resetButton = document.querySelector("button.memory-reset");
const wholeGameContainer = document.querySelector("div.memory-game-container");
const gameContainer = document.querySelector("div.memory-game");
const spanMoves = document.querySelector("span.memory-moves");
const spanPairs = document.querySelector("span.memory-pairs");
const message = document.querySelector("p.memory-result-message");
const tiles = document.querySelectorAll(".memory-tile");
const tableSize = document.querySelector("span.memory-table-size");
const GameSettingsContainer = document.querySelector(".memory-game-settings");
// Game state variables
let movesMade = 0; // Tracks number of moves made
let tilesMatched = 0; // Tracks number of matched pairs
let tileSelected = false; // Stores the currently selected tile (if any)
let istileSelected = false; // (Appears unused, might be a typo or extra variable)
message.innerHTML = ""; // Resets the result message
let inMove = false; // Prevents additional moves while processing a match

// List of emoji pairs for the memory game
// If you want more possible tiles, just add emojis here
const emojis = [
  "🙉",
  "🙉",
  "💖",
  "💖",
  "🐶",
  "🐶",
  "🍔",
  "🍔",
  "🏆",
  "🏆",
  "🎁",
  "🎁",
  "💀",
  "💀",
  "😄",
  "😄",
  "🤣",
  "🤣",
  "🥰",
  "🥰",
  "😘",
  "😘",
  "😉",
  "😉",
  "😊",
  "😊",
  "🥳",
  "🥳",
  "🥵",
  "🥵",
  "🥶",
  "🥶",
  "🐢",
  "🐢",
  "🦔",
  "🦔",
  "🐳",
  "🐳",
  "🍄",
  "🍄",
  "🍎",
  "🍎",
  "🍫",
  "🍫",
  "⚽",
  "⚽",
];
// Display the total number of available tiles
tableSize.innerHTML = emojis.length;
// Initially hide the game container until the game starts
wholeGameContainer.style.display = "none";
// TILE AMOUNT SELECTION
const tilesAmountButton = document.querySelector(
  "button.memory-tiles-amount-button"
);
const tilesAmountInput = document.querySelector(
  "input.memory-tiles-amount-input"
);
// Handles the tile amount button click
function handleSubmitTilesAmountButtonClick() {
  const inputValue = tilesAmountInput.value; // Get the user's tile amount input
  // Input validation: check for odd or invalid numbers
  if (inputValue % 2 != 0) {
    message.innerHTML = "Insert odd number";
    return;
  } else if (inputValue < 1) {
    message.innerHTML = "Insert positive number";
    return;
  }
  // If input is valid, generate the game board
  if (inputValue <= emojis.length) {
    generateBoard(inputValue);
    wholeGameContainer.style.display = "flex"; // Show the game container
  }
}
// Event listener for the tile amount button
tilesAmountButton.addEventListener("click", handleSubmitTilesAmountButtonClick);
// Function to generate the game board
function generateBoard(amount) {
  GameSettingsContainer.style.display = "none"; // Hide game settings
  tilesAmountInput.value = ""; // Reset input field
  gameContainer.innerHTML = ""; // Clear the game board
  movesMade = 0; // Reset move counter
  tilesMatched = 0; // Reset matched pairs counter
  spanMoves.innerHTML = movesMade;
  spanPairs.innerHTML = tilesMatched;
  // Shuffle the emoji list and select the specified amount
  let shuf_emoji = emojis
    .slice(0, amount)
    .sort(() => (Math.random() > 0.5 ? 2 : -1)); // Shuffle emojis
  // Create and add tiles to the game board
  for (let i = 0; i < amount; i++) {
    let tile = document.createElement("div");
    tile.className = "memory-tile";
    tile.innerHTML = shuf_emoji[i]; // Set the tile's emoji
    gameContainer.appendChild(tile); // Add tile to the board
    // Add event listener to each tile
    tile.addEventListener("click", handleTileClick);
  }
}
// Handles tile clicks
function handleTileClick(event) {
  if (inMove) {
    return; // Prevent further actions if another move is in progress
  }
  if (event.target.classList.contains("memory-shown")) {
    return; // Ignore if the tile is already matched
  }
  spanMoves.innerHTML = movesMade;
  message.innerHTML = "";
  // Check if the same tile was clicked twice
  if (tileSelected === event.target) {
    message.innerHTML = "Click on a different button!";
    return;
  }
  // If a tile was already selected, check for a match
  if (tileSelected !== false) {
    if (tileSelected.innerHTML === event.target.innerHTML) {
      tileSelected.classList.add("memory-shown"); // Mark matched tile
      event.target.classList.add("memory-shown");
      tilesMatched += 1; // Increment matched pairs
      spanPairs.innerHTML = tilesMatched;
    }
    event.target.classList.add("memory-selected"); // Show current tile
    inMove = true; // Set the inMove flag to prevent further clicks during the move
    // After 1 second, reset the selected tiles
    setTimeout(() => {
      tileSelected.classList.remove("memory-selected");
      event.target.classList.remove("memory-selected");
      tileSelected = false;
      inMove = false;
    }, 1000);
    movesMade += 1; // Increment move count
    return;
  }
  // If no tile is selected, set the current tile as selected
  event.target.classList.add("memory-selected");
  tileSelected = event.target;
}
// Resets the game
function resetMemoryGame() {
  generateBoard(0); // Clears the board
  GameSettingsContainer.style.display = "flex"; // Show game settings again
  wholeGameContainer.style.display = "none"; // Hide the game container
}
// Event listener for the reset button
resetButton.addEventListener("click", resetMemoryGame);

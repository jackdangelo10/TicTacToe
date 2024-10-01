package com.example.tictactoe

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.widget.TextView

private var mediaPlayer: MediaPlayer? = null

class GameActivity : AppCompatActivity() {

    // 2D array representing the game board, 0 = empty, 1 = human, 2 = machine
    private var board = Array(3) { IntArray(3) { 0 } }

    // Boolean to track if it's the human's turn
    private var isHumanTurn = true

    // Array to hold the references to the buttons (the grid)
    private lateinit var buttons: Array<Array<Button>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Get the starting turn information from the intent
        isHumanTurn = intent.getBooleanExtra("isHumanStarting", true)

        // Initialize the buttons (grid) and set click listeners
        setupButtons()

        // If the machine starts, make its first move
        if (!isHumanTurn) {
            makeOptimalMove()
        }
    }

    private fun playMoveSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.click_sound)
        mediaPlayer?.start()
    }

    // This method sets up the buttons in the grid and attaches click listeners
    private fun setupButtons() {
        buttons = Array(3) { row ->
            Array(3) { col ->
                val buttonId = resources.getIdentifier("button_${row}_${col}", "id", packageName)
                val button = findViewById<Button>(buttonId)
                button.setOnClickListener {
                    onButtonClick(row, col)
                }
                button
            }
        }
    }

    private fun animateButton(button: Button) {
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f)
        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY)
        set.duration = 300
        set.start()
    }


    // Handle button click by the human player
    private fun onButtonClick(row: Int, col: Int) {
        if (board[row][col] == 0 && isHumanTurn) {
            // Human makes a move
            board[row][col] = 1
            buttons[row][col].text = "O" // Represent human move with "O"
            buttons[row][col].setBackgroundResource(R.drawable.rounded_button_human) // Set human's custom drawable


            // Play the move sound
            playMoveSound()

            // Animate the button
            animateButton(buttons[row][col])

            // Check if human has won
            if (checkWinner(1))
            {
                showWinDialog("Human")
            }
            else if (isBoardFull())
            {
                showDrawDialog()
            }
            else
            {
                // It's the machine's turn now
                isHumanTurn = false
                makeOptimalMove() // Machine makes its move after human

                if (checkWinner(2))
                {
                    // Handle machine win
                    showWinDialog("Machine")
                }
                else if (isBoardFull()) {
                    // Handle draw after machine move
                    showDrawDialog()
                }
                // Switch back to human turn
                isHumanTurn = true
            }
        }
    }

    // Method for machine's turn - implements the unbeatable Minimax algorithm
    private fun makeOptimalMove() {
        val move = findBestMove()
        if (move != null) {
            board[move.first][move.second] = 2
            buttons[move.first][move.second].text = "X" // Represent machine move with "X"
            buttons[move.first][move.second].setBackgroundResource(R.drawable.rounded_button_machine) // Set machine's custom drawable

            // Check if machine has won
            if (checkWinner(2)) {
                // Handle machine win (show a dialog, restart game, etc.)
            } else if (isBoardFull()) {
                // Handle draw
            }
            // It's now the human's turn again
            isHumanTurn = true
        }
    }

    // Check if a player has won (either human or machine)
    private fun checkWinner(player: Int): Boolean {
        // Check rows, columns, and diagonals for a winning condition
        for (i in 0 until 3) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true
        return false
    }

    // Check if the board is full (draw condition)
    private fun isBoardFull(): Boolean {
        return board.all { row -> row.all { it != 0 } }
    }

    // Minimax algorithm to find the best move for the machine
    private fun findBestMove(): Pair<Int, Int>? {
        var bestVal = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        // Evaluate all empty spots and pick the best one
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                if (board[row][col] == 0) {
                    // Make a move and evaluate
                    board[row][col] = 2 // Machine makes the move
                    val moveVal = minimax(0, false) // Call minimax
                    board[row][col] = 0 // Undo the move

                    // If the move is better, store it
                    if (moveVal > bestVal) {
                        bestMove = Pair(row, col)
                        bestVal = moveVal
                    }
                }
            }
        }
        return bestMove
    }

    // Minimax algorithm
    private fun minimax(depth: Int, isMaximizing: Boolean): Int {
        val score = evaluate()

        // If machine wins
        if (score == 10) return score

        // If human wins
        if (score == -10) return score

        // If it's a draw
        if (isBoardFull()) return 0

        // If machine's turn
        if (isMaximizing) {
            var best = Int.MIN_VALUE

            // Traverse all cells
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    if (board[row][col] == 0) {
                        board[row][col] = 2
                        best = maxOf(best, minimax(depth + 1, false))
                        board[row][col] = 0
                    }
                }
            }
            return best
        } else { // If human's turn
            var best = Int.MAX_VALUE

            // Traverse all cells
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    if (board[row][col] == 0) {
                        board[row][col] = 1
                        best = minOf(best, minimax(depth + 1, true))
                        board[row][col] = 0
                    }
                }
            }
            return best
        }
    }

    // Evaluate the board to return a score based on who is winning
    private fun evaluate(): Int {
        // Check rows, columns, and diagonals for a win
        for (i in 0 until 3) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                if (board[i][0] == 2) return 10 // Machine win
                if (board[i][0] == 1) return -10 // Human win
            }
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                if (board[0][i] == 2) return 10 // Machine win
                if (board[0][i] == 1) return -10 // Human win
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == 2) return 10
            if (board[0][0] == 1) return -10
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == 2) return 10
            if (board[0][2] == 1) return -10
        }

        // No winner
        return 0
    }

    private fun showCustomResultDialog(resultText: String) {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_result, null)
        val resultTextView = dialogView.findViewById<TextView>(R.id.resultTextView)
        val mainMenuButton = dialogView.findViewById<Button>(R.id.mainMenuButton)

        // Set the result text (Win, Draw, etc.)
        resultTextView.text = resultText

        // Create a Dialog with the custom view
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent clicking outside the dialog to dismiss it
            .create()

        // Handle the button click to return to the main menu
        mainMenuButton.setOnClickListener {
            restartGame() // Call your restart method to return to the main menu
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }
    private fun showWinDialog(winner: String) {
        val resultText = "$winner wins!"
        showCustomResultDialog(resultText)
    }

    private fun showDrawDialog() {
        val resultText = "It's a draw!"
        showCustomResultDialog(resultText)
    }

    // Override the back button press behavior
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        // Prevent back press if the custom result dialog is showing
        val dialog = supportFragmentManager.findFragmentByTag("custom_dialog")
        if (dialog == null) {
            // Only allow back press if the dialog is not showing
            super.onBackPressed()
        }
    }

    private fun restartGame() {
        // Create an intent to go back to StartActivity
        val intent = Intent(this, StartActivity::class.java)
        // Clear the activity stack to prevent the user from returning to GameActivity with the back button
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Close the current GameActivity to prevent users from going back to this screen
    }
}

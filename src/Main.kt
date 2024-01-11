package dev.biogustav.minesweeper

fun main() {
    var choice: Int
    val difficulties = Difficulty.entries.size
    do {
        print("Choose a difficulty [0..%d]: ".format(difficulties - 1))
        choice = readlnOrNull()?.toIntOrNull() ?: -1
    } while (choice !in (0..<Difficulty.entries.size))

    val difficulty = Difficulty.entries[choice]

    val game = Board(difficulty)

    var terminate = false

    do {
        println(game)
        println()
        var command: String

        do {
            print("Enter a command (Choose field[c], Exit[x]): ")
            command = readlnOrNull() ?: ""
        } while (command !in listOf("c", "x"))

        when (command) {
            "c" -> {
                print("Enter x coordinate (→): ")
                val x = readlnOrNull()?.toIntOrNull()
                print("Enter y coordinate (↓): ")
                val y = readlnOrNull()?.toIntOrNull()

                if (x != null && y != null && x in (0..<difficulty.size) && y in (0..<difficulty.size)) {
                    print("Enter action (Reveal[r], Flag[f], Question[q], Clear[c], Abort[a]: ")
                    val action = readlnOrNull() ?: ""
                    when (action) {
                        "r" -> game.reveal(x, y)
                        "f" -> game.flag(x, y)
                        "q" -> game.question(x, y)
                        "c" -> game.clear(x, y)
                        "a" -> continue
                        else -> println("Invalid action!")
                    }
                } else {
                    println("Invalid coordinates!")
                }
            }
            "x" -> {
                terminate = true
            }
        }
        println()
    } while (game.state != GameState.COMPLETE && !terminate)

    println(game.revealed())
    println()

    if (!terminate) {
        println("You %s!".format(game.result.toString().lowercase()))
    } else {
        println("Game aborted!")
    }

}

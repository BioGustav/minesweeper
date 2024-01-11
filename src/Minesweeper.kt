package dev.biogustav.minesweeper

import kotlin.math.min
import kotlin.random.Random


data class Field(val width: Int = 10, val height: Int = 10, val n: Int = width * height / 4) {
    private val maxBombs = width * height
    private val rng = Random
    val nBombs = min(n, maxBombs)

    private val fields = IntArray(width * height) { 0 }.apply {
        val bombPositions = mutableSetOf<Pair<Int, Int>>().apply {
            while (size < nBombs) {
                add(rng.nextInt(0, width) to rng.nextInt(0, height))
            }
        }

        for ((x, y) in bombPositions) {
            forSurrounding(x, y, width, height) { realX, realY ->
                this[realX, realY]++
            }
        }
        for ((x, y) in bombPositions) {
            this[x, y] = -1
        }
    }

    operator fun get(x: Int, y: Int) = fields[y * width + x]
    operator fun get(i: Int) = fields[i]
    operator fun IntArray.set(x: Int, y: Int, value: Int) {
        this[y * width + x] = value
    }

    operator fun IntArray.get(x: Int, y: Int) = this[y * width + x]

    override fun toString() = fields.joinToString(" ") { "%2d".format(it) }.chunked(width * 3).joinToString("\n")

    fun isBomb(x: Int, y: Int) = this[x, y] < 0
    fun isBomb(i: Int) = this[i] < 0
}

data class Board(val difficulty: Difficulty) {
    var state = GameState.PLAYING
    var result = GameResult.WON

    private val field = Field(difficulty.size, difficulty.size, difficulty.nBombs)
    private var count = field.nBombs
    private var board = Array(field.width * field.height) { FieldState.UNTOUCHED }

    fun reveal(x: Int, y: Int) {
        if (this[x, y] == FieldState.FLAGGED) count++
        if (field.isBomb(x, y)) {
            state = GameState.COMPLETE
            result = GameResult.LOST
            return
        }


        if (field[x, y] == 0) {
            revealArea(x, y)
            return
        }

        this[x, y] = FieldState.REVEALED

        if (board.filterIndexed { i, _ -> !field.isBomb(i) }.all { it != FieldState.UNTOUCHED }) {
            state = GameState.COMPLETE
            result = GameResult.WON
        }
    }

    private fun revealArea(x: Int, y: Int) {
        if (this[x, y] != FieldState.UNTOUCHED) {
            return
        }

        this[x, y] = FieldState.REVEALED

        if (field[x, y] == 0) {
            forSurrounding(x, y, field.width, field.height) { realX, realY ->
                revealArea(realX, realY)
            }
        }
    }

    fun flag(x: Int, y: Int) {
        if (this[x, y] == FieldState.FLAGGED) return
        this[x, y] = FieldState.FLAGGED
        count--
    }

    fun question(x: Int, y: Int) {
        if (this[x, y] == FieldState.FLAGGED) count++
        this[x, y] = FieldState.QUESTIONED
    }

    fun clear(x: Int, y: Int) {
        when (this[x, y]) {
            FieldState.FLAGGED -> {
                count++
            }

            FieldState.QUESTIONED -> {}
            else -> return
        }
        this[x, y] = FieldState.UNTOUCHED
    }

    fun revealed(): String {
        count = 0
        board.withIndex().forEach { (i, state) ->
            if (state != FieldState.REVEALED) {
                board[i] = FieldState.REVEALED
            }
        }
        return this.toString()
    }

    operator fun get(x: Int, y: Int) = board[y * field.width + x]
    operator fun set(x: Int, y: Int, state: FieldState) {
        board[y * field.width + x] = state
    }

    override fun toString(): String {
        val sb = buildString {
            append("   x")
            append((0..<difficulty.size).joinToString(" ") { "%2d".format(it) })

            append("  BombCount: $count")
            append("\n")

            append(" y  ")
            append((0..<difficulty.size).joinToString("-") { "--" })
            append("\n")

            append(board.withIndex()
                .joinToString("  ") { (i, s) -> s.repr(field[i]) }
                .chunked(field.width * 3)
                .withIndex()
                .joinToString("\n") { (i, s) -> "%2d | %s".format(i, s) })
        }
        return sb
    }
}

fun forSurrounding(x: Int, y: Int, width: Int, height: Int, action: (Int, Int) -> Unit) {
    (-1..1).forEach { dx ->
        (-1..1).forEach { dy ->
            val realX = x + dx
            val realY = y + dy
            if (realX in 0..<width && realY in 0..<height) {
                action(realX, realY)
            }
        }
    }
}

enum class FieldState {
    UNTOUCHED, FLAGGED, QUESTIONED, REVEALED
}

fun FieldState.repr(x: Int = 0) = when (this) {
    FieldState.UNTOUCHED -> "☐"
    FieldState.FLAGGED -> "!"
    FieldState.QUESTIONED -> "?"
    FieldState.REVEALED -> if (x == 0) " " else if (x < 0) "*" else "$x"
}

enum class Difficulty(val size: Int, val nBombs: Int) {
    EASY(9, 10), MEDIUM(16, 40), HARD(24, 99)
}

enum class GameState {
    PLAYING, COMPLETE,
}

enum class GameResult {
    WON, LOST
}

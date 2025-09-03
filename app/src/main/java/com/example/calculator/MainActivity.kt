package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private lateinit var textExpression: TextView
    private lateinit var textResult: TextView

    private var currentExpression = ""
    private var isNewCalculation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_layout3)

        textExpression = findViewById(R.id.text_expression)
        textResult = findViewById(R.id.text_result)

        textResult.text = "0"
        textExpression.text = ""

        val numberButtons = listOf(
            R.id.button_0, R.id.button_1, R.id.button_2,
            R.id.button_3, R.id.button_4, R.id.button_5,
            R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9
        )

        for (id in numberButtons) {
            findViewById<Button>(id).setOnClickListener {
                if (isNewCalculation) {
                    currentExpression = ""
                    isNewCalculation = false
                }
                appendToExpression((it as Button).text.toString())
            }
        }

        val operationButtons = listOf(
            R.id.button_plus, R.id.button_minus,
            R.id.button_multiply, R.id.button_divide
        )

        for (id in operationButtons) {
            findViewById<Button>(id).setOnClickListener {
                isNewCalculation = false
                val operator = (it as Button).text.toString()
                if (currentExpression.isNotEmpty()) {
                    val lastChar = currentExpression.trim().lastOrNull()
                    if (isOperator(lastChar)) {
                        val trimmedExpression = currentExpression.trim()
                        val newExpression = trimmedExpression.substring(0, trimmedExpression.length - 1) + operator
                        currentExpression = newExpression.trim()
                    } else if (lastChar == '.') {
                        Toast.makeText(this, "Invalid syntax", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        currentExpression = "${currentExpression.trim()} $operator "
                    }
                } else if (operator == "-") {
                    currentExpression = "$operator "
                }
                textResult.text = currentExpression.trim()
            }
        }

        findViewById<Button>(R.id.button_percent).setOnClickListener {
            if (currentExpression.isNotEmpty() && !isOperator(currentExpression.trim().lastOrNull()) && currentExpression.trim().lastOrNull() != '%') {
                currentExpression = currentExpression.trim() + "%"
                isNewCalculation = false
            }
            textResult.text = currentExpression.trim()
        }

        findViewById<Button>(R.id.button_decimal).setOnClickListener {
            if (isNewCalculation) {
                currentExpression = "0"
                isNewCalculation = false
            }
            val parts = currentExpression.split('+', '-', 'x', '÷', '%').map { it.trim() }
            if (parts.isNotEmpty() && !parts.last().contains(".")) {
                appendToExpression(".")
            }
        }

        findViewById<Button>(R.id.button_equals).setOnClickListener {
            calculateResult()
            isNewCalculation = true
        }

        findViewById<Button>(R.id.button_plus_minus).setOnClickListener {
            toggleSign()
        }

        findViewById<Button>(R.id.button_ac).setOnClickListener {
            clearAll()
        }

        findViewById<ImageButton>(R.id.button_arrow).setOnClickListener {
            backspace()
        }
    }

    private fun isOperator(char: Char?): Boolean {
        return char == '+' || char == '-' || char == 'x' || char == '÷'
    }

    private fun isLastCharOperator(expression: String): Boolean {
        if (expression.isEmpty()) return false
        val lastChar = expression.trim().last()
        return isOperator(lastChar)
    }

    private fun appendToExpression(value: String) {
        if (isNewCalculation) {
            currentExpression = value
            isNewCalculation = false
        } else {
            val trimmedCurrent = currentExpression.trim()
            if (trimmedCurrent.isNotEmpty() && (isOperator(trimmedCurrent.last()) || trimmedCurrent.last() == '%')) { // تم التعديل هنا
                currentExpression = "$currentExpression$value"
            } else {
                currentExpression += value
            }
        }
        textResult.text = currentExpression.trim()
    }

    private fun clearAll() {
        currentExpression = ""
        textExpression.text = ""
        textResult.text = "0"
        isNewCalculation = true
    }

    private fun backspace() {
        if (isNewCalculation) {
            currentExpression = ""
            textResult.text = "0"
            textExpression.text = ""
        } else if (currentExpression.isNotEmpty()) {
            val lastChar = currentExpression.last()
            currentExpression = currentExpression.dropLast(1)
            if (lastChar == ' ' && currentExpression.isNotEmpty() && isOperator(currentExpression.last())) {
                currentExpression = currentExpression.dropLast(1).trimEnd()
            }
            textResult.text = if (currentExpression.trim().isEmpty()) "0" else currentExpression.trim()
            if (currentExpression.trim().isEmpty()) {
                isNewCalculation = true
            }
        } else {
            textResult.text = "0"
        }
    }

    private fun toggleSign() {
        if (currentExpression.trim().isEmpty() || currentExpression.trim() == "0" || currentExpression.trim() == "Error") {
            return
        }

        val operatorsAndPercent = setOf('+', '-', 'x', '÷', '%')

        val normalizedExpression = currentExpression
            .replace(Regex("\\s*([+\\-x÷])\\s*"), " $1 ")
            .trim()

        var lastNumberStart = -1
        var lastNumberEnd = normalizedExpression.length

        for (i in normalizedExpression.length - 1 downTo 0) {
            if (normalizedExpression[i] in operatorsAndPercent) {
                if (normalizedExpression[i] == '-' && (i == 0 || normalizedExpression.getOrNull(i - 1) == ' ' || normalizedExpression.getOrNull(i-1) in operatorsAndPercent)) {
                    lastNumberStart = i
                } else {
                    lastNumberStart = i + 1
                }
                break
            }
        }

        if (lastNumberStart == -1) {
            lastNumberStart = 0
        }

        val lastNumberStr = normalizedExpression.substring(lastNumberStart, lastNumberEnd).trim()

        if (lastNumberStr.isNotEmpty()) {
            val newNumberStr = if (lastNumberStr.startsWith("-")) {
                lastNumberStr.substring(1)
            } else {
                "-$lastNumberStr"
            }
            currentExpression = normalizedExpression.substring(0, lastNumberStart) + newNumberStr
            textResult.text = currentExpression.trim()
        }
    }

    private fun calculateResult() {
        if (currentExpression.trim().isEmpty()) {
            return
        }
        try {
            var expressionToEvaluate = currentExpression.trim()
            if (isLastCharOperator(expressionToEvaluate)) {
                expressionToEvaluate = expressionToEvaluate.dropLast(1).trim()
            }

            expressionToEvaluate = expressionToEvaluate
                .replace("x", "*")
                .replace("÷", "/")
                .replace(Regex("\\s+"), "")

            val percentPattern = Regex("(-?\\d+\\.?\\d*)%")
            if (expressionToEvaluate.matches(percentPattern)) {
                val number = expressionToEvaluate.dropLast(1).toDouble()
                expressionToEvaluate = (number / 100).toString()
            } else {
                val parts = expressionToEvaluate.split(Regex("(?<=[+\\-*/])|(?=[+\\-*/])")).toMutableList()
                var i = 0
                while (i < parts.size) {
                    val part = parts[i]
                    if (part.endsWith("%")) {
                        val num = part.dropLast(1).toDouble()
                        if (i > 0) {
                            val prevOperator = parts[i - 1]
                            val prevNumberStr = if (i - 2 >= 0) parts[i - 2] else ""
                            val prevNumber = prevNumberStr.toDoubleOrNull()

                            if (prevNumber != null) {
                                when (prevOperator) {
                                    "+", "-" -> {
                                        val percentageValue = (prevNumber * num) / 100
                                        parts[i] = percentageValue.toString()
                                    }
                                    "*", "/" -> {
                                        val percentageValue = (num / 100)
                                        parts[i] = percentageValue.toString()
                                    }
                                }
                            } else {
                                parts[i] = (num / 100).toString()
                            }
                        } else {
                            parts[i] = (num / 100).toString()
                        }
                    }
                    i++
                }
                expressionToEvaluate = parts.joinToString("")
            }

            val result = eval(expressionToEvaluate)

            textExpression.text = currentExpression.trim()
            val formattedResult = if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                String.format("%.10f", result).trimEnd('0').trimEnd('.')
            }

            textResult.text = formattedResult

            currentExpression = formattedResult

        } catch (e: DivisionByZeroException) {
            Toast.makeText(this, "Can't divide by zero.", Toast.LENGTH_LONG).show()
            textResult.text = "0"
            currentExpression = ""
        } catch (e: Exception) {
            textResult.text = "Error"
            currentExpression = ""
            e.printStackTrace()
        }
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm()
                    else if (eat('-'.toInt())) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt())) x *= parseFactor()
                    else if (eat('/'.toInt())) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) {
                            throw DivisionByZeroException()
                        }
                        x /= divisor
                    }
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor()
                if (eat('-'.toInt())) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.toInt())) {
                    x = parseExpression()
                    eat(')'.toInt())
                } else if ((ch in '0'.toInt()..'9'.toInt()) || ch == '.'.toInt()) {
                    while ((ch in '0'.toInt()..'9'.toInt()) || ch == '.'.toInt()) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }

    class DivisionByZeroException : RuntimeException("Division by zero is not allowed.")
}
package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import java.util.Stack

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
                if (currentExpression.isNotEmpty() && isLastCharOperator(currentExpression)) {
                    currentExpression = currentExpression.dropLast(1) + (it as Button).text.toString()
                } else if (currentExpression.isNotEmpty() || (it as Button).text.toString() == "-") {
                    appendToExpression((it as Button).text.toString())
                }
            }
        }

        findViewById<Button>(R.id.button_percent).setOnClickListener {
            // معالجة زر النسبة المئوية بشكل خاص
            handlePercent()
        }

        findViewById<Button>(R.id.button_decimal).setOnClickListener {
            if (isNewCalculation) {
                currentExpression = "0"
                isNewCalculation = false
            }
            // التأكد من عدم إضافة نقطة عشرية إذا كان الرقم الحالي يحتوي عليها بالفعل
            val parts = currentExpression.split('+', '-', 'x', '÷', '%')
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

    private fun isLastCharOperator(expression: String): Boolean {
        if (expression.isEmpty()) return false
        val lastChar = expression.last()
        return lastChar == '+' || lastChar == '-' || lastChar == 'x' || lastChar == '÷'
    }

    private fun appendToExpression(value: String) {
        if (isNewCalculation && value.matches(Regex("[0-9.]"))) {
            currentExpression = value
            isNewCalculation = false
        } else if (currentExpression == "0" && value != ".") {
            currentExpression = value
        } else {
            currentExpression += value
        }
        textResult.text = currentExpression
    }

    private fun clearAll() {
        currentExpression = ""
        textExpression.text = ""
        textResult.text = "0"
        isNewCalculation = true
    }

    private fun backspace() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)
            textResult.text = if (currentExpression.isEmpty()) "0" else currentExpression
            if (currentExpression.isEmpty()) {
                isNewCalculation = true
            }
        } else {
            textResult.text = "0"
        }
    }

    private fun toggleSign() {
        if (currentExpression.isNotEmpty() && currentExpression != "0" && currentExpression != "Error") {
            val operators = setOf('+', '-', 'x', '÷')
            var lastNumberStart = -1

            for (i in currentExpression.length - 1 downTo 0) {
                if (currentExpression[i] in operators) {
                    lastNumberStart = i + 1
                    break
                }
            }

            if (lastNumberStart == -1) {
                lastNumberStart = 0
            }

            val lastNumberStr = currentExpression.substring(lastNumberStart)

            if (lastNumberStr.isNotEmpty()) {
                val newNumberStr = if (lastNumberStr.startsWith("-")) {
                    lastNumberStr.substring(1)
                } else {
                    "-$lastNumberStr"
                }
                currentExpression = currentExpression.substring(0, lastNumberStart) + newNumberStr
                textResult.text = currentExpression
            }
        }
    }

    private fun handlePercent() {
        if (currentExpression.isEmpty()) return

        val operators = setOf('+', '-', 'x', '÷')
        var lastOperatorIndex = -1

        for (i in currentExpression.length - 1 downTo 0) {
            if (currentExpression[i] in operators) {
                lastOperatorIndex = i
                break
            }
        }

        if (lastOperatorIndex == -1) { // لا يوجد عامل حسابي، الرقم كله نسبة مئوية
            try {
                val number = currentExpression.toDouble()
                currentExpression = (number / 100).toString()
            } catch (e: NumberFormatException) {
                textResult.text = "Error"
                currentExpression = ""
            }
        } else { // يوجد عامل حسابي
            val lastNumberStr = currentExpression.substring(lastOperatorIndex + 1)
            try {
                val lastNumber = lastNumberStr.toDouble()
                val prevExpression = currentExpression.substring(0, lastOperatorIndex)
                val prevResult = eval(prevExpression.replace("x", "*").replace("÷", "/")) // نحتاج لحساب الجزء السابق
                val percentageValue = (prevResult * lastNumber) / 100

                // استبدال الجزء الأخير من التعبير بالقيمة الناتجة عن النسبة المئوية
                val operator = currentExpression[lastOperatorIndex]
                if (operator == '+' || operator == '-') {
                    currentExpression = prevExpression + operator + percentageValue
                } else { // للضرب والقسمة
                    currentExpression = prevExpression + operator + (lastNumber / 100)
                }

            } catch (e: Exception) {
                textResult.text = "Error"
                currentExpression = ""
            }
        }
        textResult.text = currentExpression
    }


    private fun calculateResult() {
        if (currentExpression.isEmpty()) {
            return
        }
        try {
            if (isLastCharOperator(currentExpression)) {
                currentExpression = currentExpression.dropLast(1)
            }

            val replaced = currentExpression
                .replace("x", "*")
                .replace("÷", "/")
            // تم حذف معالجة النسبة المئوية هنا، لأنها ستتم في handlePercent()
            // .replace("%", "/100")

            val result = eval(replaced)

            // انقل التعبير الأصلي إلى textExpression
            textExpression.text = currentExpression

            val formattedResult = if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                String.format("%.10f", result).trimEnd('0').trimEnd('.')
            }

            // عرض النتيجة في textResult (التكست الكبير)
            textResult.text = formattedResult

            currentExpression = formattedResult

        } catch (e: DivisionByZeroException) {
            Toast.makeText(this, "مأخدتش في الكلاس ان القسمة علي الصفر متنفعش 😂", Toast.LENGTH_LONG).show()
            textResult.text = "Undefined"
            currentExpression = ""
        } catch (e: Exception) {
            textResult.text = "Error"
            currentExpression = ""
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
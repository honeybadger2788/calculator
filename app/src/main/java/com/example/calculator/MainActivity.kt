package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.calculator.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    fun onClickButton( view:View){
        val valueStr = (view as Button).text.toString()

        when(view.id){
            R.id.btnDelete -> {
                val length = binding.tvOperation.text.length
                // para que no se detenga la app una vez borrado lo que habia en pantalla
                if(length > 0) {
                    val newOperation =  binding.tvOperation.text.toString().substring(0, length-1)
                    binding.tvOperation.text = newOperation
                }
            }
            R.id.btnClear -> {
                binding.tvOperation.text = ""
                binding.tvResult.text = ""
            }
            R.id.btnResolve -> {
                tryResolve(binding.tvOperation.text.toString())
            }
            else -> {
                // para que agregue un caracter atrás de otro
                binding.tvOperation.append(valueStr)
            }
        }
    }

    private fun tryResolve(operationRef: String) {
        val operator = getOperator(operationRef)

        var values = arrayOfNulls<String>(0)

        if(operator != OPERATOR_NULL) {
            // mas validaciones para el caso de suma de dos numeros negativos
            if(operator == OPERATOR_SUB){
                val index = operationRef.lastIndexOf(OPERATOR_SUB)
                if(index < operationRef.length-1){
                    values = arrayOfNulls(2)
                    values[0] = operationRef.substring(0,index)
                    values[1] = operationRef.substring(index+1)
                } else {
                    values = arrayOfNulls(1)
                    values[0] = operationRef.substring(0,index)
                }
            } else {
                values = operationRef.split(operator).toTypedArray()
            }
        }

        val numberOne = values[0]!!.toDouble()
        val numberTwo = values[1]!!.toDouble()

        binding.tvResult.text = getResult(numberOne,numberTwo,operator).toString()
    }

    private fun getOperator(operation: String): String {
        // variable de scope local
        var operator = ""

        if(operation.contains(OPERATOR_MULTI)){
            operator = OPERATOR_MULTI
        } else if (operation.contains(OPERATOR_DIV)){
            operator = OPERATOR_DIV
        } else if (operation.contains(OPERATOR_SUM)){
            operator = OPERATOR_SUM
        } else {
            operator = OPERATOR_NULL
        }

        // para evitar que la app crashee al intentar sumar dos numeros negativos
        if(operator == OPERATOR_NULL && operation.lastIndexOf(OPERATOR_SUB) > 0){
            operator = OPERATOR_SUB
        }
        return operator
    }

    private fun getResult(numberOne: Double, numberTwo: Double, operator: String): Double {
        var result = 0.0

        when(operator){
            OPERATOR_MULTI -> result = numberOne * numberTwo
            OPERATOR_DIV -> result = numberOne / numberTwo
            OPERATOR_SUM -> result = numberOne + numberTwo
            OPERATOR_SUB -> result = numberOne - numberTwo
        }
        return result
    }

    companion object {
        const val OPERATOR_MULTI = "*"
        const val OPERATOR_DIV= "÷"
        const val OPERATOR_SUM = "+"
        const val OPERATOR_SUB = "-"
        const val OPERATOR_NULL = "null"
        const val POINT = "."
    }
}
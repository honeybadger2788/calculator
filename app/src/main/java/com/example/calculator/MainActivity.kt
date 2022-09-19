package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import com.example.calculator.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // muestra lo que está sucediendo en tiempo real en el tvOperation
        binding.tvOperation.addTextChangedListener { charSequence ->
            if(canReplaceOperator(charSequence.toString())){
                val lenght = binding.tvOperation.text.length
                val newOperation = binding.tvOperation.text.toString().substring(0,lenght-2) +
                        binding.tvOperation.text.toString().substring(lenght-1)
                binding.tvOperation.text = newOperation
            }
        }
    }

    private fun canReplaceOperator(charSequence: CharSequence): Boolean {
        if(charSequence.length < 2) return false

        val lastElement = charSequence[charSequence.length -1].toString()
        val penultimateElement = charSequence[charSequence.length -2].toString()
        return (lastElement == OPERATOR_MULTI ||
                lastElement == OPERATOR_DIV ||
                lastElement == OPERATOR_SUM) &&
                (penultimateElement == OPERATOR_MULTI ||
                        penultimateElement == OPERATOR_DIV ||
                        penultimateElement == OPERATOR_SUM||
                        penultimateElement == OPERATOR_SUB)
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
                tryResolve(binding.tvOperation.text.toString(), true)
            }
            R.id.btnMulti,
            R.id.btnDiv,
            R.id.btnSub,
            R.id.btnSum -> {
                tryResolve(binding.tvOperation.text.toString(), false)

                val operator = valueStr
                val operation = binding.tvOperation.text.toString()
                addOperator(operator, operation)
            }
            R.id.btnPoint -> {
                val operation = binding.tvOperation.text.toString()
                addPoint(valueStr,operation)
            }
            else -> {
                // para que agregue un caracter atrás de otro
                binding.tvOperation.append(valueStr)
            }
        }
    }


    //valida las operaciones para evitar errores al añadir puntos
    private fun addPoint(pointStr: String, operation: String) {
        if(!operation.contains(POINT)){
            binding.tvOperation.append(pointStr)
        } else {
            val operator = getOperator(operation)
            var values = arrayOfNulls<String>(0)

            if(operator != OPERATOR_NULL) {
                if(operator == OPERATOR_SUB){
                    val index = operation.lastIndexOf(OPERATOR_SUB)
                    if(index < operation.length-1){
                        values = arrayOfNulls(2)
                        values[0] = operation.substring(0,index)
                        values[1] = operation.substring(index+1)
                    } else {
                        values = arrayOfNulls(1)
                        values[0] = operation.substring(0,index)
                    }
                } else {
                    values = operation.split(operator).toTypedArray()
                }
            }

            // permite agregar puntos en ambos parametros de la operacion
            if(values.size > 0) {
                val numberOne = values[0]!!
                if(values.size > 1) {
                    val numberTwo = values[1]!!
                    if(numberOne.contains(POINT) && !numberTwo.contains(POINT)){
                        binding.tvOperation.append(pointStr)
                    }
                } else {
                    if(numberOne.contains(POINT)){
                        binding.tvOperation.append(pointStr)
                    }
                }
            }
        }
    }

    private fun addOperator(operator: String, operation: String) {
        val lastElement = if(operation.isEmpty()) ""
        else operation.substring(operation.length-1)

        if (operator == OPERATOR_SUB) {
            if (operation.isEmpty() || lastElement != OPERATOR_SUB && lastElement != POINT){
                binding.tvOperation.append(operator)
            }
        } else {
            // evita operaciones incompletas
            if (!operation.isEmpty() && lastElement!= POINT){
                binding.tvOperation.append(operator)
            }
        }
    }

    private fun tryResolve(operationRef: String, isFromResolve: Boolean) {
        // para que no se ejecute la función si no hay nada escrito
        if(operationRef.isEmpty()) return

        // internamente trimea si se escribe un punto al final
        var operation = operationRef
        if(operation.contains(POINT) && operation.lastIndexOf(POINT) == operation.length -1){
            operation = operation.substring(0,operation.length-1)
        }
        
        val operator = getOperator(operation)

        var values = arrayOfNulls<String>(0)

        if(operator != OPERATOR_NULL) {
            // mas validaciones para el caso de suma de dos numeros negativos
            if(operator == OPERATOR_SUB){
                val index = operation.lastIndexOf(OPERATOR_SUB)
                if(index < operation.length-1){
                    values = arrayOfNulls(2)
                    values[0] = operation.substring(0,index)
                    values[1] = operation.substring(index+1)
                } else {
                    values = arrayOfNulls(1)
                    values[0] = operationRef.substring(0,index)
                }
            } else {
                values = operation.split(operator).toTypedArray()
            }
        }
        // evita que la app crashee al ingresar una operación incompleta
        if(values.size > 1) {
            try {
                val numberOne = values[0]!!.toDouble()
                val numberTwo = values[1]!!.toDouble()

                binding.tvResult.text = getResult(numberOne,numberTwo,operator).toString()

                // muestra el resultado parcial en el tvOperation
                if(binding.tvResult.text.isNotEmpty() && !isFromResolve){
                    binding.tvOperation.text = binding.tvResult.text
                }
            } catch (e: NumberFormatException){
                if(isFromResolve) showMessage("La expresión es incorrecta")
            }

        } else {
            // filtra que el mensaje no se muestre si se ingresan unicamente números
            if(isFromResolve && operator != OPERATOR_NULL) showMessage("La expresión es incorrecta")
        }

    }

    private fun getOperator(operation: String): String {
        // variable de scope local
        var operator: String

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

    private fun showMessage(msg: String){
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.llTop)
            .show()
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
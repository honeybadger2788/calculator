package com.example.calculator


/*Aquí irán todas las funciones que no afectan a la vista sino que son de apoyo*/
class Operations {
    companion object {
        fun getOperator(operation: String): String {
            // variable de scope local
            var operator = if(operation.contains(Constantes.OPERATOR_MULTI)){
                Constantes.OPERATOR_MULTI
            } else if (operation.contains(Constantes.OPERATOR_DIV)){
                Constantes.OPERATOR_DIV
            } else if (operation.contains(Constantes.OPERATOR_SUM)){
                Constantes.OPERATOR_SUM
            } else {
                Constantes.OPERATOR_NULL
            }

            // para evitar que la app crashee al intentar sumar dos numeros negativos
            if(operator == Constantes.OPERATOR_NULL && operation.lastIndexOf(Constantes.OPERATOR_SUB) > 0){
                operator = Constantes.OPERATOR_SUB
            }
            return operator
        }

        fun canReplaceOperator(charSequence: CharSequence): Boolean {
            if(charSequence.length < 2) return false

            val lastElement = charSequence[charSequence.length -1].toString()
            val penultimateElement = charSequence[charSequence.length -2].toString()
            return (lastElement == Constantes.OPERATOR_MULTI ||
                    lastElement == Constantes.OPERATOR_DIV ||
                    lastElement == Constantes.OPERATOR_SUM) &&
                    (penultimateElement == Constantes.OPERATOR_MULTI ||
                            penultimateElement == Constantes.OPERATOR_DIV ||
                            penultimateElement == Constantes.OPERATOR_SUM ||
                            penultimateElement == Constantes.OPERATOR_SUB)
        }

        fun tryResolve(operationRef: String, isFromResolve: Boolean, listener: OnResolveListener) {
            // para que no se ejecute la función si no hay nada escrito
            if(operationRef.isEmpty()) return

            // internamente trimea si se escribe un punto al final
            var operation = operationRef
            if(operation.contains(Constantes.POINT) && operation.lastIndexOf(Constantes.POINT) == operation.length -1){
                operation = operation.substring(0,operation.length-1)
            }

            val operator = getOperator(operation)

            var values = divideOperation(operator, operation)

            // evita que la app crashee al ingresar una operación incompleta
            if(values.size > 1) {
                try {
                    val numberOne = values[0]!!.toDouble()
                    val numberTwo = values[1]!!.toDouble()

                    listener.onShowResult(getResult(numberOne,numberTwo,operator))
                } catch (e: NumberFormatException){
                    if(isFromResolve) listener.onShowMessage(R.string.message_num_incorrect)
                }

            } else {
                // filtra que el mensaje no se muestre si se ingresan unicamente números
                if(isFromResolve && operator != Constantes.OPERATOR_NULL)
                    listener.onShowMessage(R.string.message_exp_incorrect)
            }
        }

        private fun getResult(numberOne: Double, numberTwo: Double, operator: String): Double {
            return when(operator){
                Constantes.OPERATOR_MULTI -> numberOne * numberTwo
                Constantes.OPERATOR_DIV -> numberOne / numberTwo
                Constantes.OPERATOR_SUM -> numberOne + numberTwo
                else -> numberOne - numberTwo //Constantes.OPERATOR_SUB
            }
        }


        fun divideOperation(operator: String, operation: String): Array<String?> {
            var values = arrayOfNulls<String>(0)
            if(operator != Constantes.OPERATOR_NULL) {
                if(operator == Constantes.OPERATOR_SUB){
                    val index = operation.lastIndexOf(Constantes.OPERATOR_SUB)
                    if(index < operation.length-1){
                        values = arrayOfNulls(2)
                        values[0] = operation.substring(0,index)
                        values[1] = operation.substring(index+1)
                    } else {
                        values = arrayOfNulls(1)
                        values[0] = operation.substring(0,index)
                    }
                } else {
                    /*dropLastWhile permite escribir una condición para eliminar el último elemento
                    de un array*/
                    values = operation.split(operator).dropLastWhile { it == "" }.toTypedArray()
                }
            }
            return values
        }
    }
}
package vision.dev

class Logger {
    companion object {
        fun error(msg: String, e: Exception? = null) {
            System.err.println(String.format("%s%s", processException(e), msg))
        }

        fun debug(msg: String) {
            System.out.println(msg)
        }

        private fun processException(e: Exception?): String {
            if(e != null)
            {
                var top = e.stackTrace[0]
                return String.format("%s:%s %s.%s: ", top.fileName, top.lineNumber, top.className, top.methodName)
            }
            else return "";
        }
    }
}
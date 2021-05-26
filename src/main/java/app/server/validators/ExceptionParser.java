package app.server.validators;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ExceptionParser {

    private static final Map<String, ErrorCode> keyWords;

    static {
        keyWords = new HashMap<>();
        keyWords.put("INSERT .* нарушает ограничение внешнего ключа", ErrorCode.FK_INSERT_UPDATE_CONSTRAINT);
        keyWords.put("Unable to find .* with id", ErrorCode.FK_INSERT_UPDATE_CONSTRAINT);
        keyWords.put("всё ещё есть ссылки в таблице", ErrorCode.FK_DELETE_CONSTRAINT);
    }

    public static ErrorCode getCode(Exception exception) {
        while (exception != null) {
            ErrorCode code = parseException(exception);

            if (code != ErrorCode.UNKNOWN_ERROR) {
                return code;
            }
            exception = (Exception) exception.getCause();
        }

        return ErrorCode.UNKNOWN_ERROR;
    }

    private static ErrorCode parseException(Exception exception) {
        String message = exception.getMessage();
        Pattern pattern;
        for (Map.Entry<String, ErrorCode> entry: keyWords.entrySet()) {
            pattern = Pattern.compile(entry.getKey());
            if (pattern.matcher(message).find()) {
                return entry.getValue();
            }
        }

        return ErrorCode.UNKNOWN_ERROR;
    }
}

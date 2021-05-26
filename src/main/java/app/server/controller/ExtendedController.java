package app.server.controller;

import app.server.util.QueryData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ExtendedController<T, ID> extends IController<T, ID> {

    @ResponseBody
    List<T> getAll(@RequestBody(required = false) QueryData queryData,
                   HttpServletRequest request, HttpServletResponse response);

    @ResponseBody
    Long countAll(HttpServletRequest request, HttpServletResponse response);

}

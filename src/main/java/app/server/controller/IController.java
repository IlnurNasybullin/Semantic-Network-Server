package app.server.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface IController<T, ID> {

    @ResponseBody
    List<T> getAll(@RequestParam(required = false, defaultValue = "false") Boolean expand,
                               HttpServletRequest request, HttpServletResponse response);

    @ResponseBody
    T get(@PathVariable ID id, @RequestParam(required = false, defaultValue = "false") Boolean expand,
          HttpServletRequest request, HttpServletResponse response);

    @ResponseBody
    T post(@RequestBody T object, @RequestParam(required = false, defaultValue = "false") Boolean expand,
           HttpServletRequest request, HttpServletResponse response) throws IOException;

    T put(@RequestBody T object, @RequestParam(required = false, defaultValue = "false") Boolean expand,
          HttpServletRequest request, HttpServletResponse response);

    @ResponseBody
    T delete(@RequestParam ID id, @RequestParam(required = false, defaultValue = "false") Boolean expand,
             HttpServletRequest request, HttpServletResponse response);
}

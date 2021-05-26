package app.server.controller;

import app.server.domain.Language;
import app.server.service.DictionaryService;
import app.server.service.LanguageJPAService;
import app.server.util.FileFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/dictionary")
public class DictionaryController {

    private final DictionaryService service;
    private final LanguageJPAService languageJPAService;

    public DictionaryController(DictionaryService service, LanguageJPAService languageJPAService) {
        this.service = service;
        this.languageJPAService = languageJPAService;
    }

    @GetMapping("/formats")
    @ResponseBody
    public FileFormat[] fileFormats() {
        return FileFormat.values();
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> generate(@RequestParam("fromId") Integer fromId, @RequestParam("toId") Integer toId,
                                             @RequestParam("format") FileFormat fileFormat,
                                             @RequestParam(value = "fileName", required = false,
                                                     defaultValue = "dictionary") String fileName,
                                             HttpServletResponse response)
            throws IOException {
        Optional<Language> from = languageJPAService.get(fromId);
        if (from.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректный fromId!");
            return null;
        }

        Optional<Language> to = languageJPAService.get(toId);
        if (to.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректный fromId!");
            return null;
        }

        Resource resource = service.generate(from.get(), to.get(), fileFormat);
        if (fileName == null) {
            fileName = "dictionary";
        }
        fileName += "." + fileFormat.name();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"").body(resource);
    }
}

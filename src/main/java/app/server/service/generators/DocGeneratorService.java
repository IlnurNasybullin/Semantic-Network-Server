package app.server.service.generators;

import app.server.util.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface DocGeneratorService {
    ByteArrayOutputStream generate(String languageFrom, String languageTo, List<Result> results) throws IOException;
}

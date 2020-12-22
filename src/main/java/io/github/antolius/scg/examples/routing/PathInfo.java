package io.github.antolius.scg.examples.routing;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

public class PathInfo {

    public static final String KEY = PathInfo.class.getCanonicalName();

    private final PathPattern pattern;
    private final PathContainer path;

    public static PathInfo from(String path) {
        return new PathInfo(
                PathPatternParser.defaultInstance.parse(path),
                PathContainer.parsePath(path)
        );
    }

    private PathInfo(PathPattern pattern, PathContainer path) {
        this.pattern = pattern;
        this.path = path;
    }

    public PathPattern getPattern() {
        return pattern;
    }

    public PathContainer getPath() {
        return path;
    }
}

package learning.jakarta.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
public class WebPageTool {

    @SneakyThrows
    @Tool("Returns the content of a web page, given the URL")
    public String getWebPageContent(@P("URL of the page") String url) {
        log.info("Fetching content from URL: {}", url);
        Document jsoupDocument = Jsoup.connect(url).get();
        return jsoupDocument.body().text();
    }
}

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.io.File;

public class Controller {
   public static void main(String[] args) throws Exception {
      String crawlStorageFolder = "/data/crawl";
      int numberOfCrawlers = 5;
      int maxPages = 20000;
      int politeness = 100;
      int maxDepth = 16;
      CrawlConfig config = new CrawlConfig();
      config.setCrawlStorageFolder(crawlStorageFolder);
      config.setMaxPagesToFetch(maxPages);
      config.setPolitenessDelay(politeness);
      config.setMaxDepthOfCrawling(maxDepth);
      config.setIncludeBinaryContentInCrawling(true);
      /*
       * Instantiate the controller for this crawl.
       */
      PageFetcher pageFetcher = new PageFetcher(config);
      RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
      RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
      CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
      /*
       * For each crawl, you need to add some seed urls. These are the first
       * URLs that are fetched and then the crawler starts following links
       * which are found in these pages
       */
      controller.addSeed("https://www.usatoday.com/");
      /*
       * Start the crawl. This is a blocking operation, meaning that your code
       * will reach the line after this only when crawling is finished.
       */
      controller.start(MyCrawler.class, numberOfCrawlers);
   }
}

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import ucar.nc2.util.IO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

public class MyCrawler extends WebCrawler {
   // Exclude non HTML, doc, pdf, images
   private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|json|mp3|mp4|zip|gz|xml))$");

   /**
    * This method receives two parameters. The first parameter is the page
    * in which we have discovered this new url and the second parameter is
    * the new url. You should implement this function to specify whether
    * the given url should be crawled or not (based on your crawling logic).
    * In this example, we are instructing the crawler to ignore urls that
    * have css, js, git, ... extensions and to only accept urls that start
    * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
    * referringPage parameter to make the decision.
    */
   @Override
   public boolean shouldVisit(Page referringPage, WebURL url) {
      String href = url.getURL().toLowerCase();
      return !FILTERS.matcher(href).matches()
              && (href.startsWith("https://www.usatoday.com/") || href.startsWith("http://www.usatoday.com/"));
   }

   /**
    * This function is called when a page is fetched and ready
    * to be processed by your program.
    */
   @Override
   public void visit(Page page) {
      String url = page.getWebURL().getURL();
      int statusCode = page.getStatusCode();
      System.out.println("Status: " + statusCode);
      System.out.println("URL: " + url);

      if (page.getParseData() instanceof HtmlParseData) {
         HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
         String text = htmlParseData.getText();
         String html = htmlParseData.getHtml();
         String type = page.getContentType().split(";")[0];
         Set<WebURL> links = htmlParseData.getOutgoingUrls();
         System.out.println("Text length: " + text.length());
         System.out.println("Html length: " + html.length());
         System.out.println("Number of outgoing links: " + links.size());
         try {
            writeLine((url.replace(",", "-") + "," + html.length() + "," + links.size() + "," + type), "visit_usatoday.csv");
            writeOutURL(links, "urls_usatoday.csv");
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      if (page.getParseData() instanceof BinaryParseData) {
         // String extension = url.substring(url.lastIndexOf("."));
         String type = page.getContentType().split(";")[0];
         int contentLength = page.getContentData().length;
         try {
            writeLine((url.replace(",", "-") + "," + contentLength + "," + 0 + "," + type), "visit_usatoday.csv");
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * This function is called once the header of a page is fetched. It can be
    * overridden by sub-classes to perform custom logic for different status
    * codes. For example, 404 pages can be logged, etc.
    *
    * @param webUrl WebUrl containing the statusCode
    * @param statusCode Html Status Code number
    * @param statusDescription Html Status COde description
    */
   @Override
   public void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
      // Do nothing by default
      // Sub-classed can override this to add their custom functionality
      String url = webUrl.getURL();
      try {
         writeLine((url.replace(",", "-") + "," + statusCode), "fetch_usatoday.csv");
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Write Line helper function
    * @param str
    * @param filename
    * @throws IOException
    */
   private void writeLine(String str, String filename) throws IOException {
      FileWriter fw = new FileWriter(filename, true);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(str);
      bw.newLine();
      bw.close();
   }

   /**
    * Write URL lists helper function
    * @param links
    * @param filename
    * @throws IOException
    */
   private void writeOutURL(Set<WebURL> links, String filename) throws IOException {
      FileWriter fw = new FileWriter(filename, true);
      BufferedWriter bw = new BufferedWriter(fw);
      for (WebURL link: links) {
         String url = link.getURL();
         String point = "N_OK";
         if (url.startsWith("https://www.usatoday.com") || url.startsWith("https://usatoday.com")) {
            point = "OK";
         }

         bw.write(url.replace(",", "-") + "," + point);
         bw.newLine();
      }
      bw.close();
   }
}

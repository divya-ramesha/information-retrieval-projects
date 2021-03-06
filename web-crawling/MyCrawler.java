import java.util.regex.Pattern;
import java.io.*;
import java.util.*;

import org.apache.http.Header;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;




public class MyCrawler extends WebCrawler {
	
	private static final Pattern FILTERS = Pattern.compile(".*\\.(css|js|mp3|zip|gz)$");
	
	CrawlState crawlState;

    public BasicCrawler() {
        crawlState = new CrawlState();
    }
    
	  private static File storageFolder;

    public static void configure(String storageFolderName) {
        storageFolder = new File(storageFolderName);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }
	
	@Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String type="NOT_OK";
        if(href.contains("latimes.com")) {
        	type="OK";
        }
        crawlState.discoveredUrls.add(new UrlInfo(href, type));
        return !FILTERS.matcher(href).matches()&&href.startsWith("http://www.latimes.com/");
    }

	@Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();
        
        String contentType = page.getContentType().split(";")[0];
        ArrayList<String> outgoingUrls = new ArrayList<String>();
        
        UrlInfo urlInfo;
        if (contentType.equals("text/html")) { 
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                Set<WebURL> links = htmlParseData.getOutgoingUrls();
                for (WebURL link : links) {
                    outgoingUrls.add(link.getURL());
                }
                urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "text/html", ".html");
                crawlState.visitedUrls.add(urlInfo);
            } else {
                urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "text/html", ".html");
                crawlState.visitedUrls.add(urlInfo);
            }
        } else if (contentType.equals("application/msword")) { 
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/msword", ".doc");
            crawlState.visitedUrls.add(urlInfo);
        } else if (contentType.equals("application/pdf")) { 
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/pdf", ".pdf");
            crawlState.visitedUrls.add(urlInfo);
        } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
            crawlState.visitedUrls.add(urlInfo);
        } else if(contentType.contains("image/jpg")) {	
        	 urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/jpg", ".jpg");
        	 crawlState.visitedUrls.add(urlInfo);
        } else if(contentType.contains("image/png")) {
           urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/png", ".png");
        	 crawlState.visitedUrls.add(urlInfo);
        } else if(contentType.contains("image/jpeg")) {
        	 urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/jpeg", ".jpeg");
        	 crawlState.visitedUrls.add(urlInfo);
        } else if(contentType.contains("image/gif")) {
        	 urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/gif", ".gif");
        	 crawlState.visitedUrls.add(urlInfo);
        } else {
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "unknown", "");
            crawlState.visitedUrls.add(urlInfo);
        }
        if (!urlInfo.extension.equals("")) {
            String filename = storageFolder.getAbsolutePath() + "/" + urlInfo.hash + urlInfo.extension;
            try {
                Files.write(page.getContentData(), new File(filename));
            } catch (IOException iox) {
                System.out.println("Failed to write file: " + filename);
            }
        }
	   }
        @Override
        protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
            crawlState.attemptUrls.add(new UrlInfo(webUrl.getURL(), statusCode));
        }

        @Override
        public Object getMyLocalData() {
            return crawlState;
        }
    }

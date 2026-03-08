package xyz.ar06.disx.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
@Deprecated
public class DisxYoutubeTitleScraper {
    public static String getYouTubeVideoTitle(String videoId) {
        try {
            // Fetch and parse the HTML content of the YouTube video page
            Document doc = Jsoup.connect("http://www.youtube.com/watch?v=" + videoId).get();

            // Extract the video title from the <meta property="og:title"> tag
            Element titleElement = doc.selectFirst("meta[property=og:title]");
            if (titleElement != null) {
                return titleElement.attr("content");
            } else {
                throw new Exception("Video Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Video Not Found";
        }
    }
}

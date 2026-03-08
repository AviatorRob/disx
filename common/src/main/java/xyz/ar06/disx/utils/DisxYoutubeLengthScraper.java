package xyz.ar06.disx.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Deprecated
public class DisxYoutubeLengthScraper {

    // Method to convert ISO 8601 duration to seconds
    private static int convertDurationToSeconds(String isoDuration) {
        // Remove 'PT' and split by 'H', 'M', and 'S'
        String duration = isoDuration.replace("PT", "");
        int hours = 0, minutes = 0, seconds = 0;

        // Extract hours
        if (duration.contains("H")) {
            String[] parts = duration.split("H");
            hours = Integer.parseInt(parts[0]);
            duration = parts[1];
        }

        // Extract minutes
        if (duration.contains("M")) {
            String[] parts = duration.split("M");
            minutes = Integer.parseInt(parts[0]);
            duration = parts[1];
        }

        // Extract seconds
        if (duration.contains("S")) {
            seconds = Integer.parseInt(duration.split("S")[0]);
        }

        // Convert total time to seconds
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static int getYoutubeVideoLength(String videoId) {
        try {
            Document doc = Jsoup.connect("http://www.youtube.com/watch?v=" + videoId).get();

            Element meta = doc.select("meta[itemprop=duration]").first();
            if (meta != null) {
                String duration = meta.attr("content"); // Duration is in ISO 8601 format (e.g., PT4M20S)

                int seconds = convertDurationToSeconds(duration);
                return seconds;
            } else {
                throw new Exception("Video Duration not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}

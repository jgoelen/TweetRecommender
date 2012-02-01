import java.text.Normalizer.Form;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.internal.json.StatusJSONImpl;

/*
 * Store the home timeline of the authenticated user is CSV file.
 * The CSV has the following headers:
 *  id: unique id of the tweet
 * 	t: timestamp of the tweet
 *  user: the screen name of the user of the tweet
 * 	favorite: flag to indicate if I marked the tweet as a favorite
 * 	retweeted: lag to indicate if I re-tweeted the tweet
 * 	text: the text message of the tweet
 *	urls: the expanded urls used in the tweet (when multiple -> space separated)
 * 
 */

ConfigurationBuilder cb = new ConfigurationBuilder();

//generate your own tokes
cb.setDebugEnabled(true)
  .setOAuthConsumerKey("fill in your own key")
  .setOAuthConsumerSecret("fill in your own key")
  .setOAuthAccessToken("fill in your own key")
  .setOAuthAccessTokenSecret("fill in your own key");
TwitterFactory tf = new TwitterFactory(cb.build());
Twitter twitter = tf.getInstance();

User me  = twitter.verifyCredentials()

def myTimeLineCsv = new File("my_home_timeline")

//myTimeLineCsv.append("id;t;user;favorite;retweeted;text;urls\n")

def idSet = [] as Set

def totalCount = 0
def totalRetweets = 0
def totalFavorite = 0


def appendTweets = { page, tweets ->
	
	println "page $page has ${tweets.size()} tweets"
	
	tweets.each { Status s ->
		
		def id = s.getId()
		
		if( ! idSet.contains(id) ){
		
			idSet.add(id)
			def ts = String.format('%tF %<tT',  s.getCreatedAt() )
			def user = s.getUser().getScreenName()
			def fav = s.isFavorited()
			def retw =  s.isRetweetedByMe()
			def text =  s.getText().replaceAll("\n", " ").replaceAll(";", ",")
			def urls = s.getURLEntities().collect { it.getExpandedURL() }.join(" ")
		
			myTimeLineCsv.append("$id;$ts;$user;$fav;$retw;$text;$urls\n")
		
			totalCount++
			if(fav) totalFavorite++
			if(retw) totalRetweets++
		
		} else {
		
			println "skipped duplicate id=$id"
		
		}
		
	}
	
}

println "*** harvest my retweets"
for(page in 1..16) {
	ResponseList tweets = twitter.getRetweetedByMe(new Paging(page,100))
	if(tweets.isEmpty()){
		break;
	}
	//for some reason the retweetByMe flag is false -> force these tweets to be re-tweeted by me
	def retweets = tweets.collect{StatusJSONImpl s -> s.getRetweetedStatus() }.each{ StatusJSONImpl s -> s.wasRetweetedByMe = true}
	appendTweets(page,retweets)
	Thread.sleep(1000*10);		
}

println "totalCount=$totalCount"
println "totalFavorite=$totalFavorite"
println "totalRetweets=$totalRetweets"

println "*** harvest the tweets of my home timeline"
for(page in 1..16) {
	ResponseList tweets = twitter.getHomeTimeline( new Paging(page,100))
	if(tweets.isEmpty()){
		break;
	}
	appendTweets(page,tweets)
	//Throttle to avoid the fail whale
	Thread.sleep(1000*10);
}



println "totalCount=$totalCount"
println "totalFavorite=$totalFavorite"
println "totalRetweets=$totalRetweets"





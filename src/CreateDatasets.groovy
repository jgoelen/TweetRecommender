import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.apache.tika.parser.html.HtmlParser


def posLabel = "like"
def negLabel = "normal"

def arffHeader =	"@relation text\n"+
					"@attribute docs string\n"+
					"@attribute class {$posLabel,$negLabel}\n\n"+
					"@data\n"
					
					
									
def files = ["twt_1":new File("data/twt_1.arff"),"twt_2":new File("data/twt_2.arff"),"twt_3":new File("data/twt_3.arff")]
files.values()*.setText(arffHeader)

def cleanString = { s ->
	s.replaceAll("\\s+"," ").replaceAll("['\"]","")
}

def replaceAllHttp = { s ->
	s.replaceAll("http\\S+","#http_link")
}

def cleanStringAll = { s ->
	s.replaceAll("http\\S+","#http_link").replaceAll("\\p{Punct}"," ")
}


new File("my_home_timeline_and_pages_it3").each{ String line ->
	
	def fields = line.split(";")
	
	if(fields.size() > 2 ){
		
		def author = fields[2]
		def favorite = "true".equals(fields[3])
		def retweetedByMe = "true".equals(fields[4])
		def tweetText = replaceAllHttp( cleanString(fields[5]) )
		def label = favorite || retweetedByMe ? posLabel : negLabel
		
		files.twt_1.append("'$tweetText',$label\n")
			
		def textAndAuthor = author + " " + tweetText
		files.twt_2.append("'$textAndAuthor',$label\n")
	
		def textAndAuthorAndHtml = textAndAuthor + " " + cleanStringAll(fields[-1])
		files.twt_3.append("'$textAndAuthorAndHtml',$label\n")
	}
			
}





